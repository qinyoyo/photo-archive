function DOMContentLoaded() {
    let images = document.querySelectorAll('img.lazy-load')
    if (images.length > 0) {
        let needLoad = []
        images.forEach(function (img) {
            img.setAttribute('src', '/static/image/loading.gif')
            needLoad.push(img)
        })
        function showImage() {
            let notLoad = []
            const H = window.innerHeight
            needLoad.forEach(function (img) {
                const rect = img.getBoundingClientRect()
                if (rect.top < 2*H && rect.bottom >= -H) { // 加载与预加载
                    img.setAttribute('src', img.getAttribute('data-src'))
                } else {
                    notLoad.push(img)
                }
            })
            if (notLoad.length == 0) window.onscroll = null
            else needLoad = notLoad
        }
        function scrollImage(fn) {
            let timer = null;
            let context = this;
            return function () {
                clearTimeout(timer);
                timer = setTimeout(function () {
                    fn.apply(context);
                }, 500)
            }
        }
        window.onscroll = scrollImage(showImage)
        showImage()
        if (typeof TransformImage === 'function') TransformImage('img')
    }
}
document.addEventListener("DOMContentLoaded", DOMContentLoaded, false);
