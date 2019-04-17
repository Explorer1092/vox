<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录详情" pageJs="work_statistic" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['work_statistic']/>
    <#include "work_common_time.ftl">
<div class="statistic_date"></div>
<script type="text/html" id="statistic_date">
    <%if(res){%>
        <%var data = res[res.dateType].data.workloadList%>
        <%var groupRoleType = res[res.dateType].data.groupRoleType%>
        <%if(res.dateType == "day"){%>
        <div class="statistics_top">
            <a href="javascript:void(0);" class="prev js-prev" data-info="day" data-type="1"><前一天 </a>
            <p><%=res[res.dateType].data.dateShowStr%></p>
            <a href="javascript:void(0);" class="next js-next" data-info="day" data-type="1">后一天> </a>
        </div>
        <%}%>
        <%if(res.dateType == "week"){%>
            <div class="statistics_top">
                <a href="javascript:void(0);" class="prev js-prev" data-info="week" data-type="2"><上周 </a>
                <p><%=res[res.dateType].data.dateShowStr%></p>
                <a href="javascript:void(0);" class="next js-next" data-info="week" data-type="2">下周> </a>
            </div>
        <%}%>
        <%if(res.dateType == "month"){%>
        <div class="statistics_top">
            <a href="javascript:void(0);" class="prev js-prev" data-info="month" data-type="3"><上月 </a>
            <p><%=res[res.dateType].data.dateShowStr%></p>
            <a href="javascript:void(0);" class="next js-next" data-info="month" data-type="3">下月> </a>
        </div>
        <%}%>
        <%if( !data || data.length < 0){%>
            <div style="text-align: center;padding:.5rem 0">暂无数据</div>
        <%}%>
        <%if(data.length > 0){%>
            <div class="statistics_main">
                <div class="s_list second">
                    <ul>
                        <%if (groupRoleType == "Country" || groupRoleType == "Region" || groupRoleType == "Area" ){%>
                            <li>
                                <div>部门</div>
                                <div>团队人均工作量（T）</div>
                            </li>
                        <%}else if(groupRoleType == "City"){%>
                            <li>
                                <div>专员</div>
                                <div>工作量（T）</div>
                            </li>
                        <%}%>
                        <%for(var i=0;i < data.length;i++){%>
                        <li class="js_showStatistics" <%if (groupRoleType == "Country" || groupRoleType == "Region" || groupRoleType == "Area" ){%>data-url="/mobile/work_record/statistics/team_workload_list.vpage?groupId=<%=data[i].id%>"<%}else if (groupRoleType == "City"){%>data-url="/view/mobile/crm/visit/visit_detail.vpage?userId=<%=data[i].id%>"<%}%>>
                            <a href="javascript:void(0);" class="arrow_right">></a>
                            <div><%=data[i].name%></div>
                            <div><%=data[i].workload%></div>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
        <%}%>

    <%}%>
</script>
<script>
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:"<#if !requestContext.getCurrentUser().isCityManager() && !requestContext.getCurrentUser().isBusinessDeveloper()>团队</#if>",
            rightTextColor:"ff7d5a",
            needCallBack:true
        };
        var topBarCallBack = function () {
            window.location.href="personal_workload_list.vpage?groupId="+getUrlParam('groupId');
        };
        setTopBarFn(setTopBar,topBarCallBack);
    });
    var ajaxUrl = "team_workload.vpage";
    var changeTabStatus = true;
</script>
</@layout.page>
