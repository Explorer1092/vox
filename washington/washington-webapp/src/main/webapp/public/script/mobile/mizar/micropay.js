/**
 * @author huihui.li
 * @description "微课堂支付"
 * @createDate 2016/12.21
 */

define(['jquery', 'knockout','YQ', 'weui', 'voxLogs'], function ($, ko, yq) {

    $(document).on("click", ".JS-submit[data-type='pay']", function () {
        var $this = $(this);

        var data = {
            id: yq.getQuery("id"),
            payAll: yq.getQuery("payAll"),
            comment:$.trim($('#remark').val()),
            track:yq.getQuery("track"),
            refer:yq.getQuery("refer")
        };
        $.post('order.vpage', data, function (res) {
            if (res.success) {
                if (window['external'] && window.external['payOrder']) {
                    window.external.payOrder(JSON.stringify({
                        orderId: res.orderId,
                        orderType: "order",
                        payType: 1
                    }));
                } else {
                    if (isWeChat()) {
                        var wechatPayLink = window.location.protocol +'//'+ weChatLinkHost + '/parent/wxpay/pay-order.vpage?oid=' + res.orderId;
                        location.href = wechatPayLink;
                    } else {
                        $.alert("跳转支付失败！");
                    }
                }
            } else {
                $.alert(res.info);
            }
        });
    });
    function isWeChat(){
        return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") != -1);
    }

    if(isWeChat()){
        function onBridgeReady(){
            WeixinJSBridge.call('hideOptionMenu');
        }

        if (typeof WeixinJSBridge == "undefined"){
            if( document.addEventListener ){
                document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
            }else if (document.attachEvent){
                document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
            }
        }else{
            onBridgeReady();
        }
    }
});