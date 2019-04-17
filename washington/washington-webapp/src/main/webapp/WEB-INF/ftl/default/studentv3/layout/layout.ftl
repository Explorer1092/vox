<#macro page pageName='index' clazzName='m-body-back' >
<!doctype html>
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
    <#include "../../nuwa/meta.ftl" />
    <title>一起教育科技，让学习成为美好体验</title>
    <@sugar.check_the_resources />
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "DD_belatedPNG", "student"] css=["plugin.alert", "new_student.base", "new_student.module", "new_student.widget"] />
    <@sugar.site_traffic_analyzer_begin />
    <script type="text/javascript">
        var pf_white_screen_time_end = +new Date(); //白屏时间结束
    </script>
    <!--[if lte IE 8]>
    <script type="text/javascript">
        if (!$17.getCookieWithDefault("goToKillIe")) {
            window.location.href = "/project/ie/index.vpage";
        }
    </script>
    <![endif]-->
</head>
<body class="${clazzName!''}">
    <#if pageName == "studentHome">
    <#--first login guide-->
    <div id="stepNoviceContainer" class="tl-practice-box">
        <div class="w-opt-back"></div>
        <div style="width: 1000px; margin: 0 auto; position: relative; z-index: 2001;">
            <div class="w-opt-back-content stepNoviceTwo"></div>
        </div>
    </div>
    </#if>
${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'TopBulletin')}
    <@ftlmacro.oldIeInfoBox />
    <#include "../../block/detectzoom.ftl">

<!--头部-->

<div class="m-header">
    <div class="m-inner">
        <div class="logo"><a href="/index.vpage" data-op="logo" class="v-studentVoxLogRecord"></a></div>
        <div class="link">
            <ul class="w-fl-left">
                <#assign primaryStudent = (currentStudentDetail.isPrimaryStudent()) />
                <#assign hasClassLevel = (currentStudentDetail.clazz.classLevel)??/>
                <#--37个城市灰度的学生才能看到学习用品中心"-->
                <#assign grayCityStudent = ((currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu", true))!false)/>
                <#--由于政策原因，部分城市下线奖品中心入口-->
                <#assign grayCityStudent2 = ((currentStudentWebGrayFunction.isAvailable("Reward", "CloseSchool") && (.now>="2019-03-25 12:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))!false)/>
                <#assign headerTopMenu = [{
                "isShow" : true,
                "current" : "studentHome",
                "name" : "首页",
                "link" : "/index.vpage"
                },{
                "isShow" : primaryStudent,
                "current" : "learcingCenter",
                "name" : "学习中心",
                "link" : "/student/learning/history/list.vpage?subject=ENGLISH"
                },{
                "isShow" : hasClassLevel && primaryStudent,
                "current" : "clazzRoom",
                "name" : "班级空间",
                "link" : "/student/clazz/index.vpage"
                },
                <#--{
                    "isShow" : hasClassLevel && primaryStudent,
                    "current" : "parentReward",
                    "name" : "家长奖励",
                    "link" : "/student/parentreward/index.vpage"
                },-->
                {
                "isShow" : hasClassLevel && primaryStudent && !grayCityStudent && !grayCityStudent2,
                "current" : "rewardCenter",
                "name" : "学习用品中心",
                "link" : "/reward/index.vpage"
                }
                <#--,{
                    "isShow" : false,
                    "current" : "reportIndex",
                    "name" : "报告",
                    "link" : "/student/learning/report/index.vpage"
                }-->
                ]/>
                <#list headerTopMenu as menu>
                    <li <#if (pageName == menu.current)!false> class="current" </#if>>
                        <div class="title">
                            <#if (menu.isShow)!false>
                                <a href="${(menu.link)!'javascript:void(0);'}" data-op="${(menu.current)!}"
                                   class="v-studentVoxLogRecord">${(menu.name)!'--'}</a>
                            <#else>
                                <#if primaryStudent && !grayCityStudent && !grayCityStudent2>
                                    <a href="javascript:void(0);" data-op="${(menu.current)!}"
                                       class="v-joinClazzBtn-popup v-studentVoxLogRecord">${(menu.name)!'--'}</a>
                                </#if>
                            </#if>
                        </div>
                    </li>
                </#list>
            </ul>

            <ul id="pull_down_box" class="w-fl-right"><!--active-->
                <li class="pull-down">
                    <div class="title">
                        <div class="pd-info" id="popinfo" style="display: none;"><span
                                class="w-icon w-icon-point"></span></div>
                        <span class="avatar"><img src="<@app.avatar href='${currentUser.fetchImageUrl()!}'/>"></span>
                        <span class="w-icon-md name">${(currentUser.profile.realname)!''}</span>
                        <i class="w-icon w-icon-arrowSmall w-icon-arrowSmall-bot"></i>
                    </div>
                    <div class="select">
                        <#if (primaryStudent)>
                            <span class="wt-box"><a
                                    href="${(ProductConfig.getUcenterUrl())!''}/student/center/index.vpage"
                                    class="v-studentVoxLogRecord" data-op="clickCenter">个人中心</a></span>
                            <span class="wt-box" style="position: relative;">
                                    <a href="${(ProductConfig.getUcenterUrl())!''}/student/message/index.vpage"
                                       class="v-studentVoxLogRecord" data-op="clickMessageCenter">消息中心
                                        <span class="w-icon w-icon-point-count unreadSystemMessageCount"
                                              style="position: absolute; top:5px; right: 28px;"></span>
                                    </a>
                                </span>
                            <span class="wt-box"><a onclick="$17.atongji('首页-导航-用户名-我的礼物','/student/gift/index.vpage');"
                                                    href="javascript:void(0);">我的礼物</a></span>
                        </#if>
                        <span class="wt-box"><a href="/help/download-student-app.vpage?ref=downmenu" target="_blank">下载手机端</a></span>
                        <#--<span class="wt-box"><a href="javascript:;" class="JS-pcdownload">PC客户端</a></span>-->
                    <#--<span class="wt-box"><a href="javascript:void(0);" id="js_download">下载桌面版</a></span>-->
                    <#--<span class="wt-box"><a href="<@app.liebao_setup_url />">猎豹浏览器</a></span>-->
                    <#--<span class="wt-box"><a onclick="$17.tongji('首页-导航-用户名-帮助');" href="http://help.17zuoye.com" target="_blank">帮助</a></span>-->
                        <span class="wt-box sign-out"><a id="logout" href="javascript:void(0);">退出</a></span>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</div>
    <#nested />
