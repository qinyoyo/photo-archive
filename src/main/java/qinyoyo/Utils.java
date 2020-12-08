package qinyoyo;

import qinyoyo.archive.ArchiveInfo;
import qinyoyo.archive.FolderInfo;
import qinyoyo.archive.PhotoInfo;
import qinyoyo.exiftool.CommandRunner;
import qinyoyo.exiftool.ExifTool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
	public static boolean equals(String s1, String s2) {
		if (s1 == null && s2 == null)
			return true;
		else if (s1 != null && s2 != null)
			return s1.toLowerCase().equals(s2.toLowerCase());
		else
			return false;
	}

	public static boolean equals(Date s1, Date s2) {
		if (s1 == null && s2 == null)
			return true;
		else if (s1 != null && s2 != null)
			return s1.getTime() == s2.getTime();
		else
			return false;
	}

	public static String getFromFile(File file) {
		try {
			FileInputStream s = new FileInputStream(file);
			InputStreamReader r = new InputStreamReader(s, "GBK");
			BufferedReader in = new BufferedReader(r);
			StringBuilder sb = new StringBuilder();
			String str;
			boolean firstLine = true;
			while ((str = in.readLine()) != null) {
				if (firstLine)
					firstLine = false;
				else
					sb.append("\r\n");
				sb.append(str);
			}
			return sb.toString();
		} catch (IOException e) {
			return null;
		}
	}

	public static void writeToFile(File file, String string) {
		try {
			FileOutputStream s = new FileOutputStream(file);
			OutputStreamWriter w = new OutputStreamWriter(s, "GBK");
			PrintWriter pw = new PrintWriter(w);
			pw.write(string);
			pw.flush();
			pw.close();
			w.close();
			s.close();
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}

	public static void appendToFile(File file, String string) {
		try {
			String s=getFromFile(file);
			if (s==null || s.isEmpty()) s=string;
			else s=s.trim()+"\r\n"+string;
			writeToFile(file,s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int contentCompare(String path1,String path2) {
		if (path1==null && path2==null) return 0;
		else if (path1==null) return -1;
		else if (path2==null) return 1;
		else {
			File file1=new File(path1), file2=new File(path2);
			if (!file1.exists() && !file2.exists()) return 0;
			else if (!file1.exists()) return -1;
			else if (!file2.exists()) return 1;
			else if (file1.length()<file2.length()) return -1;
			else if (file1.length()>file2.length()) return 1;
			else {
				FileInputStream in1=null,in2=null;
				try {
					in1=new FileInputStream(file1);
					in2=new FileInputStream(file2);
					byte[] buf1=new byte[102400],buf2 = new byte[102400];
					int len1=in1.read(buf1), len2=in2.read(buf2);
					do {
						if (len1<len2) return -1;
						else if (len1>len2) return 1;
						else if (!Arrays.equals(buf1,buf2)) return -1;
						len1=in1.read(buf1);
						len2=in2.read(buf2);
					} while (len1>0);
					return 0;
				} catch (Exception e) { return -1; }
				finally {
					if (in1!=null)
						try {
							in1.close();
						} catch (IOException e) {}
					if (in2!=null)
						try {
							in2.close();
						} catch (IOException e) {}				
					}
			}
		}
	}
	public static void saveObj(File file, Object object) {
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(object);

			objectOutputStream.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object readObj(File file) {
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			objectInputStream = new ObjectInputStream(fileInputStream);
			return objectInputStream.readObject();
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			if (objectInputStream != null)
				try {
					objectInputStream.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			if (fileInputStream != null)
				try {
					fileInputStream.close();
				} catch (IOException e) {
				}
		}
		return null;
	}

	public static String date2String(Date dt,String fmt) {
		if (dt == null) return null;
		return new SimpleDateFormat(fmt).format(dt);
	}
	public static String date2String(Date dt) {
		return date2String(dt,"yyyy-MM-dd HH:mm:ss");
	}

	public static Date dayOf(Date dt) {
		return string2Date(date2String(dt,"yyyy-MM-dd")+" 00:00:00","yyyy-MM-dd HH:mm:ss");
	}
	public static Date nextDayOf(Date dt) {
		return new Date(dayOf(dt).getTime()+24l*60*60*1000);
	}
	public static Date string2Date(String s, String fmt) {
		if (s == null)
			return null;
		try {
			return new SimpleDateFormat(fmt).parse(s);
		} catch (Exception e) {
			return null;
		}
	}

	public static Date string2Date(String s) {
		try {
			if (s == null || s.equals("-")) return null;
			Pattern p = Pattern.compile("(\\d{4})[^0-9](\\d{2})[^0-9](\\d{2})[^0-9](\\d{2})[^0-9](\\d{2})[^0-9](\\d{2})(\\.\\d+)?");
			Matcher m = p.matcher(s);
			if (m.find()) {
				String newS = String.format("%s-%s-%s %s:%s:%s", m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6));
				String ms = m.group(7);
				if (ms == null || ms.isEmpty()) ms = ".000";
				else if (ms.length() > 4) ms = ms.substring(0, 4);
				else while (ms.length() < 4) ms = ms + "0";
				if (m.end() == s.length()) return string2Date(newS + ms, "yyyy-MM-dd HH:mm:ss.SSS");
				else return string2Date(newS + ms + s.substring(m.end()), "yyyy-MM-dd HH:mm:ss.SSSz");
			} else return null;
		} catch (Exception e) {
			throw new RuntimeException(s + " 无法格式化");
		}
	}

	public static String nameWithoutExt(String name) {
		int p = name.lastIndexOf(".");
		if (p >= 0)
			return name.substring(0, p);
		else
			return name;
	}

	public static String extName(String name) {
		int p = name.lastIndexOf(".");
		if (p >= 0)
			return name.substring(p);
		else
			return "";
	}

	public static String fullPath(String root, PhotoInfo p) {
		String sub = p.getFolder();
		if (sub == null || sub.isEmpty()) {
			return new File(root, p.getFileName()).getAbsolutePath();
		} else
			return new File(new File(root, sub), p.getFileName()).getAbsolutePath();
	}

	public static String newFile(String root, PhotoInfo p) {
		String sub = p.getFolder();
		if (sub == null || sub.isEmpty() || sub.equals(".")) {
			return new File(root, p.getFileName()).getAbsolutePath();
		} else
			return new File(root, sub.replace("\\", " ") + p.getFileName()).getAbsolutePath();
	}

	public static <T> void removeAll(List<T> all, List<T> rm) {
		Iterator iter = all.iterator();
		while (iter.hasNext()) {
			if (rm.contains(iter.next())) {
				iter.remove();
			}
		}
		SystemOut.println("剩余照片数量 : " + all.size());
		;
	}

	public static Date getShootTimeFromFileName(String fileName) {
		try {
			Pattern p = Pattern.compile("(\\d{15})");
			Matcher m = p.matcher(fileName);
			if (m.find()) {
				return null;
			}
			p = Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})[^0-9]{0,2}(\\d{2})[^0-9]?(\\d{2})[^0-9]?(\\d{2})");
			m = p.matcher(fileName);
			if (m.find()) {
				String s=String.format("%s-%s-%s %s:%s:%s",m.group(1),m.group(2),m.group(3),m.group(4),m.group(5),m.group(6));
				return string2Date(s,"yyyy-MM-dd HH:mm:ss");
			}
			p=Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})[^0-9]{0,2}(\\d{2})[^0-9]?(\\d{2})");
			m = p.matcher(fileName);
			if (m.find()) {
				String s=String.format("%s-%s-%s %s:%s:00",m.group(1),m.group(2),m.group(3),m.group(4),m.group(5));
				return string2Date(s,"yyyy-MM-dd HH:mm:ss");
			}
			p=Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})[^0-9]{0,2}(\\d{2})");
			m = p.matcher(fileName);
			if (m.find()) {
				String s=String.format("%s-%s-%s %s:00:00",m.group(1),m.group(2),m.group(3),m.group(4));
				return string2Date(s,"yyyy-MM-dd HH:mm:ss");
			}
			p=Pattern.compile("(\\d{4})[^0-9]?(\\d{2})[^0-9]?(\\d{2})");
			m = p.matcher(fileName);
			if (m.find()) {
				String s=String.format("%s-%s-%s 12:00:00",m.group(1),m.group(2),m.group(3));
				return string2Date(s,"yyyy-MM-dd HH:mm:ss");
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	static void doDeleteSameFiles(List<PhotoInfo> rm,List<PhotoInfo> sameAs,ArchiveInfo archiveInfo, ArchiveInfo ref) {
		if (rm.size() > 0) {
			List<PhotoInfo> all = archiveInfo.getInfos();
			File logFile = new File(archiveInfo.getPath(), "same_photo.log");
			SystemOut.println("重复照片数量 : " + rm.size());
			File rmf = new File(new File(archiveInfo.getPath()), ".delete");
			rmf.mkdirs();
			ArchiveInfo rma = new ArchiveInfo();
			rma.setPath(rmf.getAbsolutePath());
			rma.setExifTool(archiveInfo.getExifTool());
			rma.setInfos(rm);
			rma.saveInfos();
			removeAll(all, rm);
			StringBuilder sb = new StringBuilder();
			String rootName = archiveInfo.getPath();
			String sub = rmf.getAbsolutePath();
			for (int i=0;i<rm.size();i++) {
				PhotoInfo p = rm.get(i);
				File source = new File(fullPath(rootName, p));
				File targetDir = p.getFolder()==null || p.getFolder().isEmpty() ? rmf : new File(rmf,p.getFolder());
				try {
					targetDir.mkdirs();
					Files.move(source.toPath(), new File(targetDir, source.getName()).toPath());
					if (!p.absuluteSameAs(sameAs.get(i))) appendToFile(logFile, new File(targetDir, source.getName()).getAbsolutePath() + " <-> "
							+ fullPath(ref==null?rootName:ref.getPath(), sameAs.get(i)));
				} catch (Exception e) {
					sb.append("move \"").append(fullPath(rootName, p)).append("\" \"").append(newFile(sub, p))
							.append("\"\r\n");
					if (!p.absuluteSameAs(sameAs.get(i))) appendToFile(logFile, fullPath(rootName, p) + " <-> " + fullPath(ref==null?rootName:ref.getPath(), sameAs.get(i)));
				}
			}
			String cmd = sb.toString().trim();
			if (!cmd.isEmpty())	appendToFile(new File(archiveInfo.getPath(), "manual_rm.bat"), sb.toString());
		}
	}
	// 记录必须排序好
	static void deleteFiles(ArchiveInfo archiveInfo) {
		List<PhotoInfo> rm = new ArrayList<>();
		List<PhotoInfo> sameAs = new ArrayList<>();
		List<PhotoInfo> all = archiveInfo.getInfos();
		SystemOut.println("照片数量 : " + all.size());
		for (int i = 0; i < all.size(); i++) {
			Date dt0 = all.get(i).getShootTime();
			for (int j = i + 1; j < all.size(); j++) {
				Date dt1 = all.get(j).getShootTime();
				if (!equals(dt0, dt1))
					break;
				if (all.get(i).sameAs(all.get(j))) {
					rm.add(all.get(i));
					sameAs.add(all.get(j));
					break;
				}
			}
		}
		doDeleteSameFiles(rm,sameAs,archiveInfo,null);
	}

	// 两个记录必须排序好
	static void deleteFiles(ArchiveInfo archiveInfo, ArchiveInfo ref) {
		List<PhotoInfo> rm = new ArrayList<>();
		List<PhotoInfo> sameAs = new ArrayList<>();
		List<PhotoInfo> all = archiveInfo.getInfos();
		List<PhotoInfo> refInfos = ref.getInfos();
		File logFile = new File(archiveInfo.getPath(), "same_photo.log");
		SystemOut.println("照片数量 : " + all.size());
		int j0 = 0, jstart = 0; // 0 - jstart : 没有拍摄日期
		for (int i = 0; i < refInfos.size(); i++) {
			if (refInfos.get(i).getShootTime() != null) {
				jstart = i;
				break;
			}
		}
		j0 = jstart;
		for (int i = 0; i < all.size(); i++) {
			Date dt0 = all.get(i).getShootTime();
			if (dt0 == null) { // 没有拍摄日期的互相比较
				for (int j = j0; j < jstart; j++) {
					if (all.get(i).sameAs(refInfos.get(j))) {
						rm.add(all.get(i));
						sameAs.add(refInfos.get(j));
						break;
					}
				}
			} else {
				boolean j0set = false;
				for (int j = j0; j < refInfos.size(); j++) {
					Date dt1 = refInfos.get(j).getShootTime();
					if (dt1.getTime() == dt0.getTime()) {
						if (!j0set) {
							j0 = j;
							j0set = true;
						}
						if (all.get(i).sameAs(refInfos.get(j))) {
							rm.add(all.get(i));
							sameAs.add(refInfos.get(j));
							break;
						}
					} else if (dt1.getTime() > dt0.getTime()) {
						break;
					}

				}
			}
		}
		doDeleteSameFiles(rm,sameAs,archiveInfo,ref);
	}

	static void otherFiles(ArchiveInfo archiveInfo) {
		List<PhotoInfo> all = archiveInfo.getInfos();
		SystemOut.println("照片数量 : " + all.size());
		List<PhotoInfo> other = all.stream().filter(a -> a.getShootTime() == null).collect(Collectors.toList());
		if (other.size() > 0) {
			SystemOut.println("没有拍摄日期照片数量 : " + other.size());
			File rmf = new File(new File(archiveInfo.getPath()), ".other");
			rmf.mkdirs();
			ArchiveInfo rma = new ArchiveInfo();
			rma.setPath(rmf.getAbsolutePath());
			rma.setExifTool(archiveInfo.getExifTool());
			rma.setInfos(new ArrayList<>(other));
			rma.saveInfos();
			removeAll(all, other);
			StringBuilder sb = new StringBuilder();
			String rootName = archiveInfo.getPath();
			String sub = rmf.getAbsolutePath();
			for (PhotoInfo p : other) {
				File source = new File(fullPath(rootName, p));
				try {
					Files.move(source.toPath(), new File(rmf, source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				} catch (Exception e) {
					sb.append("move \"").append(fullPath(rootName, p)).append("\" \"").append(newFile(sub, p))
							.append("\"\r\n");
				}
			}
			String batcmd = sb.toString().trim();
			if (!batcmd.isEmpty()) writeToFile(new File(archiveInfo.getPath(), "manual_other.bat"), batcmd);
		}
	}

	public static void saveFolderInfos(List<FolderInfo> infos, ArchiveInfo archived) {
		File f=new File(archived.getPath(),".folder_info.dat");
		String rootPath = archived.getPath();
		StringBuilder sb=new StringBuilder();
		for (FolderInfo fi : infos) {
			sb.append("info:\r\n").append(fi.writeToString(rootPath));
		}
		writeToFile(f,sb.toString());
	}
	public static List<FolderInfo> getFolderInfos(ArchiveInfo archived) {
		File f=new File(archived.getPath(),".folder_info.dat");
		String rootPath = archived.getPath();
		List<FolderInfo> list = new ArrayList<>();
		if (f.exists()) {
			String content = getFromFile(f);
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
			return fullPath("",a).compareTo(fullPath("",b));
		});
		FolderInfo fi = null;
		String fiPath="", pathYear=null;
		for (int i=0;i<infos.size();i++) {
			PhotoInfo pi = infos.get(i);
			String path = pi.getFolder();
			if (path.indexOf("扫描")>=0) continue;
			if (path.startsWith("\\")) path=path.substring(1);
			if (path.endsWith("\\")) path=path.substring(0,path.length()-1);
			String [] ff = path.split("\\\\");
			if (pathYear==null || !pathYear.equals(ff[0])) {
				pathYear=ff[0];
				Date dt0=null,dt1=null;
				try {
					if (ff[0].length() == 4) {
						dt0 = new SimpleDateFormat("yyyy-MM-dd").parse(ff[0] + "-01-01");
						dt1 = new SimpleDateFormat("yyyy-MM-dd").parse(ff[0] + "-12-31");
					} else {
						String [] yy = ff[0].split("-");
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
				} catch (Exception e) {
				}
			}
			if (ff.length<2) continue;
			if (fi==null || !fiPath.toLowerCase().equals((ff[0]+"\\"+ff[1]).toLowerCase())) {
				fiPath = ff[0]+"\\"+ff[1];
				fi = new FolderInfo(rootPath+"\\"+fiPath);
				if (ff[1].endsWith(" 生活") || ff[1].endsWith(" 重庆")) {
					if (ff[1].length()==7) {
						fi.setDate0(string2Date(ff[1].substring(0,4)+"-01-01","yyyy-MM-dd"));
						fi.setDate1(string2Date(ff[1].substring(0,4)+"-12-31","yyyy-MM-dd"));
						fi.setPriority(1000);
					}
					if (ff[1].length()==9) {
						int m=Integer.parseInt(ff[1].substring(4,6));
						String md = (m==2 ? "28" : (m==4 || m==6 || m==9 || m==11 ? "30" : "31"));
						fi.setDate0(string2Date(ff[1].substring(0,6)+"-01","yyyyMM-dd"));
						fi.setDate1(string2Date(ff[1].substring(0,6)+"-"+md,"yyyyMM-dd"));
						fi.setPriority(500);
					}
				}
				list.add(fi);
			}
			Date sdt = pi.getShootTime();
			if (sdt!=null) {
				if (fi.getDate0() == null || fi.getDate0().getTime()>sdt.getTime()) fi.setDate0(dayOf(sdt));
				if (fi.getDate1() == null || fi.getDate1().getTime()<sdt.getTime()) fi.setDate1(nextDayOf(sdt));
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
	public static void copyToFolder(ArchiveInfo camera, List<FolderInfo> folderInfos) {
		for (FolderInfo fi: folderInfos) {
			new File(fi.getPath(),fi.getCamera()).mkdirs();
		}
		String sameLog = getFromFile(new File(camera.getPath(),"same_photo.log"));
		if (sameLog==null) sameLog="";
		StringBuilder mvfailed=new StringBuilder();
		StringBuilder notarchived = new StringBuilder();
		String root = camera.getPath();
		for (PhotoInfo pi : camera.getInfos()) {
			Date dt = pi.getShootTime();
			if (dt==null) notarchived.append(fullPath(root,pi)).append("\r\n");
			else {
				FolderInfo fi = findFolder(dt,folderInfos);
				if (fi==null) notarchived.append(fullPath(root,pi)).append("\r\n");
				else {
					File source = new File(fullPath(root,pi));
					File targetDir = new File(fi.getPath()+File.separator+fi.getCamera());
					try {
						Files.move(source.toPath(),new File(targetDir,source.getName()).toPath());
						sameLog.replace(source.getAbsolutePath(),new File(targetDir,source.getName()).getAbsolutePath());
					} catch (IOException e) {
						mvfailed.append("move \"").append(fullPath(root,pi)).append("\" ").append("\"")
								.append(fi.getPath()).append("\\").append(fi.getCamera()).append("\"\r\n");
					}

				}
			}
		}
		if (!sameLog.isEmpty()) writeToFile(new File(camera.getPath(),"same_photo.log"),sameLog.trim());
		writeToFile(new File(root,"没有归宿的文件.log"),notarchived.toString());
		writeToFile(new File(root,"manual_archive.bat"),mvfailed.toString());
	}
	public static void removeEmptyFolder(File dir) {
		if (!dir.isDirectory()) return;
		File[] subDirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().equals(".") || pathname.getName().equals("..")) return false;
				else return pathname.isDirectory();
			}
		});
		if (subDirs.length>0) {
			for (File d : subDirs) {
				removeEmptyFolder(d);
			}
		}
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().equals(".") || pathname.getName().equals("..")) return false;
				else return true;
			}
		});
		if (files.length==0) dir.delete();
	}
	public static void processDir(ArchiveInfo a, boolean moveOtherFiles) {
		SystemOut.println("排序 " + a.getPath() + " 文件，共有 : " + a.getInfos().size() + " ...");
		a.sortInfos();
		a.saveInfos();
		SystemOut.println("备份排序后文件："+ArchiveInfo.ARCHIVE_FILE + ".sorted.dat");
		File bak = new File(a.getPath(), ArchiveInfo.ARCHIVE_FILE + ".sorted.dat");
		File dat = new File(a.getPath(), ArchiveInfo.ARCHIVE_FILE);
		try {
			Files.copy(dat.toPath(), bak.toPath());
		} catch (IOException e) {
		}
		SystemOut.println("删除重复文件...");
		deleteFiles(a);
		if (moveOtherFiles) {
			SystemOut.println("移动无拍摄日期文件...");
			otherFiles(a);
		}
		SystemOut.println("保存处理后文件...");
		a.saveInfos();
	}
	private static ArchiveInfo getArchiveInfo(BufferedReader stdin,String message, String def, boolean moveOtherFile) {
		if (stdin==null && "-".equals(def)) return null;
		String input=null;
		ArchiveInfo camera=null;
		while(camera==null) {
			if (stdin!=null) SystemOut.println(message+"(" + def + "):");
			try {
				input = (stdin==null ? def : stdin.readLine().trim());
				if (input.isEmpty()) input=def;
				camera = new ArchiveInfo(input);
				if (!camera.isReadFromFile()) processDir(camera, moveOtherFile);
			} catch (IOException ex) {
				camera=null;
			}
		}
		return camera;
	}



	public static void checkDeletedFile(String logFile) {
		List<String> list1 = new ArrayList<>(), list2 = new ArrayList<>();
		BufferedReader ins=null;
		try {
			ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
			String line = null;
			while ((line=ins.readLine())!=null) {
				String [] ll= line.split(" <-> ");
				if (ll.length==2) {
					String f1=ll[0].trim(), f2 = ll[1].trim();
					if (contentCompare(f1,f2) == 0) {
						if (!f1.isEmpty()) {
							new File(f1).delete();
						}
					} else {
						list1.add(f1);
						list2.add(f2);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			if (ins!=null) {
				try {
					ins.close();
				} catch (IOException e) {
				}
			}
		}


		TwoImageViewer tv = new TwoImageViewer(logFile, list1, list2);
		tv.saveFile();
		tv.run();

	}
	public static void main2(String[] argv) {
		/* argv
		      0 : 需要归档的目录
		      1 : 已经归档的目录
		      2 ：yes/on
		      3 : off 自动关机
		 */
		checkDeletedFile("E:\\Photo\\Archived\\same_photo.log");
	}
	public static void main(String[] argv) {
		/* argv
		      0 : 需要归档的目录
		      1 : 已经归档的目录
		      2 ：yes/on
		      3 : off 自动关机
		 */
		System.out.println("Usage1: java -jar pa.jar \"需要归档的目录完整名\" \"归档到的目录完整名\" yes|no<是否执行归档操作> <off自动关机>");
		System.out.println("Usage2: java -jar pa.jar -v \"已归档目录完整名\"");
		if (argv.length>0 && argv[0].equals("-v")) {
			BufferedReader stdin= new BufferedReader(new InputStreamReader(System.in));
			String input=(argv.length>1 ? argv[1] : null);
			while (input==null || input.isEmpty()) {
				try {
					System.out.println("已归档目录完整名：");
					input = stdin.readLine().trim();
				} catch (IOException e) {
				}
			}
			try {
				stdin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			checkDeletedFile(new File(input,"same_photo.log").getAbsolutePath());
		} else {
			BufferedReader stdin= new BufferedReader(new InputStreamReader(System.in));
			String input=null;
			while (true) {
				try {
					ExifTool.INSTALLED_VERSION = ExifTool.getInstalledVersion();
					break;
				} catch (IOException e) {
					SystemOut.println(e.getMessage());
					try {
						input = stdin.readLine().trim();
						ExifTool.EXIFTOOL = new File(input, "exiftool.exe").getAbsolutePath();
					} catch (IOException ex) {
					}
				}
			}
			SystemOut.println("Now :"+date2String(new Date()));
			ArchiveInfo camera=getArchiveInfo(argv.length>=1 ? null : stdin,"输入需要归档的目录",argv.length>=1 ? argv[0] : "E:\\Camera",true);
	
			SystemOut.println("Now :"+date2String(new Date()));
			ArchiveInfo archived = getArchiveInfo(argv.length>=2 ? null : stdin,"输入已经归档的目录",argv.length>=2 ? argv[1] : "E:\\Photo\\Archived",false);

			SystemOut.println("Now :"+date2String(new Date()));
			if (archived!=null) {
				SystemOut.println("扫描目录信息: "+archived.getPath());
				List<FolderInfo> folderInfos = getFolderInfos(archived);
				boolean isOk = true;
				for (FolderInfo fi: folderInfos) {
					if (fi.getDate0()==null) {
						isOk = false;
						SystemOut.println(fi.getPath() + " 缺少开始日期");
					}
					if (fi.getDate1()==null) {
						isOk = false;
						SystemOut.println(fi.getPath() + " 缺少结束日期");
					}
				}
				if (isOk && camera!=null) {
					folderInfos.sort((a, b) -> a.compareTo(b));
					saveFolderInfos(folderInfos, archived);
					SystemOut.println("Now :" + date2String(new Date()));
					input = (argv.length>=3 ? argv[2].toLowerCase() : null);
					while (input==null || input.isEmpty()) {
						SystemOut.println("执行文件归档？");
						try {
							input = stdin.readLine().trim();
						} catch (Exception e) {
						}
					}
					if (input.toLowerCase().startsWith("y")) {
						SystemOut.println("Now :"+date2String(new Date()));
						SystemOut.println("删除归档文件夹已经存在的待归档文件...");
						deleteFiles(camera,archived);
						SystemOut.println("Now :"+date2String(new Date()));
						SystemOut.println("将文件归档...");
						copyToFolder(camera, folderInfos);
					}
				}
		
				SystemOut.println("Now :"+date2String(new Date()));
				SystemOut.println("删除空目录");
				removeEmptyFolder(new File(archived.getPath()));
			}
			SystemOut.println("Now :"+date2String(new Date()));
			SystemOut.println("End.");
	
			SystemOut.close();
	
			try {
				stdin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			if (argv.length>=4 && argv[3].toLowerCase().contains("off")) {
				List<String> cmd = new ArrayList<>();
				cmd.add("shutdown");
				cmd.add("-s");
				cmd.add("-f");
				cmd.add("-t");
				cmd.add("10");
				try {
					CommandRunner.run(cmd);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
