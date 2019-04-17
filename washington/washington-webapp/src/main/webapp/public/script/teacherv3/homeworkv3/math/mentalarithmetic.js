/**
 * 开发的一个口算作业类型
 * Created by dell on 2017/12/28.
 */
$(function(){
	"use strict";
	if(!$17.isBlank(ko)){
		ko.bindingHandlers.exchangeHover = {
			update : function(element,valueAccessor){
				var value = ko.unwrap(valueAccessor());
				if(!value){
					$(element).unbind('mouseenter').unbind('mouseleave');
				}else{
					$(element).hover(
						function(){
							$(element).css({background : '#F2F9FD'}).children(".exchange").show();
						},
						function(){
							$(element).css({background : '#FFF'}).children(".exchange").hide();
						}
					);
				}
			}
		};
	}

	var QuestionDB = (function(){
		var questionMap = {};   //用来存储每道题的详细信息，即题库数据
		return {
			addQuestions : function(questionIds,callback){
				callback = $.isFunction(callback) ? callback : function(){};
				if(!$.isArray(questionIds) || questionIds.length == 0){
					callback({
						success : true,
						info    : "题目为空"
					});
					return false;
				}
				var unLoadQuestionIds = [];
				for(var m = 0,mLen = questionIds.length; m < mLen; m++){
					!questionMap.hasOwnProperty(questionIds[m]) && unLoadQuestionIds.push(questionIds[m]);
				}
				$.get("/exam/flash/load/newquestion/byids.vpage",{
					data:JSON.stringify({ids: unLoadQuestionIds,containsAnswer:false})
				}).done(function(res){
					if(res.success){
						var result = res.result;
						for(var t = 0,tLen = result.length; t < tLen; t++){
							questionMap[result[t].id] = result[t];
						}
					}
					callback({
						success : res.success
					});
				}).fail(function(e){
					callback({
						success : false,
						info    : e.message
					});
				});
			},
			deleteQuestions : function(questionIds){
				if(!$.isArray(questionIds) || questionIds.length == 0){
					return false;
				}
				for(var m = 0,mLen = questionIds.length; m < mLen; m++){
					delete questionMap[questionIds[m]];
				}
				return true;
			},
			getQuestionById : function(questionId){
				return questionMap[questionId] || null;
			}
		}
	}());


	var subject = constantObj.subject;
	//pointList中每个point扩展assignCount,teacherAssignTimes 字段
	function extendPoint(point){
		if(!point.hasOwnProperty("assignCount")){
			point["assignCount"] = 0;
		}
		if(!point.hasOwnProperty("teacherAssignTimes")){
			point["teacherAssignTimes"] = 0;
		}
		return point;
	}

	function clearTipPopup(resolve,reject,title){
		var popState = {
			state0 : {
				name    : 'timeLimit_mentalarithmetic',
				comment : '限时口算提示',
				html    : title || '推荐题包和自定义为二选一，继续更改会清空已选入口算题，以防题目重复',
				title   : '限时口算',
				position: { width : 550},
				focus   : 1,
				buttons : {"取消" : false,"更改" : true},
				submit  : function(e,v,f,m){
					if(v){
						$.isFunction(resolve) && resolve();
					}else{
						$.isFunction(reject) && reject();
					}
				}
			}
		};

		$.prompt(popState,{
			loaded : function(event){
				$17.voxLog({
					module  : "m_H1VyyebB",
					op      : "ah_mental_swich_alert",
					subject : subject
				});
			}
		});
	}

	/***
	 *  pointAndAssignQuestionsRef 值：
	 *  "KP_10200101197244" : [{
	 *              questionContent : "",
	 *              questionId : "",
	 *              seconds : 20
	 *          }]
	 * @param option
	 * @constructor
	 */
	function MentalPoints(option){
		var defaultOpt = {
			subject                 : null,
			param                   : null,
			clearAll                : null,
			addMentalQuestionCb     : null,
			removeMentalQuestionCb  : null
		};
		this.opts       = $.extend(true,{},defaultOpt,option);
		this.pointList  = ko.observableArray([]);
		this.pointAndAssignQuestionsRef = {};  // key : 知识点ID, value:带有题目的知识点对象
		//知识点树
		this.pointIds       = ko.observableArray([]);
		this.points         = [];
		this.pointTree      = ko.observableArray([]);
		this.unitIds        = ko.observableArray([]); //知识树弹窗，单元的展开收起
	}
	MentalPoints.prototype = {
		constructor     : MentalPoints,
		getKpTypeName   : function(kpType,type){
			var obj = {
				normal    : {
					name  : '',
					color : ''
				},
				pre       : {
					name  : '[复习]',
					color : 'type-red'
				}
			};
			return obj[kpType][type];
		},
		toggleChecked   : function(self){
			var point = this;
			var pIndex = self.pointIds().indexOf(point.kpId);
			if(pIndex == -1){
				self.points.push(point);
				self.pointIds.push(point.kpId);
			}else{
				self.points.splice(pIndex,1);
				self.pointIds.splice(pIndex,1);
			}
		},
		unitShowOrHide   : function(self){
			var unit = this;
			var unitId = unit.unitId;
			var unitIndex = self.unitIds().indexOf(unitId);
			if(unitIndex == -1){
				self.unitIds.push(unitId);
			}else{
				self.unitIds.splice(unitIndex,1);
			}
		},
		getQuestionIdsByPointId : function(pointId){
			var self = this;
			var _questionIds = [];
			var _questionList = self.pointAndAssignQuestionsRef[pointId] || [];
			for(var z = 0,zLen = _questionList.length; z < zLen; z++){
				_questionIds.push(_questionList[z].questionId);
			}
			return _questionIds;
		},
		_hasQuestionsByPackageId : function(id){
			var self = this;
			var packageId = constantObj._homeworkContent.practices[self.tabType]["id"];
			var assignQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
			return packageId === id && assignQuestions.length > 0;
		},
		addMentalQuestion : function(self){
			var that         = this;
			var diffCount = that.questionCount() - that.assignCount();
			var increaseCount = diffCount > 5 ? 5 : diffCount;
			var _assignCount = that.assignCount() + increaseCount;
			var opts = self.opts;
			if(_assignCount <= that.questionCount()){
				var addMentalFn = function(){
					that.assignCount(_assignCount);
					var pointId = that.kpId();
					var chosenQuestions = self.getQuestionIdsByPointId(pointId);
					$.post("/teacher/new/homework/mental/question.vpage",{
						contentTypeId    : that.contentTypeId(),
						knowledgePoint   : pointId,
						newQuestionCount : increaseCount,
						chosenQuestions  : chosenQuestions.join(",")
					},function(data){
						if(data.success){
							var _questions = data.questions || [];
							var point = ko.mapping.toJS(that);
							if(!self.pointAndAssignQuestionsRef[pointId]){
								self.pointAndAssignQuestionsRef[pointId] = [];
							}
							$.merge(self.pointAndAssignQuestionsRef[pointId],_questions);

							var questionIds = [];
							for(var m = 0,mLen = _questions.length; m < mLen; m++){
								questionIds.push(_questions[m].questionId);
							}
							QuestionDB.addQuestions(questionIds,function(){
								var addMentalQuestionCb = opts.addMentalQuestionCb;
								$.isFunction(addMentalQuestionCb) && addMentalQuestionCb(point,_questions);
							});
						}else{
							$17.alert(data.info);
						}
					});
				};
				if(self._hasQuestionsByPackageId("PK_RECOMMEND")){
					clearTipPopup(function(){
						$.isFunction(opts.clearAll) && opts.clearAll();
						addMentalFn();
					});
				}else{
					addMentalFn();
				}
			}
		},
		removeMentalQuestion : function(self){
			var that        = this,
				assignCount = that.assignCount();
			var opts = self.opts;
			if(assignCount > 0){
				var pointId = that.kpId(),
					removeQuestion = [];
				if(assignCount > 5){
					removeQuestion = self.pointAndAssignQuestionsRef[pointId].splice(assignCount - 5,assignCount);
				}else{
					removeQuestion = self.pointAndAssignQuestionsRef[pointId].splice(0);
				}
				that.assignCount(self.pointAndAssignQuestionsRef[pointId].length);

				var point = ko.mapping.toJS(that);
				var removeMentalQuestionCb = opts.removeMentalQuestionCb;
				$.isFunction(removeMentalQuestionCb) && removeMentalQuestionCb(point,removeQuestion);

			}
		},
		clearPointListAssign : function(){
			var self = this,opts = self.opts;
			var ref = self.pointAndAssignQuestionsRef;
			ko.utils.arrayForEach(self.pointList(),function(point,index){
				var pointId = point.kpId();
				if(ref.hasOwnProperty(pointId) && ref[pointId].length > 0){
					point.assignCount(0);
					var pointObj = ko.mapping.toJS(point);
					var removeMentalQuestionCb = opts.removeMentalQuestionCb;
					$.isFunction(removeMentalQuestionCb) && removeMentalQuestionCb(pointObj,ref[pointId]);
				}
			});
			self.pointAndAssignQuestionsRef = {};
		},
		generatePointWithQuestions : function(){
			var self = this,ref = self.pointAndAssignQuestionsRef;
			var pointWithQuestions = [];
			ko.utils.arrayForEach(self.pointList(),function(point,index){
				var pointId = point.kpId();
				if(ref.hasOwnProperty(pointId) && ref[pointId].length > 0){
					var pointObj = ko.mapping.toJS(point);
					pointObj["questions"] = ref[pointId];
					pointWithQuestions.push(pointObj);
				}
			});
			return pointWithQuestions;
		},
		deleteQuestionWithPointId : function(pointId,questionId){
			var self = this,opts = self.opts;
			var removeQuestions = [];
			$.each(self.pointAndAssignQuestionsRef[pointId],function (i) {
				if(this.questionId == questionId){
					removeQuestions = self.pointAndAssignQuestionsRef[pointId].splice(i,1);
					return false;
				}
			});
			var point;
			$.each(self.pointList(),function(){
				if(this.kpId() == pointId){
					point = ko.mapping.toJS(this);
					this.assignCount(self.pointAndAssignQuestionsRef[pointId].length);
					return false;
				}
			});

			var removeMentalQuestionCb = opts.removeMentalQuestionCb;
			$.isFunction(removeMentalQuestionCb) && removeMentalQuestionCb(point,removeQuestions);
		},
		updatePointList : function(){
			var self = this,opts = self.opts;
			//用户在知识点树中选中的知识点
			var selectPoints = self.points;
			var pointMap = {};
			for(var z = 0,zLen = selectPoints.length; z < zLen; z++){
				pointMap[selectPoints[z].kpId] = selectPoints[z];
			}
			//删除的知识点,用于计算是否需要把购物车的中的题删除
			var deletePoints = [];
			//继续保留的知识点
			var existPointIds = [];
			//新添加存在的知识点
			var newPoints = [];

			var pointList = ko.mapping.toJS(self.pointList());
			for(var j = 0,jLen = pointList.length; j < jLen; j++){
				var someItem = pointList[j];
				var pointId = someItem.kpId;
				if(pointMap.hasOwnProperty(pointId)){
					existPointIds.push(pointId);
					newPoints.push(someItem);
				}else{
					deletePoints.push(ko.mapping.toJS(someItem));
				}
			}

			var ref = self.pointAndAssignQuestionsRef;
			for(var m = 0,mLen = deletePoints.length; m < mLen; m++){
				var point = deletePoints[m];
				var removeQuestion = ref[point.kpId] || [];
				if(removeQuestion.length > 0){
					var removeMentalQuestionCb = opts.removeMentalQuestionCb;
					$.isFunction(removeMentalQuestionCb) && removeMentalQuestionCb(point,removeQuestion);
				}
			}

			for(var k = 0,kLen = selectPoints.length; k < kLen; k++){
				var kpId = selectPoints[k].kpId;
				if(existPointIds.indexOf(kpId) == -1){
					extendPoint(selectPoints[k]);
					newPoints.push(selectPoints[k]);
				}
			}
			self.pointList(ko.mapping.fromJS(newPoints)());
		},
		init : function(points,pointTree){
			var self = this,ref = self.pointAndAssignQuestionsRef;
			for(var z = 0,zLen = points.length; z < zLen; z++){
				extendPoint(points[z]);
			}


			for(var pointId in ref){
				if(ref.hasOwnProperty(pointId)){
					var hasKPId = false;
					$.each(points,function(){
						if(this.kpId == pointId){
							hasKPId = true;
							return false;
						}
					});
					if(!hasKPId && self.pointAndAssignQuestionsRef[pointId].length != 0){
						$.each(pointTree,function(){
							$.each(this.knowledgePoints,function(){
								if(this.kpId == pointId){
									points.push(extendPoint(this));
								}
							});
						});
					}
				}
			}

			$.each(points,function () {
				var assignQuestions = self.pointAndAssignQuestionsRef[this.kpId];
				this.assignCount = assignQuestions ? assignQuestions.length : 0;
			});
			self.pointList(ko.mapping.fromJS(points)());
			self.pointTree(pointTree);
		}
	};

	function PackageObj(id,name,checked){
		return new PackageObj.prototype.init(id,name,checked);
	}
	PackageObj.prototype = {
		constructor : PackageObj,
		init : function(id,name,checked){
			this.id         = id || "";
			this.name       = name || "";
			return this;
		},
		getId : function(){
			return this.id;
		}
	};
	PackageObj.prototype.init.prototype = PackageObj.prototype;

	/**
	 * MentalArithmetic 包含
	 *  1. 知识点部分
	 *  2. 题目部分
	 *  3. 其它部分，相互之前信息传递
	 *
	 * **/

	function MentalArithmetic(){
		var self = this;
		self.tabType    = "MENTAL_ARITHMETIC";
		self.subject    = subject;
		self.param      = {};
		self.sectionIds = [];
		var emptyPackageKo = ko.mapping.fromJS(PackageObj(null,""));
		var pkList          = [PackageObj("PK_CUSTOM","自定义口算题"),PackageObj("PK_RECOMMEND","推荐题目")];
		if(constantObj.mentalChangeTab){
			pkList.reverse();
		}
		self.packageList    = ko.mapping.fromJS(pkList);
		self.focusIndex     = ko.observable(-1);
		self.focusPackage   = ko.pureComputed(function(){
			var focusIndex = self.focusIndex();
			if($17.isBlank(focusIndex)  || focusIndex < 0){
				return emptyPackageKo;
			}
			self.addQuestionCountAndTime(focusIndex);
			return self.packageList()[focusIndex];
		});
		self.limitTimes                 = [1,2,3,4,5,7,10,15,0];   //限制时间，单位为分钟
		var defaultLimitTime = 5;
		self.focusLimitTime             = ko.observable(defaultLimitTime);
		self.focusLimitTime.subscribe(function(val){
			self.updateTimeLimit(val);
		});
		self.updateTimeLimit(defaultLimitTime);
		self.mentalPoints               = new MentalPoints({
			tabType                 : self.tabType,
			param                   : $.extend(true,{},self.param),
			subject                 : subject,
			clearAll                : self.clearAll.bind(self),
			addMentalQuestionCb     : self.addMentalQuestionPost.bind(self),
			removeMentalQuestionCb  : self.removeMentalQuestionPost.bind(self)
		});
		self.recommendPointQuestions    = [];
		self.displayPointQuestions      = ko.observableArray([]);
		self.questionCount              = ko.observable(0);
		self.questionTotalTime          = ko.observable(0);
		self.recommendSelected          = ko.observable(false);
		self.examLoading                = ko.observable(true);
	}

	MentalArithmetic.prototype = {
		constructor : MentalArithmetic,
		viewTimeLimitHelp : function(){
			var self = this;
			var popState = {
				state0 : {
					name    : 'arrangeHomework',
					comment : '老师推荐限时口算学生会得到奖励,以下是详细规则',
					html    : template("t:TIME_LIMIT_HELP",{}),
					title   : '老师推荐限时口算<span style="color: red;">学生会得到奖励</span>,以下是详细规则',
					position: { width : 660},
					focus   : 1,
					buttons : {"确定" : true}
				}
			};

			$.prompt(popState,{
				loaded : function(event){
					$17.voxLog({
						module : "m_H1VyyebB",
						op     : "ah_mental_page_info",
						s0     : self.subject
					});
				}
			});
		},
		addQuestionCountAndTime : function(focusIndex){
			var self = this,pointQuestions;
			var questionCnt = 0;
			var totalTime = 0;
			var packageId = self.packageList()[focusIndex].id();
			switch (packageId){
				case "PK_CUSTOM":
					pointQuestions = self.mentalPoints.generatePointWithQuestions();
					break;
				case "PK_RECOMMEND":
					pointQuestions = self.recommendPointQuestions;
					for(var i = 0,iLen = pointQuestions.length; i < iLen; i++){
						var questions = pointQuestions[i].questions;
						var jLen = questions.length;
						questionCnt += jLen;
						for(var j = 0; j < jLen; j++){
							totalTime += (questions[j].seconds || 0);
						}
					}
					break;
			}
			var questionIds = [];
			for(var t = 0,tLen = pointQuestions.length; t < tLen; t++){
				var point = pointQuestions[t];
				for(var m = 0,mLen = point.questions.length; m < mLen; m++){
					questionIds.push(point.questions[m].questionId);
				}
                questionIds = questionIds.concat(point["postQuestions"] || []);
			}
			QuestionDB.addQuestions(questionIds,function(){
				self.displayPointQuestions(ko.mapping.fromJS(pointQuestions)());
				self.questionCount(questionCnt);
				self.questionTotalTime(totalTime);
			});
		},
		packageClick : function(index,self){
			if(index == self.focusIndex()){
				return false;
			}
			var packageKo = this;
			self.focusIndex(index);
			$17.voxLog({
				module : "m_H1VyyebB",
				op     : "ah_mental_tab_click",
				s0     : self.subject,
				s1     : self.param.objectiveTabType,
				s2     : packageKo.id()
			});
		},
		timeClick : function(self){
			self.focusLimitTime(this);
		},
		updateTimeLimit : function(val){
			var self = this;
			var typePackageId = constantObj._homeworkContent.practices[self.tabType]["id"];
			if(!typePackageId || typePackageId == self.focusPackage().id()){
				constantObj._homeworkContent.practices[self.tabType].timeLimit = val;
			}
		},
		showPointPopup  : function(){
			var self = this,mentalPoints = self.mentalPoints;
			var pointTree = mentalPoints.pointTree();
			if(pointTree.length <= 0){
				$17.alert("暂无知识点内容，有疑问请联系客服");
				return false;
			}
			var pointArray = [];
			ko.utils.arrayForEach(mentalPoints.pointList(),function(pt,index){
				pointArray.push(ko.mapping.toJS(pt))
			});
			var popState = {
				state0 : {
					name    : 'addPointsPopup',
					comment : '选择要添加的知识点',
					html    : template("t:ADD_POINTS_POPUP",{}),
					title   : '选择要添加的知识点',
					position: { width : 660},
					focus   : 1,
					buttons : {"确定" : true},
					submit  : function(e,v,f,m){
						e.preventDefault();

						mentalPoints.updatePointList();
						$.prompt.close();
					}
				},
				state2 : {
					name : 'emptyPointList',
					comment:'没有任何知识点',
					title   : '系统提示',
					position: { width : 500},
					focus  : 1,
					html : "暂无知识点内容，有疑问请联系客服",
					buttons: { "确定": true},
					submit  : function(e,v,m,f){
						e.preventDefault();
						$.prompt.close();
					}
				}
			};
			$.prompt(popState,{
				loaded : function(event){
					var pointIds = [];
					for(var m = 0,mLen = pointArray.length; m < mLen; m++){
						pointIds.push(pointArray[m].kpId);
					}
					mentalPoints.pointIds(pointIds);
					mentalPoints.points = pointArray;
					var unitIds = [];
					for(var t = 0,tLen = pointTree.length; t < tLen; t++){
						unitIds.push(pointTree[t].unitId);
					}
					mentalPoints.unitIds(unitIds);
					ko.applyBindings(mentalPoints,document.getElementById("jqistate_addPointsPopup"));
					$17.voxLog({
						module  : "m_H1VyyebB ",
						op      : "btn_add_kpoints_oral_calculation",
						s0      : self.subject,
						s1      : self.param.objectiveTabType
					});
				}
			});
		},
		addMentalQuestionPost : function(point,questions){
			if(!$.isArray(questions) || questions.length == 0){
				return false;
			}
			var self = this;
			var param = self.param || {};
			var _reviewQuestions = constantObj._reviewQuestions[self.tabType];
			var _newQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
			$.each(questions,function(){
				_newQuestions.push({
					book             : point.book,
					questionId       : this.questionId,
					seconds          : this.seconds,
					knowledgePointId : this.knowledgePoint,
					objectiveId      : param.objectiveTabType
				});
			});

			_reviewQuestions = _reviewQuestions.concat(questions);
			constantObj._reviewQuestions[self.tabType] = _reviewQuestions;
			constantObj._homeworkContent.practices[self.tabType]["id"] = self.focusPackage().id();
			constantObj._homeworkContent.practices[self.tabType]["realId"] = self.focusPackage().realId;
			constantObj._homeworkContent.practices[self.tabType]["recommend"] = (self.focusPackage().id() === "PK_RECOMMEND");

            var recommendKpPostQuestionsMap = constantObj._homeworkContent.practices[self.tabType].recommendKpPostQuestionsMap;
            if(!recommendKpPostQuestionsMap.hasOwnProperty(point.kpId) && self.focusPackage().id() === "PK_RECOMMEND"){
                recommendKpPostQuestionsMap[point.kpId] = (point["postQuestions"] || []);
			}

            //向展示题目区域添加知识点题目
			//console.time("ap_update_displayPointQuestions");
			if(self.focusPackage().id() === "PK_CUSTOM"){
				var haveOldQuestions = false;
				ko.utils.arrayForEach(self.displayPointQuestions(),function(pointWithQuestions,index){
					if(pointWithQuestions.kpId() == point.kpId){
						haveOldQuestions = true;
						ko.utils.arrayPushAll(pointWithQuestions["questions"],ko.mapping.fromJS(questions)());
					}
				});
				// 如果展示区域没有此知识点题目，把当前知识点添加题目区域
				if(!haveOldQuestions){
					var pointWithQuestions = $.extend(true,{questions : []},point);
					pointWithQuestions["questions"] = questions;
					self.displayPointQuestions.push(ko.mapping.fromJS(pointWithQuestions));
				}

				self.updateTimeLimit(self.focusLimitTime());
			}else if(self.focusPackage().id() === "PK_RECOMMEND"){
				self.updateTimeLimit(5);
			}
			// console.timeEnd("ap_update_displayPointQuestions");

			 //ufo加时间
			 var sum = 0;
			 for(var z = 0,zLen = questions.length; z < zLen; z++){
			    sum += (questions[z].seconds || 0);
			 }

			 self.updateUfoMental(sum);
		},
		removeMentalQuestionPost : function(point,removeQuestions){
			var self = this;
			var _reviewQuestions = constantObj._reviewQuestions[self.tabType];

			var deleteQuestionIds = [];
			for(var k = 0,kLen = removeQuestions.length; k < kLen; k++){
				deleteQuestionIds.push(removeQuestions[k].questionId);
			}

			QuestionDB.deleteQuestions(deleteQuestionIds);

			var _newReviewQuestions = [];
			for(var j = 0,jLen = _reviewQuestions.length; j < jLen; j++){
				if(deleteQuestionIds.indexOf(_reviewQuestions[j].questionId) === -1){
					_newReviewQuestions.push(_reviewQuestions[j]);
				}
			}
			constantObj._reviewQuestions[self.tabType] = _newReviewQuestions;

			var _newQuestions = [];
			var assignQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
			for(var m = 0,mLen = assignQuestions.length; m < mLen; m++){
				if(deleteQuestionIds.indexOf(assignQuestions[m].questionId) === -1){
					_newQuestions.push(assignQuestions[m]);
				}
			}
			constantObj._homeworkContent.practices[self.tabType].questions = _newQuestions;

			//删除题目时，更新题目展示区域
			var packageId = self.focusPackage().id();
			if(packageId === "PK_CUSTOM") {
				if (self.displayPointQuestions().length > 0) {

					self.displayPointQuestions.remove(function(pointWithQuestions){
						if (pointWithQuestions.kpId() === point.kpId) {
							pointWithQuestions["questions"].remove(function(oldQuestion){
								return deleteQuestionIds.indexOf(oldQuestion.questionId()) !== -1;
							});
						}
						return pointWithQuestions["questions"]().length === 0;
					});
				}
			}else if(packageId === "PK_RECOMMEND"){
				self.updateTimeLimit(0);
				//只有推荐题目包中有后测题
                constantObj._homeworkContent.practices[self.tabType].recommendKpPostQuestionsMap = {};
			}

			//ufo加时间
			var sum = 0;
			for(var z = 0,zLen = removeQuestions.length; z < zLen; z++){
			    sum += (removeQuestions[z].seconds || 0);
			}
			self.updateUfoMental(0 - sum);
		},
		updateUfoMental : function(seconds){
			//需要增加或减少的秒数
			var self = this;
			var mentalCount = constantObj._homeworkContent.practices[self.tabType].questions.length;
			constantObj._moduleSeconds[self.tabType] += seconds;
			self.carts
			&& typeof self.carts["recalculate"] === 'function'
			&& self.carts.recalculate(self.tabType,mentalCount);
		},
		clearAll      : function(){
			var self  = this;
			self.mentalPoints.clearPointListAssign();
			self.displayPointQuestions([]);
			self._clearTypeCart();
			self.recommendSelected(false);
		},
		_clearTypeCart : function(){
			var self = this;
			constantObj._homeworkContent.practices[self.tabType].timeLimit = 0;
			constantObj._homeworkContent.practices[self.tabType]["realId"] = null;
			constantObj._homeworkContent.practices[self.tabType]["recommend"] = false;
			constantObj._homeworkContent.practices[self.tabType].questions = [];
            constantObj._homeworkContent.practices[self.tabType].recommendKpPostQuestionsMap = {};
			constantObj._reviewQuestions[self.tabType] = [];
			self.updateUfoMental(0 - constantObj._moduleSeconds[self.tabType]);
		},
		_hasQuestionsByPackageId : function(id){
			var self = this;
			var packageId = constantObj._homeworkContent.practices[self.tabType]["id"];
			var assignQuestions = constantObj._homeworkContent.practices[self.tabType].questions;
			return packageId === id && assignQuestions.length > 0;
		},
		recommendSelectAll : function(){
			var self = this;
			var realId = constantObj._homeworkContent.practices[self.tabType]["realId"];
			if(!self.recommendSelected()){
				var addMentalQuestions = function(){
					var recQuestions = self.recommendPointQuestions;
					for(var m = 0,mLen = recQuestions.length; m < mLen; m++){
						self.addMentalQuestionPost(recQuestions[m],recQuestions[m]["questions"]);
					}
				};
				var packageRealId = self.focusPackage().realId;
				if(self._hasQuestionsByPackageId("PK_CUSTOM")){
					clearTipPopup(function(){
						self.mentalPoints.clearPointListAssign();
						self._clearTypeCart();
						addMentalQuestions();
						self.recommendSelected(true);
					});
				}else if(realId && realId !== packageRealId){
					clearTipPopup(function(){
						self._clearTypeCart();
						addMentalQuestions();
						self.recommendSelected(true);
					},null,'推荐题包只能选一个，继续操作会替换原有题包');
				}else{
					//添加
					addMentalQuestions();
					self.recommendSelected(true);
				}
			}else{
				//移除购物车
				var recQuestions = self.recommendPointQuestions;
				for(var m = 0,mLen = recQuestions.length; m < mLen; m++){
					self.removeMentalQuestionPost(recQuestions[m],recQuestions[m]["questions"]);
				}
				self.recommendSelected(false);
			}
		},
		delQuestion : function(pointKo,self){
			var that = this,
				kpId = pointKo.kpId();
			var questionId = that.questionId();
			if(self.recommendSelected()){
				return false;
			}
			$17.voxLog({
				module : "m_H1VyyebB",
				op     : "ah_mental_btn_delete_click",
				s0     : self.subject
			});
			$.prompt("<div class='w-ag-center'>确定要删除该题目?</div>",
				{
					title: "系统提示",
					buttons: { "确定": true },
					position: {width: 500},
					submit: function () {
						self.mentalPoints.deleteQuestionWithPointId(kpId,questionId);
					},
					close: function () {}
				});
		},
		renderVueQuestion : function(questionId,index){
			var self = this;
			var containerId = '#' + questionId + '-' + index;
			var $containerId = $(containerId);
			var question = QuestionDB.getQuestionById(questionId);
			if(!question){
				$containerId.html("题目未加载");
				return false;
			}
			if(!$containerId.attr("data-init")){
				$containerId.attr("data-init","true");
				//config配置里的参数，可以抽象出来作为属性由父组件传进来，注意兼容性和扩展性。
				var config = {
                    container: containerId, //容器的id，（必须）
                    formulaContainer:'#tabContent', //公式渲染容器（必须）
                    questionList: [question], //试题数组，包含完整的试题json结构， （必须）
                    framework: {
                        vue: Vue, //vue框架的外部引用
                        vuex: Vuex //vuex框架的外部引用
                    },
                    showAnalysis: false, //是否展示解析
                    showUserAnswer: false, //是否展示用户答案
                    showRightAnswer: false, //是否展示正确答案
                    startIndex : 0 //从第几题开始
				};
				try{
					Venus.init(config);
				}catch (e){
					$17.log(e.message,e.stack);
				}
			}
			return "";
		},
        viewCourse : function(self){
			var pointObj = this;
            var gameUrl = "/teacher/new/homework/previewteachingcourse.vpage?" + $.param({
                courseId : pointObj["courseId"]
            });
            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>';
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
                },
                loaded : function(){
                    window.addEventListener("message",function(e){
                        $.prompt.close();
                    });
                }
            });
        },
		run : function(){
			var self = this,param = self.param || {};
			var sectionIds = self.sectionIds.join(",");
			var paramData = {
				bookId              : param.bookId,
				unitId              : param.unitId,
				sections            : sectionIds,
				type                : self.tabType,
				subject             : self.subject,
				objectiveConfigId   : param.objectiveConfigId
			};
			var url = "/teacher/new/homework/objective/content.vpage";
			self.examLoading(true);
			$.get(url,paramData).done(function(res){
				if(res.success){
					//知识点列表
					var content = res.content || [];
					var mentalPoints = self.mentalPoints;
					if($.isArray(content) && content.length > 0){
						mentalPoints.init((content[0]["kpList"] || []),(content[0]["kpTrees"] || []));

						var practiceCart = constantObj._homeworkContent.practices[self.tabType];
						//推荐题包在购物车有且只有一个
						var realId = practiceCart["realId"];
						ko.utils.arrayForEach(self.packageList(),function(packageKo,index){
							if(packageKo.id() === "PK_RECOMMEND"){
								packageKo.realId = sectionIds;
							}
						});
						var recommendQuestions = content[0]["recommendationQuestions"] || [];
						if(!!practiceCart["recommend"] &&　realId == sectionIds){
							var questions = practiceCart["questions"] || [];
							var questionPointMap = {};  //知识点与题目的映射关系
							for(var t = 0,tLen = questions.length; t < tLen; t++){
								var pointId = questions[t]["knowledgePointId"];
								if(questionPointMap.hasOwnProperty(pointId)){
									questionPointMap[pointId].push({
										knowledgePoint : pointId,
										questionId     : questions[t]["questionId"],
										seconds        : questions[t]["seconds"]
									});
								}else{
									questionPointMap[pointId] = [{
										knowledgePoint : pointId,
										questionId     : questions[t]["questionId"],
										seconds        : questions[t]["seconds"]
									}];
								}
							}
                            var recommendKpPostQuestionsMap = practiceCart["recommendKpPostQuestionsMap"] || {};
							var newRecommendQuestions = [];
							for(var m = 0,mLen = recommendQuestions.length; m < mLen; m++){
								var kpId = recommendQuestions[m].kpId;
								if(questionPointMap.hasOwnProperty(kpId)){
									var newQuestions = questionPointMap[kpId];
									recommendQuestions[m]["questionCount"] = newQuestions.length;
									recommendQuestions[m]["questions"] = newQuestions;
                                    recommendQuestions[m]["postQuestions"] = recommendKpPostQuestionsMap[kpId] || [];
									newRecommendQuestions.push(recommendQuestions[m]);
								}
							}
							self.recommendPointQuestions = newRecommendQuestions;
							self.recommendSelected(true);
						}else{
							self.recommendPointQuestions = recommendQuestions;
							self.recommendSelected(false);
						}
					}
					self.packageClick.call(self.packageList()[0],0,self);
				}else{
					res.errorCode !== "200" && $17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : url,
						s1     : $.toJSON(res),
						s2     : $.toJSON(paramData),
						s3     : param.env
					});
				}
				self.examLoading(false);
			}).fail(function(jqXHR,textStatus,errorThrown){
				$17.voxLog({
					module : "API_REQUEST_ERROR",
					op     : "API_STATE_ERROR",
					s0     : url,
					s1     : errorThrown.message,
					s2     : $.toJSON(paramData),
					s3     : param.env
				});
			});
		},
		initialise        : function(option){
			var self = this;
			option = option || {};
			self.param = $.extend(true, self.param, {
				bookId              : option.bookId,
				unitId              : option.unitId,
				tabType             : option.tabType,
				objectiveConfigId   : option.objectiveConfigId,
				objectiveTabType    : option.objectiveTabType
			});
			self.tabType = option.tabType || self.tabType;
			self.subject = option.subject || null;
			self.mentalPoints.tabType = self.tabType;
			self.mentalPoints.subject = self.subject;
			self.sectionIds = [];
			$.each(option.sections,function(i,section){
				self.sectionIds.push(section.sectionId);
			});
			self.carts = option.carts || null;
			var $mental     = $(".J_UFOInfo p[type='" + self.tabType +"']");
			if($mental.has("span").length == 0){
				$mental.empty().html(template("t:UFO_MENTAL_CART",{tabTypeName : option.tabTypeName,count : 0}));
			}
		}
	};

	$17.homeworkv3 = $17.homeworkv3 || {};
	$17.extend($17.homeworkv3, {
		getMental_arithmetic: function(){
			return new MentalArithmetic();
		}
	});
});