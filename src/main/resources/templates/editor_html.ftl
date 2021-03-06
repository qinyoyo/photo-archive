<!doctype html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="/static/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/static/css/transform_image.css">
    <link rel="stylesheet" href="/static/css/common.css">
    <link rel="stylesheet" href="/static/css/step.css">
    <link rel="stylesheet" href="/static/css/pv.css">
    <link rel="stylesheet" href="/static/css/editor.css">
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <script type="text/javascript" src="/static/js/image_lazy_load.js"></script>
    <script type="text/javascript" src="/static/js/editor.js"></script>
    <#if title??>
    <title>${title}</title>
    </#if>
</head>
<body>
<div id="tools" class = "float-editor__buttons">
    <div>
        <img src="/static/image/save.png" data-action="save">
        <img src="/static/image/insert_image.png" data-action="insert_image">
        <img src="/static/image/music.png" data-action="music">
        <img src="/static/image/video.png" data-action="video">
        <img src="/static/image/justify_left.png" data-action="justify_left">
        <img src="/static/image/justify_center.png" data-action="justify_center">
        <img src="/static/image/justify_right.png" data-action="justify_right">
        <img src="/static/image/indent.png" data-action="indent">
        <img src="/static/image/outdent.png" data-action="outdent">
        <img src="/static/image/bullets.png" data-action="bullets">
        <img src="/static/image/numbers.png" data-action="numbers">
        <img src="/static/image/style.png" data-action="style">
        <img src="/static/image/size.png" data-action="size">
        <img src="/static/image/txt_color.png" data-action="color">
        <img src="/static/image/insert_link.png" data-action="insert_link">
        <img src="/static/image/undo.png" data-action="undo">
        <img src="/static/image/redo.png" data-action="redo">
    </div>
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
</html>



