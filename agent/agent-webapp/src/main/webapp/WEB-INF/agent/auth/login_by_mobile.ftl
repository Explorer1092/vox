<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- The styles -->
    <link id="bs-css" href="${requestContext.webAppContextPath}/public/css/bootstrap-cerulean.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-bottom: 40px;
        }

        .sidebar-nav {
            padding: 9px 0;
        }
        li{list-style: none}
        .row-fluid .login-box li{float: left;width: 50%;height:34px;line-height:34px;text-align: center;}
        .row-fluid .login-box li a{color: #333;font-size: 14px;}
        .row-fluid .login-box li:last-child{background-color: #369bd7;}
        .row-fluid .login-box li:last-child a{color: #fff;}
        .row-fluid .login-box li:last-child:active{background-color: #3673d7;}
        .row-fluid .login-box ul{margin:0 20% 15px 20%;background-color: #fff;border: 1px solid #cccccc;border-radius:3px;overflow:hidden;}
        .login-box .input-prepend{position: relative}
        .login-box .input-prepend #code-btn{position: absolute;top:0;right:-74px;padding:0 3px;height:25px;
            line-height:25px;color: #369bd7;
            background-color:#fff;border: 1px solid #369bd7;cursor: pointer;}
    </style>
    <link href="${requestContext.webAppContextPath}/public/css/bootstrap-responsive.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/charisma-app.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.8.21.custom.css" rel="stylesheet">
    <link href='${requestContext.webAppContextPath}/public/css/fullcalendar.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/fullcalendar.print.css' rel='stylesheet' media='print'>
    <link href='${requestContext.webAppContextPath}/public/css/chosen.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/uniform.default.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/colorbox.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/jquery.cleditor.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/jquery.noty.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/noty_theme_default.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/elfinder.min.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/elfinder.theme.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/jquery.iphone.toggle.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/opa-icons.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/uploadify.css' rel='stylesheet'>

</head>

<body>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="row-fluid">
            <div class="span5 center login-header">
                <h2>欢迎使用</h2>
            </div>
        </div>
        <div class="row-fluid">
            <div class="well span3 center login-box" style="width: 400px;">

                <ul>
                    <li><a href="login.vpage">账号登录</a></li>
                    <li><a href="login_by_mobile.vpage" class="selected">手机登录</a></li>
                </ul>
            <#include '../widget_alert_messages.ftl' />
                <form id="loginForm" class="form-horizontal" method="post" action="/auth/login_by_mobile.vpage">
                    <input type="hidden" name="client" id="client" value="${client!}"/>
                    <fieldset>
                        <div class="input-prepend" title="手机号" data-rel="tooltip">
                            <span class="add-on"><i class="icon-user"></i></span>
                            <input autofocus class="input-large span10" id="mobile" placeholder="请输入手机号" maxlength="11" type="tel" name="mobile" value="${mobile!}"/>
                            <span id="code-btn">获取验证码</span>
                        </div>
                        <div class="clearfix"></div>

                        <div class="input-prepend" title="验证码" data-rel="tooltip">
                            <span class="add-on"><i class="icon-lock"></i></span>
                            <input class="input-large span10" id="captchaCode" placeholder="请输入验证码" maxlength="6" type="tel" name="captchaCode" autocomplete="off"/>
                        </div>
                        <div class="clearfix"></div>
                    </fieldset>
                </form>
                <p class="center span5">
                    <button class="btn btn-primary js-submit">登 录</button>
                </p>
            </div>
            <!--/span-->
        </div>
        <!--/row-->
    </div>
    <!--/fluid-row-->
</div>

<!-- jQuery -->
<script src="${requestContext.webAppContextPath}/public/js/jquery-1.7.2.min.js"></script>
<!-- jQuery UI -->
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.8.21.custom.min.js"></script>
<!-- transition / effect library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-transition.js"></script>
<!-- alert enhancer library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-alert.js"></script>
<!-- modal / dialog library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-modal.js"></script>
<!-- custom dropdown library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-dropdown.js"></script>
<!-- scrolspy library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-scrollspy.js"></script>
<!-- library for creating tabs -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-tab.js"></script>
<!-- library for advanced tooltip -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-tooltip.js"></script>
<!-- popover effect library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-popover.js"></script>
<!-- button enhancer library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-button.js"></script>
<!-- accordion library (optional, not used in demo) -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-collapse.js"></script>
<!-- carousel slideshow library (optional, not used in demo) -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-carousel.js"></script>
<!-- autocomplete library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-typeahead.js"></script>
<!-- tour library -->
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-tour.js"></script>
<!-- library for cookie management -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.cookie.js"></script>
<!-- calander plugin -->
<script src='${requestContext.webAppContextPath}/public/js/fullcalendar.min.js'></script>
<!-- data table plugin -->
<script src='${requestContext.webAppContextPath}/public/js/jquery.dataTables.min.js'></script>

<!-- chart libraries start -->
<script src="${requestContext.webAppContextPath}/public/js/excanvas.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.pie.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.stack.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery.flot.resize.min.js"></script>
<!-- chart libraries end -->

<!-- select or dropdown enhancer -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.chosen.min.js"></script>
<!-- checkbox, radio, and file input styler -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.uniform.min.js"></script>
<!-- plugin for gallery image view -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.colorbox.min.js"></script>
<!-- rich text editor library -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.cleditor.min.js"></script>
<!-- notification plugin -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.noty.js"></script>
<!-- file manager library -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.elfinder.min.js"></script>
<!-- star rating plugin -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.raty.min.js"></script>
<!-- for iOS style toggle switch -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.iphone.toggle.js"></script>
<!-- autogrowing textarea plugin -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.autogrow-textarea.js"></script>
<!-- multiple file upload plugin -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.uploadify-3.1.min.js"></script>
<!-- history.js for cross-browser state change on ajax -->
<script src="${requestContext.webAppContextPath}/public/js/jquery.history.js"></script>
<!-- application script for Charisma demo -->
<script src="${requestContext.webAppContextPath}/public/js/charisma.js"></script>

<script>
    $(function () {

        $("#code-btn").on("click", function () {
            getSmsCode();
        });

        $(".js-submit").click(function(){
            if(validate()){
                $("#loginForm").submit();
            }
        });

    });

    function getSmsCode(){
        var checkResult = checkMobile();
        if (!checkResult) {
            alert("请填写正确的手机号");
            $("#mobile").focus();
            return;
        }
        var mobile = $.trim($("#mobile").val());
        $.post("/mobile/getSMSCode.vpage",
                {mobile:mobile},
                function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        startTimer();
                    }
                }
        );
    }

    var myVar;
    var time = 60;//
    function startTimer(){
        $("#code-btn").attr("disabled",true);
        myVar=self.setInterval("myTimer()",1000);
    }
    function myTimer(){
        $("#code-btn").text(time + "s后(重发)");
        $("#code-btn").attr("style","color:#ccc;background-color: #eee;border:none;");
        time = time - 1;
        if(time == 0){
            $("#code-btn").text("获取验证码");
            $("#code-btn").attr("disabled",false);
            $("#code-btn").attr("class","inner-right send-code").attr("style","");
            clearInterval(myVar);
            time = 60;
        }
    }

    function checkMobile(){
        var result = false;
        var mobile = $.trim($("#mobile").val());
        if(mobile.length != 0){
            var reg=/^(1)\d{10}$/;
            if(reg.test(mobile)){
                result = true;
            }
        }
        return result;
    }

    function validate() {
        var checkResult = checkMobile();
        if (!checkResult) {
            alert("请填写正确的手机号");
            $("#mobile").focus();
            return false;
        }

        var captchaCode = $("#captchaCode").val();
        if (!captchaCode) {
            alert("验证码为空，请输入验证码");
            $("#captchaCode").focus();
            return false;
        }
        return true;
    }
</script>

</body>
</html>

