<#import "../layout.ftl" as layout>
<@layout.page title="电子教材目录" pageJs="chipsBookCatalog">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{display:none;}
</style>

<div class="catalogWrap" id="bookcatalog" v-cloak="">
    <div class="catalogTitle">课程目录</div>
    <ul class="catalogBox">
        <li v-for="(item,index) in list" @click="open(item.id)" :key="index">
            <span class="day">{{ item.tip }}</span>
            <span class="title">{{ item.name }}</span>
        </li>
        <#--<li>-->
            <#--<span class="day">Day 2</span>-->
            <#--<span class="title">去收拾行李吧去收拾行李吧</span>-->
        <#--</li>-->

    </ul>
    <!--  toast弹窗  -->
    <div class="scene-tip" v-if="toast" style="text-align:center;font-size: 0.65rem;width: 70%;background: #242424;margin: 0 auto;border-radius: 0.3rem;color: #fff;padding: 1rem;position: absolute;top: 50%;left: 50%;box-sizing: border-box;transform: translate(-50%,-50%);z-index: 999;">
        <p class="scene-tip-txt">{{ toast_txt }}</p>
    </div>
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

