<script id="t:WORD_RECOGNITION_AND_READING" type="text/html">
    <div class="md-course">
        <!--ko if: $root.packageList().length > 0-->
        <!--ko foreach: {data : $root.packageList(),as:'package'}-->
        <div class="course-box">
            <p class="course-title" data-bind="text:$data.lessonName"></p>
            <div class="course-list">
                <div class="course-list-item">
                    <div class="list-item-desc" data-bind="click:$root.packageDetail.bind($data,$root)">
                        <p class="item-title">生字认读</p>
                        <p class="item-txt" data-bind="text:'共'+ package.questionNum() + '个生字'"></p>
                        <div class="adjust-posit">
                            <div class="mask-box"></div>
                            <p>预览生字</p>
                        </div>
                    </div>
                    <div class="list-item-state" data-bind="css:{'current':$data.packageChecked()},click:$root.addPackage.bind($data,$root)">
                        <i></i><span data-bind="text: !$data.packageChecked()? '选入' : '移除' "></span>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
        <!--ko if:$root.packageList().length == 0-->
        <div class="h-set-homework current">
            <div class="seth-mn">
                <div class="testPaper-info">
                    <div class="inner" style="padding: 15px 10px; text-align: center;">
                        <p>本课没有生字认读的内容，您可以布置其他形式</p>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>

<script id="t:viewWordRecognitionDetail" type="text/html">
    <div class="aDetails-popup">
        <div class="r-inner showDetails">
            <div class="readTxt-list select-box">
                <div class="fl-aside" style="float:left;">
                    <p class="name" data-bind="text:$root.lessonName">&nbsp;</p>
                    <p class="textGray">共<!--ko text:$root.currentQuestions().length--><!--/ko-->个生字：跟读、音节、笔顺、部首、结构、字义、组词</p>
                </div>
                <div class="all-sel" data-bind="css:{'remove':$root.packageChecked()},text: !$root.packageChecked() ? '全部选入' : '全部移除',click:selAllCurrentQuestions">
                    全部选入
                </div>
            </div>
            <div class="readTxt-scrol" style="height: 350px;" data-tip="350像素解决平板电脑的问题">
                <div class="readTxt-details" id="tabContentQuestion">
                    <!--ko if: $root.currentQuestions().length > 0-->
                    <!--ko foreach: {data : $root.currentQuestions(),as:'question'}-->
                    <div class="readInner" style="display: block;">
                        <i class="itag" style="display: none;"></i>
                        <div class="r-sub-title subtitle" style="display: none;">
                            <i class="label audio-play" style="display: none;" data-bind="css:{'audio-play':$data.id() != $root.playingQuestionId(),'audio-pause':$data.id() == $root.playingQuestionId()},visible:$data.listenUrls && $data.listenUrls() && $data.listenUrls().length > 0"></i>
                        </div>
                        <div class="segment" style="border: 0;" >
                            <word-recognition-and-reading params="question:$root.getQuestion($data.id()),playing:$data.id() == $root.playingQuestionId(),playAudio:$root.playAudio.bind($root)"></word-recognition-and-reading>
                        </div>
                    </div>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko if: $root.currentQuestions().length == 0-->
                    <div style="margin: 200px;text-align: center;font-size: 16px;">没有满足条件的段落</div>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
</script>

<#include "chinese/recognitionreadingquestion.ftl">