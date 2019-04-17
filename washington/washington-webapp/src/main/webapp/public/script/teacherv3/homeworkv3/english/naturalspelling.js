/**
 * 自然拼读
 * Created by dell on 2017/7/17.
 */
(function($17,ko) {
	"use strict";

	ko.bindingHandlers.singleAppHover = {
		init: function(element, valueAccessor){
			var $element = $(element);
			var value = valueAccessor();
			var valueUnwrapped = ko.unwrap(value);
			if($element.hasClass("previewText")){
				$element.hover(
					function(){
						$(element).find("div.preview").show();
					},
					function(){
						$(element).find("div.preview").hide();
					}
				);
			}else if($element.hasClass("operateBtn")){
				$element.hover(
					function(){
						if(!valueUnwrapped){
							$element.closest("li").addClass("hover");
						}
					},
					function(){
						$element.closest("li").removeClass("hover");
					}
				);
			}
		},
		update: function(element, valueAccessor, allBindings, viewModel, bindingContext) {
			var $element = $(element);
			if($element.hasClass("operateBtn")){
				var value = valueAccessor();
				var valueUnwrapped = ko.unwrap(value);
				var $li = $element.closest("li");
				if(valueUnwrapped){
					$li.addClass("active");
				}else{
					$li.removeClass("active");
				}
			}

		}
	};

	var NaturalSpelling = function(){
		var self = this;
		self.ctLoading = ko.observable(false);
		self.contentList = ko.observableArray([]);
		self.tabType = "NATURAL_SPELLING";
		self.levels = ko.observableArray([]);
		self.currentLevel = ko.observable(1);
        self.currentTab = ko.observable(0);

	};
	NaturalSpelling.prototype = {
		constructor     : NaturalSpelling,
		param           : {},
		categoryIconPrefixUrl : null,
		run             : function () {
			var self = this,paramData = {
				bookId   : self.param.bookId,
				unitId   : self.param.unitId,
				sections : "", //[].toString()
				type     : self.tabType,
				subject  : constantObj.subject,
				objectiveConfigId : self.param.objectiveConfigId
			};
			self.currentTab(0);
			self.ctLoading(true);
			$.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
				if(data.success){
					var _apps = constantObj._homeworkContent.practices[self.tabType].apps || [];
					var lessonCategoryIds = [];
					for(var m = 0,mLen = _apps.length; m < mLen; m++){
						lessonCategoryIds.push(_apps[m].lessonId + ":" + (_apps[m]["categoryId"] || ""));
					}
					var contents = data.content || [];
                    contents.forEach(function (tab){
						if(tab.type=="universal")
						{
                            $.each(tab.levels,function (index,item) {
                                if(item.defaultLevel)
                                {
                                    self.currentLevel(item.level);
                                    return false;
                                }
                            });
                        }
                        tab.totalPage = tab.nonUniversalContents.length;
                        tab.userInputPage = null;
                        tab.currentPage = 1;
                        tab.pageSize = 1;
						$.each(tab.nonUniversalContents,function (index,unit) {
							var lessons = unit.lessons;
							unit.defaultUnit && (tab.currentPage = (index + 1));
							for(var i = 0, iLen = lessons.length; i < iLen; i++){
								var ctGroups = lessons[i].categoryGroups || [];
								for(var z = 0,zLen = ctGroups.length; z < zLen; z++){
									//生成一个虚拟组ID，用于选入、预览时确定category属于哪个组
									ctGroups[z].id = [lessons[i].lessonId,z].join(":");
									var _categroyList = ctGroups[z].categories || [];
									for(var t = 0, tLen = _categroyList.length; t < tLen; t++){
										var lessonCategory = lessons[i].lessonId + ":" + _categroyList[t].categoryId;
										_categroyList[t]["checked"] = (lessonCategoryIds.indexOf(lessonCategory) != -1);
									}
								}
							}
						});
					});
                    $17.voxLog({
                        module: "m_H1VyyebB",
                        op    : "o_y7zNLs2a",
                        s0    : self.param.bookId,
                        s1    : contents.length>1? "教材同步版_通用版":"通用版"
                    });
					self.contentList(ko.mapping.fromJS(contents)());
					self.getContent();
				}else{
					$17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/new/homework/content.vpage",
						s1     : $.toJSON(data),
						s2     : $.toJSON(paramData),
						s3     : $uper.env
					});
				}
				self.ctLoading(false);
			});
		},
		getContent : function () {
            var self = this,paramData = {
                bookId   : self.param.bookId,
                unitId   : self.param.unitId,
                subject  : constantObj.subject,
                objectiveConfigId : self.param.objectiveConfigId,
				level: self.currentLevel()
            };
            self.ctLoading(true);
            $.get("/teacher/new/homework/naturalspelling/content.vpage",paramData,function(data){
                if(data.success){
                    var _apps = constantObj._homeworkContent.practices[self.tabType].apps || [];
                    var lessonCategoryIds = [];
                    for(var m = 0,mLen = _apps.length; m < mLen; m++){
                        lessonCategoryIds.push(_apps[m].lessonId + ":" + (_apps[m]["categoryId"] || ""));
                    }
					var contents = data.content || [];
					$.each(contents,function (index,unit) {
						var lessons = unit.lessons;
						for(var i = 0, iLen = lessons.length; i < iLen; i++){
							var ctGroups = lessons[i].categoryGroups || [];
							for(var z = 0,zLen = ctGroups.length; z < zLen; z++){
								//生成一个虚拟组ID，用于选入、预览时确定category属于哪个组
								ctGroups[z].id = [lessons[i].lessonId,z].join(":");
								var _categroyList = ctGroups[z].categories || [];
								for(var t = 0, tLen = _categroyList.length; t < tLen; t++){
									var lessonCategory = lessons[i].lessonId + ":" + _categroyList[t].categoryId;
									_categroyList[t]["checked"] = (lessonCategoryIds.indexOf(lessonCategory) != -1);
								}
							}
						}
					});
					self.contentList().forEach(function (item) {
						if(item.type()=="universal")
						{
                            item.totalPage(contents.length);
                            item.userInputPage(null);
                            item.pageSize(1);
                            item.currentPage(1)
                            item.nonUniversalContents(ko.mapping.fromJS(contents)());
						}
                    });
                }else{
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
                self.ctLoading(false);
            });
        },
		changeLevel : function (level) {
			this.currentLevel(level);
			this.getContent();
        },
		changeTab : function (index) {
			this.currentTab(index);
            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "o_3talCa1O",
                s0    : this.param.bookId,
                s1    : this.contentList()[index].type()=="nonUniversal" ? "教材同步版":"通用版"
            });
        },
        page_click : function (pageNo) {
			var self = this;
            pageNo = +pageNo || 0;
            if(pageNo < 1 || pageNo > self.contentList()[self.currentTab()].totalPage() || pageNo == self.contentList()[self.currentTab()].currentPage()){
                self.contentList()[self.currentTab()].userInputPage(null);
                return false;
            }
            self.contentList()[self.currentTab()].currentPage(pageNo);
        },
        goSpecifiedPage:function () {
            var self = this;
            var pageNo = self.contentList()[self.currentTab()].userInputPage();
            if(/\D/g.test(pageNo)){
                self.contentList()[self.currentTab()].userInputPage(null);
            }else{
                self.page_click(pageNo);
            }
        },
		covertSentences    : function(sentences){
			if(!$.isArray(sentences)){
				return "";
			}
			return sentences.join(" / ");
		},
		getCategroyIconUrl : function(categroyIcon){
			var self = this;
			categroyIcon = +categroyIcon || 50000;
			return self.categoryIconPrefixUrl + "e-icons-" + categroyIcon + ".png";
		},
		categoryPreview: function(lessonId,self){
			var categoryKO = this;
            var gameUrl,data;
            var practices = categoryKO.practices() || [];
            if(practices.length <= 0){
                $17.alert("没有相应类别应用,暂不能预览");
                return false;
            }
            var questions = practices[0].questions() || [];
            if(questions.length <= 0){
                $17.alert("没有配相应的应试题,暂不能预览");
                return false;
            }
            var qIds = [];
            for(var t = 0, tLen = questions.length; t < tLen; t++){
                qIds.push(questions[t].questionId());
            }

			switch (self.tabType){
				case "NATURAL_SPELLING":
					var domain = "/";
					if(constantObj.env === "test"){
						domain = location.protocol + "//www.test.17zuoye.net/";
					}else{
						domain = location.protocol + "//" + location.host;
					}
					var urlParams = JSON.stringify({
						env : constantObj.env,
						img_domain:constantObj.imgDomain,
						hw_practice_url:domain + "/flash/loader/newselfstudymobile.vpage?bookId=" + self.param.bookId + "&qids="+qIds.join(",")+"&lessonId="+lessonId+"&practiceId="+practices[0].practiceId(),
						client_name:"pc",
						client_type:"pc",
                        from : "preview"
					});

					gameUrl = domain + "/resources/apps/hwh5/funkyspell/V1_0_0/index.vhtml?__p__=" + encodeURIComponent(urlParams);

					data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

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
							 var iframe = $("iframe.vox17zuoyeIframe")[0];
							 iframe.contentWindow.addEventListener("closePreviewPopup",function(){
							 		$.prompt.close();
							 });
						}
					});

					break;
				default:

					var paramObj = {
						qids : qIds.join(","),
						lessonId : lessonId,
						practiceId : practices[0].practiceId(),
						fromModule : ""
					};
                    gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
					data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

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
					break;
			}

			//布置作业预览自然拼读
			$17.voxLog({
				module: "m_H1VyyebB",
				op    : "page_assign_BasicPractice_preview_click",
				s0    : constantObj.subject,
				s1    : self.tabType,
				s2    : categoryKO.categoryId()
			});

			return false;
		},
		updateUfoApp : function(sec,questionCnt){
			var self = this;
			constantObj._moduleSeconds[self.tabType] = constantObj._moduleSeconds[self.tabType] + sec;

			self.carts
			&& typeof self.carts["recalculate"] === 'function'
			&& self.carts.recalculate(self.tabType,questionCnt);
		},
		addCategory     : function(categoryGroup,lessonObj,self){
			var categoryKO = this,param = self.param || {};
			if(categoryKO.practices){
				var practices = categoryKO.practices();
				var _apps = constantObj._homeworkContent.practices[self.tabType].apps;
				if(practices && $.isArray(practices) && practices[0].questions){
					var _questions = ko.mapping.toJS(practices[0].questions());
					var _lessonId = lessonObj.lessonId();
					var _categoryId = categoryKO.categoryId();

					_apps.push({
						"practiceCategory"  : categoryKO.categoryName(),
						"categoryId"        : categoryKO.categoryId(),
						"practiceId"        : practices[0].practiceId(),
						"practiceName"      : practices[0].practiceName(),
						"lessonId"          : _lessonId,
						"questions"         : _questions,
						"book"              : ko.mapping.toJS(categoryKO.book),
						"objectiveId"       : param.objectiveTabType
					});

					categoryKO.checked(true);

					var sec = 0;
					for(var t = 0,tLen = _questions.length; t < tLen; t++){
						sec += (+_questions[t].seconds || 0);
					}
					self.updateUfoApp(sec,_apps.length);

					var reviewObj = $.extend(true,{},ko.mapping.toJS(this));
					reviewObj.lessonName = lessonObj.lessonName();
					reviewObj.lessonId = lessonObj.lessonId();
					reviewObj.categoryGroupId = categoryGroup.id();
					reviewObj.sentences = categoryGroup.sentences();
					reviewObj.newLine = (categoryGroup.newLine && categoryGroup.newLine());
					constantObj._reviewQuestions[self.tabType].push(reviewObj);
				}else{
					$17.info("应用未配题");
				}
			}else{
				$17.info("应用不存在或应用未配题");
			}
			$17.voxLog({
				module: "m_H1VyyebB",
				op    : "page_assign_BasicPractice_select_click",
				s0    : constantObj.subject,
				s1    : self.tabType,
				s2    : categoryKO.categoryId()
			});
		},
		removeCategory : function(parent,self){
			var categoryKO = this;
			var categoryId = categoryKO.categoryId();

			var _apps = constantObj._homeworkContent.practices[self.tabType].apps;
			var zIndex = -1;
			var sec = 0;
			var _lessonId = parent.lessonId();
			for(var k = 0,kLen = _apps.length; k < kLen; k++){
				if(_apps[k].categoryId === categoryId && _lessonId === _apps[k].lessonId){
					zIndex = k;
					var questions = _apps[k].questions;
					for(var t = 0,tLen = questions.length; t < tLen; t++){
						sec += (+questions[t].seconds || 0);
					}
					break;
				}
			}
			if(zIndex != -1){
				_apps.splice(zIndex,1);
				self.updateUfoApp(0 - sec, _apps.length);

				$.each(constantObj._reviewQuestions[self.tabType],function(i){
					if(this.categoryId == categoryId){
						constantObj._reviewQuestions[self.tabType].splice(i,1);
						return false;
					}
				});

			}else{
				$17.info("未在购物车找到，忽略");
			}
			categoryKO.checked(false);

			$17.voxLog({
				module: "m_H1VyyebB",
				op    : "page_assign_BasicPractice_remove_click",
				s0    : constantObj.subject,
				s1    : self.tabType,
				s2    : categoryKO.categoryId()
			});
		},
		initialise      : function (option) {
			var self = this;
			option = option || {};
			self.param = option;
			self.categoryIconPrefixUrl = option.categoryIconPrefixUrl || null;

			self.carts = option.carts || null;
			self.tabType = option.tabType || "NATURAL_SPELLING";

			//初始化
			var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
			if($ufoexam.has("span").length == 0){
				$ufoexam.empty().html(template("t:UFO_NATURAL_SPELLING",{tabTypeName : option.tabTypeName,count : 0}));
			}
		}
	};
	$17.homeworkv3 = $17.homeworkv3 || {};
	$17.extend($17.homeworkv3, {
		getNatural_spelling: function(){
			return new NaturalSpelling();
		}
	});
}($17,ko));