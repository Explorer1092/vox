<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsFormalGroupMessage">
	<@sugar.capsule css=["chipsFormalGroupMessage"] />
<style>
	[v-cloak] { display: none }
</style>
<div id="root" v-cloak>
	<div style="text-align: center;color: #4A4A4A;margin-top: 3rem;font-size: 1.2rem;">距离购课成功只差一步</div>
	<div class="notify-label">重要通知</div>
	<div class="text">若24⼩时内未拼团成功，会⾃动将支付⾦额原路退回</div>
	<div class="white-wrapper">
		<div style="font-size: 1.2rem;font-weight: bold;color: #4A4A4A;">
			还差<span style="color: red;">1</span>人成团
		</div>
		<div style="font-size: 1rem;color: #4A4A4A;margin-top: 0.3rem;">邀请好友加快成团</div>
		<div class="group-content" style="padding-top: 1.5rem;">
			<div style="text-align: center;">
				<div class="partner-item">
					<img class="partner-avatar" src="${ image }" alt="">
					<div class="partner-label">
						团长
					</div>
					<div style="font-size: 0.7rem;color: rgba(74,74,74,1);margin-top: 0.5rem;">${ userName }</div>
				</div>
				<div class="partner-item" style="vertical-align: top;margin-left: 1rem;">
					<div class="waiting-avatar">?</div>
				</div>
			</div>
			<div style="color: #959595;font-size: 0.8rem;text-align: center;margin-top: 1rem;">拼团中，还差1人成团，<span id="count-down">--:--:--</span>后结束</div>
			<div class="invite-btn" @click="showShareModal=true">马上邀请好友</div>
		</div>
	</div>
	<div class="sharePopup downloadapp" id="sharePopup" v-if="showShareModal" @click="showShareModal=false">
		<div class="shareInner"></div>
	</div>
</div>
<#-- <script src="https://res2.wx.qq.com/open/js/jweixin-1.4.0.js"></script> -->
<script src="/public/js/utils/weixin-1.4.0.js"></script>
<script>
	var type = '${type!''}';
	var code = '${code!''}';
	var image = '${image!''}';
	var surplusTime = parseInt('${surplusTime!''}');
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
			'updateAppMessageShareData',
			'updateTimelineShareData',
			'checkJsApi',
			'updateAppMessageShareData',
            'updateTimelineShareData',
            'checkJsApi',
            'onMenuShareTimeline',
            'onMenuShareQQ',
            'onMenuShareAppMessage'
		]
	});

	

	wx.ready(function () {   //需在用户可能点击分享按钮前就先调用
		wx.updateAppMessageShareData({ 
			title: '仅差1人拼团成功！每天10分钟让英语脱口而出', // 分享标题
			desc: '点击参与拼团，最高减600元', // 分享描述
			link: location.protocol + '//' + location.host + '/chips/center/formal_group_buy.vpage?origin=invite&code='+code, // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
			imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
			success: function () {},
			fail: function(err) {
				console.log(err)
			}
		});
		wx.updateTimelineShareData({ 
	        title: '仅差1人拼团成功！每天10分钟让英语脱口而出', // 分享标题
	        link: location.protocol + '//' + location.host + '/chips/center/formal_group_buy.vpage?origin=invite&code='+code,
	        imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
	        success: function () {},
	        fail: function(err) {
				console.log(err)
			}
	    });
	    wx.onMenuShareAppMessage({ 
            title: '仅差1人拼团成功！每天10分钟让英语脱口而出', // 分享标题
            link: location.protocol + '//' + location.host + '/chips/center/formal_group_buy.vpage?origin=invite&code='+code,
            desc: '点击参与拼团，最高减600元', // 分享描述
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareTimeline({
            title: '仅差1人拼团成功！每天10分钟让英语脱口而出', // 分享标题
            link: location.protocol + '//' + location.host + '/chips/center/formal_group_buy.vpage?origin=invite&code='+code,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
        wx.onMenuShareQQ({
            title: '仅差1人拼团成功！每天10分钟让英语脱口而出', // 分享标题
            link: location.protocol + '//' + location.host + '/chips/center/formal_group_buy.vpage?origin=invite&code='+code,
            imgUrl: 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png', // 分享图标
            success: function () {},
            fail: function(err) {
                console.log(err)
            }
        });
	});



	function countDown(s) {
		var hour = Math.floor(s/3600);
		var minute = Math.floor((s % 3600)/60);
		var second = s % 60;
		return format(hour) + ':' + format(minute) + ':' + format(second);
	}
	function format(a) {
		if(a <= 0) {
			return '00';
		}
		if(a < 10) {
			return '0' + a;
		}
		return a + '';
	}
	if(surplusTime) {
		var interval = setInterval(function(){
			surplusTime -= 1;
			if(surplusTime <= 0) {
				clearInterval(interval);
				return;
			}
			var str = countDown(surplusTime);
			document.getElementById('count-down').innerText = str;
		}, 1000);
	}
</script>
</@layout.page>