<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <#if currentTeacherWebGrayFunction.isAvailable("PCHomework", "UseVenus")>
        <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre"] />
    </#if>
    <@sugar.capsule js=["ko","homework2nd","datepicker","homeworkv3.clazzreport","jplayer"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework"] />
<div class="h-workPaper" id="clazzReport">
    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:webLoading,visible:webLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>
    <div class="w-base h-baseTab" style="display: none;" data-bind="if:!webLoading(),visible:!webLoading()">
        <div class="w-base-title">
            <h3 data-bind="text:homeworkName()"></h3>
        </div>
        <div class="topTips">
            <!--ko if:$root.canMarking && $root.canMarking()-->
                <span class="txt">已提交作业中还有需要您手动批改 的照片／录音作业，请您批改</span>
                <a href="javascript:void(0);" data-bind="click: $root.toClazzreportDetail" class="btn"><i class="write-icon"></i>批改作业</a>
            <!--/ko-->
            <!--ko if:!($root.canMarking && $root.canMarking())-->
                <span class="txt">有<!--ko text:$root.finishedUserNum()--><!--/ko-->名学生完成作业，写评语鼓励下吧</span>
                <a href="javascript:void(0);" data-op="homework_passport_details_comment_btn" class="btn btn-green" data-bind="click:fastComment.bind($data,'topComment')"><i class="write-icon"></i>写评语</a>
            <!--/ko-->
        </div>
        <div class="summary-label">
            <ul>
                <li><p class="bg-yellow"><span data-bind="text:finishedUserNum() + '/' + totalUserNum()">0</span></p><p>完成作业</p></li>
                <li><p class="bg-yellow" ><span data-bind="text:avgScore">0</span></p><p>班平均分</p></li>
                <li style="display: none;" data-bind="visible:$root.showCorrect()"><p class="bg-yellow"><span data-bind="text:finishCorrectNum() + '/' + needCorrectNum()">0</span></p><p>完成订正</p></li>
            </ul>
        </div>
        <div class="hwp-main">
            <div class="w-base-title" style="height: 94px;">
                <div class="w-base-switch w-base-two-switch h-switch">
                    <ul id="tagCategory" class="Teachertitle" style="height: 94px;">
                        <!--ko foreach:{data : tabs,as:'tab'}-->
                        <li data-bind="css:{'active' : tab.tabType == $root.focusTabType()}">
                            <a href="javascript:void(0);" data-bind="click:$root.tabClick.bind($data,$root)">
                                <span class="h-arrow"><i class="w-icon-arrow" data-bind="css:{'w-icon-arrow-blue' : tab.tabType == $root.focusTabType()}"></i></span>
                                <i class="tab-icon" style="" data-bind="style:{backgroundImage : 'url(' + tab.icon + ')'}"></i>
                                <p data-bind="text:tab.tabName"></p>
                            </a>
                        </li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
            <div class="summary-box" data-bind="visible:'studentInfo' == focusTabType()">
                <div class="summary-table" data-bind="if:$root.studentView.studentReportList && $root.studentView.studentReportList().length > 0">
                    <div class="st-title">
                        <div>学生作业详情</div>
                    </div>
                    <div class="table-details">
                        <div class="table-left">
                            <table cellpadding="0" cellspacing="0">
                                <thead>
                                <tr>
                                    <td>学生姓名</td>
                                    <td>平均分</td>
                                    <!--ko if:$root.studentView.showCorrect()-->
                                    <td>订正<br/>正确率</td>
                                    <!--/ko-->
                                </tr>
                                </thead>
                                <tbody>
                                <!--ko foreach:{data : $root.studentView.studentReportList,as:'student'}-->
                                <tr data-bind="css:{'odd' : $index()%2 == 0}">
                                    <td class="td-name">
                                        <i class="icon icon-differ" data-bind="if:student.repair && student.repair(),visible:student.repair && student.repair()"></i>
                                        <i class="icon icon-unfinished" data-bind="if:!(student.finished && student.finished()),visible:!(student.finished && student.finished())"></i>
                                        <a class="name" href="javascript:void(0);" data-bind="click:$root.studentView.studentNameClick.bind($data,$root.studentView),text:student.userName">
                                        </a>
                                    </td>
                                    <td data-bind="text:student.avgScoreStr">0</td>
                                    <!--ko if:$root.studentView.showCorrect()-->
                                    <td data-bind="text:student.correctInfo">&nbsp;</td>
                                    <!--/ko-->
                                </tr>
                                <!--/ko-->
                                </tbody>
                            </table>
                        </div>
                        <div class="table-main">
                            <div class="thead">作业内容</div>
                            <div class="tbody">
                                <!--ko if: $root.studentView.typeNames().length > 4-->
                                <div class="arrow arrow-left" style="cursor: pointer;z-index: 5;" data-bind="click:$root.studentView.leftMove.bind($root.studentView,$element)"><i class="arrow-icon"></i></div>
                                <div class="arrow arrow-right" style="cursor: pointer;z-index: 5;" data-bind="click:$root.studentView.rightMove.bind($root.studentView,$element)"><i class="arrow-icon"></i></div>
                                <!--/ko-->
                                <table class="configTypesTable" cellpadding="0" cellspacing="0"  data-bind="style:{width:$root.studentView.typeNames().length > 4 ? $root.studentView.typeNames().length * 75 + 'px' : '100%',position:'relative',left:'0px',transition: 'left 1s'}">
                                    <thead>
                                    <tr>
                                        <!--ko foreach:$root.studentView.typeNames()-->
                                        <td data-bind="text:$data"></td>
                                        <!--/ko-->
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <!--ko foreach:{data : $root.studentView.studentReportList,as:'student'}-->
                                    <tr data-bind="css:{'odd' : $index()%2 == 0}">
                                        <!--ko foreach:{data : student.typeInformation(),as:'typeInfo'}-->
                                        <td data-bind="text:typeInfo">&nbsp;</td>
                                        <!--/ko-->
                                    </tr>
                                    <!--/ko-->
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="table-right">
                            <table cellpadding="0" cellspacing="0">
                                <thead>
                                <tr>
                                    <td>完成时间</td>
                                    <td>完成用时</td>
                                    <td>操作</td>
                                </tr>
                                </thead>
                                <tbody>
                                <!--ko foreach:{data : $root.studentView.studentReportList,as:'student'}-->
                                <tr data-bind="css:{'odd' : $index()%2 == 0}">
                                    <td data-bind="text:student.finishTimeStr"></td>
                                    <td data-bind="text:student.durationStr">&nbsp;</td>
                                    <td>
                                        <a href="javascript:void(0)" data-bind="text: student.comment() ? '查看' : '写评语', click: $root.studentView.showComment.bind($data,$root.studentView)" class="txt-blue">写评语</a>
                                        <a href="javascript:void(0)" data-bind="click: $root.studentView.rewardBeans.bind($data,$root.studentView)" class="txt-blue">奖学豆</a>
                                    </td>
                                </tr>
                                <!--/ko-->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="summary-box" data-bind="visible:'summaryReport' == focusTabType(),template: {name: 't:typeListTemplate', data: $root.summaryReport,as:'sReport' }">
            </div>
        </div>
        <div class="summary-table" style="margin-bottom: 15px;margin-top: -15px;display: none;" data-bind="visible:'studentInfo' == focusTabType() && $root.studentView.studentReportList && $root.studentView.studentReportList().length > 0">
            <div class="notes" style="display: none;" data-bind="visible:$root.newOpts.subject == 'MATH'">注：订正内容是根据错题推送的类题或原题。</div>
            <div class="notes" style="display: none;" data-bind="visible:$root.newOpts.subject == 'ENGLISH'">注：订正内容是根据错题推送的类题或原题，其中听说形式的作业不需要订正。</div>
            <div class="notes" style="display: none;" data-bind="visible:$root.newOpts.subject == 'CHINESE'">注：订正内容是原题，其中读背、绘本中的错题不需要订正。</div>
        </div>
    </div>
    <div id="ufo" class="t-homework-total t-homework-total-static" data-bind="if:!webLoading(),visible:!webLoading()">
        <div class="t-homework-total-inner">
            <dl>
                <dd style="margin-left: 100px;">
                    <div class="t-btn">
                        <a href="javascript:void(0);" style="width: 115px;" data-op="a_key_comment_btn" class="w-btn w-btn-small" data-bind="click:fastComment">一键写评语</a>
                        <a href="javascript:void(0);" style="width: 115px;" class="w-btn w-btn-small" data-bind="click:fastRewards">一键奖学豆</a>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>
<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<div id="clazzHomeworkReportEventDiv"></div>
<#include "htmlchip/studentcommentchip.ftl">
<#include "htmlchip/singlerewardbeanschip.ftl">
<#include "htmlchip/fastcommentchip.ftl">
<#include "htmlchip/fastrewardchip.ftl">
<#include "htmlchip/typereportchip.ftl">
<#include "htmlchip/similarquestionchip.ftl">
<script id="t:LOAD_IMAGE" type="text/html">
    <div style="height: 200px; background-color: white; width: 98%;">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
    </div>
</script>

<script type="text/javascript">
    var constantObj = {
        homeworkId       : "${homeworkId!}",
        homeworkType     : "${homeworkType!}",
        clazzId          : "${clazzId!}",
        subject          : "${(curSubject)!}",
        tabIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/tab-icon' />',
        userAuth           : !!"${((currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS")?string}",
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />,
        useVenus         : ${((currentTeacherWebGrayFunction.isAvailable("PCHomework", "UseVenus"))!false)?string}
    };

    $(function(){
        LeftMenu.focus("${curSubject!}_homeworkhistory");

        var jqFrom = $17.getQuery("from");
        $17.voxLog({
            module: "m_Odd245xH",
            op : "page_reportdetails_load",
            s0 : constantObj.subject,
            s1 : constantObj.homeworkType,
            s2 : jqFrom === "check" ? "检查作业" : (jqFrom === "view" ? "查看作业详情" : ""),
            s3 : constantObj.homeworkId
        });


        $17.backToTop();


        $(window).on("scroll", function(){
             var $Ufo = $("#ufo");
            if($Ufo.length > 0){
                if(($Ufo.offset().top - $(window).height() >  $(window).scrollTop()) ){
                    $Ufo.removeClass("t-homework-total-static");
                }else{
                    $Ufo.addClass("t-homework-total-static");
                }
            }
        });
    });
</script>
</@shell.page>