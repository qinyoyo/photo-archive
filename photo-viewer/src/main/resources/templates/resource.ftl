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
<div id="resource-list" data-path="${currentPath}">
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
        <div class="audio-list">
            <form id="audio-form">
            <#list audios as a>
                <div class="audio-item">
                    <audio src = "${fileUrl(a)}" class="audio-index-${a?index}" controls></audio>
                    <div>
                        <input type="radio" name="audio" id="audio-index-${a?index}" value="${a.urlPath(currentPath)}" />
                        <label for="audio-index-${a?index}">${a.fileName}</label>
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
                    <video src = "${fileUrl(v)}" controls class="video-index-${v?index}"></video>
                    <div>
                        <input type="radio" name="video" id="video-index-${v?index}" value="${v.urlPath(currentPath)}"/>
                        <label for="video-index-${v?index}">${v.fileName}</label>
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
                    <img src = "/.thumb${fileUrl(p)}"<#if p.orientation??> data-orientation="${p.orientation}"</#if> title="${p.toString()}"
                         class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && orientation?? && orientation> orientation-${p.orientation}</#if> img-index-${p?index}" alt="${p.fileName}" onload="adjustSize(this)"/>
                    <div>
                        <input type="checkbox" name="photo" id="img-index-${p?index}" value="${p.urlPath(currentPath)}"/>
                        <label for="img-index-${p?index}">${p.fileName}</label>
                    </div>
                </div>
            </#list>
        </div>
        </form>
    </#if>
</div>



