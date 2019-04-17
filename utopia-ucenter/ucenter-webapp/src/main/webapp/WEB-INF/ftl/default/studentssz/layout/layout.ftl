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
        <title>一起作业，一起作业网，一起作业学生</title>
        <@sugar.check_the_resources />
        <@sugar.capsule js=["jquery", "core", "alert", "template", "DD_belatedPNG", "ssz.student"] css=["plugin.alert", "ssz_student.base", "ssz_student.module", "ssz_student.widget"] />
        <@sugar.site_traffic_analyzer_begin />
        <script type="text/javascript">
            var pf_white_screen_time_end = +new Date(); //白屏时间结束
        </script>
    </head>
    <body class="${clazzName!''}">
        <!--头部-->
        <div class="m-header">
            <div class="m-inner">
                <a onclick="$17.atongji('首页-导航-logo','/');" href="javascript:void(0);" class="logo-link">
                    <div class="logo"></div>
                    <i class="junior-tip"></i>
                </a>
                <div class="link">
                    <ul class="w-fl-left">
                        <#assign juniorStudent = (currentStudentDetail.isJuniorStudent()) />
                        <#assign hasClassLevel = (currentStudentDetail.clazz.classLevel)??/>
                        <#assign hasPK = ((currentUser.createTime lt "2016/12/05 00:00:00"?datetime('yyyy/MM/dd HH:mm:ss'))!false)/>
                        <#assign headerTopMenu = [
                        <#--{
                        "isShow" : true,
                        "current" : "studentHome",
                        "name" : "首页",
                        "link" : "/index.vpage"
                        },{
                        "isShow" : juniorStudent,
                        "current" : "learcingCenter",
                        "name" : "学习中心",
                        "link" : "/student/learning/index.vpage"
                        },{
                        "isShow" : hasClassLevel && juniorStudent,
                        "current" : "fairyland",
                        "name" : "课外乐园",
                        "link" : "/student/fairyland/index.vpage"
                        },{
                        "isShow" : hasClassLevel && juniorStudent,
                        "current" : "clazzRoom",
                        "name" : "班级空间",
                        "link" : "/student/clazz/index.vpage"
                        },
                        {
                        "isShow" : hasClassLevel && juniorStudent,
                        "current" : "parentReward",
                        "name" : "家长奖励",
                        "link" : "/student/parentreward/index.vpage"
                        },
                        {
                        "isShow" : hasClassLevel && juniorStudent,
                        "current" : "rewardCenter",
                        "name" : "奖品中心",
                        "link" : "/reward/product/exclusive/index.vpage"
                        }-->
                        ]
                        />
                        <#list headerTopMenu as menu>
                            <#if (menu.isShow)!false>
                            <li <#if (pageName == menu.current)!false> class="current" </#if>>
                                <div class="title">
                                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}${(menu.link)!'javascript:void(0);'}" data-op="${(menu.current)!}" class="v-studentVoxLogRecord">${(menu.name)!'--'}</a>
                                </div>
                            </li>
                            </#if>
                        </#list>
                    </ul>

                    <ul id="pull_down_box" class="w-fl-right"><!--active-->
                        <li class="pull-down">
                            <div class="title">
                                <div class="pd-info" id="popinfo" style="display: none;"><span class="w-icon w-icon-point"></span></div>
                                <span class="avatar"><img src="<@app.avatar href='${currentUser.fetchImageUrl()!}'/>"></span>
                                <span class="w-icon-md name">${(currentUser.profile.realname)!''}</span>
                                <i class="w-icon-header-arrow"></i>
                            </div>
                            <div class="select" id="rightDropDown">
                                <span class="wt-box"><a onclick="$17.atongji('首页-导航-用户名-班级管理', '${(ProductConfig.getMainSiteBaseUrl())!''}' + '/redirector/apps/go.vpage?app_key=Shensz&return_url=/pc#!/my-class')" href="javascript:void(0);" class="v-studentVoxLogRecord" data-op="clickCenter">我的班级</a></span>
                                <span class="wt-box"><a onclick="$17.atongji('首页-导航-用户名-个人中心','${(ProductConfig.getUcenterUrl())!''}/student/center/index.vpage')" href="javascript:void(0);" class="v-studentVoxLogRecord" data-op="clickCenter">个人中心</a></span>
                                <#--<span class="wt-box" style="position: relative;">
                                   <a onclick="$17.atongji('首页-导航-用户名-消息中心','/student/message/index.vpage');" href="javascript:void(0);" class="v-studentVoxLogRecord" data-op="clickMessageCenter">消息中心
                                       <span class="w-icon w-icon-point-count unreadSystemMessageCount" style="position: absolute; top:5px; right: 28px;"></span>
                                   </a>
                                </span>-->
                                <#--<span class="wt-box"><a onclick="$17.atongji('首页-导航-用户名-我的礼物','${(ProductConfig.getMainSiteBaseUrl())!''}/student/gift/index.vpage');" href="javascript:void(0);">我的礼物</a></span>-->
                                <#--<span class="wt-box"><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/download-student-app.vpage?ref=downmenu" target="_blank">下载手机端</a></span>-->
                                <#--<span class="wt-box"><a href="JavaScript:void(0);" id="js_download">下载桌面版</a></span>-->
                                <#--<span class="wt-box"><a href="<@app.liebao_setup_url />">猎豹浏览器</a></span>-->
                                <#--<span class="wt-box"><a onclick="$17.tongji('首页-导航-用户名-帮助');" href="http://help.17zuoye.com" target="_blank">帮助</a></span>-->
                                <span class="wt-box"><a id="logout" href="javascript:void(0);">退出登录</a></span>
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
                        <span>00</span>:<span id="studentTimeM"><#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>01<#else>10</#if></span>:<span id="studentTimeS">00</span>
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
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage" target="_blank">关于我们</a>
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/uservoice.vpage" target="_blank">用户声音</a>
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a>
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/privacyprotection.vpage" target="_blank">隐私保护</a>
                    </div>
                    <div class="w-fl-left">
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/parentsguidelines.vpage" target="_blank">家长须知</a>
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/childrenhealthonline.vpage" target="_blank">儿童健康上网</a>
                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a>
                        <#--<a href="http://help.17zuoye.com" target="_blank">帮助</a>-->
                    </div>
                    <div class="m-service">
                        <#--<p class="c-title">
                            咨询时间8:00-21:00
                            <strong><@ftlmacro.hotline phoneType="student"/></strong>
                        </p>-->
                        <p class="c-btn">
                            <a href="javascript:void(0);" id="message_right_sidebar">反馈建议</a>
                        </p>
                        <p class="c-btn">
                            <a style="margin-top: 10px;" href='javascript:;' class="on-service js-commentsButton">我要评论</a>
                            <a style="margin-top: 10px;" href="javascript:;" class="js-reportButton">我要举报</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
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

        <script type="application/javascript">
            // 当前在个人中心时，给右上角下拉个人中心激活
            if (location.href.indexOf('/student/center/index.vpage') > -1) {
                $('#rightDropDown .wt-box').eq(1).addClass('active');
            }
            //反馈建议
            $("#message_right_sidebar").on("click", function(){
                if(feedBackInner.practiceName == "exam" || feedBackInner.practiceName == "数学应试练习"){
                    feedBackInner.extStr2 = studentHomeworkExam.getQuestionId().join();
                }

                var url = '${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/feedback.vpage?' + $.param(feedBackInner);

                window.open(url,
                        'feedbackwindow',
                        'height=500,width=600,top=300,left=500,toolbar=no,menubar=no,scrollbars=no, resizable=no,location=no, status=no');

                $17.tongji('网址底栏-反馈建议');
                return false;
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
