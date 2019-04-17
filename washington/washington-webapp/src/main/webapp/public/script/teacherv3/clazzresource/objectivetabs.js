/*
 * 教学子目标TAB列表
 *
 */
(function($17,ko) {
    "use strict";
    var ObjectiveTabs = function(){
        var self            = this;
        self.iconPrefixUrl = "";
        self.sectionIds     = [];
        self.tabs           = ko.observableArray([]);
        self.startPage      = ko.observable(1);
        self.displayCount   = 7;
        self.totalPage 		= ko.pureComputed(function(){
            return Math.ceil(self.tabs().length / self.displayCount);
        });
        self.currentTabs    = ko.pureComputed(function(){
            var _tabs = self.tabs();
            var _startPage = self.startPage();
            var _newTabs = [];
            if(_tabs.length <= self.displayCount){
                _newTabs = _tabs;
            }else{
                _newTabs = _tabs.slice((_startPage - 1) * self.displayCount,_startPage * self.displayCount);
            }
            return _newTabs;
        });
        self.leftEnabled    = ko.pureComputed(function(){
            //左箭头是否可用
            return self.startPage() > 1;
        });
        self.rightEnabled  = ko.pureComputed(function(){
            //右箭头是否可用
            return self.startPage() < self.totalPage();
        });

        self.focusTabType   = ko.observable("");
        self.focusTabName   = ko.observable("");
        self.focusTypeList  = [];
    };
    ObjectiveTabs.prototype = {
        constructor     : ObjectiveTabs,
        param           : {},
        extendTabClick  : function(obj){},
        tabClick        : function(self){
            var that = this;
            if(self.focusTabType() == that.type()){
                return false;
            }

            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "ah_subtarget_click",
                s0     : constantObj.subject,
                s1     : that.name(),
                s2     : that.type()
            });

            self.focusTabType(that.type());
            self.focusTabName(that.name());
            self.focusTypeList = (that.typeList && that.typeList() ? ko.mapping.toJS(that.typeList()) : []);
            self.refresh();
        },
        refresh       : function(){
            var self = this;
            self.extendTabClick($.extend(true,{
                objectiveTabType     : self.focusTabType(),
                objectiveTabTypeName : self.focusTabName(),
                typeList    : self.focusTypeList
            },self.param));
        },
        arrowClick      : function(directionOfArrow){
            var self = this;
            var _startPage = self.startPage();
            if(directionOfArrow === "arrowLeft" && self.leftEnabled()){
                self.startPage(_startPage - 1);
            }else if(directionOfArrow === "arrowRight" && self.rightEnabled()){
                self.startPage(_startPage + 1);
            }
        },
        fillObjectiveList : function(objectiveList){
            var self = this;
            var objectiveList = objectiveList || [];
            //默认应试图标
            var _tabIcon = "tab-icon-exam.png";
            var _tabIconUrl = !!self.iconPrefixUrl ? self.iconPrefixUrl +  _tabIcon: _tabIcon;
            var afterConvertObjectiveList = [];
            if(objectiveList.length > 0){
                for(var z = 0,zLen = objectiveList.length; z < zLen; z++){
                    var icon = objectiveList[z].objectiveIcon ? objectiveList[z].objectiveIcon : _tabIconUrl;
                    afterConvertObjectiveList.push({
                        type : objectiveList[z].objectiveId,
                        name : objectiveList[z].objectiveName,
                        icon : "url('" + icon + "')",
                        typeList : objectiveList[z].typeList
                    });
                }
            }

            self.tabs(ko.mapping.fromJS(afterConvertObjectiveList)());
            //恢复初始，不然当前位置会超过总tabs数
            self.startPage(1);
            if(afterConvertObjectiveList.length > 0){
                var focusTabIndex = 0;
                //这里这么找下标，是因为afterConvertObjectiveList是固定的，不是从后台获取的动态数据
                var focusTabType = self.focusTabType();
                for(var j = 0,jLen = afterConvertObjectiveList.length; j < jLen; j++){
                    if(afterConvertObjectiveList[j].type === focusTabType){
                        focusTabIndex = j;
                        break;
                    }
                }
                self.focusTabType(afterConvertObjectiveList[focusTabIndex].type);
                self.focusTabName(afterConvertObjectiveList[focusTabIndex].name);
                self.focusTypeList = afterConvertObjectiveList[focusTabIndex].typeList;
                self.refresh();
            }else{
                $("#J_HomeworkWay").hide();
            }

        },
        run             : function(){
            var self = this;
            if(!(self.param && self.param.bookId && self.param.unitId)){
                self.tabs([]);
                self.startPage(1);
                return false;
            }
            self.fillObjectiveList([{
                objectiveId : "LEVEL_READINGS",
                objectiveName : "绘本阅读",
                objectiveIcon : self.iconPrefixUrl + "newtab-icon-level_reading.png"
            },{
                objectiveId : "BASIC_APP",
                objectiveName : "基础练习",
                objectiveIcon : self.iconPrefixUrl + "newtab-icon-basic_app.png"
            }]);
        },
        initialise      : function(option){
            var self           = this;
            option = option || {};
            self.param = option;
            var _sectionIds = [];
            $.each(option.sections,function(i,section){
                _sectionIds.push(section.sectionId);
            });
            self.sectionIds    = _sectionIds;
            self.iconPrefixUrl = constantObj.tabIconPrefixUrl;
        }
    };

    $17.clazzresource = $17.clazzresource || {};
    $17.extend($17.clazzresource, {
        getObjectiveTabs: function(){
            return new ObjectiveTabs();
        }
    });
}($17,ko));