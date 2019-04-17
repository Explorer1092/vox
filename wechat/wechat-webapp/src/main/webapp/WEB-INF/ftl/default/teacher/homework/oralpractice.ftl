<div class="mse-main" data-bind="if:$data.selectedHomeworkType() == 'ORAL_PRACTICE',visible : $data.selectedHomeworkType() == 'ORAL_PRACTICE' ">
    <div class="topicPackage-tab" data-bind="visible : $data.oralPracticePackageList().length > 1">
        <div class="topicPackage-list" id="oralPracticePackageTopMenuBox">
            <ul data-bind="style: {width: ($data.oralPracticePackageList().length)*13 +'rem'}"><!--13rem*li的个数-->
                <!--ko foreach : {data : $data.oralPracticePackageList(), as : "_package"}-->
                <li data-bind="click: $root.oralPracticePackageBoxSelected, css : {'active':_package.checked()},attr:{'data-index':$index()}">
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
        <!--ko if: $data.oralPracticePackageList().length > 2-->
        <div class="allBtn" data-bind="click: $root.oralPracticePackageAllBtn">...<br>全部</div>
        <!--/ko-->
    </div>

    <div class="mhw-kwdbaType" data-bind="visible: $root.oralPracticeCurrentPackageId() != -1">
        <div class="fl">
            共<span class="txtBlue" data-bind="text:$data.oralPracticeCurrentPackageDetail().length+'题'">--</span>，
            预计<span class="txtBlue" data-bind="text: $root.oralPracticePackageTotalMin()+'分钟'">--</span>完成</div>
        <div class="fr">
            <!--ko ifnot: $root.oralPracticePackageIsSelectAll() -->
            <a class="btn w-btn w-btn-s" data-bind="click: $root.oralPracticePackageSelectAllBtn" style="cursor: pointer;">全部选入</a>
            <!-- /ko -->
            <!--ko if: $root.oralPracticePackageIsSelectAll() && $data.oralPracticeExamPackageList().length > 0 -->
            <a class="btn w-btn w-btn-s w-btn-lightBlue" data-bind="click: $root.oralPracticePackageClearAllBtn" style="cursor: pointer;">全部移除</a>
            <!-- /ko -->
        </div>
    </div>

    <div>
    <#--题包详情展示-->
        <div id="oralPracticePackageBox">
            <div data-bind="foreach : {data : $data.oralPracticeExamPackageList(), as : '_questions'}">
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
                            <div data-bind="attr:{id : 'oralPracticePackageSingleImg' + $index()}"></div>
                            <div data-bind="text:$root.loadOralPracticeSingleExam(_questions.id(),$index())"></div>
                        </div>
                        <div class="mhw-selectBtns mar-t14">
                            <!--ko ifnot: _questions.checked()-->
                            <a data-bind="click: $root.addOralPracticeExam" href="javascript:void(0)" class="btn w-btn w-btn-s">
                                <strong>+</strong>选入
                            </a>
                            <!--/ko-->

                            <!--ko if: _questions.checked()-->
                            <a data-bind="click: $root.removeOralPracticeExam" class="btn w-btn w-btn-s w-btn-lightBlue" href="javascript:void(0)">
                                <strong>-</strong>移除
                            </a>
                            <!--/ko-->
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>