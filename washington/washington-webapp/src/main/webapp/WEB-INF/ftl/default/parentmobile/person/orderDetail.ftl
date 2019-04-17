<#import '../layout.ftl' as layout>

<#include "../testPay.ftl">

<@layout.page title="订单详情" className='OrderDetail' pageJs="second"  extraJs=extraJs![] >
    <#escape x as x?html>

        <#if result.success >
            <#assign order = result.order>

            <div class="parentApp-orderDetail">
                <div class="orderBox">
                    <div class="orderHead">${order.productName!''}</div>
                    <ul class="orderMain ">
                        <li>订单编号：<span>${order.id!''}</span></li>
                        <li>价格：<em>${order.orderPrice!''}</em>元</li>
                        <#--<li>日期：<em>${order.validPeriod!''}</em>天</li>-->
                    </ul>
                </div>
            </div>
            <div class="foot_btn_box">
                <a href="javascript:;" data-order_id="${order.genUserOrderId()!''}" data-order_price="${order.orderPrice!""}"  data-order_type = "order"  class="${doPayClassName} btn_mark btn_mark_block"><span style="color: #F9F9F9;">立即支付</span></a>
            </div>
        <#include "../transComn.ftl">
        <script>
            var wechatUrlHeader = '${wechatUrlHeader}';
        </script>
        <#else>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "../errorTemple/errorBlock.ftl">
        </#if>
    </#escape>

</@layout.page>

