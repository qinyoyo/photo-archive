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
    <link rel="stylesheet" href="/static/css/common.css">
    <link rel="stylesheet" href="/static/css/exif.css">
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/corner_click.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <script src="//api.map.baidu.com/api?type=webgl&v=1.0&ak=0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o"></script>
    <script type="text/javascript" src="/static/js/bmap.js"></script>
    <script type="text/javascript" src="/static/js/step_map.js"></script>
    <title>Photo Viewer</title>
</head>
<script>
    <#include "./session-options.ftl" />
</script>
<script type="text/javascript" src="/static/js/exif.js"></script>
<#assign path = '' />
<body class="exif">
<div id="app" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>">
    <div class="folder-head no-wrap" >
        <div class="folder-head__left">
            <i class="fa fa-home folder-item folder-head__item" data-folder=""></i>
            <#if pathNames??>
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
            <input type="checkbox" id="recursion"<#if recursion?? && recursion> checked</#if> data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>${separator}</#if></#list></#if>"><label for="recursion">递归</label>
            <i class="fa fa-close" style="margin-left: 10px;" onclick="window.location.href = '/?path=' + encodeURI('<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>')"></i>
        </div>
    </div>
    <div class="exif-box">
    <div class="scroll-items-wrapper no-wrap">
        <div class="scroll-items">
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
            <div class="file-list" data-size="${photos?size?c}" tabindex="0">
                <#list photos as p>
                <div class="folder-list__item file-item" data-folder="${p.subFolder}" data-file="${p.fileName}" <@photoAttributes p />>
                    <i class = "fa fa-picture-o folder__icon" title="连续选择"></i>
                    <span>${p.fileName}</span>
                </div>
                </#list>
            </div>
            </#if>
        </div>
    </div>
    <div class="scroll-from-wrapper no-wrap">
        <div class="scroll-items">
            <div class="thumb-image-wrapper" style="display:none">
                <div class="selection-length"></div>
                <img class="thumb-image img-index-0"/>
            </div>
            <form id="exifForm" class="tag-editor" onsubmit="return false;">
                <input id="subFolder" name="subFolder" type="hidden">
                <input id="fileName" name="fileName" type="hidden">
                <input id="type" name="type" type="hidden" value="${CLIENT_POINT_TYPE}">
                <input type="hidden" id="orientation" name="orientation">
                <input type="hidden" id="orientations" name="orientations">
                <input type="checkbox" style="display:none" value="orientation" name="selectedTags" >

                <div style="position: relative">
                    <label for="artist">拍摄者</label>
                    <input class="tag-value" id="artist" name="artist" onchange="changed(this)">
                    <input type="checkbox" value="artist" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="shootTime">拍摄时间</label>
                    <input class="tag-value" id="shootTime" name="shootTime" type="datetime-local" step="0.001" onchange="changed(this)">
                    <input type="checkbox" value="shootTime" name="selectedTags" >
                </div>
<#--                <div style="position: relative">
                    <label for="model">设备</label>
                    <input class="tag-value" id="model" name="model" disabled="disabled" onchange="changed(this)">
&lt;#&ndash;                    <input type="checkbox" value="model" name="selectedTags" >&ndash;&gt;
                </div>
                <div style="position: relative">
                    <label for="lens">镜头</label>
                    <input class="tag-value" id="lens" name="lens" disabled="disabled"  onchange="changed(this)">
