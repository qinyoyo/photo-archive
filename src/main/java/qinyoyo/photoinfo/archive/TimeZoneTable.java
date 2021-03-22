package qinyoyo.photoinfo.archive;

import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Qinyoyo on 2017/5/10.
 */
public class TimeZoneTable {
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
        String c=countryCode.toUpperCase();
        switch (c) {
            case "AD": case "ANDORRA": return "Europe/Andorra";
            case "AE": case "UNITED ARAB EMIRATES": return "Asia/Dubai";
            case "AF": case "AFGHANISTAN": return "Asia/Kabul";
            case "AG": case "ANTIGUA AND BARBUDA": return "America/Antigua";
            case "AI": case "ANGUILLA": return "America/Anguilla";
            case "AL": case "ALBANIA": return "Europe/Tirane";
            case "AM": case "ARMENIA": return "Asia/Yerevan";
            case "AO": case "ANGOLA": return "Africa/Luanda";
            case "AQ": case "ANTARCTICA": return "Antarctica/Casey";
/*            case "AQ": case "ANTARCTICA": return "Antarctica/Davis";
            case "AQ": case "ANTARCTICA": return "Antarctica/DumontDUrville";
            case "AQ": case "ANTARCTICA": return "Antarctica/Mawson";
            case "AQ": case "ANTARCTICA": return "Antarctica/McMurdo";
            case "AQ": case "ANTARCTICA": return "Antarctica/Palmer";
            case "AQ": case "ANTARCTICA": return "Antarctica/Rothera";
            case "AQ": case "ANTARCTICA": return "Antarctica/Syowa";
            case "AQ": case "ANTARCTICA": return "Antarctica/Troll";
            case "AQ": case "ANTARCTICA": return "Antarctica/Vostok";*/
/*            case "AR": case "ARGENTINA": return "America/Argentina/La_Rioja";
            case "AR": case "ARGENTINA": return "America/Argentina/Mendoza";
            case "AR": case "ARGENTINA": return "America/Argentina/Rio_Gallegos";
            case "AR": case "ARGENTINA": return "America/Argentina/Salta";
            case "AR": case "ARGENTINA": return "America/Argentina/San_Juan";
            case "AR": case "ARGENTINA": return "America/Argentina/San_Luis";
            case "AR": case "ARGENTINA": return "America/Argentina/Tucuman";
            case "AR": case "ARGENTINA": return "America/Argentina/Ushuaia";*/
            case "AR": case "ARGENTINA": return "America/Argentina/Buenos_Aires";
/*            case "AR": case "ARGENTINA": return "America/Argentina/Catamarca";
            case "AR": case "ARGENTINA": return "America/Argentina/Cordoba";
            case "AR": case "ARGENTINA": return "America/Argentina/Jujuy";*/
            case "AS": case "AMERICAN SAMOA": return "Pacific/Pago_Pago";
            case "AT": case "AUSTRIA": return "Europe/Vienna";
/*            case "AU": case "AUSTRALIA": return "Antarctica/Macquarie";
            case "AU": case "AUSTRALIA": return "Australia/Adelaide";
            case "AU": case "AUSTRALIA": return "Australia/Brisbane";
            case "AU": case "AUSTRALIA": return "Australia/Broken_Hill";
            case "AU": case "AUSTRALIA": return "Australia/Darwin";
            case "AU": case "AUSTRALIA": return "Australia/Eucla";
            case "AU": case "AUSTRALIA": return "Australia/Hobart";
            case "AU": case "AUSTRALIA": return "Australia/Lindeman";
            case "AU": case "AUSTRALIA": return "Australia/Lord_Howe";
            case "AU": case "AUSTRALIA": return "Australia/Melbourne";
            case "AU": case "AUSTRALIA": return "Australia/Perth";*/
            case "AU": case "AUSTRALIA": return "Australia/Sydney";
            case "AW": case "ARUBA": return "America/Aruba";
            case "AX": case "ÅLAND ISLANDS": return "Europe/Mariehamn";
            case "AZ": case "AZERBAIJAN": return "Asia/Baku";
            case "BA": case "BOSNIA AND HERZEGOVINA": return "Europe/Sarajevo";
            case "BB": case "BARBADOS": return "America/Barbados";
            case "BD": case "BANGLADESH": return "Asia/Dhaka";
            case "BE": case "BELGIUM": return "Europe/Brussels";
            case "BF": case "BURKINA FASO": return "Africa/Ouagadougou";
            case "BG": case "BULGARIA": return "Europe/Sofia";
            case "BH": case "BAHRAIN": return "Asia/Bahrain";
            case "BI": case "BURUNDI": return "Africa/Bujumbura";
            case "BJ": case "BENIN": return "Africa/Porto-Novo";
            case "BL": case "SAINT BARTHÉLEMY": return "America/St_Barthelemy";
            case "BM": case "BERMUDA": return "Atlantic/Bermuda";
            case "BN": case "BRUNEI DARUSSALAM": return "Asia/Brunei";
            case "BO": case "BOLIVIA, PLURINATIONAL STATE OF": return "America/La_Paz";
            case "BQ": case "BONAIRE, SINT EUSTATIUS AND SABA": return "America/Kralendijk";
/*            case "BR": case "BRAZIL": return "America/Bahia";
            case "BR": case "BRAZIL": return "America/Belem";
            case "BR": case "BRAZIL": return "America/Boa_Vista";
            case "BR": case "BRAZIL": return "America/Campo_Grande";
            case "BR": case "BRAZIL": return "America/Cuiaba";
            case "BR": case "BRAZIL": return "America/Eirunepe";
            case "BR": case "BRAZIL": return "America/Fortaleza";
            case "BR": case "BRAZIL": return "America/Maceio";
            case "BR": case "BRAZIL": return "America/Manaus";
            case "BR": case "BRAZIL": return "America/Noronha";
            case "BR": case "BRAZIL": return "America/Porto_Velho";
            case "BR": case "BRAZIL": return "America/Recife";
            case "BR": case "BRAZIL": return "America/Rio_Branco";
            case "BR": case "BRAZIL": return "America/Santarem";*/
            case "BR": case "BRAZIL": return "America/Sao_Paulo";
/*            case "BR": case "BRAZIL": return "America/Araguaina";*/
            case "BS": case "BAHAMAS": return "America/Nassau";
            case "BT": case "BHUTAN": return "Asia/Thimphu";
            case "BW": case "BOTSWANA": return "Africa/Gaborone";
            case "BY": case "BELARUS": return "Europe/Minsk";
            case "BZ": case "BELIZE": return "America/Belize";
/*            case "CA": case "CANADA": return "America/Atikokan";
            case "CA": case "CANADA": return "America/Blanc-Sablon";
            case "CA": case "CANADA": return "America/Cambridge_Bay";
            case "CA": case "CANADA": return "America/Creston";
            case "CA": case "CANADA": return "America/Dawson";
            case "CA": case "CANADA": return "America/Dawson_Creek";
            case "CA": case "CANADA": return "America/Edmonton";
            case "CA": case "CANADA": return "America/Fort_Nelson";
            case "CA": case "CANADA": return "America/Glace_Bay";
            case "CA": case "CANADA": return "America/Goose_Bay";
            case "CA": case "CANADA": return "America/Halifax";
            case "CA": case "CANADA": return "America/Inuvik";
            case "CA": case "CANADA": return "America/Iqaluit";
            case "CA": case "CANADA": return "America/Moncton";
            case "CA": case "CANADA": return "America/Nipigon";
            case "CA": case "CANADA": return "America/Pangnirtung";
            case "CA": case "CANADA": return "America/Rainy_River";
            case "CA": case "CANADA": return "America/Rankin_Inlet";
            case "CA": case "CANADA": return "America/Regina";
            case "CA": case "CANADA": return "America/Resolute";
            case "CA": case "CANADA": return "America/St_Johns";
            case "CA": case "CANADA": return "America/Swift_Current";
            case "CA": case "CANADA": return "America/Thunder_Bay";
            case "CA": case "CANADA": return "America/Toronto";
            case "CA": case "CANADA": return "America/Vancouver";
            case "CA": case "CANADA": return "America/Whitehorse";
            case "CA": case "CANADA": return "America/Winnipeg";
            case "CA": case "CANADA": return "America/Yellowknife";*/
            case "CC": case "COCOS (KEELING) ISLANDS": return "Indian/Cocos";
            case "CD": case "CONGO, THE DEMOCRATIC REPUBLIC OF THE": return "Africa/Kinshasa";
//            case "CD": case "CONGO, THE DEMOCRATIC REPUBLIC OF THE": return "Africa/Lubumbashi";
            case "CF": case "CENTRAL AFRICAN REPUBLIC": return "Africa/Bangui";
            case "CG": case "CONGO": return "Africa/Brazzaville";
            case "CH": case "SWITZERLAND": return "Europe/Zurich";
            case "CI": case "CÔTE D'IVOIRE": return "Africa/Abidjan";
            case "CK": case "COOK ISLANDS": return "Pacific/Rarotonga";
            case "CL": case "CHILE": return "America/Punta_Arenas";
/*            case "CL": case "CHILE": return "America/Santiago";
            case "CL": case "CHILE": return "Pacific/Easter";*/
            case "CM": case "CAMEROON": return "Africa/Douala";
            case "CN": case "CHINA": return "Asia/Shanghai";
            case "CO": case "COLOMBIA": return "America/Bogota";
            case "CR": case "COSTA RICA": return "America/Costa_Rica";
            case "CU": case "CUBA": return "America/Havana";
            case "CV": case "CAPE VERDE": return "Atlantic/Cape_Verde";
            case "CW": case "CURAÇAO": return "America/Curacao";
            case "CX": case "CHRISTMAS ISLAND": return "Indian/Christmas";
            case "CY": case "CYPRUS": return "Asia/Famagusta";
/*            case "CY": case "CYPRUS": return "Asia/Nicosia";*/
            case "CZ": case "CZECH REPUBLIC": return "Europe/Prague";
            case "DE": case "GERMANY": return "Europe/Berlin";
/*            case "DE": case "GERMANY": return "Europe/Busingen";*/
            case "DJ": case "DJIBOUTI": return "Africa/Djibouti";
            case "DK": case "DENMARK": return "Europe/Copenhagen";
            case "DM": case "DOMINICA": return "America/Dominica";
            case "DO": case "DOMINICAN REPUBLIC": return "America/Santo_Domingo";
            case "DZ": case "ALGERIA": return "Africa/Algiers";
            case "EC": case "ECUADOR": return "America/Guayaquil";
/*            case "EC": case "ECUADOR": return "Pacific/Galapagos";*/
            case "EE": case "ESTONIA": return "Europe/Tallinn";
            case "EG": case "EGYPT": return "Africa/Cairo";
            case "EH": case "WESTERN SAHARA": return "Africa/El_Aaiun";
            case "ER": case "ERITREA": return "Africa/Asmara";
/*            case "ES": case "SPAIN": return "Africa/Ceuta";
            case "ES": case "SPAIN": return "Atlantic/Canary";*/
            case "ES": case "SPAIN": return "Europe/Madrid";
            case "ET": case "ETHIOPIA": return "Africa/Addis_Ababa";
            case "FI": case "FINLAND": return "Europe/Helsinki";
            case "FJ": case "FIJI": return "Pacific/Fiji";
            case "FK": case "FALKLAND ISLANDS (MALVINAS)": return "Atlantic/Stanley";
/*            case "FM": case "MICRONESIA, FEDERATED STATES OF": return "Pacific/Chuuk";
            case "FM": case "MICRONESIA, FEDERATED STATES OF": return "Pacific/Kosrae";*/
            case "FM": case "MICRONESIA, FEDERATED STATES OF": return "Pacific/Pohnpei";
            case "FO": case "FAROE ISLANDS": return "Atlantic/Faroe";
            case "FR": case "FRANCE": return "Europe/Paris";
            case "GA": case "GABON": return "Africa/Libreville";
            case "GB": case "UNITED KINGDOM": return "Europe/London";
            case "GD": case "GRENADA": return "America/Grenada";
            case "GE": case "GEORGIA": return "Asia/Tbilisi";
            case "GF": case "FRENCH GUIANA": return "America/Cayenne";
            case "GG": case "GUERNSEY": return "Europe/Guernsey";
            case "GH": case "GHANA": return "Africa/Accra";
            case "GI": case "GIBRALTAR": return "Europe/Gibraltar";
/*            case "GL": case "GREENLAND": return "America/Danmarkshavn";
            case "GL": case "GREENLAND": return "America/Scoresbysund";*/
            case "GL": case "GREENLAND": return "America/Thule";
            case "GM": case "GAMBIA": return "Africa/Banjul";
            case "GN": case "GUINEA": return "Africa/Conakry";
            case "GP": case "GUADELOUPE": return "America/Guadeloupe";
            case "GQ": case "EQUATORIAL GUINEA": return "Africa/Malabo";
            case "GR": case "GREECE": return "Europe/Athens";
            case "GS": case "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS": return "Atlantic/South_Georgia";
            case "GT": case "GUATEMALA": return "America/Guatemala";
            case "GU": case "GUAM": return "Pacific/Guam";
            case "GW": case "GUINEA-BISSAU": return "Africa/Bissau";
            case "GY": case "GUYANA": return "America/Guyana";
            case "HK": case "HONG KONG": return "Asia/Hong_Kong";
            case "HN": case "HONDURAS": return "America/Tegucigalpa";
            case "HR": case "CROATIA": return "Europe/Zagreb";
            case "HT": case "HAITI": return "America/Port-au-Prince";
            case "HU": case "HUNGARY": return "Europe/Budapest";
/*            case "ID": case "INDONESIA": return "Asia/Jakarta";
            case "ID": case "INDONESIA": return "Asia/Jayapura";
            case "ID": case "INDONESIA": return "Asia/Makassar";*/
            case "ID": case "INDONESIA": return "Asia/Pontianak";
            case "IE": case "IRELAND": return "Europe/Dublin";
            case "IL": case "ISRAEL": return "Asia/Jerusalem";
            case "IM": case "ISLE OF MAN": return "Europe/Isle_of_Man";
            case "IN": case "INDIA": return "Asia/Kolkata";
            case "IO": case "BRITISH INDIAN OCEAN TERRITORY": return "Indian/Chagos";
            case "IQ": case "IRAQ": return "Asia/Baghdad";
            case "IR": case "IRAN, ISLAMIC REPUBLIC OF": return "Asia/Tehran";
            case "IS": case "ICELAND": return "Atlantic/Reykjavik";
            case "IT": case "ITALY": return "Europe/Rome";
            case "JE": case "JERSEY": return "Europe/Jersey";
            case "JM": case "JAMAICA": return "America/Jamaica";
            case "JO": case "JORDAN": return "Asia/Amman";
            case "JP": case "JAPAN": return "Asia/Tokyo";
            case "KE": case "KENYA": return "Africa/Nairobi";
            case "KG": case "KYRGYZSTAN": return "Asia/Bishkek";
            case "KH": case "CAMBODIA": return "Asia/Phnom_Penh";
/*            case "KI": case "KIRIBATI": return "Pacific/Enderbury";
            case "KI": case "KIRIBATI": return "Pacific/Kiritimati";*/
            case "KI": case "KIRIBATI": return "Pacific/Tarawa";
            case "KM": case "COMOROS": return "Indian/Comoro";
            case "KN": case "SAINT KITTS AND NEVIS": return "America/St_Kitts";
            case "KP": case "KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF": return "Asia/Pyongyang";
            case "KR": case "KOREA, REPUBLIC OF": return "Asia/Seoul";
            case "KW": case "KUWAIT": return "Asia/Kuwait";
            case "KY": case "CAYMAN ISLANDS": return "America/Cayman";
            case "KZ": case "KAZAKHSTAN": return "Asia/Almaty";
/*            case "KZ": case "KAZAKHSTAN": return "Asia/Aqtau";
            case "KZ": case "KAZAKHSTAN": return "Asia/Aqtobe";
            case "KZ": case "KAZAKHSTAN": return "Asia/Atyrau";
            case "KZ": case "KAZAKHSTAN": return "Asia/Oral";
            case "KZ": case "KAZAKHSTAN": return "Asia/Qyzylorda";*/
            case "LA": case "LAO PEOPLE'S DEMOCRATIC REPUBLIC": return "Asia/Vientiane";
            case "LB": case "LEBANON": return "Asia/Beirut";
            case "LC": case "SAINT LUCIA": return "America/St_Lucia";
            case "LI": case "LIECHTENSTEIN": return "Europe/Vaduz";
            case "LK": case "SRI LANKA": return "Asia/Colombo";
            case "LR": case "LIBERIA": return "Africa/Monrovia";
            case "LS": case "LESOTHO": return "Africa/Maseru";
            case "LT": case "LITHUANIA": return "Europe/Vilnius";
            case "LU": case "LUXEMBOURG": return "Europe/Luxembourg";
            case "LV": case "LATVIA": return "Europe/Riga";
            case "LY": case "LIBYA": return "Africa/Tripoli";
            case "MA": case "MOROCCO": return "Africa/Casablanca";
            case "MC": case "MONACO": return "Europe/Monaco";
            case "MD": case "MOLDOVA, REPUBLIC OF": return "Europe/Chisinau";
            case "ME": case "MONTENEGRO": return "Europe/Podgorica";
            case "MF": case "SAINT MARTIN (FRENCH PART)": return "America/Marigot";
            case "MG": case "MADAGASCAR": return "Indian/Antananarivo";
            case "MH": case "MARSHALL ISLANDS": return "Pacific/Kwajalein";
/*            case "MH": case "MARSHALL ISLANDS": return "Pacific/Majuro";*/
            case "MK": case "MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF": return "Europe/Skopje";
            case "ML": case "MALI": return "Africa/Bamako";
            case "MM": case "MYANMAR": return "Asia/Yangon";
            case "MN": case "MONGOLIA": return "Asia/Choibalsan";
/*            case "MN": case "MONGOLIA": return "Asia/Hovd";
            case "MN": case "MONGOLIA": return "Asia/Ulaanbaatar";*/
            case "MO": case "MACAO": return "Asia/Macau";
            case "MP": case "NORTHERN MARIANA ISLANDS": return "Pacific/Saipan";
            case "MQ": case "MARTINIQUE": return "America/Martinique";
            case "MR": case "MAURITANIA": return "Africa/Nouakchott";
            case "MS": case "MONTSERRAT": return "America/Montserrat";
            case "MT": case "MALTA": return "Europe/Malta";
            case "MU": case "MAURITIUS": return "Indian/Mauritius";
            case "MV": case "MALDIVES": return "Indian/Maldives";
            case "MW": case "MALAWI": return "Africa/Blantyre";
/*            case "MX": case "MEXICO": return "America/Bahia_Banderas";
            case "MX": case "MEXICO": return "America/Cancun";
            case "MX": case "MEXICO": return "America/Chihuahua";
            case "MX": case "MEXICO": return "America/Hermosillo";
            case "MX": case "MEXICO": return "America/Matamoros";
            case "MX": case "MEXICO": return "America/Mazatlan";
            case "MX": case "MEXICO": return "America/Merida";*/
            case "MX": case "MEXICO": return "America/Mexico_City";
/*            case "MX": case "MEXICO": return "America/Monterrey";
            case "MX": case "MEXICO": return "America/Ojinaga";
            case "MX": case "MEXICO": return "America/Tijuana";*/
            case "MY": case "MALAYSIA": return "Asia/Kuala_Lumpur";
/*            case "MY": case "MALAYSIA": return "Asia/Kuching";*/
            case "MZ": case "MOZAMBIQUE": return "Africa/Maputo";
            case "NA": case "NAMIBIA": return "Africa/Windhoek";
            case "NC": case "NEW CALEDONIA": return "Pacific/Noumea";
            case "NE": case "NIGER": return "Africa/Niamey";
            case "NF": case "NORFOLK ISLAND": return "Pacific/Norfolk";
            case "NG": case "NIGERIA": return "Africa/Lagos";
            case "NI": case "NICARAGUA": return "America/Managua";
            case "NL": case "NETHERLANDS": return "Europe/Amsterdam";
            case "NO": case "NORWAY": return "Europe/Oslo";
            case "NP": case "NEPAL": return "Asia/Kathmandu";
            case "NR": case "NAURU": return "Pacific/Nauru";
            case "NU": case "NIUE": return "Pacific/Niue";
            case "NZ": case "NEW ZEALAND": return "Pacific/Auckland";
/*            case "NZ": case "NEW ZEALAND": return "Pacific/Chatham";*/
            case "OM": case "OMAN": return "Asia/Muscat";
            case "PA": case "PANAMA": return "America/Panama";
            case "PE": case "PERU": return "America/Lima";
            case "PF": case "FRENCH POLYNESIA": return "Pacific/Gambier";
/*            case "PF": case "FRENCH POLYNESIA": return "Pacific/Marquesas";
            case "PF": case "FRENCH POLYNESIA": return "Pacific/Tahiti";
            case "PG": case "PAPUA NEW GUINEA": return "Pacific/Bougainville";
            case "PG": case "PAPUA NEW GUINEA": return "Pacific/Port_Moresby";*/
            case "PH": case "PHILIPPINES": return "Asia/Manila";
            case "PK": case "PAKISTAN": return "Asia/Karachi";
            case "PL": case "POLAND": return "Europe/Warsaw";
            case "PM": case "SAINT PIERRE AND MIQUELON": return "America/Miquelon";
            case "PN": case "PITCAIRN": return "Pacific/Pitcairn";
            case "PR": case "PUERTO RICO": return "America/Puerto_Rico";
            case "PS": case "PALESTINE, STATE OF": return "Asia/Gaza";
/*            case "PS": case "PALESTINE, STATE OF": return "Asia/Hebron";*/
            case "PT": case "PORTUGAL": return "Atlantic/Azores";
/*            case "PT": case "PORTUGAL": return "Atlantic/Madeira";
            case "PT": case "PORTUGAL": return "Europe/Lisbon";*/
            case "PW": case "PALAU": return "Pacific/Palau";
            case "PY": case "PARAGUAY": return "America/Asuncion";
            case "QA": case "QATAR": return "Asia/Qatar";
            case "RE": case "RÉUNION": return "Indian/Reunion";
            case "RO": case "ROMANIA": return "Europe/Bucharest";
            case "RS": case "SERBIA": return "Europe/Belgrade";
/*            case "RU": case "RUSSIAN FEDERATION": return "Asia/Anadyr";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Barnaul";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Chita";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Irkutsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Kamchatka";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Khandyga";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Krasnoyarsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Magadan";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Novokuznetsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Novosibirsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Omsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Sakhalin";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Srednekolymsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Tomsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Ust-Nera";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Vladivostok";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Yakutsk";
            case "RU": case "RUSSIAN FEDERATION": return "Asia/Yekaterinburg";
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Astrakhan";
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Kaliningrad";
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Kirov";*/
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Moscow";
/*            case "RU": case "RUSSIAN FEDERATION": return "Europe/Samara";
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Saratov";
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Ulyanovsk";
            case "RU": case "RUSSIAN FEDERATION": return "Europe/Volgograd";*/
            case "RW": case "RWANDA": return "Africa/Kigali";
            case "SA": case "SAUDI ARABIA": return "Asia/Riyadh";
            case "SB": case "SOLOMON ISLANDS": return "Pacific/Guadalcanal";
            case "SC": case "SEYCHELLES": return "Indian/Mahe";
            case "SD": case "SUDAN": return "Africa/Khartoum";
            case "SE": case "SWEDEN": return "Europe/Stockholm";
            case "SG": case "SINGAPORE": return "Asia/Singapore";
            case "SH": case "SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA": return "Atlantic/St_Helena";
            case "SI": case "SLOVENIA": return "Europe/Ljubljana";
            case "SJ": case "SVALBARD AND JAN MAYEN": return "Arctic/Longyearbyen";
            case "SK": case "SLOVAKIA": return "Europe/Bratislava";
            case "SL": case "SIERRA LEONE": return "Africa/Freetown";
            case "SM": case "SAN MARINO": return "Europe/San_Marino";
            case "SN": case "SENEGAL": return "Africa/Dakar";
            case "SO": case "SOMALIA": return "Africa/Mogadishu";
            case "SR": case "SURINAME": return "America/Paramaribo";
            case "SS": case "SOUTH SUDAN": return "Africa/Juba";
            case "ST": case "SAO TOME AND PRINCIPE": return "Africa/Sao_Tome";
            case "SV": case "EL SALVADOR": return "America/El_Salvador";
            case "SX": case "SINT MAARTEN (DUTCH PART)": return "America/Lower_Princes";
            case "SY": case "SYRIAN ARAB REPUBLIC": return "Asia/Damascus";
            case "SZ": case "SWAZILAND": return "Africa/Mbabane";
            case "TC": case "TURKS AND CAICOS ISLANDS": return "America/Grand_Turk";
            case "TD": case "CHAD": return "Africa/Ndjamena";
            case "TF": case "FRENCH SOUTHERN TERRITORIES": return "Indian/Kerguelen";
            case "TG": case "TOGO": return "Africa/Lome";
            case "TH": case "THAILAND": return "Asia/Bangkok";
            case "TJ": case "TAJIKISTAN": return "Asia/Dushanbe";
            case "TK": case "TOKELAU": return "Pacific/Fakaofo";
            case "TL": case "TIMOR-LESTE": return "Asia/Dili";
            case "TM": case "TURKMENISTAN": return "Asia/Ashgabat";
            case "TN": case "TUNISIA": return "Africa/Tunis";
            case "TO": case "TONGA": return "Pacific/Tongatapu";
            case "TR": case "TURKEY": return "Europe/Istanbul";
            case "TT": case "TRINIDAD AND TOBAGO": return "America/Port_of_Spain";
            case "TV": case "TUVALU": return "Pacific/Funafuti";
            case "TW": case "TAIWAN, PROVINCE OF CHINA": return "Asia/Taipei";
            case "TZ": case "TANZANIA, UNITED REPUBLIC OF": return "Africa/Dar_es_Salaam";
            case "UA": case "UKRAINE": return "Europe/Kiev";
/*            case "UA": case "UKRAINE": return "Europe/Simferopol";
            case "UA": case "UKRAINE": return "Europe/Uzhgorod";
            case "UA": case "UKRAINE": return "Europe/Zaporozhye";*/
            case "UG": case "UGANDA": return "Africa/Kampala";
            case "UM": case "UNITED STATES MINOR OUTLYING ISLANDS": return "Pacific/Midway";
/*            case "UM": case "UNITED STATES MINOR OUTLYING ISLANDS": return "Pacific/Wake";*/
/*            case "US": case "UNITED STATES": return "America/Boise";
            case "US": case "UNITED STATES": return "America/Chicago";
            case "US": case "UNITED STATES": return "America/Denver";
            case "US": case "UNITED STATES": return "America/Detroit";
            case "US": case "UNITED STATES": return "America/Indiana/Indianapolis";
            case "US": case "UNITED STATES": return "America/Indiana/Knox";
            case "US": case "UNITED STATES": return "America/Indiana/Marengo";
            case "US": case "UNITED STATES": return "America/Indiana/Petersburg";
            case "US": case "UNITED STATES": return "America/Indiana/Tell_City";
            case "US": case "UNITED STATES": return "America/Indiana/Vevay";
            case "US": case "UNITED STATES": return "America/Indiana/Vincennes";
            case "US": case "UNITED STATES": return "America/Indiana/Winamac";
            case "US": case "UNITED STATES": return "America/Juneau";
            case "US": case "UNITED STATES": return "America/Kentucky/Louisville";
            case "US": case "UNITED STATES": return "America/Kentucky/Monticello";
            case "US": case "UNITED STATES": return "America/Los_Angeles";
            case "US": case "UNITED STATES": return "America/Menominee";
            case "US": case "UNITED STATES": return "America/Metlakatla";
            case "US": case "UNITED STATES": return "America/New_York";
            case "US": case "UNITED STATES": return "America/Nome";
            case "US": case "UNITED STATES": return "America/North_Dakota/Beulah";
            case "US": case "UNITED STATES": return "America/North_Dakota/Center";
            case "US": case "UNITED STATES": return "America/North_Dakota/New_Salem";
            case "US": case "UNITED STATES": return "America/Phoenix";
            case "US": case "UNITED STATES": return "America/Sitka";
            case "US": case "UNITED STATES": return "America/Yakutat";
            case "US": case "UNITED STATES": return "America/Adak";
            case "US": case "UNITED STATES": return "America/Anchorage";
            case "US": case "UNITED STATES": return "Pacific/Honolulu";*/
            case "UY": case "URUGUAY": return "America/Montevideo";
            case "UZ": case "UZBEKISTAN": return "Asia/Samarkand";
/*            case "UZ": case "UZBEKISTAN": return "Asia/Tashkent";*/
            case "VA": case "HOLY SEE (VATICAN CITY STATE)": return "Europe/Vatican";
            case "VC": case "SAINT VINCENT AND THE GRENADINES": return "America/St_Vincent";
            case "VE": case "VENEZUELA, BOLIVARIAN REPUBLIC OF": return "America/Caracas";
            case "VG": case "VIRGIN ISLANDS, BRITISH": return "America/Tortola";
            case "VI": case "VIRGIN ISLANDS, U.S.": return "America/St_Thomas";
            case "VN": case "VIET NAM": return "Asia/Ho_Chi_Minh";
            case "VU": case "VANUATU": return "Pacific/Efate";
            case "WF": case "WALLIS AND FUTUNA": return "Pacific/Wallis";
            case "WS": case "SAMOA": return "Pacific/Apia";
            case "YE": case "YEMEN": return "Asia/Aden";
            case "YT": case "MAYOTTE": return "Indian/Mayotte";
            case "ZA": case "SOUTH AFRICA": return "Africa/Johannesburg";
            case "ZM": case "ZAMBIA": return "Africa/Lusaka";
            case "ZW": case "ZIMBABWE": return "Africa/Harare";
        }
        return null;
    }
}
