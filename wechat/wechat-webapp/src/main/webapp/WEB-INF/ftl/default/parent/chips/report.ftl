<#import "../layout.ftl" as layout>
<@layout.page title="定级报告" pageJs="chipsReport">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{
        display: none;
    }
    .to_robinnormal_btn{
        position: fixed;
        bottom:0.8rem;
        right:0.8rem;
        height:3.1rem;
        width:3.1rem;
    }
</style>

<div class="planWrap" id="report" v-cloak="">
    <!-- 主要内容 -->
    <div class="planBox reportBox">
        <div class="course">- 定级报告 -</div>
        <div class="reportTxt"><!--恭喜完成薯条英语 {{size}} 天集训营！<br>-->根据您孩子的表现，达到了</div>
        <div class="report-grade">{{ data.levelName }}</div>
        <#--<div class="gradeTxt" @click="show_description" >等级说明</div>-->
        <!-- 关键提升点 -->
        <div class="keyBox">
            <div class="keyTitle">Key areas for improvement</div>
            <div class="keyTitle keyTitle02">关键提升点</div>
            <ul class="keyUl">
                <li>
                    <div class="labelName">
                        <div>Warm-up</div>
                        <div>热身</div>
                    </div>
                    <div class="choice" v-for="(item,index) in keys.warmUp">
                        <!-- active 选中状态 -->
                        {{ item.name }}<span class="choiceCase" :class="{active:item.isActive}"></span>
                    </div>
                </li>
                <li>
                    <div class="labelName">
                        <div>Dialogue</div>
                        <div>情景对话</div>
                    </div>
                    <div class="choice" v-for="(item,index) in keys.diaglogue">
                        <!-- active 选中状态 -->
                        {{ item.name }}<span class="choiceCase" :class="{active:item.isActive}"></span>
                    </div>
                </li>
                <li>
                    <div class="labelName">
                        <div>Task</div>
                        <div>任务</div>
                    </div>
                    <div class="choice" v-for="(item,index) in keys.task">
                        <!-- active 选中状态 -->
                        {{ item.name }}<span class="choiceCase" :class="{active:item.isActive}"></span>
                    </div>
                </li>
            </ul>
        </div>
        <div class="tips">
            <div class="tipTxt">CS=Communicative skills 沟通技巧</div>
            <div class="tipTxt">G=Grammar 语法 L=Lexis 词汇 P=Phonology 发音</div>
        </div>
    </div>
    <!-- 老师评语 -->
    <div class="planGuide reportComment">
        <div class="abilityTitle">老师评语</div>
        <div class="comBtn" @click.stop="translate">{{ txt }}</div>
        <!-- 翻译 -->
        <!--<div class="commentBtn commentBtn02">翻译</div>-->
        <div class="comTxt" v-if="translate_status">{{ data.cnSummary }}</div>
        <!-- 英文 -->
        <div class="comTxt comTxt-english" style="font-size: 0.7rem;">{{ data.enSummary }}</div>
        <div class="comAuthor">Signature<span>Oliver Benjamin</span></div>
    </div>

    <!-- 学习历史 -->
    <div class="planHistory">
        <div class="abilityTitle">学习历史</div>
        <div class="canvasContent" id="container2" style="height: 12rem;">

        </div>
    </div>
    <!-- 底部 -->
    <div class="rankingFoot">数据由 薯条英语 提供</div>

    <!-- 等级说明弹窗 -->
    <div class="planPopup" v-if="description_status">
        <div class="popupInner">
            <div class="closeBtn" @click="close_description"></div>
            <div class="popupTitle">等级说明</div>
            <div class="detailTitle">根据以下环节的综合评分进行分级推荐,1-3级分别对应三一口语1-3级考试要求的备选人员</div>
            <ul>
                <li>一级：（情景+任务的平均值）≤68分</li>
                <li>二级：（情景+任务的平均值）>68分且≤83分</li>
                <li>三级：（情景+任务的平均值）>83分</li>
            </ul>
        </div>
    </div>

    <#--<img class="to_robinnormal_btn" @click="to_robinnormal" src="/public/images/parent/chips/report_rightbottom_btn_1.png" alt="">-->

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

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // 定级报告页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'placement_load'
            })
        })
    }
</script>

</@layout.page>



