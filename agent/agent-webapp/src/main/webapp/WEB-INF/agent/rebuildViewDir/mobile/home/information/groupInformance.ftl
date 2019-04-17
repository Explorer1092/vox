<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="information">
    <@sugar.capsule css=['home']/>
<div class="primary-box">
    <#--<div class="res-top fixed-head">-->
        <#--<a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title js-pageTitle">业绩<#if performanceOverview??>（${performanceOverview["name"]!""}）</#if></span>-->
        <#--<a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="color:#636880">小学</a>-->
    <#--</div>-->
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;top:0;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li style="padding:.2rem 0"><span class="tab_row active" style="display:inline-block;text-align:center;height:2.5rem;line-height:2.5rem;width:100%;font-size: .75rem;border-bottom: .05rem solid #cdd3d3">小学</span></li>
            <li style="padding:.2rem 0"><span class="tab_row" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;" data-index="2" data-views="8" data-idtype="${idType!""}" data-id="${id!0}">初高中</span></li>
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
        <div class="show_gailan view-box">
            <#if performanceOverview??>
                <div class="schoolRecord-box" style="padding:.5rem 0;">
                    <div class="subTitle">区域概况</div>
                    <ul class="colList-1">
                        <li>规模<div>${performanceOverview["stuScale"]!0}</div></li>
                        <li>注册<div>${performanceOverview["regStuCount"]!0}</div></li>
                        <li>认证<div>${performanceOverview["authStuCount"]!0}</div></li>
                    </ul>
                </div>
                <div class="schoolRecord-box" style="padding:.5rem 0;">
                    <div class="srd-module">
                        <div class="mHead">目标达成情况</div>
                        <div class="mTable">
                            <table cellpadding="0" cellspacing="0">
                                <thead>
                                <tr>
                                    <td></td>
                                    <td>目标</td>
                                    <td>完成数</td>
                                    <td>完成率</td>
                                    <td>日浮</td>
                                </tr>
                                </thead>
                                <tbody>
                                <#if performanceOverview??>
                                    <#if performanceOverview["completeDataList"]??>
                                        <#list performanceOverview["completeDataList"] as list>
                                            <tr <#if list.name?? && list.name == "月活">style="color:#ff7d5a" </#if>>
                                                <td>${list.name!""}</td>
                                                <td>${list.maucBudget!0}</td>
                                                <td>${list.mauc!0}</td>
                                                <td data-info="${(list.maucCompleteRate!0) * 100}%">
                                                    <#if list.maucCompleteRate?? && list.maucCompleteRate gt 2 >
                                                        >200%
                                                    <#else>
                                                        ${(list.maucCompleteRate!0) * 100}%
                                                    </#if>
                                                </td>
                                                <td>${list.maucDf!0}</td>
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
                    <div class="subTitle">关键过程指标</div>
                    <ul class="colList-2">
                        <li>
                            <div>注册</div>
                            <div>本月 ${performanceOverview["monthRegStuCount"]!0}</div>
                            <div>昨日 <#if performanceOverview["regStuCountDf"]??> <#if performanceOverview["regStuCountDf"] gt 0>+<#elseif performanceOverview["regStuCountDf"] lt 0>-</#if></#if>${performanceOverview["regStuCountDf"]!0}</div>
                        </li>
                        <li>
                            <div>认证</div>
                            <div>本月 ${performanceOverview["monthAuthStuCount"]!0}</div>
                            <div>昨日 <#if performanceOverview["authStuCountDf"]??> <#if performanceOverview["authStuCountDf"] gt 0>+<#elseif performanceOverview["authStuCountDf"] lt 0>-</#if></#if>${performanceOverview["authStuCountDf"]!0}</div>
                        </li>
                    </ul>
                </div>
                <div class="schoolRecord-info">说明：月活=新增+长回+短回</div>
            </#if>
        </div>
        <#if userPerformanceViewMap?? || groupPerformanceViewMap?? || groupCityPerformanceViewMap??>
            <div class="schoolRecord-box show_user view-box">
                <div class="srd-module">
                        <ul class="srd-nav">
                            <li class="tab_li active <#if viewType ==3>active_btn</#if>" data-index="3">月活</li>
                            <li class="tab_li <#if viewType ==4>active_btn</#if>" data-index="4">新增</li>
                            <li class="tab_li <#if viewType ==5>active_btn</#if>" data-index="5">长回</li>
                            <li class="tab_li <#if viewType ==6>active_btn</#if>" data-index="6">短回</li>
                            <li class="tab_li <#if viewType ==7>active_btn</#if>" data-index="7">注册认证</li>
                        </ul>
                        <div class="mTable" style="display: block">
                            <#list userPerformanceViewMap?keys as key>
                                <#assign contentItem = userPerformanceViewMap[key]>
                                <#if key?? && key != "7">
                                <table class="table_${key!0}" cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr>
                                        <td>姓名</td>
                                        <td class="sortable">目标</td>
                                        <td class="sortable">完成数</td>
                                        <td class="sortable">完成率</td>
                                        <td class="sortable active">日浮</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <#list contentItem?sort_by("maucDf")?reverse as userRole>
                                    <tr class="js-userItem" data-level="1" data-id="${userRole.id!0}" data-views="${userRole.viewType!0}" data-type="${userRole.idType!""}">
                                        <td>${userRole.name!""}</td>
                                        <td class="2">${userRole.maucBudget!0}</td>
                                        <td class="1">${userRole.mauc!0}</td>
                                        <td class="3" data-info="${(userRole.maucCompleteRate!0) * 100}%">
                                            <#if userRole.maucCompleteRate?? && userRole.maucCompleteRate gt 2 >
                                                >200%
                                            <#else>
                                            ${(userRole.maucCompleteRate!0) * 100}%
                                            </#if>
                                        </td>
                                        <td class="4">${userRole.maucDf!0}</td>
                                    </tr>
                                    </#list>
                                    </tbody>
                                </table>
                                <#elseif key == "7">
                                    <table class="table_${key!0}" cellpadding="0" cellspacing="0">
                                        <thead>
                                        <tr>
                                            <td>姓名</td>
                                            <td class="sortable">本月注册</td>
                                            <td class="sortable">本月认证</td>
                                            <td class="sortable active">昨日注册</td>
                                            <td class="sortable">昨日认证</td>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <#list contentItem?sort_by("regStuCountDf")?reverse as userRole>
                                            <tr class="js-userItem" data-level="1" data-id="${userRole.id!0}" data-views="${userRole.viewType!0}" data-type="${userRole.idType!""}">
                                                <td>${userRole.name!""}</td>
                                                <td class="1">${userRole.monthRegStuCount!0}</td>
                                                <td class="2">${userRole.monthAuthStuCount!0}</td>
                                                <td class="3">${userRole.regStuCountDf!0}</td>
                                                <td class="4">${userRole.authStuCountDf!0}</td>
                                            </tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </#if>
                            </#list>
                        </div>
                </div>
            </div>
            <#--城市-->
            <div class="schoolRecord-box show_town view-box">
                <div class="srd-module">
                    <ul class="srd-nav">
                        <li class="tab_li active <#if viewType ==3>active_btn</#if>" data-index="3">月活</li>
                        <li class="tab_li <#if viewType ==4>active_btn</#if>" data-index="4">新增</li>
                        <li class="tab_li <#if viewType ==5>active_btn</#if>" data-index="5">长回</li>
                        <li class="tab_li <#if viewType ==6>active_btn</#if>" data-index="6">短回</li>
                        <li class="tab_li <#if viewType ==7>active_btn</#if>" data-index="7">注册认证</li>
                    </ul>
                    <div class="mTable" style="display: block">
                        <#list groupCityPerformanceViewMap?keys as key>
                            <#assign contentItem = groupCityPerformanceViewMap[key]>
                            <#if key?? && key != "7">
                                <table class="table_${key!0}" cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr>
                                        <td>城市</td>
                                        <td class="sortable">完成数</td>
                                        <td class="sortable active">日浮</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#list contentItem?sort_by("maucDf")?reverse as townRole>
                                        <tr <#--class="js-userItem"--> data-level="1" data-id="${townRole.id!0}" data-views="${townRole.viewType!0}" data-type="${townRole.idType!""}">
                                            <td>${townRole.name!""}</td>
                                            <td class="1">${townRole.mauc!0}</td>
                                            <td class="4">${townRole.maucDf!0}</td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            <#elseif key == "7">
                                <table class="table_${key!0}" cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr>
                                        <td>城市</td>
                                        <td class="sortable">本月注册</td>
                                        <td class="sortable">本月认证</td>
                                        <td class="sortable active">昨日注册</td>
                                        <td class="sortable">昨日认证</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#list contentItem?sort_by("regStuCountDf")?reverse as townRole>
                                        <tr class="js-userItem" data-level="1" data-id="${townRole.id!0}" data-views="${townRole.viewType!0}" data-type="${townRole.idType!""}">
                                            <td>${townRole.name!""}</td>
                                            <td class="1">${townRole.monthRegStuCount!0}</td>
                                            <td class="2">${townRole.monthAuthStuCount!0}</td>
                                            <td class="3">${townRole.regStuCountDf!0}</td>
                                            <td class="4">${townRole.authStuCountDf!0}</td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </#if>
                        </#list>
                    </div>
                </div>
            </div>
                <#--</#list>-->
            <#list groupPerformanceViewMap?keys as viewtype>
                <#assign contentItem = groupPerformanceViewMap[viewtype]>
                <div class="schoolRecord-box show_${viewtype!""} view-box">
                    <div class="srd-module">
                        <#if contentItem??>
                            <ul class="srd-nav">
                                <li class="tab_li active <#if viewType ==3>active_btn</#if>" data-index="3">月活</li>
                                <li class="tab_li <#if viewType ==4>active_btn</#if>" data-index="4">新增</li>
                                <li class="tab_li <#if viewType ==5>active_btn</#if>" data-index="5">长回</li>
                                <li class="tab_li <#if viewType ==6>active_btn</#if>" data-index="6">短回</li>
                                <li class="tab_li <#if viewType ==7>active_btn</#if>" data-index="7">注册认证</li>
                            </ul>
                            <div class="mTable">

                                <#list contentItem?keys as key1>
                                    <#assign userRole = contentItem[key1]>
                                    <#if key1?? && key1 != "7">
                                    <table class="table_${key1!0}" cellpadding="0" cellspacing="0" style="display:none;">
                                        <thead>
                                        <tr>
                                            <td>部门</td>
                                            <td class="sortable">目标</td>
                                            <td class="sortable">完成数</td>
                                            <td class="sortable">完成率</td>
                                            <td class="sortable active">日浮</td>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <#if userRole?size gt 0>
                                            <#list userRole?sort_by("maucDf")?reverse  as role>
                                                <tr class="js-item" data-level="1" data-id="${role.id!0}" data-views="${role.viewType!0}" data-type="${role.idType!""}">
                                                    <td>${role.name!""}</td>
                                                    <td class="2">${role.maucBudget!0}</td>
                                                    <td class="1">${role.mauc!0}</td>
                                                    <td class="3" data-info="${(role.maucCompleteRate!0) * 100}%">
                                                        <#if role.maucCompleteRate?? && role.maucCompleteRate gt 2 >
                                                            >200%
                                                        <#else>
                                                        ${(role.maucCompleteRate!0) * 100}%
                                                        </#if>
                                                    </td>
                                                    <td class="4">${role.maucDf!0}</td>
                                                </tr>
                                            </#list>
                                        </#if>
                                        </tbody>
                                    </table>
                                    <#elseif key1 == "7">
                                        <table class="table_${key1!0}" cellpadding="0" cellspacing="0" style="display:none;">
                                            <thead>
                                            <tr>
                                                <td>姓名</td>
                                                <td class="sortable">本月注册</td>
                                                <td class="sortable">本月认证</td>
                                                <td class="sortable active">昨日注册</td>
                                                <td class="sortable">昨日认证</td>
                                            </tr>
                                            </thead>
                                            <tbody>
                                                <#if userRole?size gt 0>
                                                    <#list userRole?sort_by("regStuCountDf")?reverse as role>
                                                    <tr class="js-item" data-level="1" data-id="${role.id!0}" data-views="${role.viewType!0}" data-type="${role.idType!""}">
                                                        <td>${role.name!""}</td>
                                                        <td class="1">${role.monthRegStuCount!0}</td>
                                                        <td class="2">${role.monthAuthStuCount!0}</td>
                                                        <td class="3">${role.regStuCountDf!0}</td>
                                                        <td class="4">${role.authStuCountDf!0}</td>
                                                    </tr>
                                                    </#list>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </#if>
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

<script>
    var rightText = "小学" ;
    var roleType = "${idType!""}";
    console.log(rightText)
</script>
</@layout.page>
