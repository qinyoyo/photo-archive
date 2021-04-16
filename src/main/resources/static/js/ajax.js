;(function () {
    function setFullscreen(element) {
        const el = element instanceof HTMLElement ? element : document.documentElement;
        const rfs = el.requestFullscreen       ||
            el.webkitRequestFullscreen ||
            el.mozRequestFullScreen    ||
            el.msRequestFullscreen;
        if (rfs) {
            rfs.call(el);
        }
    }
    function exitFullscreen(){
        const efs = document.exitFullscreen ||
            document.webkitExitFullscreen ||
            document.mozCancelFullScreen  ||
            document.msExitFullscreen;
        if (efs) {
            efs.call(document);
        }
    }
    window.fullScreenElement = function() {
        const fullscreenEnabled = document.fullscreenEnabled       ||
            document.mozFullScreenEnabled    ||
            document.webkitFullscreenEnabled ||
            document.msFullscreenEnabled;
        if (fullscreenEnabled) {
            const fullscreenElement = document.fullscreenElement    ||
                document.mozFullScreenElement ||
                document.webkitFullscreenElement;
            return  fullscreenElement
        } else return 0
    }
    window.handleFullScreen = function (element){
        const e = fullScreenElement()
        if (e) exitFullscreen()
        else if (e===0) console.log('浏览器当前不能全屏');
        else setFullscreen(element);
    }
    window.Ajax = {
        get: function (url, callback, sync) {
            const xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                // readyState == 4说明请求已完成
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    if (typeof callback==='function') callback.call(this, xhr.responseText);
                }
            };
            xhr.open('GET', url, sync? false : true);
            xhr.send();
        },
        post: function (url, data, callback, sync) {
            const xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    if (typeof callback==='function') callback.call(this, xhr.responseText);
                }
            };
            xhr.open("post", url, sync? false : true);
            xhr.send(data);
        }
    }
    window.openDialog = function(options) {
        const dlgWrapper = document.createElement('div')
        dlgWrapper.className='dialog__wrapper'

        const closeFunction = function() {
            if (typeof options.oncancel === 'function') options.oncancel()
            dlgWrapper.remove()
        }

        const dlgContent = document.createElement('div')
        dlgContent.className='dialog__content' + (options.dialogClass ? ' '+options.dialogClass : '')
        if (options.dialogStyle){
            if (options.dialogStyle){
                Object.keys(options.dialogStyle).forEach(function (key){
                    dlgContent.style[key]=options.dialogStyle[key]
                })
            }
        }
        dlgWrapper.appendChild(dlgContent)

        const dlgTitleWrapper = document.createElement('div')
        dlgTitleWrapper.className='dialog__title'
        dlgContent.appendChild(dlgTitleWrapper)
        if (options.title) {
            const title = document.createElement('span')
            title.innerText = options.title
            dlgTitleWrapper.appendChild(title)
        }
        const closeIcon = document.createElement('i')
        closeIcon.className = 'dialog__close-icon fa fa-close'
        closeIcon.onclick = closeFunction
        dlgTitleWrapper.appendChild(closeIcon)

        const dlgBody = document.createElement('div')
        dlgBody.className='dialog__body' + (options.dialogBodyClass ? ' '+options.dialogBodyClass : '')
        if (options.dialogBodyStyle) {
            if (options.dialogBodyStyle) {
                Object.keys(options.dialogBodyStyle).forEach(function(key){
                    dlgBody.style[key] = options.dialogBodyStyle[key]
                })
            }
        }

        dlgContent.appendChild(dlgBody)

        if (typeof options.body === 'string') dlgBody.innerHTML = options.body
        else if (options.body) dlgBody.appendChild(options.body)

        if (typeof options.callback==='function' || typeof options.oncancel === 'function') {
            const btns = document.createElement('div')
            btns.style.textAlign = 'center'
            dlgBody.appendChild(btns)
            if (typeof options.callback==='function'){
                const okbtn=document.createElement('button')
                okbtn.className='dialog__button'
                okbtn.innerText=(options.okText ? options.okText : '确定')
                okbtn.onclick=function (){
                    options.callback(dlgBody)
                    dlgWrapper.remove()
                }
                btns.appendChild(okbtn)
            }
            if (typeof options.oncancel==='function'){
                const cancelbtn=document.createElement('button')
                cancelbtn.className='dialog__button'
                cancelbtn.innerText=(options.cancelText ? options.cancelText : '取消')
                cancelbtn.onclick=closeFunction
                btns.appendChild(cancelbtn)
            }
        }
        document.querySelector('body').appendChild(dlgWrapper)
    }

    window.message = function (msg) {
        openDialog({
            title: '提示',
            body: msg,
            okText: '知道了',
            callback: function() {}
        })
    }
    window.toast  = function(msg,delay,onclose) {  // 显示提示信息，自动关闭
        if (typeof msg != 'string') return
        document.querySelectorAll('.message__toast').forEach(function(e) { e.remove(); })
        let toastDiv = document.createElement("div")
        toastDiv.className = 'message__toast'
        toastDiv.style.zIndex = "99999"
        toastDiv.style.display = 'hide'
        toastDiv.style.padding = '5px'
        let span = document.createElement("span")
        span.innerHTML = msg.replace(/\n/g,'<br>')
        toastDiv.appendChild(span)
        document.querySelector('body').appendChild(toastDiv)
        const w = 10 + span.offsetWidth, h = 10 + span.offsetHeight
        toastDiv.style.left = (window.innerWidth - w)/2  + 'px'
        toastDiv.style.top = (window.innerHeight - h)/2  + 'px'
        toastDiv.style.display = 'block'
        const remove = function() {
            toastDiv.remove()
            if (typeof onclose === 'function') onclose.call()
        }
        let toastTimer = setTimeout(remove,delay ? delay : 1000)
        toastDiv.onclick = function() {
            if (toastTimer) {
                clearTimeout(toastTimer)
                toastTimer = null
            } else remove()
        }
    }
    window.input = function(options) {
        let dlgOptions = {
            title: options.title,
            dialogClass: options.dialogClass,
            dialogStyle: options.dialogStyle,
            dialogBodyClass: options.dialogBodyClass,
            dialogBodyStyle: options.dialogBodyStyle
        }
        const dlgBody = document.createElement('div')
        if (options.title) dlgOptions.title = options.title
        if (options.label) {
            const t = document.createElement("span")
            t.style.paddingRight = '5px'
            t.innerText = options.label
            dlgBody.appendChild(t)
        }
        const input = document.createElement("input")
        input.id = 'dyna-dialog-input-element'
        if (options.inputStyle) {
            Object.keys(options.inputStyle).forEach(function(key){
                input.style[key] = options.inputStyle[key]
            })
        }
        if (options.inputType) input.type = options.inputType
        input.value = (options.defaultValue ? options.defaultValue : '')
        dlgBody.appendChild(input)

        dlgOptions.body = dlgBody
        if (typeof options.callback==='function') dlgOptions.callback = function() {
            options.callback(input.value)
        }
        openDialog(dlgOptions)
    }
    window.addClass = function(dom,cls) {
        if (!dom) return
        let cls0 = dom.className
        if (!cls0) dom.className = cls
        else {
            let ac = cls0.split(' ')
            if (ac.indexOf(cls)<0) {
                ac.push(cls)
                dom.className = ac.join(' ')
            }
        }
    }
    window.removeClass = function(dom,cls) {
        if (!dom) return
        let cls0 = dom.className
        if (cls0) {
            let ac = cls0.split(' ')
            let i = ac.indexOf(cls)
            if (i>=0) {
                ac.splice(i,1)
                dom.className = ac.join(' ')
            }
        }
    }
    window.showWaiting = function(id) {
        let waitingIcon = document.createElement("button")
        waitingIcon.className = 'waiting-icon__wrapper'
        waitingIcon.style.zIndex = '99999'
        waitingIcon.id = id ? id : 'waiting_icon_overlay'
        let waitingI = document.createElement("i")
        waitingI.className = 'fa fa-spinner fa-spin animated'
        waitingIcon.appendChild(waitingI)
        document.querySelector('body').appendChild(waitingIcon)
    }
    window.hideWaiting = function(id) {
        document.getElementById( id ? id : 'waiting_icon_overlay').remove()
    }
})();