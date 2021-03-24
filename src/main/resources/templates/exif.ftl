<#include "./photo_attributes.ftl" />
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
    <link rel="stylesheet" href="/static/css/transform_image.css">
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <title>Photo Viewer</title>
</head>
<script>
    <#include "./session-options.ftl" />
    function selectFile(dom) {
        const file=dom.getAttribute('data-file')
        const curPath = document.getElementById('app').getAttribute('data-folder')
        document.querySelector('.thumb-image').setAttribute('src','/.thumb/' + curPath + (curPath?'/':'') +file)
        document.querySelector('.scroll-from-wrapper').style.display='block'

        let v=dom.getAttribute('data-artist')
        document.getElementById('artist').value = (v?v:'')

        v=dom.getAttribute('data-datetimeoriginal')
        document.getElementById('shootTime').value = (v?v.replace(' ','T'):'')

        v=dom.getAttribute('data-model')
        document.getElementById('model').value = (v?v:'')

        v=dom.getAttribute('data-Lensid')
        document.getElementById('lens').value = (v?v:'')

        v=dom.getAttribute('data-subjectCode')
        document.getElementById('subjectCode').value = (v?v:'')

        v=dom.getAttribute('data-country-primarylocationname')
        document.getElementById('country').value = (v?v:'')

        v=dom.getAttribute('data-country-code')
        document.getElementById('countryCode').value = (v?v:'')

        v=dom.getAttribute('data-province-state')
        document.getElementById('province').value = (v?v:'')

        v=dom.getAttribute('data-city')
        document.getElementById('city').value = (v?v:'')

        v=dom.getAttribute('data-sub-location')
        document.getElementById('location').value = (v?v:'')


        v=dom.getAttribute('data-headline')
        document.getElementById('headline').value = (v?v:'')

        v=dom.getAttribute('data-caption-abstract')
        document.getElementById('subTitle').value = (v?v:'')

        v=dom.getAttribute('data-scene')
        document.getElementById('scene').value = (v?v:'')

        v=dom.getAttribute('data-rating')
        document.getElementById('rating').value = (v?v:'')

        v=dom.getAttribute('data-orientation')
        document.getElementById('orientation').value = (v?v:'')

        v=dom.getAttribute('data-gpslongitude')
        document.getElementById('longitude').value = (v?v:'')

        v=dom.getAttribute('data-gpslatitude')
        document.getElementById('latitude').value = (v?v:'')

        v=dom.getAttribute('data-gpsaltitude')
        document.getElementById('altitude').value = (v?v:'')

        v=dom.getAttribute('data-gpsdatetime')
        if (v && v.length == 20) v=v.substring(0,4)+'-'+v.substring(5,7)+'-'+v.substring(8,10)+'T'+v.substring(11,19)
        document.getElementById('gpsDatetime').value = (v?v:'')
    }
    window.onload=function(){
        if (window.sessionOptions.mobile) {
            removeClass(document.querySelector('.grid-box2'),'grid-box2')
            document.querySelector('.scroll-items-wrapper').style.maxHeight = (window.innerHeight/2 - 33) +'px'
            document.querySelector('.scroll-items-wrapper').style.overflow = 'auto'
        } else document.querySelectorAll('.scroll-items-wrapper, .scroll-from-wrapper').forEach(function(v){
            v.style.height = (window.innerHeight - 66) +'px'
        })
        document.querySelectorAll('.folder-item').forEach(function(d) {
            let path = d.getAttribute('data-folder')
            const url = path ? '/exif?path=' + encodeURI(path) : '/exif'
            d.onclick=function () {
                window.location.href = url
            }
        });
        document.querySelectorAll('.scroll-items-wrapper').forEach(function(v){
            v.style.height = (window.innerHeight - 66) +'px'
        })
        const fileItems = document.querySelectorAll('.file-item')
        if (fileItems.length>0) fileItems.forEach(function(v) {
            v.onclick = function() {
                selectFile(v)
                fileItems.forEach(function(i) {
                    if (i==v) addClass(i,'selected')
                    else removeClass(i,'selected')
                })
            }
        })
    }
</script>
<style>
    .file-item.selected {
        background-color: #99bcf1;;
    }
    .thumb-image-wrapper {
        text-align: center;
        vertical-align: middle;
        margin: 10px 0;
    }
    .thumb-image {
        height: 200px;
        max-width: 100%;
    }
    .scroll-items {
        max-height:100%;
        overflow: auto;
    }
    label {
        width:64px;
        text-align: right;
        display: inline-block;
    }
    input, select {
        width: calc(100% - 80px);
        margin-left: 5px;
        border-width: 1px;
        border-top-style: hidden;
        border-right-style: hidden;
        border-bottom-style: inset;
        border-left-style: hidden;
    }
    input:invalid
    {
        color: red;
    }
