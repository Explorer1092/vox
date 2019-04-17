<#import "../../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="">
    <@sugar.capsule css=["waiting_for_you"] />

<style>
	body {
		backgrond: #fbdb77;
	}
    img {
    	display: block;
    	width: 100%;
    }
</style>

<div id="waiting_for_you" class="waiting_for_you">
	<img src="/public/images/chips/temporary/waiting_for_you/01.png?v=15624156" alt="">
	<img src="/public/images/chips/temporary/waiting_for_you/02.png?v=15624156" alt="">
	<img src="/public/images/chips/temporary/waiting_for_you/03.png?v=15624156" alt="">
</div>
<script src="/public/js/utils/weixin-1.4.0.js"></script>
<script type="text/javascript">
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



    wx.ready(function () {   //需在用户可能点击分享按钮前就先调用
        var title = '薯条英语系统课程大升级';
        var link = location.href;
        var desc = '仅限薯条英语体验课用户参与，限100名额，报满即止';
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
