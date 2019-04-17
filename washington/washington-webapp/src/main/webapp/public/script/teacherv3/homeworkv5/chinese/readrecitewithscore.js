(function($17,ko) {
	"use strict";

	var selfPlayer = (function(){
		var $jPlayerContaner;
		function initJplayerElement(){
			$jPlayerContaner = $("#jquery_jplayer_1");
			if($jPlayerContaner.length == 0){
				$jPlayerContaner = $("<div></div>").attr("id","jquery_jplayer_1");
				$jPlayerContaner.appendTo("body");
			}
		}
		function playAudio(audioList,callback){
			if(!$.isArray(audioList) || audioList.length == 0){
				$17.alert('音频数据为空');
				return false;
			}
			initJplayerElement();
			var playIndex = 0;
			$jPlayerContaner.jPlayer("destroy");
			setTimeout(function(){
				$jPlayerContaner.jPlayer({
					ready: function (event) {
						playNextAudio(playIndex,audioList,callback);
					},
					error : function(event){
						playIndex++;
						playIndex = playNextAudio(playIndex,audioList,callback);
					},
					ended : function(event){
						playIndex++;
						playIndex = playNextAudio(playIndex,audioList,callback);
					},
					volume: 0.8,
					solution: "html,flash",
					swfPath: "/public/plugin/jPlayer",
					supplied: "mp3"
				});
			},200);
		}
		function playNextAudio(playIndex,audioArr,callback){
			if(playIndex >= audioArr.length){
				$jPlayerContaner.jPlayer("destroy");
				$.isFunction(callback) && callback();
			}else{
				var url = audioArr[playIndex];
				url && $jPlayerContaner.jPlayer("setMedia", {
					mp3: url
				}).jPlayer("play");
			}
			return playIndex;
		}
		function stopAudio(){
			$jPlayerContaner && $jPlayerContaner.jPlayer("clearMedia");
		}
		return {
			playAudio : playAudio,
			stopAudio : stopAudio,
			stopAll   : function(){
				$jPlayerContaner && $jPlayerContaner.jPlayer("destroy");
			}
		};
	}());


	var config_readRecite = {};
	var ReadRecite = function(){

		this.packageList = ko.observableArray([]);
	};

	ReadRecite.prototype = {
		constructor : ReadRecite,

		initialise : function (config) {
			var self = this;
			config_readRecite = $.extend(true,{},config);
			self.carts = config_readRecite.carts || null;

			var $ufo = $("p[type='" + config_readRecite.tabType +"']",".J_UFOInfo");
			if($ufo.has("span").length == 0){
				$ufo.empty().html([
					"<span class=\"name\">"+ config_readRecite.tabTypeName + "</span>" +
					"<span class=\"count\" data-count=\"0\">0</span>" +
					"<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join(""));
			}
		},
		selectStatus : function (data) {
			var count = 0;
			$.each(data.questions(),function () {
				if(this.questionChecked()){
					count++;
				}
			});

			return "已选" + count + "段，共" + data.questionNum() + "段";
		},
		addPackage : function (self) {
			var that = this,
				totalTime = 0,
				content = [],
				data = ko.mapping.toJS(this);

			$.each(data.questions,function () {
				totalTime += this.seconds;
				content.push({
					questionId : this.id,
					seconds    : this.seconds,
					submitWay  : this.submitWay
				});
			});

			$.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps,function (index) {
				if(this.questionBoxId == data.questionBoxId){

					$.each(this.questions,function () {
						var qid = this.questionId;
						constantObj._moduleSeconds[config_readRecite.tabType] -= this.seconds;

						$.each(that.questions(),function () {
							if(this.id()== qid){
								this.questionChecked(false);
								return false;
							};
						});
					});
					constantObj._homeworkContent.practices[config_readRecite.tabType].apps.splice(index,1);

					$.each(constantObj._reviewQuestions[config_readRecite.tabType],function (index) {
						if(this.questionBoxId == data.questionBoxId){
							constantObj._reviewQuestions[config_readRecite.tabType].splice(index,1);
							return false;
						};
					});
					return false;
				}
			});

			if(!this.packageChecked()){
				constantObj._homeworkContent.practices[config_readRecite.tabType].apps.push({
					questionBoxType : data.questionBoxType,
					questionBoxId   : data.questionBoxId,
					book            : data.questions[0].book,
					questions       : content,
					objectiveId     : config_readRecite.objectiveTabType
				});
				$.each(that.questions(),function () {
					this.questionChecked(true);
				});
				constantObj._moduleSeconds[config_readRecite.tabType] += totalTime;
				constantObj._reviewQuestions[config_readRecite.tabType].push(data);
			};
			$17.voxLog( {
				module : "m_H1VyyebB",
				op     :  "ReadReciteScore_select_cancel_click",
				s0     : data.questionBoxType,
				s1     : !this.packageChecked() ? "select" : "cancel"
			});

			this.packageChecked(!this.packageChecked());
			self.reSetUFO();
		},
		reSetUFO : function(){
			var self = this,
				count = 0;

			$.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps,function () {
				count += this.questions.length;
			});

			self.carts && typeof self.carts["recalculate"] === 'function'
			&& self.carts.recalculate(config_readRecite.tabType,count);
		},
		getParagraphInfo : function(questions){
			questions = ko.mapping.toJS(questions);
			questions = $.isArray(questions) ? questions : [];

			var questionLen = questions.length;
			var popupPackageChecked = true;
			var importantCount = 0;
			var importantSelCount = 0;  //重点段落被选中的数量
			var unImportantCount = 0;
			var unImportantSelCount = 0;  //非重点段落被选中的数量
			ko.utils.arrayForEach(questions,function(question){
				popupPackageChecked = popupPackageChecked && question.questionChecked;
				if(question.paragraphImportant){
					importantCount++;
					question.questionChecked && (importantSelCount++);
				}else{
					unImportantCount++;
					question.questionChecked && (unImportantSelCount++);
				}
			});

			unImportantCount = questions.length - importantCount;

			return {
				popupPackageChecked     : popupPackageChecked,
				partylyImportant        : (importantCount > 0 && importantCount < questionLen),
				importantCheckedAll     : (importantSelCount > 0 && importantSelCount == importantCount),
				unImportantCheckedAll   : (unImportantSelCount > 0 && unImportantSelCount == unImportantCount)
			};
		},
		_packageDetailWithPopup : function(article,rootObj,focusExamMap){
			var that = this;
            $.prompt(template("t:viewDetailTPL2018",{}), {
                title    : "段落调整",
                buttons  : {},
                position : { width: 960},
                close    : function () {

                    var packageChecked = false;
                    $('body').css('overflow', 'auto');

                    ko.utils.arrayForEach(that.questions(),function(question){
                        question.showQuestion(true);
                        if(question.questionChecked()){
                            packageChecked = true;
                        }
                    });

                    that.packageChecked(packageChecked);
                    //关闭所有播放
                    selfPlayer.stopAll();
                },
                loaded : function(){
                    var categories = [];
                    var categroyTypes = [];
                    var obj = rootObj.getParagraphInfo(that.questions());
                    if(obj.partylyImportant){
                        categories = categories.concat([{
                            type        : "important",
                            typeName    : "重点段落"
                        },{
                            type        : "common",
                            typeName    : "其他段落"
                        }]);
                        if(obj.importantCheckedAll){
                            categroyTypes.push("important");
                        }
                        if(obj.unImportantCheckedAll){
                            categroyTypes.push("common");
                        }
                    }

                    ko.applyBindings({
                        lessonName            : article.lessonName(),
                        packageChecked        : ko.observable(obj.popupPackageChecked), //弹出窗中【全部选入】接钮状态
                        questionBoxName       : that.questionBoxName(),
                        categoryTypes         : ko.observableArray(categroyTypes),
                        categories            : ko.observableArray(categories),
                        showCount             : ko.observable(3),
                        currentQuestions      : that.questions, //这是包下总的题数
                        playingQuestionId     : ko.observable(null),
                        focusExamMap		  : focusExamMap || {},
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
                        selAllCurrentQuestions : function () {
                            var item      = this,
                                newItem   = true,
                                content   = [],
                                totalTime = 0,
                                data      = ko.mapping.toJS(item.currentQuestions());

                            var apps = $.merge([],constantObj._homeworkContent.practices[config_readRecite.tabType].apps);
                            $.each(apps,function (i) {
                                var selItem = this;
                                if(selItem.questionBoxId == that.questionBoxId()){
                                    ko.utils.arrayForEach(item.currentQuestions(),function(itemQuestion){
                                        $.each(selItem.questions,function (j) {
                                            if(this.questionId == itemQuestion.id()){
                                                itemQuestion.questionChecked(false);
                                                constantObj._moduleSeconds[config_readRecite.tabType] -= this.seconds;
                                                constantObj._homeworkContent.practices[config_readRecite.tabType].apps[i].questions.splice(j,1);
                                                return false;
                                            }
                                        });
                                    });

                                    if(constantObj._homeworkContent.practices[config_readRecite.tabType].apps[i].questions.length == 0){
                                        constantObj._homeworkContent.practices[config_readRecite.tabType].apps.splice(i,1);
                                    }

                                    $.each(constantObj._reviewQuestions[config_readRecite.tabType], function (i) {
                                        if(this.questionBoxId == that.questionBoxId()){

                                            $.each(data,function () {
                                                var itemQuestion = this;
                                                var tempQuestions = $.merge([],constantObj._reviewQuestions[config_readRecite.tabType][i].questions);
                                                $.each(tempQuestions,function (j) {

                                                    if(itemQuestion.id == this.id){
                                                        constantObj._reviewQuestions[config_readRecite.tabType][i].questions.splice(j,1);
                                                        return false;
                                                    }
                                                });
                                            });

                                            if(constantObj._reviewQuestions[config_readRecite.tabType][i].questions.length == 0){

                                                constantObj._reviewQuestions[config_readRecite.tabType].splice(i,1);
                                            }
                                            return false;
                                        }
                                    });

                                    return false;
                                }
                            });

                            if(!this.packageChecked()){
                                $.each(data,function () {
                                    totalTime += this.seconds;
                                    content.push({
                                        questionId : this.id,
                                        seconds    : this.seconds,
                                        submitWay  : this.submitWay
                                    });
                                });

                                $.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps,function () {
                                    if(this.questionBoxId == that.questionBoxId()){
                                        var temp1 = this;
                                        newItem = false;
                                        $.each(data,function () {
                                            if(this.showQuestion){
                                                temp1.questions.push({
                                                    questionId : this.id,
                                                    seconds    : this.seconds,
                                                    submitWay  : this.submitWay
                                                });
                                            }
                                        });

                                        $.each(constantObj._reviewQuestions[config_readRecite.tabType],function () {
                                            var temp2 = this;
                                            if(this.questionBoxId == that.questionBoxId){
                                                $.each(data,function () {
                                                    if(this.showQuestion) {
                                                        temp2.questions.push(this);
                                                    }
                                                });
                                                return false;
                                            }
                                        });

                                        return false;
                                    }
                                });

                                if(newItem){
                                    constantObj._homeworkContent.practices[config_readRecite.tabType].apps.push({
                                        questionBoxType : that.questionBoxType(),
                                        questionBoxId   : that.questionBoxId(),
                                        book            : ko.mapping.toJS(that.questions()[0].book),
                                        questions       : content,
                                        objectiveId     : config_readRecite.objectiveTabType
                                    });
                                    constantObj._reviewQuestions[config_readRecite.tabType].push(ko.mapping.toJS(that));
                                }
                                constantObj._moduleSeconds[config_readRecite.tabType] += totalTime;
                                $.each(item.currentQuestions(),function () {
                                    if(this.showQuestion()){
                                        this.questionChecked(true);
                                    }
                                });
                            }
                            $17.voxLog( {
                                module : "m_H1VyyebB",
                                op     : "ReadReciteScore_ParagraphAdjustPopup_SelectAll_click",
                                s0     : that.questionBoxType(),
                                s1     : !this.packageChecked() ? "select" : "cancel"
                            });

                            this.packageChecked(!this.packageChecked());
                            this.resetQuestionType();
                            rootObj.reSetUFO();
                        },
                        resetQuestionType : function(){
                            var categoryTypes = this.categoryTypes;
                            var newCategoryTypes = [];
                            var obj = rootObj.getParagraphInfo(this.currentQuestions());
                            if(obj.importantCheckedAll){
                                newCategoryTypes.push("important");
                            }
                            if(obj.unImportantCheckedAll){
                                newCategoryTypes.push("common");
                            }
                            categoryTypes(newCategoryTypes);
                        },
                        addQuestion : function (parent,from) {
                            var item    = this,
                                newItem = true;

                            if(!this.questionChecked()){

                                $.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps,function () {
                                    if(this.questionBoxId == that.questionBoxId()){

                                        newItem = false;
                                        this.questions.push({
                                            questionId : item.id(),
                                            seconds    : item.seconds(),
                                            submitWay  : item.submitWay()
                                        });

                                        $.each(constantObj._reviewQuestions[config_readRecite.tabType],function () {
                                            if(this.questionBoxId == that.questionBoxId()){

                                                this.questions.push(ko.mapping.toJS(item));
                                                return false;
                                            }
                                        });

                                        return false;
                                    }
                                });
                                if(newItem){
                                    constantObj._homeworkContent.practices[config_readRecite.tabType].apps.push({
                                        questionBoxType : that.questionBoxType(),
                                        questionBoxId   : that.questionBoxId(),
                                        book            : ko.mapping.toJS(that.questions()[0].book),
                                        questions       : [{
                                            questionId : item.id(),
                                            seconds    : item.seconds(),
                                            submitWay  : item.submitWay()
                                        }]
                                    });
                                    constantObj._reviewQuestions[config_readRecite.tabType].push(ko.mapping.toJS({
                                        lessonName      : that.lessonName(),
                                        questionBoxId   : that.questionBoxId(),
                                        questionBoxName : that.questionBoxName(),
                                        questionBoxType : that.questionBoxType(),
                                        questions       : [ko.mapping.toJS(item)]
                                    }));
                                }
                                constantObj._moduleSeconds[config_readRecite.tabType] += item.seconds();
                            }else{
                                $.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps,function (index) {
                                    var temp = this;
                                    if(temp.questionBoxId == that.questionBoxId()){

                                        $.each(temp.questions,function (index) {
                                            if(this.questionId == item.id()){
                                                temp.questions.splice(index,1);
                                                return false;
                                            }
                                        });
                                        if(temp.questions.length == 0){
                                            constantObj._homeworkContent.practices[config_readRecite.tabType].apps.splice(index,1);
                                        }

                                        $.each(constantObj._reviewQuestions[config_readRecite.tabType],function (index2) {
                                            if(this.questionBoxId == that.questionBoxId()){
                                                var temp2 = this;
                                                $.each(temp2.questions,function (i) {
                                                    if(this.id == item.id()){

                                                        temp2.questions.splice(i,1);
                                                        return false;
                                                    }
                                                });
                                                if(temp2.questions.length == 0){
                                                    constantObj._reviewQuestions[config_readRecite.tabType].splice(index2,1);
                                                }
                                                return false;
                                            }
                                        });

                                        return false;
                                    };
                                });

                                constantObj._moduleSeconds[config_readRecite.tabType] -= item.seconds();
                            }

                            from === "normal" && $17.voxLog({
                                module: "m_H1VyyebB",
                                op    : "ReadReciteScore_ParagraphAdjustPopup_select_cancel_click",
                                s1    : that.questionBoxType(),
                                s0    : !this.questionChecked() ? "select" : "cancel"
                            });

                            this.questionChecked(!this.questionChecked());
                            parent.resetSelectAllBtn();
                            rootObj.reSetUFO();
                        },
                        resetSelectAllBtn : function () {
                            var chapter = this,
                                count = 0;
                            $.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps,function () {
                                var temp = this;
                                if(temp.questionBoxId == that.questionBoxId()){

                                    $.each(chapter.currentQuestions(),function () {
                                        var temp2 = this;
                                        $.each(temp.questions,function () {

                                            if(temp2.id() == this.questionId){
                                                temp2.questionChecked(true);
                                                if(temp2.showQuestion()){
                                                    count++;
                                                }
                                            }
                                        });
                                    });

                                    return false;
                                }
                            });

                            this.packageChecked(count != 0 && count == this.currentQuestions().length);
                            this.resetQuestionType();
                        },
                        showMoreQuestions : function(){
                            this.showCount(this.showCount()+3);
                        },
                        changeQuestionType : function (self) {
                            var categoryKo = this;
                            var type = categoryKo.type;
                            var categoryTypes = self.categoryTypes;
                            var tIndex = categoryTypes.indexOf(type);
                            var newQuestions;
                            if(tIndex === -1){
                                categoryTypes.push(type);

                                newQuestions = ko.utils.arrayFilter(self.currentQuestions(),function(question,index){
                                    return !question.questionChecked() && (type === "important" ? question.paragraphImportant() : !question.paragraphImportant());
                                });
                            }else{
                                categoryTypes.splice(tIndex,1);
                                newQuestions = ko.utils.arrayFilter(self.currentQuestions(),function(question,index){
                                    return question.questionChecked() && (type === "important" ? question.paragraphImportant() : !question.paragraphImportant());
                                });
                            }

                            $17.voxLog( {
                                module : "m_H1VyyebB",
                                op     : "ReadReciteScore_ParagraphAdjustPopup_ParagraphTick_click",
                                s0     : that.questionBoxType(),
                                s1     : type == 'important' ? "emphases" : "other"
                            });

                            ko.utils.arrayForEach(newQuestions,function(qs){
                                self.addQuestion.call(qs,self,"changeQuestionType");
                            });
                        },
                        resetCurrentQuestions : function (showImportantQuestion) {
                            var data = this,
                                count = 0;
                            var questions = [];
                            ko.utils.arrayForEach(data.currentQuestions(),function (itemQuestion) {
                                if((showImportantQuestion && this.paragraphImportant()) || (data.showCommonQuestion() && !this.paragraphImportant())){
                                    count++;
                                    itemQuestion.showQuestion(true);
                                    questions.push(this);
                                }else{
                                    itemQuestion.showQuestion(false);
                                }
                            });
                        },
                        playAudio : function(self,element){
                            var question = this;
                            var audio = ko.unwrap(question.listenUrls);
                            if($17.isBlank(audio)){
                                return false;
                            }
                            if(typeof audio === "string"){
                                audio = [audio];
                            }
                            var questionId = ko.unwrap(question.id);
                            var playingQuestionIdFn = self.playingQuestionId;
                            if(playingQuestionIdFn() == questionId){
                                selfPlayer.stopAudio();
                                playingQuestionIdFn(null);
                            }else{
                                playingQuestionIdFn(questionId);
                                selfPlayer.playAudio(audio,function(){
                                    playingQuestionIdFn(null);
                                });

                                $17.voxLog( {
                                    module : "m_H1VyyebB",
                                    op     : "ReadReciteScore_ParagraphAdjustPopup_play_click",
                                    s0     : that.questionBoxType()
                                });

                            }
                        }
                    }, document.getElementById('jqistate_state0'));
                }
            });
            $('body').css('overflow', 'hidden');
		},
		packageDetail  :function (article,rootObj) {
			var that = this;
			var questionIds = [];
			ko.utils.arrayForEach(that.questions(),function(questionObj){
				questionIds.push(questionObj.id());
			});

            $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                var focusExamMap = result.success ? result.questionMap : {};
                rootObj._packageDetailWithPopup.call(that,article,rootObj,focusExamMap);
            });

			$17.voxLog( {
				module : "m_H1VyyebB",
				op     : "ReadReciteScore_ParagraphAdjust_click",
				s0     : that.questionBoxType()
			});
		},
		run : function () {
			var self = this;
			var _sectionIds = $.map(config_readRecite.sections,function(item){
				return item.sectionId;
			}),paramData = {
				sections : _sectionIds.join(","),
				type     : config_readRecite.tabType,
				unitId   : config_readRecite.unitId,
				bookId   : config_readRecite.bookId,
				subject  : constantObj.subject,
				objectiveConfigId : config_readRecite.objectiveConfigId
			};
			$.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
				if(data.success && data.content){
					$.each(data.content,function () {
						if(this.type == "package"){
							$.each(this.packages,function () {
								$.each(this.readReciteList,function () {
									var packageInfo = this;
									packageInfo.packageChecked = false;
									$.each(packageInfo.questions,function () {
										this.questionChecked = false;
										this.showQuestion = true;
									});

									$.each(constantObj._homeworkContent.practices[config_readRecite.tabType].apps, function () {
										var selInfo = this;
										if(selInfo.questionBoxId == packageInfo.questionBoxId){
											packageInfo.packageChecked = true;
											$.each(selInfo.questions,function(){
												var selQuestion = this;
												$.each(packageInfo.questions,function () {
													if(selQuestion.questionId == this.id){
														this.questionChecked = true;
														return false;
													}
												});
											});
										}
									});
								});
							});

							self.packageList(ko.mapping.fromJS(this.packages || [])());
						}
					});
				}else{
					self.packageList([]);
					$17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/new/homework/content.vpage",
						s1     : $.toJSON(data),
						s2     : $.toJSON(paramData),
						s3     : $uper.env
					});
				}
			});
		}
	};

	$17.homeworkv3 = $17.homeworkv3 || {};
	$17.extend($17.homeworkv3, {
		getRead_recite_with_score : function(){
			return new ReadRecite();
		}
	});
}($17,ko));