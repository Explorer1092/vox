<#import "../layout.ftl" as starreward>
<@starreward.page title='领取星星学豆' pageJs="starreward">
<@sugar.capsule css=['jbox'] />
<#include "../userpopup.ftl">
<style>
    body{
        background-color: #b3d648!important;
        font: 24px/120% "微软雅黑";
        color: #4d4d4d;
    }
</style>
<div class="star-title-nav">
    <p class="stn">
        <a id="strategy_btn" href="javascript:void (0);">星星攻略</a>
        <a id="reward_rule_btn" href="javascript:void (0);">奖励规则</a>
    </p>
</div>
<div class="starBean-main">
    <div class="sea-title">
        <div class="st-bg"></div>
        <div class="st-list">
            <ul>
                <li class="sk">
                    <span data-bind="text: '上月排名奖励：'+integral()+'学豆'"></span>
                    <#--<a class="btn-s" href="javascript:void (0);" data-bind="click: getRewardMonth,attr: { data_studentid: sid() },visible: !hasReceived()">领取奖励</a>-->
                    <a class="btn-s btn-s-disable" href="javascript:void (0);" data-bind="visible: !hasReceived()">领取奖励</a>
                    <a class="btn-s btn-s-disable" href="javascript:void (0);" data-bind="visible: hasReceived()">已领取</a>
                </li>
                <li>
                <#if !isAvailableMonth!true>
                    <span>上学期排名奖励：暂无</span>
                    <a class="btn-s btn-s-disable" href="javascript:void (0);">开学当月领取</a>
                <#else>
                    <span data-bind="text: '上月排名奖励：'+integralLastTerm()+'学豆'"></span>
                    <#--<a class="btn-s" data-bind="click: getRewardTerm,attr: { data_studentid: sid() },visible: !hasReceivedLastTerm()" href="javascript:void (0);">领取奖励</a>-->
                    <a class="btn-s btn-s-disable" data-bind="visible: !hasReceivedLastTerm()" href="javascript:void (0);">领取奖励</a>
                    <a class="btn-s btn-s-disable" data-bind="visible: hasReceivedLastTerm()" href="javascript:void (0);">已领取</a>
                </#if>
                </li>
            </ul>
        </div>
    </div>
    <div class="sea-con">
        <div class="sc-tab" id="rank_but">
            <a data-rank_name="monthRank" href="javascript:void (0);" data-bind="click: showMonthTab,css: { active: currentTab() == 'month' }">本月星星榜</a>
            <a data-rank_name="termRank" href="javascript:void (0);" data-bind="click: showTermTab,css: { active: currentTab() == 'term' }">本学期星星榜</a>
        </div>
        <div class="sc-table">
            <div id="rankListBox_month" class="sl-con" data-bind="visible: currentTab() == 'month'">
                <table>
                    <thead>
                    <tr>
                        <td>目前排名</td>
                        <td>姓名</td>
                        <td>星星数量</td>
                        <td>排名奖励</td>
                    </tr>
                    </thead>
                    <tbody data-bind="foreach: mothDataAry">
                    <tr data-bind="style: { display: $parent.monthShowIndex()*10 > $index() ? 'table-row' : 'none' },attr:{'data-num': $index()}">
                        <td class="gray" data-bind="css:{'font-blue': $index() === 0},text: rank"></td>
                        <td data-bind="css:{'font-blue': $index() === 0},text: userName"></td>
                        <td class="gray" data-bind="css:{'font-blue': $index() === 0},text: star"></td>
                        <td data-bind="css:{'font-blue': $index() === 0},text: integral+'学豆'"></td>
                    </tr>
                    </tbody>
                </table>
                <div style="text-align: center; margin-top: 20px;">
                    <a class="ui-btn ui-btn-b ui-corner-all showMoreOrderBut" data-bind="style: { display: monthShowIndex()*10 < mothDataAry().length ? 'block' : 'none' },click: showMonthMore,attr:{'data-num':monthShowIndex}" href="javascript:void(0);"style="font-size:25px;">查看更多</a>
                </div>
            </div>
            <div id="rankListBox_term" class="sl-con" data-bind="visible: currentTab() == 'term'">
                <table>
                    <thead>
                    <tr>
                        <td>目前排名</td>
                        <td>姓名</td>
                        <td>星星数量</td>
                        <td>排名奖励</td>
                    </tr>
                    </thead>
                    <tbody data-bind="foreach: termDataAry">
                    <tr data-bind="style: { display: $parent.termShowIndex()*10 > $index() ? 'table-row' : 'none' },attr:{'data-num': $index()}">
                        <td class="gray" data-bind="css:{'font-blue': $index() === 0},text: rank"></td>
                        <td data-bind="css:{'font-blue': $index() === 0},text: userName"></td>
                        <td class="gray" data-bind="css:{'font-blue': $index() === 0},text: star"></td>
                        <td data-bind="css:{'font-blue': $index() === 0},text: integral+'学豆'"></td>
                    </tr>
                    </tbody>
                </table>
                <div style="text-align: center; margin-top: 20px;">
                    <a class="ui-btn ui-btn-b ui-corner-all showMoreOrderBut" data-bind="style: { display: termShowIndex()*10 < termDataAry().length ? 'block' : 'none' },click: showTermMore,attr:{'data-num':termShowIndex}" href="javascript:void(0);" style="font-size:25px;">查看更多</a>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="strategy_box" style="display: none; width: 550px;">
    <div style="padding: 20px">
        <table>
            <thead>
            <tr>
                <td>如何获得星星:</td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>1、关注家长微信每月奖励：1位家长奖10星星，2位家长奖20星星</td>
            </tr>
            <tr>
                <td>2、按时完成作业，分数更高</td>
            </tr>
            <tr>
                <td>3、邀请老师，多给学生奖励星星</td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<div id="reward_rule_box" style="display: none;width: 550px;">
    <div style="padding: 20px">
        <table>
            <thead>
            <tr>
                <td>奖励规则：</td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>1、领取条件：上月老师奖励过星星</td>
            </tr>
            <tr>
                <td>2、月度奖励次月领取，学期奖励开学领取，过期将清零</td>
            </tr>
            <tr>
                <td>3、星星数量相同时，按作业分数和完成时间进行排名</td>
            </tr>
            <tr>
                <td>4、根据星星数量排名，奖励学豆</td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

</@starreward.page>