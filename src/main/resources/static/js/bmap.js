
let baiduMap = null

function getMap() {
    return baiduMap
}
function getPosition(point) {
    return point
}
function makeIcon(options) {
    return new BMapGL.Icon(options.url, new BMapGL.Size(options.width, options.height), {
        // 指定定位位置。
        // 当标注显示在地图上时，其所指向的地理位置距离图标左上
        // 角各偏移10像素和25像素。您可以看到在本例中该位置即是
        // 图标中央下端的尖角位置。
        anchor: new BMapGL.Size(options.pointX, options.pointY)
    })
}
function setCenter(point) {
    if (baiduMap) {
        const pos = getPosition(point)
        baiduMap.setCenter(pos)
    }
}
function drawPolyline(points,options) {
    if (baiduMap){
        let p=new BMapGL.Polyline(points,options);
        baiduMap.addOverlay(p)
        return p
    } else return null
}
function removePolyline(p) {
    if (baiduMap){
        baiduMap.removeOverlay(p)
    }
}
function getDistance(start,end) {
    if (baiduMap){
        const p0=getPosition(start),p1=getPosition(end)
        return baiduMap.getDistance(p0,p1)
    } else return 0
}
let totalMarkers = 0
function placeMarker(point,markerOptions) {
    if (baiduMap) {
        const pos = getPosition(point)
        const mk = new BMapGL.Marker(pos, markerOptions)
        baiduMap.addOverlay(mk)
        totalMarkers ++
        return mk
    } else return null
}
function removeMarker(mk) {
    if (baiduMap && mk) baiduMap.removeOverlay(mk)
}

function addressSearch(address,callback) {
    if (baiduMap){
        var local=new BMapGL.LocalSearch(baiduMap,{
            renderOptions:{map: baiduMap}
        });
        local.search(address);
    }
}
function formattedAddress(province,city,location,subjectCode) {
    let cc = province ? province.search(/[\u4e00-\u9fa5]/) : (city ? city.search(/[\u4e00-\u9fa5]/):0)
    let s = subjectCode ? subjectCode : ''
    let a = ''
    if (cc>=0) {
        let aa = []
        if (province) aa.push(province)
        if (city) aa.push(city)
        if (location) aa.push(location)
        a=aa.join('')
    } else {
        let aa = []
        if (location) aa.push(location)
        if (city) aa.push(city)
        if (province) aa.push(province)
        a=aa.join(',')
    }
    if (s && a) return s+':'+a
    else return s+a
}
function deoCoderGetAddress(point, callback) {
    let add = {}
    add.type = 'bd09'
    add.longitude = point.lng
    add.latitude = point.lat
    let geocoder =  new BMapGL.Geocoder()
    geocoder.getLocation(getPosition(point), function(rs){
        const addComp = rs.addressComponents
        add.address = rs.address
        add.province = addComp.province
        if (rs.surroundingPois && rs.surroundingPois.length>0) add.subjectCode = rs.surroundingPois[0].title
        let cc = add.province.search(/[\u4e00-\u9fa5]/)
        if (cc>=0) {
            add.city = addComp.city + addComp.district
            add.location = addComp.street + addComp.streetNumber
            add.country = '中国'
            add.countryCode = 'CN'
        } else {
            add.city = (addComp.district + ' ' +addComp.city).trim()
            add.location = (addComp.streetNumber + ' ' + addComp.street).trim()
        }
        if (typeof callback === 'function') callback(add)
    })
}

function showInfoWindow({width, height, title, info, point, enableAutoPan}) {
    if (baiduMap){
        let pos = getPosition(point)
        var opts={
            width: width,
            height: height,
            enableAutoPan: enableAutoPan,
            title: title
        }
        var infoWindow=new BMapGL.InfoWindow(info,opts);  // 创建信息窗口对象
        baiduMap.openInfoWindow(infoWindow,pos);
        return infoWindow
    }
}

function addMapEventListener(event,listener,object) {
    if (!object) object = baiduMap
    if (object) {
        object.addEventListener(event, listener)
    }
}
function removeMapEventListener(event,listener,object) {
    if (!object) object = baiduMap
    if (object) {
        object.removeEventListener(event,listener)
    }
}
function createUserControl({element, position, offsetX, offsetY}) {
    //定义一个控件类
    function MyControl() {
        if (position=='RT') this.defaultAnchor = BMAP_ANCHOR_TOP_RIGHT
        else if (position=='LT') this.defaultAnchor = BMAP_ANCHOR_TOP_LEFT
        else if (position=='LB') this.defaultAnchor = BMAP_ANCHOR_BOTTOM_LEFT
        else if (position=='RB') this.defaultAnchor = BMAP_ANCHOR_BOTTOM_RIGHT
        this.defaultOffset = new BMapGL.Size(offsetX, offsetY)
    }
    //通过JavaScript的prototype属性继承于BMap.Control
    MyControl.prototype = new BMapGL.Control();

    //自定义控件必须实现自己的initialize方法，并且将控件的DOM元素返回
    //在本方法中创建个div元素作为控件的容器，并将其添加到地图容器中
    MyControl.prototype.initialize = function(map) {
        //创建一个dom元素
        var div = document.createElement('div');
        div.style.padding = "7px 5px";
        div.style.borderRadius = "5px";
        div.style.backgroundColor = "white";
        div.style.boxShadow = "0 2px 6px 0 rgba(27, 142, 236, 0.5)";
        div.appendChild(element)
        map.getContainer().appendChild(div);
        return div;
    }

    var myCtrl = new MyControl();
    return myCtrl;
}


function initMap(divId, point, myCtrl, useCityControl) {
    baiduMap = new BMapGL.Map(divId)
    let pos = (point ?getPosition(point) : new BMapGL.Point(106.59462970758844, 29.573881471271264))
    baiduMap.enableScrollWheelZoom(true)
    if (useCityControl){
        var cityControl=new BMapGL.CityListControl({
            anchor:BMAP_ANCHOR_BOTTOM_LEFT,offset:new BMapGL.Size(5,10)
        });
        baiduMap.addControl(cityControl);
    }
    baiduMap.addControl(new BMapGL.ZoomControl())
    baiduMap.addControl(new BMapGL.LocationControl())
    if (myCtrl) baiduMap.addControl(myCtrl)
    baiduMap.centerAndZoom(pos, 16)
    return baiduMap
}