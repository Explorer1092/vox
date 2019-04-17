<#assign apptag=JspTaglibs["/WEB-INF/tld/app-tag.tld"]/>

<#macro avatar href="">
	<#if href?exists && href?has_content >
		<#if href != '/'>
        	<@apptag.link href="gridfs/${href}" cdnTypeFtl="avatar"/>
		<#else>
			<@apptag.link href="gridfs/" cdnTypeFtl="avatar"/>
		</#if>
	<#else>
        <@apptag.link href="upload/images/avatar/avatar_normal.gif" cdnTypeFtl="avatar"/>
	</#if>
</#macro>

<#macro css href="" version="">
	<@apptag.css href="${href}" version="${version}" />
</#macro>

<#macro script href="" version="" cdnTypeFtl="auto">
	<@apptag.script context="" href="${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro compressScript context="" href="" version="" cdnTypeFtl="auto">
	<@apptag.script context="${context}" href="compress/??${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro versionedUrl href="" version="" cdnTypeFtl="auto">
	<@apptag.versionedUrl href="${href}" version="${version}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro link href="" cdnTypeFtl="auto">
	<@apptag.link href="${href}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro link_shared href="" cdnTypeFtl="static_shared">
	<@apptag.link href="${href}" cdnTypeFtl="${cdnTypeFtl}" />
</#macro>

<#macro voice href><@apptag.link href="/gridfs/${href}" cdnTypeFtl="static_shared" /></#macro>

<#macro wave href><@apptag.link href="/${href}" cdnTypeFtl="static_shared" /></#macro>

<#-- 前后不能留空行 -->
<#macro book href=""><#if href?exists && href?has_content ><@apptag.link href="/upload/${href}" cdnTypeFtl="static_shared" /><#else><@apptag.link href="/upload/book_normal.png" cdnTypeFtl="static_shared" /></#if></#macro>

<#macro blueimg href="">
	<@apptag.link href="public/skin/blue/images/${href}"/>
</#macro>



<#macro client_setup_url>//cdn.17zuoye.com/download/17zuoyeSetup_1.2.1.14.exe</#macro>
<#macro client_setup_md5>d5171599b2d3fdc6c5c5bd194be832ab</#macro>

<#macro liebao_setup_url>//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe</#macro>
