package qinyoyo.photoinfo.archive;

public class Point {
    public static final String WGS84 = "wgs84";
    public static final String GCJ02 = "gcj02";
    public static final String BD09 = "bd09";
    double lng;
    double lat;
    String type;
    public Point(double lng,double lat,String type) {
        this.lng=lng;
        this.lat=lat;
        if (GCJ02.equals(type)) this.type = GCJ02;
        else if (BD09.equals(type)) this.type = BD09;
        else this.type=WGS84;
    }
    public Point(double lng,double lat) {
        this(lng,lat,WGS84);
    }
}
