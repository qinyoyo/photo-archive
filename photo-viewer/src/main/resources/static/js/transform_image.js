/* transform image
 * By qinyoyo
 */
;(function () {
    window.isMobile = function() {
        const ua = navigator.userAgent.toLowerCase()
        const agents = ["android", "iphone", "symbianos", "phone", "mobile"]
        if (agents.some(a => {
            if (ua.indexOf(a) >= 0) return true
        })) return true
        else return false
    }
    const initTransformImage = function (img, initialSrc, index) {
        const pageW = window.innerWidth, pageH = window.innerHeight
        let   imageW = pageW, imageH = pageH
        let   clientW = pageW, clientH = pageH
        let   realSizeScale = 1
        const container = img.parentNode
        const waitingIcon = container.querySelector('.waiting-icon')

        const loadImageBy = function (imgIndex) {
            if (imgIndex<0) return false
            let thumb = document.querySelector('.img-index-'+imgIndex)
            if (thumb) {
                let src = thumb.getAttribute('src')
                if (src.indexOf('.thumb/')==0) src = src.substring(7)
                changeImage(src)
                index = imgIndex
                return true
            } else return false
        }
        container.onclick=function (event){
            if (event.clientX>img.offsetLeft + img.offsetWidth ||event.clientX<img.offsetLeft){
                loadImageBy(event.clientX<img.offsetLeft ? index-1 : index+1)
            }
        }
        const imageKeyEvent = function(event) {
            if (event.code=='ArrowLeft'||event.code=='ArrowRight'){
                loadImageBy(event.code=='ArrowLeft'?index-1:index+1)
            } else if (event.code=='Space' || event.code=='ArrowUp'||event.code=='ArrowDown'){
                rotate(event.code=='ArrowUp'?-90:90)
                calcSize()
            }
        }
        document.querySelector('body').onkeydown = imageKeyEvent

        let isReady = false
        let clickForChange = false
        
        const dblClick = function(point) {
            clickForChange = false
            if (!isReady) return
            if (isFitClient()) {
                realSize(point)
            } else fitClient()
        }
        const imgClick = function(point) {
            if (!isReady) return
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
        // adjust rotate to 90x, swap width, height of image and client
        const rotate90 = function() {
            let b90 = Math.round(img.rotateZ / 90)
            return Math.trunc(b90 / 2) * 2 != b90
        }
        const calcSize = function() {
            if (rotate90()) {
                imageW = img.naturalHeight
                imageH = img.naturalWidth
            } else {
                imageW = img.naturalWidth
                imageH = img.naturalHeight
            }
            clientW = Math.trunc(imageW * img.scaleX / realSizeScale)
            clientH = Math.trunc(imageH * img.scaleY / realSizeScale)
        }
        const translateLimit = function () {
            let limit = {}
            let clientLeft = Math.trunc((clientW - pageW) / 2)
            let clientTop = Math.trunc((clientH - pageH) / 2)
            if (clientLeft < 0) limit.x = 0; else limit.x = clientLeft;
            if (clientTop < 0) limit.y = 0; else limit.y = clientTop;
            return limit
        }

        const pageAxis2ImageAxis = function (point) {
            let clientLeft = Math.trunc((clientW - pageW) / 2 - img.translateX),
                clientTop = Math.trunc((clientH - pageH) / 2 - img.translateY);
            return {
                x: Math.trunc((clientLeft + point.x) * realSizeScale / img.scaleX),
                y: Math.trunc((clientTop + point.y) * realSizeScale / img.scaleY)
            }
        }
        const imageAxis2PageAxis = function (point) {
            let clientLeft = (clientW - pageW) / 2 - img.translateX,
                clientTop = (clientH - pageH) / 2 - img.translateY;
            return {
                x: Math.trunc(point.x * img.scaleX / realSizeScale - clientLeft),
                y: Math.trunc(point.y * img.scaleY / realSizeScale - clientTop)
            }
        }
        const translate = function(p) {
            let limit = translateLimit(img)
            if (p.x < -limit.x) p.x = -limit.x;
            else if (p.x > limit.x) p.x = limit.x;
            if (p.y < -limit.y) p.y = -limit.y;
            else if (p.y > limit.y) p.y = limit.y;
            img.translateX = p.x
            img.translateY = p.y
        }
        const move = function(p) {
            translate({
                x: p.x + img.translateX,
                y: p.y + img.translateY
            })
        }
        const moveTo = function(imageAxis, page) {
            let clientAxis = imageAxis2PageAxis(imageAxis)
            translate({
                x: page.x - clientAxis.x,
                y: page.y - clientAxis.y
            })
        }
        const rotate = function(angle,refPoint) {
            let image = refPoint ? pageAxis2ImageAxis(refPoint) : null
            img.rotateZ += angle;
            if (refPoint) {
                moveTo(image, refPoint)
            }
        }
        const roundRotate = function () {
            let b90 = Math.round(img.rotateZ / 90)
            let angle = b90 * 90
            if (angle!=img.rotateZ) img.rotateZ = angle
        }
        const minScale = function() {
            if (imageW<=pageW && imageH<=pageH) return 1
            else if (rotate90()) {
                let w = Math.trunc(imageW / realSizeScale)
                let h = Math.trunc(imageH / realSizeScale)
                let ms = Math.min(pageW/w, pageH/h)
                return ms
            } else return 1
        }
        const scale = function(scale, refPoint) {
            let image = refPoint ? pageAxis2ImageAxis(refPoint) : null
            let ms = minScale()
            if (scale< ms) scale=ms
            else if (scale > realSizeScale) scale = realSizeScale
            img.scaleX = img.scaleY = scale
            clientW = Math.trunc(imageW * img.scaleX / realSizeScale)
            clientH = Math.trunc(imageH * img.scaleY / realSizeScale)
            if (refPoint) {
                moveTo(image, refPoint)
            }
        }
        const isFitClient = function() {
            return clientH<=pageH && clientW<=pageW
        }
        // scale to fit client, reserve rotate
        const fitClient = function() {
            scale(minScale())
            translate({ x:0, y:0 })
        }
        // scale real size
        const realSize = function(page) {
            scale(realSizeScale,page)
        }
        const changeImage = function(src) {
            waitingIcon.style.display = 'block'
            isReady = false
            img.src = src
        }

        img.onload = function () {
            waitingIcon.style.display = 'none'
            img.rotateZ = 0
            img.scaleX = img.scaleY = 1
            img.translateX = img.translateY = 0
            realSizeScale = Math.max(img.naturalWidth / pageW, img.naturalHeight / pageH)
            if (realSizeScale<1) realSizeScale = 1
            calcSize()
            isReady = true
        }
        Transform(img)
        if (isMobile()) {
            let initScale = 1
            new AlloyFinger(img, {
                rotate:function(event){
                    event.stopPropagation();
                    event.preventDefault()
                    if (isReady) rotate(event.angle,{
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
                    if (isReady)  {
                        let scale = img.scaleY = initScale * event.zoom;
                        scale(scale,{
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
                touchMove:function(event) {
                    event.stopPropagation();
                    event.preventDefault()
                    if (!isReady) return
                    if (event.touches.length==1) {
                        move({
                            x: event.deltaX,
                            y: event.deltaY
                        })
                    }
                },
                swipe:function(event){
                    event.stopPropagation();
                    event.preventDefault()
                    if (!isReady || !isFitClient()) return
                    if (event.direction==="Right" || event.direction==="Left")
                        loadImageBy(event.direction==="Right" ? index-1:index+1)
                },
                touchEnd: function (event) {
                    event.stopPropagation();
                    event.preventDefault()
                    if (!isReady) return
                    calcSize()
                    roundRotate()
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
                if (!isReady) {
                    startDrag = false
                    return
                }
                startDrag = true
                pageX = event.pageX
                pageY = event.pageY
            }
            img.onmousemove=function(event) {
                if (!startDrag) return
                move({
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
        changeImage(initialSrc)
    }

    const addModel = function () {
        let node = document.createElement("div")
        node.className = 'v-modal'
        node.tabIndex = -1
        node.style.zIndex = "6000"
        document.querySelector('body').appendChild(node)
    }
    const removeImageDialog = function () {
        let m = document.querySelector('.v-modal')
        if (m) m.remove()
        let d = document.querySelector('.dialog__wrapper')
        if (d) d.remove()
        document.querySelector('body').style.overflow = 'auto'
    }

    const addImageDialog = function(src, index) {
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

        closeButton.onclick = function() {
            document.querySelector('body').onkeydown = null
            removeImageDialog()
        }
        initTransformImage(img,src,index)
    }
    window.TransformImage =function(className){
        document.querySelectorAll('.'+className).forEach(function (img){
            let src=img.getAttribute('src')
            if (src.indexOf('.thumb/')==0) src=src.substring(7)
            let pos=img.className.indexOf('img-index-')
            const index=(pos>=0?parseInt(img.className.substring(pos+10)):0)
            const title = img.getAttribute('title')
            img.onclick=function (event){
                event.stopPropagation()
                if (title && event.offsetX<36 && event.offsetY<36) {
                    alert(title)
                } else addImageDialog(src,index==NaN?0:index)
            }
        });
    }

})();