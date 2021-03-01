package qinyoyo.photoinfo.exiftool;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Key {

    // Exif

    APERTURE("ApertureValue", Double.class),
    ARTIST("IFD0:Artist", String.class,"创作者"),
    COLOR_SPACE("ColorSpace", Integer.class),
    CONTRAST("Contrast", Integer.class),
    CREATEDATE("CreateDate", String.class, "创建时间"),
    SUB_SEC_TIME_CREATE("SubSecTimeDigitized",String.class,"创建时间毫秒值"),
    DATETIMEORIGINAL("DateTimeOriginal", String.class, "拍摄时间"),

    DIGITAL_ZOOM_RATIO("DigitalZoomRatio", Double.class),
    EXIF_VERSION("ExifVersion", String.class),
    EXPOSURE_COMPENSATION("ExposureCompensation", Double.class),
    EXPOSURE_PROGRAM("ExposureProgram", Integer.class),
    EXPOSURE_TIME("ExposureTime", Double.class),

    FILE_NAME("fileName",String.class),
    FILE_SIZE("fileSize",String.class),
    FILE_TYPE("FileType", String.class),

    FLASH("Flash", Integer.class),
    FOCAL_LENGTH("FocalLength", Double.class),
    FOCAL_LENGTH_35MM("FocalLengthIn35mmFormat", Integer.class),

    GPS_ALTITUDE("GPSAltitude", String.class,"海拔"),
    GPS_ALTITUDE_REF("GPSAltitudeRef", Integer.class),
    GPS_BEARING("GPSDestBearing", Double.class),
    GPS_BEARING_REF("GPSDestBearingRef", String.class),
    GPS_LATITUDE("GPSLatitude", Double.class,"纬度"),
    GPS_LATITUDE_REF("GPSLatitudeRef", String.class),
    GPS_LONGITUDE("GPSLongitude", Double.class,"经度"),
    GPS_LONGITUDE_REF("GPSLongitudeRef", String.class),
    GPS_PROCESS_METHOD("GPSProcessingMethod", String.class),
    GPS_SPEED("GPSSpeed", Double.class),
    GPS_SPEED_REF("GPSSpeedRef", String.class),
    GPS_TIMESTAMP("GPSTimeStamp", String.class),

    IMAGE_HEIGHT("ImageHeight", Integer.class,"图像高度"),
    IMAGE_WIDTH("ImageWidth", Integer.class,"图像宽度"),

    IPTC_KEYWORDS("IPTC:Keywords", String.class, 64),

    IPTCDigest("IPTCDigest", String.class),
    DOCUMENT_ID("OriginalDocumentID",String.class),

    ISO("ISO", Integer.class),

    LENS_ID("LensID", String.class,"镜头"),
    LENS_MAKE("LensMake", String.class),
    LENS_MODEL("LensModel", String.class),
    MAKE("Make", String.class),
    METERING_MODE("MeteringMode", Integer.class),
    MIME_TYPE("MIMEType", String.class),
    MODEL("Model", String.class,"拍摄设备"),
    MODIFYDATE("ModifyDate", String.class, "called DateTime by the EXIF spec"),
    OFFSETTIME("OffsetTime", String.class, "time zone for ModifyDate"),
    OFFSETTIMEORIGINAL("OffsetTimeOriginal", String.class, "time zone for DateTimeOriginal"),
    ORIENTATION("Orientation", String.class,"拍摄方向"),
    OWNER_NAME("OwnerName", String.class),
    RATING("xmp:Rating", Integer.class),
    RATING_PERCENT("RatingPercent", Integer.class),
    ROTATION("Rotation", Integer.class),
    SATURATION("Saturation", Integer.class),
    SENSING_METHOD("SensingMethod", Integer.class),
    SHARPNESS("Sharpness", Integer.class),
    SHUTTER_SPEED("ShutterSpeedValue", Double.class),
    SOFTWARE("Software", String.class),
    SUBJECT("XPSubject", String.class),
    SUB_SEC_TIME_ORIGINAL("SubSecTimeOriginal", Integer.class,"连拍毫秒值"),

    WHITE_BALANCE("WhiteBalance", Integer.class),
    X_RESOLUTION("XResolution", Double.class),
    Y_RESOLUTION("YResolution", Double.class),

    COUNTRY("IPTC:Country-PrimaryLocationName",String.class,"国家",64),
    STATE("IPTC:Province-State",String.class, "省/州",32),
    CITY("IPTC:City",String.class,"城市/县",32),
    LOCATION("IPTC:Sub-location",String.class,"地址",32),

    SUBJECT_CODE("XMP-iptcCore:SubjectCode",String.class,"IPTC主题/POI/Step"),  // IPTC 主题， 记录 POI， step
    SCENE("XMP-iptcCore:Scene",String.class,"场景代码"),  // 场景代码
    CATEGORY("IPTC:Category",String.class,3),  // 类别

    TITLE("XMP-dc:Title", String.class, "标题"),  // 标题
    OBJECT_NAME("IPTC:ObjectName", String.class,"标题",64),  // 标题
    DESCRIPTION("IPTC:Caption-Abstract",String.class,"说明，副标题", 2000),   // 说明，副标题
    HEADLINE("IPTC:Headline",String.class,"提要Headline",256),  // 题要
    ;

    private static final Map<String, Key> ENTRY_MAP = Arrays.stream(Key.values()).collect(Collectors.toMap(Key::getName, k -> k));

    private final String notes;
    private final Class<?> clazz;
    private final String name;
    private final int maxLength;

    Key(String name, Class<?> clazz) {
        this(name, clazz, "",0);
    }

    Key(String name, Class<?> clazz, String notes) {
        this(name,clazz,notes,0);
    }
    Key(String name, Class<?> clazz, int len) {
        this(name,clazz,"",len);
    }
    Key(String name, Class<?> clazz, String notes, int length) {
        this.name = name;
        this.clazz = clazz;
        this.notes = notes;
        maxLength = length;
    }
    @SuppressWarnings("unchecked")
    public static <T> T parse(Key key, String value) {
        Class<?> type = key.clazz;
        if (Boolean.class.isAssignableFrom(type)) {
            return (T) Boolean.valueOf(value);
        } else if (Integer.class.isAssignableFrom(type)) {
            return (T) Integer.valueOf(value);
        } else if (Long.class.isAssignableFrom(type)) {
            return (T) Long.valueOf(value);
        } else if (Double.class.isAssignableFrom(type)) {
            return (T) Double.valueOf(value);
        } else if (String.class.isAssignableFrom(type)) {
            return (T) value;
        }

        throw new UnsupportedOperationException(String.format("Parsing not implemented for ExifTool name %s with class %s.", key.name, type));
    }

    public static Optional<Key> findKeyWithName(String name) {
        if (name==null) return Optional.empty();
        final String lname = name.toLowerCase();
        return ENTRY_MAP.entrySet().stream()
                .filter(entry -> {
                    String key = entry.getKey().toLowerCase();
                    return key.equals(lname) || key.contains(":"+lname);
                })
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static String getName(Key key) {
        return key.name;
    }
    public static String getShortName(Key key) {
        String name = key.name.toLowerCase();
        if (name.lastIndexOf(":")>=0) return name.substring(name.lastIndexOf(":")+1);
        else return name;
    }
    public static String getNotes(Key key) {
        return key.notes==null ? key.name : key.notes;
    }

    public static int getMaxLength(Key key) {
        return key.maxLength;
    }

}
