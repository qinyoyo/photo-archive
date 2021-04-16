const dataKeys = ['data-artist','data-datetimeoriginal','data-model','data-Lensid',
        'data-subjectCode','data-country-primarylocationname','data-country-code','data-province-state','data-city','data-sub-location',
        'data-headline','data-caption-abstract','data-scene','data-orientation','data-rating',
        'data-gpslongitude','data-gpslatitude','data-gpsaltitude','data-gpsdatetime'
    ],
    nameKeys = ['artist','shootTime','model','lens',
        'subjectCode','country','countryCode','province','city','location',
        'headline','subTitle','scene','orientation','rating',
        'longitude','latitude','altitude','gpsDatetime'
    ]
let selectedDom = null
let mapPoint = {}
let marker = null
let copiedPoint = null
const clipSign = 'PV_EXIF_CLIPBOARD_DATA: '

function setClipboardData(text) {
    const textArea = document.createElement("textarea");
    textArea.style.position = 'fixed';
    textArea.style.top = '-1000px';
    textArea.style.left = '0';
    textArea.style.padding = '0';
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';
    textArea.style.background = 'transparent';
    textArea.style.color = 'transparent';
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    let result = false
    try {
        result =  document.execCommand('copy');
    } catch (err) {
        result = false
    }
    textArea.remove()
    return result
}
function exifKeyEvent(event) {
    if (event.code=='ArrowUp' || event.code=='Numpad8'){
        moveSelection(true)
    } else if (event.code=='ArrowDown' || event.code=='Numpad2'){
        moveSelection(false)
    } else if (event.ctrlKey && document.querySelector('.map-wrapper').style.display=='none') {
        event.preventDefault();
        if (event.code=='KeyC') {
            copyFields(['subjectCode','country','countryCode','province','city','location',
                'headline','subTitle','longitude','latitude','altitude'])
        } else if (event.code=='KeyV') pasteFields()
        else if (event.code=='KeyS') save()
    }
}
function getClipboardData(event) {
    document.removeEventListener('paste',getClipboardData)
    if (event.clipboardData || event.originalEvent) {
        var clipboardData = (event.clipboardData || window.clipboardData);
        var text = clipboardData.getData('text');
        if (text && text.indexOf(clipSign)==0) {
            try{
                copiedPoint=JSON.parse(text.substring(clipSign.length))
                if (copiedPoint) {
                    document.getElementById('btnPaste').removeAttribute('disabled')
                    pasteFields()
                    event.preventDefault();
                }
            } catch(e) {
                copiedPoint = null
            }
        }
    }
}
function placeSelectionMarkerOnMap(point,options) {
    if (getMap()){
        const pos=getPosition(point)
        if (marker) {
            marker.setPosition(pos)
        } else {
            marker=placeMarker(pos,options)
        }
        removeClass(document.getElementById('addressSelected'),'disabled')
        setTimeout(function(){
            setCenter(pos)
        },200)
    }
}
function refresh(path) {
    const e = document.getElementById('recursion')
    window.location.href = '/exif?path=' + encodeURI(path) + '&recursion='+(e.checked ? 'true':'false')
}
function copyFields(tags){
    copiedPoint = {}
    document.getElementById('btnPaste').setAttribute('disabled', 'disabled')
    if (tags) {
        tags.forEach(function(field) {
            copiedPoint[field] = document.getElementById(field).value
        })
        document.getElementById('btnPaste').removeAttribute('disabled')
    }
    else document.querySelectorAll('input[name="selectedTags"]:checked').forEach(function(e){
        let field = e.value
        copiedPoint[field] = document.getElementById(field).value
        document.getElementById('btnPaste').removeAttribute('disabled')
    })
    setClipboardData(clipSign + JSON.stringify(copiedPoint))
}
function pasteFields(){
    if (copiedPoint) {
        Object.keys(copiedPoint).forEach(function(field) {
            document.getElementById(field).value = copiedPoint[field]
            document.getElementById(field).nextElementSibling.checked = true
        })
        toggleSaveState(true)
    }
}
function toggleSaveState(enable) {
    if (copiedPoint && Object.keys(copiedPoint).length>0) document.getElementById('btnPaste').removeAttribute('disabled')
    else document.getElementById('btnPaste').setAttribute('disabled', 'disabled')

    const selectedTag = (document.querySelectorAll('input[name="selectedTags"]:checked').length>0)

    if (selectedTag) document.getElementById('btnCopy').removeAttribute('disabled')
    else document.getElementById('btnCopy').setAttribute('disabled', 'disabled')

    if (enable && document.getElementById('fileName').value && selectedTag)
        document.getElementById('submit').removeAttribute('disabled')
    else document.getElementById('submit').setAttribute('disabled', 'disabled')
}
function setThumbImage(dom) {
    const file = dom.getAttribute('data-file')
    const lastModified = dom.getAttribute('data-lastmodified')
    const curPath = dom.getAttribute('data-folder').replace(/\\/g, '/')
    const thumbDom = document.querySelector('.thumb-image')
    thumbDom.setAttribute('src', '/.thumb/' + curPath + (curPath ? '/' : '') + file
        + (lastModified ? '?click=' + lastModified : ''))
    thumbDom.setAttribute('data-src', '/' + curPath + (curPath ? '/' : '') + file
        + (lastModified ? '?click=' + lastModified : ''))
    thumbDom.setAttribute('title', dom.getAttribute('title'))
}
function afterSelection() {
    let folders = [], files=[]
    document.querySelectorAll('.file-item.selected').forEach(function (i) {
        folders.push(i.getAttribute('data-folder'))
        files.push(i.getAttribute('data-file'))
    })
    document.getElementById('subFolder').value = folders.join(',')
    document.getElementById('fileName').value = files.join(',')
    if (selectedDom) {
        setThumbImage(selectedDom)
        for (let i = 0; i < dataKeys.length; i++) {
            let v = selectedDom.getAttribute(dataKeys[i])
            if (v && (nameKeys[i] === 'shootTime' || nameKeys[i] === 'gpsDatetime')) v = v.replace(' ', 'T')
            else if (v && nameKeys[i] === 'orientation') {
                if (v && parseInt(v) > 1 && !sessionOptions.supportOrientation) {
                    document.querySelector('.thumb-image').className = "thumb-image img-index-0 orientation-" + v
                } else document.querySelector('.thumb-image').className = "thumb-image img-index-0"
            }
            document.getElementById(nameKeys[i]).value = (v ? v : '')
        }
        if (files.length == 1) {
            document.querySelectorAll('form input[type="checkbox"]').forEach(function(e){
                e.checked = false
            })
        }
        toggleSaveState(false)
        if (document.querySelector('.map-wrapper').style.display=='none') {
            setDefaultMarket()
        }
    }
}
function moveSelection(up) {
    const fileItems = document.querySelectorAll('.file-item')
    let start = -1, end = -1
    for (let i=0;i<fileItems.length;i++) {
        if (fileItems[i].className.indexOf('selected')>=0) {
            if (start==-1) start = i;
            end = i;
        }
    }
    let index = up ? start - 1 : end + 1
    if (index>=0 && index<fileItems.length) {
        selectedDom = fileItems[index]
        fileItems.forEach(function (i) {
            if (i == selectedDom) addClass(i, 'selected')
            else removeClass(i, 'selected')
        })
    }
    afterSelection()
}
function selectFile(dom,event) {
    const fileItems = document.querySelectorAll('.file-item')
    if (event.shiftKey || (event.target && event.target.tagName == 'I')) {
        selectedDom = dom
        let start = -1, index = -1
        for (let i=0;i<fileItems.length;i++) {
            if (fileItems[i]===dom) index = i
            if (fileItems[i].className.indexOf('selected')>=0) {
                start=i
            }
            if (start>=0 && index>=0) break
        }
        if (index<start) {
            for (let i=index;i<start;i++) {
                addClass(fileItems[i],'selected')
            }
        } else if (index>start && start>=0) {
            for (let i=start+1;i<=index;i++) {
                addClass(fileItems[i],'selected')
            }
        }
    } else if (event.ctrlKey || window.sessionOptions.mobile) {
        if (dom.className.indexOf('selected')>=0) {
            removeClass(dom,'selected')
            fileItems.forEach(function(i) {
                if (i.className.indexOf('selected')>=0) {
                    selectedDom = i
                    return
                }
            })
        }
        else {
            addClass(dom,'selected')
            selectedDom = dom
        }
    } else {
        selectedDom = dom
        fileItems.forEach(function (i) {
            if (i == dom) addClass(i, 'selected')
            else removeClass(i, 'selected')
        })
    }
    afterSelection()
}

