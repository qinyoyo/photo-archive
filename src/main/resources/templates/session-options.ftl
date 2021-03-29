    window.sessionOptions = {
        debug: <#if sessionOptions.debug>true<#else>false</#if>,
        htmlEditable: <#if sessionOptions.htmlEditable>true<#else>false</#if>,
        favoriteFilter: <#if sessionOptions.favoriteFilter>true<#else>false</#if>,
        loopTimer:  ${sessionOptions.loopTimer?c},
        musicIndex: ${sessionOptions.musicIndex?c},
        unlocked: <#if sessionOptions.unlocked>true<#else>false</#if>,
        playBackMusic: <#if sessionOptions.playBackMusic>true<#else>false</#if>,
        mobile: <#if sessionOptions.mobile>true<#else>false</#if>,
        supportOrientation: <#if sessionOptions.supportOrientation>true<#else>false</#if>
    }
