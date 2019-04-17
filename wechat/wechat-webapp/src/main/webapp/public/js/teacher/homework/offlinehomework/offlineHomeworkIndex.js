define(["$17", "knockout","logger", "komapping", "weuijs"], function ($17, ko, logger) {
    "use strict";
    function offlineLog(logJson) {
        logJson = $.extend(true,{
            app    : "teacher",
            module : "m_2M8WOHc5"
        },logJson);
        logger.log(logJson);
    }
    function ContentType(typeObj){
        var self = this;
        self.type = ko.observable(typeObj.type || null);
        self.typeName = ko.observable(typeObj.typeName || "");
        self.checked = ko.observable(false);
        self.count = ko.observable(typeObj.count || 0);
        self.contentId = ko.observable(typeObj.contentId || null);
        self.contentName = ko.observable(typeObj.contentName || "");
    }
    ContentType.prototype = {
        constructor : ContentType,
        setUnit : function(unitId,unitName,count){
            var self = this;
            self.contentId(unitId);
            self.contentName(unitName);
            self.count(+count || 0);
        }
    };

    function UnitsPopUp(unitList,submitCb,closeCb){
        var self = this;
        self.minCount = 1;
        self.maxCount = 10;
        self.unitsShow = ko.observable(false);
        self.focusType = ko.observable(null);
        self.displayCount = ko.observable(3);
        self.focusUnitId = ko.observable(null);
        self.focusUnitName = ko.observable(null);
        self.unitList = ko.observable(unitList);
        self.submitCb = submitCb;
        self.closeCb = closeCb;
    }
    UnitsPopUp.prototype = {
        constructor  : UnitsPopUp,
        readingClick : function(stepCount){
            var self = this;
            var newValue = self.displayCount() + (+stepCount || 0);
            if(newValue < self.minCount || newValue > self.maxCount){
                return false;
            }

            self.displayCount(newValue);
            offlineLog({
                op : "o_ey9DYkkY",
                s0 : self.focusType()
            });
        },
        unitClick   : function(self,clickWay){
            var unit = this;
            self.focusUnitId(unit.unitId);
            self.focusUnitName(unit.unitName);
            if(clickWay && clickWay === 'MANUAL_CLICK'){
                offlineLog({
                    op : "o_WNDKbJlc",
                    s0 : self.focusType()
                });
            }
        },
        setDefault : function(type,unit,count){
            var self = this;
            self.focusType(type);
            self.displayCount(count);
            self.unitClick.call(unit,self);
        },
        closeClick : function(){
            var self = this;
            self.focusUnitId(null);
            self.focusUnitName("");
            self.displayCount(3);
            self.focusType(null);
            self.unitsShow(false);
        },
        submitClick : function(){
            var self = this;
            if(typeof self.submitCb === 'function'){
                self.submitCb(self.focusType(),self.focusUnitId(),self.focusUnitName(),self.displayCount());
            }
            self.closeClick();
        }
    };

    function OfflineHomework(params){
        var self = this,inputJson = $.isPlainObject(params) ? params : {};
        self.webLoading = ko.observable(true);
        self.from = ko.observable(inputJson.from || "");  //从哪个页面跳转过来
        self.subject = inputJson.subject || null;
        self.homeworkIds = inputJson.homeworkIds || null;
        self.groupIds = inputJson.groupIds || null;
        self.bookId = null;
        self.endDateTime = ko.observable(null);
        self.parentSign = ko.observable(true);
        self.parentSignVisible = ko.observable(true); //家长签字选项是否显示
        self.contentTypes = ko.observableArray([]);
        self.unitsPopup = null;
        self.inputOfUserDefined = null;
        self.confirmBtnDisabled = ko.observable(true);
    }
    OfflineHomework.prototype = {
        constructor : OfflineHomework,
        displayMode : function(typeObj){
            return "t:" + typeObj.type();
        },
        confirmClick : function(){
            var self = this;
            try{
                if(self.confirmBtnDisabled()){
                    return false;
                }
                offlineLog({
                    op : "o_GJx5tH9h"
                });
                self.confirmBtnDisabled(true);
                var contentMap = {};
                ko.utils.arrayForEach(self.contentTypes(),function(contentObj){
                    if(contentObj.checked && contentObj.checked()){
                        var focusType = contentObj.type();
                        switch (focusType){
                            case "READ":
                            case "LISTEN":
                                contentMap[focusType] = {
                                    bookId : self.bookId,
                                    unitId : contentObj.contentId(),
                                    practiceCount : contentObj.count()
                                };
                                break;
                            case "DICTATION":
                                contentMap[focusType] = {
                                    bookId : self.bookId,
                                    unitId : contentObj.contentId()
                                };
                                break;
                            case "CUSTOMIZE":
                                contentMap[focusType] = {
                                    customContent : contentObj.contentName()
                                };
                                break;
                            default:
                        }
                    }
                });
                if($.isEmptyObject(contentMap)){
                    self.setReponseInfo("请选择作业类型");
                }else{
                    if(contentMap["CUSTOMIZE"] && (!contentMap["CUSTOMIZE"]["customContent"] || contentMap["CUSTOMIZE"]["customContent"] === '请输入自定义作业内容')){
                        self.setReponseInfo("请输入自定义作业内容");
                        self.confirmBtnDisabled(false);
                    }else{
                        var obj = {
                            endTime         : self.endDateTime(),
                            clazzGroupIds   : self.groupIds,
                            homeworkIds     : self.homeworkIds,
                            needSign        : self.parentSignVisible() && self.parentSign(),
                            practices       : contentMap,
                            source          : "Wechat"
                        };

                        $.post("/teacher/offline/homework/assign.vpage",{
                            data : JSON.stringify(obj),
                            subject : self.subject
                        },function(data){
                            var result = (typeof data === "string") ? $.parseJSON(data) : data;
                            if(result.success){
                                location.href = "/teacher/homework/offlinehomework/share.vpage?" + $.param({
                                        from : self.from()
                                    });
                            }else{
                                self.setReponseInfo(result.info || "返回信息失败");
                                self.confirmBtnDisabled(false);
                            }
                        });
                    }
                }
            }catch(ex){
                self.setReponseInfo(ex.message);
                self.confirmBtnDisabled(false);
            }
        },
        cancelClick    : function(){
            var self = this;
            offlineLog({
                op : "o_3kD9fRJt"
            });
            $.modal({
                title: "提示",
                text: "确定取消正在编辑的内容？",
                buttons: [
                    { text: "取消", className: "default", onClick: function(){}},
                    { text: "确定", onClick: function(){
                        offlineLog({
                            op : "o_mYzR6i6N"
                        });
                        setTimeout(function(){
                            location.href = "/teacher/homework/report/history.vpage";
                        },200);
                    }}
                ]
            },function(){
                offlineLog({
                    op : "o_zrMuP5q6"
                });
            });
        },
        typeChecked    : function(element,self){
            var contentTypeObject = this;
            var oldCheckedValue = contentTypeObject.checked();
            var type = contentTypeObject.type();
            var relevanceElems = $(element).siblings("li[data-type='" + type + "']");
            contentTypeObject.checked(!oldCheckedValue);
            relevanceElems.toggle(!oldCheckedValue);
            var atLeastHaveOneChecked;
            if(oldCheckedValue){
                atLeastHaveOneChecked = false;
                //检查是否有一个是选中状态
                ko.utils.arrayForEach(self.contentTypes(),function(content){
                    atLeastHaveOneChecked = atLeastHaveOneChecked || content.checked();
                });
            }else{
                atLeastHaveOneChecked = true;
                offlineLog({
                    op : "o_hiJizHpm",
                    s0 : type
                });
            }
            self.confirmBtnDisabled(!atLeastHaveOneChecked);
            var contentName = contentTypeObject.contentName();
            if(type === 'CUSTOMIZE' && !oldCheckedValue && !contentName){
                self.inputOfUserDefined.setTypeComment(type,contentName).show(true);
            }
        },
        typeClick      : function(self){
            var contentTypeObject = this;
            var _focusType = contentTypeObject.type();
            switch(_focusType){
                case "READ":
                case "LISTEN":
                case "DICTATION":
                    self.unitsPopup.setDefault(_focusType,{
                        unitId : contentTypeObject.contentId(),
                        unitName:contentTypeObject.contentName()
                    },contentTypeObject.count());
                    self.unitsPopup.unitsShow(true);
                    break;
                case "CUSTOMIZE":
                    $.modal({
                        title: "输入更多作业",
                        text: "<textarea style='width: 95%;height:100px;' maxlength='200' placeholder='点击输入更多作业。例：背诵课文、练习题作业、手抄报等。最多输入200字'></textarea>",
                        buttons: [
                            { text: "取消", className: "default", onClick: function(){}},
                            { text: "确定", onClick: function(){
                                var $dialog = $(this),comment = $dialog.find("textarea").val();
                                self.setContentType("CUSTOMIZE",null,$17.isBlank(comment) ? "请输入自定义作业内容" : comment,0);
                                offlineLog({
                                    op : "o_uGTJb3um"
                                });
                            }}
                        ]
                    });
                    break;
                default:
            }
            offlineLog({
                op : "o_yNFpdEPC",
                s0 : _focusType
            });
        },
        setContentType : function(focusType,unitId,unitName,count){
            var self = this;
            ko.utils.arrayForEach(self.contentTypes(),function(contentTypeObj){
                if(focusType === contentTypeObj.type()){
                    contentTypeObj.setUnit(unitId,unitName,count);
                }
            });
        },
        parentSignClick : function(){
            var self = this;
            self.parentSign(!self.parentSign());
        },
        setReponseInfo : function(info){
            var self = this;
            $.toast(info,"text");
        },
        convertContentTypes : function(contentTypes,unit){
            var self = this;
            unit = unit || {};
            contentTypes = $.isArray(contentTypes) ? contentTypes : [];
            var newContentTypes = [];
            for(var m = 0,mLen = contentTypes.length; m < mLen; m++){
                var typeObj = contentTypes[m]
                    ,_type = typeObj.type
                    ,typeOpts;
                switch (_type){
                    case "READ":
                    case "LISTEN":
                    case "DICTATION":
                        typeOpts = {
                            type        : _type,
                            typeName    : typeObj.typeName,
                            count       : 3,
                            contentId   : unit.unitId,
                            contentName : unit.unitName
                        };
                        break;
                    case "CUSTOMIZE":
                    default:
                        typeOpts = {
                            type        : _type,
                            typeName    : typeObj.typeName,
                            count       : 3,
                            contentId   : null,
                            contentName : "请输入自定义作业内容"
                        };
                }
                newContentTypes.push(new ContentType(typeOpts));
            }
            return newContentTypes;
        },
        setOfflineHomework : function(){
            var self = this;
            try{
                $.showLoading();
                offlineLog({
                    op : "o_B3OQ8pxC",
                    s0 : self.from()
                });
                $.post("/teacher/offline/homework/index.vpage",{
                    homeworkIds    : self.homeworkIds,
                    clazzGroupIds  : self.groupIds,
                    subject        : self.subject
                },function(data){
                    var result = (typeof data === "string") ? $.parseJSON(data) : data;
                    $.hideLoading();
                    if(result.success){
                        var unit;
                        var units = result.units || [];
                        for(var t = 0,tLen = units.length; t < tLen; t++){
                            if(units[t].defaultUnit){
                                unit = units[t];
                                break;
                            }
                        }
                        if(!unit){
                            self.setReponseInfo("没有默认的单元");
                        }else{
                            self.bookId = result.bookId;
                            self.unitList  = units;
                            self.contentTypes(self.convertContentTypes(result.contentTypes,unit));
                            self.endDateTime(result.endTime);
                            self.unitsPopup = new UnitsPopUp(units,function(type,unitId,unitName,count){
                                self.setContentType(type,unitId,unitName,count);
                            });
                            ko.applyBindings(self.unitsPopup,document.getElementById("unitsPopUp"));

                            var nodeList = document.getElementsByClassName("assignOffline");
                            for(var m = 0,mLen = nodeList.length; m < mLen; m++){
                                ko.applyBindings(self,nodeList[m]);
                            }
                            self.webLoading(false);
                        }
                    }else{
                        self.setReponseInfo(result.info || "返回信息失败");
                    }
                });
            }catch (e){
                self.setReponseInfo(e.message);
            }
        }
    };
    var olhParams = {
        from        : $17.getQuery("from"),
        subject     : $17.getQuery("subject"),
        homeworkIds : $17.getQuery("homeworkIds")
    };
    new OfflineHomework(olhParams).setOfflineHomework();
});