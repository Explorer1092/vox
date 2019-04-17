
$(function(){
	var subject = $17.getQuery("subject");

	function ClazzReport(options){
		var defaultOpts = {
			subject : null
		};
		var opts = $.extend(true,{},defaultOpts,options);
		this.subject = opts.subject || null;
		this.clazzList = ko.observableArray([]);
		this.focusClazz = ko.observable(null);
		this.stageList = ko.observableArray([]);
		this.focusStage = ko.observable(null);
		this.typeContent = ko.observable({
			switchType : "" // content,student
		});
		this.typeMap = {};
		this.typeMapCache = {};
		this.success = ko.observable(false);
	}
	ClazzReport.prototype = {
		constructor : ClazzReport,
		initClazzList : function(clazzList){
			var self = this;
			self.clazzList(clazzList);
			self.initSimpleSlider();
			(clazzList.length > 0) && this.changeClazz.call(clazzList[0],0,self);
			self.success(true);
		},
		initSimpleSlider :function(){
			var self = this;
			var clazzListSize = self.clazzList().length;
			if(clazzListSize > 6){
				new SimpleSlider({
					slideName      : "slider",
					clickLeftId    : "#sliderL",
					clickRightId   : "#sliderR",
					slideContainer : "#tE-tabGrade",
					slideItem      : ".slideItem",
					itemWidth      : "157",
					slideCount     : 1,
					totalCount     : clazzListSize,
					clickSlideItemFun:function(){}
				});
			}else{
				$("#sliderR").hide();
			}
		},
		switchTypeClick : function (typeName) {
			var self = this;
			var newObj = {
				switchType : ""
			};
			newObj.switchType = typeName;
			var reportObj = self.typeMap["report"] || {};
			switch (typeName){
				case "content":
					newObj = $.extend(true,newObj,reportObj["contentPart"]);
					break;
				case "student":
					newObj = $.extend(true,newObj,reportObj["studentPart"]);
					break;
				default:
					break;
			}
			self.typeContent(newObj);
		},
		loadStageDetail : function(packageId,homeworkId){
			var self = this;
			var switchTypeClickFn = function(res){
				self.typeMap = res;
				self.switchTypeClick.call(self, self.subject === "ENGLISH" ? "content" : "student");
			};
			var uniqueKey = [packageId,homeworkId].join(",");
			if(self.typeMapCache.hasOwnProperty(uniqueKey)){
				switchTypeClickFn(self.typeMapCache[uniqueKey]);
			}else{
				$.get("/container/basicreview/report/detail.vpage",{
					packageId : packageId,
					homeworkId : homeworkId
				}).done(function(res){
					self.typeMapCache[uniqueKey] = res;
					switchTypeClickFn(res);
				}).fail(function(error){
					console.info(error);
				});
			}
		},
		stageClick : function(index,self){
			var stageObj = this;
			var focusClazz = self.focusClazz();
			self.focusStage(stageObj);
			self.loadStageDetail(focusClazz.packageId,stageObj.homeworkId);
			$17.voxLog({
				module  : "m_8NOEdAtE",
				op      : "basic_report_final_unit_click",
				s0      : self.subject
			});
		},
		changeClazz : function(index,self){
			var clazzObj = this;
			self.focusClazz(clazzObj);
			var stagesList = $.isArray(clazzObj.stagesList) ? clazzObj.stagesList : [];
			self.stageList(stagesList);
			(stagesList.length > 0) && self.stageClick.call(stagesList[0],0,self);
			$17.voxLog({
				module  : "m_8NOEdAtE",
				op      : "basic_report_final_class_click",
				s0      : self.subject
			});
		},
		deleteBasicReview : function(element){
			var self = this;
			var $element = $(element);
			if($element.isFreezing()){
				return false;
			}

			$17.voxLog({
				module  : "m_8NOEdAtE",
				op      : "basic_report_deletehomework_click",
				s0      : self.subject
			});

			var deletFn = function(){
				var focusClazz = self.focusClazz();
				var clazzList = self.clazzList();
				$element.freezing();
				$.post("/teacher/termreview/basicreview/delete.vpage",{
					packageId : focusClazz.packageId
				}).done(function(res){
					var info = res.success ? "删除成功" : (res.info || "删除失败");
					$17.alert(info,function(){
						if(res.success){
						   if(clazzList.length == 1){
							   location.href = "/teacher/index.vpage";
						   }else{
							   window.location.reload()
						   }
						}
					});
				}).fail(function(res){

				});
			};
			$.prompt("<div class='w-ag-center'>确认删除基础必过复习？删除后，班级学生将不能继续练习</div>", {
				focus: 1,
				title: "系统提示",
				buttons: {"确认": true, "取消": false},
				position: {width: 500},
				submit: function (e, v) {
					if (v) {
						deletFn();
					}
				}
			});
		}
	};


	var reviewReport = new ClazzReport({
		subject : subject
	});
	$.get("/teacher/basicreview/report/clazzlist.vpage",{
		subject : subject
	}).done(function(res){
		if(res.success){
			var resClazzList = $.isArray(res.clazzInfoList) ? res.clazzInfoList : [];
			reviewReport.initClazzList(resClazzList);
		}else{
			$17.alert(res.info || '获取数据失败',function(){
				location.href = "/teacher/new/homework/report/list.vpage?subject=" + subject;
			});
		}
	}).fail(function (jqXHR,textStatus,errorThrown) {
		$17.alert(jqXHR.responseText,function(){
			location.href = "/teacher/new/homework/report/list.vpage?subject=" + subject;
		});
	});

	ko.applyBindings(reviewReport,document.getElementById("#mainContent"));

	$17.voxLog({
		module  : "m_8NOEdAtE",
		op      : "basic_report_final_review_load",
		s0      : subject
	});
});