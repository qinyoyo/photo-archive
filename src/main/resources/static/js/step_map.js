
function refresh(path) {
    window.location.href = '/step?path=' + encodeURI(path)
}
function clickStepMap(e) {
    deoCoderGetAddress(e.latlng, function(add) {
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
function loadMarkerData() {
    const setImg = function(img,index) {
        if (data[index].className) img.className = data[index].className
        img.setAttribute('data-index', index)
        img.setAttribute('data-prev', data[index].prev)
        img.setAttribute('data-next', data[index].next)
        img.setAttribute('src','/.thumb'+data[index].src)
        img.setAttribute('data-src',data[index].src)
        if (data[index].orientation) addClass(img,'orientation-'+data[index].orientation)
        img.setAttribute('title',data[index].title)
    }
    const data = getPointData()
    const clickImgOn = function(img,x, width) {
        if (x>2*width/3){
            let next = parseInt(img.getAttribute('data-next'))
            if (next>=0) setImg(img,next)
        } else if (x<width/3) {
            let prev = parseInt(img.getAttribute('data-prev'))
            if (prev>=0) setImg(img,prev)
        } else {
            let index = parseInt(img.getAttribute('data-index'))
            let start = index, end = index
            while (start>0 && data[start-1].next == start) start--
            while (end<data.length-1 && data[end+1].prev == end) end++
            addImageDialog(index - start, function(i) {
                if (i == -1) return end - start + 1
                else return {
                    src: data[start + i].src,
                    orientation: data[start + i].orientation,
                    rating: data[start + i].rating,
                    title: data[start + i].title,
                    imgIndex: i
                }
            },{
                loop: end > start
            })
        }
    }
    for (let index=0; index<data.length;index++) {
        if (data[index].marker) {
            data[index].marker.addEventListener("click", function(e){
                e.domEvent.stopPropagation()
                let infoWindow = null
                let div = document.createElement('div')
                div.className = 'img-info-window-container'
                div.style.cursor = 'pointer'
                let img = document.createElement('img')
                div.appendChild(img)
                div.onclick = function(event) {
                    event.preventDefault()
                    event.stopPropagation()
                    clickImgOn(img,event.offsetX,div.clientWidth)
                }
                div.ontouchstart = function(event) {
                    event.preventDefault()
                    event.stopPropagation()
                    if (event.changedTouches.length > 0) {
                        let currentTarget = div
                        let left = 0
                        while (currentTarget !== null) {
                            left += currentTarget.offsetLeft
                            currentTarget = currentTarget.offsetParent
                        }
                        clickImgOn(img,event.changedTouches[0].pageX - left, div.clientWidth)
                    }
                }
                img.onload = function() {
                    const dat = data[parseInt(img.getAttribute('data-index'))]
                    let title = dat.shootTime ? dat.shootTime : '时间未知'
                    if (dat.prev>=0) title = '<' + title
                    if (dat.next>=0) title = title + '>'
                    if (!infoWindow) {
                        infoWindow = showInfoWindow({
                            width: img.naturalWidth,
                            height: img.naturalHeight+10,
                            title: title,
                            info: div,
                            point: dat.marker.getPosition(),
                            enableAutoPan: true
                        })
                        infoWindow.disableCloseOnClick()
                    } else {
                        infoWindow.setWidth(img.naturalWidth)
                        infoWindow.setHeight(img.naturalHeight + 10)
                        infoWindow.setTitle(title)
                        infoWindow.redraw()
                    }
                }
                setImg(img,index)
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
function mapLoaded() {
    removeEventListener('tilesloaded',mapLoaded)
    loadMarkerData()
    hideWaiting()
}
window.onload=function(){
    showWaiting()
    document.querySelector('.map-wrapper').style.width = '100%'
    document.querySelector('.map-wrapper').style.height = window.innerHeight + 'px'
    initMap('mapContainer',firstPoint, stepControl(), true)
    mapEventListener('click',clickStepMap)
    mapEventListener('tilesloaded', mapLoaded)
}