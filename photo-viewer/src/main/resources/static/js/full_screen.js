;(function () {
    function setFullscreen(element) {
        const el = element instanceof HTMLElement ? element : document.documentElement;
        const rfs = el.requestFullscreen       ||
            el.webkitRequestFullscreen ||
            el.mozRequestFullScreen    ||
            el.msRequestFullscreen;
        if (rfs) {
            rfs.call(el);
        }
    }
    function exitFullscreen(){
        const efs = document.exitFullscreen       ||
            document.webkitExitFullscreen ||
            document.mozCancelFullScreen  ||
            document.msExitFullscreen;
        if (efs) {
            efs.call(document);
        }
    }
    window.handleFullScreen = function (element){
        const fullscreenEnabled = document.fullscreenEnabled       ||
            document.mozFullScreenEnabled    ||
            document.webkitFullscreenEnabled ||
            document.msFullscreenEnabled;
        if (fullscreenEnabled) {
            const fullscreenElement = document.fullscreenElement    ||
                document.mozFullScreenElement ||
                document.webkitFullscreenElement;
            if (fullscreenElement) exitFullscreen()
            else  setFullscreen(element);
        } else {
            console.log('浏览器当前不能全屏');
        }
    }
})();