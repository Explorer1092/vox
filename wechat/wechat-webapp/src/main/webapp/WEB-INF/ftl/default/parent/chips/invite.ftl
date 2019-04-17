<#import "../layout.ftl" as layout>
<@layout.page title="邀请" pageJs="chipsInvite">
    <@sugar.capsule css=['chips'] />

<style>
    .shareWrap{
        background: #dddddd;
        padding: 1.5rem;
        box-sizing: border-box;
    }
    .shareWrap .shareInner .shareBox{
        position: relative;
        box-shadow: 5px 5px 10px #bbb;
    }
    .shareWrap .shareInner {
        padding: 0;
        margin: 0;
    }
    #canvas{
        position: absolute;
        top: 0;
        opacity: 0;
    }
    .shareWrap .shareInner .shareBox .bgImg{
        position: relative;
        z-index: 99;
    }
    .footBtn{
        z-index: 9999;
    }
    .footInner{
        z-index: 9999;
    }
</style>
<div class="shareWrap">
    <div class="shareInner">
        <div class="shareBox">
            <img class="bgImg" src="/public/images/parent/chips/invite_bg.png" alt="image">
            <canvas id="canvas" data-qrcode="" data-nickname = "${nickName ! ''}"></canvas>
        </div>
        <div class="footBtn">
            <div class="footInner">
                <span>长按图片保存发送给你的朋友</span>
            </div>
        </div>
    </div>
    <img style="opacity: 0;position: absolute;top: 0;height: 0;width: 0;" src="/chips/qrcode.vpage?url=${url !''}" id="qrcodeImg">
</div>

<script src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script type="text/javascript">
    /*
     * 注意：
     * 1. 所有的JS接口只能在公众号绑定的域名下调用，公众号开发者需要先登录微信公众平台进入“公众号设置”的“功能设置”里填写“JS接口安全域名”。
     * 2. 如果发现在 Android 不能分享自定义内容，请到官网下载最新的包覆盖安装，Android 自定义分享接口需升级至 6.0.2.58 版本及以上。
     * 3. 常见问题及完整 JS-SDK 文档地址：http://mp.weixin.qq.com/wiki/7/aaa137b55fb2e0456bf8dd9148dd613f.html
     *
     * 开发中遇到问题详见文档“附录5-常见错误及解决办法”解决，如仍未能解决可通过以下渠道反馈：
     * 邮箱地址：weixin-open@qq.com
     * 邮件主题：【微信JS-SDK反馈】具体问题
     * 邮件内容说明：用简明的语言描述问题所在，并交代清楚遇到该问题的场景，可附上截屏图片，微信团队会尽快处理你的反馈。
     */
    wx.config({
        debug: false,
        appId: '${appid!""}',
        timestamp:'${timestamp!""}',
        nonceStr: '${nonceStr!""}',
        signature: '${signature!""}',
        jsApiList: [
            'checkJsApi',
            'onMenuShareTimeline',
            'onMenuShareAppMessage',
            'onMenuShareQQ',
            'onMenuShareWeibo',
            'onMenuShareQZone',
            'hideMenuItems',
            'showMenuItems',
            'hideAllNonBaseMenuItem',
            'showAllNonBaseMenuItem',
            'translateVoice',
            'startRecord',
            'stopRecord',
            'onVoiceRecordEnd',
            'playVoice',
            'onVoicePlayEnd',
            'pauseVoice',
            'stopVoice',
            'uploadVoice',
            'downloadVoice',
            'chooseImage',
            'previewImage',
            'uploadImage',
            'downloadImage',
            'getNetworkType',
            'openLocation',
            'getLocation',
            'hideOptionMenu',
            'showOptionMenu',
            'closeWindow',
            'scanQRCode',
            'chooseWXPay',
            'openProductSpecificView',
            'addCard',
            'chooseCard',
            'openCard',
            'hideAllNonBaseMenuItem'
        ]
    });
    wx.ready(function(){
        wx.hideAllNonBaseMenuItem();
        wx.hideOptionMenu();
    })

</script>

</@layout.page>



