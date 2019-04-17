define(['jquery', 'knockout', 'WebUploader', 'impromptu', 'voxLogs'], function ($, ko, WebUploader) {
    var appealPage = {
        content: ko.observable(''),
        count : ko.observable(0),
        feedbackCtn: ko.observable(''),
        feedbackCount : ko.observable(0),
        showCheating : ko.observable(showCheating),
        showFake : ko.observable(showFake),
        fileName: ko.observable(''),
        type: ko.observable('FEEDBACK')
    };

    ko.applyBindings(appealPage);

    $(document).on("click", "#feedbackTab li", function (index) {
        var $this = $(this);

        tabSwitch($this);
        uploaderInit();
    });

    $(document).on('keyup', 'textarea', function(){
        appealPage.count(appealPage.content().length);
        appealPage.feedbackCount(appealPage.feedbackCtn().length);
    });

    if( (getQueryString("type") == "CHEATING" && appealPage.showCheating()) || (getQueryString("type") == "FAKE" && appealPage.showFake()) ){
        tabSwitch( $("#feedbackTab li[data-type='"+ getQueryString("type")  +"']") );
        appealPage.type(getQueryString("type"));

        if(getQueryString("type") == "CHEATING"){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_5mrIUiTh',
                op: 'o_4Sxj2jz1'
            })
        }

        if(getQueryString("type") == "FAKE"){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_5mrIUiTh',
                op: 'o_9nCgN7hM'
            })
        }
    }else{
        tabSwitch( $("#feedbackTab li:first") );
    }

    function tabSwitch(idx){
        idx.addClass("active");
        idx.siblings().removeClass("active");

        appealPage.type(idx.attr('data-type'));
    }

    uploaderInit();

    //提交-申诉
    $(document).on({
        click: function () {
            var $this = $(this);

            if ($this.hasClass('dis')) {
                return false;
            }

            if (appealPage.type() == "CHEATING" && appealPage.count() < 100) {
                onDialog({info: "申诉信息，不少于100字!"});
                return false;
            }

            if (appealPage.content() == "") {
                onDialog({info: "请填写完善申诉信息!"});
                return false;
            }

            if (appealPage.fileName() == "") {
                onDialog({info: "请上传照片!"});
                return false;
            }

            $this.addClass('dis');
            $.post("/ucenter/saveappeal.vpage", {
                type: appealPage.type(),
                reason: appealPage.content(),
                fileName: appealPage.fileName()
            }, function (result) {
                if (result.success) {
                    appealPage.content('');
                    appealPage.fileName('');

                    if(appealPage.type() == "FAKE"){
                        onDialog({info: "提交成功！<br/>工作人员会尽快审核，结果将在1个工作日内<br>发送至您的消息中心，请耐心等待。", type: "closeWin"});
                        YQ.voxLogs({
                            database: 'web_teacher_logs',
                            module: 'm_5mrIUiTh',
                            op: 'o_zSkc46cC'
                        })
                    }else{
                        onDialog({info: "提交成功！<br/>您的申诉结果将在3-5个工作日内<br>发送至您的消息中心，请耐心等待。", type: "closeWin"});
                        YQ.voxLogs({
                            database: 'web_teacher_logs',
                            module: 'm_5mrIUiTh',
                            op: 'o_kLbdnrzy'
                        })
                    }
                } else {
                    onDialog({info: result.info});
                }

                $this.removeClass('dis');
            });
        }
    }, '.js-submitAppeal');

    //提交反馈建议
    $(document).on({
        click: function () {
            var $this = $(this);

            if ($this.hasClass('dis')) {
                return false;
            }

            if (appealPage.feedbackCtn() == "") {
                onDialog({info: "请填写反馈内容!"});
                return false;
            }

            var $serverInfo = "账号：" + currentUserId + '|';
            $serverInfo += "描述问题：" + appealPage.feedbackCtn();

            location.href = '/redirector/onlinecs_new.vpage?type=teacher&question_type=question_advice&origin=PC-底部-建议反馈&serverInfo='+ encodeURIComponent($serverInfo);
        }
    }, '.js-submitFeedback');

    function onDialog(opt) {
        if (opt && opt.info) {
            var $dialogAlert = $("#DialogAlert");
            $dialogAlert.show().find(".js-content").html(opt.info);

            if (opt.btnText) {
                $dialogAlert.find(".js-submit").html(opt.btnText);
            } else {
                $dialogAlert.find(".js-submit").html("知道了");
            }

            if(opt.type){
                $dialogAlert.find(".js-submit").attr('data-type', opt.type);
            }else{
                $dialogAlert.find(".js-submit").attr('data-type', "default");
            }
        }
    }

    $(document).on("click", "#DialogAlert .js-submit", function () {
        var $this = $(this);
        var $dialogAlert = $("#DialogAlert");
        $dialogAlert.hide();

        if($this.attr("data-type") == "closeWin"){
            window.close();
        }
    });

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    //上传照片
    function uploaderInit(){
        //上传照片 初始化Web Uploader
        var uploader = WebUploader.create({
            // 选完文件后，是否自动上传。
            auto: true,

            // swf文件路径
            swf: httpCdnUrl + '/public/plugin/YQuploader-1.0/lib/webuploader/Uploader.swf',

            // 文件接收服务端。
            server: '/ucenter/appealupload.vpage',
            chunkSize: 10485760,
            threads: 1,
            fileNumLimit: 1,

            // 内部根据当前运行是创建，可能是input元素，也可能是flash.
            pick: '#filePicker',

            // 只允许选择图片文件。
            accept: {
                title: 'Images',
                extensions: 'gif,jpg,jpeg,bmp,png',
                mimeTypes: 'image/*'
            },
            thumb: {
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

        $(".webuploader-pick").html("点击上传照片");

        //服务上传图片成功返回fileName
        uploader.on('uploadAccept', function (data, result) {
            if (result.success) {
                appealPage.fileName(result.fileName);
            } else {
                onDialog({info: "上传失败！"});
            }

            $(".webuploader-pick").html("&nbsp;");

            //重置uploader。目前只重置了队列
            uploader.reset();
        });
    }
});