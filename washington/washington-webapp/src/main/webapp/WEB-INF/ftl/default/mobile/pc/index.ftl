<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="target-densitydpi=device-dpi, width=640px, user-scalable=no">
    <title>一起作业</title>
    <@sugar.capsule js=["jquery", "flexslider"] css=["plugin.flexslider"] />
    <@app.script href="public/skin/mobile/pc/js/fullScreenDpi.js" />
    <@sugar.site_traffic_analyzer_begin />
    <style>
        html, body{ height: 100%; overflow: hidden; background-color: #00bbff;}
        * { padding: 0; margin: 0; list-style: none; border: 0; -webkit-appearance: none; -webkit-tap-highlight-color: rgba(0,0,0,0);}
        body { font: 28px/120% "微软雅黑", regular; color: #666; }
        a { text-decoration: none; }
        ul{ list-style-type: none;}
        /*loginenter*/
        .loginenter{ width: 100%; position: relative; max-width: 720px; margin: 0 auto;}
        .loginenter .logo{ position: absolute; top: 0; left: 0; z-index: 10;}
        .loginenter .logo img{ width: 200px; height: 100px;}
        .loginenter .enter-pc{ z-index: 10; color: #fefefe; font-size: 30px; font-weight: bold; text-decoration: underline; position: absolute; top: 25px; right: 40px;}
        .loginenter .login-main{ width: 100%; overflow: hidden; position: relative; border: none; margin: 0;}
        .loginenter .login-main ul{ width: 500%;}
        .loginenter .login-main ul li{ float: left;}
        .loginenter .login-main ul li img{ width: 100%;}
        .loginenter .flex-control-nav{ bottom: 150px; position: fixed;}
        .loginenter .flex-control-nav a{ display: inline-block; cursor: pointer; margin-left: 25px; width: 20px; height: 20px; border-radius: 20px; background: #bff1ff;}
        .loginenter .flex-control-nav a.flex-active{ width: 50px; background: #fff;}
        .loginenter .flex-direction-nav{ display: none;}
        .loginenter .login-btn{ width: 100%; height: 90px; position: fixed; bottom: 43px; text-align: center;}
        .loginenter .login-btn a{ display: inline-block; margin-left: 12px; font-size: 40px; color: #fefefe; text-align: center; height: 90px; line-height: 80px; background: url(<@app.link href="public/skin/mobile/pc/images/index/wx-btns.png"/>) no-repeat;}
        .loginenter .login-btn a.core-tea{ width: 255px; background-position: 0 0;}
        .loginenter .login-btn a.core-stu{ width: 342px; background-position: -298px 0;}
    </style>
    <script type="text/javascript">
        $(function() {
            $(".flexslider").flexslider({
                animation : "slide",
                slideshowSpeed: 4000, //展示时间间隔ms
                animationSpeed: 400, //滚动时间ms
                direction : "horizontal",//水平方向
                touch: true //是否支持触屏滑动
            });
        });
    </script>
</head>
<body>
<div class="loginenter">
    <div class="login-header">
        <a href="javascript:void(0)" class="logo"><img src="<@app.link href="public/skin/mobile/pc/images/index/logo.png"/>"/></a>
        <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=back" class="enter-pc">访问电脑版</a>
    </div>
    <div class="flexslider login-main">
        <ul class="slides">
            <li><img src="<@app.link href="public/skin/mobile/pc/images/index/wx-banner01.png"/>"></li>
            <li><img src="<@app.link href="public/skin/mobile/pc/images/index/wx-banner02.png"/>"></li>
            <li><img src="<@app.link href="public/skin/mobile/pc/images/index/wx-banner03.png"/>"></li>
        </ul>
    </div>
    <div class="login-btn">
        <a href="http://wx.17zuoye.com/teacher/login?ref=wap" class="core-tea">我是老师</a>
        <a href="http://wx.17zuoye.com/download/17studentapp?cid=100150&ref=wap" class="core-stu">我是学生/家长</a>
    </div>
</div>
<@sugar.site_traffic_analyzer_end />
</body>
</html>
