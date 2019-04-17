<#import "../layout.ftl" as layout>
<@layout.page title="任务对话" pageJs="chipsTaskIntro">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{display: none;}
    .chinese-text {line-height: 0.8rem;}
    .dialogMain {top: 50%;}
    .taskTarget {position: fixed;}
</style>

<audio src="" id="audio"></audio>

<#-- 任务对话介绍 -->
<div id="taskintro" v-cloak style="height: 100%;overflow: hidden;">
    <div  v-if="taskIntroShow" class="taskIntroWrap">
        <!-- 打包行李页面-->
        <div class="taskWrap-luggage">
            <#--<div class="task-closeBtn"></div>-->
            <div class="tl-main">
                <div class="topContent">
                    <div class="topLeft"></div>
                    <div class="topRight">
                        <div class="window"></div>
                        <div class="bed"></div>
                    </div>
                </div>
                <!-- 人物和行李 -->
                <div class="bottomContent">
                    <div class="luggage"></div>
                    <div class="td-name name03">Eric</div>
                    <div class="people-img img03" @click="openTask"></div>
                    <#--<a href="/chips/center/task.vpage">-->
                    <#--<div class="people-img img03"></div>-->
                    <#--</a>-->
                </div>
            </div>
            <!-- 这一课的目标 -->
            <div class="taskTarget">
                <div class="taskTitle">{{ title }}</div>
                <div class="taskDetail">
                    <p class="text">{{ content }}</p>
                <#--<a :href="'/chips/center/task.vpage?id='+id"><div class="goBtn">GO</div></a>-->
                    <div class="goBtn" @click="lookGoal" v-if="goShow">GO</div>
                </div>
            </div>
        </div>
    </div>

    <task v-if="taskShow" :taskdata="taskdata" :audio="audio" :questions="questions" v-on:show="showSummary"></task>
</div>

<#-- 任务对话模板 -->
<script type="text/html" id="task">
    <div class="missionWrap">
        <!-- 公用关闭按钮 -->
        <div class="task-peopleImg"></div>

        <!-- 顶部提示弹窗 -->
        <div class="sentencePopup" v-if="(tip !== '' && recordAudioIcon)">
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
                <!-- 对话语音 -->
                <div class="dialogList" v-if="!isRecord">
                    <div class="dialogPic">
                        <img src="/public/images/parent/chips/task-Eric.png" alt="">
                    </div>
                    <!-- translateBox 翻译状态 -->
                    <div class="dialogCont" :class="{translateBox:translationStatus}" @click="playAudio(dialogAudio)">
                        <i class="horn04" :class="{hornActive:dialogAudioIcon}"></i>
                        <div class="english-text" v-if="translationStatus">{{ dialogTranslation }}</div>
                    </div>
                    <div class="translateBtn" @click="translate" v-if="!translationStatus"></div>
                    <div class="chinese-text" v-if="translationStatus">{{ dialogCnTranslation }}</div>
                </div>

                <!-- 正在录音对话框 -->
                <div class="dialogList-right" v-if="isRecord">
                    <div class="voiceBox">
                        <div class="headPic">
                            <img src="/public/images/parent/chips/ai-avatar.png" alt="">
                        </div>
                        <div class="voice">
                        <#--<!-- 正在录音recordingIcon 动画：recordingActive 录完后的状态horn02 动画：hornActive&ndash;&gt;-->
                            <i class="recordingIcon recordingActive"></i>
                        </div>
                    </div>
                    <div class="recordText">正在录音...</div>
                </div>
            </div>
        </div>
        <!-- 底部录音 状态01-->
        <div class="dialogBottom" style="display:block">
            <div class="bottomInner">
                <!-- helpBtn默认灰色  helpBtn-yellow选中状态-->
                <div class="helpBtn" @click="showHelp" :class="{'helpBtn-yellow':helpStatus}"></div>

                <!-- recordBtn默认灰色 recordBtn-red选中状态 btnRecord录音中 disabled置灰状态-->
                <div class="recordBtn" @click="record(taskdata.jsgf)" :class="{disabled:recordBtnDisabledIcon,'recordBtn-red':recordAudioIcon,btnRecord:isRecord}"></div>

                <!-- 对话已完成 01-->
                <div class="completeBox" style="display:none">
                    <div class="completeTitle">
                        <div class="returnBtn"></div>
                        <div class="title">对话已完成！</div>
                    </div>
                    <div class="completeText">Sarah不是你要找的人，换个人试试？+语音</div>
                </div>
            </div>

            <!-- 帮助弹层 样式在sceneTalk.scss里-->
            <div class="scene-helpBox taskFoot-inner" v-if="helpStatus" @click="showHelp">
                <i class="takeUp" @click.stop="showHelp"></i>
                <div class="sceneTalk-list" @click="playAudio(helpAudio)">
                    <!-- blackHornActive小喇叭动画 -->
                    <i class="scene-horn task-horn" :class="{blackHornActive:helpAudioIcon}"></i>
                    <p class="scene-text task-text">{{ helpAudioTextEn }}</p>
                    <div class="scene-translate">{{ helpAudioTextCn }}</div>
                </div>
            </div>
        </div>

        <!--弹窗 情景对话已完成 -->
        <div class="task-popup" v-if="end">
            <div class="taskTarget">
                <div class="taskTitle"></div>
                <div class="taskDetail taskDetail02">
                    <p class="text text02">情景对话已完成，你表现很棒！</p>
                    <div class="goBtn completeBtn" @click="next">完成</div>
                </div>
            </div>
        </div>
    </div>
</script>

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
            'openCard'
        ]
    });
</script>

</@layout.page>
