<#function replaceSpecialChar s>
    <#assign ns = s?replace('[','%5B')?replace(']','%5D') />
    <#return ns />
</#function>
<#function fileUrl h>
    <#if h.subFolder?? && h.subFolder!=''>
        <#return replaceSpecialChar('/'+h.subFolder?replace('\\','/')+'/'+h.fileName) />
    <#else>
        <#return replaceSpecialChar('/'+h.fileName) />
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
    <script type="text/javascript" src="static/js/transform.js"></script>
    <script type="text/javascript" src="static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="static/js/transform_image.js"></script>
    <script type="text/javascript" src="static/js/folder.js"></script>
    <title>Photo viewer</title>
</head>
<body>
<div id="app">
    <div class="folder-head" >
        <div class="folder-head__left">
            <i class="fa fa-home folder-item folder-head__item" data-folder=""></i>
            <#if pathNames??>
                <#assign path = '' />
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
            <i class = "fa fa-search search-item folder-head__item"></i>
            <span style="display: none" class="search-input__wrapper">
            <input type="text" autocomplete="off" placeholder="搜索关键词" class="search-input">
            <i  class="fa fa-times-circle-o search-clear-icon"></i>
            </span>
        </div>
    </div>
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
    <#if htmls??>
        <div class="html-list">
            <#list htmls as h>
                <div class="folder-list__item">
                    <a href = "${fileUrl(h)}" class="html-index-${h?index}" ><#if h.subTitle?? && h.subTitle!=''>${h.subTitle}<#else>${h.fileName}</#if></a>
                </div>
            </#list>
        </div>
    </#if>
    <#if audios??>
        <div class="audio-list grid-box">
            <#list audios as a>
                <div class="audio-item grid-cell">
                    <audio src = "${fileUrl(a)}" class="audio-index-${a?index}" controls></audio>
                    <span>${a.fileName}</span>
                </div>
            </#list>
        </div>
    </#if>
    <#if videos??>
        <div class="video-list grid-box">
            <#list videos as v>
                <div class="audio-item grid-cell">
                    <video src = "${fileUrl(v)}" poster="/.thumb${fileUrl(v)}.jpg" class="video-index-${v?index}" controls></video>
                    <span>${v.fileName}</span>
                </div>
            </#list>
        </div>
    </#if>
    <#if photos??>
        <div class="photo-list grid-box">
            <#list photos as p>
                <div class="photo-item grid-cell">
                    <img src = "/.thumb${fileUrl(p)}" title="${p.toString()}" class="gird-cell-img img-index-${p?index}" alt="${p.fileName}" onload="adjustSize(this)"/>
                </div>
            </#list>
        </div>
    </#if>
</div>
</body>
</html>



