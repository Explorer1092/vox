define ['knockout','$17','jquery','examCore','menu'],(ko,$17,$)->
	window.ko=ko
	window.download=->
		$17.loadingStart()
		$17.tongji 'parent-单元报告','点击下载按钮'
		setTimeout ->
			location.href='http://wx.17zuoye.com/download/17parentapp?cid=102005'
			return
		,200
		return
	class UnitExample
		constructor:(options)->
			@title = options.title
			@total = "/" + options.total
			@current = options.curIndex
			@countInfo=ko.computed =>
				@current+'<b class="number">'+@total+'</b>'

	example=new UnitExample {
		total: $17.getQuery('total')
		curIndex: $17.getQuery('curIndex')
		title: decodeURIComponent $17.getQuery('title')
	}

	#题目展示部分
	callback=(data)=>
		q = $("#questionContent")
		qa = $("#questionContentAnalysis")
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
	vox.exam.create	callback
	$('#loading').hide()
	$('#reportExample').show()
	ko.applyBindings example, $("#example")[0]