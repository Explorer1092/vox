<#assign apptag=JspTaglibs["/WEB-INF/tld/app-tag.tld"]/>

<#-- 如果使用cdn服务，要在本来路径前面，添加mizar目录！！ -->
<#if (ProductDevelopment.isDevEnv())!false>
	<#assign path = "" />
<#else>
	<#assign path = "mizarcdn/" />
</#if>

<#macro avatar href="">
	<#if href?exists && href?has_content >
		<@apptag.link href="gridfs/${href}" cdnTypeFtl="avatar"/>
	<#else>
		<@apptag.link href="upload/images/avatar/avatar_normal.gif" cdnTypeFtl="avatar"/>
	</#if>
</#macro>

<#macro css href="" version="">
	<@apptag.css href="${path}${href}" version="${version}" />
</#macro>

<#macro script href="" version="" cdnTypeFtl="auto">
	<@apptag.script context="" href="${path}${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro compressScript context="" href="" version="" cdnTypeFtl="auto">
	<@apptag.script context="${context}" href="compress/??${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro versionedUrl href="" version="" cdnTypeFtl="auto">
	<@apptag.versionedUrl href="${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro link href="" cdnTypeFtl="auto">
	<@apptag.link href="${path}${href}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro link_shared href="" cdnTypeFtl="static_shared">
	<@apptag.link href="${href}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro voice href><@apptag.link href="/gridfs/${href}" cdnTypeFtl="static_shared" /></#macro>

<#macro wave href><@apptag.link href="/${href}" cdnTypeFtl="static_shared" /></#macro>

<#-- 前后不能留空行 -->
<#macro book href=""><#if href?exists && href?has_content ><@apptag.link href="/upload/${href}" cdnTypeFtl="static_shared" /><#else><@apptag.link href="/upload/book_normal.png" cdnTypeFtl="static_shared" /></#if></#macro>