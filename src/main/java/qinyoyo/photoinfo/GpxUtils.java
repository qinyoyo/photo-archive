package qinyoyo.photoinfo;

import org.w3c.dom.*;
import qinyoyo.photoinfo.archive.PhotoInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import qinyoyo.photoinfo.exiftool.Key;
import qinyoyo.utils.DateUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GpxUtils {
    private static String utcTimeString(Date dt,final TimeZone zone) {
        TimeZone sysZone = TimeZone.getDefault();
        TimeZone utc = TimeZone.getTimeZone("UTC");
        if (zone==null || sysZone.hasSameRules(zone)) return DateUtil.date2String(dt,"yyyy-MM-dd HH:mm:ss",utc);
        else {
            int def  = sysZone.getRawOffset() - zone.getRawOffset();
            utc.setRawOffset(def);
            return DateUtil.date2String(dt,"yyyy-MM-dd HH:mm:ss",utc);
        }
    }
    private static String formatUtcDt(String dt) {
        if (dt!=null && dt.matches("(\\d{4}).(\\d{2}).(\\d{2}).(\\d{2})\\:(\\d{2})\\:(\\d{2}).*")) {
            return dt.substring(0,4) + "-" + dt.substring(5,7) + "-" + dt.substring(8,10) + " " + dt.substring(11,19);
        }
        return null;
    }
    private static String nullUseEmpty(String s) {
        return s==null?"":s;
    }
    public static int writeGpxInfo(File file, List<PhotoInfo> list, String title, final TimeZone zone) {
        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            int count = 0;
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element rootEle = dom.createElement("gpx");
            rootEle.setAttribute("xmlns","http://www.topografix.com/GPX/1/1");
            rootEle.setAttribute("creator","com.tang.photo-archive");
            rootEle.setAttribute("version","1.1");
            rootEle.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
            rootEle.setAttribute("xsi:schemaLocation","http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");

            Element meta = dom.createElement("metadataType");

            meta.appendChild(dom.createElement("name")).setTextContent("StepRecord");
            meta.appendChild(dom.createElement("author")).setTextContent("qinyoyo");
            meta.appendChild(dom.createElement("copyright")).setTextContent("reserved");
            meta.appendChild(dom.createElement("time")).setTextContent(utcTimeString(list.get(0).getShootTime(),zone));

            rootEle.appendChild(meta);
            Element trk = dom.createElement("trk");
            rootEle.appendChild(trk);
            if (title!=null) trk.appendChild(dom.createElement("name")).setTextContent(title);
            Element trkseg = dom.createElement("trkseg");
            for (PhotoInfo pi: list) {
                Element trkpt = dom.createElement("trkpt");
                if (pi.getLatitude()!=null) trkpt.setAttribute("lat",String.format("%.7f",pi.getLatitude()));
                if (pi.getLongitude()!=null) trkpt.setAttribute("lon",String.format("%.7f",pi.getLongitude()));
                if (pi.getAltitude()!=null) trkpt.appendChild(dom.createElement("ele")).setTextContent(String.format("%.7f",pi.getAltitude()));
                String dt = formatUtcDt(pi.getGpsDatetime());
                if (dt!=null) trkpt.appendChild(dom.createElement("time")).setTextContent(dt);
                else if (pi.getShootTime()!=null) trkpt.appendChild(dom.createElement("time")).setTextContent(utcTimeString(pi.getShootTime(),zone));
                if (pi.getSubjectCode()!=null) trkpt.appendChild(dom.createElement("step")).setTextContent(pi.getSubjectCode());
                if (pi.getCountry()!=null || pi.getProvince()!=null || pi.getCity()!=null || pi.getLocation()!=null) {
                    String desc = String.format("%s|%s|%s|%s",nullUseEmpty(pi.getCountry()),
                            nullUseEmpty(pi.getProvince()),nullUseEmpty(pi.getCity()),nullUseEmpty(pi.getLocation()));
                    trkpt.appendChild(dom.createElement("desc")).setTextContent(desc);
                }
                trkseg.appendChild(trkpt);
                count++;
            }
            trk.appendChild(trkseg);
            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(file)));
                return count;
            } catch (Exception te) {
                System.out.println(te.getMessage());
            }
        } catch (Exception pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
        return 0;
    }
    public static TreeMap<Long, Map<String,Object>> readGpxInfo(File file,String defTitle) {
        TreeMap<Long, Map<String,Object>> result = new TreeMap<>();
        if (!file.exists()) return result;
        if (file.isDirectory()) {
            File [] files = file.listFiles(f->f.isDirectory() || f.getName().endsWith(".gpx"));
            for (File f: files) {
                result.putAll(readGpxInfo(f, defTitle));
            }
            return result;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            NodeList meta = doc.getElementsByTagName("metadataType");
            String author = "Qinyoyo";
            if (meta.getLength()>0) {
                NodeList nodes = meta.item(0).getChildNodes();
                for (int i=0;i<nodes.getLength();i++) {
                    Node node=nodes.item(i);
                    if (node.getNodeName().equals("author")) {
                        author = node.getTextContent();
                        break;
                    }
                }
            }
            NodeList trkList = doc.getElementsByTagName("trk");
            for (int i=0;i<trkList.getLength();i++) {
                Node trk = trkList.item(i);
                NodeList cc = trk.getChildNodes();
                String title = defTitle;
                for(int j=0;j<cc.getLength();j++) {
                    Node node = cc.item(j);
                    if (node.getNodeName().equals("name")) {
                        title = node.getTextContent();
                        if (title==null || title.isEmpty()) title = defTitle;
                    }
                    else if (node.getNodeName().equals("trkseg")) {
                        NodeList trkptList = node.getChildNodes();
                        for (int k=0; k<trkptList.getLength(); k++) {
                            Node trkpt = trkptList.item(k);
                            double lat = 2000, lon = 2000;
                            Double ele=null;
                            String dt=null, step=null, country=null, province=null, city=null, location=null;
                            if (trkpt.getNodeName().equals("trkpt")) {
                                try {
                                    NamedNodeMap attrs = trkpt.getAttributes();
                                    for (int l = 0; l < attrs.getLength(); l++) {
                                        Node id = attrs.item(l);
                                        if (id.getNodeName().equals("lat")) lat = Double.parseDouble(id.getNodeValue());
                                        else if (id.getNodeName().equals("lon"))
                                            lon = Double.parseDouble(id.getNodeValue());
                                    }
                                    if (lat < 1000 && lon < 1000) {
                                        NodeList ptValue = trkpt.getChildNodes();
                                        for (int m = 0; m < ptValue.getLength(); m++) {
                                            Node value = ptValue.item(m);
                                            if (value.getNodeName().equals("step")) step = value.getTextContent();
                                            else if (value.getNodeName().equals("time"))
                                                dt = formatUtcDt(value.getTextContent());
                                            else if (value.getNodeName().equals("ele"))
                                                ele = Double.parseDouble(value.getTextContent());
                                            else if (value.getNodeName().equals("desc")) {
                                                String[] desc = value.getTextContent().split("\\|");
                                                if (desc.length > 0) country = desc[0];
                                                if (desc.length > 1) province = desc[1];
                                                if (desc.length > 2) city = desc[2];
                                                if (desc.length > 3) location = desc[3];
                                            }
                                        }
                                        if (dt != null) {
                                            Map<String, Object> map = new HashMap<>();
                                            if (title!=null && !title.isEmpty()) map.put(Key.getName(Key.HEADLINE),title);
                                            if (author!=null && !author.isEmpty()) map.put(Key.getName(Key.ARTIST),author);
                                            map.put(Key.getName(Key.GPS_LATITUDE), lat);
                                            map.put(Key.getName(Key.GPS_LONGITUDE), lon);
                                            if (ele != null) map.put(Key.getName(Key.GPS_ALTITUDE), ele);
                                            if (step != null) map.put(Key.getName(Key.SUBJECT_CODE), step);
                                            if (country != null) map.put(Key.getName(Key.COUNTRY), country);
                                            if (province != null) map.put(Key.getName(Key.STATE), province);
                                            if (city != null) map.put(Key.getName(Key.CITY), city);
                                            if (location != null) map.put(Key.getName(Key.LOCATION), location);
                                            map.put(Key.getName(Key.GPS_DATETIME), dt.substring(0,4)+":"+dt.substring(5,7)+":" + dt.substring(8) +"Z");
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                            result.put(sdf.parse(dt).getTime(), map);
                                        }
                                    }
                                } catch (Exception e1) { e1.printStackTrace();}
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
