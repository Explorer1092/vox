<#import "../layout.ftl" as layout>
<@layout.page title="情景对话" pageJs="chipsSceneIntro">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{
        display: none;
    }
    .chinese-text {
        line-height: 0.8rem;
    }
</style>

<audio src="" id="audio"></audio>

<div id="sceneintro" v-cloak style="height: 100%;">
    <div class="sceneTalkWrap"  v-if="sceneIntroShow">
    <#--<a href="/chips/center/studylist.vpage"><div class="task-closeBtn"></div></a>-->
        <div class="taskTarget taskTarget02">
            <div class="taskTitle">{{ title }}</div>
            <div class="taskDetail">
                <p class="text">{{ content }}</p>
                <div class="goBtn" @click="showSceneTalk">GO</div>
            </div>
        </div>
    </div>

    <scenetalk v-if="sceneTalkShow" :audio="audio" :sencedata="sencedata" v-on:show="showSummary"></scenetalk>
</div>



<script type="text/html" id="scenetalk">
    <div class="sceneTalkWrap sceneTalkWrap02">
        <#--<div class="task-closeBtn"></div>-->
        <!-- 顶部弹窗提示 -->

        <div class="sentencePopup" v-if="tip !== ''">
            <p class="s-title">提示</p>
            <div class="s-content">
                <div class="rightText">
                    <p>{{ tip }}</p>
                    <#--<div class="popup-translate">我爸爸会去喂小鸡。</div>-->
                </div>
            </div>
        </div>

        <div class="dialogMain">
            <div class="dialogInnerBox">

                <template v-for="(item,index) in dialogArr">
                    <template v-if="item.type === 'left'">
                        <!-- 对话语音 -->
                        <div class="dialogList">
                            <div class="dialogPic scenePic">
                                <img src="/public/images/parent/chips/sceneTalk-headPic.png" alt="">
                            </div>

                            <!-- translateBox 翻译状态 -->
                            <div class="dialogCont" :class="{translateBox:translationIndex === index}" @click="playAudio(item.audio,index)">
                                <i class="horn04" :class="{hornActive:dialogAudioIconIndex === index}"></i>
                                <div class="english-text" v-if="translationIndex === index">{{ item.en }}</div>
                            </div>
                            <div class="translateBtn" @click="translate(index)"  v-if="!(translationIndex === index)"></div>
                            <div class="chinese-text" v-if="translationIndex === index">{{ item.cn }}</div>
                        </div>
                    </template>
                    <template v-if="item.type === 'right'">
                        <div class="dialogList-right">
                            <div class="voiceBox">
                                <div class="headPic">
                                    <img src="/public/images/parent/chips/ai-avatar.png" alt="">
                                </div>
                                <div class="voice" @click="playLocalAudio(item.mediaId,index)">
                                    <!-- 正在录音recordingIcon 动画：recordingActive 录完后的状态horn02 动画：hornActive-->
                                    <i class="recordingIcon" :class="{recordingActive:dialogAudioIconIndex === index}"></i>
                                </div>
                                <#--<div class="addScore">+20分</div>-->
                            </div>
                            <#--<div class="recordText recordText02">赞，收获一个风筝</div>-->
                        </div>
                    </template>
                </template>
            </div>
        </div>
        <div class="sceneTalkFoot">
            <div class="sceneTalkFoot-inner">
                <!-- helpBtn-->
                <div class="helpBtn scene-helpBtn" @click="showHelp" :class="{'helpBtn-yellow':helpBtnBg}"></div>
                <!-- 摄像头 -->
                <div class="sceneTalk-recordBtn" @click="changeCamera" v-if="cameraStatus">
                    <!--  默认是灰色 cameraIcon-white 白色摄像头 -->
                    <div class="cameraIcon" :class="{'cameraIcon-white':isStart}"></div>
                    <div class="c-text">切换摄像头</div>
                </div>

                <#-- 录制状态 -->
                <div class="scene-recordBox" @click="record" v-if="!cameraStatus">
                    <!-- video-recordBtn默认点击录制 video-recordingBtn录制中,下方文字需隐藏-->
                    <div class="video-recordBtn" :class="{'video-recordingBtn':isRecording}"></div>
                    <div class="video-recordText" v-if="!isRecording">点击录制</div>
                    <#--<div class="video-recordText" v-if="isRecording">录制中</div>-->
                </div>
            </div>

            <!-- 帮助弹层 -->
            <div class="scene-helpBox" v-if="helpStatus" @click="showHelp">
                <i class="takeUp" @click.stop="showHelp"></i>
                <div class="sceneTalk-list">
                    <i class="scene-horn" :class="{whiteHornActive:helpAudioIcon}"></i>
                    <p class="scene-text">{{ helpAudioTextEn }}</p>
                    <div class="scene-translate">{{ helpAudioTextCn }}</div>
                </div>
            </div>
        </div>

        <!--弹窗 情景对话已完成 -->
        <div class="task-popup" v-if="finishedDialogStatus">
            <div class="taskTarget">
                <div class="taskTitle"></div>
                <div class="taskDetail taskDetail02">
                    <p class="text text02">情景对话已完成，你表现很棒！</p>
                    <div class="goBtn completeBtn" @click="next">完成</div>
                </div>
            </div>
        </div>

        <!--弹窗 提示微信端不能录制视频 -->
        <div class="scene-tip" v-if="noCanVideoTip === 1" style="font-size: 0.65rem;width: 70%;background: #242424;margin: 0 auto;border-radius: 0.3rem;color: #fff;padding: 1rem;position: absolute;top: 50%;left: 50%;box-sizing: border-box;transform: translate(-50%,-50%);z-index: 999;">
            <p class="scene-tip-txt">在正式课程或家长通APP-薯条英语中试用可以体验视频对话录制哦～</p>
        </div>

    </div>


</script>

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
