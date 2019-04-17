<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
    <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre","homeworkv3.homework","homeworkv5.clazzwordteachmoduledetail"]/>

<div class="h-homeworkCorrect">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a href="/teacher/new/homework/report/list.vpage">检查作业</a>&gt;<a href="/teacher/new/homework/report/detail.vpage?homeworkId=${homeworkId}">作业报告</a>&gt;<span>作业详情</span>
    </h4>
    <div class="w-base" style="border-top: 0;">
        <div class="J_mainContentHolder hc-main" id="tabContentHolder">
            <!--ko template:{name:$root.displayModel.bind($root),if:$root.moduleDetail && $root.moduleDetail(),data:$root.moduleDetail}--><!--/ko-->
        </div>
    </div>
</div>


<script type="text/html" id="EXAM_DETAIL_LIST">
    <div class="w-base-title" style="background-color: #e1f0fc;"><h3>字词训练</h3></div>
    <!--ko if:$data.questions && $data.questions.length > 0-->
    <!--ko foreach:{data:$data.questions,as:'question'}-->
    <div class="h-set-homework examTopicBox">
        <div class="seth-hd">
            <p class="fl">
                <span data-bind="text:question.contentType">&nbsp;</span>
                <span class="border-none" data-bind="text:$root.getQuestionDifficultyName(question.difficulty)">&nbsp;</span>
            </p>
        </div>
        <div class="seth-mn iconWrapper">
            <div style="padding: 20px 0 40px;">
                <ko-venus-question params="questions:$root.getQuestion(question.qid),contentId: 'subject_' + question.qid,formulaContainer:'tabContentHolder'"></ko-venus-question>
            </div>
            <div class="icon-b" data-bind="css:{'icon-correct-b':!(question.rate && question.rate > 0),'icon-error-b' : question.rate && question.rate > 0}">
                <div class="inner" data-bind="if:question.rate && question.rate > 0,visible:question.rate && question.rate > 0">
                    <div class="text">失分率</div>
                    <div class="item" data-bind="text:question.rate + '%'">&nbsp;</div>
                </div>
                <div class="inner" data-bind="if:!(question.rate && question.rate > 0),visible:!(question.rate && question.rate > 0)">
                    <div class="text">全部正确</div>
                    <div class="item"><span class="icon-correct-s"></span></div>
                </div>
            </div>
            <div class="testPaper-info">
                <div class="linkGroup">
                    <a class="view_exam_answer" style="display: none;" href="javascript:void(0);">查看答案与解析</a>
                </div>
            </div>
        </div>
        <div class="t-error-info w-table" style="margin-top: 1px;">
            <table>
                <thead>
                    <tr>
                        <td style="width: 190px;">答案</td>
                        <td>对应同学</td>
                    </tr>
                </thead>
                <!--ko if:question.errorAnswerList && question.errorAnswerList.length > 0-->
                <tbody data-bind="foreach:{data:question.errorAnswerList,as:'answerObj'}">
                    <tr data-bind="css:{'odd':(($index()+1)/2 == 0)}">
                        <td class="txt-green" data-bind="css:{'txt-green':answerObj.answer == '答案正确'},text:answerObj.answer">{answer}</td>
                        <td data-bind="text:$root.getUserNames(answerObj.users)"></td>
                    </tr>
                </tbody>
                <!--/ko-->
            </table>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->
</script>

<script type="text/html" id="IMAGETEXTRHYME">
    <div class="cultureBox">
        <p class="cultureTitle">图文入韵</p>
        <div class="cultureCard">
            <p class="culConTitle" style="display: none">图文入韵</p>
            <div class="level">
                <ul data-bind="foreach:{data:$data.result,as:'chapterObj'}">
                    <li class="active" data-bind="css:{'active':$root.focusChapter() && $root.focusChapter().chapterId == chapterObj.chapterId},click:$root.switchChapter.bind($data,$root),text:chapterObj.title">&nbsp;</li>
                </ul>
            </div>
            <div class="tabBox">
                <ul class="tHead">
                    <li>
                        <span class="tName">姓名</span>
                        <span class="tNumber">评星</span>
                        <span class="tAverage">作品</span>
                    </li>
                </ul>
                <ul class="tBody" data-bind="foreach:{data:$root.fetchCurrentStudents(),as:'student'}">
                    <li>
                        <span data-bind="text:student.studentName">&nbsp;</span>
                        <span class="starIcon">
                            <!--ko foreach:ko.utils.range(1, student.score)-->
                            <i class="starYellow"></i>
                            <!--/ko-->
                            <!--ko foreach:ko.utils.range(1, 3 - student.score)-->
                            <i class="sterGray"></i>
                            <!--/ko-->
                        </span>
                        <span class="prev" data-bind="click:$root.previewChapter.bind($data,$root)"><u>预览</u></span>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="CHINESECHARACTERCULTURE">
    <div class="cultureBox">
        <p class="cultureTitle">汉字文化</p>
        <div class="cultureCard">
            <p class="culConTitle" style="display: none">汉字文化</p>
            <div class="level">
                <ul data-bind="foreach:{data:$data.result,as:'courseObj'}">
                    <li data-bind="css:{'active':$root.focusCourse() && $root.focusCourse().courseId == courseObj.courseId},click:$root.switchCourse.bind($data,$root),text:courseObj.title">&nbsp;</li>
                </ul>
            </div>
            <div class="tabBox tabBox-2">
                <ul class="tHead ">
                    <li>
                        <span class="tName">姓名</span>
                        <span class="tNumber">完成情况</span>
                    </li>
                </ul>
                <ul class="tBody" data-bind="foreach:{data:$root.fetchCurrentStudents(), as: 'student'}">
                    <li>
                        <span data-bind="text:student.studentName">&nbsp;</span>
                        <span data-bind="text:student.finished ? '已完成' : '未完成'">&nbsp;</span>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</script>


<script type="text/html" id="UNKNOWN_TEMPLATE">
    <div class="w-base-title" style="background-color: #e1f0fc;"><h3>未知模板</h3></div>
    <div class="h-set-homework">暂没有此模块的页面模板</div>
</script>

<script type="text/javascript">
    var answerDetailData = {
        env : <@ftlmacro.getCurrentProductDevelopment />,
        imgDomain : '${imgDomain!''}',
        domain : '${requestContext.webAppBaseUrl}/'
    };

    function nextHomeWork(){
        $.prompt.close();
    }
</script>
    <@sugar.capsule js=["ko","homeworkv5.clazzwordteachmoduledetail"] />
</@shell.page>