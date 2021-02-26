package tang.qinyoyo;

import tang.qinyoyo.archive.ArchiveInfo;
import tang.qinyoyo.archive.DateUtil;
import tang.qinyoyo.archive.PhotoInfo;
import tang.qinyoyo.exiftool.CommandRunner;
import tang.qinyoyo.exiftool.ExifTool;

import java.io.*;
import java.util.*;

public class Utils {

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
	static final String PARAM_RAW = "raw";
	static final String PARAM_THUMB = "thumb";
	public static String RENAME_PATTERN = PhotoInfo.RENAME_PATTERN;
	public static Map<String,Object> parseArgv(String [] argv) throws Exception {
		Map<String,Object> result = new HashMap<>();
		if (argv==null || argv.length==0) return result;
		int total = argv.length;
		for (int i=0;i<total;i++) {
			String param = argv[i].trim();
			switch (param) {
				case "-p3":
				case "--path3":
					if (i<total-1) {
						result.put(PARAM_RAW,argv[i+1]);
						break;
					} else throw new Exception("-w 参数必须后跟一个目录，指定同步的RAW文件目录");
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
				case "-t":
				case "--thumb":
					if (i<total-1) {
						result.put(PARAM_THUMB,argv[i+1]);
						break;
					} else throw new Exception("-t 参数必须后跟一个目录，指定同步缩略图的子目录");
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
		System.out.println("  -p3 dir_name	: 指定需要同步的RAW目录,同 --path3");
		System.out.println("  -v  dir_name	:  对比浏览相同图像文件的目录, 同 --view");
		System.out.println("  -n  dir_name	<name_pattern>:  修改文件名，忽略其他选项, 同  --rename");
		System.out.println("  -m : 删除空目录, 同  --empty");
		System.out.println("  -t dir_name	: 同步thumb目录, 同  --thumb");
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
	public static String getInputString(String message, String def) {
		String input=null;
		Scanner in = new Scanner(System.in);
		while(input==null || input.isEmpty()) {
			System.out.println(message + (def==null || def.isEmpty() ? ":" : ("(" + def + "):")));
			input = in.nextLine().trim();
			if (input.isEmpty()) input=def;
		}
		return input;
	}

	public static void main1() {
		System.out.println("Usage1: java -jar pa.jar <options>");
		System.out.println("");
		while (true) {
			String input = getInputString("1 执行归档\n2 查看相同图像文件\n", "");
			if (input.startsWith("1")) break;
			else if (input.startsWith("2")) {
				input = getInputString("输入待查看的目录路径", "E:\\Camera");
				try {
					checkDeletedFile(new File(input, ArchiveUtils.same_photo_log).getCanonicalPath());
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
				return;
			}
		}
		ArchiveInfo camera = null, archived=null;
		File dir=null;
		while(dir==null || !dir.exists() || !dir.isDirectory()) {
			String input = getInputString("输入待归档的目录路径(输入-表示忽略)", "E:\\Camera");
			if (input.isEmpty() || input.equals("-")) break;
			dir = new File(input);
			if (dir!=null && dir.exists() && dir.isDirectory()) {
				boolean clear = boolValue(getInputString("是否重新完全扫描", "no"));
				boolean same = boolValue(getInputString("将相同文件移到.delete目录", "yes"));
				boolean other = boolValue(getInputString("将无法确定拍摄日期的文件移动到.other目录", "yes"));
				camera=ArchiveUtils.getArchiveInfo(input, clear, same,other);
			}
		}
		dir=null;
		while(dir==null || !dir.exists() || !dir.isDirectory()) {
			String input = getInputString("输入已经归档的目录路径(输入-表示忽略)", "E:\\Archived");
			if (input.isEmpty() || input.equals("-")) break;
			dir = new File(input);
			if (dir!=null && dir.exists() && dir.isDirectory()) {
				boolean clear = boolValue(getInputString("是否重新完全扫描", "no"));
				boolean same = boolValue(getInputString("将相同文件移到.delete目录", "yes"));
				boolean other = boolValue(getInputString("将无法确定拍摄日期的文件移动到.other目录", "no"));
				archived=ArchiveUtils.getArchiveInfo(input, clear, same,other);
			}
		}
		if (camera!=null && archived!=null) {
			System.out.println("Now :"+DateUtil.date2String(new Date()));
			System.out.println("删除归档文件夹已经存在的待归档文件...");
			camera.scanSameFilesWith(archived);
			boolean op = boolValue(getInputString("是否执行归档操作", "no"));
			if (op) ArchiveUtils.executeArchive(camera,archived);
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
		ArchiveUtils.setOutput(Utils.class,"stdout.log");
		Map<String,Object> params = null;
		try {
			params = parseArgv(argv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		if (params==null || params.isEmpty()) {
			main1();
			return;
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
			System.out.println("重新排序...");
			ai.sortInfos();
			System.out.println("重新命名...");
			for (PhotoInfo pi : ai.getInfos()) {
				try {
					String name0 = pi.getFileName();
					pi.rename(ai.getPath(),RENAME_PATTERN);
					String name1 = pi.getFileName();
					if (name1.equals(name0)) System.out.println(name0 + " 没有重命名");
					else System.out.println(name0 + " 重命名为 " + name1);
				} catch (Exception e) {
				}
			}
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
			camera=ArchiveUtils.getArchiveInfo(path, clear, same,other);
			if (camera!=null && boolValue(params.get(PARAM_THUMB1))) {
				System.out.println("Now :"+DateUtil.date2String(new Date()));
				System.out.println("建立缩略图文件<"+camera.getPath()+">...");
				camera.createThumbFiles();
			}
		}
		v = params.get(PARAM_ARCHIVED_DIR);
		if (v!=null) {
			String path = v.toString();
			boolean same = boolValue(params.get(PARAM_DELETE_SAME2));
			boolean other = boolValue(params.get(PARAM_MOVE_OTHER2));
			boolean clear = boolValue(params.get(PARAM_CLEAR2));
			archived = ArchiveUtils.getArchiveInfo(path, clear, same,other);
			if (archived!=null && camera!=null) {
				System.out.println("Now :"+DateUtil.date2String(new Date()));
				System.out.println("删除归档文件夹已经存在的待归档文件...");
				camera.scanSameFilesWith(archived);
			}
			if (archived!=null && camera!=null && boolValue(params.get(PARAM_EXECUTE))) {
				ArchiveUtils.executeArchive(camera,archived);
			}
			if (archived!=null && boolValue(params.get(PARAM_THUMB2))) {
				System.out.println("Now :"+DateUtil.date2String(new Date()));
				System.out.println("建立缩略图文件<"+archived.getPath()+">...");
				archived.createThumbFiles();
			}
			v = params.get(PARAM_THUMB);
			if(v!=null) ArchiveUtils.syncThumbOrientation(archived,v.toString());
		}
		v = params.get(PARAM_RAW);
		if (v!=null && archived!=null) {
			List<String> args = new ArrayList<String>(){{
				add("--ext");
				add("xmp");
			}};
			String path = v.toString();
			ArchiveInfo raw = new ArchiveInfo(path, args);
			raw.sortInfos();
			raw.saveInfos();
			ArchiveUtils.syncExifAttributesByTime(archived, raw);
			raw.saveInfos();
		}

		v = params.get(PARAM_VIEW_DIR);
		if (v!=null) {
			try {
				checkDeletedFile(new File(v.toString(), ArchiveUtils.same_photo_log).getCanonicalPath());
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
