<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["ko","datepicker","jplayer","homeworkv3.earlylist"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework"] />

<div id="homeworkList" class="h-homeworkList">
    <div class="w-base" style="border-color: #dae6ee;">
        <div class="hl-main">
            <div class="w-base-title">
                <h3>作业报告</h3>
            </div>
            <div class="t-homework-form" data-bind="if:levelClazzList().length > 0,visible:levelClazzList().length > 0">
                <!--ko foreach:{data : levelClazzList,as:'levelList'}-->
                <dl>
                    <dd style="margin: 0;">
                        <div class="t-homeworkClass-list">
                            <div class="pull-down">
                                <!--ko foreach:{data:levelList,as:'clazz'}-->
                                <p data-bind="css:{'w-checkbox-current':clazz.checked},click:$root.clazzClick">
                                    <span class="w-checkbox"></span>
                                    <span class="w-icon-md" data-bind="text:clazz.className"></span>
                                </p>
                                <!--/ko-->
                            </div>
                        </div>
                    </dd>
                </dl>
                <!--/ko-->
                <dl>
                    <dd style="margin: 0;">
                        <div class="t-homeworkClass-list" style="width: 100%">
                            <div class="pull-down">
                                <p style="width:auto;padding-bottom: 4px;">
                                    <span style="display:inline-block;width: 74px">开始日期：</span>
                                    <input style="width: 140px;" readonly="readonly" type="text" id="beginDateInput" data-bind="textInput: begin" />
                                    <a href="javascript:void(0);" data-bind="click:$root.searchClick" style="padding: 4px 0;width: 80px;margin-left:12px" class="w-btn w-btn-small">查询</a>
                                </p>
                            </div>
                        </div>
                        <p style="margin-left: 84px;margin-bottom: 20px;color: #999;">你将查询从布置时间开始的30天内作业记录</p>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
    <!--ko if:homeworkList().length > 0-->
    <!--ko foreach:{data:homeworkList(),as:'homework'}-->
    <div class="h-workList-box">
        <div class="hwl-header">
            <span class="state" data-bind="css:{'txt-red':!checked(),'txt-green':checked()},text:checked()?'[已检查]':'[待检查]'"></span>
            <span data-bind="text:homework.clazzName() + '&nbsp;&nbsp;' + homework.homeworkName() + ((homework.homeworkType && homework.homeworkType() == 'Similar') ? '改错类题作业' : '作业')"></span>
            <span class="s-tag" style="display: none;" data-bind="visible:homework.isTermReview && homework.isTermReview()">期末作业</span>
        </div>
        <div class="hwl-main">
            <table class="hwl-table">
                <tr>
                    <td class="td-cell01">
                        <div class="title">内容：<span data-bind="textcut: { text: homework.content(), length: 15 },attr:{title:homework.content()}"></span></div>
                        <div class="title">截止时间：<span data-bind="text:homework.endTime()"></span></div>
                    </td>
                    <td class="td-cell02">
                        <p class="txt-green"><span class="font-b" data-bind="text:homework.finishedCount()">0</span>/<span data-bind="text:homework.userCount()">0</span>人</p>
                        <p class="txt-green">已完成</p>
                    </td>
                    <td class="td-cell02" data-bind="visible:homework.includeSubjective()">
                        <p class="txt-red"><span class="font-b" data-bind="text:(homework.finishedCount() - homework.correctedCount())">0</span>人</p>
                        <p class="txt-red">待批改</p>
                    </td>
                    <td class="td-cell03">
                        <a href="javascript:void(0);" data-bind="visible:!terminated() && !checked() && homework.finishedCount() < homework.userCount(),click:$root.deleteHomework" class="link">删除</a>
                        <a href="javascript:void(0);" data-bind="visible:!terminated() && !checked() && homework.finishedCount() < homework.userCount(),click:$root.adjustHomework" class="link">调整</a>
                        <a href="javascript:void(0);" data-bind="click:$root.viewReport" class="w-btn w-btn-well">查看详情</a>
                        <a href="javascript:void(0);" data-bind="attr:{title : !showCheck() && !checked() ? '作业未到截止日期，暂不能检查' : ''},css:{'w-btn-disabled' : !showCheck()},click:$root.checkHomework,text:checked() ? '已检查' : '检查作业'" class="w-btn w-btn-well">检查作业</a>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <!--/ko-->
    <div class="system_message_page_list message_page_list" style="width: 100%; background: #edf5fa; padding:15px 0; text-align: center;">

        <a data-bind="css:{'disable' : isFirstPage(),'enable' : !isFirstPage()},click:page_click.bind($data,$root,currentPage() - 1)" href="javascript:void(0);" v="prev"><span>上一页</span></a>

        <!--ko if: (totalPage() - currentPage()) >= 5-->
        <!--ko foreach:ko.utils.range(currentPage(), currentPage() + 2)-->
        <a data-bind="css:{'this': $data == $root.currentPage()},click:$root.page_click.bind($data,$root,$data)" href="javascript:void(0);">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <span class="points"> ... </span>
        <a href="javascript:void(0);" data-bind=",click:page_click.bind($data,$root,totalPage())">
            <span data-bind="text:totalPage()"></span>
        </a>
        <!--/ko-->

        <!--ko if:(totalPage() - currentPage()) < 5 && totalPage() > 5-->
        <!--ko foreach:ko.utils.range(totalPage() - 4, totalPage())-->
        <a data-bind="css:{'this': $data == $root.currentPage()},click:$root.page_click.bind($data,$root,$data)" href="javascript:void(0);">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:((totalPage() - currentPage()) < 5) && (totalPage() <= 5)-->
        <!--ko foreach:ko.utils.range(1, totalPage())-->
        <a data-bind="css:{'this': $data == $root.currentPage()},click:$root.page_click.bind($data,$root,($index() + 1))" href="javascript:void(0);">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <!--/ko-->

        <a class="disable" data-bind="css:{'disable' : isLastPage(), 'enable' : !isLastPage()},click:page_click.bind($data,$root,currentPage() + 1)" href="javascript:void(0);" v="next"><span>下一页</span></a>
    </div>

    <!--/ko-->
    <!--ko if:homeworkList().length == 0 && hkLoading()-->
    <div data-bind="template:{name:'t:加载中'}"></div>
    <!--/ko-->
    <!--ko if:homeworkList().length == 0 && !hkLoading()-->
    <div class="h-workList-box">
        <div class="hwl-header">

        </div>
        <div class="hwl-main">
            暂无数据
        </div>
    </div>
    <!--/ko-->

</div>

<script id="t:mathAdjustTime" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">布置时间：<span id="startDateId"><%=data.startDateTime%></span></div>
            <div class="list">
                <div class="name">布置内容：</div>
                <div class="info">
                    <%for(var z = 0,zLen = data.practices.length; z < zLen; z++){%>
                    <%
                    var ocType = data.practices[z].objectiveConfigType;
                    var measurementUnits = (ocType === 'READING' ? '本': (ocType === 'BASIC_APP' ? '个' : '题'));
                    %>
                    <span class="tj"><%=data.practices[z].typeName%>（共<strong><%=data.practices[z].questionCount%></strong><%=measurementUnits%>）</span>
                    <%}%>
                </div>
            </div>
            <div class="list">预计时间：<%=minute%>分钟</div>
            <div class="list">完成时间：
                <label style="cursor: pointer;" class="endDateLable" data-relativeday="0"><span class="w-radio"></span> <span class="w-icon-md">今天内</span></label>
                <label style="cursor: pointer;" class="endDateLable" data-relativeday="1"><span class="w-radio"></span> <span class="w-icon-md">明天内</span></label>
                <label style="cursor: pointer;" class="endDateLable" data-relativeday="2"><span class="w-radio"></span> <span class="w-icon-md">三天内</span></label>
                <label style="cursor: pointer;padding-right: 0px;" class="endDateLable" data-relativeday="-1">
                    <span class="w-radio"></span> <span class="w-icon-md">自定义</span>
                    <input type="text" id="endDateInput"  placeholder="自定义时间" readonly="readonly" class="c-ipt">
                    <input type="hidden" id="endDateTime" name="endDateTime" value="<%=data.endDateTime%>"/>
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" id="endHour"><option value="23">23</option></select>时
                    <select class="w-int" style="width: 60px;" id="endMin"><option value="59">59</option></select>分
                </label>
            </div>
            <div class="list tips-grey">提交作业截止时间为<span id="endDateId"></span></div>
        <#--<div class="btn-box"><a href="javascript:void(0)" class="w-btn w-btn-well">确认发布</a></div>-->
        </div>
    </div>
</script>
<script type="text/javascript">
    var constantObj = {
        levelClazzList : ${clazzLevelMap![]},
        subject        : "${(curSubject)!}",
        currentDayEndDate : "${currentDayEndDate!}",
        defaultStartDate  : "${startDate!}"
    };

    $(function(){
        LeftMenu.focus("${curSubject!}_early_homeworkhistory");
        addTextcutEvent();

        $17.voxLog({
            module: "m_Odd245xH",
            op : "page_report_load",
            s0 : constantObj.subject
        });

    });
</script>
</@shell.page>