<#import "../../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="drawing_update">
    <@sugar.capsule css=["drawing_update"] />
<style>
	body {
		backgrond: #fbdb77;
	}
    [v-cloak] { display: none }
    img {
    	display: block;
    	width: 100%;
    }
</style>

<div id="drawing_update" class="drawing_update" v-cloak="">
	<img class="top-bg" src="/public/images/parent/chips/app/drawing_update/bg.png" alt="" style="height: 100%">
	<img class="logo" src="/public/images/parent/chips/app/drawing_update/logo.png" alt="">
	<div class="questionBox" style="position: relative;z-index: 5;margin-bottom: 1rem">
		<div class="question-wrapper">
			<img id="avatar" class="avatar" src="https://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.png" alt="">
			<div style="font-size: 0.5rem;color: #999999;text-align: center;">
				<span id="use-name" style="font-size: 0.8rem;color: #464646;margin-right: 0.5rem;">我</span>
				正在薯条英语学习...
			</div>
			<div class="statistic-wrapper">
				<div class="statistic-item">
					<div><span class="num" id="day">--</span><span class="unit">天</span></div>
					<div class="text">
						<img src="/public/images/parent/chips/app/drawing_update/insist_study.png" alt="">
					</div>
				</div>
                <div class="linellae"></div>
				<div class="statistic-item">
					<div><span class="num" id="sentence">--</span><span class="unit">句</span></div>
					<div class="text">
						<img src="/public/images/parent/chips/app/drawing_update/total_study.png" alt="">
					</div>
				</div>
			</div>
            <div class="media-wrapper" style="margin-top: 1rem;">
                <div id="fake-poster" class="video-wrapper unplay">
                    <div style="width: 100%;height: 100%;backgrond: red;">
                        <img id="video-poster" src="" alt="">
                    </div>
                    <img src="/public/images/parent/chips/app/drawing_update/video_frame.png" alt="" style="position: absolute;top: 0;left: 0;z-index: 15;width: 100%;height: 100%;z-index: 12;">
                    <img src="/public/images/parent/chips/app/drawing_update/flag.png" alt="" style="position: absolute;top: 0;left: 0;z-index: 15;width: 4rem;z-index: 13;">
                    <img src="/public/images/parent/chips/app/drawing_update/gift.png" alt="" style="position: absolute;bottom: 0;right: 0;z-index: 15;width: 4rem;z-index: 14;">
                    <img id="playBtn" class="playBtn" src="/public/images/parent/chips/app/drawing_update/play_btn.png" alt="">
                </div>
                <div id="video-wrapper" class="video-wrapper" style="display: none;">
                    <video id="video" controls="controls" class="video" src="" poster="" preload="auto" x-webkit-airplay="true" x5-playsinline="true" webkit-playsinline="true" playsinline="true"></video>
                </div>
            </div>
			<div style="font-size: 0.8rem;color: #3E1A05;text-align: center;margin-top: 0.5rem;">
                快来挑战我的问题，助我获取能量
            </div>
            <div class="learn-course"><span @click="learnCourse">我也要学习这个课程</span></div>
            <ul class="friend-box">
                <li class="friend-list clearfix" v-for="(item,index) in friendsList" :key="index" @click="intoFriend(item)">
                    <div class="list-left"><img :src="item.avatar" alt=""> <span>{{item.uname}}</span></div>
                    <div class="list-right"><span class="icon-bottle"></span> <span>+{{item.energy}}</span><span class="icon-arrow"></span></div>
                </li>
            </ul>
		</div>
	</div>
</div>
<div class="task-modal" v-show="preview">
    <img class="task-modal-close" src="/public/images/parent/chips/close.png" alt="">
	<div class="task-wrapper" style="position: absolute">
		<div id="options"></div>
		<div class="btn" id="submit">提&nbsp;&nbsp;交</div>
        <div id="toast">
            <div id="msg01"></div>
            <div id="msg02"></div>
        </div>
	</div>
</div>
<script src="/public/js/utils/weixin-1.4.0.js"></script>
<script type="text/javascript">
    var type = '${type!''}';
    var title = "我在薯条英语发起了一个提问，快来挑战吧！";
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
        var link = location.href;
        var desc = '每天10分钟，让英语脱口而出';
        var imgUrl = 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png';

        wx.updateAppMessageShareData({ 
            title: title, // 分享标题
            link: link,
            desc: desc, // 分享描述
            imgUrl: imgUrl, // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.updateTimelineShareData({ 
            title: title, // 分享标题
            link: link,
            imgUrl: imgUrl, // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareAppMessage({ 
            title: title, // 分享标题
            link: link,
            desc: desc, // 分享描述
            imgUrl: imgUrl, // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareTimeline({
            title: title, // 分享标题
            link: link,
            imgUrl: imgUrl, // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareQQ({
            title: title, // 分享标题
            link: link,
            imgUrl: imgUrl, // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
    });
</script>
</@layout.page>

<#--</@chipsIndex.page>-->
