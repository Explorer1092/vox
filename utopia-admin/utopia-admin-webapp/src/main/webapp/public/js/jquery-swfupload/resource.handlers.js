var fileAllSize = 0;
var fileAllQueue = [];
var fromUploadFileIds =  []; //上传
function cancelQueue(instance) {
    document.getElementById(instance.customSettings.cancelButtonId).disabled = true;
    instance.stopUpload();
    var stats;

    do {
        stats = instance.getStats();
        instance.cancelUpload();
    } while (stats.files_queued !== 0);
}

function preLoad() {
    if (!this.support.loading) {
        alert("你的Flash Player版本过低，请升级9.028以上才能使用。");
        return false;
    }
}
function fileQueued(file) {
    try {
        //防止同名文件重复添加
        for (var i in fileAllQueue) {
            if (fileAllQueue[i] == file.name) {
                this.cancelUpload(file.id);
                return false;
            }
        }

        if(fileAllQueue.length == 5){
            alert("只能上传 " + this.settings.file_upload_limit + " 个文件")
            this.cancelUpload(file.id);
            return false;
        }

        fileAllQueue.push(file.name);

        fileAllSize = fileAllSize + file.size;

        var progress = new FileProgress(file, this.customSettings.progressTarget);
        progress.setStatus("就绪...");
        progress.toggleCancel(true, this);

        $("#fileTotal").text(fileAllQueue.length);
        $("#fileSize").text(formatBytes(fileAllSize));

        $("#step1").css({ width : 1, position: "absolute", overflow: "hidden"});
        $("#step2").show();
    } catch (ex) {
        this.debug(ex);
    }
}

function fileQueueError(file, errorCode, message) {
    try {
        var errorName = '';
        switch (errorCode) {
            case SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED:
                errorName = "只能上传 " + this.settings.file_upload_limit + " 个文件"; //同时
                break;
            case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
                errorName = "选择的文件超过了当前大小限制：" + this.settings.file_size_limit;
                break;
            case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
                errorName = "零大小文件";
                break;
            case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
                errorName = "文件扩展名必需为：" + this.settings.file_types_description + " (" + this.settings.file_types + ")";
                break;
            default:
                errorName = "未知错误";
                break;
        }
        alert(errorName);
    } catch (ex) {
        this.debug(ex);
    }
}

function fileDialogComplete(numFilesSelected, numFilesQueued) {
    try {
        if (numFilesSelected > 0) {
            document.getElementById(this.customSettings.cancelButtonId).disabled = false;
        }
        this.startUpload();
    } catch (ex)  {
        this.debug(ex);
    }
}

function uploadStart(file) {
    try {
        var progress = new FileProgress(file, this.customSettings.progressTarget);
        progress.setStatus("开始上传...");
        progress.toggleCancel(true, this);
    }
    catch (ex) {
    }
    return true;
}

function uploadProgress(file, bytesLoaded, bytesTotal) {
    try {
        var percent = Math.ceil((bytesLoaded / bytesTotal) * 100);

        var progress = new FileProgress(file, this.customSettings.progressTarget);
        progress.setProgress(percent);
        progress.setStatus("上传中...");
    } catch (ex) {
        this.debug(ex);
    }
}

function uploadSuccess(file, serverData) {
    try {
        var data = Object;
        var progress = new FileProgress(file,  this.customSettings.upload_target);
        eval("data=" + serverData);
        if ( data.success ) {
            fromUploadFileIds.push(data.fileId);

            //progress.setComplete();
            progress.setStatus("上传成功.");
            progress.toggleCancel(false);
        } else {
            progress.setCancelled();
            progress.setStatus(data.info);
            progress.setError();
            progress.toggleCancel(false);

            //删除队列个数
            fileAllQueue.splice($.inArray(file.name, fileAllQueue), 1);
            fileAllSize = fileAllSize - file.size;
            $("#fileTotal").text(fileAllQueue.length);
            $("#fileSize").text(formatBytes(fileAllSize));
            $("#btnStart").removeClass("btn_disable").find("span").text("提交");
        }
    } catch (ex) {
        this.debug(ex);
    }
}

function uploadComplete(file, serverData) {
    try {
        if (this.getStats().files_queued > 0) {
            this.startUpload();
        } else {
            if(fromUploadFileIds.length == fileAllQueue.length){
                fromSubmit();
            }
        }
    } catch (ex) {
        this.debug(ex);
    }
}

