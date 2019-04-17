<#--JSKid-->
<#assign jskid = "" />
<#if (!ProductDevelopment.isDevEnv())!false>
    <#assign jskid = ".min" />
</#if>

<#--兼容String pageJs or Array pageJs-->
<#function getPageJs(items)>
    <#if items?has_content>
        <#compress >
            <#if ((items?is_string))!false>
                <#return "'${items}'"/>
            <#else>
                <#assign names = ""/>
                <#list items as i>
                    <#assign names = (i_has_next)?string("${names + i }'" +  ", '", "'${names + i}'")/>
                </#list>
                <#return names>
            </#if>
        </#compress>
    </#if>
</#function>

<#--获取带版本Url-->
<#function getVersionUrl(url, type="")>
    <#assign versionUrl>
        <#if type?has_content>
            <@app.link href=(url + (jskid!) + type)/>
        <#else>
            <@app.link href=url/>
        </#if>
    </#assign>

    <#if (type == ".css" && versionUrl?index_of('?') == -1)!false>
        <#return versionUrl?replace(".css", "")>
    <#elseif (type == ".js" && versionUrl?index_of('?') == -1)!false>
        <#return versionUrl?replace(".js", "")>
    <#else>
        <#return versionUrl>
    </#if>
</#function>

<#--获取网站基础信息-->
<#function getWebInfo(type)>
    <#switch type>
        <#case "juniorTel">
            <#return "400-160-1717"/><#--初中客服电话-->
            <#break />
        <#case "tel">
            <#return "400-160-1717"/><#--公用客服电话-->
            <#break />
        <#case "isDev"><#--isDev 本地-->
            <#return (ProductDevelopment.isDevEnv())!false/>
            <#break />
        <#case "isTest"><#--isTest 测试-->
            <#return (ProductDevelopment.isTestEnv())!false/>
            <#break />
        <#case "isStaging"><#--isStaging 预发布环境-->
            <#return (ProductDevelopment.isStagingEnv())!false/>
            <#break />
        <#case "copyright">
            <#return "${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}"/>
            <#break />
        <#case "icp">
            <#return ""/>
            <#break />
        <#default>
            <#return "---"/>
    </#switch>
</#function>