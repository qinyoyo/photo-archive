<body>
<div id="tools" class = "float-editor__buttons">
    <div><img src="/static/image/save.png" data-action="save"></div>
    <div><img src="/static/image/insert_image.png" data-action="insert_image"></div>
    <div><img src="/static/image/music.png" data-action="music"></div>
    <div><img src="/static/image/video.png" data-action="video"></div>
    <div><img src="/static/image/justify_left.png" data-action="justify_left"></div>
    <div><img src="/static/image/justify_center.png" data-action="justify_center"></div>
    <div><img src="/static/image/justify_right.png" data-action="justify_right"></div>
    <div><img src="/static/image/indent.png" data-action="indent"></div>
    <div><img src="/static/image/outdent.png" data-action="outdent"></div>
    <div><img src="/static/image/bullets.png" data-action="bullets"></div>
    <div><img src="/static/image/numbers.png" data-action="numbers"></div>
    <div><img src="/static/image/style.png" data-action="style"></div>
    <div><img src="/static/image/size.png" data-action="size"></div>
    <div><img src="/static/image/txt_color.png" data-action="color"></div>
    <div><img src="/static/image/insert_link.png" data-action="insert_link"></div>
    <div><img src="/static/image/undo.png" data-action="undo"></div>
    <div><img src="/static/image/redo.png" data-action="redo"></div>
</div>
<div id="editor" contenteditable="true" data-file="${sourceFile}">
${body}
</div>

<div id="select-resource" style="display:none" class="dialog__wrapper">
    <div class="dialog__content" style="margin-top: 20px;">
        <div class="dialog__title">
            <span class="select-resource-title">选择媒体资源</span>
            <i class="dialog__close-icon fa fa-close" onclick="document.getElementById('select-resource').style.display='none'"></i>
        </div>
        <div class="dialog__body" style="top:10px">
            <div id="select-resource-content" >
                ${resource}
            </div>
            <div style="text-align: center">
                <button class="dialog__button resource-selected">确定</button>
                <button class="dialog__button" onclick="document.getElementById('select-resource').style.display='none'">取消</button>
            </div>
        </div>
    </div>
</div>
</body>




