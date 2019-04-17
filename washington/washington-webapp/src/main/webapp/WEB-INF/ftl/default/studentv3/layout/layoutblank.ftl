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
        <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "DD_belatedPNG", "student"] css=["plugin.alert", "new_student.base", "new_student.module", "new_student.widget"] />
        <@sugar.site_traffic_analyzer_begin />
        <script type="text/javascript">
            var pf_white_screen_time_end = +new Date(); //白屏时间结束
        </script>
        <!--[if lte IE 8]>
        <script type="text/javascript">
            if(!$17.getCookieWithDefault("goToKillIe")){
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
        <#if (pageName != "newexamv2" && pageName != "newexamv3")>
        <div class="m-header">
            <div class="m-inner">
                <div class="logo"><a href="javascript:void (0);" style="cursor: default;" data-op="logo" class="v-studentVoxLogRecord"></a></div>
            </div>
        </div>
        </#if>

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
        <#if pageName != "newexamv2" && pageName != "newexamv3_paperlist">
            <div class="m-footer">
                <div class="m-inner">
                    <div style="text-align: center;">
                        <div class="copyright">
                        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
                        </div>
                    </div>
                </div>
            </div>
        </#if>
        <#include "activex.ftl" >
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
                title : "账号异常",
                buttons : {'退出登录': true},
                classes : {
                    close: 'w-hide'
                },
                submit: function(){
                    $17.voxLog({
                        module : "studentForbidden",
                        op : "popup-logout"
                    }, 'student');
                    location.href = "/ucenter/logout.vpage";
                },
                loaded : function(){
                    $17.voxLog({
                        module : "studentForbidden",
                        op : "popup-load"
                    }, 'student');
                }
            });
        </script>
        </#if>
        <#if pageName == "studentHome" || !(currentStudentDetail.clazz.classLevel)??>
            <#include '../joinclazz.ftl' />
        </#if>
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
