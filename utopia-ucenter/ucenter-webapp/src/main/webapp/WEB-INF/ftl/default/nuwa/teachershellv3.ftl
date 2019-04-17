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
    <title>一起教育科技，让学习成为美好体验</title>
    <#include "meta.ftl" />
    <@sugar.check_the_resources />
    <@sugar.capsule js=["jquery", "core", "alert", "template", "teacher", "DD_belatedPNG_class"] css=["plugin.alert", "new_teacher.basev1", "new_teacher.widget", "new_teacher.module"] />

    <@sugar.site_traffic_analyzer_begin />
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
<div class="w-opt-back" style="z-index: 150; display: <#if first!false>block<#else>none</#if>;"
     id="showTipOptBack"></div>
<!--头部-->
<div class="m-header">
    <div class="m-inner">
        <div class="logo logo-back"><a href="/" title="返回首页"></a></div>
        <div class="link ">
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <ul class="w-fl-left">
                    <#assign grayCityTeacher = (currentTeacherWebGrayFunction.isAvailable("Reward", "Close"))!false />
                    <#assign grayCityTeacher2 = ((currentTeacherWebGrayFunction.isAvailable("Reward", "CloseSchool") && (.now>="2019-03-25 12:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))!false)/>
                    <#if !grayCityTeacher && !grayCityTeacher2 >
                        <li style="width: 120px;">
                            <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                            <div class="title">
                                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/reward/index.vpage" target="_blank"><span
                                        class="w-icon w-icon-10"></span><span class="w-icon-md">教学用品中心</span></a>
                            </div>
                        </li>
                    <#--<li>
                        <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                        <div class="title">
                            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/campaign/teacherlottery.vpage" target="_blank"><span class="w-icon w-icon-28"></span><span class="w-icon-md">大抽奖</span></a>
                        </div>
                    </li>-->
                    </#if>
                    <li>
                        <div class="title">
                        <#--<a href="/teacher/invite/activateteacher.vpage"><span class="w-icon w-icon-16"></span><span class="w-icon-md">有奖互助</span></a>-->
                            <a href="javascript:;" class="JS_prize_button"><span class="w-icon w-icon-16"></span><span
                                    class="w-icon-md">有奖互助</span></a>
                        </div>
                    </li>
                <#--fix #35543-->
                    <#if false && (currentTeacherDetail.subject != "CHINESE" )!false>
                        <li>
                            <div class="title">
                                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/ambassador/center.vpage"><span
                                        class="w-icon w-icon-14"></span><span class="w-icon-md">校园大使</span></a>
                            </div>
                        </li>
                    </#if>
                </ul>
            <#else>
                <#assign grayCityTeacher3 = ((currentTeacherWebGrayFunction.isAvailable("Reward", "CloseSchool") && (.now>="2019-03-25 12:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))!false)/>
                <#if (currentTeacherWebGrayFunction.isAvailableWithSchoolLevel("Reward", "Index") && !grayCityTeacher3)!false>
                    <ul class="w-fl-left">
                        <li>
                            <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                            <div class="title">
                                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/reward/index.vpage" target="_blank"><span
                                        class="w-icon w-icon-10"></span><span class="w-icon-md">教学用品中心</span></a>
                            </div>
                        </li>
                    </ul>
                </#if>
            </#if>
            <ul class="w-fl-right">
            <#--<li style="width: 121px;">
                <div class="title">
                    <a href="/teacher/invite/index.vpage"><span class="w-icon w-icon-6"></span><span class="w-icon-md">邀请老师</span></a>
                </div>
            </li>-->
                <li>
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a> <span class="w-icon-2wicon"></span><span class="w-icon-md w-icon-thapp">老师APP</span></a>
                        <div class="link-wbox">
                            <div class="link-wtriangle"></div>
                            <div class="link-wbg">
                                <#if (currentTeacherDetail.isPrimarySchool())!false>
                                    <img src="<@app.link href="public/skin/teacherv3/images/publicbanner/2wteacher-v1.png"/>" width="100" height="100">
                                <#else>
                                    <img src="<@app.link href="public/skin/teacherv3/images/publicbanner/17juniorteacher-code.png"/>" width="100" height="100">
                                </#if>
                                <p>手机扫描二维码下载</p></div>
                        </div>
                    </div>
                </li>
                <li class="v-menu-hover v-menu-click <#if show == "message">current</#if>">
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="/teacher/center/index.vpage#/teacher/message/index.vpage"><span
                                class="w-icon w-icon-7"></span><span class="w-icon-md">消息</span></a>
                    </div>
                    <div class="info-bar"><span class="v-msg-count w-icon-arrow w-icon-redInfo"
                                                style="display: none;"></span></div>
                </li>
                <li class="v-menu-hover pull-down <#if show == "else">current</#if>"><!--active-->
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="javascript:void(0);"><span class="w-icon w-icon-8"></span><span
                                class="w-icon-md"><#if currentUser.profile.realname?has_content>${(currentUser.profile.realname)!?substring(0, 1)}</#if>
                            老师</span><span class="w-icon-arrow"></span></a>
                    </div>
                    <div class="select">
                        <a href="/teacher/center/index.vpage">个人中心</a>
                        <#if (currentTeacherDetail.isPrimarySchool())!false>
                            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/index.vpage?menu=teacher"
                               target="_blank">帮助与支持</a>
                        <#else>
                            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/junior.vpage" target="_blank">帮助与支持</a>
                        </#if>
                    <#--<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" target="_blank">课题相关</a>-->
                    <#--<a href="http://keti.17zuoye.com/edusociety/index.php" target="_blank">教育学会课题</a>
                    <a href="http://keti.17zuoye.com" target="_blank">十三五课题</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/index.vpage" target="_blank">教育部课题</a>-->
                        <a href="javascript:void(0)" class="sign-out">退出</a>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</div>
