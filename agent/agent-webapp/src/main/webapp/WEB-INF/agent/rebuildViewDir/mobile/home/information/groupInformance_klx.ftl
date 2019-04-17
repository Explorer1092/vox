<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="information">
    <@sugar.capsule css=['home']/>
<div class="primary-box">
    <#--<div class="res-top fixed-head">-->
        <#--<a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title js-pageTitle">业绩<#if performanceOverview??>（${performanceOverview["name"]!""}）</#if></span>-->
        <#--<a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="color:#636880">初高中</a>-->
    <#--</div>-->
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;top:0;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li style="padding:.2rem 0"><span class="tab_row" style="display:inline-block;text-align:center;height:2.5rem;line-height:2.5rem;width:100%;font-size: .75rem;border-bottom: .05rem solid #cdd3d3" data-views="3" data-idtype="${idType!""}" data-index="1" data-id="${id!0}">小学</span></li>
            <li style="padding:.2rem 0"><span class="tab_row active" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;">初高中</span></li>
        </ul>
    </div>
    <div class="c-opts gap-line">
        <span class="js-tab <#if tabIndex?? && tabIndex == 1>active</#if> active" data-tabindex="1" data-viewtype="gailan">概览</span>
        <#if groupRoleType??>
            <#if groupRoleType == "City" ||  groupRoleType == "Region" || groupRoleType == "Country">
                <#if groupRoleType == "Country">
                    <span class="js-tab <#if tabIndex?? && tabIndex == 2>active</#if>" data-tabindex="2" data-viewtype="Region">大区</span>
                </#if>
                <#if groupRoleType == "Region" || groupRoleType == "Country">
                    <span class="js-tab <#if tabIndex?? && tabIndex == 3>active</#if>" data-tabindex="3" data-viewtype="City">分区</span>
                </#if>
                <span class="js-tab <#if tabIndex?? && tabIndex == 5>active</#if>" data-tabindex="5" data-viewtype="town">城市</span>
                <span class="js-tab <#if tabIndex?? && tabIndex == 4>active</#if>" data-tabindex="4" data-viewtype="user">专员</span>
            </#if>
        </#if>
    </div>
    <div>
        <div class="show_gailan">
            <#if performanceOverview??>
                <div class="schoolRecord-box" style="padding:.5rem 0;">
                    <div class="subTitle">本月目标</div>
                    <ul class="colList-1">
                        <li>数学<div>${performanceOverview["mathAnshGte2StuCountBudget"]!0}</div></li>
                        <li>数学新增<div>${performanceOverview["mathAnshGte2IncStuCountBudget"]!0}</div></li>
                        <li>数学回流<div>${performanceOverview["mathAnshGte2BfStuCountBudget"]!0}</div></li>
                    </ul>
                </div>
                <div class="schoolRecord-box" style="padding:.5rem 0;">
                    <div class="srd-module">
                        <div class="mHead">扫描数据</div>
                        <div class="mTable">
                            <table cellpadding="0" cellspacing="0">
                                <thead>
                                <tr>
                                    <td></td>
                                    <td>全部扫描</td>
                                    <td>新增</td>
                                    <td>回流</td>
                                    <td>扫描日浮</td>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if performanceOverview??>
                                        <#if performanceOverview["completeDataList"]??>
                                            <#list performanceOverview["completeDataList"] as list>
                                            <tr <#if list.name?? && list.name == "全科">style="color:#ff7d5a" </#if>>
                                                <td>${list.name!""}</td>
                                                <td>${list.anshGte2StuCount!0}</td>
                                                <td>${list.anshGte2IncStuCount!0}</td>
                                                <td>${list.anshGte2BfStuCount!0}</td>
                                                <td>${list.anshGte2StuCountDf!0}</td>
                                            </tr>
                                            </#list>
                                        </#if>
                                    </#if>
                                <#--<tr>
                                    <td>新增</td>
                                    <td><span class="fontDefault fontRed">30%</span></td>
                                    <td><span class="fontDefault">0%</span></td>
                                    <td><span class="fontDefault fontRed">20%</span></td>
                                    <td><span class="fontDefault fontGreen">-5%</span></td>
                                </tr>-->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="schoolRecord-box" style="padding:.5rem 0;">
                    <div class="subTitle">英语活跃数据</div>
                    <ul class="colList-2">
                        <li>
                            <div>英活</div>
                            <div>本月 ${performanceOverview["engHwGte3AuStuCount"]!0}</div>
                            <div>昨日 <#if performanceOverview["engHwGte3AuStuCountDf"]??> <#if performanceOverview["engHwGte3AuStuCountDf"] gt 0>+<#elseif performanceOverview["engHwGte3AuStuCountDf"] lt 0>-</#if></#if>${performanceOverview["engHwGte3AuStuCountDf"]!0}</div>
                        </li>
                    </ul>
                </div>
                <div class="schoolRecord-info">说明：全部扫描=新增+回流</div>
            </#if>
        </div>
        <#if userPerformanceViewMap?? || groupPerformanceViewMap?? || groupCityPerformanceViewMap??>
            <div class="schoolRecord-box show_user">
                <div class="srd-module">
                    <div class="mTable" style="display: block">
                        <#list userPerformanceViewMap?keys as key>
                            <#assign contentItem = userPerformanceViewMap[key]>
                            <table class="table_${key!0}" cellpadding="0" cellspacing="0">
                                <thead>
                                    <tr>
                                        <td>姓名</td>
                                        <td class="sortable">全科扫描</td>
                                        <td class="sortable active">全科日浮</td>
                                        <td class="sortable">英语月活</td>
                                        <td class="sortable">英语日浮</td>
                                    </tr>
                                </thead>
                                <tbody>
                                    <#list contentItem?sort_by("anshGte2StuCountDf")?reverse as userRole>
                                        <tr class="js-item" data-level="2" data-id="${userRole.id!0}" data-views="${userRole.viewType!0}" data-type="${userRole.idType!""}">
                                            <td>${userRole.name!""}</td>
                                            <td class="1">${userRole.anshGte2StuCount!0}</td>
                                            <td class="2">${userRole.anshGte2StuCountDf!0}</td>
                                            <td class="3">${userRole.engHwGte3AuStuCount!0}</td>
                                            <td class="4">${userRole.engHwGte3AuStuCountDf!0}</td>
                                        </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </#list>
                    </div>
                </div>
            </div>
            <div class="schoolRecord-box show_town">
                <div class="srd-module">
                    <div class="mTable" style="display: block">
                        <#list groupCityPerformanceViewMap?keys as key>
                            <#assign contentItem = groupCityPerformanceViewMap[key]>
                            <table class="table_${key!0}" cellpadding="0" cellspacing="0">
                                <thead>
                                <tr>
                                    <td>城市</td>
                                    <td class="sortable">全科扫描</td>
                                    <td class="sortable active">全科日浮</td>
                                    <td class="sortable">英语月活</td>
                                    <td class="sortable">英语日浮</td>
                                </tr>
                                </thead>
                                <tbody>
                                    <#list contentItem?sort_by("anshGte2StuCountDf")?reverse as townRole>
                                    <tr class="js-item" data-level="2" data-id="${townRole.id!0}" data-views="${townRole.viewType!0}" data-type="${townRole.idType!""}">
                                        <td>${townRole.name!""}</td>
                                        <td class="1">${townRole.anshGte2StuCount!0}</td>
                                        <td class="2">${townRole.anshGte2StuCountDf!0}</td>
                                        <td class="3">${townRole.engHwGte3AuStuCount!0}</td>
                                        <td class="4">${townRole.engHwGte3AuStuCountDf!0}</td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </#list>
                    </div>
                </div>
            </div>
        <#--</#list>-->
            <#list groupPerformanceViewMap?keys as viewtype>
                <#assign contentItem = groupPerformanceViewMap[viewtype]>
                <div class="schoolRecord-box show_${viewtype!""}">
                    <div class="srd-module">
                        <#if contentItem??>
                            <div class="mTable">
                                <#list contentItem?keys as key1>
                                    <#assign userRole = contentItem[key1]>
                                        <table cellpadding="0" cellspacing="0">
                                            <thead>
                                            <tr>
                                                <td>部门</td>
                                                <td class="sortable">全科扫描</td>
                                                <td class="sortable active">全科日浮</td>
                                                <td class="sortable">英语月活</td>
                                                <td class="sortable">英语日浮</td>
                                            </tr>
                                            </thead>
                                            <tbody>
                                                <#if userRole?size gt 0>
                                                    <#list userRole?sort_by("anshGte2StuCountDf")?reverse as role>
                                                    <tr class="js-item" data-level="2" data-id="${role.id!0}" data-views="${role.viewType!0}" data-type="${role.idType!""}">
                                                        <td>${role.name!""}</td>
                                                        <td class="1">${role.anshGte2StuCount!0}</td>
                                                        <td class="2">${role.anshGte2StuCountDf!0}</td>
                                                        <td class="3">${role.engHwGte3AuStuCount!0}</td>
                                                        <td class="4">${role.engHwGte3AuStuCountDf!0}</td>
                                                    </tr>
                                                    </#list>
                                                </#if>
                                            </tbody>
                                        </table>
                                </#list>
                            </div>
                        </#if>
                    </div>
                </div>
            </#list>
        </#if>
    </div>
</div>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>
<script src="/public/rebuildRes/lib/echarts/echarts.min.js"></script>

<script>
    var rightText = "初高中";
    var roleType = "${idType!""}";
</script>
</@layout.page>