/**
 * Created by dell on 2018/1/11.
 * 绘本阅读另一种类型展示
 */
;(function($17,ko) {
    "use strict";
    var module             = constantObj.logModule || "m_aHrND8yNXX";  //打点module
    var subject            = constantObj.subject;

    function AllReading(options){
        var self = this;
        var defaultOpts = {
            readingPreview   : null,
            tabType         : ""
        };
        self.opts = $.extend(true,{},defaultOpts,options);
        self.previewLinkFlag = ko.observable(self.opts.previewLinkFlag || false);
        self.clazzLevelList = ko.observableArray([]);
        self.levelIds       = ko.observableArray([]);
        self.levelSelectAll = ko.pureComputed(function(){
            // 不限：数组为零的情况
            return self.levelIds().length === 0;
        },self);
        self.levelIds.subscribe(function(levelArr){
            self.searchReadings({
                clazzLevel : levelArr.join(","),
                pageNum    : 1
            });
        });

        self.searchOptions = {
            bookId      : null,
            unitId      : null,
            clazzLevel  : "",
            pageNum     : 1,
            pageSize    : 8,
            subject     : null
        };

        self.readingList    = ko.observableArray([]);
        self.pagination = $17.pagination.initPages({
            pageClickCb : self.pageClickPost.bind(self)
        });
    }
    AllReading.prototype = {
        constructor : AllReading,
        init : function(obj,searchParam,config){
            var self = this;
            if($.isEmptyObject(obj)){
                return false;
            }
            if(!self instanceof  AllReading){
                //类型错误
                return false;
            }
            self.clazzLevelList(obj.clazzLevelList || []);
            self.searchReadings(searchParam);
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
                    op      : "resource_levelreadings_grades_click",
                    s0      : levelObj.levelName
                });
            }
        },
        unlimitClick : function(type){
            var self = this;
            var logOp;
            switch (type){
                case "clazzLevels":
                    self.levelIds([]);
                    logOp = "resource_levelreadings_grades_click";
                    break;
                default:
                    break;
            }
            $17.voxLog({
                module  : module,
                op      : logOp,
                s0      : "不限"
            });
        },
        showAll : function(element){
            var $element = $(element);
            var $item = $element.parents(".theme-box");
            if($item.hasClass("showAll")){
                $item.removeClass("showAll");
            }else{
                $item.addClass("showAll");
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
            var url = "/teacher/clazzresource/picturebook/videolist.vpage";
            $.get(url,self.searchOptions).done(function(data){
                if(data.success){
                    self.readingList(data.pictureBookVideo);
                    self.pagination.setPage(data.pageNum,data.pageCount);
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
            $.isFunction(opts.readingPreview) && opts.readingPreview(reading,"all");
        },
        previewVideo : function(self){
            var readingObj = this;
            $.prompt(template("t:video_js_preview_popup",{
                videoList : [readingObj.videoUrl],
                poster : readingObj.pictureBookImgUrl
            }), {
                title   : "预 览",
                buttons : {},
                position: { width: 700 },
                loaded : function(){
                    console.info("loaded");
                    $("#my-video").contextmenu(function(){
                        return false;
                    });
                },
                close   : function(){}
            });
            $17.voxLog({
                module : module,
                op     : "resource_lerevideo_click",
                s0     : readingObj.pictureBookId
            })
        }
    };

    function ReadingPackage(id,name){
        this.id = id;
        this.name = name;
    }
    var packageList = [new ReadingPackage("PK_ALL","全部绘本")];

    function LevelReadings(){
        var self = this;
        self.allReading         = new AllReading({
            readingPreview  : self.readingPreivew.bind(self),
            previewLinkFlag : $uper.userId === "14711851"
        });
        self.displayReadings    = ko.observableArray([]);
        self.config_reading     = {};
        self.subject            = constantObj.subject;
        self.packageList        = ko.observableArray(packageList);
        self.focusIndex         = ko.observable(0);
        self.focusPackage       = ko.pureComputed(function(){
            return this.packageList()[this.focusIndex()];
        },self);
    }

    LevelReadings.prototype = {
        constructor : LevelReadings,
        initialise:function(config){
            this.config_reading = config;
        },
        run: function(){
            var self = this;
            var config = this.config_reading;
            self.allReading.init({
                "clazzLevelList": [
                    {
                        "levelId": 1,
                        "levelName": "一年级"
                    },
                    {
                        "levelId": 2,
                        "levelName": "二年级"
                    },
                    {
                        "levelId": 3,
                        "levelName": "三年级"
                    },
                    {
                        "levelId": 4,
                        "levelName": "四年级"
                    },
                    {
                        "levelId": "5",
                        "levelName": "五年级"
                    },
                    {
                        "levelId": "6",
                        "levelName": "六年级"
                    }
                ]
            },{
                bookId      : config.bookId,
                unitId      : config.unitId,
                subject     : self.subject,
                pageNum     : 1
            },{
                tabType         : config.tabType,
                pictureBookIds  : []  //选入的绘本ID
            });

        },
        packageClick   : function(packageIndex,self){
            self.focusIndex(packageIndex);
        },
        displayMode    : function(self, bindingContext){
            var template = "t:",pkId = self.focusPackage().id;
            switch (pkId){
                case "PK_ALL":
                    template += pkId;
                    break;
                default:
                    template += "UNKNOWN_READINGS";
            }
            return template;
        },
        fetchPreviewUrl : function(pictureBookIds){
            var domain = "/";
            if(constantObj.env === "test"){
                domain = "//www.test.17zuoye.net/";
            }else{
                domain = location.protocol + "//" + location.host;
            }
            return domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param({
                pictureBookIds : pictureBookIds,
                from : "preview"
            });
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
            var self = this;
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
            var gameUrl =  self.fetchPreviewUrl(newOptions.readingParam.pictureBookIds);
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

    $17.clazzresource = $17.clazzresource || {};
    $17.extend($17.clazzresource, {
        getLevel_readings: function(){
            return new LevelReadings();
        }
    });
}($17,ko));


