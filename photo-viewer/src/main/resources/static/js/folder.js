const MODEL = '<div class="v-modal" tabindex="0" style="z-index: 6666;"></div>'
const DIALOG = '<div class="dialog__wrapper" style="z-index: 8888;">' +
               '  <div class="dialog" style="margin-top: 0px; top: 0px;">' +
               '    <div class="dialog__body">' +
               '      <img src="SRC" />' +
               '    </div>' +
               '  </div>' +
               '</div>'
window.onload=function(){
    document.querySelectorAll('.folder-item').forEach(function(d) {
        let path = d.getAttribute('data-folder')
        const url = path ? '/?path=' + encodeURI(path) : '/'
        d.onclick=function (){
            window.location.href = url
        }
    });
    document.querySelectorAll('.gird-cell-img').forEach(function(img) {
        let src = img.getAttribute('src')
        if (src.indexOf('.thumb/')==0) src = src.substring(7)
        const html = DIALOG.replace('SRC',src)
        img.onclick=function (){
            window.location.href = src
        }
    });
}