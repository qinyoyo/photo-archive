
function searchText(text) {
    if (text) window.location.href = '/search?text=' + encodeURI(text)
}
function videoOverlay(v) {
    const wrapper = document.createElement('div')
    wrapper.className = 'dialog__wrapper'
    wrapper.style.background = '#808080'
    const video = document.createElement('video')
    const w = parseInt(v.getAttribute('data-width')), h = parseInt(v.getAttribute('data-height'))
    video.style.position = 'fixed'
    video.style.left = (window.innerWidth - w)/2  + 'px'
    video.style.top = (window.innerHeight - h)/2  + 'px'
    video.style.width =  w +'px'
    video.style.height = h +'px'
    video.style.maxHeight = window.innerHeight +'px'
    video.setAttribute('src',v.getAttribute('src'))
    video.setAttribute('controls','true')
    video.currentTime = v.currentTime
    wrapper.ondblclick=function() {
        video.pause()
        wrapper.remove()
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
function videoSlideController(video) {
    const recordPos = function(v,x,y) {
        v.setAttribute('data-x',x)
        v.setAttribute('data-y',y)
    }
    const endSlide = function(event,v,x,y) {
        let x0 = v.getAttribute('data-x')
        let y0 = v.getAttribute('data-y')
        if (x0 && y0 && !v.paused) {
            x0 = parseInt(x0)
            y0 = parseInt(y0)
            let w = v.clientWidth
            if (Math.abs(x-x0)*2 < Math.abs(y-y0) && Math.abs(y-y0) > 30 ) {  // 上下滑动
                event.preventDefault()
                recordPos(v,x,y)
                if ( x0 > w/3 && x0 < 2*w/3 && x > w/3 && x < 2*w/3) {
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
                } else if ( x > 2*w/3 && x0 > 2*w/3) {
                    let s = v.volume
                    if (y<y0) {
                        s = s + 0.1
                        if (s>1) s=1
                    } else {
                        s = s - 0.1
                        if (s<0) s=0
                    }
                    v.volume = s
                }
                else if ( x < w/3 && x0 < w/3) {
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
                }
            }
            else if (Math.abs(y-y0)*2 < Math.abs(x-x0) && Math.abs(x-x0) > 30 ) {
                event.preventDefault()
                recordPos(v,x,y)
                let s = v.currentTime
                s = s + (x-x0)
                if (s<0) s=0
                else if (s>v.duration) s=v.duration
                v.currentTime = s
            }
        }
    }
    const slideStart = function(e) {
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
window.onload=function(){
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
        videoSlideController(v)
        v.onclick = function() {
            this.controls = !this.controls
        }
        const vw = v.getAttribute('data-width'), vh = v.getAttribute('data-height')
        if (vw && vh && parseInt(vw)<w && parseInt(vh)<h) {
            v.ondblclick = function() {
                videoOverlay(v)
            }
            const div = v.nextElementSibling
            if (div && div.tagName.toUpperCase()==='DIV') {
                div.style.cursor = 'pointer'
                div.onclick = function() {
                    videoOverlay(v)
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