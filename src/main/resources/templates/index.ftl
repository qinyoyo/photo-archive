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
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <#if !loopPlay??>
    <script type="text/javascript" src="/static/js/folder.js"></script>
    </#if>
    <title>Photo Viewer</title>
</head>
<script>
    function onavplay(e) {
      document.querySelectorAll('audio,video').forEach(function(r){
          if (r!==e) r.pause()
      })
    }
    function radioClick() {
        let o = document.getElementById('loginOptions')
        if (o) {
            const g=document.getElementById('login')
            if (g && g.checked) o.style.display='block'
            else o.style.display='none'
        }
        o = document.getElementById('stepOptions')
        if (o) {
            const g=document.getElementById('newStep')
            if (g && g.checked) o.style.display='block'
            else o.style.display='none'
        }
    }
    function openSetting() {
        const f = document.getElementById('favorite')
        if (f) f.checked = <#if sessionOptions.favoriteFilter>true<#else>false</#if>
        const l = document.getElementById('loopTimerValue')
        if (l) l.value = window.sessionOptions.loopTimer + ''
        const b = document.getElementById('backMusic')
        if (b) {
            const e = document.querySelector('audio.background-music')
            if (e) b.checked = !e.paused
            else b.disabled = true
        }
        radioClick()
        document.getElementById('settings').style.display = 'block'
    }
    function closeSettingDialog() {
        document.getElementById('settings').style.display = 'none'
    }

    function doSetting() {
        closeSettingDialog()
        let e = document.getElementById('favorite')
        if (e && e.checked) {
            Ajax.get('/favorite?filter=<#if sessionOptions.favoriteFilter>false<#else>true</#if>', function(txt) {
                if (txt=='ok') {
                    window.location.reload()
                }
            })
            return
        }
        e = document.getElementById('stdout')
        if (e && e.checked) {
            window.location.href = '/stdout'
            return
        }
        e = document.getElementById('shutdown')
        if (e && e.checked) {
            window.location.href = '/shutdown'
            return
        }
        e = document.getElementById('logout')
        if (e && e.checked) {
            Ajax.get('/logout', function(txt) {
                if (txt=='ok') {
                    window.location.reload()
                }
            })
            return
        }
        e = document.getElementById('loopTimer')
        if (e && e.checked) {
            let l = parseInt(document.getElementById('loopTimerValue').value)
            if (l<0) l=1000
            Ajax.get('/loopTimer?value='+l, function(txt) {
                if (txt=='ok') {
                    window.sessionOptions.loopTimer = l
                }
            })
            return
        }
        e = document.getElementById('newStep')
        if (e && e.checked) {
            const path = e.getAttribute('data-folder')
            const name=document.getElementById('stepName').value
            if (!name) {
                message('必须输入游记名')
            }
            window.location.href = '/?path=' + (path ? encodeURI(path) : '') + '&newStep=' + encodeURI(name)
            return
        }
        e = document.getElementById('mapView')
        if (e && e.checked) {
            const path = e.getAttribute('data-folder')
            window.location.href = '/step?path=' + (path ? encodeURI(path) : '')
            return
        }
        e = document.getElementById('exif')
        if (e && e.checked) {
            const path = e.getAttribute('data-folder')
            window.location.href = '/exif?path=' + (path ? encodeURI(path) : '')
            return
        }
        e = document.getElementById('rescan')
        if (e && e.checked) {
            const path = e.getAttribute('data-folder')
            Ajax.get('/scan?path=' + (path ? encodeURI(path) : ''), function(res) {
                if (res=='ok') {
                    toast('已提交后台执行')
                }
            })
            return
        }
        e = document.getElementById('login')
        if (e && e.checked) {
            const password=document.getElementById('password').value
            if (!password) {
                message('必须输入解锁码')
                return
            }
            const url = '/login?password='+password
                        +'&debug=' + (document.getElementById('debug').checked ? 'true' : 'false')
                        +'&htmlEditable=' + (document.getElementById('htmlEditable').checked ? 'true' : 'false')
            Ajax.get(url, function(txt) {
                    if (txt=='ok') {
                        window.location.reload()
                    } else {
                        message('解锁失败')
                    }
                })
            return
        }
    }
    function toggleBackMusic() {
        const e = document.querySelector('audio.background-music')
        if (e) {
            Ajax.get('/playBackMusic?value='+(e.paused ? 'true' : 'false'), function(txt) {
                if (txt=='ok') {
                    if (e.paused) e.play()
                    else if (e) e.pause()
                }
            })
        }
    }
    <#if loopPlay?? && photos??>
    window.onload=function(){
        macPlayOSBackMusic()
        window.AutoLoopPlayImage(<#if startFrom??>${startFrom?c}<#else>0</#if>)
    }
    </#if>
    <#include "./session-options.ftl" />
</script>
<#assign path = '' />
<body class="image-editable">
<#if backgroundMusic??>
    <audio class="background-music" src="${backgroundMusic}" style="display:none"<#if sessionOptions.playBackMusic> autoplay</#if> onplay="onavplay(this)"></audio>
</#if>
<#assign path = '' />
<div id="app" data-folder="<#if pathNames??><#list pathNames as name>${name}<#if name_has_next>/</#if></#list></#if>">
    <#if loopPlay??>
    <#if photos??>
    <div class="auto-play-loop-images photo-list" data-size="${photos?size?c}" style="display:none">
        <#list photos as p>
        <img class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && !sessionOptions.supportOrientation> orientation-${p.orientation}</#if> img-index-${p?index?c}"
             <@photoAttributes p /> />
        </#list>
    </div>
    <#else>
        没有可循环播放的图像
    </#if>
    <#else>
    <div class="folder-head no-wrap">
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
            <i class = "fa fa-search search-item folder-head__item" title="关键词搜索"></i>
            <span style="display: none" class="search-input__wrapper">
            <input type="text" autocomplete="off" placeholder="搜索关键词" class="search-input">
            <i  class="fa fa-times-circle-o search-clear-icon"></i>
            </span>
            <i class="fa fa-cog folder-head__item" title="参数设置" onclick="openSetting()"></i>
            <i <#if sessionOptions.favoriteFilter>style="color:red" </#if>class="fa fa-play folder-head__item" data-folder="${path}" title="循环播放该目录下图片"></i>
        </div>
    </div>
    <#if subDirectories??>
        <div class="folder-list no-wrap" >
            <#list subDirectories as d>
                <div class="folder-list__item folder-item" data-folder="${d.path}">
                    <i class = "fa fa-folder folder__icon"></i>
                    <span>${d.name}</span>
                    <i class="folder-item__arrow fa fa-angle-right" ></i>
                </div>
            </#list>
        </div>
    </#if>
    <div class="collapse no-wrap">
    <#if htmls??>
        <div class = "collapse-item<#if !photos?? && !videos?? && !audios??>-expanded</#if> folder-list__item">
            <i class = "fa fa-file-text-o collapse__icon"></i>
            <span>游记(共 ${htmls?size?c}篇)</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content html-list">
        <#list htmls as h>
            <div class="folder-list__item">
                <#if sessionOptions.htmlEditable>
                <a href = "/${h.subFolder?replace('\\','/')}/editor?html=${h.fileName}" style="padding-right: 8px;" ><i class="fa fa-edit" title="编辑游记"></i></a>
                </#if>
                <a href = "${fileUrl(h)}" class="html-index-${h?index?c}" title="阅读游记"><#if h.subTitle?? && h.subTitle!=''>${h.subTitle}<#else>${h.fileName}</#if></a>
            </div>
        </#list>
        </div>
    </#if>
    <#if audios??>
        <div class = "collapse-item<#if !photos?? && !videos??>-expanded</#if> folder-list__item">
            <i class = "fa fa-file-audio-o collapse__icon"></i>
            <span>录音(共 ${audios?size?c}节)</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content audio-list grid-box">
        <#list audios as a>
            <div class="audio-item grid-cell">
                <audio src = "${fileUrl(a)}" class="audio-index-${a?index?c}" controls onplay="onavplay(this)"></audio>
                <div>${a.fileName}</div>
            </div>
        </#list>
        </div>
    </#if>
    <#if videos??>
        <div class = "collapse-item<#if !photos??>-expanded</#if> folder-list__item">
            <i class = "fa fa-file-video-o collapse__icon"></i>
            <span>视频(共 ${videos?size?c}片段)</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content video-list grid-box">
        <#list videos as v>
            <div class="video-item grid-cell">
                <video src = "${fileUrl(v)}"<#if noVideoThumb?? && noVideoThumb> controls<#else> poster="/.thumb${fileUrl(v)}.jpg"</#if> class="video-index-${v?index?c}"<#if v.width?? && v.height??> data-width="${v.width?c}" data-height="${v.height?c}"</#if> onplay="onavplay(this)"></video>
                <div>${v.fileName}</div>
            </div>
        </#list>
        </div>
    </#if>
    <#if photos??>
        <div class = "collapse-item-expanded folder-list__item">
            <i class = "fa fa-file-image-o collapse__icon"></i>
            <span>照片(共 ${photos?size?c}张)</span>
            <i class="folder-item__arrow fa fa-angle-right" ></i>
            <i class="folder-item__arrow fa fa-angle-down" ></i>
        </div>
        <div class="collapse-content photo-list grid-box" data-size="${photos?size?c}">
            <#list photos as p>
                <div class="photo-item grid-cell">
                    <img<#if !notLoadImage??> src="/.thumb${fileUrl(p)}?click=${p.lastModified?c}" alt="${p.fileName}" onload="adjustSize(this)"</#if> class="gird-cell-img<#if p.orientation?? && p.orientation gt 1 && !sessionOptions.supportOrientation> orientation-${p.orientation}</#if> img-index-${p?index?c}"
                         <@photoAttributes p /> />
                    <#if !sessionOptions.favoriteFilter>
                    <i class="fa fa-heart img-favorite-state"></i>
                    </#if>
                </div>
            </#list>
        </div>
    </#if>
    </div>
    <#if needScan?? && needScan>
        <div class="scan-folder" data-folder="${path}">
        <span><i class="fa fa-refresh"></i>强制重新扫描</span>
        </div>
    </#if>
    </#if>
</div>
<div id="settings" class="dialog__wrapper" data-folder="${path}" style="display: none">
    <div class="dialog__content">
        <div class="dialog__title">
            <span>浏览参数设置</span>
            <i class="dialog__close-icon fa fa-close" onclick="closeSettingDialog()"></i>
        </div>
        <div class="dialog__body" style="min-width:300px">
            <div>
                <input type="checkbox" id="backMusic" onclick="toggleBackMusic()"><label for="backMusic">播放背景音乐</label>
            </div>
            <div>
                <input type="radio" name="action" id="stdout" onclick="radioClick(this)"><label for="stdout">查看后台输出</label>
            </div>
            <div>
                <input type="radio" name="action" id="favorite" checked onclick="radioClick(this)"><label for="favorite"><#if sessionOptions.favoriteFilter>浏览所有照片<#else>只浏览收藏的照片</#if></label>
            </div>
            <div>
                <input type="radio" name="action" id="loopTimer" onclick="radioClick(this)"><label for="loopTimer">循环时长设置为</label>
                <input type="number" min="1000" max="20000" step="250" style="width:80px" id="loopTimerValue"><label for="loopTimerValue">毫秒</label>
            </div>
            <div>
                <input type="radio" name="action" id="mapView" data-folder="${path}" onclick="radioClick(this)"><label for="mapView">地图浏览</label>
            </div>
            <#if sessionOptions.unlocked>
                <#if !htmls?? && sessionOptions.htmlEditable>
                <div>
                    <input type="radio" name="action" id="newStep" data-folder="${path}" onclick="radioClick(this)"><label for="newStep">新建游记</label>
                </div>
                <div id="stepOptions" style="padding-left:20px;display:none">
                    <div><label for="stepName">游记名</label><input type="text" style="width:200px;" id="stepName"></div>
                </div>
                </#if>
                <div>
                    <input type="radio" name="action" id="exif" data-folder="${path}" onclick="radioClick(this)"><label for="exif">exif信息编辑</label>
                </div>
                <div>
                    <input type="radio" name="action" id="rescan" data-folder="${path}" onclick="radioClick(this)"><label for="rescan">重新扫描 ${path}</label>
                </div>
                <div>
                    <input type="radio" name="action" id="logout" onclick="radioClick(this)"><label for="logout">退出编辑状态</label>
                </div>
                <div>
                    <input type="radio" name="action" id="shutdown" onclick="radioClick(this)"><label for="shutdown">关闭后台服务器</label>
                </div>
            <#else>
                <div>
                    <input type="radio" name="action" id="login" onclick="radioClick(this)"><label for="login">解锁，支持编辑</label>
                </div>
                <div id="loginOptions" style="padding-left:20px;display:none">
                    <div><label for="password">解锁密码</label><input type="text" style="width:160px" id="password"></div>
                    <div><input type="checkbox" id="debug"><label for="debug">打开调试</label></div>
                    <div><input type="checkbox" id="htmlEditable"><label for="htmlEditable">允许编辑游记</label></div>
                </div>
            </#if>
            <div style="text-align: center">
                <button class="dialog__button" onclick="doSetting()">确定</button>
                <button class="dialog__button" onclick="closeSettingDialog()">取消</button>
            </div>
        </div>

    </div>
</div>
</body>
</html>



