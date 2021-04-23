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
            marker.setZIndex(1)
        }
        removeClass(document.getElementById('addressSelected'),'disabled')
        if (document.querySelector('.map-wrapper').style.display=='block')  setCenter(pos)
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
    if (document.getElementById('fileName').value) document.getElementById('btnMove').removeAttribute('disabled')
    else document.getElementById('btnMove').setAttribute('disabled', 'disabled')
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
    const thumbDom=document.querySelector('.thumb-image')
    const items = document.querySelectorAll('.file-item.selected')
    if (dom){
        for (let i=0;i<items.length;i++) {
            if (dom===items.item(i)) {
                document.querySelector('.selection-length').innerText = (i+1) + '/' + items.length
                break
            }
        }
        thumbDom.parentElement.style.display = 'block'
        const file=dom.getAttribute('data-file')
        const lastModified=dom.getAttribute('data-lastmodified')
        const curPath=dom.getAttribute('data-folder').replace(/\\/g,'/')
        thumbDom.setAttribute('src','/.thumb/'+curPath+(curPath?'/':'')+file+(lastModified?'?click='+lastModified:''))
        thumbDom.setAttribute('data-src','/'+curPath+(curPath?'/':'')+file+(lastModified?'?click='+lastModified:''))
        thumbDom.setAttribute('title',dom.getAttribute('title'))
    } else {
        thumbDom.parentElement.style.display = 'none'
        document.querySelector('.selection-length').innerText = '0/' + items.length
    }
}
function afterSelection() {
    let folders = [], files=[]
    document.querySelectorAll('.file-item.selected').forEach(function (i) {
        folders.push(i.getAttribute('data-folder'))
        files.push(i.getAttribute('data-file'))
    })
    document.getElementById('subFolder').value = folders.join(',')
    document.getElementById('fileName').value = files.join(',')
    setThumbImage(selectedDom)
    if (selectedDom) {
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
        let start = -1, index = -1, end = -1
        for (let i=0;i<fileItems.length;i++) {
            if (fileItems[i]===dom) index = i
            if (fileItems[i].className.indexOf('selected')>=0) {
                end=i
                if (start==-1) start = i
            }
        }
        if (index<start) {
            for (let i=index;i<start;i++) addClass(fileItems[i],'selected')
        } else if (index>end) {
            for (let i=end+1;i<=index;i++) addClass(fileItems[i],'selected')
        } else if (start<index && end>index) {
            for (let i=index+1;i<=end;i++) removeClass(fileItems[i],'selected')
        }
    } else if (event.ctrlKey || window.sessionOptions.mobile) {
        if (dom.className.indexOf('selected')>=0) {
            removeClass(dom,'selected')
            selectedDom = null
            if (fileItems.length) selectedDom = fileItems.item(0)
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

function clickExifMap(e) {
    if (skipNextClick) return
    console.log('map click')
    deoCoderGetAddress(e.latlng, selectMapPoint)
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
        let dom = document.getElementById("address")
        if (dom) dom.value=formattedAddress(document.getElementById('province').value,document.getElementById('city').value,document.getElementById('location').value,document.getElementById('subjectCode').value)
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
            longitude: longitude ? longitude : (dom.getAttribute('data-gpslongitude') ? parseFloat(dom.getAttribute('data-gpslongitude')) : null),
            latitude: latitude ? latitude : (dom.getAttribute('data-gpslatitude') ? parseFloat(dom.getAttribute('data-gpslatitude')) : null),
            altitude: dom.getAttribute('data-gpsaltitude') ? parseFloat(dom.getAttribute('data-gpsaltitude')) : null
        }
}

function selectFilesByMarker(index) {
    const fileItems = document.querySelectorAll('.file-item')
    fileItems.forEach(function(e) {removeClass(e,'selected')})
    selectedDom = null
    let folders = [], files=[]
    const param = getImageIndexSet(pointDataList,index)
    if (param){
        for (let i=0; i<param.set.length; i++){
            let d = pointDataList[param.set[i]]
            addClass(d.domElement,'selected')
            selectedDom=d.domElement
            folders.push(d.domElement.getAttribute('data-folder'))
            files.push(d.domElement.getAttribute('data-file'))
        }
    }
    document.getElementById('subFolder').value = folders.join(',')
    document.getElementById('fileName').value = files.join(',')
    setThumbImage(selectedDom)
    if (selectedDom) {
        toggleSaveState(true)
    }
}

function markerClick(event,index) {
    let point = pointDataList[index]
    if (point && point.domElement) {
        mapPoint = pointInfoFromDom(point.domElement, point.longitude, point.latitude)
        placeSelectionMarkerOnMap({lng:point.longitude, lat: point.latitude})
        document.getElementById("address").value = formattedAddress(mapPoint.province,mapPoint.city,mapPoint.location,mapPoint.subjectCode)
        if (event.shiftKey) selectFilesByMarker(index)
    }
}
function markerDrag(event, index) {
    stopNextClick()
    if (!document.getElementById('autoSaveMarkerDrag').checked) return
    let p = pointDataList[index].marker.getPosition()
    pointDataList[index].longitude = p.lng
    pointDataList[index].latitude = p.lat
    const clearImageIndexSet = function(next) {
        if (pointDataList[next].imageIndexSet) {
            let set = pointDataList[next].imageIndexSet.set
            for (let i=0;i<set.length;i++) {
                pointDataList[set[i]].imageIndexSet = null
            }
        } else while (next!=-1) {
            pointDataList[next].imageIndexSet = null
            next = pointDataList[next].next
        }
    }
    const afterGeo = function(moveBy) {
        clearImageIndexSet(index)
        setAllImageIndexSet(pointDataList)
        selectFilesByMarker(index)
        selectAddress()
        save()
        if (moveBy) movePointOfPolylineBy(moveBy)
        else redrawPolyline()
    }
    let moveByMarker = pointDataList[index].marker
    for (let i=0;i<pointDataList.length;i++) {  // 自动粘连
        if (i!=index && pointDataList[i].marker && getDistance(p,pointDataList[i].marker.getPosition()) <= distanceLimit) {
            pointDataList[index].longitude = pointDataList[i].longitude
            pointDataList[index].latitude = pointDataList[i].latitude
            mapPoint = pointInfoFromDom(pointDataList[i].domElement, pointDataList[i].longitude, pointDataList[i].latitude)
            p = {lng: pointDataList[index].longitude, lat: pointDataList[index].latitude}
            pointDataList[index].marker.setPosition(p)
            placeSelectionMarkerOnMap(p)
            document.getElementById("address").value = formattedAddress(mapPoint.province,mapPoint.city,mapPoint.location,mapPoint.subjectCode)
            let midMarker = 0
            let i0 = Math.min(index,i), i1 = Math.max(index,i)
            for (let j = i0+1; j<i1; j++) {
                if (pointDataList[j].marker) {
                    midMarker = 1
                    break
                }
            }
            if (!midMarker) {
                pointDataList[i0].next = i0+1
                pointDataList[i0+1].prev = i0
                pointDataList[i1-1].next = i1
                pointDataList[i1].prev = i1-1
                if (pointDataList[i1].marker) {
                    clearImageIndexSet(i1)
                    removeMarker(pointDataList[i1].marker)
                }
                pointDataList[i1].marker = null
                moveByMarker = null
            }
            afterGeo(moveByMarker)
            return
        }
    }
    deoCoderGetAddress(p, function(add) {
        selectMapPoint(add)
        afterGeo(moveByMarker)
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
    let icon = stepIcon0
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
                    {icon: i==photos.length-1 ? stepIcon1 : icon})
                    icon = stepIcon
                    point.marker.setZIndex(1000+i)
                }())
            }
            if (i == photos.length -1 && point.marker==null) {
                for (let j=i-1;j>=0;j--) {
                    if (pointDataList[j].marker) {
                        pointDataList[j].marker.setIcon(stepIcon1)
                        break
                    }
                }
            }
            pointDataList.push(point)
        }
    }
    redrawPolyline()
    return pointDataList
}