&lt;#&ndash;                    <input type="checkbox" value="lens" name="selectedTags" >&ndash;&gt;
                </div>-->
                <div style="position: relative">
                    <label for="rating">星级</label>
                    <select class="tag-value" id="rating" name="rating" onchange="changed(this)">
                        <option value="">无</option>
                        <option value="1">☆</option>
                        <option value="2">☆☆</option>
                        <option value="3">☆☆☆</option>
                        <option value="4">☆☆☆☆</option>
                        <option value="5">☆☆☆☆☆(收藏)</option>
                    </select>
                    <input type="checkbox" value="rating" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="headline">标题</label>
                    <input class="tag-value" id="headline" name="headline" onchange="changed(this)">
                    <input type="checkbox" value="headline" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="subTitle">题注</label>
                    <input class="tag-value" id="subTitle" name="subTitle" onchange="changed(this)">
                    <input type="checkbox" value="subTitle" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="scene">场景</label>
                    <input class="tag-value" id="scene" name="scene" onchange="changed(this)">
                    <input type="checkbox" value="scene" name="selectedTags" >
                </div>
                <div style="position: relative; margin-top:10px">
                    <label for="subjectCode">POI</label>
                    <input class="tag-value" id="subjectCode" name="subjectCode" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="subjectCode" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="country">国家</label>
                    <select class="tag-value" id="country" name="country" onchange="changed(this)">
                        <option data-code="" value="">未知</option>
                        <#list countries as c>
                            <option data-code="${c[0]}" value="<#if c[0]=='CN'>${c[2]}<#else>${c[1]}</#if>">${c[2]}/${c[1]}</option>
                        </#list>
                    </select>
                    <input class="gps-info" type="checkbox" value="country" name="selectedTags" onclick="countryToggle(this)">
                </div>
                <input type="hidden" id="countryCode" name="countryCode">
                <input class="gps-info" type="checkbox" value="countryCode" name="selectedTags" style="display:none">
                <div style="position: relative">
                    <label for="province">省/州</label>
                    <input class="tag-value" id="province" name="province"  maxlength="4" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="province" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="city">城市</label>
                    <input class="tag-value" id="city" name="city" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="city" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="location">地址</label>
                    <input class="tag-value" id="location" name="location" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="location" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="longitude">经度</label>
                    <input class="tag-value" id="longitude" name="longitude" type="number" min="-180" max="180" step="0.0000001" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="longitude" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="latitude">纬度</label>
                    <input class="tag-value" id="latitude" name="latitude" type="number" min="-90" max="90"  step="0.0000001" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="latitude" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="altitude">海拔</label>
                    <input class="tag-value" id="altitude" name="altitude" type="number" min="-10000" max="8848" step="0.1" onchange="changed(this)">
                    <input class="gps-info" type="checkbox" value="altitude" name="selectedTags" >
                </div>
<#--                <div style="position: relative">
                    <label for="gpsDatetime">GPS时间</label>
                    <input class="tag-value" id="gpsDatetime" name="gpsDatetime" type="datetime-local" step="1" onchange="changed(this)">
                    <input type="checkbox" value="gpsDatetime" name="selectedTags" >
                </div>-->
            </form>
            <input style="margin-left:32px" type="checkbox" value="1" id="autoSaveMarkerDrag" title="拖动足迹标识自动同步修改对应的所有图像文件的位置信息" >
            <label for="autoSaveMarkerDrag">拖动修改</label>
            <input style="margin-left:10px" type="checkbox" value="1" id="markerSelection" title="点击地图标识选择对应文件" >
            <label for="markerSelection">点选</label>
            <a style="margin-right:5px;float:right" id="selectAllTags" title="选择/取消选择全部标签" >Gps</a>
            <div style="text-align: center">
                <button id="btnMove" class="exif__button" disabled="disabled" onclick="moveFiles()" title="将选定文件移动到其他目录">移动</button>
                <button id="btnCopy" class="exif__button" disabled="disabled" onclick="copyFields()" title="复制指定的标签">复制</button>
                <button id="btnPaste" class="exif__button" disabled="disabled" onclick="pasteFields()">粘贴</button>
                <button class="exif__button" onclick="showMap()">地图</button>
                <button id="submit" class="exif__button" disabled="disabled" onclick="save()">保存</button>
            </div>
        </div>
    </div>
    </div>
</div>
<div class="map-wrapper" style="display: none">
    <div id="mapContainer"></div>
</div>
<div id="select-resource" style="display:none" class="dialog__wrapper">
    <div class="dialog__content none-select" style="margin-top: 20px;">
        <div class="dialog__title">
            <span class="select-resource-title">选择目录</span>
            <i class="dialog__close-icon fa fa-close" onclick="document.getElementById('select-resource').style.display='none'"></i>
        </div>
        <div class="dialog__body" style="top:10px">
            <div id="select-resource-content" >
            </div>
            <div style="text-align: center">
                <button class="dialog__button resource-selected">移动文件</button>
                <button class="dialog__button" onclick="document.getElementById('select-resource').style.display='none'">取消</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>



