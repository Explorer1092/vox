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
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "alert"] css=["plugin.alert", "new_teacher.widget"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div class="loading" style="padding: 100px; text-align: center; font: 20px/150% '微软雅黑', 'Microsoft YaHei', arial">加载中...</div>
    <#--<div style="padding: 40px 70px 20px;line-height: 200%;">
        重庆市第二届外研杯小学英语优质课大赛，每两年举行一次，是在全市开展英语光盘课大赛的基础上，从所有一等奖获得者中挑选出的具有代表性的教师，再进行的一次全市性的教学比赛，可以说是优中选优！有来自全市40个区县直属学校1000余人到场观摩
    </div>-->
    <script type="text/javascript">
        $(function(){
            <#if (currentTeacherDetail.subject)?? && currentTeacherDetail.subject == "ENGLISH" && [500000]?seq_contains(currentTeacherDetail.rootRegionCode)>
                <#if currentUser.fetchCertificationState() == "SUCCESS">
                    location.href = "//cdn.17zuoye.com/static/download/cq.rar";
                    $(".loading").html("公开课视频下载中，<a href='//cdn.17zuoye.com/static/download/cq.rar' class='w-blue'>重新下载</a> 或 <a href='/teacher/index.vpage' class='w-blue'>返回首页</a>");
                <#else>
                    $(".loading").html("");
                    $.prompt("<div class='w-ag-center'>通过认证后，才可以下载公开课视频哦！</div>", {
                        title: "系统提示",
                        focus : 1,
                        buttons: { "返回首页": false, "去完成认证": true },
                        position: {width: 500},
                        submit : function(e, v){
                            if(v){
                                location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=resource";
                            }else{
                                location.href = "/teacher/index.vpage";
                            }
                            $(".loading").html("加载中...");
                        },
                        classes : {
                            close: 'w-hide'
                        }
                    });
                </#if>
            <#else>
                location.href = "/";
            </#if>
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