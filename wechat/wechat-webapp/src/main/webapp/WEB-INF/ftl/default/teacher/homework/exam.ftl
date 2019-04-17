<div class="mse-main" data-bind="visible : $data.selectedHomeworkType() == 'EXAM' ">
    <div class="topicPackage-tab" data-bind="visible : $data.packageList().length > 1">
        <div class="topicPackage-list" id="examPackageTopMenuBox">
            <ul data-bind="style: {width: ($data.packageList().length)*13 +'rem'}"><!--13rem*li的个数-->
                <!--ko foreach : {data : $data.packageList(), as : "_package"}-->
                <li data-bind="click: $root.packageBoxSelected, css : {'active':_package.checked()},attr:{'data-index':$index()}">
                    <p class="name" data-bind="text: _package.name()">--</p>
                    <!--ko if: _package.selCount() * 1 != 0 -->
                    <span class="state" data-bind="text: _package.selCount">--</span>
                    <!--/ko-->

                    <!--ko if: _package.packageUsed() && _package.selCount() == 0 -->
                    <span class="state state-grey">用过</span>
                    <!--/ko-->
                    <span class="arrow"></span>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <!--ko if: $data.packageList().length > 2-->
        <div class="allBtn" data-bind="click: $root.examPackageAllBtn">...<br>全部</div>
        <!--/ko-->
    </div>

    <div class="mhw-kwdbaType" data-bind="visible: $root.examCurrentPackageId() != -1">
        <div class="fl">
            共<span class="txtBlue" data-bind="text:$data.examCurrentPackageDetail().length+'题'">--</span>，
            预计<span class="txtBlue" data-bind="text: $root.examPackageTotalMin()+'分钟'">--</span>完成</div>
        <div class="fr">
            <!--ko ifnot: $root.examPackageIsSelectAll() -->
            <a class="btn w-btn w-btn-s" data-bind="click: $root.examPackageSelectAllBtn" style="cursor: pointer;">全部选入</a>
            <!-- /ko -->
            <!--ko if: $root.examPackageIsSelectAll() && ($data.examPackageList().length > 0 || $data.questionList().length > 0) -->
            <a class="btn w-btn w-btn-s w-btn-lightBlue" data-bind="click: $root.examPackageClearAllBtn" style="cursor: pointer;">全部移除</a>
            <!-- /ko -->
        </div>
    </div>

    <div>
        <#--题包详情展示-->
        <div id="examPackageBox">
            <div data-bind="foreach : {data : $data.examPackageList(), as : '_questions'}">
                <div class="mhw-base mar-b20">
                    <div class="mb-hd clearfix">
                        <div class="fl txt-grey">
                            <span data-bind="text: _questions.questionType()"></span>
                            <span data-bind="text: _questions.difficultyName()"></span>
                        <#--<span data-bind="visible: _questions.assignTimes() > 0">共被使用<span data-bind="text: _questions.assignTimes()" style="padding: 0"></span>次</span>-->
                            <span data-bind="visible: _questions.upImage()" style="display: none;">支持上传解答过程</span>
                        </div>
                        <div class="fr txt-red" data-bind="visible: _questions.teacherAssignTimes() > 0" style="display: none;">
                            <span>您已布置<span data-bind="text: _questions.teacherAssignTimes()" style="padding: 0"></span>次</span>
                        </div>
                    </div>
                    <div class="mb-mn pad-30">
                        <div data-bind="click: $root.viewPackage.bind($data,'question')">
                            <div data-bind="attr:{id : 'mathExamPackageSingleImg' + $index()}"></div>
                            <div data-bind="text:$root.loadExamPackageSingleImg(_questions.id(),$index())"></div>
                        </div>
                        <div class="mhw-selectBtns mar-t14">
                            <!--ko ifnot: _questions.checked()-->
                            <a data-bind="click: $root.addExam" href="javascript:void(0)" class="btn w-btn w-btn-s">
                                <strong>+</strong>选入
                            </a>
                            <!--/ko-->

                            <!--ko if: _questions.checked()-->
                            <a data-bind="click: $root.removeExam" class="btn w-btn w-btn-s w-btn-lightBlue" href="javascript:void(0)">
                                <strong>-</strong>移除
                            </a>
                            <!--/ko-->
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div id="hkTabcontent">
        <div class="knowledgePoint" data-bind="visible : $root.examKnowledgePointsDetail().length" style="display: none;">
            <div class="fl">
                <p class="text">
                    <!--ko if: $root.examKnowledgePointsDetail().length > 0-->
                    <!-- ko foreach : {data : $root.examKnowledgePointsDetail(), as : '_kp'} -->
                    <!-- ko foreach : {data : _kp.knowledgePoints, as : '_kpd'} -->
                    <!--ko if: _kpd.checked-->
                    知识点：
                    <!--/ko-->
                    <span data-bind="text: _kpd.kpName || '--',visible: _kpd.checked">--</span>
                    <!--/ko-->
                    <!--/ko-->
                    <!--/ko-->
                </p>
            </div>
            <div class="fr">
                <div class="mhw-selectBtns"><a href="javascript:void(0)" data-bind="click : $root.examKnowledgePointsBtn" class="btn w-btn w-btn-s">切换</a></div>
            </div>
        </div>

        <div class="slide-top clearfix" id="questionsSearchBox" style="display: none;">
            <div class="item item-all">全部题目</div>
            <div class="item item-slide slideUp" data-bind="visible: $data.examArrangeSearchDetail().length > 0">
                <div class="mhw-menuBox">
                    <div class="slideItem">
                        <label for="examArrangeSearch">布置</label>
                        <select name="" id="examArrangeSearch" data-bind="
                        options: $data.examArrangeSearchDetail(),
                        optionsText : 'name',
                        value : examCurrentArrangeSearchVal,
                        event: {change : examCurrentArrangeSearch }" class="int"></select>
                    </div>
                </div>
            </div>
            <div class="item item-slide slideUp" data-bind="visible : $data.examDifficultySearchDetail().length > 0">
                <div class="mhw-menuBox">
                    <div class="slideItem">
                        <label for="examDifficultySearch">难度</label>
                        <select name="" id="examDifficultySearch" data-bind="
                        options: $data.examDifficultySearchDetail(),
                        optionsText : 'name',
                        value : examCurrentDifficultySearchVal,
                        event: {change : examCurrentDifficultySearch }" class="int"></select>
                    </div>
                </div>
            </div>
            <div class="item item-slide slideUp" data-bind="visible: $data.examPatternsSearchDetail().length > 0">
                <div class="mhw-menuBox">
                    <div class="slideItem">
                        <label for="examPatternsSearch">题型</label>
                        <select name="" id="examPatternsSearch" data-bind="
                        options: $data.examPatternsSearchDetail(),
                        optionsText : 'name',
                        value : examCurrentPatternsSearchVal,
                        event: {change: examCurrentPatternsSearch }" class="int"></select>
                    </div>
                </div>
            </div>
        </div>
        <div data-bind="visible : $data.questionList().length == 0 && !$root.ajaxLoading()" style="display: none; text-align: center; padding: 20px;">没有对应题目</div>

        <div id="questionsListBox" data-bind="foreach : {data : $data.questionList(), as : '_questions'}">
            <div class="mhw-base mar-b20">
                <div class="mb-hd clearfix">
                    <div class="fl txt-grey">
                        <span data-bind="text: _questions.questionType()"></span>
                        <span data-bind="text: _questions.difficultyName()"></span>
                        <span data-bind="visible: _questions.upImage()" style="display: none;">支持上传解答过程</span>
                    </div>
                    <div class="fr txt-red" data-bind="visible: _questions.teacherAssignTimes() > 0" style="display: none;">
                        <span>您已布置<span data-bind="text: _questions.teacherAssignTimes()" style="padding: 0"></span>次</span>
                    </div>
                </div>
                <div class="mb-mn pad-30">
                    <div data-bind="click: $root.viewPackage.bind($data,'question')">
                        <div data-bind="attr:{id : 'mathExamImg' + $index()}"></div>
                        <div data-bind="text:$root.loadExamImg(_questions.id(),$index())"></div>
                    </div>
                    <div class="mhw-selectBtns mar-t14">
                        <!--ko ifnot: _questions.checked()-->
                        <a data-bind="click: $root.addExam" href="javascript:void(0)" class="btn w-btn w-btn-s">
                            <strong>+</strong>选入
                        </a>
                        <!--/ko-->

                        <!--ko if: _questions.checked()-->
                        <a data-bind="click: $root.removeExam" class="btn w-btn w-btn-s w-btn-lightBlue" href="javascript:void(0)">
                            <strong>-</strong>移除
                        </a>
                        <!--/ko-->
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


