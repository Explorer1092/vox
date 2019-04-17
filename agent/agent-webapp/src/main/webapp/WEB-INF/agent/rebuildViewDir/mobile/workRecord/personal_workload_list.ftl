<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="下属工作情况" pageJs="work_statistic" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['work_statistic']/>
    <#include "work_common_time.ftl">
<div class="statistic_date"></div>
<script type="text/html" id="statistic_date">
    <%if(res){%>
    <%var bdWorkloadList = res[res.dateType].data.bdWorkloadList%>
    <%var cmWorkloadList = res[res.dateType].data.cmWorkloadList%>
    <%var amWorkloadList = res[res.dateType].data.amWorkloadList%>
    <%var rmWorkloadList = res[res.dateType].data.rmWorkloadList%>
    <%var groupRoleType = res[res.dateType].data.groupRoleType%>
    <%if(res.dateType == "day"){%>
    <div class="statistics_top">
        <a href="javascript:void(0);" class="prev js-prev" data-info="day" data-type="1"><前一天 </a>
        <p><%=res[res.dateType].data.dateShowStr||0%></p>
        <a href="javascript:void(0);" class="next js-next" data-info="day" data-type="1">后一天> </a>
    </div>
    <%}%>
    <%if(res.dateType == "week"){%>
    <div class="statistics_top">
        <a href="javascript:void(0);" class="prev js-prev" data-info="week" data-type="2"><上周 </a>
        <p><%=res[res.dateType].data.dateShowStr||0%></p>
        <a href="javascript:void(0);" class="next js-next" data-info="week" data-type="2">下周> </a>
    </div>
    <%}%>
    <%if(res.dateType == "month"){%>
    <div class="statistics_top">
        <a href="javascript:void(0);" class="prev js-prev" data-info="month" data-type="3"><上月 </a>
        <p><%=res[res.dateType].data.dateShowStr||0%></p>
        <a href="javascript:void(0);" class="next js-next" data-info="month" data-type="3">下月> </a>
    </div>
    <%}%>
    <div class="statistics_main">
        <div class="s_list third js-chooseRole" >
            <ul>
                <li>
                    <div class="active" data-info="showBds" style="width: 25%;">专员</div>
                    <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager()||requestContext.getCurrentUser().isAreaManager()>
                        <div data-info="showCitys" style="width: 25%;">市经理</div>
                    </#if>

                    <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager()>
                        <div data-info="showAreas" style="width: 25%;">区域经理</div>
                    </#if>
                    <#if requestContext.getCurrentUser().isCountryManager()>
                        <div data-info="showRegions" style="width: 25%;">大区经理</div>
                    </#if>
                </li>
            </ul>
        </div>
    </div>
    <div>
        <%if((!bdWorkloadList || bdWorkloadList.length < 0 ) && (!cmWorkloadList || cmWorkloadList.length < 0 ) && (!rmWorkloadList || rmWorkloadList.length < 0 )){%>
            <div style="text-align: center;padding:.5rem 0">暂无数据</div>
        <%}%>
        <%if(bdWorkloadList && bdWorkloadList.length > 0){%>
            <div class="statistics_main showBds">
                <div class="s_list third">
                    <ul>
                        <li>
                            <div>姓名</div>
                            <div>分区</div>
                            <div>个人日均工作量（T）</div>
                        </li>
                        <%for(var i=0;i < bdWorkloadList.length;i++){%>
                        <li class="js-showDetail" style="cursor: pointer;<%if(bdWorkloadList[i].workload == 0){%>background:yellow;color:red<%}%>" data-id="<%=bdWorkloadList[i].id%>">
                            <div><%=bdWorkloadList[i].name%></div>
                            <div><%=bdWorkloadList[i].groupName%></div>
                            <div><%=bdWorkloadList[i].workload%></div>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
        <%}%>
        <%if(cmWorkloadList && cmWorkloadList.length > 0){%>
            <div class="statistics_main showCitys" style="display:none;">
                <div class="s_list third">
                    <ul>
                        <li>
                            <div>姓名</div>
                            <div>大区</div>
                            <div>个人日均工作量（T）</div>
                        </li>
                        <%for(var i=0;i < cmWorkloadList.length;i++){%>
                        <li class="js-showDetail" style="cursor: pointer;" data-id="<%=cmWorkloadList[i].id%>">
                            <div><%=cmWorkloadList[i].name%></div>
                            <div><%=cmWorkloadList[i].groupName%></div>
                            <div><%=cmWorkloadList[i].workload%></div>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
        <%}%>
        <%if(amWorkloadList && amWorkloadList.length > 0){%>
            <div class="statistics_main showAreas" style="display: none;">
                <div class="s_list third">
                    <ul>
                        <li>
                            <div>姓名</div>
                            <div>分区</div>
                            <div>个人日均工作量（T）</div>
                        </li>
                        <%for(var i=0;i < amWorkloadList.length;i++){%>
                        <li class="js-showDetail" style="cursor: pointer;" data-id="<%=amWorkloadList[i].id%>">
                            <div><%=amWorkloadList[i].name%></div>
                            <div><%=amWorkloadList[i].groupName%></div>
                            <div><%=amWorkloadList[i].workload%></div>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
        <%}%>
        <%if(rmWorkloadList && rmWorkloadList.length > 0){%>
            <div class="statistics_main showRegions" style="display: none;">
                <div class="s_list third">
                    <ul>
                        <li>
                            <div>姓名</div>
                            <div>分区</div>
                            <div>个人日均工作量（T）</div>
                        </li>
                        <%for(var i=0;i < rmWorkloadList.length;i++){%>
                        <li class="js-showDetail" style="cursor: pointer;" data-id="<%=rmWorkloadList[i].id%>">
                            <div><%=rmWorkloadList[i].name%></div>
                            <div><%=rmWorkloadList[i].groupName%></div>
                            <div><%=rmWorkloadList[i].workload%></div>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
        <%}%>
    </div>
    <%}%>
</script>
<script>
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:"<#if !requestContext.getCurrentUser().isCityManager() && !requestContext.getCurrentUser().isBusinessDeveloper()>个人<#else>""</#if>",
            rightTextColor:"ff7d5a",
            needCallBack:true
        };
        var topBarCallBack = function () {
            window.location.href="team_workload_list.vpage?groupId="+getUrlParam('groupId');
        };
        setTopBarFn(setTopBar,topBarCallBack);
    });
    var ajaxUrl = "personal_workload.vpage";
    $(document).on("click",".js-showDetail",function(){
        openSecond("/view/mobile/crm/visit/visit_detail.vpage?userId="+$(this).data("id"))
    });
    var changeTabStatus = true;
</script>
</@layout.page>
