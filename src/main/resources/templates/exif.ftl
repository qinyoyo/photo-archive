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
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <script type="text/javascript" src="/static/js/bd_wgs84.js"></script>
<#--
    <script src="https://webapi.amap.com/maps?v=1.4.15&key=c87baf255d37dae4db646bfd3cf5bd0c&plugin=AMap.Geocoder"></script>
    <script type="text/javascript" src="/static/js/amap.js"></script>
    -->
    <script src="//api.map.baidu.com/api?type=webgl&v=1.0&ak=0G9lIXB6bpnSqgLv0QpieBnGMXK6WA6o"></script>
    <script type="text/javascript" src="/static/js/bmap.js"></script>
    <title>Photo Viewer</title>
</head>
<script>
    <#include "./session-options.ftl" />
    const dataKeys = ['data-artist','data-datetimeoriginal','data-model','data-Lensid',
            'data-subjectCode','data-country-primarylocationname','data-country-code','data-province-state','data-city','data-sub-location',
            'data-headline','data-caption-abstract','data-scene','data-orientation','data-rating',
            'data-gpslongitude','data-gpslatitude','data-gpsaltitude','data-gpsdatetime'
        ],
      nameKeys = ['artist','shootTime','model','lens',
          'subjectCode','country','countryCode','province','city','location',
          'headline','subTitle','scene','orientation','rating',
          'longitude','latitude','altitude','gpsDatetime'
        ]
    let selectedDom = null
    function refresh(path) {
        const e = document.getElementById('recursion')
        const curPath = path ? path : document.getElementById('app').getAttribute('data-folder')
        window.location.href = '/exif?path=' + (curPath ? encodeURI(curPath) : '') + '&recursion='+(e.checked ? 'true':'false')
    }
    function selectFile(dom,event) {
        const fileItems = document.querySelectorAll('.file-item')
        if (event.ctrlKey) {
            if (dom.className.indexOf('selected')>=0) {
                removeClass(dom,'selected')
                fileItems.forEach(function(i) {
                    if (i.className.indexOf('selected')>=0) {
                        selectedDom = i
                        return
                    }
                })
            }
            else {
                addClass(dom,'selected')
                selectedDom = dom
            }
        } else if (event.shiftKey) {
            selectedDom = dom
            let start = -1, end = -1, index = -1
            for (let i=0;i<fileItems.length;i++) {
                if (fileItems[i]===dom) index = i;
                if (fileItems[i].className.indexOf('selected')>=0) {
                    if (start==-1) start=i;
                    else end=i;
                }
            }
            if (index<start) {
                for (let i=index;i<start;i++) {
                    addClass(fileItems[i],'selected')
                }
            } else if (index>end) {
                for (let i=end+1;i<=index;i++) {
                    addClass(fileItems[i],'selected')
                }
            }
        } else {
            selectedDom = dom
            fileItems.forEach(function (i) {
                if (i == dom) addClass(i, 'selected')
                else removeClass(i, 'selected')
            })
        }
        let folders = [], files=[]
        document.querySelectorAll('.file-item.selected').forEach(function (i) {
            folders.push(i.getAttribute('data-folder'))
            files.push(i.getAttribute('data-file'))
        })
        document.getElementById('subFolder').value = folders.join(',')
        document.getElementById('fileName').value = files.join(',')
        if (selectedDom) {
            const file = selectedDom.getAttribute('data-file')
            const lastModified = selectedDom.getAttribute('data-lastmodified')
            const curPath = selectedDom.getAttribute('data-folder').replace(/\\/g, '/')
            const thumbDom = document.querySelector('.thumb-image')
            thumbDom.setAttribute('src', '/.thumb/' + curPath + (curPath ? '/' : '') + file
                + (lastModified ? '?click=' + lastModified : ''))
            thumbDom.setAttribute('data-src', '/' + curPath + (curPath ? '/' : '') + file
                + (lastModified ? '?click=' + lastModified : ''))
            thumbDom.setAttribute('title', selectedDom.getAttribute('title'))
            for (let i = 0; i < dataKeys.length; i++) {
                let v = selectedDom.getAttribute(dataKeys[i])
                if (v && nameKeys[i] === 'shootTime') v = v.replace(' ', 'T')
                else if (v && nameKeys[i] === 'gpsDatetime' && v.length == 20) v = v.substring(0, 4) + '-' + v.substring(5, 7) + '-' + v.substring(8, 10) + 'T' + v.substring(11, 19)
                else if (v && nameKeys[i] === 'orientation') {
                    if (v && parseInt(v) > 1 && !sessionOptions.supportOrientation) {
                        document.querySelector('.thumb-image').className = "thumb-image img-index-0 orientation-" + v
                    } else document.querySelector('.thumb-image').className = "thumb-image img-index-0"
                }
                document.getElementById(nameKeys[i]).value = (v ? v : '')
            }
            if (files.length == 1) {
                document.querySelectorAll('form input[type="checkbox"]').forEach(function(e){
                    e.checked = false
                })
            }
            document.getElementById('submit').setAttribute('disabled', 'disabled')
        }
    }

    function changed(dom) {
        document.getElementById('submit').removeAttribute('disabled')
        dom.nextElementSibling.checked = true
        if (dom.id=='country') {
            document.getElementById('countryCode').value
                = dom.options[dom.selectedIndex].getAttribute('data-code')
            document.getElementById('countryCode').nextElementSibling.checked = true
        }
    }
    function countryToggle(dom) {
        document.getElementById('countryCode').nextElementSibling.checked = dom.checked
    }
    function save() {
        const form = document.getElementById('exifForm')
        let url = '/exifSave'
        let data = new FormData(form)
        let value = data.get('gpsDatetime')
        if (value) {
            value=value.substring(0,4)+':'+value.substring(5,7)+':'+value.substring(8,10)+' '+value.substring(11,19)+'Z'
            data.set('gpsDatetime',value)
        }
        Ajax.post(url,data,function(msg) {
            if (msg && msg.indexOf('ok')==0) {
                document.getElementById('submit').setAttribute('disabled','disabled')
                if (selectedDom) for (let i=0;i<dataKeys.length;i++) {
                    let val=data.get(nameKeys[i])
                    selectedDom.setAttribute(dataKeys[i],val?val:'')
                }
                let pp = msg.split(',')
                if (pp.length>1) {
                    const file=selectedDom.getAttribute('data-file')
                    const curPath = selectedDom.getAttribute('data-folder').replace(/\\/g,'/')
                    selectedDom.setAttribute('data-lastmodified',pp[1])
                    document.querySelector('.thumb-image').setAttribute('src','/.thumb/' + curPath + (curPath?'/':'') +file
                        +'?click='+pp[1])
                    document.querySelector('.thumb-image').setAttribute('data-src','/' + curPath + (curPath?'/':'') + file
                        +'?click='+pp[1])
                }

            } else toast(msg)
        })
    }

    function showMap() {
        document.querySelector('.map-wrapper').style.display = 'block'
        document.querySelector('#app').style.display = 'none'
        let lon = document.getElementById('longitude').value,
            lat = document.getElementById('latitude').value
        if (lon && lat) {
            setTimeout(function(){
                setMarker({lon:parseFloat(lon),lat:parseFloat(lat)})
            },100)

        }

    }
    function hideMap() {
        document.querySelector('.map-wrapper').style.display = 'none'
        document.querySelector('#app').style.display = 'block'
    }
    function selectAddress() {
        document.getElementById('submit').removeAttribute('disabled')
        if (mapPoint.longitude) {
            let e = document.getElementById('longitude')
            e.value = mapPoint.longitude.toFixed(6)
            e.nextElementSibling.checked=true
        }
        if (mapPoint.latitude) {
            let e = document.getElementById('latitude')
            e.value = mapPoint.latitude.toFixed(6)
            e.nextElementSibling.checked=true
        }
        if (mapPoint.province) {
            let e = document.getElementById('province')
            e.value = mapPoint.province
            e.nextElementSibling.checked=true
        }
        if (mapPoint.city) {
            let e = document.getElementById('city')
            e.value = mapPoint.city
            e.nextElementSibling.checked=true
        }
        if (mapPoint.location) {
            let e = document.getElementById('location')
            e.value = mapPoint.location
            e.nextElementSibling.checked=true
        }
        if (mapPoint.subjectCode) {
            let e = document.getElementById('subjectCode')
            e.value = mapPoint.subjectCode
            e.nextElementSibling.checked=true
        }
        hideMap()
     }
    window.onload=function(){
        document.getElementById('app').style.height = (window.innerHeight - 20) +'px'
        document.querySelectorAll('.folder-item').forEach(function(d) {
            const path = d.getAttribute('data-folder')
            d.onclick=function () {
                refresh(path)
            }
        });

        let point = null
        const fileItems = document.querySelectorAll('.file-item')
        if (fileItems.length>0) fileItems.forEach(function(v) {
            if (!point) {
                let lon = v.getAttribute('data-gpslongitude'),
                    lat = v.getAttribute('data-gpslatitude')
                if (lon && lat) {
                    point = {lon:parseFloat(lon),lat:parseFloat(lat)}
                }
            }
            v.onclick = function(event) {
                selectFile(v,event)
            }
        })
        initMap('mapContainer',point)
        document.getElementById('findAddress').onclick = function() {
            getPoint(document.getElementById('address').value)
        }
        TransformImage('.thumb-image')
    }
