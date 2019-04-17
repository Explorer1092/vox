<#--
    className : 这个页面的body命名空间
    title : 标题
    requirejs : 这个页面依赖的js模块
    globalJs     :  这个页面需要的 全局 js模块  eg core
    pageJs : 本页总js  requirejs 初始化使用
-->
<#macro page className='Index' title='' globalJs =[] specialHead = "normal" extraJs = [] extraRequireJs = [] pageJs="no_require_module" specialCss=''>
<#include "constants.ftl">
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
    <#if specialHead == "normal">
        <#include "headModule.ftl">
    <#else >
        ${specialHead}
    </#if>

    ${buildLoadStaticFileTag("skin", "css")}

    <#if pageJs != "seattle">
    ${buildLoadStaticFileTag("null-page", "css")}
    </#if>

    <#if specialCss != ''>
        ${buildLoadStaticFileTag("", "css","/public/skin/parentMobile/css/" + specialCss)}
    </#if>

    <script>
        <#-- javascript的 全局变量  命名空间: PM(parentMobile) -->
        window.PM = { };

        <#if sid?has_content >
           PM.sid = ${sid};
        </#if>

        <#-- TODO FIXME 因为1.3.1 原生才在userAgent上加上 并且common.js  上报打点功能 判断移动端使用PM.isWebview  以及在打点or错误日志的时候 使用userAgent中的app_version 【当客户端V1.3 没有的时候，删除这段代码】 -->
        PM.isWebview = ${(client_name?? && client_name == "17Parent")?string};

    </script>

</head>

<body class="parentMobile${className!''}">

    <noscript>
        本网站需要javascript的支持， 请开启javascript
    </noscript>
    <#-- 新loading -->
    <div id="globalLoading" class="loading"><span class="icon"></span></div>
    <#-- 旧loading-->
    <#--<div class="parentApp-loading hide" id="globalLoading"></div>-->

    <div id="mockBody" class="hide">
        <#nested />
    </div>

	<i id="17parent" style="display:none;">17parent_loading</i>

    <#include "./mainJs.ftl">

</body>
</html>
<!doctype html>
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

