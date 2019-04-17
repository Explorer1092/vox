<#import "../layout.ftl" as layout>
<@layout.page title="电子教材" pageJs="chipsBookDrama">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{display:none;}
</style>

<audio src="" id="audio"></audio>

<div class="bookWrap" id="bookdrama" v-cloak="">
    <div class="bookHeader">
        <div class="bookTab">
            <ul>
                <li :class="{active:sign === 'dialogueplay'}" @click="change_tab('dialogueplay')">情景对话</li>
                <li :class="{active:sign === 'taskplay'}" @click="change_tab('taskplay')">任务挑战</li>
            </ul>
        </div>
    </div>
    <!-- 中部内容 -->
    <div class="bookMain">
        <#--<div class="bookSection">-->
            <#--<div class="bookTitle">Chapter 1 My first sea journey </div>-->
            <#--<div class="translate">第一章 我的第一次海上旅行</div>-->
        <#--</div>-->
        <div class="bookSection" v-for="(item,index) in playlist" :key="index" @click="playAudio(index)">
            <div class="bookSentence">
                <p :class="{bookTitle:current_index === index}">{{ item.roleName }}: <span style="font-weight: 500;">{{ item.original }}</span></p>
            </div>
            <div class="translate" v-if="translation">{{ item.translation }}</div>
        </div>
        <#--<div class="bookSection">-->
            <#--<div class="bookSentence">-->
                <#--<p>Student: <span style="font-weight: 500;">Captain Pete: The treasure box. Let's open it. Gold coins! We are rich. Let's go home, millionaire.</span></p>-->
            <#--</div>-->
            <#--<div class="translate" v-if="translation">翻译翻译翻译翻译翻译翻译翻译翻译翻翻译翻译翻译翻译翻译翻译翻译翻译翻</div>-->
        <#--</div>-->
    </div>
    <!-- 底部控制区域 -->
    <div class="bookFoot">
        <div class="bookFoot-inner">
            <div class="bookContent">
                <div class="progressBar">
                    <div class="barInner" :style="styleObject"></div>
                </div>
                <div class="timeBox" style="visibility: hidden">
                    <span>6:00</span>
                    <span class="allTime">22:08</span>
                </div>
                <div class="btnsBox">
                    <!-- 默认是暂停，active是正在播放 -->
                    <div class="playBtn" @click="play_paused" :class="{active:is_play}"><i></i></div>
                    <div class="rightBox">
                        <span>翻译</span>
                        <!-- active 打开状态 -->
                        <div class="switchBox" @click="translate" :class="{active:translation}">
                            <span class="switch on">OFF</span>
                            <span class="switch off">ON</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

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

