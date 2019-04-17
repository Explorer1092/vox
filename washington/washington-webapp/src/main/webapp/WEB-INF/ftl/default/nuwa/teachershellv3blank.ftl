<#--使用于没有任何导航的页面 为了解决不支持https的页面-->
<#macro page show="main" showNav="show" title="">
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
    <title>一起作业，一起作业网，一起作业学生</title>
    <#include "meta.ftl" />
    <@sugar.check_the_resources />
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "base", "DD_belatedPNG_class","ko"] css=["plugin.alert", "new_teacher.basev1", "new_teacher.widget", "new_teacher.module"] />

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
            cityCode    : "${(currentTeacherDetail.cityCode)!0}"
        };
    </script>
    <script type="text/javascript">
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
</head>
<body>
    <div class="m-header">
        <div class="m-inner">
            <div class="logo" style="width: 200px;"><a href="javascript:void (0);" style="cursor: default;"></a></div>
        </div>
    </div>

    <!--//start-->
    <div style="width: 1000px; margin: 0 auto;">
        <#nested>
    </div>
    <!--end//-->

    <div class="m-footer">
        <div class="m-inner">
            <div class="w-fl-left">
                <div class="copyright">
                ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
                </div>
                <div class="link">
                    <a class="spare-icon spare-weibo" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
                </div>
            </div>
            <div class="m-foot-link w-fl-right">

                <div class="m-code">
                    <p class="c-image"></p>
                    <p class="c-title">关注我们</p>
                </div>
            </div>
        </div>
    </div>

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