<#--保护视力-->
<div id="studentTime" class="studentStep" style="display: none;">
    <div class="alpha_back"></div>
    <div class="alpha_content_layer">
        <div class="content timeback">
            <a id="studentTimeClose" href="javascript:void(0);" class="close" title="close"></a>
            <div class="info">
                <b>
                    <span class="text_blue">${(currentUser.profile.realname)!}</span>同学：
                    你今天上网的时间有点长了，眼睛可能会疲劳，休息一会儿再继续作业吧！
                </b>
            </div>
            <div class="time">
                <span>00</span>:<span
                    id="studentTimeM"><#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>01<#else>
                10</#if></span>:<span id="studentTimeS">00</span>
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
                <a href="/help/aboutus.vpage" target="_blank">关于我们</a>
            <#--<a href="/help/uservoice.vpage" target="_blank">用户声音</a>-->
                <a href="/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href="/help/privacyprotection.vpage" target="_blank">隐私保护</a>
            </div>
            <div class="w-fl-left">
                <a href="/help/parentsguidelines.vpage" target="_blank">家长须知</a>
                <a href="/help/childrenhealthonline.vpage" target="_blank">儿童健康上网</a>
                <a href="/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a>
            <#--<a href="http://help.17zuoye.com" target="_blank">帮助</a>-->
            </div>
            <div class="m-service">
            <#--<p class="c-title">
                咨询时间8:00-21:00
                <strong><@ftlmacro.hotline phoneType="student"/></strong>
            </p>-->
                <p class="c-btn" style="overflow: hidden">
                    <a  href='javascript:;' class="js-commentsButton">我要评论</a>
                    <a  href="javascript:;" class="on-service js-reportButton">我要举报</a>
                </p>
                <p class="c-btn" style="overflow: hidden">
                    <#--<a href="javascript:void(0);" id="message_right_sidebar">反馈建议</a>-->
                    <a style="margin-top: 10px;" href="/help/kf/index.vpage?menu=student" class="on-service" target="_blank">帮助中心</a>
                </p>
            </div>
        </div>
    </div>
