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
    img.draggable = false
    img.className = 'img-fit'
    img.style.zIndex = "6004"

    let closeButton = document.createElement("button")
    closeButton.className = 'button-close'
    closeButton.style.left = (pageW - 36)/2 + 'px'
    closeButton.style.zIndex = "6005"

    let closeIcon = document.createElement("img")
    closeIcon.src = 'static/image/close.png'
    closeButton.appendChild(closeIcon)

    let waitingIcon = document.createElement("button")
    waitingIcon.className = 'waiting-icon'
    waitingIcon.style.zIndex = '6006'
    waitingIcon.style.left = (pageW - 50)/2 + 'px'

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
            loadImageBy(event.code=='ArrowLeft'?index-1:index+1)
        } else if (event.code=='Space' || event.code=='ArrowUp'||event.code=='ArrowDown'){
            img._rotate(event.code=='ArrowUp'?-90:90)
            img._calcSize()
        }
    }
    let clickForChange = false
    const dblClick = function(point) {
        clickForChange = false
        if (!img._isReady) return
        if (img._isFitClient()) {
            img._realSize(point)
        } else img._fitClient()
    }
    const imgClick = function(point) {
        if (!img._isReady) return
        if (point.x>pageW - 50 ||point.x< 50) {
            clickForChange = true
            setTimeout(function(){
                if (clickForChange) {
                    clickForChange = false
                    loadImageBy(point.x < 50 ? index - 1 : index + 1)
                }
            },400)
        }

    }
    if (isMobile()) {
        let initScale = 1
        let swipStart = false
        new AlloyFinger(img, {
            rotate:function(event){
                event.stopPropagation();
                event.preventDefault()
                if (img._isReady) img._rotate(event.angle,{
                    x: (event.changedTouches[0].pageX + event.changedTouches[1].pageX)/2,
                    y: (event.changedTouches[0].pageY + event.changedTouches[1].pageY)/2
                })
            },
            multipointStart: function (event) {
                event.stopPropagation();
                event.preventDefault()
                initScale = img.scaleX;
            },
            pinch: function (event) {
                event.stopPropagation();
                event.preventDefault()
                if (img._isReady)  {
                    let scale = img.scaleY = initScale * event.zoom;
                    img._scale(scale,{
                        x: (event.changedTouches[0].pageX + event.changedTouches[1].pageX)/2,
                        y: (event.changedTouches[0].pageY + event.changedTouches[1].pageY)/2
                    })
                }
            },
            tap:function(event) {
                event.stopPropagation();
                event.preventDefault()
                imgClick({
                    x: event.changedTouches[0].pageX,
                    y: event.changedTouches[0].pageY
                })
            },
            doubleTap:function(event){
                event.stopPropagation();
                event.preventDefault()
                dblClick({
                        x: event.changedTouches[0].pageX,
                        y: event.changedTouches[0].pageY
                    })
            },
            touchStart:function(event) {
                if (!img._isReady) return
                if (event.touches.length == 1 && event.touches[0].pageX > pageW - 30 || event.touches[0].pageX < 30) swipStart = true;
                else swipStart = false;
            },
            touchMove:function(event) {
                event.stopPropagation();
                event.preventDefault()
                if (!img._isReady) return
                if (event.touches.length==1) {
                    img._move({
                        x: event.deltaX,
                        y: event.deltaY
                    })
                }
            },
            swipe:function(event){
                event.stopPropagation();
                event.preventDefault()
                if (!img._isReady) return
                if (swipStart){
                    loadImageBy(event.direction==="Right" ? index-1:index+1)
                }
                swipStart = false;
            },
            touchEnd: function (event) {
                event.stopPropagation();
                event.preventDefault()
                if (!img._isReady) return
                img._calcSize()
                img._roundRotate()
            }
        });
    } else {
        let pageX = 0,pageY = 0
        let startDrag = false
        img.onclick=function (event){
            event.stopPropagation();
            event.preventDefault()
            imgClick({
                x: event.clientX,
                y: event.clientY
            })
        }
        img.ondblclick=function (event){
            event.stopPropagation();
            event.preventDefault()
            dblClick({
                x:event.clientX,
                y:event.clientY
            })
        }
        img.onmousedown=function (event) {
            if (!img._isReady) {
                startDrag = false
                return
            }
            startDrag = true
            pageX = event.pageX
            pageY = event.pageY
        }
        img.onmousemove=function(event) {
            if (!startDrag) return
            img._move({
                x: event.pageX - pageX,
                y: event.pageY - pageY
            })
            pageX = event.pageX
            pageY = event.pageY
        }
        img.onmouseup=function (event) {
            startDrag = false
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
        img.onclick=function (event){
            event.stopPropagation()
            addImageDialog(src, index == NaN ? 0 : index)
        }
    });
}