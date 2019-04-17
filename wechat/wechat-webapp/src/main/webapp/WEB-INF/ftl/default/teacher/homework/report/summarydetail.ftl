<div data-bind="visible: $root.selectedTabName() =='homework'" style="display: none;">
    <!-- ko foreach : {data : $data.studentHomeworkDetail().typeReportList, as : '_report'} -->
        <!--ko if: _report.type == 'UNIT_QUIZ'-->
        <div class="mjd-content">
            <div class="hl-head">
                <a href="javascript:void(0);" class="hl-right" data-bind="click: $root.detailBtn.bind($data,_report.type),visible: _report.finishCount > 0">查看答题详情 ></a>
                <p data-bind="text: _report.typeName">--</p>
            </div>
            <div data-bind="visible: _report.finishCount > 0">
                <div class="hl-column">
                    <ol>
                        <li>
                            <div><span class="textRed" data-bind="text:_report.finishCount">--</span>/<span data-bind="text: $root.studentHomeworkDetail().userCount"></span></div>
                            <div>已完成</div>
                        </li>
                        <li>
                            <div><span data-bind="text:_report.avgScore">--</span></div>
                            <div>班平均分</div>
                        </li>
                        <li>
                            <div data-bind="text: _report.avgDuration +'分钟'">--</div>
                            <div>平均时长</div>
                        </li>
                    </ol>
                </div>
                <div class="hl-graph">
                    <ul>
                        <!-- ko foreach : {data : _report.questionsInfo , as : '_question'} -->
                        <li>
                            <div class="radial radial-green" data-bind="attr : {id : 'radial_' + _report.type +_question.questionId}, css: {'radial-red' : _question.proportion <= 60}">
                                <div class="text">
                                    <span data-bind="text: _question.proportion+'%'">--</span>
                                    <span>正确率</span>
                                </div>
                            </div>
                            <p data-bind="text: '第'+ _question.position+'题'"></p>
                            <p data-bind="text: $root.setRadialIndicator(_question.questionId,_question.proportion, (_question.proportion <= 60 ? 'red': 'green'),_report.type)"></p>
                        </li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
            <div data-bind="visible: _report.finishCount == 0" style="padding: 2rem 1rem;">该作业类型还没有学生完成哦！</div>
        </div>
        <!--/ko-->

        <!--ko if: _report.type == 'PHOTO_OBJECTIVE' ||  _report.type == 'VOICE_OBJECTIVE' -->
            <div class="mjd-content">
                <div class="hl-head">
                    <a href="javascript:void(0);" class="hl-right" data-bind="click: $root.detailBtn.bind($data,_report.type),visible: _report.finishCount > 0">查看答题详情 ></a>
                    <p data-bind="text: _report.typeName">--</p>
                </div>
                <div class="hl-column" data-bind="visible: _report.finishCount > 0">
                    <ol>
                        <li>
                            <div><span class="textRed" data-bind="text: _report.unCorrectCount"></span></div>
                            <div>待批改</div>
                        </li>
                        <li>
                            <div><span class="textRed" data-bind="text: _report.finishCount">--</span>/<span data-bind="text: $root.studentHomeworkDetail().userCount"></span></div>
                            <div>已完成</div>
                        </li>
                    </ol>
                </div>
                <div data-bind="visible: _report.finishCount == 0" style="padding: 2rem 1rem;">该作业类型还没有学生完成哦！</div>
            </div>
        <!--/ko-->

        <!--ko if: _report.type == 'EXAM' || _report.type == 'LISTEN_PRACTICE' || _report.type == 'INTELLIGENCE_EXAM' || _report.type == 'ORAL_PRACTICE'-->
        <div class="mjd-content">
            <div class="hl-head">
                <a href="javascript:void(0);" class="hl-right" data-bind="click: $root.detailBtn.bind($data,_report.type),visible: _report.finishCount > 0">查看答题详情 ></a>
                <p data-bind="text: _report.typeName">--</p>
            </div>
            <div class="hl-column" data-bind="visible: _report.finishCount > 0">
                <ol>
                    <li>
                        <div><span class="textRed" data-bind="text: _report.finishCount">--</span>/<span data-bind="text: $root.studentHomeworkDetail().userCount">--</span></div>
                        <div>已完成</div>
                    </li>
                    <li>
                        <div><span data-bind="text: _report.avgScore">--</span></div>
                        <div>班平均分</div>
                    </li>
                    <li>
                        <div data-bind="text: _report.avgDuration+'分钟'">--</div>
                        <div>平均时长</div>
                    </li>
                </ol>
            </div>
            <div class="hl-graph" data-bind="visible: _report.finishCount > 0">
                <ul>
                    <!-- ko foreach : {data : _report.questionsInfo , as : '_question'} -->
                    <li>
                        <div class="radial radial-green" data-bind="attr : {id : 'radial_'+_report.type+_question.questionId}, css: {'radial-red' : _question.proportion <= 60}">
                            <div class="text">
                                <span data-bind="text: _report.type == 'ORAL_PRACTICE' ? _question.proportion : _question.proportion+'%'">--</span>
                                <span data-bind="text: _report.type == 'ORAL_PRACTICE' ? '平均分' : '正确率'">&nbsp;</span>
                            </div>
                        </div>
                        <p data-bind="text: '第'+ _question.position+'题'"></p>
                        <p data-bind="text: $root.setRadialIndicator(_question.questionId,_question.proportion, (_question.proportion <= 60 ? 'red': 'green'),_report.type)"></p>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
            <div data-bind="visible: _report.finishCount == 0" style="padding: 2rem 1rem;">该作业类型还没有学生完成哦！</div>
        </div>
        <!--/ko-->

        <!--ko if: _report.type == 'MENTAL'-->
        <div class="mjd-content">
            <div class="hl-head">
                <a href="javascript:void(0);" class="hl-right" data-bind="click: $root.detailBtn.bind($data,_report.type),visible: _report.finishCount > 0">查看答题详情 ></a>
                <p data-bind="text: _report.typeName">--</p>
            </div>
            <div class="hl-column" data-bind="visible: _report.finishCount > 0">
                <ol>
                    <li>
                        <div><span class="textRed" data-bind="text: _report.finishCount">--</span>/<span data-bind="text: $root.studentHomeworkDetail().userCount">--</span></div>
                        <div>已完成</div>
                    </li>
                    <li>
                        <div><span data-bind="text: _report.avgScore">--</span></div>
                        <div>班平均分</div>
                    </li>
                    <li>
                        <div data-bind="text: _report.avgDuration+'分钟'">--</div>
                        <div>平均时长</div>
                    </li>
                </ol>
            </div>
            <div class="hl-table" data-bind="visible: _report.finishCount > 0">
                <table>
                    <thead>
                    <tr>
                        <td>知识点</td>
                        <td>正确率</td>
                    </tr>
                    </thead>
                    <tbody>
                    <!-- ko foreach : {data : _report.knowledgePointData , as : '_kl'} -->
                    <tr>
                        <td class="describle"><span data-bind="text: _kl.name +'('+ _kl.questionNum +')' + '道'">--</span></td>
                        <td data-bind="text: _kl.percentage+'%'">--</td>
                    </tr>
                    <!--/ko-->
                    </tbody>
                </table>
            </div>

            <div data-bind="visible: _report.finishCount == 0" style="padding: 2rem 1rem;">该作业类型还没有学生完成哦！</div>
        </div>
        <!--/ko-->

        <!--ko if: _report.type == 'BASIC_APP'-->
        <div class="mjd-content">
            <div class="hl-head">
                <a href="javascript:void(0);" class="hl-right" data-bind="click: $root.detailBtn.bind($data,_report.type), visible: _report.finishCount > 0">查看答题详情 ></a>
                <p data-bind="text: _report.typeName">--</p>
            </div>
            <div class="hl-column" data-bind="visible: _report.finishCount > 0">
                <ol>
                    <li>
                        <div><span class="textRed" data-bind="text: _report.finishCount">--</span>/<span data-bind="text: $root.studentHomeworkDetail().userCount">--</span></div>
                        <div>已完成</div>
                    </li>
                    <li>
                        <div><span data-bind="text: _report.avgScore">--</span></div>
                        <div>班平均分</div>
                    </li>
                    <li>
                        <div data-bind="text: _report.avgDuration+'分钟'">--</div>
                        <div>平均时长</div>
                    </li>
                </ol>
            </div>
            <div data-bind="visible: _report.finishCount == 0" style="padding: 2rem 1rem;">该作业类型还没有学生完成哦！</div>
        </div>
        <!--/ko-->

        <!--ko if: _report.type == 'READING' || _report.type == 'WORD_PRACTICE' || _report.type == 'READ_RECITE'-->
        <div class="mjd-content">
            <div class="hl-head">
                <a href="javascript:void(0);" class="hl-right" data-bind="click: $root.detailBtn.bind($data,_report.type), visible: _report.finishCount > 0">查看答题详情 ></a>
                <p data-bind="text: _report.typeName">--</p>
            </div>
            <div class="hl-column" data-bind="visible: _report.finishCount > 0">
                <ol>
                    <li>
                        <div><span class="textRed" data-bind="text: _report.finishCount">--</span>/<span data-bind="text: $root.studentHomeworkDetail().userCount">--</span></div>
                        <div>已完成</div>
                    </li>
                    <li>
                        <div><span data-bind="text: _report.avgScore">--</span></div>
                        <div>班平均分</div>
                    </li>
                    <li>
                        <div data-bind="text: _report.avgDuration+'分钟'">--</div>
                        <div>平均时长</div>
                    </li>
                </ol>
            </div>
            <div data-bind="visible: _report.finishCount == 0" style="padding: 2rem 1rem;">该作业类型还没有学生完成哦！</div>
        </div>
        <!--/ko-->

        <!--ko if: _report.type == 'KEY_POINTS'-->
            <div class="mjd-content">
                <div class="hl-head">
                    <p data-bind="text:_report.typeName">重难点视频解析</p>
                </div>
                <div style="padding: 2rem 1rem;">此作业类型暂不支持在微信中查看，请下载老师APP查看</div>
            </div>
        <!--/ko-->


        <!--ko if: ["UNIT_QUIZ","VOICE_OBJECTIVE","PHOTO_OBJECTIVE","ORAL_PRACTICE","INTELLIGENCE_EXAM","LISTEN_PRACTICE","EXAM","MENTAL","BASIC_APP","READ_RECITE","WORD_PRACTICE","READING","KEY_POINTS"].indexOf(_report.type) == -1 -->
        <div class="mjd-content">
            <div class="hl-head">
                <p data-bind="text:_report.typeName">&nbsp;</p>
            </div>
            <div style="padding: 2rem 1rem;">此作业类型暂不支持在微信中查看，请<a href="http://wx.17zuoye.com/download/17teacherapp?cid=302014">下载老师APP</a>查看</div>
        </div>
        <!--/ko-->

    <!--/ko-->
</div>