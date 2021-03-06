$(function(){
	var constantObj = window.constantObj || {};
	var newExamId = constantObj.newExamId;
	var subject = constantObj.subject || "";
	var clazzId = constantObj.clazzId || "";
	
	var typeTemplateMap  = {
		ViewByPaper     : "T:ViewByPaper",
		ViewByStudents  : "T:ViewByStudents",
		AnalyzePaper    : "T:AnalyzePaper"
	};

	//作业形式click扩展
	function extendTabClick(obj){
		var _tabType = obj.tabType;

		var $tabContent = $("#tabContent");
		$tabContent.empty();
		var getTabType = "get" + _tabType;
		var fn = $17.newexamv2[getTabType];
		var tabContent;
		if(typeof fn === 'function') {
			$("<div></div>").attr("id",_tabType).attr("data-bind","template:{'name':'" + typeTemplateMap[obj.tabType]  + "'}").appendTo($tabContent);
			tabContent = examHistory.getTabContent(_tabType);
			if(!tabContent){
				tabContent = fn.apply(null, [obj]);
				examHistory.addTabContent(_tabType,tabContent);
			}
			//todo 第一次进入，页面没有上面元素，不知道什么原因。，待查
			var node = document.getElementById(_tabType);
			if(node){
				ko.cleanNode(node);
				ko.applyBindings(tabContent, node);
			}else{
				$17.log("元素:" + _tabType + "未找到");
			}
		}else{
			$17.alert("没有符合的模板");
		}
	}


	var defaultOpts = {
		tabClick : null
	};
	function ExamHistory(opts){
		this.hsLoading = ko.observable(true);
		this.options = $.extend(true,{},defaultOpts,opts);
		this.tabContentMap = {};
		this.tabList = [{
			tabType : "ViewByPaper",
			name: "按试卷查看"
		},{
			tabType : "ViewByStudents",
			name : "按学生查看"
		},{
			tabType : "AnalyzePaper",
			name : "试卷分析"
		}];
		this.focusTabIndex = ko.observable(-1);
		this.result = ko.observable({});
		this.init();
	}

	ExamHistory.prototype = {
		constructor : ExamHistory,
		init : function(){
			var self = this;
			$.get("/teacher/newexam/report/newpaperinfo.vpage",{
				clazzId : clazzId,
				examId  : newExamId
			}).done(function(res){
				if(res.success){
					self.result(res);
					self.hsLoading(false);
					self.tabClick.call(self.tabList[0],0,self);
				}else{
					$17.alert(res.info || '查询失败');
				}
			}).fail(function(jqXHR,textStatus,error){
				$17.alert(error);
			});
		},
		addTabContent : function(key,value){
			this.tabContentMap[key] = value;
		},
		getTabContent : function(key){
			return this.tabContentMap[key] || null;
		},
		tabClick : function(index,self){
			self.focusTabIndex(index);
			if(typeof self.options.tabClick == "function"){
				var params = $.extend(true,{},this,{
					clazzId : clazzId,
					examId : newExamId,
					subject : subject,
					newExamPaperInfos : self.result().newExamPaperInfos || []
				});
				self.options.tabClick.call(self,params);
			}
		}

	};

	var examHistory = new ExamHistory({
		tabClick : extendTabClick
	});

	ko.applyBindings(examHistory,document.getElementById("newexamv2Root"));
});