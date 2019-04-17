/**
 * Created by dell on 2018/1/11.
 * 绘本阅读另一种类型展示
 */
;(function($17,ko) {
    "use strict";
    var module             = "m_H1VyyebB";  //打点module
    var subject            = constantObj.subject;
    var PracticeTypesPopup = (function(){
        var noop = function(){};
        var pp = {
            pictureBookId       : null,
            questionPractices   : ko.observableArray([]),
            expandPractices     : ko.observableArray([]),
            practiceIds         : ko.observableArray([]),
            typeClick           : function(self){
                var practice = this;
                var type = practice.type;
                var typeIndex = self.practiceIds.indexOf(type);
                if(typeIndex == -1){
                    self.practiceIds.push(type);
                }else{
                    self.practiceIds.splice(typeIndex,1);
                }
                $17.voxLog({
                    module : module,
                    op     : "levelreadings_practice_type_click",
                    s0     : subject,
                    s1     : self.pictureBookId
                });
            }
        };
        var html = template("t:SELECT_PRACTICES_POPUP",{});
        return {
            showTypePopup : function(pictureBook,callback,readingModule){
                var practiceIds = [];
                callback = $.isFunction(callback) ? callback : noop;
                var practices = pictureBook.questionPractices;
                practices = $.isArray(practices) ? practices : [];

                var expandPractices = $.isArray(pictureBook.expandPractices) ? pictureBook.expandPractices : [];
                var allPractices = practices.concat(expandPractices);

                for(var j = 0,jLen = allPractices.length; j < jLen; j++){
                    var practice = allPractices[j];
                    !$17.isBlank(practice.isSelect) && String(practice.isSelect) === "true" && practiceIds.push(practice.type);
                }

                pp.pictureBookId = pictureBook.pictureBookId;
                pp.questionPractices(practices);
                pp.expandPractices(expandPractices);
                pp.practiceIds(practiceIds);
                var popState = {
                    state0 : {
                        name    : 'readingPractice',
                        comment : '选择绘本练习类型',
                        html    : html,
                        title   : '选择要推荐的练习类型',
                        position: { width : 350},
                        focus   : 1,
                        buttons : {"取消" : false,"确认" : true},
                        submit  : function(e,v,m,f){
                            if(v){
                                e.preventDefault();

                                var types = pp.practiceIds();
                                if(types.length > 0){
                                    var questionPractices = [],extendPractice = [];
                                    ko.utils.arrayForEach(pp.questionPractices(),function(practice,i){
                                        (types.indexOf(practice.type) != -1) && questionPractices.push(practice);
                                    });
                                    ko.utils.arrayForEach(pp.expandPractices(),function(practice,i){
                                        (types.indexOf(practice.type) != -1) && extendPractice.push(practice);
                                    });
                                    if(questionPractices.length == 0){
                                        $.prompt.goToState('state2', true);
                                        return false;
                                    }else{
                                        $17.voxLog({
                                            module  : module,
                                            op      : "levelreadings_practicetype_select",
                                            s0      : subject,
                                            s1      : pp.pictureBookId,
                                            s2      : JSON.stringify(extendPractice),
                                            s3      : readingModule
                                        });
                                        callback(questionPractices.concat(extendPractice));
                                    }
                                }else{
                                    $.prompt.goToState('state1', true);
                                    return false;
                                }
                            }
                            $.prompt.close();
                        }
                    },
                    state1 : {
                        html : "练习类型不能为空",
                        focus   : 1,
                        buttons : {"确认" : true},
                        submit  : function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('readingPractice');
                        }
                    },
                    state2 : {
                        html : "请选择必选练习",
                        focus   : 1,
                        buttons : {"确认" : true},
                        submit  : function(e,v,m,f){
                            e.preventDefault();
                            $.prompt.goToState('readingPractice');
                        }
                    }
                };

                $.prompt(popState,{
                    loaded : function(event){
                        ko.applyBindings(pp,document.getElementById("jqistate_readingPractice"));
                        $17.voxLog({
                            module  : module,
                            op      : "levelreadings_select_click",
                            s0      : subject,
                            s1      : pp.pictureBookId,
                            s2      : JSON.stringify(practices),
                            s3      : readingModule
                        });
                    }
                });

            }
        }
    }());

    function ReadingRecommend(options){
        var self = this;
        var defaultOpts = {
            addReading      : null,
            removeReading   : null,
            tabType         : ""
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.recTypeList    = ko.observableArray([]);
        self.pictureBookIds = ko.observableArray([]);
    }

    ReadingRecommend.prototype = {
        constructor : ReadingRecommend,
        init : function(recTypeList,config){
            config = $.isPlainObject(config) ? config : {};
            var self = this;
            if(!$.isArray(recTypeList) || recTypeList.length == 0){
                return false;
            }
            if(!this instanceof ReadingRecommend){
                //警告类型不对
                return false;
            }
            self.opts = $.extend(true,self.opts,config);
            for(var m = 0,mLen = recTypeList.length; m < mLen; m++){
                recTypeList[m]["beginPos"] = ko.observable(0);  //起始位置
                recTypeList[m]["moveCount"] = 3;
                recTypeList[m]["totalCount"] = recTypeList[m].pictureBookList.length;
            }

            var pictureBookIds = config.pictureBookIds || [];
            self.recTypeList(recTypeList);
            self.pictureBookIds(pictureBookIds);

        },
        addOrCancel : function(self,element,module){
            var pictureBook = this,opts = self.opts;
            var pId = pictureBook.pictureBookId;
            var pIndex = self.pictureBookIds.indexOf(pId);
            if(pIndex == -1){
                PracticeTypesPopup.showTypePopup(pictureBook,function(ps){
                    if(!$.isArray(ps) || ps.length == 0){
                        return false;
                    }
                    //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                    $(element).closest(".examTopicBox").fly({
                        target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                        border: "5px #39f solid",
                        time  : 600
                    });
                    self.pictureBookIds.push(pId);
                    var extendPictureBook = $.extend(true,{},pictureBook);
                    extendPictureBook.practices = ps;
                    $.isFunction(opts.addReading) && opts.addReading(extendPictureBook,module);
                },module);
            }else{
                self.pictureBookIds.splice(pIndex,1);
                $.isFunction(opts.removeReading) && opts.removeReading(pictureBook,module);
            }
        },
        arrowLeftClick : function(self){
            var recTypeObj = this;
            var oldBeginPos = recTypeObj.beginPos();
            if(oldBeginPos <= 0){
                return false;
            }
            var beginPos = (oldBeginPos - recTypeObj.moveCount);
            recTypeObj.beginPos(beginPos < 0 ? 0 : beginPos);
        },
        arrowRightClick : function(self){
            var recTypeObj = this;
            var oldBeginPos = recTypeObj.beginPos();
            var beginPos = (oldBeginPos + recTypeObj.moveCount);
            if(beginPos >= recTypeObj.totalCount){
                return false;
            }
            recTypeObj.beginPos(beginPos);
        },
        readingView : function(self,module){
            var reading = this;
            var opts = self.opts;
            $.isFunction(opts.readingPreivew) && opts.readingPreivew(reading,module);
        }
    };

    function AllReading(options){
        var self = this;
        var defaultOpts = {
            addReading      : null,
            removeReading   : null,
            tabType         : ""
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.clazzLevelList = ko.observableArray([]);
        self.levelIds       = ko.observableArray([]);
        self.levelSelectAll = ko.pureComputed(function(){
            // 不限：数组为零的情况
            return self.levelIds().length == 0;
        },self);
        self.levelIds.subscribe(function(levelArr){
            self.searchReadings({
                clazzLevel : levelArr.join(","),
                pageNum    : 1
            });
        });
        self.seriesList     = ko.observableArray([]);
        self.seriesIds      = ko.observableArray([]);
        self.seriesSelectAll= ko.pureComputed(function(){
            return self.seriesIds().length == 0;
        },self);
        self.seriesIds.subscribe(function(seriesArr){
            self.searchReadings({
                seriesIds : seriesArr.join(","),
                pageNum    : 1
            });
        });
        self.topicList      = ko.observableArray([]);
        self.topicIds       = ko.observableArray([]);
        self.topicSelectAll = ko.pureComputed(function(){
            return self.topicIds().length == 0;
        },self);
        self.topicIds.subscribe(function(topicArr){
            self.searchReadings({
                topicIds : topicArr.join(","),
                pageNum  : 1
            });
        });
        self.readingList    = ko.observableArray([]);
        self.pictureBookIds = ko.observableArray([]);
        self.searchOptions = {
            bookId      : null,
            unitId      : null,
            searchWord  : "",
            clazzLevel  : "",
            topicIds    : "",
            seriesIds   : "",
            pageNum     : 1,
            pageSize    : 9,
            subject     : null
        };
        self.pagination = $17.pagination.initPages({
            pageClickCb : self.pageClickPost.bind(self)
        });
    }
    AllReading.prototype = {
        constructor : AllReading,
        init : function(obj,searchParam,config){
            config = $.isPlainObject(config) ? config : {};
            var self = this;
            if($.isEmptyObject(obj)){
                return false;
            }
            if(!self instanceof  AllReading){
                //类型错误
                return false;
            }
            self.opts = $.extend(true,self.opts,config);
            self.clazzLevelList(obj.clazzLevelList || []);
            self.seriesList(obj.seriesList || []);
            self.topicList(obj.topicList || []);

            var pictureBookIds = config.pictureBookIds || [];
            self.searchReadings(searchParam);
            self.pictureBookIds(pictureBookIds);
        },
        addOrCancelLevel : function (self) {
            var levelObj = this;
            var levelIndex = self.levelIds.indexOf(levelObj.levelId);
            if(self.levelIds.length > 0 && levelIndex != -1){
                //已经添加了
                return false;
            }else{
                self.levelIds([levelObj.levelId]);
                $17.voxLog({
                    module  : module,
                    op      : "levelreadings_allbooks_grades_click",
                    s0      : subject
                });
            }
        },
        addOrCancelSeries : function(self){
            var seriesObj = this,seriesId = seriesObj.seriesId;
            var seriesIndex = self.seriesIds.indexOf(seriesId);
            if(seriesIndex != -1){
                self.seriesIds.splice(seriesIndex,1);
            }else{
                self.seriesIds.push(seriesId);
            }
            $17.voxLog({
                module  : module,
                op      : "levelreadings_allbooks_series_click",
                s0      : subject
            });
        },
        addOrCancelTopic : function(self){
            var topicObj = this,topicId = topicObj.topicId;
            var topicIndex = self.topicIds.indexOf(topicId);
            if(topicIndex != -1){
                self.topicIds.splice(topicIndex,1);
            }else{
                self.topicIds.push(topicId);
            }
            $17.voxLog({
                module  : module,
                op      : "levelreadings_allbooks_topics_click",
                s0      : subject
            });
        },
        unlimitClick : function(type){
            var self = this;
            var logOp;
            switch (type){
                case "clazzLevels":
                    self.levelIds([]);
                    logOp = "levelreadings_allbooks_grades_click";
                    break;
                case "topics":
                    self.topicIds([]);
                    logOp = "levelreadings_allbooks_topics_click";
                    break;
                case "series":
                    self.seriesIds([]);
                    logOp = "levelreadings_allbooks_series_click";
                    break;
                default:
                    break;
            }
            $17.voxLog({
                module  : module,
                op      : logOp,
                s0      : subject
            });
        },
        showAll : function(element){
            var $element = $(element);
            var $item = $element.parents(".theme-box");
            if($item.hasClass("showAll")){
                $item.removeClass("showAll");
            }else{
                $item.addClass("showAll");
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op : "page_assign_PictureBook_all_more_click",
                    s0 : subject,
                    s1 : "READING",
                    s2 : $element.attr("filterType")
                });
            }
        },
        addOrCancel : function(self,element){
            var pictureBook = this,opts = self.opts;
            var pId = pictureBook.pictureBookId;
            var pIndex = self.pictureBookIds.indexOf(pId);
            if(pIndex == -1){
                PracticeTypesPopup.showTypePopup(pictureBook,function(ps){
                    if(!$.isArray(ps) || ps.length == 0){
                        return false;
                    }
                    //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                    $(element).closest(".examTopicBox").fly({
                        target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                        border: "5px #39f solid",
                        time  : 600
                    });
                    self.pictureBookIds.push(pId);
                    var extendPictureBook = $.extend(true,{},pictureBook);
                    extendPictureBook.practices = ps;
                    $.isFunction(opts.addReading) && opts.addReading(extendPictureBook,"all");
                },"all");
            }else{
                self.pictureBookIds.splice(pIndex,1);
                $.isFunction(opts.removeReading) && opts.removeReading(pictureBook,"all");
            }
        },
        pageClickPost  : function(pageNo){
            this.searchReadings({
                pageNum : pageNo
            });
        },
        searchReadings : function(searchParam){
            var self = this;
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            var url = "/teacher/new/homework/picturebookplus/search.vpage";
            $.post(url,self.searchOptions).done(function(data){
                if(data.success){
                    self.readingList(data.pictureBookList);
                    self.pagination.setPage(data.pageNum,data.pageCount);

                    $17.voxLog({
                        module  : module,
                        op      : "levelreadings_searchresult_load",
                        s0      : subject,
                        s1      : $.toJSON(self.searchOptions),
                        s2      : (self.searchOptions.pageSize * data.pageCount)
                    });
                }else{
                    self._resetSearchReading();
                    data.errorCode != "200" && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : url,
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(self.searchOptions),
                        s3     : $uper.env
                    });
                }
            }).fail(function(e){
                self._resetSearchReading();
            });
        },
        _resetSearchReading : function(){
            var self = this;
            self.readingList([]);
            self.pagination.setPage(1,0);
        },
        readingView : function(self){
            var reading = this;
            var opts = self.opts;
            $.isFunction(opts.readingPreivew) && opts.readingPreivew(reading,"all");
        },
        resetReadingData : function(){
            var self = this;
            self._resetSearchReading();
            self.pictureBookIds([]);
        },
        setSearchWord : function(searchWord){
            var self = this;
            self.searchOptions.searchWord = searchWord || "";
        }
    };
    function AssignedHistory(options){
        var self = this;
        var defaultOpts = {
            addReading      : null,
            removeReading   : null,
            tabType         : ""
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.historyList = ko.observableArray([]);
        self.pictureBookIds = ko.observableArray([]);
        self.pagination = $17.pagination.initPages({
            pageClickCb : self.pageClickPost.bind(self)
        });
        self.searchOptions = {
            bookId      : null,
            unitId      : null,
            pageNum     : 1,
            pageSize    : 9,
            subject     : null
        };
    }
    AssignedHistory.prototype = {
        constructor : AssignedHistory,
        init : function(obj,searchParam,config){
            config = $.isPlainObject(config) ? config : {};
            var self = this;
            if($.isEmptyObject(obj)){
                return false;
            }
            if(!self instanceof  AssignedHistory){
                //类型错误
                return false;
            }
            self.loadHistoryList(searchParam);
            //注意的地方,当深拷贝时，如果后一个对象是属性值是一个空数组，且前一个对象的相同属性值时数组时，
            // 不会覆盖前面相同属性的值 $.extend(true,{a:[1]},{a:[]}) ==> {a:[1]}
            self.opts = $.extend(true,self.opts,config);
            var pictureBookIds = config.pictureBookIds || [];
            self.pictureBookIds(pictureBookIds);
        },
        loadHistoryList : function(searchParam){
            var self = this;
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            var url = "/teacher/new/homework/picturebookplus/history.vpage";
            $.get(url,self.searchOptions).done(function(data){
                if(data.success){
                    self.historyList(data.pictureBookList);
                    self.pagination.setPage(data.pageNum,data.pageCount);
                }else{
                    data.errorCode != "200" && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : url,
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(self.searchOptions),
                        s3     : $uper.env
                    });
                }
            }).fail(function(e){

            });
        },
        addOrCancel : function(self,element){
            var pictureBook = this,opts = self.opts;
            var pId = pictureBook.pictureBookId;
            var pIndex = self.pictureBookIds.indexOf(pId);
            if(pIndex == -1){
                PracticeTypesPopup.showTypePopup(pictureBook,function(ps){
                    if(!$.isArray(ps) || ps.length == 0){
                        return false;
                    }
                    //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                    $(element).closest(".examTopicBox").fly({
                        target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                        border: "5px #39f solid",
                        time  : 600
                    });
                    self.pictureBookIds.push(pId);
                    var extendPictureBook = $.extend(true,{},pictureBook);
                    extendPictureBook.practices = ps;
                    $.isFunction(opts.addReading) && opts.addReading(extendPictureBook,"history");
                },"history");
            }else{
                self.pictureBookIds.splice(pIndex,1);
                $.isFunction(opts.removeReading) && opts.removeReading(pictureBook,"history");
            }
        },
        pageClickPost:function(pageNo){
            this.loadHistoryList({
                pageNum     : pageNo
            });
        },
        readingView : function(self){
            var reading = this;
            var opts = self.opts;
            $.isFunction(opts.readingPreivew) && opts.readingPreivew(reading,"history");
        }
    };

    var isInitUFO = true;

    function ReadingPackage(id,name){
        this.id = id;
        this.name = name;
    }
    var packageList = [new ReadingPackage("PK_RECOMMEND","推荐绘本"),
        new ReadingPackage("PK_ALL","全部绘本"),
        new ReadingPackage("PK_ASSIGNED_HISTORY","推荐历史")];

    function LevelReadings(){
        var self = this;
        self.includeAllModule = ko.observable(false);
        self.readingRecommend   = new ReadingRecommend({
            readingPreivew  : self.readingPreivew.bind(self),
            addReading      : self.addReading.bind(self),
            removeReading   : self.removeReading.bind(self)
        });
        self.allReading         = new AllReading({
            readingPreivew  : self.readingPreivew.bind(self),
            addReading      : self.addReading.bind(self),
            removeReading   : self.removeReading.bind(self)
        });
        self.assignedHistory    = new AssignedHistory({
            readingPreivew  : self.readingPreivew.bind(self),
            addReading      : self.addReading.bind(self),
            removeReading   : self.removeReading.bind(self)
        });
        self.searchWord         = ko.observable("");
        self.searchWord.subscribe(function(val){
            self.allReading.setSearchWord(val);
        });
        self.displayReadings    = ko.observableArray([]);
        self.config_reading     = {};
        self.subject            = constantObj.subject;
        self.packageList        = ko.observableArray(packageList);
        self.focusIndex         = ko.observable(0);
        self.focusPackage       = ko.pureComputed(function(){
            var packageObj = this.packageList()[this.focusIndex()];
            var config = this.config_reading;
            var logOp;
            switch (packageObj.id){
                case "PK_ASSIGNED_HISTORY":
                    self.assignedHistory.init(packageObj,{
                        bookId      : config.bookId,
                        unitId      : config.unitId,
                        subject     : self.subject
                    },{
                        tabType         : config.tabType,
                        pictureBookIds  : self.getPictureBookIdsInCart()
                    });
                    logOp = "levelreadings_history_click";
                    break;
                case "PK_ALL":
                    self.allReading.searchReadings({
                        pageNum : 1
                    });
                    // self.allReading.setSearchWord(self.searchWord());
                    logOp = "levelreadings_allbooks_click";
                    break;
                default:
                    logOp = "levelreadings_recommend_click";
                    break;
            }
            $17.voxLog({
                module : module,
                op     : logOp,
                s0     : self.subject
            });

            return packageObj;
        },self);
        self.carts              = null;
    }

    LevelReadings.prototype = {
        constructor : LevelReadings,
        initialise:function(config){
            this.config_reading = config;
            this.carts = config.carts || null;
            if(isInitUFO){
                isInitUFO = false;
                var str = ["<span class=\"name\">" + config.tabTypeName +"</span>" +
                "<span class=\"count\" data-count=\"0\">0</span>" +
                "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join("");
                $(".J_UFOInfo p[type='" + config.tabType + "']").html(str);
            }
        },
        getPictureBookIdsInCart : function(){
            var config = this.config_reading;
            var pictureBookIds = [];
            $.each(constantObj._homeworkContent.practices[config.tabType].apps,function(){
                pictureBookIds.push(this.pictureBookId);
            });
            return pictureBookIds;
        },
        run: function(){
            var self = this;
            var config = this.config_reading;
            var _sectionIds = $.map(config.sections,function(item){
                return item.sectionId;
            });
            var paramData = {
                sections            : _sectionIds.join(","),
                type                : config.tabType,
                unitId              : config.unitId,
                bookId              : config.bookId,
                subject             : self.subject,
                objectiveConfigId   : config.objectiveConfigId
            };
            $.get("/teacher/new/homework/objective/content.vpage", paramData).done(function(data){
                if(data.success){
                    var content = data.content || [];
                    var recTypeList = [];

                    var pictureBookIds = self.getPictureBookIdsInCart();
                    var includeAllModule = false;
                    $.each(content,function(){
                        if(this.module == "all"){
                            includeAllModule = true;
                            self.allReading.init(this,{
                                bookId      : config.bookId,
                                unitId      : config.unitId,
                                subject     : self.subject,
                                pageNum     : 1
                            },{
                                tabType         : config.tabType,
                                pictureBookIds  : pictureBookIds
                            });
                        }else{
                            recTypeList.push(this);
                        }
                    });
                    self.includeAllModule(includeAllModule);
                    self.readingRecommend.init(recTypeList,{
                        tabType         : config.tabType,
                        pictureBookIds  : pictureBookIds
                    });
                }else{
                    self._resetReading();
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
            }).fail(function(e){
                self._resetReading();
            });
        },
        _resetReading  : function(){
            var self = this;
            var config = this.config_reading;
            var pictureBookIds = self.getPictureBookIdsInCart();
            self.allReading.resetReadingData();
            self.readingRecommend.init([],{
                tabType         : config.tabType,
                pictureBookIds  : pictureBookIds
            });
        },
        packageClick   : function(packageIndex,self){
            self.focusIndex(packageIndex);
        },
        displayMode    : function(self, bindingContext){
            var template = "t:",pkId = self.focusPackage().id;
            switch (pkId){
                case "PK_RECOMMEND":
                case "PK_ALL":
                case "PK_ASSIGNED_HISTORY":
                    template += pkId;
                    break;
                default:
                    template += "UNKNOWN_READINGS";
            }
            return template;
        },
        searchClick : function(){
            var self = this;
            var pkAllIndex;
            var focusIndex = self.focusIndex();
            ko.utils.arrayForEach(self.packageList(),function(obj,i){
                if(obj.id === "PK_ALL"){
                    pkAllIndex = i;
                }
            });
            if(!$17.isBlank(pkAllIndex) && focusIndex != pkAllIndex){
                var searchWord = self.searchWord();
                if($17.isBlank(searchWord) && searchWord === ''){
                    return false;
                }
                self.packageClick.call(self.packageList()[pkAllIndex],pkAllIndex,self);
            }else if(focusIndex == pkAllIndex){
                self.allReading.searchReadings({
                    pageNum : 1
                });
            }
            $17.voxLog({
                module  : module,
                op      : "levelreadings_search_click",
                s0      : constantObj.subject
            });
        },
        addReading : function(reading,readingType){
            /* readingType : {
                 "syncRecommend" : "课堂同步拓展",
                 "topicRecommend" : "主题阅读",
                 "seriesRecommend" : "系列阅读",
                 "all"  : "全部",
                 "history" : "布置历史"
             }
             */
            var self = this;
            var config = self.config_reading;
            var practices = reading.practices;
            var types = [];
            for(var t = 0,tLen = practices.length; t < tLen; t++){
                types.push(practices[t].type);
            }
            constantObj._homeworkContent.practices[config.tabType].apps.push({
                pictureBookId   : reading.pictureBookId,
                book            : reading.book,
                practiceTypes   : types,
                objectiveId     : config.objectiveTabType
            });

            var seconds = reading.seconds;
            for(var m = 0,mLen = practices.length; m < mLen; m++){
                seconds += (practices[m].seconds || 0);
            }

            constantObj._moduleSeconds[config.tabType] += seconds;

            var reviewObj = $.extend(true,{
                unitId      : config.unitId
            },reading);
            constantObj._reviewQuestions[config.tabType].push(reviewObj);

            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_PictureBook_recommend_select_click",
                s0 : subject,
                s1 : config.tabType,
                s2 : readingType,
                s3 : reading.pictureBookId
            });
            self.reSetUFO();
        },
        removeReading:function(reading,readingType){
            var self = this;
            var config = self.config_reading;
            $.each(constantObj._homeworkContent.practices[config.tabType].apps,function(i){
                if(this.pictureBookId == reading.pictureBookId){
                    constantObj._homeworkContent.practices[config.tabType].apps.splice(i,1);
                    return false;
                }
            });
            $.each(constantObj._reviewQuestions[config.tabType],function(i){
                if(this.pictureBookId == reading.pictureBookId){
                    constantObj._reviewQuestions[config.tabType].splice(i,1);
                    return false;
                }
            });

            var practices = reading.practices;
            var seconds = reading.seconds;
            for(var m = 0,mLen = practices.length; m < mLen; m++){
                seconds += (practices[m].seconds || 0);
            }

            constantObj._moduleSeconds[config.tabType] -= seconds;
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_PictureBook_recommend_remove_click",
                s0 : subject,
                s1 : config.tabType,
                s2 : readingType,
                s3 : reading.pictureBookId
            });
            self.reSetUFO();
        },
        reSetUFO : function(){
            var self = this;
            var count = constantObj._homeworkContent.practices[self.config_reading.tabType].apps.length;
            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(self.config_reading.tabType,count);
        },
        readingPreivew : function(reading,readingType){
            var self = this;
            var readingParam = {
                pictureBookIds : reading.pictureBookId,
                from : "preview"
            };
            self.fetchPrompt({
                buttons     : {"全屏查看" : true},
                readingParam : readingParam,
                submitFn : function(e,v,f,m){
                    if(v){
                        self.fetchPrompt({
                            top         : 0,
                            promptWidth : $(window).width(),
                            readingContainerHeight : $(window).height()-150, //去掉浏览菜单及标签的高度
                            readingParam : readingParam
                        });
                        return false;
                    }
                }
            });
        },
        fetchPrompt : function(options){
            var newOptions = $.extend({},{
                promptWidth : 960,
                buttons    : {},
                top: '8%',
                readingContainerHeight : 644,
                readingParam : {},
                submitFn : function(e,v,m,f){ return true;},
                closeFn  : function(){}
            },options);

            var dataHtml = "";
            var domain = "/";
            if(constantObj.env === "test"){
                domain = "//www.test.17zuoye.net/";
            }else{
                domain = location.protocol + "//" + location.host;
            }
            var gameUrl = domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param(newOptions.readingParam);
            dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="' + (newOptions.promptWidth - 60) + '" marginwidth="0" height="' + newOptions.readingContainerHeight + '" marginheight="0" scrolling="no" frameborder="0"></iframe>';

            $.prompt(dataHtml, {
                title   : "预 览",
                top: newOptions.top,
                buttons : newOptions.buttons,
                position: { width: newOptions.promptWidth },
                submit  : newOptions.submitFn,
                close   : newOptions.closeFn
            });

        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getLevel_readings: function(){
            return new LevelReadings();
        }
    });
}($17,ko));