</style>
<#assign path = '' />
<body class="image-editable">
<div id="app" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>">
    <div class="folder-head" >
        <div class="folder-head__left">
            <i class="fa fa-home folder-item folder-head__item" data-folder=""></i>
            <#if pathNames??>
                <#list pathNames as name>
                    <i class="fa fa-angle-right"></i>
                    <#if path==''>
                        <#assign path = name />
                    <#else>
                        <#assign path = path + separator + name />
                    </#if>
                    <span class="folder-item folder-head__item" data-folder="${path}">${name}</span>
                </#list>
            </#if>
        </div>
    </div>
    <div class="grid-box2">
    <div class="scroll-items-wrapper">
        <div class="scroll-items">
            <#if subDirectories??>
            <div class="folder-list" >
                <#list subDirectories as d>
                <div class="folder-list__item folder-item" data-folder="${d.path}">
                    <i class = "fa fa-folder folder__icon"></i>
                    <span>${d.name}</span>
                    <i class="folder-item__arrow fa fa-angle-right" ></i>
                </div>
                </#list>
            </div>
            </#if>
            <#if photos??>
            <div class="file-list" data-size="${photos?size?c}">
                <#list photos as p>
                <div class="folder-list__item file-item" data-file="${p.fileName}" <@photoAttributes p />>
                    <i class = "fa fa-picture-o folder__icon"></i>
                    <span>${p.fileName}</span>
                </div>
                </#list>
            </div>
            </#if>
        </div>
    </div>
    <div class="scroll-from-wrapper" style="display:none">
        <div class="scroll-items">
            <div class="thumb-image-wrapper">
                <img class="thumb-image" />
            </div>
            <form id="exif">
                <div><label for="artist">拍摄者</label><input id="artist" name="artist"></div>
                <div><label for="shootTime">拍摄时间</label><input id="shootTime" name="shootTime" type="datetime-local" step="0.001"></div>
                <div><label for="model">设备</label><input id="model" name="model"></div>
                <div><label for="lens">镜头</label><input id="lens" name="lens"></div>
                <div><label for="subjectCode">POI</label><input id="subjectCode" name="subjectCode"></div>
                <div><label for="country">国家</label><input id="country" name="country"></div>
                <div><label for="countryCode">国家代码</label><input id="countryCode" name="countryCode" maxlength="2"></div>
                <div><label for="province">省/州</label><input id="province" name="province"  maxlength="4"></div>
                <div><label for="city">城市</label><input id="city" name="city"></div>
                <div><label for="location">地址</label><input id="location" name="location"></div>
                <div><label for="headline">标题</label><input id="headline" name="headline"></div>
                <div><label for="subTitle">题注</label><input id="subTitle" name="subTitle"></div>
                <div><label for="scene">场景</label><input id="scene" name="scene"></div>
                <div><label for="rating">星级</label>
                    <select id="rating" name="rating">
                        <option value="">无</option>
                        <option value="1">☆</option>
                        <option value="2">☆☆</option>
                        <option value="3">☆☆☆</option>
                        <option value="4">☆☆☆☆</option>
                        <option value="5">☆☆☆☆☆(收藏)</option>
                    </select>
                </div>
                <div><label for="orientation">方向</label>
                    <select id="orientation" name="orientation">
                        <option value="">默认</option>
                        <option value="1">水平</option>
                        <option value="2">垂直翻转</option>
                        <option value="3">旋转180度</option>
                        <option value="4">水平翻转</option>
                        <option value="5">垂直翻转逆旋转270度</option>
                        <option value="6">逆时针旋转90度</option>
                        <option value="7">垂直翻转逆旋转90度</option>
                        <option value="8">逆时针旋转270度</option>
                    </select>
                </div>
                <div><label for="longitude">经度</label><input id="longitude" name="longitude" type="number" min="-180" max="180" step="0.000001"></div>
                <div><label for="latitude">纬度</label><input id="latitude" name="latitude" type="number" min="-90" max="90"  step="0.000001"></div>
                <div><label for="altitude">海拔</label><input id="altitude" name="altitude" type="number" min="-10000" max="8848" step="0.1"></div>
                <div><label for="gpsDatetime">GPS时间</label><input id="gpsDatetime" name="gpsDatetime" type="datetime-local" step="1"></div>
            </form>
        </div>
    </div>
    </div>
</div>
</body>
</html>



