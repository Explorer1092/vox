<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert"] css=["plugin.alert", "teacher.widget", "plugin.register"] />
    <style type="text/css">
        html, body { width: 100%; height: 100%;  background: url(<@app.link href="public/skin/project/invite/invite.jpg"/>) repeat-x 0 0 #fff;
        }
        body { font: 12px/1.125 "微软雅黑", "Microsoft YaHei", Arial, "黑体"; color: #666; }
        body, h1, h2, h3, h4, h5, h6, dl, dt, dd, ul, ol, li, th, td, p, blockquote, pre, form, fieldset, legend, input, button, textarea, hr { padding: 0; margin: 0; }
        input, button, select { vertical-align: middle; }
        table { border-collapse: collapse; }
        li { list-style: none outside; }
        fieldset, img { vertical-align: middle; border: 0 none; }
        address, caption, cite, code, dfn, em, i, s, th, var { font-style: normal; font-weight: normal; }
        s { vertical-align: middle; font: 0px/0px arial; }
        a { color: #666; text-decoration: none; }
        .clear { clear: both; font: 0pt/0 Arial; height: 0px !important; visibility: hidden; padding: 0 !important; margin: 0 !important; width: 96%; float: none !important; }
        .blueclr { color: #39f; }
            /**/
        .main { width: 920px; margin: 0 auto; }
        .header .logo a, .header .titleBox { display: block; overflow: hidden; text-indent: -1000px; }
        .invite {
            background: url(<@app.link href="public/skin/project/invite/invite-04.jpg"/>) no-repeat; width: 920px; height: 720px; position: relative; }
        .header { height: 61px; position: relative; }
        .header .logo, .header .titleBox, .header .navs, .invite .regbtn { position: absolute; width: 161px; height: 69px; }
        .header .logo { top: 1px; left: 5px; z-index: 5;  background: url(<@app.link href="public/skin/project/invite/invite-02.jpg"/>) no-repeat;
        }
        .header .logo a { width: 100%; height: 58px; outline: none; }
        .header .titleBox { top: 9px; right: 7px; width: 415px; height: 60px;  background: url(<@app.link href="public/skin/project/invite/invite-03.jpg"/>) no-repeat;
        }
        .header .navs { top: 73px; left: 597px; width: 258px; height: 26px; }
        .header .navs li { float: left; }
        .header .navs li a { margin: 0 12px; display: inline-block; font-size: 14px; font-weight: bold; }
        .header .navs li a:hover { color: #fe9c02; }
        .leftctn { font-size: 14px; position: absolute; left: 81px; top: 45px; width: 391px; height: 128px; z-index: 1; color: #39f; }
        .leftctn b { display: inline-block; padding: 0 0 16px; }
        .leftctn b span { color: #666; }
        .leftctn p { line-height: 28px; text-indent: 28px; }
        .rightCtn { position: absolute; left: 104px; top: 175px; width: 361px; height: 456px; z-index: 1; font-size: 14px; line-height: 20px; }
        .rightCtn h4 { padding: 20px 0 10px; font: bold 14px/1.125 "微软雅黑", "Microsoft YaHei", Arial, "黑体"; }
        .rightCtn p i { width: 80px; display: inline-block; }
        .rightCtn .detailMore { float: right; color: #0380e0; font: bold 14px/1.125 "微软雅黑", "Microsoft YaHei", Arial, "黑体"; padding: 10px 0 0; }
        .rightCtn .detailMore:hover { text-decoration: underline; }
            /**/
        .main .loginbox { background: none; border: none; border-radius: 12px; padding: 0; }
        .regBox { position: absolute; left: 514px; top: 35px; width: 380px; height: 660px; z-index: 1; }
        .loginbox .loginMainNavs { padding: 6px; margin: 0 0 20px; border: solid 1px #ccc; background: #eee; border-radius: 8px; }
        .loginbox .loginMainNavs .reecommended { padding: 10px; color: #7ac21e; float: left; width: 170px; line-height: 22px; }
        .loginbox .loginMainNavs .reecommended b { color: #333; display: inline-block; margin: 0 5px; }
        .loginbox li.inp .tit { width: 140px; }
        .recomInfo span { margin: 0; padding: 0; }
        .loginbox li.inp input { width: 210px; }
        .loginbox li.inp .hint { clear: both; padding: 5px 0 0; width: 210px; margin: 0 0 0 154px; }
        .loginbox li.inp { height: auto; clear: both; padding: 0 0 10px; }
        .loginbox li.pad { padding: 0 0 0 154px; }
        .loginbox li.pad .hint { margin: 0 !important;}
    </style>
</head>
<body>
<div class="main">
    <div class="header">
        <div class="logo">
            <a href="/">一起作业</a>
        </div>
        <div class="titleBox">
            邀请您加入一起作业
        </div>
    </div>
    <div class="invite" id="inviteDiv">
        <div class="regBox">
            <div id="signup_form_box" class="loginbox">
                <h4>注册新账号</h4>
                <div class="loginMainNavs">
                    <a href="javascript:void(0)" class="mytc sel">
                        <span>我是<#if staff??>教研员<#else>老师</#if></span>
                    </a>
                    <span class="reecommended">推荐人：<b>${teacherName!?html}</b><#if staff??>教研员<#elseif invitationType?? && (invitationType == "TEACHER_INVITE_TEACHER_LINK" || invitationType == "TEACHER_INVITE_TEACHER_EMAIL")>老师<#else>学生</#if> <br>
                    <#if schoolInfo?? && schoolInfo?has_content>${schoolInfo!?html}</#if></span>
                    <div class="clear"></div>
                </div>

                <ul>
                    <li class="inp">
                        <b class="tit"><i>*</i> 手机号码(必填)：</b>
                        <input id="mobile" name="mobile" class="require" type="text" value="" data-label="手机号" data-role="teacher" autocomplete="off"/>
                        <span class="hint"></span>
                    </li>
                    <li class="pad phoneType" style="padding-bottom: 10px;">
                        <a id="getCheckCodeBtn" href="javascript:void(0);" class="reg_btn reg_btn_gray reg_btn_well" style="padding: 5px 10px;">
                            <span class="text_blue text_small text_normal">免费获取短信验证码</span>
                        </a><br>
                        <span class="hint"></span>
                    </li>
                    <li class="inp phoneType">
                        <b class="tit"><i>*</i> 手机验证码(必填)：</b>
                        <input id="checkCodeBox" class="require" name="checkCodeBox" type="text" data-label="验证码" value="" data-role="teacher" data-content-id="smsCodeBox"/>
                        <span class="hint"></span>
                    </li>

                    <li class="inp"><b class="tit"><i>*</i>真实姓名(必填)：</b>
                        <input name="name" type="text" value="" id="realname" data-label="姓名" data-role="teacher" class="require" autocomplete="off" required/>
                        <span class="hint"></span>
                    </li>
                    <li class="inp"><b class="tit"><i>*</i>密码(必填)：</b>
                        <input name="password" type="password" value="" id="password" data-label="密码" class="require" autocomplete="off" required/>
                        <span class="hint"><i></i></span>
                    </li>
                    <li class="inp"><b class="tit"><i>*</i>确认密码(必填)：</b>
                        <input name="verify_password" type="password" value="" data-label="确认密码" class="require" id="verify_password" autocomplete="off" required/>
                        <span class="hint"><i></i></span>
                    </li>

                    <li class="inp" style="display: none"><b class="tit">邀请人：</b>
                        <input name="invite" type="text"
                           <#if inviteUserId?? && inviteUserId?has_content>value="${inviteUserId!?html}"
                           <#else>value=""</#if>
                           id="invite_info"
                           data-role="teacher"
                           autocomplete="off">
                            <span class="hint"></span>
                    </li>
                    <input type="hidden" id="invitation" value="${invitationType!""?html}">
                    <li class="inp txt pad" style="padding: 0 0 0 75px !important;">
                        <span class="rememberme" style="padding: 0;">
                            <input id="accept_protocol" type="checkbox" checked="checked" class="checku" style="border: none; padding: 0; margin: 0;"/> 我已经阅读并接受
                           <#-- <s id="accept_protocol" class="checku"><i></i></s>我已经阅读并接受-->
                        </span>
                        <a class="clrblue" title="用户协议" href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/agreement.vpage" target="_blank">《一起作业用户协议》</a>
                        <span class="hint"></span>
                    </li>
                    <li class="inp pad" style="width:236px; padding: 0 0 0 75px !important;"><a id="register_teacher_btn" href="javascript:void(0);" class="reg_btn submitBtn" style="display: block; text-align: center;"><i>注册</i></a>
                    </li>
                </ul>
            </div>
        </div>

        <div class="leftctn" id="leftctnDiv" style="height: auto">
            <b>Hi 我是<span>${teacherName!?html}</span>:</b>
            <p>
                <#if staff??>
                    欢迎参与课题，提高学生兴趣、控制学习时间、自动打分评价…快和学生一起参与使用吧！
                <#elseif invitationType?? && (invitationType == "TEACHER_INVITE_TEACHER_LINK" || invitationType == "TEACHER_INVITE_TEACHER_EMAIL")>
                    一起作业跟我使用的教材同步，可以便捷的布置作业，系统自动批改作业，节省很多时间，游戏化作业深受学生喜欢，快来使用吧！
                 <#else>
                    邀请您加入一起作业，这是一个有趣的作业平台，我和同学们使用后学习都有不同程度提高，在这里可以使用同步教材布置作业，系统自动批改作业，节省很多时间，快来使用吧！
                </#if>
            </p>
        </div>

        <div class="rightCtn" id="rightCtnDiv">
            <h4><#if county?? && county?has_content>${county!?html}</#if>正在使用一起作业的老师：</h4>
            <p>
                <#if teacherSchoolList?? && teacherSchoolList?has_content>
                    <#list teacherSchoolList as list >
                        <#if list.teacherName?length != 0 && list.teacherName?index_of("测试") == -1 && list.teacherName?index_of("测验") == -1 && list.teacherName?index_of("必填") == -1 && list.teacherName?index_of("姓名") == -1>
                            <i>${list.teacherName!?html}</i>${list.schoolName!?html}<br/>
                        </#if>
                    </#list>
                <#else>
                    暂无相关资料
                </#if>
            </p>
            <h4>一起作业得到了全国专家的认可：</h4>
            <p>
                <i>龚亚夫</i>中国教育学会外语教学专业委员会理事长<br>
                <i>&nbsp;</i>教育部中央教育科学研究所研究员<br>
                <i>张连仲</i>北京外国语大学教授<br>
                <i>&nbsp;</i>国教育学会外语教学专业委员会顾问<br>
                <i>沈玲娣</i>北京市中小学外语教研室主任<br>
                <i>武祥村</i>清华大学教授<br>
                <i>尚俊杰</i>北京大学教授<br>
                <i>李玉顺</i>北京师范大学教授
            </p>
            <a href="/project/about/index.vpage" target="_blank" class="detailMore">了解更多>></a>
        </div>

    </div>

    <div id="signup_loading_box" class="loginbox" style="border:1px solid #CCCCCC;padding:30px;display:none;">
        <div class="finsh">
            注册中，请稍后... ...
        </div>
    </div>
    <div id="signup_success_box" class="loginbox" style="border:1px solid #CCCCCC;padding:30px;display:none;">
        <h4>注册新账号</h4>
        <ul class="ulpad">
            <li class="inp aliCenter regSuccess"><i></i><b style="color: gray;"><h5>注册成功</h5></b></li>
            <li class="aliCenter gray">
                <em style="width: 500px;">
                    <p class="text_big">
                        <b>您的一起作业号是：</b>
                        <strong id="success_id" style="bold 18px/1.125 arial; color: #c00"></strong>
                        <strong>用于登录，请牢记！</strong>
                        <a id="download_userinfo_but" target="_blank" href="javascript:void(0);" style="color: #39f;">下载</a>
                    </p>
                </em>
            </li>
            <li class="aliCenter" style="padding:15px 0;">
            <#if !staff??>
                <a href="/teacher/guide/bindMobile.vpage" class="greenBtn widthB"><i>教师身份验证</i></a>
            </#if>
                <a id="signup_login_btn" href="javascript:void(0);" style="color: #39f;"><#if !staff??>暂不验证，</#if>开始一起作业></a>
            </li>
        </ul>
    </div>
    <#if !staff??>
        <div class="text_center text_red" style="border: 1px dashed #ddd; background-color: #fafafa; font-size: 14px; padding:7px 5px; margin: 5px 0;">
            注册并免费认证身份后，可获得<b>话费奖励</b>！
        </div>
    </#if>
</div>
<script type="text/javascript">
    function validate() {
        var errorCount = 0;

        if($("#register_teacher_btn").hasClass('reg_btn_gray')){
            errorCount++;
            return false;
        }

        $(".require").each(function () {
            if ($17.isBlank($(this).val())) {
                $(this).parent().addClass('err');
                var errorMessage = $(this).data("label") + '不可为空';
                $(this).siblings('span').html("<i></i>" + errorMessage);
                errorCount++;
            }
        });

        if ($("li.err").size() == 0 && $("li.cor").size() > 0 && errorCount == 0) {
            return true;
        } else {
            return false;
        }
    }

    function login(data, url) {
        $.post('/j_spring_security_check', data,function (data) {
            if (data.success) {
                setTimeout(function(){ location.href = url; }, 200);
            } else {
                alert('登录失败，请重试！');
            }
        }).fail(function () {
            alert("网络请求失败，请稍等重试或者联系客服人员");
        });
    }

    $(function () {
        $("#download_userinfo_but").click(function(){
            $(this).attr("href","/ucenter/fetchaccount.vpage");
        });

        $("#accept_protocol").on("click", function () {
            var $this = $(this);
            var b = $("#register_teacher_btn");
            if($this.prop('checked')){
                b.removeClass('reg_btn_gray').css({cursor : 'pointer'});
                $this.parents('li').removeClass("err");
                $this.parent().siblings('span.hint').html('');
            }else{
                b.addClass('reg_btn_gray').css({cursor : 'default'});
                $this.parents('li').addClass("err");
                $this.parent().siblings('span.hint').html('<i></i>您还没有接受协议哦');
            }
        });

        <#--Form validate by Sanmao-->
        $("input").on("focus blur change", function (e) {
            var _this = $(this);
            var notice = "";
            var row = _this.parent();
            var _type = _this.attr("id");
            var span = _this.siblings("span");
            var condition = true;
            var errorMessage = "";
            var password = $("#password").val();
            var verify_password = $("#verify_password");
            if (e.type != "blur") {
                switch (_type) {
                    case "realname":
                        var value = _this.val().replace(/\s+/g, "");
                        condition = !(value.match(/[^\u4e00-\u9fa5]/g));
                        errorMessage = "请输入您的真实姓名,须为中文";
                        if (_this.data("role") == "teacher") {
                            notice = "请输入真实姓名，以便学生找到您";
                        } else {
                            notice = "请输入真实姓名";
                        }
                        break;
                    case "password":
                        if (_this.val().length > 16) {
                            errorMessage = "密码不可超过16位";
                            condition = false;
                        } else {
                            if (verify_password.val() != "") {
                                if (_this.val() == verify_password.val()) {
                                    verify_password.parent().removeClass("err").addClass("cor");
                                    verify_password.siblings('span').html("<i></i>");
                                } else {
                                    verify_password.parent().removeClass("cor").addClass("err");
                                    verify_password.siblings("span").html("<i></i>密码填写不一致，请重新填写");
                                }
                            }
                        }
                        notice = "请输入1—16位任意字符（字母区分大小写）";
                        break;
                    case "verify_password":
                        condition = (password == _this.val());
                        if ($("#password").val() != "") {
                            if (condition == true && _this.val() < 16) {
                                $("#password").parent().removeClass("err").addClass("cor");
                                $("#password").siblings('span').html("<i></i>");
                            }
                        }
                        errorMessage = "密码填写不一致，请重新填写";
                        notice = "请再次输入密码";
                        break;
                    case "mobile":
                        condition = $17.isMobile(_this.val());
                        errorMessage = "请填写正确的手机号码";
                        notice = "请输入手机号，验证通过后可用于登录、找回密码";
                        break;
                    case "email":
                        condition = $17.isEmail(_this.val());
                        errorMessage = "请填写正确格式的邮箱";
                        notice = "请输入常用邮箱，验证通过后可用于登录和找回密码";
                        break;
                    case "clazzId":
                        condition = (_this.val().length >= 6 && /^(C|c)[0-9]/.test(_this.val()));
                        errorMessage = "编号无效";
                        notice = "请向您的任课老师询问班级编号";
                        break;
                    case "invite_info":
                        if (_this.data("role") == "student") {
                            notice = "请输入邀请人的一起作业号";
                        } else {
                            notice = "请输入邀请人的一起作业号或手机号";
                        }
                        break;
                    default:
                        break;
                }
            }
            if (e.type == "focus") {
                if (!row.hasClass("err") && _this.val() == "") {
                    span.html(notice)
                }
            } else if (e.type == "blur") {
                if (!row.hasClass("err") && _this.val() == "") {
                    span.html("<i></i>");
                }
            } else if (e.type == "change") {
                if (!$17.isBlank(_this.val())) {
                    if (!condition) {
                        row.removeClass("cor").addClass("err");
                    } else {
                        row.removeClass("err").addClass("cor");
                        errorMessage = "";
                    }
                    span.html("<i></i>" + errorMessage);
                } else {
                    if (_this.hasClass("require")) {
                        errorMessage = _this.data("label") + '不可为空';
                        row.removeClass("cor").addClass("err");
                        span.html("<i></i>" + errorMessage);
                    } else {
                        row.removeClass("err");
                        span.html("");
                    }
                }
            }
        });

        var getClickCount = 1;
        $("#getCheckCodeBtn").live("click", function(){
            var $this = $(this);
            $17.tongji('注册2-老师-验证码');

            if($this.hasClass("btn_disable")){
                return false;
            }

            $.post("/signup/tmsignsvc.vpage", {
                mobile: $("#mobile").val(),
                count : getClickCount,
                cid   : "${contextId!?html}"
            }, function(data){
                var timerCount;
                var timer;
                var second = 60;

                if(data.success){
                    timerCount = second;
                    $this.siblings(".hint").html("验证码已发送") ;
                }else{
                    timerCount = data.timer || null;
                    $this.siblings(".hint").html(data.info);
                }

                var smsCodeBox = $("input[data-content-id=smsCodeBox]");
                smsCodeBox.next("span").html("<b class='vox_custom_icon vox_custom_icon_1'></b><span class='text_azure'>请将您手机收到的验证码数字填写到此处</span>");

                if(timerCount == null) {
                    return false;
                }

                getClickCount++;

                timer = $.timer(function() {
                    if(timerCount <= 0){
                        $this.removeClass("btn_disable");
                        $this.find("span, strong").html("免费获取短信验证码");
                        $this.siblings(".init, .hint, .msgInfo").html("");
                        timerCount = second;
                        timer.stop();
                    } else {
                        $this.addClass("btn_disable");
                        $this.find("span, strong").html(--timerCount + "秒之后可重新发送");
                        if(timerCount <= 30 && $17.getCookieWithDefault("STEL") < 2){
                            $this.siblings(".hint").html("长时间未收到请点，<a class='reg_btn reg_btn_gray' href='javascript:void(0)' id='serviceCallMe' style='padding:  5px 10px;'>免费发验证码</a> 为您提供一对一服务") ;
                        }
                    }
                });
                timer.set({ time : 1000});
                timer.play();
            });
        });

        //长时间未收到，点击【致电给我】
        if( $17.isBlank($17.getCookieWithDefault("STEL")) || $17.getCookieWithDefault("STEL") < 2){
            var serviceCallMeCount = 0;
            $("#serviceCallMe").live("click", function(){
                if(serviceCallMeCount < 1){
                    serviceCallMeCount = 1;
                    $.post("/signup/feedback.vpage", {mobile : $("#mobile").val()}, function(data){
                        if(data.success){
                            $17.alert("请求已发送，稍后工作人员将会与您取得联系。");
                        }else{
                            $17.alert(data.info);
                            serviceCallMeCount = 0;
                        }
                    });
                    //Cookie设置【致电给我】发送次数，最多记录到2
                    $17.setCookieOneDay("STEL", ($17.getCookieWithDefault("STEL")*1) + serviceCallMeCount, 1);
                }else{
                    $17.alert("请求已发送，稍后工作人员将会与您取得联系。");
                }
                $17.tongji('老师注册-致电给我 点击次数');
            });
        }


        $('#register_teacher_btn').on('click', function () {
            var _this = $(this);

            var success = validate();
            if (success) {
                var data = {
                    role            : 'ROLE_TEACHER',
                    userType        : 1,
                    mobile          : $("#mobile").val(),
                    code            : $("#checkCodeBox").val(),
                    realname        : $('#realname').val(),
                    password        : $('#verify_password').val(),
                    registerType    : 0,
                    inviteInfo      : $('#invite_info').val()
                <#if invitationType?has_content>
                    , invitationType: $('#invitation').val()
                </#if>
                };
                if( _this.hasClass("save")){return false;}
                _this.addClass("save");
                App.postJSON("/signup/msignup.vpage", data, function (data) {
                    if (data && data.success) {
                        $17.tongji('老师qq邀请注册页-成功');
                        setTimeout(function(){
                            location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage";
                        }, 500);
                    } else {
                        _this.removeClass("save");
                        var attrs = data.attributes;
                        if(attrs){
                            $.each(attrs, function (key, value) {
                                var el = $('#' + key);
                                if (el.length > 0) {
                                    el.parent().removeClass('cor').addClass('err');
                                    el.siblings("span").html("<i></i>" + value);
                                } else {
                                    if (attrs.none) {
                                        alert(attrs.none);
                                    }
                                }
                            });
                        }else{
                            $17.alert(data.info);
                        }
                    }
                }, function () {
                    _this.removeClass("save");
                    alert("网络请求失败，请稍等重试或者联系客服");
                });
            }
            return false;
        });
        $17.tongji('老师qq邀请注册页');
    });
</script>
</body>
</html>