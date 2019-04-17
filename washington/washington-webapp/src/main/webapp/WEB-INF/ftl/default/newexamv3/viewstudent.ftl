
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
<html xmlns="http://www.w3.org/1999/html">
<head>
<#include "../nuwa/meta.ftl" />
    <title>${headTitle!'一起作业，一起作业网，一起作业学生'}</title>
<@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "jplayer", "jquery.flashswf","ko","plugin.newexamv3"]
                css=["plugin.alert", "new_teacher.base", "new_teacher.widget", "new_teacher.module", "newexamv3"] />
<@sugar.site_traffic_analyzer_begin />
</head>
<body >
<div class="mk-container" id="paperModule">
    <div class="mk-header">
        <div class="inner inner-2">
            <a href="/" class="logo"></a>
        </div>
    </div>
    <div class="link-page"></div>
    <#--【添加导航栏】-->
    <div class="rp-nav" data-bind="visible:$root.subject == 'MATH'">
        <div class="nav-item on" id="examDetailsNav"
             data-bind="click:$root.changeNav.bind($root,'examDetails'),css:{'on':$root.showExam() === 'examDetails'}">
            考试报告
        </div>
        <div class="nav-item" id="examReportNav"
             data-bind="click:$root.changeNav.bind($root,'examReport'),css:{'on':$root.showExam() === 'examReport'}">
            试卷详情
        </div>
    </div>
    <#--【试卷详情】-->
    <div  id="examDetailsAction" data-bind="visible:$root.showExam() === 'examDetails'">
        <#include "./template/detailsreport.ftl"/>
        <a class="gotop" href="javascript:void(0);"></a>
        <div id="jquery_jplayer_1" class="jp-jplayer"></div>
    </div>
    <!--主体-->
    <div  id="examReportAction" style="display: none" data-bind="if:$root.showExam() === 'examReport',visible:$root.showExam() === 'examReport'">
        <div class="mk-main">
            <div class="h-mk-answer">
                <div class="h-answerBox">
                    <div class="h-wrap">
                        <h2 class="h-title" data-bind="text:$root.userName() + '模考报告详情'"></h2>

                        <div class="h-content" id="J_paperContent" style="position:relative;">
                            <#--<div class="subject-type">
                                <h5 class="sub-title">模块名</h5>
                                <div>
                                    <div>题目加载中...</div>
                                    <div>题目加载中...</div>
                                </div>
                            </div>-->
                            <!--ko if:$root.themeForSubs().length > 0 -->
                            <div style="display: none;" data-bind="text:$root.loadQuestionContent()"></div>
                            <!--/ko-->
                        </div>
                    </div>
                </div>
                <div class="h-answerCard">
                    <div class="cardBox">
                        <div class="fraction"><i data-bind="text:$root.gradeType()==0?$root.score():$root.embedRank()"></i><!--ko if:$root.gradeType()==0 -->分<!--/ko--></div>
                        <div class="eachTag">
                            <span class="itm"><i class="sk-green"></i>正确</span>
                            <span class="itm" data-bind="if:$root.subject == 'ENGLISH',visible:$root.subject == 'ENGLISH'"><i class="sk-blue"></i>口语</span>
                            <span class="itm"><i class="sk-red"></i>错误</span>
                        </div>
                        <div class="cardInner">
                            <!--ko foreach:{data : themeForSubs,as:'theme'}-->
                            <div class="cardTitle" data-bind="text:($index()+1)+'.'+theme.desc"></div>
                            <div class="cardNum">
                                <!--ko foreach:{data : theme.subQuestions,as:'question'}-->
                                <a data-bind="text:question.index,css:$root.getQuestionNoClass(question.qid,question.subIndex),click:$root.questionNoClick.bind($root,question.qid,question.index)" >
                                </a>
                                <!--/ko-->
                            </div>
                            <!--/ko-->
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--底部两边山谷-->
        <div class="valleyIcon01"></div>
        <div class="valleyIcon02"></div>
        <a class="gotop" href="javascript:void(0);"></a>
        <div id="jquery_jplayer_1" class="jp-jplayer"></div>
    </div>
</div>
<#--口语题-->
<script id="tmpl_ORAL" type="text/html">
    <div class="answeBox">
        <p class="answe-itm">
            学生答案：
            <% if (!voiceUrlList || voiceUrlList.length == 0) { %>
            <i class="wrong">未答</i>
            <% }%>
        </p>
        <% if (voiceUrlList && voiceUrlList.length > 0) { %>
        <div class="stud-record">
            <div class="teach-record-box">
                <a  class="stud-audio-s1 btnPlay" href="javascript:void(0);" data-audio_src="<%=voiceUrlList.join('|')%>"></a>
            </div>
            <div class="stud-info">
                <p><%=userName%></p>
                <% if (!(gradeType == 1 && from == "student_history")) { %>
                <p class="frac"><%=userScore%>分</p>
                <% } %>
            </div>
        </div>
        <% }%>
        <% if (!(gradeType == 1 && from == "student_history")) { %>
        <p class="answe-itm ">本题分值：<%=standardScore%>分&nbsp;&nbsp;&nbsp;&nbsp;学生得分：<%=userScore%>分</p>
        <% } %>
        <% if (referenceAnswers && referenceAnswers.length>0) { %>
        <div class="answe-refer">
            <i>参考答案：</i>
            <% for (var i = 0; i < referenceAnswers.length; i ++) { %>
            <span style="float:left">（<%=i+1 %>）</span><p><%==referenceAnswers[i].answer%></p>
            <% } %>
        </div>
        <% } %>
    </div>
</script>
<script id="tmpl_OTHERS" type="text/html">
    <div class="answeBox">
        <#--<div style="line-height: 22px;" class="answe-itm">正确答案：<div class="correct"><%=standardAnswer %></div></p>-->
        <% if (!(gradeType == 1 && from == "student_history")) { %>
        <p class="answe-itm-2">本题分值：<%=standardScore %>分&nbsp;&nbsp;&nbsp;&nbsp;学生得分：<%=userScore %>分</p>
        <% } %>
        <#--<p class="answe-itm">解析：</p>
        <div><%==analysis.length==0?"无":analysis %></div>-->
    </div>
</script>

<script type="text/javascript">
    var constantObj = {
        subject          : "${subject!}",
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />
    };
    /*$(function(){
        //主菜单经过浮动条效果
        $(".v-menu-hover").hover(function(){ $(this).addClass("active"); }, function(){ $(this).removeClass("active"); });
    });*/
</script>
<@sugar.site_traffic_analyzer_end />
<@sugar.capsule js=["newexamv3.viewstudent"] />
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
</html> -->
