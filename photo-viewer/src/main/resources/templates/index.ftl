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
    <script type="text/javascript" src="static/js/folder.js"></script>
    <title>Photo viewer</title>
</head>
<body>
<div id="app">
    <div class="folder-head" >
        <i class = "fa fa-search"></i>
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
        <div class="photo-list grid-box">
            <#list photos as p>
                <div class="photo-item grid-cell">
                    <div>
                    <#if p.subFolder?? && p.subFolder!=''>
                        <img src = "${p.subFolder?replace('\\','/')+'/'+p.fileName}" class="gird-cell-img" alt="${p.fileName}" />
                    <#else>
                        <img src = "${p.fileName}" class="gird-cell-img" alt="${p.fileName}" />
                    </#if>
                    </div>
                </div>
            </#list>
        </div>
    </#if>
</div>
</body>
</html>



