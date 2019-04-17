<#--title : 标题 requirejs : 这个页面依赖的js模块 pageJs : 本页总js  requirejs 初始化使用-->
<#macro page title='' pageJs="" bodyClass="" specialHead = "normal">
<!DOCTYPE HTML>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <script type="text/javascript">
        var pf_time_start = +new Date(); //性能统计时间起点
    </script>
    <#if specialHead == "normal">
        <meta charset="utf-8"/>
        <#--<meta name="viewport" content="target-densitydpi=device-dpi, width=640px, user-scalable=no"/>-->
        <meta name="viewport" content="width=device-width, user-scalable=no"/>
        <meta name="format-detection" content="telephone=no"/>
        <link href="https://17zuoye.com/favicon.ico" rel="shortcut icon">
        <title>${title?has_content?string("${title!}","一起作业")}</title>
    <#else >
        ${specialHead}
    </#if>
    <@sugar.check_the_resources />
    <#if !(['thanksgiving']?seq_contains(pageJs))>
        <#--<@sugar.capsule css=['base'] />-->
        <@sugar.capsule />
    </#if>

    <script type="text/javascript">
        var LoggerProxy = {
            openId: '${openId!0}',
            currentUserType : '${currentUserType!0}'
        };

        <#-- 不使用document.referrer 是因为某些android不兼容.并且 有些 webview  是重新打开一个webview 相当于打开一个新窗口 获取不到 referre -->
        window.isFromParent = window.navigator.userAgent.toLowerCase().indexOf("17parent") > -1;

        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
    <@sugar.site_traffic_analyzer />
</head>

<body class="${bodyClass}" >
    <#nested>
<#include  "script.ftl" />
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
</#macro>