</script>
<style>
    body {
        max-width: 1080px;
    }
    .exif-box {
        height: calc(100% - 50px);
    }
    /*竖屏, 如果宽高比最大1:1的话，显示这个内容*/
    @media screen and (max-aspect-ratio: 1/1) {
        .exif-box {
            width: 100%;
        }
        .scroll-items-wrapper {
            max-height: calc(50% - 33px);
            overflow: auto;
        }
        .scroll-from-wrapper {
            padding-bottom: 10px;
        }
    }
    /*横屏, 如果宽高最小为1:1的话，显示这个内容*/
    @media screen and (min-aspect-ratio: 1/1) {
        .exif-box {
            display: grid;
            grid-auto-flow: row;
            grid-template-columns: repeat(2, 1fr);
            grid-gap: 5px;
            align-items: center;
            justify-content: center;
            width: 100%;
        }
        .scroll-items-wrapper, .scroll-from-wrapper {
            height: calc(100% - 66px);
        }
    }
    .file-list {
        user-select: none;
    }
    .file-item.selected {
        background-color: #99bcf1;;
    }
    .thumb-image-wrapper {
        text-align: center;
        vertical-align: middle;
        margin: 10px 0;
    }
    .thumb-image {
        max-height: 200px;
        max-width: 100%;
    }
    .scroll-items {
        max-height:100%;
        overflow: auto;
    }
    form label {
        width:64px;
        text-align: right;
        display: inline-block;
    }
    form input[type="checkbox"] {
        width:16px;
        right: 0px;
        position: absolute;
        margin-top:8px;
    }
    .tag-value {
        width: calc(100% - 100px);
        margin-left: 5px;
        border-width: 1px;
        border-top-style: hidden;
        border-right-style: hidden;
        border-bottom-style: inset;
        border-left-style: hidden;
    }
    input:invalid
    {
        color: red;
    }
    input[type="datetime-local"]::-webkit-clear-button {
        visibility: visible;
    }
    .map-wrapper {
        width:100%;
        height:100%;
        left:0;
        top:0;
        position: fixed;
        z-index: 9999;
        background: #fff;
    }
    i {
        cursor: pointer;
    }
    #mapContainer {
        margin-top:10px;
        width:100%;
        height: calc(100% - 50px);
    }
    #address {
        width: calc(100% - 100px);
        margin:0 5px;
    }
