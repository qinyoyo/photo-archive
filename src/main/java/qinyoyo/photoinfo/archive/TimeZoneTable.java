package qinyoyo.photoinfo.archive;

import java.util.*;

/**
 * Created by Qinyoyo on 2021/3/23.
 */
public class TimeZoneTable {
    public static final Map<String,String[]> countryTimeZones = new HashMap<String,String[]>() {{
        put("AD",new String[]{"Andorra","安道尔","Europe/Andorra"});
        put("AE",new String[]{"United Arab Emirates","阿联酋","Asia/Dubai"});
        put("AF",new String[]{"Afghanistan","阿富汗","Asia/Kabul"});
        put("AG",new String[]{"Antigua and Barbuda","安提瓜和巴布达岛","America/Antigua"});
        put("AI",new String[]{"Anguilla","安圭拉","America/Anguilla"});
        put("AL",new String[]{"Albania","阿尔巴尼亚","Europe/Tirane"});
        put("AM",new String[]{"Armenia","亚美尼亚","Asia/Yerevan"});
        put("AO",new String[]{"Angola","安哥拉","Africa/Luanda"});
        put("AQ",new String[]{"Antarctica","南极洲","Antarctica/Troll","Antarctica/Syowa","Antarctica/Rothera","Antarctica/Mawson",
                "Antarctica/DumontDUrville","Antarctica/Davis","Antarctica/Casey","Antarctica/McMurdo","Antarctica/Vostok"});
        put("AR",new String[]{"Argentina","阿根廷","America/Argentina/Buenos_Aires"});
        put("AS",new String[]{"American Samoa","美属萨摩亚","Pacific/Pago_Pago"});
        put("AT",new String[]{"Austria","奥地利","Europe/Vienna"});
        put("AU",new String[]{"Australia","澳大利亚","Australia/Melbourne","Australia/Perth","Australia/Darwin","Australia/Eucla","Antarctica/Macquarie","Australia/Lord_Howe"});
        put("AW",new String[]{"Aruba","阿鲁巴","America/Aruba"});
        put("AX",new String[]{"Åland Islands","奥兰群岛","Europe/Mariehamn"});
        put("AZ",new String[]{"Azerbaijan","阿塞拜疆","Asia/Baku"});
        put("BA",new String[]{"Bosnia and Herzegovina","波黑","Europe/Sarajevo"});
        put("BB",new String[]{"Barbados","巴巴多斯","America/Barbados"});
        put("BD",new String[]{"Bangladesh","孟加拉国","Asia/Dhaka"});
        put("BE",new String[]{"Belgium","比利时","Europe/Brussels"});
        put("BF",new String[]{"Burkina Faso","布基纳法索","Africa/Ouagadougou"});
        put("BG",new String[]{"Bulgaria","保加利亚","Europe/Sofia"});
        put("BH",new String[]{"Bahrain","巴林","Asia/Bahrain"});
        put("BI",new String[]{"Burundi","布隆迪","Africa/Bujumbura"});
        put("BJ",new String[]{"Benin","贝宁","Africa/Porto-Novo"});
        put("BL",new String[]{"Saint Barthélemy","圣巴泰勒米","America/St_Barthelemy"});
        put("BM",new String[]{"Bermuda","百慕大群岛","Atlantic/Bermuda"});
        put("BN",new String[]{"Brunei Darussalam","文莱","Asia/Brunei"});
        put("BO",new String[]{"Bolivia","玻利维亚","America/La_Paz"});
        put("BQ",new String[]{"Bonaire","博内尔岛","America/Kralendijk"});
        put("BR",new String[]{"Brazil","巴西","America/Sao_Paulo","America/Rio_Branco","America/Porto_Velho","America/Noronha"});
        put("BS",new String[]{"Bahamas","巴哈马","America/Nassau"});
        put("BT",new String[]{"Bhutan","不丹","Asia/Thimphu"});
        put("BW",new String[]{"Botswana","博茨瓦纳","Africa/Gaborone"});
        put("BY",new String[]{"Belarus","白俄罗斯","Europe/Minsk"});
        put("BZ",new String[]{"Belize","伯利兹","America/Belize"});
        put("CA",new String[]{"Canada","加拿大","Canada/Central","Canada/Newfoundland","Canada/Atlantic","Canada/Eastern","Canada/Pacific","Canada/Mountain"});
        put("CC",new String[]{"Cocos (Keeling) Islands","科科斯群岛","Indian/Cocos"});
        put("CD",new String[]{"Congo","刚果民主共和国","Africa/Kinshasa","Africa/Lubumbashi"});
        put("CF",new String[]{"Central African Republic","中非共和国","Africa/Bangui"});
        put("CG",new String[]{"Congo","刚果","Africa/Brazzaville"});
        put("CH",new String[]{"Switzerland","瑞士","Europe/Zurich"});
        put("CI",new String[]{"Côte d'Ivoire","象牙海岸","Africa/Abidjan"});
        put("CK",new String[]{"Cook Islands","库克群岛","Pacific/Rarotonga"});
        put("CL",new String[]{"Chile","智利","America/Santiago","Pacific/Easter","America/Punta_Arenas"});
        put("CM",new String[]{"Cameroon","喀麦隆","Africa/Douala"});
        put("CN",new String[]{"China","中国","Asia/Shanghai"});
        put("CO",new String[]{"Colombia","哥伦比亚","America/Bogota"});
        put("CR",new String[]{"Costa Rica","哥斯达黎加","America/Costa_Rica"});
        put("CU",new String[]{"Cuba","古巴","America/Havana"});
        put("CV",new String[]{"Cape Verde","佛得角","Atlantic/Cape_Verde"});
        put("CW",new String[]{"Curaçao","库拉索岛","America/Curacao"});
        put("CX",new String[]{"Christmas Island","圣诞岛","Indian/Christmas"});
        put("CY",new String[]{"Cyprus","塞浦路斯","Asia/Nicosia"});
        put("CZ",new String[]{"Czech Republic","捷克共和国","Europe/Prague"});
        put("DE",new String[]{"Germany","德国","Europe/Berlin"});
        put("DJ",new String[]{"Djibouti","吉布提","Africa/Djibouti"});
        put("DK",new String[]{"Denmark","丹麦","Europe/Copenhagen"});
        put("DM",new String[]{"Dominica","多米尼加","America/Dominica"});
        put("DO",new String[]{"Dominican Republic","多明尼加共和国","America/Santo_Domingo"});
        put("DZ",new String[]{"Algeria","阿尔及利亚","Africa/Algiers"});
        put("EC",new String[]{"Ecuador","厄瓜多尔","Pacific/Galapagos","America/Guayaquil"});
        put("EE",new String[]{"Estonia","爱沙尼亚","Europe/Tallinn"});
        put("EG",new String[]{"Egypt","埃及","Africa/Cairo"});
        put("EH",new String[]{"Western Sahara","西撒哈拉","Africa/El_Aaiun"});
        put("ER",new String[]{"Eritrea","厄立特里亚","Africa/Asmara"});
        put("ES",new String[]{"Spain","西班牙","Europe/Madrid"});
        put("ET",new String[]{"Ethiopia","埃塞俄比亚","Africa/Addis_Ababa"});
        put("FI",new String[]{"Finland","芬兰","Europe/Helsinki"});
        put("FJ",new String[]{"Fiji","斐济","Pacific/Fiji"});
        put("FK",new String[]{"Falkland Islands (Malvinas)","福克兰群岛","Atlantic/Stanley"});
        put("FM",new String[]{"Micronesia","密克罗尼西亚联邦","Pacific/Pohnpei","Pacific/Chuuk"});
        put("FO",new String[]{"Faroe Islands","法罗群岛","Atlantic/Faroe"});
        put("FR",new String[]{"France","法国","Europe/Paris"});
        put("GA",new String[]{"Gabon","加蓬","Africa/Libreville"});
        put("GB",new String[]{"United Kingdom","大不列颠联合王国","Europe/London"});
        put("GD",new String[]{"Grenada","格林纳达","America/Grenada"});
        put("GE",new String[]{"Georgia","格鲁吉亚共和国","Asia/Tbilisi"});
        put("GF",new String[]{"French Guiana","法属圭亚那","America/Cayenne"});
        put("GG",new String[]{"Guernsey","根西岛","Europe/Guernsey"});
        put("GH",new String[]{"Ghana","加纳","Africa/Accra"});
        put("GI",new String[]{"Gibraltar","直布罗陀","Europe/Gibraltar"});
        put("GL",new String[]{"Greenland","格陵兰岛","America/Thule","America/Scoresbysund","America/Danmarkshavn"});
        put("GM",new String[]{"Gambia","冈比亚","Africa/Banjul"});
        put("GN",new String[]{"Guinea","几内亚","Africa/Conakry"});
        put("GP",new String[]{"Guadeloupe","瓜德罗普岛","America/Guadeloupe"});
        put("GQ",new String[]{"Equatorial Guinea","赤道几内亚","Africa/Malabo"});
        put("GR",new String[]{"Greece","希腊","Europe/Athens"});
        put("GS",new String[]{"South Georgia and the South Sandwich Islands","南乔治亚岛和南威奇群岛","Atlantic/South_Georgia"});
        put("GT",new String[]{"Guatemala","危地马拉","America/Guatemala"});
        put("GU",new String[]{"Guam","关岛","Pacific/Guam"});
        put("GW",new String[]{"Guinea-Bissau","几内亚比绍","Africa/Bissau"});
        put("GY",new String[]{"Guyana","圭亚那","America/Guyana"});
        put("HK",new String[]{"Hong Kong","香港","Asia/Hong_Kong"});
        put("HN",new String[]{"Honduras","洪都拉斯","America/Tegucigalpa"});
        put("HR",new String[]{"Croatia","克罗地亚","Europe/Zagreb"});
        put("HT",new String[]{"Haiti","海地","America/Port-au-Prince"});
        put("HU",new String[]{"Hungary","匈牙利","Europe/Budapest"});
        put("ID",new String[]{"Indonesia","印度尼西亚","Asia/Jakarta","Asia/Jayapura","Asia/Makassar"});
        put("IE",new String[]{"Ireland","爱尔兰","Europe/Dublin"});
        put("IL",new String[]{"Israel","以色列","Asia/Jerusalem"});
        put("IM",new String[]{"Isle of Man","马恩岛","Europe/Isle_of_Man"});
        put("IN",new String[]{"India","印度","Asia/Kolkata"});
        put("IO",new String[]{"British Indian Ocean Territory","英属印度洋领地","Indian/Chagos"});
        put("IQ",new String[]{"Iraq","伊拉克","Asia/Baghdad"});
        put("IR",new String[]{"Iran","伊朗","Asia/Tehran"});
        put("IS",new String[]{"Iceland","冰岛","Atlantic/Reykjavik"});
        put("IT",new String[]{"Italy","意大利","Europe/Rome"});
        put("JE",new String[]{"Jersey","泽西","Europe/Jersey"});
        put("JM",new String[]{"Jamaica","牙买加","America/Jamaica"});
        put("JO",new String[]{"Jordan","乔丹","Asia/Amman"});
        put("JP",new String[]{"Japan","日本","Asia/Tokyo"});
        put("KE",new String[]{"Kenya","肯尼亚","Africa/Nairobi"});
        put("KG",new String[]{"Kyrgyzstan","吉尔吉斯斯坦","Asia/Bishkek"});
        put("KH",new String[]{"Cambodia","柬埔寨","Asia/Phnom_Penh"});
        put("KI",new String[]{"Kiribati","基里巴斯","Pacific/Kiritimati","Pacific/Enderbury","Pacific/Tarawa"});
        put("KM",new String[]{"Comoros","科摩罗","Indian/Comoro"});
        put("KN",new String[]{"Saint Kitts and Nevis","尼维斯","America/St_Kitts"});
        put("KP",new String[]{"Democratic People's Republic of Korea","朝鲜","Asia/Pyongyang"});
        put("KR",new String[]{"Korea","韩国","Asia/Seoul"});
        put("KW",new String[]{"Kuwait","科威特","Asia/Kuwait"});
        put("KY",new String[]{"Cayman Islands","开曼群岛","America/Cayman"});
        put("KZ",new String[]{"Kazakhstan","哈萨克斯坦","Asia/Aqtau","Asia/Qyzylorda"});
        put("LA",new String[]{"Lao People's Democratic Republic","老挝","Asia/Vientiane"});
        put("LB",new String[]{"Lebanon","黎巴嫩","Asia/Beirut"});
        put("LC",new String[]{"Saint Lucia","圣露西亚","America/St_Lucia"});
        put("LI",new String[]{"Liechtenstein","列支敦士登","Europe/Vaduz"});
        put("LK",new String[]{"Sri Lanka","斯里兰卡","Asia/Colombo"});
        put("LR",new String[]{"Liberia","利比里亚","Africa/Monrovia"});
        put("LS",new String[]{"Lesotho","莱索托","Africa/Maseru"});
        put("LT",new String[]{"Lithuania","立陶宛","Europe/Vilnius"});
        put("LU",new String[]{"Luxembourg","卢森堡","Europe/Luxembourg"});
        put("LV",new String[]{"Latvia","拉脱维亚","Europe/Riga"});
        put("LY",new String[]{"Libya","利比亚","Africa/Tripoli"});
        put("MA",new String[]{"Morocco","摩洛哥","Africa/Casablanca"});
        put("MC",new String[]{"Monaco","摩纳哥","Europe/Monaco"});
        put("MD",new String[]{"Moldova","摩尔多瓦共和国","Europe/Chisinau"});
        put("ME",new String[]{"Montenegro","黑山","Europe/Podgorica"});
        put("MF",new String[]{"Saint Martin (French part)","法属圣马丁","America/Marigot"});
        put("MG",new String[]{"Madagascar","马达加斯加","Indian/Antananarivo"});
        put("MH",new String[]{"Marshall Islands","马绍尔群岛","Pacific/Majuro"});
        put("MK",new String[]{"Macedonia","马其顿","Europe/Skopje"});
        put("ML",new String[]{"Mali","马里","Africa/Bamako"});
        put("MM",new String[]{"Myanmar","缅甸","Asia/Yangon"});
        put("MN",new String[]{"Mongolia","蒙古","Asia/Ulaanbaatar","Asia/Hovd"});
        put("MO",new String[]{"Macao","澳门","Asia/Macau"});
        put("MP",new String[]{"Northern Mariana Islands","北马里亚纳群岛","Pacific/Saipan"});
        put("MQ",new String[]{"Martinique","马提尼克","America/Martinique"});
        put("MR",new String[]{"Mauritania","毛里塔尼亚","Africa/Nouakchott"});
        put("MS",new String[]{"Montserrat","蒙特塞拉特岛","America/Montserrat"});
        put("MT",new String[]{"Malta","马耳他","Europe/Malta"});
        put("MU",new String[]{"Mauritius","毛里求斯","Indian/Mauritius"});
        put("MV",new String[]{"Maldives","马尔代夫","Indian/Maldives"});
        put("MW",new String[]{"Malawi","马拉维","Africa/Blantyre"});
        put("MX",new String[]{"Mexico","墨西哥","America/Mexico_City","America/Tijuana","America/Mazatlan","America/Cancun"});
        put("MY",new String[]{"Malaysia","马来西亚","Asia/Kuala_Lumpur"});
        put("MZ",new String[]{"Mozambique","莫桑比克","Africa/Maputo"});
        put("NA",new String[]{"Namibia","纳米比亚","Africa/Windhoek"});
        put("NC",new String[]{"New Caledonia","新喀里多尼亚","Pacific/Noumea"});
        put("NE",new String[]{"Niger","尼日尔","Africa/Niamey"});
        put("NF",new String[]{"Norfolk Island","诺福克岛","Pacific/Norfolk"});
        put("NG",new String[]{"Nigeria","尼日利亚","Africa/Lagos"});
        put("NI",new String[]{"Nicaragua","尼加拉瓜","America/Managua"});
        put("NL",new String[]{"Netherlands","荷兰","Europe/Amsterdam"});
        put("NO",new String[]{"Norway","挪威","Europe/Oslo"});
        put("NP",new String[]{"Nepal","尼泊尔","Asia/Kathmandu"});
        put("NR",new String[]{"Nauru","瑙鲁","Pacific/Nauru"});
        put("NU",new String[]{"Niue","纽埃","Pacific/Niue"});
        put("NZ",new String[]{"New Zealand","新西兰","Pacific/Auckland","Pacific/Chatham"});
        put("OM",new String[]{"Oman","阿曼","Asia/Muscat"});
        put("PA",new String[]{"Panama","巴拿马","America/Panama"});
        put("PE",new String[]{"Peru","秘鲁","America/Lima"});
        put("PF",new String[]{"French Polynesia","法属波利尼西亚","Pacific/Gambier","Pacific/Marquesas","Pacific/Tahiti"});
        put("PG",new String[]{"Papua New Guinea","新几内亚","Pacific/Bougainville","Pacific/Port_Moresby"});
        put("PH",new String[]{"Philippines","菲律宾","Asia/Manila"});
        put("PK",new String[]{"Pakistan","巴基斯坦","Asia/Karachi"});
        put("PL",new String[]{"Poland","波兰","Europe/Warsaw"});
        put("PM",new String[]{"Saint Pierre and Miquelon","圣皮埃尔和密克隆","America/Miquelon"});
        put("PN",new String[]{"Pitcairn","皮特凯恩","Pacific/Pitcairn"});
        put("PR",new String[]{"Puerto Rico","波多黎各","America/Puerto_Rico"});
        put("PS",new String[]{"Palestinian Territory","巴勒斯坦","Asia/Hebron"});
        put("PT",new String[]{"Portugal","葡萄牙","Europe/Lisbon","Atlantic/Azores"});
        put("PW",new String[]{"Palau","帕劳","Pacific/Palau"});
        put("PY",new String[]{"Paraguay","巴拉圭","America/Asuncion"});
        put("QA",new String[]{"Qatar","卡塔尔","Asia/Qatar"});
        put("RE",new String[]{"Réunion","留尼汪","Indian/Reunion"});
        put("RO",new String[]{"Romania","罗马尼亚","Europe/Bucharest"});
        put("RS",new String[]{"Serbia","塞尔维亚","Europe/Belgrade"});
        put("RU",new String[]{"Russian Federation","俄罗斯","Europe/Moscow","Asia/Omsk","Asia/Yekaterinburg","Europe/Samara","Europe/Kaliningrad"
                ,"Asia/Kamchatka","Asia/Sakhalin","Asia/Vladivostok","Asia/Yakutsk","Asia/Irkutsk","Asia/Tomsk"});
        put("RW",new String[]{"Rwanda","卢旺达","Africa/Kigali"});
        put("SA",new String[]{"Saudi Arabia","沙特阿拉伯","Asia/Riyadh"});
        put("SB",new String[]{"Solomon Islands","所罗门群岛","Pacific/Guadalcanal"});
        put("SC",new String[]{"Seychelles","塞舌尔","Indian/Mahe"});
        put("SD",new String[]{"Sudan","苏丹","Africa/Khartoum"});
        put("SE",new String[]{"Sweden","瑞典","Europe/Stockholm"});
        put("SG",new String[]{"Singapore","新加坡","Asia/Singapore"});
        put("SH",new String[]{"Saint Helena, Ascension and Tristan da Cunha","阿森松岛","Atlantic/St_Helena"});
        put("SI",new String[]{"Slovenia","斯洛文尼亚","Europe/Ljubljana"});
        put("SJ",new String[]{"Svalbard and Jan Mayen","斯瓦尔巴岛和扬马延岛","Arctic/Longyearbyen"});
        put("SK",new String[]{"Slovakia","斯洛伐克","Europe/Bratislava"});
        put("SL",new String[]{"Sierra Leone","塞拉利昂","Africa/Freetown"});
        put("SM",new String[]{"San Marino","圣马力诺","Europe/San_Marino"});
        put("SN",new String[]{"Senegal","塞内加尔","Africa/Dakar"});
        put("SO",new String[]{"Somalia","索马里","Africa/Mogadishu"});
        put("SR",new String[]{"Suriname","苏里南","America/Paramaribo"});
        put("SS",new String[]{"South Sudan","南苏丹","Africa/Juba"});
        put("ST",new String[]{"Sao Tome and Principe","圣多美和普林西比","Africa/Sao_Tome"});
        put("SV",new String[]{"El Salvador","萨尔瓦多","America/El_Salvador"});
        put("SX",new String[]{"Sint Maarten (Dutch part)","Sint Maarten","America/Lower_Princes"});
        put("SY",new String[]{"Syrian Arab Republic","叙利亚","Asia/Damascus"});
        put("SZ",new String[]{"Swaziland","斯威士兰","Africa/Mbabane"});
        put("TC",new String[]{"Turks and Caicos Islands","土耳其和凯科斯群岛","America/Grand_Turk"});
        put("TD",new String[]{"Chad","乍得","Africa/Ndjamena"});
        put("TF",new String[]{"French Southern Territories","法属南部领地","Indian/Kerguelen"});
        put("TG",new String[]{"Togo","多哥","Africa/Lome"});
        put("TH",new String[]{"Thailand","泰国","Asia/Bangkok"});
        put("TJ",new String[]{"Tajikistan","塔吉克斯坦","Asia/Dushanbe"});
        put("TK",new String[]{"Tokelau","托克劳","Pacific/Fakaofo"});
        put("TL",new String[]{"Timor-Leste","东帝汶","Asia/Dili"});
        put("TM",new String[]{"Turkmenistan","土库曼斯坦","Asia/Ashgabat"});
        put("TN",new String[]{"Tunisia","突尼斯","Africa/Tunis"});
        put("TO",new String[]{"Tonga","汤加","Pacific/Tongatapu"});
        put("TR",new String[]{"Turkey","土耳其","Europe/Istanbul"});
        put("TT",new String[]{"Trinidad and Tobago","特立尼达和多巴哥","America/Port_of_Spain"});
        put("TV",new String[]{"Tuvalu","图瓦卢","Pacific/Funafuti"});
        put("TW",new String[]{"Taiwan","台湾","Asia/Taipei"});
        put("TZ",new String[]{"Tanzania","坦桑尼亚","Africa/Dar_es_Salaam"});
        put("UA",new String[]{"Ukraine","乌克兰","Europe/Kiev","Europe/Simferopol"});
        put("UG",new String[]{"Uganda","乌干达","Africa/Kampala"});
        put("UM",new String[]{"United States Minor Outlying Islands","美国本土外小岛屿","Pacific/Wake","美国本土外小岛屿","Pacific/Midway"});
        put("US",new String[]{"United States","美国","US/Eastern","US/Central","US/Aleutian","US/Alaska","US/Mountain",
                "US/Arizona","US/Pacific","US/Hawaii"});
        put("UY",new String[]{"Uruguay","乌拉圭","America/Montevideo"});
        put("UZ",new String[]{"Uzbekistan","乌兹别克斯坦","Asia/Tashkent","Asia/Samarkand"});
        put("VA",new String[]{"Vatican","梵蒂冈","Europe/Vatican"});
        put("VC",new String[]{"Saint Vincent and the Grenadines","圣文森特和格林纳丁斯","America/St_Vincent"});
        put("VE",new String[]{"Venezuela","委内瑞拉","America/Caracas"});
        put("VG",new String[]{"Virgin Islands, British","英属群岛（英属）群岛","America/Tortola"});
        put("VI",new String[]{"Virgin Islands, U.S.","美属维尔京群岛","America/St_Thomas"});
        put("VN",new String[]{"Viet Nam","越南","Asia/Ho_Chi_Minh"});
        put("VU",new String[]{"Vanuatu","瓦努阿图","Pacific/Efate"});
        put("WF",new String[]{"Wallis and Futuna","瓦利斯和富图纳","Pacific/Wallis"});
        put("WS",new String[]{"Samoa","萨摩亚","Pacific/Apia"});
        put("YE",new String[]{"Yemen","也门","Asia/Aden"});
        put("YT",new String[]{"Mayotte","马约特","Indian/Mayotte"});
        put("ZA",new String[]{"South Africa","南非","Africa/Johannesburg"});
        put("ZM",new String[]{"Zambia","赞比亚","Africa/Lusaka"});
        put("ZW",new String[]{"Zimbabwe","津巴布韦","Africa/Harare"});
    }};
    public static TimeZone getTimeZone(String country, String state,String city,Double longitude ) {
        if (country==null || country.trim().isEmpty()) {
            if (longitude!=null) return getTimeZone(longitude);
            else return null;
        }
        String c = country.trim().toUpperCase();
        if (c.equals("CN") || c.equals("CHINA") || c.equals("中国"))  return TimeZone.getTimeZone("Asia/Shanghai");
        else if (( c.equals("US") || c.equals("USA") || c.equals("美国")
                || c.matches(".*\\bUNITED\\s+STATES\\b.*") ) ) {
            String id=timeIdForUS(state,city);
            if (id==null) id="US/Eastern";
            return TimeZone.getTimeZone(id);
        } else if (( c.equals("CA") || c.equals("CAN") || c.equals("加拿大")
                || c.equals("CANADA") ) ) {
            String id=timeIdForCA(state,longitude);
            if (id==null) id="Canada/Central";
            return TimeZone.getTimeZone(id);
        } else {
            String id = timeIdJustFromCountry(c);
            if (id != null) return TimeZone.getTimeZone(id);
        }
        return null;
    }
    static String zoneString(TimeZone zone,long time) {
        if (zone!=null) {
            String nm=zone.getDisplayName();
            long now=(time==0?new Date().getTime():time);
            int offset=zone.getOffset(now)/1000;
            String sign=(offset>=0)?"+":"-";
            offset = Math.abs(offset);
            int h= offset / 3600;
            int m = (offset % 3600)/60;
            nm = nm + " UTC"+sign+h;
            if (m>0) nm=nm+":"+m;
            return nm;
        } else return null;
    }
    static int  timeZoneInt(double longitude){
        return (longitude>=0)?(int)((longitude+7.5)/15.0):-(int)((-longitude+7.5)/15.0);
    }
    static TimeZone getTimeZone(double longitude) {
        // 73°33′E至135°05′E
        if (longitude>73.55 && longitude<135.083334) return TimeZone.getTimeZone("Asia/Chongqing");
        int tz=timeZoneInt(longitude);
        if(tz>=0)  {
            String s=String.format("GMT+%02d:00",tz);
            return TimeZone.getTimeZone(s);
        }
        else {
            String s=String.format("GMT-%02d:00",-tz);
            return TimeZone.getTimeZone(s);
        }
    }
    static String timeIdForUS(String state,String city) {
        if (state==null) return null;
        String s=state.toLowerCase();
        if (s.matches(".*\\balaska\\b.*")) {
            if (city != null && city.toLowerCase().contains("aleutians")) return "US/Aleutian";
            else return "US/Alaska";
        } else if (s.matches(".*\\barizona\\b.*")) {
            if (city != null && city.toLowerCase().contains("navajo")) return "US/Mountain";
            else return "US/Arizona";
        } 
        else if (s.matches(".*\\bcalifornia\\b.*")) return "US/Pacific" ;
        else if (s.matches(".*\\bnevada\\b.*")) return "US/Pacific" ;
        else if (s.matches(".*\\boregon\\b.*")) return "US/Pacific" ;
        else if (s.matches(".*\\bwashington\\b.*")) return "US/Pacific" ;
        else if (s.matches(".*\\bcolorado\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\bidaho\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\billinois\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\biowa\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\bmontana\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\bnew\\s+mexico\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\btexas\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\butah\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\bwyoming\\b.*")) return "US/Mountain" ;
        else if (s.matches(".*\\balabama\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\barkansas\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bdelaware\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bkansas\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\blouisiana\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bmississippi\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bmissouri\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bnebraska\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bnorth\\s+dakota\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bsouth\\s+dakota\\b.*")) return "US/Central" ;
        else if (s.matches(".*\\bhawaii\\b.*")) return "US/Hawaii" ;
        else return "US/Eastern";
    }
    static String timeIdForCA(String state,Double longitude) {
        if (state==null) return null;
        String s=state.toLowerCase();
        if (s.matches(".*\\bnewfoundland\\s+and\\s+labrador\\b.*")) return "Canada/Newfoundland" ;
        else if (s.matches(".*\\bnova\\s+scotia\\b.*")) return "Canada/Atlantic" ;
        else if (s.matches(".*\\bnew\\s+brunswic\\b.*")) return "Canada/Atlantic" ;
        else if (s.matches(".*\\bprince\\s+edward\\s+island\\b.*")) return "Canada/Atlantic" ;
        else if (s.matches(".*\\bqubec\\b.*")) return "Canada/Eastern" ;
        else if (s.matches(".*\\bnunavut\\b.*") || s.matches(".*\\bontario\\b.*")) {
            if (longitude == null) return "Canada/Central";
            else {
                int tz = timeZoneInt(longitude);
                if (tz == -5) return "Canada/Eastern";
                else return "Canada/Central";
            }
        }
        else if (s.matches(".*\\bmanitoba\\b.*")) return "Canada/Central" ;
        else if (s.matches(".*\\bsaskatchewan\\b.*") || s.matches(".*\\bnorthwest\\s+territories\\b.*") || s.matches(".*\\balberta\\b.*")) return "Canada/Mountain" ;
        else if (s.matches(".*\\byukon\\b.*") || s.matches(".*\\bbritish\\s+columbia\\b.*")) return "Canada/Pacific" ;
        else return "Canada/Central";
    }
    static String timeIdJustFromCountry(String countryCode) {
        if (countryCode==null) return null;
        String c=countryCode.toUpperCase();
        if (c.matches("[A-Z][A-Z]")) {
            String [] params = countryTimeZones.get(c);
            if (params!=null && params.length>2) return params[2];
        } else for (String code : countryTimeZones.keySet()) {
            String [] params = countryTimeZones.get(code);
            if (params!=null && params.length>2 && (params[0].toUpperCase().equals(c) || params[1].equals(c))) return params[2];
        }
        return null;
    }
}
