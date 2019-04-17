<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录详情" pageJs="work_statistic" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['work_statistic']/>
    <#include "work_common_time.ftl">
<div class="statistic_date"></div>
<script type="text/html" id="statistic_date">
    <%if(res){%>
        <%var data = res[res.dateType].data.workRecordStatisticsSummary%>
        <%var pdata = res[res.dateType].data.personIntoSchoolStatisticsItem%>
        <%if(res.dateType == "day"){%>
            <div class="statistics_top">
                <a href="javascript:void(0);" class="prev js-prev" data-info="day" data-type="1"><前一天 </a>
                <p><%=res[res.dateType].data.dateShowStr||0%></p>
                <a href="javascript:void(0);" class="next js-next" data-info="day" data-type="1">后一天> </a>
            </div>
            <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAreaManager() || requestContext.getCurrentUser().isCityManager()>
                <div class="statistics_main" style="cursor: pointer;" data-id="<%=data.groupId%>" onclick="team_workload_summary(this)">
                    <div class="s_title"><span>></span>下属工作情况</div>
                    <div id="workSituationChart" class="s_circle workSituationChart">
                        <!--圆形图-->
                    </div>
                    <div class="s_right">
                        <ul>
                            <li><div>专员：</div><div><%=data.bdFillInWorkRecordUserCount || 0%>/<%=data.bdUserCount || 0%></div><div><%=data.bdPerCapitaWorkload || 0%>T</div></li>
                            <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAreaManager()>
                                <li><div>市经理：</div><div><%=data.cmFillInWorkRecordUserCount || 0%>/<%=data.cmUserCount || 0%></div><div><%=data.cmPerCapitaWorkload || 0%>T</div></li>
                            </#if>
                            <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager()>
                                <li><div>区域经理：</div><div><%=data.amFillInWorkRecordUserCount || 0%>/<%=data.amUserCount || 0%></div><div><%=data.amPerCapitaWorkload || 0%>T</div></li>
                            </#if>
                            <#if requestContext.getCurrentUser().isCountryManager()>
                                <li><div>大区经理：</div><div><%=data.rmFillInWorkRecordUserCount || 0%>/<%=data.rmUserCount || 0%></div><div><%=data.rmPerCapitaWorkload || 0%>T</div></li>
                            </#if>
                        </ul>
                    </div>
                </div>
            </#if>
        <%}%>
        <%if(res.dateType == "week" || res.dateType == "month"){%>
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
            <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isCityManager()|| requestContext.getCurrentUser().isAreaManager()>
                <div class="statistics_main" style="cursor: pointer;" data-id="<%=data.groupId%>" onclick="team_workload_summary(this)">
                    <div class="s_title"><span>></span><%if(res.dateType == "week"){%>下属工作情况（当周日均T）<%}else if(res.dateType == "month"){%>下属工作情况（当月日均T）<%}%></div>
                    <div class="s_column third">
                        <ul>
                            <li>
                                <div><%=data.bdPerCapitaWorkload||0%>T</div>
                                <div>专员</div>
                            </li>
                        <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager()|| requestContext.getCurrentUser().isAreaManager()>
                            <li>
                                <div><%=data.cmPerCapitaWorkload||0%>T</div>
                                <div>市经理</div>
                            </li>
                        </#if>
                        <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager()>
                            <li>
                                <div><%=data.amPerCapitaWorkload||0%>T</div>
                                <div>区域经理</div>
                            </li>
                        </#if>
                        <#if requestContext.getCurrentUser().isCountryManager()>
                            <li>
                                <div><%=data.rmPerCapitaWorkload||0%>T</div>
                                <div>大区经理</div>
                            </li>
                        </#if>
                        </ul>
                    </div>
                </div>
            </#if>
        <%}%>
        <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isCityManager()|| requestContext.getCurrentUser().isAreaManager()>
            <div class="statistics_main" data-id="<%=data.groupId%>" onclick="into_school_list(this)">
                <div class="s_title"><span>></span>专员进校情况</div>
                <div class="s_column forth">
                    <ul>
                        <li>
                            <div><%=data.bdPerCapitaIntoSchool || 0%></div>
                            <div>人均进</div>
                            <div>校次数</div>
                        </li>
                        <li>
                            <div><%=data.bdVisitSchoolAvgTeaCount || 0%></div>
                            <div>校均拜</div>
                            <div>访老师数</div>
                        </li>
                        <li>
                            <div><%= Math.round(data.bdVisitEngTeaPercent * 100) || 0%>%</div>
                            <div>拜访英语</div>
                            <div>老师占比</div>
                        </li>
                        <li>
                            <div><%= Math.round(data.bdVisitMathTeaPercent * 100) || 0%>%</div>
                            <div>拜访数学</div>
                            <div>老师占比</div>
                        </li>
                    </ul>
                </div>
            </div>
        </#if>
    <%if(pdata){%>
        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
            <div class="statistics_main showMyWork" data-info="<%=pdata.id%>">
                <div class="s_title"><span>></span>进校情况</div>
                <div class="s_column fifth">
                    <ul>
                        <li>
                            <div><%=pdata.perCapitaIntoSchool || 0%></div>
                            <div>进校</div>
                        </li>
                        <li>
                            <div><%=pdata.visitSchoolAvgTeaCount || 0%></div>
                            <div>校均拜</div>
                            <div>访老师数</div>
                        </li>
                        <li>
                            <div><%= Math.round(pdata.visitEngTeaPercent*100) || 0%>%</div>
                            <div>英语老师</div>
                        </li>
                        <li>
                            <div><%=Math.round(pdata.visitMathTeaPercent*100) || 0%>%</div>
                            <div>数学老师</div>
                        </li>
                        <%if(res.dateType == "month"){%>
                        <li>
                            <div><%=Math.round(pdata.userVisitAndAssignHwTeaPct *100) || 0%>%</div>
                            <div>布置率</div>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
        </#if>
        <div class="statistics_main">
            <div class="s_title showMyWork" data-info="<%=pdata.id%>"><span><%=pdata.workload || 0%>T></span>我的工作</div>
        </div>
        <%}%>
    <%}%>
</script>
<script src="/public/rebuildRes/lib/echarts/echarts.min.js"></script>
<script>
    var ajaxUrl = "summary.vpage";
    $(document).on("click",".showMyWork",function () {
        openSecond('/view/mobile/crm/visit/visit_detail.vpage?userId='+$(this).data("info"))
    });
    <#if requestContext.getCurrentUser().isRegionManager() || requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isCityManager()|| requestContext.getCurrentUser().isAreaManager()>
    var isAgent = true ;
    <#else>
    var isAgent = false ;
    </#if>
</script>
</@layout.page>
