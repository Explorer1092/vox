<!DOCTYPE html>
<html>
<head>
    <title>Module</title>
    <meta charset="utf-8">
    <#--离线应用的另一个技巧-->
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <#--指定的iphone中safari顶端的状态条的样式-->
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <#--告诉设备忽略将页面中的数字识别为电话号码-->
    <meta name="format-detection" content="telephone=no" />
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />
    <meta name="viewport" content="target-densitydpi=device-dpi,width=640, user-scalable=no" />
    <meta name="MobileOptimized" content="320" />
    <meta name="Iphone-content" content="320" />
    ${buildLoadStaticFileTag("", "css", basePublicPath + "css/skin_mobile")}
</head>
<body>
    <div class="t-student-downloadPage">
        <div class="banner"></div>
        <div class="btn">
            <a href="${studentDownUrl}" >立即下载</a>
        </div>
        <div class="info">（支持小学和中学）</div>
        <div class="foot">
            <a href="javascript:void(0);" class="upward-slider">向上滑动下载家长端<i class="slider-icon"></i></a>
        </div>
    </div>
    <div class="t-parent-downloadPage">
        <div class="banner"></div>
        <div class="btn">
            <a href="${parsentDownUrl}" >立即下载</a>
            <div class="info">（只支持小学）</div>
        </div>
    </div>

    ${buildLoadStaticFileTag("targetDensitydpi", "js")}
    <script>
        var adaptUILayout = window.adaptUILayout;
        (typeof adaptUILayout === "function") && adaptUILayout(640);
    </script>
</body>
</html>

