<#macro page title='' pageJs="" >
<!doctype html>
<#if (!ProductDevelopment.isDevEnv())!false>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
</#if>
<html class="${htmlClass!}">
<head>
    <script type="text/javascript">
        var pf_time_start = +new Date(); //性能统计时间起点
    </script>
    <meta charset="utf-8">
    <#--页面窗口自动调整到设备宽度，并禁止用户缩放页面-->
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <#--关闭电话号码识别：-->
    <meta name="format-detection" content="telephone=no" />
    <#--关闭邮箱地址识别：-->
    <meta name="format-detection" content="email=no" />
    <#--指定 iOS 的 safari 顶端状态条的样式 可选default、black、black-translucent-->
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <link href="https://17zuoye.com/favicon.ico" rel="shortcut icon">
    <title>${title?has_content?string("${title!}","一起作业")}</title>
    <@sugar.check_the_resources />
    <@sugar.capsule css=['jquery-weui'] />
    <script type="text/javascript">
        var LoggerProxy = {
            openId: '${openId!0}',
            subject: '${currentSubject!'MATH'}',
            currentUserType : '${currentUserType!0}',
            currentUserId : '${currentUserId!0}',
            subjectList: ${currentSubjectList![]},
            wechatJavaToPythonUrl : '<@ftlmacro.wechatJavaToPython />'
        };

        window.isFromTeacherApp = window.navigator.userAgent.toLowerCase().indexOf("17teacher") > -1;
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
    <@sugar.site_traffic_analyzer />
</head>
<body class="${bodyClass!}">
    <#nested>
    <#include  "script.ftl" />
</body>
</html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
</#macro>