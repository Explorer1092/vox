<div class="mhw-testPaper-Detail" data-bind="visible : $data.showQuizViewBox" style="display: none;">
    <div class="mhw-header">
        <h2 class="title">试卷详情</h2>
    </div>

    <!-- ko foreach: {data : $data.quizViewDetail(), as : '_qv'} -->
    <!-- ko foreach: {data : _qv.papers(), as : '_qvp'} -->
        <div class="mtd-main">
            <div class="title">
                <p data-bind="text: _qvp.title()"></p>
                <p class="font-18">出卷人：<span data-bind="text: _qvp.paperSource()"></span> </p>
            </div>
            <!-- ko foreach: {data : _qvp.questions(), as : '_qvpq'} -->
                <div class="mhw-base mar-b20">
                    <div class="mb-hd clearfix">
                        <div class="fl txt-grey">
                            <span data-bind="text: _qvpq.questionType()"></span>
                            <span data-bind="text: _qvpq.difficultyName()"></span>
                        </div>
                        <#--<div class="fr txt-red" data-bind="visible:_qvpq.teacherAssignTimes() > 0" style="display: none;">
                            <span data-bind="text: '您已布置'+_qvpq.teacherAssignTimes()+'次'"></span>
                        </div>-->
                    </div>
                    <div class="mb-mn pad-30">
                        <div data-bind="attr:{id : 'quizImg' + $index()}"></div>
                        <div data-bind="text:$root.loadQuizImg(_qvpq.questionId(),$index())"></div>
                    </div>
                </div>
            <!-- /ko -->
        </div>
    <!--/ko-->
    <!--/ko-->
    <div data-bind="click:$root.quizViewMoreBtn, visible: $root.quizViewTotalPage() > $root.quizViewCurrentPage()" style="padding: 0 0 2rem; text-align: center;">查看更多</div>
    <div class="footer-empty">
        <div class="btns pad-30 fixFooter">
            <a data-bind="click: $data.viewQuizBackBtn" href="javascript:void(0)" class="w-btn">返回</a>
        </div>
    </div>
</div>