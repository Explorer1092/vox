<div class="mhw-taskPreview" data-bind="visible: $data.showViewHomeworkBox" style="display: none;">
    <div class="mhw-header mar-b14">
        <div class="header-inner">
            <h2 class="title">作业列表</h2>
            <div class="switch-box slideUp"><!--通过slideUp切换箭头-->
                <div class="mhw-menuBox" data-bind="css: {'slideUp' : $data.showViewHomeworkSelectBox}">
                    <div class="slideItem" data-bind="click: $data.showViewHomeworkTitleClick">切换作业类型<span class="arrow"></span></div>
                    <ul class="slideInfo" data-bind="visible: $data.showViewHomeworkSelectBox">
                        <li data-bind="visible : $root.basicAppCardCount().questionCount > 0,click : $root.showViewHomeworkClick.bind($data,'BASIC_APP'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'BASIC_APP'},text: $root.getPackageNameByType('BASIC_APP')">--</li>
                        <li data-bind="visible : $root.examCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'EXAM'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'EXAM'},text: $root.getPackageNameByType('EXAM')">--</li>
                        <li data-bind="visible : $root.mentalCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'MENTAL'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'MENTAL'},text: $root.getPackageNameByType('MENTAL')">--</li>
                        <li data-bind="visible : $root.readingCardCount() > 0,click : $root.showViewHomeworkClick.bind($data,'READING'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'READING'},text: $root.getPackageNameByType('READING')">--</li>
                        <li data-bind="visible : $root.wordPracticeCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'WORD_PRACTICE'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'WORD_PRACTICE'},text: $root.getPackageNameByType('WORD_PRACTICE')">--</li>
                        <li data-bind="visible : $root.readReciteCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'READ_RECITE'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'READ_RECITE'},text: $root.getPackageNameByType('READ_RECITE')">--</li>
                        <li data-bind="visible : $root.quizQuestionsCount() > 0,click : $root.showViewHomeworkClick.bind($data,'UNIT_QUIZ'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'UNIT_QUIZ'},text: $root.getPackageNameByType('UNIT_QUIZ')">--</li>
                        <li data-bind="visible : $root.photoObjectiveCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'PHOTO_OBJECTIVE'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'PHOTO_OBJECTIVE'},text: $root.getPackageNameByType('PHOTO_OBJECTIVE')">--</li>
                        <li data-bind="visible : $root.voiceObjectiveCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'VOICE_OBJECTIVE'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'VOICE_OBJECTIVE'},text: $root.getPackageNameByType('VOICE_OBJECTIVE')">--</li>
                        <li data-bind="visible : $root.oralPracticeCartCount() > 0,click : $root.showViewHomeworkClick.bind($data,'ORAL_PRACTICE'),css : {'active' : $root.viewHomeworkSelectedTabName() == 'ORAL_PRACTICE'},text: $root.getPackageNameByType('ORAL_PRACTICE')">--</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <div class="mtp-main">
        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'EXAM' || $root.viewHomeworkSelectedDetail().type == 'ORAL_PRACTICE'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType($root.viewHomeworkSelectedDetail().type)">&nbsp;</div>
            <!-- ko foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vh'} -->
            <div class="mhw-base mar-b20">
                <div class="mb-hd clearfix">
                    <div class="fl txt-grey">
                        <span data-bind="text: _vh.questionType"></span>
                        <span data-bind="text: _vh.difficultyName"></span>
                    </div>
                </div>
                <div class="mb-mn pad-30">
                    <div data-bind="attr:{id : 'viewHomeworkExamImg' + $index()}"></div>
                    <div data-bind="text:$root.viewHomeworkExamImg(_vh.questionId,$index())"></div>
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'MENTAL'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('MENTAL')">口算练习</div>
            <div class="mhw-base mar-b20">
                <div class="mb-mn pad-30">
                    <div class="mhw-subject">
                        <table data-bind="foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vme'}">
                            <tr>
                                <td data-bind="text: $index() + 1+'.'"></td>
                                <td class="info" data-bind="html: $root.displayQuestionContent(_vme.questionContent)"></td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'UNIT_QUIZ'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.viewHomeworkSelectedDetail().typeName"></div>
            <!-- ko foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vuq'} -->
            <!--ko if: $index() < 10 -->
            <div class="mhw-base mar-b20">
                <div class="mb-hd clearfix">
                    <div class="fl txt-grey">
                        <span data-bind="text: _vuq.questionType"></span>
                        <span data-bind="text: _vuq.difficultyName"></span>
                        <#--<span>被使用100次</span>-->
                    </div>
                    <#--<div class="fr txt-red"><span>您已布置2次</span></div>-->
                </div>
                <div class="mb-mn pad-30">
                    <div data-bind="attr:{id : 'viewHomeworkExamImg' + $index()}"></div>
                    <div data-bind="text:$root.viewHomeworkExamImg(_vuq.questionId,$index())"></div>
                </div>
            </div>
            <!--/ko-->
            <!--/ko-->
        </div>
        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'PHOTO_OBJECTIVE'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('PHOTO_OBJECTIVE')">动手做一做</div>
            <!-- ko foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vp'} -->
            <div class="mhw-base mar-b20">
                <div class="mb-mn pad-30">
                    <div data-bind="attr:{id : 'viewHomeworkExamImg' + $index()}"></div>
                    <div data-bind="text:$root.viewHomeworkExamImg(_vp.questionId,$index())"></div>
                </div>
            </div>
            <!--/ko-->
        </div>

        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'VOICE_OBJECTIVE'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('VOICE_OBJECTIVE')">概念说一说</div>
            <!-- ko foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vv'} -->
            <div class="mhw-base mar-b20">
                <div class="mb-mn pad-30">
                    <div data-bind="attr:{id : 'viewHomeworkExamImg' + $index()}"></div>
                    <div data-bind="text:$root.viewHomeworkExamImg(_vv.questionId,$index())"></div>
                </div>
            </div>
            <!--/ko-->
        </div>

        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'WORD_PRACTICE'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('WORD_PRACTICE')">生字词练习</div>
            <!-- ko foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vh'} -->
            <div class="mhw-base mar-b20">
                <div class="mb-hd clearfix">
                    <div class="fl txt-grey">
                        <span data-bind="text: _vh.questionType"></span>
                        <span data-bind="text: _vh.difficultyName"></span>
                    </div>
                </div>
                <div class="mb-mn pad-30">
                    <div data-bind="attr:{id : 'viewHomeworkExamImg' + $index()}"></div>
                    <div data-bind="text:$root.viewHomeworkExamImg(_vh.questionId,$index())"></div>
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'READ_RECITE'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('READ_RECITE')">课文读背题</div>
            <!-- ko foreach: {data : $root.viewHomeworkSelectedDetail().questions, as : '_vh'} -->
            <div class="mhw-base mar-b20">
                <div class="mb-hd clearfix">
                    <div class="fl txt-grey">
                        <span data-bind="text: _vh.paragraphCName"></span>
                        <span data-bind="text: _vh.articleName"></span>
                    </div>
                </div>
                <div class="mb-mn pad-30">
                    <div data-bind="attr:{id : 'viewHomeworkExamImg' + $index()}"></div>
                    <div data-bind="text:$root.viewHomeworkExamImg(_vh.questionId,$index())"></div>
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'BASIC_APP'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('BASIC_APP')">基础练习</div>
            <div class="homeworkView-box">
                <!-- ko foreach : {data : $root.viewHomeworkSelectedDetail().lessons, as : '_lesson'} -->
                <div class="ex-header">
                    <div class="ex-fLeft"><span data-bind="text: _lesson.lessonName"></span></div>
                </div>
                <div class="d-container mar-b20">
                    <ul class="ex-side">
                        <!-- ko foreach : {data : _lesson.categories, as : '_categories'} -->
                        <li>
                            <div class="ex-left">
                                <span class="ex-content">
                                    <img src="" data-bind="attr : {'src': '/public/images/teacher/homework/english/basicappicon/e-icons-'+_categories.categoryIcon+'.png'}" alt="">
                                </span>
                                <span class="des" data-bind="text: _categories.categoryName">--</span>
                            </div>

                        </li>
                        <!--/ko-->
                    </ul>
                </div>
                <!--/ko-->
            </div>
        </div>
        <!--/ko-->

        <!--ko if: $root.viewHomeworkSelectedDetail().type == 'READING'-->
        <div class="mtp-list">
            <div class="list-hd" data-bind="text: $root.getPackageNameByType('READING')">绘本阅读</div>
            <div class="pictureBook-list">
                <ul class="pb-box clearfix">
                    <!-- ko foreach : {data : $root.viewHomeworkSelectedDetail().pictureBooks, as : '_pictureBooks'} -->
                    <li>
                        <div class="pic">
                            <img src="" data-bind="attr: {'src' : _pictureBooks.pictureBookThumbImgUrl}">
                            <div class="title"><span data-bind="text: _pictureBooks.pictureBookName"></span></div>
                            <div class="label">
                                <span data-bind="visible: _pictureBooks.teacherAssignTimes">已出过</span>
                                <span data-bind="if: _pictureBooks.hasOral,visible:_pictureBooks.hasOral">跟读</span>
                            </div>
                        </div>
                        <div class="intro">
                            <div class="pb-text">
                                <p class="text" data-bind="text: _pictureBooks.pictureBookSeries">--</p>
                                <p class="text">
                                    <!-- ko foreach : {data : _pictureBooks.pictureBookClazzLevels, as : '_level'} -->
                                    <span data-bind="text: _level">--</span>
                                    <!--/ko-->
                                </p>
                                <p class="text" data-bind="text: _pictureBooks.pictureBookTopics.join(',')">--</p>
                            </div>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
        <!--/ko-->
    </div>
    <div class="footer-empty">
        <div class="btns pad-30 fixFooter">
            <a data-bind="click: $data.viewHomeworkBackBtn" href="javascript:void(0)" class="w-btn">返回</a>
        </div>
    </div>
</div>