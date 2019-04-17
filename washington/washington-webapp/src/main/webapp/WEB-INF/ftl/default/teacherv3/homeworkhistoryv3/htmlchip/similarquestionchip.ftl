<script id="t:PREVIEW_QUESTION" type="text/html">
    <div class="topicDetails-dialog" style="margin-top:-20px;">
        <div class="tips tips-grey">
            <p>
                <!--ko text:submitNum--><!--/ko-->人提交，
                <!--ko if:$root.tabType != 'ORAL_PRACTICE'--><!--ko text:error--><!--/ko-->人错误&nbsp;&nbsp;<!--/ko-->
                <!--ko if:$root.tabType == 'ORAL_PRACTICE'-->平均分<!--ko text:averageScore--><!--/ko-->分<!--/ko-->
            </p>
        </div>
        <div class="h-set-homework">
            <div class="seth-hd" style="display: none;">
                <p class="fl"><span data-bind="text:questionObject().questionType">&nbsp;</span><span class="noBorder" data-bind="text:questionObject().difficultyName">&nbsp;</span></p>
            </div>
            <div class="seth-mn">
                <div class="testPaper-info" id="tabContent_201811081212">
                    <div class="inner">
                        <!--ko if:useVenus-->
                        <ko-venus-question params="questions:$root.getQuestion($root.questionId),contentId:'mathExamImgSourceElem',formulaContainer:'tabContent_201811081212'"></ko-venus-question>
                        <!--/ko-->
                        <!--ko ifnot:useVenus-->
                        <div class="box" id="mathExamImgSource"></div>
                        <div data-bind="text:$root.loadExamImg('mathExamImgSource','examImg',questionObject().questionId(),0)"></div>
                        <!--/ko-->
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>