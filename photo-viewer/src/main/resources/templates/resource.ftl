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
<div id="resource-list">
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
        <div class="collapse-content video-list grid-box">
            <#list videos as v>
                <div class="video-item grid-cell">
                    <video src = "${fileUrl(v)}" controls class="video-index-${v?index}"></video>
                    <span>${v.fileName}</span>
                </div>
            </#list>
        </div>
    </#if>
    <#if photos??>
        <div class="collapse-content photo-list grid-box">
            <#list photos as p>
                <div class="photo-item grid-cell">
                    <img src = "/.thumb${fileUrl(p)}"<#if p.orientation??> data-orientation="${p.orientation}"</#if> title="${p.toString()}"
                         class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && orientation?? && orientation> orientation-${p.orientation}</#if> img-index-${p?index}" alt="${p.fileName}" onload="adjustSize(this)"/>
                </div>
            </#list>
        </div>
    </#if>
</div>



