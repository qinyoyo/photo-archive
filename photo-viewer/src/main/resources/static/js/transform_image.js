/* transform image
 * By qinyoyo
 */
const PI = 3.1415926
/* Page :
     可见的页面部分， 固定值, 大小 pageW = window.innerWidth, pageH = window.innerHeight
   Client :
     img的大小，没有经过任何变换， 大小为 clientW = img.width, clientH = img.height
     如果client比page小，居中显示
     为避免图像模糊，图像缩放修改该值，而不是通过transform缩放
   Frame :
     实际显示的图像， Client 通过 transform 旋转平移之后的显示图像
   Image：
     显示的图像内容，大小为 imageW = img.naturalWidth， imageH = img.naturalHeight
     Image和Frame与变换有关

   translateX ： Frame沿Client X 轴移动距离，为Client坐标,向右移动为正.单位为 page 单位
   translateY ： Frame沿Client Y 轴移动距离，为Client坐标，向下移动为正.单位为 page 单位
   rotateZ ： Client绕中心点旋转角度，顺时针(CW) 为正
 */

/* client绕origin旋转angle(顺时针为正)后，由新坐标系坐标求原坐标
   frame: 变化后的坐标系中的点坐标
   angle: 旋转角度
   origin : 旋转基点，为client坐标
*/
function clientFromFrame(frame,angle,origin) {
    angle = 2*PI/360*angle
    return {
        x: origin.x + (frame.x - origin.x) * Math.cos(angle) - (frame.y - origin.y) * Math.sin(angle),
        y: origin.y + (frame.x - origin.x) * Math.sin(angle) + (frame.y - origin.y) * Math.cos(angle)
    }
}
/* client绕origin旋转angle(顺时针为正)后，由client坐标系坐标求旋转后的坐标
   client: client的点坐标
   angle: 旋转角度
   origin : 旋转基点，为client坐标
*/
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

    const transform = function(element,translateX,translateY,rotateZ, mirrorH, mirrorV) {
        let t = new Array()
        if (mirrorH && mirrorV) {
            rotateZ+=180
            mirrorH = mirrorV = false
        }
        if (rotateZ!=0) t.push('rotate('+rotateZ+'deg)')
        if (translateX!=0 || translateY!=0) {
            let frameTranslate = frameFromClient({ x: translateX, y: translateY },
                rotateZ, {x:0, y:0})
            t.push('translate('+Math.round(frameTranslate.x) + 'px,' + Math.round(frameTranslate.y) + 'px)')
        }
        if (mirrorH) t.push('rotateY(180deg)')
        else if (mirrorV) t.push('rotateX(180deg)')
        if (t.length==0) t.push('none')
        element.style.transform = element.style.msTransform = element.style.OTransform = element.style.MozTransform = t.join(' ')
    }
    const initTransformImage = function (img, initialSrc, index) {
        let translateX = 0, translateY = 0
        let rotateZ = 0, mirrorH = false, mirrorV = false
        let translateLimit = {
            x: {
                min: 0,
                max: 0
            },
            y: {
                min: 0,
                max: 0
            }
        }
        const pageW = window.innerWidth, pageH = window.innerHeight
        let   imageW = pageW, imageH = pageH
        let   clientW = pageW, clientH = pageH
        let   realSizeScale = 1
        const container = img.parentNode
        const waitingIcon = container.querySelector('.waiting-icon')
        let   scaleValue = 1
        const pageFromClient = function(client) {
            let paddingLeft = Math.max(0,(pageW - clientW)/2),
                paddingTop = Math.max(0,(pageH - clientH)/2);
            return {
                x: client.x + paddingLeft,
                y: client.y + paddingTop
            }
        }
        const clientFromPage = function(page) {
            let paddingLeft = Math.max(0,(pageW - clientW)/2),
                paddingTop = Math.max(0,(pageH - clientH)/2);
            return {
                x: (page.x - paddingLeft) ,
                y: (page.y - paddingTop)
            }
        }
        /* page 坐标 获得 图像坐标 */
        const imageFromPage = function (page) {
            let client = clientFromPage(page)
            client.x = client.x - translateX
            client.y = client.y - translateY
            let frame = frameFromClient(client, rotateZ, {x: clientW/2, y:clientH/2}) // 图像坐标随frame坐标改变
            return {
                x: Math.trunc(frame.x * realSizeScale / scaleValue),
                y: Math.trunc(frame.y * realSizeScale / scaleValue)
            }
        }

        /* 图像坐标 获得 client坐标 */
        const clientFromImage = function (point) {
            let frame = {
                x: point.x * scaleValue / realSizeScale,
                y: point.y * scaleValue / realSizeScale
            }
            let client = clientFromFrame(frame, rotateZ,{x: clientW/2, y:clientH/2});
            return {
                x: client.x + translateX,
                y: client.y + translateY
            }
        }

        const saveOrientation = function () {
            const imgIndex = index
            let path = img.getAttribute('src')
            let pos = path.indexOf('?')
            if (pos>=0) path = path.substring(0,pos)
            if (Math.trunc(rotateZ/90) * 90 == rotateZ) {
                if (mirrorH && mirrorV) {
                    mirrorH = mirrorV = false
                    rotateZ += 180
                }
                rotateZ = rotateZ % 360
                if (rotateZ < 0) rotateZ += 360
                let orientations = ''
                if (mirrorH) orientations = '2'
                else if (mirrorV) orientations = '4'
                if (rotateZ) {
                    if (orientations) orientations = orientations + ','
                    if (rotateZ==90) orientations += '6'
                    else if (rotateZ==180) orientations += '3'
                    else if (rotateZ==270) orientations += '8'
                }
                if (orientations) {
                    let url = '/orientation?path='+encodeURI(path)+'&orientations='+orientations
                    Ajax.get(url, function(responseText) {
                        if ("ok"==responseText){
                            let thumb = document.querySelector('.img-index-'+imgIndex)
                            let tp = thumb.getAttribute('src')
                            let pos = tp.indexOf('?')
                            if (pos>=0) tp = tp.substring(0,pos)
                            thumb.setAttribute('src', tp + '?click='+(new Date().getTime()))
                        }
                    })
                }
            }
        }
        const loadImageBy = function (imgIndex) {
            saveOrientation()
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

        const axisLimit = function(w, h) {
            const min4 = function (v1,v2,v3,v4) {
                return Math.min(v1,Math.min(v2,Math.min(v3,v4)))
            }
            const max4 = function (v1,v2,v3,v4) {
                return Math.max(v1,Math.max(v2,Math.max(v3,v4)))
            }
            let origin = {x:w/2, y:h/2}
            let lt = clientFromFrame({x:0,y:0},rotateZ,origin)
            let rt = clientFromFrame({x:w,y:0},rotateZ,origin)
            let lb = clientFromFrame({x:0,y:h},rotateZ,origin)
            let rb = clientFromFrame({x:w,y:h},rotateZ,origin)
            let minX = min4(lt.x,rt.x,lb.x,rb.x),
                maxX = max4(lt.x,rt.x,lb.x,rb.x),
                minY = min4(lt.y,rt.y,lb.y,rb.y),
                maxY = max4(lt.y,rt.y,lb.y,rb.y)
            return {
                x: { min: minX, max: maxX },
                y: { min: minY, max: maxY }
            }
        }
        let debug = false
        const calcTranslateLimit = function () {
            let limit = axisLimit(clientW,clientH)
            let pageMin = pageFromClient({x: limit.x.min, y:limit.y.min }),
                pageMax = pageFromClient({x: limit.x.max, y:limit.y.max })
            let x = {min:0, max:0}, y={min:0, max:0}
/*            if (debug) alert(''+limit.x.min+','+limit.x.max+' '+limit.y.min + ',' + limit.y.max
                + ' ' + pageW + ','+pageH + ' ' + clientW + ','+clientH +' ' + scaleValue +' '
                +translateX+','+translateY+' '+rotateZ)
            debug=false*/
            if (pageMin.x < 0) x.max =  -pageMin.x
            if (pageMax.x > pageW) x.min = pageW - pageMax.x
            if (limit.y.max - limit.y.min > pageH) {
                y.max = -pageMin.y
                y.min = pageH - pageMax.y
            }
            translateLimit = {
                x: x,
                y: y
            }
        }

        let isReady = false
        let clickForChange = false
        
        const dblClick = function(page) {
            clickForChange = false
            if (!isReady) return
            if (isFitClient()) {
                realSize(page)
            } else fitClient()
        }
        const calcSize = function() {
            clientW = Math.trunc(imageW * scaleValue / realSizeScale)
            clientH = Math.trunc(imageH * scaleValue / realSizeScale)
            img.style.width = clientW +'px'
            img.style.height = clientH + 'px'
            calcTranslateLimit()
        }

        let translateXChanged = false
        // 平移
        const translate = function(p, justCalc) {
            if (p.x>translateLimit.x.max) p.x=translateLimit.x.max; else if (p.x<translateLimit.x.min) p.x=translateLimit.x.min;
            if (p.y>translateLimit.y.max) p.y=translateLimit.y.max; else if (p.y<translateLimit.y.min) p.y=translateLimit.y.min;
            if (p.x != translateX) translateXChanged = true
            translateX = p.x
            translateY = p.y
            if (!justCalc) transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV)
        }
        // 在当前位移的基础上，移动一个位移
        const move = function(p) {
            translate({
                x: p.x + translateX,
                y: p.y + translateY
            })
        }
        // 将图像上的某点平移到page某点
        const translateTo = function(image, page) {
            translateX = translateY = 0
            let client = clientFromImage(image)
            let page1 = pageFromClient(client)
            translate({
                x: page.x - page1.x,
                y: page.y - page1.y
            })
        }
        const translateHome = function() {
            translateTo({x: imageW/2, y: imageH/2}, { x: pageW/2, y: pageH/2})
        }
        // 旋转一个角度，保持refPage点的图像不变
        const rotate = function(angle,refPage){
            let image=refPage?imageFromPage(refPage):null
            rotateZ=Math.trunc(angle)%360
            translate({x:translateX,y:translateY},true)
            transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV)
            if (refPage){
                translateTo(image,refPage)
            } else translateHome()
        }
        // 旋转角度如果与90度倍数差小于15度，按90度取整旋转
        const roundRotate = function () {
            let b90 = Math.round(rotateZ / 90)
            let angle = b90 * 90
            if (Math.abs(angle-rotateZ)<15 ) {
                rotateZ = angle
                transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV)
            }
        }
        const minScale = function() {
            if (rotateZ) {
                let limit = axisLimit(imageW,imageH)
                let w = limit.x.max - limit.x.min
                let h = limit.y.max - limit.y.min
                let ms = Math.min(pageW/w * realSizeScale, pageH/h * realSizeScale)
                return ms >= realSizeScale ? realSizeScale : ms
            } else return 1
        }
        // 缩放，保持refPage点的图像不变
        const scale = function(scale, refPage) {
            let image = refPage ? imageFromPage(refPage) : null
            let ms = minScale()
            if (scale< ms) scale=ms
            else if (scale > realSizeScale) scale = realSizeScale
            scaleValue = scale
            calcSize()
            if (refPage) {
                translateTo(image, refPage)
            } else translateHome()
        }
        const isFitClient = function() {
            return translateLimit.x.min == 0 && translateLimit.x.max == 0 &&
                translateLimit.y.min == 0 && translateLimit.y.max == 0
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
        const mirror = function(vertical) {
            if (Math.round(rotateZ/90) % 2) vertical = (vertical ? false : true)
            if (vertical) mirrorV = !mirrorV
            else mirrorH = !mirrorH
            if (mirrorH && mirrorV) {
                mirrorH = mirrorV = false
                rotateZ += 180
            }
            transform(img,translateX,translateY,rotateZ,mirrorH,mirrorV)
        }
        const changeImage = function(src) {
            const loadImg = new Image()
            loadImg.onload = function() {
                waitingIcon.style.display = 'none'
                img.setAttribute('src', '')
                imageW = loadImg.naturalWidth
                imageH = loadImg.naturalHeight
                rotateZ = 0
                mirrorV = mirrorH = false
                scaleValue = 1
                translateX = translateY = 0
                realSizeScale = Math.max(imageW / pageW, imageH / pageH)
                if (realSizeScale<1) realSizeScale = 1
                calcSize()
                img.setAttribute('src', src)
                transform(img, 0, 0, 0, mirrorH, mirrorV)
                isReady = true
            }
            loadImg.onerror = function() {
                waitingIcon.style.display = 'none'
                toast('加载失败')
            }
            loadImg.setAttribute('src', src)
            waitingIcon.style.display = 'block'
            isReady = false
        }

        if (isMobile()) {
            let initScale = 1, allowSingleSwipe = false
            let touchPos0 = {x:0, y:0}, touchPos1 = {x:0, y:0}, touchMinXPos = {x:0, y:0}, touchMaxYPos = {x:0, y:0}
            new AlloyFinger(img, {
                multipointStart: function (event) {
                    event.stopPropagation()
                    event.preventDefault()
                    if (!isReady) return
                    allowSingleSwipe = false
                    initScale = scaleValue
                },
                rotate:function(event){
                    event.stopPropagation();
                    event.preventDefault()
                    allowSingleSwipe = false
                    if (isReady) rotate(rotateZ + event.angle,{
                        x: (event.touches[0].pageX + event.touches[1].pageX)/2,
                        y: (event.touches[0].pageY + event.touches[1].pageY)/2
                    })
                },
                pinch: function (event) {
                    event.stopPropagation();
                    event.preventDefault()
                    allowSingleSwipe = false
                    if (isReady)  {
                        let sc=initScale*event.zoom;
                        scale(sc,{
                            x:(event.touches[0].pageX+event.touches[1].pageX)/2,y:(event.touches[0].pageY+event.touches[1].pageY)/2
                        })
                    }
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
                        if (event.touches[0].pageX < touchMinXPos.x) touchMinXPos =  { x: event.touches[0].pageX, y: event.touches[0].pageY}
                        if (event.touches[0].pageY > touchMaxYPos.y) touchMaxYPos =  { x: event.touches[0].pageX, y: event.touches[0].pageY}
                        move({
                            x: event.deltaX,
                            y: event.deltaY
                        })
                    }
                },
                touchStart:function(event) {
                    if (event.touches.length == 1) {
                        translateXChanged = false
                        allowSingleSwipe = true
                        touchPos0 = touchPos1 = touchMinXPos = touchMaxYPos = { x: event.touches[0].pageX, y: event.touches[0].pageY}
                    }
                    event.stopPropagation();
                    event.preventDefault()
                },
                touchEnd: function (event) {
                    event.stopPropagation();
                    event.preventDefault()
                    if (!isReady) return
                    if (allowSingleSwipe && event.changedTouches.length == 1) {
                        touchPos1 = { x: event.changedTouches[0].pageX, y: event.changedTouches[0].pageY}
                        if (touchPos0.x > touchMinXPos.x + 100 && touchPos1.x >touchMinXPos.x + 100 &&
                            Math.abs(touchPos0.x - touchPos1.x) < 50 &&
                            Math.abs((touchPos0.y+touchPos1.y)/2 - touchMinXPos.y ) <50 ) {    // like  '<'
                            allowSingleSwipe = false
                            mirror(false)
                        } else if (touchPos0.y + 100 < touchMaxYPos.y && touchPos1.y + 100 <touchMaxYPos.y &&
                            Math.abs(touchPos0.y - touchPos1.y) < 50 &&
                            Math.abs((touchPos0.x+touchPos1.x)/2 - touchMaxYPos.x ) <50 ) {    // like  'V'
                            allowSingleSwipe = false
                            mirror(true)
                        }
                    }
                    roundRotate()
                    calcSize()
                    if (scaleValue<=minScale()) translateHome()
                    else translate({x: translateX, y: translateY})

                    debug=true
                },
                swipe:function(event){
                    event.stopPropagation();
                    event.preventDefault()
                    if (!isReady || !allowSingleSwipe) return
                    allowSingleSwipe = false
                    if (!translateXChanged){
                        if (event.direction==="Right"){
                            let left = clientW/2 - pageW/2  - translateX
                            if (left<=1) loadImageBy(index-1)
                        } else if (event.direction==="Left"){
                            let right = clientW/2 + pageW/2  - translateX
                            if (right>=clientW-1) loadImageBy(index+1)
                        }
                    }
                }
            });
        } else {
            let pageX = 0,pageY = 0
            let startDrag = false
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
                pageX = event.clientX
                pageY = event.clientY
            }
            img.onmousemove=function(event) {
                if (!startDrag) return
                move({
                    x: event.clientX - pageX,
                    y: event.clientY - pageY
                })
                pageX = event.clientX
                pageY = event.clientY
            }
            img.onmouseup=function (event) {
                startDrag = false
            }
        }

        container.onclick=function (event) {
            let limit = axisLimit(clientW,clientH)
            let minX = limit.x.min + translateX ,
                maxX = limit.x.max + translateX
            let minPage = pageFromClient({x: minX, y:0}),
                maxPage = pageFromClient({x: maxX, y:0})
            if (event.pageX>maxPage.x ||event.pageX<minPage.x){
                loadImageBy(event.pageX<minPage.x ? index-1 : index+1)
            }
        }

        const imageKeyEvent = function(event) {
            if (event.code=='ArrowLeft' || event.code=='Numpad4'){
                move({x: -10, y: 0})
            } else if (event.code=='ArrowRight' || event.code=='Numpad6'){
                move({x: 10, y: 0})
            } else if (event.code=='ArrowUp' || event.code=='Numpad8'){
                move({x: 0, y: -10})
            } else if (event.code=='ArrowDown' || event.code=='Numpad2'){
                move({x: 0, y: 10})
            } else if (event.code=='Space'){
                rotate(rotateZ+(event.shiftKey ? -90 : 90))
                calcSize()
            } else if ((event.code=='Equal' || event.code=='NumpadAdd') && scaleValue<realSizeScale){
                scale(scaleValue+1)
                calcSize()
            } else if ((event.code=='Minus' || event.code=='NumpadSubtract') && scaleValue>minScale()){
                scale(scaleValue-1)
                calcSize()
            } else if (event.code=='PageUp'||event.code=='Comma' || event.code=='Numpad9'){
                loadImageBy(index-1)
            } else if (event.code=='PageDown'||event.code=='Period' || event.code=='Numpad3'){
                loadImageBy(index+1)
            } else if (event.code=='Home'|| event.code=='Numpad7') {
                translateHome()
            } else if (event.code=='KeyH') {
                mirror(false)
            } else if (event.code=='KeyV') {
                mirror(true)
            }
        }
        document.querySelector('body').onkeydown = imageKeyEvent

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
        img.className = 'center-transform'
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
    window.TransformImage =function(selector){
        document.querySelectorAll(selector).forEach(function (img){
            let pos=img.className.indexOf('img-index-')
            const index=(pos>=0?parseInt(img.className.substring(pos+10)):0)
            const title = img.getAttribute('title')
            img.onclick=function (event){
                event.stopPropagation()
                if (title && event.offsetX<36 && event.offsetY<36) {
                    alert(title)
                } else {
                    let src = img.getAttribute('src')
                    if (src.indexOf('/.thumb/')==0 || src.indexOf('.thumb/')==0) src=src.substring(7)
                    addImageDialog(src,index==NaN?0:index)
                }
            }
        });
    }

})();