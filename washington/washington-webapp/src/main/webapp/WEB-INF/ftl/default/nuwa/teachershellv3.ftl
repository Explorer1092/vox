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
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "base", "DD_belatedPNG_class"] css=["plugin.alert", "new_teacher.basev1", "new_teacher.widget", "new_teacher.module"] />

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
            cityCode    : "${(currentTeacherDetail.cityCode)!0}",
            env         : <@ftlmacro.getCurrentProductDevelopment />
        };
    </script>
    <script type="text/javascript">
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
</head>
<body id="ng-app" ng-app="Soul" class="ng-app:Soul">
${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'LayoutBanner')}
<#--showTip - popup //FIXME 瞎弄：蒙板又不是天天用的，竟然加载到全局shell里。。。-->
<div class="w-opt-back" style="z-index: 150; display: <#if first!false>block<#else>none</#if>;" id="showTipOptBack"></div>
    <@ftlmacro.oldIeInfoBox />
    <#include "../teacherv3/block/voiceplugin.ftl">
    <#include "../teacherv3/block/loading.ftl" />
<!--头部-->
<#if !((currentTeacherDetail.is17XueTeacher())!false)>
<div class="m-header">
    <div class="m-inner">
        <div class="logo <#if title != "首页">logo-back</#if>"><a href="/" title="返回首页"></a></div>
        <div class="link ">
            <ul class="w-fl-left">
                <#--<li class="v-menu-hover v-menu-click <#if show == "main">current</#if>">
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a class="v-menu-hover v-menu-click" href="/teacher/index.vpage"><spa   n class="w-icon w-icon-9"></span><span class="w-icon-md">教师空间</span></a>
                    </div>
                </li>-->
                <#assign grayCityTeacher = (currentTeacherWebGrayFunction.isAvailable("Reward", "Close"))!false />
                <#assign grayCityTeacher2 = ((currentTeacherWebGrayFunction.isAvailable("Reward", "CloseSchool") && (.now>="2019-03-25 12:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))!false)/>
                <#if !grayCityTeacher && !grayCityTeacher2>
                <li style="width: 120px;">
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <#--<#if userType == 'STUDENT'>-->
                            <#--<a href="/campaign/studentlottery.vpage" target="_blank"><span class="w-icon w-icon-10"></span><span class="w-icon-md">教学用品中心</span></a>-->
                        <#--</#if>-->
                        <#--<#if userType == 'TEACHER'>-->
                            <a href="/reward/index.vpage" target="_blank"><span class="w-icon w-icon-10"></span><span class="w-icon-md">教学用品中心</span></a>
                        <#--</#if>-->
                    </div>
                </li>
                <#--<li>
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="/campaign/teacherlottery.vpage" target="_blank"><span class="w-icon w-icon-28"></span><span class="w-icon-md">大抽奖</span></a>
                    </div>
                </li>-->
                </#if>
                <li>
                    <div class="title">
                        <#--<a href="/teacher/invite/activateteacher.vpage"><span class="w-icon w-icon-16"></span><span class="w-icon-md">有奖互助</span></a>-->
                            <#--<#if userType == 'STUDENT'>-->
                                <#--<a href="javascript:;" class="JS_prize_button"><span class="w-icon w-icon-16"></span><span class="w-icon-md">有奖互助</span></a>-->
                            <#--</#if>-->

                    </div>
                </li>
                <li class="courseware-activity">
                    <div class="title">
                        <a href="/courseware/contest/index.vpage?referrer=teahcer_index_page" target="_blank"><span class="w-icon w-icon-41"></span><span class="w-icon-md">教学设计展示活动</span></a>
                    </div>
                </li>

                <#--暂时关闭 “校园大使” 入口 11-16-->
                <#if false && (((currentTeacherDetail.subject == "CHINESE" && currentTeacherDetail.schoolAmbassador) || currentTeacherDetail.subject != "CHINESE")!false)>
                    <li>
                        <div class="title">
                            <a href="/ambassador/center.vpage"><span class="w-icon w-icon-14"></span><span class="w-icon-md">校园大使</span></a>
                        </div>
                    </li>
                </#if>

                <#if false && (currentTeacherDetail.subject != "CHINESE" && currentUser.fetchCertificationState() == "SUCCESS" )!false> //下线
                <li style="width: 104px;">
                    <div class="title">
                        <a href="/teacher/activity/strengthteacher.vpage" target="_blank"><span class="w-icon w-icon-37"></span><span class="w-icon-md">教学实力派</span></a>
                    </div>
                </li>
                </#if>
                <#--<li>
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}/open.php?mod=register&teacherType=2" target="_blank"><span class="w-icon w-icon-16"></span><span class="w-icon-md">教师论坛</span></a>
                    </div>
                </li>-->
                <#--<#if (currentTeacherDetail.activityFlag)?? && currentTeacherDetail.activityFlag>
                    <li class="v-menu-hover v-menu-click <#if show == "thanksgiving">current</#if>">
                        <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                        <div class="title">
                            <a class="v-menu-hover v-menu-click" href="/teacher/reward/thanksgiving.vpage"><span class="w-icon" style="background: url(<@app.link href="public/skin/project/halloween/images/goldcoin.gif"/>) no-repeat center center"></span><span class="w-icon-md">唤醒学生</span></a>
                        </div>
                    </li>
                </#if>-->
            <#--<li class="v-menu-hover v-menu-click <#if show == "experience">current</#if>">
                <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                <div class="title">
                    <a class="v-menu-hover v-menu-click" href="javascript:void(0);"><span class="w-icon w-icon-11"></span><span class="w-icon-md">体验中心</span></a>
                </div>
            </li>-->
            </ul>
            <ul class="w-fl-right">
                <#--<#if (data.tcrshow == "SHOW_OLD")!false>
                    <li>
                        <div class="title">
                            <a href="/teacher/invite/index.vpage" style="float: right;"><span class="w-icon w-icon-6"></span><span class="w-icon-md">邀请老师</span></a>
                        </div>
                    </li>
                </#if>-->
                <#--<li style="width:180px;margin-top:9px;">
                    <a target="_blank" href="/project/fallactivities/teacherleague.vpage"><img src="<@app.link href="public/skin/teacherv3/images/index_activity.jpg"/>"> </a>
                </li>-->
                <li>
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a> <span class="w-icon-2wicon"></span><span class="w-icon-md w-icon-thapp">老师APP</span></a>
                        <div class="link-wbox"><div class="link-wtriangle"></div><div class="link-wbg"><img src="<@app.link href="public/skin/teacherv3/images/publicbanner/2wteacher-v1.png"/>" width="100" height="100"><p>手机扫描二维码下载</p></div></div>
                    </div>
                </li>

                <li class="v-menu-hover v-menu-click <#if show == "message">current</#if>">
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/message/index.vpage"><span class="w-icon w-icon-7"></span><span class="w-icon-md">消息</span></a>
                    </div>
                    <div class="info-bar"><span class="v-msg-count w-icon-arrow w-icon-redInfo" style="display: none;"></span></div>
                </li>
                <li class="v-menu-hover pull-down <#if show == "else">current</#if>"><!--active-->
                    <div class="h-arrow"><span class="w-icon w-icon-arrow w-icon-arrow-blue"></span></div>
                    <div class="title">
                        <a href="javascript:void(0);"><span class="w-icon w-icon-8"></span><span class="w-icon-md"><#if currentUser.profile.realname?has_content>${(currentUser.profile.realname)!?substring(0, 1)}</#if>老师</span><span class="w-icon-arrow"></span></a>
                    </div>
                    <div class="select">
                        <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage" target="_blank">个人中心</a>
                        <a href="/help/kf/index.vpage?menu=teacher" target="_blank">帮助与支持</a>
                        <#--<a href="/project/educationsubject/list.vpage" target="_blank">课题相关</a>-->
                        <#--<a href="javascript:;" class="JS-pcdownload">PC客户端</a>-->
                    <#--<a href="http://keti.17zuoye.com/edusociety/index.php" target="_blank">教育学会课题</a>
                    <a href="http://keti.17zuoye.com" target="_blank">十三五课题</a>
                    <a href="/project/educationsubject/index.vpage" target="_blank">教育部课题</a>-->
                        <#--<a href="javascript:void(0)" id="installActiveX">下载桌面版</a>-->
                        <#--<a href="<@app.liebao_setup_url />" onclick="$17.tongji('download-liebao-teacher')">猎豹浏览器</a>-->
                        <a href="javascript:void(0)" class="sign-out">退出</a>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</div>
