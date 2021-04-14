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
function moveUseKey(event) {
    if (event.code=='ArrowUp' || event.code=='Numpad8'){
        moveSelection(true)
    } else if (event.code=='ArrowDown' || event.code=='Numpad2'){
        moveSelection(false)
    } else if (event.ctrlKey && document.querySelector('.map-wrapper').style.display=='none') {
        event.preventDefault();
        if (event.code=='KeyC') copyFields()
        else if (event.code=='KeyA'){
            document.querySelectorAll('form input[type="checkbox"]').forEach(function (e){
                e.checked=(e.value!='artist'&&e.value!='shootTime'&&e.value!='model'&&e.value!='lens'&&e.value!='scene'&&e.value!='orientation'&&e.value!='rating'&&e.value!='gpsDatetime')
            })
            document.getElementById('btnCopy').removeAttribute('disabled')
            copyFields()
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
function placeSelectionMarkerOnMap(point) {
    if (getMap()){
        if (marker) removeMarker(marker)
        const pos=getPosition(point)
        marker=placeMarker(pos)
        setCenter(pos)
        removeClass(document.getElementById('addressSelected'),'disabled')
    }
}
function refresh(path) {
    const e = document.getElementById('recursion')
    window.location.href = '/exif?path=' + encodeURI(path) + '&recursion='+(e.checked ? 'true':'false')
}
function copyFields(){
    copiedPoint = {}
    document.getElementById('btnPaste').setAttribute('disabled', 'disabled')
    document.querySelectorAll('input[name="selectedTags"]:checked').forEach(function(e){
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
function afterSelection() {
    let folders = [], files=[]
    document.querySelectorAll('.file-item.selected').forEach(function (i) {
        folders.push(i.getAttribute('data-folder'))
        files.push(i.getAttribute('data-file'))
    })
    document.getElementById('subFolder').value = folders.join(',')
    document.getElementById('fileName').value = files.join(',')
    if (selectedDom) {
        const file = selectedDom.getAttribute('data-file')
        const lastModified = selectedDom.getAttribute('data-lastmodified')
        const curPath = selectedDom.getAttribute('data-folder').replace(/\\/g, '/')
        const thumbDom = document.querySelector('.thumb-image')
        thumbDom.setAttribute('src', '/.thumb/' + curPath + (curPath ? '/' : '') + file
            + (lastModified ? '?click=' + lastModified : ''))
        thumbDom.setAttribute('data-src', '/' + curPath + (curPath ? '/' : '') + file
            + (lastModified ? '?click=' + lastModified : ''))
        thumbDom.setAttribute('title', selectedDom.getAttribute('title'))
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
        if (document.querySelector('.map-wrapper').style.display=='block') {
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
                        dom.setAttribute(dataKeys[i],val?val:'')
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

function clickExifMap(e) {
    deoCoderGetAddress(e.latlng, function(add) {
        placeSelectionMarkerOnMap(e.latlng)
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
    })
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
    if (lng&&lat){
        setTimeout(function (){
            placeSelectionMarkerOnMap({lng:parseFloat(lng),lat:parseFloat(lat)})
            addClass(document.getElementById('addressSelected'),'disabled')
            document.getElementById("address").value=formattedAddress(document.getElementById('province').value,document.getElementById('city').value,document.getElementById('location').value,document.getElementById('subjectCode').value)
        },100)
    }
}
function showMap() {
    if (!getMap()){
        initMap('mapContainer',point, exifControl(),true)
        mapEventListener('click',clickExifMap)
    }
    document.querySelector('.map-wrapper').style.display='block'
    document.querySelector('#app').style.display='none'
    const fileList = document.querySelector('.file-list')
    if (fileList) fileList.onkeydown = null
    document.onkeydown = moveUseKey
    setDefaultMarket()
}
function hideMap() {
    document.querySelector('.map-wrapper').style.display = 'none'
    document.querySelector('#app').style.display = 'block'
    const fileList = document.querySelector('.file-list')
    if (fileList) fileList.onkeydown = moveUseKey
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
    hideMap()
    toggleSaveState(true)
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
    if (fileList) fileList.onkeydown = moveUseKey
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