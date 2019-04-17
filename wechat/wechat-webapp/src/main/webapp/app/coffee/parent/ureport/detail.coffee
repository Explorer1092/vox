define ['jquery','knockout','$17','unitImportant','unitPrepare','unitWrong','menu'],($,ko,$17,important,prepare,wrong)->
	$('#loading').hide()
	$('#reportDetail').show()
	#填充数据
	$("#reportTitle").html result.unitName
	#预习安排
	prepare.setPrepares result.nextPoints or []
	prepare.title = result.unitName
	#本班情况
	wrong.setWrongs result.wql or []
	wrong.title = result.unitName

	if wrong.showWrongs.length is 0
		$('#noWrong').show()
	#单元重点
	important.setImportants result.points or []
	important.title = result.unitName

	ko.applyBindings important,document.getElementById('important')
	ko.applyBindings prepare,document.getElementById('prepare')


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


	return