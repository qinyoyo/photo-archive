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
let skipNextClick = false
function stopNextClick() {
    skipNextClick = true
    setTimeout(function() {
        skipNextClick = false
    },300)
}
function getImageIndexSet(data, index) {
    const paramsOf = function(imgIndex) {
        let start = imgIndex, end = imgIndex
        while (start>0 && data[start-1].next == start) start--
        while (end<data.length-1 && data[end+1].prev == end) end++
        return {
            start: start,
            end: end,
            position: data[start].marker ? data[start].marker.getPosition() : null
        }
    }
    if (index<0 || index>=data.length) return null
    if (data[index].imageIndexSet) return data[index].imageIndexSet
    let param = paramsOf(index)
    const indexSet = []
    let startIndex = 0
    if (param) {
        if (param.position) {
            for (let i=0;i<data.length;) {
                if (i>=param.start && i<=param.end) {
                    startIndex = index - param.start + indexSet.length
                    for (let j=param.start;j<=param.end;j++) indexSet.push(j)
                    i=param.end + 1
                    continue
                }
                let p = paramsOf(i)
                if (!p) i++
                else {
                    if (p.position && getDistance(p.position,param.position) <= distanceLimit) {
                        for (let j=p.start;j<=p.end;j++) indexSet.push(j)
                    }
                    i = p.end + 1
                }
            }
        } else {
            for (let i=param.start;i<=param.end;i++) indexSet.push(i)
            startIndex = index - param.start
        }
        data[index].imageIndexSet = {
            set: indexSet,
            startIndex: startIndex
        }
        return data[index].imageIndexSet
    } else return null
}
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
        let index = parseInt(img.getAttribute('data-index'))
        const params = getImageIndexSet(data,index)
        if (!params) return
        let imgL = (width - img.naturalWidth)/2 + img.naturalWidth / 3, imgR =  (width + img.naturalWidth)/2 - img.naturalWidth / 3
        if (x>=imgR){
            let next = params.startIndex+1
            if (next<params.set.length) setImg(img,params.set[next])
        } else if (x<=imgL) {
            let prev = params.startIndex - 1
            if (prev>=0) setImg(img,params.set[prev])
        } else {
            document.querySelector('.map-wrapper').style.display='none'
            addImageDialog(params.startIndex, function(i) {
                if (i == -1) return params.set.length
                else {
                    const dataIndex = params.set[i]
                    return {
                        src: data[dataIndex].src,
                        orientation: data[dataIndex].orientation,
                        rating: data[dataIndex].rating,
                        title: data[dataIndex].title,
                        imgIndex: i
                    }
                }
            },{
                loop: params.set.length > 1
            }, function (){
                document.querySelector('.map-wrapper').style.display='block'
            })
        }
    }
    for (let index=0; index<data.length;index++) {
        if (data[index].marker) {
            (function(){ // 使用闭包
                data[index].marker.addEventListener("click",function (e){
                    stopNextClick()
                    console.log('marker click')
                    if (typeof markerClick==='function') markerClick(e,data[index])
                    e.domEvent.stopPropagation()
                    let infoWindow=null
                    let div=document.createElement('div')
                    div.className='img-info-window-container'
                    div.style.cursor='pointer'
                    let img=document.createElement('img')
                    div.appendChild(img)
                    div.onclick=function (event){
                        stopNextClick()
                        console.log('img container click')
                        clickImgOn(img,event.layerX,div.clientWidth)
                    }
                    div.ontouchstart=function (event){
                        stopNextClick()
                        console.log('img container touch start')
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


