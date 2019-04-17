<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="作业报告" pageJs="basicAppDetail">
    <@sugar.capsule css=['homework','jbox'] />
    <div class="mhw-header mar-b14" data-bind="visible: $data.basicAppDetail().categoryName">
        <div class="header-inner">
            <div class="fl" data-bind="text: $data.basicAppDetail().categoryName+'详情'">详情</div>
            <a data-bind="visible:$data.basicAppDetail().needRecord" href="/teacher/homework/report/scorerule.vpage" style="display:none;float: right; font-size: 1rem; color: #189cfb;" >分数计算规则</a>
        </div>
    </div>

    <!-- ko foreach : {data : $data.basicAppDetail().questionInfoMapper, as : '_map'} -->
    <div class="details-box" style="display: none;" data-bind="visible: $root.basicAppDetail().questionInfoMapper">
        <div class="ex-header blue">
            <div class="ex-fLeft">
                <!-- ko foreach : {data : _map.sentences, as : '_sentences'} -->
                <span data-bind="text: _sentences.sentenceContent">--</span>
                <!--/ko-->
            </div>
        </div>
        <!--ko if: _map.needRecord && _map.recordInfo && _map.recordInfo.length > 0-->
        <div class="d-container">
            <ul class="d-list">
                <!-- ko foreach : {data : _map.recordInfo, as : '_recordInfo'} -->
                <li>
                    <p class="name" data-bind="text: _recordInfo.userName">--</p>
                    <!--ko ifnot:_recordInfo.voiceScoringMode == "Normal"-->
                    <div class="play listening">
                        <i class="d-gradeIcon" data-bind="text: _recordInfo.score, css:{'iconGreen' : _recordInfo.score <= 60}">80</i>
                    </div>
                    <!--/ko-->
                    <!--ko if:_recordInfo.voiceScoringMode == "Normal"-->
                    <div class="play playAudioBtn" data-bind="attr:{'data-audio_src':_recordInfo.userVoiceUrl}">
                        <i class="d-gradeIcon" data-bind="text: _recordInfo.score, css:{'iconGreen' : _recordInfo.score <= 60}">80</i>
                    </div>
                    <!--/ko-->
                </li>
                <!--/ko-->
            </ul>
        </div>
        <!--/ko-->

        <!--ko if: !_map.needRecord-->
        <div class="d-container d-answer">
            <div class="d-title">学生答案</div>
            <ul class="ex-side">
                <li data-bind="visible: _map.answerErrorInfo.length > 0">
                    <div class="ex-left">
                        <span>错误</span>
                    </div>
                    <div class="ex-right" data-bind="click: $root.showStudentsBtn.bind($data,'error')">
                        <span data-bind="text: _map.answerErrorInfo.length +'人'+ $root.errorOrRightPercent(_map.answerErrorInfo.length,_map.answerErrorInfo.length + _map.answerRightInfo.length)">--</span>
                        <a class="ex-point" href="javascript:void(0);"></a>
                    </div>
                </li>
                <li data-bind="visible: _map.answerRightInfo.length > 0">
                    <div class="ex-left">
                        <span class="des-cor">正确</span>
                    </div>
                    <div class="ex-right" data-bind="click: $root.showStudentsBtn.bind($data,'right')">
                        <span data-bind="text: _map.answerRightInfo.length +'人' + $root.errorOrRightPercent(_map.answerRightInfo.length,_map.answerErrorInfo.length + _map.answerRightInfo.length)">--</span>
                        <a class="ex-point" href="javascript:void(0);"></a>
                    </div>
                </li>
            </ul>
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
    <#--声音-->
    <div id="jplayerId"></div>

    <div class="mhw-slideBox" data-bind="visible: $root.showStudentsListBox" style="display: none;">
        <div class="mask"></div>
        <div class="innerBox">
            <div class="hd">学生名单<span class="close" data-bind="click: function(){$root.showStudentsListBox(false)}">×</span>
            </div>
            <div class="mn mhw-slideOverflow">
                <ul class="infoSubject">
                    <!-- ko foreach : {data : $root.studentsList(), as : '_stu'} -->
                        <li data-bind="text:_stu"></li>
                    <!--/ko-->

                </ul>
            </div>
        </div>
    </div>
</@homeworkReport.page>