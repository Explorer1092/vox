<#import "../layout.ftl" as layout>
<@layout.page title="${sharePrimeTitle!}" pageJs="chipsActiveServicePreviewV2">
    <@sugar.capsule css=["chipsTodayStudy"] />

<style>
    [v-cloak] {
        display: none;
    }
</style>

<div class="today_study" v-cloak id="today_study_normal">
    <div class="container">
        <div class="header">
            <p>${questionName!}</p>
        </div>
        <div class="well" v-if="summary && summary != ''">
            <div class="dialog_box">
                <div class="portrait">
                    <div class="img">
                        <img v-bind:src="teacher.headPortrait" alt="portrait">
                    </div>
                    <p>{{ teacher.name }}</p>
                </div>
                <div class="dialog_box_container dialog_box_iconArrow">
                    <div class="content">
                        <p v-html="summary"></p>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="userAnswer && userAnswer != ''">
            <div v-if="userAnswer.type === 'video' &&userAnswer.value!==''" class="control-group">
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>
                    <div class="dialog_box_container dialog_box_iconArrow dialog_box_video ">
                        <span class="iconPlay" v-on:click="playVideoOnce"></span>
                        <div id="modal"  class="modalImageBox">

                        </div>
                        <video id="userVideo" v-bind:src="userAnswer.value" style="width: 100%"  class="videoBox">
                        </video>

                    </div>
                </div>
            </div>
            <div v-else  class="dialog_box" style="display: flex;">
                <div class="portrait">
                    <div class="img">
                        <img v-bind:src="teacher.headPortrait" alt="portrait">
                    </div>
                    <p>{{ teacher.name }}</p>
                </div>
                <div class="audioBox iconRedDot" v-on:click="playAudio(userAnswer.value, -1, $event)" >
                    <span class="icon_play "></span>
                    <span  id="audioTime_-1"  class="audio-time"></span>
                    <audio id="audioTag_-1" v-bind:src="userAnswer.value" v-on:loadedmetadata="loadDuration(-1)" preload="auto"></audio>
                </div>
            </div>
        </div>
        <div class="well" v-if="pronList && pronList.length > 0">
            <template v-for="(item, i) in pronList">
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>
                    <div class="dialog_box_container dialog_box_iconArrow">
                        <div class="content">
                            <p v-html="item.comment"></p>
                        </div>
                    </div>
                </div>
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>
                    <div class="audioBox iconRedDot" v-on:click="playAudio(item.audio,i,$event)">
                        <span class="icon_play "></span>
                        <span  v-bind:id="'audioTime_'+i" class="audio-time" v-text ="item.value == ''? 0+' `' : ''"></span>
                        <audio v-bind:id="'audioTag_'+i" class="audio" v-bind:src="item.audio" v-on:loadedmetadata="loadDuration(i)" preload="auto"></audio>
                    </div>
                </div>
            </template>
        </div>
        <div class="well" v-if="gramList && gramList.length > 0">
            <template v-for="(item, i) in gramList">
            <#--<div class="dialog_box">-->

            <#--<div class="portrait">-->
            <#--<div class="img">-->
            <#--&lt;#&ndash;<img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">&ndash;&gt;-->
            <#--<img v-bind:src="teacher.headPortrait" alt="portrait">-->
            <#--</div>-->
            <#--<p>{{ teacher.name }}</p>-->
            <#--</div>-->
            <#--<div class="dialog_box_container">-->
            <#--<div class="arrow">-->
            <#--<span></span>-->
            <#--</div>-->
            <#--<div class="content">-->
            <#--<p v-html="item.level"></p>-->
            <#--</div>-->
            <#--</div>-->
            <#--</div>-->
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                        <#--<img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">-->
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>
                    <div class="dialog_box_container dialog_box_iconArrow">
                        <div class="content">
                            <p v-html="item.comment"></p>
                        </div>
                    </div>
                </div>
                <div class="dialog_box">
                    <div class="portrait">
                        <div class="img">
                        <#--<img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">-->
                            <img v-bind:src="teacher.headPortrait" alt="portrait">
                        </div>
                        <p>{{ teacher.name }}</p>
                    </div>


                    <div class="audioBox iconRedDot" v-on:click="playAudio(item.audio,i,$event)">
                        <span class="icon_play "></span>
                        <span  v-bind:id="'audioTime_'+i" class="audio-time" v-text ="item.value == ''? 0+' `' : ''"></span>
                        <audio v-bind:id="'audioTag_'+i" class="audio" v-bind:src="item.audio" v-on:loadedmetadata="loadDuration(i)" preload="auto"></audio>
                    </div>
                </div>
            </template>

        </div>
        <div class="well" v-if="knowledgeList && knowledgeList.length > 0">
            <template v-for="(item, i) in knowledgeList" :key="i">
                <div v-if="item.type === 'text' &&item.value!==''" class="control-group">
                <#--<label class="control-label">{{ item.key }}：</label>-->
                <#--<div class="controls">-->
                <#--<textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>-->
                <#--</div>-->
                    <div class="dialog_box">
                        <div class="portrait" v-if="item.value != ''">
                            <div class="img">
                            <#--<img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">-->
                                <img v-bind:src="teacher.headPortrait" alt="portrait">
                            </div>
                            <p>{{ teacher.name }}</p>
                        </div>
                        <div class="dialog_box_container dialog_box_iconArrow" v-if="item.value != ''">
                            <div class="content">
                                <p v-html="item.value" ></p>
                            </div>
                        </div>
                    </div>
                </div>
                <div v-if="item.type === 'audio' &&item.value!==''" class="control-group">
                    <div class="dialog_box">
                        <div class="portrait">
                            <div class="img">
                            <#--<img src="/public/images/parent/chips/ts_avator_winston.png" alt="portrait">-->
                                <img v-bind:src="teacher.headPortrait" alt="portrait">
                            </div>
                            <p>{{ teacher.name }}</p>
                        </div>
                        <div class="audioBox iconRedDot" v-on:click="playAudio(item.value,i,$event)">
                            <span class="icon_play "></span>
                            <span  v-bind:id="'audioTime_'+i" class="audio-time" v-text ="item.value == ''? 0+' `' : ''"></span>
                            <audio v-bind:id="'audioTag_'+i" class="audio" v-bind:src="item.value" v-on:loadedmetadata="loadDuration(i)" preload="auto"></audio>
                        </div>
                    </div>
                </div>
            </template>
            <#--<audio id="audioTag" class="audio" v-bind:src="audioUrl"  onloadedmetadata="loadDuration"></audio>-->
        </div>

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
        timestamp: '${timestamp!""}',
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

    wx.ready(function () {
        // 微信朋友圈分享
        wx.onMenuShareTimeline({
            title: "${sharePrimeTitle!}''", // 分享标题
            link: "", // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户点击了分享后执行的回调函数
            }
        });

        //分享给好友
        wx.onMenuShareAppMessage({
            title: "${sharePrimeTitle!}''", // 分享标题
            desc: "${shareSubTitle!}''", // 分享描述
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
            title: "${sharePrimeTitle!}''", // 分享标题
            desc: "${shareSubTitle!}''", // 分享描述
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
            title: "${sharePrimeTitle!}''", // 分享标题
            desc: "${shareSubTitle!}''", // 分享描述
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
