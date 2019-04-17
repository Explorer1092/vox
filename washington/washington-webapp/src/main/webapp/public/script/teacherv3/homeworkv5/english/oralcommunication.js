!function($17,ko) {
    "use strict";
    var isInitUFO = true;

    function PackageEntity(id,name){
        this.id = id;
        this.name = name;
    }

    var packageList = [new PackageEntity("RECOMMEND_PK","推荐情景包"),
        new PackageEntity("ALL_CONTENT","全部情景包")];

    /**
     * 推荐情景包
     * @param options
     * @constructor
     */
    function RecommendPk(options){
        var self = this;
        var defaultOpts = {
            previewItem  : null,
            addItem      : null,
            removeItem   : null,
            tabType         : "",
            from            : "RECOMMEND_PK" //来源
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.recommendModuleList = ko.observableArray([]);
        self.oralCommunicationIds = ko.observableArray([]); //选择的oralCommunicationIds
    }

    RecommendPk.prototype = {
        constructor : RecommendPk,
        init : function(recommendModuleList,config){
            config = $.isPlainObject(config) ? config : {};
            var self = this;
            if(!$.isArray(recommendModuleList) || recommendModuleList.length === 0){
                return false;
            }
            if(!this instanceof RecommendPk){
                //警告类型不对
                return false;
            }
            self.opts = $.extend(true,self.opts,config);
            for(var m = 0,mLen = recommendModuleList.length; m < mLen; m++){
                recommendModuleList[m]["beginPos"] = ko.observable(0);  //起始位置
                recommendModuleList[m]["moveCount"] = 4;
                recommendModuleList[m]["totalCount"] = recommendModuleList[m].oralCommunicationList.length;
            }
            self.recommendModuleList(recommendModuleList);
            self.oralCommunicationIds(config.oralCommunicationIds || []);

        },
        addOrCancel : function(self,element){
            var oralCommunicationObj = this,opts = self.opts;
            var pId = oralCommunicationObj.oralCommunicationId;
            var pIndex = self.oralCommunicationIds.indexOf(pId);
            var extendOralCommunicationObj = $.extend(true,{},oralCommunicationObj);
            if(pIndex === -1){
                //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
                self.oralCommunicationIds.push(pId);
                $.isFunction(opts.addItem) && opts.addItem(extendOralCommunicationObj,opts.from);
            }else{
                self.oralCommunicationIds.splice(pIndex,1);
                $.isFunction(opts.removeItem) && opts.removeItem(extendOralCommunicationObj,opts.from);
            }
        },
        previewItem : function(self,module){
            var item = this;
            var opts = self.opts;
            $.isFunction(opts.previewItem) && opts.previewItem(item,opts.from);
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
     * 全部情景包
     * @param options
     * @constructor
     */
    function AllContent(options){
        var self = this;
        var defaultOpts = {
            previewItem     : null,
            addItem         : null,
            removeItem      : null,
            tabType         : "",
            from            : "ALL_CONTENT"
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.itemList    = ko.observableArray([]);
        self.itemIds = ko.observableArray([]);
        this.searchOptions = {
            bookId      : null,
            unitId      : null,
            clazzLevel  : "0",
            type        : null,
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
        self.clazzLevelList = ko.observable([]);
        self.clazzLevelSize = ko.observable(self.defaultLevelSize); //展示几个开始显示折叠
        self.defaultClazzLevel = ko.observable(0);

        //类型
        self.defaultOralTypeSize = 5;
        self.oralTypeSize = ko.observable(self.defaultOralTypeSize); //展示几个开始显示折叠
        self.oralTypeList = ko.observableArray([]);
        self.oralTypeIds = ko.observableArray([]);
    }
    AllContent.prototype = {
        constructor : AllContent,
        init : function(obj){
            var self = this;
            if($.isEmptyObject(obj)){
                return false;
            }
            if(!self instanceof  AllContent){
                //类型错误
                return false;
            }
            self.description(obj.description || "");
            self.defaultClazzLevel(obj.defaultClazzLevel || 0);
            self.oralTypeList(obj.oralTypeList || []);
            self.clazzLevelList(obj.clazzLevelList || []);
        },
        run : function(searchParam,config){
            var self = this;
            self.opts = $.extend(true,self.opts,config);
            self.resetFilter(searchParam);
            self.itemIds(config.itemIds || []);
        },
        addOrCancel : function(self,element){
            var itemObj = this,opts = self.opts;
            var pId = itemObj.oralCommunicationId;
            var pIndex = self.itemIds.indexOf(pId);
            var extendItem = $.extend(true,{},itemObj);
            if(pIndex === -1){
                //此动画一定在放在页面重新渲染之前，不然页面就没有选入元素了
                $(element).closest(".examTopicBox").fly({
                    target: ".J_UFOInfo p[type='" + opts.tabType + "']",
                    border: "5px #39f solid",
                    time  : 600
                });
                self.itemIds.push(pId);
                $.isFunction(opts.addItem) && opts.addItem(extendItem,opts.from);
            }else{
                self.itemIds.splice(pIndex,1);
                $.isFunction(opts.removeItem) && opts.removeItem(extendItem,opts.from);
            }
        },
        searchItems : function(searchParam){
            var self = this;
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            $.post("/teacher/new/homework/oralcommunication/search.vpage",self.searchOptions).done(function(data){
                if(data.success){
                    self.itemList(data.oralCommunicationList);
                    self.pagination.setPage(data.pageNum,data.pageCount);
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op     : "dubwithscore_result_load",
                        s0     : JSON.stringify(self.searchOptions),
                        s1     : data.totalSize
                    });
                }else{
                    self._resetSearchItems();
                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/oralcommunication/search.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(self.searchOptions),
                        s3     : $uper.env
                    });
                }
            }).fail(function(){
                self._resetSearchItems();
            });
        },
        pageClickPost  : function(pageNo){
            this.resetFilter({
                pageNum : pageNo
            });
        },
        _resetSearchItems : function(){
            var self = this;
            self.itemList([]);
            self.pagination.setPage(1,0);
        },
        previewItem : function(self){
            var item = this;
            var opts = self.opts;
            $.isFunction(opts.previewItem) && opts.previewItem(item, opts.from);
        },
        resetItemData : function(){
            var self = this;
            self._resetSearchItems();
            self.itemIds([]);
        },
        setSearchWord : function(searchWord){
            var self = this;
            self.searchOptions.searchWord = searchWord || "";
        },
        showAll : function(filterType){
            var self = this;
            switch (filterType) {
                case "LEVEL":
                    var clazzLevelSize = self.clazzLevelSize();
                    var defaultLevelSize = self.defaultLevelSize;
                    self.clazzLevelSize(clazzLevelSize === defaultLevelSize ? self.clazzLevelList.length : defaultLevelSize);
                    break;
                case "ORAL_TYPE":
                    var oralTypeSize = self.oralTypeSize();
                    var defaultOralTypeSize = self.defaultOralTypeSize;
                    self.oralTypeSize(oralTypeSize === defaultOralTypeSize ? self.oralTypeList().length : defaultOralTypeSize);
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
                    self.defaultClazzLevel(item.levelId);
                    $17.extend(logObject,{
                        op     : "OC_allcontents_grades_click",
                        s0     : self.opts.clazzGroupIdsStr,
                        s1     : item.levelName
                    });
                    break;
                case "ORAL_TYPE":
                    var oralTypeId = item.typeId;
                    var oralTypeIndex = self.oralTypeIds.indexOf(oralTypeId);
                    if(oralTypeIndex === -1){
                        self.oralTypeIds.push(oralTypeId);
                    }else{
                        self.oralTypeIds.splice(oralTypeIndex,1);
                    }
                    $17.extend(logObject,{
                        op     : "OC_allcontents_types_click",
                        s0     : self.opts.clazzGroupIdsStr,
                        s1     : item.typeName
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
                case "LEVEL":
                    self.defaultClazzLevel(0);
                    break;
                case "ORAL_TYPE":
                    self.oralTypeIds([]);
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
            this.searchOptions.type    = self.oralTypeIds().join(",");
            self.searchOptions = $.extend(true,self.searchOptions,searchParam);
            this.searchItems();
        }
    };


    function OralCommunication(){
        var self = this;
        self.includeAllModule = ko.observable(false);
        self.recommendPk   = new RecommendPk({
            previewItem  : self.previewItem.bind(self),
            addItem      : self.addItem.bind(self),
            removeItem   : self.removeItem.bind(self)
        });
        self.allContent         = new AllContent({
            previewItem  : self.previewItem.bind(self),
            addItem      : self.addItem.bind(self),
            removeItem   : self.removeItem.bind(self)
        });

        self.searchWord         = ko.observable("");
        self.searchWord.subscribe(function(val){
            self.allContent.setSearchWord(val);
        });
        this.config_option  = {};
        this.carts           = null;
        self.packageList        = ko.observableArray(packageList);
        self.focusIndex         = ko.observable(0);
        self.focusPackage       = ko.pureComputed(function(){
            return this.packageList()[this.focusIndex()];
        },self);

        self.clazzGroupIdsStr = "";
    }

    OralCommunication.prototype = {
        constructor : OralCommunication,
        initialise:function(config){
            this.config_option = config;
            this.carts = config.carts || null;
            if(isInitUFO){
                isInitUFO = false;
                var str = ["<span class=\"name\">" + config.tabTypeName +"</span>" +
                "<span class=\"count\" data-count=\"0\">0</span>" +
                "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join("");
                $(".J_UFOInfo p[type='" + config.tabType + "']").html(str);
            }
        },
        run: function(extOption){
            var self = this;
            extOption = extOption || {};
            self.clazzGroupIdsStr = extOption.clazzGroupIdsStr || "";
            var config = this.config_option;
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
                    var recommendList = [];

                    var includeAllModule = false;
                    $.each(content,function(){
                        if(this.module === "all"){
                            includeAllModule = true;
                            self.allContent.init(this);
                        }else{
                            recommendList.push(this);
                        }
                    });
                    self.includeAllModule(includeAllModule);
                    if(includeAllModule && self.focusPackage().id === "ALL_CONTENT"){
                        self._packageClick();
                    }
                    self.recommendPk.init(recommendList,{
                        tabType     : config.tabType,
                        oralCommunicationIds  : self.getOralCommunicationIdsInCart(),
                        clazzGroupIdsStr : self.clazzGroupIdsStr
                    });
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op     : "assignhomework_extend_OCpage_load",
                        s0     : self.clazzGroupIdsStr
                    });
                    //因为首次默认推荐配音选项卡，所以加此打个推荐配音选项的打点
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op     : "OC_recommend_click",
                        s0     : self.clazzGroupIdsStr,
                        s1     : "DUBBING_RECOMMEND"
                    });

                }else{
                    self._resetOralCommunication();
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
                self._resetOralCommunication();
            });
        },
        _resetOralCommunication  : function(){
            var self = this;
            var config = this.config_option;
            var oralCommunicationIds = self.getOralCommunicationIdsInCart();
            self.allContent.resetItemData();
            self.recommendPk.init([],{
                tabType     : config.tabType,
                oralCommunicationIds  : oralCommunicationIds
            });
        },
        getOralCommunicationIdsInCart : function(){
            var config = this.config_option;
            var itemIds = [];
            $.each(constantObj._homeworkContent.practices[config.tabType].apps,function(){
                itemIds.push(this.oralCommunicationId);
            });
            return itemIds;
        },
        _packageClick : function(){
            var self = this;
            var config = self.config_option;
            var packageObj = self.focusPackage();
            var viewModel = self;
            var selectItemIds = self.getOralCommunicationIdsInCart();
            switch (packageObj.id){
                case "ALL_CONTENT":
                    viewModel = self.allContent;
                    viewModel.run({
                        bookId      : config.bookId,
                        unitId      : config.unitId,
                        subject     : constantObj.subject,
                        pageNum     : 1
                    },{
                        tabType     : config.tabType,
                        itemIds     : selectItemIds,
                        clazzGroupIdsStr : self.clazzGroupIdsStr
                    });
                    break;
                case "RECOMMEND_PK":
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
                op     : "OC_recommend_click",
                s0     : self.clazzGroupIdsStr,
                s1     : self.focusPackage().id
            });
        },
        displayMode    : function(packageEntity, bindingContext){
            var self = bindingContext.$root;
            var prefixTemplate = "t:",pkId = self.focusPackage().id;
            var template = prefixTemplate + pkId;
            if(["RECOMMEND_PK","ALL_CONTENT"].indexOf(pkId) === -1){
                template = prefixTemplate + "UNKNOWN_TEMPLATE";
            }
            return template;
        },
        displayViewModel : function(){
            var self = this;
            var packageObj = self.focusPackage();
            var viewModel = self;
            switch (packageObj.id){
                case "ALL_CONTENT":
                    viewModel = self.allContent;
                    break;
                case "RECOMMEND_PK":
                    viewModel = self.recommendPk;
                    break;
                default:
                    break;
            }
            return viewModel;
        },
        searchClick : function(){
            var self = this;
            var allContentIndex = null;  //全部题包的下标
            var focusIndex = self.focusIndex();
            ko.utils.arrayForEach(self.packageList(),function(obj,i){
                if(obj.id === "ALL_CONTENT"){
                    allContentIndex = i;
                }
            });
            if($17.isBlank(allContentIndex)){
                return false;
            }
            if(focusIndex !== allContentIndex){
                var searchWord = self.searchWord();
                if($17.isBlank(searchWord)){
                    return false;
                }
                self.packageClick.call(self.packageList()[allContentIndex],allContentIndex,self);
            }else{
                self.allContent.resetFilter();
            }
            $17.voxLog({
                module  : "m_H1VyyebB",
                op      : "OC_search_click",
                s0      : self.clazzGroupIdsStr
            });
        },
        previewItem : function(item,from){
            var self = this;
            var config = this.config_option;
            $17.homeworkv3.viewOralCommunicationDetail({
                bookId : config.bookId,
                unitId : config.unitId,
                oralCommunicationId : item.oralCommunicationId,
                subject   : constantObj.subject,
                homeworkType : config.tabType
            });

            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "OC_detailpage_load",
                s0     : self.clazzGroupIdsStr,
                s1     : item.oralCommunicationId,
                s2     : from
            });
        },
        addItem : function(item,moduleType){
            var self = this,config = self.config_option,isRepeat=false;
            var oralCommunicationId = item.oralCommunicationId;
            $.each(constantObj._reviewQuestions[config.tabType],function(){
                if(this.oralCommunicationId === oralCommunicationId){
                    $17.alert("该题目与已选题目重复~");
                    isRepeat = true;
                    return false;
                }
            });
            if(isRepeat) return false;

            constantObj._homeworkContent.practices[config.tabType].apps.push({
                oralCommunicationId   : oralCommunicationId,
                book        : item.book,
                objectiveId : config.objectiveTabType
            });

            constantObj._moduleSeconds[config.tabType] += item.seconds;

            var reviewObj = $.extend(true,{
                bookId      : config.bookId,
                unitId      : config.unitId
            },ko.mapping.toJS(item));
            constantObj._reviewQuestions[config.tabType].push(reviewObj);

            self.reSetUFO();
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "OC_select_click",
                s0     : self.clazzGroupIdsStr,
                s1     : oralCommunicationId,
                s2     : moduleType,
                s3     : "选入"
            });
        },
        removeItem : function(item,moduleType){
            var self = this,config = self.config_option;
            var itemId = item.oralCommunicationId;
            $.each(constantObj._homeworkContent.practices[config.tabType].apps,function(i){
                if(this.oralCommunicationId === itemId){
                    constantObj._homeworkContent.practices[config.tabType].apps.splice(i,1);
                    return false;
                }
            });
            $.each(constantObj._reviewQuestions[config.tabType],function(i){
                if(this.oralCommunicationId === itemId){
                    constantObj._reviewQuestions[config.tabType].splice(i,1);
                    return false;
                }
            });

            constantObj._moduleSeconds[config.tabType] -= item.seconds;

            self.reSetUFO();
            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "OC_select_click",
                s0     : self.clazzGroupIdsStr,
                s1     : itemId,
                s2     : moduleType,
                s3     : "移除"
            });
        },
        reSetUFO : function(){
            var self = this;
            var count = constantObj._homeworkContent.practices[this.config_option.tabType].apps.length;
            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(this.config_option.tabType,count);
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getOral_communication : function(){
            return new OralCommunication();
        }
    });
}($17,ko);

