/**
 * 智能同步习题
 * Created by dell on 2017/7/17.
 */
(function($17,ko) {
	"use strict";
	ko.bindingHandlers.singleExamHover = {
		init: function(element, valueAccessor){
			$(element).hover(
				function(){
					$(element).addClass("current");
					$(element).find("a.feedback").show();
					$(element).find("a.viewExamAnswer").show();
				},
				function(){
					var _value = ko.unwrap(valueAccessor());
					if(!_value){
						$(element).removeClass("current");
					}
					$(element).find("a.feedback").hide();
					$(element).find("a.viewExamAnswer").hide();
				}
			);
		},
		update:function(element, valueAccessor){
			var _value = ko.unwrap(valueAccessor());
			if(_value){
				$(element).addClass("current");
			}else{
				$(element).removeClass("current");
			}
		}
	};
	/*
	 * 下拉框的显示隐藏
	 * */
	ko.bindingHandlers.pullDownHover = {
		init: function(element, valueAccessor){
			$(element).hover(
				function(){
					$(element).find("ul.starBoxSelect").show();
				},
				function(){
					$(element).find("ul.starBoxSelect").hide();
				}
			);
		}
	};
	// Here's a custom Knockout binding that makes elements shown/hidden via jQuery's fadeIn()/fadeOut() methods
	ko.bindingHandlers.fadeVisible = {
		init: function(element, valueAccessor) {
			// Initially set the element to be instantly visible/hidden depending on the value
			var value = valueAccessor();
			$(element).toggle(ko.unwrap(value)); // Use "unwrapObservable" so we can handle values that may or may not be observable
		},
		update: function(element, valueAccessor) {
			// Whenever the value subsequently changes, slowly fade the element in or out
			var value = valueAccessor();
			ko.unwrap(value) ? $(element).fadeIn() : $(element).fadeOut();
		}
	};



	/**
	 * 智能组题搜索项对象
	 * @constructor
	 * @param obj
	 *  {
     *    packageAlgoTypes : 场景数组, 数组中元素对象包含字段 algoType, algoTypeName
     *    knowledgePoints : 知识点数组,数组中元素对象包含字段 id , name,checked
     *    patterns        : 题型数据，数组中元素对象包含字段 id, name, checked
     *  }
	 */
	var SmartSearchItems = function(obj){
		var self = this;
		obj = $.isPlainObject(obj) ? obj : {};
		self.type       = obj.type || ""; //作业类型
		self.subject    = obj.subject || ""; //学科
		self.packageAlgoTypes = ($.isArray(obj.packageAlgoTypes) ? ko.mapping.fromJS(obj.packageAlgoTypes) : ko.observableArray([])); // 场景
		self.algoTypesPatternMap = obj.algoTypesPatternMap || {};
		self.focusAlgoType = ko.observable("");
		self.difficults = ko.observableArray([1,2,3,4,5]);
		self.focusDifficult = ko.observable(1);
		self.minQuestionCnt = 3;
		self.maxQuestionCnt = 50;
		self.questionStepCnt = 1;
		self.questionCnt = ko.observable(15);
		self.oldQuestionCnt = 15;  //题量改变时，记住原有的题量
		self.questionCnt.subscribe(function(oldValue) {
			self.oldQuestionCnt = oldValue;
		}, null, "beforeChange");
		self.questionCnt.subscribe(function(newValue){
			newValue = (+newValue || self.minQuestionCnt);
			if(newValue < self.minQuestionCnt || newValue > self.maxQuestionCnt){
				self.questionCnt(self.oldQuestionCnt);
			}
		});
		self.minusBtnDisabled = ko.pureComputed(function(){
			return self.questionCnt() <= self.minQuestionCnt;
		},self);
		self.addBtnDisabled = ko.pureComputed(function(){
			return self.questionCnt() + self.questionStepCnt > self.maxQuestionCnt;
		},self);
		self.knowledgePoints = ko.observableArray([]);
		/*var checkedAll = true;
		ko.utils.arrayForEach(self.knowledgePoints(),function(point,i){
			checkedAll = (checkedAll && point.checked && point.checked());
		});*/
		self.knowledgePointsAllChecked = ko.observable(false);

		self.patterns        = ko.observableArray([]);
		self.patternIsCheckedAll = ko.pureComputed(function(){
			var _patterns = self.patterns();
			var _checked = true;
			for(var z = 0,zLen = _patterns.length; z < zLen; z++){
				_checked = _checked && _patterns[z].checked();
			}
			return _checked;
		});

		self.goExamCallback = obj.goExamCallback || null;
	};
	SmartSearchItems.prototype = {
		constructor : SmartSearchItems,
		starBoxClick  : function (self) {
			var difficult = this,
				currentDiff = self.focusDifficult();
			var newDifficult = typeof difficult === "object" ? (+difficult.toString() || 1) : difficult;
			if(newDifficult === currentDiff){
				return false;
			}
			self.focusDifficult(newDifficult);
			self._addFilterItemClickLog("难度");
		},
		addQuestionCnt : function(){
			var self = this;
			self.questionCnt(self.questionCnt() + self.questionStepCnt);
			self._addFilterItemClickLog("题量");
		},
		minusQuestionCnt : function(){
			var self = this;
			self.questionCnt(self.questionCnt() - self.questionStepCnt);
			self._addFilterItemClickLog("题量");
		},
		knowledgePointAllCheckedClick : function(){
			var self = this;
			var checkedAll = self.knowledgePointsAllChecked();
			ko.utils.arrayForEach(self.knowledgePoints(),function(point,i){
				point.checked(!checkedAll);
			});
			self.knowledgePointsAllChecked(!checkedAll);
			self._addFilterItemClickLog("知识点");
		},
		knowledgePointClick : function(self){
			var point = this;
			point.checked(!point.checked());

			var checkedAll = true;
			ko.utils.arrayForEach(self.knowledgePoints(),function(point,i){
				checkedAll = (checkedAll && point.checked && point.checked());
			});
			self.knowledgePointsAllChecked(checkedAll);
			self._addFilterItemClickLog("知识点");
		},
		patternAllClick  : function(){
			var self = this;
			var _checkedAll = self.patternIsCheckedAll();
			ko.utils.arrayForEach(self.patterns(),function(pattern,z){
				pattern.checked(!_checkedAll);
			});
			self._addFilterItemClickLog("题型");
		},
		pattern_click    : function(self){
			var that = this;
			that.checked(!that.checked());
			self._addFilterItemClickLog("题型");
		},
		algoTypeClick : function(type){
			var self = this;
			self.setItemsDefaultValue({
				focusAlgoType : type
			});
			self._addFilterItemClickLog("场景");
		},
		_addFilterItemClickLog : function(filterItem){
			var self = this;
			$17.voxLog({
				module  : "m_H1VyyebB",
				op      : "o_jglaMh6V",
				s0      : self.subject,
				s1      : self.type,
				s2      : filterItem
			});
		},
		goExam : function(){
			var self = this,kpIds = [],contentTypeIds = [];
			ko.utils.arrayForEach(self.knowledgePoints(),function(point,i){
				point.checked() && kpIds.push(point.id());
			});
			ko.utils.arrayForEach(self.patterns(),function(pattern,z){
				pattern.checked() && (contentTypeIds.push(pattern.id()));
			});
			$.isFunction(self.goExamCallback) && self.goExamCallback({
				algoType        : self.focusAlgoType(),
				difficulty      : self.focusDifficult(),
				questionCount   : self.questionCnt(),
				kpIds           : kpIds.join(","),
				contentTypeIds  : contentTypeIds.join(",")
			});
		},
		setItemsDefaultValue : function(obj){
			var self = this;
			obj = $.isPlainObject(obj) ? obj : {};
			// obj : {focusAlgoType : 场景类型ID,focusDifficult:难度值,questionCnt:题量}
			var focusAlgoType,knowledgePoints,patterns;
			if(obj.focusAlgoType && self.algoTypesPatternMap.hasOwnProperty(obj.focusAlgoType)){
				focusAlgoType = obj.focusAlgoType;
			}else{
				focusAlgoType = self.packageAlgoTypes()[0].type();
			}
			var mapObj = self.algoTypesPatternMap[focusAlgoType];
			knowledgePoints = mapObj.knowledgePoints;
			patterns = mapObj.contentTypes;
			ko.utils.arrayForEach(patterns,function(pattern,i){
				pattern.checked = true;
			});
			self.focusAlgoType(focusAlgoType);
			obj.focusDifficult && (self.focusDifficult(obj.focusDifficult));
			obj.questionCnt && (self.questionCnt(obj.questionCnt));
			ko.utils.arrayForEach(knowledgePoints,function(point,i){
				point.checked = true;
			});
			self.knowledgePoints(ko.mapping.fromJS(knowledgePoints)());
			self.knowledgePointsAllChecked(true);
			self.patterns(ko.mapping.fromJS(patterns)());
		}
	};

	var IntelligenceExam = function(){
		var self = this;
		self.tabType         = "";
		self.examLoading     = ko.observable(true); //正在加载应试
		self.sectionIds      = [];
		self.loadExamInitialize = false;
		self.packageList     = ko.observableArray([]);
		self.packageQuestionsMap = {};
		self.examQuestions   = [];   //更多题目中全部的应试题
		self.questionList    = [];   //筛选后符合条件的应试题集
		self.currentPage     = ko.observable(1);
		self.userInputPage   = ko.observable(null);
		self.focusExamList   = ko.observableArray([]);
		self.focusExamMap = {};
		self.focusExamList.subscribe(self.setExamChecked,self);
		self.focusPackageIndex = ko.observable(0); //当前焦点题包在packageList的下标
		self.focusPackage = ko.pureComputed(function(){
			return self.packageList()[self.focusPackageIndex()];
		});
		self.totalPage = ko.observable(0);
		self.patterns        = ko.observableArray([]);
		self.patternIsCheckedAll = ko.pureComputed(function(){
			var _patterns = self.patterns();
			var _checked = true;
			for(var z = 0,zLen = _patterns.length; z < zLen; z++){
				_checked = _checked && _patterns[z].checked();
			}
			return _checked;
		});
		self.difficulties           = ko.observableArray([]);
		self.difficultyIsCheckedAll = ko.pureComputed(function(){
			var _difficulties = self.difficulties();
			var _checked = true;
			for(var z = 0,zLen = _difficulties.length; z < zLen; z++){
				_checked = _checked && _difficulties[z].checked();
			}
			return _checked;
		});
		self.assignIsCheckedAll = ko.pureComputed(function(){
			var _assigns = self.assigns();
			var _checked = true;
			for(var z = 0,zLen = _assigns.length; z < zLen; z++){
				_checked = _checked && _assigns[z].checked();
			}
			return _checked;
		});
		self.knowledgePoints = ko.observable([]);
		self.focusPointId    = ko.observable(null);

		self.smartSearchItems = null;
		self.displayAdvancedOptions = ko.observable(false);
		self.smartSearchLoading = ko.observable(false);
		self.smartSearchResponseInfo = ko.observable("");
		self.clazzGroupIdsStr = null;
		self.subject = constantObj.subject;
	};
	IntelligenceExam.prototype = {
		constructor       : IntelligenceExam,
		param             : {},
		assigns           : ko.observableArray([]),
		customPackage     : {
			id: "-2",  //虚拟题包ID
			name: "自定义智能组题",
			difficulty:1,
			selCount: 0,
			totalCount: 0,
			assignTimes: 0,
			totalMin: 0,
			teacherUsed: false,
			usageName: '',
			usageColor: '',
			algoType  : '',
			flag: "smart_exam_questions"
		},
		getQuestion : function(examId){
            var self = this;
			var questionObj = self.focusExamMap[examId];
			if(!questionObj){
				return 	[];
			}
			var questions = questionObj.questions;
			if(!$.isArray(questions) || questions.length === 0){
				return [];
			}
			return questions.slice(0,1);
		},
		run               : function(obj){
			var self = this,paramData = {
				bookId   : self.param.bookId,
				unitId   : self.param.unitId,
				sections : self.sectionIds.toString(),
				type     : self.tabType,
				subject  : constantObj.subject,
				clazzs   : null,
				objectiveConfigId : self.param.objectiveConfigId
			};
			obj = $.isPlainObject(obj) ? obj : {};
			self.clazzGroupIdsStr = obj.clazzGroupIdsStr;
			paramData.clazzs = obj.clazzGroupIdsStr;

			self.examLoading(true);
			self.displayAdvancedOptions(false);
			self.smartSearchResponseInfo("");
			$.get("/teacher/new/homework/objective/content.vpage", paramData, function(data){
				if(data.success){
					var _boxQuestionMap = self._getBoxQuestionMap(),
						_content = data.content || [],
						_packages = [],
						moreQuestions = [],
						smartSearchAlgoTypes = [],
						smartSearchKnowledgePoints = [];
					var algoTypesPatternMap = {};
					for(var i = 0,iLen = _content.length; i < iLen; i++){
						if(_content[i].type === "package"){
							var _realPackages = _content[i].packages || [];
							for(var z = 0, zLen = _realPackages.length; z < zLen; z++){
								var _packageObj = {},
									_boxId = _realPackages[z].id,
									totalSec = _realPackages[z].seconds || 0;
								_packageObj["id"] = _boxId;
								_packageObj["name"] = _realPackages[z].name;
								_packageObj["selCount"] = _boxQuestionMap[_realPackages[z].id] ? _boxQuestionMap[_realPackages[z].id].length : 0;
								_packageObj["flag"] = "package";
								_packageObj["totalCount"] = _realPackages[z].questions.length || 0;
								//题目是否全部使用过
								_packageObj["teacherUsed"] = _realPackages[z].showAssigned || false;
								_packageObj["totalMin"] = Math.ceil(totalSec/60);
								_packageObj["usageName"] = _realPackages[z].usageName || '';
								_packageObj['usageColor'] = _realPackages[z].usageColor || '';
								_packageObj['difficulty'] = (+_realPackages[z].difficulty || 0);
								_packageObj['algoType'] = _realPackages[z].algoType || "";
								_packages.push(_packageObj);
								self.packageQuestionsMap[_boxId] = _realPackages[z].questions || [];
								algoTypesPatternMap[_realPackages[z].algoType] = {
									contentTypes : $.isArray(_realPackages[z].contentTypes) ? _realPackages[z].contentTypes : [],
									knowledgePoints : $.isArray(_realPackages[z].knowledgePoints) ? _realPackages[z].knowledgePoints : []
								};
								smartSearchAlgoTypes.push({
									type    : _realPackages[z].algoType,
									name    : _realPackages[z].algoTypeName
								});
							}
						}else if(_content[i].type === "question"){
							moreQuestions = _content[i].questions || [];

							var _kpCategories = _content[i].knowledgePoints || [];
							var pointCnt = 0;
							for(var t = 0,tLen = _kpCategories.length; t < tLen; t++){
								var points = _kpCategories[t].knowledgePoints || [];
								for(var m = 0,mLen = points.length; m < mLen; m++){
									pointCnt++;
								}
							}
							if(pointCnt > 800){
								$17.voxLog({
									module: "unit_point_too_much",
									op : "exam_tab",
									bookId : self.param.bookId,
									unitId : self.param.unitId
								});
							}else{
								self.knowledgePoints(ko.mapping.fromJS(_kpCategories)());
							}

							var _patterns = _content[i].questionTypes || [];
							for(var s = 0,sLen = _patterns.length; s < sLen; s++){
								_patterns[s]["checked"] = false;
							}
							_patterns.length > 0 && (self.patterns(ko.mapping.fromJS(_patterns)()));
						}
					}

					//自定义智能组题
					if(_packages.length > 0){
						_packages.push(self.customPackage);
						self.smartSearchItems = new SmartSearchItems({
							type                : self.tabType,
							subject             : constantObj.subject,
							packageAlgoTypes    : smartSearchAlgoTypes,
							algoTypesPatternMap : algoTypesPatternMap,
							goExamCallback      : self.goExamCallbackFn.bind(self)
						});	
					}
					

					if(moreQuestions.length > 0) {
						self.packageQuestionsMap["-1"] = moreQuestions;
						_packages.push({
							id: "-1",  //虚拟题包ID
							name: "更多题目",
							selCount: _boxQuestionMap["-1"] ? _boxQuestionMap["-1"].length : 0,
							totalCount: 0,
							assignTimes: 0,
							totalMin: 0,
							teacherUsed: false,
							difficulty:0,
							usageName: '',
							usageColor: '',
							algoType  : '',
							flag: "more_question"
						});
					}
					self.packageList(ko.mapping.fromJS(_packages)());
					if(_packages.length > 0){
						self.forwardSpecialPackage.call(self.focusPackage(),self,self.focusPackageIndex());
					}
				}else{
					self.packageList([]);
					self.questionList = [];
					data.errorCode !== "200" && $17.voxLog( {
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/new/homework/content.vpage",
						s1     : $.toJSON(data),
						s2     : $.toJSON(paramData),
						s3     : $uper.env
					});
				}
				self.examLoading(false);
			});
		},
		_resetExamList : function(_questions){
			var self = this;
			if(!$.isArray(_questions)){
				_questions = [];
			}
			for(var k = 0,kLen = _questions.length; k < kLen; k++){
				_questions[k]["checked"] = false;
				_questions[k]["upImage"] = _questions[k].upImage || false;
			}
			self.examQuestions = _questions;
			var startPage = 1;
			self.currentPage(startPage);
			var _startIndex = (startPage - 1) * 5;
			var _subExamQuestions = _questions.slice(_startIndex, _startIndex + 5);

			self.questionList = _questions;

			self._fillFocusExamList(_subExamQuestions,function(){
                self.totalPage(Math.ceil(_questions.length/5));
                self.userInputPage(null);
			});

		},
		_fillFocusExamList: function(questions,callback){
			var self = this;
            var questionIds = [];
            for(var m = 0; m < questions.length; m++){
                questionIds.push(questions[m].id);
            }

            $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                self.focusExamMap = result.success ? result.questionMap : {};
                self.focusExamList(ko.mapping.fromJS(questions)());
                $.isFunction(callback) && callback();
            });
		},
		_getBoxQuestionMap : function(){
			var self = this;
			var _exams = constantObj._homeworkContent.practices[self.tabType].questions || [];
			var _packageDetail = {};
			for(var z = 0,zLen = _exams.length; z < zLen; z++){
				var _questionBoxId = _exams[z].questionBoxId;
				if(!$17.isBlank(_questionBoxId)){
					if(!$.isArray(_packageDetail[_questionBoxId])){
						_packageDetail[_questionBoxId] = [];
					}
					_packageDetail[_questionBoxId].push(_exams[z].questionId);
				}
			}
			return _packageDetail;
		},
		_isExistsByQuestionId : function(questionId){
			var self = this;
			var _exams = constantObj._homeworkContent.practices[self.tabType].questions || [];
			var _index = -1;
			for(var z = 0,zLen = _exams.length; z < zLen; z++){
				if(_exams[z].questionId && _exams[z].questionId === questionId){
					_index = z;
					break;
				}
			}
			return _index;
		},
		_setPackageProperties : function(customPackage,questions){
			var self = this;
			self.packageList()[self.focusPackageIndex()].id(customPackage.id);
			self.packageList()[self.focusPackageIndex()].selCount(customPackage.selCount);
			self.packageList()[self.focusPackageIndex()].totalCount(customPackage.totalCount);
			self.packageList()[self.focusPackageIndex()].totalMin(customPackage.totalMin);
			self.packageList()[self.focusPackageIndex()].difficulty(customPackage.difficulty);
		},
		point_click: function(element,self,kpType){
			var _point = this;
			if(self.examLoading()){
				$17.alert("处理中,请稍候");
				return false;
			}
			self.examLoading(true);
			if($(element).hasClass("active")){
				self.focusPointId(null);
			}else{
				self.focusPointId(_point.kpId());
			}
			self.examFilter();
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "page_assign_tongbu_practice_usedSift_click",
				s0 : constantObj.subject,
				s1 : kpType,
				s2 : _point.kpId()
			});
			self.examLoading(false);
		},
		goExamCallbackFn : function(obj){
			var self =  this,paramData = {
				bookId   : self.param.bookId,
				unitId   : self.param.unitId,
				sections : self.sectionIds.toString(),
				type     : self.tabType,
				subject  : constantObj.subject,
				clazzs   : self.clazzGroupIdsStr,
				difficulty : null
			};
			!$.isEmptyObject(obj) && (paramData = $.extend(true,paramData,obj));
			self.smartSearchLoading(true);
			$.post("/teacher/new/homework/intelligence/question.vpage",paramData,function(data){
				var questions = [];
				if(data.success){
					questions = $.isArray(data.questions) ? data.questions : [];
					var newPackageObj = $.extend(true,{},self.customPackage,{
						id : (data.id || ("CUS_" + (10000000 + Math.ceil(Math.random()*100000)))),
						totalCount : questions.length,
						totalMin : Math.ceil((+data.seconds || 0)/60),
						difficulty : (paramData.difficulty || self.currentPage.difficulty)
					});
					self.packageQuestionsMap[newPackageObj.id] = questions;
					self._setPackageProperties(newPackageObj);
					self.displayAdvancedOptions(false);

					$17.voxLog({
						module: "m_H1VyyebB",
						op : "assignhomework_tongbu_SmartProQues_start_click",
						s0 : constantObj.subject,
						s1 : data.id,
						s2 : $.toJSON(paramData)
					});
					self.smartSearchResponseInfo("");
				}else{
					self._setPackageProperties(self.customPackage);
					self.smartSearchResponseInfo(data.info || "没有找到题目");
				}
				self._resetExamList(questions);
				self.smartSearchLoading(false);
			}).fail(function(){
				self.smartSearchResponseInfo("请稍候刷新重试");
				self.smartSearchLoading(false);
			});
		},
		forwardSmartSearch : function(){
			var self = this,
				packageList = self.packageList();
			for(var i = packageList.length - 1; i >= 0; i--){
				if(packageList[i].flag() === "smart_exam_questions"){
					self.forwardSpecialPackage.call(packageList[i],self,i);
					break;
				}
			}
		},
		forwardSpecialPackage : function(self,index){
			var that = this,
				focusPackage = self.focusPackage(),
				_focusBoxId = that.id(); //this -> package obj
			if(that.flag() === "smart_exam_questions"){
				_focusBoxId = self.customPackage.id;
				var questionCnt = (+focusPackage.totalCount() || 15);
				that.id(self.customPackage.id);
				that.totalCount(self.customPackage.totalCount);
				that.totalMin(self.customPackage.totalMin);
				// obj : {focusAlgoType : 场景类型ID,focusDifficult:难度值,questionCnt:题量}
				self.smartSearchItems.setItemsDefaultValue({
					focusAlgoType  : focusPackage.flag() === 'more_question' ? "" : focusPackage.algoType(),
					focusDifficult : focusPackage.flag() === 'more_question' ? 1  : focusPackage.difficulty(),
					questionCnt    : questionCnt
				});
			}
			self.smartSearchResponseInfo("");
			self.displayAdvancedOptions(that.flag() === "smart_exam_questions");
			self.focusPackageIndex(index);
			self._resetExamList(self.packageQuestionsMap[_focusBoxId]);
		},
		viewPackage : function(self,index){
			var that = this,
				_focusPackageIndex = self.focusPackageIndex();
			if(_focusPackageIndex == index){
				//同一包的点击
				return false;
			}
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "assignhomework_tongbu_Scenes_click",
				s0 : constantObj.subject,
				s1 : that.name(),
				s2 : that.id()
			});
			self.forwardSpecialPackage.call(that,self,index);
		},
		addOrRemovePackage  : function(){
			var self = this,
				that = self.focusPackage(),
				_selCount = that.selCount(),
				_totalCount = that.totalCount(),
				_packageId = that.id(),
				_questions = self.packageQuestionsMap[_packageId] || [];
			if(_selCount >= _totalCount){
				//取消勾选
				var removeSeconds = 0;
				if(_questions.length > 0){
					for(var t = 0,tLen = _questions.length; t < tLen; t++){
						if(self._removeExam(_packageId,_questions[t])){
							//有，删除
							removeSeconds += _questions[t].seconds;
						}
					}
					self.setExamChecked();
				}
				self.packageList()[self.focusPackageIndex()].selCount(0);

				//更新UFO_EXAM
				self.updateUfoExam(0 - removeSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);

				$17.voxLog({
					module: "m_H1VyyebB",
					op    : "assignhomework_tongbu_PackageDetail_DelselectAll_click",
					s0    : constantObj.subject,
					s1    : that.name(),
					s2    : _packageId
				});

			}else{
				//全选
				var addSeconds = 0;
				var cnt = 0;
				var beenSelected = 0; //在其他题包已选过计数,同题包中不会出现重题
				if(_questions.length > 0){
					var _boxSelQuestionMap = self._getBoxQuestionMap();
					var _boxSelQuestions = _boxSelQuestionMap[_packageId] || [];
					for(var z = 0,zLen = _questions.length; z < zLen; z++){
						var qId = _questions[z].id;
						var existsQuestionFlag =  self._isExistsByQuestionId(qId);
						if(existsQuestionFlag == -1 && self._addExam(_packageId,_questions[z]).success){
							addSeconds +=  _questions[z].seconds;
							cnt++;
						}else if(existsQuestionFlag != -1 && _boxSelQuestions.indexOf(qId) == -1){
							beenSelected++;
						}
					}
					self.setExamChecked();
				}
				cnt = self.packageList()[self.focusPackageIndex()].selCount() + cnt;
				self.packageList()[self.focusPackageIndex()].selCount(cnt);
				//更新UFO_EXAM
				self.updateUfoExam(addSeconds,constantObj._homeworkContent.practices[self.tabType].questions.length);
				if(beenSelected > 0){
					$17.alert("有" + beenSelected + "道题与已选题目重复");
				}
				$17.voxLog({
					module: "m_H1VyyebB",
					op    : "assignhomework_tongbu_PackageDetail_selectAll_click",
					s0    : constantObj.subject,
					s1    : that.name(),
					s2    :  _packageId
				});
			}

		},
		setExamChecked : function(){
			//设置当前页面显示的题的选中状态
			var self = this; //Exam
			$17.info("设置当前页中题的状态");
			var _boxQuestionMap = self._getBoxQuestionMap();
			var _selectQuestions = self.focusPackage() ? (_boxQuestionMap[self.focusPackage().id()] || []) : [];

			var _focusExamList = self.focusExamList();
			for(var z = 0,zLen = _focusExamList.length; z < zLen; z++){
				var _questionId = _focusExamList[z].id();

				var _checked = _focusExamList[z].checked();
				if(_selectQuestions.length > 0 && !_checked && _selectQuestions.indexOf(_questionId) != -1){
					self.focusExamList()[z].checked(true);
				}else if(_selectQuestions.length == 0 && _checked){
					self.focusExamList()[z].checked(false);
				}
			}
		},
		updateUfoExam : function(sec,questionCnt){
			var self = this;
			constantObj._moduleSeconds[self.tabType] = constantObj._moduleSeconds[self.tabType] + sec;
			self.carts
			&& typeof self.carts["recalculate"] === 'function'
			&& self.carts.recalculate(self.tabType,questionCnt);
		},
		_getSpecialBoxInfo : function(boxId,questionId){
			//返回指定的包ID下选择的题数，指定题ID在指定包中的下标，没有返回-1
			var self = this;
			var _questions = constantObj._homeworkContent.practices[self.tabType].questions;
			var _questionIndex = -1;
			var cnt = 0;
			for(var m = 0,mLen = _questions.length; m < mLen; m++){
				if(_questions[m].questionBoxId === boxId){
					cnt++;
					if(_questions[m].questionId === questionId && _questionIndex == -1){
						_questionIndex = m;
					}
				}
			}
			return {
				selectCount : cnt,
				questionIndex : _questionIndex
			};
		},
		_addExam    : function(currentBoxId,question){
			var self = this,param = self.param || {};
			//内部方法，约定currentBoxId,question合法的,currentBoxId,question的合法性放在外面判断
			var _questionId = question.id,
				ids = [],
				existsQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
			existsQuestions = $.isArray(existsQuestions) ? existsQuestions : [];
			$.each(existsQuestions,function(i,question){
				ids.push(question.questionId);
			});
			if(ids.indexOf(_questionId) == -1){
				var _similarIds = question.similarQuestionIds || [],
					_questionObj = {
						questionId          : _questionId,
						seconds             : question.seconds,
						submitWay           : question.submitWay,
						questionBoxId       : currentBoxId,
						similarQuestionId   : _similarIds.length > 0 ? _similarIds[0] : null,
						book                : question.book || null,
						objectiveId         : param.objectiveTabType
					};
				constantObj._homeworkContent.practices[self.tabType].questions.push(_questionObj);
				constantObj._reviewQuestions[self.tabType].push(question);
				return {
					success : true,
					info    : "添加成功"
				};
			}else{
				return {
					success : false,
					info    : "添加失败"
				};
			}
		},
		addExam     : function(self,element){
			var that = this;
			var _currentBoxId = null;
			if(self.focusPackage() && self.focusPackage().id){
				_currentBoxId = self.focusPackage().id();
			}

			var _question = ko.mapping.toJS(that);
			var existsQuestionFlag =  self._isExistsByQuestionId(_question.id);
			if(existsQuestionFlag == -1 && self._addExam(_currentBoxId,_question).success){
				that.checked(true);
				var cnt = self.packageList()[self.focusPackageIndex()].selCount() + 1;
				self.packageList()[self.focusPackageIndex()].selCount(cnt);

				var _questionsInCart = constantObj._homeworkContent.practices[self.tabType].questions || [];
				self.updateUfoExam(that.seconds(),_questionsInCart.length);

				$(element).closest(".examTopicBox").fly({
					target: ".J_UFOInfo p[type='" + self.tabType + "']",
					border: "5px #39f solid",
					time  : 600
				});
			}else if(existsQuestionFlag != -1){
				$17.alert("该题与已选题目重复");
			}

			$17.voxLog({
				module: "m_H1VyyebB",
				op    : "assignhomework_tongbu_PackageDetail_ques_select_click",
				s0    : constantObj.subject,
				s1    : self.focusPackage().name(),
				s2    : self.focusPackage().id(),
				s3    : _question.id
			});

		},
		_removeExam : function(currentBoxId,question){
			var self = this;
			var _questionId = question.id;
			var _tempObj = self._getSpecialBoxInfo(currentBoxId,_questionId);
			var _questionIndex = _tempObj.questionIndex;
			if(_questionIndex != -1){
				constantObj._homeworkContent.practices[self.tabType].questions.splice(_questionIndex,1);

				$.each(constantObj._reviewQuestions[self.tabType],function(i){
					if(this.id == _questionId){
						constantObj._reviewQuestions[self.tabType].splice(i,1);
						return false;
					}
				});
				return true;
			}else{
				$17.info("已经移除过这道题了");
			}
			return false;
		},
		removeExam : function(self){
			var that = this;
			var _questionId = that.id();
			that.checked(false);
			var _boxId = self.focusPackage().id();
			var _question = ko.mapping.toJS(that);
			if(self._removeExam(_boxId,_question)){
				var cnt = self.packageList()[self.focusPackageIndex()].selCount();
				self.packageList()[self.focusPackageIndex()].selCount(cnt > 0 ? cnt - 1 : 0);
				var _questionsInCart = constantObj._homeworkContent.practices[self.tabType].questions || [];
				self.updateUfoExam(0 - that.seconds(),_questionsInCart.length);
			}else{
				$17.info("这道题不在小车中");
			}
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "assignhomework_tongbu_PackageDetail_ques_Delselect_click",
				s0 : constantObj.subject,
				s1 : self.focusPackage().name(),
				s2 : _boxId,
				s3 : _questionId
			});
		},
		clearAll  : function(){
			var self = this;
			var _packageList = self.packageList();
			for(var z = 0,zLen = _packageList.length; z < zLen; z++){
				_packageList[z].selCount(0);
			}
			constantObj._homeworkContent.practices[self.tabType].questions = [];
			constantObj._reviewQuestions[self.tabType] = [];
			self.setExamChecked();
		},
		goSpecifiedPage : function(){
			var self = this; //Exam
			var pageNo = self.userInputPage();
			if(/\D/g.test(pageNo)){
				self.userInputPage(null);
			}else{
				self.page_click(self,pageNo);
			}
		},
		page_click : function(self,pageNo){
			pageNo = +pageNo || 0;
			if(pageNo < 1 || pageNo > self.totalPage() || pageNo == self.currentPage()){
				return false;
			}
			self.currentPage(pageNo);
			var _startIndex = (pageNo - 1) * 5;
            self._fillFocusExamList(self.questionList.slice(_startIndex,_startIndex + 5));
		},
		pattern_click    : function(self){
			var that = this;
			if(self.examLoading()){
				$17.alert("处理中,请稍候");
				return false;
			}
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "page_assign_tongbu_practice_typeSift_click",
				s0 : constantObj.subject,
				s1 : self.tabType,
				s2 : that.id()
			});
			self.examLoading(true);
			that.checked(!that.checked());
			self.examFilter();
			self.examLoading(false);
		},
		patternAllClick  : function(){
			var self = this;
			if(self.examLoading()){
				$17.alert("处理中,请稍候");
				return false;
			}
			self.examLoading(true);
			var _checkedAll = self.patternIsCheckedAll();
			var _patterns = self.patterns();
			for(var z = 0,zLen = _patterns.length; z < zLen; z++){
				_patterns[z].checked(!_checkedAll);
			}
			self.examFilter();
			self.examLoading(false);
		},
		difficulty_click : function(self){
			var that = this;
			if(self.examLoading()){
				$17.alert("处理中,请稍候");
				return false;
			}

			self.examLoading(true);
			that.checked(!that.checked());
			self.examFilter();
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "page_assign_tongbu_practice_difficultySift_click",
				s0 : constantObj.subject,
				s1 : self.tabType,
				s2 : that.value()
			});
			self.examLoading(false);
		},
		difficultyAllClick : function(){
			var self = this;
			if(self.examLoading()){
				$17.alert("处理中,请稍候");
				return false;
			}
			self.examLoading(true);
			var _checkedAll = self.difficultyIsCheckedAll();
			var _difficulties = self.difficulties();
			for(var z = 0,zLen = _difficulties.length; z < zLen; z++){
				_difficulties[z].checked(!_checkedAll);
			}
			self.examFilter();
			self.examLoading(false);
		},
		assign_click     : function(self){
			var that = this;
			that.checked(!that.checked());
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "page_assign_tongbu_practice_usedSift_click",
				s0 : constantObj.subject,
				s1 : self.tabType,
				s2 : that.name()
			});
			self.examFilter();
		},
		assignAllClick : function(){
			var self = this;
			var _checkedAll = self.assignIsCheckedAll();
			var assigns = self.assigns();
			for(var k = 0,kLen = assigns.length; k < kLen; k++){
				assigns[k].checked(!_checkedAll);
			}
			self.examFilter();
		},
		examFilter : function(){
			var self = this;
			self.examLoading(true);
			var patternIds = [],
				diffculties = [],
				assignKeys = [],
				_patterns = self.patterns(),
				_difficulties = self.difficulties(),
				_assigns = self.assigns();
			for(var z = 0, zLen = _patterns.length; z < zLen; z++){
				if(_patterns[z].checked()){
					patternIds.push(_patterns[z].id());
				}
			}

			for(var k = 0,kLen = _difficulties.length; k < kLen; k++){
				if(_difficulties[k].checked()){
					diffculties = $.merge(diffculties,_difficulties[k].value());
				}
			}
			for(var s = 0,sLen = _assigns.length; s < sLen; s++){
				if(_assigns[s].checked()){
					assignKeys.push(_assigns[s].key());
				}
			}

			var _examQuestions = self.examQuestions;

			var filterQuestions = [];
			var _focusPointId = self.focusPointId();
			for(var t = 0,tLen = _examQuestions.length; t < tLen; t++){
				var _question = _examQuestions[t];

				var questionPoints = _question.knowledgePoints || [];
				if(_focusPointId && (questionPoints.length === 0 || questionPoints.indexOf(_focusPointId) === -1)){
					continue;
				}

				if(patternIds.length > 0 && $.inArray(_question.questionTypeId,patternIds) === -1){
					continue;
				}
				if(diffculties.length > 0 && $.inArray(_question.difficulty,diffculties) === -1){
					continue;
				}
				var assign = (_question.teacherAssignTimes > 0) ? 1 : 0;
				if(assignKeys.length > 0 && $.inArray(assign,assignKeys) === -1){
					continue;
				}

				filterQuestions.push(_question);
			}
			self.currentPage(1);
			self.totalPage(Math.ceil(filterQuestions.length/5));
			self.questionList = filterQuestions;
			self._fillFocusExamList(filterQuestions.slice(0,5));

			self.examLoading(false);
		},
		renderExam        : function(examId,node){
			return vox.exam.render(node, 'normal', {
				ids       : [examId],
				imgDomain : constantObj.imgDomain,
				env       : constantObj.env,
				domain    : constantObj.domain
			});
		},
		initialise        : function(option){
			var self = this;
			option = option || {};
			self.param = option;
			self.tabType = option.tabType; //必传字段
			var _sectionIds = [];
			$.each(option.sections,function(i,section){
				_sectionIds.push(section.sectionId);
			});
			self.sectionIds  = _sectionIds;
			var _difficulties = [{name : "容易",value:[1,2],checked:false},
				{name : "中等",value:[3],checked:false},
				{name : "困难",value:[4,5],checked:false}];
			self.difficulties(ko.mapping.fromJS(_difficulties)());

			var assigns = [{key : 1, name:"已推荐", checked : false},{key : 0,name : "未推荐",checked : false}];
			self.assigns(ko.mapping.fromJS(assigns)());
			self.loadExamInitialize = option.examInitComplete || false;

			//换单元，页码初始化,初始化题包
			self.currentPage(1);
			self.focusPackageIndex(0);

			self.carts = option.carts || null;
			//初始化
			var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
			if($ufoexam.has("span").length == 0){
				$ufoexam.empty().html(template("t:UFO_EXAM",{tabTypeName : option.tabTypeName,count : 0}));
			}
		},
		feedback : function(self){
			var that = this; //single exam
			var _questionId = that.id();
			var _currentBoxId = null;
			if(self.focusPackage() && self.focusPackage().id){
				_currentBoxId = self.focusPackage().id();
			}
			$.prompt("<div><span class='text_blue'>如果您发现题目出错了，请及时反馈给我们，感谢您的支持！</span><textarea id='feedbackContent' cols='91' rows='8' style='width: 94%' class='int_vox'></textarea><p class='init text_red'></p></div>", {
				title: "错题反馈", focus: 1, buttons: {"取消": false, "提交": true}, submit: function(e, v){
					if(v){
						var feedbackContent = $("#feedbackContent")
							,paramData = {
							feedbackType: 4,
							examId      : that.id(),
							content     : feedbackContent.val()
						};
						if($17.isBlank(feedbackContent.val())){
							feedbackContent.siblings(".init").html("错题反馈不能为空。");
							feedbackContent.focus();
							return false;
						}
						$.post("/project/examfeedback.vpage", paramData, function(data){
							if(data.success){
								$17.alert("提交成功，感谢您的支持！");
							}else{
								$17.voxLog({
									module : "API_REQUEST_ERROR",
									op     : "API_STATE_ERROR",
									s0     : "/project/examfeedback.vpage",
									s1     : $.toJSON(data),
									s2     : $.toJSON(paramData),
									s3     : $uper.env
								});
							}
						});
						$17.voxLog({
							module: "m_H1VyyebB",
							op    : "page_assign_tongbu_feedback_popup_submit_click",
							s0    : constantObj.subject,
							s1    : self.tabType,
							s2    : _currentBoxId,
							s3    : _questionId
						});
					}
				},
				loaded : function(){
					$17.voxLog({
						module: "m_H1VyyebB",
						op    : "page_assign_tongbu_package_feedback_popup_show",
						s0    : constantObj.subject,
						s1    : self.tabType,
						s2    : _currentBoxId,
						s3    : _questionId
					});
				}
			});

			$17.voxLog({
				module: "m_H1VyyebB",
				op    : "page_assign_tongbu_package_feedback_click",
				s0    : constantObj.subject,
				s1    : self.tabType,
				s2    : _currentBoxId,
				s3    : _questionId
			});
		},
		viewExamAnswer : function(self,index){
			var that = this; //single exam
			var _questionId = that.id();
			var _currentBoxId = null;
			if(self.focusPackage() && self.focusPackage().id){
				_currentBoxId = self.focusPackage().id();
			}
            var gameUrl = "/teacher/new/homework/viewquestion.vpage?" + $.param({qids:_questionId});
            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';
            $.prompt(data, {
                title   : "预 览",
                buttons : {},
                position: { width: 740 },
                close   : function(){
                    $('iframe').each(function(){
                        var win = this.contentWindow || this;
                        if(win.destroyHomeworkJavascriptObject){
                            win.destroyHomeworkJavascriptObject();
                        }
                    });
                }
            });

			$17.voxLog({
				module: "m_H1VyyebB",
				op    : "page_assign_tongbu_question_package_answerKey_click",
				s0    : constantObj.subject,
				s1    : self.tabType,
				s2    : _currentBoxId,
				s3    : _questionId
			});
		}
	};

	$17.homeworkv3 = $17.homeworkv3 || {};
	$17.extend($17.homeworkv3, {
		getBasic_knowledge : function(){
			return new IntelligenceExam();
		},
		getIntelligence_exam  : function(){
			return new IntelligenceExam();
		},
        getOral_practice  : function(){
            return new IntelligenceExam();
        }
	});
}($17,ko));
