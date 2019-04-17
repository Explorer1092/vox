(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(factory);
    } else if (typeof exports === 'object') {
        // CommonJS
        module.exports = factory();
    } else {
        // 浏览器全局变量(root 即 window)
        root.skin_tpl = factory();
    }
}(this, function () {
    var tpl = [
        '<div class="upload" style="opacity:0; filter: alpha(opacity=0);">',
            '<div class="" style="opacity:0; filter: alpha(opacity=0);">',
                '<div class="uploader_pick"></div>',
            '</div>',
        '</div>',
        '<div class="perViewList" style="margin-bottom:170px;margin-top:8px;">',
        '</div>'
    ];

    var initEvent = function(instance, container, config){
        //获取容器内元素
        var getEleInContainer = function(className){
            return $('.'+className,container);
        };

        var uploaderList = getEleInContainer('uploader_list');
        instance.on('fileQueued', function(file){
            //uploaderList.append(createItemHTML(file, instance));
            //$('.uploader_pick', container).addClass('webuploader-element-invisible');
            $('.uploader_valid_error', container).html('');

            var $lis = getEleInContainer('perViewList');

            instance.makeThumb(file,function(error,ret){
                if ( error ) {
                    $lis.text('预览错误');
                } else {
                    var perViewHtml = ['<div class="pic-test">',
                        '<div><img src="'+ret+'" style="width:100px; height:100px;" data-file-id="'+file.id+'"></div>',
                        '<i class="close js-close" data-file-id="'+file.id+'"></i>',
                        '</div>'];
                    $lis.append(perViewHtml.join(""));
                }
            });

            config.onCustomFileQueued && typeof config.onCustomFileQueued == 'function' && config.onCustomFileQueued(file);
        })
        .on('uploadSuccess', function(file,result){
            console.log("uploadSuccess~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            config.onCustomUploadSuccess && typeof config.onCustomUploadSuccess == 'function' && config.onCustomUploadSuccess(file,result);
        })
        .on('uploadError', function(file, reason){
            console.log("uploadError~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            $('.uploader_info_success', container).text('');
            $('.uploader_info_error', container).html('上传失败，<a href="javascript:void(0);" class="uploader_reupload">重新上传</a>').show();
            config.onCustomUploadError && typeof config.onCustomUploadError == 'function' && config.onCustomUploadError(file, reason);
        })
        .on('uploadFinished', function(file,result){
            console.log("uploadFinished~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            config.onCustomUploadFinished && typeof config.onCustomUploadFinished == 'function' && config.onCustomUploadFinished(file,result);
        }).on('error', function(type){
            console.log('error type：'+type);
            var errorText = instance.errorTextMap[type] || '文件不合法';
            $.prompt('<p style="text-align:center;">'+errorText+'</p>', {
                buttons: { "确定": false},
                position:{ width: 300}
            });
        });

        container.on('click', '.uploader_reupload', function(){
            $('.uploader_list', container).html('');
            container.find('input[type="file"]').trigger('click');
        });

        //删除
        container.on('click', '.js-close', function(){
            $(this).parent('div.pic-test').remove();
            instance.removeFile( $(this).attr("data-file-id") , true);
        });

        //预览
        container.on("click","img",function(){

            var fileCurrent,filesArray = instance.getFiles();

            for(var i=0; i<filesArray.length;i++){
                if(filesArray[i].id == $(this).attr("data-file-id")){
                    fileCurrent = filesArray[i];
                }
            };

            instance.makeThumb(fileCurrent,function(error,src){
                if ( error ) {
                    $.prompt('<div style="text-align:center;">预览出错</div>', {
                        buttons: { "确定": false},
                        position:{ width: 200}
                    });
                } else {
                    var perViewHtml = ['<img src="'+src+'" style="width:502px;">'];
                    $.prompt(perViewHtml.join(""), {
                        buttons: { "确定": false},
                        position:{ width: 564}
                    });
                }
            });
        });

        //提交作业
        $(document).on("click","#v-handOutImage",function(){
            instance.upload();
            //$('#loadingIamge').show();
            //$("#v-addPhotoSubmitShadow").submit();
        });
    }

    return {
        tpl: tpl.join(''),
        initEvent: initEvent
    }
}));