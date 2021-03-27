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
    function refresh() {
        const e = document.getElementById('recursion')
        const curPath = document.getElementById('app').getAttribute('data-folder')
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
            if (files.length>1 || files.length==0) {
                document.querySelectorAll('form input[type="checkbox"]').forEach(function(e){
                    e.checked = false
                })
                document.querySelectorAll('.tag-value').forEach(function(e){
                    e.setAttribute('disabled', 'disabled')
                })
            } else {
                document.querySelectorAll('form input[type="checkbox"]').forEach(function(e){
                    e.checked = true
                })
                document.querySelectorAll('.tag-value').forEach(function(e){
                    e.removeAttribute('disabled')
                })
            }
            document.getElementById('submit').setAttribute('disabled', 'disabled')
        }
    }

    function changed(dom) {
        document.getElementById('submit').removeAttribute('disabled')
        //dom.querySelector('+input').checked = true
    }
    function toggleSelection(dom) {
        let e = document.getElementById(dom.value)
        if (dom.checked) e.removeAttribute('disabled')
        else e.setAttribute('disabled', 'disabled')
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

    window.onload=function(){
        if (window.innerWidth < window.innerHeight) {
            removeClass(document.querySelector('.grid-box2'),'grid-box2')
            document.querySelector('.scroll-items-wrapper').style.maxHeight = (window.innerHeight/2 - 33) +'px'
            document.querySelector('.scroll-items-wrapper').style.overflow = 'auto'
        } else document.querySelectorAll('.scroll-items-wrapper, .scroll-from-wrapper').forEach(function(v){
            v.style.height = (window.innerHeight - 66) +'px'
        })
        document.querySelectorAll('.folder-item').forEach(function(d) {
            let path = d.getAttribute('data-folder')
            const url = path ? '/exif?path=' + encodeURI(path) : '/exif'
            d.onclick=function () {
                window.location.href = url
            }
        });
        document.querySelectorAll('.scroll-items-wrapper').forEach(function(v){
            v.style.height = (window.innerHeight - 66) +'px'
        })
        const fileItems = document.querySelectorAll('.file-item')
        if (fileItems.length>0) fileItems.forEach(function(v) {
            v.onclick = function(event) {
                selectFile(v,event)
            }
        })
        TransformImage('.thumb-image')
    }
</script>
<style>
    body {
        max-width: 1080px;
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
        </div>
    </div>
    <div class="grid-box2">
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
                    <input type="checkbox" value="artist" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="shootTime">拍摄时间</label>
                    <input class="tag-value" id="shootTime" name="shootTime" type="datetime-local" step="0.001" onchange="changed(this)">
                    <input type="checkbox" value="shootTime" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="model">设备</label>
                    <input class="tag-value" id="model" name="model" onchange="changed(this)">
                    <input type="checkbox" value="model" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="lens">镜头</label>
                    <input class="tag-value" id="lens" name="lens" onchange="changed(this)">
                    <input type="checkbox" value="lens" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="subjectCode">POI</label>
                    <input class="tag-value" id="subjectCode" name="subjectCode" onchange="changed(this)">
                    <input type="checkbox" value="subjectCode" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="country">国家</label>
                    <input class="tag-value" id="country" name="country" onchange="changed(this)">
                    <input type="checkbox" value="country" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="countryCode">国家代码</label>
                    <input class="tag-value" id="countryCode" name="countryCode" maxlength="2" onchange="changed(this)">
                    <input type="checkbox" value="countryCode" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="province">省/州</label>
                    <input class="tag-value" id="province" name="province"  maxlength="4" onchange="changed(this)">
                    <input type="checkbox" value="province" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="city">城市</label>
                    <input class="tag-value" id="city" name="city" onchange="changed(this)">
                    <input type="checkbox" value="city" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="location">地址</label>
                    <input class="tag-value" id="location" name="location" onchange="changed(this)">
                    <input type="checkbox" value="location" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="headline">标题</label>
                    <input class="tag-value" id="headline" name="headline" onchange="changed(this)">
                    <input type="checkbox" value="headline" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="subTitle">题注</label>
                    <input class="tag-value" id="subTitle" name="subTitle" onchange="changed(this)">
                    <input type="checkbox" value="subTitle" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="scene">场景</label>
                    <input class="tag-value" id="scene" name="scene" onchange="changed(this)">
                    <input type="checkbox" value="scene" name="selectedTags" checked onclick="toggleSelection(this)">
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
                    <input type="checkbox" value="rating" name="selectedTags" checked onclick="toggleSelection(this)">
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
                    <input type="checkbox" value="orientation" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="longitude">经度</label>
                    <input class="tag-value" id="longitude" name="longitude" type="number" min="-180" max="180" step="0.000001" onchange="changed(this)">
                    <input type="checkbox" value="longitude" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="latitude">纬度</label>
                    <input class="tag-value" id="latitude" name="latitude" type="number" min="-90" max="90"  step="0.000001" onchange="changed(this)">
                    <input type="checkbox" value="latitude" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="altitude">海拔</label>
                    <input class="tag-value" id="altitude" name="altitude" type="number" min="-10000" max="8848" step="0.1" onchange="changed(this)">
                    <input type="checkbox" value="altitude" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="position: relative">
                    <label for="gpsDatetime">GPS时间</label>
                    <input class="tag-value" id="gpsDatetime" name="gpsDatetime" type="datetime-local" step="1" onchange="changed(this)">
                    <input type="checkbox" value="gpsDatetime" name="selectedTags" checked onclick="toggleSelection(this)">
                </div>
                <div style="text-align: center">
                    <button id="submit" class="dialog__button" onclick="save()">保存</button>
                </div>
            </form>
        </div>
    </div>
    </div>
</div>
</body>
</html>



