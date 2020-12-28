
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
    <script type="text/javascript" src="static/js/transform.js"></script>
    <script type="text/javascript" src="static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="static/js/transform_image.js"></script>
    <title>Photo viewer</title>
</head>
<body>
<div id="app">
    <#if sames??>
        <div class="grid-box3">
            <#list sames as p>
                <div class="photo-item grid-cell delete-index-${p?index}">
                    <img src = ".thumb/${p.same1}" class="gird-cell-img img-index-${2*p?index}" alt="${p.same1}" />
                </div>
                <div class="delete-index-${p?index}">
                    <button type="button" style="margin:10px" class="delete-file" data-index="${p?index}" data-file="${p.same1}"><i class="fa fa-close"></i></button>
                    <button type="button" style="margin:10px" class="delete-file" data-index="${p?index}" data-file="${p.same1} <-> ${p.same2}"><i class="fa fa-close"></i>自动</button>
                    <button type="button" style="margin:10px" class="delete-file" data-index="${p?index}" data-file="${p.same2}"><i class="fa fa-close"></i></button>
                </div>
                <div class="photo-item grid-cell delete-index-${p?index}">
                    <img src = ".thumb/${p.same2}" class="gird-cell-img img-index-${2*p?index+1}" alt="${p.same2}" />
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
        document.querySelectorAll('.gird-cell-img').forEach(function(img) {
            let src = img.getAttribute('src')
            if (src.indexOf('.thumb/')==0) src = src.substring(7)
            let pos = img.className.indexOf('img-index-')
            const index = (pos>=0 ? parseInt(img.className.substring(pos+10)) : 0)
            img.onclick=function (event){
                event.stopPropagation()
                addImageDialog(src, index == NaN ? 0 : index)
            }
        });
        document.querySelectorAll('.delete-file').forEach(function(img) {
            const src = img.getAttribute('data-file')
            const index = parseInt(img.getAttribute('data-index'))
            img.onclick=function (event){
                event.stopPropagation()
                Ajax.get("/deleteSame?path="+encodeURI(src), function(el,responseText) {
                    document.querySelectorAll('.delete-index-'+index).forEach(function(e){ e.remove() })
                })
            }
        });
    }
</script>
</body>
</html>



