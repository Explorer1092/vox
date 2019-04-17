<#--
	className     : 这个页面的body命名空间
	title         : 标题
	viewport      : 是否需要特殊配置的viewport
	viewportWidth : 仅修改viewport width 属性
	headBlock     : head配置
	bottomBlock   : Bottom配置
-->
<#macro page
	className     = 'Index'
	title         = ''
	viewport      = 'default'
	viewportWidth = '640'
    headBlock     = ''
    bottomBlock   = ''
>
<#include "./config.ftl">
<!DOCTYPE HTML>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
	<head>
		<#include "./head.ftl">

		<#list ["reset"] as common_css>
            ${buildLoadStaticFileTag(COMMON_SKIN_PATH + common_css, "css")}
		</#list>

		${headBlock!""}
	</head>
	<body class="${className!''}">
		<noscript> 本网站需要javascript的支持， 请开启javascript </noscript>

		<#nested />

		<script>
			window.app = {
				viewportWidth : "${viewportWidth}"
			};
		</script>

		${buildLoadStaticFileTagByList(
			[
				{
					"path" : "${DEFAULT_CONFIG.js.dpi}",
					"type" : "js"
				},
				<#--  现在log.js 还是基于jquery的一些方法 等把依赖清除后, 加入log.js
				{
					"path" : "${DEFAULT_CONFIG.js.log}",
					"type" : "js"
				},
				-->
				{
					"path" : "${DEFAULT_CONFIG.js.common}",
					"type" : "js"
				}
			]
		)}

		<script>
			typeof window.adaptUILayout === "function" && window.adaptUILayout(${viewportWidth});
		</script>

		${bottomBlock!""}
	</body>
</html>
<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>

</#macro>

