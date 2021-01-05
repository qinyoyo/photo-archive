<#function deletePrefix n>
    <#if n?index_of('.delete'+ separator)==0>
        <#return n?substring(8)?replace(separator,'/')>
    <#else>
        <#return n?replace(separator,'/')>
    </#if>
</#function>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <link rel="stylesheet" href="static/css/pv.css">
    <link rel="stylesheet" href="static/font-awesome-4.7.0/css/font-awesome.min.css">
    <script type="text/javascript" src="static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="static/js/transform_image.js"></script>
    <title>Photo viewer</title>
</head>
<style>
    .delete-buttons {
        text-align: center;
    }
    button {
        margin: 5px 0;
        color:#ff6060;
    }
    .button-left {
        float: left;
    }
    .button-right {
        float:right;
    }
    .grid-cell {
        height:150px;
    }
</style>
<script>
    function adjustSize(img) {
        let w = img.parentNode.clientWidth
        let iw = img.naturalWidth, ih = img.naturalHeight
        if (iw<=w) img.parentNode.style.height = Math.min(w,ih) + 'px'
        else img.parentNode.style.height = Math.min(w, ih*w/iw) + 'px';
    }
</script>
<body>
<div id="app">
    <#if sames??>
        <div class="grid-box3">
            <#list sames as p>
                <div class="photo-item grid-cell delete-index-${p?index}">
                    <img src = "/.thumb/${deletePrefix(p.same1)}" title="${p.title1}" class="gird-cell-img img-index-${2*p?index}" alt="${p.same1}" onload="adjustSize(this)"/>
                    <div class="photo-info info-${p?index}" style="display:none">${p.title1}</div>
                </div>
                <div class="grid-cell delete-buttons delete-index-${p?index}" style="height:90px">
                    <div>
                        <button type="button"  class="button-info hide" style="color:#202122" data-index="${p?index}"><i class="fa fa-info"></i></button>
                    </div>
                    <div>
                        <button type="button"  class="button-left delete-file" data-index="${p?index}" data-file="${p.same1}"><i class="fa fa-close"></i></button>
                        <button type="button"  class="button-right delete-file" data-index="${p?index}" data-file="${p.same2}"><i class="fa fa-close"></i></button>
                    </div>
                    <div>
                        <button type="button"  style="color:#202122" class="delete-file" data-index="${p?index}" data-file="${p.same1} <-> ${p.same2}"><i class="fa fa-check"></i></button>
                    </div>
                </div>
                <div class="photo-item grid-cell delete-index-${p?index}">
                    <img src = "/.thumb/${deletePrefix(p.same2)}" title="${p.title2}" class="gird-cell-img img-index-${2*p?index+1}" alt="${p.same2}" onload="adjustSize(this)"/>
                    <div class="photo-info info-${p?index}" style="display:none">${p.title2}</div>
                </div>
            </#list>
        </div>
    </#if>
</div>
<script>
    const Ajax={
        get: function(url, fn) {
            // XMLHttpRequest对象用于在后台与服务器交换数据
            var xhr = new XMLHttpRequest();
            xhr.open('GET', url, true);
            xhr.onreadystatechange = function() {
                // readyState == 4说明请求已完成
                if (xhr.readyState == 4 && xhr.status == 200 || xhr.status == 304) {
                    // 从服务器获得数据
                    fn.call(this, xhr.responseText);
                }
            };
            xhr.send();
        },
        // datat应为'a=a1&b=b1'这种字符串格式，在jq里如果data为对象会自动将对象转成这种字符串格式
        post: function (url, data, fn) {
            var xhr = new XMLHttpRequest();
            xhr.open("POST", url, true);
            // 添加http头，发送信息至服务器时内容编码类型
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.onreadystatechange = function() {
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    fn.call(this, xhr.responseText);
                }
            };
            xhr.send(data);
        }
    }
    window.onload=function(){
        document.querySelectorAll('.delete-file').forEach(function(img) {
            const src = img.getAttribute('data-file')
            const index = parseInt(img.getAttribute('data-index'))
            img.onclick=function (event){
                event.stopPropagation()
                let url = src.indexOf(" <-> ")>0 ? '/save-file?path=' : '/delete-file?path='
                Ajax.get(url+encodeURI(src), function(responseText) {
                    if ("ok"==responseText){
                        document.querySelectorAll('.delete-index-'+index).forEach(function (e){
                            e.remove()
                        })
                    }
                })
            }
        });
        document.querySelectorAll('.button-info').forEach(function(btn) {
            const index = parseInt(btn.getAttribute('data-index'))
            btn.onclick=function (event){
                if (btn.className.indexOf('hide')>=0) {
                    btn.className = 'button-info'
                    document.querySelectorAll('.info-'+index).forEach(function(e){
                        e.style.display = ''
                    });
                } else {
                    btn.className = 'button-info hide'
                    document.querySelectorAll('.info-'+index).forEach(function(e){
                        e.style.display = 'none'
                    });
                }
                event.stopPropagation()
            }
        });
        document.querySelectorAll('.delete-file').forEach(function(img) {
            const src = img.getAttribute('data-file')
            const index = parseInt(img.getAttribute('data-index'))
            img.onclick=function (event){
                event.stopPropagation()
                let url = src.indexOf(" <-> ")>0 ? '/save-file?path=' : '/delete-file?path='
                Ajax.get(url+encodeURI(src), function(responseText) {
                    if ("ok"==responseText){
                        document.querySelectorAll('.delete-index-'+index).forEach(function (e){
                            e.remove()
                        })
                    }
                })
            }
        });
        TransformImage('.gird-cell-img')
    }
</script>
</body>
</html>



