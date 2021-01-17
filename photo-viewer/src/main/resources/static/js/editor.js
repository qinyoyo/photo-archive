
function adjustSize(img) {
    let w = 196
    let iw = img.naturalWidth, ih = img.naturalHeight
    if (iw<=w) img.parentNode.style.height = (Math.min(w,ih) + 26) + 'px'
    else img.parentNode.style.height = Math.trunc(Math.min(w, ih*w/iw)+26) + 'px';
}
function resourceSelected(ok) {
    const dialog = document.getElementById('select-resource')
    if (ok) {
        const type = document.getElementById('select-resource-content').className
        if (type)  {
            const form = document.getElementById(type+'-form')
            if (form) {
                let formData = new FormData(form)
                let value = formData.getAll(type)
                if (value instanceof Array) value = value.join(",")
                dialog.close(value)
                return
            }
        }
    }
    dialog.close()
}
;(function () {

    const RE = {};

    RE.currentSelection = {
        "startContainer": 0,
        "startOffset": 0,
        "endContainer": 0,
        "endOffset": 0};

    RE.editor = document.getElementById('editor');

    RE.getDocumentWidth = function () {
        return  document.documentElement.clientWidth;
    }

    RE.setHtml = function(contents) {
        RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
    }

    RE.getHtml = function() {
        return RE.editor.innerHTML;
    }

    RE.getText = function() {
        return RE.editor.innerText;
    }

    RE.setBaseTextColor = function(color) {
        RE.editor.style.color  = color;
    }

    RE.setBaseFontSize = function(size) {
        RE.editor.style.fontSize = size;
    }

    RE.setPadding = function(left, top, right, bottom) {
        RE.editor.style.paddingLeft = left;
        RE.editor.style.paddingTop = top;
        RE.editor.style.paddingRight = right;
        RE.editor.style.paddingBottom = bottom;
    }

    RE.setBackgroundColor = function(color) {
        document.body.style.backgroundColor = color;
    }

    RE.setBackgroundImage = function(image) {
        RE.editor.style.backgroundImage = image;
    }

    RE.setWidth = function(size) {
        RE.editor.style.minWidth = size;
    }

    RE.setHeight = function(size) {
        RE.editor.style.height = size;
    }

    RE.setTextAlign = function(align) {
        RE.editor.style.textAlign = align;
    }

    RE.setVerticalAlign = function(align) {
        RE.editor.style.verticalAlign = align;
    }

    RE.setPlaceholder = function(placeholder) {
        RE.editor.setAttribute("placeholder", placeholder);
    }

    RE.setInputEnabled = function(inputEnabled) {
        RE.editor.contentEditable = String(inputEnabled);
    }

    RE.undo = function() {
        document.execCommand('undo', false, null);
    }

    RE.redo = function() {
        document.execCommand('redo', false, null);
    }

    RE.setBold = function() {
        document.execCommand('bold', false, null);
    }

    RE.setItalic = function() {
        document.execCommand('italic', false, null);
    }

    RE.setSubscript = function() {
        document.execCommand('subscript', false, null);
    }

    RE.setSuperscript = function() {
        document.execCommand('superscript', false, null);
    }

    RE.setStrikeThrough = function() {
        document.execCommand('strikeThrough', false, null);
    }

    RE.setUnderline = function() {
        document.execCommand('underline', false, null);
    }

    RE.setBullets = function() {
        document.execCommand('insertUnorderedList', false, null);
    }

    RE.setNumbers = function() {
        document.execCommand('insertOrderedList', false, null);
    }

    RE.setTextColor = function(color) {
        RE.restorerange();
        document.execCommand("styleWithCSS", null, true);
        document.execCommand('foreColor', false, color);
        document.execCommand("styleWithCSS", null, false);
    }

    RE.setTextBackgroundColor = function(color) {
        RE.restorerange();
        document.execCommand("styleWithCSS", null, true);
        document.execCommand('hiliteColor', false, color);
        document.execCommand("styleWithCSS", null, false);
    }

    RE.setFontSize = function(fontSize){
        document.execCommand("fontSize", false, fontSize);
    }

    RE.setHeading = function(heading) {
        document.execCommand('formatBlock', false, '<h'+heading+'>');
    }

    RE.setIndent = function() {
        //document.execCommand('formatBlock', false, '<blockquote>');
        document.execCommand('indent', false, null);
    }

    RE.setOutdent = function() {
        document.execCommand('outdent', false, null);
    }

    RE.setJustifyLeft = function() {
        document.execCommand('justifyLeft', false, null);
    }

    RE.setJustifyCenter = function() {
        document.execCommand('justifyCenter', false, null);
    }

    RE.setJustifyRight = function() {
        document.execCommand('justifyRight', false, null);
    }

    RE.setBlockquote = function() {
        document.execCommand('formatBlock', false, '<blockquote>');
    }

    RE.insertImageW = function(url, alt, width) {
        if (!url || url.length==0) return
        const getAlt = function(i) {
            if (!alt) return 'photo'
            else if (i<alt.length) return alt[i]
            else return alt[0]
        }
        url = (typeof url === 'string' ? url.split(',') : url)
        alt = (typeof alt === 'string' ? alt.split(',') : alt)
        let html
        if (url.length==1) html = '<div class="center-block"><img src="' + url[0]  + '" alt="' + getAlt(0) + '" width="' + width + '"/></div>';
        else {
            width = Math.trunc((100 - url.length)/url.length) + '%'
            html = '<div class="center-block">'
            for (let i=0;i<url.length;i++) {
                html += ('<img src="' + url[i] + '" alt="' + getAlt(i) +'"' + ' style="width:'+width+'"' +'></img>')
            }
            html += '</div>';
        }
        RE.insertHTML(html);
    }

    RE.insertVideoW = function(url, width) {
        if (!url || url.length==0) return
        let html
        if (typeof url === 'string' || url.length==1) html = '<div class="center-block"><video src="' + (typeof url === 'string' ? url : url[0]) + '" width="' + width + '" controls></video></div>';
        else {
            width = Math.trunc((width - (url.length-1)*5)/url.length)
            html = '<div class="center-block">'
            for (let i=0;i<url.length;i++) {
                html += ('<video src="' + url[i] + '" width="' + width + '" controls' + (i<url.length-1 ? ' style="padding-right:5px"':'') +'></video>')
            }
            html += '</div>';
        }
        RE.insertHTML(html);
    }

    RE.insertAudioW = function(url, width) {
        if (!url || url.length==0) return
        let html
        if (typeof url === 'string' || url.length==1) html = '<div class="center-block"><audio src="' + (typeof url === 'string' ? url : url[0]) + '" controls></audio></div>';
        else {
            width = Math.trunc((width - (url.length-1)*5)/url.length)
            html = '<div class="center-block">'
            for (let i=0;i<url.length;i++) {
                html += ('<audio src="' + url[i] + '" controls' + (i<url.length-1 ? ' style="padding-right:5px; width:'+width+'px"':' style="width:'+width+'px"') +'></audio>')
            }
            html += '</div>';
        }
        RE.insertHTML(html);
    }

    RE.insertHTML = function(html) {
        RE.restorerange();
        document.execCommand('insertHTML', false, html);
    }

    RE.insertLink = function(url, title) {
        RE.restorerange();
        const sel = document.getSelection();
        if (sel.toString().length == 0) {
            document.execCommand("insertHTML",false,"<a href='"+url+"'>"+title+"</a>");
        } else if (sel.rangeCount) {
            const el = document.createElement("a");
            el.setAttribute("href", url);
            el.setAttribute("title", title);

            const range = sel.getRangeAt(0).cloneRange();
            range.surroundContents(el);
            sel.removeAllRanges();
            sel.addRange(range);
        }
    }

    RE.setTodo = function(text) {
        const html = '<input type="checkbox" name="'+ text +'" value="'+ text +'"/> &nbsp;';
        document.execCommand('insertHTML', false, html);
    }

    RE.prepareInsert = function() {
        return RE.backuprange();
    }

    RE.backuprange = function(){
        const selection = window.getSelection();
        if (selection.rangeCount > 0) {
            var range = selection.getRangeAt(0);
            RE.currentSelection = {
                "startContainer": range.startContainer,
                "startOffset": range.startOffset,
                "endContainer": range.endContainer,
                "endOffset": range.endOffset
            };
        }
        return selection.rangeCount
    }

    RE.restorerange = function(){
        const selection = window.getSelection();
        selection.removeAllRanges();
        const range = document.createRange();
        range.setStart(RE.currentSelection.startContainer, RE.currentSelection.startOffset);
        range.setEnd(RE.currentSelection.endContainer, RE.currentSelection.endOffset);
        selection.addRange(range);
    }

    RE.focus = function() {
        const range = document.createRange();
        range.selectNodeContents(RE.editor);
        range.collapse(false);
        const selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
        RE.editor.focus();
    }

    RE.blurFocus = function() {
        RE.editor.blur();
    }

    RE.removeFormat = function() {
        document.execCommand('removeFormat', false, null);
    }

    const showDialog = function(dialog, callback) {
        dialog.showModal()
        dialog.onclose = function(){
            if (dialog.returnValue) {
                setTimeout(function() { callback(dialog.returnValue) },100)
            }
        };
    }
    const getColor = function(msg, callback) {
        const dialog = document.getElementById('select-color')
        document.getElementById('select-color-title').innerText = msg
        showDialog(dialog, callback)
    }
    const getResource = function(type,callback) {
        const dialog = document.getElementById('select-resource')
        document.getElementById('select-resource-content').className=type
        dialog.querySelectorAll('input').forEach(function(e){ e.checked = false })
        showDialog(dialog, callback)
    }
    const getLinkResource = function(callback) {
        const dialog = document.getElementById('select-link')
        showDialog(dialog, callback)
    }
    const initResource = function() {
        const dialog = document.getElementById('select-resource')
        dialog.querySelectorAll('.folder-item').forEach(function(d) {
            let path = d.getAttribute('data-folder')
            let currentPath = document.getElementById('resource-list').getAttribute("data-path")
            const url = '/resource?current='+encodeURI(currentPath) + (path ? '&path=' + encodeURI(path) : '')
            d.onclick=function () {
                Ajax.get(url, function (responseText) {
                    if (responseText && responseText!='error') {
                        document.getElementById('select-resource-content').innerHTML = responseText
                        initResource()
                    }
                })
            }
        });
    }

    window.onload = function() {
        window.onbeforeunload = function(e) {
            const dialogText = '页面已修改';
            e.returnValue = dialogText;
            return dialogText;
        };
        const sw =window.innerWidth,
              bw = document.querySelector('body').clientWidth
        document.querySelector('.float-editor__buttons').style.right = (Math.round((sw - bw)/2) - 32) + 'px'
        RE.editor = document.getElementById('editor');
        RE.setBaseFontSize('14px');
        initResource()
        let htmlSaved = RE.getHtml()
        document.querySelectorAll('.float-editor__buttons img').forEach(function(img) {
            const action = img.getAttribute("data-action")
            img.onclick = function(){
                console.log(action)
                switch (action) {
                    case 'justify_left':
                        RE.setJustifyLeft()
                        break;
                    case 'justify_right':
                        RE.setJustifyRight()
                        break;
                    case 'justify_center':
                        RE.setJustifyCenter()
                        break;
                    case 'save':
                        const html = RE.getHtml()
                        if (html!=htmlSaved){
                            const source=RE.editor.getAttribute("data-file")
                            let data=new FormData()
                            data.append("source",source)
                            data.append("body",html)
                            Ajax.post("/save",data,function (msg){
                                if (msg=='ok') htmlSaved = html
                                alert(msg)
                            })
                        } else alert('没有改变')
                        break;
                    case 'insert_image':
                        if (RE.prepareInsert()){
                            getResource('photo',function (url){
                                RE.insertImageW(url.indexOf(',')>=0?url.split(','):url,url.indexOf(',')>=0?url.split(','):url,720)
                            })
                        }
                        break;
                    case 'music':
                        if (RE.prepareInsert()){
                            getResource('audio',function (url){
                                RE.insertAudioW(url.indexOf(',')>=0?url.split(','):url,720);
                            })
                        }
                        break;
                    case 'video':
                        if (RE.prepareInsert()){
                            getResource('video',function (url){
                                RE.insertVideoW(url.indexOf(',')>=0?url.split(','):url,720);
                            })
                        }
                        break;
                    case 'indent':
                        RE.setIndent()
                        break;
                    case 'outdent':
                        RE.setOutdent()
                        break;
                    case 'bullets':
                        RE.setBullets()
                        break;
                    case 'numbers':
                        RE.setNumbers()
                        break;
                    case 'bold':
                        RE.setBold()
                        break;
                    case 'italic':
                        RE.setItalic()
                        break;
                    case 'subscript':
                        RE.setSubscript()
                        break;
                    case 'superscript':
                        RE.setSuperscript()
                        break;
                    case 'strikethrough':
                        RE.setStrikeThrough()
                        break;
                    case 'underline':
                        RE.setUnderline()
                        break;
                    case 'h1':
                        RE.setHeading('1')
                        break;
                    case 'h2':
                        RE.setHeading('2')
                        break;
                    case 'h3':
                        RE.setHeading('3')
                        break;
                    case 'h4':
                        RE.setHeading('4')
                        break;
                    case 'h5':
                        RE.setHeading('5')
                        break;
                    case 'h6':
                        RE.setHeading('6')
                        break;
                    case 'txt_color':
                        if (RE.prepareInsert()){
                            getColor('选择字体颜色',function (col){
                                RE.setTextColor(col)
                            })
                        }
                        break;
                    case 'bg_color':
                        if (RE.prepareInsert()){
                            getColor('选择背景颜色',function (col){
                                RE.setTextBackgroundColor(col)
                            })
                        }
                        break;
                    case 'blockquote':
                        RE.setBlockquote()
                        break;
                    case 'insert_link':
                        if (RE.prepareInsert()){
                            getLinkResource(function (link){
                                RE.insertLink(link,link)
                            })
                        }
                        break;
                    case 'undo':
                        RE.undo()
                        break;
                    case 'redo':
                        RE.redo()
                        break;
                }
            }
        })
    }
})();