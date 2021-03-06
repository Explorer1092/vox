<#macro page title="一起作业，一起作业网，一起作业学生" pageJs=[] pageJsFile={}  pageCssFile={} requireFlag=true fastClickFlag=true
htmlClass=""
bodyClass=""
keywords="17作业，作业，一起作业下载，一起作业学生，学生APP，学生端下载，在线教育平台"
description="一起作业是一款免费学习工具，是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起作业，让学习成为美好体验。"
>
<#include "webview.include.ftl"/>
<!doctype html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
<html class="${htmlClass!}">
<head>
    <meta charset="utf-8">
    <#--页面窗口自动调整到设备宽度，并禁止用户缩放页面-->
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
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
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
    <title>${title!}</title>
    <meta name="keywords" content="${keywords!}">
    <meta name="description" content="${description!}">
    <!--[if lte IE 8]>
    <script type="text/javascript">
            if(!pageGetCookie("goToKillIe")){
                window.location.href = "/project/ie/index.vpage";
            }

            function pageGetCookie(name){
                var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
                if(arr=document.cookie.match(reg))
                    return unescape(arr[2]);
                else
                    return null;
            }
        </script>
    <![endif]-->
    <#--页面配置需依赖CSS-->
    <#if pageCssFile??>
        <#list pageCssFile?keys as file>
            <#list pageCssFile[file] as k><@app.css href=(k + ".css")/></#list>
        </#list>
    </#if>
    <script type="text/javascript">
        var requirePaths = {}, requireShimPaths = {}, pageRunJs = [], signRunScript = function(){};
    </script>
</head>
<body class="${bodyClass!}">
    <#nested />

    <#include "webview.script.ftl"/>
</body>
</html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
</#macro>