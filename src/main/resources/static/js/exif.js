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
function refresh(path) {
    const e = document.getElementById('recursion')
    window.location.href = '/exif?path=' + encodeURI(path) + '&recursion='+(e.checked ? 'true':'false')
}
function selectFile(dom,event) {
    const fileItems = document.querySelectorAll('.file-item')
    if (event.ctrlKey) {
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
    } else if (event.shiftKey) {
        selectedDom = dom
        let start = -1, end = -1, index = -1
        for (let i=0;i<fileItems.length;i++) {
            if (fileItems[i]===dom) index = i;
            if (fileItems[i].className.indexOf('selected')>=0) {
                if (start==-1) start=i;
                else end=i;
            }
        }
        if (index<start) {
            for (let i=index;i<start;i++) {
                addClass(fileItems[i],'selected')
            }
        } else if (index>end) {
            for (let i=end+1;i<=index;i++) {
                addClass(fileItems[i],'selected')
            }
        }
    } else {
        selectedDom = dom
        fileItems.forEach(function (i) {
            if (i == dom) addClass(i, 'selected')
            else removeClass(i, 'selected')
        })
    }
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
            if (v && nameKeys[i] === 'shootTime') v = v.replace(' ', 'T')
            else if (v && nameKeys[i] === 'gpsDatetime' && v.length == 20) v = v.substring(0, 4) + '-' + v.substring(5, 7) + '-' + v.substring(8, 10) + 'T' + v.substring(11, 19)
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
        document.getElementById('submit').setAttribute('disabled', 'disabled')
    }
}

function changed(dom) {
    document.getElementById('submit').removeAttribute('disabled')
    dom.nextElementSibling.checked = true
    if (dom.id=='country') {
        document.getElementById('countryCode').value
            = dom.options[dom.selectedIndex].getAttribute('data-code')
        document.getElementById('countryCode').nextElementSibling.checked = true
    }
}
function countryToggle(dom) {
    document.getElementById('countryCode').nextElementSibling.checked = dom.checked
}
function save() {
    const form = document.getElementById('exifForm')
    let url = '/exifSave'
    let data = new FormData(form)
    let value = data.get('gpsDatetime')
    if (value) {
        value=value.substring(0,4)+':'+value.substring(5,7)+':'+value.substring(8,10)+' '+value.substring(11,19)+'Z'
        data.set('gpsDatetime',value)
    }
    Ajax.post(url,data,function(msg) {
        if (msg && msg.indexOf('ok')==0) {
            document.getElementById('submit').setAttribute('disabled','disabled')
            if (selectedDom) for (let i=0;i<dataKeys.length;i++) {
                let val=data.get(nameKeys[i])
                selectedDom.setAttribute(dataKeys[i],val?val:'')
            }
            let pp = msg.split(',')
            if (pp.length>1) {
                const file=selectedDom.getAttribute('data-file')
                const curPath = selectedDom.getAttribute('data-folder').replace(/\\/g,'/')
                selectedDom.setAttribute('data-lastmodified',pp[1])
                document.querySelector('.thumb-image').setAttribute('src','/.thumb/' + curPath + (curPath?'/':'') +file
                    +'?click='+pp[1])
                document.querySelector('.thumb-image').setAttribute('data-src','/' + curPath + (curPath?'/':'') + file
                    +'?click='+pp[1])
            }

        } else message(msg)
    })
}
let point = null
let mapObject = null
function showMap() {
    document.querySelector('.map-wrapper').style.display = 'block'
    document.querySelector('#app').style.display = 'none'
    if (!mapObject) mapObject = initMap('mapContainer',point, exifControl(), true)
    mapObject.addEventListener('click',clickMap)
    let lon = document.getElementById('longitude').value,
        lat = document.getElementById('latitude').value
    if (lon && lat) {
        setTimeout(function(){
            selectionPointMarker({lon:parseFloat(lon),lat:parseFloat(lat)})
            addClass(document.getElementById('addressSelected'),'disabled')
        },100)
    }

}
function hideMap() {
    document.querySelector('.map-wrapper').style.display = 'none'
    document.querySelector('#app').style.display = 'block'
}
function setAddressValue(field) {
    let e = document.getElementById(field)
    if (mapPoint[field]) {
        if (field==='longitude' || field==='latitude') e.value = mapPoint[field].toFixed(6)
        else e.value = mapPoint[field]
        e.nextElementSibling.checked=true
    }
    else {
        e.value = ''
        e.nextElementSibling.checked=false
    }
}
function selectAddress() {
    document.getElementById('submit').removeAttribute('disabled')
    setAddressValue('longitude')
    setAddressValue('latitude')
    setAddressValue('province')
    setAddressValue('city')
    setAddressValue('location')
    setAddressValue('subjectCode')
    setAddressValue('country')
    setAddressValue('countryCode')
    hideMap()
}
window.onload=function(){
    document.querySelector('.map-wrapper').style.width = '100%'
    document.querySelector('.map-wrapper').style.height = (window.innerHeight - 20) + 'px'
    document.getElementById('app').style.height = (window.innerHeight - 20) +'px'
    document.querySelectorAll('.folder-item').forEach(function(d) {
        const path = d.getAttribute('data-folder')
        d.onclick=function () {
            refresh(path)
        }
    });

    const fileItems = document.querySelectorAll('.file-item')
    if (fileItems.length>0) fileItems.forEach(function(v) {
        if (!point) {
            let lon = v.getAttribute('data-gpslongitude'),
                lat = v.getAttribute('data-gpslatitude')
            if (lon && lat) {
                point = {lon:parseFloat(lon),lat:parseFloat(lat)}
            }
        }
        v.onclick = function(event) {
            selectFile(v,event)
        }
    })
    TransformImage('.thumb-image')
}