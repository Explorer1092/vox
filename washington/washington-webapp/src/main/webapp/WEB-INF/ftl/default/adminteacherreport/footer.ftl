<div class="m-footer">
    <div class="m-inner">
        <div class="m-left w-fl-left">
            <div class="copyright">
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
            </div>
            <div class="link">
                <a class="spare-icon spare-weibo" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
            </div>
        </div>
        <div class="m-foot-link w-fl-right">
            <div class="m-left w-fl-left">
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage" target="_blank">关于我们</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/uservoice.vpage" target="_blank">用户声音</a>
                <a href='javascript:;' class="js-commentsButton">我要评论</a>
                <a href='javascript:;' class="js-reportButton">我要举报</a>
            </div>
            <div class="m-left w-fl-left">
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/news/index.vpage" target="_blank">新闻中心</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/junior.vpage" target="_blank">帮助中心</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" target="_blank">课题相关</a>
                <!--<a href="http://help.17zuoye.com" target="_blank">帮助</a>-->
            </div>
        </div>
    </div>
</div>
<#include "../common/to_comments_report.ftl" >
