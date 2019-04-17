
accessid = ''
accesskey = ''
host = ''
policyBase64 = ''
signature = ''
callbackbody = ''
filename = ''
key = ''
expire = 0
g_object_name = ''
g_object_name_type = ''
_filename = ''
now = timestamp = Date.parse(new Date()) / 1000;
function send_request(_serverUrl)
{
    var xmlhttp = null;
    if (window.XMLHttpRequest)
    {
        xmlhttp=new XMLHttpRequest();
    }
    else if (window.ActiveXObject)
    {
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }

    if (xmlhttp!=null)
    {
        /* '/mobile/file/getsignature.vpage'*/
        serverUrl = _serverUrl;
        xmlhttp.open( "GET", serverUrl, false );
        xmlhttp.send( null );
        return xmlhttp.responseText
    }
    else
    {
        alert("Your browser does not support XMLHTTP.");
    }
};

function check_object_radio() {
    var tt = document.getElementsByName('myradio');
    for (var i = 0; i < tt.length ; i++ )
    {
        if(tt[i].checked)
        {
            g_object_name_type = tt[i].value;
            break;
        }
    }
}

// type 为自定义参数，用于设置当上传图片时，signature接口不设置过期时间，每点击一次获取一次签名(这样可以多个上传共用一个Uploader)
function get_signature(_serverUrl, type)
{
    // var type = type || '';
    //可以判断当前expire是否超过了当前时间,如果超过了当前时间,就重新取一下.3s 做为缓冲
    now = timestamp = Date.parse(new Date()) / 1000;
    if (expire < now + 3)
    {
        body = send_request(_serverUrl);
        var _obj = JSON.stringify(JSON.parse(body).data)
        var obj = eval ("(" + _obj + ")");
        host = obj['host']
        policyBase64 = obj['policy']
        accessid = obj['accessid']
        signature = obj['signature']
        expire = type !== 'image' ? parseInt(obj['expire']) : 0 // 过期时间， 原先默认的是设置parseInt(obj['expire'])，后来增加了一个判断
        callbackbody = obj['callback']
        key = obj['dir']
        _filename = obj['filename']
        _17zyHost = obj['videoHost']
        videoSnapshotHost = obj['videoSnapshotHost']
        return true;
    }
    return false;
};

