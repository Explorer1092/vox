define(['wx'],function(wx){
    function backToOrderList(){
        setTimeout(function(){
            location.href = wxcfg.pay.backUrl;
        });
    }
    wx.config({
        debug:  wxcfg.config.debug, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
        appId: wxcfg.config.appId,// 必填，公众号的唯一标识
        timestamp:  wxcfg.config.timestamp, // 必填，生成签名的时间戳
        nonceStr:  wxcfg.config.nonceStr, // 必填，生成签名的随机串
        signature:  wxcfg.config.signature,// 必填，签名，见附录1
        jsApiList: ['chooseWXPay'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
    });
    wx.ready(function(){
        wx.chooseWXPay({
            appId:  wxcfg.pay.appId,
            timestamp: wxcfg.pay.timestamp,
            nonceStr: wxcfg.pay.nonceStr,
            package: wxcfg.pay.package,
            signType: wxcfg.pay.signType,
            paySign: wxcfg.pay.paySign,
            success: function (data) {
                backToOrderList();
            },
            error: function (res) {
                alert('操作失败,' + res.err_msg);
                window.history.back();
            },
            fail: function (res) {
                alert('操作失败,' + res.err_msg);
                window.history.back();
            },
            cancel : function(){
                window.history.back();
            }
        });
    });
});