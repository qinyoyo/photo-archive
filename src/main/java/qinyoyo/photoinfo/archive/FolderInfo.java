package qinyoyo.photoinfo.archive;


import lombok.Getter;
import lombok.Setter;
import qinyoyo.photoinfo.ArchiveUtils;
import qinyoyo.utils.DateUtil;
import qinyoyo.utils.Util;

import java.util.*;

@Setter
@Getter
public class FolderInfo {
	public static final long DAY_LENGTH = 86400000;
	private String path;
	private long date0;
	private long date1;
	private Set<String> models;

	public FolderInfo(String path) {
		this.path = path;
		this.date0 = -1;
		this.date1 = -1;
		this.models = new HashSet<>();
	}

	@Override public String toString() {
		return (Util.isEmpty(path) ? "[Root]" : path)
				+ "\n   ["+(date0>=0 ? DateUtil.date2String(new Date(date0*DAY_LENGTH),"yyyy-MM-dd") : "") + " 至 "
				+     (date1>=0 ? DateUtil.date2String(new Date(date1*DAY_LENGTH),"yyyy-MM-dd") : "") + "]"
				+ (models!=null && !models.isEmpty() ? "\n   " + models : "");
	}
	public static List<FolderInfo> seekFolderInfo(ArchiveInfo archiveInfo) {
		List<FolderInfo> list = new ArrayList<>();
		Map<String,List<PhotoInfo>> grouped = new HashMap<>();
		archiveInfo.getInfos().stream().reduce(grouped,(acc,pi)->{
			if (pi.getShootTime()!=null && !ArchiveUtils.isInWebFolder(pi.getSubFolder())) {
				List<PhotoInfo> ps = acc.get(pi.getSubFolder());
				if (ps!=null) ps.add(pi);
				else acc.put(pi.getSubFolder(), new ArrayList<PhotoInfo>(){{ add(pi); }});
			}
			return acc;
		},(acc,pi)->null);
		for (String path: grouped.keySet()) {
			FolderInfo fi = new FolderInfo(path);
			grouped.get(path).forEach(p->{
				long dt = p.getShootTime().getTime() / DAY_LENGTH;
				if (fi.date0 < 0 || fi.date0>dt) fi.date0 = dt;
				if (fi.date1 < 0 || fi.date1<dt) fi.date1 = dt;
				fi.models.add(p.getModel());
			});
			list.add(fi);
		}
		return list;
	}
}
