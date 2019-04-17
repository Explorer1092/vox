<#macro page title="华罗庚金杯数学竞赛" pageJs=[] pageJsFile={}  pageCssFile={} requireFlag=true fastClickFlag=true>
<#include "function.ftl"/>
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
    <title>${title!}</title>
    <meta name="keywords" content="${keywords!}">
    <meta name="description" content="${description!}">
<#--页面配置需依赖CSS-->
    <#if pageCssFile??>
        <#list pageCssFile?keys as file>
            <#list pageCssFile[file] as k>
                <link href="${k}.css" rel="stylesheet" type="text/css" />
            </#list>
        </#list>
    </#if>

    <script type="text/javascript">
        var requirePaths = {}, requireShimPaths = {}, pageRunJs = [], signRunScript = function(){};
    </script>
</head>
<body class="${bodyClass!}">
    <#nested />
    <#if title?has_content>
    <script type="text/javascript">
        document.title = "${title!''}";
    </script>
    </#if>

    <#if requireFlag!false>
    <script src="/public/plugin/requirejs/require.2.1.9.min.js" type="text/javascript"></script>
    <#--JSLoad-->
    <script type="text/javascript">
        <#--配置JS模块包-->
        var paths = {
            'jquery' : "/public/plugin/jquery/jquery-1.7.1.min",
            'knockout' : "/public/plugin/knockoutjs-3.3.0/knockout",
            'prompt' : "/public/plugin/jquery-impromptu/jquery-impromptu"
        };

        <#--页面配置需启动JS-->
            <#if pageJsFile??>
                <#list pageJsFile?keys as file>
                paths["${file}"] = "${pageJsFile[file]}";
                </#list>
            </#if>

        pageExtend(paths, requirePaths);

        <#--依赖JS或CSS-->
        var shimPaths = {
            'jquery': {
                exports: "jquery"
            },
            'prompt' : {deps : ['jquery', 'css!/public/plugin/jquery-impromptu/impromptu-atuo-ui']}
        };

        pageExtend(shimPaths, requireShimPaths);

        requirejs.config({
            paths : paths,
            map : {
                '*' : {
                    css : "/public/plugin/require-css/css.min.js"
                }
            },
            shim : shimPaths,
            urlArgs: ""
        });

        <#--组件自动加载-->
        var pageJs = [${(getPageJs(pageJs))!}];

        Array.prototype.push.apply(pageRunJs, pageJs);

        require(pageRunJs, signRunScript);

        function pageExtend(child, parent){
            var $key;
            for($key in parent){
                if(parent.hasOwnProperty($key)){
                    child[$key] = parent[$key];
                }
            }
        }
    </script>
    </#if>
</body>
</html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
</#macro>