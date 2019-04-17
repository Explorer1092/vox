<#include "./constants.ftl">
<@app.script href="public/plugin/knockoutjs-3.3.0/knockout.js"/>
<#assign examStaticJsFile = "/resources/apps/hwh5/exam/wechat/js/examCore${staticKid}.js">

<#-- TODO 开发环境定义是 : 本地开发 或 default环境 -->
<#assign isDevEnv = ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>
<#if (isDevEnv) >
    <#assign byidsUrl = "//www.test.17zuoye.net/" examEnv="dev">
<#elseif (ProductDevelopment.isStagingEnv())>
    <#assign byidsUrl = "//www.staging.17zuoye.net/" examEnv="staging">
<#-- <#elseif (ProductDevelopment.isProductionEnv())> -->
<#else>
    <#assign byidsUrl = "//www.17zuoye.com/" examEnv="prod">
</#if>

<script type="text/javascript" src="<@app.link href="${examStaticJsFile}" cdnTypeFtl="skip" />"></script>
<script>
    PM.requireOpts.suffixObj.exam = "${examSuffix!''}";
    vox.exam.env = "${examEnv}"
</script>
