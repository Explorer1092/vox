<div class="mse-main" data-bind="visible : $data.selectedHomeworkType() == 'WORD_PRACTICE' ">
    <div class="slide-top clearfix" id="w_questionsSearchBox">
        <div class="item item-all"  data-bind="visible : true">全部题目</div>
        <div class="item item-slide slideUp" data-bind="if : $data.wordArrangeSearchDetail().length > 0">
            <div class="mhw-menuBox">
                <div class="slideItem">
                    <label for="wordArrangeSearch">布置</label>
                    <select data-bind="
                    options: wordArrangeSearchDetail(),
                    optionsText : 'name',
                    value: wordSearchByArrangeVal,
                    event: {change: wordPracticeWordSearch.bind($data,'arrange')}" id="wordArrangeSearch" class="int"></select>
                </div>
            </div>
        </div>

        <div class="item item-slide slideUp" data-bind="if : $data.wordPracticeCategoriesSearchDetail().length > 0">
            <div class="mhw-menuBox">
                <div class="slideItem">
                    <label for="wordPracticeCategoriesSearch">类别</label>
                    <select data-bind="
                    options: wordPracticeCategoriesSearchDetail(),
                    value: wordCurrentPracticeCategoriesSearchVal ,
                    event: {change: wordPracticeWordSearch.bind($data,'categories')}" id="wordPracticeCategoriesSearch" class="int"></select>
                </div>
            </div>
        </div>

        <div class="item item-slide slideUp" data-bind="if: $data.wordPracticeWordSearchDetail().length > 0">
            <div class="mhw-menuBox">
                <div class="slideItem">
                    <label for="wordPracticeWordSearch">生字</label>
                    <select id="wordPracticeWordSearch" data-bind="options: $data.wordPracticeWordSearchDetail(), value : wordCurrentPracticeWordSearchVal, event: {change: wordPracticeWordSearch.bind($data,'practice')}" class="int"></select>
                </div>
            </div>
        </div>
    </div>
    <div data-bind="if:$data.wordPracticeQuestionList().length == 0, visible : $data.wordPracticeQuestionList().length == 0" style="display: none; text-align: center;">没有对应题目</div>
    <div id="wordQuestionsListBox" data-bind="foreach : {data : $data.wordPracticeQuestionList(), as : '_questions'}">
        <div class="mhw-base mar-b20">
            <div class="mb-hd clearfix">
                <div class="fl txt-grey">
                    <span data-bind="text: _questions.questionType()"></span>
                    <span data-bind="text: _questions.difficultyName()"></span>
                    <#--<span data-bind="visible: _questions.assignTimes() > 0" style="display: none">共被使用<span data-bind="text: _questions.assignTimes()" style="padding: 0"></span>次</span>-->
                </div>
            <div class="fr txt-red" data-bind="visible: _questions.teacherAssignTimes() > 0" style="display: none;">
                <span>您已布置<span data-bind="text: _questions.teacherAssignTimes()" style="padding: 0"></span>次</span>
            </div>
            </div>
            <div class="mb-mn pad-30">
                <div data-bind="click: $root.viewPackage.bind($data,'question')">
                    <div data-bind="attr:{id : 'wordPracticeImg' + $index()}"></div>
                    <div data-bind="text:$root.loadWordPracticeImg(_questions.id(),$index())"></div>
                </div>
                <div class="mhw-selectBtns mar-t14">

                    <!--ko ifnot: _questions.checked()-->
                    <a data-bind="click: $root.addWordPractice" href="javascript:void(0)" class="btn w-btn w-btn-s">
                        <strong>+</strong>选入
                    </a>
                    <!--/ko-->

                    <!--ko if: _questions.checked()-->
                    <a data-bind="click: $root.removeWordPractice" class="btn w-btn w-btn-s w-btn-lightBlue" href="javascript:void(0)">
                        <strong>-</strong>移除
                    </a>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
</div>