</style>
<#assign path = '' />
<body>
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
            <input type="checkbox" id="recursion"<#if recursion?? && recursion> checked</#if> onclick="refresh()"><label for="recursion">递归</label>
            <i class="fa fa-close" onclick="window.location.href = '/?path=' + encodeURI('<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>')"></i>
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
            <div class="file-list" data-size="${photos?size?c}">
                <#list photos as p>
                <div class="folder-list__item file-item" data-folder="${p.subFolder}" data-file="${p.fileName}" <@photoAttributes p />>
                    <i class = "fa fa-picture-o folder__icon"></i>
                    <span>${p.fileName}</span>
                </div>
                </#list>
            </div>
            </#if>
        </div>
    </div>
    <div class="scroll-from-wrapper no-wrap">
        <div class="scroll-items">
            <div class="thumb-image-wrapper">
                <img class="thumb-image img-index-0"/>
            </div>
            <form id="exifForm" class="tag-editor" onsubmit="return false;">
                <input id="subFolder" name="subFolder" type="hidden">
                <input id="fileName" name="fileName" type="hidden">
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
                <div style="position: relative">
                    <label for="model">设备</label>
                    <input class="tag-value" id="model" name="model" onchange="changed(this)">
                    <input type="checkbox" value="model" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="lens">镜头</label>
                    <input class="tag-value" id="lens" name="lens" onchange="changed(this)">
                    <input type="checkbox" value="lens" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="subjectCode">POI</label>
                    <input class="tag-value" id="subjectCode" name="subjectCode" onchange="changed(this)">
                    <input type="checkbox" value="subjectCode" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="country">国家</label>
                    <select class="tag-value" id="country" name="country" onchange="changed(this)">
                        <option data-code="" value="">未知</option>
                        <#list countries as c>
                            <option data-code="${c[0]}" value="<#if c[0]=='CN'>${c[2]}<#else>${c[1]}</#if>">${c[2]}/${c[1]}</option>
                        </#list>
                    </select>
                    <input type="checkbox" value="country" name="selectedTags" onclick="countryToggle(this)">
                </div>
                <input type="hidden" id="countryCode" name="countryCode">
                <input type="checkbox" value="countryCode" name="selectedTags" style="display:none">
                <div style="position: relative">
                    <label for="province">省/州</label>
                    <input class="tag-value" id="province" name="province"  maxlength="4" onchange="changed(this)">
                    <input type="checkbox" value="province" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="city">城市</label>
                    <input class="tag-value" id="city" name="city" onchange="changed(this)">
                    <input type="checkbox" value="city" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="location">地址</label>
                    <input class="tag-value" id="location" name="location" onchange="changed(this)">
                    <input type="checkbox" value="location" name="selectedTags" >
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
                    <label for="orientation">方向</label>
                    <select class="tag-value" id="orientation" name="orientation" onchange="changed(this)">
                        <option value="">默认</option>
                        <option value="1">水平</option>
                        <option value="2">水平翻转</option>
                        <option value="3">旋转180度</option>
                        <option value="4">垂直翻转</option>
                        <option value="5">水平翻转逆旋转270度</option>
                        <option value="6">逆时针旋转90度</option>
                        <option value="7">水平翻转逆旋转90度</option>
                        <option value="8">逆时针旋转270度</option>
                    </select>
                    <input type="checkbox" value="orientation" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="longitude">经度</label>
                    <input class="tag-value" id="longitude" name="longitude" type="number" min="-180" max="180" step="0.000001" onchange="changed(this)">
                    <input type="checkbox" value="longitude" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="latitude">纬度</label>
                    <input class="tag-value" id="latitude" name="latitude" type="number" min="-90" max="90"  step="0.000001" onchange="changed(this)">
                    <input type="checkbox" value="latitude" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="altitude">海拔</label>
                    <input class="tag-value" id="altitude" name="altitude" type="number" min="-10000" max="8848" step="0.1" onchange="changed(this)">
                    <input type="checkbox" value="altitude" name="selectedTags" >
                </div>
                <div style="position: relative">
                    <label for="gpsDatetime">GPS时间</label>
                    <input class="tag-value" id="gpsDatetime" name="gpsDatetime" type="datetime-local" step="1" onchange="changed(this)">
                    <input type="checkbox" value="gpsDatetime" name="selectedTags" >
                </div>
                <div style="text-align: center">
                    <button id="submit" class="dialog__button" onclick="save()">保存</button>
                    <button class="dialog__button" onclick="showMap()">地图选点</button>
                </div>
            </form>
        </div>
    </div>
    </div>
</div>
<div class="map-wrapper" style="display: none">
    <div>
        <input id="address" >
        <i class="fa fa-search" id="findAddress"></i>
        <i class="fa fa-check" style="margin-left:5px;" onclick="selectAddress()"></i>
        <i class="fa fa-close" style="margin: 0 5px;" onclick="hideMap()"></i>
    </div>
    <div id="mapContainer"></div>
</div>
</body>
</html>



