
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
    video.onclick = function() {
        if (video.paused) video.play()
        else video.pause()
    }
    wrapper.ondblclick=function() {
        video.pause()
        wrapper.remove()
    }
    document.querySelectorAll('audio,video').forEach(function(r){
        r.pause()
    })
    wrapper.appendChild(video)
    document.querySelector('body').appendChild(wrapper)
    video.play()
}

function adjustSize(img) {
    let w = img.parentNode.clientWidth
    let iw = img.naturalWidth, ih = img.naturalHeight
    if (iw<=w) img.parentNode.style.height = Math.min(w,ih) + 'px'
    else img.parentNode.style.height = Math.trunc(Math.min(w, ih*w/iw)) + 'px';
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