</div>
    <#include "activex.ftl" >
    <#include "../../common/to_comments_report.ftl" >
<#--右下角弹窗模板-->
<script id="t:右下角新消息" type="text/html">
    <ul>
        <%for(var i = 0; i < msgList.length; i++){%>
        <li><%==msgList[i]%></li>
        <%}%>
    </ul>
</script>

<script type="text/html" id="t:bandingWeiXin">
    <div class='weiXinSideDetail' style='text-align: center'>
        <dl>
            <dt>
                <img src='<%=weiXinCode%>' width='200' height='200'/>
            </dt>
            <dd>
                微信扫一扫
            </dd>
            <%if(bandingType == 'pk'){%>
            <dd>绑定成功后，即可获赠<span style="color: red; font-weight: bold;">10</span>个PK活力值，绑定成功后刷新PK馆页面可见</dd>
            <%}else if(bandingType == 'travelAmerica'){%>
            <dd>绑定成功后，即可获走遍美国<span style="color: red; font-weight: bold;">100</span>钻石，绑定成功后刷新走遍美国页面可见</dd>
            <%}else if(bandingType == 'bable'){%>
            <dd>随时查看孩子学习通天塔的结果</dd>
            <%}else if(bandingType == 'xxt'){%>
            <dd>关注公告号：一起作业家长通，全免费接收老师通知、作业信息、做错考点和学习报告，同时也将为你提供丰富的免费学习资料。</dd>
            <%}%>
        </dl>
        <div style="clear:both;"></div>
    </div>
</script>
    <#if stuforbidden!false>
    <script type="text/javascript">
        $.prompt("<div style='text-align: center; padding: 30px 0;'>账号异常，暂时无法使用</div>", {
            title: "账号异常",
            buttons: {'退出登录': true},
            classes: {
                close: 'w-hide'
            },
            submit: function () {
                $17.voxLog({
                    module: "studentForbidden",
                    op: "popup-logout"
                }, 'student');
                location.href = "/ucenter/logout.vpage";
            },
            loaded: function () {
                $17.voxLog({
                    module: "studentForbidden",
                    op: "popup-load"
                }, 'student');
            }
        });
    </script>
    </#if>
    <#if pageName == "studentHome" || !(currentStudentDetail.clazz.classLevel)??>
        <#include '../joinclazz.ftl' />
    </#if>

<#--<#assign chatIframeRootRegionCode = [110000]/>
<#assign chatIframeCityCode = [370100,430100]/>
<#--班级群聊-开发环境和Staging不开放，只开放Test环境和线上灰度地区-->
<#--<#if ( !ftlmacro.devTestStagingSwitch && (chatIframeRootRegionCode?seq_contains(currentStudentDetail.rootRegionCode) || chatIframeCityCode?seq_contains(currentStudentDetail.cityCode)) ) || ProductDevelopment.isTestEnv()>
    <div class="m-chatAbs_box" id="chatIframeAbsoluteBoxPopup" style="display: none;">
        <div class="ct-inner">
            <iframe id="chatIframeAbsoluteBox" allowtransparency="true" style="background-color=transparent" frameborder="0" scrolling="no" width="131" height="35"></iframe>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            var chatFrameLoader = function(){
                $("#chatIframeAbsoluteBox").attr("src", "/student/chat/index.vpage");
                $("#chatIframeAbsoluteBoxPopup").show();
            };

            if (window.attachEvent){
                window.attachEvent('onload', chatFrameLoader);
            }else if(window.addEventListener){
                window.addEventListener('load', chatFrameLoader, false);
            }
        });
    </script>
</#if>-->
    <@sugar.site_traffic_analyzer_end />
</body>
<script>
    $(".JS-pcdownload").on("click", function () {
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
</script>
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
