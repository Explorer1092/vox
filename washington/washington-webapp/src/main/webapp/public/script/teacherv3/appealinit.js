define(['jquery', 'knockout', 'WebUploader', 'voxLogs'], function ($, ko, WebUploader) {
    var appealPage = {
        isSuccess: ko.observable(false),
        content: ko.observable(''),
        fileName : ko.observable(''),
        type : moduleType
    };

    ko.applyBindings(appealPage);

    //上传照片 初始化Web Uploader
    var uploader = WebUploader.create({
        // 选完文件后，是否自动上传。
        auto: true,

        // swf文件路径
        swf: '/public/plugin/YQuploader-1.0/lib/webuploader/Uploader.swf',

        // 文件接收服务端。
        server: '/ucenter/appealupload.vpage',
        chunkSize : 10485760,
        threads : 1,
        fileNumLimit : 1,

        // 内部根据当前运行是创建，可能是input元素，也可能是flash.
        pick: {
            id: '#filePicker',
            multiple: false
        },

        // 只允许选择图片文件。
        accept: {
            title: 'Images',
            extensions: 'gif,jpg,jpeg,bmp,png',
            mimeTypes: 'image/*'
        },
        thumb : {
            quality: 60,
            allowMagnify: false
        },
        compress : {
            width: 600,
            height: 600,
            quality: 80,
            crop: false
        }
    });

    //服务上传图片成功返回fileName
    uploader.on( 'uploadAccept', function( data, result) {
        if(result.success){
            appealPage.fileName(result.fileName);
        }else{
            onDialog({info: "上传失败！"});
        }

        //重置uploader。目前只重置了队列
        uploader.reset();
    });

    //提交
    $(document).on({
        click: function () {
            var $this = $(this);

            if($this.hasClass('dis')){
                return false;
            }

            if (appealPage.type == "CHEATING" && appealPage.content().length < 100) {
                onDialog({info: "申诉信息，不少于100字!"});
                return false;
            }

            if(appealPage.content() == ""){
                onDialog({info: "请填写完善申诉信息!"});
                return false;
            }

            if(appealPage.fileName() == ""){
                onDialog({info: "请上传照片!"});
                return false;
            }

            $this.addClass('dis');
            $.post("/ucenter/saveappeal.vpage", {
                type : appealPage.type,
                reason : appealPage.content(),
                fileName : appealPage.fileName()
            }, function(result){
                if(result.success){
                    appealPage.isSuccess(true);
                }else{
                    appealPage.isSuccess(false);
                    onDialog({info: result.info});
                }

                $this.removeClass('dis');
            });

            if(appealPage.type == "CHEATING"){
                if(window["external"] && window.external["log_b"]){
                    YQ.voxLogs({
                        database: 'web_teacher_logs',
                        module: 'm_BUsu1caN',
                        op: 'o_pV4WmJ8f'
                    });
                }else{
                    YQ.voxLogs({
                        database: 'wechat_logs',
                        module: 'm_joeG94ki',
                        op: 'o_BmLJ6wMx'
                    });
                }

            }

            if(appealPage.type == "FAKE"){
                if(window["external"] && window.external["log_b"]){
                    YQ.voxLogs({
                        database: 'web_teacher_logs',
                        module: 'm_BUsu1caN',
                        op: 'o_PpAQavov'
                    });
                }else{
                    YQ.voxLogs({
                        database: 'wechat_logs',
                        module: 'm_joeG94ki',
                        op: 'o_JaKVlZS6'
                    });
                }
            }
        }
    }, '.js-submitAppeal');

    //知道了
    $(document).on({
        click: function () {
            appealPage.isSuccess(false);
            appealPage.content('');
            appealPage.fileName('');

            window.history.back();
        }
    }, '.js-gotIt');

    function onDialog(opt) {
        if(opt && opt.info){
            var $dialogAlert = $("#DialogAlert");
            $dialogAlert.show().find(".js-content").html(opt.info);

            if(opt.btnText){
                $dialogAlert.find(".js-submit").html(opt.btnText);
            }else{
                $dialogAlert.find(".js-submit").html("知道了");
            }
        }
    }

    $(document).on("click", "#DialogAlert .js-submit", function(){
        var $dialogAlert = $("#DialogAlert");
        $dialogAlert.hide();
    });

    if(appealPage.type == "CHEATING"){
        if(window["external"] && window.external["log_b"]){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_BUsu1caN',
                op: 'o_BTMaJVbw'
            });
        }else{
            YQ.voxLogs({
                database: 'wechat_logs',
                module: 'm_joeG94ki',
                op: 'o_5X1Ri6ma'
            });
        }

    }

    if(appealPage.type == "FAKE"){
        if(window["external"] && window.external["log_b"]){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_BUsu1caN',
                op: 'o_IpCfjaEc'
            });
        }else{
            YQ.voxLogs({
                database: 'wechat_logs',
                module: 'm_joeG94ki',
                op: 'o_VuNfGvqI'
            });
        }
    }
});