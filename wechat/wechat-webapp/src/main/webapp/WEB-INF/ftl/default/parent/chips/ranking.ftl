<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语排行榜" pageJs="chipsRanking">
    <@sugar.capsule css=['chipsAll'] />

<style>
    [v-cloak]{
        display: none;
    }
    .rankingWrap .rankingHead .changeBtn .change.active {
        background: #fff;

    }
</style>

<div class="rankingWrap" id="ranking" v-cloak="">
    <div class="rankingHead">
        <div class="title ">排行榜</div>
        <div class="title01">{{ data.name }}<span>{{ data.date }}</span></div>
    </div>
    <#--<div class="rankingHead">-->
        <#--<div class="title title01">薯条英语排行榜</div>-->
        <#--<div class="title">{{ data.name }} {{ data.date }}</div>-->
        <#--<div  class="changeBtn">-->
            <#--<div class="change"  @click="changeTab('hotVideoRank')" :class="{active:sign === 'hotVideoRank'}" style="border-radius:.375rem 0 0 .375rem;">热度</div>-->
            <#--<div class="change" @click="changeTab('scoreRank')" :class="{active:sign === 'scoreRank'}" style="border-radius: 0 .375rem .375rem 0;">得分</div>-->
        <#--</div>-->
    <#--</div>-->
    <!-- 排行榜 -->
    <div class="rankingMain">
        <div  class="changeBtn">
            <div class="change" @click="changeTab('hotVideoRank')" :class="{active:sign === 'hotVideoRank'}" >热度排名</div>
            <div class="change" @click="changeTab('scoreRank')" :class="{active:sign === 'scoreRank'}" >得分排名</div>
        </div>
        <ul class="rankingBox">
            <li v-for="(item,idnex) in rank">
                <div class="rankList">
                    <div class="col01"><i class="medal" :class="{medal01:item.rank === 1,medal02:item.rank === 2,medal03:item.rank === 3}">{{ item.rank > 3 ? item.rank : '' }}</i></div>
                    <div class="col02">
                        <div class="photo">
                            <img :src="item.image" alt="" style="border-radius: 50%;">
                            <!-- crown01 crown02 crown03 分别对应1、2、3名  -->
                            <i class="crown" :class="{crown01:item.rank === 1,crown02:item.rank === 2,crown03:item.rank === 3}" style="z-index: 99;"></i>
                        </div>
                    </div>
                    <div class="col03">
                        <div class="name">{{ item.userName }}</div>
                        <!-- prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 -->
                        <div class="prizeIcon" :class="{prizeIcon01:item.rank === 1,prizeIcon02:item.rank === 2,prizeIcon03:item.rank === 3}"></div>
                    </div>
                    <div class="col04">{{ item.number }} {{ sign === 'hotVideoRank' ? '次观看' : '分' }}</div>
                </div>
            </li>

         <#--<li>-->
                <#--<div class="rankList">-->
                    <#--<div class="col01">1</div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown crown01"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">韩梅梅</div>-->
                        <#--<!-- prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon prizeIcon01"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">100860 次观看</div>-->
                <#--</div>-->
            <#--</li>-->
            <#--<li>-->
                <#--<div class="rankList">-->
                    <#--<div class="col01">2</div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown crown02"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">bai88365bai88365bai88365</div>-->
                        <#--<!-- prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon prizeIcon02"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">10082 次观看</div>-->
                <#--</div>-->
            <#--</li>-->
            <#--<li>-->
                <#--<div class="rankList">-->
                    <#--<div class="col01">3</div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown crown03"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">琪琪的爷琪琪的爷琪琪的爷</div>-->
                        <#--<!-- prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon prizeIcon03"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">8234 次观看</div>-->
                <#--</div>-->
            <#--</li>-->
            <#--<li>-->
                <#--<div class="rankList">-->
                    <#--<div class="col01">4</div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown默认无 crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">琪琪的爷琪琪的爷琪琪的爷</div>-->
                        <#--<!-- prizeIcon默认无 prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">8234 次观看</div>-->
                <#--</div>-->
            <#--</li>-->
            <#--<!-- 相同排名 &ndash;&gt;-->
            <#--<li>-->
                <#--<div class="rankList">-->
                    <#--<div class="col01">5</div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown默认无 crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">琪琪</div>-->
                        <#--<!-- prizeIcon默认无 prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">823 次观看</div>-->
                <#--</div>-->
                <#--<div class="rankList rankList02">-->
                    <#--<div class="col01"></div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown默认无 crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">琪琪琪琪琪琪琪琪</div>-->
                        <#--<!-- prizeIcon默认无 prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">823 次观看</div>-->
                <#--</div>-->
            <#--</li>-->
            <#--<!-- 得分  &ndash;&gt;-->
            <#--<li>-->
                <#--<div class="rankList">-->
                    <#--<div class="col01">4</div>-->
                    <#--<div class="col02">-->
                        <#--<div class="photo">-->
                            <#--<img src="/public/images/parent/chips/ai-avatar.png" alt="">-->
                            <#--<!-- crown默认无 crown01 crown02 crown03 分别对应1、2、3名  &ndash;&gt;-->
                            <#--<i class="crown"></i>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="col03">-->
                        <#--<div class="name">琪琪的爷琪琪的爷琪琪的爷</div>-->
                        <#--<!-- prizeIcon默认无 prizeIcon01 prizeIcon02 prizeIcon03 分别对应1、2、3名 &ndash;&gt;-->
                        <#--<div class="prizeIcon"></div>-->
                    <#--</div>-->
                    <#--<div class="col04">95 分</div>-->
                <#--</div>-->
            <#--</li>-->
        </ul>
    </div>
    <!-- 底部 -->
    <div class="rankingFoot">数据由 薯条英语 提供</div>
</div>

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // 学习排行榜页面_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'rankpage_load'
            })
        })
    }
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
            'openCard',
            'hideAllNonBaseMenuItem'
        ]
    });
    wx.ready(function(){
        // 微信朋友圈分享
        wx.onMenuShareTimeline({
            title: '薯条英语排行榜', // 分享标题
            link: '', // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {
                // 用户点击了分享后执行的回调函数
            }
        });

        //分享给好友
        wx.onMenuShareAppMessage({
            title: '薯条英语排行榜', // 分享标题
            desc: '薯条英语排行榜', // 分享描述
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
            title: '薯条英语排行榜', // 分享标题
            desc: '薯条英语排行榜', // 分享描述
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
            title: '薯条英语排行榜', // 分享标题
            desc: '薯条英语排行榜', // 分享描述
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



