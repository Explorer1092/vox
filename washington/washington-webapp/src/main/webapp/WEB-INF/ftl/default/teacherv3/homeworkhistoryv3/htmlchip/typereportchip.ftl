<script type="text/html" id="t:typeListTemplate">
    <!--ko template:{name:sReport.displayModel.bind(sReport),foreach:sReport.typeReportList,as:'typeReport'}--><!--/ko-->
</script>

<script id="t:NORMAL_TEXT" type="text/html">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:$data.finishCount && $data.finishCount > 0-->
            <a href="javascript:void(0);" data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:$data.finishCount && $data.finishCount > 0,visible:!($data.finishCount && $data.finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <div class="numCollect"
                 data-bind="if:$data.finishCount && $data.finishCount > 0,visible:$data.finishCount && $data.finishCount > 0">
                <ul>
                    <li><!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko--><span
                            class="text">已完成</span></li>
                    <li><span data-bind="text:avgScore">0</span><span class="text">/班平均分</span></li>
                    <li><span data-bind="text:avgDuration">0</span><span class="text">分钟/平均时长</span></li>
                </ul>
            </div>
        </div>
    </div>
</script>
<script id="t:BASIC_APP_PIC" type="text/html">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a href="javascript:void(0);" data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <div class="numCollect"
                 data-bind="if:finishCount && finishCount > 0,visible:finishCount && finishCount > 0">
                <ul>
                    <li><!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko--><span
                            class="text">已完成</span></li>
                    <li><span data-bind="text:avgScore">0</span><span class="text">/班平均分</span></li>
                    <li><span data-bind="text:avgDuration">0</span><span class="text">分钟/平均时长</span></li>
                </ul>
            </div>
            <div class="b-tablePractice" data-bind="if:typeReport.baseAppInformation && typeReport.baseAppInformation.length > 0,visible:typeReport.baseAppInformation && typeReport.baseAppInformation.length > 0">
                <table cellpadding="0" cellspacing="0" class="tableBorder">
                    <thead>
                    <tr>
                        <td width="110" class="unitName" colspan="2">课时</td>
                        <td>练习</td>
                        <td>平均分</td>
                    </tr>
                    </thead>
                    <tbody>
                    <!--ko foreach:{data:baseAppInformation,as:'unit'}-->
                    <!--ko foreach:{data:unit.lessons,as:'lesson'}-->
                    <!--ko foreach:{data:lesson.categories,as:'category'}-->
                    <tr data-bind="css:{'odd' : $parentContext.$parentContext.$index() % 2 == 0}">
                        <!--ko if:$parentContext.$index() == 0 && $index() == 0-->
                        <td data-bind="attr:{rowspan:$parents[3].rowspanCount(unit.lessons)}" class="unitName">
                            <span data-bind="text:unit.unitName">&nbsp;</span></td>
                        <!--/ko-->
                        <!--ko if:$index() == 0-->
                        <td class="point h-textBlue" data-bind="attr:{rowspan:lesson.categories.length}">
                            <span class="text" data-bind="text:lesson.lessonName">&nbsp;</span></td>
                        <!--/ko-->
                        <td class="h-textBlue" data-bind="text:category.categoryName">&nbsp;</td>
                        <td data-bind="text:category.averageScore != null ? category.averageScore : ''"></td>
                    </tr>
                    <!--/ko-->
                    <!--/ko-->
                    <!--/ko-->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>


<script id="t:EXAM_PIC" type="text/html">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <!--ko if:finishCount && finishCount > 0-->
            <div class="numCollect">
                <ul>
                    <li><!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko--><span
                            class="text">已完成</span></li>
                    <li><span data-bind="text:avgScore">0</span><span class="text">/班平均分</span></li>
                    <li><span data-bind="text:avgDuration">0</span><span class="text">分钟/平均时长</span></li>
                </ul>
            </div>
            <div class="errorCollect"
                 data-bind="if:questionsInfo && questionsInfo.length > 0,visible:questionsInfo && questionsInfo.length > 0">
                <ul class="errorList">
                    <li class="header">
                        <div class="li-cell li-cell01">题号</div>
                        <div class="li-cell li-cell02" data-bind="text:type=='ORAL_PRACTICE'?'得分':'正确率'"></div>
                        <div class="li-cell li-cell03">操作</div>
                    </li>
                    <!--ko foreach:{data:questionsInfo,as:'question'}-->
                    <li data-bind="css:{'odd' : $index() % 2 == 0}">
                        <div class="li-cell li-cell01" data-bind="text:'第' + ($index() + 1) + '题'">&nbsp;</div>
                        <div class="li-cell li-cell02">
                            <div class="proBox">
                                <div class="proCurrent"
                                     data-bind="css:{'proCurrent-red' : proportion < 60},style:{width:(proportion > 90 && proportion < 100) ? '90%' : proportion + '%'}"></div>
                                <!--ko ifnot:$parent.type=="ORAL_PRACTICE"-->
                                <span data-bind="text:proportion == 100 ? '全部正确' : proportion + '%',css:{'allCorrect' : proportion == 100}">&nbsp;</span>
                                <!--/ko-->
                                <!--ko if:$parent.type=="ORAL_PRACTICE"-->
                                <span data-bind="text:proportion,css:{'allCorrect' : proportion == 100}">&nbsp;</span>
                                <!--/ko-->
                            </div>
                        </div>
                        <div class="li-cell li-cell03">
                            <a href="javascript:void(0)"
                               data-bind="click:$parents[1].previewQuestion.bind($data,$parent.type,$parent.typeName,$parents[1]),text:'详情'">
                                &nbsp;</a>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
            <!--/ko-->
        </div>
    </div>
</script>
<script type="text/html" id="t:MENTAL">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <div class="numCollect" data-bind="if:finishCount && finishCount > 0,visible:finishCount && finishCount > 0">
                <ul>
                    <li><!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko--><span
                            class="text">已完成</span></li>
                    <li><span data-bind="text:avgScore">0</span><span class="text">/班平均分</span></li>
                    <li><span data-bind="text:avgDuration">0</span><span class="text">分钟/平均时长</span></li>
                </ul>
            </div>
        </div>
        <div class="b-tableOral" data-bind="if:typeReport.calculationStudents && typeReport.calculationStudents.length > 0,visible:typeReport.calculationStudents && typeReport.calculationStudents.length > 0">
            <h3 class="tableState">口算班级榜单（又快又准的前<!--ko text:typeReport.calculationStudents.length--><!--/ko-->名）</h3>
            <table cellpadding="0" cellspacing="0">
                <thead>
                <tr>
                    <td>学生姓名</td>
                    <td>得分</td>
                    <td>时间</td>
                </tr>
                </thead>
                <tbody data-bind="foreach:{data: typeReport.calculationStudents,as:'student'}">
                <tr data-bind="css:{'even':$index()%2 == 0}">
                    <td data-bind="text:student.userName">&nbsp;</td>
                    <td data-bind="text:student.score + '分'">&nbsp;</td>
                    <td data-bind="text:student.durationStr">&nbsp;</td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="sl-info" data-bind="if:finishCount && finishCount > 0,visible:finishCount && finishCount > 0">
            <ul class="list">
                <!--ko foreach:{data:knowledgePointData,as:'point'}-->
                <li class="odd" data-bind="css:{'odd' : $index() % 2 == 0}">
                    <div class="left" data-bind="text: point.name + '（' + point.questionNum + '）道 '">&nbsp;</div>
                    <div class="right" data-bind="text:'正确率' + point.percentage + '%'"></div>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
</script>
<script type="text/html" id="t:SUBJECTIVE">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <!--ko if:finishCount && finishCount > 0-->
            <div class="numCollect">
                <ul>
                    <li>
                        <span class="yellow" data-bind="text:unCorrectCount">0</span><span class="text">人/待批改</span>
                    </li>
                    <li>
                        <!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko-->
                        <span class="text">已完成</span>
                    </li>
                </ul>
            </div>
            <!--/ko-->
        </div>
    </div>
</script>
<script type="text/html" id="t:DUBBING">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <!--ko if:finishCount && finishCount > 0-->
            <div class="numCollect">
                <ul>
                    <li><!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko--><span
                            class="text">已完成</span></li>
                    <li data-bind="if:type=='DUBBING_WITH_SCORE',visible:type=='DUBBING_WITH_SCORE'"><span data-bind="text:avgScore">0</span><span class="text">/班平均分</span></li>
                    <li><span data-bind="text:avgDuration">0</span><span class="text">分钟/平均时长</span></li>
                </ul>
            </div>
            <!--/ko-->
        </div>
    </div>
</script>
<script id="t:NON_SUPPORT_TYPE" type="text/html">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'"></h2>
            <a href="javascript:void(0);" style="display: none;" data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
        </div>
        <div class="sl-info">
            <div class="numCollect">
                <ul>
                    <li>暂不支持此类型展示<span class="text"></span></li>
                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="t:READ_RECITE_WITH_SCORE">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <!--ko if:finishCount && finishCount > 0-->
            <div class="numCollect" style="border-bottom: 0px;">
                <ul>
                    <li>
                        <!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko-->
                        <span class="text">已完成</span>
                    </li>
                </ul>
            </div>
            <div style="border-bottom: 1px solid #dfdfdf;" data-bind="if:readReciteAppInfos && readReciteAppInfos.length > 0,visible:readReciteAppInfos && readReciteAppInfos.length > 0">
                <ul class="report-tab">
                    <li class="thead">
                        <div class="each">形式</div>
                        <div class="each">课文</div>
                        <div class="each">达标比例</div>
                    </li>
                    <!--ko foreach:{data:readReciteAppInfos,as:'app'}-->
                    <li>
                        <div class="each" data-bind="text:app.questionBoxTypeName ? app.questionBoxTypeName : '??'">&nbsp;</div>
                        <div class="each" data-bind="text:app.lessonName">&nbsp;</div>
                        <div class="each" data-bind="text:app.standardNum + '/' + $parent.finishCount">&nbsp;</div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>

            <!--/ko-->
        </div>
    </div>
</script>

<script id="t:OCR_MENTAL_ARITHMETIC" type="text/html">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <div class="numCollect"
                 data-bind="if:finishCount && finishCount > 0,visible:finishCount && finishCount > 0">
                <ul>
                    <li><!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko--><span
                            class="text">已完成</span></li>
                    <li><span data-bind="text:avgScore">0</span><span class="text">/班平均分</span></li>
                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="t:WORD_RECOGNITION_AND_READING">
    <div class="summary-list">
        <div class="sl-title">
            <h2 data-bind="text:typeName ? typeName : '未知'">&nbsp;</h2>
            <!--ko if:finishCount && finishCount > 0-->
            <a data-bind="click:$parent.viewReportDetail.bind($data,$parent)">查看答题详情</a>
            <!--/ko-->
        </div>
        <div class="sl-info">
            <div class="tips"
                 data-bind="ifnot:finishCount && finishCount > 0,visible:!(finishCount && finishCount > 0)">
                该作业类型还没有学生完成哦！
            </div>
            <!--ko if:finishCount && finishCount > 0-->
            <div class="numCollect" style="border-bottom: 0px;">
                <ul>
                    <li>
                        <!--ko text:finishCount--><!--/ko-->/<!--ko text:$parent.userCount--><!--/ko-->
                        <span class="text">已完成</span>
                    </li>
                </ul>
            </div>
            <div style="border-bottom: 1px solid #dfdfdf;" data-bind="if:wordAppInfos && wordAppInfos.length > 0,visible:wordAppInfos && wordAppInfos.length > 0">
                <ul class="report-tab">
                    <li class="thead">
                        <div class="each" style="width: 49.8%;">课文</div>
                        <div class="each" style="width: 49.8%;">达标比例</div>
                    </li>
                    <!--ko foreach:{data:wordAppInfos,as:'app'}-->
                    <li>
                        <div class="each" style="width: 49.8%;" data-bind="text:app.lessonName">&nbsp;</div>
                        <div class="each" style="width: 49.8%;" data-bind="text:app.standardNum + '/' + $parent.finishCount">&nbsp;</div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>

            <!--/ko-->
        </div>
    </div>
</script>