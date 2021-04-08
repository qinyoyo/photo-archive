package qinyoyo.photoinfo.archive;


import lombok.Getter;
import lombok.Setter;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.FileUtil;
import qinyoyo.utils.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Setter
@Getter
public class FolderInfo {
	public static final String DEFPATH = "Camera";
	private String path;
	private String name;
	private int priority;
	private String camera;
	private Date date0;
	private Date date1;
	private Double latitude0;
	private Double latitude1;
	private Double longitude0;
	private Double longitude1;

	public FolderInfo(String absolutePath) {
		if (absolutePath.endsWith("\\")) this.path=absolutePath.substring(0,absolutePath.length()-1);
		else this.path=absolutePath;
		int p = this.path.lastIndexOf("\\");
		if (p>=0) name = this.path.substring(p+1);
		else name=this.path;
		camera = "Camera";
		priority = 100;
	}
	public FolderInfo(String propertyLines,String rootPath) {
		camera = "Camera";
		priority = 100;
		String [] lines = propertyLines.split("\n");
		for (int i=0;i<lines.length;i++) {
			String line = lines[i];
			int p = line.indexOf("#");
			if (p>=0) line = line.substring(0,p);
			line=line.trim();
			p=line.indexOf("=");
			if (p>0 && p<line.length()-1) {
				String label = line.substring(0,p).trim().toLowerCase(), value = line.substring(p+1).trim();
				switch (label) {
					case "path" :
						try { path = new File(rootPath,value).getCanonicalPath(); break; }
						catch (Exception e) { throw new RuntimeException(e.getMessage()); }
					case "name" : name = value; break;
					case "priority" : priority = Integer.parseInt(value); break;
					case "camera" : camera = value; break;
					case "date0" : date0 = DateUtil.string2Date(value,"yyyy-MM-dd"); break;
					case "date1" : date1 = DateUtil.string2Date(value,"yyyy-MM-dd"); break;
					case "longitude0" : longitude0 = Double.parseDouble(value); break;
					case "longitude1" : longitude1 = Double.parseDouble(value); break;
					case "latitude0" : latitude0 = Double.parseDouble(value); break;
					case "latitude1" : latitude1 = Double.parseDouble(value); break;
				}
			}
		}
	}
	public String writeToString(String rootPath) {
		if (path.toLowerCase().startsWith(rootPath.toLowerCase())) {
			StringBuilder sb = new StringBuilder();
			String s = path.substring(rootPath.length());
			if (s.startsWith("\\")) s=s.substring(1);
			sb.append("\tpath=").append(s).append("\r\n");
			if (name!=null && !name.isEmpty()) sb.append("\tname=").append(name).append("\r\n");
			sb.append("\tpriority=").append(priority).append("\r\n");
			if (camera!=null && !camera.isEmpty()) sb.append("\tcamera=").append(camera).append("\r\n");
			if (date0!=null) sb.append("\tdate0=").append(DateUtil.date2String(date0,"yyyy-MM-dd")).append("\r\n");
			if (date1!=null) sb.append("\tdate1=").append(DateUtil.date2String(date1,"yyyy-MM-dd")).append("\r\n");
			if (longitude0!=null) sb.append("\tlongitude0=").append(longitude0).append("\r\n");
			if (longitude1!=null) sb.append("\tlongitude1=").append(longitude1).append("\r\n");
			if (latitude0!=null) sb.append("\tlatitude0=").append(latitude0).append("\r\n");
			if (latitude1!=null) sb.append("\tlatitude1=").append(latitude1).append("\r\n");
			return sb.toString();
		} else return null;
	}
	public int compareTo(FolderInfo fi) {
		if (date1.getTime()<=fi.date0.getTime()) return -1;
		else if (date0.getTime()>=fi.date1.getTime()) return 1;
		else if (priority<fi.priority) return -1;
		else if (priority>fi.priority) return 1;
		else if (date0.getTime()<fi.date0.getTime()) return -1;
		else if (date0.getTime()>fi.date0.getTime()) return 1;
		else if (date1.getTime()<fi.date1.getTime()) return -1;
		else if (date1.getTime()>fi.date1.getTime()) return 1;
		else return path.compareTo(fi.path);
	}

