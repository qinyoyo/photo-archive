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
        let   realSizeScale = 1
        const container = img.parentNode
        const waitingIcon = container.querySelector('.waiting-icon')

        img._isReady = false

        // adjust rotate to 90x, swap width, height of image and client
        const rotate90 = function() {
            let b90 = Math.round(img.rotateZ / 90)
            return Math.trunc(b90 / 2) * 2 != b90
        }
        img._calcSize = function() {
            if (rotate90()) {
                imageW = img.naturalHeight
                imageH = img.naturalWidth
            } else {
                imageW = img.naturalWidth
                imageH = img.naturalHeight
            }
            clientW = Math.trunc(imageW * img.scaleX / realSizeScale)
            clientH = Math.trunc(imageH * img.scaleY / realSizeScale)
        }
        const translateLimit = function () {
            let limit = {}
            let clientLeft = Math.trunc((clientW - pageW) / 2)
            let clientTop = Math.trunc((clientH - pageH) / 2)
            if (clientLeft < 0) limit.x = 0; else limit.x = clientLeft;
            if (clientTop < 0) limit.y = 0; else limit.y = clientTop;
            return limit
        }

        const pageAxis2ImageAxis = function (point) {
            let clientLeft = Math.trunc((clientW - pageW) / 2 - img.translateX),
                clientTop = Math.trunc((clientH - pageH) / 2 - img.translateY);
            return {
                x: Math.trunc((clientLeft + point.x) * realSizeScale / img.scaleX),
                y: Math.trunc((clientTop + point.y) * realSizeScale / img.scaleY)
            }
        }
        const imageAxis2PageAxis = function (point) {
            let clientLeft = (clientW - pageW) / 2 - img.translateX,
                clientTop = (clientH - pageH) / 2 - img.translateY;
            return {
                x: Math.trunc(point.x * img.scaleX / realSizeScale - clientLeft),
                y: Math.trunc(point.y * img.scaleY / realSizeScale - clientTop)
            }
        }
        const translate = function(p) {
            let limit = translateLimit(img)
            if (p.x < -limit.x) p.x = -limit.x;
            else if (p.x > limit.x) p.x = limit.x;
            if (p.y < -limit.y) p.y = -limit.y;
            else if (p.y > limit.y) p.y = limit.y;
            img.translateX = p.x
            img.translateY = p.y
        }
        img._move = function(p) {
            translate({
                x: p.x + img.translateX,
                y: p.y + img.translateY
            })
        }
        const moveTo = function(imageAxis, page) {
            let clientAxis = imageAxis2PageAxis(imageAxis)
            translate({
                x: page.x - clientAxis.x,
                y: page.y - clientAxis.y
            })
        }
        img._rotate = function(angle,refPoint) {
            let image = refPoint ? pageAxis2ImageAxis(refPoint) : null
            img.rotateZ += angle;
            if (refPoint) {
                moveTo(image, refPoint)
            }
        }
        img._roundRotate = function () {
            let b90 = Math.round(img.rotateZ / 90)
            let angle = b90 * 90
            if (angle!=img.rotateZ) img.rotateZ = angle
        }
        const minScale = function() {
            if (imageW<=pageW && imageH<=pageH) return 1
            else if (rotate90()) {
                let w = Math.trunc(imageW / realSizeScale)
                let h = Math.trunc(imageH / realSizeScale)
                let ms = Math.min(pageW/w, pageH/h)
                return ms
            } else return 1
        }
        img._scale = function(scale, refPoint) {
            let image = refPoint ? pageAxis2ImageAxis(refPoint) : null
            let ms = minScale()
            if (scale< ms) scale=ms
            else if (scale > realSizeScale) scale = realSizeScale
            img.scaleX = img.scaleY = scale
            clientW = Math.trunc(imageW * img.scaleX / realSizeScale)
            clientH = Math.trunc(imageH * img.scaleY / realSizeScale)
            if (refPoint) {
                moveTo(image, refPoint)
            }
        }
        img._isFitClient = function() {
            return clientH<=pageH && clientW<=pageW
        }
        // scale to fit client, reserve rotate
        img._fitClient = function() {
            img._scale(minScale())
            translate({ x:0, y:0 })
        }
        // scale real size
        img._realSize = function(page) {
            img._scale(realSizeScale,page)
        }
        img._changeImage = function(src) {
            waitingIcon.style.display = 'block'
            img._isReady = false
            img.src = src
        }

        img.onload = function () {
            waitingIcon.style.display = 'none'
            img.rotateZ = 0
            img.scaleX = img.scaleY = 1
            img.translateX = img.translateY = 0
            realSizeScale = Math.max(img.naturalWidth / pageW, img.naturalHeight / pageH)
            if (realSizeScale<1) realSizeScale = 1
            img._calcSize()
            img._isReady = true
        }
        Transform(img)
    }
})();