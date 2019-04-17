define ['jquery','knockout','userpopup','$17','menu'],($,ko,userpopup,$17)->
	requestUrl = '/parent/homework/report/unitreport.vpage'
	fetch=(sid,subject)->
		viewModel.unitReportList.removeAll()
		request = {
			sid:viewModel.sId
			subject:viewModel.focusTab().toUpperCase()
		}
		$17.ajax {
			url:requestUrl
			showLoading:true
			data:request
			success:(data)->
				if data.success
					urs=data.urs
					viewModel.unitReportList(urs)
			error:(data)->

		}
	#单元报告列表数据项
	class UnitReport
		constructor:(options)->
			@title=options.title

	#单元报告列表ViewModel
	class UnitReportVM
		constructor:()->
			@unitReportList=ko.observableArray([])
			@focusTab=ko.observable('english')
			@sId=0
		changeTab:(subject)->
			@focusTab(subject)
			if subject is 'english' then fetch() else @unitReportList.removeAll()
		toDetail:(data)->
			root = @
			=>
				$17.loadingStart()
				$17.tongji 'parent-单元报告','点击查看按钮'
				setTimeout ->
					location.href='/parent/homework/report/unitreportdetail.vpage?sid=' + root.sId + 
					'&unitId=' + data.unitId + 
					'&subject=' + root.focusTab().toUpperCase()
					return
				,200

	viewModel=new UnitReportVM()

	###菜单组件，后完善###
	# Menu={
	# 	catalogs:[[{title:'首页',href:'/'}],[{title:'单元报告',href:'123'}],[{title:'做错考点',href:'123'}],[{title:'每周报告',href:'123'}]]
	# }
	# ko.applyBindings Menu,document.getElementById 'menuBoxContainer'
	$('#loading').hide()
	$('#unitReport').show()
	ko.applyBindings viewModel, document.getElementById 'unitReport'

	userpopup.selectStudent "unitReport";
	loadMessageById:(sId)->
		viewModel.sId = sId
		fetch()
		