</#if>
<!--主体-->
    <#if showNav == "show" && (!((currentTeacherDetail.is17XueTeacher())!false))>
    <div class="m-main">
        <div class="m-column">
            <!--个人信息-->
            <#include "../teacherv3/block/leftpersoninfo.ftl" />
        </div>
        <div class="m-container">
            <#nested>
        </div>
        <#--版权风险提示-->
        <div id="copyrightNotice201509241455" style="display:none;font-size: 12px; color: #b9b9b9; padding: 10px 0 15px 220px;clear: both;">
            版权声明：本版块部分教辅材料取自互联网或其他途径，用户只能用于学习用途；如有任何版权问题，请及时与我们联系。
            电话：<span style="color: #b9b9b9"><@ftlmacro.hotline/></span>，邮箱：<span style="color: #b9b9b9">jiaofu.test@17zuoye.com</span>
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
            <div class="w-fl-left">
            <#--<h3>关于</h3>-->
                <a href="/help/aboutus.vpage" target="_blank">关于我们</a>
                <#--<a href="/help/uservoice.vpage" target="_blank">用户声音</a>-->
                <a href="/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href="/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                <a href="javascript:;" class="js-commentsButton">我要评论</a>
                <a href="javascript:;" class="js-reportButton">我要举报</a>
            </div>
            <div class="w-fl-left">
            <#--<h3>联系</h3>-->
                <a href="/help/news/index.vpage" target="_blank">新闻中心</a>
                <a href="/project/educationsubject/list.vpage" target="_blank">课题相关</a>
                <a href="/help/kf/index.vpage?menu=teacher" target="_blank">帮助中心</a>
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
<style>
    .prize_pop{display:none;width: 100%;height:100%;position: fixed;left:0;top:0;background: rgba(0,0,0,.5); z-index: 99;}
    .prize_box{width: 566px;height: 550px;position: fixed;left: 50%; top: 50%; margin: -283px 0 0 -275px; }
    .prize_main{position: relative;}
    .prize_close{position:absolute;right: 0;top:0;cursor: pointer}
    .prize_banner{margin-top:42px;}
    .prize_ok{display: block;width: 180px;margin:20px auto; text-align: center; cursor: pointer}
