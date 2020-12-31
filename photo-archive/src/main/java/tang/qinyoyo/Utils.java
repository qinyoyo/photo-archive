package tang.qinyoyo;

import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.DateUtil;
import tang.qinyoyo.archive.FolderInfo;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.CommandRunner;
import tang.qinyoyo.exiftool.ExifTool;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
	public static void saveFolderInfos(List<FolderInfo> infos, ArchiveInfo archived) {
		File f=new File(archived.getPath(),ArchiveInfo.folder_info_dat);
		String rootPath = archived.getPath();
		StringBuilder sb=new StringBuilder();
		for (FolderInfo fi : infos) {
			sb.append("info:\r\n").append(fi.writeToString(rootPath));
		}
		ArchiveUtils.writeToFile(f,sb.toString());
	}
	public static List<FolderInfo> getFolderInfos(ArchiveInfo archived) {
		File f=new File(archived.getPath(),ArchiveInfo.folder_info_dat);
		String rootPath = archived.getPath();
		List<FolderInfo> list = new ArrayList<>();
		if (f.exists()) {
			String content = ArchiveUtils.getFromFile(f);
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
	public static void copyToFolder(ArchiveInfo camera, ArchiveInfo archived, List<FolderInfo> folderInfos) {
		for (FolderInfo fi: folderInfos) {
			new File(fi.getPath(),fi.getCamera()).mkdirs();
		}
		String sameLog = ArchiveUtils.getFromFile(new File(camera.getPath(),ArchiveInfo.same_photo_log));
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
				if (fi==null) { // 没有发现目录，新建目录
					if (pi.getSubFolder().isEmpty()) {
						targetDir = new File(archived.getPath() + File.separator + DateUtil.date2String(dt, "yyyy")
								+ File.separator + DateUtil.date2String(dt, "yyyyMM") + File.separator + FolderInfo.DEFPATH);
						targetDir.mkdirs();
					} else {  // 使用原有的目录，合并文件夹
						targetDir = new File(archived.getPath() + File.separator + pi.getSubFolder());
						targetDir.mkdirs();
					}
				}
				else targetDir = new File(fi.getPath()+File.separator+fi.getCamera());
				try {
					archived.moveFile(pi, camera.getPath(), new File(targetDir,source.getName()));
					sameLog.replace(source.getCanonicalPath(),new File(targetDir,source.getName()).getCanonicalPath());
				} catch (Exception e) {
					mvfailed.append("move \"").append(pi.fullPath(root)).append("\" ").append("\"")
							.append(fi.getPath()).append("\\").append(fi.getCamera()).append("\"\r\n");
				}
			}
		}
		processDir(archived,false,false);
		if (!sameLog.isEmpty()) ArchiveUtils.writeToFile(new File(camera.getPath(),ArchiveInfo.same_photo_log),sameLog.trim());
		ArchiveUtils.writeToFile(new File(root,ArchiveInfo.no_shottime_log),notarchived.toString());
		ArchiveUtils.writeToFile(new File(root,ArchiveInfo.manual_archive_bat),mvfailed.toString());
	}

	public static void processDir(ArchiveInfo a, boolean removeSameFile, boolean moveOtherFiles) {
		SystemOut.println("排序 " + a.getPath() + " 文件，共有 : " + a.getInfos().size() + " ...");
		a.sortInfos();

		SystemOut.println(removeSameFile?"删除重复文件..." : "扫描重复文件...");
		a.scanSameFiles(removeSameFile);

		if (moveOtherFiles) {
			SystemOut.println("移动无拍摄日期文件...");
			a.moveNoShootTimeFiles();
		}
		SystemOut.println("保存处理后文件...");
		a.saveInfos();
	}
	private static ArchiveInfo getArchiveInfo(String path, boolean clearInfo, boolean removeSameFile, boolean moveOtherFile) {
		if (path==null || path.isEmpty() || "-".equals(path)) return null;
		if (clearInfo) {
			new File(path,ArchiveInfo.ARCHIVE_FILE).delete();
			new File(path,ArchiveInfo.ARCHIVE_FILE+ ".sorted.dat").delete();
			new File(path,ArchiveInfo.same_photo_log).delete();
			new File(path,ArchiveInfo.manual_other_bat).delete();
			new File(path,ArchiveInfo.manual_rm_bat).delete();
			new File(path,ArchiveInfo.manual_archive_bat).delete();
			new File(path,ArchiveInfo.no_shottime_log).delete();
			new File(path,ArchiveInfo.folder_info_dat).delete();
		}
		ArchiveInfo	a = new ArchiveInfo(path);
		if (!a.isReadFromFile()) processDir(a, removeSameFile, moveOtherFile);
		return a;
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
					if (!f1.isEmpty() || !f2.isEmpty()) {
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
			ArchiveUtils.writeToFile(new File(archived.getPath(),ArchiveInfo.folder_info_lost_log),logmsg);
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
			ArchiveUtils.removeEmptyFolder(new File(archived.getPath()));
			ArchiveUtils.removeEmptyFolder(new File(camera.getPath()));
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
				try {
					stdin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					checkDeletedFile(new File(input, ArchiveInfo.same_photo_log).getCanonicalPath());
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
			camera.scanSameFilesWith(archived);
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
			ArchiveUtils.removeEmptyFolder(new File(path));
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
				camera.scanSameFilesWith(archived);
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
				checkDeletedFile(new File(v.toString(), ArchiveInfo.same_photo_log).getCanonicalPath());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		} else {
			if (boolValue(params.get(PARAM_SHUTDOWN))) {
				CommandRunner.shutdown(10);
			}
		}
	}
}
