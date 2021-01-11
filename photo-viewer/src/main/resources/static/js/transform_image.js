/* transform image
 * By qinyoyo
 */

;(function () {
    window.loopTimer = 4000
    window.enableRemove = false
    window.notSupportOrientation = false
    window.enableDebug = false
    window.debug = function(options) {
        if (window.debugElement) {
            if (typeof options === 'string'){
                window.debugElement.style.left = '0px'
                window.debugElement.style.top = '0px'
                window.debugElement.innerHTML=options
            }
            else {
                if (options.position) {
                    window.debugElement.style.left = options.position.x + 'px'
                    window.debugElement.style.top = options.position.y + 'px'
                } else {
                    window.debugElement.style.left = '0px'
                    window.debugElement.style.top = '0px'
                }
                window.debugElement.innerHTML =
                    (options.append ? window.debugElement.innerHTML + ' | ' + options.text : options.text)
            }
        }
    }
    window.getBrowserType = function() {
        const ua = navigator.userAgent.toLowerCase()
        if (ua.indexOf("opera") > -1) return 'opera'
        else if (ua.indexOf("compatible") > -1 && ua.indexOf("msie") > -1 ) return 'msie'
        else if (ua.indexOf("edge") > -1) return 'edge'
        else if (ua.indexOf("firefox") > -1) return 'firefox'
        else if (ua.indexOf("safari") > -1 && ua.indexOf("chrome") == -1) return 'safari'
        else if (ua.indexOf("chrome") > -1 && ua.indexOf("safari") > -1) return 'chrome'
    }
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
        toast.className = 'tran-img__toast'
        toast.style.zIndex = "9999"
        toast.innerText = msg
        document.querySelector('body').appendChild(toast)
        setTimeout(function(){
            toast.remove()
        },delay ? delay : 500)
    }


    const PI = 3.1415926
    let loopTimerId = null

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

    /**********  通用变换 ***********/
    const transform = function(element,translateX,translateY,rotateZ, mirrorH, mirrorV, orientation) {
        if (window.notSupportOrientation && orientation) {
            if (orientation=='2' || orientation=='5' || orientation=='7') mirrorH = !mirrorH
            else if (orientation=='4') mirrorV = !mirrorH
            if (orientation=='6'|| orientation=='7') rotateZ += 90
            else if (orientation=='3') rotateZ += 180
            else if (orientation=='5' || orientation=='8') rotateZ += 270
        }
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

    /********* 初始化 图像变换 *************/
    const initTransformImage = function ({img, initialSrc, index, orientation}) {
        /**  变量  */
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
        const wrapper = document.querySelector('.tran-img__wrapper')
        const waitingIcon = container.querySelector('.tran-img__waiting')
        const removeBtn = window.enableRemove ? container.querySelector('.tran-img__remove') : null
        let   scaleValue = 1
        let   isReady = false
        let   translateXChanged = false, translateYChanged = false
        let   imgOrientation = orientation

        /********   image load, modify   **********/
        let removedIndexList = []
        const srcByIndex = function (imgIndex) {
            while (removedIndexList.indexOf(imgIndex)>=0) {
                if (imgIndex<index) imgIndex--
                else imgIndex++
            }
            if (imgIndex>=0) {
                let thumb = document.querySelector('.img-index-' + imgIndex)
                if (thumb) {
                    let src = thumb.getAttribute('src')
                    let orientation = thumb.getAttribute('data-orientation')
                    if (src.indexOf('.thumb/') == 0 || src.indexOf('/.thumb/') == 0) src = src.substring(7)
                    return { src, orientation }
                }
            }
            return false
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
                            preLoadImageBy(imgIndex)  // 预加载文件
                        }
                    })
                }
            }
        }
        const loadImageBy = function (imgIndex, skipSave) {
            if (!skipSave) saveOrientation()
            let { src, orientation } = srcByIndex(imgIndex)
            if (src) {
                changeImage({src, fromLeft :imgIndex < index, orientation})
                index = imgIndex
                return true
            } else {
                toast('没有更多了')
                return false
            }
        }
        const preLoadImageBy = function(imgIndex) {
            let {src, orientation} = srcByIndex(imgIndex)
            if (src) {
                const image = new Image()
                image.setAttribute('src',src)
            }
        }
        const changeImage = function({src, fromLeft, orientation }) {
            const loadImg = new Image()
            loadImg.onload = function() {
                preLoadImageBy(index + (fromLeft? -1 : 1))  // 预加载文件
                waitingIcon.style.display = 'none'

                const step = pageW / 10
                let newLeft = 0, left = (fromLeft ? -(pageW + 10) : pageW + 10)

                let newWrapper = createWrapper()
                newWrapper.innerHTML = wrapper.innerHTML
                wrapper.parentElement.appendChild(newWrapper)
                wrapper.style.left = left + 'px'

                const moveImage = function() {
                    if (fromLeft) {
                        newLeft += step
                        left += step
                    } else{
                        newLeft -= step
                        left -= step
                    }
                    if ((!fromLeft && left<=0) || (fromLeft && left>=0)) {
                        newWrapper.remove()
                        wrapper.style.left = '0px'
                        isReady = true
                    } else {
                        newWrapper.style.left = newLeft+'px'
                        wrapper.style.left = left + 'px'
                        setTimeout(moveImage, 50);
                    }
                }
                img.onload = function() {
                    if (!img.complete) {
                        let timer=setInterval(function (){
                            if (img.complete){
                                clearInterval(timer)
                            }
                        },10)
                    }
                    imgOrientation = orientation
                    imageW = img.naturalWidth
                    imageH = img.naturalHeight
                    rotateZ = 0
                    mirrorV = mirrorH = false
                    translateX = translateY = 0
                    scaleValue = 1
                    realSizeScale = Math.max(imageW / pageW, imageH / pageH)
                    if (realSizeScale<1) realSizeScale = 1
                    calcSize()
                    transform(img, 0, 0, 0, mirrorH, mirrorV,imgOrientation)
                    moveImage()
                }
                img.setAttribute('src', src)
            }
            loadImg.onerror = function() {
                waitingIcon.style.display = 'none'
                toast('加载失败')
            }
            loadImg.setAttribute('src', src)
            waitingIcon.style.display = 'block'
            isReady = false
        }
        const swapWHByOrientation = function () {
            return (window.notSupportOrientation && (orientation=='5' || orientation=='6'
                        || orientation=='7' || orientation=='8'))
        }
        /*******  坐标变换  *************/
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

        /*********  平移控制  *************/
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
        const calcTranslateLimit = function () {
            let limit = axisLimit(clientW,clientH)
            let pageMin = pageFromClient({x: limit.x.min, y:limit.y.min }),
                pageMax = pageFromClient({x: limit.x.max, y:limit.y.max })
            let x = {min:0, max:0}, y={min:0, max:0}
            if (pageW >= (pageMax.x - pageMin.x)) {
                x = {
                    min: (pageW - pageMax.x - pageMin.x)/2,
                    max: (pageW - pageMax.x - pageMin.x)/2
                }
            } else {
                x = {
                    min :Math.min(-pageMin.x, pageW - pageMax.x),
                    max: Math.max(-pageMin.x, pageW - pageMax.x)
                }
            }
            if (pageH >= (pageMax.y - pageMin.y)) {
                y = {
                    min: (pageH - pageMax.y - pageMin.y)/2,
                    max: (pageH - pageMax.y - pageMin.y)/2
                }
            } else {
                y = {
                    min :Math.min(-pageMin.y, pageH - pageMax.y),
                    max: Math.max(-pageMin.y, pageH - pageMax.y)
                }
            }

            translateLimit = {
                x: x,
                y: y
            }
        }

        // 平移
        const translate = function(p, justCalc) {
            if (p.x>translateLimit.x.max) p.x=translateLimit.x.max; else if (p.x<translateLimit.x.min) p.x=translateLimit.x.min;
            if (p.y>translateLimit.y.max) p.y=translateLimit.y.max; else if (p.y<translateLimit.y.min) p.y=translateLimit.y.min;
            if (p.x != translateX) translateXChanged = true
            if (p.y != translateY) translateYChanged = true
            translateX = p.x
            translateY = p.y
            if (!justCalc) transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation)
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

        /***********   缩放  *************/
        const calcSize = function() {
            clientW = Math.trunc(imageW * scaleValue / realSizeScale)
            clientH = Math.trunc(imageH * scaleValue / realSizeScale)
            img.style.width = clientW +'px'
            img.style.height = clientH + 'px'
            calcTranslateLimit()
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
        // scale real size
        const realSize = function(page) {
            scale(realSizeScale,page)
        }
        // scale to fit client, reserve rotate
        const fitClient = function() {
            scale(minScale())
            translate({ x:0, y:0 })
        }
        const isFixed = function() {
            return scaleValue <= minScale()
        }

        /**********  旋转  *********/
        // 旋转一个角度，保持refPage点的图像不变
        const rotate = function(angle,refPage){
            let image=refPage?imageFromPage(refPage):null
            rotateZ=Math.trunc(angle)%360
            translate({x:translateX,y:translateY},true)
            transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation)
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
                transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation)
            }
        }


        /***********  翻转  ***************/
        const mirror = function(vertical) {
            if (Math.round(rotateZ/90) % 2) vertical = (vertical ? false : true)
            if (vertical) mirrorV = !mirrorV
            else mirrorH = !mirrorH
            if (mirrorH && mirrorV) {
                mirrorH = mirrorV = false
                rotateZ += 180
            }
            transform(img,translateX,translateY,rotateZ,mirrorH,mirrorV,imgOrientation)
        }

        /*************   轮播  *************/
        const startLoop = function(runAtOnce) {
            if (window.loopTimer) {
                if (loopTimerId) clearInterval(loopTimerId)
                const loopView = function() {
                    if (window.loopTimer) {
                        if (!loadImageBy(index + 1)) {
                            stopLoop()
                        }
                    }
                }
                loopTimerId = setInterval(loopView, window.loopTimer)
                if (runAtOnce) loopView()
            }
        }
        const isLooping = function() {
            return window.loopTimer && loopTimerId
        }

        const stopLoop = function() {
            if (loopTimerId) clearInterval(loopTimerId)
            loopTimerId = null
            window.loopTimer = 0
        }
        const pauseLoop = function() {
            if (isLooping()) {
                clearInterval(loopTimerId)
                loopTimerId = null
            }
        }
        const resumeLoop = function(runAtOnce) {
            if (window.loopTimer && !loopTimerId)  startLoop(runAtOnce)
        }
        /***********  事件处理  *************/

        const imgClick = function(event) {
            event.stopPropagation()
            event.preventDefault()
            let y = event.changedTouches && event.changedTouches.length>0 ? event.changedTouches[0].clientY : event.offsetY
            if (removeBtn && removeBtn.style.display == 'block') {
                removeBtn.style.display = 'none'
                resumeLoop()
                return true
            } else if (y<50 && removeBtn && removeBtn.style.display == 'none') {
                pauseLoop()
                removeBtn.style.display = 'block'
                return true
            }
            return false
        }
        if (removeBtn)  removeBtn.onclick = function(event) {
            if (confirm("确定要从磁盘删除该图像？")) {
                const imgIndex = index
                let url = '/remove?path=' + encodeURI(img.getAttribute('src'))
                Ajax.get(url, function (responseText) {
                    if ("ok" == responseText) {
                        removedIndexList.push(imgIndex)
                        document.querySelector('.img-index-' + imgIndex).remove()
                    }
                })
                loadImageBy(index + 1, true)
            }
        }
        const dblClick = function(event) {
            event.stopPropagation()
            event.preventDefault()
            if (window.loopTimer) stopLoop()
            if (!isReady) return
            if (isFixed()){
                let page = {
                    x: event.changedTouches && event.changedTouches.length>0 ? event.changedTouches[0].clientX : event.clientX,
                    y: event.changedTouches && event.changedTouches.length>0 ? event.changedTouches[0].clientY : event.clientY
                }
                debug({
                    position:page,
                    text: page.x+','+page.y
                })
                realSize(page)
            } else fitClient()
        }

        let touchPos0 = {x:0, y:0}, touchPos1 = {x:0, y:0}, touchMinPos = {x:0, y:0}, touchMaxPos = {x:0, y:0}
        let preClientX = 0, preClientY = 0
        let dragStart = false
        const touchStart = function(event) {
            event.stopPropagation()
            event.preventDefault()
            if (!isReady) return
            if ((event.touches && event.touches.length == 1) || (event.button == 0 && event.buttons == 1)) {
                translateXChanged = false
                translateYChanged = false
                dragStart = true
                let pos = event.touches ? { x: event.touches[0].clientX, y: event.touches[0].clientY} : { x: event.clientX, y: event.clientY }
                touchPos0 = { x: pos.x, y: pos.y }
                touchPos1 = { x: pos.x, y: pos.y }
                touchMinPos = { x: pos.x, y: pos.y }
                touchMaxPos = { x: pos.x, y: pos.y }
                preClientX = pos.x
                preClientY = pos.y
            }
        }
        const touchMove = function(event) {
            event.stopPropagation()
            event.preventDefault()
            if (!isReady) return
            if (dragStart && ((event.touches && event.touches.length == 1) || (event.button == 0 && event.buttons == 1) )) {
                let x =  event.touches ?  event.touches[0].clientX : event.clientX,
                    y =  event.touches ?  event.touches[0].clientY : event.clientY
                if (x < touchMinPos.x) touchMinPos.x =  x
                else if (x > touchMaxPos.x) touchMaxPos.x =  x
                if (y < touchMinPos.y) touchMinPos.y =  y
                else if (y > touchMaxPos.y) touchMaxPos.y =  y
                if (event.touches) {
                    move({
                        x: event.deltaX,
                        y: event.deltaY
                    })
                } else {
                    move({
                        x: x - preClientX,
                        y: y - preClientY
                    })
                    preClientX = event.clientX
                    preClientY = event.clientY
                }
            }
        }
        const touchEnd = function (event){
            event.stopPropagation()
            event.preventDefault()
            if (!isReady) return
            if (dragStart && (!translateXChanged || !translateYChanged) &&
                ((event.changedTouches && event.changedTouches.length==1) || (event.button == 0) )) {
                touchPos1={
                    x: event.changedTouches ?  event.changedTouches[0].clientX : event.clientX,
                    y: event.changedTouches ?  event.changedTouches[0].clientY : event.clientY
                }
                let minX=Math.min(touchPos0.x,touchPos1.x),
                    maxX=Math.max(touchPos0.x,touchPos1.x),
                    minY=Math.min(touchPos0.y,touchPos1.y),
                    maxY=Math.max(touchPos0.y,touchPos1.y)
                if ( !translateXChanged && !translateYChanged
                    && ((touchMinPos.x<minX-100)||(touchMaxPos.x>maxX+100))  // 线长 > 100
                    && Math.abs(touchPos0.x-touchPos1.x)<50   // 末端对齐误差
                    && Math.abs(touchPos0.y-touchPos1.y)>50 ){  // 开口
                    // like  '<' '>'
                    mirror(false)
                } else if (!translateXChanged && !translateYChanged
                    && ((touchMinPos.y<minY-100)||(touchMaxPos.y>maxY+100))  // 线长 > 100
                    && Math.abs(touchPos0.y-touchPos1.y)<50  // 末端对齐误差
                    && Math.abs(touchPos0.x-touchPos1.x)>50 ){  // 开口
                    // like  'V' '^'
                    mirror(true)
                } else if (!translateXChanged
                    && touchMinPos.x>minX-30 &&touchMaxPos.x < maxX+30 && touchMinPos.y>minY-30 && touchMaxPos.y<maxY+30
                    && Math.abs(touchPos0.x - touchPos1.x) >= Math.abs(touchPos0.y - touchPos1.y)
                    && Math.abs(touchPos0.x - touchPos1.x) > 30) {
                    loadImageBy(index + (touchPos0.x > touchPos1.x ? 1 : -1))
                }
            }
            roundRotate()
            calcSize()
            if (scaleValue<=minScale()) translateHome()
            else translate({x: translateX, y: translateY})
            dragStart = false
        }
        if (isMobile()) {
            let initScale = 1
            new AlloyFinger(img, {
                multipointStart: function (event) {
                    event.stopPropagation()
                    event.preventDefault()
                    if (!isReady) return
                    initScale = scaleValue
                },
                rotate:function(event){
                    event.stopPropagation();
                    event.preventDefault()
                    if (isReady) {
                        const page = {
                            x: (event.touches[0].clientX + event.touches[1].clientX)/2,
                            y: (event.touches[0].clientY + event.touches[1].clientY)/2
                        }
                        rotate(rotateZ + event.angle,page)
                        debug({
                            position:page,
                            text: 'rotate: '+page.x+','+page.y
                        })
                    }

                },
                pinch: function (event) {
                    event.stopPropagation();
                    event.preventDefault()
                    if (isReady)  {
                        let sc=initScale*event.zoom;
                        const page = {
                            x:(event.touches[0].clientX+event.touches[1].clientX)/2,y:(event.touches[0].clientY+event.touches[1].clientY)/2
                        }
                        scale(sc,page)
                        debug({
                            position:page,
                            text: 'scale: ('+page.x+','+page.y + ') '+sc
                        })
                    }
                },
                tap: imgClick,
                doubleTap:dblClick,
                touchStart: function(event) {
                    pauseLoop()
                    touchStart(event)
                },
                touchMove: touchMove,
                touchEnd: function(event) {
                    resumeLoop(false)
                    touchEnd(event)
                }
            });
        } else {
            img.onclick = imgClick
            img.onmouseenter=function() {
                pauseLoop()
            }
            img.onmouseleave = function() {
                if (!removeBtn || removeBtn.style.display == 'none')  resumeLoop(true)
            }
            img.ondblclick=dblClick
            img.onmousedown=touchStart
            img.onmousemove=touchMove
            img.onmouseup=touchEnd
            img.onwheel = function (event) {
                event.stopPropagation()
                event.preventDefault()
                scale(scaleValue + (event.deltaY > 0 ? 1 : -1)*0.1,{
                    x: event.clientX, y: event.clientY
                })
            }
        }

        container.onclick=function (event) {
            if (!imgClick(event)) {
                let limit = axisLimit(clientW, clientH)
                let minX = limit.x.min + translateX, maxX = limit.x.max + translateX
                let minPage = pageFromClient({x: minX, y: 0}), maxPage = pageFromClient({x: maxX, y: 0})
                if (event.pageX > maxPage.x || event.pageX < minPage.x) {
                    loadImageBy(event.pageX < minPage.x ? index - 1 : index + 1)
                }
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
        changeImage({ src: initialSrc, orientation })
        startLoop()
    }

    /************  动态创建DOM元素  ***************/
    const addModel = function () {
        let node = document.createElement("div")
        node.className = 'tran-img__modal'
        node.tabIndex = -1
        node.style.zIndex = "6000"
        document.querySelector('body').appendChild(node)
    }
    const removeImageDialog = function () {
        if (loopTimerId) {
            clearInterval(loopTimerId)
            loopTimerId = null
        }
        let m = document.querySelector('.tran-img__modal')
        if (m) m.remove()
        let d = document.querySelector('.tran-img__wrapper')
        if (d) d.remove()
        document.querySelector('body').style.overflow = 'auto'
    }

    const createWrapper = function() {
        let wrapper = document.createElement("div")
        wrapper.className = 'tran-img__wrapper'
        wrapper.style.zIndex = "6001"
        return wrapper
    }
    const addImageDialog = function({ src, index, orientation}) {
        removeImageDialog()
        addModel()
        const pageW = window.innerWidth, pageH = window.innerHeight
        const body = document.querySelector('body')
        body.style.overflow = 'hidden'
        let wrapper = createWrapper()
        let content = document.createElement("div")
        content.className = 'tran-img__content'
        content.style.zIndex = "6002"
        let dialogBody = document.createElement("div")
        dialogBody.className = 'tran-img__body'
        dialogBody.style.zIndex = "6003"
        dialogBody.style.width = pageW+'px'
        dialogBody.style.height = pageH+'px'
        dialogBody.tabIndex = -1

        let img = document.createElement("img")
        img.draggable = false
        img.className = 'center-transform'
        img.style.zIndex = "6004"

        let closeButton = document.createElement("button")
        closeButton.className = 'tran-img__close'
        closeButton.style.left = (pageW - 36)/2 + 'px'
        closeButton.style.zIndex = "6005"

        let closeIcon = document.createElement("img")
        closeIcon.src = 'static/image/close.png'
        closeButton.appendChild(closeIcon)

        let waitingIcon = document.createElement("button")
        waitingIcon.className = 'tran-img__waiting'
        waitingIcon.style.zIndex = '6006'
        waitingIcon.style.left = (pageW - 50)/2 + 'px'

        let waitingI = document.createElement("i")
        waitingI.className = 'fa fa-spinner fa-spin animated'
        waitingIcon.appendChild(waitingI)

        dialogBody.appendChild(img)
        dialogBody.appendChild(closeButton)
        dialogBody.appendChild(waitingIcon)

        if (window.enableRemove){
            let removeBtn=document.createElement("button")
            removeBtn.className='tran-img__remove'
            removeBtn.style.zIndex='6006'
            removeBtn.style.left=(pageW-36)/2+'px'
            removeBtn.style.display='none'
            let removeI=document.createElement("i")
            removeI.className='fa fa-trash-o'
            removeBtn.appendChild(removeI)
            dialogBody.appendChild(removeBtn)
        }

        content.appendChild(dialogBody)
        wrapper.appendChild(content)
        body.appendChild(wrapper)

        if (window.enableDebug) {
            window.debugElement = document.createElement("div")
            window.debugElement.className = 'tran-img__debug'
            body.appendChild(window.debugElement)
        }

        closeButton.onclick = function() {
            document.querySelector('body').onkeydown = null
            removeImageDialog()
        }
        initTransformImage({img, initialSrc: src, index, orientation})
    }

    /*****************  入口函数  *********************
     *  selector : 一个缩略图 img 元素                *
     *      条件 ： src 以 .thumb/ 开头               *
     *             类 img-index-xx, xx为序号          *
     *************************************************/
    window.TransformImage =function(selector){
        document.querySelectorAll(selector).forEach(function (img){
            let pos=img.className.indexOf('img-index-')
            const index=(pos>=0?parseInt(img.className.substring(pos+10)):0)
            const title = img.getAttribute('title')
            const orientation = img.getAttribute('data-orientation')
            img.onclick=function (event){
                event.stopPropagation()
                if (isMobile() && title && event.offsetX<36 && event.offsetY<36) {
                    alert(title)
                } else {
                    let src = img.getAttribute('src')
                    if (src.indexOf('/.thumb/')==0 || src.indexOf('.thumb/')==0) src=src.substring(7)
                    addImageDialog({
                        src,
                        index: index==NaN?0:index,
                        orientation
                    })
                }
            }
        });
    }

})();