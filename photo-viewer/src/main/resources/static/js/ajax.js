;(function () {
    window.Ajax = {
        get: function (url, callback) {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', url, true);
            xhr.onreadystatechange = function () {
                // readyState == 4说明请求已完成
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    if (typeof callback==='function') callback.call(this, xhr.responseText);
                }
            };
            xhr.send();
        },
        post: function (url, data, callback) {
            const xhr = new XMLHttpRequest();
            xhr.open("POST", url, true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    if (typeof callback==='function') callback.call(this, xhr.responseText);
                }
            };
            xhr.send(data);
        }
    }
    window.input = function(options) {
        const body = document.querySelector('body')
        const dialog = document.createElement("dialog")
        dialog.className = 'common-dialog'
        if (options.dialogStyle) {
            if (options.dialogStyle) {
                Object.keys(options.dialogStyle).forEach(key=>{
                    dialog.style[key] = options.dialogStyle[key]
                })
            }
        }
        if (options.title) {
            const t = document.createElement("div")
            t.style.textAlign='center'
            t.style.fontWeight='bold'
            t.style.color='blue'
            t.style.marginBottom='10px'
            t.innerText = options.title
            dialog.appendChild(t)
        }
        if (options.label) {
            const t = document.createElement("span")
            t.style.paddingRight = '5px'
            t.innerText = options.label
            dialog.appendChild(t)
        }
        const input = document.createElement("input")
        input.id = 'dyna-dialog-input-element'
        if (options.inputStyle) {
            Object.keys(options.inputStyle).forEach(key=>{
                input.style[key] = options.inputStyle[key]
            })
        }
        if (options.inputType) input.type = options.inputType
        dialog.appendChild(input)

        const split =  document.createElement("div")
        split.style.paddingTop = '20px'
        split.style.textAlign = 'center'
        const ok = document.createElement("button")
        ok.style.marginRight = '20px'
        ok.style.width = '80px'
        ok.innerText='确定'
        ok.onclick = function() {
            if (typeof options.callback === 'function') {
                options.callback(input.value)
            }
            dialog.close(input.value)
        }
        split.appendChild(ok)
        const cancel = document.createElement("button")
        cancel.innerText='取消'
        cancel.style.width = '80px'
        cancel.onclick = function() {
            if (typeof options.oncancel === 'function') {
                options.oncancel()
            }
            dialog.close()
        }
        split.appendChild(cancel)

        dialog.appendChild(split)
        body.appendChild(dialog)

        dialog.showModal()
        dialog.onclose = function(){
            dialog.remove()
        }
    }
})();