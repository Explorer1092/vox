<#import "../layout.ftl" as pay>
<@pay.page title="订单详情" pageJs="pay">
<div style="text-align: center; padding: 50px 0;">跳转中...</div>
    <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv())>
    <div>
        <h2>订单号: ${oid}</h2>
        <div>
            <button id="btn_pay_for_test" class="btn btn-blue" style="width:100px;height:40px;" onclick="pay_fail()">
                模拟支付
            </button>
            <a id="lnk_next" href="#">下一步</a>
        </div>
        <div>
            <h2>支付结果:</h2>
            <div id="div_pay_fail">支付失败测试:</div>
            <div id="div_pay_success">支付成功测试:</div>
            <div id="div_pay_repeat">支付重发测试:</div>
        </div>
    </div>

    <script type="text/javascript">
        //发送支付失败的通知
        function pay_fail() {
            $('#btn_pay_for_test').attr('disabled', "true");

            var orderId = '${oid}';

            $.post('/parent/wxpay/payfortest-fail.vpage', {oid: orderId}, function (data) {
                console.log(data);
                if (data.success) {
                    $('#div_pay_fail').html('支付失败测试:OK');
                    pay_success();
                } else {
                    $('#div_pay_fail').html('支付失败测试:FAIL');
                }
            });
        }
        //发送支付成功的通知
        function pay_success() {
            var orderId = '${oid}';

            $.post('/parent/wxpay/payfortest-success.vpage', {oid: orderId}, function (data) {
                console.log(data);

                if (data.success) {
                    $('#div_pay_success').html('支付成功测试:OK');
                    $('#lnk_next').attr('href', data.url);

                    pay_repeat();
                } else {
                    $('#div_pay_success').html('支付成功测试:FAIL');
                }
            });
        }
        //重发支付成功的通知
        function pay_repeat() {
            var orderId = '${oid}';

            $.post('/parent/wxpay/payfortest-repeat.vpage', {oid: orderId}, function (data) {
                console.log(data);

                if (data.success) {
                    $('#div_pay_repeat').html('支付重发测试:OK');
                } else {
                    $('#div_pay_repeat').html('支付重发测试:FAIL');
                }
            });
        }
    </script>
    <#elseif test!false>
    <script type="text/javascript">
        var wxcfg = {
            "config": {
                debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                appId: '${appid!0}',// 必填，公众号的唯一标识
                timestamp: ${config_timestamp!0}, // 必填，生成签名的时间戳
                nonceStr: '${config_nonceStr!0}', // 必填，生成签名的随机串
                signature: '${config_signature!0}',// 必填，签名，见附录1
                jsApiList: ['chooseWXPay'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
            },
            "pay": {
                appId: '${appid!0}',
                timestamp: '${timeStamp!0}',
                nonceStr: '${nonceStr!0}',
                package: '${package!0}',
                signType: '${signType!0}',
                paySign: '${sign!0}',
                backUrl: '${backUrl!''}'
            }
        };
    </script>
    <#else>
    <script type="text/javascript">
        var wxcfg = {
            "config": {
                debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                appId: '${appid!0}',// 必填，公众号的唯一标识
                timestamp: ${config_timestamp!0}, // 必填，生成签名的时间戳
                nonceStr: '${config_nonceStr!0}', // 必填，生成签名的随机串
                signature: '${config_signature!0}',// 必填，签名，见附录1
                jsApiList: ['chooseWXPay'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
            },
            "pay": {
                appId: '${appid!0}',
                timestamp: '${pay_timestamp!0}',
                nonceStr: '${pay_nonceStr!0}',
                package: '${pay_package!0}',
                signType: '${pay_signType!0}',
                paySign: '${pay_paySign!0}',
                backUrl: '${pay_backUrl!''}'
            }
        };
    </script>
    </#if>
</@pay.page>