<#macro page title="一起小学"  nav=0>
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
-->
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起小学，一起教育科技，一起小学学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "colorbox"] css=["plugin.alert", "common.so", "teacher.widget", "teacher.columns", "service"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <!--header-->
    <#include "../layout/project.header.ftl"/>
	<div class="main">
		<#nested>
	</div>
    <!--footer-->
    <#include "../layout/project.footer.ftl"/>
    <@sugar.site_traffic_analyzer_end />
    <script>
        $(function(){
            $("table.table_vox_striped tbody tr:odd").addClass("odd");

            $("table.table_vox_striped tbody tr").hover(function(){
                $(this).addClass("active");
            },function(){
                $(this).removeClass("active");
            });
        });
    </script>
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