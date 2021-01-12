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

    RE.insertImage = function(url, alt) {
        const html = '<div class="center-block"><img src="' + url + '" alt="' + alt + '"/></div>';
        RE.insertHTML(html);
    }

    RE.insertImageW = function(url, alt, width) {
        const html = '<div class="center-block"><img src="' + url + '" alt="' + alt + '" width="' + width + '"/></div>';
        RE.insertHTML(html);
    }

    RE.insertImageWH = function(url, alt, width, height) {
        const html = '<div class="center-block"><img src="' + url + '" alt="' + alt + '" width="' + width + '" height="' + height +'"/></div>';
        RE.insertHTML(html);
    }

    RE.insertVideo = function(url, alt) {
        const html = '<div class="center-block"><video src="' + url + '" controls></video></div>';
        RE.insertHTML(html);
    }

    RE.insertVideoW = function(url, width) {
        const html = '<div class="center-block"><video src="' + url + '" width="' + width + '" controls></video></div>';
        RE.insertHTML(html);
    }

    RE.insertVideoWH = function(url, width, height) {
        const html = '<div class="center-block"><video src="' + url + '" width="' + width + '" height="' + height + '" controls></video></div>';
        RE.insertHTML(html);
    }

    RE.insertAudio = function(url, alt) {
        const html = '<div class="center-block"><audio src="' + url + '" controls></audio></div>';
        RE.insertHTML(html);
    }

    RE.insertYoutubeVideo = function(url) {
        const html = '<iframe width="100%" height="100%" src="' + url + '" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe><br/>\n'
        RE.insertHTML(html);
    }

    RE.insertYoutubeVideoW = function(url, width) {
        const html = '<iframe width="' + width + '" src="' + url + '" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe><br/>\n'
        RE.insertHTML(html);
    }

    RE.insertYoutubeVideoWH = function(url, width, height) {
        const html = '<iframe width="' + width + '" height="' + height + '" src="' + url + '" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe><br/>\n'
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
        RE.backuprange();
    }

    RE.backuprange = function(){
        const selection = window.getSelection();
        if (selection.rangeCount > 0) {
            var range = selection.getRangeAt(0);
            RE.currentSelection = {
                "startContainer": range.startContainer,
                "startOffset": range.startOffset,
                "endContainer": range.endContainer,
                "endOffset": range.endOffset};
        }
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
    const getImageResource = function() {
        return 'res/rwert.jpg'
    }
    const getAudioResource = function() {
        return 'audio-demo.mp3'
    }
    const getVideoResource = function() {
        return 'video-demo.mp4'
    }

    const getLinkResource = function(callback) {
        const dialog = document.getElementById('select-link')
        showDialog(dialog, callback)
    }
    window.onload = function() {
        const sw =window.innerWidth,
              bw = document.querySelector('body').clientWidth
        document.querySelector('.float-editor__buttons').style.right = (Math.round((sw - bw)/2) - 32) + 'px'
/*        document.querySelector('body').onclick = function() {
            console.log(window.getSelection())
            console.log(document.activeElement)
        }*/
        RE.editor = document.getElementById('editor');
        RE.setBaseFontSize('14px');
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
                        break;
                    case 'insert_image':
                        const imgUrl = getImageResource()
                        if (imgUrl){
                            RE.prepareInsert()
                            RE.insertImageW(imgUrl,imgUrl,'100%')
                        }
                        break;
                    case 'music':
                        const audioUrl = getAudioResource()
                        if (audioUrl){
                            RE.prepareInsert()
                            RE.insertAudio(audioUrl);
                        }
                        break;
                    case 'video':
                        const videoUrl = getVideoResource()
                        if (videoUrl){
                            RE.prepareInsert()
                            RE.insertVideoW(videoUrl,700);
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
                        RE.prepareInsert()
                        getColor('选择字体颜色', function(col) {
                            RE.setTextColor(col)
                        })
                        break;
                    case 'bg_color':
                        RE.prepareInsert()
                        getColor('选择背景颜色', function(col) {
                            RE.setTextBackgroundColor(col)
                        })
                        break;
                    case 'blockquote':
                        RE.setBlockquote()
                        break;
                    case 'insert_link':
                        RE.prepareInsert()
                        getLinkResource(function(link) {
                            RE.insertLink(link,link)
                        })
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