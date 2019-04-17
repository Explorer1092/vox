<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"/>
    <meta name="format-detection" content="telephone=no"/>
    <meta name="format-detection" content="email=no"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black"/>
    <title>阿分题</title>
    <script type="text/javascript">
        function myBrowser() {
            var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
            var isIE = userAgent.indexOf("compatible") > -1 && (userAgent.indexOf("MSIE 5.5") > -1 || userAgent.indexOf("MSIE 6.0") > -1 || userAgent.indexOf("MSIE 7.0") > -1 || userAgent.indexOf("MSIE 8.0") > -1); //判断是否IE浏览器
        }
    </script>
<@sugar.capsule js=["jquery", "core"] css=[] />
<@app.css href="public/ie/css/skin.css?1.0.2"/>
    <style>
        @media only screen and (max-width: 1000px) {
            .section02 {
                display: none;
            }
        }
    </style>
</head>
<body>
<div style="padding: 3% 3% 0; height: 90%">
<div class="upgradeBox" style="margin: 0; position: static; padding: 0; width: auto; height: 100%; ">
    <div class="header">
        <span class="logo"></span>
    </div>
    <div class="main" style="width: 100%; margin: 0; padding: 0;">
        <div class="section01" style="margin: 0 auto; height: auto">
            <div class="upgrade-pic" style="float: none; margin: 0 auto;"></div>
            <div class="upgrade-text" style="margin: 0; text-align: center;">
                <h3>《阿分题》正在系统升级，升级期间无法使用。
                    全国各地将于8月23日傍晚前陆续完成升级 。
                    升级后可以继续使用，体验更棒的阿分题新版本。</h3>
            </div>
        </div>
        <div class="section02">
            <div style="clear: both; padding: 40px; text-align: center; ">
                <a href="/student/fairyland/index.vpage" style="color: #39f; font-size: 14px; text-decoration: underline;"><h2>返回课外乐园>></h2></a>
            </div>
        </div>
    </div>
</div>
</div>
</body>
</html>