function uploadError(file, errorCode, message) {
    var progress;
    try {
        switch (errorCode) {
            case SWFUpload.UPLOAD_ERROR.FILE_CANCELLED:
                try {
                    progress = new FileProgress(file,  this.customSettings.upload_target);
                    progress.setCancelled();
                    progress.setStatus("取消上传。");
                    progress.toggleCancel(false);

                    //删除队列个数
                    fileAllQueue.splice($.inArray(file.name, fileAllQueue), 1);
                    fileAllSize = fileAllSize - file.size;
                    $("#fileTotal").text(fileAllQueue.length);
                    $("#fileSize").text(formatBytes(fileAllSize));
                    $("#btnStart").removeClass("btn_disable").find("span").text("提交");
                }
                catch (ex1) {
                    this.debug(ex1);
                }
                break;
            case SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED:
                try {
                    progress = new FileProgress(file,  this.customSettings.upload_target);
                    progress.setCancelled();
                    progress.setStatus("中断上传。");
                    progress.toggleCancel(true);
                }
                catch (ex2) {
                    this.debug(ex2);
                }
            case SWFUpload.UPLOAD_ERROR.UPLOAD_LIMIT_EXCEEDED:
                progress = new FileProgress(file,  this.customSettings.upload_target);
                progress.setCancelled();
                progress.setStatus("上传超过限制。");
                progress.toggleCancel(true);
                break;
            default:
                alert(message);
                break;
        }


    } catch (ex3) {
        this.debug(ex3);
    }
}

/* ******************************************
 *	FileProgress Object
 * ****************************************** */
function formatBytes(bytes){
    var s = ['Byte', 'KB', 'MB', 'GB', 'TB', 'PB'];
    var e = Math.floor(Math.log(bytes) / Math.log(1024));
    return bytes == 0 ? 0 : (bytes / Math.pow(1024, Math.floor(e))).toFixed(2) + " " + s[e];
}
/*
 A simple class for displaying file information and progress
 Note: This is a demonstration only and not part of SWFUpload.
 Note: Some have had problems adapting this class in IE7. It may not be suitable for your application.
 */

// Constructor
// file is a SWFUpload file object
// targetID is the HTML element id attribute that the FileProgress HTML structure will be added to.
// Instantiating a new FileProgress object with an existing file will reuse/update the existing DOM elements
function FileProgress(file, targetID) {
    this.fileProgressID = file.id;

    this.opacity = 100;
    this.height = 0;

    this.fileProgressWrapper = document.getElementById(this.fileProgressID);
    if (!this.fileProgressWrapper) {
        this.fileProgressWrapper = document.createElement("div");
        this.fileProgressWrapper.className = "progressWrapper";
        this.fileProgressWrapper.id = this.fileProgressID;

        this.fileProgressElement = document.createElement("div");
        this.fileProgressElement.className = "progressContainer";

        //new tag bar
        this.fileBarProgressElement = document.createElement("div");
        this.fileBarProgressElement.className = "progressBarBox";

        var progressCancel = document.createElement("div");
        progressCancel.className = "progressCancel";
        progressCancel.appendChild(document.createTextNode(" "));
        progressCancel.innerHTML = "<a href='javascript:void(0);'><i class='icon_new_all delete_u'></i> 删除</a> ";

        var progressText = document.createElement("div");
        progressText.className = "progressName";
        progressText.appendChild(document.createTextNode(file.name));

        var progressBar = document.createElement("div");
        progressBar.className = "progressBarWarp";
        progressBar.innerHTML = "<div class='progressBarSpeed'></div>";

        var progressSize = document.createElement("div");
        progressSize.className = "progressBarSize";
        progressSize.appendChild(document.createTextNode(formatBytes(file.size)));

        var progressStatus = document.createElement("div");
        progressStatus.className = "progressBarStatus";
        progressStatus.innerHTML = "&nbsp;";

        this.fileProgressElement.appendChild(progressCancel);
        this.fileProgressElement.appendChild(progressText);
        this.fileProgressElement.appendChild(progressSize);
        this.fileProgressElement.appendChild(progressStatus);

        //new tag bar
        this.fileProgressElement.appendChild(this.fileBarProgressElement);

        this.fileBarProgressElement.appendChild(progressBar);

        this.fileProgressWrapper.appendChild(this.fileProgressElement);

        document.getElementById(targetID).appendChild(this.fileProgressWrapper);
    } else {
        this.fileProgressElement = this.fileProgressWrapper.firstChild;
        this.reset();
    }

    this.height = this.fileProgressWrapper.offsetHeight;
    this.setTimer(null);
}

FileProgress.prototype.setTimer = function (timer) {
    this.fileProgressElement["FP_TIMER"] = timer;
};
FileProgress.prototype.getTimer = function (timer) {
    return this.fileProgressElement["FP_TIMER"] || null;
};

