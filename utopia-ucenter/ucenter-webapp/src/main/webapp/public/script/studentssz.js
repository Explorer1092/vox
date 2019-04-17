var feedBackInner = {
    refUrl     : window.location.pathname,
    homeworkType : "" ,//作业学科(英语:数学)
    practiceType : "", // 游戏类型：编号
    practiceName : "",//游戏名称
    extStr1 : "", // 作业ID
    extStr2 : "" //
};

// getSMSVerifyCode拷至$17中，但按钮长度首限制，故而单独提出设置
function getSMSVerifyCode($this, data, second){
    var timerCount;
    var timer;
    second = second ? second : 60;
    $this.addClass("btn_disable");
    if(data.success){
        timerCount = second;
    }else{
        timerCount = data.timer || null;
        if(timerCount == null){
            $this.removeClass("btn_disable");
            return false;
        }
    }
    timer = $.timer(function(){
        if(timerCount <= 0){
            $this.removeClass("btn_disable");
            $this.find("span, strong").html("获取验证码");
            $this.siblings(".init, .hint, .msgInfo").html("");
            timerCount = second;
            timer.stop();
        }else{
            $this.find("span, strong").html(--timerCount + "s后重新发送");
        }
    });
    timer.set({ time: 1000});
    timer.play();
}

//target下的input验证
function validate(target){
    var $inputs;
    var $errorInputs;
    $inputs = $(target).find("input.w-int");
    $inputs.each(function(){
        if(!$17.isBlank($(this).attr("id"))){
            $(this).trigger("change");
        }
    });
    $errorInputs = $(target).find("input.w-int-error:visible");
    if($errorInputs.size() == 0){
        return true;
    }else{
        return false;
    }
}

// 折叠卡片（我的资料、账号安全）并添加类
function foldCard (boxType) {
    $('.accountBox').slideUp().filter('.accountBox[data-box_type='+boxType+']').toggle();
    $(".accountBut[data-box_type!="+ boxType +"]").removeClass('up');
    if ($(".accountBut[data-box_type="+ boxType +"]").hasClass('up')) {
        $(".accountBut[data-box_type="+ boxType +"]").removeClass('up');
    } else {
        $(".accountBut[data-box_type="+ boxType +"]").addClass('up');
    }
}

