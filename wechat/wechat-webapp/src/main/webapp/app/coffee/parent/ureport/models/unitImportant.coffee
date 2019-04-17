define ['knockout','$17','examCore'],(ko,$17)->
	###单元重点###
	class Important
		constructor:->
			@title = ''
			@importantTotal = 0
			@importants = []
			@showImportants = ko.observableArray([])
			@hasMoreImportant = ko.observable(false)
		order:(index)->
			index + 1
		setImportants:(datas)->
			@importantTotal=datas.length
			@importants = datas
			@showMoreImportants(3)
		showMoreImportants:(count)->
			if not isNaN(count)
				temp=@importants.splice(0,3)
			else
				temp=@importants.splice(0)
			@showImportants.push item for item in temp
			@hasMoreImportant(@importants.length>0)
		###显示单元重点例题 ###
		showCurUnitQuestion:(data, curIndex)->
			=>
				$17.loadingStart()
				$17.tongji 'parent-单元报告','点击单元重点例题'
				setTimeout =>
					location.href='/parent/homework/report/example.vpage?eid=' + data.eid + 
					'&total=' + @importantTotal +
					'&curIndex=' + curIndex +
					'&title=' + encodeURIComponent @title
					return
				,200
				return

	important=new Important
	important

