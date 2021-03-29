let mapPoint = {}
let map = null
let marker = null

function removeMarker(mk) {
    if (map && mk) map.removeOverlay(mk)
}
function bdPosition(wgs84, notTrans) {
    let  loc = notTrans ? wgs84 : wgs84tobd09(wgs84)
    return new BMapGL.Point(loc.lon, loc.lat);
}
function setCenter(wgs84, notTrans) {
    if (map) {
        map.setCenter(bdPosition(wgs84, notTrans))
    }
}

function placeMarker(wgs84, notTrans, markerOptions) {
    if (map) {
        const pos = bdPosition(wgs84, notTrans)
        const mk = new BMapGL.Marker(pos, markerOptions)
        map.addOverlay(mk)
        return mk
    } else return null
}
function selectionPointMarker(wgs84,notTrans) {
    removeMarker(marker)
    marker = placeMarker(wgs84,notTrans)
    map.setCenter(bdPosition(wgs84, notTrans))
    removeClass(document.getElementById('addressSelected'),'disabled')
}
function getPoint(address) {
    var local = new BMapGL.LocalSearch(map, {
        renderOptions:{map: map}
    });
    local.search(address);
}
function getAddress(latlng) {
    mapPoint={}
    let loc={lon:latlng.lng, lat:latlng.lat}

    selectionPointMarker(loc,true)
    let wgs84 = bd09towgs84(loc)
    mapPoint.longitude = wgs84.lon
    mapPoint.latitude = wgs84.lat
    let geocoder =  new BMapGL.Geocoder()
    geocoder.getLocation(latlng, function(rs){
        const addComp = rs.addressComponents
        mapPoint.province = addComp.province
        if (rs.surroundingPois && rs.surroundingPois.length>0) {
            mapPoint.subjectCode = rs.surroundingPois[0].title
            document.getElementById("address").value = mapPoint.subjectCode + ':' + rs.address
        } else document.getElementById("address").value = rs.address
        let cc = mapPoint.province.search(/[\u4e00-\u9fa5]/)
        if (cc>=0) {
            mapPoint.city = addComp.city + addComp.district
            mapPoint.location = addComp.street + addComp.streetNumber
            mapPoint.country = '中国'
            mapPoint.countryCode = 'CN'

        } else {
            mapPoint.city = (addComp.district + ' ' +addComp.city).trim()
            mapPoint.location = (addComp.streetNumber + ' ' + addComp.street).trim()
        }
    })
}
function clickMap(e) {
    getAddress(e.latlng)
}

function stepControl() {
    //定义一个控件类
    function MyControl() {
        this.defaultAnchor = BMAP_ANCHOR_TOP_RIGHT;
        this.defaultOffset = new BMapGL.Size(5, 5)
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


        var btnOk = document.createElement('i')
        btnOk.id = 'showLine'
        btnOk.className = 'fa fa-line-chart'
        btnOk.onclick = showLine
        btnOk.style.marginLeft = '15px';
        btnOk.style.marginRight = '15px';
        div.appendChild(btnOk)

        var btnHide = document.createElement('i')
        btnHide.className = 'fa fa-close'
        btnHide.onclick = hideMap
        btnHide.style.margin = '0 5px';
        div.appendChild(btnHide)

        map.getContainer().appendChild(div);
        return div;
    }

    var myCtrl = new MyControl();
    return myCtrl;
}

function exifControl() {
    //定义一个控件类
    function MyControl() {
        this.defaultAnchor = BMAP_ANCHOR_TOP_RIGHT;
        this.defaultOffset = new BMapGL.Size(5, 5)
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

        var search = document.createElement('input')
        search.id = 'address'
        search.className = 'tag-value'
        search.style.width = '300px'
        search.onkeypress = function (e) {
            if (e.key == 'Enter') getPoint(this.value)
        }
        div.appendChild(search)

        var btnSearch = document.createElement('i')
        btnSearch.className = 'fa fa-search'
        btnSearch.style.marginLeft = '5px';
        btnSearch.onclick = function() {
            getPoint(search.value)
        }
        div.appendChild(btnSearch)

        var btnOk = document.createElement('i')
        btnOk.id = 'addressSelected'
        btnOk.className = 'fa fa-check disabled'
        btnOk.onclick = function() {
            if (btnOk.className.indexOf('disabled')<0) selectAddress()
        }
        btnOk.style.marginLeft = '15px';
        btnOk.style.marginRight = '15px';
        div.appendChild(btnOk)

        var btnHide = document.createElement('i')
        btnHide.className = 'fa fa-close'
        btnHide.onclick = hideMap
        btnHide.style.margin = '0 5px';
        div.appendChild(btnHide)

        map.getContainer().appendChild(div);
        return div;
    }

    var myCtrl = new MyControl();
    return myCtrl;
}
function initMap(divId, wgs84, myCtrl, useCityControl) {
    map = new BMapGL.Map(divId)
    let pos
    if (wgs84) {
        let  loc = wgs84tobd09(wgs84)
        pos = new BMapGL.Point(loc.lon, loc.lat)
    } else pos = new BMapGL.Point(106.637110, 29.718058)

    map.centerAndZoom(pos, 12)
    map.enableScrollWheelZoom(true)

    if (useCityControl){
        var cityControl=new BMapGL.CityListControl({
            anchor:BMAP_ANCHOR_TOP_LEFT,offset:new BMapGL.Size(10,5)
        });
        map.addControl(cityControl);
    }
    map.addControl(new BMapGL.ZoomControl())
    map.addControl(new BMapGL.LocationControl())
    if (myCtrl) map.addControl(myCtrl)
    return map
}