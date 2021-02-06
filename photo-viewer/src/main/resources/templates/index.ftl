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
    <script type="text/javascript" src="/static/js/full_screen.js"></script>
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <#if !loopPlay??>
    <script type="text/javascript" src="/static/js/folder.js"></script>
    </#if>
    <title>Photo Viewer</title>
</head>
<#if (debug?? && debug) || (canRemove?? && canRemove) || (orientation?? && orientation) || (loopTimer??)>
<script>
    <#if debug?? && debug>
    window.enableDebug = true
    </#if>
    <#if canRemove?? && canRemove>
    window.enableRemove = true
    </#if>
    <#if orientation?? && orientation>
    window.notSupportOrientation = true
    </#if>
    <#if loopTimer??>
    window.loopTimer = ${loopTimer?c}
    </#if>
    <#if loopPlay?? && photos??>
    window.onload=function(){
        macPlayOSBackMusic()
        window.AutoLoopPlayImage(<#if startFrom??>${startFrom?c}<#else>0</#if>)
    }
    </#if>
</script>
</#if>
<#assign path = '' />
<body>
<#if backgroundMusic??>
    <audio class="background-music" src="${backgroundMusic}" style="display:none" autoplay></audio>
</#if>
<div id="app" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>"<#if rangeExif??> data-rangeExif="${rangeExif}"</#if>>
    <#if loopPlay??>
    <#if photos??>
    <div class="auto-play-loop-images photo-list" data-size="${photos?size?c}" style="display:none">
        <#list photos as p>
        <img class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && orientation?? && orientation> orientation-${p.orientation}</#if> img-index-${p?index?c}"
             <@photoAttributes p /> />
        </#list>
    </div>
    <#else>
        没有可循环播放的图像
    </#if>
    <#else>
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
            <i class = "fa fa-search search-item folder-head__item" title="关键词搜索"></i>
            <span style="display: none" class="search-input__wrapper">
            <input type="text" autocomplete="off" placeholder="搜索关键词" class="search-input">
            <i  class="fa fa-times-circle-o search-clear-icon"></i>
            </span>
            <#if !isMobile?? && !htmls?? && htmlEditable?? && htmlEditable>
            <i class="fa fa-edit add-new-step folder-head__item" title="新建游记" data-folder="${path}"></i>
            </#if>
            <i class="fa <#if favoriteFilter?? && favoriteFilter>fa-heart<#else>fa-heart-o</#if> favorite-item folder-head__item" title="只显示收藏图片"></i>
            <i class="fa fa-play folder-head__item" data-folder="${path}" title="循环播放该目录下图片"></i>
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
    <div class="collapse">
    <#if htmls??>
        <div class = "collapse-item<#if !photos?? && !videos?? && !audios??>-expanded</#if> folder-list__item">
            <i class = "fa fa-file-text-o collapse__icon"></i>
            <span>足迹</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content html-list">
        <#list htmls as h>
            <div class="folder-list__item">
                <#if htmlEditable?? && htmlEditable>
                <a href = "/editor?path=${fileUrl(h)}" style="padding-right: 8px;" ><i class="fa fa-edit" title="编辑游记"></i></a>
                </#if>
                <a href = "${fileUrl(h)}" class="html-index-${h?index?c}" title="阅读游记"><#if h.subTitle?? && h.subTitle!=''>${h.subTitle}<#else>${h.fileName}</#if></a>
            </div>
        </#list>
        </div>
    </#if>
    <#if audios??>
        <div class = "collapse-item<#if !photos?? && !videos??>-expanded</#if> folder-list__item">
            <i class = "fa fa-file-audio-o collapse__icon"></i>
            <span>录音</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content audio-list grid-box">
        <#list audios as a>
            <div class="audio-item grid-cell">
                <audio src = "${fileUrl(a)}" class="audio-index-${a?index?c}" controls></audio>
                <span>${a.fileName}</span>
            </div>
        </#list>
        </div>
    </#if>
    <#if videos??>
        <div class = "collapse-item<#if !photos??>-expanded</#if> folder-list__item">
            <i class = "fa fa-file-video-o collapse__icon"></i>
            <span>视频</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content video-list grid-box">
        <#list videos as v>
            <div class="video-item grid-cell">
                <video src = "${fileUrl(v)}"<#if noVideoThumb?? && noVideoThumb> controls<#else> poster="/.thumb${fileUrl(v)}.jpg"</#if> class="video-index-${v?index?c}"></video>
                <span>${v.fileName}</span>
            </div>
        </#list>
        </div>
    </#if>
    <#if photos??>
        <div class = "collapse-item-expanded folder-list__item">
            <i class = "fa fa-file-image-o collapse__icon"></i>
            <span>照片</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content photo-list grid-box" data-size="${photos?size?c}">
            <#list photos as p>
                <div class="photo-item grid-cell">
                    <img src="/.thumb${fileUrl(p)}" alt="${p.fileName}" onload="adjustSize(this)"
                         class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && orientation?? && orientation> orientation-${p.orientation}</#if> img-index-${p?index?c}"
                         <@photoAttributes p /> />
                    <i class="fa fa-heart img-favorite-state"></i>
                </div>
            </#list>
        </div>
    </#if>
    </div>
    <#if needScan?? && needScan>
        <div class="scan-folder" data-folder="${path}">
        <span><i class="fa fa-refresh"></i>强制重新扫描</span>
        </div>
    </#if>
    </#if>
</div>
</body>
</html>



