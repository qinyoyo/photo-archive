package qinyoyo.utils;

/* 各地图API坐标系统比较与转换;
        * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,
        * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）;
        * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
        * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系; BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系;
        * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。 chenhua
        */
public class PositionUtil {
    static public class LatLng {
        public double longitude;
        public double latitude;
        public LatLng(double lat, double lng) {
            latitude = lat;
            longitude = lng;
        }
    }
    private static final double pi = 3.1415926535897932384626;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;
    private static final LatLng[] chinaBound = new LatLng[] {
            new LatLng(39.895004,124.359406),
            new LatLng(40.907676,126.125547),
            new LatLng(41.732924,126.944227),
            new LatLng(41.429018,127.33057),
            new LatLng(41.318149,128.158448),
            new LatLng(41.629482,128.397613),
            new LatLng(41.966774,128.158448),
            new LatLng(41.980503,129.013923),
            new LatLng(42.377348,129.418663),
            new LatLng(42.425073,129.814205),
            new LatLng(42.961151,129.970582),
            new LatLng(42.890169,130.145356),
            new LatLng(42.390987,130.632885),
            new LatLng(42.418257,130.701875),
            new LatLng(42.707265,130.582292),
            new LatLng(42.724223,130.476508),
            new LatLng(42.832646,131.023827),
            new LatLng(42.957773,131.203201),
            new LatLng(43.47247,131.377975),
            new LatLng(44.039221,131.34578),
            new LatLng(44.846499,131.106615),
            new LatLng(45.091379,131.727524),
            new LatLng(45.250799,131.943692),
            new LatLng(44.996817,132.964742),
            new LatLng(45.159757,133.176311),
            new LatLng(45.594129,133.530459),
            new LatLng(48.381803,135.22761),
            new LatLng(48.479753,135.190815),
            new LatLng(47.913948,132.743974),
            new LatLng(47.690833,131.216999),
            new LatLng(48.869652,130.720272),
            new LatLng(49.41282,129.579639),
            new LatLng(49.711995,127.73991),
            new LatLng(53.109767,125.707008),
            new LatLng(53.566739,123.444141),
            new LatLng(53.286532,120.804129),
            new LatLng(52.603079,119.994648),
            new LatLng(52.474073,120.684546),
            new LatLng(50.369585,119.09318),
            new LatLng(50.281251,119.341544),
            new LatLng(49.973745,118.743632),
            new LatLng(49.574603,117.823767),
            new LatLng(49.851971,116.739476),
            new LatLng(48.863583,116.072574),
            new LatLng(48.811968,116.095571),
            new LatLng(48.51949,115.801214),
            new LatLng(48.252956,115.82881),
            new LatLng(48.142256,115.55745),
            new LatLng(47.907763,115.594244),
            new LatLng(47.675304,115.975988),
            new LatLng(47.790114,116.132365),
            new LatLng(47.852068,116.288742),
            new LatLng(47.83349,116.481914),
            new LatLng(47.861355,116.946446),
            new LatLng(47.619358,117.410977),
            new LatLng(47.985017,117.875509),
            new LatLng(47.923223,118.593004),
            new LatLng(46.772925,119.862417),
            new LatLng(46.671706,119.632451),
            new LatLng(46.779245,118.896559),
            new LatLng(46.576639,117.46157),
            new LatLng(46.576639,117.46157),
            new LatLng(46.386002,116.790069),
            new LatLng(45.867923,116.228951),
            new LatLng(45.429204,114.812359),
            new LatLng(44.777744,113.644131),
            new LatLng(45.071827,112.448307),
            new LatLng(45.078345,112.061963),
            new LatLng(44.337078,111.372065),
            new LatLng(43.673117,111.868792),
            new LatLng(42.758126,110.351015),
            new LatLng(42.452328,107.490235),
            new LatLng(41.870587,104.565065),
            new LatLng(42.48638,101.860662),
            new LatLng(42.771682,96.405864),
            new LatLng(44.973968,94.133798),
            new LatLng(45.545672,91.043052),
            new LatLng(47.985017,90.251968),
            new LatLng(49.241464,87.749936),
            new LatLng(49.0967,86.811673),
            new LatLng(48.403246,85.818219),
            new LatLng(47.100567,85.487068),
            new LatLng(47.263632,83.058625),
            new LatLng(45.545672,82.267541),
            new LatLng(45.286525,82.488309),
            new LatLng(45.33845,81.844403),
            new LatLng(44.895559,79.912687),
            new LatLng(43.345045,80.648579),
            new LatLng(42.615609,80.188647),
            new LatLng(42.09708,80.023071),
            new LatLng(40.022427,73.712798),
            new LatLng(38.506521,73.823182),
            new LatLng(38.332831,74.871828),
            new LatLng(37.252464,75.000609),
            new LatLng(37.193613,74.393498),
            new LatLng(36.928209,74.559074),
            new LatLng(36.63222,75.53413),
            new LatLng(35.662321,76.601174),
            new LatLng(35.331314,77.907382),
            new LatLng(32.665439,79.176795),
            new LatLng(32.540853,78.974425),
            new LatLng(32.634309,78.772055),
            new LatLng(32.447299,78.404109),
            new LatLng(31.095641,78.882438),
            new LatLng(29.900768,81.163703),
            new LatLng(30.252798,81.66043),
            new LatLng(27.795155,86.038987),
            new LatLng(27.925966,88.780184),
            new LatLng(27.27032,88.872171),
            new LatLng(27.204538,89.203322),
            new LatLng(28.089256,89.810433),
            new LatLng(27.696941,91.539779),
            new LatLng(26.726449,92.036506),
            new LatLng(26.891539,93.931427),
            new LatLng(28.170807,95.918335),
            new LatLng(27.615029,97.077365),
            new LatLng(27.827873,97.482106),
            new LatLng(28.268585,97.647681),
            new LatLng(28.105571,98.089216),
            new LatLng(27.401766,98.420368),
            new LatLng(25.69752,98.365176),
            new LatLng(24.827544,97.537297),
            new LatLng(23.714402,97.555695),
            new LatLng(23.917522,98.622738),
            new LatLng(22.078081,99.193054),
            new LatLng(21.373256,100.388878),
            new LatLng(21.062726,101.87906),
            new LatLng(22.249455,101.750278),
            new LatLng(22.489024,104.022345),
            new LatLng(22.766569,104.151126),
            new LatLng(22.809218,104.744438),
            new LatLng(22.941344,104.877819),
            new LatLng(23.086106,104.850223),
            new LatLng(23.166934,105.535522),
            new LatLng(22.890214,105.89427),
            new LatLng(22.7751,106.795737),
            new LatLng(22.587297,106.643959),
            new LatLng(21.953702,106.703751),
            new LatLng(21.545486,107.437343),
            new LatLng(21.459397,108.210029),
            new LatLng(21.045455,108.430797),
            new LatLng(20.022922,107.823686),
            new LatLng(17.631759,108.145639),
            new LatLng(16.152744,109.332264),
            new LatLng(15.074022,109.930177),
            new LatLng(12.231536,110.371712),
            new LatLng(6.855216,108.421598),
            new LatLng(2.980728,109.599025),
            new LatLng(3.202672,111.843495),
            new LatLng(7.039065,115.633339),
            new LatLng(10.770837,118.724084),
            new LatLng(17.517052,119.533565),
            new LatLng(22.796425,119.202414),
            new LatLng(26.528021,121.520474),
            new LatLng(31.063971,123.985711),
            new LatLng(34.146718,123.029052),
            new LatLng(37.765434,123.470587),
            new LatLng(37.765434,123.470587)
    };

