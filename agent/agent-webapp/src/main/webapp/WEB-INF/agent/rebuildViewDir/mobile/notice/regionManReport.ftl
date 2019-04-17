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
        <i class="tip-icon icon04"></i>
        城市综合业绩
    </div>
    <div class="item itemSituation">
        <table cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <th>城市</th>
                <th>本周排名</th>
                <th>上周排名</th>
                <th>涨幅</th>
            </tr>
            </thead>
            <tbody>
            <#list weeklyReportList as sd>
                <tr>
                    <td>${sd.groupName!''}</td>
                    <td>${sd.ranking!'--'}</td>
                    <td>${sd.preWeekRanking!'--'}</td>
                    <td>
                        <span class="
                            <#if (sd.preWeekRanking!0)?number gt (sd.ranking!0)?number>
                                fontRed
                            <#elseif (sd.preWeekRanking!0)?number lt (sd.ranking!0)?number>
                                fontGreen
                            <#else>
                               default
                            </#if>">
                        ${((sd.preWeekRanking!0)?number - (sd.ranking!0)?number)!'--'}
                        </span>

                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</div>
<div class="flow">
    <div class="item tip">
        <i class="tip-icon icon05"></i>
        本周指标涨幅
    </div>
    <div class="item itemSituation">
        <table>
            <thead>
            <tr>
                <th>城市</th>
                <th>小学单活</th>
                <th>小学双活</th>
                <th>中学单活</th>
            </tr>
            </thead>
            <#list weeklyReportList as sd>
                <tr>
                    <td>${sd.groupName!''}</td>
                    <td>${sd.juniorSascFloat!'0'}%</td>
                    <td>${sd.juniorDascFloat!'0'}%</td>
                    <td>${sd.middleSascFloat!'0'}%</td>
                </tr>
            </#list>
        </table>
    </div>
</div>
</@layout.page>
