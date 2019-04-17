<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录详情" pageJs="work_statistic" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['work_statistic']/>
<#include "work_common_time.ftl">
<div class="statistic_date"></div>
<script type="text/html" id="statistic_date">
    <%if(res){%>
    <%var bdIntoSchoolList = res[res.dateType].data.bdIntoSchoolList%>
    <%var cityIntoSchoolList = res[res.dateType].data.cityIntoSchoolList%>
    <%var regionIntoSchoolList = res[res.dateType].data.regionIntoSchoolList%>
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
    <div class="statistics_main" <%if(res[res.dateType].data.groupRoleType != "Country" && res[res.dateType].data.groupRoleType != "Region" && res[res.dateType].data.groupRoleType != "Area"){%>style="display:none;"<%}%>>
        <div class="s_list third js-chooseRole">
            <ul>
                <li>
                    <%if(res[res.dateType].data.groupRoleType == "Country"){%>
                        <div class="showList active" data-info="showRegions">大区</div>
                        <div class="showList" data-info="showCitys">分区</div>
                        <div class="showList" data-info="showBds">专员</div>
                    <%}%>
                    <%if(res[res.dateType].data.groupRoleType == "Region" || res[res.dateType].data.groupRoleType == "Area"){%>
                        <div class="active showList" data-info="showCitys" style="width:50%">分区</div>
                        <div class="showList" data-info="showBds" style="width: 50%;">专员</div>
                    <%}%>
                    <%if(res[res.dateType].data.groupRoleType == "City"){%>
                    <div class="showList active" data-info="showBds" style="width: 50%;">专员</div>
                    <%}%>
                </li>
            </ul>
        </div>
    </div>
    <div>
    <%if((!bdIntoSchoolList || bdIntoSchoolList.length < 0 ) && (!cityIntoSchoolList || cityIntoSchoolList.length < 0 ) && (!regionIntoSchoolList || regionIntoSchoolList.length < 0 )){%>
        <div style="text-align: center;padding:.5rem 0">暂无数据</div>
    <%}%>
    <#--专员进校情况-->
    <%if(bdIntoSchoolList){%>
    <div class="statistics_main showBds" style="display:none;">
        <%if(bdIntoSchoolList.length > 0){%>
        <div class="s_list fifth">
            <ul>
                <li>
                    <div>姓名</div>
                    <%if(res.dateType == "day"){%>
                    <div>进校<br/>次数</div>
                    <%}else{%>
                    <div>日均进<br/>校次数</div>
                    <%}%>
                    <div>校均拜<br/>访老师</div>
                    <div>拜访英语老<br/>师占比</div>
                    <div>拜访数学老<br/>师占比</div>
                </li>
                <%for(var i=0;i < bdIntoSchoolList.length;i++){%>
                <li class="js_showStatistics" style="cursor:pointer;" data-url="/view/mobile/crm/visit/visit_detail.vpage?userId=<%=bdIntoSchoolList[i].id%>">
                    <div><%=bdIntoSchoolList[i].name%></div>
                    <div><%=bdIntoSchoolList[i].perCapitaIntoSchool%></div>
                    <div><%=bdIntoSchoolList[i].visitSchoolAvgTeaCount%></div>
                    <div><%=Math.round(bdIntoSchoolList[i].visitEngTeaPercent * 100)%>%</div>
                    <div><%=Math.round(bdIntoSchoolList[i].visitMathTeaPercent * 100)%>% ></div>
                </li>
                <%}%>
            </ul>
        </div>
        <%}else{%>
        <div style="text-align: center;padding:.5rem 0">暂无数据</div>
        <%}%>
    </div>
    <%}%>
    <#--市经理进校情况-->
    <%if(cityIntoSchoolList ){%>
    <div class="statistics_main showCitys" style="display:none;">
        <%if(cityIntoSchoolList.length > 0){%>
        <div class="s_list fifth">
            <ul>
                <li>
                    <div>分区</div>
                    <div>人均进<br/>校次数</div>
                    <div>校均拜<br/>访老师</div>
                    <div>拜访英语老<br/>师占比</div>
                    <div>拜访数学老<br/>师占比</div>
                </li>
                <%for(var i=0;i < cityIntoSchoolList.length;i++){%>
                <li class="js_showStatistics" style="cursor:pointer;" data-url="/mobile/work_record/statistics/into_school_list.vpage?groupId=<%=cityIntoSchoolList[i].id%>">
                    <div><%=cityIntoSchoolList[i].name%></div>
                    <div><%=cityIntoSchoolList[i].perCapitaIntoSchool%></div>
                    <div><%=cityIntoSchoolList[i].visitSchoolAvgTeaCount%></div>
                    <div><%=Math.round(cityIntoSchoolList[i].visitEngTeaPercent * 100)%>%</div>
                    <div><%=Math.round(cityIntoSchoolList[i].visitMathTeaPercent * 100)%>% ></div>
                </li>
                <%}%>
            </ul>
        </div>
        <%}else{%>
            <div style="text-align: center;padding:.5rem 0">暂无数据</div>
        <%}%>
    </div>
    <%}%>
    <#--大区进校情况-->
    <%if(regionIntoSchoolList){%>

    <div class="statistics_main showRegions">
        <%if(regionIntoSchoolList.length > 0){%>
        <div class="s_list fifth">
            <ul>
                <li>
                    <div>部门</div>
                    <div>人均进<br/>校次数</div>
                    <div>校均拜<br/>访老师</div>
                    <div>拜访英语老<br/>师占比</div>
                    <div>拜访数学老<br/>师占比</div>
                </li>
                <%for(var i=0;i < regionIntoSchoolList.length;i++){%>
                <li class="js_showStatistics" style="cursor:pointer;" data-url="/mobile/work_record/statistics/into_school_list.vpage?groupId=<%=regionIntoSchoolList[i].id%>">
                    <div><%=regionIntoSchoolList[i].name%></div>
                    <div><%=regionIntoSchoolList[i].perCapitaIntoSchool%></div>
                    <div><%=regionIntoSchoolList[i].visitSchoolAvgTeaCount%></div>
                    <div><%=Math.round(regionIntoSchoolList[i].visitEngTeaPercent * 100)%>%</div>
                    <div><%=Math.round(regionIntoSchoolList[i].visitMathTeaPercent * 100)%>% > </div>
                </li>
                <%}%>
            </ul>
        </div>
        <%}else{%>
        <div style="text-align: center;padding:.5rem 0">暂无数据</div>
        <%}%>
    </div>

    <%}%>
    </div>
    <%}%>
</script>
<script>
    var ajaxUrl = "into_school.vpage";
    $(document).on("click",".js-showUser",function(){
        window.location.href="personal_workload_list.vpage?groupId="+getUrlParam('groupId');
    });
</script>
</@layout.page>
