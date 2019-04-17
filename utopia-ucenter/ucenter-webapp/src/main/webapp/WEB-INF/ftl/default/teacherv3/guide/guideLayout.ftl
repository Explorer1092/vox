<#macro page>
<!DOCTYPE HTML>
<html>
<head>
    <title>一起作业，一起作业网，一起作业学生</title>
    <#include "../../nuwa/meta.ftl" />

    <@sugar.capsule js=["jquery", "core", "template", "alert"] css=["plugin.alert", "new_teacher.guideStep", "new_teacher.widget"] />

    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <@ftlmacro.oldIeInfoBox />
    <#nested>
    <div class="build_footer_box">
        <div class="build_main_box">
            <div class="left">
                ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
                <p class="spare">
                    <a class="spare_img spare_weibo" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
                    <a class="spare_img spare_rr" href="http://t.qq.com/zone_17zuoye" target="_blank" title="QQ微博"></a>
                    <a class="spare_img spare_qq" href="http://user.qzone.qq.com/2484705684/main" target="_blank" title="QQ空间"></a>
                    <#--<a class="spare_img spare_wx" href="http://17zuoyeweixin.diandian.com/post/2012-08-22/40038027452" target="_blank" title="微信"></a>-->
                    <a class="spare_img spare_space" href="http://user.qzone.qq.com/2484705684/main" target="_blank" title="QQ空间"></a>
                </p>
            </div>
            <div class="right">
                <ul>
                    <li class="font"><a href="javascript:void(0);">关于</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage" target="_blank">关于我们</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/childrenhealthonline.vpage" target="_blank">儿童健康上网</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/privacyprotection.vpage" target="_blank">隐私保护</a></li>
                    <li><a href="javascript:;" class="js-commentsButton">我要评论</a></li>
                    <li><a href="javascript:;" class="js-reportButton">我要举报</a></li>
                </ul>
                <ul style="width: 112px">
                    <li class="font" ><a href="javascript:void(0);">联系</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/contactus.vpage" target="_blank">联系我们</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/parentsguidelines.vpage" target="_blank">家长须知</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/teacher/homework.vpage" target="_blank">帮助</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a></li>
                </ul>
                <ul style="width: 90px">
                    <li class="weixin"><a href="javascript:void(0);"></a></li>
                    <li style="text-align: center;">关注我们</li>
                </ul>
            </div>
        </div>
    </div>
<#include "../../common/to_comments_report.ftl" >
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>