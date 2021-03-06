<#include "./photo_attributes.ftl" />
<div id="resource-list" data-path="${currentPath}">
    <div class="folder-head" >
        <div class="folder-head__left">
            <#if !folderOnly?? && resourceByDate??>
                <i class="fa fa-calendar date-item folder-head__item"></i>
                <input type="date" class="date-item-input" value="${resourceByDate}" />
            <#else>
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
            </#if>
        </div>
        <div class="folder-head__right">
            <#if folderOnly??>
                <i class = "fa fa-plus add-folder folder-head__item" title="增加目录"></i>
            <#else>
            <#if resourceByDate??>
                <i class = "fa fa-folder-open-o folder-picker folder-head__item" title="按目录检索"></i>
            <#else>
                <i class = "fa fa-calendar date-picker folder-head__item" title="按日期检索"></i>
            </#if>
            </#if>
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
    <#if !folderOnly??>
        <#if audios??>
            <div class="audio-list">
                <form id="audio-form">
                    <#list audios as a>
                        <div class="audio-item">
                            <audio src = "${fileUrl(a)}" class="audio-index-${a?index?c}" controls></audio>
                            <div class="grid-cell-label">
                                <input type="radio" name="audio" id="audio-index-${a?index?c}" value="${a.urlPath(currentPath)}" />
                                <label for="audio-index-${a?index?c}">${a.fileName}</label>
                            </div>
                        </div>
                    </#list>
                </form>
            </div>
        </#if>
        <#if videos??>
            <div class="video-list">
                <form id="video-form">
                    <#list videos as v>
                        <div class="video-item">
                            <video src = "${fileUrl(v)}" controls class="video-index-${v?index?c}"></video>
                            <div class="grid-cell-label">
                                <input type="radio" name="video" id="video-index-${v?index?c}" value="${v.urlPath(currentPath)}"/>
                                <label for="video-index-${v?index?c}">${v.fileName}</label>
                            </div>
                        </div>
                    </#list>
                </form>
            </div>
        </#if>
        <#if photos??>
            <form id="photo-form" class="photo-list">
                <div class="grid-box3">
                    <#list photos as p>
                        <div class="photo-item grid-cell">
                            <img src = "/.thumb${fileUrl(p)}?click=${p.lastModified?c}" data-value="${p.urlPath(currentPath)}" alt="${p.fileName}" onload="adjustSize(this)"
                                 class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && !sessionOptions.supportOrientation> orientation-${p.orientation}</#if> img-index-${p?index?c}"
                                    <@photoAttributes p /> />
                            <div class="grid-cell-label">
                                <input type="checkbox" name="photo" id="img-index-${p?index?c}" value="${p?index?c}"/>
                                <label for="img-index-${p?index?c}">${p.fileName}</label>
                            </div>
                        </div>
                    </#list>
                </div>
            </form>
        </#if>
    </#if>
</div>