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
    <link rel="stylesheet" href="/static/css/common.css">
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script src="//api.map.baidu.com/api?type=webgl&v=1.0&ak=0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o"></script>
    <script type="text/javascript" src="/static/js/bmap.js"></script>
    <script type="text/javascript" src="/static/js/step_map.js"></script>
    <title>Photo Viewer</title>
</head>
<style>
    body {
        position: relative;
        margin: 0;
        overflow: hidden;
    }
    i {
        cursor: pointer;
    }
    #mapContainer {
        width:100%;
        height: 100%;
        display:block;
    }
    .img-info-window-container {
        user-select: none;
        overflow: hidden;
        text-align: center;
        position: relative;
    }
    .img-info-window-container img {
        max-width: 100%;
        max-height: 100%;
    }
    .BMap_bubble_content {
        width:100%!important;
        overflow: hidden;
    }
    .BMap_bubble_title {
        text-align: center;
    }
</style>
<body>
<div class="map-wrapper" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>">
    <div id="mapContainer"></div>
</div>
</body>
<script>
    <#include "./session-options.ftl" />
    function return2view() {
        window.location.href = '/?path=' + encodeURI('<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>')
    }
    const distanceLimit = 10
    const pointDataList = []
    const firstPoint = <#if photos??><#assign photoPointTemp = photos[0].getPointMap(CLIENT_POINT_TYPE) />{lng:${photoPointTemp.lng}, lat:${photoPointTemp.lat}}<#else>null</#if>
    console.log(firstPoint)
    function getPointData() {
        <#if photos??>
        const stepIcon = makeIcon({
            url: "/static/image/step.png",
            width: 18,
            height: 27,
            pointX: 9,
            pointY: 26
        })
        const stepIcon0 = makeIcon({
            url: "/static/image/step0.png",
            width: 18,
            height: 27,
            pointX: 9,
            pointY: 26
        })
        const stepIcon1 = makeIcon({
            url: "/static/image/step1.png",
            width: 18,
            height: 27,
            pointX: 9,
            pointY: 26
        })
        let distance = 1000
        <#list photos as p>
        <#assign photoPointTemp = p.getPointMap(CLIENT_POINT_TYPE) />
        <#if p_index gt 0>
        distance = getDistance({lng: pointDataList[${(p_index-1)?c}].longitude,lat:pointDataList[${(p_index-1)?c}].latitude},{lng: ${photoPointTemp.lng},lat: ${photoPointTemp.lat}})
        if (distance < distanceLimit) pointDataList[${(p_index-1)?c}].next = ${p_index?c}
        </#if>
        pointDataList.push({
            <#if p.shootTime??>
            shootTime: '${statics['qinyoyo.utils.DateUtil'].date2String(p.shootTime,'yyyy-MM-dd HH:mm')}',
            </#if>
            src: '/.thumb${fileUrl(p)}?click=${p.lastModified?c}',
            address: '${p.formattedAddress(false)}',
            <#if p_index gt 0>
            prev: (distance < distanceLimit ? ${(p_index-1)?c} : -1),
            <#else>
            prev: -1,
            </#if>
            next: -1,
            longitude: ${photoPointTemp.lng}, latitude: ${photoPointTemp.lat},
            <#if p.orientation?? && p.orientation gt 1 && !sessionOptions.supportOrientation>
            className: 'orientation-${p.orientation}',
            </#if>
            <#if p_index gt 0>
            marker: (distance < distanceLimit ? pointDataList[${(p_index-1)?c}].marker: placeMarker({lng:${photoPointTemp.lng}, lat:${photoPointTemp.lat}}, {icon: stepIcon<#if p_index==0>0<#elseif !p_has_next>1</#if>}))
            <#else>
            marker: placeMarker({lng:${photoPointTemp.lng}, lat:${photoPointTemp.lat}}, {icon: stepIcon<#if p_index==0>0<#elseif !p_has_next>1</#if>})
            </#if>
        })
        </#list>
        </#if>
        return pointDataList
    }
</script>
</html>



