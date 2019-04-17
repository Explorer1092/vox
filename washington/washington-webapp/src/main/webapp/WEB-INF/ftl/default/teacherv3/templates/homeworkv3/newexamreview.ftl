<div id="newexamreview" class="w-base h-homeworkPreview" style="display:none;position: relative; zoom: 1;  z-index: 5;">

    <h2 class="h2-title">预览
        <span class="fr">
            <a href="javascript:void(0);" class="w-btn w-btn-green" data-bind="click:$root.backToArrange">重新选择</a>
            <a href="javascript:void(0);" class="w-btn w-btn-blue" data-bind="click:saveNewExam">布置模考</a>
        </span>
    </h2>

    <div class="hPreview-main">
        <div class="hp-title">
            <h3 data-bind="text:$root.paperName() + '(' + $root.questionCount() + '题)' "></h3>
        </div>
        <!--ko foreach:$root.questions().slice(0,$root.currentQuestionCount())-->
        <div class="h-set-homework">
            <div class="seth-hd">
                <p class="fl">
                    <span data-bind="text:$data.questionType"></span>
                    <span data-bind="text:$data.difficultyName"></span>
                </p>
            </div>
            <div class="seth-mn" data-bind="attr:{id:'subjective_'+$data.id+$index()}">题目加载中...</div>
            <div style="display: none;" data-bind="text:$root.loadQuestionContent($data,$index())"></div>
        </div>
        <!--/ko-->
        <!--ko if:$root.questions().length > $root.currentQuestionCount()-->
        <div class="t-dynamic-btn" style="margin: 8px 15px;" data-bind="click:$root.showMoreQuestion">
            <a class="more" href="javascript:void(0);" >展开更多</a>
        </div>
        <!--/ko-->
    </div>
</div>

