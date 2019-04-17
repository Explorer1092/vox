<#--题包详情预览-->
<div data-bind="visible: $root.showViewPackageBox" class="mhw-testPaper-Detail" style="display: none;">
    <div class="mhw-header mar-b14">
        <div class="header-inner">
            <div class="fl">答案与解析</div>
        </div>
    </div>
    <div class="mtd-main">
        <!-- ko foreach: {data : $data.examPackageQuestionObj(), as : '_exam'} -->
        <!-- ko foreach: {data : _exam.questions, as : '_examq'} -->
        <div class="mhw-base mar-b20">
            <div class="mb-hd clearfix">
                <div class="fl txt-grey">
                    <span data-bind="text: _examq.questionType"></span>
                    <span data-bind="text: _examq.difficultyName"></span>
                    <#--<span data-bind="visible: _examq.assignTimes > 0" style="display: none;">被使用<span data-bind="text: _examq.assignTimes" style="padding: 0"></span>次</span>-->
                    <span data-bind="visible: _examq.upImage" style="display: none;">支持上传解答过程</span>
                </div>
                <div class="fr txt-red" data-bind="visible: _examq.teacherAssignTimes > 0" style="display: none;">
                    <span>您已布置<span data-bind="text: _examq.teacherAssignTimes" style="padding: 0"></span>次</span>
                </div>
            </div>
            <div class="mb-mn pad-30">
                <div data-bind="attr:{id : 'mathExamPackageImg' + $index()}"></div>
                <div data-bind="text:$root.loadExamPackageImg(_examq.questionId,$index())"></div>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
    <div class="footer-empty">
        <div class="btns pad-30 fixFooter">
            <a data-bind="click: $data.viewPackageBackBtn" href="javascript:void(0)" class="w-btn">返回</a>
        </div>
    </div>
</div>