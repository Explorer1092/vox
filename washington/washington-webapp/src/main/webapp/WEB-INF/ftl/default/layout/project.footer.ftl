<!--底部-->
<div class="m-footer">
    <div class="m-inner">
        <div class="m-left w-fl-left">
            <div class="copyright">
                ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
            </div>
            <div class="link">
                <a class="spare-icon spare-weibo" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
                <#--<a class="spare-icon spare-rr" href="http://t.qq.com/zone_17zuoye" target="_blank" title="QQ微博"></a>-->
                <#--<a class="spare-icon spare-wx" href="http://17zuoyeweixin.diandian.com/post/2012-08-22/40038027452" target="_blank" title="微信"></a>-->
                <#--<a class="spare-icon spare-qzone" href="http://user.qzone.qq.com/2484705684/main" target="_blank" title="QQ空间"></a>-->
            </div>
        </div>
        <div class="m-foot-link w-fl-right">
            <div class="m-left w-fl-left">
                <#--<h3>关于</h3>-->
                <a href="/help/aboutus.vpage" target="_blank">关于我们</a>
                <a href="/help/contactus.vpage" target="_blank">联系我们</a>
                <a href="/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href='javascript:;' class="js-commentsButton">我要评论</a>
                <a href='javascript:;' class="js-reportButton">我要举报</a>
            </div>
            <div class="m-left w-fl-left">
                <#--<h3>联系</h3>-->
                <a href="/help/news/index.vpage" target="_blank">新闻中心</a>
                <a href="/project/educationsubject/index.vpage" target="_blank">教育部课题</a>
                 <a href="http://help.17zuoye.com" target="_blank">帮助</a>
                <a href="/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                <a href="/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a>
            </div>
            <div class="m-code">
                <p class="c-image"></p>
                <p class="c-title">关注我们</p>
            </div>
        </div>
    </div>
</div>
<#include "../common/to_comments_report.ftl" >