function changed(dom) {
    dom.nextElementSibling.checked = true
    if (dom.id=='country') {
        document.getElementById('countryCode').value
            = dom.options[dom.selectedIndex].getAttribute('data-code')
        document.getElementById('countryCode').nextElementSibling.checked = true
    }
    toggleSaveState(true)
}
function countryToggle(dom) {
    document.getElementById('countryCode').nextElementSibling.checked = dom.checked
}
function save() {
    const form = document.getElementById('exifForm')
    let url = '/exifSave'
    let data = new FormData(form)
    if (data.get('selectedTags')){
        toggleSaveState(false)
        Ajax.post(url,data,function (msg){
            let pp=(msg?msg.split(","):['error'])
            if (pp[0]=='ok'){
                let lastModified=(new Date().getTime())+''
                document.querySelectorAll('.file-item.selected').forEach(function (dom){
                    dom.setAttribute('data-lastmodified',lastModified)
                    for (let i=0; i<dataKeys.length; i++){
                        let val=data.get(nameKeys[i])
                        if (val) dom.setAttribute(dataKeys[i],val)
                        else dom.removeAttribute(dataKeys[i])
                        if (nameKeys[i]=='orientation'&&dom===selectedDom){
                            const file=dom.getAttribute('data-file')
                            const curPath=dom.getAttribute('data-folder').replace(/\\/g,'/')
                            document.querySelector('.thumb-image').setAttribute('src','/.thumb/'+curPath+(curPath?'/':'')+file+'?click='+lastModified)
                            document.querySelector('.thumb-image').setAttribute('data-src','/'+curPath+(curPath?'/':'')+file+'?click='+lastModified)
                        }
                    }
                })
                if (pp.length>1) toast(pp[1])
            }else{
                //toggleSaveState(true)
                toast(msg)
            }
        })
    }
}
function selectMapPoint(add) {
    placeSelectionMarkerOnMap({lng:add.longitude, lat: add.latitude})
    mapPoint = add
    document.getElementById("address").value = (mapPoint.subjectCode ? mapPoint.subjectCode + ':' : '') + mapPoint.address
    if (add.address && add.address.search(/[\u4e00-\u9fa5]/) < 0) {
        let aa = add.address.split(',')
        let country = aa[aa.length - 1].trim()
        if (country === 'Scotland' ||country == 'Northern Ireland') {
            mapPoint.country = 'England'
            mapPoint.countryCode = 'GB'
        } else {
            const options = document.getElementById('country').options
            for (let i=0;i<options.length;i++){
                if (options[i].value==country){
                    mapPoint.country=country
                    mapPoint.countryCode=options[i].getAttribute('data-code')
                    return
                }
            }
            console.log('国家地区检索失败: '+ add.address)
        }
    } else if (add.address) {
        mapPoint.country = '中国'
        mapPoint.countryCode = 'CN'
    }
}
let pauseMapClick = false
function clickExifMap(e) {
    console.log('map click')
    if (!pauseMapClick) deoCoderGetAddress(e.latlng, selectMapPoint)
}
function exifControl() {
    var div = document.createElement('div');
    var search = document.createElement('input')
    search.id = 'address'
    search.className = 'tag-value'
    search.style.width = '200px'
    search.onkeypress = function (e) {
        if (e.key == 'Enter') addressSearch(this.value)
    }
    div.appendChild(search)

    var btnSearch = document.createElement('i')
    btnSearch.className = 'fa fa-search'
    btnSearch.style.marginLeft = '5px';
    btnSearch.onclick = function() {
        addressSearch(search.value)
    }
    div.appendChild(btnSearch)

    var btnOk = document.createElement('i')
    btnOk.id = 'addressSelected'
    btnOk.className = 'fa fa-check disabled'
    btnOk.onclick = function() {
        if (btnOk.className.indexOf('disabled')<0) {
            selectAddress()
            hideMap()
            toggleSaveState(true)
        }
    }
    btnOk.style.marginLeft = '15px';
    btnOk.style.marginRight = '15px';
    div.appendChild(btnOk)

    var btnHide = document.createElement('i')
    btnHide.className = 'fa fa-close'
    btnHide.onclick = hideMap
    btnHide.style.margin = '0 5px';
    div.appendChild(btnHide)
    return createUserControl({
        element: div,
        position: 'RT',
        offsetX: 5,
        offsetY:5
    })
}
let point = null
function setDefaultMarket() {
    let lng=document.getElementById('longitude').value,lat=document.getElementById('latitude').value
    if (lng && lat){
        let point = {lng:parseFloat(lng),lat:parseFloat(lat)}
        placeSelectionMarkerOnMap(point)
        addClass(document.getElementById('addressSelected'),'disabled')
        document.getElementById("address").value=formattedAddress(document.getElementById('province').value,document.getElementById('city').value,document.getElementById('location').value,document.getElementById('subjectCode').value)
    }
}
function pointInfoFromDom(dom,longitude,latitude) {
    return {
            country: dom.getAttribute('data-country-primarylocationname'),
            countryCode: dom.getAttribute('data-country-code'),
            location: dom.getAttribute('data-sub-location'),
            city: dom.getAttribute('data-city'),
            province: dom.getAttribute('data-province-state'),
            subjectCode: dom.getAttribute('data-subjectCode'),
            longitude: longitude ? longitude : (dom.getAttribute('data-gpslongitude') ? parseFloat(domElement.getAttribute('data-gpslongitude')) : null),
            latitude: latitude ? latitude : (dom.getAttribute('data-gpslatitude') ? parseFloat(domElement.getAttribute('data-gpslatitude')) : null),
            altitude: dom.getAttribute('data-gpsaltitude') ? parseFloat(domElement.getAttribute('data-gpsaltitude')) : null
        }
}
function markerDrag(e,index) {
    e.domEvent.stopPropagation()
    e.domEvent.preventDefault()
    let p = pointDataList[index].marker.getPosition()
    pointDataList[index].longitude = p.lng
    pointDataList[index].latitude = p.lat

    const fileItems = document.querySelectorAll('.file-item')
    fileItems.forEach(function(e) {removeClass(e,'selected')})
    let folders = [], files=[]
    for (let i=0;;i++) {
        addClass(pointDataList[i+index].domElement,'selected')
        selectedDom = pointDataList[i+index].domElement
        folders.push(pointDataList[i+index].domElement.getAttribute('data-folder'))
        files.push(pointDataList[i+index].domElement.getAttribute('data-file'))
        if (pointDataList[i+index].next==-1) break
    }
    document.getElementById('subFolder').value = folders.join(',')
    document.getElementById('fileName').value = files.join(',')
    if (selectedDom) {
        setThumbImage(selectedDom)
        toggleSaveState(true)
    }
    const afterGeo = function() {
        if (document.getElementById('autoSaveMarkerDrag').checked) {
            selectAddress()
            save()
        }
        showLine(true)
    }
    for (let i=0;i<pointDataList.length;i++) {  // 自动粘连
        if (i!=index && pointDataList[i].marker && getDistance(p,pointDataList[i].marker.getPosition()) <= distanceLimit) {
            pointDataList[index].longitude = pointDataList[i].longitude
            pointDataList[index].latitude = pointDataList[i].latitude
            mapPoint = pointInfoFromDom(pointDataList[i].domElement, pointDataList[i].longitude, pointDataList[i].latitude)
            p = {lng: pointDataList[index].longitude, lat: pointDataList[index].latitude}
            pointDataList[index].marker.setPosition(p)
            placeSelectionMarkerOnMap(p)
            document.getElementById("address").value = formattedAddress(mapPoint.province,mapPoint.city,mapPoint.location,mapPoint.subjectCode)
            afterGeo()
            return
        }
    }
    deoCoderGetAddress(p, function(add) {
        selectMapPoint(add)
        afterGeo()
    })
}
function getPointData() {
    if (pointDataList && pointDataList.length>0) {
        pointDataList.forEach(function(p){
            if (p.marker) removeMarker(p.marker)
        })
        pointDataList.splice(0,pointDataList.length)
    }
    let selectedIndex = -1
    let distance = 1000
    const photos = document.querySelectorAll('.file-item[data-gpslongitude][data-gpslatitude]')
    if (photos.length){
        for (let i=0; i<photos.length; i++){
            if (photos[i] === selectedDom) selectedIndex = i
            const point={
                domElement: photos[i],
                longitude:parseFloat(photos[i].getAttribute('data-gpslongitude')),
                latitude:parseFloat(photos[i].getAttribute('data-gpslatitude')),
                shootTime: photos[i].getAttribute('data-datetimeoriginal')?photos[i].getAttribute('data-datetimeoriginal').substring(0,16):null,
                orientation: photos[i].getAttribute('data-orientation') ? parseInt(photos[i].getAttribute('data-orientation')) : null,
                src: photos[i].getAttribute('data-src'),
                rating: photos[i].getAttribute('data-rating') ? parseInt(photos[i].getAttribute('data-rating')) : null,
                title: photos[i].getAttribute('title'),
                prev: -1,
                next: -1
            }
            if (i>0){
                distance = getDistance({lng: pointDataList[i-1].longitude,lat:pointDataList[i-1].latitude},{lng:point.longitude, lat:point.latitude})
                if (distance < distanceLimit) {
                    pointDataList[i-1].next = i
                    point.prev = i-1
                    point.marker = null
                }
            }
            if (point.prev == -1) {
                (function(){
                    point.marker = placeMarker({lng:point.longitude, lat:point.latitude},
                    {icon: selectedIndex<0 ? stepIcon : (selectedIndex == i ? stepIcon0 : stepIcon1), enableDragging: true})
                    point.marker.addEventListener('dragend',function (e){
                        pauseMapClick=true
                        markerDrag(e,i)
                        setTimeout(function() {
                            pauseMapClick=false
                        },200)
                    })
                }())
            }
            if (selectedIndex == i && point.marker==null) {
                for (let j=i-1;j>=0;j--) {
                    if (pointDataList[j].marker) {
                        pointDataList[j].marker.setIcon(stepIcon0)
                        break
                    }
                }
            }
            pointDataList.push(point)
        }
    }
    showLine()
    return pointDataList
}
function markerClick(e, point) {
    console.log('marker click')
    if (point && point.domElement) {
        mapPoint = pointInfoFromDom(point.domElement, point.longitude, point.latitude)
        placeSelectionMarkerOnMap({lng:point.longitude, lat: point.latitude})
        document.getElementById("address").value = formattedAddress(mapPoint.province,mapPoint.city,mapPoint.location,mapPoint.subjectCode)
    }
}
function mapLoaded() {
    removeEventListener('tilesloaded',mapLoaded)
    loadMarkerData(markerClick)
    hideWaiting()
}

