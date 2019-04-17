<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsShareVideo">
    <@sugar.capsule css=['chipsShareVideo'] />

<style>
    [v-cloak]{
        display: none;
    }
    .playWindow{
        background: rgba(0,0,0,0.01);
    }
    .playWindow video{
        width: 100%;
        height: 100%;
    }
    .playWindow:before{
        position: absolute;
        content: "";
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        opacity: 1;
        background: rgba(0,0,0,.5);
        display: inline-block;
        height: 1px;
        width: 1px;
    }
</style>
<div id="output"></div>
<div class="sharevideo" id="sharevideo" v-cloak="">
    <div class="shareWrap">
        <#--<div class="returnBtn"></div>-->
        <div class="shareBox">
            <p>我家宝贝已经和老外聊</p>
            <p><span class="bold">${unitName!}</span>了快来围观吧！</p>
            <div class="shareVideo">
                <div class="playWindow">
                    <#--<img src="/public/images/parent/chips/rugbyBanner01.jpg" alt="">-->
                    <#--<img src="/public/images/parent/chips/sharePlayBtn.png" class="playBtn" alt="">-->
                    <#--<video src="${url!}" controls></video>-->
                    <video controls style="z-index: 9999;" poster="${unitImage}">
                        <source src="${url!}">
                    </video>
                </div>
                <div class="summaryInfo">
                    <div class="avatar">
                        <img src="/public/images/parent/chips/build01.png">
                    </div>
                    <div class="info">
                        <div class="name"><span>${userName!}</span><i class="core"></i></div>
                        <div class="time">${dateTime!}</div>
                    </div>
                </div>
            </div>
            <!-- 老师评语 -->
            <div class="shareComment">
                <div class="comTitle">老师评语</div>

                <#if commentAudio ?? && commentAudio != ''>
                    <!-- active 播放动画 -->
                    <div class="comHorn" @click="playAudio()" :class="{active:isPlay}"></div>
                    <audio id="audio" src="${commentAudio!}"></audio>
                </#if>
                <#--<audio id="audio" src="http://other.web.ra01.sycdn.kuwo.cn/resource/n3/192/19/57/297064714.mp3?crazycache=1"></audio>-->

                <ul class="comWords">
                <#if labels?? && labels?size gt 0>
                    <#list labels as e >
                        <#if e ?? && e == 'Pronunciation'>
                            <li class="orange">发音好</li>
                             <#elseif e ?? && e == 'Express'>
                                 <li class="green">表达好</li>
                             <#elseif e ?? && e == 'Fluency'>  <li class="blue">英语流利</li>
                             <#elseif e ?? && e == 'UsingTools'>   <li class="yellow">有道具</li>
                             <#elseif e ?? && e == 'ActionMatch'>   <li class="pink">有动作</li>
                             <#elseif e ?? && e == 'Characteristic'>  <li class="cyan">有特色</li>
                             <#elseif e ?? && e == 'Expressiveness'>  <li class="purple">表情好</li>
                             <#elseif e ?? && e == 'OldFriend'>  <li class="yellow">老朋友</li>
                             <#elseif e ?? && e == 'NewFriend'>  <li class="yellow">新朋友</li>
                             <#elseif e ?? && e == 'Other'>  <li class="yellow">其他</li>
                             <#elseif e ?? && e == 'Word'>  <li class="blue">单词发音</li>
                             <#elseif e ?? && e == 'Whole'>  <li class="blue">整体语音</li>
                        </#if>
                    </#list>

                </#if>
                </ul>
                <div class="comSentence">${comment!}</div>
            </div>
            <!-- 炫耀按钮 -->
            <div class="shareBtn" @click="share">炫耀一下</div>
            <!-- 底部 -->
            <div class="shareFoot">
                <p class="footTitle">薯条英语</p>
                <p class="footDetail">在这里<span>聊老外</span> 轻松<span>学口语</span></p>
                <div class="footCode">
                    <img src="/public/images/parent/chips/adCode.png" alt="">
                </div>
                <p class="footText">长按二维码识别关注</p>
            </div>
        </div>
    </div>

    <!-- 弹窗 -->
    <div class="sharePopup" v-if="tipStatus" @click="share">
        <div class="shareInner"></div>
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
            title: '${title!"薯条英语，AI情景对话"}', // 分享标题
            link: '', // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户点击了分享后执行的回调函数
            }
        });

        //分享给好友
        wx.onMenuShareAppMessage({
            title: '${title!"薯条英语，AI情景对话"}', // 分享标题
            desc: '薯条英语，AI情景对话', // 分享描述
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
            title: '${title!"薯条英语，AI情景对话"}', // 分享标题
            desc: '薯条英语，AI情景对话', // 分享描述
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
            title: '${title!"薯条英语，AI情景对话"}', // 分享标题
            desc: '薯条英语，AI情景对话', // 分享描述
            link: '', // 分享链接
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户确认分享后执行的回调函数
            },
            cancel: function () {
                // 用户取消分享后执行的回调函数
            }
        });
    })

</script>

</@layout.page>



