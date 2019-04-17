<#import "../../module.ftl" as module>
<@module.page
title="我的收入"
pageJsFile={"revenue" : "public/script/basic/revenue"}
pageJs=["revenue"]
leftMenu="我的收入"
>
<div class="op-wrapper clearfix" style="margin-top:25px">
    <form id="filter-form" action="index.vpage" method="get">
        <div  class="item time-region">
            <div>
                <div class="time-select">
                    <p>查询月份</p>
                    <input hidden id="month" value="${month!}" name="month" autocomplete="off" class="v-select" style="margin-left:15px" placeholder="请选择月份"  />
                    <select name="" id="monthDate">
                        <#if monthList?? && monthList?size gt 0>
                            <#list monthList as monthDate>
                                <option value="${monthDate}" <#if monthDate == month>selected</#if>>${monthDate?date("yyyyMM")?string("yyyy-MM")}</option>
                            </#list>
                        </#if>
                    </select>
                </div>
            </div>
        </div>
        <div  class="item">
            <a class="blue-btn submit-search" style="float: right;" href="javascript:void(0)">搜索</a>
            <#--<p style="float: right;clear:both;margin-top:15px">最新数据截止日期：${startDate!}</p>-->
        </div>
    </form>
</div>

<div class="settlementIncome-box">
    <div class="income-title">全部学校</div>
    <div class="income-list">
        <ul>
            <li>
                <div class="sub">本月分成</div>
                <div class="num">${totalPayment!0}</div>
            </li>
            <#--<li>-->
                <#--<div class="sub">本月交易额</div>-->
                <#--<div class="num">${totalAmount!0}</div>-->
            <#--</li>-->
        </ul>
    </div>
</div>
<div class="settlementIncome-box">
    <div class="income-title">各校明细<i></i></div>
    <div class="income-arrow" style="display:none;">
        <a href="javascript:void(0);" class="close_btn"></a>
        <div>提成算法：</div>
        <div>1、有服务期的产品，提成计算按照服务期时间分月摊销计算；无服务期的产品，提成一次性计算。</div>
        <div>2、本月分成=本月运营收入×分成比例－退款订单扣除分成；本月运营收入包括各月订单在本月摊销的金额及本月一次性消费金额。</div>
    </div>
    <#if settlementList?? && settlementList?size gt 0>
        <div class="income-column">
            <#list settlementList as settlement>
                <div class="subDetails">
                    <div class="left">
                        <div class="city">${settlement.schoolName!}</div>
                        <div class="commission">本月分成（元）</div>
                        <div class="txt"><span>${settlement.payment!0}</span> = (${settlement.basicSettlementAmount!0}*${(settlement.paymentRate!0) * 100}%)-${settlement.refundAmortizeAmount!0}</div>
                    </div>
                    <div class="right">
                        <ul>
                            <li>
                                <div class="item">本月运营收入</div>
                                <div class="info"></div>
                                <div class="num">${settlement.basicSettlementAmount!0}</div>
                            </li>
                            <li>
                                <div class="item">退款订单扣除分成</div>
                                <div class="info"></div>
                                <div class="num">${settlement.refundAmortizeAmount!0}</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </#list>
        </div>
    </#if>
</div>

</@module.page>