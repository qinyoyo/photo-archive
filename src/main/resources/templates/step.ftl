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
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/bd_wgs84.js"></script>
    <script src="//api.map.baidu.com/api?type=webgl&v=1.0&ak=0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o"></script>
    <script type="text/javascript" src="/static/js/bmap.js"></script>
    <title>Photo Viewer</title>
</head>
<script>
    <#include "./session-options.ftl" />
    function return2view() {
        window.location.href = '/?path=' + encodeURI('<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>')
    }
</script>
<script type="text/javascript" src="/static/js/step.js"></script>
<style>
    body {
        max-width: 1080px;
    }
    i {
        cursor: pointer;
    }
    #mapContainer {
        width:100%;
        height: 100%;
    }
    .selected {
        background-color: #99bcf1;
    }
    .selected img {
        opacity: 0.5;
    }
    .img-on-the-map {
        position: fixed;
        z-index: 10000;
        background: #fff;
    }
    .img-on-the-map img {
        width:100%;
        height:100%;
    }
</style>
<#assign path = '' />
<body>
<div id="app" style="display: none" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>">
    <div class="folder-head no-wrap" >
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
        <div class="folder-head__right">
            <i class="fa fa fa-map-marker" onclick="showMap()"></i>
            <i class="fa fa-close" style="margin-left: 10px;" onclick="return2view()"></i>
        </div>
    </div>
    <#if subDirectories??>
        <div class="folder-list no-wrap" >
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
        <div class="photo-list grid-box" data-size="${photos?size?c}">
            <#list photos as p>
                <div class="photo-item grid-cell selected">
                    <img<#if !notLoadImage??> src="/.thumb${fileUrl(p)}?click=${p.lastModified?c}" alt="${p.fileName}" onload="adjustSize(this)"</#if> class="<#if p.orientation?? && p.orientation gt 1 && !sessionOptions.supportOrientation>orientation-${p.orientation}</#if>"
                            <@photoAttributes p /> />
                    <#if !sessionOptions.favoriteFilter>
                        <i class="fa fa-heart img-favorite-state"></i>
                    </#if>
                </div>
            </#list>
        </div>
    </#if>
</div>
<div class="map-wrapper" >
    <div id="mapContainer"></div>
</div>
<div class="img-on-the-map" style="display:none">
    <img id="img-on-the-map"></img>
    <i class="fa fa-close" style="position:absolute; left:0; top:0"></i>
</div>
</body>
</html>



