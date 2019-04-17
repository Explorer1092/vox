<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="作业报告" pageJs="history">
    <@sugar.capsule css=['homework','jbox'] />
    <div class="mhw-taskPreview">
        <div class="mhw-header mar-b14">
            <div class="header-inner">
                <h2 class="title" data-bind="text: $root.getSubjectValue($root.subject())+'作业报告'" style="overflow: hidden;"></h2>
                <div class="switch-box slideUp" data-bind="visible: $root.subjectList().length > 1 && $data.reportDetail()" style="display: none;"><!--通过slideUp切换箭头-->
                    <div class="mhw-menuBox" data-bind="css: {'slideUp' : $data.showSubjectSelectBox}">
                        <div class="slideItem" data-bind="click: function(){$data.showSubjectSelectBox(!$data.showSubjectSelectBox())}">切换学科<span class="arrow"></span></div>
                        <ul class="slideInfo" data-bind="visible: $data.showSubjectSelectBox">
                            <!-- ko foreach : {data : $root.subjectList(), as : '_subject'} -->
                            <li data-bind="text: _subject.value(), css: {'active' : _subject.checked()}, click: $root.selectSubjectBtn">--</li>
                            <!--/ko-->
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div style="clear: both"></div>
    <div class="mhw-main" style="height: 100%; overflow: hidden; overflow-y: scroll; -webkit-overflow-scrolling : touch;" data-bind="event: { scroll: $root.reportScrolled }">
        <div class="mjd-detailsList">
            <div class="hd">
                <span class="font-bold">班级</span>
            </div>
            <div class="mn">
                <div class="msf-stuInfo-label">
                    <ul class="classInfo">
                        <!-- ko foreach: {data : $data.allClazzDetail(), as : '_clazz'} -->
                            <li data-bind="text: _clazz.classLevel()+'年级'+_clazz.className(),click: $root.selectClazzBtn, css: {'active': _clazz.checked()}" class="txt-overflow">--</li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
        </div>

        <div data-bind="visible : $data.reportDetail().length > 0" style="display: none;">
            <!-- ko foreach: {data : $data.reportDetail(), as : '_report'} -->
            <div class="mjd-detailsList">
                <div class="hd">
                <span class="font-bold">
                    <i class="new-icon" data-bind="visible: !_report.checked">新</i>
                    <span data-bind="text: _report.clazzName+' '+_report.homeworkName + ((_report.homeworkType == 'Similar') ? '改错类题作业' : '作业')"></span>
                    <a href="javascript:void(0);" class="w-btn w-btn-lightBlue" style="display: none;" data-bind="visible:_report.offlineHomeworkId == null && _report.showAssignOffline,click:$root.addOfflineHomework.bind($data)">发送作业单</a>
                    <a href="javascript:void(0);" class="w-btn w-btn-lightBlue" style="display: none;" data-bind="visible:_report.offlineHomeworkId != null, click:$root.viewOfflineHomework.bind($data)">查看作业单</a>
                </span>
                </div>
                <div class="mn">
                    <div class="mjd-progressBar">
                        <h2 class="title">内容：<span data-bind="text: _report.content"></span></h2>
                        <h2 class="title txtGrey">截止时间： <span data-bind="text: _report.endTime"></span></h2>
                        <div class="progressBox">
                            <div class="progress">
                                <div class="txt">完成人数</div>
                                <div class="proBox">
                                    <div class="proCurrent bg-green" data-bind="style:{width: $root.processRate(_report.finishedCount,_report.userCount)}"></div>
                                </div>
                                <div class="txt txtGreen txt-c"><span data-bind="text: _report.finishedCount+ '/' +_report.userCount">0/0</span></div>
                            </div>
                            <div class="progress" data-bind="visible: _report.includeSubjective">
                                <div class="txt" data-bind="css:{'txtGrey' : !_report.includeSubjective}">批改进度</div>
                                <div class="proBox">
                                    <div class="proCurrent " data-bind="style:{width: $root.processRate(_report.correctedCount,_report.finishedCount)},css:{'bg-yellow':_report.includeSubjective}"></div>
                                </div>
                                <!--ko if:_report.includeSubjective-->
                                <div class="txt txtYellow txt-c"><span data-bind="text:_report.correctedCount + '/' + _report.finishedCount">0/0</span></div>
                                <!--/ko-->
                            </div>
                        </div>
                    </div>
                    <div class="mhw-btns btns-2">
                        <a data-bind="click: $root.viewHomeworkDetailBtn.bind($data,_report.homeworkId)" href="javascript:void(0)" class="w-btn w-btn-lightBlue">查看作业详情</a>
                        <a href="javascript:void(0)" class="w-btn" data-bind="css:{'disabled' : !_report.showCheck},click:$root.checkHomework,text:_report.checked ? '已检查' : '检查'">检查</a>
                    </div>
                </div>
            </div>
            <!--/ko-->
        </div>

        <!--ko if: $data.reportDetail()-->
        <div data-bind="visible: $data.reportDetail().length == 0 && !$root.reportLoading()" style="display: none; text-align: center; font-size: 30px; padding:  3rem;">暂无作业报告</div>
        <!--/ko-->
    </div>
</@homeworkReport.page>