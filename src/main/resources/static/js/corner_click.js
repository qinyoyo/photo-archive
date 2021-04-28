; (function () {
    function transformInitial(img) {
        if (!img || !img.className) return null
        if (img.className.indexOf('orientation-2')>=0) return {mirrorH : true}
        else if (img.className.indexOf('orientation-3')>=0) return {rotateZ : 180}
        else if (img.className.indexOf('orientation-4')>=0) return {mirrorV : true}
        else if (img.className.indexOf('orientation-5')>=0) return {mirrorH : true, rotateZ: 270}
        else if (img.className.indexOf('orientation-6')>=0) return {rotateZ: 90}
        else if (img.className.indexOf('orientation-7')>=0) return {mirrorH : true, rotateZ: 90}
        else if (img.className.indexOf('orientation-8')>=0) return {rotateZ: 270}
        else return null
    }
    function getRealPosition(pos, el) {
        if (!el || !pos || pos==='h' || pos==='v') return pos
        else{
            const transform = el.style.transform || el.style.msTransform || el.style.OTransform || el.style.MozTransform
            if (!transform || transform==='none') return pos
            let mirrorH = (transform.indexOf('rotateY(180deg)') >= 0)
            let mirrorV = (transform.indexOf('rotateX(180deg)') >= 0)
            let p = new RegExp('rotate\\((\\d*(\\.\\d*)?)deg\\)')
            let rotateZ = (p.test(transform) ? Math.trunc(parseFloat(RegExp.$1)) : 0)
            let v = transformInitial(el)
            if (v) {

            }
            if (mirrorV && mirrorH) {
                rotateZ += 180
                mirrorH = mirrorV = false
            }
            rotateZ = rotateZ % 360
            if (rotateZ<0) rotateZ += 360
            if (mirrorH){
                if (pos.indexOf('l')>=0) pos=pos.replace('l','r') 
                else if (pos.indexOf('r')>=0) pos=pos.replace('r','l')
            }
            if (mirrorV){
                if (pos.indexOf('t')>=0) pos=pos.replace('t','b') 
                else if (pos.indexOf('b')>=0) pos=pos.replace('b','t')
            }
            if (rotateZ > 45){
                if (rotateZ < 135){
                    if (pos=='rt') pos='rb'
                    else if (pos=='rb') pos='lb'
                    else if (pos=='lb') pos='lt'
                    else if (pos=='lt') pos='rt'
                    else if (pos=='h') pos='v'
                    else if (pos=='v') pos='h'
                } else if (rotateZ < 225) {
                    if (pos=='lt') pos='rb'
                    else if (pos=='rt') pos='lb'
                    else if (pos=='rb') pos='lt'
                    else if (pos=='lb') pos='rt'
                } else if (rotateZ < 315){
                    if (pos=='lb') pos='rb'
                    else if (pos=='lt') pos='lb'
                    else if (pos=='rt') pos='lt'
                    else if (pos=='rb') pos='rt'
                    else if (pos=='h') pos='v'
                    else if (pos=='v') pos='h'
                }
            }
            return pos
        }
    }
    const CornerClick = function (el, options) {
        this.element = typeof el == 'string' ? document.querySelector(el) : el
        this.options = Object.assign({cornerSize:50} ,options)
        this.clickEvent = this.options.clickEvent
        const _this = this
        this.element.onclick = function(e) {
            let pos=''
            if (e.offsetY<=_this.options.cornerSize && e.offsetX<=_this.options.cornerSize) pos='lt'
            else if (e.offsetY<=_this.options.cornerSize && e.offsetX>=this.clientWidth - _this.options.cornerSize) pos='rt'
            else if (e.offsetX < _this.options.cornerSize && e.offsetY>this.clientHeight - _this.options.cornerSize) pos='lb'
            else if (e.offsetX>this.clientWidth - _this.options.cornerSize && e.offsetY>this.clientHeight - _this.options.cornerSize) pos='rb'
            else if (e.offsetX < _this.options.cornerSize || e.offsetX > this.clientWidth - _this.options.cornerSize) pos = 'h'
            else if (e.offsetY < _this.options.cornerSize || e.offsetY > this.clientHeight - _this.options.cornerSize) pos = 'v'
            if (pos) {
                pos =  getRealPosition(pos, _this.element)
                if (pos === _this.options.resetPos && typeof _this.options.reset === 'function') _this.options.reset.call(_this, e,_this.element)
                else if (pos=='lb' && typeof _this.options.actionLB=='function') _this.options.actionLB.call(_this, e,_this.element)
                else if (pos=='rb' && typeof _this.options.actionRB=='function') _this.options.actionRB.call(_this, e,_this.element)
                else if (pos=='lt' && typeof _this.options.actionLT=='function') _this.options.actionLT.call(_this, e,_this.element)
                else if (pos=='rt' && typeof _this.options.actionRT=='function') _this.options.actionRT.call(_this, e,_this.element)
                else if (pos=='h'  && typeof _this.options.actionH=='function') _this.options.actionH.call(_this, e,_this.element)
                else if (pos=='v' &&  typeof _this.options.actionV=='function') _this.options.actionV.call(_this, e,_this.element)
            } else if (typeof _this.clickEvent === 'function') {
                _this.clickEvent.call(_this, e)
            }
        }
        return this
    }

    CornerClick.prototype = {
        destroy: function() {
            if (this.element) {
                if (typeof this.options.reset === 'function') this.options.reset.call(this,this.element)
                this.element.onclick = null
            }
            return null;
        }
    };

    if (typeof module !== 'undefined' && typeof exports === 'object') {
        module.exports = CornerClick;
    } else {
        window.CornerClick = CornerClick;
    }
})();