<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="基本情况" pageJs="schoolOverView" navBar="hidden">
    <@sugar.capsule css=['home']/>
    <#macro noneDataBlock>
    <tr>
        <td colspan="3" style="text-align: center;">
            暂无
        </td>
    </tr>
    </#macro>
<style>
    #container div div{background:#ebebeb;padding-bottom:0.2rem;}
</style>
<div class="primary-box">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">基本情况</span>
        <a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="color:#636880"><#if maucOrDf == 1>本月<#else>昨日</#if></a></div>
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;top:9%;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li class="tab_row" style="padding:.2rem 0"><span id="thisMonth" class="thisMonthShow <#if maucOrDf == 1 >active</#if>" style="display:inline-block;text-align:center;height:2.5rem;line-height:2.5rem;width:100%;font-size: .75rem;border-bottom: .05rem solid #cdd3d3"  href="javascript:void(0);" data-index="0" >本月</span></li>
            <li class="tab_row" style="padding:.2rem 0"><span id="lastDayBtn" class="lastDayBtn lastDayShow <#if maucOrDf == 2 >active</#if>" style="height:2.5rem;line-height:2.5rem;text-align:center;display:inline-block;width:100%;font-size: .75rem;"   href="javascript:void(0);" data-index="1" >昨日</span></li>
        </ul>
    </div>

    <div class="body_color">
        <div id="month" style="display: none;">
            <div class="c-opts gap-line">
                <span id="monthTab_JUNIOR" class="js-monthTab active" data-viewtype="JUNIOR">小学</span>
                <span id="monthTab_MIDDLE" class="js-monthTab" data-viewtype="MIDDLE">中学</span>
            </div>
            <div class="pr-list pr-fake">
                <#if overviewData??>
                    <#list overviewData as group>
                        <ul id="month_ul_${group.viewType}" class="pr-ulist">
                            <div class="overview-list">
                                <table cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td>
                                            <div>${group.stuScale!0}</div>
                                            <div>规模</div>
                                        </td>
                                        <td>
                                            <div>${group.regStuNum!0}</div>
                                            <div>累计注册</div>
                                        </td>
                                        <td>
                                            <div>${group.authStuNum!0}</div>
                                            <div>累计认证</div>
                                        </td>
                                        <td>
                                            <#assign authRate = group.authRate * 100>
                                            <div>${authRate!0}%</div>
                                            <div>渗透率</div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td>
                                            <div>${group.monthRegStuNum!0}</div>
                                            <div>本月注册</div>
                                        </td>
                                        <td>
                                            <div>${group.monthAuthStuNum!0}</div>
                                            <div>本月认证</div>
                                        </td>
                                        <td></td>
                                    </tr>
                                </table>
                            </div>
                        </ul>
                    </#list>
                </#if>
            </div>
            <div class="pr-side">
                <div>
                    <#if schoolOverviewViewMap??>
                        <#list schoolOverviewViewMap?keys as key>
                            <#assign contentItem = schoolOverviewViewMap[key]>
                            <#if contentItem??>
                                <div id="month_table_${key}">
                                    <table class="sideTable">
                                        <thead>
                                            <tr><td style="width:47%">学校</td><td class="sortable">本月注册<i></i></td><td class="sortable">本月认证<i></i></td></tr>
                                        </thead>
                                        <tbody>
                                            <#list contentItem as item>
                                                <tr data-idtype="${item.idType!0}" data-id="${item.id!0}" data-viewtype="${item.viewType}"><td style="width:47%">${item.name!'--'}</td><td class="2">${item.monthRegStuNum!'--'}</td><td>${item.monthAuthStuNum!'--'}</td><td style="width:3%;padding-right:.75rem">></td></tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </#if>
                        </#list>
                    <#else>
                        暂无
                    </#if>
                </div>
            </div>
        </div>

        <div id="lastDay" style="display: none;">
            <div class="c-opts gap-line">
                <span id="lastDayTab_JUNIOR" class="js-lastDayTab active" data-viewtype="JUNIOR">小学</span>
                <span id="lastDayTab_MIDDLE" class="js-lastDayTab" data-viewtype="MIDDLE">中学</span>
            </div>
            <div class="pr-list pr-fake">
                <#if overviewData??>
                    <#list overviewData as group>
                        <ul id="lastDay_ul_${group.viewType}" class="pr-ulist">
                            <div class="overview-list">
                                <table cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td></td>
                                        <td>
                                            <div>${group.dayRegStuNum!0}</div>
                                            <div>昨日注册</div>
                                        </td>
                                        <td>
                                            <div>${group.dayAuthStuNum!0}</div>
                                            <div>昨日认证</div>
                                        </td>
                                        <td></td>
                                    </tr>
                                </table>
                            </div>
                        </ul>
                    </#list>
                </#if>
            </div>
            <div id="container" style="text-align:center;background:#fff;padding-bottom:1rem;display:none">
                <div id="container_JUNIOR" style="height:10rem;width:17.75rem;margin:0 auto;"></div>
                <div id="container_MIDDLE" style="height:10rem;width:17.75rem;margin:0 auto;"></div>
            </div>
            <div class="pr-side">
                <div>
                    <#if schoolOverviewViewMap??>
                        <#list schoolOverviewViewMap?keys as key2>
                            <#assign contentItem = schoolOverviewViewMap[key2]>
                            <#if contentItem??>
                                <div id="lastDay_table_${key2}">
                                    <table class="tableSort sideTable">
                                        <thead>
                                            <tr><td style="width:47%">学校</td><td class="sortable">昨日注册<i></i></td><td class="sortable">昨日认证<i></i></td></tr>
                                        </thead>
                                        <tbody>
                                            <#list contentItem as item>
                                                <tr data-idtype="${item.idType!0}" data-id="${item.id!0}" data-viewtype="${item.viewType}"><td style="width:47%">${item.name!'--'}</td><td class="2">${item.dayRegStuNum!'--'}</td><td>${item.dayAuthStuNum!'--'}</td><td style="width:3%;padding-right:.75rem">></td></tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                            <#else>
                                暂无
                            </#if>
                        </#list>
                    <#else>
                        暂无
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>
<script src="/public/rebuildRes/lib/echarts/echarts.min.js"></script>
<script>
    var maucOrDf = ${maucOrDf!1};
    var activeType = '${viewType?string!'JUNIOR'}';
    var idType = '${idType!''}';
    var groupRoleType = '${groupRoleType!''}';
    var id = ${id!0};
</script>
</@layout.page>