$(function(){
    // 我的资料、账号安全 验证、报错等
    $("input").on("focus blur change", function (e) {
        var $this = $(this);
        var notice = "";
        var idType = $this.attr("id");
        var span = $this.siblings('span');
        var errorIcon = '<i class="w-spot w-icon-error"></i>';
        var successIcon = '<i class="w-spot w-icon-success"></i>';
        var condition = true;
        var errorMessage = "";
        var user_new_password = $("#user_new_password");
        var user_new_again_password = $("#user_new_again_password");
        var errorClass = 'w-int-error';

        if(e.type != 'blur'){
            switch (idType) {
                case "parentname" :
                    var value = $this.val().replace(/\s+/g, "");
                    condition = !(value.match(/[^\u4e00-\u9fa5]/g));
                    errorMessage = "请输入家长真实姓名,须为中文";
                    break;

                case "user_current_password":

                    notice = '填写初始密码';
                    break;
                case "user_new_password":
                    if ($this.val().length > 16) {
                        errorMessage = "密码不可超过16位";
                        condition = false;
                    }
                    notice = '填写新密码';
                    break;
                case "user_new_again_password":
                    condition = (user_new_password.val() == $this.val());
                    if (!$17.isBlank(user_new_password.val())) {
                        if (condition && $this.val() < 16) {
                            user_new_again_password.html(successIcon);
                        }
                    }

                    errorMessage = "密码填写不一致";
                    notice = '再次填写新密码';
                    break;
                case "confirmNewPaymentPassword_reset":
                    var newPaymentPassword_reset = $("#newPaymentPassword_reset");
                    condition = (newPaymentPassword_reset.val() == $this.val());
                    if (!$17.isBlank(newPaymentPassword_reset.val())) {
                        if (condition && $this.val() < 16) {
                            $("#confirmNewPaymentPassword_reset").html(successIcon);
                        }
                    }
                    errorMessage = "密码填写不一致";
                    notice = '再次填写新密码';

                    break;

                case "confirmNewPaymentPassword":
                    var newPaymentPassword = $("#newPaymentPassword");
                    condition = (newPaymentPassword.val() == $this.val());
                    if (!$17.isBlank(newPaymentPassword.val())) {
                        if (condition && $this.val() < 16) {
                            $("#confirmNewPaymentPassword").html(successIcon);
                        }
                    }
                    errorMessage = "密码填写不一致";
                    notice = '再次填写新密码';

                    break;
                case "mobile":
                case "mobile_text":
                case "edit_mobile_text":

                    condition = $17.isMobile($this.val());
                    errorMessage = "请填写正确的手机号码";
                    notice = "请输入手机号，验证通过后可用于登录、找回密码";
                    break;
                case "email_box":
                    condition = $17.isEmail($this.val());
                    errorMessage = "请填写正确格式的邮箱";
                    notice = "请输入常用邮箱，验证通过后可用于登录和找回密码";
                    break;
                default:

                    break;
            }
        }

        if (e.type == "focus") {

        } else if (e.type == "blur") {

        } else if (e.type == "change") {
            if (!$17.isBlank($this.val())) {
                if (!condition) {
                    span.html(errorIcon + '<div class="info">'+errorMessage+'</div>');
                    $this.addClass(errorClass);
                } else {
                    span.html(successIcon);
                    errorMessage = "";
                    $this.removeClass(errorClass);
                }
            } else {
                if($this.hasClass("require")){
                    errorMessage = $this.data("label") + '不可为空';
                    span.html(errorIcon + '<div class="info">'+errorMessage+'</div>');
                    $this.addClass(errorClass);
                }
            }
        }
    });

    //下拉列表
    $("#pull_down_box").hover(function(){
        $(this).addClass('active');
    }, function(){
        $(this).removeClass('active');
    });

    //下载客户端
    $(document).on('click','#js_download',function () {
        var ua = navigator.userAgent;
        if(ua.indexOf("Windows NT 5") > -1 || ua.indexOf("Windows NT 6.0") > -1) {
            // console.log('您使用的是XP系统 AND Vista!');
            window.location.href = "http://cdn.17zuoye.com/static/download/17zuoye_setup_xp_20170425.exe";
        }else if((ua.indexOf("Windows NT 6") || ua.indexOf("Windows NT 10")) > -1) {
            // console.log('您使用的是Win7系统！');
            window.location.href = "http://cdn.17zuoye.com/static/download/17zuoye_setup_win7_20170425.exe";
        }else if(ua.indexOf("Windows") > -1){
            window.location.href = "http://cdn.17zuoye.com/static/download/17zuoye_setup_win7_20170425.exe";
        }else{
            $17.alert("暂无非windows系统桌面版，敬请期待!");
        }
    });

    //message
    var bubbles = new $17.Model({
        unreadTotalCount            : 0,
        timer                       : $17.sdv(60 * 1000, 30 * 1000)
    });
    bubbles.extend({
        getBubbles: function(){
            var $this = this;
            $.get("/student/bubbles.vpage", function(data){
                if(data.success){
                    $this.unreadTotalCount            = data.unreadTotalCount || 0;
                    $this.setBubbles();
                    //$("#unreadLetterAndReplyCount").closest("p").find("a").attr("href", "/student/conversation/index.vpage");
                }
            });
        },
        setBubbles: function(){
            var $this = this;
            if($this.unreadTotalCount == 0 ){
                $("#popinfo").hide();
                $(".unreadSystemMessageCount").hide();
            }else{
                $("#popinfo").show();
                $(".unreadSystemMessageCount").text($this.unreadTotalCount).show();
            }
        },
        init: function(){
            var $this = this;
            $this.getBubbles();
        }
    }).init();

    //右下角消息窗
    var userPopups = new $17.Model({
        messageUrl      : "/ucenter/getuserpopups.vpage",
        continueTime    : $17.sdv(40 * 1000, 100 * 60 * 1000),
        timer           : $17.sdv(60 * 1000, 20 * 1000)
    });
    userPopups.extend({
        getBubbles: function(){
            var $this = this;
            $.get($this.messageUrl, function(data){
                if(data.success){
                    $this.setBubbles(data.htmlList);
                }
            });
        },
        setBubbles: function(htmlList){
            var $this   = this;
            var html    = null;

            if(htmlList.length != 0){

                html = template("t:右下角新消息", {
                    msgList : htmlList
                });

                $.eBox({
                    size:{width:396, height:235},
                    title       : { html : "消息助手" },
                    content     : {
                        html : html
                    },
                    effect      : { type : "slide", speed : 200 },
                    skin : "",
                    openOnce    : false
                });

                setTimeout(function(){$("#eBoxWrap").remove();}, $this.continueTime);
            }
        },
        init: function(){
            var $this = this;
            $this.getBubbles();
        }
    }).init();

    //保护视力
    var protection = null;
    var diff = null;
    var timer = null;

    $("#studentTimeClose").on("click", function(){
        $17.tongji("保护视力_用户关闭", "");
        $("#studentTime").hide();
        clearInterval(timer);
        timer = null;
        $("#studentTimeM").html($17.sdv("10", "01"));
        $("#studentTimeS").html("00");
    });

    setInterval(function(){
        protection = $17.getCookieWithDefault("protection");
        if(!$17.isBlank(protection)){
            if(protection == "show"){
                diff = $17.DateDiff($17.getCookieWithDefault("protectionTime"), $17.DateUtils("%Y-%M-%d %h:%m"), "m");
                if(diff > $17.sdv(90, 5)){
                    $17.setCookieOneDay("protectionTime", $17.DateUtils("%Y-%M-%d %h:%m"), 60);
                }else if(diff >= $17.sdv(40, 2)){
                    $("#studentTime").show();
                    $17.tongji("保护视力_提示弹出", "");

                    if(!timer){
                        timer = setInterval(function(){
                            var m = $("#studentTimeM").html()*1;
                            var s = $("#studentTimeS").html()*1;
                            if(m == 0 && s == 0){
                                $("#studentTime").hide();
                                $("#studentTimeM").html($17.sdv("10", "01"));
                                $("#studentTimeS").html("00");
                                clearInterval(timer);
                                timer = null;
                                $17.tongji("保护视力_过期关闭", "");
                            }else{
                                if(s == 0){
                                    s = 59;
                                    m -= 1;
                                    $("#studentTimeM").html($17.strPad(m, "0", 2));
                                    $("#studentTimeS").html($17.strPad(s, "0", 2));
                                }else{
                                    s -= 1;
                                    $("#studentTimeS").html($17.strPad(s, "0", 2));
                                }
                            }
                        }, 1000);
                    }

                    $17.setCookieOneDay("protectionTime", $17.DateUtils("%Y-%M-%d %h:%m"), 60);
                }
            }
        }else{
            $17.setCookieOneDay("protection", "show", 60);
            $17.setCookieOneDay("protectionTime", $17.DateUtils("%Y-%M-%d %h:%m"), 60);
        }
    }, $17.sdv(2 * 60 * 1000, 2 * 1000));

    //退出系统
    $("#logout").on("click", function(){
        $17.tongji("首页-导航-用户名-退出");
        setTimeout(function(){ location.href = "/ucenter/logout.vpage"; }, 200);
        return false;
    });

    //语音插件
    $("#installActiveX").on("click", function(){
        AC_InstallActiveX();
        $17.tongji("首页-导航-用户名-下载");
    });

    ////反馈建议
    //$("#message_right_sidebar").on("click", function(){
    //    if(feedBackInner.practiceName == "exam" || feedBackInner.practiceName == "数学应试练习"){
    //        feedBackInner.extStr2 = studentHomeworkExam.getQuestionId().join();
    //    }
    //
    //    var url = '/ucenter/feedback.vpage?' + $.param(feedBackInner);
    //    var html = "<iframe class='vox17zuoyeIframe' width='600' height='430' frameborder=0 src='" + url + "'></iframe>";
    //    $.prompt(html, { title: "给一起作业提建议", position : { width:660 }, buttons: {} } );
    //    $17.tongji('网址底栏-反馈建议');
    //    return false;
    //});

    //绑定微信（走遍美国 PK馆 通天塔）
    $(document).on('click','.bandingWeiXin', function(){
        var $this = $(this);
        var bandingType = $this.data('banding_type');
        var campaignId = $this.data('campaign_id');
        var campaignIdBox = campaignId ? "?campaignId=" + campaignId : "";
        var qrCodeUrl = "http://cdn-cc.17zuoye.com/public/skin/studentv3/images/2dbarcode.jpg";

        $.get("/student/qrcode.vpage"+campaignIdBox, function(data){
            if(data.success){
                qrCodeUrl = data.qrcode_url
            }
            $.prompt(template("t:bandingWeiXin", { weiXinCode : qrCodeUrl , bandingType : bandingType}),{
                title : "关注微信",
                buttons : {}
            });
            $17.tongji("绑定微信-"+bandingType);
        });
    });

    //公用clickVoxLog统计事件
    $(document).on("click", ".v-studentVoxLogRecord", function(){
        var $this = $(this);
        var $op = $this.attr("data-op");
        $op = $op.split("|");

        if( $op.length > 0 ){
            $17.voxLog({
                app : "student",
                module: $op[1] || "studentOperationTrack",
                op: $op[0]
            }, "student");
        }
    });
});

