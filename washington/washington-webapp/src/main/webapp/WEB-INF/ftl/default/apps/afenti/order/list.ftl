<div class="main">
    <!--order-->
    <div class="payMainBox">
        <div class="curaddress">我的订单</div>
        <!--pay-->
        <#if orders?size lt 1 >
            <div style="padding: 50px 0 70px; color: #666; font-size: 14px;" class="text_center">
                暂无订单！<a href="/apps/afenti/order/basic-cart.vpage?refer=noOrder" target="_blank">立即购买冒险岛</a>
            </div>
        </#if>
        <#list orders![] as order>
        <div class="tabbox">
            <table style="width:100%;">
                <thead>
                <tr>
                    <td>订单号 ${order.id}</td>
                    <th width="100">周期</th>
                    <th width="100">价格</th>
                    <th width="180">有效期</th>
                    <th width="130">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>${order.productName}</td>
                    <th><#if order.validPeriod?has_content>${order.validPeriod}天</#if></th>
                    <th>${order.totalPrice}<#if (order.productServiceType!'') == 'VoxAppPay'>作业币<#else>元</#if>
                    </th>
                    <th>
                        <#if (order.payStatus == 'Paid')!false>
                            <#if (order.activateStatus!'') == 'Activated' >
                                <#if order.serviceEndDatetime?? && order.serviceEndDatetime gt .now>
                                    <#assign action="valid" />
                                    <p>${(order.serviceStartDatetime?string('yyyy-MM-dd'))!} 至<br>${(order.serviceEndDatetime?string('yyyy-MM-dd'))!}</p>
                                <#else>
                                    <#assign action="renew" />
                                    <p class="clrred">${(order.serviceStartDatetime?string('yyyy-MM-dd'))!} 至<br>${(order.serviceEndDatetime?string('yyyy-MM-dd'))!}<br />(已过期)</p>
                                </#if>
                            <#else>
                                <#if (order.productServiceType!'') == 'VoxAppPay'>
                                    <#assign action="activate" />
                                    <p class="clrred">-</p>
                                <#else>
                                    <#assign action="activate" />
                                    <p class="clrred">未激活</p>
                                </#if>
                            </#if>
                        <#else>
                            <#assign action="pay" />
                            <p class="clrred">未支付</p>
                        </#if>
                    </th>
                    <th>
                        <#if action == 'valid'>
                            <span class="getGreen">已开通</span>
                        <#elseif action == 'renew'>
                            <#if (order.productServiceType!'') == 'AfentiBasic' >
                                <a href="/apps/afenti/order/basic-cart.vpage" target="_blank" class="publicBtn orangeBtn"><i class="lB"></i><i class="tB"><span>续 费</span></i><i class="rB"></i></a>
                            <#elseif (order.productServiceType!'') == 'AfentiTalent'>
                                <a href="/apps/afenti/order/talent-cart.vpage" target="_blank" class="publicBtn orangeBtn"><i class="lB"></i><i class="tB"><span>续 费</span></i><i class="rB"></i></a>
                            <#else>
                                <a href="/apps/afenti/order/exam-cart.vpage" target="_blank" class="publicBtn orangeBtn"><i class="lB"></i><i class="tB"><span>续 费</span></i><i class="rB"></i></a>
                            </#if>
                        <#elseif action == 'pay'>
                            <a href="/apps/afenti/order/confirm.vpage?orderId=${order.id}"  target="_blank" data-order_id="${order.id}" data-product_type="${order.productServiceType!}" class="publicBtn orangeBtn payOrder"><i class="lB"></i><i class="tB"><span>支 付</span></i><i class="rB"></i></a>
                            <#-- 暂时不考虑取消订单，好像没什么必要 <a href="javascript:void(0);" class="publicBtn blueBtn"><i class="lB"></i><i class="tB"><span>取 消</span></i><i class="rB"></i></a> -->
                        </#if>
                    </th>
                </tr>
                </tbody>
            </table>
        </div>
        </#list>
    </div>
    <!--//-->
</div>
<@app.css href="public/skin/project/afenti/css/order.css" version="1.0.0" />