
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

    const addNewStep = document.querySelector('.add-new-step')
    if (addNewStep) addNewStep.onclick = function() {
        const path = this.getAttribute('data-folder')
        window.input({
            title: '当前目录下新建一个游记',
            label: '游记名称：',
            dialogStyle: {
                width: '300px'
            },
            inputStyle: {
                width: '100%'
            },
            inputType: 'text',
            callback: function(v) {
                window.location.href = '/?path=' + (path ? encodeURI(path) : '') + '&newStep=' + encodeURI(v)
            }
        })
    }

    document.querySelector('.fa-play.folder-head__item').onclick = function() {
        const path = this.getAttribute('data-folder')
        window.location.href = '/play?path=' + (path ? encodeURI(path) : '')
    }

    const scanFolder = document.querySelector('.scan-folder')
    if (scanFolder) scanFolder.onclick = function() {
        const path = this.getAttribute('data-folder')
        Ajax.get('/scan?path=' + (path ? encodeURI(path) : ''), function(res) {
            if (res=='ok') toast('已提交后台执行')
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