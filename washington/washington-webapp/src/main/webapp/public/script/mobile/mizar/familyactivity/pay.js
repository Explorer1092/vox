/**
 * @author xinqiang.wang
 * @description "支付"
 * native支付接口[payType数字类型:【1:微信 2：支付宝】]
 * @createDate 2016/10/31
 */

define(['jquery', "weui", 'voxLogs'], function ($) {
    //选择支付方式
    var payWay = 'zfb';
    $('#payWayBox').find('a').on('click', function () {
        var that = $(this);
        that.addClass('active').siblings('a').removeClass('active');
        payWay = that.data('way');
    });

    //支付
    $('#submitBtn').on('click', function () {
        var payType = 2;
        if (payWay == 'zfb') {
            payType = 2;
        } else if (payWay == 'wx') {
            payType = 1;
        }

        $.post('/mizar/familyactivity/order.vpage', {
            activityId: payMapper.actId,
            dp: payMapper.dp,
            item: payMapper.itemId
        }, function (data) {
            if (data.success) {
                if (window['external'] && window.external['payOrder']) {
                    window.external.payOrder(JSON.stringify({
                        orderId: data.orderId,
                        orderType: "family",
                        payType: payType
                    }));
                } else {
                    $.alert("跳转支付失败！");
                }
            } else {
                $.alert(data.info);
            }
        }).fail(function () {
            $.toast("数据异常！");
        });

        YQ.voxLogs({
            database: 'parent',
            module: 'm_BqyGPVoT',
            op: "o_FPi2tsKG",
            s0: payMapper.actId,
            s1: payMapper.price
        });
    });
});