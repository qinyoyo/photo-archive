/* transform image
 * By qinyoyo
 */

;(function () {
    window.readOnly = false
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
    window.downloadImg = function(img){
        let url = img.src
        if (!url) url=img.getAttribute("data-src")
        if (url){
            url = decodeURI(url)
            if (url.indexOf('.thumb/')==0) url = url.substring(7)
            const a=document.createElement('a')
            const event=new MouseEvent('click')
            if (url.lastIndexOf('/')>=0) a.download = url.substring(url.lastIndexOf('/')+1)
            else a.download = url
            a.href = encodeURI(url)
            a.dispatchEvent(event)
            a.remove()
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

    window.toast  = function(msg,delay,onclose) {  // 显示提示信息，自动关闭
        if (typeof msg != 'string') return
        document.querySelectorAll('.tran-img__toast').forEach(e=>e.remove())
        let toast = document.createElement("div")
        toast.className = 'tran-img__toast'
        toast.style.zIndex = "9999"
        toast.style.display = 'hide'
        toast.style.padding = '5px'
        let span = document.createElement("span")
        span.innerHTML = msg.replace(/\n/g,'<br>')
        toast.appendChild(span)
        document.querySelector('body').appendChild(toast)
        const w = 10 + span.offsetWidth, h = 10 + span.offsetHeight
        toast.style.left = (window.innerWidth - w)/2  + 'px'
        toast.style.top = (window.innerHeight - h)/2  + 'px'
        toast.style.display = 'block'
        const remove = function() {
            toast.remove()
            if (typeof onclose === 'function') onclose.call()
        }
        let toastTimer = setTimeout(remove,delay ? delay : 1000)
        toast.onclick = function() {
            if (toastTimer) {
                clearTimeout(toastTimer)
                toastTimer = null
            } else remove()
        }
    }
    window.macPlayOSBackMusic = function() {
        if (navigator.userAgent.toLowerCase().indexOf('mac os') >= 0) {
            const playBkMusic = function () {
                const music = document.querySelector('.background-music')
                if (music) {
                    music.play()
                }
                window.removeEventListener('touchstart', playBkMusic)
            }
            window.addEventListener('touchstart', playBkMusic)
        }
    }

    const PI = 3.1415926
    let transformObject = null

    /*************   轮播  *************/
    let loopTimerId = null
    let looperState = 1
    const isLooping = function() {
        return looperState ? true : false
    }
    const loopWaiting = function (error) {
        if (transformObject && looperState)
            loopTimerId = setTimeout(function() {
                loopTimerId = null
                transformObject.loopAction()
                setPlayButtonIcon()
            }, error ? 500 : window.loopTimer)
    }

    const setPlayButtonIcon = function() {
        const icon = document.querySelector('.tran-img__float-button.play i')
        if (icon) icon.className = isLooping() ? 'fa fa-pause-circle-o' : 'fa fa-play-circle-o'
    }
    const startLoop = function(runAtOnce) {
        if (loopTimerId) {
            clearTimeout(loopTimerId)
            loopTimerId = null
        }
        if (transformObject) {
            looperState = 1
            if (runAtOnce) transformObject.loopAction()
            else loopTimerId=setTimeout(transformObject.loopAction, window.loopTimer)
        } else looperState = 0
        setPlayButtonIcon()
    }

    const stopLoop = function() {
        looperState = 0
        if (loopTimerId) {
            clearTimeout(loopTimerId)
            loopTimerId = null
        }
        setPlayButtonIcon()
    }
    const pauseLoop = function() {
        if (isLooping()) {
            stopLoop()
            return true
        } else return false
    }
    const resumeLoop = function(runAtOnce) {
        if (!isLooping())  startLoop(runAtOnce)
        setPlayButtonIcon()
    }

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
    const initTransformImage = function (img, index) {
        let removedIndexList = []
        let loopDirection = 1
        const totalImages = parseInt(document.querySelector('.photo-list').getAttribute('data-size'))
        const srcByIndex = function (imgIndex) {
            while (removedIndexList.indexOf(imgIndex)>=0) {
                imgIndex = imgIndex + loopDirection
            }
            if (imgIndex>=0 && imgIndex < totalImages) {
                let thumb = document.querySelector('.img-index-' + imgIndex)
                if (thumb) {
                    let title = thumb.getAttribute('title')
                    let src = thumb.getAttribute('data-src')
                    let click = thumb.getAttribute('src')
                    if (click && click.indexOf('?click=')>=0) {
                        src = src + click.substring(click.indexOf('?click='))
                    }
                    let orientation = thumb.getAttribute('data-orientation')
                    let rating = thumb.getAttribute('data-rating')
                    return { src, orientation, rating, title, imgIndex }
                }
            }
            return {
                src: null,
                imgIndex
            }
        }
        /**  变量  */
        let { src, orientation, rating, title } = srcByIndex(index)
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
        let pageW = window.innerWidth, pageH = window.innerHeight
        let   imageW = pageW, imageH = pageH
        let   clientW = pageW, clientH = pageH
        let   realSizeScale = 1
        const container = img.parentNode
        const wrapper = document.querySelector('.tran-img__wrapper')
        const waitingIcon = document.querySelector('.tran-img__waiting')
        let   scaleValue = 1
        let   isReady = false
        let   translateXChanged = false, translateYChanged = false
        let   imgOrientation = orientation
        let   imgRating = rating
        let   imgInfo = title

        /********   image load, modify   **********/

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
                if (orientations || (imgRating && imgRating.indexOf('+')===0)) {
                    let url = '/orientation?path='+encodeURI(path)+
                        (orientations? ('&orientations='+orientations) : '') +
                        (imgRating && imgRating.indexOf('+')===0 ?
                           ('&rating='+(imgRating=='+5'?'0':'5')) :'')
                    Ajax.get(url, function(responseText) {
                        if (responseText && responseText.indexOf('ok,')===0){
                            const pp=responseText.split(',')
                            if (index==imgIndex) {
                                imgOrientation = pp[1]
                                favorite(pp[2])
                            }
                            let thumb = document.querySelector('.img-index-'+imgIndex)
                            thumb.setAttribute('data-rating',pp[2])
                            if (orientations){
                                thumb.setAttribute('data-orientation',pp[1])
                                let tp=thumb.getAttribute('src')
                                if (tp) {
                                    let pos = tp.indexOf('?')
                                    if (pos >= 0) tp = tp.substring(0, pos)
                                    thumb.setAttribute('src', tp + '?click=' + (new Date().getTime()))
                                }
                                preLoadImageBy(imgIndex)  // 预加载文件
                            }
                        }
                    })
                }
            }
        }
        const loadImageBy = function (imgIndex0, skipSave) {
            if (loopTimerId) {
                clearTimeout(loopTimerId)
                loopTimerId = null
            }
            if (!skipSave && !window.readOnly) saveOrientation()
            let { src, orientation, rating, title, imgIndex } = srcByIndex(imgIndex0)
            if (src) {
                let fromLeft = (loopDirection<0)
                changeImage({src, fromLeft, orientation, rating, title})
                index = imgIndex
                return true
            } else {
                const autoLoop = document.querySelector('.auto-play-loop-images')
                if (autoLoop ) {
                    toast('循环开始')
                    if (imgIndex<0) imgIndex = totalImages - 1
                    else imgIndex = 0
                    return loadImageBy(imgIndex,skipSave)
                }
                toast('没有更多了')
                return false
            }
        }
        const preLoadImageBy = function(imgIndex) {
            let {src} = srcByIndex(imgIndex)
            if (src) {
                const image = new Image()
                image.setAttribute('src',src)
            }
        }
        const changeImage = function({src, fromLeft, orientation, rating, title }) {
            isReady = false
            waitingIcon.style.display = 'block'
            const loadImg = new Image()
            loadImg.onload = function() {
                preLoadImageBy(index + loopDirection)  // 预加载文件
                waitingIcon.style.display = 'none'

                const step = pageW / 10
                let newLeft = 0, left = (fromLeft ? -(pageW + 10) : pageW + 10)

                let newDialog = createImagePlayerDialog()
                newDialog.dialogBody.innerHTML = wrapper.querySelector('.tran-img__body').innerHTML
                wrapper.parentElement.prepend(newDialog.wrapper)

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
                        newDialog.wrapper.remove()
                        document.querySelectorAll('.tran-img__wrapper').forEach(w=>{
                            if (w !== wrapper) w.remove()
                        })
                        wrapper.style.left = '0px'
                        isReady = true
                        loopWaiting()
                    } else {
                        newDialog.wrapper.style.left = newLeft+'px'
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
                    imgInfo = title
                    favorite(rating)
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
                preLoadImageBy(index + loopDirection)  // 预加载文件
                waitingIcon.style.display = 'none'
                toast('加载失败')
                img.setAttribute('src', src)
                img.setAttribute('alt', src)
                imgOrientation = 1
                imgInfo = title
                favorite(rating)
                imageW = 32
                imageH = 32
                rotateZ = 0
                mirrorV = mirrorH = false
                translateX = translateY = 0
                scaleValue = 1
                realSizeScale = 1
                calcSize()
                isReady = true
                loopWaiting(true)
            }
            loadImg.setAttribute('src', src)
            let pathLength = document.getElementById('app').getAttribute('data-folder').length
            document.querySelector('head title').innerText = (pathLength ? src.substring(pathLength+1) : src)
            let floatTitle = title
            if (floatTitle) {
                let pos = title.indexOf('\ufeff')
                if (pos>=0) floatTitle = title.substring(0,pos).replace(/\n/g,'<br>')
                else floatTitle = title.replace(/\n/g,'<br>')
            }
            document.querySelector('.tran-img__title').innerHTML =
                '<b>' + (index+1) + '/' + totalImages +'&nbsp;&nbsp;</b>' + floatTitle

        }

        const favorite = function(f) {
            if (f=='toggle') {
                if (imgRating && imgRating.indexOf('+')==0)
                    imgRating = imgRating.substring(1)
                else if (imgRating) imgRating = '+' + imgRating
                else imgRating = '+0'
            } else imgRating = f
            const e = document.querySelector('.tran-img__float-button.favorite i.fa')
            if (e && imgRating && imgRating!='+5' && (imgRating==='5'|| imgRating.indexOf('+')===0))
                e.className = 'fa fa-heart'
            else if (e)
                e.className = 'fa fa-heart-o'
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




        /***********  事件处理  *************/

        const imgClick = function(event) {
            event.stopPropagation()
            event.preventDefault()
            if (isLooping()) pauseLoop()
        }

        const dblClick = function(event) {
            event.stopPropagation()
            event.preventDefault()
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
            if (isLooping()) pauseLoop()
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
                    loopDirection = (touchPos0.x > touchPos1.x ? 1 : -1)
                    loadImageBy(index + loopDirection)
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
                touchStart: touchStart,
                touchMove: touchMove,
                touchEnd: touchEnd
            });
        } else {
            img.onclick = imgClick
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
            let limit = axisLimit(clientW, clientH)
            let minX = limit.x.min + translateX, maxX = limit.x.max + translateX
            let minPage = pageFromClient({x: minX, y: 0}), maxPage = pageFromClient({x: maxX, y: 0})
            if (event.pageX > maxPage.x || event.pageX < minPage.x) {
                loopDirection = event.pageX < minPage.x ?  - 1 : 1
                loadImageBy(index + loopDirection)
            }
        }
        const imageKeyEvent = function(event) {
            if (document.querySelector('.common-dialog')) return
            if (isLooping()) pauseLoop()
            if (event.code=='ArrowLeft' || event.code=='Numpad4'){
                move({x: -10, y: 0})
            } else if (event.code=='ArrowRight' || event.code=='Numpad6'){
                move({x: 10, y: 0})
            } else if (event.code=='ArrowUp' || event.code=='Numpad8'){
                move({x: 0, y: -10})
            } else if (event.code=='ArrowDown' || event.code=='Numpad2'){
                move({x: 0, y: 10})
            } else if (event.code=='Slash' || event.code=='NumpadDivide'){
                rotate(rotateZ+(event.shiftKey ? -90 : 90))
                calcSize()
            } else if ((event.code=='Equal' || event.code=='NumpadAdd') && scaleValue<realSizeScale){
                scale(scaleValue+1)
                calcSize()
            } else if ((event.code=='Minus' || event.code=='NumpadSubtract') && scaleValue>minScale()){
                scale(scaleValue-1)
                calcSize()
            } else if (event.code=='PageUp'||event.code=='Comma' || event.code=='Numpad9'){
                loopDirection = -1
                loadImageBy(index + loopDirection)
            } else if (event.code=='PageDown'||event.code=='Period' || event.code=='Numpad3'){
                loopDirection = 1
                loadImageBy(index + loopDirection)
            } else if (event.code=='Home'|| event.code=='Numpad7') {
                translateHome()
            } else if (event.code=='KeyH') {
                mirror(false)
            } else if (event.code=='KeyV') {
                mirror(true)
            }
        }
        document.querySelector('body').onkeydown = imageKeyEvent
        changeImage({ src, orientation, rating, title })

        /*************暴露的函数 **********************/
        this.resize = function() {
            pageW = window.innerWidth
            pageH = window.innerHeight
            realSizeScale = Math.max(imageW / pageW, imageH / pageH)
            if (realSizeScale<1) realSizeScale = 1
            let ms = minScale()
            if (scaleValue < ms) scaleValue = ms
            calcSize()
            transform(img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation)
        }

        this.indexImg = function() {
            return document.querySelector('img.img-index-'+index)
        }
        this.toggleFavorite = function() {
            favorite('toggle')
        }
        this.showInfo = function() {
            const state = pauseLoop()
            toast(imgInfo.replace(/'|,|{|}/g,'') + '<div style="color: #1f63d2; text-align: center">' + (index+1) + '/' + totalImages +'</div>',2000, function () {
                if (state) resumeLoop(true)
            })
        }
        this.removeImage = function(event) {
            let needResumeLoop = isLooping()
            if (needResumeLoop) pauseLoop()
            if (confirm("确定要从磁盘删除该图像？")) {
                const imgIndex = index
                let url = '/remove?path=' + encodeURI(img.getAttribute('src'))
                Ajax.get(url, function (responseText) {
                    if ("ok" == responseText) {
                        removedIndexList.push(imgIndex)
                        document.querySelector('.img-index-' + imgIndex).remove()
                    }
                })
                if (needResumeLoop) resumeLoop(true)
                else loadImageBy(index + loopDirection)
            } else if (needResumeLoop) resumeLoop()
        }

        this.loopAction = function() {
            if (!loadImageBy(index + loopDirection)) {
                stopLoop()
            }
        }
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

    const createImagePlayerDialog = function() {
        const pageW = window.innerWidth, pageH = window.innerHeight
        let wrapper = document.createElement("div")
        wrapper.className = 'tran-img__wrapper'
        wrapper.style.zIndex = "6001"
        let content = document.createElement("div")
        content.className = 'tran-img__content'
        content.style.zIndex = "6002"
        let dialogBody = document.createElement("div")
        dialogBody.className = 'tran-img__body'
        dialogBody.style.zIndex = "6003"
        dialogBody.style.width = pageW+'px'
        dialogBody.style.height = pageH+'px'
        dialogBody.tabIndex = -1

        content.appendChild(dialogBody)
        wrapper.appendChild(content)

        return {wrapper, content, dialogBody }
    }

    const resizeEvent = function() {
        const pageW = window.innerWidth, pageH = window.innerHeight
        const dialogBody = document.querySelector('.tran-img__body')
        if (dialogBody) {
            dialogBody.style.width = pageW + 'px'
            dialogBody.style.height = pageH + 'px'
        }
        if (transformObject) {
            transformObject.resize()
        }
    }
    const addImageDialog = function(index) {
        removeImageDialog()
        addModel()
        const body = document.querySelector('body')
        body.style.overflow = 'hidden'
        let {wrapper, content, dialogBody } = createImagePlayerDialog()

        const titleDiv = document.createElement("div")
        titleDiv.className = 'tran-img__title'

        const img = document.createElement("img")
        img.draggable = false
        img.className = 'center-transform'
        img.style.zIndex = "6004"

        let waitingIcon = document.createElement("button")
        waitingIcon.className = 'tran-img__waiting'
        waitingIcon.style.zIndex = '6005'

        let waitingI = document.createElement("i")
        waitingI.className = 'fa fa-spinner fa-spin animated'
        waitingIcon.appendChild(waitingI)

        let floatButtons = document.createElement("div")
        floatButtons.style.zIndex = "6006"

        function toggleFloatButtons(show) {
            if (show) {
                floatButtons.className = floatButtons.className + ' show'
                if (titleDiv.style.display === 'none') titleDiv.style.display = 'block'
            } else {
                floatButtons.className='tran-img__fb-wrapper' + (window.innerWidth<400?' small':'')
                titleDiv.style.display = 'none'
            }
        }
        toggleFloatButtons(false)
        function floatButtonClick(event, onclick) {
            event.stopPropagation()
            event.preventDefault()
            const floatButtons = document.querySelector('.tran-img__fb-wrapper')
            if (floatButtons.className.indexOf('show')>=0 && typeof onclick === 'function') {
                onclick(event)
            } else toggleFloatButtons(true)
        }

        const createButton=function({className, iconClass, onclick, title}) {
            const button = document.createElement("button")
            button.className = 'tran-img__float-button ' + className
            if (title) button.title = title
            const icon = document.createElement("i")
            icon.className = iconClass
            button.appendChild(icon)
            floatButtons.appendChild(button)
            button.onclick = function(event) {
                floatButtonClick(event,onclick)
            }
        }

        if (rangeExif) {
            createButton({
                className: 'close',
                title: '范围开始',
                iconClass: 'fa fa-angle-left',
                onclick: function (){
                    if (transformObject) {
                        const e = transformObject.indexImg()
                        if (e) rangeExif.start = decodeURI(img.getAttribute('src'))
                        if (rangeExif.start) {
                            let needResumeLoop = isLooping()
                            if (needResumeLoop) {
                                pauseLoop()
                            }
                            window.input({
                                title: '批量设置 ' + rangeExif.note +' 起点',
                                label: rangeExif.note,
                                defaultValue: e.getAttribute('data-'+rangeExif.exif),
                                callback: function(exif) {
                                    rangeExif.value = exif
                                    if (needResumeLoop) resumeLoop(true)
                                },
                                oncancel: function() {
                                    rangeExif.start = null
                                    if (needResumeLoop) resumeLoop(true)
                                }
                            })
                        }
                    }
                }
            })
        }

        createButton({
            className:'close',
            title: '关闭',
            iconClass:'fa fa-power-off',
            onclick:function (){
                document.querySelector('body').onkeydown = null
                window.onresize = null
                removeImageDialog()
                document.querySelector('head title').innerText = 'Photo Viewer'
                if (document.querySelector('.auto-play-loop-images')) {
                    history.back()
                }
            }
        })

        if (!window.readOnly) createButton({
            className:'favorite',
            title: '收藏',
            iconClass:'fa fa-heart-o',
            onclick:function (){
                if (transformObject) transformObject.toggleFavorite()
            }
        })

        createButton({
            className:'close',
            title: '图像信息',
            iconClass:'fa fa-info-circle',
            onclick:function (){
                if (transformObject) transformObject.showInfo()
            }
        })

        const bkMusic = document.querySelector('.background-music')
        if (bkMusic) {
            createButton({
                className:'music',
                title: '切换背景音乐',
                iconClass:'fa fa-music',
                onclick:function (){
                    bkMusic.src = '/music?click='+new Date().getTime()
                }
            })
            bkMusic.onended = function() {
                bkMusic.src = '/music?click='+new Date().getTime()
            }
        }

        createButton({
            className:'play',
            title: '自动播放/暂停',
            iconClass:'fa fa-pause-circle-o',
            onclick:function (){
                if (isLooping()) pauseLoop()
                else startLoop(true)
            }
        })

        if (fullScreenElement() !==0 ) {
            createButton({
                className:'close',
                title: '全屏/取消',
                iconClass:'fa fa-arrows-alt',
                onclick:function (){
                    handleFullScreen()
                }
            })
        }
        if (!window.readOnly) {
            createButton({
                className:'remove',
                title: '删除图像',
                iconClass:'fa fa-trash-o',
                onclick:function (event){
                    if (transformObject) transformObject.removeImage(event)
                }
            })
        }

        createButton({
            className:'close',
            title: '下载图片',
            iconClass:'fa fa-arrow-circle-down',
            onclick:function (){
                downloadImg(img)
                /*
                const src = img.getAttribute('src')
                if (src) {
                    Ajax.get('/share?path='+encodeURI(src),function(reposeText) {
                        if (reposeText=='ok') toast('成功分享到指定目录')
                    })
                }
                 */
            }
        })
        if (rangeExif) {
            createButton({
                className: 'close',
                title: '范围结束',
                iconClass: 'fa fa-angle-right',
                onclick: function (){
                    if (rangeExif.start && transformObject) {
                        rangeExif.end = decodeURI(img.getAttribute('src'))
                        if (rangeExif.end) {
                            let msg = (rangeExif.value ? '批量设置 '+ rangeExif.note +'=' + rangeExif.value : '批量删除 '+ rangeExif.note) + ' ?\n' +
                                '['+ rangeExif.start + ' 至 ' + rangeExif.end + ']'
                            let needResumeLoop = isLooping()
                            if (needResumeLoop) pauseLoop()
                            if (confirm(msg)) {
                                let url = '/range?type=' + encodeURI(rangeExif.exif) +'&value=' + encodeURI(rangeExif.value)
                                     +'&start=' + encodeURI(rangeExif.start) +'&end=' + encodeURI(rangeExif.end)
                                     +'&includeSubFolder=' + rangeExif.includeSubFolder
                                     +'&path=' + encodeURI(document.getElementById('app').getAttribute('data-folder'))
                                rangeExif.value = null
                                rangeExif.start = null
                                rangeExif.end = null
                                Ajax.get(url, function (responseText) {
                                    if ("ok" == responseText) {
                                        toast('已提交后台执行')
                                    } else toast(responseText)
                                })
                            }
                            if (needResumeLoop) resumeLoop(true)
                        }
                    }
                }
            })
        }

        if (!isMobile()){
            floatButtons.onmouseenter=function (event){
                floatButtonClick(event)
            }
            floatButtons.onmouseleave=function (event){
                floatButtonClick(event,function (){
                    toggleFloatButtons(false)
                })
            }
        } else{
            floatButtons.onclick=function (event){
                floatButtonClick(event,function (){
                    toggleFloatButtons(false)
                })
            }
        }

        dialogBody.appendChild(img)

        content.appendChild(titleDiv)
        content.appendChild(waitingIcon)
        content.appendChild(floatButtons)

        body.appendChild(wrapper)

        if (window.enableDebug) {
            window.debugElement = document.createElement("div")
            window.debugElement.className = 'tran-img__debug'
            body.appendChild(window.debugElement)
        }

        transformObject = new initTransformImage(img, index)
    }
    let rangeExif = null
    /*****************  入口函数  *********************
     *  selector : 一个缩略图 img 元素                *
     *      条件 ： src 以 .thumb/ 开头               *
     *             类 img-index-xx, xx为序号          *
     *************************************************/
    window.TransformImage =function(selector){
        rangeExif = !window.readOnly && document.getElementById("app").getAttribute("data-rangeExif") ?
            {
                exif: document.getElementById("app").getAttribute("data-rangeExif"),
                note: document.getElementById("app").getAttribute("data-rangeExifNote"),
                includeSubFolder: false
            } : null
        document.querySelectorAll(selector).forEach(function (img){
            let pos=img.className.indexOf('img-index-')
            const index=(pos>=0?parseInt(img.className.substring(pos+10)):0)
            img.onclick=function (event){
                event.stopPropagation()
                window.onresize = resizeEvent
                addImageDialog(index==NaN?0:index)
            }
        });
    }
    window.AutoLoopPlayImage =function(starterIndex){
        rangeExif = document.getElementById("app").getAttribute("data-rangeExif") ?
            {
                exif: document.getElementById("app").getAttribute("data-rangeExif"),
                note: document.getElementById("app").getAttribute("data-rangeExifNote"),
                includeSubFolder: true
            } : null
        starterIndex = starterIndex ? starterIndex : 0
        let firstImg = document.querySelector('.img-index-'+starterIndex)
        if (firstImg) {
            window.onresize = resizeEvent
            addImageDialog(starterIndex)
        } else if (starterIndex) {
            firstImg = document.querySelector('.img-index-0')
            if (firstImg) {
                window.onresize = resizeEvent
                addImageDialog(0)
            }
        }
    }

})();