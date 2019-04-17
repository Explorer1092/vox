<script id="t:INTELLIGENT_TEACHING" type="text/html">
    <!--ko if:$root.examLoading && !examLoading() && $root.packageList && packageList().length > 0-->
    <div class="h-topicPackage">
        <div class="topicBox">
            <ul>
                <!--ko foreach:{data:packageList(),as:'package'}-->
                <li data-bind="css:{'active':$root.focusPackage() && package.id() == $root.focusPackage().id(),'special':package.id() == '-1'},click:$root.viewPackage.bind($data,$root,$index())">
                    <p data-bind="attr:{title:package.name()}"><!--ko text:package.name()--><!--/ko--></p>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <div class="line"></div>
    </div>

    <!--ko if:focusExamList().length > 0 && !examLoading()-->
    <!--ko if:$root.focusPackage().flag() == "package"-->
    <div class="new-topicPackage-hd">
        <div class="l-topic-inner">
            <div class="title">
                <div data-bind="text:$root.focusPackage().name()">&nbsp;</div>
            </div>
        </div>
        <div class="allCheck" data-bind="css:{'checked' : $root.focusPackage().selCount() >= $root.focusPackage().totalCount()},click:$root.addOrRemovePackage"><!--全选被选中时添加类checked-->
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:'全选' + $root.focusPackage().totalCount() + '道题'">&nbsp;</span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.focusPackage().totalMin() + '分钟'">&nbsp;</i></span>
        </div>
    </div>
    <!--/ko-->
    <!--ko foreach:{data:focusExamList(),as:'question'}-->
    <div class="h-set-homework examTopicBox" data-bind="singleExamHover:question.checked">
        <div class="seth-hd">
            <p class="fl">
                <span data-bind="text:question.questionType"></span>
                <span data-bind="text:question.difficultyName"></span>
                <span data-bind="if:question.upImage,visible:question.upImage">支持上传解答过程</span>
                <!--ko if:question.assignTimes && question.assignTimes() > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes() == 0)-->
                <span class="noBorder" data-bind="text:'共被使用' + question.assignTimes() + '次'"></span>
                <!--/ko-->
                <!--ko if:question.questionTypeId && question.questionTypeId() == 1010013-->
                <span class="noBorder" style="color:red;" data-bind="text:'支持手写'"></span>
                <!--/ko-->
            </p>
            <p class="fr">
                <!--ko if:question.teacherAssignTimes && question.teacherAssignTimes() > 0-->
                <span class="txtYellow">布置过</span>
                <!--/ko-->
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <div class="box">
                        <ko-venus-question params="questions:$root.getQuestion($data.id()),contentId:'mathExamImg'+ $index(),formulaContainer:'tabContent'"></ko-venus-question>
                    </div>
                    <ul class="h-analysis-box">
                        <li class="postInfo">
                            <div class="c-title">课程辅导 <i class="tag">辅导做错的学生</i></div>
                            <div class="text" style="cursor: pointer;" data-bind="text:question.courseName,click:$root.viewCourse.bind($data,$root)"></div>
                            <i class="tagIcon"></i>
                        </li>
                        <li class="postInfo lastChild">
                            <div class="c-title">测试巩固题 <i class="tag">测试学生能否最终学会</i></div>
                            <div class="context" data-bind="foreach:{data:question.postQuestions(),as:'postQuestion'}">
                                <div class="box">
                                    <ko-venus-question params="questions:$root.getQuestion($data.id()),contentId:'mathExamImg_post' + $parentContext.$index() + $index(),formulaContainer:'tabContent'"></ko-venus-question>
                                </div>
                            </div>
                            <i class="tagIcon"></i>
                        </li>
                    </ul>
                </div>
                <#--<div class="linkGroup">
                    <a href="javascript:void(0)" style="display: none;" class="viewExamAnswer" data-bind="click:$root.viewExamAnswer.bind($data,$root,$index())">查看答案解析</a>
                    <a href="javascript:void(0)" style="display: none;" class="feedback" data-bind="click:$root.feedback.bind($data,$root)">反馈</a>
                </div>-->
            </div>
        </div>
    </div>
    <!--/ko-->
    <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:$root.pagination,if:$root.focusExamList().length > 0}"></div>
    <!--/ko-->
    <!--/ko-->

    <!--ko if:!examLoading() && !$root.focusPackage() && (!$root.packageList() || $root.packageList().length == 0 || focusExamList().length == 0)-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>没有找到题目</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:examLoading,visible:examLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
</script>
