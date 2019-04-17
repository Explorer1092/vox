<#-- TODO 新的家长登录首页 indexOld.ftl 是以前的家长首页，如A/B测试没问题，就干掉他  -->
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
        <@sugar.check_the_resources />
        <@sugar.capsule js=["jquery","core"] />
        <@sugar.site_traffic_analyzer_begin />
        <#include "../parentmobile/constants.ftl">
        ${buildLoadStaticFileTag("", "css", "public/skin/parent/skin")}
    </head>
    <body>
        <#include "../block/detectzoom.ftl">
        <div class="parentPCDownload-mod1"></div>
        <div class="parentPCDownload-mod2"></div>
        <a href="/ucenter/logout.vpage" class="parentPCDownload-btn">退出登录</a>
        <div class="parentPCDownload-text">
            <div class="textHD">家长通电脑版已下线，请家长直接使用手机家长通， 用手机扫描下方二维码安装并登录</div>
            <div class="textCode">
                <#--100115-->
                <#--<#include "../studentv3/apps/JZT_QR.ftl">-->
                <div class="code">
                    <img src="<@app.link href="public/skin/common/images/download_parent_code.png"/>" alt="家长通二维码">
                </div>
            </div>
            <div class="textFT">你可以通过家长通实时了解孩子作业动态、查看做错考点<br>免费接收老师通知、及时与老师互动送花</div>
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