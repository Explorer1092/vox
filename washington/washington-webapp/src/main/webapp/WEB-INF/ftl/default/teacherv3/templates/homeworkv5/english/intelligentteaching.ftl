<script id="t:INTELLIGENT_TEACHING" type="text/html">
    <!--ko if:$root.examLoading && !examLoading() && $root.bigPackageList && bigPackageList().length > 0-->
    <div class="h-topicPackage">
        <div class="topicBox">
            <ul>
                <!--ko foreach:{data:bigPackageList(),as:'bigPackage'}-->
                <li data-bind="css:{'active':$root.focusBigPackage() && bigPackage.id() == $root.focusBigPackage().id(),'special':bigPackage.id() == '-1'},click:$root.viewPackage.bind($data,$root,$index())">
                    <i class="recommended" data-bind="if:bigPackage.showAssigned && bigPackage.showAssigned(),visible:bigPackage.showAssigned && bigPackage.showAssigned()"></i>
                    <p data-bind="attr:{title:bigPackage.name()}"><!--ko text:bigPackage.name()--><!--/ko--></p>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <div class="line"></div>
    </div>
    <!--ko if:$root.smallPackageList && $root.smallPackageList().length > 0-->
    <!--ko if:$root.focusBigPackage().flag() == "package"-->
    <div class="new-topicPackage-hd">
        <div class="l-topic-inner">
            <div class="title">
                <div data-bind="text:$root.focusBigPackage().name()">&nbsp;</div>
            </div>
        </div>
        <div class="allCheck" data-bind="css:{'checked' : $root.focusBigPackage().selectAll()},click:$root.addOrRemovePackage"><!--全选被选中时添加类checked-->
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:'全选' + $root.focusBigPackage().totalCount() + '道题'">&nbsp;</span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.focusBigPackage().totalMin() + '分钟'">&nbsp;</i></span>
        </div>
    </div>
    <!--/ko-->

    <!--ko foreach:{data:$root.smallPackageList, as:'smallPackage'}-->
    <intelligent-teaching-smallpackage params="{smallPackage:smallPackage,showHeader:$root.smallPackageList().length > 1,
        viewCourse:$root.viewCourse.bind($root),addOrRemoveSmallPackage:$root.addOrRemoveSmallPackage.bind($root)}"></intelligent-teaching-smallpackage>
    <!--/ko-->

    <!--/ko-->
    <!--/ko-->

    <!--ko if:!examLoading() && !$root.focusBigPackage() && (!$root.smallPackageList || $root.smallPackageList().length == 0)-->
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


<script id="t:intelligent-teaching-smallpackage" type="text/html">
    <div class="h-set-homework examTopicBox">
        <div class="seth-hd it-header" data-bind="if:showHeader,visible:showHeader">
            <p class="fl">
                <span class="packageTitle" data-bind="text:smallPackage.title">&nbsp;</span>
            </p>
            <p class="fr">
                <span class="txtYellow" data-bind="if:smallPackage.assigned && smallPackage.assigned(),visible:smallPackage.assigned && smallPackage.assigned()">布置过</span>
                <span class="btn" style="cursor: pointer;" data-bind="css:{'btn-cancel':smallPackage.checked && smallPackage.checked(),'btn':!(smallPackage.checked && smallPackage.checked())},click:addOrRemoveSmallPackage">
                    <i></i>
                    <a class="selectBtn" href="javascript:void(0);" data-bind="text:smallPackage.checked && smallPackage.checked() ? '移除':'选入'">&nbsp;</a>
                </span>
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <!--ko foreach:{data:questions(),as:'question'}-->
                    <div class="box">
                        <div class="subjectNum" data-tip="题号"><span data-bind="text:($index() + 1)"></span></div>
                        <div class="teachingQuestion">
                            <ko-venus-question params="questions:$parent.getQuestion(question.id()),contentId:'examImg-' + $index() + '_' + (new Date().getTime()),formulaContainer:'tabContent'"></ko-venus-question>
                        </div>
                    </div>
                    <!--/ko-->
                    <ul class="h-analysis-box2">
                        <li class="postInfo">
                            <div class="c-title">课程辅导 <i class="tag">辅导做错的学生</i></div>
                            <div class="text">
                                <div class="coursePic" data-bind="click:viewCourse">
                                    <i class="playBtn" style="cursor: pointer;"></i>
                                </div>
                                <div class="desc" style="cursor: pointer;" data-bind="text:smallPackage.courseName,click:viewCourse">&nbsp;</div>
                            </div>
                            <i class="tagIcon"></i>
                        </li>
                        <li class="postInfo lastChild">
                            <div class="c-title">测试巩固题 <i class="tag">测试学生能否最终学会</i></div>
                            <!--ko if:postQuestions && postQuestions().length > 0 -->
                            <div class="context" data-bind="foreach:{data:postQuestions(),as:'postQuestion'}">
                                <div class="box">
                                    <div class="subjectNum" data-tip="题号"><span data-bind="text:($index() + 1)"></span></div>
                                    <div class="teachingQuestion">
                                        <ko-venus-question params="questions:$parent.getQuestion(postQuestion.id()),contentId:'postExamImg-' + $index() + '_' + (new Date().getTime()),formulaContainer:'tabContent'"></ko-venus-question>
                                    </div>
                                </div>
                            </div>
                            <!--/ko-->
                            <i class="tagIcon"></i>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</script>