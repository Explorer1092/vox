(function($17,ko) {
    "use strict";

    if(!$17.isBlank(ko)){
        ko.bindingHandlers.allUnitHover = {
            init: function(element, valueAccessor){
                $(element).hover(
                    function(){
                        $17.voxLog({
                            module: "Newhomework_assign_" + $uper.subject.key,
                            op : "all_unit"
                        });
                    },
                    function(){}
                );
            }
        };
    }

    var Book = function(){
        var self = this;
        self.bookId = ko.observable(null);
        self.bookName = ko.observable(null);
        self.unitList = ko.observableArray([]);
        self.focusUnitIndex = ko.observable(-1);
        self.focusUnit = ko.pureComputed(function(){
            var _startIndex = self.focusUnitIndex();
            if(_startIndex == -1 || _startIndex >= self.unitList().length){
                return ko.observable(null);
            }
            return self.unitList.slice(_startIndex, _startIndex + 1)[0];
        });

        //换教材属性
        self.terms = [1,2]; //所有学期
        self.level = 0;
        self.term = 1;
        self.clazzGroupIds  = [];
        self.changeBookModule = null;
    };

    Book.prototype = {
        constructor         : Book,
        checkSections : function(){
            var self = this;
            var _allSections = [];
            var _checkedSections = [];
            var _unit = ko.mapping.toJS(self.focusUnit());
            for(var j = 0,jLen = _unit.sections.length; j < jLen; j++){
                var _section = {
                    sectionId   : _unit.sections[j].sectionId,
                    sectionName : _unit.sections[j].cname
                };
                _allSections.push(_section);
                if(_unit.sections[j]["checked"]){
                    _checkedSections.push(_section);
                }
            }

            if(_checkedSections.length == 0){
                return _allSections;
            }else{
                return _checkedSections;
            }
        },
        _sectionClick : function(){
            var self = this;
            var _checkSections = self.checkSections();

            if(!($.isArray(_checkSections) && _checkSections.length > 0)){
                $("#tabContent").empty();
            }
            self.extendSectionClick({
                bookId     : self.bookId(),
                bookName   : self.bookName(),
                unitId     : self.focusUnit().unitId(),
                unitName   : self.focusUnit().cname(),
                sections   : _checkSections
            });
        },
        sectionClick : function(self,sectionIndex){
            var that = this;
            that.checked(!that.checked());
            if(that.checked()){
                $17.voxLog({
                    module: "Newhomework_assign_" + $uper.subject.key,
                    op : "add_choose_section"
                });
            }else{
                $17.voxLog({
                    module: "Newhomework_assign_" + $uper.subject.key,
                    op : "cancel_choose_section"
                });
            }

            self._sectionClick();
        },
        extendSectionClick  : function(obj){},
        changeUnit          : function(nextUnitIndex,source){
            var self = this;
            nextUnitIndex = (+nextUnitIndex) || 0;
            if(nextUnitIndex < 0){
                return false;
            }
            if(nextUnitIndex >= self.unitList().length){
                return false;
            }

            var curUnitIndex = self.focusUnitIndex();
            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_changetUnit_unit_click",
                subject : constantObj.subject
            });


            self.focusUnitIndex(nextUnitIndex);
            self._sectionClick();
        },
        specUnitName : function(unitIndex){
            var self = this;
            unitIndex = (+unitIndex) || 0;
            if(unitIndex < 0){
                return "";
            }
            var unitList = self.unitList();
            if(unitIndex >= unitList.length){
                return "";
            }

            return unitList[unitIndex].cname() + '<i></i>';
        },
        initBookInfo : function(bookObj){
            var self = this;
            var _book = bookObj;
            self.bookId(_book.bookId);
            self.bookName(_book.bookName);
            var _unitList = _book.unitList || [];
            var defaultUnitIndex = -1;
            for(var z = 0,zLen = _unitList.length; z < zLen; z++){
                if(!$17.isBlank(_unitList[z].defaultUnit) && _unitList[z].defaultUnit){
                    defaultUnitIndex = z;
                }
                var _unit = _unitList[z];
                for(var j = 0,jLen = _unit.sections.length; j < jLen; j++){
                    _unit.sections[j]["checked"] = _unit.sections[j].defaultSection || false;
                }
            }
            self.unitList(ko.mapping.fromJS(_unitList)());
            self.focusUnitIndex(defaultUnitIndex);
            self._sectionClick();
        },
        loadBookByClazzIds  : function(clazzGroupIds){
            var self = this;
            var str = clazzGroupIds.join(",")
                ,clazzBookUrl = "/teacher/new/homework/clazz/book.vpage"
                ,paramData = {clazzs : str, isTermEnd : constantObj.isTermEnd, subject : constantObj.subject};
            $.get(clazzBookUrl,paramData,function(data){
                if(data.success && data.clazzBook){
                    var _book = data.clazzBook;
                    self.initBookInfo(_book);
                    //自动更新新学期教材
                    if(_book.remindBookFlag && !$17.isBlank(_book.remindBook) && $17.getQuery("step") != 'showtip'){
                        $.prompt(template("T:自动更新新学期教材", {
                            bookName       : _book.bookName,
                            remindBookName : _book.remindBook.name,
                            remindBookPress: _book.viewContent,
                            color          : _book.color
                        }), {
                            title   : "系统提示",
                            focus   : 1,
                            buttons : { "暂不更换": false, "更换教材": true },
                            position: { width: 500 },
                            submit  : function(e, v){
                                if(v){
                                    $17.voxLog({
                                        module: "m_H1VyyebB",
                                        op : "systemPrompt_changetNewBook_popup_change_click",
                                        s0 : constantObj.subject
                                    });
                                    // 保存课本
                                    self.saveChangeBook(_book.remindBook.id);

                                }else{
                                    $17.voxLog({
                                        module: "m_H1VyyebB",
                                        op : "systemPrompt_changetNewBook_popup_nonChange_click",
                                        s0 : constantObj.subject
                                    });
                                    $.get("/teacher/book/remindbook.vpage", {
                                        clazzLevel : self.level,
                                        subject    : constantObj.subject
                                    }, function(data){
                                        if(!data.success){
                                            $17.voxLog({
                                                module : "API_REQUEST_ERROR",
                                                op : "API_STATE_ERROR",
                                                s0 : "/teacher/book/remindbook.vpage",
                                                s1 : $.toJSON(data),
                                                s2 : $.toJSON({
                                                    clazzLevel : self.level,
                                                    subject    : constantObj.subject
                                                }),
                                                s3 : $uper.env
                                            });
                                        }
                                    });
                                }
                            },
                            loaded : function(){
                                $17.voxLog({
                                    module: "m_H1VyyebB",
                                    op : "systemPrompt_changetNewBook_popup_show",
                                    s0 : constantObj.subject
                                });
                            }
                        });
                    }
                }else{
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op : "API_STATE_ERROR",
                        s0 : clazzBookUrl,
                        s1 : $.toJSON(data),
                        s2 : $.toJSON(paramData),
                        s3 : $uper.env
                    });
                }
            });
        },
        initialise          : function(option){
            var self = this;
            self.clazzGroupIds = option.clazzGroupIds;
            self.level = option.level || 0;
            self.term = $.inArray(option.term,self.terms) != -1 ? option.term : self.terms[0];
            self.loadBookByClazzIds(option.clazzGroupIds);
            return this;
        },
        changeBook : function(){
            var self = this;
            var _level = self.level;
            var _term = self.term;
            if($17.isBlank(_level) || _level == 0){
               $17.alert("请选择年级");
               return false;
            }

            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_changeBook_click",
                s0 : constantObj.subject
            });

            var bookPop = function(){
                var changeBookOption = {
                    level :_level,
                    term :_term,
                    clazzGroupIds:self.clazzGroupIds,
                    bookName:self.bookName(),
                    subject : constantObj.subject,
                    isSaveBookInfo : true
                };
                if(!self.changeBookModule){
                    self.changeBookModule = new ChangeBook();
                }
                self.changeBookModule.init(changeBookOption,function(data){
                    $17.alert(data.info,function(){},function(){
                        self.loadBookByClazzIds(self.clazzGroupIds);
                    });
                });
            };
            var assignTime = +$.trim($("#assignTotalTime").text()) || 0;
            if(assignTime > 0){
               $.prompt("<div class='w-ag-center'>更换教材后，当前教材下选入的作业会删除，确认更换吗？</div>",
                       {
                           title: "系统提示",
                           buttons: {"取消":false, "确认": true},
                           position: {width: 500},
                           submit: function(e,v,f,m){
                               // use e.preventDefault() to prevent closing when needed or return false.
                               e.preventDefault();
                               if(v){
                                   $(".J_delete",".J_UFOInfo").trigger("click");
                                   self._sectionClick();
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
        saveChangeBook : function(bookId){
            var self = this,paramData = {
                clazzs  : self.clazzGroupIds.join(","),
                bookId  : bookId,
                subject : constantObj.subject
            };
            if($17.isBlank(bookId)){
                return false;
            }
            $.post("/teacher/new/homework/changebook.vpage", paramData, function(data){
                $17.alert(data.info,function(){
                    if(data.success){
                        self.loadBookByClazzIds(self.clazzGroupIds);
                    }else{
                        $17.voxLog({
                            module : "API_REQUEST_ERROR",
                            op     : "API_STATE_ERROR",
                            s0     : "/teacher/new/homework/changebook.vpage",
                            s1     : $.toJSON(data),
                            s2     : $.toJSON(paramData),
                            s3     : $uper.env
                        });
                    }
                });
            });
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getBook: function(){
            return new Book();
        }
    });
}($17,ko));
