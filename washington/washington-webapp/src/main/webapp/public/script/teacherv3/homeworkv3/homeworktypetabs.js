/*
 * COMMON(1, ""),
 * EXAM(2, "同步习题"),
 * MENTAL(3, "口算"),
 * UNIT_QUIZ(4, "单元测验"),
 * MID_QUIZ(5, "期中测验"),
 * END_QUIZ(6, "期末测验"),
 * PHOTO_OBJECTIVE(7, "动手做一做"),
 * VOICE_OBJECTIVE(8, "概念说一说"),
 * COMMON_ENGLISH(9, "");
 * READ_RECITE(,"课文读背题");
 *
 */
(function($17,ko) {
	"use strict";
	var HomeworkTypeTabs = function(){
		var self            = this;
        self.sectionIds     = [];
		self.tabs           = ko.observableArray([]);
		self.startPage  	= ko.observable(0);
		self.displayCount 	= 5;
        self.totalPage 		= ko.pureComputed(function(){
            return Math.ceil(self.tabs().length / self.displayCount);
        });
		self.focusTabType   = ko.observable("");
		self.focusTabName   = ko.observable("");
		self.objectiveConfigId = ko.observable("");
		self.currentTabs    = ko.pureComputed(function(){
            var _tabs = self.tabs();
            var _startPage = self.startPage();
            var _newTabs = [];
            if(_tabs.length <= self.displayCount){
                _newTabs = _tabs;
            }else{
                _newTabs = _tabs.slice((_startPage - 1) * self.displayCount,_startPage * self.displayCount);
            }
			return _newTabs;
		});
		self.leftEnabled    = ko.pureComputed(function(){
            //左箭头是否可用
            return self.startPage() > 1;
		});
		self.rightEnabled  = ko.pureComputed(function(){
            //右箭头是否可用
            return self.startPage() < self.totalPage();
		});

	};
	HomeworkTypeTabs.prototype = {
		constructor     : HomeworkTypeTabs,
		param           : {},
		extendTabClick  : function(obj){},
		tabClick        : function(self){
			var that = this;
			if(self.objectiveConfigId() == that.objectiveConfigId()){
				return false;
			}

			$17.voxLog({
				module : "m_H1VyyebB",
				op     : "page_assign_BasicPractice_click",
				s0     : constantObj.subject,
				s1     : that.type()
			});

			self.focusTabType(that.type());
			self.focusTabName(that.typeName());
			self.objectiveConfigId(that.objectiveConfigId());
			self.refresh();
		},
		refresh       : function(){
			var self = this;
			self.extendTabClick($.extend(true,{
				tabType   : self.focusTabType(),
				tabTypeName : self.focusTabName(),
				objectiveConfigId : self.objectiveConfigId()
			},self.param));
		},
		arrowClick      : function(directionOfArrow){
			var self = this;
			var _startPage = self.startPage();
			if(directionOfArrow === "arrowLeft" && self.leftEnabled()){
				self.startPage(_startPage - 1);
			}else if(directionOfArrow === "arrowRight" && self.rightEnabled()){
				self.startPage(_startPage + 1);
			}
		},
		run             : function(){
			var self = this;
			if(!(self.param && self.param.bookId && self.param.unitId)){
				self.tabs([]);
				self.startPage(1);
				return false;
			}
			var typeList = self.param.typeList;
			if(!$.isArray(typeList) || typeList.length === 0){
				self.tabs([]);
                self.startPage(1);
			}
			self.tabs(ko.mapping.fromJS(typeList)());
			//恢复初始，不然当前位置会超过总tabs数
            self.startPage(1);
			if(typeList.length > 0){
				self.focusTabType(typeList[0].type);
				self.focusTabName(typeList[0].typeName);
				self.objectiveConfigId(typeList[0].objectiveConfigId);
				self.refresh();
			}
		},
		initialise      : function(option){
			var self           = this;
			option = option || {};
			self.param = option;
			var _sectionIds = [];
			$.each(option.sections,function(i,section){
				_sectionIds.push(section.sectionId);
			});
			self.sectionIds    = _sectionIds;

		}
	};

	$17.homeworkv3 = $17.homeworkv3 || {};
	$17.extend($17.homeworkv3, {
		getHomeworkTypeTabs: function(){
			return new HomeworkTypeTabs();
		}
	});
}($17,ko));