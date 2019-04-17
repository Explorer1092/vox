<#import "../layout.ftl" as layout>
<@layout.page title="跟读" pageJs="chipsFollowWordList">
    <@sugar.capsule css=["chipsAll"] />

<style>
    [v-cloak]{display: none;}
    .followUp-proBar .proBar .barInner {width: 50%;}
    .followUp-state .questionIcon {position: relative;z-index: 9999;}
    .followUp-footer .followBtn.btnTime .grayHorn {top: 40%;}
    .txt01{color:#000 !important;}
    .star01_color{color: #ff9d31 !important;}
    .star02_color{color: #000000 !important;}
    .star03_color{color: #87e12c !important;}


</style>
<audio src="" id="audio"></audio>

<#-- 单词列表 -->
<div id="followwordlist" v-cloak style="height: 100%;">
    <#--<a href="/chips/center/studylist.vpage"><div class="task-closeBtn"></div></a>-->
        <div class="followUp-wrap" v-if="followWordListShow">
            <div class="follow01Main" style="height: 100%;">
                <!--单词跟读提示-->
                <div class="followWord-tips" style="margin-top:1rem;">收拾行李必备单词<strong>，行李箱，外套，运动鞋，护照，</strong>学会就能出发了，赶紧来学吧！</div>
                <!--单词跟读列表-->
                <div class="followWord-list">
                    <ul>
                        <li v-for="(item,index) in questions[1]">{{ item.content }}</li>
                    </ul>
                </div>

                <#--<a :href="'/chips/center/followword.vpage?id='+id"><div class="pubBtn" style="margin-top: 5rem;font-weight: 500;">继续学习GO</div></a>-->
                <div @click="showWordFollow" class="pubBtn" style="margin-top: 1rem;font-weight: 500;">开始GO</div></a>
            </div>
        </div>

        <followword v-if="followWordShow" :questions="questions" :audio="audio" v-on:show="showFollowSentenceList"></followword>
        <followsentencelist v-if="followSentenceListShow" :questions="questions" :audio="audio" v-on:show="showFollowSentence"></followsentencelist>
        <followsentence v-if="followSentenceShow" :questions="questions" :audio="audio" v-on:show="showSummary"></followsentence>
</div>


<#-- 单词跟读 -->
<script type="text/html" id="followword">
    <div class="followWrap02">
        <#--<div class="task-closeBtn"></div>-->
        <!--单词跟读进度条-->
        <div class="followUp-proBar">
            <div class="proTxt"><span>1</span>/<span>2</span></div>
            <div class="proBar"><div class="barInner"></div></div>
        </div>

        <!--跟读帮助说明-->
        <div class="followUp-state">
            <span class="questionIcon" @click="showHelp"></span>
            <!-- 发音打分颜色说明 -->
            <div class="followUp-explain" v-if="helpStatus">
                <div class="explainBox">
                    <p>发音打分颜色说明</p>
                    <div class="explainText">
                        <span class="txtRed">Hello </span>! Nice to<span class="txtGreen"> meet you </span>!
                    </div>
                    <!-- 颜色说明 -->
                    <div class="colorBar">
                        <div class="redBar"></div>
                        <div class="blackBar"></div>
                    </div>
                    <div class="colorText">
                        <span>有待提高</span>
                        <span>还不错</span>
                        <span>非常棒</span>
                    </div>
                </div>
            </div>
        </div>
        <!--单词跟读内容-->
        <div class="followWord-main">
            <div class="wordBox">
                <div class="wordPic" @click="playAudio(questions[1][0].audio)"><img :src="questions[1][0].image"></div>
                <div class="wordInfo" @click="playAudio(questions[1][0].audio)">
                    <!-- blackHornActive小喇叭动画效果 -->
                    <div class="hornIcon" :class="{blackHornActive:wordAudioIcon}"></div>
                    <div class="word">
                        <p class="txt01" :class="{star01_color:(star === 0 || star === 1),star02_color:star === 2,star03_color:star === 3}">{{ questions[1][0].content }}</p>
                        <p class="txt02">{{ questions[1][0].description }}</p>
                    </div>
                </div>
                <div class="stars" v-if="star >= 0">
                    <span class="starIcon" :class="{light:star >= 1 }"></span>
                    <span class="starIcon" :class="{light:star >= 2 }"></span>
                    <span class="starIcon" :class="{light:star >= 3 }"></span>
                </div>
            </div>
        </div>
        <!--跟读底部按钮-->
        <div class="followUp-footer">
            <div class="footerInner">
                <div class="btnBox">
                    <div class="followBtn btnTime" @click="playRecord" v-if="recordTimeBtn">
                        <i class="grayHorn" :class="{grayHornActive:recordAudioIcon}"></i>
                        <span>{{ recordTime }}s</span>
                    </div>
                    <!--followBtn默认开始录音，disabled置灰状态，btnRecord录音中-->
                    <div class="followBtn btnFollowRecord" :class="{disabled:recordBtnDisabledIcon,btnRecord:isRecord}" @click="record(questions[1][0].content)"></div>
                    <div class="followBtn btnNext" @click="next" v-if="nextBtn"></div>
                </div>
            </div>
        </div>
    </div>
</script>


<#-- 句子跟读列表模板 -->
<script type="text/html" id="followsentencelist">
    <div class="followWrap03">
        <#--<div class="task-closeBtn"></div>-->
        <!--单词跟读进度条-->
        <div class="followUp-proBar">
            <div class="proTxt"><span>1</span>/<span>2</span></div>
            <div class="proBar"><div class="barInner"></div></div>
        </div>
        <!--句子跟读列表-->
        <div class="followSentence-main">
            <div class="fSentence-list">
                <div class="roleInfo">
                    <span class="avatar"><img :src="questions[2][0].preloads[0].sentences[0].image"></span>
                    <span class="name">{{ questions[2][0].roles[0].name }}</span>
                </div>
                <div class="sentenceInfo" @click="playAudio(questions[2][0].preloads[0].sentences[0].audio)">
                    <div class="hornIcon" :class="{blackHornActive:sentence1Icon}"></div>
                    <div class="sentence">{{ questions[2][0].preloads[0].sentences[0].content }}</div>
                </div>
            </div>
            <div class="fSentence-list">
                <div class="roleInfo">
                    <span class="avatar"><img :src="questions[2][0].preloads[0].sentences[1].image"></span>
                    <span class="name">{{ questions[2][1].roles[0].name }}</span>
                </div>
                <div class="sentenceInfo" @click="playAudio(questions[2][0].preloads[0].sentences[1].audio)">
                    <div class="hornIcon"  :class="{blackHornActive:sentence2Icon}"></div>
                    <div class="sentence">{{ questions[2][0].preloads[0].sentences[1].content }}</div>
                </div>
            </div>
        </div>
        <!--跟读小贴士-->
        <div class="followSentence-tips">
            <div class="tipsTitle" @click="playAudio(questions[2][0].preloads[0].audio)">
                地道表达小贴士
                <div class="rIcon" :class="{blackHornActive:proLoadDesIcon}"></div>
            </div>
            <div class="tipsTxt" v-html="questions[2][0].preloads[0].description"></div>
        </div>
        <div class="followUp-footer diff">
            <div class="footerInner">
                <span class="followBtn" @click="next">去跟读 GO!</span>
            </div>
        </div>
    </div>
</script>

<#-- 句子跟读模板 -->
<script type="text/html" id="followsentence">
    <div class="followWrap04">
        <#--<div class="task-closeBtn"></div>-->
        <!--单词跟读进度条-->
        <div class="followUp-proBar">
            <div class="proTxt"><span>2</span>/<span>2</span></div>
            <div class="proBar"><div class="barInner" style="width: 100%;"></div></div>
        </div>
        <!--跟读帮助说明-->
        <div class="followUp-state">
            <span class="questionIcon" @click="showHelp"></span>
            <!-- 发音打分颜色说明 -->
            <div class="followUp-explain" v-if="helpStatus">
                <div class="explainBox">
                    <p>发音打分颜色说明</p>
                    <div class="explainText">
                        <span class="txtRed">Hello </span>! Nice to<span class="txtGreen"> meet you </span>!
                    </div>
                    <!-- 颜色说明 -->
                    <div class="colorBar">
                        <div class="redBar"></div>
                        <div class="blackBar"></div>
                    </div>
                    <div class="colorText">
                        <span>有待提高</span>
                        <span>还不错</span>
                        <span>非常棒</span>
                    </div>
                </div>
            </div>
        </div>
        <!--句子跟读内容-->
        <div class="followSentence-main">
            <div class="fSentence-list">
                <div class="roleInfo">
                    <span class="avatar"><img :src="questions[2][0].roles[0].image"></span>
                    <span class="name">{{ questions[2][0].roles[0].name }}</span>
                </div>
                <!-- 未点评状态 -->
                <div class="sentenceInfo" @click="playAudio(questions[2][0].audio)">
                    <div class="hornIcon" :class="{blackHornActive:sentenceIcon}"></div>
                    <#--<div class="sentence">{{ questions[2][0].content }}</div>-->
                    <div class="sentence">
                        <span :class="{txtGreen:wordstar.where > 7,txtRed:(wordstar.where < 3 && wordstar.where >= 0)}">Where&nbsp;</span>
                        <span :class="{txtGreen:wordstar.is > 7,txtRed:(wordstar.is < 3 && wordstar.is >= 0)}">is&nbsp;</span>
                        <span :class="{txtGreen:wordstar.your > 7,txtRed:(wordstar.your < 3 && wordstar.your >= 0)}">your&nbsp;</span>
                        <span :class="{txtGreen:wordstar.passport > 7,txtRed:(wordstar.passport < 3 && wordstar.passport >= 0)}">passport&nbsp;</span>
                    </div>
                    <div class="sentenceScore" v-if="star >= 0">
                        <i class="starsIcon" :class="{light:star >= 1 }"></i>
                        <i class="starsIcon" :class="{light:star >= 2 }"></i>
                        <i class="starsIcon" :class="{light:star >= 3 }"></i>
                    </div>

                    <div class="stars">
                        <span class="starIcon" :class="{light:star >= 1 }"></span>
                        <span class="starIcon" :class="{light:star >= 2 }"></span>
                        <span class="starIcon" :class="{light:star >= 3 }"></span>
                    </div>
                </div>
                <!-- 点评之后的状态 -->
                <#--<div class="sentenceInfo">-->
                    <#--<div class="hornIcon blackHornActive"></div>-->
                    <#--<div class="sentence">What do <span class="txtGreen">you</span> <span class="txtRed">find</span>？Hello！Hello！</div>-->
                <#--</div>-->
            </div>
        </div>
        <#--<!--跟读底部按钮&ndash;&gt;-->
        <#--<div class="followUp-footer">-->
            <#--<div class="footerInner">-->
                <#--<div class="followBtn btnTime">-->
                    <#--<i class="grayHorn grayHornActive"></i>-->
                    <#--<span>0.2s</span>-->
                <#--</div>-->
                <#--<!--followBtn默认开始录音，disabled置灰状态，btnRecord录音中&ndash;&gt;-->
                <#--<div class="followBtn btnRecord"></div>-->
                <#--<div class="followBtn btnNext"></div>-->
            <#--</div>-->
        <#--</div>-->

        <div class="followUp-footer">
            <div class="footerInner">
                <div class="btnBox">
                    <div class="followBtn btnTime" @click="playRecord" v-if="recordTimeBtn">
                        <i class="grayHorn" :class="{grayHornActive:recordAudioIcon}"></i>
                        <span>{{ recordTime }}s</span>
                    </div>
                    <!--followBtn默认开始录音，disabled置灰状态，btnRecord录音中-->
                    <div class="followBtn btnFollowRecord" :class="{disabled:recordBtnDisabledIcon,btnRecord:isRecord}" @click="record(questions[2][0].content)"></div>
                    <div class="followBtn btnNext" @click="next" v-if="nextBtn"></div>
                </div>

            </div>
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
