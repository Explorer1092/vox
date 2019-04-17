<div data-bind="visible : $data.selectedHomeworkType() == 'UNIT_QUIZ' && $data.subject() != 'CHINESE' " style="display: none;">
    <div class="topicPackage-tab" data-bind="visible : $data.quizPackageList().length > 1">
        <div class="topicPackage-list" id="quizPackageTopMenuBox">
            <ul data-bind="style: {width: ($data.quizPackageList().length)*13 +'rem'}"><!--13rem*li的个数-->
                <!--ko foreach : {data : $data.quizPackageList(), as : "_package"}-->
                <li data-bind="click: $root.quizPackageBoxSelected, css : {'active':_package.checked()},attr:{'data-index':$index()}">
                    <p class="name" data-bind="text: _package.title()">--</p>
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
        <!--ko if: $data.quizPackageList().length > 2-->
        <div class="allBtn" data-bind="click: $root.quizPackageAllBtn">...<br>全部</div>
        <!--/ko-->
    </div>
    <div class="mhw-kwdbaType">
        <div class="fl">
            共<span class="txtBlue" data-bind="text:$data.quizCurrentPackageDetail().length+'题'">--</span>，
            预计<span class="txtBlue" data-bind="text: $root.quizPackageTotalMin()+'分钟'">--</span>完成</div>
        <div class="fr">
            <!--ko ifnot: $root.quizPackageIsSelectAll() -->
            <a class="btn w-btn w-btn-s" data-bind="click: $root.quizPackageSelectAllBtn" style="cursor: pointer;">全部选入</a>
            <!-- /ko -->
            <!--ko if: $root.quizPackageIsSelectAll() && $data.quizDetail().length > 0 -->
            <a class="btn w-btn w-btn-s w-btn-lightBlue" data-bind="click: $root.quizPackageClearAllBtn" style="cursor: pointer;">全部移除</a>
            <!-- /ko -->
        </div>
    </div>
    <div data-bind="foreach : {data : $data.quizDetail(), as : '_questions'}">
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
                    <div data-bind="attr:{id : 'quizPackageSingleImg' + $index()}"></div>
                    <div data-bind="text:$root.loadQuizPackageSingleImg(_questions.id(),$index())"></div>
                </div>
                <div class="mhw-selectBtns mar-t14">
                    <!--ko ifnot: _questions.checked()-->
                    <a data-bind="click: $root.addQuiz" href="javascript:void(0)" class="btn w-btn w-btn-s">
                        <strong>+</strong>选入
                    </a>
                    <!--/ko-->

                    <!--ko if: _questions.checked()-->
                    <a data-bind="click: $root.removeQuiz" class="btn w-btn w-btn-s w-btn-lightBlue" href="javascript:void(0)">
                        <strong>-</strong>移除
                    </a>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
</div>

