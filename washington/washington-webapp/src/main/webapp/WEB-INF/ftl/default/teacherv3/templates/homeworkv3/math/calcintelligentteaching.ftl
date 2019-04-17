<script id="t:CALC_INTELLIGENT_TEACHING" type="text/html">
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

    <!--ko if:$root.displayPointQuestions().length > 0 && !examLoading()-->
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

    <div class="oralCount" style="margin-top: 15px;" data-bind="if:$root.displayPointQuestions().length > 0,visible: $root.displayPointQuestions().length > 0">
        <div class="t-choose-Knowledge">
            <!--ko foreach:{data:$root.displayPointQuestions,as:'pointObj'}-->
            <div class="oralType" data-bind="text:pointObj.kpName + '(' + pointObj.questionCount + ')'">&nbsp;</div>
            <table class="t-questionBox" style="border-bottom: 0">
                <tbody data-bind="foreach:ko.utils.range(1,Math.ceil(pointObj.questions.length/3))">
                <tr data-bind="foreach:{data:pointObj.questions.slice($index() * 3, ($index() + 1) * 3),as:'question'}">
                    <td style="background: rgb(255, 255, 255);">
                        <div class="t-question" data-bind="attr:{'id':question.questionId + '-' + $index()}">正在加载...</div>
                        <div data-bind="text:$root.renderVueQuestion(question.questionId,$index())"></div>
                    </td>
                </tr>
                </tbody>
            </table>
            <!--ko if:pointObj.postQuestions && pointObj.postQuestions.length > 0-->
            <div style="border-bottom: 1px solid #dae6ee;">
                <ul class="h-analysis-box2">
                    <li class="postInfo" style="float:none;width: 100%;">
                        <div class="c-title">课程辅导 <i class="tag">辅导做错的学生</i></div>
                        <div class="text" style="cursor: pointer;">
                            <div class="coursePic" data-bind="click:$root.viewCourse.bind($data,$root)">
                                <i class="playBtn" style="cursor: pointer;"></i>
                            </div>
                            <div class="desc" style="cursor: pointer;line-height: normal;" data-bind="text:pointObj.courseName,click:$root.viewCourse.bind($data,$root)">&nbsp;</div>
                        </div>
                        <i class="tagIcon"></i>
                    </li>
                    <li class="postInfo lastChild" style="float:none;width: 100%;">
                        <div class="c-title">测试巩固题 <i class="tag">测试学生能否最终学会</i></div>
                        <div class="context">
                            <table class="t-questionBox" style="border-bottom:0;">
                                <tbody data-bind="foreach:ko.utils.range(1,Math.ceil(pointObj.postQuestions.length/2))">
                                <tr data-bind="foreach:{data:pointObj.postQuestions.slice($index() * 2, ($index() + 1) * 2),as:'questionId'}">
                                    <td style="background: rgba(255, 255, 255,.8);">
                                        <div class="t-question" data-bind="attr:{id : questionId + '-p-' + $parentContext.$parentContext.$index() + '-' + $index()}">正在加载...</div>
                                        <div data-bind="text:$root.renderVueQuestion(questionId,('p-' + $parentContext.$parentContext.$index() + '-' + $index()))"></div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <i class="tagIcon"></i>
                    </li>
                </ul>
            </div>
            <!--/ko-->
            <div class="w-clear"></div>
            <!--/ko-->
        </div>
    </div>

    <!--/ko-->
    <!--/ko-->

    <!--ko if:!examLoading() && !$root.focusPackage() && (!$root.packageList() || $root.packageList().length == 0 || $root.displayPointQuestions().length == 0)-->
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
