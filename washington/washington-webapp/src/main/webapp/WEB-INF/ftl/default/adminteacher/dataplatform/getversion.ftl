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