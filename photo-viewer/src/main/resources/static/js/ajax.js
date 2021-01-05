;(function () {
    window.Ajax = {
        get: function (url, callback) {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', url, true);
            xhr.onreadystatechange = function () {
                // readyState == 4说明请求已完成
                if (xhr.readyState == 4 && xhr.status == 200 || xhr.status == 304) {
                    callback.call(this, xhr.responseText);
                }
            };
            xhr.send();
        },
        post: function (url, data, callback) {
            const xhr = new XMLHttpRequest();
            xhr.open("POST", url, true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {
                    callback.call(this, xhr.responseText);
                }
            };
            xhr.send(data);
        }
    }
})();