<#include "constants.ftl">

<script>
	<#-- TODO 兼容老版本 新版本(1.5.1)可以考虑使用 getInitParams -->
	PM.client_params = {
		app_version    : "${app_version!''}".split(',')[0],
		client_type    : "${client_type!''}",
		client_name    : "${client_name!''}",
		imei           : "${imei!''}",
		model          : "${model!''}",
		system_version : "${system_version!''}"
	};
</script>

<@sugar.capsule js=["neoJquery", "log", "fastClick", "crossProjectShare"]  />

<#--广告位voxSpread-->
<#if (['index','fairyland']?seq_contains(pageJs))!false>
    <@sugar.capsule js=['voxSpread', 'flexslider']  css=['plugin.flexslider']/>
</#if>

<#-- 这里放一些 经常需要调试的js -->
<#assign willBeDebugJs = ["targetDensitydpi", "common"] >

<#function addExtraJs jsFiles specialFilePath>
    <#assign result>
        <#list jsFiles![] as jsFile>
            <#if specialFilePath>
                ${buildLoadStaticFileTag("", "js", jsFile + staticKid)?trim}
            <#else>
                ${buildLoadStaticFileTag(jsFile, "js")?trim}
            </#if>
        </#list>
    </#assign>
    <#return result?trim>
</#function>

${addExtraJs(willBeDebugJs, false)}
${addExtraJs(extraJs![], true)}

<#--TODO 本地调试用
<script src="//jsconsole.com/remote.js?rambo"></script>
-->

<#if pageJs != 'no_require_module' >

    <#function getJsFileSuffix withOutExtUrl staticKid>

        <#assign scriptUrl>
            <@app.link href=withOutExtUrl + staticKid + '.js'/>
        </#assign>

        <#assign  suffixIndex = scriptUrl?index_of("${staticKid}-V")>
        <#if suffixIndex == -1>
            <#assign  suffix = "">
        <#else>
            <#assign  suffix = scriptUrl?substring(suffixIndex)?replace(".js", "")>
        </#if>

        <#return suffix?trim>
    </#function>
    <#assign
        pluginsModule = [
            "chart/Chart.Core",
            "chart/Chart.PolarArea",
            "chart/Chart.Doughnut",
            "chart/Chart.Line",
            "chart/Chart.Bar",
            "template"
        ]
        normalModule = [
            "ajax",
            "fullTemplate",
            "tab",
            "audio",
            "jqPopup",
            "shortHref",
            "getKids",
            "slide",
            "getFromRecordByJq",
            "sendFlower",
            "progress",
            "versionCompare",
            "build_map_params",

            "index",
        "fairyland",
            "unitReportDetail",
            "redo",
            "homework",
            "homeworkReport",
            "second",
            "ucenter",
            "studyTrack",
            "question",
            "classes",
            "error",
            "paycourseDesc",
            "seattle",
            "walkerreport"
        ]
    >
    <#assign jsFileObj = {
        "plugins" : pluginsModule,
        "normal" : normalModule
    }>

    <#assign scriptUrl>
        <@app.link href="public/script/parentMobile/index${staticKid}.js"/>
        <#-- 测试url
        "https://cdn-cc.test.17zuoye.net/public/plugirequirejs/require.2.1.9.min-V20151028122934.js"
         -->
    </#assign>

    <script>
        PM.requireOpts = {
            pageJs : "${pageJs?trim}",

            <#-- get static js file baseUrl -->
            <#assign publicIndex = scriptUrl?index_of("public")>
            <#assign  baseUrl = scriptUrl?substring(0, publicIndex)>
            baseUrl : "${baseUrl}",

            <#-- get static js files version suffix -->
            <#assign pluginSrc = "public/plugin/" normalSrc="public/script/parentMobile/">
            pluginSrc : "${pluginSrc}",
            normalSrc : "${normalSrc}",
            suffixObj : {
                <#list jsFileObj?keys as jsFileType>
                    <#assign isPlugin = jsFileType == "plugins">
                    <#assign baseUrl = isPlugin?string(pluginSrc, normalSrc)>

                    <#list jsFileObj[jsFileType] as jsFile>
                        "${jsFile}" : "${getJsFileSuffix(baseUrl + jsFile, staticKid)}"<#if jsFile_has_next || jsFileType_has_next>,</#if>
                    </#list>

                </#list>
            },

            <#-- get plugin js file -->
            isPlugin :[
                <#list pluginsModule as pluginFile>
                    "${pluginFile}"<#if pluginFile_has_next>,</#if>
                </#list>
            ]
        };
    </script>

    <#if "UnitReportDetail,WrongQuestionDetail"?index_of(className) gt -1 >
        <#assign examSuffix = getJsFileSuffix(normalSrc + "exam", staticKid)>
        <#include "examModule.ftl">
    </#if>

    <@sugar.capsule js=["requirejs"] + globalJs  + ["parentMobileRequireConfig"] />

    ${addExtraJs(extraRequireJs![], true)}

    <#if ProductDevelopment.isDevEnv()>
        <script >
            requirejs.config({
                urlArgs: "staticFileTimeStamp=" +  (new Date()).getTime(),
            });
        </script>
    </#if>

</#if>
