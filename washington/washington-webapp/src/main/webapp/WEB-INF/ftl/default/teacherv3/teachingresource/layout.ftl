<#macro page show="main" title="">

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
    <script type="text/javascript">
        var pf_time_start = +new Date(); //性能统计时间起点
    </script>
    <title>一起教育科技，让学习成为美好体验</title>
    <#include "../../nuwa/meta.ftl" />
    <@sugar.check_the_resources />
    <@sugar.capsule js=["jquery", "core", "alert", "template", "DD_belatedPNG_class"] css=["plugin.alert"] />

    <@sugar.site_traffic_analyzer_begin />

    <#if (currentTeacherWebGrayFunction.isAvailable("Browser", "Upgrade"))!false>
        <!--[if lte IE 8]>
        <script type="text/javascript">
            if(!$17.getCookieWithDefault("goToKillIe")){
                window.location.href = "/project/ie/index.vpage";
            }
        </script>
        <![endif]-->
    </#if>

    <script type="text/javascript">
        var $uper = {
            userId      : "${(currentUser.id)!}",
            userName    : "${(currentUser.profile.realname)!}",
            userAuth    : "${((currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS")?string}",
            subject     : {
                key     : "${(currentTeacherDetail.subject)!}",
                name    : "${(currentTeacherDetail.getSubject().getValue())!}"
            },
            isOpenVoxLog: true,
            cityCode    : "${(currentTeacherDetail.cityCode)!0}",
            env         : <@ftlmacro.getCurrentProductDevelopment />
        };
    </script>
    <script type="text/javascript">
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
</head>
<body id="ng-app" ng-app="Soul" class="ng-app:Soul">
    <@ftlmacro.oldIeInfoBox />
    <#include "../block/loading.ftl" />
<!--主体-->
    <#nested>
<!--底部-->
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