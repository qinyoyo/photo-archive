
function searchText(text) {
    if (text) window.location.href = '/search?text=' + encodeURI(text)
}
function adjustSize(img) {
    let w = img.parentNode.clientWidth
    let iw = img.naturalWidth, ih = img.naturalHeight
    if (iw<=w) img.parentNode.style.height = Math.min(w,ih) + 'px'
    else img.parentNode.style.height = Math.trunc(Math.min(w, ih*w/iw)) + 'px';
}
window.onload=function(){
    if (navigator.userAgent.toLowerCase().indexOf('mac os')>=0) {
        const playBkMusic = function() {
            const music = document.querySelector('.background-music')
            if (music) {
                music.play()
            }
            window.removeEventListener('touchstart',playBkMusic)
        }
        window.addEventListener('touchstart',playBkMusic)
    }
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
    document.querySelector('.favorite-item').onclick = function() {
        const favorite = this.className.indexOf('fa-heart-o')>=0;
        const _this=this
        Ajax.get('/favorite?filter='+(favorite?'true':'false'), function(txt) {
            if (txt=='ok') {
                if (favorite) _this.className = 'fa fa-heart favorite-item'
                else _this.className = 'fa fa-heart-o favorite-item'
                window.location.reload()
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
        v.onclick = function() {
            this.controls = !this.controls
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