function showMap() {
    if (document.getElementById('autoSaveMarkerDrag').checked && !confirm("确实需要自动保存拖动改变的地理信息？")) return
    if (!getMap()){
        showWaiting()
        document.querySelector('.map-wrapper').style.width = '100%'
        document.querySelector('.map-wrapper').style.height = window.innerHeight + 'px'
        initMap('mapContainer',point, exifControl(),true)
        mapEventListener('click',clickExifMap)
        mapEventListener('tilesloaded', mapLoaded)
    } else loadMarkerData(markerClick)
    document.querySelector('.map-wrapper').style.display='block'
    document.querySelector('#app').style.display='none'
    removeClass(document.body,"exif")
    addClass(document.body,"map")
    const fileList = document.querySelector('.file-list')
    if (fileList) fileList.onkeydown = null
    document.onkeydown = exifKeyEvent
}
function hideMap() {
    if (polyline){
        removePolyline(polyline)
        polyline=null
    }
    removeClass(document.body,"map")
    addClass(document.body,"exif")
    document.querySelector('.map-wrapper').style.display = 'none'
    document.querySelector('#app').style.display = 'block'
    const fileList = document.querySelector('.file-list')
    if (fileList) fileList.onkeydown = exifKeyEvent
    document.onkeydown = null
}
function setAddressValue(field) {
    let e = document.getElementById(field)
    if (mapPoint[field]) {
        if (field==='longitude' || field==='latitude') e.value = mapPoint[field].toFixed(7)
        else e.value = mapPoint[field]
        e.nextElementSibling.checked=true
    }
    else {
        e.value = ''
        e.nextElementSibling.checked=false
    }
}
function selectAddress() {
    setAddressValue('longitude')
    setAddressValue('latitude')
    setAddressValue('province')
    setAddressValue('city')
    setAddressValue('location')
    setAddressValue('subjectCode')
    setAddressValue('country')
    setAddressValue('countryCode')
}
window.onload=function(){
    document.querySelector('.map-wrapper').style.width = '100%'
    document.querySelector('.map-wrapper').style.height = (window.innerHeight) + 'px'
    document.getElementById('app').style.height = (window.innerHeight -20) +'px'
    document.querySelectorAll('.folder-item, #recursion').forEach(function(d) {
        const path = d.getAttribute('data-folder')
        d.onclick=function () {
            refresh(path)
        }
    })
    document.querySelector('.thumb-image').onclick = function() {
        addImageDialog(0, this)
    }
    const fileList = document.querySelector('.file-list')
    if (fileList) fileList.onkeydown = exifKeyEvent
    const fileItems = document.querySelectorAll('.file-item')
    if (fileItems.length>0) fileItems.forEach(function(v) {
        if (!point) {
            let lng = v.getAttribute('data-gpslongitude'),
                lat = v.getAttribute('data-gpslatitude')
            if (lng && lat) {
                point = {lng:parseFloat(lng),lat:parseFloat(lat)}
            }
        }
        v.onclick = function(event) {
            selectFile(v,event)
        }
    })
    document.querySelectorAll('input[name="selectedTags"]').forEach(function(e){
        e.onclick=function() {
            toggleSaveState(true)
        }
    })
    document.addEventListener("paste", getClipboardData)
}