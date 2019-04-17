<#import "../layout.ftl" as layout>
<@layout.page title="对话实录" pageJs="chipsDialogue">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{
        display: none;
    }
    .dialogueInner {
        padding-top: 1rem;
    }
    .dialogBox-better .better-title:before{
        left:0;
    }
</style>

<audio src="" id="audio"></audio>

<div class="dialogueWrap"  id="dialogue" v-cloak>
    <#--<div class="task-closeBtn"></div>-->
    <#--<div class="dialogTitle">对话实录</div>-->
    <div class="dialogueInner">
        <div class="dialogueMain">
            <template v-for="(item,index) in dialog_data" :key="index">
                <template v-if="item.type === 'left'">
                    <div class="section">
                        <div class="headPic">
                            <img :src="item.image" alt="">
                        </div>
                        <div class="dialogBox" @click="playAudio(item.audio,index)">
                            <div class="sentenceBox">
                                <i class="horn" :class="{hornActive:(dialogAudioIcon === index && dialogSuggestionAudioIcon === -1)}"></i>
                                <p class="sentence">{{ item.en }}</p>
                            </div>
                            <div class="translate">{{ item.cn }}</div>
                        </div>
                    </div>
                    <!-- 这样说会更好哦 -->
                    <div class="dialogBox-better" v-if="item.suggestion.length > 0">
                        <div class="better-title">你可以参考以下几种回答</div>
                        <div class="betterBox">
                            <div class="betterList" v-for="(sItem,sIndex) in item.suggestion"  @click="playAudio(sItem.sentence_audio,index,sIndex)">
                                <i class="horn03" :class="{whiteHornActive:(dialogAudioIcon === index && dialogSuggestionAudioIcon === sIndex)}"></i>
                                <div class="rightContent">
                                    <p class="sentence02">{{ sItem.sentence }} </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
                <template v-if="item.type === 'right'">
                    <div class="section sectionRight">
                        <div class="voiceBox">
                            <div class="headPic">
                                <img :src="item.image" alt="">
                            </div>
                            <div class="voice" @click="playLocalAudio(item.mediaId,index)">
                                <i class="horn02" :class="{hornActive:(dialogAudioIcon === index && dialogSuggestionAudioIcon === -1)}"></i>
                            </div>
                        </div>
                    </div>
                </template>
            </template>
        </div>
    </div>
</div>


<script src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script>
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
            'openCard'
        ]
    });
</script>


</@layout.page>

<#--</@chipsIndex.page>-->
