
function searchText(text) {
    if (text) window.location.href = '/search?text=' + encodeURI(text)
}

let videoParametersAdjusted = false

function videoOverlay(v) {
    const display = document.getElementById('app').style.display
    document.getElementById('app').style.display = 'none'
    if (!window.fullScreenElement()) window.handleFullScreen(document.body)
    const wrapper = document.createElement('div')
    wrapper.className = 'dialog__wrapper'
    wrapper.style.background = '#000'
    const video = document.createElement('video')
    const setSize = function(){
        const w=parseInt(v.getAttribute('data-width')),h=parseInt(v.getAttribute('data-height'))
        video.style.position='fixed'
        if (window.innerWidth>w){
            video.style.left=(window.innerWidth-w)/2+'px'
            video.style.width=w+'px'
        }else{
            video.style.left='0px'
            video.style.width=window.innerWidth+'px'
        }
        if (window.innerHeight>h){
            video.style.top=(window.innerHeight-h)/2+'px'
            video.style.height=h+'px'
        }else{
            video.style.top='0px'
            video.style.height=window.innerHeight+'px'
        }
        video.style.maxHeight=window.innerHeight+'px'
        video.style.maxWidth=window.innerWidth+'px'
    }
    setSize()
    window.onresize = setSize
    video.setAttribute('src',v.getAttribute('src'))
    video.setAttribute('controls','true')
    video.currentTime = v.currentTime
    video.onclick = function(event){
        event.preventDefault()
        if (!videoParametersAdjusted) {
            if (video.paused) video.play()
            else video.pause()
        }
    }
    video.onmouseenter = function() {
        this.controls = true
    }
    const dblclick=function() {
        if (window.fullScreenElement()) window.handleFullScreen(document.body)
        document.getElementById('app').style.display = display
        video.pause()
        wrapper.remove()
    }
    if (window.sessionOptions.mobile) {
        new AlloyFinger(wrapper, {
            doubleTap: dblclick
        });
    } else{
        wrapper.ondblclick=dblclick
    }
    document.querySelectorAll('audio,video').forEach(function(r){
        r.pause()
    })
    wrapper.appendChild(video)
    document.querySelector('body').appendChild(wrapper)
    videoSlideController(video)
    video.play()
}

