define ['knockout','$17','examCore'],(ko,$17)->
	class Prepare
		constructor:->
			###预习安排###
			@title = ''
			@prepareTotal = 0
			@prepares = []
			@showPrepares = ko.observableArray([])
			@hasMorePrepare = ko.observable(false)
		order:(index)->
			index + 1
		setPrepares: (datas)->
			@prepareTotal = datas.length
			@prepares = datas
			@showMorePrepares(3)

		showMorePrepares:(count)->
			if not isNaN(count)
				temp=@prepares.splice(0,count);
			else
				temp=@prepares.splice(0);

			@showPrepares.push item for item in temp
			@hasMorePrepare(@prepares.length>0)
		###显示预习安排例题 ###
		showNextUnitQuestion:(data, curIndex)->
			=>
				$17.loadingStart()
				$17.tongji 'parent-单元报告','点击预习安排例题'
				setTimeout =>
					location.href='/parent/homework/report/example.vpage?eid=' + data.eid + 
					'&total=' + @prepareTotal +
					'&curIndex=' + curIndex +
					'&title=' + encodeURIComponent @title
					return
				,200
				return

	prepare=new Prepare
	prepare