</style>
<div class="prize_pop">
    <div class="prize_box">
        <div class="prize_main">
            <img class="prize_close JS_prize_close" src="<@app.link href="public/skin/teacherv3/images/prize_close.png"/>" alt="">
            <img class="prize_banner" src="<@app.link href="public/skin/teacherv3/images/prize_banner.png"/>" alt="">
            <img class="prize_ok JS_prize_close" src="<@app.link href="public/skin/teacherv3/images/prize_ok.png"/>" alt="">
        </div>
    </div>
</div>
<#--右下角弹窗模板-->
<script id="t:右下角新消息" type="text/html">
    <ul>
        <%for(var i = 0; i < msgList.length; i++){%>
        <li><%==msgList[i]%></li>
        <%}%>
    </ul>
</script>
<script type="text/javascript">
    $(function() {
        $(".JS_prize_button").on('click',function () {
            $(".prize_pop").show();
        });
        $(".JS_prize_close").on('click',function () {
            $(".prize_pop").hide();
        });

        $(".JS-pcdownload").on("click",function () {
            var ua = navigator.userAgent;
            if (ua.indexOf("Windows NT 6.0") > -1) {
                $17.alert("暂不支持您使用的操作系统");
            } else if (ua.indexOf("Windows NT 5") > -1) {
                window.location.href = "https://cdn-cnc.17zuoye.cn/resources/mobile/student/download/17browser_setup_1.2.1_kcp_off_xpsp3.exe";
            } else if(ua.indexOf("Windows NT 6") > -1 || ua.indexOf("Windows NT 10") > -1) {
                window.location.href = "https://cdn-cnc.17zuoye.cn/resources/mobile/student/download/17client_setup_1.2.1_kcp_off_gte_win7_sp1.exe";
            }else{
                $17.alert("暂不支持您使用的操作系统");
            }
        });
        $("#installActiveX").on("click", function(){
            AC_InstallActiveX();
        });

        $(document).on("click", ".message_right_sidebar", function(){
            var url = '${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage',type = $(this).attr("data-type");
            window.open (type ? (url + "?" + $.param({feedbackType : type})) : url, 'feedbackwindow', 'height=500, width=700,top=200,left=450');
        });
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