	public static void saveFolderInfos(List<FolderInfo> infos, ArchiveInfo archived) {
		File f=new File(archived.getPath(),ArchiveUtils.folder_info_dat);
		String rootPath = archived.getPath();
		StringBuilder sb=new StringBuilder();
		for (FolderInfo fi : infos) {
			sb.append("info:\r\n").append(fi.writeToString(rootPath));
		}
		FileUtil.writeToFile(f,sb.toString());
	}
	public static List<FolderInfo> getFolderInfos(ArchiveInfo archived) {
		File f=new File(archived.getPath(),ArchiveUtils.folder_info_dat);
		String rootPath = archived.getPath();
		List<FolderInfo> list = new ArrayList<>();
		if (f.exists()) {
			String content = FileUtil.getFromFile(f);
			if (content!=null) {
				String[] segs = content.split("info:");
				if (segs.length > 0) {
					for (String seg : segs) {
						seg=seg.trim();
						if (seg.isEmpty()) continue;
						FolderInfo fi = new FolderInfo(seg, rootPath);
						list.add(fi);
					}
					return list;
				}
			}
		}
		List<PhotoInfo> infos = archived.getInfos();
		infos.sort((a,b)->{
			return a.fullPath("").compareTo(b.fullPath(""));
		});
		FolderInfo fi = null;
		String fiPath="", pathYear=null;
		for (int i=0;i<infos.size();i++) {
			PhotoInfo pi = infos.get(i);
			String path = pi.getSubFolder();
			if (path.indexOf("扫描")>=0) continue;
			if (path.startsWith("\\")) path=path.substring(1);
			if (path.endsWith("\\")) path=path.substring(0,path.length()-1);
			String [] ff = path.split("\\\\",-1);
			if (pathYear==null || !pathYear.equals(ff[0])) {
				pathYear=ff[0];
				Date dt0=null,dt1=null;
				try {
					if (ff[0].length() == 4) {
						dt0 = new SimpleDateFormat("yyyy-MM-dd").parse(ff[0] + "-01-01");
						dt1 = new SimpleDateFormat("yyyy-MM-dd").parse(ff[0] + "-12-31");
					} else {
						String [] yy = ff[0].split("-",-1);
						if (yy.length==2) {
							dt0 = new SimpleDateFormat("yyyy-MM-dd").parse(yy[0].trim() + "-01-01");
							dt1 = new SimpleDateFormat("yyyy-MM-dd").parse(yy[1].trim() + "-12-31");
						}
					}
					if (dt0!=null && dt1!=null) {
						FolderInfo yfi = new FolderInfo(rootPath+"\\"+pathYear);
						yfi.setDate0(dt0);
						yfi.setDate1(dt1);
						yfi.setPriority(2000);
						list.add(yfi);
					}
				} catch (Exception e){ Util.printStackTrace(e);}
			}
			if (ff.length<2) continue;
			if (fi==null || !fiPath.toLowerCase().equals((ff[0]+"\\"+ff[1]).toLowerCase())) {
				fiPath = ff[0]+"\\"+ff[1];
				fi = new FolderInfo(rootPath+"\\"+fiPath);
				if (ff[1].endsWith(" 生活") || ff[1].toLowerCase().endsWith(" life") || ff[1].endsWith(" 重庆")) {
					if (ff[1].length()==7) {
						fi.setDate0(DateUtil.string2Date(ff[1].substring(0,4)+"-01-01","yyyy-MM-dd"));
						fi.setDate1(DateUtil.string2Date(ff[1].substring(0,4)+"-12-31","yyyy-MM-dd"));
						fi.setPriority(1000);
					}
					if (ff[1].length()==9) {
						int m=Integer.parseInt(ff[1].substring(4,6));
						String md = (m==2 ? "28" : (m==4 || m==6 || m==9 || m==11 ? "30" : "31"));
						fi.setDate0(DateUtil.string2Date(ff[1].substring(0,6)+"-01","yyyyMM-dd"));
						fi.setDate1(DateUtil.string2Date(ff[1].substring(0,6)+"-"+md,"yyyyMM-dd"));
						fi.setPriority(500);
					}
				}
				list.add(fi);
			}
			Date sdt = pi.getShootTime();
			if (sdt!=null) {
				if (fi.getDate0() == null || fi.getDate0().getTime()>sdt.getTime()) fi.setDate0(DateUtil.dayOf(sdt));
				if (fi.getDate1() == null || fi.getDate1().getTime()<sdt.getTime()) fi.setDate1(DateUtil.nextDayOf(sdt));
			}
			Double lat = pi.getLatitude();
			if (lat!=null) {
				if (fi.getLatitude0() == null || fi.getLatitude0()>lat) fi.setLatitude0(lat);
				if (fi.getLatitude1() == null || fi.getLatitude1()<lat) fi.setLatitude1(lat);
			}
			Double lon = pi.getLongitude();
			if (lon!=null) {
				if (fi.getLongitude0() == null || fi.getLongitude0()>lon) fi.setLongitude0(lon);
				if (fi.getLongitude1() == null || fi.getLongitude1()<lon) fi.setLongitude1(lon);
			}
		}
		return list;
	}
	public static FolderInfo findFolder(Date dt, List<FolderInfo> folderInfos) {
		for (FolderInfo fi: folderInfos) {
			if (fi.getDate0().getTime()<=dt.getTime() && fi.getDate1().getTime()>=dt.getTime()) return fi;
		}
		return null;
	}
	public static List<FolderInfo> scanFolderInfo(ArchiveInfo archived) {
		System.out.println("扫描目录信息: "+archived.getPath());
		List<FolderInfo> folderInfos = getFolderInfos(archived);
		StringBuilder sb=new StringBuilder();

		Iterator<FolderInfo> iter = folderInfos.iterator();
		while (iter.hasNext()) {
			FolderInfo fi = iter.next();
			if (fi.getDate0()==null) {
				System.out.println(fi.getPath() + " 缺少开始日期");
				sb.append(fi.getPath()).append("\r\n");
				iter.remove();
			} else if (fi.getDate1()==null) {
				System.out.println(fi.getPath() + " 缺少结束日期");
				sb.append(fi.getPath()).append("\r\n");
				iter.remove();
			}
		}
		String logmsg=sb.toString();
		if (!logmsg.isEmpty()) {
			FileUtil.writeToFile(new File(archived.getPath(),ArchiveUtils.folder_info_lost_log),logmsg);
		}
		if (folderInfos.size()>0) {
			folderInfos.sort((a, b) -> a.compareTo(b));
			saveFolderInfos(folderInfos, archived);
		}
		return folderInfos;
	}

}
