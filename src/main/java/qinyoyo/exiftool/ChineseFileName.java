package qinyoyo.exiftool;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChineseFileName {
	File folder;
	Map<Integer,String> originalNameMap;
	public static String indexName(int index) {
		return String.format("_Pa_%06d_aP_", index);		
	}
	private String[] splitName(String name) {
		int pos = name.lastIndexOf(".");
		if (pos>=0) return new String[] {name.substring(0,pos),name.substring(pos)};
		else return new String[] {name,""};
	}
	private boolean isAsciiCharsOf(String str) {
		for (int i=0;i<str.length();i++) {
			char ch=str.charAt(i);
			if (ch<0 || ch>128) return false;
		}
		return true;
	}
	public String getOrignalName(String name) {
		if (name!=null && originalNameMap.size()>0) {
			Pattern p = Pattern.compile("_Pa_(\\d{6})_aP_");
			Matcher m = p.matcher(name);
			if (m.find()) {
				int index = Integer.parseInt(m.group(1));
				String oriName = originalNameMap.get(index);
				if (oriName!=null) return oriName;
			}
		} 
		return name;
	}
	public ChineseFileName(File dir) {
		originalNameMap = new HashMap<>();
		if (dir!=null && dir.isDirectory() && dir.exists()) {
			folder = dir;
			File[] files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return !pathname.isDirectory() && !isAsciiCharsOf(pathname.getName());
				}
			});
			if (files!=null && files.length>0) {				
				for (int i=0;i<files.length;i++) {
					try {
						String oriName = files[i].getName();
						String [] names = splitName(oriName);
						files[i].renameTo(new File(dir,indexName(i)+names[1]));
						originalNameMap.put(i, oriName);
					} catch (Exception e) {}
				}
			}
		} else folder=null;
	}
	public void reverse() {
		if (folder!=null && originalNameMap.size()>0) {
			for (int i=0;i<originalNameMap.size();i++) {
				try {
					String oriName = originalNameMap.get(i);
					String [] names = splitName(oriName);
					new File(folder,indexName(i)+names[1]).renameTo(new File(folder,oriName));
				} catch (Exception e) {}
			}
			originalNameMap.clear();
			folder=null;
		}
	}
}
