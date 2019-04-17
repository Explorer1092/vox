<#import "../layout.ftl" as ucenter>
<@ucenter.page title='订单确认' pageJs="trusteePay">
    <@sugar.capsule css=['trustee', 'jbox'] />
    <div class="order-details">
        <div class="order-a">
            <div class="od-hd">
                <h2>请核实订单信息及金额</h2>
            </div>
            <div class="od-mn">
                <p class="num"><span class="tit">订单编号： </span><span>${orderId!0} <span></span></span></p>
                <p class="price"><span class="p-tita tit">折后价</span><span class="p-num">${price!0} 元</span></p>
                <p class="type"><span class="tit">所选类型： </span><span>${(trusteeType.description)!'---'}</span></p>
            </div>
        </div>
        <div class="order-footer">
            <a href="javascript:void(0);" class="pay-btn js-payNowBtn" data-oid="${orderId!0}">立即支付</a>
        </div>
    </div>
<script>
    var trusteeType =${json_encode(trusteeType)};
</script>
</@ucenter.page>