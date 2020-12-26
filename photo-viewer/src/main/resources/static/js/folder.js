/*
const MODEL = '<div class="v-modal" tabindex="0" style="z-index: 6666;"></div>'
const DIALOG = '<div class="dialog__wrapper" style="z-index: 8888;">' +
               '  <div class="dialog" style="margin-top: 0px; top: 0px;">' +
               '    <div class="dialog__body">' +
               '      <img src="SRC" />' +
               '    </div>' +
               '  </div>' +
               '</div>'
*/

function isMobile() {
    const ua = navigator.userAgent.toLowerCase()
    const agents = ["android", "iphone","symbianos", "phone","mobile"]
    let flag = true;
    if (agents.some(a=>{ if (ua.indexOf(a) >= 0) return true })) return true
    else return false
}
function addModel() {
    let node = document.createElement("div")
    node.className = 'v-modal'
    node.tabIndex = -1
    node.style.zIndex = "6000"
    document.querySelector('body').appendChild(node)
}
function removeImageDialog() {
    let m = document.querySelector('.v-modal')
    if (m) m.remove()
    let d = document.querySelector('.dialog__wrapper')
    if (d) d.remove()
    document.querySelector('body').style.overflow = 'auto'
}


function addImageDialog(src, index) {
    let maxScale = 1.0
    const pageW = window.innerWidth, pageH = window.innerHeight
    let imageReady = false
    const loadImageBy = function (img, index) {
        if (index<0) return false
        let thumb = document.querySelector('.img-index-'+index)
        if (thumb) {
            waitingIcon.style.display = 'block'
            let src = thumb.getAttribute('src')
            imageReady = false
            if (src.indexOf('.thumb/')==0) src = src.substring(7)
            img.className = 'img-fit'
            img.src = src
            return true
        } else return false
    }

    removeImageDialog()
    addModel()
    const body = document.querySelector('body')
    body.style.overflow = 'hidden'
    let wrapper = document.createElement("div")
    wrapper.className = 'dialog__wrapper'
    wrapper.style.zIndex = "6001"
    let dialog = document.createElement("div")
    dialog.className = 'dialog'
    dialog.style.zIndex = "6002"
    let dialogBody = document.createElement("div")
    dialogBody.className = 'dialog__body'
    dialogBody.style.zIndex = "6003"
    dialogBody.style.width = pageW+'px'
    dialogBody.style.height = pageH+'px'
    dialogBody.tabIndex = -1

    let img = document.createElement("img")
    img.src = src
    img.className = 'img-fit'
    img.style.zIndex = "6004"

    let button = document.createElement("button")
    button.className = 'button-close'
    button.style.zIndex = "6005"

    let closeIcon = document.createElement("img")
    closeIcon.src = 'static/image/close.png'
    button.appendChild(closeIcon)

    let waitingIcon = document.createElement("button")
    waitingIcon.className = 'waiting-icon'
    waitingIcon.style.zIndex = '6006'

    let waitingI = document.createElement("i")
    waitingI.className = 'fa fa-spinner fa-spin animated'
    waitingIcon.appendChild(waitingI)

    dialogBody.appendChild(img)
    dialogBody.appendChild(button)
    dialogBody.appendChild(waitingIcon)

    dialog.appendChild(dialogBody)
    wrapper.appendChild(dialog)
    body.appendChild(wrapper)

    let imageW = pageW, imageH = pageH
    let clientW = pageW,clientH = pageH
    const resetImageParameter = function(img) {
        let b90 = Math.trunc(img.rotateZ/90)
        if (Math.trunc(b90 / 2)*2 != b90) {
            imageW = img.naturalHeight
            imageH = img.naturalWidth
        } else {
            imageW = img.naturalWidth
            imageH = img.naturalHeight
        }
        if ((imageW > imageH && clientW <= clientH) || (imageW < imageH && clientW >= clientH)) {
            let t = clientW
            clientW = clientH
            clientH = t
        }
        maxScale = Math.max(imageW/pageW, imageW/pageH)
        if (maxScale<1) maxScale=1
    }

    img.onload = function() {
        waitingIcon.style.display = 'none'
        img.rotateZ = 0
        resetImageParameter(img)
        if (isMobile()) {
            img.scaleX = img.scaleY = 1
            clientW = Math.round(imageW * img.scaleX / maxScale)
            clientH = Math.round(imageH * img.scaleY / maxScale)
        }
        imageReady = true
    }
    button.onclick = function() {
        removeImageDialog()
    }
    Transform(img)

    dialogBody.onclick=function (event){
        if (event.clientX>img.offsetLeft + img.offsetWidth ||event.clientX<img.offsetLeft){
            if (loadImageBy(img,event.clientX<img.offsetLeft ? index-1 : index+1)){
                if (event.clientX<img.offsetLeft) index--; else index++;
            }
        }
    }
    body.onkeydown=function (event){
        if (event.code=='ArrowLeft'||event.code=='ArrowRight'){
            if (loadImageBy(img,event.code=='ArrowLeft'?index-1:index+1)){
                if (event.code=='ArrowLeft') index--; else index++;
            }
        }else if (event.code=='Space' || event.code=='ArrowUp'||event.code=='ArrowDown'){
            if (!imageReady) return
            img.rotateZ+=(event.code=='ArrowUp'?-90:90)
            if (img.rotateZ>=360) img.rotateZ=0; else if (img.rotateZ<0) img.rotateZ+=360;
            resetImageParameter(img)
        }
    }
    if (isMobile()) {
        const pageAxis2ImageAxis = function(img, point) {
            clientW = Math.round(imageW * img.scaleX / maxScale)
            clientH = Math.round(imageH * img.scaleY / maxScale)
            let clientLeft = Math.round((clientW - pageW) / 2 - img.translateX),
                clientTop = Math.round((clientH - pageH) / 2 - img.translateY);
            return {
                x: Math.round((clientLeft + point.x) * maxScale / img.scaleX),
                y: Math.round((clientTop + point.y) * maxScale / img.scaleY)
            }
        }
        const translateLimit = function(img) {
            let limit = {}
            let clientLeft = Math.round((clientW - pageW) / 2)
            let clientTop = Math.round((clientH - pageH) / 2)
            if (clientLeft<0) limit.x = 0; else limit.x = clientLeft;
            if (clientTop<0) limit.y = 0; else limit.y = clientTop;
            return limit
        }
        const translate = function(img, p) {
            let limit = translateLimit(img)
            if (p.x < -limit.x) p.x = -limit.x;
            else if (p.x>limit.x) p.x=limit.x;
            if (p.y < -limit.y) p.y = -limit.y;
            else if (p.y > limit.y) p.y=limit.y;
            img.translateX = p.x
            img.translateY = p.y
        }
        let initScale = 1
        let swipStart = false
        wrapper.style.overflow = 'hidden'
        new AlloyFinger(img, {
            rotate:function(event){
                event.stopPropagation();
                if (imageReady) img.rotateZ += event.angle;
            },
            multipointStart: function () {
                event.stopPropagation();
                if (imageReady) initScale = img.scaleX;
            },
            pinch: function (event) {
                event.stopPropagation();
                if (imageReady) {
                    let scale = img.scaleY = initScale * event.zoom;
                    if (scale > maxScale) scale = maxScale
                    else if (scale < 1) scale = 1
                    img.scaleX = img.scaleY = scale
                    clientW = Math.round(imageW * img.scaleX / maxScale)
                    clientH = Math.round(imageH * img.scaleY / maxScale)
                }
            },
            doubleTap:function(event){
                event.stopPropagation();
                if (!imageReady) return
                if (img.scaleX>1) {
                    img.scaleX = img.scaleY =  1
                    clientW = Math.round(imageW / maxScale)
                    clientH = Math.round(imageH / maxScale)
                    img.translateX = img.translateY = 0
                }
                else {
                    let page = {
                        x: event.changedTouches[0].pageX,
                        y: event.changedTouches[0].pageY
                    }
                    let image = pageAxis2ImageAxis(img, page)
                    img.scaleX = img.scaleY =  maxScale;
                    clientW = Math.round(imageW * img.scaleX / maxScale)
                    clientH = Math.round(imageH * img.scaleY / maxScale)
                    translate(img, {
                        x: Math.round(imageW/2-image.x),
                        y: Math.round(imageH/2-image.y)
                    })
                }
            },
            touchStart:function(event) {
                event.stopPropagation();
                if (event.touches.length == 1 && event.touches[0].pageX > pageW - 30 || event.touches[0].pageX < 30) swipStart = true;
                else swipStart = false;
            },
            touchMove:function(event) {
                event.stopPropagation();
                if (!imageReady) return
                if (event.touches.length==1) {
                    translate(img,{
                        x: img.translateX + event.deltaX,
                        y: img.translateY += event.deltaY
                    })
                }
            },
            tap:function(event) {
                let x = event.changedTouches[0].pageX
                if (x<30 || x > pageW - 30) {
                    if (loadImageBy(img,x<30 ? index-1:index+1)){
                        if (x<100) index--; else index++;
                    }
                }
            },
            swipe:function(event){
                event.stopPropagation();
                if (swipStart && loadImageBy(img,event.direction==="Right" ? index-1:index+1)){
                    if (event.direction==="Right") index--; else index++;
                }
                swipStart = false;
            },
            touchEnd: function (event) {
                event.stopPropagation();
                if (!imageReady) return
                let angle = Math.round(img.rotateZ/90) * 90;
                if (angle!=img.rotateZ) {
                    if (angle >= 360) angle -= 360; else if (angle < 0) angle += 360;
                    img.rotateZ = angle;
                }
                translate(img,{ x: img.translateX, y: img.translateY })
                resetImageParameter(img)
            }

        });
    } else {
        img.onclick=function (event){
            if (!imageReady) return
            if (event.clientX>img.offsetLeft + img.offsetWidth-30 ||event.clientX<img.offsetLeft+30) {
                if (loadImageBy(img,event.clientX<img.offsetLeft+30 ? index-1 : index+1)){
                    if (event.clientX<img.offsetLeft+30) index--; else index++;
                    event.stopPropagation()
                }
            } else if (img.className=='img-fit') {
                let scaleX = imageW/pageW, scaleY=imageH/pageH;
                //if (scaleY>scale) scale = scaleY
                img.className='';
                wrapper.scrollTo(event.clientX*scaleX-pageW/2, event.clientY*scaleY-pageH/2)
            }
            else img.className='img-fit';
        }
    }
}
window.onload=function(){
    document.querySelectorAll('.folder-item').forEach(function(d) {
        let path = d.getAttribute('data-folder')
        const url = path ? '/?path=' + encodeURI(path) : '/'
        d.onclick=function (){
            window.location.href = url
        }
    });
    document.querySelectorAll('.gird-cell-img').forEach(function(img) {
        let src = img.getAttribute('src')
        if (src.indexOf('.thumb/')==0) src = src.substring(7)
        let pos = img.className.indexOf('img-index-')
        const index = (pos>=0 ? parseInt(img.className.substring(pos+10)) : 0)
        img.onclick=function (){
            addImageDialog(src, index == NaN ? 0 : index)
        }
    });
}