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

function loadImageBy(img, index) {
    if (index<0) return false
    let thumb = document.querySelector('.img-index-'+index)
    if (thumb) {
        let src = thumb.getAttribute('src')
        if (src.indexOf('.thumb/')==0) src = src.substring(7)
        img.className = 'img-fit'
        img.src = src
        return true
    } else return false
}

function addImageDialog(src, index) {
    let maxScale = 1.0
    removeImageDialog()
    addModel()
    const body = document.querySelector('body')
    body.style.overflow = 'hidden'
    const windowWidth = window.innerWidth, windowHeight = window.innerHeight
    let wrapper = document.createElement("div")
    wrapper.className = 'dialog__wrapper'
    wrapper.style.zIndex = "6001"
    let dialog = document.createElement("div")
    dialog.className = 'dialog'
    dialog.style.zIndex = "6002"
    let dialogBody = document.createElement("div")
    dialogBody.className = 'dialog__body'
    dialogBody.style.zIndex = "6003"
    dialogBody.style.width = windowWidth+'px'
    dialogBody.style.height = windowHeight+'px'
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

    dialogBody.appendChild(img)
    dialogBody.appendChild(button)

    dialog.appendChild(dialogBody)
    wrapper.appendChild(dialog)
    body.appendChild(wrapper)

    let imageWidth = windowWidth, imageHeight = windowHeight
    img.onload = function() {
        imageWidth = img.naturalWidth
        imageHeight = img.naturalHeight
        maxScale = Math.max(imageWidth/windowWidth, imageWidth/windowHeight)
        img.rotateZ = 0
        if (maxScale<1) maxScale=1
        if (isMobile()) img.scaleX = img.scaleY = 1
    }
    button.onclick = function() {
        removeImageDialog()
    }
    Transform(img)
    if (isMobile()) {
        let initScale = 1
        let swipStart = false
        //wrapper.style.overflow = 'hidden'
        new AlloyFinger(img, {
            rotate:function(event){
                img.rotateZ += event.angle;
            },
            multipointStart: function () {
                initScale = img.scaleX;
            },
            pinch: function (event) {
                let scale = img.scaleY = initScale * event.zoom;
                if (scale>maxScale) scale=maxScale
                else if (scale<1) scale = 1
                img.scaleX = img.scaleY = scale
            },
            doubleTap:function(event){
                event.stopPropagation();
                img.rotateZ = 0
                if (img.scaleX>1) img.scaleX = img.scaleY =  1;
                else img.scaleX = img.scaleY =  maxScale;
            },
            touchStart:function(event) {
                if (event.touches.length==1 && event.touches[0].clientX > windowWidth-30 || event.touches[0].clientX < 30) swipStart = true;
                else swipStart = false;
            },
            touchMove:function(event) {
                if (swipStart) {
                    event.stopPropagation();
                }
            },
            swipe:function(event){
                if (swipStart && loadImageBy(img,event.direction==="Right" ? index-1:index+1)){
                    if (event.direction==="Right") index--; else index++;
                }
                swipStart = false;
            }

        });
    } else {
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
                img.rotateZ+=(event.code=='ArrowUp'?-90:90)
                if (img.rotateZ>=360) img.rotateZ=0; else if (img.rotateZ<0) img.rotateZ+=360;
            }
        }
        img.onclick=function (event){
            if (event.clientX>img.offsetLeft + img.offsetWidth-30 ||event.clientX<img.offsetLeft+30) {
                if (loadImageBy(img,event.clientX<img.offsetLeft+30 ? index-1 : index+1)){
                    if (event.clientX<img.offsetLeft+30) index--; else index++;
                    event.stopPropagation()
                }
            } else if (img.className=='img-fit') {
                let scaleX = imageWidth/windowWidth, scaleY=imageHeight/windowHeight;
                //if (scaleY>scale) scale = scaleY
                img.className='';
                wrapper.scrollTo(event.clientX*scaleX-windowWidth/2, event.clientY*scaleY-windowHeight/2)
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