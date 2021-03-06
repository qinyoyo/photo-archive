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
<#function photoTitle p>
    <#assign r = '' />
    <#if p.shootTime??>
        <#assign r = r + statics['qinyoyo.utils.DateUtil'].date2String(p.shootTime,'yyyy-MM-dd HH:mm:ss') + '\n' />
    <#elseif p.createTime??>
        <#assign r = r + statics['qinyoyo.utils.DateUtil'].date2String(p.createTime,'yyyy-MM-dd HH:mm:ss') + '\n'/>
    </#if>
    <#assign address = p.formattedAddress(false) />
    <#if address?? && address!="" >
        <#if p.longitude?? && p.latitude??>
            <#assign r = r + '&#x2727;' + address + '\n'/>
        <#else>
            <#assign r = r + address + '\n' />
        </#if>
    </#if>
    <#assign r = r + '&#xfeff;'>
    <#if p.model??>
        <#assign r = r + p.model />
        <#if p.lens??>
            <#assign r = r + ' - ' + p.lens />
        </#if>
        <#if p.artist??>
            <#assign r = r + ' by ' + p.artist />
        </#if>
        <#assign r = r + '\n' />
    </#if>
    <#assign r = r + fileUrl(p)?substring(1) + '\n' />
    <#if p.width?? && p.height??>
    <#assign r = r + p.width + 'x' + p.height + '(' + p.fileSize + ')\n' />
    </#if>
    <#return r />
</#function>
<#macro photoAttributes p>
    data-src="${fileUrl(p)?substring(1)}" date-size="${p.fileSize?c}" data-lastModified="${p.lastModified?c}"
    <#if p.orientation??>
        data-orientation="${p.orientation}"
    </#if>
    <#if p.rating??>
        data-rating="${p.rating}"
    </#if>
    <#if p.scene??>
        data-scene="${p.scene}"
    </#if>
    <#if p.width??>
        data-width="${p.width?c}"
    </#if>
    <#if p.height??>
        data-height="${p.height?c}"
    </#if>
    <#if p.shootTime??>
        data-shootTime="${statics['qinyoyo.utils.DateUtil'].date2String(p.shootTime,'yyyy-MM-dd HH:mm:ss.SSS')}"
    </#if>
    <#if p.createTime??>
        data-createTime="${statics['qinyoyo.utils.DateUtil'].date2String(p.createTime,'yyyy-MM-dd HH:mm:ss')}"
    </#if>
    <#if p.headline??>
        data-headline="${p.headline?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.subTitle??>
        data-subTitle="${p.subTitle?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.artist??>
        data-artist="${p.artist?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.model??>
        data-model="${p.model?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.lens??>
        data-lens="${p.lens?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.subjectCode??>
        data-subjectCode="${p.subjectCode?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.country??>
        data-country="${p.country}"
    </#if>
    <#if p.countryCode??>
        data-countryCode="${p.countryCode}"
    </#if>
    <#if p.province??>
        data-province="${p.province?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.city??>
        data-city="${p.city?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if p.location??>
        data-location="${p.location?replace('"','&#34;')?replace("'","&#39;")}"
    </#if>
    <#if CLIENT_POINT_TYPE??>
        <#if p.longitude?? && p.latitude??>
        <#assign photoPointTemp = p.getPointMap(CLIENT_POINT_TYPE) />
        data-longitude="${photoPointTemp.lng}"
        data-latitude="${photoPointTemp.lat}"
        </#if>
    <#else>
        <#if p.longitude??>
        data-longitude="${statics['java.lang.String'].format('%.7f',p.longitude)}"
        </#if>
        <#if p.latitude??>
        data-latitude="${statics['java.lang.String'].format('%.7f',p.latitude)}"
        </#if>
    </#if>
    <#if p.altitude??>
        data-altitude="${statics['java.lang.String'].format('%.1f',p.altitude)}"
    </#if>
    <#if p.gpsDatetime??>
        data-gpsDatetime="${statics['qinyoyo.utils.DateUtil'].date2StringByZoneId(p.gpsDatetime,'yyyy-MM-dd HH:mm:ss','UTC')}"
    </#if>
    title="${photoTitle(p)?replace('"','&#34;')?replace("'","&#39;")}"
</#macro>