package qinyoyo.photoinfo.archive;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Qinyoyo on 2021/3/23.
 */
public class TimeZoneTable {
    public static final List<String[]> countryTimeZones = new ArrayList<String[]>() {{
        add(new String[]{"CN","China","中国","Asia/Shanghai"});
        add(new String[]{"AL","Albania","阿尔巴尼亚","Europe/Tirane"});
        add(new String[]{"DZ","Algeria","阿尔及利亚","Africa/Algiers"});
        add(new String[]{"AF","Afghanistan","阿富汗","Asia/Kabul"});
        add(new String[]{"AR","Argentina","阿根廷","America/Argentina/Buenos_Aires"});
        add(new String[]{"AE","United Arab Emirates","阿联酋","Asia/Dubai"});
        add(new String[]{"AW","Aruba","阿鲁巴","America/Aruba"});
        add(new String[]{"OM","Oman","阿曼","Asia/Muscat"});
        add(new String[]{"AZ","Azerbaijan","阿塞拜疆","Asia/Baku"});
        add(new String[]{"SH","Saint Helena, Ascension and Tristan da Cunha","阿森松岛","Atlantic/St_Helena"});
        add(new String[]{"EG","Egypt","埃及","Africa/Cairo"});
        add(new String[]{"ET","Ethiopia","埃塞俄比亚","Africa/Addis_Ababa"});
        add(new String[]{"IE","Ireland","爱尔兰","Europe/Dublin"});
        add(new String[]{"EE","Estonia","爱沙尼亚","Europe/Tallinn"});
        add(new String[]{"AD","Andorra","安道尔","Europe/Andorra"});
        add(new String[]{"AO","Angola","安哥拉","Africa/Luanda"});
        add(new String[]{"AI","Anguilla","安圭拉","America/Anguilla"});
        add(new String[]{"AG","Antigua and Barbuda","安提瓜和巴布达岛","America/Antigua"});
        add(new String[]{"AT","Austria","奥地利","Europe/Vienna"});
        add(new String[]{"AX","Åland Islands","奥兰群岛","Europe/Mariehamn"});
        add(new String[]{"AU","Australia","澳大利亚","Australia/Melbourne","Australia/Perth","Australia/Darwin","Australia/Eucla","Antarctica/Macquarie","Australia/Lord_Howe"});
        add(new String[]{"MO","Macao","澳门","Asia/Macau"});
        add(new String[]{"BB","Barbados","巴巴多斯","America/Barbados"});
        add(new String[]{"BS","Bahamas","巴哈马","America/Nassau"});
        add(new String[]{"PK","Pakistan","巴基斯坦","Asia/Karachi"});
        add(new String[]{"PY","Paraguay","巴拉圭","America/Asuncion"});
        add(new String[]{"PS","Palestina","巴勒斯坦","Asia/Hebron"});
        add(new String[]{"BH","Bahrain","巴林","Asia/Bahrain"});
        add(new String[]{"PA","Panama","巴拿马","America/Panama"});
        add(new String[]{"BR","Brazil","巴西","America/Sao_Paulo","America/Rio_Branco","America/Porto_Velho","America/Noronha"});
        add(new String[]{"BY","Belarus","白俄罗斯","Europe/Minsk"});
        add(new String[]{"BM","Bermuda","百慕大群岛","Atlantic/Bermuda"});
        add(new String[]{"BG","Bulgaria","保加利亚","Europe/Sofia"});
        add(new String[]{"MP","Northern Mariana Islands","北马里亚纳群岛","Pacific/Saipan"});
        add(new String[]{"BJ","Benin","贝宁","Africa/Porto-Novo"});
        add(new String[]{"BE","Belgium","比利时","Europe/Brussels"});
        add(new String[]{"IS","Iceland","冰岛","Atlantic/Reykjavik"});
        add(new String[]{"BO","Bolivia","玻利维亚","America/La_Paz"});
        add(new String[]{"PR","Puerto Rico","波多黎各","America/Puerto_Rico"});
        add(new String[]{"BA","Bosnia and Herzegovina","波黑","Europe/Sarajevo"});
        add(new String[]{"PL","Poland","波兰","Europe/Warsaw"});
        add(new String[]{"BW","Botswana","博茨瓦纳","Africa/Gaborone"});
        add(new String[]{"BQ","Bonaire","博内尔岛","America/Kralendijk"});
        add(new String[]{"BZ","Belize","伯利兹","America/Belize"});
        add(new String[]{"BT","Bhutan","不丹","Asia/Thimphu"});
        add(new String[]{"BF","Burkina Faso","布基纳法索","Africa/Ouagadougou"});
        add(new String[]{"BI","Burundi","布隆迪","Africa/Bujumbura"});
        add(new String[]{"KP","North Korea","朝鲜","Asia/Pyongyang"});
        add(new String[]{"GQ","Equatorial Guinea","赤道几内亚","Africa/Malabo"});
        add(new String[]{"DK","Denmark","丹麦","Europe/Copenhagen"});
        add(new String[]{"DE","Germany","德国","Europe/Berlin"});
        add(new String[]{"TL","East Timor","东帝汶","Asia/Dili"});
        add(new String[]{"TG","Togo","多哥","Africa/Lome"});
        add(new String[]{"DM","Dominica","多米尼加","America/Dominica"});
        add(new String[]{"DO","Dominican Republic","多明尼加共和国","America/Santo_Domingo"});
        add(new String[]{"RU","Russia","俄罗斯","Europe/Moscow","Asia/Omsk","Asia/Yekaterinburg","Europe/Samara","Europe/Kaliningrad","Asia/Kamchatka","Asia/Sakhalin","Asia/Vladivostok","Asia/Yakutsk","Asia/Irkutsk","Asia/Tomsk"});
        add(new String[]{"EC","Ecuador","厄瓜多尔","Pacific/Galapagos","America/Guayaquil"});
        add(new String[]{"ER","Eritrea","厄立特里亚","Africa/Asmara"});
        add(new String[]{"FR","France","法国","Europe/Paris"});
        add(new String[]{"FO","Faroe Islands","法罗群岛","Atlantic/Faroe"});
        add(new String[]{"PF","French Polynesia","法属波利尼西亚","Pacific/Gambier","Pacific/Marquesas","Pacific/Tahiti"});
        add(new String[]{"GF","French Guiana","法属圭亚那","America/Cayenne"});
        add(new String[]{"TF","French Southern Territories","法属南部领地","Indian/Kerguelen"});
        add(new String[]{"MF","Saint Martin (French part)","法属圣马丁","America/Marigot"});
        add(new String[]{"VA","Vatican","梵蒂冈","Europe/Vatican"});
        add(new String[]{"FJ","Fiji","斐济","Pacific/Fiji"});
        add(new String[]{"PH","Philippines","菲律宾","Asia/Manila"});
        add(new String[]{"FI","Finland","芬兰","Europe/Helsinki"});
        add(new String[]{"CV","Cape Verde","佛得角","Atlantic/Cape_Verde"});
        add(new String[]{"FK","Falkland Islands (Malvinas)","福克兰群岛","Atlantic/Stanley"});
        add(new String[]{"GM","Gambia","冈比亚","Africa/Banjul"});
        add(new String[]{"CG","Republic of the Congo","刚果","Africa/Brazzaville"});
        add(new String[]{"CD","Democratic Republic of the Congo","刚果民主共和国","Africa/Kinshasa","Africa/Lubumbashi"});
        add(new String[]{"CO","Colombia","哥伦比亚","America/Bogota"});
        add(new String[]{"CR","Costa Rica","哥斯达黎加","America/Costa_Rica"});
        add(new String[]{"GD","Grenada","格林纳达","America/Grenada"});
        add(new String[]{"GL","Greenland","格陵兰岛","America/Thule","America/Scoresbysund","America/Danmarkshavn"});
        add(new String[]{"GE","Georgia","格鲁吉亚共和国","Asia/Tbilisi"});
        add(new String[]{"GG","Guernsey","根西岛","Europe/Guernsey"});
        add(new String[]{"CU","Cuba","古巴","America/Havana"});
        add(new String[]{"GP","Guadeloupe","瓜德罗普岛","America/Guadeloupe"});
        add(new String[]{"GU","Guam","关岛","Pacific/Guam"});
        add(new String[]{"GY","Guyana","圭亚那","America/Guyana"});
        add(new String[]{"KZ","Kazakhstan","哈萨克斯坦","Asia/Aqtau","Asia/Qyzylorda"});
        add(new String[]{"HT","Haiti","海地","America/Port-au-Prince"});
        add(new String[]{"KR","South Korea","韩国","Asia/Seoul"});
        add(new String[]{"NL","Netherlands","荷兰","Europe/Amsterdam"});
        add(new String[]{"ME","Montenegro","黑山","Europe/Podgorica"});
        add(new String[]{"HN","Honduras","洪都拉斯","America/Tegucigalpa"});
        add(new String[]{"KI","Kiribati","基里巴斯","Pacific/Kiritimati","Pacific/Enderbury","Pacific/Tarawa"});
        add(new String[]{"DJ","Djibouti","吉布提","Africa/Djibouti"});
        add(new String[]{"KG","Kyrgyzstan","吉尔吉斯斯坦","Asia/Bishkek"});
        add(new String[]{"GN","Guinea","几内亚","Africa/Conakry"});
        add(new String[]{"GW","Guinea-Bissau","几内亚比绍","Africa/Bissau"});
        add(new String[]{"CA","Canada","加拿大","Canada/Central","Canada/Newfoundland","Canada/Atlantic","Canada/Eastern","Canada/Pacific","Canada/Mountain"});
        add(new String[]{"GH","Ghana","加纳","Africa/Accra"});
        add(new String[]{"GA","Gabon","加蓬","Africa/Libreville"});
        add(new String[]{"KH","Cambodia","柬埔寨","Asia/Phnom_Penh"});
        add(new String[]{"CZ","Czech Republic","捷克共和国","Europe/Prague"});
        add(new String[]{"ZW","Zimbabwe","津巴布韦","Africa/Harare"});
        add(new String[]{"CM","Cameroon","喀麦隆","Africa/Douala"});
        add(new String[]{"QA","Qatar","卡塔尔","Asia/Qatar"});
        add(new String[]{"KY","Cayman Islands","开曼群岛","America/Cayman"});
        add(new String[]{"CC","Cocos (Keeling) Islands","科科斯群岛","Indian/Cocos"});
        add(new String[]{"KM","Comoros","科摩罗","Indian/Comoro"});
        add(new String[]{"KT","Cote d'Ivoire","科特迪瓦","Africa/Abidjan"});
        add(new String[]{"KW","Kuwait","科威特","Asia/Kuwait"});
        add(new String[]{"HR","Croatia","克罗地亚","Europe/Zagreb"});
        add(new String[]{"KE","Kenya","肯尼亚","Africa/Nairobi"});
        add(new String[]{"CK","Cook Islands","库克群岛","Pacific/Rarotonga"});
        add(new String[]{"CW","Curaçao","库拉索岛","America/Curacao"});
        add(new String[]{"LV","Latvia","拉脱维亚","Europe/Riga"});
        add(new String[]{"LS","Lesotho","莱索托","Africa/Maseru"});
        add(new String[]{"LA","Laos","老挝","Asia/Vientiane"});
        add(new String[]{"LB","Lebanon","黎巴嫩","Asia/Beirut"});
        add(new String[]{"LR","Liberia","利比里亚","Africa/Monrovia"});
        add(new String[]{"LY","Libya","利比亚","Africa/Tripoli"});
        add(new String[]{"LT","Lithuania","立陶宛","Europe/Vilnius"});
        add(new String[]{"LI","Liechtenstein","列支敦士登","Europe/Vaduz"});
        add(new String[]{"RE","Reunion","留尼汪","Indian/Reunion"});
        add(new String[]{"LU","Luxembourg","卢森堡","Europe/Luxembourg"});
        add(new String[]{"RW","Rwanda","卢旺达","Africa/Kigali"});
        add(new String[]{"RO","Romania","罗马尼亚","Europe/Bucharest"});
        add(new String[]{"MG","Madagascar","马达加斯加","Indian/Antananarivo"});
        add(new String[]{"IM","Isle of Man","马恩岛","Europe/Isle_of_Man"});
        add(new String[]{"MT","Malta","马耳他","Europe/Malta"});
        add(new String[]{"MV","Maldives","马尔代夫","Indian/Maldives"});
        add(new String[]{"MW","Malawi","马拉维","Africa/Blantyre"});
        add(new String[]{"MY","Malaysia","马来西亚","Asia/Kuala_Lumpur"});
        add(new String[]{"ML","Mali","马里","Africa/Bamako"});
        add(new String[]{"MK","Macedonia","马其顿","Europe/Skopje"});
        add(new String[]{"MH","Marshall Islands","马绍尔群岛","Pacific/Majuro"});
        add(new String[]{"MQ","Martinique","马提尼克","America/Martinique"});
        add(new String[]{"YT","Mayotte","马约特","Indian/Mayotte"});
        add(new String[]{"MU","Mauritius","毛里求斯","Indian/Mauritius"});
        add(new String[]{"MR","Mauritania","毛里塔尼亚","Africa/Nouakchott"});
        add(new String[]{"US","United States","美国","US/Eastern","US/Central","US/Aleutian","US/Alaska","US/Mountain","US/Arizona","US/Pacific","US/Hawaii"});
        add(new String[]{"UM","United States Minor Outlying Islands","美国本土外小岛屿","Pacific/Wake","美国本土外小岛屿","Pacific/Midway"});
        add(new String[]{"AS","American Samoa","美属萨摩亚","Pacific/Pago_Pago"});
        add(new String[]{"VI","Virgin Islands, U.S.","美属维尔京群岛","America/St_Thomas"});
        add(new String[]{"MN","Mongolia","蒙古","Asia/Ulaanbaatar","Asia/Hovd"});
        add(new String[]{"MS","Montserrat","蒙特塞拉特岛","America/Montserrat"});
        add(new String[]{"BD","Bangladesh","孟加拉国","Asia/Dhaka"});
        add(new String[]{"PE","Peru","秘鲁","America/Lima"});
        add(new String[]{"FM","Micronesia","密克罗尼西亚联邦","Pacific/Pohnpei","Pacific/Chuuk"});
        add(new String[]{"MM","Myanmar","缅甸","Asia/Yangon"});
        add(new String[]{"MD","Moldova","摩尔多瓦共和国","Europe/Chisinau"});
        add(new String[]{"MA","Morocco","摩洛哥","Africa/Casablanca"});
        add(new String[]{"MC","Monaco","摩纳哥","Europe/Monaco"});
        add(new String[]{"MZ","Mozambique","莫桑比克","Africa/Maputo"});
        add(new String[]{"MX","Mexico","墨西哥","America/Mexico_City","America/Tijuana","America/Mazatlan","America/Cancun"});
        add(new String[]{"NA","Namibia","纳米比亚","Africa/Windhoek"});
        add(new String[]{"ZA","South Africa","南非","Africa/Johannesburg"});
        add(new String[]{"AQ","Antarctica","南极洲","Antarctica/Troll","Antarctica/Syowa","Antarctica/Rothera","Antarctica/Mawson","Antarctica/DumontDUrville","Antarctica/Davis","Antarctica/Casey","Antarctica/McMurdo","Antarctica/Vostok"});
        add(new String[]{"GS","South Georgia and the South Sandwich Islands","南乔治亚岛和南威奇群岛","Atlantic/South_Georgia"});
        add(new String[]{"SS","South Sudan","南苏丹","Africa/Juba"});
        add(new String[]{"NP","Nepal","尼泊尔","Asia/Kathmandu"});
        add(new String[]{"NI","Nicaragua","尼加拉瓜","America/Managua"});
        add(new String[]{"NE","Niger","尼日尔","Africa/Niamey"});
        add(new String[]{"NG","Nigeria","尼日利亚","Africa/Lagos"});
        add(new String[]{"KN","Saint Kitts and Nevis","尼维斯","America/St_Kitts"});
        add(new String[]{"NU","Niue","纽埃","Pacific/Niue"});
        add(new String[]{"NO","Norway","挪威","Europe/Oslo"});
        add(new String[]{"NF","Norfolk Island","诺福克岛","Pacific/Norfolk"});
        add(new String[]{"PW","Palau","帕劳","Pacific/Palau"});
        add(new String[]{"PN","Pitcairn","皮特凯恩","Pacific/Pitcairn"});
        add(new String[]{"PT","Portugal","葡萄牙","Europe/Lisbon","Atlantic/Azores"});
        add(new String[]{"JO","Jordan","乔丹","Asia/Amman"});
        add(new String[]{"JP","Japan","日本","Asia/Tokyo"});
        add(new String[]{"SE","Sweden","瑞典","Europe/Stockholm"});
        add(new String[]{"CH","Switzerland","瑞士","Europe/Zurich"});
        add(new String[]{"SV","El Salvador","萨尔瓦多","America/El_Salvador"});
        add(new String[]{"WS","Samoa","萨摩亚","Pacific/Apia"});
        add(new String[]{"RS","Serbia","塞尔维亚","Europe/Belgrade"});
        add(new String[]{"SL","Sierra Leone","塞拉利昂","Africa/Freetown"});
        add(new String[]{"SN","Senegal","塞内加尔","Africa/Dakar"});
        add(new String[]{"CY","Cyprus","塞浦路斯","Asia/Nicosia"});
        add(new String[]{"SC","Seychelles","塞舌尔","Indian/Mahe"});
        add(new String[]{"SA","Saudi Arabia","沙特阿拉伯","Asia/Riyadh"});
        add(new String[]{"BL","Saint-Barthelemy","圣巴泰勒米","America/St_Barthelemy"});
        add(new String[]{"CX","Christmas Island","圣诞岛","Indian/Christmas"});
        add(new String[]{"ST","Sao Tome and Principe","圣多美和普林西比","Africa/Sao_Tome"});
        add(new String[]{"LC","Saint Lucia","圣露西亚","America/St_Lucia"});
        add(new String[]{"SX","Saint Maarten","圣马丁岛","America/Lower_Princes"});
        add(new String[]{"SM","San Marino","圣马力诺","Europe/San_Marino"});
        add(new String[]{"PM","Saint Pierre and Miquelon","圣皮埃尔和密克隆","America/Miquelon"});
        add(new String[]{"VC","Saint Vincent and the Grenadines","圣文森特和格林纳丁斯","America/St_Vincent"});
        add(new String[]{"LK","Sri Lanka","斯里兰卡","Asia/Colombo"});
        add(new String[]{"SK","Slovakia","斯洛伐克","Europe/Bratislava"});
        add(new String[]{"SI","Slovenia","斯洛文尼亚","Europe/Ljubljana"});
        add(new String[]{"SJ","Svalbard and Jan Mayen","斯瓦尔巴岛和扬马延岛","Arctic/Longyearbyen"});
        add(new String[]{"SZ","Swaziland","斯威士兰","Africa/Mbabane"});
        add(new String[]{"SD","Sudan","苏丹","Africa/Khartoum"});
        add(new String[]{"SR","Suriname","苏里南","America/Paramaribo"});
        add(new String[]{"SO","Somalia","索马里","Africa/Mogadishu"});
        add(new String[]{"SB","Solomon Islands","所罗门群岛","Pacific/Guadalcanal"});
        add(new String[]{"TJ","Tajikistan","塔吉克斯坦","Asia/Dushanbe"});
        add(new String[]{"TW","Taiwan","台湾","Asia/Taipei"});
        add(new String[]{"TH","Thailand","泰国","Asia/Bangkok"});
        add(new String[]{"TZ","Tanzania","坦桑尼亚","Africa/Dar_es_Salaam"});
        add(new String[]{"TO","Tonga","汤加","Pacific/Tongatapu"});
        add(new String[]{"TT","Trinidad and Tobago","特立尼达和多巴哥","America/Port_of_Spain"});
        add(new String[]{"TN","Tunisia","突尼斯","Africa/Tunis"});
        add(new String[]{"TV","Tuvalu","图瓦卢","Pacific/Funafuti"});
        add(new String[]{"TR","Turkey","土耳其","Europe/Istanbul"});
        add(new String[]{"TC","Turks and Caicos Islands","土耳其和凯科斯群岛","America/Grand_Turk"});
        add(new String[]{"TM","Turkmenistan","土库曼斯坦","Asia/Ashgabat"});
        add(new String[]{"TK","Tokelau","托克劳","Pacific/Fakaofo"});
        add(new String[]{"WF","Wallis and Futuna","瓦利斯和富图纳","Pacific/Wallis"});
        add(new String[]{"VU","Vanuatu","瓦努阿图","Pacific/Efate"});
        add(new String[]{"GT","Guatemala","危地马拉","America/Guatemala"});
        add(new String[]{"VE","Venezuela","委内瑞拉","America/Caracas"});
        add(new String[]{"BN","Brunei Darussalam","文莱","Asia/Brunei"});
        add(new String[]{"UG","Uganda","乌干达","Africa/Kampala"});
        add(new String[]{"UA","Ukraine","乌克兰","Europe/Kiev","Europe/Simferopol"});
        add(new String[]{"UY","Uruguay","乌拉圭","America/Montevideo"});
        add(new String[]{"UZ","Uzbekistan","乌兹别克斯坦","Asia/Tashkent","Asia/Samarkand"});
        add(new String[]{"ES","Spain","西班牙","Europe/Madrid"});
        add(new String[]{"EH","Western Sahara","西撒哈拉","Africa/El_Aaiun"});
        add(new String[]{"GR","Greece","希腊","Europe/Athens"});
        add(new String[]{"HK","Hong Kong","香港","Asia/Hong_Kong"});
        add(new String[]{"CI","Côte d'Ivoire","象牙海岸","Africa/Abidjan"});
        add(new String[]{"PG","Papua New Guinea","新几内亚","Pacific/Bougainville","Pacific/Port_Moresby"});
        add(new String[]{"SG","Singapore","新加坡","Asia/Singapore"});
        add(new String[]{"NC","New Caledonia","新喀里多尼亚","Pacific/Noumea"});
        add(new String[]{"NZ","New Zealand","新西兰","Pacific/Auckland","Pacific/Chatham"});
        add(new String[]{"HU","Hungary","匈牙利","Europe/Budapest"});
        add(new String[]{"SY","Syria","叙利亚","Asia/Damascus"});
        add(new String[]{"JM","Jamaica","牙买加","America/Jamaica"});
        add(new String[]{"AM","Armenia","亚美尼亚","Asia/Yerevan"});
        add(new String[]{"YE","Yemen","也门","Asia/Aden"});
        add(new String[]{"IQ","Iraq","伊拉克","Asia/Baghdad"});
        add(new String[]{"IR","Iran","伊朗","Asia/Tehran"});
        add(new String[]{"IL","Israel","以色列","Asia/Jerusalem"});
        add(new String[]{"IT","Italy","意大利","Europe/Rome"});
        add(new String[]{"IN","India","印度","Asia/Kolkata"});
        add(new String[]{"ID","Indonesia","印度尼西亚","Asia/Jakarta","Asia/Jayapura","Asia/Makassar"});
        add(new String[]{"GB","England","英国","Europe/London"});
        add(new String[]{"VG","Virgin Islands(British)","英属群岛","America/Tortola"});
        add(new String[]{"IO","British Indian Ocean Territory","英属印度洋领地","Indian/Chagos"});
        add(new String[]{"VN","Viet Nam","越南","Asia/Ho_Chi_Minh"});
        add(new String[]{"ZM","Zambia","赞比亚","Africa/Lusaka"});
        add(new String[]{"JE","Jersey","泽西","Europe/Jersey"});
        add(new String[]{"TD","Chad","乍得","Africa/Ndjamena"});
        add(new String[]{"GI","Gibraltar","直布罗陀","Europe/Gibraltar"});
        add(new String[]{"CL","Chile","智利","America/Santiago","Pacific/Easter","America/Punta_Arenas"});
        add(new String[]{"CF","Central African Republic","中非共和国","Africa/Bangui"});
        add(new String[]{"NR","Nauru","瑙鲁","Pacific/Nauru"});
    }};
    public static String standCountryName(String country, boolean seekCountryCode) {
        if (country==null) return null;
        String c=country.toUpperCase();
        if (c.equals("CN") || c.equals("CHINA") || c.equals("中国")) return seekCountryCode ? "CN" : "中国";
        else if (country.equals("SCOTLAND") ||country.equals("NORTHERN IRELAND")) return seekCountryCode ? "GB" : "England";
        for (int i=0;i<countryTimeZones.size();i++) {
            String [] params = countryTimeZones.get(i);
            if (c.equals(params[0]) || c.equals(params[1].toUpperCase()) || c.equals(params[2])) return params[seekCountryCode ? 0 : 1];
        }
        return null;
    }
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
        for (int i=0;i<countryTimeZones.size();i++) {
            String [] params = countryTimeZones.get(i);
            if (c.equals(params[0]) || c.equals(params[1].toUpperCase()) || c.equals(params[2])) return params[3];
        }
        return null;
    }
    public static boolean hasChinese(String value) {
        if (value == null) return false;
        String regex = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(value);
        return match.find();
    }
    public static boolean isEmpty(Object o) {
        return o==null || o.toString().trim().isEmpty();
    }
    public static String joinAll(String separator, String ... ss) {
        if (ss==null || ss.length==0) return "";
        String a = (ss[0]==null ? "" : ss[0]);
        for (int i=1;i<ss.length;i++) {
            if (isEmpty(ss[i])) continue;
            else if (isEmpty(a)) a =  ss[i];
            else a = a + separator + ss[i];
        }
        return a;
    }
    public static String formatAddress(String country, String province, String city,String location, String poi) {
        boolean cc = hasChinese(country) || hasChinese(province) || hasChinese(city) || hasChinese(location);
        if (cc) {
            String address = joinAll("", country , province, city!=null && city.equals(province) ?"":city, location);
            if (!isEmpty(poi) && !address.toUpperCase().contains(poi.toUpperCase())) return joinAll(",",address, poi);
            else return address;
        } else {
            String address = joinAll(",",location, city!=null && city.equals(province) ?"":city, province, country);
            if (!isEmpty(poi) && !address.toUpperCase().contains(poi.toUpperCase())) return joinAll(",",poi, address);
            else return address;
        }
    }
}
