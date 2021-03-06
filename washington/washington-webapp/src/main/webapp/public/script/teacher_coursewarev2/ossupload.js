
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

function get_signature(_serverUrl)
{
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
        expire = parseInt(obj['expire'])
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

function set_upload_param(up, filename, ret,_serverUrl)
{
    if (ret == false)
    {
        ret = get_signature(_serverUrl)
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

// demo
// var uploader = new plupload.Uploader({
// 	runtimes : 'html5,flash,silverlight,html4',
// 	browse_button : 'selectfiles',
//     multi_selection: false,
// 	// container: document.getElementById('container'),
// 	// flash_swf_url : 'lib/plupload-2.1.2/js/Moxie.swf',
// 	// silverlight_xap_url : 'lib/plupload-2.1.2/js/Moxie.xap',
//     url : 'http://oss.aliyuncs.com',
//
//     filters: {
//         mime_types : [ // 限制上传的文件后缀，只允许上传图片和zip,rar文件
//             // { title : "Image files", extensions : "jpg,gif,png,bmp" },
//             { title : "Ppt files", extensions : "ppt,pptx" },
//             { title : "Zip files", extensions : "aplication/zip" },
//             // { title : "Rar files", extensions : "rar" },
//
//         ],
//         max_file_size : '100mb', //最大只能上传100mb的文件
//         prevent_duplicates : true //不允许选取重复文件
//     },
//
// 	init: {
// 		PostInit: function(_this) {
// 		    console.log('_this', _this)
// 			// document.getElementById('ossfile').innerHTML = '';
// 			document.getElementById('postfiles').onclick = function() {
//                 var upFileName = _this.files[0].name;
//                 var index1=upFileName.lastIndexOf(".");
//                 var index2=upFileName.length;
//                 var suffix1=upFileName.substring(index1+1,index2);
// 			    var _serverUrl = '/courseware/contest/getsignature.vpage?ext=' + suffix1;
//                 set_upload_param(uploader, '', false, _serverUrl);
//                 return false;
// 			};
// 		},
//
// 		FilesAdded: function(up, files) {
// 			// plupload.each(files, function(file) {
// 			// 	document.getElementById('ossfile').innerHTML += '<div id="' + file.id + '">' + file.name + ' (' + plupload.formatSize(file.size) + ')<b></b>'
// 			// 	+'<div class="progress"><div class="progress-bar" style="width: 0%"></div></div><div class="delete_video" style="position: absolute;right:-25px;top:10px;cursor: pointer" data-id="file.id">X</div>'
// 			// 	+'</div>';
// 			// });
// 			document.getElementById('selectfiles').style = 'display:none';
// 		},
//
// 		BeforeUpload: function(up, file) {
//             check_object_radio();
//             set_upload_param(up, file.name, true);
//         },
//
// 		UploadProgress: function(up, file) {
// 			var d = document.getElementById(file.id);
// 			d.getElementsByTagName('b')[0].innerHTML = '<span>' + file.percent + "%</span>";
//             var prog = d.getElementsByTagName('div')[0];
// 			var progBar = prog.getElementsByTagName('div')[0]
// 			progBar.style.width= 2*file.percent+'px';
// 			progBar.setAttribute('aria-valuenow', file.percent);
// 		},
//
// 		FileUploaded: function(up, file, info) {
//             if (info.status == 200) {
//                 // video_status = true;
//                 console.log('111', uploader.settings.multipart_params.key);
//                 document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = '上传成功';
//             } else {
//                 document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = info.response;
//             }
// 		},
//
// 		Error: function(up, err) {
//             if (err.code == -600) {
//                 document.getElementById('console').appendChild(document.createTextNode("\n选择的文件太大了,应不超过100mb"));
//             } else if (err.code == -601) {
//                 document.getElementById('console').appendChild(document.createTextNode("\n选择的文件后缀不对，仅支持mp4,webm,ogg,格式"));
//             } else if (err.code == -602) {
//                 document.getElementById('console').appendChild(document.createTextNode("\n这个文件已经上传过一遍了"));
//             } else {
//                 document.getElementById('console').appendChild(document.createTextNode("\n上传失败"));
//             }
// 		}
// 	}
// });
// uploader.init();