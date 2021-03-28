const pi = 3.14159265358979324;
const a = 6378245.0;
const ee = 0.00669342162296594323;
const x_pi=3.14159265358979324 * 3000.0 / 180.0;

function bd09togcj02(baidu_point){
    var mars_point={lon:0,lat:0};
    var x=baidu_point.lon-0.0065;
    var y=baidu_point.lat-0.006;
    var z=Math.sqrt(x*x+y*y)- 0.00002 * Math.sin(y * x_pi);
    var theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
    mars_point.lon=z * Math.cos(theta);
    mars_point.lat=z * Math.sin(theta);
    return mars_point;
}
function gcj02tobd09(mars_point){
    var baidu_point={lon:0,lat:0};
    var x=mars_point.lon;
    var y=mars_point.lat;
    var z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
    var theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
    baidu_point.lon = z * Math.cos(theta) + 0.0065;
    baidu_point.lat = z * Math.sin(theta) + 0.006;
    return baidu_point;
}

/*判断是否在国内，不在国内则不做偏移*/
function outOfChina(lon, lat)
{
    if ((lon < 72.004 || lon > 137.8347)&&(lat < 0.8293 || lat > 55.8271)){
        return true;
    }else {
        return false;
    }
}
function transformLat(x,y)
{
    var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
    return ret;
}

function transformLon(x,y)
{
    var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
    return ret;
}
function delta(lon, lat) {
    let a = 6378245.0 //  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
    let ee = 0.00669342162296594323 //  ee: 椭球的偏心率。
    let dLat = transformLat(lon - 105.0, lat - 35.0)
    let dLon = transformLon(lon - 105.0, lat - 35.0)
    let radLat = lat / 180.0 * pi
    let magic = Math.sin(radLat)
    magic = 1 - ee * magic * magic
    let sqrtMagic = Math.sqrt(magic)
    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi)
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi)
    return {
        lat: dLat,
        lon: dLon
    }
}
function wgs84togcj02(wgs84)
{
    if (outOfChina(wgs84.lon, wgs84.lat)) return wgs84
    let d = delta(wgs84.lon, wgs84.lat)
    return {
        'lat': wgs84.lat - d.lat,
        'lon': wgs84.lon - d.lon
    }
}
function gcj02towgs84(gcj02) {
    let d = delta(gcj02.lon, gcj02.lat)
    return {
        'lat': gcj02.lat - d.lat,
        'lon': gcj02.lon - d.lon
    }
}
function bd09towgs84(bd09) {
    let wgs84={}
    Ajax.get('position?lat='+bd09.lat.toFixed(6)
        +'&lng='+bd09.lon.toFixed(6)
        +'&towgs84=true',function (s){
        let ss = s.split(',')
            wgs84.lon = parseFloat(ss[0])
            wgs84.lat = parseFloat(ss[1])
        },true
    )
    return wgs84
}
function wgs84tobd09(wgs84) {
    let bd09={}
    Ajax.get('position?lat='+wgs84.lat.toFixed(6)
        +'&lng='+wgs84.lon.toFixed(6)
        +'&towgs84=false',function (s){
            let ss = s.split(',')
            bd09.lon = parseFloat(ss[0])
            bd09.lat = parseFloat(ss[1])
        },true
    )
    return bd09
}


