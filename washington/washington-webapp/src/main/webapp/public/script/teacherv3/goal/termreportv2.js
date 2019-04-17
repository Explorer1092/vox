$(function(){
	"use strict";



	function TermReport(options){
		var defaultOptions = {
			subject     : null,
			clazzList   : [],
			domain      : null,
			dateList    : []
		};
		var newOptions = $.extend(true,{},defaultOptions,options);
		var clazzList = $.isArray(newOptions.clazzList) ? newOptions.clazzList : [];
		var dateList = $.isArray(newOptions.dateList) ? newOptions.dateList : [];
		this.domain  = newOptions.domain;
		this.subject = newOptions.subject;
		this.groupId = ko.observable(clazzList.length > 0 ? clazzList[0].groupId : "");
		this.clazzList = ko.observableArray(clazzList);
		this.dateList = ko.observableArray(ko.mapping.fromJS(dateList)());
		this.termDate = ko.observable("");  // 班级的日期TITLE
		this.layoutHomeworkTimes = ko.observable("0"); // 布置作业次数
		this.studentList = ko.observableArray([]);
		this.monthLayoutInfoList = ko.observableArray([]);
		this.initSimpleSlider();
	}

	TermReport.prototype = {
		constructor : TermReport,
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
			this.initStudentList();
		},
		initStudentList:function(){
			var self = this;
			// 获取班级中学生信息
			var focusDate;
			ko.utils.arrayForEach(self.dateList(),function(dateObj,i){
				ko.mapping.toJS(dateObj.active)  && (focusDate = dateObj);
			});

			var paramData = {
				groupId     : self.groupId(),
				dateRange   : focusDate ? focusDate.dateRange() : null,
				subject     : self.subject
			};
			var uniqueKey = [paramData.groupId,paramData.dateRange,paramData.subject].join("_");

			$.ajax({
				url:'/teacher/newhomework/report/term.vpage',
				data:{
					groupId     : self.groupId(),
					dateRange   : focusDate ? focusDate.dateRange() : null,
					subject     : self.subject
				},
				success:function(res){
					if(res.success){
						var resData = res.data || {};
						var tempKey = [resData.groupId,resData.dateRange,resData.subject].join("_");
						if(uniqueKey === tempKey){
							self.layoutHomeworkTimes(resData.totalMonthLayoutTimes);
							self.studentList(resData.studentTermReportList);
							self.monthLayoutInfoList(resData.monthLayoutInfoList);
						}
					}else{
						$17.alert(res.info || "学生列表获取失败，请稍后再试！！");
					}
				}
			});
		},
		changeClazz:function(element,self){
			var clazzObj = this;
			if(clazzObj.groupId != self.groupId()){
				self.groupId(clazzObj.groupId);
				self.initStudentList();
			}
		},
		changeTerm:function(element,self){
			var that = this;
			var tempName = ko.mapping.toJS(that.name);
			var lastName;
			ko.utils.arrayForEach(self.dateList(),function(dateObj,i){
				var iName = ko.mapping.toJS(dateObj.name);
				lastName = ko.mapping.toJS(dateObj.active) ? iName : "";
				if(tempName == iName){
					dateObj.active(true);
					self.termDate(ko.mapping.toJS(dateObj.title));
				}else{
					dateObj.active(false);
				}



			});
			(tempName != lastName) && (self.initStudentList());
		},
		generateDownloadUrl : function(){
			var self = this;
			var focusDate;
			ko.utils.arrayForEach(self.dateList(),function(dateObj,i){
				ko.mapping.toJS(dateObj.active)  && (focusDate = dateObj);
			});

			return self.domain + "teacher/newhomework/report/downloadHomeworkTermReport.vpage?" + $.param({
					groupId : self.groupId(),
					subject : self.subject,
					dateRange: focusDate ? focusDate.dateRange() : null
				});
		}
	};

	ko.applyBindings(new TermReport(constantObj),document.getElementById("mainContent"));
});