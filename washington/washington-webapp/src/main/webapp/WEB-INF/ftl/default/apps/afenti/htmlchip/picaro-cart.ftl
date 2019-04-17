<@app.css href="public/skin/project/afenti/picaro/skin.css"/>
<div class="picaro-main">
    <div class="bar">
        <div class="inner">
            <h2 class="logo">
                <a href="/"></a>
            </h2>
            <div class="productView" id="productMainList">
                <div class="revealCtn">
                    <h1>PICARO</h1>
                    <p class="info">
                        <span>剑桥研发，纯正英语环境，学习更有趣</span>
                        <strong style="display: none;"></strong>
                    </p>
                    <p class="period">
                        周期：
                        <i class="sel"><b>30</b>天</i>
                        <i><b>180</b>天</i>
                    </p>
                    <p class="period price">价格：<span><b><em>￥</em>49.00</b></span></p>
                    <p class="btn">
                        <a class="getOrange" href="javascript:void(0);"></a>
                    </p>
                </div>
            </div>
        </div>
    </div>
    <div class="head">
        <div class="inner"></div>
    </div>
    <div class="con">
        <div class="inner"></div>
    </div>
    <div class="intro-me">
        <div class="inner"></div>
    </div>
    <div class="back">
        <div class="inner">
            <div class="back-top">
                <a href="#top"></a>
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
                <a class="spare-icon spare-wx" href="http://17zuoyeweixin.diandian.com/post/2012-08-22/40038027452" target="_blank" title="微信"></a>
                <#--<a class="spare-icon spare-qzone" href="http://user.qzone.qq.com/2484705684/main" target="_blank" title="QQ空间"></a>-->
            </div>
        </div>
        <div class="m-foot-link w-fl-right">
            <div class="w-fl-left">
                <a href="/help/aboutus.vpage" target="_blank">关于我们</a>
                <a href="/help/contactus.vpage" target="_blank">联系我们</a>
                <a href="/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href="/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                <a href='javascript:;' class="js-commentsButton">我要评论</a>
                <a href="javascript:;" class="js-reportButton">我要举报</a>
            </div>
            <div class="w-fl-left">
                <a href="/help/parentsguidelines.vpage" target="_blank">家长须知</a>
                <a href="/help/childrenhealthonline.vpage" target="_blank">儿童健康上网</a>
                <a href="http://help.17zuoye.com" target="_blank">帮助</a>
                <a href="/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a>
            </div>
            <div class="m-service">
                <p class="c-title">
                    咨询时间8:00-21:00
                    <strong><@ftlmacro.hotline phoneType="student"/></strong>
                </p>
                <p class="c-btn">
                    <a href="javascript:void(0);" id="message_right_sidebar">反馈建议</a>
                    <script type="text/javascript">
                        $(function(){
                            var __t = new Date().getHours();
                            var vHotLine = $("a.v-hotline");
                            if(__t < 9 || __t >= 21){
                                vHotLine.addClass("disabled");
                                vHotLine.removeAttr("onclick");
                            }else{
                                vHotLine.removeClass("disabled");
                            }
                        });
                    </script>
                </p>
            </div>
        </div>
    </div>
</div>
<#include "../../../common/to_comments_report.ftl" >
