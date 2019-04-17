<script id="t:DICTATION" type="text/html">

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:$root.ctLoading && $root.ctLoading(),visible:$root.ctLoading && $root.ctLoading()">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>

    <!--ko if:$root.ctLoading && !$root.ctLoading()-->
    <!--ko if:$root.lessonList && $root.lessonList().length > 0-->
    <div class="new-topicPackage-hd">
        <div class="l-topic-inner">
            <div class="title">
                <div class="dictation-title"><!--ko text:$root.title--><!--/ko--><i class="dictation-help"></i></div>
            </div>
        </div>
        <div class="allCheck" data-bind="css:{'checked' : $root.lessonIds().length > 0 && $root.lessonIds().length == $root.lessonList().length},click:$root.addOrRemovePackage"><!--全选被选中时添加类checked-->
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:'全选' + $root.totalCount() + '道题'">&nbsp;</span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.totalMin() + '分钟'">&nbsp;</i></span>
        </div>
    </div>

    <!--ko foreach:{data:$root.lessonList(),as:'lessonObj'}-->
    <div class="h-set-homework examTopicBox" data-bind="singleExamHover:false">
        <div class="seth-hd">
            <p class="fl">
                <span class="noBorder" data-bind="text:lessonObj.lessonName"></span>
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <div class="wordListBox">
                        <ul data-bind="foreach:{data:lessonObj.questions,as:'question'}">
                            <li class="worldSingle" data-bind="css:{'selectedSingle':$root.questionIds.indexOf(question.id) != -1},text:question.sentence,click:$root.addOrRemoveQuestion.bind($data,lessonObj,$root,'single')">&nbsp;</li>
                        </ul>
                    </div>
                </div>
                <div class="btnGroup">
                    <a href="javascript:void(0)" data-bind="if:$root.previewLessonBtns.indexOf(lessonObj.lessonId) != -1,visible:$root.previewLessonBtns.indexOf(lessonObj.lessonId) != -1,click:$root.previewWords.bind($data,$root)">预览</a>
                    <a href="javascript:void(0)" class="btn" data-bind="if:$root.lessonIds.indexOf(lessonObj.lessonId) == -1,visible:$root.lessonIds.indexOf(lessonObj.lessonId) == -1, click:$root.addLesson.bind($data,$root)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                    <a href="javascript:void(0)" class="btn cancel" data-bind="if:$root.lessonIds.indexOf(lessonObj.lessonId) != -1,visible:$root.lessonIds.indexOf(lessonObj.lessonId) != -1,click:$root.removeLesson.bind($data,$root)"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->

    <!--ko ifnot:$root.lessonList && $root.lessonList().length > 0-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>没有推荐题目</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <!--/ko-->

</script>


<script type="text/html" id="t:SUBMIT_WAY_SELECT_POPUP">
    <div class="choiceBox">
        <!--ko foreach:{data:$root.submitWays,as:'wayObj'}-->
        <div class="choice" data-bind="click:$root.switchWay.bind($data,$root)">
            <i class="choiceSelected" data-bind="css:{'choiceSelected':$root.selectWay() && wayObj.id == $root.selectWay().id}"></i>
            <span data-bind="text:wayObj.name">&nbsp;</span>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:SELECT_QUESTION_PREVIEW">
    <div class="pinyinBox">
        <!--ko foreach:{data:$root.questions,as:'questionObj'}-->
        <div class="english-grid">
            <span data-bind="text:questionObj.sentence"></span>
        </div>
        <!--/ko-->
    </div>
</script>