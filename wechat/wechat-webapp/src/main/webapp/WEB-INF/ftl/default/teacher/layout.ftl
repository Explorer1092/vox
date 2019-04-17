<#--title : 标题 requirejs : 这个页面依赖的js模块 pageJs : 本页总js  requirejs 初始化使用-->
<#macro page title='' pageJs="" >
<!DOCTYPE HTML>
<#if (!ProductDevelopment.isDevEnv())!false>
<#--本地调试不启用-->
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
</#if>

<html>
<head>
    <script type="text/javascript">
        var pf_time_start = +new Date(); //性能统计时间起点
    </script>
    <meta charset="utf-8"/>
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <meta name="format-detection" content="telephone=no" />

    <meta name="viewport" content="target-densitydpi=device-dpi, width=640, user-scalable=no">
    <#--<meta name="viewport" content="initial-scale=1, width=device-width, maximum-scale=1, user-scalable=no">-->
    <link href="https://17zuoye.com/favicon.ico" rel="shortcut icon">
    <title>${title?has_content?string("${title!}","一起作业")}</title>
    <@sugar.check_the_resources />
    <@sugar.capsule css=['weui','widget'] />
    <script type="text/javascript">
        var LoggerProxy = {
            openId: '${openId!0}',
            subject: '${currentSubject!'MATH'}',
            currentUserType : '${currentUserType!0}',
            currentUserId : '${currentUserId!0}',
            subjectList: ${currentSubjectList![]}
        };
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
        (function(){var d=document.documentElement,e=function(){var b=d.getBoundingClientRect().width;d.style.fontSize=0.0625*(1080<=b?1080:b)/2+"px"},f=null;window.addEventListener("resize",function(){clearTimeout(f);f=setTimeout(e,300)});e()})();
    </script>
    <@sugar.site_traffic_analyzer />
</head>

<body>
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
