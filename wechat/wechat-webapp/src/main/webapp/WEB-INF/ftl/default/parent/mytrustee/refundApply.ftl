<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='退款详情' pageJs="refundDetail">
<@sugar.capsule css=['mytrustee','jbox'] />
<div class="mc-refundDetail mc-wrap mc-margin15">
    <table class="mc-payment" cellpadding="0" cellspacing="0">
        <tr>
            <td>商品名：</td>
            <td><p data-bind="text: goodsName"></p></td>
        </tr>
        <tr>
            <td>订单号：</td>
            <td><p data-bind="text: orderId"></p></td>
        </tr>
        <tr>
            <td>学习券：</td>
            <td><p><span data-bind="text: voucher"></span><span class="state" data-bind="text:voucherState">未激活</span></p></td>
        </tr>
        <tr>
            <td>购买数：</td>
            <td><p><span data-bind="text: count"></span>个</p></td>
        </tr>
        <tr>
            <td>退款原因：</td>
            <td><p data-bind="text: refundReason"></p></td>
        </tr>
        <tr>
            <td>退款说明：</td>
            <td><p data-bind="text: refundDesc"></p></td>
        </tr>
        <tr>
            <td>退款金额：</td>
            <td><p>￥<span data-bind="text: amount"></span>元</p></td>
        </tr>
    </table>
    <div class="mrd-list">
        <h3 class="listTop"><span class="refundState">退款状态</span><a href="tel:400-160-1717" class="tel js-callServiceBtn">咨询客服</a></h3>
        <div class="listMain" data-bind="template: {name : 'refundDetailTemplate',foreach: refundStatuses}"></div>
    </div>
</div>
<script id='refundDetailTemplate' type="text/html">
    <div class="listInfo">
        <span class="listCircle"></span>
        <span class="triangle"></span>
        <p class="title" data-bind="attr:{status: status}">
            <span class="state" data-bind="text: name"> </span>
            <span class="time" data-bind="text: time"></span>
        </p>
        <p class="intro" data-bind="text: desc"></p>
    </div>
</script>
<script>
var orderId = "${orderId!''}";
</script>
</@trusteeMain.page>