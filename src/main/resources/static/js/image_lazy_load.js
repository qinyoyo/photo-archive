;(function () {
    if (document.addEventListener) {
        function DOMContentLoaded() {
            let images = document.querySelectorAll('img')
            if (images.length > 6) {
                let needLoad = []
                images.forEach(function (img) {
                    let src = img.getAttribute('src')
                    if (src && !src.endsWith('.png')) {
                        img.setAttribute('data-src',src)
                        img.setAttribute('src', '/static/image/loading.gif')
                        needLoad.push(img)
                    }
                })

                function showImage() {
                    let notLoad = []
                    const H = window.innerHeight
                    needLoad.forEach(function (img) {
                        const rect = img.getBoundingClientRect()
                        if (rect.top < H && rect.bottom >= 0) {
                            img.setAttribute('src', img.getAttribute('data-src'))
                        } else notLoad.push(img)
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
    }
})();