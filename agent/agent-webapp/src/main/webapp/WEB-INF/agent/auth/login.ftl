<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- The styles -->
    <link id="bs-css" href="${requestContext.webAppContextPath}/public/css/bootstrap-cerulean.css" rel="stylesheet">
    <style type="text/css">
        body {padding-bottom: 40px;}
        .sidebar-nav {padding: 9px 0;}
        li{list-style: none}
        .row-fluid .login-box li{float: left;width: 50%;height:34px;line-height:34px;text-align: center;}
        .row-fluid .login-box li a{color: #333;font-size: 14px;}
        .row-fluid .login-box li:first-child{background-color: #369bd7;}
        .row-fluid .login-box li:first-child a{color: #fff;}
        .row-fluid .login-box li:first-child:active{background-color: #3673d7;}
        .row-fluid .login-box ul{margin:0 20% 15px 20%;background-color: #fff;border: 1px solid #cccccc;border-radius:3px;overflow:hidden;}
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
                    <li><a href="login.vpage" class="selected">账号登录</a></li>
                    <li><a href="login_by_mobile.vpage">手机登录</a></li>
                </ul>
                <#include '../widget_alert_messages.ftl' />
                <form class="form-horizontal" method="post" action="/auth/login.vpage">
                    <input type="hidden" name="client" id="client" value="${client!}"/>
                    <fieldset>
                        <div class="input-prepend" title="用户名" data-rel="tooltip">
                            <span class="add-on"><i class="icon-user"></i></span><input autofocus class="input-large span10" name="username" id="username" type="text"/>
                        </div>
                        <div class="clearfix"></div>

                        <div class="input-prepend" title="密码" data-rel="tooltip">
                            <span class="add-on"><i class="icon-lock"></i></span><input class="input-large span10" name="password" id="password" type="password"/>
                        </div>
                        <div class="clearfix"></div>

                        <dd>
                            <input type="hidden" value="${captchaToken!0}" name="captchaToken"/>
                            <input type="text" value="" name="captchaCode" class="int" style="width: 80px;" maxlength="4"/>
                            <span style="display: inline-block; vertical-align: middle; margin-bottom: 10px;" id="captchaClick">
                                <img id="captchaImage"/>
                            </span>
                        </dd>

                        <p class="center span5">
                            <button type="submit" class="btn btn-primary">登 录</button>
                        </p>
                    </fieldset>
                </form>
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
<script type="text/javascript">
    $(function () {
        $("#captchaClick").click(function () {
            refreshCaptcha();
        });
        //验证码
        refreshCaptcha();
    });

    function refreshCaptcha() {
        $('#captchaImage').attr('src', "/captcha.vpage?" + $.param({
            'module': 'findAccount',
            'token': '${captchaToken!0}',
            't': new Date().getTime()
        }));
    }
</script>
</body>
</html>

