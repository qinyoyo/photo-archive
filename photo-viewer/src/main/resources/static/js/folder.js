function addModel() {
    let node = document.createElement("div")
    node.className = 'v-modal'
    node.tabIndex = -1
    node.style.zIndex = "6000"
    document.querySelector('body').appendChild(node)
}
function removeImageDialog() {
    let m = document.querySelector('.v-modal')
    if (m) m.remove()
    let d = document.querySelector('.dialog__wrapper')
    if (d) d.remove()
    document.querySelector('body').style.overflow = 'auto'
}

function addImageDialog(src, index) {
    removeImageDialog()
    addModel()
    const pageW = window.innerWidth, pageH = window.innerHeight
    const body = document.querySelector('body')
    body.style.overflow = 'hidden'
    let wrapper = document.createElement("div")
    wrapper.className = 'dialog__wrapper'
    wrapper.style.zIndex = "6001"
    let dialog = document.createElement("div")
    dialog.className = 'dialog'
    dialog.style.zIndex = "6002"
    let dialogBody = document.createElement("div")
    dialogBody.className = 'dialog__body'
    dialogBody.style.zIndex = "6003"
    dialogBody.style.width = pageW+'px'
    dialogBody.style.height = pageH+'px'
    dialogBody.tabIndex = -1

    let img = document.createElement("img")
    img.draggable = false
    img.className = 'img-fit'
    img.style.zIndex = "6004"

    let closeButton = document.createElement("button")
    closeButton.className = 'button-close'
    closeButton.style.left = (pageW - 36)/2 + 'px'
    closeButton.style.zIndex = "6005"

    let closeIcon = document.createElement("img")
    closeIcon.src = 'static/image/close.png'
    closeButton.appendChild(closeIcon)

    let waitingIcon = document.createElement("button")
    waitingIcon.className = 'waiting-icon'
    waitingIcon.style.zIndex = '6006'
    waitingIcon.style.left = (pageW - 50)/2 + 'px'

    let waitingI = document.createElement("i")
    waitingI.className = 'fa fa-spinner fa-spin animated'
    waitingIcon.appendChild(waitingI)

    dialogBody.appendChild(img)
    dialogBody.appendChild(closeButton)
    dialogBody.appendChild(waitingIcon)

    dialog.appendChild(dialogBody)
    wrapper.appendChild(dialog)
    body.appendChild(wrapper)

    closeButton.onclick = function() {
        document.querySelector('body').onkeydown = null
        removeImageDialog()
    }
    TransformImage(img,src,index)
}
function searchText(text) {
    if (text) window.location.href = '/search?text=' + encodeURI(text)
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
    document.querySelectorAll('.gird-cell-img').forEach(function(img) {
        let src = img.getAttribute('src')
        if (src.indexOf('.thumb/')==0) src = src.substring(7)
        let pos = img.className.indexOf('img-index-')
        const index = (pos>=0 ? parseInt(img.className.substring(pos+10)) : 0)
        img.onclick=function (event){
            event.stopPropagation()
            addImageDialog(src, index == NaN ? 0 : index)
        }
    });

}