function random_string(len) {
    len = len || 32;
    var chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
    var maxPos = chars.length;
    var pwd = '';
    for (i = 0; i < len; i++) {
        pwd += chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}

function get_suffix(filename) {
    pos = filename.lastIndexOf('.')
    suffix = ''
    if (pos != -1) {
        suffix = filename.substring(pos)
    }
    return suffix;
}

function calculate_object_name(filename)
{
    suffix = get_suffix(filename);
    g_object_name = key + _filename + suffix;
    return ''
}

function get_uploaded_object_name(filename)
{
    if (g_object_name_type == 'local_name')
    {
        tmp_name = g_object_name
        tmp_name = tmp_name.replace("${filename}", filename);
        return tmp_name
    }
    else if(g_object_name_type == 'random_name')
    {
        return g_object_name
    }
}

// type 参数为自定义参数
function set_upload_param(up, filename, ret,_serverUrl, type)
{
    if (ret == false)
    {
        ret = get_signature(_serverUrl, type)
    }
    g_object_name = key;
    if (filename != '') {
        suffix = get_suffix(filename)
        calculate_object_name(filename)
    }
    new_multipart_params = {
        'key' : g_object_name,
        'policy': policyBase64,
        'OSSAccessKeyId': accessid,
        'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
        //'callback' : callbackbody,
        'signature': signature,
    };

    up.setOption({
        'url': host,
        'multipart_params': new_multipart_params
    });

    up.start();
}


// 阿里云直传附件
function OSSFileUploader(vm) {
    var fileUploader = new plupload.Uploader({
        runtimes: 'html5,flash,silverlight,html4',
        browse_button: 'fileSelect', // 选择按钮
        multi_selection: false,
        url : 'http://oss.aliyuncs.com',
        filters: {
            mime_types : [ // 限制上传的文件后缀
                { title : "files", extensions : "jpeg,jpg,png,doc,docx,ppt,pptx,pdf,zip,rar,mp3,mp4" },
            ],
            max_file_size : '500mb', //最大只能上传500mb的文件
            prevent_duplicates : false //不允许选取重复文件
        },
        init: {
            PostInit: function(_this) {
                document.getElementById('fileUpload').onclick = function() {
                    var upFileName = _this.files[0].name;
                    var index1 = upFileName.lastIndexOf(".");
                    var index2 = upFileName.length;
                    var suffix1 = upFileName.substring(index1 + 1, index2);
                    var _serverUrl = '/opmanager/teacher_resource/getsignature.vpage?ext=' + suffix1;
                    set_upload_param(fileUploader, '', false, _serverUrl);
                    return false;
                };
            },
            // 文件选择成功
            FilesAdded: function(up, files) {
                if (!files.length) return;
                // 触发开始上传
                $('#fileUpload').click();
            },
            // 开始上传
            BeforeUpload: function(up, file) {
                check_object_radio();
                set_upload_param(up, file.name, true);
                vm.uploadFileObj.uploadState = 'start';
            },
            // 上传中
            UploadProgress: function(up, file) {
                vm.uploadFileObj.uploadState = 'progress';
                vm.uploadFileObj.uploadProgress = file.percent + '%';
            },
            // 上传结束
            FileUploaded: function(up, file, info) {
                if (info.status == 200) {
                    vm.uploadFileObj.uploadState = 'end';
                    var fileSrc = window.location.protocol + '//v.17xueba.com/' + fileUploader.settings.multipart_params.key; // 返回的地址
                    vm.editingFileObj.fileUrl = fileSrc;
                } else {
                    vm.showTip(info.response, 'error');
                }
            },
            // 上传出错
            Error: function(up, err) {
                if (err.code == -600) {
                    vm.showTip('文件大小不能超过500M哦~', 'error');
                } else if (err.code == -601) {
                    vm.showTip('仅支持上传jpeg,jpg,png,doc,docx,ppt,pptx,pdf,mp3,mp4哦~', 'error');
                } else if (err.code == -602) {
                    vm.showTip('这个文件已经上传过一遍了哦~', 'error');
                } else {
                    vm.showTip('上传失败~', 'error');
                }
            }
        }
    });
    fileUploader.init();
}

// 阿里云直传图片
function OSSImageUploader(vm) {
    var imageUploader = new plupload.Uploader({
        runtimes: 'html5,flash,silverlight,html4',
        browse_button: 'imageSelect', // 选择按钮
        multi_selection: false,
        url : 'http://oss.aliyuncs.com',
        filters: {
            mime_types : [ // 限制上传的文件后缀
                { title : "image files", extensions : "jpeg,jpg,png" },
            ],
            max_file_size : '10mb', //最大只能上传100mb的文件
            prevent_duplicates : false //不允许选取重复文件
        },
        init: {
            PostInit: function(_this) {
                document.getElementById('imageUpload').onclick = function() {
                    var upFileName = _this.files[0].name;
                    var index1 = upFileName.lastIndexOf(".");
                    var index2 = upFileName.length;
                    var suffix1 = upFileName.substring(index1 + 1, index2);
                    var _serverUrl = '/opmanager/teacher_resource/getsignature.vpage?ext=' + suffix1;
                    set_upload_param(imageUploader, '', false, _serverUrl, 'image');
                    return false;
                };
            },
            // 文件选择成功
            FilesAdded: function(up, files) {
                if (!files.length) return;
                // 触发开始上传
                $('#imageUpload').click();
            },
            // 开始上传
            BeforeUpload: function(up, file) {
                check_object_radio();
                set_upload_param(up, file.name, true);
            },
            // 上传中
            UploadProgress: function(up, file) {
            },
            // 上传结束
            FileUploaded: function(up, file, info) {
                if (info.status == 200) {
                    var imageSrc = window.location.protocol + '//v.17xueba.com/' + imageUploader.settings.multipart_params.key; // 返回的地址
                    vm.$set(vm.detailInfo, vm.uploadImageType, imageSrc);
                } else {
                    vm.showTip(info.response, 'error');
                }
            },
            // 上传出错
            Error: function(up, err) {
                if (err.code == -600) {
                    vm.showTip('图片大小不能超过10M哦~', 'error');
                } else if (err.code == -601) {
                    vm.showTip('仅支持上传jpeg,jpg,png哦~', 'error');
                } else if (err.code == -602) {
                    vm.showTip('这个文件已经上传过一遍了哦~', 'error');
                } else {
                    vm.showTip('上传失败~', 'error');
                }
            }
        }
    });
    imageUploader.init();
}