(function($){
    $.fn.extend({
        textScroll:function(opt,callback){
            //参数初始化
            if(!opt) var opt={};

            var $this = this.eq(0).find("ul:first");
            var lineH = $this.find("li:first").height(), //获取行高
                line=opt.line?parseInt(opt.line,10):parseInt(this.height()/lineH,10), //每次滚动的行数，默认为一屏，即父容器高度
                speed=opt.speed?parseInt(opt.speed,10):500, //卷动速度，数值越大，速度越慢（毫秒）
                timer=opt.timer?parseInt(opt.timer,10):1000; //滚动的时间间隔（毫秒）

            if(line==0){
                line=1;
            }

            if($this.find("li").length < 2){
                return false;
            }

            var upHeight=0-line*lineH;

            //滚动函数
            scrollUp = function(){
                $this.animate({
                    marginTop:upHeight
                },speed,function(){
                    for(i=1;i<=line;i++){
                        $this.find("li:first").appendTo($this);
                    }
                    $this.css({marginTop:0});
                });
            };

            //鼠标事件绑定
            $this.bind({
                mouseenter: function(){
                    clearInterval(timerID);
                },
                mouseleave: function(){
                    timerID=setInterval("scrollUp()",timer);
                }
            }).mouseleave();
        }
    })
})(jQuery);

