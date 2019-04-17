!function($17,ko) {
    "use strict";
    var isInitUFO = true;

    function DubbingPackage(id,name){
        this.id = id;
        this.name = name;
    }

    var packageList = [new DubbingPackage("DUBBING_RECOMMEND","推荐配音"),
        new DubbingPackage("DUBBING_ALL","全部配音"),
        new DubbingPackage("COLLECT_DUBBING","我的收藏")];

    /**
     * 推荐配音
     * @param options
     * @constructor
     */
    function DubbingRecommend(options){
        var self = this;
        var defaultOpts = {
            dubbingPreview  : null,
            addDubbing      : null,
            removeDubbing   : null,
            tabType         : "",
            from            : "DUBBING_RECOMMEND" //来源
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.recommendModuleList = ko.observableArray([]);
        self.dubbingIds = ko.observableArray([]); //选择的dubbingIds
    }

    DubbingRecommend.prototype = {
        constructor : DubbingRecommend,
        init : function(recommendModuleList,config){
            config = $.isPlainObject(config) ? config : {};
            var self = this;
            if(!$.isArray(recommendModuleList) || recommendModuleList.length === 0){
                return false;
            }
            if(!this instanceof DubbingRecommend){
                //警告类型不对
                return false;
            }
            self.opts = $.extend(true,self.opts,config);
            for(var m = 0,mLen = recommendModuleList.length; m < mLen; m++){
                recommendModuleList[m]["beginPos"] = ko.observable(0);  //起始位置
                recommendModuleList[m]["moveCount"] = 4;
                recommendModuleList[m]["totalCount"] = recommendModuleList[m].dubbingList.length;
            }

            var dubbingIds = config.dubbingIds || [];
            self.recommendModuleList(recommendModuleList);
            self.dubbingIds(dubbingIds);

        },
        addOrCancel : function(self,element,recommendModule){
            //与全部配音和我的收藏方法不同的时，方法参数多了一个recommendModule参数，是因为推荐里想再详分出来自具体的哪个推荐模块
            var dubbingObj = this,opts = self.opts;
            var pId = dubbingObj.dubbingId;
            var pIndex = self.dubbingIds.indexOf(pId);
            var extendDubbing = $.extend(true,{},dubbingObj);
            if(pIndex === -1){
                //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
                self.dubbingIds.push(pId);
                $.isFunction(opts.addDubbing) && opts.addDubbing(extendDubbing,opts.from);
            }else{
                self.dubbingIds.splice(pIndex,1);
                $.isFunction(opts.removeDubbing) && opts.removeDubbing(extendDubbing,opts.from);
            }
        },
        dubbingView : function(self,module){
            var dubbingObj = this;
            var opts = self.opts;
            $.isFunction(opts.dubbingPreview) && opts.dubbingPreview(dubbingObj,opts.from);
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
        }
    };

    /**
     * 全部配音
     * @param options
     * @constructor
     */
    function AllDubbing(options){
        var self = this;
        var defaultOpts = {
            dubbingPreview  : null,
            addDubbing      : null,
            removeDubbing   : null,
            tabType         : "",
            from            : "DUBBING_ALL"
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.dubbingList    = ko.observableArray([]);
        self.dubbingIds = ko.observableArray([]);
        this.searchOptions = {
            bookId      : null,
            unitId      : null,
            clazzLevel  : "0",
            channelIds  : "",
            albumIds    : "",
            themeIds    : "",
            searchWord  : "",
            pageNum     : 1,
            pageSize    : 8,
            subject     : null
        };
        self.pagination = $17.pagination.initPages({
            pageClickCb : self.pageClickPost.bind(self)
        });
        self.description = ko.observable("");

        self.defaultLevelSize = 5;
        self.levelSize = ko.observable(self.defaultLevelSize);
        self.defaultClazzLevel = ko.observable(0);

        self.defaultChannelSize = 5;
        self.channelSize = ko.observable(self.defaultChannelSize);
        self.channelList = ko.observableArray([]);
        self.channelIds = ko.observableArray([]);

        self.defaultThemeSize = 5;
        self.themeSize = ko.observable(self.defaultThemeSize);
        self.themeList = ko.observableArray([]);
        self.themeIds = ko.observableArray([]);

        self.defaultAlbumSize = 5;
        self.albumSize = ko.observable(self.defaultAlbumSize);
        self.albumList = ko.observableArray([]);
        self.albumIds = ko.observableArray([]);
    }
    AllDubbing.prototype = {
        constructor : AllDubbing,
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
        init : function(obj){
            var self = this;
            if($.isEmptyObject(obj)){
                return false;
            }
            if(!self instanceof  AllDubbing){
                //类型错误
                return false;
            }
            self.description(obj.description || "");
            self.defaultClazzLevel(obj.defaultClazzLevel || 0);
            self.themeList(obj.themeList);
            self.albumList(obj.albumList);
            self.channelList(obj.channelList);
        },
        run : function(searchParam,config){
            var self = this;
            self.opts = $.extend(true,self.opts,config);
            var dubbingIds = config.dubbingIds || [];
            self.resetFilter(searchParam);
            self.dubbingIds(dubbingIds);
        },
        addOrCancel : function(self,element){
            var dubbingObj = this,opts = self.opts;
            var pId = dubbingObj.dubbingId;
            var pIndex = self.dubbingIds.indexOf(pId);
            var extendDubbing = $.extend(true,{},dubbingObj);
            if(pIndex === -1){
                //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
                self.dubbingIds.push(pId);
                $.isFunction(opts.addDubbing) && opts.addDubbing(extendDubbing,opts.from);
            }else{
                self.dubbingIds.splice(pIndex,1);
                $.isFunction(opts.removeDubbing) && opts.removeDubbing(extendDubbing,opts.from);
            }
        },
        searchDubbings : function(searchParam){
            var self = this;
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            $.post("/teacher/new/homework/dubbing/search.vpage",self.searchOptions).done(function(data){
                if(data.success){
                    self.dubbingList(data.dubbingList);
                    self.pagination.setPage(data.pageNum,data.pageCount);
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op     : "dubwithscore_result_load",
                        s0     : JSON.stringify(self.searchOptions),
                        s1     : data.totalSize
                    });
                }else{
                    self._resetSearchDubbing();
                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/dubbing/search.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(self.searchOptions),
                        s3     : $uper.env
                    });
                }
            }).fail(function(){
                self._resetSearchDubbing();
            });
        },
        pageClickPost  : function(pageNo){
            this.resetFilter({
                pageNum : pageNo
            });
        },
        _resetSearchDubbing : function(){
            var self = this;
            self.dubbingList([]);
            self.pagination.setPage(1,0);
        },
        dubbingView : function(self){
            var dubbingObj = this;
            var opts = self.opts;
            $.isFunction(opts.dubbingPreview) && opts.dubbingPreview(dubbingObj,opts.from);
        },
        resetDubbingData : function(){
            var self = this;
            self._resetSearchDubbing();
            self.dubbingIds([]);
        },
        setSearchWord : function(searchWord){
            var self = this;
            self.searchOptions.searchWord = searchWord || "";
        },
        showAll : function(filterType){
            var self = this;
            switch (filterType) {
                case "LEVEL":
                    var levelSize = self.levelSize();
                    var defaultLevelSize = self.defaultLevelSize;
                    self.levelSize(levelSize === defaultLevelSize ? self.levelList.length : defaultLevelSize);
                    break;
                case "CHANNEL":
                    var channelSize = self.channelSize();
                    var defaultChannelSize = self.defaultChannelSize;
                    self.channelSize(channelSize === defaultChannelSize ? self.channelList().length : defaultChannelSize);
                    break;
                case "THEME":
                    var themeSize = self.themeSize();
                    var defaultThemeSize = self.defaultThemeSize;
                    self.themeSize(themeSize === defaultThemeSize ? self.themeList().length : defaultThemeSize);
                    break;
                case "ALBUM":
                    var albumSize = self.albumSize();
                    var defaultAlbumSize = self.defaultAlbumSize;
                    self.albumSize(albumSize === defaultAlbumSize ? self.albumList().length : defaultAlbumSize);
                    break;
                default:
                    break;
            }
        },
        selFilter : function(self,filterType){
            var item = this;
            var logObject = {
                module : "m_H1VyyebB",
                op     : "",
                s0     : ""
            };
            switch (filterType) {
                case "LEVEL":
                    self.defaultClazzLevel(item.key);
                    $17.extend(logObject,{
                        op     : "dubwithscore_allvideo_grades_click",
                        s0     : item.name
                    });
                    break;
                case "CHANNEL":
                    var channelId = item.channelId;
                    var channelIndex = self.channelIds.indexOf(channelId);
                    if(channelIndex === -1){
                        self.channelIds.push(channelId);
                    }else{
                        self.channelIds.splice(channelIndex,1);
                    }
                    $17.extend(logObject,{
                        op     : "dubwithscore_allvideo_types_click",
                        s0     : item.channelName
                    });
                    break;
                case "THEME":
                    var themeId = item.themeId;
                    var themeIndex = self.themeIds.indexOf(themeId);
                    if(themeIndex === -1){
                        self.themeIds.push(themeId);
                    }else{
                        self.themeIds.splice(themeIndex,1);
                    }
                    $17.extend(logObject,{
                        op : "dubwithscore_allvideo_topics_click",
                        s0 : item.themeName
                    });
                    break;
                case "ALBUM":
                    var albumId = item.albumId;
                    var albumIndex = self.albumIds.indexOf(albumId);
                    if(albumIndex === -1){
                        self.albumIds.push(albumId);
                    }else{
                        self.albumIds.splice(albumIndex,1);
                    }
                    $17.extend(logObject,{
                        op : "dubwithscore_allvideo_albums_click",
                        s0 : item.albumName
                    });
                    break;
                default:
                    break;
            }
            $17.voxLog(logObject);
            self.resetFilter();
        },
        noFilter : function(filterType){
            var self = this;
            switch (filterType) {
                case "CHANNEL":
                    self.channelIds([]);
                    break;
                case "THEME":
                    self.themeIds([]);
                    break;
                case "ALBUM":
                    self.albumIds([]);
                    break;
                default:
                    break;
            }
            this.resetFilter();
        },
        resetFilter : function(searchParam){
            var self = this;
            this.searchOptions.pageNum     = 1;
            this.searchOptions.clazzLevel  = self.defaultClazzLevel();
            this.searchOptions.channelIds    = self.channelIds().join(",");
            this.searchOptions.albumIds   = self.albumIds().join(",");
            this.searchOptions.themeIds   = self.themeIds().join(",");
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            this.searchDubbings();
        }
    };

    /**
     * 配音收藏
     * @param options
     * @constructor
     */
    function CollectDubbing(options){
         var self = this;
        var defaultOpts = {
            dubbingPreview  : null,
            addDubbing      : null,
            removeDubbing   : null,
            tabType         : "",
            from            : "COLLECT_DUBBING"  //打点使用，表示触发配音某个动作时（比如预览），来源于哪（我的收藏）
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.dubbingList    = ko.observableArray([]);
        self.dubbingIds = ko.observableArray([]);
        self.pagination = $17.pagination.initPages({
            pageClickCb : self.pageClickPost.bind(self)
        });
        self.searchOptions = {
            bookId      : null,
            unitId      : null,
            pageNum     : 1,
            pageSize    : 8
        };
    }
    CollectDubbing.prototype = {
        constructor : CollectDubbing,
        init : function(obj,searchParam,config){
            config = $.isPlainObject(config) ? config : {};
            var self = this;
            if($.isEmptyObject(obj) || !self instanceof CollectDubbing){
                return false;
            }
            self.loadCollectDubbingList(searchParam);
            //注意的地方,当深拷贝时，如果后一个对象是属性值是一个空数组，且前一个对象的相同属性值时数组时，
            // 不会覆盖前面相同属性的值 $.extend(true,{a:[1]},{a:[]}) ==> {a:[1]}
            self.opts = $.extend(true,self.opts,config);
            var dubbingIds = config.dubbingIds || [];
            self.dubbingIds(dubbingIds);
        },
        pageClickPost  : function(pageNo){
            this.loadCollectDubbingList({
                pageNum : pageNo
            });
        },
        loadCollectDubbingList : function(searchParam){
            var self = this;
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            var url = "/teacher/new/homework/dubbing/collection/record.vpage";
            $.get(url,self.searchOptions).done(function(data){
                if(data.success){
                    self.dubbingList(data.dubbingList);
                    self.pagination.setPage(data.pageNum,data.pageCount);
                }else{
                    data.errorCode !== "200" && $17.voxLog({
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
            var dubbingObj = this,opts = self.opts;
            var pId = dubbingObj.dubbingId;
            var pIndex = self.dubbingIds.indexOf(pId);
            var extendDubbingObj = $.extend(true,{},dubbingObj);
            if(pIndex === -1){
                //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
                self.dubbingIds.push(pId);
                $.isFunction(opts.addDubbing) && opts.addDubbing(extendDubbingObj,opts.from);
            }else{
                self.dubbingIds.splice(pIndex,1);
                $.isFunction(opts.removeDubbing) && opts.removeDubbing(extendDubbingObj,opts.from);
            }
        },
        dubbingView : function(self){
            var dubbingObj = this;
            var opts = self.opts;
            $.isFunction(opts.dubbingPreview) && opts.dubbingPreview(dubbingObj,opts.from);
        }
    };


    var Dubbing = function(){
        var self = this;
        self.includeAllModule = ko.observable(false);
        self.dubbingRecommend   = new DubbingRecommend({
            dubbingPreview  : self.dubbingPreview.bind(self),
            addDubbing      : self.addDubbing.bind(self),
            removeDubbing   : self.removeDubbing.bind(self)
        });
        self.allDubbing         = new AllDubbing({
            dubbingPreview  : self.dubbingPreview.bind(self),
            addDubbing      : self.addDubbing.bind(self),
            removeDubbing   : self.removeDubbing.bind(self)
        });
        self.collectDubbing    = new CollectDubbing({
            dubbingPreview  : self.dubbingPreview.bind(self),
            addDubbing      : self.addDubbing.bind(self),
            removeDubbing   : self.removeDubbing.bind(self)
        });

        self.searchWord         = ko.observable("");
        self.searchWord.subscribe(function(val){
            self.allDubbing.setSearchWord(val);
        });
        this.config_dubbing  = {};
        this.carts           = null;
        self.packageList        = ko.observableArray(packageList);
        self.focusIndex         = ko.observable(0);
        self.focusPackage       = ko.pureComputed(function(){
            return this.packageList()[this.focusIndex()];
        },self);

        self.dubbingDetailMap = {}; //缓存配音详情
    };
    Dubbing.prototype = {
        constructor : Dubbing,
        initialise:function(config){
            this.config_dubbing = config;
            this.carts = config.carts || null;

            config.tabType === "DUBBING" && this.packageList.splice(2,1);  //去掉配音收藏

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
            var config = this.config_dubbing;
            var _sectionIds = $.map(config.sections,function(item){
                return item.sectionId;
            }),paramData = {
                sections : _sectionIds.join(","),
                type     : config.tabType,
                unitId   : config.unitId,
                bookId   : config.bookId,
                subject  : constantObj.subject,
                objectiveConfigId : config.objectiveConfigId
            };
            $.get("/teacher/new/homework/objective/content.vpage", paramData).done(function(data){
                if(data.success){
                    var content = data.content || [];
                    var recommendDubbingList = [];

                    var includeAllModule = false;
                    $.each(content,function(){
                        if(this.module === "all"){
                            includeAllModule = true;
                            self.allDubbing.init(this);
                        }else{
                            recommendDubbingList.push(this);
                        }
                    });
                    self.includeAllModule(includeAllModule);
                    if(includeAllModule && self.focusPackage().id === "DUBBING_ALL"){
                        self._packageClick();
                    }
                    self.dubbingRecommend.init(recommendDubbingList,{
                        tabType     : config.tabType,
                        dubbingIds  : self.getDubbingIdsInCart()
                    });
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op     : "assignhomework_extend_dubwithscore_load"
                    });
                    //因为首次默认推荐配音选项卡，所以加此打个推荐配音选项的打点
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op     : "dubwithscore_recommend_click",
                        s0     : "DUBBING_RECOMMEND"
                    });

                }else{
                    self._resetDubbing();
                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
            }).fail(function(){
                self._resetDubbing();
            });
        },
        _resetDubbing  : function(){
            var self = this;
            var config = this.config_dubbing;
            var dubbingIds = self.getDubbingIdsInCart();
            self.allDubbing.resetDubbingData();
            self.dubbingRecommend.init([],{
                tabType     : config.tabType,
                dubbingIds  : dubbingIds
            });
        },
        getDubbingIdsInCart : function(){
            var config = this.config_dubbing;
            var dubbingIds = [];
            $.each(constantObj._homeworkContent.practices[config.tabType].apps,function(){
                dubbingIds.push(this.dubbingId);
            });
            return dubbingIds;
        },
        _packageClick : function(){
            var self = this;
            var config = self.config_dubbing;
            var packageObj = self.focusPackage();
            var viewModel = self;
            var selectDubbingIds = self.getDubbingIdsInCart();
            switch (packageObj.id){
                case "COLLECT_DUBBING":
                    viewModel = self.collectDubbing;
                    viewModel.init(packageObj,{
                        bookId      : config.bookId,
                        unitId      : config.unitId,
                        subject     : constantObj.subject
                    },{
                        tabType     : config.tabType,
                        dubbingIds  : selectDubbingIds
                    });
                    break;
                case "DUBBING_ALL":
                    viewModel = self.allDubbing;
                    viewModel.run({
                        bookId      : config.bookId,
                        unitId      : config.unitId,
                        subject     : constantObj.subject,
                        pageNum     : 1
                    },{
                        tabType     : config.tabType,
                        dubbingIds  : selectDubbingIds
                    });
                    break;
                case "DUBBING_RECOMMEND":
                    break;
                default:
                    break;
            }
        },
        packageClick   : function(packageIndex,self){
            self.focusIndex(packageIndex);
            self._packageClick();

            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "dubwithscore_recommend_click",
                s0     : self.focusPackage().id
            });
        },
        displayMode    : function(packageEntity, bindingContext){
            var self = bindingContext.$root;
            var prefixTemplate = "t:",pkId = self.focusPackage().id;
            var template = prefixTemplate + pkId;
            if(["DUBBING_RECOMMEND","DUBBING_ALL","COLLECT_DUBBING"].indexOf(pkId) === -1){
                    template = prefixTemplate + "UNKNOWN_DUBBING";
            }
            return template;
        },
        displayViewModel : function(){
            var self = this;
            var packageObj = self.focusPackage();
            var viewModel = self;
            switch (packageObj.id){
                case "COLLECT_DUBBING":
                    viewModel = self.collectDubbing;
                    break;
                case "DUBBING_ALL":
                    viewModel = self.allDubbing;
                    break;
                case "DUBBING_RECOMMEND":
                    viewModel = self.dubbingRecommend;
                    break;
                default:
                    break;
            }
            return viewModel;
        },
        searchClick : function(){
            var self = this;
            var dubbingAllIndex = null;
            var focusIndex = self.focusIndex();
            ko.utils.arrayForEach(self.packageList(),function(obj,i){
                if(obj.id === "DUBBING_ALL"){
                    dubbingAllIndex = i;
                }
            });
            if($17.isBlank(dubbingAllIndex)){
                return false;
            }
            if(focusIndex !== dubbingAllIndex){
                var searchWord = self.searchWord();
                if($17.isBlank(searchWord)){
                    return false;
                }
                self.packageClick.call(self.packageList()[dubbingAllIndex],dubbingAllIndex,self);
            }else{
                self.allDubbing.resetFilter();
            }
            $17.voxLog({
                module  : "m_H1VyyebB",
                op      : "dubwithscore_search_click"
            });
        },
        dubbingPreview : function(dubbingObj,from){
            var self = this;
            var config = this.config_dubbing;
            $17.homeworkv3.viewDubbingDetail({
                bookId : config.bookId,
                unitId : config.unitId,
                dubbingId : dubbingObj.dubbingId,
                subject   : constantObj.subject,
                homeworkType : config.tabType,
                collectDubbingCb : function(oldValue,newValue){
                    var btnText = oldValue ? "收藏" : "取消";
                    $17.voxLog({
                        module  : "m_H1VyyebB",
                        op      : "dubwithscore_detail_collect_click",
                        s0      : btnText,
                        s1      : dubbingObj.dubbingId
                    });
                },
                closeCb   : function(){
                    if(from === "COLLECT_DUBBING"){
                        var packageObj = self.focusPackage();
                        self.collectDubbing.init(packageObj,{
                            bookId      : config.bookId,
                            unitId      : config.unitId,
                            subject     : constantObj.subject
                        },{
                            tabType     : config.tabType,
                            dubbingIds  : self.getDubbingIdsInCart()
                        });
                    }
                }
            });

            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "dubwithscore_detail_load",
                s0     : from,
                s1     : dubbingObj.dubbingId
            });
        },
        dubbingView : function(self,from){
            self.dubbingPreview(this,from);
        },
        addDubbing : function(dubbingObj,moduleType){
            var self = this,config = self.config_dubbing,isRepeat=false;
            var dubbingId = dubbingObj.dubbingId;
            $.each(constantObj._reviewQuestions[config.tabType],function(){
                if(this.dubbingId === dubbingId){
                    $17.alert("该题目与已选题目重复~");
                    isRepeat = true;
                    return false;
                }
            });
            if(isRepeat) return false;

            constantObj._homeworkContent.practices[config.tabType].apps.push({
                dubbingId   : dubbingId,
                book        : dubbingObj.book,
                objectiveId : config.objectiveTabType
            });

            constantObj._moduleSeconds[config.tabType] += dubbingObj.seconds;

            var reviewObj = $.extend(true,{
                bookId      : config.bookId,
                unitId      : config.unitId
            },ko.mapping.toJS(dubbingObj));
            constantObj._reviewQuestions[config.tabType].push(reviewObj);

            self.reSetUFO();
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "dubwithscore_select_click",
                s1     : "选入",
                s0     : moduleType
            });
        },
        removeDubbing : function(dubbingObj,moduleType){
            var self = this,config = self.config_dubbing;
            var dubbingId = dubbingObj.dubbingId;
            $.each(constantObj._homeworkContent.practices[config.tabType].apps,function(i){
                if(this.dubbingId === dubbingId){
                    constantObj._homeworkContent.practices[config.tabType].apps.splice(i,1);
                    return false;
                }
            });
            $.each(constantObj._reviewQuestions[config.tabType],function(i){
                if(this.dubbingId === dubbingId){
                    constantObj._reviewQuestions[config.tabType].splice(i,1);
                    return false;
                }
            });

            constantObj._moduleSeconds[config.tabType] -= dubbingObj.seconds;

            self.reSetUFO();
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "dubwithscore_select_click",
                s1     : "移除",
                s0     : moduleType
            });
        },
        reSetUFO : function(){
            var self = this;
            var count = constantObj._homeworkContent.practices[this.config_dubbing.tabType].apps.length;
            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(this.config_dubbing.tabType,count);
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getDubbing : function(){
            return new Dubbing();
        },
        getDubbing_with_score:function(){
            return new Dubbing();
        }
    });
}($17,ko);

