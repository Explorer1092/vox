(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery', 'webuploader', 'skin_tpl'], factory);
    } else if (typeof exports === 'object') {
        // CommonJS之类的
        module.exports = factory(require('jquery'), require('webuploader'), require('skin_tpl'));
    } else {
        // 浏览器全局变量(root 即 window)
        root.YQuploader = factory(root.jQuery, root.WebUploader, root.skin_tpl);
    }
}(this, function ($, WebUploader, skin_tpl) {
    function YQuploader(config){
        //默认配置
        var defaults = {
            container: '#upload_container', //上传控件容器
            pick: {
                id: '.uploader_pick',
                label: '',
                innerHTML: '选择文件',
                multiple: true
            },
            swf: './lib/webuploader/Uploader.swf',
            accept: {
                title: '文档、图片、音视频',
                extensions: 'doc,docx,ppt,pptx,xls,xlsx,pdf,gif,jpg,jpeg,bmp,png,mp3,mp4,avi,rmvb,flv'
            },
            auto: true,
            fileVal: 'file', //文件上传域的name
            fileNumLimit: 3,
            fileSingleSizeLimit: 1024*1024*1024,  //单个文件大小，单位Byte
            resize: false,
            onCustomBeforeFileQueued: null,
            onCustomFileQueued: null,
            onCustomFilesQueued: null,
            onCustomUploadSuccess: null,
            onCustomUploadError: null,
            onCustomUploadComplete: null
        };
        config = $.extend(defaults, config);

        //初始化模板和实例
        var container = $(config.container);
        container.html(skin_tpl.tpl);
        var instance = WebUploader.create(config);

        //为该实例添加id，防止一个页面有多个上传控件的冲突问题
        if(WebUploader.instanceCounts){
            instance.id = 'INSTANCE_' + (++WebUploader.instanceCounts);
        }
        else{
            instance.id = 'INSTANCE_' + 1;
            WebUploader.instanceCounts = 1;
        }
        container.data('instanceNum',instance.id);

        //错误码对应的提示文字
        instance.errorTextMap = {
            F_EXCEED_SIZE: '文件大小超出限制',
            Q_TYPE_DENIED: '文件格式不符合要求',
            Q_EXCEED_NUM_LIMIT: '文件个数超出限制'
        }
        
        //初始化对应模板下的操作行为
        skin_tpl.initEvent(instance, container, config);

        return instance;
    };

    return YQuploader;
}));