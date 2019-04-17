<#macro page proxy=true>
<!DOCTYPE html>
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
        <#include "../nuwa/meta.ftl" />
		<title>一起作业，一起作业网，一起作业学生</title>
        <@sugar.capsule js=["jquery", "toolkit", "core", "swfupload", "jmp3", "qtip"] css=["red.widget", "red.inside", "common.practice"] />
        <@sugar.site_traffic_analyzer_begin />
	</head>
	<body>
		<div class="user_inner_wrap">
			<#nested>
		</div>
		<div id="__iframe_downLoad_box" style="display:none;"></div>
		<!-- 数据加载 -->
		<div id="loading" style="display:none;padding:5px 10px;color:#FFF;font-size:12px;font-weight:700;background-color:#CC0000;position:absolute;top:0;right:0;z-index:9999;">数据加载中...</div>
        <div id="pageLoding" style="display: none;">
            <div class="pageLoding"></div>
        </div>
		<#if proxy >
        <script type="text/javascript">
			/** 定义常量  */
			var APP_MIN_HEIGHT = 650, APP_TIMER = 2000,
			APP_LOADER_FILE = "<@sugar.capsule js=["loader"] />",
			APP_PROXY_FILE = "/public/plugin/html-proxy/proxy.html";
			/** 动态加载loader脚本文件（支持跨域）  */
			var oScript= '<script type = "text/javascript" \
				src = "' + APP_LOADER_FILE + '" \
				data-proxy = "' + APP_PROXY_FILE + '" \
				data-frameid = "iframe_01" \
				data-sideid = "sider_01" \
				data-minheight = ' + APP_MIN_HEIGHT + ' \
				data-timer = ' + APP_TIMER + '> \
				<\/script>';
			document.write(oScript);

			$(function(){
                /** 初始化框架参数  */
                var oIfrm = $("#iframe_01", window.parent.document);
                oIfrm.height(APP_MIN_HEIGHT);

                $("#loading").ajaxStart(function () {
                    $(this).show();
                }).ajaxStop(function () {
                    $(this).hide();
                });
			});
		</script>
		</#if>
    <@sugar.site_traffic_analyzer_end />
	</body>
</html>
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