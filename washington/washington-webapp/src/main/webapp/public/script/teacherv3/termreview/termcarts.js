(function(window,$17,constantObj,undefined){
    "use strict";
    var homeworkTypeName = {
        EXAM            : "同步习题",
        UNIT_QUIZ       : "配套试卷",
        BASIC_KNOWLEDGE : "基础知识",
        CHINESE_READING : "阅读",
        INTELLIGENT_TEACHING : "课时讲练测"
    },bookEnity = {
        bookId      : null,
        bookName    : ""
    },unitEnity = {
        unitId      : null,
        unitName    : ""
    },lessonEnity = {
        lessonId    : null,
        lessonName  : "",
        sentences   : ""
    },sectionEnity = {
        sectionId   : null,
        sectionName : ""
    },practiceEnity = {
        categoryId       : null,
        practiceCategory : "",
        categoryIcon     : null,
        practiceId       : null,
        practiceName     : ""
    },questionEnity = {
        questionId          : null,
        seconds             : 0,
        questionBoxId       : null,
        answerType          : null,
        submitWay           : null,
        assignTimes         : 0,
        difficulty          : 1,
        difficultyName      : "容易",
        questionType        : "",
        similarQuestionIds  : [],
        teacherAssignTimes  : 0,
        sourceType          : null,
        similarQuestionId   : ""
    };

    ko.bindingHandlers.singleHomeworkTypeHover = {
        init: function(element, valueAccessor){
            var _value = ko.unwrap(valueAccessor());
            $(element).hover(function(e){
                var $this = $(this);
                $this.addClass("hover");
                if((+_value || 0) > 0){
                    $this.find(".h-set-icon-delete").removeClass("h-set-icon-deleteGrey").addClass("h-set-icon-deleteRed");
                }else{
                    $this.find(".h-set-icon-delete").removeClass("h-set-icon-deleteRed").addClass("h-set-icon-deleteGrey");
                }
            },function(e){
                $(this).removeClass("hover");
            });
        },
        update:function(element, valueAccessor){}
    };
    /**
     *
     * @param obj 包含 {
     *     termCartsAssign : 布置按钮回调
     *     previewBtnClick : 预览按钮回调
     *     backAdjustClick : 返回调整回调
     *  }
     * @param termCartsResolve
     * @param termCartsReject
     * @constructor
     */
    function TermCarts(obj,termCartsResolve,termCartsReject){
        obj = obj || {};
        var self = this;
        self.loading            = ko.observable(false);
        self.tabList            = ko.observableArray([]);
        self.totalMin           = ko.pureComputed(function(){
            return Math.ceil(self.totalSeconds()/60);
        },self);
        self.totalSeconds       = ko.observable(0);
        self.homeworkTypeData   = ko.observableArray([]);
        self.typeDisplayMap     = {
            EXAM            : ko.observable(3),
            UNIT_QUIZ       : ko.observable(3),
            BASIC_KNOWLEDGE : ko.observable(3),
            CHINESE_READING : ko.observable(3),
            INTELLIGENT_TEACHING : ko.observable(3)
        };//预览应试题 默认显示三个
        self.termCartsAssign    = obj.termCartsAssign || null;
        self.previewBtnClick    = obj.previewBtnClick || null;
        self.backAdjustClick    = obj.backAdjustClick || null;
        self.switchPanel        = ko.observable("ASSIGN_PANEL");  //switchPanel : ASSIGN_PANEL | PREVIEW_PANEL
        self.deleteTypeDataCb   = obj.deleteTypeDataCb || null;
    }
    TermCarts.prototype = {
        constructor : TermCarts,
        displayMode : function(homeworkTypeContent){
            homeworkTypeContent = homeworkTypeContent || {};
            var templateName;
            switch (homeworkTypeContent.type){
                case "BASIC_APP":
                    templateName = "T:BASIC_APP_REVIEW";
                    break;
                case "EXAM":
                case "UNIT_QUIZ":
                case "BASIC_KNOWLEDGE":
                case "CHINESE_READING":
                case "INTELLIGENT_TEACHING":
                    templateName = "T:EXAM_REVIEW";
                    break;
                case "KEY_POINTS":
                    templateName = "T:KEY_POINTS_REVIEW";
                    break;
                default:
                    templateName = "T:UNKNOWN_REVIEW";
            }
            return templateName;
        },
        getHomeworkTypeName : function(homeworkType){
            return homeworkTypeName[homeworkType] || "未知类型";
        },
        addDisplayCount : function(self,homeworkType){
            var count = self.typeDisplayMap.hasOwnProperty(homeworkType) ? self.typeDisplayMap[homeworkType]() : 3;
            return self.typeDisplayMap[homeworkType](count + 3);
        },
        loadExamImg : function(examId,index){
            var self = this;
            if(!$17.isBlank(examId) && constantObj.examInitComplete){
                var $mathExamImg = $("#"+examId + index);
                $mathExamImg.empty();
                $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id","reviewExamImg-" + examId).appendTo($mathExamImg);
                var node = document.getElementById("reviewExamImg-" + examId);
                var obj = vox.exam.render(node, 'normal', {
                    ids       : [examId],
                    imgDomain : constantObj.imgDomain,
                    env       : constantObj.env,
                    domain    : constantObj.domain
                });
            }else{
                $("#mathExamImgReview" + index).html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
            }
            return "";
        },
        getKpPointsQuestions : function(contents){
            contents = $.isArray(contents) ? contents : [];
            var questions = [];
            for(var m = 0,mLen = contents.length; m < mLen; m++){
                questions = questions.concat(contents[m].questions() || []);
            }
            return questions;
        },
        categoryPreview: function(lessonId,self){
            var category = this
                ,questions = category.questions || [];
            if(questions.length <= 0){
                $17.alert("没有配相应的应试题,暂不能预览");
                return false;
            }
            var qIds = [];
            for(var t = 0, tLen = questions.length; t < tLen; t++){
                qIds.push(questions[t].questionId);
            }
            var paramObj = {
                qids        : qIds.join(","),
                lessonId    : lessonId,
                practiceId  : category.practiceId,
                fromModule  : ""
            };
            var gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
            var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

            $.prompt(data, {
                title   : "预 览",
                buttons : {},
                position: { width: 740 },
                close   : function(){
                    $('iframe').each(function(){
                        var win = this.contentWindow || this;
                        if(win.destroyHomeworkJavascriptObject){
                            win.destroyHomeworkJavascriptObject();
                        }
                    });
                }
            });
            return false;
        },
        getLessonGroup : function(contents){
            contents = $.isArray(contents) ? contents : [];
            var lessonPositionMap = {},lessons = [];
            for(var m = 0,mLen = contents.length; m < mLen; m++){
                var newKey = [contents[m].bookId, contents[m].unitId, contents[m].lessonId].join(":");
                if(lessonPositionMap.hasOwnProperty(newKey)){
                    lessons[lessonPositionMap[newKey]].categories.push(contents[m]);
                }else{
                    var lessonObj = $.extend(true,{categories : []},lessonEnity);
                    for(var key in lessonObj){
                        if(lessonObj.hasOwnProperty(key) && contents[m].hasOwnProperty(key)){
                            lessonObj[key] = contents[m][key];
                        }
                    }
                    lessonObj.categories.push(contents[m]);
                    lessons.push(lessonObj);
                    lessonPositionMap[newKey] = lessons.length - 1;
                }
            }
            return lessons;
        },
        getCategoryIconUrl : function(categoryIcon){
            categoryIcon = +categoryIcon || 9;
            return constantObj.basicIconPrefixUrl + "e-icons-" + categoryIcon + ".png";
        },
        previewOrBackAdjust : function(){
            var self = this;
            if(self.switchPanel() === "ASSIGN_PANEL"){

                if(self.totalSeconds() <= 0){

                    $17.alert("请选择作业内容");
                    return false;
                }
                self.switchPanel("PREVIEW_PANEL");
                typeof self.previewBtnClick === 'function'
                && self.previewBtnClick();

                $17.voxLog({
                    module : "m_8NOEdAtE",
                    op     : "final_review_assign_preview_load",
                    s0     : constantObj.subject
                });
            }else if(self.switchPanel() === "PREVIEW_PANEL"){

                self.switchPanel("ASSIGN_PANEL");
                typeof self.backAdjustClick === 'function'
                && self.backAdjustClick();
            }
        },
        clearCarts                 : function(){
            var self = this,tabList = ko.mapping.toJS(self.tabList());
            for(var j = 0,jLen = tabList.length; j < jLen; j++){
                var obj = {
                    type            : tabList[j].key,
                    newAddCount     : 0 - tabList[j].count,
                    newAddSeconds   : 0 - tabList[j].seconds
                };
                self.internalAddTabList(obj);
            }
            self.homeworkTypeData([]);

        },
        deleteTypeData       : function(self){
            var homeworkType = this.key()
                ,counterObj = {type : homeworkType,newAddCount : 0,newAddSeconds : 0}
                ,storageData = self.getStorageByHomeworkType(homeworkType) || {}
                ,storageContents = storageData.values || [];
            if($.isEmptyObject(storageData) || storageData.position == -1){
                //ignore
                return false;
            }
            switch (homeworkType){
                case "BASIC_APP" :
                    counterObj.newAddCount = 0 - storageContents.length;
                    for(var m = 0,mLen = storageContents.length; m < mLen; m++){
                        var questions = $.isArray(storageContents[m].questions) ? storageContents[m].questions : [];
                        for(var q = 0,qLen = questions.length; q < qLen; q++){
                            counterObj.newAddSeconds -= (questions[q].seconds || 0);
                        }
                    }
                    break;
                case "EXAM" :
                case "UNIT_QUIZ":
                case "BASIC_KNOWLEDGE":
                case "CHINESE_READING":
                    counterObj.newAddCount = 0 - storageContents.length;
                    var examQ = $.isArray(storageContents) ? storageContents : [];
                    for(var t = 0,tLen = examQ.length; t < tLen; t++){
                        counterObj.newAddSeconds -= (examQ[t].seconds || 0);
                    }
                    break;
                case "KEY_POINTS":
                    for(var k = 0,kLen = storageContents.length; k < kLen; k++){
                        var pointQuestions = $.isArray(storageContents[k].questions) ? storageContents[k].questions : []
                            ,zLen = pointQuestions.length;

                        //清空时只能清空最后一个包的数量 chen
                        //counterObj.newAddCount = 0 - zLen;
                        for(var z = 0; z < zLen; z++){
                            counterObj.newAddSeconds -= (pointQuestions[z].seconds || 0);
                            counterObj.newAddCount--
                        }
                    }
                    break;
            }
            self.internalAddTabList(counterObj);
            self.homeworkTypeData.splice(storageData.position,1);
            $.isFunction(self.deleteTypeDataCb) && self.deleteTypeDataCb(homeworkType);
        },
        internalAddTabList   : function(obj){
            var self = this,tabIndex = -1,count = 0,seconds = 0;
            //obj : {type : 'BASIC_APP',newAddCount : 0,newAddSeconds : 0}
            ko.utils.arrayForEach(self.tabList(),function(tab,i){
                 if(tab.key() === obj.type){
                     tabIndex = i;
                 }
            });

            if(tabIndex != -1){
                count = self.tabList()[tabIndex]["count"]() || 0;
                count += (obj.newAddCount || 0);
                self.tabList()[tabIndex].count(count);
                seconds = self.tabList()[tabIndex]["seconds"]() || 0;
                seconds += (obj.newAddSeconds || 0);
                self.tabList()[tabIndex].seconds(seconds);
            }else{
                var newTabEnity = {
                    key     : obj.type,
                    name    : homeworkTypeName[obj.type],
                    count   : (obj.newAddCount || 0),
                    seconds : (obj.newAddSeconds || 0)
                };
                self.tabList.push(ko.mapping.fromJS(newTabEnity));
            }
            self.totalSeconds(self.totalSeconds() + (obj.newAddSeconds || 0));
            if(self.totalSeconds() <= 0 && self.switchPanel() == 'PREVIEW_PANEL'){
                self.previewOrBackAdjust();
            }
        },
        getStorageByHomeworkType : function(homeworkType){
            var self = this,newMap = {},newValues = [],typeData = ko.mapping.toJS(self.homeworkTypeData);
            for(var m = 0,mLen = typeData.length; m < mLen; m++){
                if(typeData[m].type === homeworkType){
                    newMap["position"] = m;
                    newMap["values"] = (typeData[m].contents || typeData[m].questions);
                    break;
                }
            }
            return newMap;
        },
        addEXAM : function(obj){
            var self = this,
                homeworkType = obj.type,
                storageData = self.getStorageByHomeworkType(homeworkType) || {},
                storageContents = storageData.values || [],unAddQuestions = obj.questions || []
                ,counterObj = {type : homeworkType,newAddCount : 0,newAddSeconds : 0};
            //同类型下不能有重题
            var existsQuestionIds = [];
            for(var k = 0,kLen = storageContents.length; k < kLen; k++){
                existsQuestionIds.push(storageContents[k].questionId);
            }
            var newUnAddQuestions = [];
            for(var m = 0,mLen = unAddQuestions.length; m < mLen; m++){
                var unAddQuestionId = unAddQuestions[m].questionId;
                if(existsQuestionIds.indexOf(unAddQuestionId) != -1){
                    //ignore
                    continue;
                }
                existsQuestionIds.push(unAddQuestionId);
                counterObj.newAddCount += 1;
                counterObj.newAddSeconds += (unAddQuestions[m].seconds || 0);
                newUnAddQuestions.push(unAddQuestions[m]);
            }
            if(newUnAddQuestions.length == 0){
                //ignore
                return false;
            }

            if(storageContents.length == 0){
                var tabObj = {
                    type        : homeworkType,
                    questions   : ko.observableArray(newUnAddQuestions)
                };
                self.homeworkTypeData.push(tabObj);
            }else{
                var tempArr = self.homeworkTypeData()[storageData.position]["questions"]();
                self.homeworkTypeData()[storageData.position]["questions"](tempArr.concat(newUnAddQuestions));
            }
            self.internalAddTabList(counterObj);
        },
        removeEXAM  : function(obj){
            var self = this
                ,homeworkType = obj.type
                ,planDeleteQuestions = obj.questions || []
                ,storageData = self.getStorageByHomeworkType(homeworkType) || {}
                ,storageQuestions = storageData.values || []
                ,removeQuestionIds = []
                ,newQuestions = []
                ,counterObj = {type : homeworkType,newAddCount : 0,newAddSeconds : 0};
            if($.isEmptyObject(storageData) || storageData.position === -1){
                //ignore
                return false;
            }
            for(var t = 0,tLen = planDeleteQuestions.length; t < tLen; t++){
                removeQuestionIds.push(planDeleteQuestions[t].questionId);
            }
            for(var m = 0,mLen = storageQuestions.length; m < mLen; m++){
                if(removeQuestionIds.indexOf(storageQuestions[m].questionId) === -1){
                    newQuestions.push(storageQuestions[m]);
                }else{
                    counterObj.newAddSeconds -= (storageQuestions[m].seconds || 0);
                    counterObj.newAddCount -= 1;
                }
            }
            if(newQuestions.length > 0){
                self.homeworkTypeData()[storageData.position]["questions"](newQuestions);
            }else{
                self.homeworkTypeData.splice(storageData.position,1);
            }
            self.internalAddTabList(counterObj);
        },
        addQuestion : function(obj){
            $17.info("add question");
            $17.info(obj);
            var self = this,
                //addQuestionFn = "add" + obj.type;
                addQuestionFn = "addEXAM";

            if(typeof self[addQuestionFn] === 'function'){
                self[addQuestionFn].apply(self,[obj]);
            }else{
                $17.info("function 【" + addQuestionFn + "】 NOT FOUND");
            }
        },
        removeQuestion : function(obj){
            $17.info("remove question");
            $17.info(obj);
            var self = this,
                //removeQuestionFn = "remove" + obj.type;
                removeQuestionFn = "removeEXAM";

            if(typeof self[removeQuestionFn] === 'function'){
                self[removeQuestionFn].apply(self,[obj]);
            }else{
                $17.info("function 【" + removeQuestionFn + "】 NOT FOUND");
            }
        },
        getQuestionsByHomeworkType : function(homeworkType){
            if($17.isBlank(homeworkType)){
                return ko.mapping.toJS(this.homeworkTypeData);
            }
            return this.getStorageByHomeworkType(homeworkType).values || [];
        },
        assignClick : function(){
            var self = this;
            typeof self.termCartsAssign === 'function'
            && self.termCartsAssign({
                tabDetails : ko.mapping.toJS(self.tabList()),
                totalTime  : self.totalSeconds()
            });

            $17.voxLog({
                module : "m_8NOEdAtE",
                op     : "popup_assign_confirmcontent_show",
                s0     : constantObj.subject
            });
        },

        getEXAM_Practices  : function(questions){
            var newQuestions = [],
                sectionMap = {},
                examBooks = [];
            for(var j = 0,jLen = questions.length; j < jLen; j++){
                var question = {
                    questionId          : questions[j].questionId,
                    seconds             : questions[j].seconds || 0,
                    questionBoxId       : questions[j].questionBoxId || null,
                    answerType          : questions[j].answerType,
                    submitWay           : questions[j].submitWay,
                    sourceType          : questions[j].sourceType || null,
                    similarQuestionId   : questions[j].similarQuestionId || ""
                };
                newQuestions.push(question);
                var newKey = [questions[j].bookId, questions[j].unitId, questions[j].lessonId, questions[j].sectionId].join("_");
                if(sectionMap.hasOwnProperty(newKey)){
                    sectionMap[newKey]["includeQuestions"].push(questions[j].questionId);
                }else{
                    sectionMap[newKey] = {
                        bookId           : questions[j].bookId,
                        unitId           : questions[j].unitId,
                        lessonId         : questions[j].lessonId,
                        sectionId        : questions[j].sectionId,
                        includeQuestions : [questions[j].questionId]
                    }
                }
            }
            for(var p in sectionMap){
                if(sectionMap.hasOwnProperty(p)){
                    examBooks.push(sectionMap[p]);
                }
            }
            return [{
                questions : newQuestions
            },examBooks]
        },

        getPracticesAndBooks : function(){
            var self = this,practicesMap = {},bookMap = {},homeworkTypeData = self.homeworkTypeData() || [];
            for(var m = 0; m < homeworkTypeData.length; m++){
                var homeworkType = homeworkTypeData[m].type,newQuestions = [],practicesFn = "getEXAM_Practices";
                if(typeof self[practicesFn] === 'function'){
                    var _contents = homeworkTypeData[m].contents ? ko.mapping.toJS(homeworkTypeData[m].contents()) : null
                        ,_questions = homeworkTypeData[m].questions ? ko.mapping.toJS(homeworkTypeData[m].questions()) : null
                        ,practiceBookArr = self[practicesFn].call(self,_contents || _questions);
                    practicesMap[homeworkType] = practiceBookArr[0];
                    bookMap[homeworkType] = practiceBookArr[1];
                }else{
                    $17.info("function 【" + practicesFn + "】 not found")
                }
            }
            return [practicesMap,bookMap];
        }
    };

    /**
     * 布置期末作业确认弹窗
     * @constructor
     */
    ko.extenders.maxlength = function (target, maxlength) {
        var view = ko.dependentObservable({
            read: target,
            write: function (value) {
                if (value.length <= maxlength) {
                    target(value);
                } else {
                    view.notifySubscribers(target()); // "refresh" the view
                }
            }
        });
        target.view = view;
        target.maxlength = maxlength;
        return target;
    };

    var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
    var m = [
        '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
        '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
        '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
        '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
        '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
        '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
    ];
    function TermConfirm(option,termConfirmAssign){
        var self = this;
        self.clazzGroupIds  = option.clazzGroupIds || [];
        self.clazzNames     = ko.observableArray(option.clazzNames || []);
        self.startDateTime  = ko.observable(option.startDateTime || "");  //yyyy-MM-dd 00:00:00
        self.startDate      = ko.pureComputed(function(){
            return self.startDateTime().substring(0,10);
        });
        self.endDateTime    = ko.observable(self.startDate() + " 23:59:59");
        self.endLabel       = ko.observable("zero"); // 结束时间label项,zero:今天, one:明天, two:三天,curstom : 自定义
        self.endDateInput   = ko.pureComputed(function(){
            return self.endDateTime().substring(0,10);
        });
        self.displayEndDate = ko.pureComputed(function(){
            var _endDateTime = self.endDateTime();
            var timeArr = _endDateTime.split(/:|-|\s/g);
            var endDate = new Date(timeArr[0], timeArr[1] - 1, timeArr[2], timeArr[3], timeArr[4], timeArr[5]);
            return self.endDateTime() + self.getWeekName(endDate.getDay());
        });

        self.nowEndTime = option.nowEndTime || "23:59";
        self.hourSelect = ko.observableArray(h);
        self.focusHour = ko.observable("23");
        self.minSelect = ko.observableArray(m);
        self.focusMin  = ko.observable("59");
        self.focusHour.subscribe(function(newValue){
            self.endLabel("custom");
            self.endDateTime(self.endDateInput() + " " + newValue + ":" + self.focusMin() + ":59");
        },self);
        self.focusMin.subscribe(function(newValue){
            self.endLabel("custom");
            self.endDateTime(self.endDateInput() + " " + self.focusHour() + ":" + newValue + ":59");
        },self);

        self.comment = ko.observable("").extend({ maxlength: 100 });
        var tabDetails = option.tabDetails || [],newTabDetail = [];
        for(var t = 0,tLen = tabDetails.length; t < tLen; t++){
            var count = +tabDetails[t].count || 0;
            if(count == 0){
                continue;
            }
            newTabDetail.push(tabDetails[t]);
        }
        self.tabDetails = ko.observableArray(newTabDetail || []);
        self.totalTime = ko.observable(+option.totalTime || 0);
        self.minute    = ko.pureComputed(function(){
            return Math.ceil(self.totalTime() / 60);
        });
        self.initBeanCount = 0;
        self.beanCount = ko.observable(0);
        self.maxBeanCount = ko.observable(0);
        self.showBean = ko.observable(false);
        self.beanCount.subscribe(self.beanCountValidate,self);
        self.termConfirmAssign = termConfirmAssign || null;
    }
    TermConfirm.prototype = {
        constructor : TermConfirm,
        splitDateTime     : function(dateTime){
            return dateTime.split(/:|-|\s/g);
        },
        getTimeArray      : function(array, index){
            return $.grep(array, function (val, key) {
                return val >= index;
            });
        },
        getWeekName       : function(weekDay){
            return ["(星期日)","(星期一)","(星期二)","(星期三)","(星期四)","(星期五)","(星期六)"][weekDay];
        },
        resetEndDateTime  : function(){
            var self = this;
            var _startDate = self.startDate();
            var _endDate = self.endDateTime().substring(0,10);
            var defaultHour = "00";
            var defaultMin = "00";
            if(_startDate === _endDate){
                var sdtArr = self.splitDateTime(self.nowEndTime);
                defaultHour = sdtArr[0];
                defaultMin = sdtArr[1];
            }
            var _hArr = self.getTimeArray(h,defaultHour);
            var _mArr = self.getTimeArray(m,defaultMin);
            self.hourSelect(_hArr);
            self.minSelect(_mArr);
            var _lastHour = _hArr[_hArr.length - 1];
            self.focusHour(_lastHour);
            var _lastMin = _mArr[_mArr.length - 1];
            self.focusMin(_lastMin);
            self.endDateTime(self.endDateInput() + " " + _lastHour + ":" + _lastMin + ":59");
        },
        changeEndDate     : function(day, endLabel){
            var self = this;
            self.endLabel(endLabel);
            switch (endLabel){
                case "zero":
                case "one":
                case "two":
                    var _startDateTime = self.startDateTime();
                    var startTimeArr = _startDateTime.split(/:|-|\s/g);
                    var refDate = new Date(startTimeArr[0], startTimeArr[1] - 1, startTimeArr[2], startTimeArr[3], startTimeArr[4], startTimeArr[5]);
                    self.endDateTime($17.DateUtils("%Y-%M-%d 23:59:59",day,"d",refDate));
                    self.resetEndDateTime();
                    break;
                default:
                    break;

            }
        },
        beanCountValidate  : function(){
            var self =  this;
            var _beanCount = self.beanCount();
            if(/\D/g.test(_beanCount)){
                self.beanCount(self.initBeanCount);
            }
        },
        plusBean           : function(){
            var self = this;
            var _beanCount = +self.beanCount();
            if(_beanCount >= self.maxBeanCount()){
                return false;
            }
            self.beanCount(_beanCount + 1);
        },
        minusBean          : function(){
            var self = this;
            var _beanCount = +self.beanCount() || 0;
            if(_beanCount <= 0){
                self.beanCount(0);
                return false;
            }
            self.beanCount(_beanCount - 1);
        },
        loadBean : function(){
            var self = this;
            $.post("/teacher/new/homework/maxic.vpage", {
                clazzIds : self.clazzGroupIds.join(","),
                subject  : constantObj.subject
            }, function(data){
                var _dc = +data.dc || 0;
                self.initBeanCount = _dc;
                self.beanCount(_dc);
                self.maxBeanCount(+data.mc || 0);
                self.showBean(true);
            });
        },
        saveHomework      : function(element){
            var $element = $(element);
            if($element.hasClass("w-btn-disabled")){
                return false;
            }
            var self = this
                ,_comment = self.comment()
                ,commentLen = _comment.length;
            if(commentLen > 100 || commentLen < 0){
                _comment = "";
            }
            typeof self.termConfirmAssign === 'function' && self.termConfirmAssign($element,{
                remark      : _comment,
                duration    : self.totalTime(),
                startTime   : self.startDateTime(),
                endTime     : self.endDateTime()
            });

            $17.voxLog({
                module : "m_8NOEdAtE",
                op     : "popup_assign_confirmcontent_confirm_click",
                s0     : constantObj.subject
            });
        },
        run : function(){
            var self = this,_minDate = self.startDate();
            var saveHomeworkPopFn = function(){
                var node = document.getElementById("saveMathDialog");
                ko.cleanNode(node);
                ko.applyBindings(self, node);

                $("#endDateInput").datepicker({
                    dateFormat      : 'yy-mm-dd',
                    defaultDate     : _minDate,
                    numberOfMonths  : 1,
                    minDate         : _minDate,
                    maxDate         : null,
                    onSelect        : function(selectedDate){
                        self.endDateTime(selectedDate + " 23:59:59");
                        self.resetEndDateTime();
                    }
                });
                self.resetEndDateTime();
                self.loadBean();
            };
            var _whiteTabList = ["BASIC_APP","READING","PHOTO_OBJECTIVE","VOICE_OBJECTIVE"];  //只要包含其中一项，则不检查题量多少
            var _includeTabFlag = false; //是否包含白名单中的一项
            var _questionCount = 0;
            var _tabDetails = self.tabDetails() || [];
            for(var m = 0,mLen = _tabDetails.length; m < mLen; m++){
                _questionCount  +=  (+_tabDetails[m].count || 0);
                _includeTabFlag  = _includeTabFlag || (_whiteTabList.indexOf(_tabDetails[m].key) != -1);
            }

            var popState = {
                state0 : {
                    name    : 'arrangeHomework',
                    comment : '布置作业',
                    html    : template("T:TERM_REVIEW_CONFIRM",{}),
                    title   : '布置作业',
                    position: { width : 760},
                    focus   : 1,
                    buttons : {}
                },
                state1 : {
                    name : 'tooLittleTip',
                    comment:'布置应试题量过少',
                    title   : '系统提示',
                    focus  : 1,
                    position: { width : 500},
                    html : "您本次作业题量过少，继续布置将不会获得园丁豆！",
                    buttons: { "继续布置": true, "调整题目": false },
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        if(v){
                            $.prompt.goToState('arrangeHomework',false,function(){
                                saveHomeworkPopFn();
                            });
                        }else{
                            $.prompt.close();
                        }
                    }
                },
                state2 : {
                    name : 'noArrangeHomework',
                    comment:'没有选择任何题',
                    title   : '系统提示',
                    position: { width : 500},
                    focus  : 1,
                    html : "请选择作业内容",
                    buttons: { "确定": true},
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.close();
                    }
                },
                state3 : {
                    name : 'noClazzs',
                    comment:'没有选择班级',
                    title   : '系统提示',
                    position: { width : 500},
                    focus  : 1,
                    html : "请选择班级",
                    buttons: { "确定": true},
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.close();
                    }
                }
            };

            $.prompt(popState,{
                loaded : function(event){
                    if(!$.isArray(self.clazzNames()) || self.clazzNames().length == 0){
                        $.prompt.goToState('noClazzs');
                    }else if(_tabDetails.length == 0) {
                        $.prompt.goToState('noArrangeHomework');
                    }else if(!_includeTabFlag && _questionCount < 3){
                        $.prompt.goToState('tooLittleTip');
                    }else{
                        saveHomeworkPopFn();
                    }
                }
            });
        }
    };


    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getTermCarts : function(obj,termCartsResolve,termCartsReject){
            var termCarts = new TermCarts(obj,termCartsResolve,termCartsReject)
                ,nodes = obj.nodes || [];
            if(!$.isEmptyObject(obj) && nodes.length > 0){
                for(var n = 0,nLen = nodes.length; n < nLen; n++){
                    ko.applyBindings(termCarts, nodes[n]);
                }
            }
            return {
                addQuestion                 : termCarts.addQuestion.bind(termCarts),
                removeQuestion              : termCarts.removeQuestion.bind(termCarts),
                getQuestionsByHomeworkType  : termCarts.getQuestionsByHomeworkType.bind(termCarts),
                getPracticesAndBooks        : termCarts.getPracticesAndBooks.bind(termCarts),
                getAssignTotalTime          : function(){
                    return termCarts.totalSeconds();
                },
                clearCarts                  : termCarts.clearCarts.bind(termCarts)
            }
        },
        getTermConfirm : function(option, termConfirmAssign){
            return new TermConfirm(option, termConfirmAssign);
        }
    });
}(window,$17,constantObj));

(function(){
    var cartObj = {
        rootElement : "#ufo",
        cartPosition: function(){
            $(window).on('scroll', function() {
                var $cartElement = $(this.rootElement);

                var wHeight = $(window).height();
                var selfHeight = $cartElement.height();
                var top = parseInt((wHeight - selfHeight)/2);
                $cartElement.css('top',top + 'px');
                $cartElement.css('visibility','visible');
            }.bind(this));
        },
        cartWhere : function(){
            var $vUfo = $(this.rootElement);
            if(parseInt(($(window).width() - 1000)/2) >= 140 + 15){
                var left = parseInt(($(window).width() - 1000)/2) + 1000 + 15;
                $vUfo.css({'left':left+'px','right':'auto'});
            }else{
                $vUfo.css({'left':'auto','right':'0'});
            }
        },
        init : function(){
            $(window).on('resize',function(){
                this.cartWhere();
            }.bind(this));

            this.cartWhere();
        }
    };
    cartObj.init();
}());