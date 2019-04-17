<#if (ftlmacro.devTestSwitch)!false>
    <style type="text/css">
        .payment-testBox{ display: none;}
        .payment-testBox h2{ text-align: center;  padding: 20px 0; color: #f00;}
        .payment-testBox h2 .info{ color: #00cc33;}
        .payment-testBox .button-box{text-align: center;}
        .payment-testBox button{ font-size: 18px; padding: 10px; cursor: pointer; border-radius: 8px; outline: none;}
    </style>
    <div class="payment-testBox" id="paymentTestBox">
        <h2>支付模拟测试！ <span class="info" id="testBoxInfo"></span></h2>
        <div class="button-box">
            <button class="fail" data-ajax_type="fail" data-order_id="${(orderId)!0}">失败</button>
            <button class="success" data-ajax_type="success" data-order_id="${(orderId)!0}">成功</button>
            <button class="repeat" data-ajax_type="repeat" data-order_id="${(orderId)!0}">重复</button>
            <button class="back" data-ajax_type="back" data-order_id="${(orderId)!0}">返回游戏</button>
        </div>
        <div class="returnUrl"></div>
    </div>
    <script type="text/javascript">
        var testLinkFiles =  "${(hasAfentiPay)?string('/api/1.0/afenti/', '/apps/order/')}";

        <#--可在当前页面配置调用JS and CSS 模块;-->
        requirePaths.test = "${layout.getVersionUrl("public/script/paymentmobile/test", ".js")}";
        pageRunJs.push("test");
    </script>
</#if>