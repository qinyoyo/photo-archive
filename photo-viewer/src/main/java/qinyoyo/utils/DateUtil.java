package qinyoyo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class DateUtil {
	/**
	 * 字符串转换为日期 
	 * @param dt    日期字符串
	 * @param fmt   格式，null时为标准格式 yyyy-MM-dd "+(pm?"hh":"HH")+":mm:ss
	 * @return 日期对象
	 */
	static private final String amString=new SimpleDateFormat("a").format(string2Date("19701116060606"));
	static private final String pmString=new SimpleDateFormat("a").format(string2Date("19701116161616"));
	static public Date string2Date(String dt, String fmt) {
		if (dt == null)	return null;
		if ("{}".equals(dt)) {
			return null;
		}
		dt=dt.trim();
		if (dt.isEmpty()) return null;
		try {
			if (fmt!=null)
				return new SimpleDateFormat(fmt).parse(dt);
			else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  //获取时区
				try {
					return sdf.parse(dt);
				} catch (Exception e) {
				}
				int len = dt.length();
	            if( Pattern.matches("[0-9]+",dt)){  // 全部是数字
					if (len==14) return new SimpleDateFormat("yyyyMMddHHmmss").parse(dt);
					else if (len==12) return new SimpleDateFormat("yyyyMMddHHmm").parse(dt);
					else if (len==10) return new SimpleDateFormat("yyyyMMddHH").parse(dt);
					else if (len==8) return new SimpleDateFormat("yyyyMMdd").parse(dt);
					else if (len==6) return new SimpleDateFormat("HHmmss").parse(dt);
					else if (len==4) return new SimpleDateFormat("HHmm").parse(dt);
					else return null;
	            }
				String ndt=dt.toLowerCase().replace("/","-").replace("am","").replace(amString.toLowerCase(),"").trim();
				String sdt=null,sfmt=null;
				boolean pm=false;
				if (ndt.indexOf(pmString.toLowerCase())>=0) {
					pm=true;
					ndt=ndt.replace(pmString.toLowerCase(),"").trim();
				} else if (ndt.indexOf("pm")>=0) {
					pm=true;
					ndt=ndt.replace("pm","").trim();
				} 
				if (Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.\\d\\d:\\d\\d:\\d\\d.*",ndt)) {
					sfmt="yyyy-MM-dd "+(pm?"hh":"HH")+":mm:ss"+(pm?"a":"");
					sdt=ndt.substring(0, 10)+" "+ndt.substring(11, 19)+(pm?pmString:"");
				}
				else if (Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.\\d\\d:\\d\\d.*",ndt)) {
					sfmt="yyyy-MM-dd "+(pm?"hh":"HH")+":mm"+(pm?"a":"");
					sdt=ndt.substring(0, 10)+" "+ndt.substring(11, 16)+(pm?pmString:"");
				}
				else if (Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.\\d\\d.*",ndt)) {
					sfmt="yyyy-MM-dd "+(pm?"hh":"HH")+(pm?"a":"");
					sdt=ndt.substring(0, 10)+" "+ndt.substring(11, 13)+(pm?pmString:"");
				}
				else if (Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.*",ndt)) {
					sfmt="yyyy-MM-dd";
					sdt=ndt.substring(0, 10);
				}
				else if (Pattern.matches("\\d\\d-\\d\\d.*",ndt)) {
					sfmt="MM-dd";
					sdt=ndt.substring(0, 5);
				}
				else if (Pattern.matches("\\d\\d:\\d\\d:\\d\\d.*",ndt)) {
					sfmt=(pm?"hh":"HH")+":mm:ss"+(pm?"a":"");
					sdt=ndt.substring(0, 8)+(pm?pmString:"");
				}
				else if (Pattern.matches("\\d\\d:\\d\\d.*",ndt)) {
					sfmt=(pm?"hh":"HH")+":mm"+(pm?"a":"");
					sdt=ndt.substring(0, 5)+(pm?pmString:"");
				}
				if (sfmt!=null) {
					return new SimpleDateFormat(sfmt).parse(sdt);
				}
				else return new SimpleDateFormat().parse(dt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	static public Date string2Date(String dt) {
		return string2Date(dt,null);
	}
	/**
	 * 得到时间的礼拜数 
	 * @param dt    日期
	 * @return 礼拜数，0,1，..6对应礼拜日，一，...六
	 */
	static public int weekOf(Date dt) {
		Calendar ca = Calendar.getInstance();
		ca.setFirstDayOfWeek(Calendar.MONDAY);
		if (dt != null)
			ca.setTime(dt);
		return ca.get(Calendar.DAY_OF_WEEK) - 1; // 0:星期日，1::星期一...
	}

	public static final int DAY = 1;
	public static final int WEEK = 2;
	public static final int MONTH = 3;
	public static final int QUARTER = 4;
	public static final int YEAR = 5;

	/**
	 * 根据日期求与其相关的阶段开始时间 
	 * @param dt   日期，null表示当前
	 * @param mode     阶段类型 ,DAY，WEEK，MONTH，QUARTER，YEAR
	 * @return 该阶段开始时间，一周以礼拜一开始
	 */
	static public Date startOf(Date dt, int mode) {
		Calendar ca = Calendar.getInstance();
		if (dt != null)
			ca.setTime(dt);
		switch (mode) {
		case WEEK:
			ca.setFirstDayOfWeek(Calendar.MONDAY);
			ca.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			break;
		case MONTH:
			ca.set(Calendar.DAY_OF_MONTH, 1);
			break;
		case QUARTER:
			ca.set(Calendar.DAY_OF_MONTH, 1);
			int i = ca.get(Calendar.MONTH) / 3;
			ca.set(Calendar.MONTH, 3 * i);
			break;
		case YEAR:
			ca.set(Calendar.DAY_OF_MONTH, 1);
			ca.set(Calendar.MONTH, Calendar.JANUARY);
			break;
		default:
		}
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		return ca.getTime();
	}

	/**
	 * 根据日期求与其相关的阶段结束时间 
	 * @param dt   日期，null表示当前
	 * @param mode   阶段类型 ,DAY，WEEK，MONTH，QUARTER，YEAR
	 * @return 该阶段结束时间，一周以礼拜一开始
	 */
	static public Date endOf(Date dt, int mode) {
		Calendar ca = Calendar.getInstance();
		if (dt != null)
			ca.setTime(dt);
		switch (mode) {
		case WEEK:
			ca.setFirstDayOfWeek(Calendar.MONDAY);
			ca.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			break;
		case MONTH:
			ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
			break;
		case QUARTER:
			int i = ca.get(Calendar.MONTH) / 3;
			ca.set(Calendar.MONTH, i * 3 + 2);
			ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
			break;
		case YEAR:
			ca.set(Calendar.DAY_OF_MONTH, 31);
			ca.set(Calendar.MONTH, Calendar.DECEMBER);
			break;
		default:
		}
		ca.set(Calendar.HOUR_OF_DAY, 23);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		ca.set(Calendar.MILLISECOND, 999);
		return ca.getTime();
	}

	/**
	 * 根据日期增加一段后的时间 
	 * @param dt   日期，null表示当前
	 * @param amount    位移量
	 * @param mode    阶段类型 ,DAY，WEEK，MONTH，QUARTER，YEAR
	 * @return 位移后的时间
	 */
	static public Date dateAdd(Date dt, int amount, int mode) {
		Calendar ca = Calendar.getInstance();
		if (dt != null)
			ca.setTime(dt);
		switch (mode) {
		case WEEK:
			ca.add(Calendar.DATE, amount * 7);
			break;
		case MONTH:
			ca.add(Calendar.MONTH, amount);
			break;
		case QUARTER:
			ca.add(Calendar.MONTH, amount * 3);
			break;
		case YEAR:
			ca.add(Calendar.YEAR, amount);
			break;
		default:
			ca.add(Calendar.DATE, amount);
		}
		return ca.getTime();
	}

	/**
	 * 上一个工作日 
	 * @param dt     日期，null表示当前
	 * @return 上一个工作日(非周六周日)
	 */
	static public Date prevWorkDay(Date dt) {
		int week = weekOf(dt);
		if (week == 0)
			return dateAdd(dt, -2, DAY);
		else if (week == 1)
			return dateAdd(dt, -3, DAY);
		else
			return dateAdd(dt, -1, DAY);
	}
	/**
	 * 根据生日计算年龄周岁 
	 * @param birth 生日
	 * @return 周岁
	 */
	static public Integer age(Date birth) {
		if (birth==null) return null;
		Calendar ca = Calendar.getInstance();
		ca.setTime(birth);
		int by = ca.get(Calendar.YEAR), bm = ca.get(Calendar.MONTH), bd = ca.get(Calendar.DAY_OF_MONTH);
		ca.setTime(new Date());
		int y = ca.get(Calendar.YEAR), m = ca.get(Calendar.MONTH), d = ca.get(Calendar.DAY_OF_MONTH);
		int age = y - by;
		if (m < bm)
			age--;
		else if (m == bm && d < bd)
			age--;
		return age;
	}

	/**
	 * 时间转换为字符串
	 * @param dt      日期
	 * @param fmt    格式，null时为标准格式 yyyy-MM-dd "+(pm?"hh":"HH")+":mm:ss
	 * @return 字符串
	 */
	static public String date2String(Date dt, String fmt) {
		if (dt == null)
			return null;
		return new SimpleDateFormat(fmt == null ? "yyyy-MM-dd HH:mm:ss" : fmt).format(dt);
	}
}
