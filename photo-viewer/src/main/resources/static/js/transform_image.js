/* transform image
 * By qinyoyo
 */
const PI = 3.1415926
// client以origin旋转angle(顺时针为正)后，由新坐标系坐标求原坐标
function clientFromFrame(frame,angle,origin) {
    angle = 2*PI/360*angle
    return {
        x: origin.x + (frame.x - origin.x) * Math.cos(angle) - (frame.y - origin.y) * Math.sin(angle),
        y: origin.y + (frame.x - origin.x) * Math.sin(angle) + (frame.y - origin.y) * Math.cos(angle)
    }
}
// client以origin旋转angle(顺时针为正)后在新坐标系的坐标
function frameFromClient(client,angle,origin) {
    angle = 2*PI/360*angle
    return {
        x: origin.x + (client.x - origin.x) * Math.cos(angle) + (client.y - origin.y) * Math.sin(angle),
        y: origin.y - (client.x - origin.x) * Math.sin(angle) + (client.y - origin.y) * Math.cos(angle)
    }
}

;(function () {
    window.isMobile = function() {
        const ua = navigator.userAgent.toLowerCase()
        const agents = ["android", "iphone", "symbianos", "phone", "mobile"]
        if (agents.some(a => {
            if (ua.indexOf(a) >= 0) return true
        })) return true
        else return false
    }
    window.toast  = function(msg,delay) {  // 显示提示信息，自动关闭
        if (typeof msg != 'string') return
        let toast = document.createElement("div")
        toast.className = 'toast-center'
        toast.style.zIndex = "9999"
        toast.innerText = msg
        document.querySelector('body').appendChild(toast)
        setTimeout(function(){
            toast.remove()
        },delay ? delay : 500)
    }
    const rotate90 = function(angle) {
        let b90 = Math.round(angle / 90)
        return Math.trunc(b90 / 2) * 2 != b90
    }
    const transform = function(element,translateX,translateY,rotateZ) {
        rotateZ = Math.trunc(rotateZ) % 360
        let t = ''
        if (rotateZ!=0) t = 'rotate('+rotateZ+'deg)'
        if (translateX!=0 || translateY!=0) {
            let frameTranslate = frameFromClient({ x: translateX, y: translateY },
                rotateZ, {x:0, y:0})
            t = t + (t?' ':'') + 'translate('+Math.round(frameTranslate.x) + 'px,'
                + Math.round(frameTranslate.y) + 'px)'
        }
        if (!t) t='none'
        element.style.transform = element.style.msTransform = element.style.OTransform = element.style.MozTransform = t
    }
    const initTransformImage = function (img, initialSrc, index) {
        let translateX = 0, translateY = 0 , rotateZ = 0
        const pageW = window.innerWidth, pageH = window.innerHeight
        let   imageW = pageW, imageH = pageH
        let   clientW = pageW, clientH = pageH
        let   realSizeScale = 1
        const container = img.parentNode
        const waitingIcon = container.querySelector('.waiting-icon')
        let   scaleValue = 1
        const loadImageBy = function (imgIndex) {
            let thumb = document.querySelector('.img-index-'+imgIndex)
            if (thumb) {
                let src = thumb.getAttribute('src')
                if (src.indexOf('.thumb/')==0 || src.indexOf('/.thumb/')==0) src = src.substring(7)
                changeImage(src)
                index = imgIndex
                return true
            } else {
                toast('没有更多了')
                return false
            }
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
        
        const dblClick = function(page) {
            clickForChange = false
            if (!isReady) return
            if (isFitClient()) {
                realSize()
            } else fitClient()
        }
        const imgClick = function(page) {
            if (isReady) return
            if (page.x>pageW - 50 ||page.x< 50) {
                clickForChange = true
                setTimeout(function(){
                    if (clickForChange) {
                        clickForChange = false
                        loadImageBy(page.x < 50 ? index - 1 : index + 1)
                    }
                },400)
            }
        }
        // adjust rotate to 90x, swap width, height of image and client

        const calcSize = function() {
            imageW = img.naturalWidth
            imageH = img.naturalHeight
            const r90 = rotate90(rotateZ)
            if (r90) {
                clientH = Math.trunc(imageW * scaleValue / realSizeScale)
                clientW = Math.trunc(imageH * scaleValue / realSizeScale)
                // 旋转不需要修改宽度高度
                img.style.width = clientH +'px'
                img.style.height = clientW + 'px'
            } else {
                clientW = Math.trunc(imageW * scaleValue / realSizeScale)
                clientH = Math.trunc(imageH * scaleValue / realSizeScale)
                img.style.width = clientW +'px'
                img.style.height = clientH + 'px'
            }
        }

        const translateLimit = function () {
            const min4 = function (v1,v2,v3,v4) {
                return Math.min(v1,Math.min(v2,Math.min(v3,v4)))
            }
            const max4 = function (v1,v2,v3,v4) {
                return Math.max(v1,Math.max(v2,Math.max(v3,v4)))
            }
            let w = img.width, h=img.height, origin = {x:w/2, y:h/2}
            let lt = clientFromFrame({x:0,y:0},rotateZ,origin)
            let rt = clientFromFrame({x:w,y:0},rotateZ,origin)
            let lb = clientFromFrame({x:0,y:h},rotateZ,origin)
            let rb = clientFromFrame({x:w,y:h},rotateZ,origin)
            let minx = min4(lt.x,rt.x,lb.x,rb.x),
                maxx = max4(lt.x,rt.x,lb.x,rb.x),
                miny = min4(lt.y,rt.y,lb.y,rb.y),
                maxy = max4(lt.y,rt.y,lb.y,rb.y)
            let x = {}, y={}
            x.min = pageW - maxx
            x.max = -minx
            y.min = pageH - maxy
            y.max = -miny
            return {
                x: x,
                y: y
            }
        }
        const pageAxis2ClientAxis = function(page) {
            let clientLeft = -translateX,
                clientTop = -translateY,
                paddingLeft = Math.max(0,(pageW - clientW)/2),
                paddingTop = Math.max(0,(pageH - clientH)/2);
            return {
                x: page.x - paddingLeft + clientLeft,
                y: page.y - paddingTop +clientTop
            }
        }
        const pageAxis2ImageAxis = function (page) {
            let client = pageAxis2ClientAxis(page);
            return {
                x: Math.trunc(client.x * realSizeScale / scaleValue),
                y: Math.trunc(client.y * realSizeScale / scaleValue)
            }
        }
        const imageAxis2PageAxis = function (point) {
            let client = {
                x: point.x * scaleValue / realSizeScale,
                y: point.y * scaleValue / realSizeScale
            };
            let clientLeft = -translateX,
                clientTop = -translateY,
                paddingLeft = Math.max(0,(pageW - clientW)/2),
                paddingTop = Math.max(0,(pageH - clientH)/2);
            return {
                x: Math.trunc( client.x - clientLeft + paddingLeft),
                y: Math.trunc(client.y  - clientTop + paddingTop)
            }
        }

        let translateXChanged = false
        const translate = function(p) {

            let limit = translateLimit()
            if (p.x > limit.x.max) p.x = limit.x.max;
            else if (p.x < limit.x.min) p.x = limit.x.min;
            if (p.y > limit.y.max) p.y = limit.y.max;
            else if (p.y < limit.y.min) p.y = limit.y.min;

            if (p.x != translateX) translateXChanged = true
            translateX = p.x
            translateY = p.y
            transform(img,translateX,translateY,rotateZ)
        }
        const move = function(p) {
            translate({
                x: p.x + translateX,
                y: p.y + translateY
            })
        }
        const moveTo = function(imageAxis, page) {
            let pageAxis = imageAxis2PageAxis(imageAxis)
            translate({
                x: page.x - pageAxis.x,
                y: page.y - pageAxis.y
            })
        }
        const rotate = function(angle,refPage) {
            let image = refPage ? pageAxis2ImageAxis(refPage) : null
            rotateZ += angle;
            transform(img,translateX,translateY,rotateZ)
            if (refPage) {
                moveTo(image, refPage)
            }
        }
        const roundRotate = function () {
            let b90 = Math.round(rotateZ / 90)
            let angle = b90 * 90
            if (Math.abs(angle-rotateZ)<15) {
                rotateZ = angle
                transform(img,translateX,translateY,rotateZ)
            }
        }
        const minScale = function() {
            if (rotate90(rotateZ)) {
                let w = Math.trunc(imageH / realSizeScale)
                let h = Math.trunc(imageW / realSizeScale)
                let ms = Math.min(pageW/w, pageH/h)
                return ms
            } else return 1
        }
        const scale = function(scale, refPage) {
            let image = refPage ? pageAxis2ImageAxis(refPage) : null
            let ms = minScale()
            if (scale< ms) scale=ms
            else if (scale > realSizeScale) scale = realSizeScale
            scaleValue = scale
            calcSize()
            if (refPage) {
                moveTo(image, refPage)
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
            rotateZ = 0
            scaleValue = 1
            translateX = translateY = 0
            realSizeScale = Math.max(img.naturalWidth / pageW, img.naturalHeight / pageH)
            if (realSizeScale<1) realSizeScale = 1
            calcSize()
            isReady = true
        }
        // Transform(img)
        if (isMobile()) {
            let initScale = 1, startTime = 0
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
                    initScale = scaleValue;
                },
                pinch: function (event) {
                    event.stopPropagation();
                    event.preventDefault()
                    if (isReady)  {
                        let sc = initScale * event.zoom;
                        scale(sc,{
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
                touchStart:function(event) {
                    translateXChanged = false
                    event.stopPropagation();
                    event.preventDefault()
                },
                swipe:function(event){
                    event.stopPropagation();
                    event.preventDefault()
                    if (!isReady || translateXChanged) return
                    if (event.direction==="Right"){
                        let left = clientW/2 - pageW/2  - translateX
                        if (left<=1) loadImageBy(index-1)
                    } else if (event.direction==="Left"){
                        let right = clientW/2 + pageW/2  - translateX
                        if (right>=clientW-1) loadImageBy(index+1)
                    }
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
                    x: event.pageX,
                    y: event.pageY
                })
            }
            img.ondblclick=function (event){
                event.stopPropagation();
                event.preventDefault()
                dblClick({
                    x:event.pageX,
                    y:event.pageY
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
            if (src.indexOf('/.thumb/')==0 || src.indexOf('.thumb/')==0) src=src.substring(7)
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