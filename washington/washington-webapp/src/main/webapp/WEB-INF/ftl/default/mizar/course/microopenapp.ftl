<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="target-densitydpi=device-dpi, width=960px, user-scalable=no"/>
    <meta name="MobileOptimized" content="320" />
    <meta name="Iphone-content" content="320" />
    <meta name="apple-mobile-web-app-capable" content="no">
    <meta name='apple-touch-fullscreen' content='no'>
<#--关闭电话号码识别：-->
    <meta name="format-detection" content="telephone=no" />
<#--关闭邮箱地址识别：-->
    <meta name="format-detection" content="email=no" />
<#--指定 iOS 的 safari 顶端状态条的样式 可选default、black、black-translucent-->
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <meta http-equiv="X-UA-Compatible" content="IE=Edge, chrome=1"/>
    <title>一起作业，一起作业网，一起作业学生</title>
    <style>
        html, body{height: 100%; width: 100%; padding: 0; margin: 0;body { background-color: #f5f5f5; font: 28px/22px "微软雅黑", regular; color: #666; } }
    </style>
</head>
<body>
    <iframe src="//wx.17zuoye.com/download/17parentapp" name="0" marginwidth="0" marginheight="0" align="top" scrolling="auto" frameborder="0" style="width: 100%; height: 100%;" yqif></iframe>
    <script type="text/javascript">
        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        }

        var direct_url = encodeURIComponent(location.origin+"/mizar/course/courseperiod.vpage?track=message&period=" + getQueryString("period")),
                ua = navigator.userAgent.toLowerCase(),
                open_app_url = 'a17parent://platform.open.api:/parent_main?from=h5&type=news_detail&url='+direct_url,
                ios_version = /ip(ad|hone|od)/.test(ua) && ua.match(/os (\d+)_(\d+)/);
        if(ios_version){
            open_app_url = 'a17parent://parent_main?yq_from=h5&yq_type=webview&yq_val='+direct_url;
        }
        location.href = open_app_url;
    </script>
</body>
</html>