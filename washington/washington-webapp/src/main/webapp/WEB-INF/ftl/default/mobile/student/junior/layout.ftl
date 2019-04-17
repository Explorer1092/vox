<#--  暂存
<#import '../../common/layout.ftl' as layout>
<#assign env = "student_junior">
<#include "../../common/config.ftl">

<#macro page className='Index' title='' viewport='default' viewportWidth='640' head = ''>>
    <@layout.commonLayout className='Index' title='' viewport='default' viewportWidth='640' head = ''>
        <#nested />
    </@layout.commonLayout>
</#macro>
-->

<#assign env = "student_junior">
<#include "../../common/config.ftl">

<#import '../../common/layout.ftl' as layout>
<#assign layout = layout>

<#assign headBlock>
    ${buildLoadStaticFileTag(CONFIG.SKINCSSPATH, "css")}
</#assign>

<#assign bottomBlock>
${buildLoadStaticFileTagByList(
[
	{
		"path" : "${DEFAULT_CONFIG.js.fastClick}",
		"type" : "js",
		"addKid" : false
	},
	{
		"path" : "${DEFAULT_CONFIG.js.reqwest}",
		"type" : "js",
		"addKid" : false
	},
	{
		"path" : "${DEFAULT_CONFIG.js.vue}",
		"type" : "js",
		"addKid" : false
	}
]
)}

<#list extraJs![] as js>
	${buildLoadStaticFileTagByList(
		[
			{
				"path" : (js.spcial!false)?string(js.path, CONFIG.JS_BASE_PATH + js.path),
				"type" : js.type!"js",
				"addKid" : js.addKid!true
			}
		]
	)}
</#list>
</#assign>

