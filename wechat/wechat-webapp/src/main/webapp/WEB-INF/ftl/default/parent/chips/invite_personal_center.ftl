<#import "../layout.ftl" as layout>
<@layout.page title="邀请有奖-个人中心" pageJs="invite_personal_center">
    <@sugar.capsule css=[ "invite_personal_center"] />

<div style="width: 100%; height:100%;background:#f1f1f1">
    <div class="personal_wrap"  id="personalWrap"  v-cloak="">
        <!--未成功推荐页面-->
        <div class="no_invite" v-if="!flag">
            <div class="header_pic"><img src="/public/images/parent/chips/invite_award/logo.png" alt=""></div>
            <div class="text">暂无收入</div>
            <div class="eranBtn" v-on:click="shareEarn" >去赚钱</div>
        </div>
        <!--已成功推荐一人页面-->
        <div class="invite_people" v-else-if="flag" style="padding-bottom: 2rem;">
            <div class="personal_bg" ></div>
            <div class="personal_bg01" >
                <#--<img :src="useHeaderImage" alt="">-->
            </div>
            <div class="personalMain">
                <div class="header_pic">
                    <img :src="img" alt="">
                </div>
                <!--个人分红-->
                <div class="people_invite" v-show="!friendList">
                    <div class="total_money">
                        <div class="word">今日总分红</div>
                        <div class="num"><span class="money_num">￥{{todayAcount}}</span></div>
                    </div>
                    <ul class="totalList clearfix">
                        <li class="num_li total_num">
                            <div class="title">累计分红</div>
                            <div class="num">￥{{totalAcount}}</div>
                        </li>
                        <li class="num_li people_num">
                            <div class="title">累计邀请用户</div>
                            <div class="num">{{paidNum}}人</div>
                        </li>
                        <li class="num_li allow_extract">
                            <div class="title">可提现</div>
                            <div class="num">￥{{drawableAcount}}</div>
                        </li>
                    </ul>
                    <ul class="invitePeople clearfix">
                        <li class="invite_li look_people clearfix">
                            <div class="left_title">已浏览好友</div>
                            <div class="right_num"  v-on:click="invitNum>0 && shareFriend(0,$event)">{{invitNum}}人 <span class="icon_arrow"></span></div>
                        </li>
                        <li class="invite_li no_payPeople clearfix">
                            <div class="left_title">未付款好友</div>
                            <a class="right_num" v-on:click="noPaidNum>0 && shareFriend(1,$event)">{{noPaidNum}}人 <span class="icon_arrow"></span></a>
                        </li>
                        <li class="invite_li buy_people clearfix">
                            <div class="left_title">邀请购买成功好友</div>
                            <a class="right_num" v-on:click="paidNum>0 && shareFriend(2,$event)">{{paidNum}}人 <span class="icon_arrow"></span></a>
                        </li>
                    </ul>
                </div>
                <!--邀请好友列表-->
                <#--<div class="friend_list clearfix" v-show="friendList">-->
                    <#--<div class="friend_title clearfix">-->
                        <#--<div class="left_title" id="titleBar"></div>-->
                        <#--<div class="right_num">{{count}}人</div>-->
                    <#--</div>-->
                    <#--<ul class="friend_message clearfix">-->
                        <#--<li class="mess_bar clearfix" v-for="item in headerImgList">-->
                            <#--<div class="left_pic"><img :src="item.image" alt=""></div>-->
                            <#--<div class="right_name">{{item.nickName}}</div>-->
                        <#--</li>-->
                    <#--</ul>-->
                <#--</div>-->
            </div>
            <div class="eran_btn" v-on:click="shareEarn ">去赚钱</div>

        </div>

    </div>

</div>
<script src="/public/js/utils/weixin-1.4.0.js"></script>
    <script type="text/javascript">
        var activiType = "${activityType !''}";
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
            wx.hideAllNonBaseMenuItem();
            wx.hideOptionMenu();
        })

    </script>

</@layout.page>

<#--</@chipsIndex.page>-->
