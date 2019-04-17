<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="invite_award_pic">
    <@sugar.capsule css=["swiper3", "invite_award_pic"] />

<div>
    <div class="invitePic_wrap" id="inviteAwardWrap" v-cloak="">
        <#--轮播图-->
        <div class="picHeader swiper-container" id="shareFriend">
            <div class="swiper-wrapper" id="shareCanvas">
                <#--<div class="cover_pic swiper-slide"><img class="adver_img" src="/public/images/parent/chips/invite_award/adversiting01.png" alt=""></div>-->
                <#--<div class="cover_pic swiper-slide"><img class="adver_img" src="/public/images/parent/chips/invite_award/adversiting02.png" alt=""></div>-->
                <div class="cover_pic swiper-slide" id="shareImg01">
                    <img class="adver_img adver_img01" src="/public/images/parent/chips/invite_award/adversiting01.png" alt="">
                    <canvas id="canvas01" class="canvasImg" data-qrcode="" data-nickname = "${nickName ! ''}" style="display: none;"></canvas>
                </div>
                <div class="cover_pic swiper-slide" id="shareImg02">
                    <img class="adver_img adver_img02" src="/public/images/parent/chips/invite_award/adversiting02.png" alt="">
                    <canvas id="canvas02" class="canvasImg" data-qrcode="" data-nickname = "${nickName ! ''}" style="display: none;"></canvas>
                </div>
            </div>
            <div class="swiper-pagination dot"></div>
        </div>

        <div class="picMainBtns" >
            <div class="invite_word clearfix" v-on:click="inviteWord">
                <span class="icon_envelop"></span> <span class="invite_text"> 邀请语：</span>
                <div class="inviteText" id="scroll">
                    <div class="invite_state" id="scrollUl">
                        <li v-html="inviteText[0]">薯条英语真的太棒了！省时省事孩子还学得快，比线下英语教学效率高。现在，他们开设了体验课，只需要9.9 ，你们可以体验一下，购课还可获得旅行手账和优惠券，真的太值了！</li>
                        <li v-html="inviteText[0]">薯条英语真的太棒了！省时省事孩子还学得快，比线下英语教学效率高。现在，他们开设了体验课，只需要9.9 ，你们可以体验一下，购课还可获得旅行手账和优惠券，真的太值了！</li>
                    </div>
                </div>
             </div>
                <#--<span class="icon_envelop"></span> 第一次接触线上培训英语，发</div>-->
            <div class="save_btn">
                <div class="click_pic"><span class="icon_arrow"></span> 长按上方图片保存邀请海报</div>
                <div class="share_btn">快去分享给好友</div>
            </div>
             <!-- <img style="opacity: 0;position: absolute;top: 0;height: 0;width: 0;" src="/chips/qrcode.vpage?url=${url !''}" id="qrcodeImg"> -->
            <!--详细规则-->
            <ul class="ruleBox ruleShareBox">
                <li class="rule_title">详细规则</li>
                <li class="rule_li rule01">每邀请1人成功购课，就可以返现30%</li>
                <li class="rule_li rule01">在<span class="time">${beginDate !''}-${endDate !''}</span>期间参与邀请有奖活动，邀请10人及以上通过海报生成购课，包邮赠送《薯条英语礼盒套装》；填写发货信息在活动结 束发送，一周内安排寄送</li>
                <li class="rule_li rule01">将邀请海报保存到手机，通过微信发送给好友或者群或者朋友圈（每次生成邀请海报有效期至活动结束）</li>
                <li class="rule_li rule01">好友通过邀请海报成功购买，邀请人即可获得返现</li>
                <li class="rule_li rule01">返现收入在【薯条英语】服务号的“邀请有奖-我的收入”中，好友完课后，即可提现。若好友中途退课，则“我的收入”减少对应返现金额</li>
                <li class="rule_li rule01">有什么疑问可咨询在线客服，活动解释权归薯条英语所有</li>
            </ul>
        </div>
        <div class="popUp_wrap" v-show="inviteWordBox">
            <div class="popUp_box">
                <div class="pop_bar clearfix">
                    <div class="invite_title">贴心的邀请语提高成功率</div>
                    <div class="change_invite" v-on:click="changeInvite">换一个</div>
                </div>
                <div class="invite_text"  >
                    <textarea name="" v-bind:value="inviteText[num]" id="inviteDetails"></textarea>

                </div>
                <div class="copyBtn" id="copyBtn" v-on:click="copyText" data-clipboard-target="#inviteDetails">复制邀请语</div>
            </div>
        </div>
    </div>

</div>
<script src="/public/js/utils/weixin-1.4.0.js"></script>
<script src="/public/js/utils/html2canvas/html2canvas.js"></script>
<script src="http://eruda.liriliri.io/eruda.min.js"></script>
<script type="text/javascript">

    function longPress(){
        timeOutEvent = 0;
        alert("长按事件触发发");
    }
    var qrcodeUrl = "${linkUrl !''}";
    var avatar = "${avatar !''}";
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
        var title = '薯条英语邀请有奖';
        var desc = '薯条英语体验课，每成功邀请1人返现2.97元';
        var link = location.protocol + '//' + location.host + '/chips/center/invite_award_activity.vpage';
        wx.updateAppMessageShareData({
            title: title, // 分享标题
            link: link,
            desc: desc, // 分享描述
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });

        wx.updateTimelineShareData({
            title: title, // 分享标题
            link: link,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareAppMessage({
            title: title, // 分享标题
            link: link,
            desc: desc, // 分享描述
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareTimeline({
            title: title, // 分享标题
            link: link,
            desc: desc, // 分享描述
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareQQ({
            title: title, // 分享标题
            link: link,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });

    });

</script>
</@layout.page>

<#--</@chipsIndex.page>-->
