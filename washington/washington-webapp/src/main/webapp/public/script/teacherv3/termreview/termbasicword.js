/*
* 基础必过
* */

!function($17,ko) {
    "use strict";

    var termBasicWord = function(obj,termCarts,levelsAndBook){

        this.obj = obj;
        this.levelsAndBook = levelsAndBook;
        this.groupStatus = ko.observable("");
        this.groupStatusId = ko.observable("");
        this.homeworkContent = ko.observableArray([]);
        this.homeworkDays = ko.observableArray([]);
    };

    termBasicWord.prototype = {
        constructor : termBasicWord,

        run : function(option){
            var self = this;
            $.extend(true, self.obj, option);
            var paramData = {
                type          : option.type,
                bookId        : option.bookId,
                subject       : constantObj.subject,
                clazzGroupIds : option.clazzGroupId
            };
            $.get("/teacher/termreview/content.vpage", paramData, function(data){
                if(data.success && data.basic){
                    var res = data.basic;
                    if(res.groupStatus){
                        var assignClazz = [],
                            assignClazzId = [];
                        $.each(res.groupStatus,function () {
                            if(this.assigned){
                                assignClazz.push(this.clazzName);
                                assignClazzId.push(this.groupId);
                            };
                        });
                        self.groupStatus(assignClazz.join("、"));
                        self.groupStatusId(assignClazzId.join("、"));
                    };

                    if(res.homeworkContent){

                        $.each(res.homeworkContent,function () {
                            this.checked = true;
                        });

                        self.homeworkContent(ko.mapping.fromJS(res.homeworkContent)());
                    };

                    if(self.homeworkDays){

                        $.each(res.homeworkDays,function () {

                            this.checked = this.isSelect;
                        });

                        self.homeworkDays(ko.mapping.fromJS(res.homeworkDays)());
                    };
                }else{

                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/termreview/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : constantObj.env
                    });
                    //清除上次查询记录
                    self.groupStatus("");
                    self.homeworkContent([]);
                    self.homeworkDays([]);
                }
            });
        },

        selContent : function () {

            this.checked(!this.checked());
        },

        selDays : function (self) {
            var item = this;

            $.each(self.homeworkDays(),function(){

                this.checked(item.day() == this.day());
            });
        },

        getGroupIds:function () {
            var groupIds = [],
                checkGroup = this.groupStatusId();
            if(this.levelsAndBook && this.levelsAndBook.getClazzGroupId){
                var ids = this.levelsAndBook.getClazzGroupId().split(",");
                $.each(ids,function () {
                    var groupId = this.split("_")[1];
                    if(checkGroup.indexOf(groupId) < 0){
                        groupIds.push(groupId);
                    }
                });
            }

            return groupIds;
        },

        assignBasicWord : function () {

            var self = this,
                homeworkDays = "",
                contentTypes = [],
                groupIds = self.getGroupIds();

            $.each(self.homeworkDays(),function () {
                if(this.checked()){

                    homeworkDays = this.day();
                    return false;
                };
            });

            $.each(self.homeworkContent(),function () {
               if(this.checked()){

                   contentTypes.push(this.contentType());
               }
            });

            if(contentTypes.length == 0 || groupIds.length == 0){

                $17.alert("请选择需要布置的班级和内容");
                return false;
            };

            $.post("/teacher/termreview/basicreview/assign.vpage",{
                subject : constantObj.subject,
                data : JSON.stringify({
                    bookId       : self.obj.bookId,
                    groupIds     : groupIds.join(),
                    homeworkDays : homeworkDays,
                    contentTypes : contentTypes
                })
            },function(res) {

                if(res.success){

                    $17.alert("基础必过已布置，点击确定刷新并继续布置专项复习和模考卷吧~ ",function () {

                        window.location.reload();
                    });
                }else{

                    $17.alert(res.info || "基础必过布置失败，请稍后再试～")
                }

            });

            $17.voxLog({
                module : "m_8NOEdAtE",
                op     : "basic_assign_click",
                s0     : constantObj.subject
            });
        }
    };

    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getBasic_word: function(obj,termCarts,levelsAndBook){
            return new termBasicWord(obj,termCarts,levelsAndBook);
        }
    });
}($17,ko);


