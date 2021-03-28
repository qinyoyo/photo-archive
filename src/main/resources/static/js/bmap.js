let mapPoint = {}
let map = null
let marker = null
function setMarker(wgs84,notTrans) {
    if (map && marker) map.removeOverlay(marker)
    let  loc = notTrans ? wgs84:wgs84tobd09(wgs84)
    let pos = new BMapGL.Point(loc.lon, loc.lat);
    marker = new BMapGL.Marker(pos)
    map.addOverlay(marker)
    map.setCenter(pos)

}
function getPoint(address) {
    let geocoder =  new BMapGL.Geocoder()
    geocoder.getPoint(address, function (point) {
        if (point) {
            getAddress({lon:point.lng, lat:point.lat})
        }
    },mapPoint.city)
}
function getAddress(latlng) {
    mapPoint={}
    let loc={lon:latlng.lng, lat:latlng.lat}

    setMarker(loc,true)
    let wgs84 = bd09towgs84(loc)
    mapPoint.longitude = wgs84.lon
    mapPoint.latitude = wgs84.lat
    let geocoder =  new BMapGL.Geocoder()
    geocoder.getLocation(latlng, function(rs){
        const addComp = rs.addressComponents
        mapPoint.province = addComp.province
        if (rs.surroundingPois && rs.surroundingPois.length>0) {
            mapPoint.subjectCode = rs.surroundingPois[0].title
        }
        let cc = mapPoint.province.search(/[\u4e00-\u9fa5]/)
        if (cc>=0) {
            mapPoint.city = addComp.city + addComp.district
            mapPoint.location = addComp.street + addComp.streetNumber
            document.getElementById("address").value
                = mapPoint.province + mapPoint.city + mapPoint.location
                + (mapPoint.subjectCode?':'+mapPoint.subjectCode:'')
        } else {
            mapPoint.city = (addComp.district + ' ' +addComp.city).trim()
            mapPoint.location = (addComp.streetNumber + ' ' + addComp.street).trim()
            document.getElementById("address").value
                = (mapPoint.subjectCode?mapPoint.subjectCode + ':':'') +
                mapPoint.location +' ' + mapPoint.city + ' ' +mapPoint.province
        }
    })
}
function clickMap(e) {
    getAddress(e.latlng)
}

function initMap(divId, wgs84) {
    map = new BMapGL.Map(divId)
    if (wgs84) {
        let  loc = wgs84tobd09(wgs84)
        let pos = new BMapGL.Point(loc.lon, loc.lat)
        map.centerAndZoom(pos, 12)
    } else map.centerAndZoom('重庆市', 12)

    map.enableScrollWheelZoom(true)
    map.addControl(new BMapGL.ZoomControl())
    map.addControl(new BMapGL.LocationControl())
    map.addEventListener('click',clickMap)
}