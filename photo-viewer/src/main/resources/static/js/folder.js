
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