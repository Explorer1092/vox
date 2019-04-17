<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="周报详情" pageJs="">
    <@sugar.capsule css=['new_home','notice']/>
<style>
    body{
        background-color: #f1f2f5;
    }
</style>
<div class="res-top fixed-head">
    <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
    <span class="return-line"></span>
    <span class="res-title">周报详情</span>
</div>
<div class="flow">
    <div class="item tip">
        <i class="tip-icon icon01"></i>
        本周业绩
    </div>
    <div class="item itemTable">
        <table cellpadding="0" cellspacing="0">
            <thead>
                <tr>
                    <th>指标</th>
                    <th>本周涨幅</th>
                    <th>当前完成</th>
                </tr>
            </thead>
            <tbody>
            <#if weeklyReport??>
                <tr>
                    <td>小学单活</td>
                    <td><span class="tableNum">${weeklyReport.juniorSascFloat!0}%</span></td>
                    <td><span class="tableNum">${weeklyReport.juniorSascCompleteRate!0}%</span></td>
                </tr>
                <tr>
                    <td>小学双活</td>
                    <td><span class="tableNum">${weeklyReport.juniorDascFloat!0}%</td>
                    <td><span class="tableNum">${weeklyReport.juniorDascCompleteRate!0}%</span></td>
                </tr>
                <tr>
                    <td>中学单活</td>
                    <td><span class="tableNum">${weeklyReport.middleSascFloat!0}%</span></td>
                    <td><span class="tableNum">${weeklyReport.middleSascCompleteRate!0}%</span></td>
                </tr>
            </#if>
            <tr>
                <td colspan="3" style="width: 33.33%;">
                    综合排名: 本周 ${weeklyReport.ranking!'--'} ,上周 ${weeklyReport.preWeekRanking!'--'}
                    <#if weeklyReport.ranking?has_content && weeklyReport.preWeekRanking?has_content>
                        <#if (weeklyReport.preWeekRanking!0)?number gt (weeklyReport.ranking!0)?number>
                            ,上升 <span class="upSub fontRed">${(weeklyReport.preWeekRanking!0)?number - (weeklyReport.ranking!0)?number}</span> 名
                        <#elseif (weeklyReport.preWeekRanking!0)?number lt (weeklyReport.ranking!0)?number>
                            ,下降 <span class="declineSub fontGreen">${(weeklyReport.ranking!0)?number - (weeklyReport.preWeekRanking!0)?number}</span> 名
                        <#else>
                            ,排名未变化
                        </#if>
                    </#if>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<div class="flow">
    <div class="item tip">
        <i class="tip-icon icon02"></i>
        本周陪访建议(${visitSuggest?size})
    </div>
    <#if visitSuggest?? && visitSuggest?size gt 0>
        <#list visitSuggest as vs>
            <div class="item acc-visitMain">
                <div class="acc-person">
                    陪访人：${vs.workName!''}
                </div>
                <div class="acc-date">
                    ${vs.date!''}
                </div>
                <div class="acc-content">
                    <div>
                        陪访学校：${vs.schoolName!''}
                    </div>
                    <div class="column">
                        <div>陪访建议：</div>
                        <div>
                            <p>${vs.partnerSuggest!''}</p>
                        </div>
                    </div>
                </div>
            </div>
        </#list>
    </#if>
</div>
</@layout.page>