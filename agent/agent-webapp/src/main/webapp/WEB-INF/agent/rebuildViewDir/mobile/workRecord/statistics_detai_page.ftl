<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录详情" pageJs="work_statistic" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['work_statistic']/>
<li class="statistic requestAjax active" style="display: none;" data-type="3" data-info="month">月</li>
<div class="statistic_date"></div>
<script type="text/html" id="statistic_date">
    <%if(res){%>
        <div class="statistics_top">
            <a href="javascript:void(0);" class="prev js-prev" data-info="month" data-type="3"><上月 </a>
            <p><%=res[res.dateType].data.dateShowStr||0%></p>
            <a href="javascript:void(0);" class="next js-next" data-info="month" data-type="3">下月> </a>
        </div>
        <div>
            <div class="statistics_main">
                <div class="s_title">我的工作</div>
                <div class="s_column second">
                    <ul>
                        <li>
                            <div><%=res[res.dateType].data.statistics.avgWorkload||0%>T</div>
                            <div>日均工作量</div>
                        </li>
                        <%if(res[res.dateType].data.agentRoleType != "BusinessDeveloper"){%>
                            <li>
                                <div><%=res[res.dateType].data.statistics.visitCount||0%></div>
                                <div>陪访（次）</div>
                            </li>
                        <%}else if(res[res.dateType].data.agentRoleType == "BusinessDeveloper"){%>
                            <li>
                                <div><%=res[res.dateType].data.statistics.intoSchoolCount||0%></div>
                                <div>进校（次）</div>
                            </li>
                        <%}%>
                    </ul>
                </div>
                <div class="s_info">工作量：
                    <%if(res[res.dateType].data.agentRoleType != "BusinessDeveloper"){%>
                        陪访（<%=res[res.dateType].data.statistics.visitCount||0%>）、
                    <%}%>
                    进校（<%=res[res.dateType].data.statistics.intoSchoolCount||0%>）、
                    组会（<%=res[res.dateType].data.statistics.groupMeetingCount||0%>）、
                    教研员（<%=res[res.dateType].data.statistics.researchersCount||0%>）</div>
            </div>
            <%if(res[res.dateType].data.recordList && res[res.dateType].data.recordList.length>0){%>
                <%for(var i=0;i< res[res.dateType].data.recordList.length;i++){%>
                    <%var record = res[res.dateType].data.recordList[i]%>
                    <div class="statistics_box">
                        <div class="s_time"><span><%=record.workload%>T</span><%=record.sortDate%></div>
                        <ul>
                            <%if(record.workRecordListData && record.workRecordListData.length>0){%>
                                <%for(var j=0;j< record.workRecordListData.length;j++){%>
                                    <%var workRecordListData = record.workRecordListData[j]%>
                                    <li class="showRecordList" style="cursor: pointer;" data-type="<%=workRecordListData.workRecordType%>" data-info="<%=workRecordListData.workRecordId%>"><%if(workRecordListData.workRecordType == "SCHOOL"){%><span><%=workRecordListData.visitTeacherCount%>位老师</span><%}%>
                                        <%if(workRecordListData.workRecordType == "SCHOOL"){%>
                                        （校）
                                        <%}%>
                                        <%if(workRecordListData.workRecordType == "VISIT"){%>
                                        （访）
                                        <%}%>
                                        <%if(workRecordListData.workRecordType == "MEETING" || workRecordListData.workRecordType == "JOIN_MEETING"){%>
                                        （组）
                                        <%}%>
                                        <%if(workRecordListData.workRecordType == "TEACHING"){%>
                                        （教）
                                        <%}%>
                                        <%=workRecordListData.workRecordRemarks%>
                                        <%if(workRecordListData.workRecordType == "SCHOOL" && workRecordListData.intoSchoolMultiSubject ==false){%>
                                            <i class="icon-oneSubject"></i>
                                        <%}%>
                                    </li>
                                <%}%>
                            <%}%>
                        </ul>
                    </div>
                <%}%>
            <%}%>
        </div>
        <%if(res[res.dateType].data.userVisitAndAssignHwTeaPct){%>
            <div class="statistics_footer">
                <div class="inner">
                    <a href="javascript:void(0);" class="showTeacherList" >布置率<%=Math.floor(res[res.dateType].data.userVisitAndAssignHwTeaPct)%>%——查看访后未布置老师明细</a>
                </div>
            </div>
        <%}%>
    <%}%>
</script>
<script>
    template.helper('Math',Math);
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:'',
            rightTextColor:"ff7d5a",
            needCallBack:false
        } ;
        setTopBarFn(setTopBar);
    });
    var ajaxUrl = "statistics_detai.vpage";
    var changeTabStatus = true;
    $(document).on("click",".showTeacherList",function () {
//        window.location.href = "/mobile/into_school/visit_teacher.vpage?userId="+getUrlParam("userId");
        openSecond("/mobile/into_school/visit_teacher.vpage?userId="+getUrlParam("userId"));
    });
    $(document).on("click",".showRecordList",function () {
        var _type= $(this).data("type");
        if(_type == "SCHOOL"){
//            window.location.href = "/mobile/work_record/showSchoolRecord.vpage?recordId="+$(this).data("info");
            openSecond("/mobile/work_record/showSchoolRecord.vpage?recordId="+$(this).data("info"));
        }else if(_type == "VISIT"){
//            window.location.href = "/mobile/work_record/show_visit_school_record.vpage?recordId="+$(this).data("info");
            openSecond("/mobile/work_record/show_visit_school_record.vpage?recordId="+$(this).data("info"));
        }else if(_type == "MEETING") {
            openSecond('/mobile/work_record/showMeetingRecord.vpage?recordId='+$(this).data("info"))
        }else if (_type == 'JOIN_MEETING'){
            openSecond('/mobile/work_record/showJoinMeetingRecord.vpage?recordId='+$(this).data("info"))
        }else{
            openSecond("/mobile/work_record/record_details.vpage?workRecordId="+$(this).data("info"));
//            window.location.href = "/mobile/work_record/record_details.vpage?workRecordId="+$(this).data("info");
        }

    })
</script>
</@layout.page>
