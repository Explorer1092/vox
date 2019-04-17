<#import "../../layout/project.module.ftl" as temp />
<#--teacher/reward/mobilerecharge.vpage-->
<@temp.page title="课题申请">
<@app.css href="public/skin/project/educationsubject/css/skin.css" />
<div class="topic-box">
    <div class="tlist-outer">
        <div class="tlist-middle">
            <div class="tlist-inner">
                <h3></h3>
                <div class="tlist-box" style="margin: 48px 100px 0 130px;">
                    <a class="tlist-a" href="http://keti.17zuoye.com/edusociety/index.php" target="_blank">
                        <div class="tlist-title">中国教育学会“十三五”教育科研规划课题</div>
                        <p class="tlist-p1">基于混合式作业的学生发展核心素养促进与研究</p>
                        <p class="tlist-p2">简介：</p>
                        <p class="tlist-p1 tlist-p3">本课题以国内外混合式学习的研究成果为基础，深入地探索利用混合式作业实现学生核心素养评价和促进的方法和策略...</p>
                        <span class="tlist-href">查看详情</span>
                    </a>
                </div>
                <div class="tlist-box tlist-box2" style="display: none;">
                    <a class="tlist-a" href="http://keti.17zuoye.com" target="_blank">
                        <div class="tlist-title">中国教育学会外语专业委员会“十三五”规划课题</div>
                        <p class="tlist-p1">教育信息化下的中小学在线英语学习形式探究</p>
                        <p class="tlist-p2">简介：</p>
                        <p class="tlist-p1 tlist-p3">本课题核心概念主要是研究在中小学阶段如何应用智能英语 “一起作业” 教学平台，通过网络作业的形式，开创老师、学生和家长三方共同学习和发展的局面...</p>
                        <span class="tlist-href">查看详情</span>
                    </a>
                </div>
                <div class="tlist-box tlist-box3" style="margin: 48px 0 0 0;">
                    <a class="tlist-a" href="/project/educationsubject/index.vpage" target="_blank">
                        <div class="tlist-title">全国教育科学“十二五”规划2014年度教育部重点课题</div>
                        <p class="tlist-p1">智能感知技术在中小学作业减负中的应用研究与实践探索</p>
                        <p class="tlist-p2">简介：</p>
                        <p class="tlist-p1 tlist-p3">本课题以个性化学习理论为依据，利用云计算和大数据背景下的智能感知技术，从知识水平和情感状态两个维度对学生完成作业过程中的学习活动进行感知...</p>
                        <span class="tlist-href">查看详情</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
<!--底部-->
<div class="m-footer">
    <div class="m-inner">
        <div class="w-fl-left">
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
            <div class="w-fl-left">
            <#--<h3>关于</h3>-->
                <a href="/help/aboutus.vpage" target="_blank">关于我们</a>
                <a href="/help/uservoice.vpage" target="_blank">用户声音</a>
                <a href="/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href="javascript:;" class="js-commentsButton">我要评论</a>
                <a href="javascript:;" class="js-reportButton">我要举报</a>
            </div>
            <div class="w-fl-left">
            <#--<h3>联系</h3>-->
                <a href="/help/news/index.vpage" target="_blank">新闻中心</a>
                <a href="/project/educationsubject/index.vpage" target="_blank">教育部课题</a>
                <a href="/help/kf/index.vpage?menu=teacher" target="_blank">帮助中心</a>
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
    <#include "../../common/to_comments_report.ftl" >

</@temp.page>