function mapLoaded() {
    loadMarkerData(markerClick, markerDrag)
    hideWaiting()
}

function showMap() {
    if (!getMap()){
        showWaiting()
        document.querySelector('.map-wrapper').style.width = '100%'
        document.querySelector('.map-wrapper').style.height = window.innerHeight + 'px'
        initMap('mapContainer',point, exifControl(),true)
        addMapEventListener('click',clickExifMap)
        mapLoaded()
    } else loadMarkerData(markerClick, markerDrag)
    if (marker) setTimeout(function(){
        setCenter(marker.getPosition())
    },200)
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
const getResource = function(callback) {
    const dialog = document.getElementById('select-resource')
    dialog.querySelector('.dialog__content').style.width = '500px'
    const html = document.getElementById('select-resource-content').innerHTML.trim()
    if (!html) {
        reloadResourceByPath(document.getElementById('app').getAttribute("data-folder"))
    }
    dialog.querySelector('button.resource-selected').onclick = function() {
        const d = dialog.querySelector('.folder-item.selected')
        if (d) callback(d.getAttribute('data-folder'))
        dialog.style.display='none'
    }
    dialog.style.display='block'
}
const reloadResourceByPath = function(path, selectedPath) {
    let currentPath = document.getElementById('app').getAttribute("data-folder")
    const url = '/resource?folderOnly=true&current='+encodeURI(currentPath) + (path ? '&path=' + encodeURI(path) : '')
    Ajax.get(url, function (responseText) {
        if (responseText && responseText!='error') {
            document.getElementById('select-resource-content').innerHTML = responseText
            initResource(selectedPath)
        }
    })
}
const makeDirectory = function() {
    window.input({
        title: '输入目录名',
        dialogClass: 'float-editor__dialog',
        inputType: 'text',
        callback: function(v) {
            if (v) {
                const dirs = document.querySelectorAll('.folder-head .folder-head__left .folder-item')
                if (dirs.length) {
                    const current = dirs.item(dirs.length-1).getAttribute('data-folder')
                    const url='/mkdir?current='+encodeURI(current)+'&path='+encodeURI(v)
                    Ajax.get(url,function (msg){
                        if (msg&&msg=='ok'){
                            reloadResourceByPath(current,v)
                        }else toast(msg)
                    })
                }
            }
        }
    })
}
const initResource = function(selectedPath) {
    const dialog = document.getElementById('select-resource')
    dialog.querySelectorAll('.folder-item .folder-item__arrow, .folder-head .folder-item').forEach(function(d){
        d.onclick = function () {
            reloadResourceByPath((d.tagName==='I'?d.parentElement:d).getAttribute('data-folder'))
        }
    })
    dialog.querySelector('.add-folder').onclick = makeDirectory
    dialog.querySelectorAll('.folder-list .folder-item').forEach(function(d) {
        if (selectedPath && d.querySelector('span').innerText === selectedPath) {
            addClass(d,'selected')
            selectedPath = null
        }
        d.ondblclick=function () {
            reloadResourceByPath(d.getAttribute('data-folder'))
        }
        d.onclick=function () {
            dialog.querySelectorAll('.folder-item.selected').forEach(function(d1) {
                removeClass(d1,'selected')
            })
            addClass(d,'selected')
        }
    })
}
function moveFiles() {
    if (document.getElementById('fileName').value) getResource(function(path) {
        let url = '/moveFile?path=' + encodeURI(path)
            + '&subFolder=' + encodeURI(document.getElementById('subFolder').value)
            + '&fileName=' + encodeURI(document.getElementById('fileName').value)
        Ajax.get(url, function (msg) {
            if (msg === 'ok') {
                document.getElementById('subFolder').value = ''
                document.getElementById('fileName').value = ''
                document.querySelectorAll('.file-item.selected').forEach(function(d){
                    d.remove()
                })
                selectedDom = null
                afterSelection()
            } else toast(msg)
        })
    })
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
        const selection = []
        let start = 0
        let i=0
        document.querySelectorAll('.file-item.selected').forEach(function (d) {
            selection.push(d)
            if (d===selectedDom) start = i
            i++
        })
        if (selection.length>0) addImageDialog(start, function(i) {
            if (i == -1) return selection.length
            else {
                return getTranImageParams(selection[i], i)
            }
        },{
            loop: selection.length > 1,
            download: true
        })
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
    document.getElementById('autoSaveMarkerDrag').onchange = function() {
        if (this.checked && !confirm("确实需要自动保存拖动改变的地理信息？")) this.checked = false
    }
    document.addEventListener("paste", getClipboardData)
}