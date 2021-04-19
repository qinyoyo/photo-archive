const distanceLimit = 10
const pointDataList = []
const stepIcon = makeIcon({
    url: "/static/image/step.png",
    width: 18,
    height: 27,
    pointX: 9,
    pointY: 26
})
const stepIcon0 = makeIcon({
    url: "/static/image/step0.png",
    width: 18,
    height: 27,
    pointX: 9,
    pointY: 26
})
const stepIcon1 = makeIcon({
    url: "/static/image/step1.png",
    width: 18,
    height: 27,
    pointX: 9,
    pointY: 26
})

function loadMarkerData(markerClick) {
    const setImg = function(img,index) {
        if (data[index].className) img.className = data[index].className
        img.setAttribute('data-index', index)
        img.setAttribute('data-prev', data[index].prev)
        img.setAttribute('data-next', data[index].next)
        img.setAttribute('src','/.thumb'+(data[index].src.indexOf('/')==0?'':'/')+data[index].src)
        img.setAttribute('data-src',data[index].src)
        if (data[index].orientation) addClass(img,'orientation-'+data[index].orientation)
        img.setAttribute('title',data[index].title)
    }
    const data = getPointData()
    const clickImgOn = function(img,x, width) {
        let imgL = (width - img.naturalWidth)/2 + img.naturalWidth / 3, imgR =  (width + img.naturalWidth)/2 - img.naturalWidth / 3
        if (x>=imgR){
            let next = parseInt(img.getAttribute('data-next'))
            if (next>=0) setImg(img,next)
        } else if (x<=imgL) {
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
            (function(){ // 使用闭包
                data[index].marker.addEventListener("click",function (e){
                    if (typeof markerClick==='function') markerClick(e,data[index])
                    e.domEvent.stopPropagation()
                    let infoWindow=null
                    let div=document.createElement('div')
                    div.className='img-info-window-container'
                    div.style.cursor='pointer'
                    let img=document.createElement('img')
                    div.appendChild(img)
                    div.onclick=function (event){
                        event.preventDefault()
                        event.stopPropagation()
                        clickImgOn(img,event.layerX,div.clientWidth)
                    }
                    div.ontouchstart=function (event){
                        //event.preventDefault()
                        //event.stopPropagation()
                        if (event.changedTouches.length>0){
                            let currentTarget=div
                            let left=0
                            while (currentTarget!==null){
                                left+=currentTarget.offsetLeft
                                currentTarget=currentTarget.offsetParent
                            }
                            clickImgOn(img,event.changedTouches[0].pageX-left,div.clientWidth)
                        }
                    }
                    img.onload=function (){
                        const dat=data[parseInt(img.getAttribute('data-index'))]
                        let title=dat.shootTime?dat.shootTime:'时间未知'
                        if (dat.prev>=0) title='<'+title
                        if (dat.next>=0) title=title+'>'
                        if (!infoWindow){
                            infoWindow=showInfoWindow({
                                width:img.naturalWidth,height:img.naturalHeight+10,title:title,info:div,point:dat.marker.getPosition(),enableAutoPan:true
                            })
                            infoWindow.disableCloseOnClick()
                        }else{
                            infoWindow.setWidth(img.naturalWidth)
                            infoWindow.setHeight(img.naturalHeight+10)
                            infoWindow.setTitle(title)
                            infoWindow.redraw()
                        }
                    }
                    setImg(img,index)
                });
            }())
        }
    }
}

let polyline = null
function showLine(forceShow) {
    function show() {
        const pointList = []
        pointDataList.forEach(function(d){
            if (d.marker) pointList.push(d.marker.getPosition())
        })
        if (pointList.length) polyline = drawPolyline(pointList, {strokeColor:"blue", strokeWeight:2, strokeOpacity:0.5});
    }
    if (!polyline) show()
    else {
        removePolyline(polyline)
        polyline = null
        if (forceShow) show()
    }
}


