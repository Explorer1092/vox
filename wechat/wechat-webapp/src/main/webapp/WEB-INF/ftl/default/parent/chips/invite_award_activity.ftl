<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="invite_award_activity">
    <@sugar.capsule css=["swiper3", "invite_award_activity"] />

<div>
    <div class="invite_wrap" id="inviteAwardWrap" v-cloak="">
        <div class="inviteHeader">
            <div class="award_bg"></div>
            <div class="roll_bar" v-if="phoneData && phoneData.length>0">
                <div class="icon_awardPic clearfix"></div>
                <div class="usePhone scroll-box">
                    <ul class="usePhoneBox ">
                        <li v-for="item in phoneData" v-if="phoneData && item.rolling"> <span>{{item.rolling}}</span>  已邀请 <span>{{item.count}}</span> 人<span class="award-box">获赠礼包! </span></li>
                    </ul>
                </div>

            </div>
        </div>
        <div class="inviteMain">
            <!--赚钱攻略-->
            <div class="earnPath">
                <div class="title"></div>
                <ul class="path_box">
                    <li class="path path01">
                        <span class="path_num">1</span>
                        <span class="path_word">生成邀请海报</span>
                        <span class="icon_path icon_path01"></span>
                    </li>
                    <li class="path path02">
                        <span class="path_num">2</span>
                        <span class="path_word">分享给好友</span>
                        <span class="icon_path icon_path02"></span>
                    </li>
                    <li class="path path03">
                        <span class="path_num">3</span>
                        <span class="path_word">好友购课成功</span>
                        <span class="icon_path icon_path03"></span>
                    </li>
                    <li class="path path04">
                        <span class="path_num">4</span>
                        <span class="path_word">现金返还到账</span>
                        <span class="icon_path icon_path04"></span>
                    </li>
                </ul>
            </div>
            <!--赢取好礼-->
            <div class="earnPath prizeInfo">
                <div class="prize_title">邀请赢取重磅好礼</div>
                <div class="priceInfo">
                    <div class="prize_word"><span class="activity_time">${beginDate !''}-${endDate !''}</span>活动期间参与邀请有奖活动，邀请人数达 <span class="people_num">10</span>人以上，即可额外包邮赠送薯条英语礼盒套装！</div>
                    <div class="prize_pic"></div>
                </div>
            </div>
            <!--详细规则-->
            <ul class="ruleBox">
                <li class="rule_title">详细规则</li>
                <li class="rule_li rule01">每邀请1人成功购课，就可以返现30%</li>
                <li class="rule_li rule01">在<span class="time">${beginDate !''}-${endDate !''}</span>期间参与邀请有奖活动，邀请10人及以上通过海报生成购课，包邮赠送《薯条英语礼盒套装》；填写发货信息在活动结 束发送，一周内安排寄送</li>
                <li class="rule_li rule01">将邀请海报保存到手机，通过微信发送给好友或者群或者朋友圈（每次生成邀请海报有效期至活动结束）</li>
                <li class="rule_li rule01">好友通过邀请海报成功购买，邀请人即可获得返现</li>
                <li class="rule_li rule01">返现收入在【薯条英语】服务号的“邀请有奖-我的收入”中，好友完课后，即可提现。若好友中途退课，则“我的收入”减少对应返现金额</li>
                <li class="rule_li rule01">有什么疑问可在微信公众号内直接咨询，活动最终解释权归薯条英语所有</li>
            </ul>
        </div>
        <div class="inviteFoot"><a class="inviteBtn" href="/chips/center/invite_award_pic.vpage" v-on:click="invitePoster">生成邀请海报</a></div>

    </div>
    <script src="/public/js/utils/weixin-1.4.0.js"></script>
    <script type="text/javascript">

        var activityType = "${productId !''}";
        var type = '${type!''}';
        wx.config({
            debug: false,
            appId: '${appid!""}',
            timestamp:'${timestamp!""}',
            nonceStr: '${nonceStr!""}',
            signature: '${signature!""}',
            jsApiList: [
                'updateAppMessageShareData',
                'updateTimelineShareData',
                'checkJsApi',
                'onMenuShareTimeline',
                'onMenuShareQQ',
                'onMenuShareAppMessage'
            ]
        });

        wx.ready(function () {
            var timer = null;
            setInterval(function(){scrollList()}, 1000)
            function scrollList() {
                var $uList = $(".scroll-box ul");
                var scrollHeight = $("ul li:first").height();
                $uList.animate({marginTop:-scrollHeight}, 800,function(){
                    $uList.css({marginTop: 0}).find("li:first").appendTo($uList);
                });
            }
            //需在用户可能点击分享按钮前就先调用
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

</div>
</@layout.page>

<#--</@chipsIndex.page>-->
