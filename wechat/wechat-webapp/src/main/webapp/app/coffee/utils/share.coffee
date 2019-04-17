###!
微信分享功能封装
@options {
	config:{基础配置
		debug:true
		appId:''
		timestamp:''
		nonceStr:''
		signature:''
	}
	title:'分享标题'
	desc:'分享描述'
	link:'分享链接'
	imgUrl:'分享图标'
	success:function(){} //确认分享回调
	cancel:function(){}  //取消分享回调
 }
###
define ['wx'],(wx)->

	shareInit=(options)->
		shareConfig={
			title:options.title
			desc:options.desc
			link:options.link
			imgUrl:options.imgUrl
			success:options.success
			cancel:options.cancel
		}
		wx.config {
			debug: options.config.debug
			appId: options.config.appId
			timestamp:options.config.timestamp
			nonceStr: options.config.nonceStr
			signature: options.config.signature
			jsApiList: ['onMenuShareTimeline','onMenuShareAppMessage','onMenuShareQQ','onMenuShareWeibo','onMenuShareQZone']
		}
		wx.ready ->
			#分享到朋友圈
			wx.onMenuShareTimeline shareConfig
			#分享给朋友
			wx.onMenuShareAppMessage shareConfig
			#分享到QQ
			wx.onMenuShareQQ shareConfig
			#分享到腾讯微博
			wx.onMenuShareWeibo shareConfig
			#分享到QQ空间
			wx.onMenuShareQZone shareConfig
			return
		return
	###测试###
	###testConfig={
		config:{
			debug: true
			appId: 'appId'
			timestamp: 'timestamp'
			nonceStr: 'nonceStr'
			signature: 'signature'
		}
		title: info.title
		desc: info.title
		link: location.href
		# imgUrl:'imgUrl'
		success:()->
			$17.tongji('微信','家长-单元报告','分享电子报告')
		cancel:()->
	}
	share.init testConfig###
	{
		init:shareInit
	}