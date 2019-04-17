<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语口语测试" pageJs="chipsOralTest">
    <@sugar.capsule css=["chipsOralTest"] />

<style>
    [v-cloak] { display: none }
    #chips_survey{
        height:100%;
        width:100%;
    }
</style>

<div id="chips_survey" v-cloak>
    <div class="questionWrap">
        <div class="questionHead">
            <div class="title">薯条英语[英语口语测试]</div>
            <div class="title">时间选择表</div>
            <div class="intro">为了更好的让您和孩子对于本期课程的学习效果有一个直观的了解，老师这边希望在孩子有空的时间给他做一个口语测试，请从下面的时间选择一个合适的时间</div>
        </div>
        <div class="questionMain">
            <!-- 1 unselected -->
            <div class="questionList" v-for="(item,index) in list" v-bind:class="{unselected:item.red_sign == 'red'}">
                <div class="listTitle"><i>*</i>{{ item.index + 1 }}.{{ item.title }}<span>{{ item.ps }}</span></div>
                <ul class="answerBox">

                    <li v-for="(sub_item,sub_index) in item.options" @click="select(item.index,sub_index,item.type)" style="display: flex">
                        <span class="choice" v-if="item.type != 'multi_select'" v-bind:class="{active:sub_item.is_active}"></span>
                        <span class="choice"v-if="item.type == 'multi_select'"  v-bind:class="{active:sub_item.is_active}"></span>
                        <span class="answer">{{ sub_item.text }}</span>
                    </li>
                </ul>
            </div>
        </div>
        <div class="questionSubmit" @click="submit">提交</div>
    </div>
    <!-- 提示弹窗 -->
    <div class="questionPopup" v-if="toast_status">
        <div class="tipsBox">请完成作答哦～</div>
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
    //    wx.ready(function(){
    //        wx.hideAllNonBaseMenuItem();
    //        wx.hideOptionMenu();
    //    })

    wx.ready(function () {
        // 微信朋友圈分享
        wx.onMenuShareTimeline({
            title: "薯条英语口语测试", // 分享标题
            link: "", // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户点击了分享后执行的回调函数
            }
        });

        //分享给好友
        wx.onMenuShareAppMessage({
            title: "薯条英语口语测试", // 分享标题
            desc: "请选择适合孩子测试的时间", // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            type: '', // 分享类型,music、video或link，不填默认为link
            dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });

        wx.onMenuShareQQ({
            title: "薯条英语口语测试", // 分享标题
            desc: "请选择适合孩子测试的时间", // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });

        // 分享到QQ空间
        wx.onMenuShareQZone({
            title: "薯条英语口语测试", // 分享标题
            desc: "请选择适合孩子测试的时间", // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });
    });

</script>

</@layout.page>

<#--</@chipsIndex.page>-->
