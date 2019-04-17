<#macro page title="模考V3.0历史" level="" headTitle="一起作业，一起作业网，一起作业学生">
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
<html xmlns="http://www.w3.org/1999/html">
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>${headTitle!'一起作业，一起作业网，一起作业学生'}</title>
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "jplayer", "jquery.flashswf"] css=["plugin.alert", "new_teacher.base", "new_teacher.widget", "new_teacher.module", "newexamv3"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body >
    <#include "../block/loading.ftl" />
<div class="mk-container">
    <!--头部-->
    <div class="mk-header">
        <div class="inner inner-2">
            <a href="/" class="logo"></a>
        </div>
    </div>
    <!--主体-->
    <!--//start-->
    <#nested>
    <!--end//-->
</div>
<!--底部-->
   <#-- <#include "../../layout/project.footer.ftl"/>-->
<script type="text/javascript">
    /*$(function(){
        //主菜单经过浮动条效果
        $(".v-menu-hover").hover(function(){ $(this).addClass("active"); }, function(){ $(this).removeClass("active"); });
    });*/
</script>
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