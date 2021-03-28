let mapPoint = {}
let map = null
let marker = null
function setMarker(wgs84,notTrans) {
    if (map && marker) map.remove(marker)
    let  loc = notTrans ? wgs84:wgs84togcj02(wgs84)
    let pos = new AMap.LngLat(loc.lon,loc.lat)
    marker = new AMap.Marker({
        position: pos,
        title: ''
    })
    map.add(marker)
    map.setCenter(pos)
}
function getPoint(address) {
    let geocoder = new AMap.Geocoder();
    geocoder.getLocation(address, function(status, result) {
        if (status === 'complete'&& result.geocodes.length) {
            var lnglat = result.geocodes[0].location
            //setMarker({lon:lnglat.getLng(), lat:lnglat.getLat()}, true)
            getAddress(lnglat)
        }
    })
}
function getAddress(lnglat) {
    mapPoint={}
    let gcj02={lon:lnglat.getLng(), lat:lnglat.getLat()}
    setMarker(gcj02,true)
    let wgs84 = gcj02towgs84(gcj02)
    mapPoint.longitude = wgs84.lon
    mapPoint.latitude = wgs84.lat
    let geocoder =  new AMap.Geocoder();
    geocoder.getAddress(lnglat, function(status, result) {
        if (status === 'complete'&&result.regeocode) {
            let address = result.regeocode.formattedAddress
            document.getElementById('address').value = address
            const addComp = result.regeocode.addressComponent
            mapPoint.province = addComp.province
            if (result.regeocode.pois && result.regeocode.pois.length>0) {
                mapPoint.subjectCode = result.regeocode.pois[0].title
            }
            let cc = mapPoint.province.search(/[\u4e00-\u9fa5]/)
            if (cc>=0) {
                mapPoint.city = addComp.city + addComp.district
                mapPoint.location = addComp.street + addComp.streetNumber
            } else {
                mapPoint.city = (addComp.district + ' ' +addComp.city).trim()
                mapPoint.location = (addComp.streetNumber + ' ' + addComp.street).trim()
            }
        }
    });
}
function clickMap(e) {
    getAddress(e.lnglat)
}

function initMap(divId, wgs84) {
    let  loc = wgs84togcj02(wgs84)
    map = new AMap.Map(divId, {
        resizeEnable: true,
        dragEnable :  true,
        keyboardEnable :  true,
        doubleClickZoom :  true,
        zoomEnable :  true,
        center:[loc.lon,loc.lat],
        zoom:11
    })
    AMap.plugin(['AMap.ToolBar','AMap.Geolocation'],function(){
        map.addControl(new AMap.ToolBar())
        map.addControl(new AMap.Geolocation())
    })
    map.on('click',clickMap)
}