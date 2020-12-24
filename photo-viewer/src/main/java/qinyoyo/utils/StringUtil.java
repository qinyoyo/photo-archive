package qinyoyo.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;

public class StringUtil {
	/**
	 * 对象转换为string，避免为null是的错误
	 * @param o	 对象
	 * @return	对象转换为string
	 */
	static public String toString(Object o) {
		if (o == null)
			return null;
		else if (o instanceof Date) {
			return DateUtil.date2String((Date)o, null);
		} else
			return o.toString();
	}

	static public boolean containsItem(String list,@NonNull String item){
		if (Util.isEmpty(list)) return false;
		String [] ll = list.split(",");
		return Arrays.stream(ll).anyMatch(x->item.trim().equals(x.trim()));
	}
	/**
	 * 首字符小写的字符串
	 * @param s 串
	 * @return 首字符小写的字符串
	 */
	public static String firstLetterLower(String s) {
		if (s != null && s.length() >= 1) {
			s = s.substring(0, 1).toLowerCase() + s.substring(1);
		}
		return s;
	}

	/**
	 * 首字符大写的字符串
	 * @param s 串
	 * @return 首字符大写的字符串
	 */
	public static String firstLetterUpper(String s) {
		if (s != null && s.length() >= 1) {
			s = s.substring(0, 1).toUpperCase() + s.substring(1);
		}
		return s;
	}

	/**
	 * 驼峰字符串
	 * @param s 串
	 * @return 驼峰字符串
	 */
	public static String camelString(String s) {
		if (Util.isEmpty(s))
			return s;
		s = s.trim();
		String[] ss = s.toLowerCase().replace(" ", "_").replaceAll("[-/\\\\\\.]", "_").split("_");
		String r = ss[0];
		for (int i = 1; i < ss.length; i++)
			r += firstLetterUpper(ss[i]);
		return r;
	}
	/**
	 * pascal字符串
	 * @param s 串
	 * @return pascal字符串
	 */
	public static String pascalString(String s) {
		if (Util.isEmpty(s))
			return s;
		s = s.trim();
		String[] ss = s.toLowerCase().replace(" ", "_").replaceAll("-", "_").split("_");
		String r = "";
		for (int i = 0; i < ss.length; i++)
			r += firstLetterUpper(ss[i]);
		return r;
	}
	/**
	 * kebab字符串
	 * @param s 串
	 * @return kebab字符串
	 */
	public static String kebabString(String s) {
		if (Util.isEmpty(s))
			return s;
		s = s.trim();
		String[] ss = s.toLowerCase().replace(" ", "_").replaceAll("-", "_").split("_");
		String r = ss[0];
		for (int i = 1; i < ss.length; i++)
			r += ("-"+ss[i]);
		return r;
	}
		
	/**
	 * 替换字符串指定区间
	 * @param src  源串
	 * @param left 左边界串
	 * @param right 右边界串
	 * @param newS  去替换的新串
	 * @param ignoreCase 大小写无关
	 * @param replaceIncludeLeftRight  边界串是否也一起替换
	 * @return 替换后的串
	 */
	public static String replaceBetween(String src, String left, String right, String newS, boolean ignoreCase,
			boolean replaceIncludeLeftRight) {
		if (src == null || left == null || right == null)
			return src;
		int p0 = ignoreCase ? src.toLowerCase().indexOf(left.toLowerCase()) : src.indexOf(left);
		if (p0 < 0)
			return src;
		int p1 = ignoreCase ? src.toLowerCase().indexOf(right.toLowerCase(), p0) : src.indexOf(right, p0);
		if (p1 < 0)
			return src;
		if (replaceIncludeLeftRight)
			p1 = p1 + right.length();
		else
			p0 = p0 + left.length();
		return (p0 > 0 ? src.substring(0, p0) : "") + (newS == null ? "" : newS)
				+ (p1 < src.length() ? src.substring(p1) : "");
	}
	/**
	 * 密码加密
	 * @param password password
	 * @return 字符串的加密值
	 */
	public static String encodePassword(String password) {
		if (Util.isEmpty(password))
			return password;
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		char[] charArray = password.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}
	static public String uuid() {
		return UUID.randomUUID().toString().replace("-","").toLowerCase();
	}
	static public String printStackTrace(Exception e) {
		if (e==null) return null;
		StringWriter sw = new StringWriter();  
		PrintWriter pw = new PrintWriter(sw);  
		e.printStackTrace(pw);  
		return sw.toString();
	}

	static public String regGroup(String reg,String s,int group) {
		if (Util.isEmpty(reg) || Util.isEmpty(s) || group<0) return null;
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(s);
		if (m.find() && group<=m.groupCount()) return m.group(group);
		else return null;
	}
}
