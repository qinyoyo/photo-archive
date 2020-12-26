package tang.qinyoyo;

import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.DateUtil;
import tang.qinyoyo.archive.FolderInfo;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.CommandRunner;
import tang.qinyoyo.exiftool.ExifTool;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
	static final String same_photo_log = ".same_photo.log";
	static final String manual_other_bat = ".manual_other.bat";
	static final String manual_rm_bat = ".manual_rm.bat";
	static final String manual_archive_bat = ".manual_archive.bat";
	static final String no_shottime_log = ".no_shottime.log";
	static final String folder_info_dat = ".folder_info.dat";
	static final String folder_info_lost_log = ".folder_info_lost.log";
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
					byte[] buf1=new byte[10240],buf2 = new byte[10240];
					long passLength = Math.max(0,file1.length() / 10 - 10240);
					int len1=in1.read(buf1), len2=in2.read(buf2);
					do {
						if (len1<len2) return -1;
						else if (len1>len2) return 1;
						else if (!Arrays.equals(buf1,buf2)) return -1;
						if (passLength>0) {
							in1.skip(passLength);
							in2.skip(passLength);
						}
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



	public static File bakNameOf(File file) {
		if (!file.exists()) return file;
		String path = file.getAbsolutePath();
		int pos = path.lastIndexOf(".");
		String ext = (pos>=0 ? path.substring(pos) : "");
		String name = (pos>=0 ? path.substring(0,pos) : path);
		Pattern p = Pattern.compile("_(\\d+)$");
		Matcher m = p.matcher(name);
		int index = 0;
		if (m.find()) {
			name = name.substring(0,m.start());
			index = Integer.parseInt(m.group(1));
		}
		index ++;
		File f = new File(name+"_"+index + ext);
		while (f.exists()) {
			index ++;
			f = new File(name+"_"+index + ext);
		}
		return f;
	}

	public static String newFile(String root, PhotoInfo p) {
		try {
			String sub = p.getSubFolder();
			if (sub == null || sub.isEmpty() || sub.equals(".")) {
				return new File(root, p.getFileName()).getCanonicalPath();
			} else
				return new File(root, sub.replace("\\", " ") + p.getFileName()).getCanonicalPath();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
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


	static void listSameFiles(List<PhotoInfo> rm,List<PhotoInfo> sameAs,ArchiveInfo archiveInfo) {
		try {
			if (rm.size() > 0) {
				List<PhotoInfo> all = archiveInfo.getInfos();
				File logFile = new File(archiveInfo.getPath(), same_photo_log);
				SystemOut.println("重复照片数量 : " + rm.size());
				StringBuilder sb = new StringBuilder();
				String rootName = archiveInfo.getPath();
				for (int i = 0; i < rm.size(); i++) {
					File one = new File(rm.get(i).fullPath(rootName));
					File two = new File(sameAs.get(i).fullPath(rootName));
					if (one.exists() && two.exists()) {
						sb.append(one.getCanonicalPath() + " <-> " + two.getCanonicalPath()).append("\r\n");
					}
				}
				writeToFile(logFile,sb.toString());
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	// 完全一致的文件将被删除
	static void doDeleteSameFiles(List<PhotoInfo> rm,List<PhotoInfo> sameAs,ArchiveInfo archiveInfo, ArchiveInfo ref) {
		try {
			if (rm.size() > 0) {
				List<PhotoInfo> all = archiveInfo.getInfos();
				File logFile = new File(archiveInfo.getPath(), same_photo_log);
				SystemOut.println("重复照片数量 : " + rm.size());
				File rmf = new File(new File(archiveInfo.getPath()), ".delete");
				rmf.mkdirs();
				ArchiveInfo rma = new ArchiveInfo();
				rma.setPath(rmf.getCanonicalPath());
				rma.setExifTool(archiveInfo.getExifTool());
				rma.setInfos(rm);
				rma.saveInfos();
				removeAll(all, rm);
				StringBuilder sb = new StringBuilder();
				String rootName = archiveInfo.getPath();
				String sub = rmf.getCanonicalPath();
				for (int i = 0; i < rm.size(); i++) {
					PhotoInfo p = rm.get(i);
					File source = new File(p.fullPath(rootName));

					try {
						if (p.absoluteSameAs(sameAs.get(i))) {
							File targetDir = p.getSubFolder() == null || p.getSubFolder().isEmpty() ? new File(rmf, "absolute") : new File(new File(rmf, p.getSubFolder()), "absolute");
							//source.delete();
							targetDir.mkdirs();
							Files.move(source.toPath(), new File(targetDir, source.getName()).toPath());
							appendToFile(new File(targetDir, Utils.same_photo_log + ".bat"), "fc /b \"" + new File(targetDir, source.getName()).getCanonicalPath()
									+ "\" \""
									+ sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()) + "\"");
						} else {
							File targetDir = p.getSubFolder() == null || p.getSubFolder().isEmpty() ? rmf : new File(rmf, p.getSubFolder());
							targetDir.mkdirs();
							Files.move(source.toPath(), new File(targetDir, source.getName()).toPath());
							appendToFile(logFile, new File(targetDir, source.getName()).getCanonicalPath() + " <-> "
									+ sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()));
						}
					} catch (Exception e) {
						sb.append("move \"").append(p.fullPath(rootName)).append("\" \"").append(newFile(sub, p))
								.append("\"\r\n");
						if (!p.absoluteSameAs(sameAs.get(i)))
							appendToFile(logFile, p.fullPath(rootName) + " <-> " + sameAs.get(i).fullPath(ref == null ? rootName : ref.getPath()));
					}
				}
				String cmd = sb.toString().trim();
				if (!cmd.isEmpty()) appendToFile(new File(archiveInfo.getPath(), manual_rm_bat), sb.toString());
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	// 记录必须排序好
	static void deleteFiles(ArchiveInfo archiveInfo, boolean removeSameFile) {
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
				boolean same = false;
				try {
					same = all.get(i).sameAs(all.get(j));
				} catch (Exception e) {
					same = contentCompare(all.get(i).fullPath(archiveInfo.getPath()),all.get(j).fullPath(archiveInfo.getPath())) == 0;
				}
				if (same) {
					rm.add(all.get(i));
					sameAs.add(all.get(j));
					break;
				}
			}
		}
		if (removeSameFile) doDeleteSameFiles(rm,sameAs,archiveInfo,null);
		else listSameFiles(rm,sameAs,archiveInfo);
	}

	// 两个记录必须排序好
	static void deleteFiles(ArchiveInfo archiveInfo, ArchiveInfo ref) {
		String signFile = "." + ref.getPath().replace(":", "_").replace(File.separator, "_");
		if (new File(archiveInfo.getPath(),signFile).exists()) return;
		List<PhotoInfo> rm = new ArrayList<>();
		List<PhotoInfo> sameAs = new ArrayList<>();
		List<PhotoInfo> all = archiveInfo.getInfos();
		List<PhotoInfo> refInfos = ref.getInfos();
		File logFile = new File(archiveInfo.getPath(), same_photo_log);
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
					boolean same = false;
					try {
						same = all.get(i).sameAs(refInfos.get(j));
					} catch (Exception e) {
						same = contentCompare(all.get(i).fullPath(archiveInfo.getPath()),refInfos.get(j).fullPath(ref.getPath())) == 0;
					}
					if (same) {
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
						boolean same = false;
						try {
							same = all.get(i).sameAs(refInfos.get(j));
						} catch (Exception e) {
							same = contentCompare(all.get(i).fullPath(archiveInfo.getPath()),refInfos.get(j).fullPath(ref.getPath())) == 0;
						}
						if (same) {
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
		writeToFile(new File(archiveInfo.getPath(),signFile),ref.getPath());
	}

	static void otherFiles(ArchiveInfo archiveInfo) {
		try {
			List<PhotoInfo> all = archiveInfo.getInfos();
			SystemOut.println("照片数量 : " + all.size());
			List<PhotoInfo> other = all.stream().filter(a -> a.getShootTime() == null).collect(Collectors.toList());
			if (other.size() > 0) {
				SystemOut.println("没有拍摄日期照片数量 : " + other.size());
				File rmf = new File(new File(archiveInfo.getPath()), ".other");
				rmf.mkdirs();
				ArchiveInfo rma = new ArchiveInfo();
				rma.setPath(rmf.getCanonicalPath());
				rma.setExifTool(archiveInfo.getExifTool());
				rma.setInfos(new ArrayList<>(other));
				rma.saveInfos();
				removeAll(all, other);
				StringBuilder sb = new StringBuilder();
				String rootName = archiveInfo.getPath();
				String sub = rmf.getCanonicalPath();
				for (PhotoInfo p : other) {
					File source = new File(p.fullPath(rootName));
					try {
						Files.move(source.toPath(), new File(rmf, source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
					} catch (Exception e) {
						sb.append("move \"").append(p.fullPath(rootName)).append("\" \"").append(newFile(sub, p))
								.append("\"\r\n");
					}
				}
				String batcmd = sb.toString().trim();
				if (!batcmd.isEmpty()) writeToFile(new File(archiveInfo.getPath(), manual_other_bat), batcmd);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void saveFolderInfos(List<FolderInfo> infos, ArchiveInfo archived) {
		File f=new File(archived.getPath(),folder_info_dat);
		String rootPath = archived.getPath();
		StringBuilder sb=new StringBuilder();
		for (FolderInfo fi : infos) {
			sb.append("info:\r\n").append(fi.writeToString(rootPath));
		}
		writeToFile(f,sb.toString());
	}
	public static List<FolderInfo> getFolderInfos(ArchiveInfo archived) {
		File f=new File(archived.getPath(),folder_info_dat);
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
	public static void copyToFolder(ArchiveInfo camera, ArchiveInfo archived, List<FolderInfo> folderInfos) {
		for (FolderInfo fi: folderInfos) {
			new File(fi.getPath(),fi.getCamera()).mkdirs();
		}
		String sameLog = getFromFile(new File(camera.getPath(),same_photo_log));
		if (sameLog==null) sameLog="";
		StringBuilder mvfailed=new StringBuilder();
		StringBuilder notarchived = new StringBuilder();
		String root = camera.getPath();
		List<PhotoInfo> archivedInfos = archived.getInfos();
		for (PhotoInfo pi : camera.getInfos()) {
			Date dt = pi.getShootTime();
			if (dt==null) notarchived.append(pi.fullPath(root)).append("\r\n");
			else {
				FolderInfo fi = findFolder(dt,folderInfos);
				File source = new File(pi.fullPath(root));
				File targetDir = null;
				if (fi==null) {
					targetDir = new File(archived.getPath() + File.separator+DateUtil.date2String(dt,"yyyy")
							+ File.separator+DateUtil.date2String(dt,"yyyyMM")+ File.separator+FolderInfo.DEFPATH);
					targetDir.mkdirs();
				}
				else targetDir = new File(fi.getPath()+File.separator+fi.getCamera());
				try {
					Files.move(source.toPath(),new File(targetDir,source.getName()).toPath());
					PhotoInfo newPi = pi.cloneObject();
					newPi.setSubFolder(targetDir.getCanonicalPath().substring(archived.getPath().length()+1));
					archivedInfos.add(newPi);
					sameLog.replace(source.getCanonicalPath(),new File(targetDir,source.getName()).getCanonicalPath());
				} catch (Exception e) {
					mvfailed.append("move \"").append(pi.fullPath(root)).append("\" ").append("\"")
							.append(fi.getPath()).append("\\").append(fi.getCamera()).append("\"\r\n");
				}
			}
		}
		processDir(archived,false,false);
		if (!sameLog.isEmpty()) writeToFile(new File(camera.getPath(),same_photo_log),sameLog.trim());
		writeToFile(new File(root,no_shottime_log),notarchived.toString());
		writeToFile(new File(root,manual_archive_bat),mvfailed.toString());
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

		if (subDirs!=null && subDirs.length>0) {
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
		if (files!=null && files.length==0) dir.delete();
	}
	public static void processDir(ArchiveInfo a, boolean removeSameFile, boolean moveOtherFiles) {
		SystemOut.println("排序 " + a.getPath() + " 文件，共有 : " + a.getInfos().size() + " ...");
		a.sortInfos();

		SystemOut.println(removeSameFile?"删除重复文件..." : "扫描重复文件...");
		deleteFiles(a,removeSameFile);

		if (moveOtherFiles) {
			SystemOut.println("移动无拍摄日期文件...");
			otherFiles(a);
		}
		SystemOut.println("保存处理后文件...");
		a.saveInfos();
	}
	private static ArchiveInfo getArchiveInfo(String path, boolean clearInfo, boolean removeSameFile, boolean moveOtherFile) {
		if (path==null || path.isEmpty() || "-".equals(path)) return null;
		if (clearInfo) {
			new File(path,ArchiveInfo.ARCHIVE_FILE).delete();
			new File(path,ArchiveInfo.ARCHIVE_FILE+ ".sorted.dat").delete();
			new File(path,same_photo_log).delete();
			new File(path,manual_other_bat).delete();
			new File(path,manual_rm_bat).delete();
			new File(path,manual_archive_bat).delete();
			new File(path,no_shottime_log).delete();
			new File(path,folder_info_dat).delete();
		}
		ArchiveInfo	a = new ArchiveInfo(path);
		if (!a.isReadFromFile()) processDir(a, removeSameFile, moveOtherFile);
		return a;
	}

	public static void checkDeletedFile(String logFile, boolean absoluteCheck) {
		List<String> list1 = new ArrayList<>(), list2 = new ArrayList<>();
		BufferedReader ins=null;
		if (absoluteCheck) System.out.println("删除完全一致的文件...");
		try {
			ins = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"GBK"));
			String line = null;
			while ((line=ins.readLine())!=null) {
				String [] ll= line.split(" <-> ");
				if (ll.length==2) {
					String f1=ll[0].trim(), f2 = ll[1].trim();
						if (absoluteCheck && !f1.isEmpty() && !f2.isEmpty() && contentCompare(f1, f2) == 0) {
							if (!f1.isEmpty()) {
								new File(f1).delete();
							}
						} else if (!f1.isEmpty() || !f2.isEmpty()) {
							list1.add(f1);
							list2.add(f2);
						}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		} finally {
			if (ins!=null) {
				try {
					ins.close();
				} catch (IOException e) {
				}
			}
		}

		if (list1.size()>0) {
			TwoImageViewer tv = new TwoImageViewer(logFile, list1, list2);
			tv.saveFile();
			tv.run();
		} else System.out.println("没有可比较的文件");

	}

	static final String PARAM_VIEW_DIR = "view";
	static final String PARAM_CAMERA_DIR = "camera";
	static final String PARAM_FORCE_CHECK = "force";
	static final String PARAM_ARCHIVED_DIR = "archived";
	static final String PARAM_DELETE_SAME1 = "same1";
	static final String PARAM_DELETE_SAME2 = "same2";
	static final String PARAM_MOVE_OTHER1 = "other1";
	static final String PARAM_MOVE_OTHER2 = "other2";
	static final String PARAM_CLEAR1 = "clear1";
	static final String PARAM_CLEAR2 = "clear2";
	static final String PARAM_THUMB1 = "thumb1";
	static final String PARAM_THUMB2 = "thumb2";
	static final String PARAM_EXECUTE = "execute";
	static final String PARAM_SHUTDOWN = "shutdown";
	static final String PARAM_HELP = "help";
	static final String PARAM_EMPTY = "empty";
	static final String PARAM_RENAME = "rename";
	public static String RENAME_PATTERN = PhotoInfo.RENAME_PATTERN;
	public static Map<String,Object> parseArgv(String [] argv) throws Exception {
		Map<String,Object> result = new HashMap<>();
		if (argv==null || argv.length==0) return result;
		int total = argv.length;
		for (int i=0;i<total;i++) {
			String param = argv[i].trim();
			switch (param) {
				case "-n":
				case "--rename":
					if (i<total-1) {
						result.put(PARAM_RENAME,argv[i+1]);
					} else throw new Exception("-n 参数必须后跟一个目录，指定修改文件名的目录");
					if (i<total-2) {
						RENAME_PATTERN = argv[i+2];
					}
					break;
				case "-v" :
				case "--view":
					if (i<total-1) {
						result.put(PARAM_VIEW_DIR,argv[i+1]);
						break;
					} else throw new Exception("-v 参数必须后跟一个目录，指定浏览相同图片的目录");
				case "-p1":
				case "--path1":
					if (i<total-1) {
						result.put(PARAM_CAMERA_DIR,argv[i+1]);
						break;
					} else throw new Exception("-c 参数必须后跟一个目录，指定需要归档的文件目录");
				case "-p2":
				case "--path2":
					if (i<total-1) {
						result.put(PARAM_ARCHIVED_DIR,argv[i+1]);
						break;
					} else throw new Exception("-a 参数必须后跟一个目录，指定归档目标路径");
				case "-a":
					result.put(PARAM_FORCE_CHECK,true);
					break;
				case "-h":
				case "--help":
					result.put(PARAM_HELP,true);
					break;
				case "-s1":
				case "--same1":
				case "-s2":
				case "--same2":
					result.put(param.endsWith("1") ? PARAM_DELETE_SAME1 : PARAM_DELETE_SAME2,true);
					break;
				case "-o1":
				case "--other1":
				case "-o2":
				case "--other2":
					result.put(param.endsWith("1") ? PARAM_MOVE_OTHER1 : PARAM_MOVE_OTHER2,true);
					break;
				case "-c1":
				case "--clear1":
				case "-c2":
				case "--clear2":
					result.put(param.endsWith("1") ? PARAM_CLEAR1 : PARAM_CLEAR2,true);
					break;
				case "-t1":
				case "--thumb1":
				case "-t2":
				case "--thumb2":
					result.put(param.endsWith("1") ? PARAM_THUMB1 : PARAM_THUMB2,true);
					break;
				case "-e":
				case "--execute":
					result.put(PARAM_EXECUTE,true);
					break;
				case "-m":
				case "--empty":
					if (i<total-1) {
						result.put(PARAM_EMPTY,argv[i+1]);
						break;
					} else throw new Exception("-m 参数必须后跟一个目录，指定需要删除空目录的文件夹");				
				case "-f":
				case "--shutdown":
					result.put(PARAM_SHUTDOWN,true);
					break;
			}
		}
		return result;
	}
	private static void printUsage() {
		System.out.println("Usage:  java -jar pa.jar <options>");
		System.out.println("options:");
		System.out.println("  -p1 dir_name	: 指定需要归档的图像文件目录, 同 --path1");
		System.out.println("  -p2 dir_name	: 指定归档图像文件最终保存目录,同 --path2");
		System.out.println("  -v  dir_name	:  对比浏览相同图像文件的目录, 同 --view");
		System.out.println("  -n  dir_name	<name_pattern>:  修改文件名，忽略其他选项, 同  --rename");
		System.out.println("  -m : 删除空目录, 同  --empty");
		System.out.println("  -s1 : 将相同文件移到.delete目录, 同  --same1");
		System.out.println("  -o1 : 将无法确定拍摄日期的文件移动到.other目录, 同  --other1");
		System.out.println("  -c1 : 需要归档的目录重新分析, 同  --clear1");
		System.out.println("  -t1 : 创建缩略图, 同  --thumb1");
		System.out.println("  -s2 : 将相同文件移到.delete目录, 同  --same2");
		System.out.println("  -o2 : 将无法确定拍摄日期的文件移动到.other目录, 同  --other2");
		System.out.println("  -c2 : 归档目标目录重新分析, 同  --clear2");
		System.out.println("  -t2 : 创建缩略图, 同  --thumb2");
		System.out.println("  -a : 强制将path1与path2进行文件相同分析");
		System.out.println("  -e : 扫描完成后立即执行归档操作, 同  --execute");
		System.out.println("  -f : 完成后自动关机, 同  --shutdown");
		System.out.println("  -h : 显示本帮助, 同  --help");
	}
	private static boolean boolValue(Object o) {
		if (o==null) return false;
		else if (o instanceof Boolean) return (Boolean) o;
		else {
			String s=o.toString().toLowerCase();
			if (s.startsWith("y")) return true;
			else return false;
		}
	}
	public static String getInputString(BufferedReader stdin, String message, String def) {
		String input=null;
		while(input==null || input.isEmpty()) {
			System.out.println(message + (def==null || def.isEmpty() ? ":" : ("(" + def + "):")));
			try {
				input = stdin.readLine().trim();
				if (input.isEmpty()) input=def;
			} catch (IOException ex) {
				input=null;
			}
		}
		return input;
	}

	public static List<FolderInfo> scanFolderInfo(ArchiveInfo archived) {
		SystemOut.println("扫描目录信息: "+archived.getPath());
		List<FolderInfo> folderInfos = getFolderInfos(archived);
		StringBuilder sb=new StringBuilder();
		
		Iterator<FolderInfo> iter = folderInfos.iterator();
		while (iter.hasNext()) {
			FolderInfo fi = iter.next();
			if (fi.getDate0()==null) {
				SystemOut.println(fi.getPath() + " 缺少开始日期");
				sb.append(fi.getPath()).append("\r\n");
				iter.remove();
			} else if (fi.getDate1()==null) {
				SystemOut.println(fi.getPath() + " 缺少结束日期");
				sb.append(fi.getPath()).append("\r\n");
				iter.remove();
			}
		}
		String logmsg=sb.toString();
		if (!logmsg.isEmpty()) {
			writeToFile(new File(archived.getPath(),folder_info_lost_log),logmsg);
		}
		if (folderInfos.size()>0) {
			folderInfos.sort((a, b) -> a.compareTo(b));
			saveFolderInfos(folderInfos, archived);
		}
		return folderInfos;
	}
	static void executeArchive(ArchiveInfo camera, ArchiveInfo archived) {
		List<FolderInfo> folderInfos = scanFolderInfo(archived);
		if (folderInfos!=null) {
			SystemOut.println("Now :"+DateUtil.date2String(new Date()));
			SystemOut.println("将文件归档...");
			copyToFolder(camera, archived, folderInfos);
			SystemOut.println("Now :"+DateUtil.date2String(new Date()));
			SystemOut.println("删除空目录");
			removeEmptyFolder(new File(archived.getPath()));
			removeEmptyFolder(new File(camera.getPath()));
			SystemOut.println("Now :"+DateUtil.date2String(new Date()));
			SystemOut.println("归档完成");
		}
	}

	public static void main1(BufferedReader stdin) {
		System.out.println("Usage1: java -jar pa.jar <options>");
		System.out.println("");
		while (true) {
			String input = getInputString(stdin, "1 执行归档\n2 查看相同图像文件\n", "");
			if (input.startsWith("1")) break;
			else if (input.startsWith("2")) {
				input = getInputString(stdin, "输入待查看的目录路径", "E:\\Camera");
				String ac = getInputString(stdin, "是否需要对文件内容进行比较", "no");
				try {
					stdin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					checkDeletedFile(new File(input, same_photo_log).getCanonicalPath(), boolValue(ac));
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
				return;
			}
		}
		ArchiveInfo camera = null, archived=null;
		File dir=null;
		while(dir==null || !dir.exists() || !dir.isDirectory()) {
			String input = getInputString(stdin, "输入待归档的目录路径(输入-表示忽略)", "E:\\Camera");
			if (input.isEmpty() || input.equals("-")) break;
			dir = new File(input);
			if (dir!=null && dir.exists() && dir.isDirectory()) {
				boolean clear = boolValue(getInputString(stdin, "是否重新完全扫描", "no"));
				boolean same = boolValue(getInputString(stdin, "将相同文件移到.delete目录", "yes"));
				boolean other = boolValue(getInputString(stdin, "将无法确定拍摄日期的文件移动到.other目录", "yes"));
				camera=getArchiveInfo(input, clear, same,other);
			}
		}
		dir=null;
		while(dir==null || !dir.exists() || !dir.isDirectory()) {
			String input = getInputString(stdin, "输入已经归档的目录路径(输入-表示忽略)", "E:\\Archived");
			if (input.isEmpty() || input.equals("-")) break;
			dir = new File(input);
			if (dir!=null && dir.exists() && dir.isDirectory()) {
				boolean clear = boolValue(getInputString(stdin, "是否重新完全扫描", "no"));
				boolean same = boolValue(getInputString(stdin, "将相同文件移到.delete目录", "yes"));
				boolean other = boolValue(getInputString(stdin, "将无法确定拍摄日期的文件移动到.other目录", "no"));
				archived=getArchiveInfo(input, clear, same,other);
			}
		}
		if (camera!=null && archived!=null) {
			SystemOut.println("Now :"+DateUtil.date2String(new Date()));
			SystemOut.println("删除归档文件夹已经存在的待归档文件...");
			deleteFiles(camera,archived);
			boolean op = boolValue(getInputString(stdin, "是否执行归档操作", "no"));
			if (op) executeArchive(camera,archived);
		}
		SystemOut.close();
	}
	public static void checkVersion(BufferedReader stdin) {
		String input=null;
		while (true) {
			try {
				ExifTool.INSTALLED_VERSION = ExifTool.getInstalledVersion();
				break;
			} catch (IOException e) {
				SystemOut.println(e.getMessage());
				try {
					input = stdin.readLine().trim();
					ExifTool.EXIFTOOL = new File(input, "exiftool").getCanonicalPath();
				} catch (IOException ex) {
				}
			}
		}
	}
	public static void pathScene(File dir) {
		List<String> args = new ArrayList<>();
		args.add("-Scene=Landscape");
		args.add("-overwrite_original");
		ExifTool.dirAction(dir, args, true, new ExifTool.FileActionListener() {
			@Override
			public boolean accept(File dir) {
				return (dir.getName().toLowerCase().equals("l")) || dir.getName().contains("风景");
			}

			@Override
			public void before(File dir) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void after(File dir) {
				dir.renameTo(new File(dir.getParentFile(),"Landscape"));
				
			}
		});
		args.clear();
		args.add("-Scene=Portrait");
		args.add("-overwrite_original");
		ExifTool.dirAction(dir, args, true, new ExifTool.FileActionListener() {
			@Override
			public boolean accept(File dir) {
				return (dir.getName().toLowerCase().equals("p")) || dir.getName().contains("人物") || dir.getName().contains("人像");
			}

			@Override
			public void before(File dir) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void after(File dir) {
				dir.renameTo(new File(dir.getParentFile(),"Portrait"));
				
			}
		});
		args.clear();
		args.add("-Scene=Group");
		args.add("-overwrite_original");
		ExifTool.dirAction(dir, args, true, new ExifTool.FileActionListener() {
			@Override
			public boolean accept(File dir) {
				return dir.getName().toLowerCase().contains("group") || dir.getName().toLowerCase().contains("合影");
			}

			@Override
			public void before(File dir) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void after(File dir) {
				// TODO Auto-generated method stub
				dir.renameTo(new File(dir.getParentFile(),"Group"));
			}
		});
	}
	public static void main(String[] argv) {
		BufferedReader stdin= new BufferedReader(new InputStreamReader(System.in));
		checkVersion(stdin);

		Map<String,Object> params = null;
		try {
			params = parseArgv(argv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			try {
				stdin.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return;
		}
		if (params==null || params.isEmpty()) {
			main1(stdin);
			try {
				stdin.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return;
		}
		try {
			stdin.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (boolValue(params.get(PARAM_HELP))) {
			printUsage();
			return;
		}
		Object v = null;
		v = params.get(PARAM_RENAME);
		if (v!=null) {
			String path = v.toString();
			ArchiveInfo ai = new ArchiveInfo(path);
			SystemOut.println("重新排序...");
			ai.sortInfos();
			SystemOut.println("重新命名...");
			for (PhotoInfo pi : ai.getInfos()) {
				try {
					String name0 = pi.getFileName();
					pi.rename(ai.getPath(),RENAME_PATTERN);
					String name1 = pi.getFileName();
					if (name1.equals(name0)) SystemOut.println(name0 + " 没有重命名");
					else SystemOut.println(name0 + " 重命名为 " + name1);
				} catch (Exception e) {
				}
			}
			SystemOut.close();
			return;
		}

		ArchiveInfo camera = null, archived=null;
		v = params.get(PARAM_EMPTY);
		if (v!=null) {
			String path = v.toString();
			removeEmptyFolder(new File(path));
		}
		v = params.get(PARAM_CAMERA_DIR);
		if (v!=null) {
			String path = v.toString();
			boolean same = boolValue(params.get(PARAM_DELETE_SAME1));
			boolean other = boolValue(params.get(PARAM_MOVE_OTHER1));
			boolean clear = boolValue(params.get(PARAM_CLEAR1));
			camera=getArchiveInfo(path, clear, same,other);
			if (camera!=null && boolValue(params.get(PARAM_THUMB1))) {
				SystemOut.println("Now :"+DateUtil.date2String(new Date()));
				SystemOut.println("建立缩略图文件<"+camera.getPath()+">...");
				camera.createThumbFiles();
			}
		}
		v = params.get(PARAM_ARCHIVED_DIR);
		if (v!=null) {
			String path = v.toString();
			boolean same = boolValue(params.get(PARAM_DELETE_SAME2));
			boolean other = boolValue(params.get(PARAM_MOVE_OTHER2));
			boolean clear = boolValue(params.get(PARAM_CLEAR2));
			archived = getArchiveInfo(path, clear, same,other);
			if (archived!=null && camera!=null) {
				SystemOut.println("Now :"+DateUtil.date2String(new Date()));
				SystemOut.println("删除归档文件夹已经存在的待归档文件...");
				deleteFiles(camera,archived);
			}
			if (archived!=null && camera!=null && boolValue(params.get(PARAM_EXECUTE))) {
				executeArchive(camera,archived);
			}
			if (archived!=null && boolValue(params.get(PARAM_THUMB2))) {
				SystemOut.println("Now :"+DateUtil.date2String(new Date()));
				SystemOut.println("建立缩略图文件<"+archived.getPath()+">...");
				archived.createThumbFiles();
			}
		}

		SystemOut.close();
		v = params.get(PARAM_VIEW_DIR);
		if (v!=null) {
			try {
				checkDeletedFile(new File(v.toString(), same_photo_log).getCanonicalPath(), boolValue(params.get(PARAM_FORCE_CHECK)));
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		} else {
			if (boolValue(params.get(PARAM_SHUTDOWN))) {
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
