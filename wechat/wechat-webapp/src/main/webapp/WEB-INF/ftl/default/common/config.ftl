<#assign apptag=JspTaglibs["/WEB-INF/tld/app-tag.tld"]/>

<#-- 如果使用cdn服务，要在本来路径前面，添加wechat目录！！ -->
<#if (ProductDevelopment.isDevEnv())!false>
    <#assign path = "" />
<#else>
    <#assign path = "wechat/" />
</#if>

<#macro css href="" version="" cdnTypeFtl="auto">
    <#if cdnTypeFtl != 'skip'>
        <@apptag.css href="${path}${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
    <#else>
        <@apptag.css href="${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
    </#if>
</#macro>

<#macro script href="" version="" cdnTypeFtl="auto">
    <#if cdnTypeFtl != 'skip'>
        <@apptag.script context="" href="${path}${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
    <#else>
        <@apptag.script context="" href="${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
    </#if>
</#macro>

<#macro link href="" cdnTypeFtl="auto">
    <#if cdnTypeFtl != 'skip'>
        <@apptag.link href="${path}${href}" cdnTypeFtl="${cdnTypeFtl}" />
    <#else>
        <@apptag.link href="${href}" cdnTypeFtl="${cdnTypeFtl}" />
    </#if>
</#macro>

<#macro avatar href="">
    <#if href?? && href?has_content >
        <@apptag.link href="gridfs/${href}" cdnTypeFtl="avatar"/>
    <#else>
        <@apptag.link href="upload/images/avatar/avatar_normal.gif" cdnTypeFtl="avatar"/>
    </#if>
</#macro>

<#macro cdnImage href="" cdnTypeFtl="true">
    <@apptag.link href="gridfs/${href}" cdnTypeFtl="avatar"/>
</#macro>

<#macro link_shared href="" cdnTypeFtl="static_shared">
    <@apptag.link href="${href}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>




