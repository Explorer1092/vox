<#macro page title="听力材料" level="" headTitle="一起作业，一起作业网，一起作业学生">
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
    <#include "../nuwa/meta.ftl" />
    <title>${headTitle!'一起作业，一起作业网，一起作业学生'}</title>
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "jplayer", "jquery.flashswf"] css=["plugin.alert", "new_teacher.base", "new_teacher.widget", "new_teacher.module", "new_teacher.quiz", "new_teacher.tts"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body >
<!--头部-->
<div class="m-header">
    <div class="m-inner">
        <div class="logo" style="width: 200px;"><a href="/"></a></div>
        <div class="link ">
            <ul class="w-fl-right">
                <#if currentUser??>
                    <li class="v-menu-hover pull-down">
                        <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                        <div class="title">
                            <a href="javascript:void(0);"><span class="w-icon w-icon-8"></span><span class="w-icon-md"><#if currentUser.profile.realname?has_content>${(currentUser.profile.realname)!?substring(0, 1)}</#if>老师</span></a>
                        </div>
                    </li>
                <#else>
                    <li class="v-menu-hover v-menu-click" style="width: 60px;">
                        <div class="title">
                            <a href="/login.vpage"><span class="w-icon-md">登录</span></a>
                        </div>
                    </li>
                    <li class="v-menu-hover v-menu-click"" style="width: 60px;">
                        <div class="title">
                            <a href="/signup/index.vpage"><span class="w-icon-md">注册</span></a>
                        </div>
                    </li>
                </#if>
                <li class="v-menu-hover v-menu-click">
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="/"><span class="w-icon w-icon-2"></span><span class="w-icon-md">返回首页</span></a>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</div>
<!--主体-->
<!--//start-->
<#nested>
<!--end//-->

<!--底部-->
<#include "../layout/project.footer.ftl"/>
<script type="text/javascript">
    $(function(){
        //主菜单经过浮动条效果
        $(".v-menu-hover").hover(function(){ $(this).addClass("active"); }, function(){ $(this).removeClass("active"); });
    });
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