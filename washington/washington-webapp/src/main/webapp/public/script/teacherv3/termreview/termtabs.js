(function (window,$17,constantObj,undefined) {
    "use strict";

    function TermTabs(obj, resolveCb, rejectCb){
        var self               = this;
        self.tabs              = ko.observableArray([]);
        self.startPosition     = ko.observable(0);
        self.focusTabType      = ko.observable("");
        self.focusHomeworkType = ko.observable("");
        self.focusTabName      = ko.observable("");
        self.displayCount      = 7;
        self.currentTabs       = ko.pureComputed(function(){
            var _tabs = self.tabs();
            var _startPos = self.startPosition();
            var _newTabs = [];
            var displayCount = self.displayCount;
            if(_tabs.length <= displayCount){
                _newTabs = _tabs;
            }else{
                if(_tabs.length - _startPos >= (displayCount + 1)){
                    _newTabs = _tabs.slice(_startPos,_startPos + displayCount);
                }else{
                    _newTabs = _tabs.slice(_startPos);
                }
            }
            return _newTabs;
        });
        self.leftEnabled    = ko.pureComputed(function(){
            //左箭头是否可用
            return self.startPosition() > 0;
        });
        self.rightEnabled   = ko.pureComputed(function(){
            //右箭头是否可用
            return (self.startPosition() + self.displayCount) < self.tabs().length;
        });
        self.tabNewIconWhiteList = []; //作业类型旁边的NEW图标白名单
        self.resolveCb      = resolveCb || null;
        self.rejectCb       = rejectCb || null;
        self.info           = ko.observable("");
    }
    TermTabs.prototype = {
        constructor         : TermTabs,
        extendTabClick      : function(obj){},
        addNewIcon          : function(tabType,self){
            return self.tabNewIconWhiteList.indexOf(tabType) != -1;
        },
        tabClick            : function(self){
            var that = this;
            if(self.focusTabType() == that.type()){
                return false;
            }

            self.focusTabType(that.type());
            self.focusHomeworkType(that.objectiveConfigType());
            self.focusTabName(that.typeName());
            self.refresh();
        },
        refresh       : function(){
            var self = this,
                resolveCb = self.resolveCb;
            typeof resolveCb === "function" && resolveCb({
                success      : true,
                type         : self.focusTabType(),
                typeName     : self.focusTabName(),
                homeworkType : self.focusHomeworkType()
            });
        },
        arrowClick      : function(directionOfArrow){
            var self = this;
            var _startPos = self.startPosition();
            if(directionOfArrow == "arrowLeft" && self.leftEnabled()){
                self.startPosition(_startPos - 1);
            }else if(directionOfArrow == "arrowRigth" && self.rightEnabled()){
                self.startPosition(_startPos + 1);
            }
        },
        run : function(mapMessage){
            var self = this;
            if(!mapMessage || !mapMessage.success){
                self.tabs([]);
                self.startPosition(0);
                self.info(mapMessage.info || "当前单元暂无作业内容，请切换其他单元查看");
                return false;
            }
            $.get("/teacher/termreview/typelist.vpage",{
                bookId   : mapMessage.bookId,
                clazzGroupIds : mapMessage.clazzGroupIds
            },function(data){
                var _newContentTypeList = [];
                if(data.success){
                    if(data.contentTypes && data.contentTypes.length > 0){
                        var _contentTypeList = data.contentTypes || [];
                        if(_contentTypeList.length > 0){
                            for(var z = 0,zLen = _contentTypeList.length; z < zLen; z++){

                                _contentTypeList[z].icon = "url('" + _contentTypeList[z].iconUrl + "')";
                                _newContentTypeList.push(_contentTypeList[z]);
                            }
                        }
                    }else{

                        typeof self.rejectCb === "function" && self.rejectCb();
                        self.info("该教材暂无复习内容，请切换其他教材再试～");
                    }
                }else{

                    var info = data.info || "配套复习内容获取失败，请稍后再试～";
                    typeof self.rejectCb === "function" && self.rejectCb();
                    self.info(mapMessage.clazzGroupIds ? info : "请选择需要布置的班级~");
                }

                self.tabs(ko.mapping.fromJS(_newContentTypeList)());
                //恢复初始，不然当前位置会超过总tabs数
                self.startPosition(0);
                if(_newContentTypeList.length > 0){
                    self.focusTabType(_newContentTypeList[0].type);
                    self.focusTabName(_newContentTypeList[0].typeName);
                    self.focusHomeworkType(_newContentTypeList[0].objectiveConfigType);
                    self.refresh();
                }
            });
        },
        getFocusType : function(){
            return this.focusTabType();
        }
    };
    
    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getTermTabs: function(obj,resolveCb,rejectCb){
            return new TermTabs(obj,resolveCb,rejectCb);
        }
    });
}(window,$17,constantObj));