!function(window,ko) {
    "use strict";

    var unitReport = function(){
        this.clazzId = ko.observable("");
        this.groupId = ko.observable("");
        this.clazzList = ko.observableArray([]);
        this.changeBookModule = null;
        this.bookId = ko.observable("");
        this.bookName = ko.observable("");
        this.unitId = ko.observable("");
        this.unitName = ko.observable("");
        this.isModuleList = ko.observable(false);
        this.unitList = ko.observableArray([]);
        this.layoutHomeworkTimes = ko.observable("0");
        this.studentList = ko.observableArray([]);

        this.init();
    };

    unitReport.prototype = {
        constructor : unitReport,

        init : function(){
            this.clazzId(constantObj.batchclazzs[0].classId);
            this.groupId(constantObj.batchclazzs[0].groupId);
            this.clazzList(constantObj.batchclazzs);
            if(constantObj.batchclazzs && constantObj.batchclazzs.length > 6){
                new SimpleSlider({
                    slideName      : "slider",
                    clickLeftId    : "#swipingLeft",
                    clickRightId   : "#swipingRight",
                    slideContainer : "#slideContainer",
                    slideItem      : ".slideItem",
                    itemWidth      : "157",
                    slideCount     : 1,
                    totalCount     : constantObj.batchclazzs.length,
                    clickSlideItemFun : function(){}
                });
            }else{
                $("#swipingRight").hide();
            }

            this.initBook();
        },
        initBook : function(){
            var self = this;
            $.get("/teacher/new/homework/clazz/book.vpage",{
                clazzs : self.clazzId()+"_"+self.groupId(),
                isTermEnd : false,
                subject : constantObj.subject
            },function(data){
                if(data.success){
                    var result = data.clazzBook;
                    self.bookId(result.bookId);
                    self.bookName(result.bookName);
                    self.initUnitList(result);
                    self.initStudentList();
                }else{
                    $17.alert(data.info || "课本信息获取失败,请稍后再试~");
                }
            });
        },
        initUnitList : function(result){
            var self = this;
            if(result.moduleList && result.moduleList.length > 0){
                self.isModuleList(true);
                self.unitList(result.moduleList);
                $.each(result.moduleList,function(){
                    $.each(this.units,function(){
                        if(this.defaultUnit){
                            self.unitId(this.unitId);
                            self.unitName(this.cname);
                            return false;
                        }
                    });
                });
            }else{
                self.isModuleList(false);
                self.unitList(result.unitList);
                $.each(result.unitList,function(){
                    if(this.defaultUnit){
                        self.unitId(this.unitId);
                        self.unitName(this.cname);
                        return false;
                    }
                });
            }
        },
        initStudentList : function(){
            var self = this;
            $.get("/teacher/newhomework/report/unit.vpage",{
                groupId : self.groupId(),
                unitId  : self.unitId()
            },function(res){
                if(res.success){
                    self.layoutHomeworkTimes(res.data.layoutHomeworkTimes);
                    self.studentList(res.data.studentUnitReportBOList);
                }else{
                    $17.alert(res.info || "学生列表获取失败,请稍后再试~");
                }
            });
        },
        changeClazz : function(element,self){
            $(element).addClass("active").siblings().removeClass("active");
            self.clazzId(this.classId);
            self.groupId(this.groupId);
            self.initBook();
        },
        changeBook : function(self){
            if(!self.changeBookModule){
                self.changeBookModule = new ChangeBook();
            }
            self.changeBookModule.init({
                clazzGroupIds  : [self.clazzId()+"_"+self.groupId()],
                bookName       : self.bookName(),
                subject        : constantObj.subject,
                isSaveBookInfo : false
            },function(data){
                self.bookId(data.bookId);
                self.bookName(data.bookName);

                $.get("/teacher/new/homework/goal/changebook.vpage",{
                    bookId : data.bookId,
                    subject : constantObj.subject
                },function(data){
                    self.initUnitList(data.clazzBook);
                    self.initStudentList();
                });
            });
        },
        changeUnit : function(element,self){
            $(element).addClass("w-radio-current").siblings(".J_unitRadio").removeClass("w-radio-current");
            self.unitId(this.unitId);
            self.unitName(this.cname);
            self.initStudentList();
        },
        downloadData : function(){
            var url = constantObj.domain + "/teacher/newhomework/report/downloadHomeworkUnitReport.vpage?";
            return url + "groupId=" + this.groupId() + "&unitId=" + this.unitId() + "&clazzId=" + this.clazzId();
        }
    };

    ko.applyBindings(new unitReport(),document.getElementById("mainContent"));

}(window,ko);