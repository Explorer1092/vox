<#macro page title="" type="" phoneType="servicePhone" header="show">
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
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "new_teacher.widget", "specialskin"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div id="specialHeader" data-val="teacher" <#if header != "show">style="display: none"</#if>></div>
    <!--//start-->
        <#nested>
    <!--end//-->
    <div id="footerPablic" data-type="${phoneType}"></div>
    <script src="//cdn.17zuoye.com/static/project/module/js/project-plug.js"></script>
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
