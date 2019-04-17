<#import "../layout.ftl" as ucenter>
<@ucenter.page title='支付' pageJs="">
<div class="main body_background_gray">
    <div class="sendBean_box">
        <div class="sb_down">
            <div class="price_content">
                <p>
                    订单编号：${orderId!0}
                </p>
                <p>
                    学豆：<strong class="text_red">${integral!} </strong> 个
                </p>
                <p>
                    价格：<strong class="text_red">${price!0} </strong> 元
                </p>
            </div>
        </div>
    </div>
    <div class="foot_btn_box">
        <a class="btn_mark btn_mark_block" href="/parent/wxpay/pay-integral.vpage?oid=${orderId!0}">立即支付</a>
    </div>
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'smart',
                op: 'smart_click_pay_integral'
            })
        })
    }
</script>
</@ucenter.page>