FileProgress.prototype.reset = function () {
    this.fileProgressElement.className = "progressContainer";

    this.fileProgressElement.childNodes[3].innerHTML = "&nbsp;";
    this.fileProgressElement.childNodes[3].className = "progressBarStatus";

    this.fileProgressElement.childNodes[4].style.display = "block";
    this.fileProgressElement.childNodes[4].className = "progressBarBox";
    this.fileProgressElement.childNodes[4].childNodes[0].childNodes[0].style.width = "100%";

    this.appear();
};

FileProgress.prototype.setProgress = function (percentage) {
    this.fileProgressElement.childNodes[4].style.display = "block";
    this.fileProgressElement.className = "progressContainer green";
    this.fileProgressElement.childNodes[4].className = "progressBarBox";
    this.fileProgressElement.childNodes[4].childNodes[0].childNodes[0].style.width = percentage + "%";

    this.appear();
};
FileProgress.prototype.setComplete = function () {
    this.fileProgressElement.className = "progressContainer blue";
    this.fileProgressElement.childNodes[3].className = "progressBarComplete";
    this.fileProgressElement.childNodes[3].style.width = "";

    var oSelf = this;
    this.setTimer(setTimeout(function () {
        oSelf.disappear();
    }, 200));
};
FileProgress.prototype.setError = function () {
    this.fileProgressElement.className = "progressContainer red";
    this.fileProgressElement.childNodes[3].className = "progressBarError";
    this.fileProgressElement.childNodes[3].style.width = "";

    var oSelf = this;
    this.setTimer(setTimeout(function () {
        oSelf.disappear();
    }, 200));
};
FileProgress.prototype.setCancelled = function () {
    this.fileProgressElement.className = "progressContainer";
    this.fileProgressElement.childNodes[3].className = "progressBarError";
    this.fileProgressElement.childNodes[3].style.width = "";

    var oSelf = this;
    this.setTimer(setTimeout(function () {
        oSelf.disappear();
    }, 200));
};
FileProgress.prototype.setStatus = function (status) {
    this.fileProgressElement.childNodes[3].innerHTML = status;
};

// Show/Hide the cancel button
FileProgress.prototype.toggleCancel = function (show, swfUploadInstance) {
    this.fileProgressElement.childNodes[0].style.visibility = show ? "visible" : "hidden";
    if (swfUploadInstance) {
        var fileID = this.fileProgressID;
        this.fileProgressElement.childNodes[0].onclick = function () {
            swfUploadInstance.cancelUpload(fileID);
            return false;
        };
    }
};

FileProgress.prototype.appear = function () {
    if (this.getTimer() !== null) {
        clearTimeout(this.getTimer());
        this.setTimer(null);
    }

    if (this.fileProgressWrapper.filters) {
        try {
            this.fileProgressWrapper.filters.item("DXImageTransform.Microsoft.Alpha").opacity = 100;
        } catch (e) {
            // If it is not set initially, the browser will throw an error.  This will set it if it is not set yet.
            this.fileProgressWrapper.style.filter = "progid:DXImageTransform.Microsoft.Alpha(opacity=100)";
        }
    } else {
        this.fileProgressWrapper.style.opacity = 1;
    }

    this.fileProgressWrapper.style.height = "";

    this.height = this.fileProgressWrapper.offsetHeight;
    this.opacity = 100;
    this.fileProgressWrapper.style.display = "";

};

// Fades out and clips away the FileProgress box.
FileProgress.prototype.disappear = function () {

    var reduceOpacityBy = 15;
    var reduceHeightBy = 4;
    var rate = 30;	// 15 fps

    if (this.opacity > 0) {
        this.opacity -= reduceOpacityBy;
        if (this.opacity < 0) {
            this.opacity = 0;
        }

        if (this.fileProgressWrapper.filters) {
            try {
                this.fileProgressWrapper.filters.item("DXImageTransform.Microsoft.Alpha").opacity = this.opacity;
            } catch (e) {
                // If it is not set initially, the browser will throw an error.  This will set it if it is not set yet.
                this.fileProgressWrapper.style.filter = "progid:DXImageTransform.Microsoft.Alpha(opacity=" + this.opacity + ")";
            }
        } else {
            this.fileProgressWrapper.style.opacity = this.opacity / 100;
        }
    }

    if (this.height > 0) {
        this.height -= reduceHeightBy;
        if (this.height < 0) {
            this.height = 0;
        }

        this.fileProgressWrapper.style.height = this.height + "px";
    }

    if (this.height > 0 || this.opacity > 0) {
        var oSelf = this;
        this.setTimer(setTimeout(function () {
            oSelf.disappear();
        }, rate));
    } else {
        this.fileProgressWrapper.style.display = "none";
        this.setTimer(null);
    }
};