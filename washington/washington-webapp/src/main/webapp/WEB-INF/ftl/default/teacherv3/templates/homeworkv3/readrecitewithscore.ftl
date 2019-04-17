<script id="t:READ_RECITE_WITH_SCORE" type="text/html">
    <div class="md-course">
        <!--ko if: $root.packageList().length > 0-->
        <!--ko foreach: $root.packageList()-->
        <div class="course-box">
            <p class="course-title" data-bind="text:$data.lessonName"></p>
            <div class="course-list">
                <!--ko foreach: $data.readReciteList()-->
                <div class="course-list-item">
                    <div class="list-item-desc" data-bind="click:$root.packageDetail.bind($data,$parent,$root)">
                        <p class="item-title" data-bind="text:$data.questionBoxName"></p>
                        <p class="item-txt" data-bind="text:$root.selectStatus($data)"></p>
                        <div class="adjust-posit">
                            <div class="mask-box"></div>
                            <p>段落调整</p>
                        </div>
                    </div>
                    <div class="list-item-state" data-bind="css:{'current':$data.packageChecked()},click:$root.addPackage.bind($data,$root)">
                        <i></i><span data-bind="text: !$data.packageChecked()? '选入' : '移除' "></span>
                    </div>
                </div>
                <!--/ko-->
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
        <!--ko if:$root.packageList().length == 0-->
        <div class="h-set-homework current">
            <div class="seth-mn">
                <div class="testPaper-info">
                    <div class="inner" style="padding: 15px 10px; text-align: center;">
                        <p>本课没有朗读背诵的内容，您可以布置其他形式</p>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>


<script id="t:viewDetailTPL2018" type="text/html">
    <div class="aDetails-popup">
        <div class="r-inner showDetails">
            <div class="readTxt-list">
                <div class="fl-aside">
                    <p class="name" data-bind="text:$root.lessonName">&nbsp;</p>
                    <p class="textGray">请选择需要的段落</p>
                </div>
            </div>
            <div class="select-box">
                <div class="sel-option" data-bind="if:$root.categories && $root.categories().length > 0,visible:$root.categories && $root.categories().length > 0">
                    <!--ko foreach:{data:$root.categories,as:'category'}-->
                    <span data-bind="click:$root.changeQuestionType.bind($data,$root)">
                        <i data-bind="css:{'selted':$root.categoryTypes.indexOf(category.type) != -1}"></i>
                        <!--ko text:category.typeName--><!--/ko-->
                    </span>
                    <!--/ko-->
                </div>
                <div class="all-sel" data-bind="css:{'remove':$root.packageChecked()},text: !$root.packageChecked() ? '全部选入' : '全部移除',click:selAllCurrentQuestions">
                    全部选入
                </div>
            </div>
            <div class="readTxt-scrol" style="height: 350px;" data-tip="350像素解决平板电脑的问题">
                <div class="readTxt-details">
                    <!--ko if: $root.currentQuestions().length > 0-->
                    <!--ko foreach: $root.currentQuestions().slice(0,$root.showCount())-->
                    <div class="readInner" data-bind="style:{'display': $data.showQuestion() ? 'block' : 'none'}">
                        <i class="itag" style="display: none;" data-bind="visible:$data.paragraphImportant"></i>
                        <div class="r-sub-title subtitle">
                            <!--ko text:'第'+$data.paragraphNumber()+'段'--><!--/ko-->
                            <i class="label audio-play" style="display: none;" data-bind="css:{'audio-play':$data.id() != $root.playingQuestionId(),'audio-pause':$data.id() == $root.playingQuestionId()},visible:$data.listenUrls && $data.listenUrls() && $data.listenUrls().length > 0,click:$root.playAudio.bind($data,$root,$element)"></i>
                        </div>
                        <div class="segment" data-bind="attr:{id:'subjective_'+$data.id()+$index()}">题目加载中...</div>
                        <div style="display: none;" data-bind="text:$root.loadQuestionContent($data,$index())"></div>
                        <div class="r-playBtn">
                            <!--ko if: !$data.questionChecked()-->
                            <span class="btn-option" style="cursor: pointer;" data-bind="click:$root.addQuestion.bind($data,$root,'normal')"><i></i>选入</span>
                            <!--/ko-->
                            <!--ko if: $data.questionChecked()-->
                            <span class="btn-option remove" style="cursor: pointer;" data-bind="click:$root.addQuestion.bind($data,$root,'normal')"><i></i>移除</span>
                            <!--/ko-->
                        </div>
                    </div>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko if: $root.currentQuestions().length > 3 && $root.currentQuestions().length > $root.showCount() -->
                    <div class="t-dynamic-btn" data-bind="click:$root.showMoreQuestions">
                        <a class="more" href="javascript:void(0);">展开更多</a>
                    </div>
                    <!--/ko-->
                    <!--ko if: $root.currentQuestions().length == 0-->
                    <div style="margin: 200px;text-align: center;font-size: 16px;">没有满足条件的段落</div>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
</script>