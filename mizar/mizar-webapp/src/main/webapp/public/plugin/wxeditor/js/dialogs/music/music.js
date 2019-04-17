function Music() {
    this.init();
}
(function () {
    Music.prototype = {
        loadingDivId: 'loadingDiv',
        fileNodeId: 'uploadVocie',
        urlInputId: "uploadUrl",
        musicMaxSize: 10 * 1024 * 1024,
        musicFinalTemp: '',
        init: function () {
            var me = this;
            domUtils.on($G(me.fileNodeId), "change", function () {
                me.doUpload();
            });
        },
        doUpload: function () {
            var me = this;
            var files = $G(me.fileNodeId).files;
            if (files.length > 0) {
                me.validateUpload(files[0]);
            }
        },
        validateUpload: function (file) {
            var me = this;
            if (file.size > me.musicMaxSize) {
                $G('errorMsg').innerHTML = '文件大小不能超过10M';
                return false;
            }
            if (file.type.indexOf('mp3') == -1) {
                $G('errorMsg').innerHTML = '请上传mp3格式文件';
                return false;
            }
            me.upload(file);
        },
        upload: function (file) {
            var me = this;
            var postData = new FormData();
            postData.append('file', file);
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        me.getParentNode(me.loadingDivId).style.display = "none";
                        var response = JSON.parse(xhr.response);
                        if (response.success) {
                            me.callback(response.fileName);
                            $G('errorMsg').innerHTML = '上传成功';
                        } else {
                            $G('errorMsg').innerHTML = response.info ? response.info : '上传失败，请重新上传';
                        }
                    } else {
                        me.getParentNode(me.loadingDivId).style.display = "none";
                        $G('errorMsg').innerHTML = "上传失败";
                    }
                }
            };
            xhr.open('post', me.getParentNode(me.urlInputId).value, true);
            xhr.send(postData);
            debugger;
            me.getParentNode(me.loadingDivId).style.display = "block";
        },
        getParentNode: function (id) {
            return window.parent.document.getElementById(id);
        },
        callback: function (fileUrl) {
            var me = this;
            if (fileUrl) {
                me.musicFinalTemp = '<p><br/></p>' +
                    '<p>' +
                    '<audio controls="controls" preload="none" width="100%">' +
                    '<source src="' + fileUrl + '" type="audio/mp3"/>' +
                    '<embed src="' + fileUrl + '" width="100%" />' +
                    '</audio>' +
                    '</p>' +
                    '<p><br/></p>';
            }
        },
        exec: function () {
            var me = this;
            if (me.musicFinalTemp != "") {
                editor.execCommand('inserthtml', me.musicFinalTemp);
            }
        }
    };
})();



