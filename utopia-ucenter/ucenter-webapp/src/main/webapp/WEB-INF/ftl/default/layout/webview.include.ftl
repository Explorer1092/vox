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
        <#compress>
            <#if type?has_content>
                <@app.link href=(url + (jskid!) + type)/>
            <#else>
                <@app.link href=url/>
            </#if>
        </#compress>
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
            <#return "Copyright &copy; 2011-${.now?string('yyyy')} 17ZUOYE Corporation. All Rights Reserved."/>
            <#break />
        <#case "icp">
            <#return "ICP证沪B2-20150026 沪ICP备13031855号-2 北京市公安局朝阳分局备案编号：11010502027249"/>
            <#break />
        <#default>
            <#return "---"/>
    </#switch>
</#function>