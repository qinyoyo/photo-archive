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
    removeImageDialog()
    addModel()
    const pageW = window.innerWidth, pageH = window.innerHeight
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
    img.className = 'img-fit'
    img.style.zIndex = "6004"

    let closeButton = document.createElement("button")
    closeButton.className = 'button-close'
    closeButton.style.zIndex = "6005"

    let closeIcon = document.createElement("img")
    closeIcon.src = 'static/image/close.png'
    closeButton.appendChild(closeIcon)

    let waitingIcon = document.createElement("button")
    waitingIcon.className = 'waiting-icon'
    waitingIcon.style.zIndex = '6006'

    let waitingI = document.createElement("i")
    waitingI.className = 'fa fa-spinner fa-spin animated'
    waitingIcon.appendChild(waitingI)

    dialogBody.appendChild(img)
    dialogBody.appendChild(closeButton)
    dialogBody.appendChild(waitingIcon)

    dialog.appendChild(dialogBody)
    wrapper.appendChild(dialog)
    body.appendChild(wrapper)

    const loadImageBy = function (imgIndex) {
        if (imgIndex<0) return false
        let thumb = document.querySelector('.img-index-'+imgIndex)
        if (thumb) {
            let src = thumb.getAttribute('src')
            if (src.indexOf('.thumb/')==0) src = src.substring(7)
            img._changeImage(src)
            index = imgIndex
            return true
        } else return false
    }

    closeButton.onclick = function() {
        removeImageDialog()
    }

    dialogBody.onclick=function (event){
        if (event.clientX>img.offsetLeft + img.offsetWidth ||event.clientX<img.offsetLeft){
            loadImageBy(event.clientX<img.offsetLeft ? index-1 : index+1)
        }
    }
    body.onkeydown=function (event){
        if (event.code=='ArrowLeft'||event.code=='ArrowRight'){
            loadImageBy(img,event.code=='ArrowLeft'?index-1:index+1)
        } else if (event.code=='Space' || event.code=='ArrowUp'||event.code=='ArrowDown'){
            img._rotate(event.code=='ArrowUp'?-90:90)
        }
    }
    if (isMobile()) {
        let initScale = 1
        let swipStart = false
        wrapper.style.overflow = 'hidden'
        new AlloyFinger(img, {
            rotate:function(event){
                event.stopPropagation();
                if (img._isReady) img._rotate(event.angle,{
                    x: (event.changedTouches[0].pageX + event.changedTouches[1].pageX)/2,
                    y: (event.changedTouches[0].pageY + event.changedTouches[1].pageY)/2
                })
            },
            multipointStart: function (event) {
                event.stopPropagation();
                initScale = img.scaleX;
            },
            pinch: function (event) {
                event.stopPropagation();
                if (img._isReady)  {
                    let scale = img.scaleY = initScale * event.zoom;
                    img._scale(scale,{
                        x: (event.changedTouches[0].pageX + event.changedTouches[1].pageX)/2,
                        y: (event.changedTouches[0].pageY + event.changedTouches[1].pageY)/2
                    })
                }
            },
            doubleTap:function(event){
                event.stopPropagation();
                if (!img._isReady) return
                if (img.scaleX>1) {
                    img._fitClient()
                }
                else {
                    img._realSize({
                        x: event.changedTouches[0].pageX,
                        y: event.changedTouches[0].pageY
                    })
                }
            },
            touchStart:function(event) {
                event.stopPropagation();
                if (!img._isReady) return
                if (event.touches.length == 1 && event.touches[0].pageX > pageW - 30 || event.touches[0].pageX < 30) swipStart = true;
                else swipStart = false;
            },
            touchMove:function(event) {
                event.stopPropagation();
                if (!img._isReady) return
                if (event.touches.length==1) {
                    img._translate({
                        x: img.translateX + event.deltaX,
                        y: img.translateY += event.deltaY
                    })
                }
            },
            tap:function(event) {
                let x = event.changedTouches[0].pageX
                if (x<30 || x > pageW - 30) {
                    loadImageBy(img,x<30 ? index-1:index+1)
                }
            },
            swipe:function(event){
                event.stopPropagation();
                if (!img._isReady) return
                if (swipStart){
                    loadImageBy(img,event.direction==="Right" ? index-1:index+1)
                }
                swipStart = false;
            },
            touchEnd: function (event) {
                event.stopPropagation();
                if (!img._isReady) return
                img._roundRotate()
                img._translate({ x: img.translateX, y: img.translateY })
            }
        });
    } else {
        img.onclick=function (event){
            if (!img._isReady) return
            if (event.clientX>img.offsetLeft + img.offsetWidth-30 ||event.clientX<img.offsetLeft+30) {
                if (loadImageBy(img,event.clientX<img.offsetLeft+30 ? index-1 : index+1)){
                    event.stopPropagation()
                }
            } else if (img._isFitClient()) {
                img._realSize({
                    x:event.pageX,
                    y:event.pageY
                })
            }
            else img._fitClient()
        }
    }
    TransformImage(img)
    img._changeImage(src)
}
window.onload=function(){
    document.querySelectorAll('.folder-item').forEach(function(d) {
        let path = d.getAttribute('data-folder')
        const url = path ? '/?path=' + encodeURI(path) : '/'
        d.onclick=function () {
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