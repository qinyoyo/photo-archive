/* transform image
 * By qinyoyo
 */
;(function () {
    window.isMobile = function() {
        const ua = navigator.userAgent.toLowerCase()
        const agents = ["android", "iphone", "symbianos", "phone", "mobile"]
        if (agents.some(a => {
            if (ua.indexOf(a) >= 0) return true
        })) return true
        else return false
    }
    window.TransformImage = function (img) {
        const pageW = window.innerWidth, pageH = window.innerHeight
        let   imageW = pageW, imageH = pageH
        let   clientW = pageW, clientH = pageH
        let   maxScale = 1
        const container = img.parentNode
        const waitingIcon = container.querySelector('.waiting-icon')

        img._isReady = false

        // adjust rotate to 90x, swap width, height of image and client
        img._roundRotate = function() {
            let b90 = Math.trunc(img.rotateZ / 90)
            if (Math.trunc(b90 / 2) * 2 != b90) {
                imageW = img.naturalHeight
                imageH = img.naturalWidth
            } else {
                imageW = img.naturalWidth
                imageH = img.naturalHeight
            }
            if ((imageW > imageH && clientW <= clientH) || (imageW < imageH && clientW >= clientH)) {
                let t = clientW
                clientW = clientH
                clientH = t
            }
        }
        img._calcMaxScale = function() {
            maxScale = Math.max(imageW / pageW, imageW / pageH)
            if (maxScale < 1) maxScale = 1
        }
        const resetImageParameter = function () {
            img._roundRotate()
            img._calcMaxScale()
        }
        const translateLimit = function () {
            let limit = {}
            let clientLeft = Math.round((clientW - pageW) / 2)
            let clientTop = Math.round((clientH - pageH) / 2)
            if (clientLeft < 0) limit.x = 0; else limit.x = clientLeft;
            if (clientTop < 0) limit.y = 0; else limit.y = clientTop;
            return limit
        }
        const pageAxis2ImageAxis = function (point) {
            clientW = Math.round(imageW * img.scaleX / maxScale)
            clientH = Math.round(imageH * img.scaleY / maxScale)
            let clientLeft = Math.round((clientW - pageW) / 2 - img.translateX),
                clientTop = Math.round((clientH - pageH) / 2 - img.translateY);
            return {
                x: Math.round((clientLeft + point.x) * maxScale / img.scaleX),
                y: Math.round((clientTop + point.y) * maxScale / img.scaleY)
            }
        }
        img._translate = function(p) {
            let limit = translateLimit(img)
            if (p.x < -limit.x) p.x = -limit.x;
            else if (p.x > limit.x) p.x = limit.x;
            if (p.y < -limit.y) p.y = -limit.y;
            else if (p.y > limit.y) p.y = limit.y;
            img.translateX = p.x
            img.translateY = p.y
        }
        img._rotate = function(angle,refPoint) {
            img.rotateZ += angle;
            if (refPoint) {

            }
        }
        img._scale = function(scale, refPoint) {
            if (scale<1) scale=1
            else if (scale>maxScale) scale=maxScale
            img.scaleX = img.scaleY = scale
            if (refPoint) {

            }
        }
        img._isFitClient = function() {
            return img.scaleX == 1
        }
        // scale to fit client, reserve rotate
        img._fitClient = function() {
            img.scaleX = img.scaleY = 1
            img._translate({x:0, y:0})
            clientW = Math.round(imageW * img.scaleX / maxScale)
            clientH = Math.round(imageH * img.scaleY / maxScale)
        }
        // scale real size
        img._realSize = function(page) {
            let image = pageAxis2ImageAxis(img, page)
            img.scaleX = img.scaleY = maxScale;
            clientW = Math.round(imageW * img.scaleX / maxScale)
            clientH = Math.round(imageH * img.scaleY / maxScale)
            img._translate({
                x: Math.round(imageW / 2 - image.x),
                y: Math.round(imageH / 2 - image.y)
            })
        }
        img._changeImage = function(src) {
            waitingIcon.style.display = 'block'
            img._isReady = false
            img.src = src
        }

        img.onload = function () {
            waitingIcon.style.display = 'none'
            img.rotateZ = 0
            resetImageParameter(img)
            if (isMobile()) {
                img.scaleX = img.scaleY = 1
                clientW = Math.round(imageW * img.scaleX / maxScale)
                clientH = Math.round(imageH * img.scaleY / maxScale)
            }
            img._isReady = true
        }
        Transform(img)
    }
})();