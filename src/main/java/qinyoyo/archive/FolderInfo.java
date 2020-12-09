package qinyoyo.archive;

import qinyoyo.Utils;

import java.io.File;
import java.util.Date;

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getCamera() {
		return camera;
	}

	public void setCamera(String camera) {
		this.camera = camera;
	}

	public Date getDate0() {
		return date0;
	}

	public void setDate0(Date date0) {
		this.date0 = date0;
	}

	public Date getDate1() {
		return date1;
	}

	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	public Double getLatitude0() {
		return latitude0;
	}

	public void setLatitude0(Double latitude0) {
		this.latitude0 = latitude0;
	}

	public Double getLatitude1() {
		return latitude1;
	}

	public void setLatitude1(Double latitude1) {
		this.latitude1 = latitude1;
	}

	public Double getLongitude0() {
		return longitude0;
	}

	public void setLongitude0(Double longitude0) {
		this.longitude0 = longitude0;
	}

	public Double getLongitude1() {
		return longitude1;
	}

	public void setLongitude1(Double longitude1) {
		this.longitude1 = longitude1;
	}

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
					case "path" : path = new File(rootPath,value).getAbsolutePath(); break;
					case "name" : name = value; break;
					case "priority" : priority = Integer.parseInt(value); break;
					case "camera" : camera = value; break;
					case "date0" : date0 = Utils.string2Date(value,"yyyy-MM-dd"); break;
					case "date1" : date1 = Utils.string2Date(value,"yyyy-MM-dd"); break;
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
			if (date0!=null) sb.append("\tdate0=").append(Utils.date2String(date0,"yyyy-MM-dd")).append("\r\n");
			if (date1!=null) sb.append("\tdate1=").append(Utils.date2String(date1,"yyyy-MM-dd")).append("\r\n");
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
}
