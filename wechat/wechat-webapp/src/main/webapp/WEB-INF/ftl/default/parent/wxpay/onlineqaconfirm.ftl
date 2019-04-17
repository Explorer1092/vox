<#import "../layout.ftl" as ucenter>
<@ucenter.page title='订单详情' pageJs="onlineqaPay">
    <@sugar.capsule css=['product'] />
<div class="main body_background_gray">
    <div class="sendBean_box" style="margin:0;border:none;">
        <div class="sb_up" style="line-height: 90px;height:187px;background-color: #f9f9f9;">
            <dl>
                <dd>
                    <p style="font-size: 26px;white-space:nowrap;overflow:hidden; text-overflow:ellipsis;"
                       class="text_blue">${productName!''}</p>
                </dd>
            </dl>
        </div>
        <div class="sb_down">
            <div class="price_content">
                <p>
                    支付金额：<strong class="text_red">￥ ${price!0}</strong>
                </p>

                <p>
                    订单编号：${orderId!0}
                </p>
            </div>
        </div>
    </div>

    <div class="foot_btn_box">
        <div data-href="/parent/wxpay/pay-onlineqa.vpage?oid=${orderId!0}" class="btn_mark btn_mark_block js-payNowBtn" style="background-color: #ff9b2f;">
            <span style="color: #FFFFFF; font-weight: normal;">立即支付</span>
        </div>
    </div>
</div>
<script>
    var productType =${json_encode(productType)};
</script>
</@ucenter.page>