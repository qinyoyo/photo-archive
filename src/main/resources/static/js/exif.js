const nameKeys = ['artist','shootTime',
        // 'model','lens',
        'subjectCode','country','countryCode','province','city','location',
        'headline','subTitle','scene',
        'orientation', 'rating',
        'longitude','latitude','altitude'
        // ,'gpsDatetime'
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
            if (field!='orientation') {
                copiedPoint[field] = document.getElementById(field).value
                document.getElementById('btnPaste').removeAttribute('disabled')
            }
        })
    }
    else document.querySelectorAll('input[name="selectedTags"]:checked').forEach(function(e){
        let field = e.value
        if (field!='orientation'){
            copiedPoint[field]=document.getElementById(field).value
            document.getElementById('btnPaste').removeAttribute('disabled')
        }
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

    const selectedTags = document.querySelectorAll('input[name="selectedTags"]:checked')

    if (selectedTags.length>1 || (selectedTags.length==1 && selectedTags.item(0).value!=='orientation')) document.getElementById('btnCopy').removeAttribute('disabled')
    else document.getElementById('btnCopy').setAttribute('disabled', 'disabled')

    if (enable && document.getElementById('fileName').value && selectedTags.length>0)
        document.getElementById('submit').removeAttribute('disabled')
    else document.getElementById('submit').setAttribute('disabled', 'disabled')
}
let prevSelectedDom  = null
function resetThumbImageTransform() {
    const thumbDom=document.querySelector('.thumb-image')
    thumbDom.removeAttribute('data-mirrorH')
    thumbDom.removeAttribute('data-mirrorV')
    thumbDom.removeAttribute('data-rotateZ')
    thumbDom.style.transform = thumbDom.style.msTransform = thumbDom.style.OTransform = thumbDom.style.MozTransform = 'none'
    document.querySelector('input[type="checkbox"][value="orientation"][name="selectedTags"]').checked = false
}
function setThumbImage(dom) {
    resetThumbImageTransform()
    const thumbDom=document.querySelector('.thumb-image')
    document.querySelector('input[type="checkbox"][value="orientation"][name="selectedTags"]').checked = false
    const items = document.querySelectorAll('.file-item.selected')
    if (prevSelectedDom && prevSelectedDom!==dom) removeClass(prevSelectedDom,'current-selection')
    prevSelectedDom = dom
    if (dom){
        addClass(dom,'current-selection')
        for (let i=0;i<items.length;i++) {
            if (dom===items.item(i)) {
                document.querySelector('.selection-length').innerText = (i+1) + '/' + items.length
                break
            }
        }
        thumbDom.parentElement.style.display = 'block'
        const file=dom.getAttribute('data-file')
        const lastModified=dom.getAttribute('data-lastModified')
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
    document.getElementById('subFolder').value = folders.join('|')
    document.getElementById('fileName').value = files.join('|')
    setThumbImage(selectedDom)
    if (selectedDom) {
        for (let i = 0; i < nameKeys.length; i++) {
            let v = selectedDom.getAttribute('data-'+nameKeys[i])
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
function clearSelection() {
    document.querySelectorAll('.file-item.selected').forEach(function (i) {
        removeClass(i,'selected')
    })
    document.getElementById('subFolder').value = ''
    document.getElementById('fileName').value = ''
    selectedDom = null
    setThumbImage(selectedDom)
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
            selectedDom = document.querySelector('.file-item.selected')
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
        let orientations = getTransformParams()
        if (orientations) data.append('orientations',orientations)
        Ajax.post(url,data,function (msg){
            let pp=(msg?msg.split(","):['error'])
            if (pp[0]=='ok'){
                resetThumbImageTransform()
                let lastModified=(new Date().getTime())+''
                document.querySelectorAll('.file-item.selected').forEach(function (dom){
                    dom.setAttribute('data-lastModified',lastModified)
                    for (let i=0; i<nameKeys.length; i++){
                        let val=data.get(nameKeys[i])
                        if (val) dom.setAttribute('data-'+nameKeys[i], ''+val)
                        else dom.removeAttribute('data-'+nameKeys[i])
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
    } else {
        toast('未选择修改字段')
        toggleSaveState(false)
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
            country: dom.getAttribute('data-country'),
            countryCode: dom.getAttribute('data-countryCode'),
            location: dom.getAttribute('data-location'),
            city: dom.getAttribute('data-city'),
            province: dom.getAttribute('data-province'),
            subjectCode: dom.getAttribute('data-subjectCode'),
            longitude: longitude ? longitude : (dom.getAttribute('data-longitude') ? parseFloat(dom.getAttribute('data-longitude')) : null),
            latitude: latitude ? latitude : (dom.getAttribute('data-latitude') ? parseFloat(dom.getAttribute('data-latitude')) : null),
            altitude: dom.getAttribute('data-altitude') ? parseFloat(dom.getAttribute('data-altitude')) : null
        }
}

function selectFilesByMarker(index, append) {
    const fileItems = document.querySelectorAll('.file-item')
    if (!append) fileItems.forEach(function(e) {removeClass(e,'selected')})
    selectedDom = null
    const param = getImageIndexSet(pointDataList,index)
    if (param){
        for (let i=0; i<param.set.length; i++){
            let d = pointDataList[param.set[i]]
            addClass(d.domElement,'selected')
            selectedDom=d.domElement
        }
    }
    afterSelection()
}

function markerClick(event,index) {
    let point = pointDataList[index]
    if (point && point.domElement) {
        mapPoint = pointInfoFromDom(point.domElement, point.longitude, point.latitude)
        placeSelectionMarkerOnMap({lng:point.longitude, lat: point.latitude})
        document.getElementById("address").value = formattedAddress(mapPoint.province,mapPoint.city,mapPoint.location,mapPoint.subjectCode)
        if (document.getElementById('markerSelection').checked) selectFilesByMarker(index,event.shiftKey)
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
    const photos = document.querySelectorAll('.file-item[data-longitude][data-latitude]')
    let icon = stepIcon0
    if (photos.length){
        for (let i=0; i<photos.length; i++){
            if (photos[i] === selectedDom) selectedIndex = i
            const point={
                domElement: photos[i],
                longitude:parseFloat(photos[i].getAttribute('data-longitude')),
                latitude:parseFloat(photos[i].getAttribute('data-latitude')),
                shootTime: photos[i].getAttribute('data-shootTime')?photos[i].getAttribute('data-shootTime').substring(0,16):null,
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
    if (selectedDom) {
        const wrapper = document.querySelector('.scroll-items-wrapper .scroll-items')
        if (wrapper && selectedDom.offsetTop > wrapper.offsetHeight) wrapper.scrollTop = selectedDom.offsetTop - wrapper.offsetHeight
    }
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
    document.getElementById('resource-list').style.maxHeight = (window.innerHeight -120) + 'px'
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
function doTransform(img,op) {
    if (op==='c') {
        resetThumbImageTransform()
        toggleSaveState(true)
    }
    else if (selectedDom && document.querySelectorAll('.file-item.selected').length==1){
        let rotateZ=img.getAttribute('data-rotateZ')?parseInt(img.getAttribute('data-rotateZ')):0
        let mirrorH=img.getAttribute('data-mirrorH')?true:false
        let mirrorV=img.getAttribute('data-mirrorV')?true:false
        let imgOrientation=selectedDom.getAttribute('data-orientation')?parseInt(selectedDom.getAttribute('data-orientation')):0
        if (op==='+') rotateZ+=90
        else if (op==='-') rotateZ-=90
        else if (op==='h') mirrorH= !mirrorH
        else if (op==='v') mirrorV= !mirrorV
        else return
        if (mirrorH&&mirrorV){
            mirrorH=mirrorV=false
            rotateZ+=180
        }
        rotateZ=rotateZ%360
        if (rotateZ<0) rotateZ+=360
        if (rotateZ==90 || rotateZ==270) {
            let scale = img.clientHeight/img.clientWidth
            transform({img,rotateZ,mirrorV,mirrorH,imgOrientation,scale})
        } else transform({img,rotateZ,mirrorV,mirrorH,imgOrientation})
        if (mirrorH) img.setAttribute('data-mirrorH','true')
        else img.removeAttribute('data-mirrorH')
        if (mirrorV) img.setAttribute('data-mirrorV','true')
        else img.removeAttribute('data-mirrorV')
        if (rotateZ) img.setAttribute('data-rotateZ',''+rotateZ)
        else img.removeAttribute('data-rotateZ')
        document.querySelector('input[type="checkbox"][value="orientation"][name="selectedTags"]').checked=(rotateZ||mirrorV||mirrorH)
        toggleSaveState(rotateZ||mirrorV||mirrorH)
    }
}
function getTransformParams() {
    if (selectedDom && document.querySelectorAll('.file-item.selected').length==1){
        let img=document.querySelector('.thumb-image')
        let rotateZ = img.getAttribute('data-rotateZ') ? parseInt(img.getAttribute('data-rotateZ')) : 0
        let mirrorH = img.getAttribute('data-mirrorH') ? true : false
        let mirrorV = img.getAttribute('data-mirrorV') ? true : false
        if (mirrorH && mirrorV) {
            mirrorH = mirrorV = false
            rotateZ += 180
        }
        rotateZ = rotateZ % 360
        if (rotateZ < 0) rotateZ += 360
        if (mirrorH || mirrorV || rotateZ) {
            let orientations = ''
            if (mirrorH) orientations = '2'
            else if (mirrorV) orientations = '4'
            if (rotateZ) {
                if (orientations) orientations = orientations + ','
                if (rotateZ==90) orientations += '6'
                else if (rotateZ==180) orientations += '3'
                else if (rotateZ==270) orientations += '8'
            }
            return orientations
        } else return null
    } else return null
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

    document.querySelector('.thumb-image').onclick = function(e) {
        if (e.offsetY<=50 && (e.offsetX<=50 || e.offsetX>=this.clientWidth - 50)) {
            doTransform(this,'c')
        } else if (e.offsetX < 50 && e.offsetY>this.clientHeight - 50) { // left-bottom
            doTransform(this,'-')
        } else if (e.offsetX>this.clientWidth - 50 && e.offsetY>this.clientHeight - 50) { // right-bottom
            doTransform(this,'+')
        } else if (e.offsetX < 50 || e.offsetX > this.clientWidth - 50) {
            doTransform(this,'h')
        } else if (e.offsetY < 50 || e.offsetY > this.clientHeight - 50) {
            doTransform(this,'v')
        } else {
            const selection=[]
            let start=0
            let i=0
            document.querySelectorAll('.file-item.selected').forEach(function (d){
                selection.push(d)
                if (d===selectedDom) start=i
                i++
            })
            if (selection.length>0) addImageDialog(start,function (i){
                if (i== -1) return selection.length
                else return getTranImageParams(selection[i],i)
            },{
                loop:selection.length>1,download:true
            })
        }
    }
    const fileList = document.querySelector('.file-list')
    if (fileList) fileList.onkeydown = exifKeyEvent
    const fileItems = document.querySelectorAll('.file-item')
    if (fileItems.length>0) fileItems.forEach(function(v) {
        if (!point) {
            let lng = v.getAttribute('data-longitude'),
                lat = v.getAttribute('data-latitude')
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
    document.getElementById('selectAllTags').onclick = function() {
        const selected = document.querySelectorAll('input.gps-info[name="selectedTags"]:checked')
        if (selected.length) {
            selected.forEach(function(e){
                e.checked = false
            })
            toggleSaveState(false)
        } else {
            document.querySelectorAll('input.gps-info[name="selectedTags"]').forEach(function(e){
                e.checked = true
            })
            toggleSaveState(true)
        }
    }
    document.querySelector('.selection-length').onclick = clearSelection
    document.getElementById('autoSaveMarkerDrag').onchange = function() {
        if (this.checked && !confirm("确实需要自动保存拖动改变的地理信息？")) this.checked = false
    }
    document.addEventListener("paste", getClipboardData)

}