    public static LatLng gps84_To_Gcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new LatLng(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new LatLng(mgLat, mgLon);
    }

    public static LatLng gps84_To_Bd09(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new LatLng(lat, lon);
        }
        LatLng gcj=gps84_To_Gcj02(lat,lon);
        return gcj02_To_Bd09(gcj.latitude, gcj.longitude);
    }

    public static LatLng gcj_To_Gps84(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new LatLng(lat, lon);
        }
        LatLng gps = transform(lat, lon);
        double lontitude = lon * 2 - gps.longitude;
        double latitude = lat * 2 - gps.latitude;
        return new LatLng(latitude, lontitude);
    }

    public static LatLng bd09_To_Gps84(double bd_lat, double bd_lon) {
        if (outOfChina(bd_lat, bd_lon)) {
            return new LatLng(bd_lat, bd_lon);
        }
        LatLng gcj02 = PositionUtil.bd09_To_Gcj02(bd_lat, bd_lon);
        LatLng map84 = PositionUtil.gcj_To_Gps84(gcj02.latitude,gcj02.longitude);
        return map84;
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param gg_lat
     * @param gg_lon
     */
    private static LatLng gcj02_To_Bd09(double gg_lat, double gg_lon) {
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new LatLng(bd_lat, bd_lon);
    }

    /**
     * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
     * bd_lat * @param bd_lon * @return
     */
    private static LatLng bd09_To_Gcj02(double bd_lat, double bd_lon) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new LatLng(gg_lat, gg_lon);
    }

    private static LatLng transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new LatLng(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new LatLng(mgLat, mgLon);
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }

    private static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return !isPointInPolygon(lon,lat,chinaBound);
    }

    private static boolean isPointInPolygon ( double px , double py,LatLng [] bounds )
    {
        boolean isInside = false;
        double ESP = 1e-9;
        int count = 0;
        double linePoint1x;
        double linePoint1y;
        double linePoint2x = 180;
        double linePoint2y;

        linePoint1x = px;
        linePoint1y = py;
        linePoint2y = py;

        for (int i = 0; i < bounds.length - 1; i++)
        {
            double cx1 = bounds[i].longitude;
            double cy1 = bounds[i].latitude;
            double cx2 = bounds[i+1].longitude;
            double cy2 = bounds[i+1].latitude;
            if ( isPointOnLine(px, py, cx1, cy1, cx2, cy2) )
            {
                return true;
            }
            if ( Math.abs(cy2 - cy1) < ESP )
            {
                continue;
            }

            if ( isPointOnLine(cx1, cy1, linePoint1x, linePoint1y, linePoint2x, linePoint2y) )
            {
                if ( cy1 > cy2 )
                    count++;
            }
            else if ( isPointOnLine(cx2, cy2, linePoint1x, linePoint1y, linePoint2x, linePoint2y) )
            {
                if ( cy2 > cy1 )
                    count++;
            }
            else if ( isIntersect(cx1, cy1, cx2, cy2, linePoint1x, linePoint1y, linePoint2x, linePoint2y) )
            {
                count++;
            }
        }
        if ( count % 2 == 1 )
        {
            isInside = true;
        }

        return isInside;
    }

    static double Multiply ( double px0 , double py0 , double px1 , double py1 , double px2 , double py2 )
    {
        return ((px1 - px0) * (py2 - py0) - (px2 - px0) * (py1 - py0));
    }

    static boolean isPointOnLine ( double px0 , double py0 , double px1 , double py1 , double px2 , double py2 )
    {
        boolean flag = false;
        double ESP = 1e-9;
        if ( (Math.abs(Multiply(px0, py0, px1, py1, px2, py2)) < ESP) && ((px0 - px1) * (px0 - px2) <= 0)
                && ((py0 - py1) * (py0 - py2) <= 0) )
        {
            flag = true;
        }
        return flag;
    }

    static boolean isIntersect ( double px1 , double py1 , double px2 , double py2 , double px3 , double py3 , double px4 ,
                                 double py4 )
    {
        boolean flag = false;
        double d = (px2 - px1) * (py4 - py3) - (py2 - py1) * (px4 - px3);
        if ( d != 0 )
        {
            double r = ((py1 - py3) * (px4 - px3) - (px1 - px3) * (py4 - py3)) / d;
            double s = ((py1 - py3) * (px2 - px1) - (px1 - px3) * (py2 - py1)) / d;
            if ( (r >= 0) && (r <= 1) && (s >= 0) && (s <= 1) )
            {
                flag = true;
            }
        }
        return flag;
    }
}