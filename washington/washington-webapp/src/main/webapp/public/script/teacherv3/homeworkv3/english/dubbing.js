!function($17,ko) {
	"use strict";
	var isInitUFO = true;

	var Dubbing = function(){
		this.weeklyRecommend = ko.observable({});
		this.searchWord 	 = ko.observable("");
		this.searchList      = ko.observable({});
		this.searchResult    = ko.observableArray([]);

		this.currentPageQuestionsCount = 8;
		this.currentPage     = ko.observable(1);
		this.userInputPage   = ko.observable(null);
		this.totalPage       = ko.observable(1);
		this.config_dubbing  = {};
	};
	Dubbing.prototype = {
		constructor : Dubbing,
		levelList : [{
			key : 1,
			name : "一年级"
		},{
			key : 2,
			name : "二年级"
		},{
			key : 3,
			name : "三年级"
		},{
			key : 4,
			name : "四年级"
		},{
			key : 5,
			name : "五年级"
		},{
			key : 6,
			name : "六年级"
		}],
		initialise:function(config){
			this.config_dubbing = config;
			this.carts = config.carts || null;
			this.searchOptions = {
				bookId      : config.bookId,
				unitId      : config.unitId,
				clazzLevel  : "0",
				channelIds  : "",
				albumIds    : "",
				themeIds    : "",
                searchWord  : "",
				pageNum     : 1,
				pageSize    : this.currentPageQuestionsCount,
				subject     : constantObj.subject
			};

			if(isInitUFO){
				isInitUFO = false;
				var str = ["<span class=\"name\">" + config.tabTypeName +"</span>" +
				"<span class=\"count\" data-count=\"0\">0</span>" +
				"<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join("");
				$(".J_UFOInfo p[type='" + config.tabType + "']").html(str);
			}
		},
		run: function(){
			var self = this;
			var _sectionIds = $.map(this.config_dubbing.sections,function(item){
				return item.sectionId;
			}),paramData = {
				sections : _sectionIds.join(","),
				type     : this.config_dubbing.tabType,
				unitId   : this.config_dubbing.unitId,
				bookId   : this.config_dubbing.bookId,
				subject  : constantObj.subject,
				objectiveConfigId : this.config_dubbing.objectiveConfigId
			};
			$.get("/teacher/new/homework/objective/content.vpage", paramData,function(data){
				if(data.success){
					var content = data.content || [];
					self.weeklyRecommend(ko.mapping.fromJS({}));
					self.searchList(ko.mapping.fromJS({}));
					self.searchResult(ko.mapping.fromJS([]));

					$.each(content,function(){
						if(this.module == "recommend"){
							$.each(this.dubbingList,function(){
								var item = this;
								item.isChecked = false;
								$.each(constantObj._homeworkContent.practices[self.config_dubbing.tabType].apps,function(){
									if(this.dubbingId == item.dubbingId){
										item.isChecked = true;
										return false;
									}
								});
							});

							self.weeklyRecommend(ko.mapping.fromJS(this));
							new SimpleSlider({
								slideName      : "J_"+this.module,
								clickLeftId    : "#left-"+this.module,
								clickRightId   : "#right-"+this.module,
								slideContainer : "#container-"+this.module,
								slideItem      : "",
								itemWidth      : "190",
								slideCount     : 4,
								totalCount     : this.dubbingList.length
							});
						}else if(this.module == "all"){
							self.searchList(ko.mapping.fromJS(this));
							self.searchOptions.clazzLevel = this.defaultClazzLevel || 0;
							self.searchDubbings();
						}
					});

					$17.voxLog({
						module : "m_H1VyyebB",
						op     : "o_IVh8RzeE"
					});

				}else{
					(data.errorCode !== "200") && $17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/new/homework/content.vpage",
						s1     : $.toJSON(data),
						s2     : $.toJSON(paramData),
						s3     : $uper.env
					});
				}
			});
		},
		showAll : function(element){
			var $item = $(element).parents(".theme-box");
			if($item.hasClass("showAll")){
				$item.removeClass("showAll");
			}else{
				$item.addClass("showAll");
			}
		},
		selLevelsFilter : function(self,element){
			var $item = $(element);
			var filterType = $item.attr("filterType"),filterId = $item.attr("filterid");
			$item.addClass("w-radio-current").siblings("p.filter-item").removeClass("w-radio-current");
			self.resetFilter(filterType,filterId);

			$17.voxLog({
				module : "m_H1VyyebB",
				op     : "o_bsVK4soS",
				s0     : $item.find("span[title]").text(),
				s1     : filterId
			});
		},
		noFilter: function(element){
			var item = $(element);
			var $siblingArr = item.addClass("label-check-current").siblings().children("p.filter-item").removeClass("w-checkbox-current");

			this.resetFilter();
		},
		selFilter : function(self,element){
			var $item = $(element);
			var filterType = $item.attr("filterType"),filterId = $item.attr("filterid");
			if($item.hasClass("w-checkbox-current")){
				$item.removeClass("w-checkbox-current");
			}else{
				$item.addClass("w-checkbox-current");

				var logOp;
				switch(filterType){
					case "channels":
						logOp = "o_c2lv3OZV";
						break;
					case "themes":
						logOp = "o_USE40Ze0";
						break;
					case "albums":
						logOp = "o_bMcTXdqn";
						break;
					default:
						break;
				}
				logOp && $17.voxLog({
					module : "m_H1VyyebB",
					op     : logOp,
					s0     : $item.find("span[title]").text(),
					s1     : filterId
				});
			}
			self.resetFilter(filterType,filterId);
		},
		resetFilter : function(filterType,filterId){
			var filter = {clazzLevels  : [], channels  : [], themes : [],albums:[]};
			for(var temp in filter){
				$(".J_filter-"+temp).find(".filter-item").each(function(){
					var item = $(this);
					if(item.hasClass("w-checkbox-current")){
						filter[temp].push(item.attr("filterId"));
					}else if(item.hasClass("w-radio-current")){
						filter[temp].push(item.attr("filterId"));
					}
				});

				if(filter[temp].length == 0){
					$(".J_filter-" + temp + " .selAll").addClass("label-check-current");
				}else{
					$(".J_filter-" + temp + " .selAll").removeClass("label-check-current");
				}
			}

			this.searchOptions.pageNum     = 1;
			this.searchOptions.clazzLevel  = filter.clazzLevels.join(",");
			this.searchOptions.channelIds    = filter.channels.join(",");
			this.searchOptions.albumIds   = filter.albums.join(",");
			this.searchOptions.themeIds   = filter.themes.join(",");

			this.searchDubbings();
		},
		searchDubbings : function(){
			var self = this;
            self.searchOptions.searchWord = self.searchWord();
			$.post("/teacher/new/homework/dubbing/search.vpage",self.searchOptions,function(data){
				if(data.success){
					$.each(data.dubbingList,function(){
						var item = this;
						this.isChecked = false;
						$.each(constantObj._homeworkContent.practices[self.config_dubbing.tabType].apps,function(){
							if(this.dubbingId == item.dubbingId){
								item.isChecked = true;
								return false;
							}
						});
					});
					self.searchResult(ko.mapping.fromJS(data.dubbingList)());
					self.currentPage(data.pageNum);
					self.totalPage(data.pageCount);
				}else{
					(data.errorCode !== "200") && $17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/new/homework/reading/search.vpage",
						s1     : $.toJSON(data),
						s2     : $.toJSON(self.searchOptions),
						s3     : $uper.env
					});
				}
			});
		},

		addOrCancel : function(self,element,from){
			var data = this,config = self.config_dubbing,isRepeat=false;
			var readingType = $(element).parents("li").attr("readingtype");
			if(!$(element).hasClass("cancel")){
				$(element).parents("li").addClass("active");
				$.each(constantObj._reviewQuestions[config.tabType],function(){
					if(this.dubbingId == data.dubbingId()){
						$17.alert("该题目与已选题目重复~");
						isRepeat = true;
						return false;
					}
				});
				if(isRepeat) return false;

				constantObj._homeworkContent.practices[config.tabType].apps.push({
					dubbingId   : data.dubbingId(),
					book        : ko.mapping.toJS(data.book),
					objectiveId : config.objectiveTabType
				});

				constantObj._moduleSeconds[config.tabType] += data.seconds();

				var reviewObj = $.extend(true,{
					unitId      : config.unitId
				},ko.mapping.toJS(this));
				constantObj._reviewQuestions[config.tabType].push(reviewObj);

				$(element).closest(".examTopicBox").fly({
					target: ".J_UFOInfo p[type='" + config.tabType + "']",
					border: "5px #39f solid",
					time  : 600
				});


			}else{
				$(element).parents("li").removeClass("active");
				$.each(constantObj._homeworkContent.practices[config.tabType].apps,function(i){
					if(this.dubbingId == data.dubbingId()){
						constantObj._homeworkContent.practices[config.tabType].apps.splice(i,1);
						return false;
					}
				});
				$.each(constantObj._reviewQuestions[config.tabType],function(i){
					if(this.dubbingId == data.dubbingId()){
						constantObj._reviewQuestions[config.tabType].splice(i,1);
						return false;
					}
				});

				constantObj._moduleSeconds[config.tabType] -= data.seconds();
			}
			this.isChecked(!this.isChecked());
			self.reSetUFO();

			$17.voxLog({
				module : "m_H1VyyebB",
				op     : "o_CSZXqDcq",
				s0     : from,
				s1     : this.isChecked() ? "选入" : "移除"
			});
		},
		dubbingView : function(self,from){
			var dubbingKO = this;
			var dataHtml = "";
			var dubbingObj = ko.mapping.toJS(dubbingKO);
			dataHtml = template("t:SINGLE_DUBBING_PREVIEW",{
				dubbingObj : dubbingObj
			});
			var flashWidth = 550,flashHeight = 275;
			$.prompt(dataHtml,{
				title   : "预 览",
				position    : { width: 600},
				buttons     : {},
				focus       : 1,
				submit:function(e,v,m,f){},
				close   : function(){},
				loaded : function(){
					$("#dubbingPlayVideoContainer").getFlash({
						id       : "DUBBING_PLAY_PREVIEW",
						width    : flashWidth,//flash 宽度
						height   : flashHeight, //flash 高度
						movie    : constantObj.flashPlayerUrl,
						scale    : 'showall',
						flashvars: "file=" + dubbingObj.videoUrl + "&amp;image=" + dubbingObj.coverUrl + "&amp;width=" + flashWidth + "&amp;height=" + flashHeight + "&amp;autostart=true"
					});
				}
			});

			$17.voxLog({
				module : "m_H1VyyebB",
				op     : "o_gKHSy6Xw",
				s0     : from,
				s1     : dubbingObj.dubbingId
			});
		},
		reSetUFO : function(){
			var self = this;
			var count = constantObj._homeworkContent.practices[this.config_dubbing.tabType].apps.length;
			self.carts
			&& typeof self.carts["recalculate"] === 'function'
			&& self.carts.recalculate(this.config_dubbing.tabType,count);
		},

		page_click : function(self,pageNo){
			pageNo = +pageNo || 0;
			if(pageNo < 1 || pageNo > self.totalPage() || pageNo == self.currentPage()){
				return false;
			}
			self.searchOptions.pageNum = pageNo;
			self.searchDubbings();
		},
		goSpecifiedPage:function(){
			var self = this;
			var pageNo = self.userInputPage();
			if(/\D/g.test(pageNo)){
				self.userInputPage(null);
			}else{
				self.page_click(self,pageNo);
			}
		}
	};

	$17.homeworkv3 = $17.homeworkv3 || {};
	$17.extend($17.homeworkv3, {
		getDubbing: function(){
			return new Dubbing();
		}
	});
}($17,ko);