<!--主体-->
    <#if showNav == "show">
    <div class="m-main">
        <div class="m-column">
            <!--个人信息-->
            <#include "../teacherv3/block/leftpersoninfo.ftl" />
        </div>
        <div class="m-container">
            <#nested>
        </div>
    </div>
    <#else>
    <div <#if show != "resource-reading">class="m-main"</#if>>
        <#nested>
    </div>
    </#if>
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
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <div class="w-fl-left">
                <#--<h3>关于</h3>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage" target="_blank">关于我们</a>
                <#--<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/uservoice.vpage" target="_blank">用户声音</a>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                    <a href='javascript:;' class="js-commentsButton">我要评论</a>
                    <a href="javascript:;" class="js-reportButton">我要举报</a>
                </div>
                <div class="w-fl-left">
                <#--<h3>联系</h3>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/news/index.vpage" target="_blank">新闻中心</a>
                <#--<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" target="_blank">课题相关</a>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/index.vpage?menu=teacher" target="_blank">帮助</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=0"
                       target="_blank">用户协议</a>
                </div>
            <#else>
                <div class="w-fl-left">
                <#--<h3>关于</h3>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage" target="_blank">关于我们</a>
                <#--<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/uservoice.vpage" target="_blank">用户声音</a>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/index.vpage?menu=teacher"
                       target="_blank">帮助中心</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                    <a href='javascript:;' class="js-commentsButton">我要评论</a>
                    <a href="javascript:;" class="js-reportButton">我要举报</a>
                </div>
                <div class="w-fl-left">
                <#--<h3>联系</h3>-->
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/news/index.vpage" target="_blank">新闻中心</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage"
                       target="_blank">课题相关</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=0"
                       target="_blank">用户协议</a>
                </div>
            </#if>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <div class="m-code">
                    <p class="c-image"></p>
                    <p class="c-title">关注我们</p>
                </div>
            </#if>
        </div>
    </div>
</div>
<#include "../common/to_comments_report.ftl" >
<style>
    .prize_pop {
        display: none;
        width: 100%;
        height: 100%;
        position: fixed;
        left: 0;
        top: 0;
        background: rgba(0, 0, 0, .5);
        z-index: 99;
    }

    .prize_box {
        width: 566px;
        height: 550px;
        position: fixed;
        left: 50%;
        top: 50%;
        margin: -283px 0 0 -275px;
    }

    .prize_main {
        position: relative;
    }

    .prize_close {
        position: absolute;
        right: 0;
        top: 0;
        cursor: pointer
    }

    .prize_banner {
        margin-top: 42px;
    }

    .prize_ok {
        display: block;
        width: 180px;
        margin: 20px auto;
        text-align: center;
        cursor: pointer
    }
</style>
<div class="prize_pop">
    <div class="prize_box">
        <div class="prize_main">
            <img class="prize_close JS_prize_close"
                 src="<@app.link href="public/skin/teacherv3/images/prize_close.png"/>" alt="">
            <img class="prize_banner" src="<@app.link href="public/skin/teacherv3/images/prize_banner.png"/>" alt="">
            <img class="prize_ok JS_prize_close" src="<@app.link href="public/skin/teacherv3/images/prize_ok.png"/>"
                 alt="">
        </div>
    </div>
</div>
<script>
    $(".JS_prize_button").on('click', function () {
        $(".prize_pop").show();
    });
    $(".JS_prize_close").on('click', function () {
        $(".prize_pop").hide();
    });

    //反馈建议
    $(document).on("click", ".message_right_sidebar", function () {
        window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage', 'feedbackwindow', 'height=500, width=700,top=200,left=450');
    });

    //反馈建议
    $(document).on("click", ".js-clickWarningCheating", function () {
        var $data = $(this).data();
        window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage?type=' + $data.type, 'feedbackwindow', 'height=500, width=700,top=200,left=450');
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
</#macro>