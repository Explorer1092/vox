<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="作业报告" pageJs="reportDetail">
    <@sugar.capsule css=['homework','jbox'] />

    <div class="mhw-header mar-b14">
        <div class="header-inner">
            <div class="fl">
                <!--ko text: clazzName--><!--/ko-->
                <!--ko text: createAt--><!--/ko-->
                作业报告
            </div>
        </div>
    </div>
    <div class="mhw-main">
        <div class="mhw-jobDetails">
            <div class="mjd-tips" data-bind="visible: $data.studentHomeworkDetail().success, click: includeSubjectiveFlag() ? gotoCheckHomeworkBtn : quickRemarksBtn.bind($data,'top')" style="display: none;">
                <!--ko if: includeSubjectiveFlag-->
                    已提交作业中还有需要您手动批改 的照片／录音作业，请您批改
                <!--/ko-->

                <!--ko ifnot: includeSubjectiveFlag-->
                    有<!--ko text: finishedCount--><!--/ko-->名学生完成作业，写评语鼓励下吧
                <!--/ko-->

                <!--ko if: includeSubjectiveFlag-->
                    去批改>
                <!--/ko-->
                <!--ko ifnot: includeSubjectiveFlag-->
                    写评语>
                <!--/ko-->
            </div>

            <div class="mjd-content" data-bind="visible: $data.studentHomeworkDetail().success" style="display: none;">
                <div class="hl-head">
                    <p>
                        完成情况
                    </p>
                </div>
                <div class="hl-column">
                    <ol>
                        <li>
                            <div><span class="textRed" data-bind="text: $data.studentHomeworkDetail().finishCount">--</span>/<span data-bind="text: $data.studentHomeworkDetail().userCount">--</span></div>
                            <div>完成作业</div>
                        </li>
                        <li>
                            <div><span data-bind="text: $data.studentHomeworkDetail().avgScore">--</span></div>
                            <div>班平均分</div>
                        </li>
                    </ol>
                </div>
            </div>

            <#--录音推荐-->
            <div class="mjd-content" data-bind="visible: hasRecommend && voiceRecommendListDetail().length > 0,if: hasRecommend && voiceRecommendListDetail().length > 0" style="display: none;">
                <div class="hl-head">
                    <a href="javascript:void (0);" data-bind="attr: {'href' : '/teacher/homework/report/voicerecommend.vpage?homeworkId='+$root.homeworkId},visible: !hasRecommended()" class="hl-right">更多录音 ></a>
                    <p>录音推荐</p>
                </div>
                <div class="hl-record">
                    <table>
                        <thead>
                        <tr>
                            <td>姓名</td>
                            <td>作业</td>
                            <td>录音</td>
                            <!--ko if: !$root.hasRecommended()-->
                            <td>得分</td>
                            <!--/ko-->
                        </tr>
                        </thead>
                        <tbody>
                        <!-- ko foreach : {data : voiceRecommendListDetail, as : '_list'} -->
                        <tr>
                            <td><span class="name" data-bind="text: _list.studentName()">--</span></td>
                            <td><span class="type" data-bind="text: _list.categoryName()">--</span></td>
                            <td>
                                <span class="record-btn" data-bind="css: {'record-stop': _list.isPlay()},click: $root.voicePlayOrStopBtn">
                                    <i class="icon"></i>点击<!--ko if: _list.isPlay()-->停止<!--/ko--><!--ko ifnot: _list.isPlay()-->播放<!--/ko-->
                                </span>
                            </td>
                            <!--ko if: !$root.hasRecommended()-->
                            <td><span data-bind="text: _list.score()">--</span></td>
                            <!--/ko-->
                        </tr>
                        <!--/ko-->
                        </tbody>
                    </table>

                    <div class="tips tips-blue" data-bind="visible: !hasRecommended()" style="display: none;">
                        <span data-bind="click: $root.voiceRecommendToParentBtn" style="display: inline-block; width: 100%">
                            <i class="zan-icon"></i>推荐给家长
                        </span>
                        <div class="tips-inner" data-bind="visible: $root.voiceRecommendShowTip() && requestParentCount() > 0">
                            有<!--ko text: requestParentCount()--><!--/ko-->位家长希望您推荐录音
                            <span class="close" data-bind="click: function(){$root.voiceRecommendShowTip(false)}">×</span>
                        </div>
                    </div>
                    <div data-bind="visible: hasRecommended()" style="display: none" class="tips">已向家长推荐以上录音</div>
                </div>
            </div>
            <div id="jplayerId"></div>


            <div class="mjd-tab" data-bind="visible:$root.tabList().length > 0" style="display: none;">
                <ul class="tab">
                    <!-- ko foreach: {data: $root.tabList,as : '_tab'}-->
                    <li data-bind="text: _tab.name(), css: {'active' : _tab.checked()},click: $root.tabClick"></li>
                    <!--/ko-->
                </ul>
            </div>

            <#--学生完成情况-->
            <div class="mjd-detailsList" data-bind="visible: $root.selectedTabName() =='student' && $root.studentHomeworkDetail().success" style="display: none;">
                <div class="List-left">
                    <table class="table-box" cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>姓名</td>
                            <td>平均分</td>
                        </tr>
                        </thead>
                        <tbody>
                        <!--ko foreach : {data : $data.studentHomeworkDetail().studentReportList, as : '_stu'}-->
                        <tr>
                            <td>
                                <span class="name">
                                    <i class="label-icon label-icon-yellow" data-bind="visible:_stu.repair" style="display: none;">补</i>
                                    <i class="label-icon label-icon-red" data-bind="visible:_stu.finishAt == null" style="display: none;">未</i>
                                    <span data-bind="text: _stu.userName || _stu.userId"></span>
                                </span>
                            </td>
                            <td data-bind="text: _stu.avgScore == null ? '--' : _stu.avgScore"></td>

                        </tr>
                        <!--/ko-->
                        </tbody>
                    </table>
                </div>
                <!--table右侧内容-->
                <div class="List-right">
                    <table class="table-box" cellpadding="0" cellspacing="0" data-bind="style:{width: $data.objectiveConfigTypesLength() > 2 ? $data.objectiveConfigTypesLength() * 7.5 + 18 +'rem' : '100%'}"><!--作业内容的length*5.5rem+18rem-->
                        <thead>

                        <tr>
                            <td data-bind="attr: {colspan : $data.objectiveConfigTypesLength()}">作业内容</td><!--colspan的值为作业内容的length-->
                            <td rowspan="2" class="time">完成时间</td>
                            <td rowspan="2" class="time">完成用时</td>
                        </tr>
                        <tr>
                            <!--ko foreach : {data : $data.studentHomeworkDetail().objectiveConfigTypes, as : '_detail'}-->
                            <td><span data-bind="text: _detail">--</span></td>
                            <!--/ko-->
                        </tr>
                        </thead>
                        <tbody>
                        <!--ko foreach : {data : $data.studentHomeworkDetail().studentReportList, as : '_stu'}-->
                        <tr>
                            <!--ko foreach : {data : _stu.typeInfos, as : '_info'}-->
                            <td><span class="text-info mhw-txtOverflow" data-bind="text:_info != null ? _info : '未完成'"></span></td>
                            <!--/ko-->
                            <td class="time"><span data-bind="text: _stu.finishAt || '未完成'"></span></td>
                            <td class="time"><span data-bind="text:_stu.finishAt == null ? '--' : _stu.duration + '分钟'"></span></td>
                        </tr>
                        <!--/ko-->
                        </tbody>
                    </table>
                </div>
            </div>

            <#include "summarydetail.ftl">

        </div>
    </div>
    <div class="footer-empty" style="height: 7rem;">
        <div class="mhw-btns btns-2 fixFooter" style="padding: 1.5rem 0.7rem;">
            <a data-bind="click: $root.quickRemarksBtn.bind($data,'bottom')" href="javascript:void (0);" class="w-btn w-btn-lightBlue">一键写评语</a>
            <a data-bind="click: $root.quickAwardsBtn.bind($data,'${clazzId!''}')" href="javascript:void (0);" class="w-btn">一键发奖励</a>
        </div>
    </div>
</@homeworkReport.page>