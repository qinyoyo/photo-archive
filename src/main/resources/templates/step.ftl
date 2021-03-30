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
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/bd_wgs84.js"></script>
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
    .img-on-the-map {
        position: fixed;
        z-index: 10000;
        background: #fff;
        user-select: none;
    }
    .img-on-the-map img {
        width:100%;
        height:100%;
    }
</style>
<body>
<div class="map-wrapper" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>">
    <div class="img-on-the-map" style="display:none">
        <img id="img-on-the-map"></img>
        <i class="fa fa-ellipsis-h" style="position:absolute; left:0; top:0"></i>
    </div>
    <div id="mapContainer"></div>
</div>
</body>
<script>
    <#include "./session-options.ftl" />
    function return2view() {
        window.location.href = '/?path=' + encodeURI('<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>')
    }
    const pointDataList = []
    function getPointData() {
        <#if photos??>
        const stepIcon = makeIcon({
            url: "/static/image/step.png",
            width: 32,
            height: 32,
            pointX: 15,
            pointY: 28
        })
        const stepIcon0 = makeIcon({
            url: "/static/image/step0.png",
            width: 32,
            height: 32,
            pointX: 15,
            pointY: 28
        })
        const stepIcon1 = makeIcon({
            url: "/static/image/step1.png",
            width: 32,
            height: 32,
            pointX: 15,
            pointY: 28
        })
        <#list photos as p>
        pointDataList.push({
            src: '/.thumb${fileUrl(p)}?click=${p.lastModified?c}',
            address: '${p.formattedAddress(false)}',
            <#if p.shootTime??>
            shootTime: '${statics['qinyoyo.utils.DateUtil'].date2String(p.shootTime,'yyyy-MM-dd HH:mm')}',
            </#if>
            longitude: ${statics['java.lang.String'].format('%.6f',p.longitude)},
            latitude: ${statics['java.lang.String'].format('%.6f',p.latitude)},
            <#if p.orientation?? && p.orientation gt 1 && !sessionOptions.supportOrientation>
            className: 'orientation-${p.orientation}',
            </#if>
            marker: placeMarker({lon:${statics['java.lang.String'].format('%.6f',p.longitude)}, lat:${statics['java.lang.String'].format('%.6f',p.latitude)}}, 'wgs84',{icon: stepIcon<#if p_index==0>0<#elseif !p_has_next>1</#if>})
        })
        <#if p_index==0>
        setTimeout(function(){
            setCenter({
                lon:${statics['java.lang.String'].format('%.6f',p.longitude)},
                lat:${statics['java.lang.String'].format('%.6f',p.latitude)}
            },'wgs84')
        },100)
        </#if>
        </#list>
        </#if>
        return pointDataList
    }
</script>
</html>



