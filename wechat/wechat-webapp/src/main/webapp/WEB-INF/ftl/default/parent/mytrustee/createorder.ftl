<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='提交订单' pageJs="mytrusteecreateorder">
<@sugar.capsule css=['mytrustee','jbox'] />
<div class="orderConfirm mc-wrap mc-margin15">
    <div class="mc-confirmList mcList-box">
        <div class="title">已购服务</div>
        <ul>
            <li>
                <p class="js-goodName">${detail.name!""}</p>
                <p>
                <#if detail.tags?? && detail.tags?size gt 0>
                    <#list detail.tags as tag>
                        <span>${tag!""}</span>
                    </#list>
                </#if>
                </p>
            </li>
        </ul>
    </div>
    <div class="mc-confirmList mcList-box">
        <div class="title">已购服务时长</div>
        <ul>
            <#if detail.type == "experience">
                <li data-type="experience">购买时长：<span class="fr-txt mc-txtOrange"><span class="js-expMaxPeriodNum">${detail.maxPeriodNum!""}</span>天体验</span></li>
                <li>课程单价：<span class="fr-txt">￥<span class="js-expPrice">${detail.price!""}</span>元/${detail.maxPeriodNum!""}天(${detail.maxPeriodNum!""}次)</span></li>
            </#if>
            <#if detail.type == "common">
                <li data-type="common">
                    <table>
                        <tr>
                            <td class="td-left"><p>购买时长（月）：</p><p class="ft-18">为最大力度保障您的权益，一次最多购买${detail.maxPeriodNum!""}个月</p></td>
                            <td class="td-right">
                                <span class="reduce js-reduceMonth"></span>
                                <input type="text" value="${oid_period!"1"}" class="ipt" maxlength="2" id="buyMonth">
                                <span class="add js-addMonth"></span>
                            </td>
                        </tr>
                    </table>
                </li>
                <li>课程单价：<span class="fr-txt">￥<span class="js-singlePrice">${detail.price!""}</span>元/月</span></li>
                <li>订单合计：<span class="fr-txt"><span class="js-totalPrice">${detail.price!""}</span>元</span></li>
            </#if>

        </ul>
    </div>
    <div class="mc-confirmList mcList-box">
        <ul>
            <li>手机号码：<input type="tel" value="${oid_mobile!''}" class="fr-txt mc-txtBlue tel" maxlength="11" id="orderMobile" placeholder="请输入手机号"/></li>
        </ul>
    </div>
    <div class="mc-confirmList mcList-box">
        <div class="title">详细说明</div>
        <ul>
            <li><div class="intro">${detail.desc!""}</div></li>
        </ul>
    </div>
    <input type="hidden" value="${oid_mobile!""}" id="perMobile"/>
    <div class="mc-confirmFooter">
        <div class="posFix">
            <div class="pf-l">总价：<span class="js-totalPrice">${detail.price!""}</span>元</div>
            <div class="pf-r"><a href="javascript:void(0)" class="js-payAndBuy" data-bid="${branchId!0}">确认并支付</a></div>
        </div>
    </div>
    <input type="hidden" value="${detail.maxPeriodNum!'1'}" id="maxPeriodNum"/>
</div>

</@trusteeMain.page>