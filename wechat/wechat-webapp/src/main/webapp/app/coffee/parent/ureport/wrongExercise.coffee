define ['knockout','$17','jquery','examCore','menu'],(ko, $17,$)->
	window.ko=ko
	window.download=->
		$17.loadingStart()
		$17.tongji 'parent-单元报告','点击下载按钮'
		setTimeout ->
			location.href='http://wx.17zuoye.com/download/17parentapp?cid=102005'
			return
		,200
		return

	params={
		total: $17.getQuery('total')
		curIndex: $17.getQuery('curIndex')
		title: decodeURIComponent($17.getQuery('title'))
		rate: $17.getQuery('rate')
		sr: decodeURIComponent($17.getQuery('sr'))
	}

	rightFlag = $('#rightFlag')
	wrongFlag = $('#wrongFlag')

	fillData = (data)->
		if params.sr == "正确" then rightFlag.show() else wrongFlag.show()
		$('.subHead').html(data.curIndex + '<b class="number">/' + data.total + '</b>')
		$('#accuracy').html(data.rate+'%')
	$('#loading').hide()
	$('#reportWrong').show()
	fillData(params)



	callback =(data) ->
		q = $('#wrongQuestion')
		qa = $('#wrongQuestionAnalysis')
		if data.success
			vox.exam.render(q[0]
				,'parent_preview'
				,{
					ids:[$17.getQuery('eid')]
					getQuestionByIdsUrl : 'parent/homework/loadquestion.vpage'
					app:'exam_parent_preview'
				})
			vox.exam.render(qa[0]
				,'parent_preview'
				,{
					ids:[$17.getQuery('eid')]
					getQuestionByIdsUrl : 'parent/homework/loadquestion.vpage'
					app:'exam_parent_preview'
					showExplain:true
				})
		else
			q.html('暂不支持预览')
			qa.html('暂不支持预览')
	vox.exam.create	callback