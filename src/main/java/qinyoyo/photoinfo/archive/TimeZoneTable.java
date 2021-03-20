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
        String c = country.trim().toLowerCase();
        String id=timeIdJustFromCountry(c);
        if (id!=null)  return TimeZone.getTimeZone(id);

        if (( c.equals("us") || c.equals("usa") || c.equals("美国")
                || c.contains("united states") ) ) {
            id=timeIdForUS(state,city);
            if (id==null) id="US/Eastern";
            return TimeZone.getTimeZone(id);
        }
        if (( c.equals("ca") || c.equals("can") || c.equals("加拿大")
                || c.equals("canada") ) ) {
            id=timeIdForCA(state,longitude);
            if (id==null) id="Canada/Central";
            return TimeZone.getTimeZone(id);
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
        switch (s) {
            case "alaska" :
                if (city!=null && city.toLowerCase().contains("aleutians")) return "US/Aleutian";
                else return "US/Alaska" ;
            case "arizona" :
                if (city!=null && city.toLowerCase().contains("navajo")) return "US/Mountain";
                else  return "US/Arizona" ;
            case "california" : return "US/Pacific" ;
            case "nevada" : return "US/Pacific" ;
            case "oregon" : return "US/Pacific" ;
            case "washington" : return "US/Pacific" ;
            case "colorado" : return "US/Mountain" ;
            case "idaho" : return "US/Mountain" ;
            case "illinois" : return "US/Mountain" ;
            case "iowa" : return "US/Mountain" ;
            case "montana" : return "US/Mountain" ;
            case "new mexico" : return "US/Mountain" ;
            case "texas" : return "US/Mountain" ;
            case "utah" : return "US/Mountain" ;
            case "wyoming" : return "US/Mountain" ;
            case "alabama" : return "US/Central" ;
            case "arkansas" : return "US/Central" ;
            case "delaware" : return "US/Central" ;
            case "kansas" : return "US/Central" ;
            case "louisiana" : return "US/Central" ;
            case "mississippi" : return "US/Central" ;
            case "missouri" : return "US/Central" ;
            case "nebraska" : return "US/Central" ;
            case "north dakota" : return "US/Central" ;
            case "south dakota" : return "US/Central" ;
            case "hawaii" : return "US/Hawaii" ;
        }
        return "US/Eastern";
    }
    static String timeIdForCA(String state,Double longitude) {
        if (state==null) return null;
        String s=state.toLowerCase();
        switch (s) {
            case "newfoundland and labrador" : return "Canada/Newfoundland" ;
            case "nova scotia" : return "Canada/Atlantic" ;
            case "new brunswic" : return "Canada/Atlantic" ;
            case "prince edward island" : return "Canada/Atlantic" ;
            case "qubec" : return "Canada/Eastern" ;
            case "nunavut":
            case "ontario" :
                if (longitude==null) return "Canada/Central";
                else {
                    int tz = timeZoneInt(longitude);
                    if (tz == -5) return "Canada/Eastern";
                    else return "Canada/Central";
                }
            case "manitoba" : return "Canada/Central" ;
            case "saskatchewan" :
            case "northwest territories":
            case "alberta" : return "Canada/Mountain" ;
            case "yukon" :
            case "british columbia" : return "Canada/Pacific" ;
        }
        return "Canada/Central";
    }
    static String timeIdJustFromCountry(String country) {
        String c=country.toLowerCase();
        switch (c) {
            case "china":
            case "中国":      return "Asia/Chongqing";
            case "andorra" : return "Europe/Andorra" ;
            case "austria" : return "Europe/Vienna" ;
            case "albania" : return "Europe/Tirane" ;
            case "ireland" : return "Europe/Dublin" ;
            case "estonia" : return "Europe/Tallinn" ;
            case "iceland" : return "Atlantic/Reykjavik" ;
            case "belarus" : return "Europe/Minsk" ;
            case "bulgaria" : return "Europe/Sofia" ;
            case "poland" : return "Europe/Warsaw" ;
            case "bosnia" : return "Europe/Sarajevo" ;
            case "belgium" : return "Europe/Brussels" ;
            case "germany" : return "Europe/Berlin" ;
            case "denmark" : return "Europe/Copenhagen" ;
            case "russia" : return "Europe/Moscow" ;
            case "france" : return "Europe/Paris" ;
            case "finland" : return "Europe/Helsinki" ;
            case "holand" : return "Europe/Amsterdam" ;
            case "czech" : return "Europe/Prague" ;
            case "croatia" : return "Europe/Zagreb" ;
            case "latvia" : return "Europe/Riga" ;
            case "lithuania" : return "Europe/Vilnius" ;
            case "liechtenstein" : return "Europe/Vaduz" ;
            case "romania" : return "Europe/Bucharest" ;
            case "macedonia" : return "Europe/Skopje" ;
            case "malta" : return "Europe/Malta" ;
            case "luxembourg" : return "Europe/Luxembourg" ;
            case "monaco" : return "Europe/Monaco" ;
            case "moldova" : return "Europe/Chisinau" ;
            case "norway" : return "Europe/Oslo" ;
            case "serbia" : return "Europe/Belgrade" ;
            case "portugal" : return "Europe/Lisbon" ;
            case "sweden" : return "Europe/Stockholm" ;
            case "switzerland" : return "Europe/Zurich" ;
            case "slovak" : return "Europe/Bratislava" ;
            case "slovenia" : return "Europe/Ljubljana" ;
            case "san marino" : return "Europe/San_Marino" ;
            case "ukraine" : return "Europe/Kiev" ;
            case "spain" : return "Europe/Madrid" ;
            case "greece" : return "Europe/Athens" ;
            case "hungary" : return "Europe/Budapest" ;
            case "italy" : return "Europe/Rome" ;
            case "england" : return "Europe/London" ;
            case "afghanistan" : return "Asia/Kabul" ;
            case "arab" : return "Asia/Dubai" ;
            case "oman" : return "Asia/Muscat" ;
            case "azerbaijan" : return "Asia/Baku" ;
            case "pakistan" : return "Asia/Karachi" ;
            case "palestine" : return "Asia/Jerusalem" ;
            case "bahrain" : return "Asia/Bahrain" ;
            case "bhutan" : return "Asia/Thimphu" ;
            case "north korea" : return "Asia/Pyongyang" ;
            case "timor" : return "Asia/Dili" ;
            case "philippines" : return "Asia/Manila" ;
            case "georgia" : return "Asia/Tbilisi" ;
            case "kazakhstan" : return "Asia/Oral" ;
            case "south korea" : return "Asia/Seoul" ;
            case "kirgizstan" : return "Asia/Bishkek" ;
            case "qatar" : return "Asia/Qatar" ;
            case "kuwait" : return "Asia/Kuwait" ;
            case "laos" : return "Asia/Vientiane" ;
            case "lebanon" : return "Asia/Beirut" ;
            case "maldives" : return "Indian/Maldives" ;
            case "malaysia" : return "Asia/Kuala_Lumpur" ;
            case "mongolia" : return "Asia/Ulan_Bator" ;
            case "bangladesh" : return "Asia/Dhaka" ;
            case "myanmar" : return "Asia/Rangoon" ;
            case "nepal" : return "Asia/Kathmandu" ;
            case "japan" : return "Asia/Tokyo" ;
            case "cyprus" : return "Asia/Nicosia" ;
            case "saudi arabia" : return "Asia/Riyadh" ;
            case "srilanka" : return "Asia/Colombo" ;
            case "tajikistan" : return "Asia/Dushanbe" ;
            case "thailand" : return "Asia/Bangkok" ;
            case "turkey" : return "Asia/Istanbul" ;
            case "turkmenistan" : return "Asia/Ashgabat" ;
            case "brunei" : return "Asia/Brunei" ;
            case "uzbekistan" : return "Asia/Tashkent" ;
            case "singapore" : return "Asia/Singapore" ;
            case "syria" : return "Asia/Damascus" ;
            case "armenia" : return "Asia/Yerevan" ;
            case "yemen" : return "Asia/Aden" ;
            case "iran" : return "Asia/Tehran" ;
            case "iraq" : return "Asia/Baghdad" ;
            case "israel" : return "Asia/Jerusalem" ;
            case "india" : return "Asia/Calcutta" ;
            case "indonesia" : return "Asia/Jakarta" ;
            case "jordan" : return "Asia/Amman" ;
            case "vietnam" : return "Asia/Hanoi" ;
            case "argentina" : return "America/Argentina/Buenos_Aires" ;
            case "antigua barbuda" : return "America/St_Johns" ;
            case "barbados" : return "America/Barbados" ;
            case "bolivia" : return "America/La_Paz" ;
            case "brazil" : return "Brazil/West" ;
            case "dominica" : return "America/Dominica" ;

            case "cuba" : return "America/Havana" ;
            case "colombia" : return "America/Bogota" ;
            case "grenada" : return "America/Grenada" ;
            case "guyana" : return "America/Guyana" ;

            case "peru" : return "America/Lima" ;

            case "mexico" : return "America/Mexico_City" ;
            case "surinam" : return "America/Paramaribo" ;
            case "saint lucia" : return "America/St_Lucia" ;
            case "trinidad and tobago" : return "America/Port_of_Spain" ;
            case "uruguay" : return "America/Montevideo" ;
            case "venezuela" : return "America/Caracas" ;
            case "jamaica" : return "America/Jamaica" ;
            case "chile" : return "America/Santiago" ;
            case "bahamas" : return "America/Nassau" ;
            case "algeria" : return "Africa/Algiers" ;
            case "egypt" : return "Africa/Cairo" ;
            case "ethiopia" : return "Africa/Addis_Ababa" ;
            case "angola" : return "Africa/Luanda" ;
            case "benin" : return "Africa/Porto-Novo" ;
            case "botswana" : return "Africa/Gaborone" ;
            case "burkina faso" : return "Africa/Ouagadougou" ;
            case "burundi" : return "Africa/Bujumbura" ;
            case "equatorial guinea" : return "Africa/Malabo" ;
            case "togo" : return "Africa/Lome" ;
            case "eritrea" : return "Africa/Asmara" ;

            case "gambia" : return "Africa/Banjul" ;
            case "congo" : return "Africa/Brazzaville" ;
            case "congo kinshasa" : return "Africa/Kinshasa" ;
            case "djibouti" : return "Africa/Djibouti" ;
            case "guinea" : return "Africa/Conakry" ;
            case "guinea bissau" : return "Africa/Bissau" ;
            case "gabon" : return "Africa/Libreville" ;
            case "ghana" : return "Africa/Accra" ;
            case "zimbabwe" : return "Africa/Harare" ;
            case "cameroon" : return "Africa/Blantyre" ;
            case "comoros" : return "Africa/Blantyre" ;
            case "cote d lvoire" : return "Africa/Abidjan" ;
            case "kenya" : return "Africa/Nairobi" ;
            case "lesotho" : return "Africa/Maseru" ;
            case "liberia" : return "Africa/Monrovia" ;
            case "libya" : return "Africa/Tripoli" ;
            case "rwanda" : return "Africa/Kigali" ;
            case "madagascar" : return "Indian/Antananarivo" ;
            case "mali" : return "Africa/Bamako" ;
            case "mauritius" : return "Indian/Mauritius" ;
            case "mauritania" : return "Africa/Nouakchott" ;
            case "morocco" : return "Africa/Blantyre" ;
            case "mozambique" : return "Africa/Maputo" ;
            case "namibia" : return "Africa/Windhoek" ;
            case "south africa" : return "Africa/Johannesburg" ;
            case "niger" : return "Africa/Niamey" ;
            case "nigeria" : return "Africa/Blantyre" ;
            case "sierra leone" : return "Africa/Freetown" ;
            case "senegal" : return "Africa/Dakar" ;
            case "seychelles" : return "Australia/Victoria" ;
            case "sudan" : return "Africa/Khartoum" ;
            case "south sudan" : return "Africa/Khartoum" ;
            case "somali" : return "Africa/Mogadishu" ;
            case "tanzania" : return "Africa/Asmara" ;
            case "tunisia" : return "Africa/Tunis" ;
            case "uganda" : return "Africa/Kampala" ;
            case "zambia" : return "Africa/Lusaka" ;
            case "chad" : return "Africa/Gaborone" ;
            case "central africa" : return "Africa/Bangui" ;
            case "australia" : return "Australia/Canberra" ;
            case "fiji" : return "Pacific/Fiji" ;
            case "samoa" : return "Pacific/Samoa" ;
            case "nauru" : return "Pacific/Nauru" ;
            case "new zealand" : return "Antarctica/South_Pole" ;
            case "malawi" : return "Africa/Harare" ;
            case "somalia" : return "Africa/Asmara" ;
            case "democratic republic of the congo" : return "Africa/Blantyre" ;
            case "central african republic" : return "Africa/Blantyre" ;
        }
        return null;
    }
}
