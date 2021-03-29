
function refresh(path) {
    window.location.href = '/step?path=' + encodeURI(path)
}
function selectFile(dom,event) {
    const photoItems = document.querySelectorAll('.photo-item')
    if (event.ctrlKey) {
        if (dom.className.indexOf('selected')>=0) {
            removeClass(dom,'selected')
            photoItems.forEach(function(i) {
                if (i.className.indexOf('selected')>=0) {
                    return
                }
            })
        }
        else {
            addClass(dom,'selected')
        }
    } else if (event.shiftKey) {
        let start = -1, end = -1, index = -1
        for (let i=0;i<photoItems.length;i++) {
            if (photoItems[i]===dom) index = i;
            if (photoItems[i].className.indexOf('selected')>=0) {
                if (start==-1) start=i;
                else end=i;
            }
        }
        if (index<start) {
            for (let i=index;i<start;i++) {
                addClass(photoItems[i],'selected')
            }
        } else if (index>end) {
            for (let i=end+1;i<=index;i++) {
                addClass(photoItems[i],'selected')
            }
        }
    } else {
        photoItems.forEach(function (i) {
            if (i == dom) addClass(i, 'selected')
            else removeClass(i, 'selected')
        })
    }
}


let markerList=[]
let mapObject = null
let stepIcon=null, stepIcon0 = null, stepIcon1 = null
function showMap() {
    if (stepIcon==null) {
        stepIcon = new BMapGL.Icon("/static/image/step.png", new BMapGL.Size(32, 32), {
            // 指定定位位置。
            // 当标注显示在地图上时，其所指向的地理位置距离图标左上
            // 角各偏移10像素和25像素。您可以看到在本例中该位置即是
            // 图标中央下端的尖角位置。
            anchor: new BMapGL.Size(15, 28)
        })
        stepIcon0 = new BMapGL.Icon("/static/image/step0.png", new BMapGL.Size(32, 32), {
            anchor: new BMapGL.Size(15, 28)
        })
        stepIcon1 = new BMapGL.Icon("/static/image/step1.png", new BMapGL.Size(32, 32), {
            anchor: new BMapGL.Size(15, 28)
        })
    }
    if (!mapObject) mapObject = initMap('mapContainer',null, stepControl())
    mapObject.addEventListener('click',clickStepMap)
    const selected = document.querySelectorAll('.photo-item.selected img')
    if (selected.length>0) {
        markerList.forEach(function(mk){
            removeMarker(mk)
        })
        const wrapper = document.querySelector('.map-wrapper')
        const width = wrapper.clientWidth, height = wrapper.clientHeight
        markerList = []
        const img = document.querySelector('.img-on-the-map img')
        selected.forEach(function (i) {
            let lon = parseFloat(i.getAttribute('data-gpslongitude'))
            let lat = parseFloat(i.getAttribute('data-gpslatitude'))
            let mk = placeMarker({lon:lon, lat:lat}, false, {icon: stepIcon})
            if (mk) {
                markerList.push(mk)
                mk.addEventListener("click", function(e){
                    img.onload = function() {
                        img.className = i.className
                        let x = e.clientX + img.naturalWidth <= width ? e.clientX : width - img.naturalWidth
                        let y = e.clientY + img.naturalHeight <= height ? e.clientY : height - img.naturalHeight
                        img.parentElement.style.left = x + 'px'
                        img.parentElement.style.width = img.naturalWidth + 'px'
                        img.parentElement.style.top = y + 'px'
                        img.parentElement.style.height = img.naturalHeight + 'px'
                        img.parentElement.style.display = 'block'
                        let index = markerList.indexOf(mk)
                        if (index<markerList.length-1) {
                            let next = markerList[index+1]
                            if (getDistance(mk.getPosition(),next.getPosition())<20) {
                                img.nextElementSibling.style.display='block'
                                img.nextElementSibling.onclick = function() {
                                    next.dispatchEvent(e, true);
                                }
                            }  else img.nextElementSibling.style.display='none'
                        } else img.nextElementSibling.style.display='none'
                    }
                    img.setAttribute('src',i.getAttribute('src'))
                });

            }
        })
        document.querySelector('.map-wrapper').style.display='block'
        document.querySelector('#app').style.display='none'
        if (markerList.length>0) {
            markerList[0].setIcon(stepIcon0)
            markerList[markerList.length-1].setIcon(stepIcon1)
            setTimeout(function(){
                mapObject.setCenter(markerList[0].getPosition())
            },1000)
        }
    }
}
let pointList=[]
let polyline = null
function showLine() {
    if (!polyline) {
        pointList = []
        markerList.forEach(function(m){
            pointList.push(m.getPosition())
        })
        polyline = new BMapGL.Polyline(pointList, {strokeColor:"blue", strokeWeight:2, strokeOpacity:0.5});
        mapObject.addOverlay(polyline)
    } else {
        mapObject.removeOverlay(polyline)
        polyline = null
        pointList=[]
    }
}
function hideMap() {
    return2view()
/*    document.querySelector('.map-wrapper').style.display = 'none'
    document.querySelector('#app').style.display = 'block'*/
}
function adjustSize(img) {
    let w = img.parentNode.clientWidth
    let iw = img.naturalWidth, ih = img.naturalHeight
    if (iw<=w) img.parentNode.style.height = Math.min(w,ih) + 'px'
    else img.parentNode.style.height = Math.trunc(Math.min(w, ih*w/iw)) + 'px';
}
window.onload=function(){
    document.querySelector('.map-wrapper').style.width = '100%'
    document.querySelector('.map-wrapper').style.height = window.innerHeight + 'px'
    document.querySelectorAll('.folder-item').forEach(function(d) {
        const path = d.getAttribute('data-folder')
        d.onclick=function () {
            refresh(path)
        }
    })
    document.querySelectorAll('.photo-item').forEach(function(v) {
        v.onclick = function(event) {
            selectFile(v,event)
        }
    })
    document.querySelector('.img-on-the-map').onclick = function() {
        this.style.display = 'none'
    }
    showMap()
}