; (function () {
    function getRealPosition(pos, el) {
        if (!el || !pos || pos==='h' || pos==='v') return pos
        else{
            const transform = el.style.transform || el.style.msTransform || el.style.OTransform || el.style.MozTransform
            if (!transform || transform==='none') return pos
            let mirrorH = (transform.index('rotateY(180deg)') >= 0)
            let mirrorV = (transform.index('rotateX(180deg)') >= 0)
            let p=new RegExp('rotate\\(\\d*(\\.\\d*)?deg\\)')
            let rotateZ = p.test(transform) ? Math.trunc(parseFloat(p.$1)) : 0
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
                }
            }
            return pos
        }
    }
    const cornerSize = 50
    const CornerClick = function (el, options) {
        this.element = typeof el == 'string' ? document.querySelector(el) : el
        this.options = options
        this.onclick = this.element.onclick
        const _this = this
        this.element.onclick = function(e) {
            let pos=''
            if (e.offsetY<=cornerSize && e.offsetX<=cornerSize) pos='lt'
            else if (e.offsetY<=cornerSize && e.offsetX>=this.clientWidth - cornerSize) pos='rt'
            else if (e.offsetX < cornerSize && e.offsetY>this.clientHeight - cornerSize) pos='lb'
            else if (e.offsetX>this.clientWidth - cornerSize && e.offsetY>this.clientHeight - cornerSize) pos='rb'
            else if (e.offsetX < cornerSize || e.offsetX > this.clientWidth - cornerSize) pos = 'h'
            else if (e.offsetY < cornerSize || e.offsetY > this.clientHeight - cornerSize) pos = 'v'
            if (pos) {
                pos =  getRealPosition(pos, _this.element)
                if (pos === _this.options.resetPos && typeof _this.options.reset === 'function') _this.options.reset(_this.element)
                else if (pos=='lb' && typeof _this.options.actionLB=='function') _this.options.actionLB(e)
                else if (pos=='rb' && typeof _this.options.actionRB=='function') _this.options.actionRB(e)
                else if (pos=='lt' && typeof _this.options.actionLT=='function') _this.options.actionLT(e)
                else if (pos=='rt' && typeof _this.options.actionRT=='function') _this.options.actionRT(e)
                else if (pos=='h'  && typeof _this.options.actionH=='function') _this.options.actionH(e)
                else if (pos=='v' &&  typeof _this.options.actionV=='function') _this.options.actionV(e)
            } else {
                _this.onclick(e)
            }
        }
    }

    CornerClick.prototype = {
        destroy: function() {
            if (this.element) {
                if (typeof this.options.reset === 'function') this.options.reset(this.element)
                this.element.onclick = this.onclick
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