function adjustSize(img) {
    let w = img.parentNode.clientWidth
    let iw = img.naturalWidth, ih = img.naturalHeight
    if (iw<=w) img.parentNode.style.height = Math.min(w,ih) + 'px'
    else img.parentNode.style.height = Math.trunc(Math.min(w, ih*w/iw)) + 'px';
}
function getOffsetPosition(el,x,y) {
    let currentTarget = el
    let top = 0
    let left = 0
    while (currentTarget !== null) {
        top += currentTarget.offsetTop
        left += currentTarget.offsetLeft
        currentTarget = currentTarget.offsetParent
    }
    return {
        x: x-left,
        y: y-top
    }
}
let videoSlideMode = ''
function videoSlideController(video) {
    const recordPos = function(v,x,y) {
        v.setAttribute('data-x',x)
        v.setAttribute('data-y',y)
    }
    const endSlide = function(event,v,x,y) {
        let x0 = v.getAttribute('data-x')
        let y0 = v.getAttribute('data-y')
        if (x0 && y0) {
            x0 = parseInt(x0)
            y0 = parseInt(y0)
            let w = v.clientWidth
            if (!videoSlideMode) {
                if (Math.abs(x-x0)*2 < Math.abs(y-y0) && Math.abs(y-y0) > 30 ) {
                    if ( x0 > w/3 && x0 < 2*w/3 && x > w/3 && x < 2*w/3) videoSlideMode = 'vs'
                    else if ( x > 2*w/3 && x0 > 2*w/3) videoSlideMode = 'vv'
                    else if ( x < w/3 && x0 < w/3) videoSlideMode = 'vb'
                }  else if (Math.abs(y-y0)*2 < Math.abs(x-x0) && Math.abs(x-x0) > 30 ) {
                    videoSlideMode = 'h'
                }
            }
            if (!videoSlideMode) return
            if ( videoSlideMode.indexOf('v')==0 && Math.abs(y-y0) > 30 ) {  // 上下滑动
                event.preventDefault()
                recordPos(v,x,y)
                videoParametersAdjusted = true
                if (videoSlideMode==='vs' ) {
                    let s = v.playbackRate
                    const findRate = function(r,dir) {
                        const rates = [0.5,1,1.25,1.5,2,3,4,8,16]
                        for (let i=(dir>0?0:rates.length-1);;) {
                            if (i>=0 && i<rates.length) {
                                if (dir<0 && rates[i]<r) return rates[i]
                                else if (dir>0 && rates[i]>r) return rates[i]
                                if (dir>0) i++;
                                else i--;
                            } else return r
                        }
                    }
                    let r = findRate(s,y<y0 ? 1 : -1)
                    v.playbackRate = r
                    window.toast(r+'x')
                } else if ( videoSlideMode==='vv') {
                    let s = v.volume
                    if (y<y0) {
                        s = s + 0.1
                        if (s>1) s=1
                    } else {
                        s = s - 0.1
                        if (s<=0) s=0
                    }
                    v.volume = s
                    window.toast(s ? Math.trunc(s*100)+'%' : '静音')
                }
                else if ( videoSlideMode==='vb') {
                    let s = v.getAttribute('data-brightness')
                    if (s) s=parseFloat(s)
                    else s=0.5
                    if (y<y0) {
                        s = s + 0.1
                        if (s>1) s=1
                    } else {
                        s = s - 0.1
                        if (s<0) s=0
                    }
                    v.style.filter = "brightness(" + s + ")";
                    v.setAttribute('data-brightness',s)
                    window.toast(s ? Math.trunc(s*100)+'%' : '黑屏')
                }
            }
            else if (videoSlideMode==='h' && Math.abs(x-x0) > 30 ) {
                event.preventDefault()
                recordPos(v,x,y)
                videoParametersAdjusted = true
                let s = v.currentTime
                s = s + (x-x0)
                if (s<0) s=0
                else if (s>v.duration) s=v.duration
                v.currentTime = s
                const secondText = function(sec) {
                    sec=Math.trunc(sec)
                    let t = ''
                    if (sec>=3600) {
                        t=Math.trunc(sec/3600) + ':'
                        sec = sec % 3600
                    }
                    t = t + Math.trunc(sec/60) + ':' + sec % 60
                    return t
                }
                window.toast(secondText(s) + '/' + secondText(v.duration))
            }
        }
    }
    const slideStart = function(e) {
        videoParametersAdjusted = false
        videoSlideMode = ''
        if (window.sessionOptions.mobile) {
            let offset = getOffsetPosition(video,e.touches[0].pageX,e.touches[0].pageY)
            recordPos(video, offset.x, offset.y)
            video.addEventListener("touchmove", slideMove, false);
        } else {
            recordPos(video,e.offsetX,e.offsetY)
            video.addEventListener("mousemove", slideMove, false);
        }
    }
    const slideMove = function(e) {
        if (window.sessionOptions.mobile) {
            let offset = getOffsetPosition(video,e.touches[0].pageX,e.touches[0].pageY)
            endSlide(e,video,offset.x, offset.y)
        } else {
            endSlide(e,video,e.offsetX,e.offsetY)
        }
    }
    const slideEnd = function(e) {
        if (window.sessionOptions.mobile) {
            video.removeEventListener("touchmove", slideMove);
        } else {
            video.removeEventListener("mousemove", slideMove);
        }
    }
    if (window.sessionOptions.mobile) {
        video.addEventListener("touchstart", slideStart, false);
        video.addEventListener("touchend", slideEnd, false);
    } else {
        video.addEventListener("mousedown", slideStart, false);
        video.addEventListener("mouseup", slideEnd, false);
    }
}
let lastTouchEnd = 0  //更新手指弹起的时间
function disableSafariScale() {
    //阻止safari浏览器双击放大功能
    document.addEventListener("touchstart", function (event) {
        //多根手指同时按下屏幕，禁止默认行为
        if (event.touches.length > 1) {
            event.preventDefault();
        }
    });
    document.addEventListener("touchend", function (event) {
        let now = (new Date()).getTime();
        if (now - lastTouchEnd <= 300) {
            //当两次手指弹起的时间小于300毫秒，认为双击屏幕行为
            event.preventDefault();
        }else{ // 否则重新手指弹起的时间
            lastTouchEnd = now;
        }
    }, false);
    //阻止双指放大页面
    document.addEventListener("gesturestart", function (event) {
        event.preventDefault();
    });
}
window.onload=function(){
    //disableSafariScale()
    macPlayOSBackMusic()
    document.querySelectorAll('.folder-item').forEach(function(d) {
        let path = d.getAttribute('data-folder')
        const url = path ? '/?path=' + encodeURI(path) : '/'
        d.onclick=function () {
            document.querySelector('.search-input__wrapper').style.display = 'none';
            window.location.href = url
        }
    });
    document.querySelector('.search-clear-icon').onclick = function() {
        document.querySelector('.search-input').value = ''
    }
    document.querySelector('.search-input').onkeydown = function(event) {
        if (event.code=='Enter') {
            document.querySelector('.search-input__wrapper').style.display = 'none'
            searchText(this.value)
        }
    }

    document.querySelector('.fa-play.folder-head__item').onclick = function() {
        const path = this.getAttribute('data-folder')
        window.location.href = '/play?path=' + (path ? encodeURI(path) : '')
    }

    const scanFolder = document.querySelector('.scan-folder')
    if (scanFolder) scanFolder.onclick = function() {
        const path = this.getAttribute('data-folder')
        Ajax.get('/scan?path=' + (path ? encodeURI(path) : ''), function(res) {
            if (res=='ok') {
                toast('已提交后台执行')
                scanFolder.remove()
            }
        })
    }

    document.querySelector('.search-item').onclick = function() {
        const inputWrapper = document.querySelector('.search-input__wrapper')
        if (inputWrapper.style.display == 'none') inputWrapper.style.display = 'initial';
        else {
            inputWrapper.style.display = 'none';
            searchText(document.querySelector('.search-input').value)
        }
    }
    document.querySelectorAll('video').forEach(function(v) {
        const w=window.innerWidth, h=window.innerHeight
        const dblclick = function (event){
            event.preventDefault()
            v.pause()
            videoOverlay(v)
        }
        if (window.sessionOptions.mobile) {
            new AlloyFinger(v, {
                doubleTap: dblclick
            });
        } else{
            v.ondblclick=dblclick
        }
        const icon = v.nextElementSibling
        if (icon && icon.className.indexOf('video-remove')>=0) {
            icon.onclick = function() {
                let fn = v.getAttribute('src')
                let pos = fn.indexOf('?')
                if (pos>=0) fn = fn.substring(0,pos)
                if (fn && confirm('确定删除 '+fn+' ?')) {
                    let url = '/remove?path=' + encodeURI(fn)
                    Ajax.get(url, function (responseText) {
                        if ("ok" == responseText) {
                            v.parentElement.remove()
                        }
                    })
                }
            }
        }
    })
    document.querySelectorAll('.collapse .collapse-item, .collapse .collapse-item-expanded').forEach(function(v) {
        v.onclick = function() {
            let cls = v.className
            if (cls.indexOf('collapse-item-expanded') >=0) {
                v.className = cls.replace('collapse-item-expanded','collapse-item')
            } else {
                v.parentElement.querySelectorAll('.collapse-item-expanded').forEach(function(v1){
                    v1.className = v1.className.replace('collapse-item-expanded','collapse-item')
                })
                v.className = cls.replace('collapse-item','collapse-item-expanded')
            }
        }
    })
    TransformImage('.gird-cell-img')
}