/* transform image
 * By qinyoyo
 */

;(function () {
    window.sessionOptions = {
        debug: false,
        htmlEditable: false,
        favoriteFilter: false,
        loopTimer:  3456,
        musicIndex: 0,
        unlocked: false,
        playBackMusic: true,
        mobile: false,
        supportOrientation: false
    }

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
            }, error ? 500 : window.sessionOptions.loopTimer)
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
            else loopTimerId=setTimeout(transformObject.loopAction, window.sessionOptions.loopTimer)
        }
        else looperState = 0
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
    const transform = function({img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation, scale}) {
        if (!translateX) translateX = 0
        if (!translateY) translateY = 0
        if (!rotateZ) rotateZ = 0
        if (!window.sessionOptions.supportOrientation && imgOrientation) {
            if (typeof imgOrientation !== 'string') imgOrientation = imgOrientation +''
            if (imgOrientation=='2' || imgOrientation=='5' || imgOrientation=='7') mirrorH = (mirrorH ? false : true)
            else if (imgOrientation=='4') mirrorV = (mirrorV ? false : true)
            if (imgOrientation=='6'|| imgOrientation=='7') rotateZ += 90
            else if (imgOrientation=='3') rotateZ += 180
            else if (imgOrientation=='5' || imgOrientation=='8') rotateZ += 270
        }
        let t = new Array()
        if (mirrorH && mirrorV) {
            rotateZ+=180
            mirrorH = mirrorV = false
        }
        if (rotateZ) t.push('rotate('+rotateZ+'deg)')
        if (translateX || translateY) {
            let frameTranslate = frameFromClient({ x: translateX, y: translateY  },
                rotateZ, {x:0, y:0})
            t.push('translate('+Math.round(frameTranslate.x) + 'px,' + Math.round(frameTranslate.y) + 'px)')
        }
        if (mirrorH) t.push('rotateY(180deg)')
        else if (mirrorV) t.push('rotateX(180deg)')
        if (scale && Math.abs(scale-1)>0.0001) t.push('scale('+scale+')')
        img.style.transform = img.style.msTransform = img.style.OTransform = img.style.MozTransform = (t.length==0 ? 'none' : t.join(' '))
    }

    window.getTranImageParams = function(thumb, index) {
        if (thumb) {
            let title = thumb.getAttribute('title')
            let src = thumb.getAttribute('data-src')
            if (!src) src = thumb.getAttribute('src')
            let click = thumb.getAttribute('data-lastModified')
            if (click) {
                src = src + '?click='+click
            }
            let orientation = thumb.getAttribute('data-orientation')
            let rating = thumb.getAttribute('data-rating')
            return { src: src, orientation:orientation, rating:rating, title:title, imgIndex: index }
        } else return {
            src: null,
            imgIndex: index
        }
    }

    const saveImageOrientation = function(img, orientations, rating, callback) {
        if (!img || (!orientations && !rating)) return
        let path = img.getAttribute('src')
        let pos = path.indexOf('?')
        if (pos>=0) path = path.substring(0,pos)
        if (path.indexOf('/.thumb/')==0) path=path.substring(7)
        let url = '/orientation?path='+encodeURI(path)+
            (orientations? ('&orientations='+orientations) : '') +
            (rating? ('&rating='+rating) : '')
        Ajax.get(url, callback)
    }
    /********* 初始化 图像变换 *************/
    const initTransformImage = function (img, index, getDataObject) {
        let removedIndexList = []
        const totalImages = typeof getDataObject === 'function' ? getDataObject(-1) : Math.max(document.querySelectorAll('*[class*="img-index-"]').length,(getDataObject?1:0))
        let loopDirection = (index === totalImages - 1 ? -1 : 1)
        const srcByIndex = function (imgIndex) {
            if (imgIndex < 0) return totalImages
            else if (imgIndex >= totalImages) return {src: null, imgIndex:imgIndex}
            else if (typeof getDataObject==='function') return getDataObject(imgIndex)
            while (removedIndexList.indexOf(imgIndex)>=0) {
                imgIndex = imgIndex + loopDirection
            }
            if (imgIndex>=0 && imgIndex < totalImages) {
                let thumb = document.querySelector('.img-index-' + imgIndex)
                if (!thumb && totalImages==1) thumb = getDataObject
                return getTranImageParams(thumb, imgIndex)
            }
            return {
                src: null,
                imgIndex:imgIndex
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
        const tranImageContainer = img.parentNode
        const tranImageWrapper = document.querySelector('.tran-img__wrapper')
        let   scaleValue = 1
        let   isReady = false
        let   translateXChanged = false, translateYChanged = false
        let   imgOrientation = orientation
        let   imgRating = rating
        let   imgInfo = title

        /********   image load, modify   **********/

        const saveOrientation = function () {
            const imgIndex = index
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
                if (orientations || (typeof imgRating === 'string' && imgRating.indexOf('+')===0)) {
                    let rating = (imgRating && imgRating.indexOf('+')===0 ? ((imgRating=='+5'?'0':'5')) : null)
                    //(imgRating && imgRating.indexOf('+')===0 ?
                    //    ('&rating='+(imgRating=='+5'?'0':'5')) :'')
                    saveImageOrientation(img,orientations,rating,function(responseText) {
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
                                    const lastModified = (pp.length>3?pp[3]:(new Date().getTime()))
                                    thumb.setAttribute('src', tp + '?click=' + lastModified)
                                    thumb.setAttribute('data-lastModified',lastModified)
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
            if (!skipSave && window.sessionOptions.unlocked) saveOrientation()
            let { src, orientation, rating, title, imgIndex } = srcByIndex(imgIndex0)
            if (src) {
                let fromLeft = (loopDirection<0)
                changeImage({src:src, fromLeft:fromLeft, orientation:orientation, rating:rating, title:title})
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
                if (window.sessionOptions.loopTimer) toast('没有更多了')
                return false
            }
        }
        const preLoadImageBy = function(imgIndex) {
            if (window.sessionOptions.loopTimer){
                let {src}=srcByIndex(imgIndex)
                if (src){
                    const image=new Image()
                    image.setAttribute('src',src)
                }
            }
        }
        const changeImage = function({src, fromLeft, orientation, rating, title }) {
            isReady = false
            showWaiting()
            const loadImg = new Image()
            loadImg.onload = function() {
                preLoadImageBy(index + loopDirection)  // 预加载文件
                hideWaiting()

                const step = pageW / 10
                let newLeft = 0, left = (fromLeft ? -(pageW + 10) : pageW + 10)

                let newDialog = createImagePlayerDialog()
                newDialog.dialogBody.innerHTML = tranImageWrapper.querySelector('.tran-img__body').innerHTML
                tranImageWrapper.parentElement.prepend(newDialog.wrapper)

                tranImageWrapper.style.left = left + 'px'

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
                        document.querySelectorAll('.tran-img__wrapper').forEach(function(w){
                            if (w !== tranImageWrapper) w.remove()
                        })
                        tranImageWrapper.style.left = '0px'
                        isReady = true
                        loopWaiting()
                    } else {
                        newDialog.wrapper.style.left = newLeft+'px'
                        tranImageWrapper.style.left = left + 'px'
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
                    transform({img, mirrorH, mirrorV,imgOrientation})
                    moveImage()
                }
                img.setAttribute('src', src)
                img.setAttribute('title', title)
            }
            loadImg.onerror = function() {
                preLoadImageBy(index + loopDirection)  // 预加载文件
                hideWaiting()
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
            if (document.getElementById('app')){
                let pathLength=document.getElementById('app').getAttribute('data-folder').length
                document.querySelector('head title').innerText=(pathLength?src.substring(pathLength+1):src)
            }
            let floatTitle = title
            if (floatTitle) {
                let pos = title.indexOf('\ufeff')
                if (pos>=0) floatTitle = title.substring(0,pos).replace(/\n/g,'<br>')
                else floatTitle = title.replace(/\n/g,'<br>')
                document.querySelector('.tran-img__title').innerHTML =
                    '<b>' + (index+1) + '/' + totalImages +'&nbsp;&nbsp;</b>' + floatTitle
            }
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
            if (!justCalc) transform({img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation})
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
            transform({img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation})
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
                transform({img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation})
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
            transform({img,translateX,translateY,rotateZ,mirrorH,mirrorV,imgOrientation})
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
        if (window.sessionOptions.mobile) {
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

        tranImageContainer.onclick=function (event) {
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
            } else if (event.code=='Escape') {
                document.querySelector('.tran-img__float-button.close').onclick(event)
            }
        }
        document.querySelector('body').onkeydown = imageKeyEvent
        changeImage({ src:src, orientation:orientation, rating:rating, title:title })

        /*************暴露的函数 **********************/
        this.resize = function() {
            pageW = window.innerWidth
            pageH = window.innerHeight
            realSizeScale = Math.max(imageW / pageW, imageH / pageH)
            if (realSizeScale<1) realSizeScale = 1
            let ms = minScale()
            if (scaleValue < ms) scaleValue = ms
            calcSize()
            transform({img,translateX,translateY,rotateZ, mirrorH, mirrorV, imgOrientation})
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
                let fn = img.getAttribute('src')
                let pos = fn.indexOf('?')
                if (pos>=0) fn = fn.substring(0,pos)
                let url = '/remove?path=' + encodeURI(fn)
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
    const backAsClose = function(event) {
        document.querySelector('.tran-img__float-button.close').onclick(event)
    }
    const removeImageDialog = function () {
        if (loopTimerId) {
            clearInterval(loopTimerId)
            loopTimerId = null
        }
        window.removeEventListener("popstate", backAsClose);
        let m = document.querySelector('.tran-img__modal')
        if (m) m.remove()
        let d = document.querySelector('.tran-img__wrapper')
        if (d) d.remove()
        document.querySelector('body').style.overflow = 'auto'
        let bodyClass = document.querySelector('body').className
        if (bodyClass && bodyClass.indexOf('transform_image_show')>=0) {
            bodyClass = bodyClass.replace('transform_image_show','').trim()
            document.querySelector('body').className = bodyClass
        } else if (!bodyClass) bodyClass = 'transform_image_show'
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

        return {wrapper:wrapper, content:content, dialogBody:dialogBody }
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


    /**
     * 打开图像浏览窗口
     * @param index 初始显示图片的索引值
     * @param getDataObject 图像资源信息的获取对象
     *                      1 一个class包含 img-index-xxx 的dom对象，可能是缩略图，使用属性
     *                        data-src/src, data-orientation, data-rating, title传递参数
     *                      2 一个回调函数，入参 为 index , 出参为 { src, orientation, rating, title, imgIndex }，
     *                        入参 index = -1时，返回循环图像总数
     * @param buttonOptions 显示哪些按钮
     * @param onclose 关闭时回调
     */
    let imageDialogOnClose = null
    const addImageDialog = function(index, getDataObject, buttonOptions, onclose) {
        removeImageDialog()
        addModel()
        if (typeof onclose === 'function') imageDialogOnClose = onclose
        else imageDialogOnClose = null
        let bodyClass = document.querySelector('body').className
        if (bodyClass && bodyClass.indexOf('transform_image_show')<0) {
            bodyClass = bodyClass + ' transform_image_show'
        } else if (!bodyClass) bodyClass = 'transform_image_show'
        document.querySelector('body').className = bodyClass

        const body = document.querySelector('body')
        body.style.overflow = 'hidden'
        let {wrapper, content, dialogBody } = createImagePlayerDialog()

        const titleDiv = document.createElement("div")
        titleDiv.className = 'tran-img__title'

        const img = document.createElement("img")
        img.draggable = false
        img.className = 'center-transform'
        img.style.zIndex = "6004"

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
            if (typeof onclick === 'function' && ( event instanceof KeyboardEvent || event instanceof PopStateEvent || document.querySelector('.tran-img__fb-wrapper.show'))) {
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

        createButton({
            className:'close white',
            title: '关闭',
            iconClass:'fa fa-power-off',
            onclick:function (){

                document.querySelector('body').onkeydown = null
                window.onresize = null
                removeImageDialog()
                if (typeof imageDialogOnClose === 'function') imageDialogOnClose()
                document.querySelector('head title').innerText = 'Photo Viewer'
                if (document.querySelector('.auto-play-loop-images')) {
                    history.back()
                }
            }
        })

        if (!document.querySelector('.auto-play-loop-images')) {
            history.pushState(null, null, location.href)
            window.addEventListener("popstate", backAsClose, false);
        }
        if (buttonOptions && buttonOptions.favorite) createButton({
            className:'favorite',
            title: '收藏',
            iconClass:'fa fa-heart-o',
            onclick:function (){
                if (transformObject) transformObject.toggleFavorite()
            }
        })

        if (buttonOptions && buttonOptions.info) createButton({
            className:'white',
            title: '图像信息',
            iconClass:'fa fa-info-circle',
            onclick:function (){
                if (transformObject) transformObject.showInfo()
            }
        })

        const bkMusic = document.querySelector('.background-music')
        if (bkMusic && buttonOptions && buttonOptions.music) {
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
        if (window.sessionOptions.loopTimer && buttonOptions && buttonOptions.loop) {
            createButton({
                className:'play',
                title: '自动播放/暂停',
                iconClass:'fa fa-pause-circle-o',
                onclick:function (){
                    if (isLooping()) pauseLoop()
                    else startLoop(true)
                }
            })
        } else stopLoop()

        if (fullScreenElement() !==0 && buttonOptions && buttonOptions.fullScreen ) {
            createButton({
                className:'white',
                title: '全屏/取消',
                iconClass:'fa fa-arrows-alt',
                onclick:function (){
                    handleFullScreen()
                }
            })
        }
        if (buttonOptions && buttonOptions.remove) {
            createButton({
                className:'remove',
                title: '删除图像',
                iconClass:'fa fa-trash-o',
                onclick:function (event){
                    if (transformObject) transformObject.removeImage(event)
                }
            })
        }
        if (buttonOptions && buttonOptions.download){
            createButton({
                className:'white',title:'下载图片',iconClass:'fa fa-arrow-circle-down',onclick:function (){
                    downloadImg(img)
                }
            })
        }

        if (!window.sessionOptions.mobile){
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
        content.appendChild(floatButtons)

        body.appendChild(wrapper)

        if (window.sessionOptions.debug) {
            window.debugElement = document.createElement("div")
            window.debugElement.className = 'tran-img__debug'
            body.appendChild(window.debugElement)
        }
        transformObject = new initTransformImage(img, index, getDataObject)
    }

    const defaultButtonOptions = function() {
        const imageEditable = document.querySelector('body.image-editable')
        return {
            favorite: imageEditable && window.sessionOptions.unlocked,
            info: imageEditable,
            music: imageEditable,
            loop: window.sessionOptions.loopTimer,
            fullScreen: true,
            download: true,
            remove: imageEditable && window.sessionOptions.unlocked
        }
    }

    /*****************  入口函数  *********************
     *  selector : 一个缩略图 img 元素                *
     *      条件 ： src 以 .thumb/ 开头               *
     *             类 img-index-xx, xx为序号          *
     *************************************************/
    const TransformImage =function(selector){
        const buttonOptions = defaultButtonOptions()
        let imgIndex = 0
        document.querySelectorAll(selector).forEach(function (img){
            let pos=img.className.indexOf('img-index-')
            if (pos<0) img.className = img.className + ' img-index-'+imgIndex
            const index=(pos>=0?parseInt(img.className.substring(pos+10)):imgIndex)
            imgIndex ++;
            const clickEvent = function (event) {
                event.stopPropagation()
                window.onresize = resizeEvent
                addImageDialog(index == NaN ? 0 : index, img, buttonOptions)
            }
            if (window.sessionOptions.unlocked) {
                new ImageCornerClick(img, {
                    clickEvent: clickEvent
                })
            } else img.onclick = clickEvent
        });
        if (imgIndex>1 && window.sessionOptions.loopTimer===3456) {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', '/options', true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    const options = JSON.parse(xhr.responseText)
                    if (options && options.loopTimer) window.sessionOptions.loopTimer = options.loopTimer
                }
            };
            xhr.send();
        }
        else if (imgIndex<=1) window.sessionOptions.loopTimer = 0
    }
    const AutoLoopPlayImage =function(starterIndex){
        const buttonOptions = defaultButtonOptions()
        starterIndex = starterIndex ? starterIndex : 0
        let firstImg = document.querySelector('.img-index-'+starterIndex)
        if (firstImg) {
            window.onresize = resizeEvent
            addImageDialog(starterIndex, firstImg, buttonOptions)
        } else if (starterIndex) {
            firstImg = document.querySelector('.img-index-0')
            if (firstImg) {
                window.onresize = resizeEvent
                addImageDialog(0, firstImg, buttonOptions)
            }
        }
    }

    const ImageCornerClick = function(el, options) {
        this.element = typeof el == 'string' ? document.querySelector(el) : el
        this.imgOrientation = options.orientation || this.element.getAttribute('data-orientation')
        this.rating0 = options.rating || this.element.getAttribute('data-rating')

        this.changeEvent = options.changeEvent
        this.clickEvent = options.clickEvent
        this.initial=function() {
            if (this.rating0) this.element.setAttribute('data-rating', this.rating0)
            else this.element.removeAttribute('data-rating')
            this.rating = this.rating0

            this.mirrorV = false
            this.mirrorH = false
            this.rotateZ = 0

            transform({
                img: this.element,
                rotateZ: this.rotateZ,
                mirrorV: this.mirrorV,
                mirrorH:this.mirrorH,
                imgOrientation: this.imgOrientation
            })
        }
        this.initial()
        this.reset=function() {
            this.initial()
            if (typeof this.changeEvent === 'function') this.changeEvent({
                rotateZ: this.rotateZ,
                mirrorV: this.mirrorV,
                mirrorH:this.mirrorH,
                rating: this.rating,
                rating0: this.rating0,
                orientations: ''
            })
        }
        this.favorite = function() {
            const f = this.element.getAttribute('data-rating')
            if (f==='5') {
                if (this.rating0 && this.rating0 !== '5'){
                    this.element.setAttribute('data-rating',this.rating0)
                    this.rating = this.rating0
                }
                else {
                    this.element.removeAttribute('data-rating')
                    this.rating = null
                }
            } else {
                this.element.setAttribute('data-rating','5')
                this.rating = '5'
            }
            if (typeof this.changeEvent === 'function') this.changeEvent({
                rotateZ: this.rotateZ,
                mirrorV: this.mirrorV,
                mirrorH:this.mirrorH,
                rating: this.rating,
                rating0: this.rating0,
                orientations: this.getOperations()
            })
        }
        this.mirrorHFunc = function() {
            this.mirrorH = !this.mirrorH
            this.doTransform()
        }
        this.mirrorVFunc = function() {
            this.mirrorV = !this.mirrorV
            this.doTransform()
        }
        this.rotate = function(v) {
            this.rotateZ += v
            this.doTransform()
        }
        this.doTransform = function() {
            if (this.mirrorH && this.mirrorV){
                this.mirrorH=this.mirrorV=false
                this.rotateZ+=180
            }
            this.rotateZ=this.rotateZ%360
            if (this.rotateZ<0) this.rotateZ+=360
            let r90 = Math.trunc(this.rotateZ / 90)
            if (this.imgOrientation === '5' || this.imgOrientation === '6' ||this.imgOrientation === '7' ||this.imgOrientation === '8') {
                r90++
            }
            r90 = r90 % 2
            let scale = 1
            if (r90) {
                let mW = this.element.parentElement.clientWidth
                let mH = this.element.parentElement.clientHeight
                let w = this.element.clientHeight, h = this.element.clientWidth
                if (w>mW || h>mH) scale = Math.min(mW / w,mH / h)
            }
            const transformOptions = {
                img: this.element,
                rotateZ: this.rotateZ,
                mirrorV: this.mirrorV,
                mirrorH:this.mirrorH,
                imgOrientation: this.imgOrientation,
                scale: scale
            }
            transform(transformOptions)
            if (typeof this.changeEvent === 'function') this.changeEvent({
                rotateZ: this.rotateZ,
                mirrorV: this.mirrorV,
                mirrorH:this.mirrorH,
                rating: this.rating,
                rating0: this.rating0,
                orientations: this.getOperations()
            })
        }
        this.save=function() {
            let newRating = null
            if ((this.rating && !this.rating0) || (!this.rating && this.rating0) || this.rating!=this.rating0) {
                newRating = this.rating
            }
            const operators = this.getOperations()
            const _this = this
            saveImageOrientation(this.element,operators,newRating, function(responseText) {
                if (responseText && responseText.indexOf('ok,')===0){
                    const pp=responseText.split(',')
                    _this.element.setAttribute('data-rating',pp[2])
                    if (operators){
                        _this.element.setAttribute('data-orientation',pp[1])
                        if (!window.sessionOptions.supportOrientation) {
                            let cls = _this.element.className
                            if (cls) {
                                if (cls.indexOf('orientation-')>=0) _this.element.className = cls.replace(/orientation-\d/,'orientation-'+pp[1])
                                else _this.element.className = cls + ' orientation-'+pp[1]
                            } else _this.element.className = 'orientation-'+pp[1]
                        }
                        let tp=_this.element.getAttribute('src')
                        if (tp) {
                            let pos = tp.indexOf('?')
                            if (pos >= 0) tp = tp.substring(0, pos)
                            const lastModified = (pp.length>3?pp[3]:(new Date().getTime()))
                            _this.element.setAttribute('src', tp + '?click=' + lastModified)
                            _this.element.setAttribute('data-lastModified',lastModified)
                            _this.element.onload = function() {
                                toast('已保存')
                            }
                        }
                    }
                    _this.changeImage(pp[2],pp[1])
                }
            })
        }
        const f = el.getAttribute('data-orientation')
        el.setAttribute('data-orientation0',f ? f : '0')
        const _this = this
        this.cornerClick = new CornerClick(el,{
            cornerSize: 32,
            clickEvent: _this.clickEvent,
            actionLT: function() {
                _this.rotate(-90)
            },
            actionRT: function() {
                _this.rotate(90)
            },
            actionLB: function() {
            },
            actionRB: function() {
                _this.favorite()
            },
            actionH: function() {
                _this.mirrorHFunc()
            },
            actionV: function() {
                _this.mirrorVFunc()
            },
            /*
            reset: function(){
                _this.reset()
            },
            resetPos: 'lb'
            */
            actionLB: function() {
                _this.save()
            }
        })
        this.getOperations = function() {
            if (this.mirrorH && this.mirrorV) {
                this.mirrorH = this.mirrorV = false
                this.rotateZ += 180
            }
            this.rotateZ = this.rotateZ % 360
            if (this.rotateZ < 0) this.rotateZ += 360
            if (this.mirrorH || this.mirrorV || this.rotateZ) {
                let orientations = ''
                if (this.mirrorH) orientations = '2'
                else if (this.mirrorV) orientations = '4'
                if (this.rotateZ>45) {
                    if (orientations) orientations = orientations + ','
                    if (this.rotateZ<135) orientations += '6'
                    else if (this.rotateZ<225) orientations += '3'
                    else if (this.rotateZ<315) orientations += '8'
                }
                return orientations
            } else return null
        }
        this.changeImage = function(rating,orientation) {
            this.rating0 = rating
            this.imgOrientation = orientation
            this.reset()
        }
        return this
    }
    ImageCornerClick.prototype = {
        destroy: function() {
            this.cornerClick.destroy()
            return null
        }
    }
    if (typeof module !== 'undefined' && typeof exports === 'object') {
        module.exports = [addImageDialog, transform, AutoLoopPlayImage, TransformImage, ImageCornerClick]
    } else {
        window.addImageDialog = addImageDialog
        window.transform = transform
        window.AutoLoopPlayImage = AutoLoopPlayImage
        window.TransformImage = TransformImage
        window.ImageCornerClick = ImageCornerClick
    }

})();
