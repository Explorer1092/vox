(function(window,$17,constantObj,undefined){
    "use strict";
    function LevelsAndBook(obj, resolveCb, rejectCb, termCarts){
        var self = this;
        self.resolveCb         = resolveCb;
        self.rejectCb          = rejectCb;
        self.loading           = ko.observable(true);

        //年级
        self.clazzList         = ko.observableArray([]);
        self.focusClazzGroupId = ko.observable(null);
        self.focusClazzLevel   = ko.observable(null);
        self.showClazzList     = ko.observableArray([]);
        self.isAllChecked = ko.pureComputed(function(){

            return self.showClazzList().length == self.checkedClazzGroupIds().split(",").length;
        });

        //换教材
        self.changeBookModule = null;
        self.bookId = ko.observable(null);
        self.bookName = ko.observable(null);
        self.termCarts = termCarts || {};
    }
    LevelsAndBook.prototype = {
        constructor : LevelsAndBook,
        internalSetClazz : function(clazzGroupId,level){

            this.focusClazzGroupId(clazzGroupId);
            this.focusClazzLevel(level);
            if(level){

                var showClazzList = [];
                $.each(this.clazzList(),function () {

                    var currentClazzLevel = this.clazzLevel;
                    var clazzs = this.clazzs;
                    $.each(this.clazzs,function () {

                        this.checked = false;
                        if(currentClazzLevel == level){
                            showClazzList = clazzs;
                            this.checked = true;
                        };
                    });
                });
                this.showClazzList(ko.mapping.fromJS(showClazzList)());
            }
        },
        internalClearCart : function(){
            var self = this;
            typeof self.termCarts.clearCarts === 'function'
            && self.termCarts.clearCarts();
        },
        run : function(){
            var self         = this,
                newClazzList = [],
                clazzList    = constantObj.batchclazzs;

            if($.isArray(clazzList)){
                for(var i = 0; i < clazzList.length; i++){

                    var item = $.extend(true,{},clazzList[i]);
                    item.clazzGroupId = [];
                    if(item.canBeAssigned && $.isArray(item.clazzs)){
                        var newClazzs = [];
                        $.each(item.clazzs,function(){
                            if(!this.hasUncheckedHomework){
                                var newThis = $.extend(true,{},this);
                                newThis.checked = false;
                                newClazzs.push(newThis);
                                item.clazzGroupId.push (newThis.clazzId + "_" +newThis.groupId);
                            }
                        });
                        item.clazzs = newClazzs;
                        newClazzList.push(item);
                    }
                }
                self.clazzList(newClazzList);
            };
            if(newClazzList.length > 0){
                var clazzGroupId = newClazzList[0].clazzGroupId.join(",");
                self.clazzList(newClazzList);
                self.internalSetClazz(clazzGroupId,newClazzList[0].clazzLevel);
                self.loadBookByClazzIds(clazzGroupId);
            }else{
                //对当前重置
                self.loading(false);
                self.clazzList([]);
                self.internalSetClazz(null,null);
                self.rejectCb({
                   errorCode : "NO_CLAZZ"
                });
            }
        },

        chooseOrCancelAll:function () {

            var self = this,
                _isAllChecked = self.isAllChecked(),
                _clazzList = self.showClazzList();
            for(var k = 0; k < _clazzList.length; k++){
                self.showClazzList()[k].checked(!_isAllChecked);
            }
            self.focusClazzGroupId(self.checkedClazzGroupIds());
            typeof self.resolveCb === "function" && self.resolveCb();
        },

        checkedClazzGroupIds : function(){
            var self = this;
            var checkedClazzGroupIds = [];
            var _clazzList = self.showClazzList();
            for(var z = 0; z < _clazzList.length; z++){
                if(_clazzList[z].checked()){
                    var clazzGroupId = _clazzList[z].clazzId() + "_" + _clazzList[z].groupId();
                    checkedClazzGroupIds.push(clazzGroupId);
                }
            }
            return checkedClazzGroupIds.join();
        },

        singleClazzAddOrCancel:function (self,index) {

            var checked = self.showClazzList()[index].checked();
            self.showClazzList()[index].checked(!checked);
            self.focusClazzGroupId(self.checkedClazzGroupIds());

            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_selectClass_click",
                s0 : constantObj.subject
            });
            typeof self.resolveCb === "function" && self.resolveCb({
                success : true,
                clazzGroupIds:self.focusClazzGroupId()
            });
        },

        levelClick : function(self){
            var clazz = this,
                clazzGroupId = clazz.clazzGroupId.join(),
                clazzLevel = clazz.clazzLevel;
            if(clazzGroupId === self.focusClazzGroupId()){
                return false;
            }
            var levelFn = function(){
                self.internalSetClazz(clazzGroupId, clazzLevel);
                self.loadBookByClazzIds(clazzGroupId);
            };
            var assignTime = +self.termCarts.getAssignTotalTime() || 0;
            if(assignTime > 0){
                $.prompt("<div class='w-ag-center'>选入的作业会被删除，是否确定更换年级？</div>",
                    {
                        title   : "系统提示",
                        buttons : {"取消":false, "确认": true},
                        position: {width: 500},
                        submit: function(e,v,f,m){
                            if(v){
                                self.internalClearCart();
                                levelFn();
                            }
                        },
                        close: function(){}
                    });
            }else{
                levelFn();
            }
        },

        initBookInfo : function(bookObj){

            this.bookId(bookObj.bookId);
            this.bookName(bookObj.bookName);
            this.termType = bookObj.termType || 1;
        },

        loadBookByClazzIds  : function(clazzGroupId){
            if($17.isBlank(clazzGroupId)){

                return false;
            }
            var self = this,
                clazzBookUrl = "/teacher/termreview/clazzbook.vpage",
                paramData = {
                    subject : constantObj.subject,
                    clazzGroupIds  : clazzGroupId
                };

            $.get(clazzBookUrl,paramData,function(data){
                self.loading(false);

                if(data.success && data.clazzbook){

                    self.initBookInfo(data.clazzbook);
                    typeof self.resolveCb === "function" && self.resolveCb();
                }else{
                    //重置课本信息
                    self.bookId(null);
                    self.bookName(null);
                    //调用 reject 回调
                    $17.voxLog({
                        module  : "API_REQUEST_ERROR",
                        op      : "API_STATE_ERROR",
                        s0      : clazzBookUrl,
                        s1      : $.toJSON(data),
                        s2      : $.toJSON(paramData)
                    });
                }
            });
        },
        changeBook : function(){
            var self = this;
            var _level = self.focusClazzLevel();
            var _term = self.termType;
            if($17.isBlank(_level)){
                $17.alert("请选择年级");
                return false;
            }

            var bookPop = function(){
                var changeBookOption = {
                    level           : _level,
                    term            : _term,
                    clazzGroupIds   : [self.focusClazzGroupId()],
                    bookName        : self.bookName(),
                    subject         : constantObj.subject,
                    isSaveBookInfo  : true,
                    bookListUrl     : "/teacher/termreview/booklist.vpage"
                };
                if(!self.changeBookModule){
                    self.changeBookModule = new ChangeBook()
                }
                self.changeBookModule.init(changeBookOption,function(data){
                    $17.alert(data.info,function(){
                    },function(){
                        self.loadBookByClazzIds(self.focusClazzGroupId());
                    });
                });
            };
            var assignTime = +self.termCarts.getAssignTotalTime() || 0;
            if(assignTime > 0){
                $.prompt("<div class='w-ag-center'>更换教材后，当前教材下选入的作业会删除，确认更换吗？</div>",
                    {
                        title: "系统提示",
                        buttons: {"取消":false, "确认": true},
                        position: {width: 500},
                        submit: function(e,v,f,m){
                            e.preventDefault();
                            if(v){

                                self.internalClearCart();
                                typeof self.resolveCb === "function" && self.resolveCb();
                                bookPop();
                            }else{
                                $.prompt.close();
                            }
                        }
                    });
            }else{
                bookPop();
            }
        },

        getBookId : function(){
            return this.bookId();
        },
        getBookName : function(){
            return this.bookName();
        },
        getClazzGroupId : function(){
            return this.focusClazzGroupId();
        },
        checkedClazzName : function(){
            var self = this,
                clazzList = self.showClazzList() || [],
                clazzName = [];

            $.each(clazzList, function () {
               if(this.checked()){

                   clazzName.push(this.fullName());
               }
            });

            return clazzName.join("，");
        }
    };

    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getLevelsAndBook: function(obj,resolveCb,rejectCb, termCarts){
            return new LevelsAndBook(obj,resolveCb,rejectCb, termCarts);
        }
    });
}(window,$17,constantObj));