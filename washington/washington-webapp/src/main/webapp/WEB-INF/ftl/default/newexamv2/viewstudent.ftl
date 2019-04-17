
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
<@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "jplayer", "jquery.flashswf","ko","newexamV2"] css=["plugin.alert", "new_teacher.base", "new_teacher.widget", "new_teacher.module","newexamv2.questioncss", "newexamv2"] />
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
    <!--主体-->
    <div class="mk-main">
        <div class="h-mk-answer">
            <div class="h-answerBox">
                <div class="h-wrap">
                    <h2 class="h-title" data-bind="text:$root.userName() + '模考报告详情'"></h2>

                    <div class="h-content">
                        <!--ko foreach:{data:themeForSubs,as:'theme'}-->
                        <div class="subject-type">
                            <h5 class="sub-title" data-bind="text:$17.Arabia_To_SimplifiedChinese($index()+1)+'、'+theme.desc"></h5>
                            <div data-bind="foreach:{data:theme.questionList,as:'qid'}">
                                <div data-bind="attr:{id:'question_'+qid+$index()}">题目加载中...</div>
                                <div style="display: none;" data-bind="text:$root.loadQuestionContent(qid,$index())"></div>
                            </div>
                        </div>
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
    <a class="gotop" href="javascript:;"></a>
    <div id="jquery_jplayer_1" class="jp-jplayer"></div>
</div>
<#--口语题-->
<script id="tmpl1" type="text/html">
    <div class="answeBox">
        <p class="answe-itm">
            学生答案：
            <% if (!hasAnswer) { %>
            <i class="wrong">未答</i>
            <% }%>
        </p>
        <% if (hasAnswer) { %>
        <div class="stud-record">
            <div class="teach-record-box">
                <a  class="stud-audio-s1 btnPlay" href="javascript:void(0);" data-audio_src="<%=voiceUrlList.join('|')%>"></a>
            </div>
            <div class="stud-info">
                <p><%=userName%></p>
                <% if (!(gradeType == 1 && from == "student_history")) { %>
                <p class="frac"><%=personalScore%>分</p>
                <% } %>
            </div>
        </div>
        <% }%>
        <% if (!(gradeType == 1 && from == "student_history")) { %>
        <p class="answe-itm ">本题分值：<%=standardScore%>分&nbsp;&nbsp;&nbsp;&nbsp;学生得分：<%=personalScore%>分</p>
        <% } %>
        <% if (referenceAnswers&&referenceAnswers.length>0) { %>
        <div class="answe-refer">
            <i>参考答案：</i>
            <% for (var i = 0; i < referenceAnswers.length; i ++) { %>
            <span style="float:left">（<%=i+1 %>）</span><p><%==referenceAnswers[i].answer%></p>
            <% } %>
        </div>
        <% } %>
    </div>
</script>
<#--选择题-->
<script id="tmpl2" type="text/html">
    <div class="answeBox">
        <p class="answe-itm">正确答案：<i class="correct"><%=standardAnswer %></i></p>
        <p class="answe-itm">学生答案：<i class="<%=personalGrasp ? 'correct':'wrong' %>"><%=hasAnswer?personalAnswer:'未答' %></i></p>
        <% if (!(gradeType == 1 && from == "student_history")) { %>
        <p class="answe-itm-2">本题分值：<%=standardScore %>分&nbsp;&nbsp;&nbsp;&nbsp;学生得分：<%=personalScore %>分</p>
        <% } %>
        <p class="answe-itm">解析：<%==analysis.length==0?"无":analysis %></p>
    </div>
</script>
<#--填空题-->
<script id="tmpl3" type="text/html">
    <div class="answeBox">
        <p class="answe-itm">正确答案：<%=standardAnswer %></p>
        <p class="answe-itm">学生答案：
            <% if (personalAnswerDetail.length>0) { %>
            <% for (var i = 0; i < personalAnswerDetail.length; i ++) { %>
            <% if (personalAnswerDetail[i].grasp) { %>
            <i class="correct"><%=personalAnswerDetail[i].answer%>
                <% if(i < personalAnswerDetail.length-1){ %>
                ;
                <% } %>
            </i>
            <% } else {%>
            <i class="wrong"><%=personalAnswerDetail[i].answer%>
                <% if(i < personalAnswerDetail.length-1){ %>
                ;
                <% } %>
            </i>
            <% } %>


            <% } %>
            <% } else if(!hasAnswer) { %>
            <i class="wrong">未答</i>
            <% } %>
        </p>
        <% if (!(gradeType == 1 && from == "student_history")) { %>
        <p class="answe-itm-2">本题分值：<%=standardScore %>分&nbsp;&nbsp;&nbsp;&nbsp;学生得分：<%=personalScore %>分</p>
        <% } %>
        <p class="answe-itm">解析：<%==analysis.length==0?"无":analysis %></p>
    </div>
</script>
<!--底部-->
<#-- <#include "../../layout/project.footer.ftl"/>-->
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
<@sugar.capsule js=["newexamv2.viewstudent"] />
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
