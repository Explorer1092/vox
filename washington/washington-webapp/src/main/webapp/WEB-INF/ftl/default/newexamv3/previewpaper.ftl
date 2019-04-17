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
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "jplayer", "jquery.flashswf"] css=["plugin.alert", "new_teacher.base", "new_teacher.widget", "new_teacher.module", "newexamv3"] />
    <@sugar.capsule js=["echarts-3.4.0","ko","jplayer","plugin.newexamv3"]/>
    <@sugar.site_traffic_analyzer_begin />
</head>
<body >
    <#include "../block/loading.ftl" />
<div class="mk-container">
    <!--头部-->
    <div class="mk-header">
        <div class="inner inner-2">
            <a href="/" class="logo"></a>
        </div>
    </div>
    <!--主体-->
    <!--//start-->
    <div class="mk-main" id="newexamv2Root">
        <div class="h-mk-answer" style="display: none;" data-bind="if:!$root.hsLoading(),visible:!$root.hsLoading()">
            <div class="info-test" style="display: none;">
                <p class="info-clazz"><i></i></p>
                <p class="info-tips" style="display: none;">(可修改口语题成绩)</p>
            </div>
            <!-- 主体内容 -->
            <div id="tabContent">
                <div class="content-main">
                    <div class="h-answerBox">
                        <div class="h-wrap">
                            <!--ko if:$root.newExamPaperInfos().length > 1 -->
                            <div class="test-paper">
                                <span style="vertical-align: top;">试卷：</span>
                                <!--ko foreach:{data:$root.newExamPaperInfos,as:'paper'}-->
                                <a class="active" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;width: 110px;" href="javascript:void (0);" data-bind="attr:{title:paper.paperName},text:paper.paperName,css:{'active':$index()==$root.currentPaperIndex()},click:$root.changePaper.bind($root,$index())"></a>
                                <!--/ko-->
                            </div>
                            <!--/ko-->
                            <div class="content-title"></div>
                            <div class="h-content" id="J_paperContent" style="position:relative;">
                                <#--<div class="subject-type">
                                    <h5 class="sub-title">模块名称</h5>
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
                            <div class="cardInner">
                                <!--ko foreach:{data : themeForSubs,as:'theme'}-->
                                <div class="cardTitle" data-bind="text:($index()+1)+'.'+theme.desc"></div>
                                <div class="cardNum">
                                    <!--ko foreach:{data : theme.subQuestions,as:'question'}-->
                                    <a data-bind="text:question.index,click:$root.questionNoClick.bind($root,question.qid,question.index,question.subIndex)" >
                                    </a>
                                    <!--/ko-->
                                </div>
                                <!--/ko-->
                            </div>
                        </div>
                    </div>
                </div>
                <a class="teach-gotop" href="javascript:void(0);"></a>
            </div>
        </div>
    </div>
    <div id="jquery_jplayer_1" class="jp-jplayer"></div>
    <!--end//-->
</div>
<!--底部-->

<script id="TPL_DESCRIPTION" type="text/html">
    <div class="answeBox">
        <p class="answe-itm-2"><span>本题分值：<%=standardScore%>分</span></p>
    </div>
</script>

<script type="text/javascript">
    var constantObj = {
        imgDomain   : "${imgDomain!}",
        env : <@ftlmacro.getCurrentProductDevelopment />
    };
</script>
<#include "../teacherv3/newexamv3/template/viewpaper.ftl">

 <@sugar.capsule js=["newexamv3.previewpaper"]/>
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