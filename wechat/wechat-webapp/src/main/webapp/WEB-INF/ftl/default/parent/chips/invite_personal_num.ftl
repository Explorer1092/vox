<#import "../layout.ftl" as layout>
<@layout.page title="邀请有奖-个人中心" pageJs="invite_personal_num">
    <@sugar.capsule css=[ "invite_personal_num"] />

<div style="width: 100%; height:100%;background:#f1f1f1">
    <div class="personal_wrap"  id="personalWrap"  v-cloak="">
        <!--未成功推荐页面-->
        <div class="no_invite" v-show="false">
            <div class="header_pic"><img src="${avatar !''}" alt=""></div>
            <div class="text">暂无收入</div>
            <div class="eranBtn" v-on:click="shareEarn" >去赚钱</div>
        </div>
        <!--已成功推荐一人页面-->
        <div class="invite_people"  style="padding-bottom: 2rem;">
            <div class="personal_bg" ></div>
            <div class="personal_bg01" >
            <#--<img :src="useHeaderImage" alt="">-->
            </div>
            <div class="personalMain">
                <div class="header_pic">
                    <img src="${avatar !''}" alt="">
                </div>

                <!--邀请好友列表-->
                <div class="friend_list clearfix" >
                    <div class="friend_title clearfix">
                        <div class="left_title" id="titleBar">{{ titleBar[num] }}</div>
                        <div class="right_num" id="people_num">{{ headerImgList.length }}</div>
                    </div>
                    <ul class="friend_message clearfix">
                        <li class="mess_bar clearfix" v-for="item in headerImgList">
                            <div class="left_pic"><img :src="item.image" alt=""></div>
                            <div class="right_name">{{item.nickName}}</div>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="eran_btn" v-on:click="shareEarn ">去赚钱</div>

        </div>

    </div>

</div>
 <script src="/public/js/utils/weixin-1.4.0.js"></script>
    <script type="text/javascript">
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

        wx.ready(function () {   //需在用户可能点击分享按钮前就先调用
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
