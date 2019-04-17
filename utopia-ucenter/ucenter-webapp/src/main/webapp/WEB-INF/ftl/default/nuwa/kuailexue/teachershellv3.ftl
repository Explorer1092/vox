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
    <#include "../meta.ftl" />
    <@sugar.check_the_resources />
    <@sugar.capsule js=["jquery", "core", "alert", "template", "teacher", "DD_belatedPNG_class"] css=["plugin.alert", "new_teacher.basev1", "new_teacher.widget", "new_teacher.kuailexue"] />

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
    <div class="w-opt-back" style="z-index: 150; display: <#if first!false>block<#else>none</#if>;" id="showTipOptBack"></div>
    <!--头部-->

    <div id="page_hd">
        <div class="page_hd">
            <h1 class="logo">
                <!--未登录用户可跳转至17作业首页-->
                <a href="${ProductConfig.getKuailexueUrl()!}" title="一起教育科技" style="float:left;">
                    <img src="<@app.link href="public/skin/teacherv3/images/logo-new.png"/>">
                </a>
                <#--<#if ((currentUser.webSource!'') == "happy_study")>
                    <a href="/" title="快乐学" style="float:left;margin-left:0;">
                        <img src="<@app.link href="public/skin/teacherv3/images/logo_17_home_klx.png"/>">
                    </a>
                </#if>-->
            </h1>

            <ul class="user_nv" >
            <#if currentUser.id?has_content>
                <#if (currentTeacherDetail.isOldJuniorTeacher())!false >
                    <li class="user_in"><a href="${(ProductConfig.getJuniorSchoolUrl())!}/teacher/index"><i class="in_icon"></i>旧版网站入口</a></li>
                </#if>
                <li class="user_message">
                    <a href="/teacher/center/index.vpage#/teacher/message/index.vpage"><i class="message_icon"></i>消息
                        <span class="v-msg-count message_count" style="display: none"></span>
                    </a>
                </li>
                <li class="has_sub v-menu-hover">
                    <span class="menu_click">${(currentUser.profile.realname)!''} <i class="arrow"></i></span>
                    <ul class="sub_nav select" style="left: inherit; right: 0px; width: 83px;">
                        <li><a href="/teacher/center/index.vpage">个人中心</a></li>
                        <li><a href="${(ProductConfig.getKuailexueUrl())!''}/math/profile">教材版本</a></li>
                        <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" target="_blank">课题相关</a></li>
                        <li><a href="javascript:;"  class="sign-out">退出</a></li>
                    </ul>
                </li>
                <#else>
                <li>
                    <a href="" class="user_nv_reglogin">注册</a>
                </li>
                <li class="dotted"></li>
                <li>
                    <a href="" class="user_nv_reglogin">登录</a>
                </li>
                </#if>
            </ul>
        </div>
    </div>
    <!--主体-->
    <#if showNav == "show">
    <div class="class-section">
        <#include "leftpersoninfo.ftl" />
        <#nested>
    </div>
    </#if>
    <@sugar.site_traffic_analyzer_end />

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
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                    <a href="javascript:;" class="js-commentsButton">我要评论</a>
                    <a href="javascript:;" class="js-reportButton">我要举报</a>
                </div>
                <div class="m-left w-fl-left">
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/news/index.vpage" target="_blank">新闻中心</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/junior.vpage" target="_blank">帮助中心</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" target="_blank">课题相关</a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a>
                    <!--                        <a href="http://help.17zuoye.com" target="_blank">帮助</a>-->
                </div>
                <!--                    <div class="m-code">-->
                <!--                        <p class="c-image"></p>-->
                <!--                        <p class="c-title">关注我们</p>-->
                <!--                    </div>-->
            </div>
        </div>
    </div>
    <#include "../../common/to_comments_report.ftl" >
    <script type="text/javascript">
        <#--此处为header点击姓名下拉框，原本为css控制的鼠标滑过-->
        $(document).on("click",function () {
            $(".menu_click").removeClass("active");
            $(".sub_nav").hide();
        }).on("click",".menu_click",function (event) {
            event.stopPropagation();
            if ($(this).hasClass("active")){
                $(this).removeClass("active").siblings(".sub_nav").hide();
            }else{
                $(this).addClass("active").siblings(".sub_nav").show();
            }
        });
    </script>
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