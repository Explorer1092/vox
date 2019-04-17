define ['knockout','$17','jquery','examCore'],(ko,$17,$)->
	window.ko=ko;
	window.WApp={}
	#转到高频错题详情页面
	WApp.toWrongQuestion =(index,eid) ->
		$17.loadingStart()
		$17.tongji 'parent-单元报告','点击高频错题例题'
		setTimeout =>
			location.href='/parent/homework/report/wrongExercise.vpage?eid=' + wrong.curQuestion.eid + 
			'&total=' + wrong.wrongsTotal +
			'&curIndex=' + wrong.curQuestion.index +
			'&title=' + encodeURIComponent(wrong.title) +
			'&sr=' + encodeURIComponent(wrong.curQuestion.sr) +
			'&rate=' + wrong.curQuestion.rate
			return
		,200
		return
	#预留自定义绑定
	# ko.bindingHandlers.QuestionView={
	# 	update:(element, valueAccessor, allBindings,viewModel,bindContext)->
	# 		vox.exam.render(element,'mobile',{ids:valueAccessor()})
	# }
	
	# 显示更多按钮
	moreWrong=$('#moreWrong')
	moreWrong.on 'click',->
		wrong.showMoreWrongs()
	wqDom=$('#wrongQuestions')
	class Wrong
		constructor:->
			###本班情况###
			@title = ''
			@wrongsTotal = 0
			@wrongs = []
			@showWrongs = []
			@hasMoreExercises = ko.observable(false)
			@curQuestion = null
		order:(index)->
			index + 1

		setWrongs:(datas)->
			@wrongsTotal = datas.length
			$("#wrong").hide() if @wrongsTotal==0
			@wrongs = datas
			d.index=index+1 for d,index in datas
			callback=(data)=>
				if(data.success)
					@showMoreWrongs()
			vox.exam.create	callback
		showMoreWrongs:(count)->
			if not isNaN(count)
				temp=@wrongs.splice(0,count)
			else
				temp=@wrongs.splice(0)

			@showWrongs.push item for item in temp
			@hasMoreExercises(@wrongs.length>0)
			@appendQuestion q for q in temp

		### 显示高频错题 ###
		showWrongQuestion:(data,curIndex)->
			=>
				$17.loadingStart()
				$17.tongji 'parent-单元报告','点击高频错题例题'
				setTimeout =>
					location.href='/parent/homework/report/wrongExercise.vpage?eid=' + data.eid + 
					'&total=' + @wrongsTotal +
					'&curIndex=' + curIndex +
					'&title=' + @title
					return
				,200
				return
		appendQuestion:(question)->
			hf='javascript:WApp.toWrongQuestion()'
			@curQuestion=question
			t = $('<div class="column"><h2><a href='+hf+' class="btn-view">查看</a><span>第'+question.index+'题</span></h2></div><div class="content"></div>')
			wqDom.append(t)
			vox.exam.render(t[1]
				,'parent_preview'
				,{
					ids:[question.eid]
					getQuestionByIdsUrl : 'parent/homework/loadquestion.vpage'
					app:'exam_parent_preview'
				})		
			if @wrongs.length>0
				moreWrong.show()
			else 
				moreWrong.hide()

	
	
	wrong=new Wrong
	window.wrong=wrong
	wrong
	#测试数据
	
