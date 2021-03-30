
function refresh(path) {
    window.location.href = '/step?path=' + encodeURI(path)
}
function clickStepMap(e) {
    deoCoderGetAddress(e.latlng, '', function(add) {
        if (add && add.address) {
            showInfoWindow({
                width: 0,
                height: 0,
                info: add.address,
                title: add.subjectCode,
                enableAutoPan: true,
                point: e.latlng
            })
        }
    })
}

function stepControl() {
    var div = document.createElement('div');
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
    return createUserControl({
        element: div,
        position: 'RT',
        offsetX: 5,
        offsetY:5
    })
}

function showMap() {
    if (!getMap()) {
        initMap('mapContainer',null, stepControl()).disableDoubleClickZoom()
        mapEventListener('dblclick',clickStepMap)
    }
    const data = getPointData()
/*    const wrapper = document.querySelector('.map-wrapper')
    const width = wrapper.clientWidth, height = wrapper.clientHeight
    const img = document.querySelector('.img-on-the-map img')*/
    for (let index=0; index<data.length;index++) {
        if (data[index].marker) {
            data[index].marker.addEventListener("click", function(e){
                e.domEvent.stopPropagation()
                let div = document.createElement('div')
                div.style.marginBottom = '10px'
                let img = document.createElement('img')
                if (data[index].className) img.className = data[index].className
                img.style.width='100%'
                img.style.height='100%'
                img.style.marginLeft ='8px'
                div.appendChild(img)
                img.onload = function() {
                    showInfoWindow({
                        width:0,
                        height:0,
                        title:data[index].shootTime,
                        info: div,
                        point: data[index].marker.getPosition(),
                        enableAutoPan: true
                    }).disableCloseOnClick()
                }

/*                img.onload = function() {
                    if (data[index].className) img.className = data[index].className
                    let x = e.clientX, y=e.clientY
                    if (!x) {
                        if (e.domEvent.changedTouches.length > 0) {
                            x = e.domEvent.changedTouches[0].clientX
                            y = e.domEvent.changedTouches[0].clientY
                        } else if (e.domEvent.touches.length>0) {
                            x = e.domEvent.touches[0].clientX
                            y = e.domEvent.touches[0].clientY
                        } else {
                            x=0
                            y=0
                        }
                    }
                    if (x + img.naturalWidth > width) x = width - img.naturalWidth
                    if (y + img.naturalHeight > height) y = height - img.naturalHeight
                    img.parentElement.style.left = x + 'px'
                    img.parentElement.style.width = img.naturalWidth + 'px'
                    img.parentElement.style.top = y + 'px'
                    img.parentElement.style.height = img.naturalHeight + 'px'
                    img.parentElement.style.display = 'block'
                    if (index < data.length-1) {
                        let next = data[index+1].marker
                        if (getDistance(data[index].marker.getPosition(),next.getPosition())<20) {
                            img.nextElementSibling.style.display='block'
                            img.nextElementSibling.onclick = function() {
                                next.dispatchEvent(e, true);
                            }
                        }  else img.nextElementSibling.style.display='none'
                    } else img.nextElementSibling.style.display='none'
                }*/
                img.setAttribute('src',data[index].src)
                img.setAttribute('title',(data[index].shootTime ? data[index].shootTime + '\n': '')+data[index].address)
            });
        }
    }
}
let polyline = null
function showLine() {
    if (!polyline) {
        const pointList = []
        pointDataList.forEach(function(d){
            pointList.push(d.marker.getPosition())
        })
        polyline = drawPolyline(pointList, {strokeColor:"blue", strokeWeight:2, strokeOpacity:0.5});
    } else {
        removePolyline(polyline)
        polyline = null
    }
}
function hideMap() {
    return2view()
}
window.onload=function(){
    document.querySelector('.map-wrapper').style.width = '100%'
    document.querySelector('.map-wrapper').style.height = window.innerHeight + 'px'
    document.querySelector('.img-on-the-map').onclick = function(e) {
        e.stopPropagation()
        this.style.display = 'none'
    }
    showMap()
}