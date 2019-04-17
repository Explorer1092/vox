(function($17,ko) {
    "use strict";
    var Levels = function(){
        var self          = this;
        self.showLevel    = ko.observable(0);
        self.showClazzList= ko.observableArray([]);
        self.isMoreLevel  = ko.observable(false);
        self.batchclazzs  = [[],[],[],[],[],[]];
        self.isAllChecked = ko.pureComputed(function(){
            return self.showClazzList().length == self.checkedClazzGroupIds().length;
        });
        self.hasStudents = ko.observable(true);
        self.clazzClickCb = null;
    };

    Levels.prototype = {
        constructor             : Levels,
        chooseOrCancelAll       : function(){
            var self = this,
                _isAllChecked = self.isAllChecked(),
                _clazzList = self.showClazzList();
            for(var k = 0; k < _clazzList.length; k++){
                self.showClazzList()[k].checked(!_isAllChecked);
            }
            typeof self.clazzClickCb === "function" && self.clazzClickCb();
        },
        singleClazzAddOrCancel  : function(self,index){
            var checked = self.showClazzList()[index].checked();
            self.showClazzList()[index].checked(!checked);

            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_selectClass_click",
                s0 : constantObj.subject
            });
            typeof self.clazzClickCb === "function" && self.clazzClickCb();
        },
        checkedClazzGroupIds    : function(){
            var self = this;
            var checkedClazzGroupIds = [];
            var _clazzList = self.showClazzList();
            for(var z = 0; z < _clazzList.length; z++){
                if(_clazzList[z].checked()){
                    var clazzGroupId = _clazzList[z].classId() + "_" + _clazzList[z].groupId();
                    checkedClazzGroupIds.push(clazzGroupId);
                }
            }
            return checkedClazzGroupIds;
        },
        checkedClazzNames    : function(){
            var self = this;
            var checkedClazzNameList = [];
            var _level = self.showLevel();
            var _clazzList = self.showClazzList();
            for(var z = 0; z < _clazzList.length; z++){
                if(_clazzList[z].checked()){
                    var clazzName = _level + "年级" + _clazzList[z].className();
                    checkedClazzNameList.push(clazzName);
                }
            }
            return checkedClazzNameList;
        },
        getCheckedGroups  : function(){
            var self = this,
                newCheckedGroups = [],
                _level = self.showLevel(),
                _clazzList = self.showClazzList();
            for(var z = 0; z < _clazzList.length; z++){
                if(_clazzList[z].checked()){
                    var clazzGroupId = _clazzList[z].groupId(),
                        clazzName = _level + "年级" + _clazzList[z].className();
                    newCheckedGroups.push({
                        groupId : clazzGroupId,
                        groupName : clazzName,
                        clazzName : clazzName
                    });
                }
            }
            return newCheckedGroups;
        },
        levelClick              : function(self,level){
            level  = (+level) || 0;
            if(level <= 0 || level == self.showLevel()){
                return false;
            }
            var levelfn = function(){
                var _clazzList = [];
                var _clazzs = self.batchclazzs[level - 1];
                for(var i = 0, iLen = _clazzs.length; i < iLen; i++){
                    if(_clazzs[i].canBeAssigned){
                        _clazzs[i]["checked"] = true;
                        _clazzList.push(_clazzs[i]);
                    }
                }
                self.showClazzList(ko.mapping.fromJS(_clazzList)());
                self.showLevel(level);
                self.extendLevelClick({level : level, clazzGroupIds : self.checkedClazzGroupIds(),checkedGroups : self.getCheckedGroups()});
            };
            var assignTime = +$.trim($("#assignTotalTime").text()) || 0;
            if(assignTime > 0){
                $.prompt("<div class='w-ag-center'>选入的作业会被删除，是否确定更换年级？</div>",
                    {
                        title   : "系统提示",
                        buttons : {"取消":false, "确认": true},
                        position: {width: 500},
                        submit: function(e,v,f,m){
                            if(v){
                                levelfn();
                            }
                        },
                        close: function(){}
                    });
            }else{
                levelfn();
            }
        },
        extendLevelClick        : function(option){},
        initialise              : function(option){
            var self = this;
            var _batchclazzs = option.batchclazzs || [];
            self.hasStudents(option.hasStudents);
            self.clazzClickCb = option.clazzClickCb || null;
            if(_batchclazzs.length > 0){
                self.isMoreLevel(_batchclazzs.length > 1);
                var iLen = _batchclazzs.length;
                var clazzLevel = -1;
                for(var i = 0; i < iLen; i++){
                    if(_batchclazzs[i].clazzs.length > 0 && _batchclazzs[i].canBeAssigned){
                        var level = +_batchclazzs[i].classLevel;
                        self.batchclazzs[level - 1] = self.batchclazzs[level - 1].concat(_batchclazzs[i].clazzs);
                        if(_batchclazzs[i].canBeAssigned && clazzLevel == -1){
                            clazzLevel = level;
                        }
                    }
                }
                if(clazzLevel > 0){
                    self.levelClick(self,clazzLevel);
                }else{
                    $17.info("clazzs can't assign");
                    $("#bookInfo").hide();
                    $("#hkTabcontent").hide();
                    $("#ufo").hide();
                }
            }else{
                $17.info("no clazz");
                $("#bookInfo").hide();
                $("#hkTabcontent").hide();
                $("#ufo").hide();
            }
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getLevels: function(){
            return new Levels();
        }
    });
}($17,ko));