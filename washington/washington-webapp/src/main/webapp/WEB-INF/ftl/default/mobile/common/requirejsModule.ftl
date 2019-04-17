<#include "constants.ftl">

<#if requirejsConfig?exist>

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
        pluginsModule = requirejsConfig.plugins,
        normalModule = requirejsConfig.normal
        jsFileObj = {
            "plugins" : pluginsModule,
            "normal" : normalModule
        }
        scriptUrl = '<@app.link href="public/script/parentMobile/index${staticKid}.js"/>'
    >

    <script>
        PM.requireOpts = {
            module : "${requirejsConfig.pageJs?trim}",

            <#-- get static js file baseUrl -->
            <#assign
				publicIndex = scriptUrl?index_of("public")
				baseUrl = scriptUrl?substring(0, publicIndex)
			>
            baseUrl : "${baseUrl}",

            <#-- get static js files version suffix -->
            <#assign pluginSrc = "public/plugin/" >
            pluginSrc : "${pluginSrc}",
            normalSrc : "${requirejsConfig.normalSrc}",
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

    <@sugar.capsule js=["requirejs"] + (globalJs![])  + ["parentMobileRequireConfig"] />

    ${addExtraJs(requirejsConfig.extraRequireJs![], true)}

    <#if ProductDevelopment.isDevEnv()>
        <script >
            requirejs.config({
                urlArgs: "staticFileTimeStamp=" +  (new Date()).getTime(),
            });
        </script>
    </#if>

</#if>
