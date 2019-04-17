<#macro page title="" t="1">
<!DOCTYPE html>
<html>
    <head>
        <#include "../../nuwa/meta.ftl" />
        <title>一起作业，一起作业网，一起作业学生</title>
        <@sugar.capsule js=["jquery"] css=["plugin.register"] />
        <@sugar.site_traffic_analyzer_begin />
    </head>
    <body>
    <div class="main">
        <div class="logo"><a href="//" title="了解一起作业"></a></div>
        <#nested>
        <div class="backLogin"><a href="/"> <i class="clrblue"><< 返回首页</i></a></div>
        <div class="footer">
            <p class="navs">
                <a href='${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage'>关于我们</a><span>•</span>
                <a href='${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage'>诚聘英才</a><span>•</span>
                <a href='${(ProductConfig.getMainSiteBaseUrl())!''}/help/contactus.vpage'>联系我们</a><span>•</span>
                <a href='${(ProductConfig.getMainSiteBaseUrl())!''}/help/parentsguidelines.vpage'>家长须知</a><span>•</span>
                <a href='${(ProductConfig.getMainSiteBaseUrl())!''}/help/childrenhealthonline.vpage'>儿童健康上网</a><span>•</span>
                <a href='${(ProductConfig.getMainSiteBaseUrl())!''}/help/index.vpage'>帮助</a>
            </p>
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
    <@sugar.site_traffic_analyzer_end />
    </body>
</html>
</#macro>