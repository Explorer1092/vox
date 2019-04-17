<#import "../layout.ftl" as layout>
<@layout.page title="${sharePrimeTitle!}"  pageJs="renewPreviewV1">
    <@sugar.capsule css=["chipsTodayStudy"] />
<style>
[v-cloak]{
    display: none;
}
table.mark-report-table {
    text-align: center;
    border: 2px rgb(133, 133, 133) solid;
    font-size: 0.5rem;
    border-collapse: collapse;
    width: 100%;
}
table.mark-report-table .thead {
    background: rgb(231, 237, 255);
    font-weight: bold;
}
table.mark-report-table td {
    border-right: 0.5px rgb(156, 156, 156) solid;
    border-bottom: 0.5px rgb(156, 156, 156) solid;
    padding: 0.3rem 0.8rem;
}
table.mark-report-table td:last-child {
    border-right: none;
}
table.mark-report-table tr:last-child td {
    border-bottom: none;
}
</style>

<div class="today_study" v-cloak id="today_study_normal">
    <div class="container">
        <div class="header">
            <p>{{pageTitle}}</p>
        </div>
        <template v-for="(item, i) in topItemList">
                <div v-if="item.type === 'text' && item.value!==''" class="control-group">
                    <div class="dialog_box">
                        <div class="portrait">
                            <div class="img">
                                <img v-bind:src="teacher.headPortrait" alt="portrait">
                            </div>
                            <p>{{ teacher.name }}</p>
                        </div>
                        <div class="dialog_box_container dialog_box_iconArrow">
                            <div class="content">
                                <p v-html="item.value"></p>
                            </div>
                        </div>
                    </div>
                </div>
                <div v-if="item.type === 'audio' && item.value!==''" class="control-group">
                        <div class="dialog_box">
                            <div class="portrait">
                                <div class="img">
                                    <img v-bind:src="teacher.headPortrait" alt="portrait">
                                </div>
                                <p>{{ teacher.name }}</p>
                            </div>
                            <div class="audioBox iconRedDot" v-on:click="playAudio(item.value,i,$event)">
                                <span class="icon_play "></span>
                                <span  v-bind:id="'top_audioTime_'+i" class="audio-time" v-text ="item.value == ''? 0+' `' : ''"></span>
                                <audio v-bind:id="'top_audioTag_'+i" class="audio" v-bind:src="item.value" v-on:loadedmetadata="loadDurationTop(i)" preload="auto"></audio>
                            </div>
                        </div>
                    </div>
        </template>
        <template v-for="(wpItem, j) in weekPointList">
            <template v-for="(item, i) in wpItem.wpItemList">
                <div v-if="item.type === 'text' && item.value!==''" class="control-group">
                    <div class="dialog_box">
                        <div class="portrait">
                            <div class="img">
                                <img v-bind:src="teacher.headPortrait" alt="portrait">
                            </div>
                            <p>{{ teacher.name }}</p>
                        </div>
                        <div class="dialog_box_container dialog_box_iconArrow">
                            <div class="content">
                                <p v-html="item.value"></p>
                            </div>
                        </div>
                    </div>
                </div>
                <div v-if="item.type === 'audio' && item.value!==''" class="control-group">
                    <div class="dialog_box">
                        <div class="portrait">
                            <div class="img">
                                <img v-bind:src="teacher.headPortrait" alt="portrait">
                            </div>
                            <p>{{ teacher.name }}{{i}}</p>
                        </div>
                        <div class="audioBox iconRedDot" v-on:click="playAudio(item.value,i,$event)">
                            <span class="icon_play "></span>
                            <span  v-bind:id="j + 'wp_audioTime_'+i" class="audio-time" v-text ="item.value == ''? 0+' `' : ''"></span>
                            <audio v-bind:id="j + 'wp_audioTag_'+i" class="audio" v-bind:src="item.value" v-on:loadedmetadata="loadDurationWp(i, j)" preload="auto"></audio>
                        </div>
                    </div>
                </div>
            </template>
        </template>
        <template v-for="(item, i) in bottomItemList">
            <div v-if="item.type === 'text' && item.value!==''" class="control-group">
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>
                    <div class="dialog_box_container dialog_box_iconArrow">
                        <div class="content">
                            <p v-html="item.value"></p>
                        </div>
                    </div>
                </div>
            </div>
            <div v-if="item.type === 'audio' && item.value!==''" class="control-group">
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>
                    <div class="audioBox iconRedDot" v-on:click="playAudio(item.value,i,$event)">
                        <span class="icon_play "></span>
                        <span  v-bind:id="'bot_audioTime_'+i" class="audio-time" v-text ="item.value == ''? 0+' `' : ''"></span>
                        <audio v-bind:id="'bot_audioTag_'+i" class="audio" v-bind:src="item.value" v-on:loadedmetadata="loadDurationBot(i)" preload="auto"></audio>
                    </div>
                </div>
            </div>
        </template>
        <#--<p class="footer">-数据由 薯条英语 提供-</p>-->
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
        // 微信朋友圈分享
        wx.onMenuShareTimeline({
            title: "${sharePrimeTitle!''}", // 分享标题
            link: "", // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户点击了分享后执行的回调函数
            }
        });

        //分享给好友
        wx.onMenuShareAppMessage({
            title: "${sharePrimeTitle!''}", // 分享标题
            desc: "${shareSubTitle!''}", // 分享描述
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
            title: "${sharePrimeTitle!''}", // 分享标题
            desc: "${shareSubTitle!''}", // 分享描述
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
            title: "${sharePrimeTitle!''}", // 分享标题
            desc: "${shareSubTitle!''}", // 分享描述
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
<#--</@chipsIndex.page>-->

</@layout.page>
