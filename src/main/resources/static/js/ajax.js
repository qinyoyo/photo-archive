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
    /**
     * 打开对话框
     * @param options
        {
           title: '标题',
           okText: 'Save', //确定执行按钮文字，默认为 确定
           onshow: function(dlgBody) {
           }, // 创建后回调，参数为 dialog__body
           callback: function(dlgBody) {
           }, // 确定时回调，参数为 dialog__body， 空不显示确定按钮
           oncancel: function(dlgBody) {
           }, // 取消时回调，参数为 dialog__body， 空不显示取消按钮
           dialogClass: '', // dialog__content的附加class
           dialogStyle: {
           }, // dialog__content的附加style， map类型，key与元素style类别key相同
           dialogBodyClass: '', // dialog__body的附加class
           dialogBodyStyle: {
             width: 'auto'
           }, //dialog__body的附加style， map类型，key与元素style类别key相同
           body: '' // 对话框内容，html string 或 element object
        }
     */
    window.openDialog = function(options) {
        const dlgWrapper = document.createElement('div')
        dlgWrapper.className='dialog__wrapper'

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

        const dlgBody = document.createElement('div')

        const closeFunction = function() {
            if (typeof options.oncancel === 'function') options.oncancel(dlgBody)
            dlgWrapper.remove()
        }

        const closeIcon = document.createElement('i')
        closeIcon.className = 'dialog__close-icon fa fa-close'
        closeIcon.onclick = closeFunction
        dlgTitleWrapper.appendChild(closeIcon)


        dlgBody.className='dialog__body' + (options.dialogBodyClass ? ' '+options.dialogBodyClass : '')
        if (options.dialogBodyStyle) {
            if (options.dialogBodyStyle) {
                Object.keys(options.dialogBodyStyle).forEach(function(key){
                    if (key==='width' && options.dialogBodyStyle[key]==='auto') {
                        dlgBody.style[key] = (window.innerWidth>=530 ? '500px' : (window.innerWidth - 30) +'px')
                    } else dlgBody.style[key] = options.dialogBodyStyle[key]
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
        if (typeof options.onshow==='function') options.onshow(dlgBody)

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
    window.hasClass = function(dom,clsRegExp) {
        if (!dom || !clsRegExp) return false
        let cls0 = dom.className
        if (!cls0) return false
        return new RegExp('\\b'+clsRegExp+'\\b').test(cls0)
    }
    window.addClass = function(dom,cls) {
        if (!dom || !cls) return
        let cls0 = dom.className
        if (!cls0) dom.className = cls
        else {
            if (hasClass(dom,cls)) return
            dom.className = cls0 +' '+cls
        }
    }
    window.removeClass = function(dom,clsRegExp) {
        if (!dom || !clsRegExp) return
        let cls0 = dom.className
        if (cls0) {
            let ac = cls0.split(' ')
            let nac = []
            const p=new RegExp(clsRegExp)
            for (let i=0;i<ac.length;i++) {
                if (!p.test(ac[i])) nac.push(ac[i])
            }
            if (nac.length) dom.className = nac.join(' ')
            else dom.className = null
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
        const icon = document.getElementById( id ? id : 'waiting_icon_overlay')
        if (icon) icon.remove()
    }
})();