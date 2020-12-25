/*
const MODEL = '<div class="v-modal" tabindex="0" style="z-index: 6666;"></div>'
const DIALOG = '<div class="dialog__wrapper" style="z-index: 8888;">' +
               '  <div class="dialog" style="margin-top: 0px; top: 0px;">' +
               '    <div class="dialog__body">' +
               '      <img src="SRC" />' +
               '    </div>' +
               '  </div>' +
               '</div>'
*/
function addModel() {
    let node = document.createElement("div")
    node.className = 'v-modal'
    node.tabIndex = 0
    node.style.zIndex = "6666"
    document.querySelector('body').appendChild(node)
}
function removeImageDialog() {
    let m = document.querySelector('.v-modal')
    if (m) m.remove()
    let d = document.querySelector('.dialog__wrapper')
    if (d) d.remove()
    document.querySelector('body').style.overflow = 'auto'
}
function loadImageBy(img, index) {
    if (index<0) return false
    let thumb = document.querySelector('.img-index-'+index)
    if (thumb) {
        let src = thumb.getAttribute('src')
        if (src.indexOf('.thumb/')==0) src = src.substring(7)
        img.className = 'img-fit'
        img.src = src
        return true
    } else return false
}
function addImageDialog(src, index) {
    removeImageDialog()
    addModel()
    const body = document.querySelector('body')
    body.style.overflow = 'hidden'
    const maxWidth = window.innerWidth, maxHeight = window.innerHeight
    let wrapper = document.createElement("div")
    wrapper.className = 'dialog__wrapper'
    wrapper.style.zIndex = "8888"
    let dialog = document.createElement("div")
    dialog.className = 'dialog'
    let dialogBody = document.createElement("div")
    dialogBody.className = 'dialog__body'
    dialogBody.style.width = maxWidth+'px'
    dialogBody.style.height = maxHeight+'px'

    let img = document.createElement("img")
    img.src = src
    img.className = 'img-fit'
    dialogBody.appendChild(img)
    dialog.appendChild(dialogBody)
    wrapper.appendChild(dialog)
    body.appendChild(wrapper)
    dialogBody.onclick = function(event) {
        if (event.clientX > maxWidth - 100 || event.clientX<100) {
            if (loadImageBy(img, event.clientX<100 ? index-1 : index+1)) {
                if (event.clientX<100) index --
                else index ++;
                return;
            }
        }
    }
    Transform(img)
    body.onkeydown = function(event) {
        if (event.code=='ArrowLeft' || event.code=='ArrowRight') {
            if (loadImageBy(img, event.code=='ArrowLeft' ? index-1 : index+1)) {
                if (event.code=='ArrowLeft') index --
                else index ++;
                return;
            }
        } else if (event.code=='ArrowUp' || event.code=='ArrowDown') {
            img.rotateZ += (event.code=='ArrowUp' ? 90 : -90)
            if (img.rotateZ>=360) img.rotateZ = 0;
            else if (img.rotateZ<0) img.rotateZ += 360;
        }
    }
    img.onclick = function(event) {
        if (img.className=='img-fit') img.className=''
        else img.className='img-fit'
    }
}
window.onload=function(){
    document.querySelectorAll('.folder-item').forEach(function(d) {
        let path = d.getAttribute('data-folder')
        const url = path ? '/?path=' + encodeURI(path) : '/'
        d.onclick=function (){
            window.location.href = url
        }
    });
    document.querySelectorAll('.gird-cell-img').forEach(function(img) {
        let src = img.getAttribute('src')
        if (src.indexOf('.thumb/')==0) src = src.substring(7)
        let pos = img.className.indexOf('img-index-')
        const index = (pos>=0 ? parseInt(img.className.substring(pos+10)) : 0)
        img.onclick=function (){
            addImageDialog(src, index == NaN ? 0 : index)
        }
    });
}