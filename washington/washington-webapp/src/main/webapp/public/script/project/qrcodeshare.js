/**
 * @author: pengmin.chen
 * @description: "生成二维码，手机扫码分享"
 * @createdDate: 2018/10/16
 * @lastModifyDate: 2018/10/16
 */

define(['jquery', 'YQ', 'qrcode'], function ($, YQ) {
    /*
    * 链接参数：
    * @param wxtip(string, true/false): 是否显示微信扫码提示, 默认为false，不显示
    * @param link(string): 二维码参数
    * */

    function init() {
        var isShowWxTip = YQ.getQuery('wxtip');
        var url = window.decodeURIComponent(YQ.getQuery('link'));
        $('#shareLink').text(url);

        // 是否显示微信扫码提示
        if (isShowWxTip === 'true') {
            $('#wxTip').show();
        }

        // 检测是否支持canvas绘制
        if (canvasSupport()) {
            $('#qrcodeBox').show();
            $('#notSupportTip').hide();

            getQrcode(url);
        } else {
            $('#wxTip').hide();
            $('#qrcodeBox').hide();
            $('#notSupportTip').show();
        }
    }

    function getQrcode(url) {
        $('#shareQrcode').qrcode({
            render: "canvas",
            width: 256, // 设置宽度
            height: 256, // 设置高度
            typeNumber: -1, // 计算模式
            correctLevel: 2, // 纠错等级
            text: url // 链接
        });
    }

    function canvasSupport() {
        return !!document.createElement('canvas').getContext;
    }

    init();
});