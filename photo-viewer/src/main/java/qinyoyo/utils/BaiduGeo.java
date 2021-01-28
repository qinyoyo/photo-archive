package qinyoyo.utils;

import org.json.JSONObject;

import java.io.IOException;

public class BaiduGeo {
    private static final String address = "http://api.map.baidu.com/geocoder/v2/?location=%.6f,%.6f&output=json&pois=0&ak=%s";
    private static final String defaultAK = "0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o";
    public JSONObject getGeoInfo(double longitude, double latitude, String ak) throws IOException {
        String url = String.format(address,latitude,longitude, ak==null?defaultAK:ak);
        return HttpUtils.doJsonGet(url,null);
    }
}
