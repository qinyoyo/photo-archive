<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <link rel="stylesheet" href="/static/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/static/css/pv.css">
    <link rel="stylesheet" href="/static/css/editor.css">
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/editor.js"></script>
    <script type="text/javascript" src="/static/js/image_lazy_load.js"></script>
    <title><#if title??>${title}</#if></title>
</head>
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
    <div><img src="/static/image/bold.png" data-action="bold"></div>
    <div><img src="/static/image/italic.png" data-action="italic"></div>
    <div><img src="/static/image/subscript.png" data-action="subscript"></div>
    <div><img src="/static/image/superscript.png" data-action="superscript"></div>
    <div><img src="/static/image/strikethrough.png" data-action="strikethrough"></div>
    <div><img src="/static/image/underline.png" data-action="underline"></div>
    <div><img src="/static/image/h1.png" data-action="h1"></div>
    <div><img src="/static/image/h2.png" data-action="h2"></div>
    <div><img src="/static/image/h3.png" data-action="h3"></div>
    <div><img src="/static/image/h4.png" data-action="h4"></div>
    <div><img src="/static/image/h5.png" data-action="h5"></div>
    <div><img src="/static/image/h6.png" data-action="h6"></div>
    <div><img src="/static/image/txt_color.png" data-action="txt_color"></div>
    <div><img src="/static/image/bg_color.png" data-action="bg_color"></div>
    <div><img src="/static/image/insert_link.png" data-action="insert_link"></div>
    <div><img src="/static/image/undo.png" data-action="undo"></div>
    <div><img src="/static/image/redo.png" data-action="redo"></div>
</div>
<div id="editor" contenteditable="true" data-file="${sourceFile}">
${body?replace('contenteditable="false"','contenteditable="true"')}
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
</html>



