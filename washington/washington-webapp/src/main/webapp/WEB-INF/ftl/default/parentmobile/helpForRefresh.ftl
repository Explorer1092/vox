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
        <#include "headModule.ftl">
        ${buildLoadStaticFileTag("skin", "css")}
    </head>
    <body>
        <#if result.success>
            <#assign tipText = "正在跳转..." tipType = "qinqin">
            <#include "./tip.ftl">
        <#else>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>
    </body>
    <#assign  url = (result.url!"")?trim>
    <script>
        <#if result.success && (url != "")>
            if(navigator.userAgent.toLowerCase().indexOf('android') != -1){
                window.external.goHome("0");
                window.location.href = "${url}";
            }else{
                location.replace("${url}");
            }
        </#if>
    </script>
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
