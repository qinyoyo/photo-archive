function cellWidth() {
    let sw = window.innerWidth
    let w = 196
    if (w>(sw-20)/3-10) w = Math.trunc((sw-20)/3-10)
    return w
}
function adjustSize(img) {
    let iw = img.naturalWidth, ih = img.naturalHeight
    let w = cellWidth()
    img.parentNode.style.width = w+'px'
    if (iw<=w) {
        img.style.height = Math.min(w,ih) + 'px'
        img.parentNode.style.height = (Math.min(w,ih) + 26) + 'px'
    }
    else {
        img.style.height = Math.trunc(Math.min(w, ih*w/iw)) + 'px'
        img.parentNode.style.height = Math.trunc(Math.min(w, ih*w/iw)+26) + 'px'
    }
    const  checkbox = img.parentNode.querySelector('.grid-cell-label input[type="checkbox"]')
    if (checkbox) img.onclick = function() {
        checkbox.checked = !checkbox.checked
    }
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
        RE.removeFormat()
        document.execCommand("fontSize", false, fontSize);
    }

    RE.setHeading = function(heading) {
        RE.removeFormat()
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

    RE.insertImageW = function(url, alt, width, justGetHtml, classes) {
        if (!url || url.length==0) return
        const getAlt = function(i) {
            if (!alt) return 'photo'
            else if (i<alt.length) return alt[i]
            else return alt[0]
        }
        url = (typeof url === 'string' ? url.split(',') : url)
        alt = (typeof alt === 'string' ? alt.split(',') : alt)
        let end
        let step
        if (url.length<6) step = url.length
        else step = 3
        let html=''
        for (let start=0;start<url.length;) {
            end = start + step
            if (url.length - end == 1) end++
            else if (url.length - end == 5) end++
            html += '<div class="row">'
            for (let i = start; i < end; i++) {
                html += ('<div class="col-1-' + (end - start) + '"><img' + (classes?' class="'+classes+'"':'') +
                    ' src="' + url[i] + '" alt="' + getAlt(i) + '"></img></div>')
            }
            html += '</div>';
            start = end;
        }

        if (justGetHtml) return html
        else RE.insertHTML(html);
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
        try{
            const selection=window.getSelection();
            selection.removeAllRanges();
            const range=document.createRange();
            range.setStart(RE.currentSelection.startContainer,RE.currentSelection.startOffset);
            range.setEnd(RE.currentSelection.endContainer,RE.currentSelection.endOffset);
            selection.addRange(range);
        } catch (e){
            console.log(e)
        }
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

    const setStyle = function(callback) {
        const html = '<div class="font-style-setter">\n' +
            '            <div>\n' +
            '                <input type="radio" checked name="fontStyle" id="style-bold"><label for="style-bold">粗体</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="radio" name="fontStyle" id="style-italic"><label for="style-italic">斜体</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="radio" name="fontStyle" id="style-underline"><label for="style-underline">下划线</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="radio" name="fontStyle" id="style-strikethrough"><label for="style-strikethrough">删除线</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="radio" name="fontStyle" id="style-superscript"><label for="style-superscript">上标</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="radio" name="fontStyle" id="style-subscript"><label for="style-subscript">下标</label>\n' +
            '            </div>\n' +
            '        </div>'

        window.openDialog({
            title: '设置/取消字体样式',
            body: html,
            dialogStyle: {
                minWidth: '200px'
            },
            callback: function() {
                const style=document.querySelector('.font-style-setter input:checked').id.substring(6)
                setTimeout(function() { callback(style) },100)
            }
        })
    }
    RE.removeFormat = function() {
        document.execCommand('removeFormat', false, null);
    }

    const getColor = function(msg, callback) {
        const html = '<div>\n' +
            '            <div>\n' +
            '                <input type="checkbox" id="color-back"><label for="color-back">设置背景色</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="color" id="color_input"><label for="color_input">选择颜色</label>\n' +
            '            </div>\n' +
            '        </div>'

        window.openDialog({
            title: '选择颜色',
            body: html,
            callback: function() {
                const color = document.getElementById('color_input').value
                const isBack = document.getElementById('color-back').checked
                setTimeout(function() { callback(color,isBack) },100)
            }
        })
    }
    const setTitle = function(callback) {
        const html = '<div>\n' +
            '            <div>\n' +
            '                <input type="radio" checked name="sizeaction" id="size_title"><label for="size_title">设置标题</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <input type="radio" name="sizeaction" id="size_font"><label for="size_font">设置字体大小</label>\n' +
            '            </div>\n' +
            '            <div>\n' +
            '                <label for="size_level">大小</label>\n' +
            '                <select id="size_level" value="5">\n' +
            '                    <option value="1">1</option>\n' +
            '                    <option value="2">2</option>\n' +
            '                    <option value="3">3</option>\n' +
            '                    <option value="4">4</option>\n' +
            '                    <option value="5">5</option>\n' +
            '                    <option value="6">6</option>\n' +
            '                </select>\n' +
            '            </div>\n' +
            '        </div>'


        window.openDialog({
            title: '选择字体大小',
            body: html,
            dialogStyle: {
                minWidth: '200px'
            },
            callback: function() {
                const level = document.getElementById('size_level').value
                const isTitle = document.getElementById('size_title').checked
                setTimeout(function() { callback(level,isTitle) },100)
            }
        })
    }
    const getLinkResource = function(callback) {
        window.input({
            title: '输入一个链接地址',
            dialogStyle: {
                width: '300px'
            },
            inputType: 'url',
            callback: function(v) {
                if (v) setTimeout(function() { callback(v) },100)
            }
        })
    }

    const getResource = function(type,callback) {
        const dialog = document.getElementById('select-resource')
        const dialogTitle = document.querySelector('.select-resource-title')
        if (dialogTitle) {
            if (type=='video') dialogTitle.innerText = '选择视频资源文件'
            else if (type=='audio') dialogTitle.innerText = '选择语音资源文件'
            else if (type=='photo') dialogTitle.innerText = '选择图像资源文件'
            else dialogTitle.innerText = '选择资源文件'
        }
        document.getElementById('select-resource-content').className=type
        dialog.querySelectorAll('input').forEach(function(e){ e.checked = false })
        dialog.querySelector('button.resource-selected').onclick = function() {
            if (type)  {
                const form = document.getElementById(type+'-form')
                if (form) {
                    let formData = new FormData(form)
                    let value = formData.getAll(type)
                    if (value instanceof Array) value = value.join(",")
                    callback(value)
                }
            }
            dialog.style.display='none'
        }
        dialog.style.display='block'
    }
    const reloadResourceByPath = function(path) {
        let currentPath = document.getElementById('resource-list').getAttribute("data-path")
        const url = '/resource?current='+encodeURI(currentPath) + (path ? '&path=' + encodeURI(path) : '')
        Ajax.get(url, function (responseText) {
            if (responseText && responseText!='error') {
                document.getElementById('select-resource-content').innerHTML = responseText
                initResource()
            }
        })
    }
    const reloadResourceByDate = function(date) {
        let currentPath = document.getElementById('resource-list').getAttribute("data-path")
        const url = '/resource?current='+encodeURI(currentPath) + '&date=' + encodeURI(date)
        Ajax.get(url, function (responseText) {
            if (responseText && responseText!='error') {
                document.getElementById('select-resource-content').innerHTML = responseText
                initResource()
            }
        })
    }
    let dateValue = '2000-01-01'
    const initResource = function() {
        const dialog = document.getElementById('select-resource')
        const cw = cellWidth() + 'px'
        document.querySelectorAll('.grid-cell-label').forEach(function(e) {
            e.style.width = cw
        })
        document.getElementById('resource-list').style.maxHeight = (window.innerHeight - 120) + 'px'

        const folderPicker = dialog.querySelector('.folder-picker')
        if (folderPicker) folderPicker.onclick = function() {
            let path = document.getElementById('resource-list').getAttribute("data-path")
            reloadResourceByPath(path)
        }
        const datePicker = dialog.querySelector('.date-picker')
        if (datePicker) datePicker.onclick = function() {
            reloadResourceByDate(dateValue)
        }
        const dateItem = dialog.querySelector('.date-item')
        if (dateItem) dateItem.onclick = function() {
            dateValue = dialog.querySelector('.date-item-input').value
            reloadResourceByDate(dateValue)
        }
        dialog.querySelectorAll('.folder-item').forEach(function(d) {
            let path = d.getAttribute('data-folder')
            d.onclick=function () {
                reloadResourceByPath(path)
            }
        });
    }

    window.onload = function() {
        window.onbeforeunload = function(e) {
            const dialogText = '页面已修改';
            e.returnValue = dialogText;
            return dialogText;
        };
        const sw = window.innerWidth,
              bw = document.querySelector('body').clientWidth
        if ((sw - bw)/2 > 32) document.querySelector('.float-editor__buttons').style.right = (Math.round((sw - bw)/2) - 32) + 'px'

        document.querySelector('#select-resource .dialog__body').style.width = (sw>600 ? '600px' : sw+'px')

        const cw = cellWidth() + 'px'
        document.querySelectorAll('.grid-cell-label').forEach(function(e) {
            e.style.width = cw
        })
        RE.editor = document.getElementById('editor');
        RE.setBaseFontSize('14px');
        initResource()
        let htmlSaved = RE.getHtml()
        document.querySelectorAll('.float-editor__buttons img').forEach(function(img) {
            const action = img.getAttribute("data-action")
            img.onclick = function(event){
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
                                if (msg=='ok') {
                                    htmlSaved = html
                                    message('保存成功')
                                }
                                else message(msg)
                            })
                        } else message('没有改变')
                        break;
                    case 'insert_image':
                        if (RE.prepareInsert()){
                            getResource('photo',function (values){
                                const indexes = values.split(',')
                                let url = []
                                let alt = []
                                let imgs = []
                                indexes.forEach(function(i){
                                    const img = document.querySelector('#photo-form img.img-index-'+i)
                                    url.push(img.getAttribute("data-value"))
                                    alt.push(img.getAttribute("alt"))
                                    imgs.push(img)
                                })
                                let fullHtml = RE.insertImageW(url,alt,720, true, 'lazy-load')
                                if (imgs.length>0) {
                                    let detail = ''
                                    imgs.forEach(function(img){
                                        let gpsfound = false
                                        let html = '<div class="gps-block">'
                                        let dt = img.getAttribute('data-datetimeoriginal')
                                        let poi = img.getAttribute('title')
                                        if (poi) {
                                            let pos = poi.indexOf('\ufeff')
                                            if (pos>=0) poi = poi.substring(0,pos)
                                        }
                                        let longitude = img.getAttribute('data-gpslongitude')
                                        let latitude = img.getAttribute('data-gpslatitude')
                                        if (longitude && latitude) {
                                            gpsfound = true
                                            if (!poi) poi='poi'
                                            else if (dt && poi.indexOf(dt)==0) poi=poi.substring(dt.length)
                                            html += (dt?'<span>' + dt + '</span>' : '') +'<a href="'
                                                + "https://uri.amap.com/marker?src=mySteps" + "&name=" + poi
                                                + "&position=" + longitude +"," + latitude
                                                + "&coordinate=wgs84"
                                                + '">' + poi + '</a><br>'
                                        } else if (poi) {
                                            html += '<span>' + poi + '</span>'
                                        } else if (dt) {
                                            html += '<span>' + dt + '</span>'
                                        }
                                        html += '</div>'
                                        detail = html
                                        if (gpsfound) return
                                    })
                                    fullHtml += detail
                                }
                                if (fullHtml) {
                                    RE.setJustifyLeft()
                                    RE.insertHTML(fullHtml)
                                }
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
                    case 'style':
                        if (RE.prepareInsert()){
                            setStyle(function(style) {
                                RE.restorerange()
                                switch (style) {
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
                                }
                            })
                        }
                        break;
                    case 'size':
                        if (RE.prepareInsert()){
                            setTitle(function(level,isTitle) {
                                RE.restorerange()
                                if (isTitle) RE.setHeading(level)
                                else RE.setFontSize(parseInt(level))
                            })
                        }
                        break;
                    case 'color':
                        if (RE.prepareInsert()){
                            getColor(function (col,isBack){
                                RE.restorerange()
                                if (isBack) RE.setTextBackgroundColor(col)
                                else RE.setTextColor(col)
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