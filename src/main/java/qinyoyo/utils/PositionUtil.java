package qinyoyo.utils;

/* 各地图API坐标系统比较与转换;
        * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,
        * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）;
        * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
        * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系; BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系;
        * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。 chenhua
        */
public class PositionUtil {
    public static final String WGS84 = "wgs84";
    public static final String GCJ02 = "gcj02";
    public static final String BD09 = "bd09";
    static public class LatLng {
        public double longitude;
        public double latitude;
        public String type;
        public LatLng(double lat, double lng) {
            latitude = lat;
            longitude = lng;
            type = WGS84;
        }
        public LatLng(double lat,double lng,String type) {
            latitude = lat;
            longitude = lng;
            if (GCJ02.equals(type)) this.type = GCJ02;
            else if (BD09.equals(type)) this.type = BD09;
            else this.type=WGS84;
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

    public static LatLng wgs84ToGcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new LatLng(lat, lon, GCJ02);
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
        return new LatLng(mgLat, mgLon, GCJ02);
    }
    public static LatLng gcj02ToWgs84(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new LatLng(lat, lon, WGS84);
        }
        LatLng gps = transform(lat, lon);
        double lontitude = lon * 2 - gps.longitude;
        double latitude = lat * 2 - gps.latitude;
        return new LatLng(latitude, lontitude, WGS84);
    }
    public static LatLng gcj02ToBd09(double gg_lat, double gg_lon) {
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new LatLng(bd_lat, bd_lon, BD09);
    }
    public static LatLng bd09ToGcj02(double bd_lat, double bd_lon) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new LatLng(gg_lat, gg_lon, GCJ02);
    }
    public static LatLng wgs84ToBd09(double lat, double lon) {
        if (outOfChina(lat, lon)) return new LatLng(lat, lon, BD09);
        LatLng gcj= wgs84ToGcj02(lat,lon);
        return gcj02ToBd09(gcj.latitude, gcj.longitude);
    }
    public static LatLng bd09ToWgs84(double bd_lat, double bd_lon) {
        if (outOfChina(bd_lat, bd_lon)) return new LatLng(bd_lat, bd_lon, WGS84);
        LatLng gcj02 = bd09ToGcj02(bd_lat, bd_lon);
        return gcj02ToWgs84(gcj02.latitude,gcj02.longitude);
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

    static private double EARTHRADIUS = 6370996.81;
    static private double [] MCBAND = new double[]{12890594.86, 8362377.87, 5591021, 3481989.83, 1678043.12, 0};
    static private int[] LLBAND = new int[]{86, 60, 45, 30, 15, 0};
    static private double[][] MC2LL = new double[][] {{1.410526172116255e-8, 0.00000898305509648872, -1.9939833816331, 200.9824383106796, -187.2403703815547, 91.6087516669843, -23.38765649603339, 2.57121317296198, -0.03801003308653, 17337981.2},
            {-7.435856389565537e-9, 0.000008983055097726239, -0.78625201886289, 96.32687599759846, -1.85204757529826, -59.36935905485877, 47.40033549296737, -16.50741931063887, 2.28786674699375, 10260144.86},
            {-3.030883460898826e-8, 0.00000898305509983578, 0.30071316287616, 59.74293618442277, 7.357984074871, -25.38371002664745, 13.45380521110908, -3.29883767235584, 0.32710905363475, 6856817.37},
            {-1.981981304930552e-8, 0.000008983055099779535, 0.03278182852591, 40.31678527705744, 0.65659298677277, -4.44255534477492, 0.85341911805263, 0.12923347998204, -0.04625736007561, 4482777.06},
            {3.09191371068437e-9, 0.000008983055096812155, 0.00006995724062, 23.10934304144901, -0.00023663490511, -0.6321817810242, -0.00663494467273, 0.03430082397953, -0.00466043876332, 2555164.4},
            {2.890871144776878e-9, 0.000008983055095805407, -3.068298e-8, 7.47137025468032, -0.00000353937994, -0.02145144861037, -0.00001234426596, 0.00010322952773, -0.00000323890364, 826088.5}};

    static private double[][] LL2MC = new double[][]
            {{-0.0015702102444, 111320.7020616939, 1704480524535203.0, -10338987376042340.0, 26112667856603880.0, -35149669176653700.0, 26595700718403920.0, -10725012454188240.0, 1800819912950474.0, 82.5},
                    {0.0008277824516172526, 111320.7020463578, 647795574.6671607, -4082003173.641316, 10774905663.51142, -15171875531.51559, 12053065338.62167, -5124939663.577472, 913311935.9512032, 67.5},
                    {0.00337398766765, 111320.7020202162, 4481351.045890365, -23393751.19931662, 79682215.47186455, -115964993.2797253, 97236711.15602145, -43661946.33752821, 8477230.501135234, 52.5},
                    {0.00220636496208, 111320.7020209128, 51751.86112841131, 3796837.749470245, 992013.7397791013, -1221952.21711287, 1340652.697009075, -620943.6990984312, 144416.9293806241, 37.5},
                    {-0.0003441963504368392, 111320.7020576856, 278.2353980772752, 2485758.690035394, 6070.750963243378, 54821.18345352118, 9540.606633304236, -2710.55326746645, 1405.483844121726, 22.5},
                    {-0.0003218135878613132, 111320.7020701615, 0.00369383431289, 823725.6402795718, 0.46104986909093, 2351.343141331292, 1.58060784298199, 8.77738589078284, 0.37238884252424, 7.45}};

    static private class HT {
        double lng;
        double lat;
        HT latLng;
        public HT(double lng,double lat) {
            this.lng=lng;
            this.lat=lat;
            latLng=null;
        }
        public boolean equals(HT i) {
            if (i==null) {
                return false;
            }
            double hS = Math.abs(this.lat - i.lat);
            double T = Math.abs(this.lng - i.lng);
            double e = 1e-8;
            if (hS < e && T < e) {
                return true;
            }
            return false;
        }
    }
    static private double getLoop(double T, double i, double e) {
        while (T > e) {
            T -= e - i;
        }
        while (T < i) {
            T += e - i;
        }
        return T;
    }
    static private double fG(double T, double i, double e) {
        if (T < i) {
            T = i;
        } else {
            if (T > e) {
                T = e;
            }
        }
        return T;
    }
    static private HT convertor(HT T, double[]hS) {
        if (T==null || hS==null) {
            return null;
        }
        double e = hS[0] + hS[1] * Math.abs(T.lng);
        double i = Math.abs(T.lat) / hS[9];
        double hT = hS[2] + hS[3] * i + hS[4] * i * i + hS[5] * i * i * i + hS[6] * i * i * i * i + hS[7] * i * i * i * i * i + hS[8] * i * i * i * i * i * i;
        e *= (T.lng < 0 ? -1 : 1);
        hT *= (T.lat < 0 ? -1 : 1);
        return new HT(e,hT);
    }
    static private HT convertLL2MC(LatLng hV) {
        if (hV==null) {
            return new HT(0,0);
        }
        double hX = hV.latitude;
        double hS = hV.longitude;
        hS = getLoop(hV.longitude, -180, 180);
        hX = fG(hX, -85, 85);
        double[] hU = null;
        for (int hT = 0; hT < LLBAND.length; hT++) {
            if (hX >= LLBAND[hT]) {
                hU = LL2MC[hT];
                break;
            }
        }
        if (hU==null) {
            for (int hT = 0; hT < LLBAND.length; hT++) {
                if (hX <= -LLBAND[hT]) {
                    hU = LL2MC[hT];
                    break;
                }
            }
        }
        HT T = new HT(hS,hX);
        HT hW = convertor(T, hU);
        HT e = new HT(hW.lng,hW.lat);
        e.latLng = c5(hV.latitude,hV.longitude);
        return e;
    }
    static private HT c5(double i, double e) {
        if (i < -90) {
            i = -90;
        } else {
            if (i > 90) {
                i = 90;
            }
        }
        while (e < -180) {
            e += 360;
        }
        while (e > 180) {
            e -= 360;
        }
        return new HT(e, i);
    }
    static private HT convertMC2LL(HT e) {
        if (e == null) {
            return new HT(0,0);
        }
        double[] hT = null;
        HT T = new HT(Math.abs(e.lng),Math.abs(e.lat));
        for (int hS = 0; hS < MCBAND.length; hS++) {
            if (T.lat >= MCBAND[hS]) {
                hT = MC2LL[hS];
                break;
            }
        }
        HT hU = convertor(e, hT);
        return c5(hU.lat,hU.lng);
    }
    static private double dL(double e) {
        return e * Math.PI / 180;
    }
    static private double getDistanceByMC(HT hU, HT hS) {
        if (hU==null || hS==null) {
            return 0;
        }
        hU = convertMC2LL(hU);
        if (hU==null) {
            return 0;
        }
        double i = dL(hU.lng);
        double hT = dL(hU.lat);
        hS = convertMC2LL(hS);
        if (hS==null) {
            return 0;
        }
        double e = dL(hS.lng);
        double T = dL(hS.lat);
        return EARTHRADIUS * Math.acos((Math.sin(hT) * Math.sin(T) + Math.cos(hT) * Math.cos(T) * Math.cos(e - i)));
    }
    static private double getDistanceIn(HT hT, HT e) {
        if (hT==null || e==null) {
            return 0;
        }
        if (hT.equals(e)) {
            return 0;
        }
        double i = getDistanceByMC(hT, e);
        return i;
    }
    static double distanceBetween(LatLng a, LatLng b) {
        if (a==null || b==null) return 0;
        HT i = convertLL2MC(a);
        HT hS = convertLL2MC(b);
        double e = getDistanceIn(i, hS);
        return e;
    }
}