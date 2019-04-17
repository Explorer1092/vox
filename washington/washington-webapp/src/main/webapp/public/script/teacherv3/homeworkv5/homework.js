$(function(){
    addTextcutEvent();
    constantObj._homeworkContent = {//保存布置作业
        homeworkType:"Normal",
        des: "",
        startTime: "",
        endTime: "",
        clazzIds: "",
        subject : null,
        extData : null,
        remark: "",
        duration:0,
        prize   :0,
        practices: {
            EXAM: {
                questions: []
            },
            WORD_PRACTICE:{
                questions: []
            },
            NEW_READ_RECITE:{
                apps:[]
            },
            KEY_POINTS : {
                apps : []
            },
            INTERESTING_PICTURE :{
                questions :[]
            },
            UNIT_QUIZ: {
                questions: []
            },
            MID_QUIZ: {
                questions: []
            },
            END_QUIZ: {
                questions: []
            },
            LISTEN_PRACTICE : {
                questions: []
            },
            BASIC_KNOWLEDGE : {
                questions : []
            },
            CHINESE_READING : {
                questions : []
            },
            INTELLIGENCE_EXAM : {
                questions : []
            },
            FALLIBILITY_QUESTION : {
                questions : []
            },
            PHOTO_OBJECTIVE : {
                questions : []
            },
            VOICE_OBJECTIVE : {
                questions : []
            },
            MENTAL_ARITHMETIC : {
                timeLimit : 0,
                questions : [],
                recommendKpPostQuestionsMap:[]
            },
            READ_RECITE_WITH_SCORE:{
                apps:[]
            },
            LEVEL_READINGS:{
                apps: []
            },
            INTELLIGENT_TEACHING:{
                questions : []
            },
            OCR_MENTAL_ARITHMETIC : {
                apps : []
            },
            WORD_RECOGNITION_AND_READING : {
                apps : []
            },
            CALC_INTELLIGENT_TEACHING : {
                questions : []
            },
            WORD_TEACH_AND_PRACTICE : {
                apps : []
            }
        }
    };

    constantObj._reviewQuestions = {
        EXAM: [],
        WORD_PRACTICE :[],
        KEY_POINTS:[],
        INTERESTING_PICTURE :[],
        UNIT_QUIZ: [],
        MID_QUIZ: [],
        END_QUIZ: [],
        LISTEN_PRACTICE : [],
        BASIC_KNOWLEDGE : [],
        CHINESE_READING : [],
        INTELLIGENCE_EXAM : [],
        FALLIBILITY_QUESTION : [],
        NEW_READ_RECITE:[],
        PHOTO_OBJECTIVE : [],
        VOICE_OBJECTIVE : [],
        MENTAL_ARITHMETIC : [],
        READ_RECITE_WITH_SCORE :[],
        LEVEL_READINGS:[],
        INTELLIGENT_TEACHING : [],
        OCR_MENTAL_ARITHMETIC : [],
        WORD_RECOGNITION_AND_READING : [],
        CALC_INTELLIGENT_TEACHING : [],
        WORD_TEACH_AND_PRACTICE : []
    };
    constantObj._moduleSeconds = {
        EXAM: 0,
        KEY_POINTS : 0,
        INTERESTING_PICTURE :0,
        UNIT_QUIZ: 0,
        MID_QUIZ: 0,
        END_QUIZ: 0,
        LISTEN_PRACTICE : 0,
        WORD_PRACTICE  : 0,
        BASIC_KNOWLEDGE : 0,
        CHINESE_READING : 0,
        INTELLIGENCE_EXAM : 0,
        FALLIBILITY_QUESTION : 0,
        NEW_READ_RECITE:0,
        PHOTO_OBJECTIVE:0,
        VOICE_OBJECTIVE:0,
        MENTAL_ARITHMETIC : 0,
        READ_RECITE_WITH_SCORE:0,
        LEVEL_READINGS:0,
        INTELLIGENT_TEACHING : 0,
        OCR_MENTAL_ARITHMETIC : 0,
        WORD_RECOGNITION_AND_READING : 0,
        CALC_INTELLIGENT_TEACHING : 0,
        WORD_TEACH_AND_PRACTICE : 0
    };

    var mathCarts = $17.homeworkv3.getCarts();
    var book = null;
    var objectiveTabs = null;
    var homeworkTypeTabs = null;
    var tabContentMap = {};  //tab下的类对象
    var reviewHomeworkObj = null;
    var objectiveConfigType  = {
            EXAM                    : "t:EXAM",
            KEY_POINTS              : "t:KEY_POINTS",
            INTERESTING_PICTURE     : "t:EXAM",
            UNIT_QUIZ               : "t:UNIT_QUIZ",
            MID_QUIZ                : "t:UNIT_QUIZ",
            END_QUIZ                : "t:UNIT_QUIZ",
            LISTEN_PRACTICE         : "t:EXAM",
            WORD_PRACTICE           : "t:EXAM",
            // NEW_READ_RECITE         : "t:NEW_READ_RECITE",   已经废弃
            BASIC_KNOWLEDGE         : "t:INTELLIGENCE_EXAM",
            CHINESE_READING         : "t:EXAM",
            INTELLIGENCE_EXAM       : "t:INTELLIGENCE_EXAM",
            FALLIBILITY_QUESTION    : "t:FALLIBILITY_QUESTION",
            PHOTO_OBJECTIVE         : "t:PHOTO_OBJECTIVE",
            VOICE_OBJECTIVE         : "t:VOICE_OBJECTIVE",
            MENTAL_ARITHMETIC       : "t:MENTAL_ARITHMETIC",
            READ_RECITE_WITH_SCORE  : "t:READ_RECITE_WITH_SCORE",
            LEVEL_READINGS          : "t:LEVEL_READINGS",
            INTELLIGENT_TEACHING    : "t:INTELLIGENT_TEACHING",
            OCR_MENTAL_ARITHMETIC   : "t:OCR_MENTAL_ARITHMETIC",
            WORD_RECOGNITION_AND_READING : "t:WORD_RECOGNITION_AND_READING",
            CALC_INTELLIGENT_TEACHING : "t:CALC_INTELLIGENT_TEACHING",
            WORD_TEACH_AND_PRACTICE : "t:WORD_TEACH_AND_PRACTICE"
    };

    var levelAndclazzs = $17.homeworkv3.getLevels(mathCarts);
    levelAndclazzs.extendLevelClick = extendLevelClick;
    levelAndclazzs.initialise({
        batchclazzs : constantObj.batchclazzs,
        hasStudents : constantObj.hasStudents,
        clazzClickCb  : function(){
            //班级change时触发回调
            constantObj._homeworkContent.clazzIds = levelAndclazzs.checkedClazzGroupIds().join(",");
            mathCarts.resetClazzGroups(levelAndclazzs.getCheckedGroups());
            objectiveTabs.refresh();
        }
    });
    ko.applyBindings(levelAndclazzs, document.getElementById('level_and_clazzs'));

    //年级click扩展
    function extendLevelClick(obj){
        if(book == null){
            book = $17.homeworkv3.getBook();
            book.extendSectionClick = extendSectionClick;
            ko.applyBindings(book, document.getElementById('bookInfo'));
        }
        constantObj._homeworkContent.clazzIds = levelAndclazzs.checkedClazzGroupIds().join(",");
        mathCarts.resetClazzGroups(levelAndclazzs.getCheckedGroups());
        //清空作业内容
        $(".J_delete",".J_UFOInfo").trigger("click");
        //作业形式内容清空
        $("#tabContent").empty();

        book.initialise($.extend(obj,{
            term : constantObj.term
        }));
    }

    //课时click扩展
    function extendSectionClick(obj){
        $("#tabContent").empty();
        if(objectiveTabs == null){
            objectiveTabs = $17.homeworkv3.getObjectiveTabs();
            objectiveTabs.extendTabClick = extendObjectiveTabClick;
            ko.applyBindings(objectiveTabs, document.getElementById('objectiveTabs'));
        }
        objectiveTabs.initialise($.extend(true,{},obj));
        objectiveTabs.run();
    }

    function extendObjectiveTabClick(obj){
        $("#J_HomeworkWay").show();
        $17.backToTop(500);
        if(homeworkTypeTabs == null){
            homeworkTypeTabs = $17.homeworkv3.getHomeworkTypeTabs();
            homeworkTypeTabs.extendTabClick = extendTabClick;
            ko.applyBindings(homeworkTypeTabs, document.getElementById('homeworkTypeTabs'));
        }
        homeworkTypeTabs.initialise($.extend(true,{},obj));
        homeworkTypeTabs.run();
    }


    //作业形式click扩展
    function extendTabClick(obj){
        var _tabType = obj.tabType;
        $17.voxLog({
            module: "Newhomework_assign_" + constantObj.subject,
            op : _tabType + "_tab_click"
        });

        $17.voxLog({
            module: "m_H1VyyebB",
            op : "ah_topicpage_show",
            s0 : constantObj.subject,
            s1 : _tabType,
            s2 : obj.objectiveTabType
        });

        var $tabContent = $("#tabContent");
        $tabContent.empty();
        var getTabType = "get" + _tabType.slice(0,1) + _tabType.slice(1).toLocaleLowerCase();
        var fn = $17.homeworkv3[getTabType];
        var elementId;
        var tabContent;
        if(typeof fn === 'function') {
            $("<div></div>").attr("id",obj.tabType).attr("data-bind","template:{'name':'" + objectiveConfigType[obj.tabType]  + "'}").appendTo($tabContent);
            if(tabContentMap[obj.tabType]){
                tabContent = tabContentMap[obj.tabType];
            }else{
                tabContent = fn.apply(null, []);
                tabContentMap[obj.tabType] = tabContent;
            }
            elementId = obj.tabType;
        }else{
            tabContent = $17.homeworkv3.getDefault();
            $("<div></div>").attr("id","default").attr("data-bind","template:{'name':'t:default'}").appendTo($tabContent);
            elementId = "default";
        }
        tabContent.initialise($.extend(true,{
            carts            : mathCarts,
            subject          : constantObj.subject,
            env              : constantObj.env
        },obj));
        tabContent.run({
            clazzGroupIdsStr : constantObj._homeworkContent.clazzIds
        });
        var node = document.getElementById(elementId);
        ko.cleanNode(node);
        ko.applyBindings(tabContent, node);
    }

    $("#saveHomworkBtn").on("click",function(){
        //循环选择的作业
        var _practices = constantObj._homeworkContent.practices,
            _tabDetails = [],
            _whiteTabList = ["OCR_MENTAL_ARITHMETIC","WORD_RECOGNITION_AND_READING","NEW_READ_RECITE","LEVEL_READINGS","READ_RECITE_WITH_SCORE","PHOTO_OBJECTIVE","VOICE_OBJECTIVE","WORD_TEACH_AND_PRACTICE"],
            _includeTabFlag = false,
            _questionCount = 0,
            groupNames = mathCarts.getGroupDuration(levelAndclazzs.getCheckedGroups());

        for(var tab in _practices){
            if(_practices.hasOwnProperty(tab)){
                var _newContents = _practices[tab].questions || _practices[tab].apps;
                _newContents = $.isArray(_newContents) ? _newContents : [];
                if(_newContents.length > 0){
                    var _obj = {
                        tabType   : tab,
                        tabName   : $("span.name","p[type='" + tab + "']").text(),
                        assignCnt : $("span.count","p[type='" + tab + "']").text()
                    };
                    _tabDetails.push(_obj);
                    var tabQuestionCnt = 0;
                    switch (tab){
                        case "KEY_POINTS":
                            $.each(_newContents,function(i,ct){
                                tabQuestionCnt += ($.isArray(ct.questions) ? ct.questions.length : 0);
                            });
                            break;
                        default:
                            tabQuestionCnt = _newContents.length;
                    }
                    _questionCount += tabQuestionCnt;
                    _includeTabFlag = _includeTabFlag || (_whiteTabList.indexOf(tab) != -1);
                }
            }
        }

        var clazzGroupIds = levelAndclazzs.checkedClazzGroupIds();
        var newClazzGroupIds = [];
        var newClazzNames = [];
        if($.isArray(groupNames) && groupNames.length > 0){
            for(var s = 0,sLen = groupNames.length; s < sLen; s++){
                var groupObj = groupNames[s],seconds = (+groupObj.seconds || 0);
                if(seconds && seconds > 0){
                    for(var m = 0,mLen = clazzGroupIds.length; m < mLen; m++){
                        var clazzGroupIdStr = clazzGroupIds[m];
                        if(clazzGroupIdStr.indexOf("_" + groupObj.groupId) != -1){
                            newClazzGroupIds.push(clazzGroupIdStr);
                            break;
                        }
                    }
                    newClazzNames.push(groupObj);
                }
            }
        }

        constantObj._homeworkContent.clazzIds = newClazzGroupIds.join(",");

        var saveHomeworkPopFn = function(){
            var node = document.getElementById("saveMathDialog");
            ko.cleanNode(node);
            var confirmModule = $17.homeworkv3.getConfirmModule();
            confirmModule.initialise({
                clazzNames    : newClazzNames,
                startDateTime : constantObj.currentDateTime,
                endDate       : constantObj.endDate,
                nowEndTime    : constantObj.endTime,
                tabDetails    : _tabDetails
            });
            ko.applyBindings(confirmModule, node);

            $17.voxLog({
                module: "m_H1VyyebB",
                op    : "popup_assign_confirmcontent_show",
                s0    : constantObj.subject
            });
        };

        var popState = {
            state0 : {
                name    : 'arrangeHomework',
                comment : '布置作业',
                html    : template("t:confirm",{}),
                title   : '布置作业',
                position: { width : 760},
                focus   : 1,
                buttons : {}
            },
            state1 : {
                name : 'tooLittleTip',
                comment:'确认布置这些题目吗？',
                title   : '系统提示',
                focus  : 1,
                position: { width : 500},
                html : "确认布置这些题目吗？",
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

        $17.voxLog({
            module: "m_H1VyyebB",
            op    : "page_assign_cart_assign_click",
            s0    : constantObj.subject
        });

        $.prompt(popState,{
            loaded : function(event){
                if(clazzGroupIds.length === 0){
                    $.prompt.goToState('noClazzs');
                }else if(_tabDetails.length === 0) {
                    $.prompt.goToState('noArrangeHomework');
                }else if(!_includeTabFlag && _questionCount < 3){
                    $.prompt.goToState('tooLittleTip');
                }else{
                    saveHomeworkPopFn();
                }
            }
        });

    });

    $("#previewBtn").on("click",function(){
        var $this = $(this);
        if($this.hasClass("preview")){
            var _reviewQuestions = constantObj._reviewQuestions;
            var contents = {};
            for(var tab in _reviewQuestions){
                if(_reviewQuestions.hasOwnProperty(tab) && _reviewQuestions[tab].length > 0){
                    contents[tab] = _reviewQuestions[tab];
                }
            }
            if($.isEmptyObject(contents)){
                $17.alert("请选择内容");
                return false
            }

            $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_cart_preview_click",
                s0 : constantObj.subject
            });

            $("#arrangehomework").hide();
            $this.removeClass("preview");
            $this.text("返回调整");
            $17.backToTop(1000);
            if(!reviewHomeworkObj){
                reviewHomeworkObj = new homeworkReview();
            }
            reviewHomeworkObj.initialise();
        }else{
            $("#arrangehomework").show();
            reviewHomeworkObj.hide();
            $this.text("预览");
            $this.addClass("preview");
            objectiveTabs && objectiveTabs.refresh();
        }
    });

    $(".J_UFOInfo p").hover(function(e){
        $(e.currentTarget).addClass("hover");
        var count = $(e.currentTarget).find(".count").html();
        if(parseInt(count)>0){
            $(e.currentTarget).find(".h-set-icon-delete").removeClass("h-set-icon-deleteGrey").addClass("h-set-icon-deleteRed");
        }else{
            $(e.currentTarget).find(".h-set-icon-delete").removeClass("h-set-icon-deleteRed").addClass("h-set-icon-deleteGrey");
        }
    },function(e){
        $(e.currentTarget).removeClass("hover");
    });

    $(".J_UFOInfo").on("click",".J_delete",function(e){
        var item = $(e.currentTarget).parents("p");
        if(item.find(".count").html()==0)return false;

        var type = item.attr("type");
        item.find(".count").html("0");

        constantObj._moduleSeconds[type] = 0;
        var totalTime = 0;
        for(var z in constantObj._moduleSeconds){
            if(constantObj._moduleSeconds.hasOwnProperty(z)){
                totalTime += constantObj._moduleSeconds[z];
            }
        }
        var $assignTotaltime = $("#assignTotalTime");
        $assignTotaltime.html(Math.ceil(totalTime/60));
        constantObj._reviewQuestions[type] = [];

        if(constantObj._homeworkContent.practices[type].hasOwnProperty("questions")){
            constantObj._homeworkContent.practices[type].questions = [];
        }

        if(constantObj._homeworkContent.practices[type].hasOwnProperty("apps")){
            constantObj._homeworkContent.practices[type].apps = [];
        }

        $17.voxLog({
            module: "m_H1VyyebB",
            op    : "page_assign_cart_empty_click",
            s0    : constantObj.subject,
            s1    : self.tabType
        });

        var tabObj = tabContentMap[type];

        if($("#reviewhomework:visible")[0]){
            ((["MENTAL_ARITHMETIC","OCR_MENTAL_ARITHMETIC"].indexOf(type) !== -1) && tabObj && tabObj.clearAll());
            reviewHomeworkObj.removeHomeworkModules(type);
        }else{
            if(tabObj){
                if(["EXAM","INTERESTING_PICTURE","WORD_PRACTICE","MENTAL_ARITHMETIC","OCR_MENTAL_ARITHMETIC"].indexOf(type) !== -1){
                    tabObj.clearAll();
                }else{
                    tabObj.run({
                        clazzGroupIdsStr : constantObj._homeworkContent.clazzIds
                    });
                }
            }
        }
    });

    $(window).on("scroll",function(){
        var $objectiveTabs = $("#objectiveTabs");
        var $jHomeworkWay = $("#J_HomeworkWay");
        if($(window).scrollTop() > $("#hkTabcontent").offset().top){
            $objectiveTabs.addClass("h-fixHeader");
            //把浮动的高度填充上，不然会出现闪屏
            $jHomeworkWay.css("margin-top","117px");
        }else{
           $objectiveTabs.removeClass("h-fixHeader");
            $jHomeworkWay.css("margin-top","0px");
        }
    });

});

(function($17,ko){
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

    var ConfirmModule = function(){
        var self = this;
        self.clazzNames     = ko.observableArray([]);
        self.startDateTime  = ko.observable("");  //yyyy-MM-dd 00:00:00
        self.startDate      = ko.pureComputed(function(){
            return self.startDateTime().substring(0,10);
        });
        self.endDateTime    = ko.observable($17.DateUtils("%Y-%M-%d 23:59:59"));
        self.endLabel       = ko.observable("zero"); // 结束时间label项,zero:今天, one:明天, two:三天,custom : 自定义
        self.endDateInput   = ko.pureComputed(function(){
            return self.endDateTime().substring(0,10);
        });
        self.displayEndDate = ko.pureComputed(function(){
            var _endDateTime = self.endDateTime();
            var timeArr = _endDateTime.split(/:|-|\s/g);
            var endDate = new Date(timeArr[0], timeArr[1] - 1, timeArr[2], timeArr[3], timeArr[4], timeArr[5]);
            return self.endDateTime() + self.getWeekName(endDate.getDay());
        });
        self.nowEndTime = "23:59";
        self.hourSelect = ko.observableArray(h);
        self.focusHour = ko.observable("23");
        self.minSelect = ko.observableArray(m);
        self.focusMin  = ko.observable("59");
        self.focusHour.subscribe(function(newValue){
            self.endLabel("custom");
            self.endDateTime(self.endDateInput() + " " + newValue + ":" + self.focusMin() + ":59");
            self.resetEndMin();
        },self);
        self.focusMin.subscribe(function(newValue){
            self.endLabel("custom");
            self.endDateTime(self.endDateInput() + " " + self.focusHour() + ":" + newValue + ":59");
        },self);

        self.comment = ko.observable("").extend({ maxlength: 100 });
        self.tabDetails = ko.observableArray([]);
        self.initBeanCount = 0;
        self.beanCount = ko.observable(0);
        self.maxBeanCount = ko.observable(0);
        self.showBean = ko.observable(false);
        self.beanCount.subscribe(self.beanCountValidate,self);
        self.maxDurationMinutes = 0; //0:不限制时间
        self.overTimeGids = [];
        self.limitTime = 0;   //当overTimeGids有值时，不能超过limitTime时间阈值
    };
    ConfirmModule.prototype = {
        constructor       : ConfirmModule,
        getUnitOfMeasure  : function(tabType){
            var ut = "";
            switch (tabType){
                case "READING":
                    ut = "本";
                    break;
                case "BASIC_APP":
                    ut = "个";
                    break;
                case "KNOWLEDGE_REVIEW":
                    ut = "班";
                    break;
                case "FALLIBILITY_QUESTION":
                    ut = "班";
                    break;
                case "WORD_TEACH_AND_PRACTICE":
                    ut = "题包";
                    break;
                default:
                    ut = "道";
            }
            return ut;
        },
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
        resetEndMin  : function(){
            var self = this,newEndTimeArr = self.splitDateTime(self.endDateTime());
            var _startDate = self.startDate();
            var _endDate = self.endDateTime().substring(0,10);
            var defaultMin = "00";
            if(_startDate === _endDate){
                var sdtArr = self.splitDateTime(self.nowEndTime);
                defaultMin = (sdtArr[0] === newEndTimeArr[3] ? sdtArr[1] : defaultMin);
            }
            var _mArr = self.getTimeArray(m,defaultMin);
            self.minSelect(_mArr);
            var _lastMin = _mArr[0];
            self.focusMin(_lastMin);
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
                    var startTimeArr = self.splitDateTime(_startDateTime);
                    var refDate = new Date(startTimeArr[0], startTimeArr[1] - 1, startTimeArr[2], startTimeArr[3], startTimeArr[4], startTimeArr[5]);
                    self.endDateTime($17.DateUtils("%Y-%M-%d 23:59:59",day,"d",refDate));
                    self.resetEndDateTime();
                    break;
                default:
                    break;

            }
            $17.voxLog({
                module: "Newhomework_assign_" + $uper.subject.key,
                op : "Modify_the_deadline"
            });
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
            $.post("/teacher/new/homework/maxic.vpage", {clazzIds : constantObj._homeworkContent.clazzIds, subject : constantObj.subject}, function(data){
                var _dc = +data.dc || 0;
                self.initBeanCount = _dc;
                self.beanCount(_dc);
                self.maxBeanCount(+data.mc || 0);
                self.showBean(true);
                self.maxDurationMinutes = (+data.maxDurationMinutes || 0);
                self.overTimeGids = $.isArray(data.overTimeGids) ? data.overTimeGids : [];
                self.limitTime = data.limitTime || 0;
            });
        },
        checkMaxDurationMinstes : function(element){
            var self = this;
            var maxSeconds = 0;
            var durations = ko.mapping.toJS(self.clazzNames());
            for(var m = 0,mLen = durations.length; m < mLen; m++){
                var totalSeconds = (+durations[m].seconds || 0);
                (totalSeconds > maxSeconds) && (maxSeconds = totalSeconds);
            }
            if(self.maxDurationMinutes != 0 && maxSeconds >= self.maxDurationMinutes * 60){
                var popState = {
                    state0 : {
                        name    : 'maxDurationMinutes',
                        comment : '布置作业',
                        html    : "<div class='w-ag-center'>建议作业时长不要超过" + self.maxDurationMinutes + "分钟</div>",
                        title   : '布置作业',
                        position: { width : 450},
                        focus   : 1,
                        buttons: { "继续布置": true, "返回调整": false },
                        submit  : function(e,v,m,f){
                            e.preventDefault();
                            $17.voxLog({
                                module : "m_H1VyyebB",
                                op     : "popup_25min_button_click",
                                s0     : constantObj.subject,
                                s1     : v ? "继续布置":"返回调整"
                            });
                            if(v){
                                self._saveHomework(element);
                            }else{
                                $.prompt.close();
                            }
                        }
                    }
                };
                $.prompt(popState,{
                    loaded : function(event){
                        $17.voxLog({
                            module : "m_H1VyyebB",
                            op     : "popup_25min_show",
                            s0     : constantObj.subject
                        });
                    }
                });
            }else{
                var limitTime = self.limitTime;
                var limitClazzNames = [];
                var overTimeGids = self.overTimeGids;
                if(overTimeGids.length > 0){
                    var clazzDurationMap = {};
                    $.each(overTimeGids,function(j,clazz){
                        clazzDurationMap[clazz.groupId] = (+clazz.duration || 0);
                    });
                    $.each(durations,function(i,clazz){
                        var sum = (+clazz.seconds || 0) + (+clazzDurationMap[clazz.groupId] || 0);
                        (sum > limitTime) && (limitClazzNames.push(clazz.groupName));
                    });
                    if(limitClazzNames.length > 0 && limitTime > 0){
                        var clazzNamesTotal = limitClazzNames.length;
                        limitClazzNames = limitClazzNames.slice(0,2);
                        var joinLimitClazzNames = limitClazzNames.join("、");
                        var limitPopState = {
                            state0 : {
                                name    : 'multiSubjectDurationLimit',
                                comment : '布置作业',
                                html    : "<div class='w-ag-center'>" + (clazzNamesTotal > 2 ? (joinLimitClazzNames + "等" + clazzNamesTotal + "个班级") : joinLimitClazzNames) + "已经有其它科目的作业正在进行，若继续布置，学生的作业时长将大于" + Math.ceil(limitTime/60) + "分钟</div>",
                                title   : '布置作业',
                                position: { width : 450},
                                focus   : 1,
                                buttons: { "继续布置": true, "返回调整": false },
                                submit  : function(e,v,m,f){
                                    e.preventDefault();
                                    if(v){
                                        self._saveHomework(element);
                                    }else{
                                        $.prompt.close();
                                    }
                                }
                            }
                        };
                        $.prompt(limitPopState,{
                            loaded : function(event){}
                        });
                    }else{
                        self._saveHomework(element);
                    }
                }else{
                    self._saveHomework(element);
                }
            }
        },
        saveHomework      : function(element){
            var self = this;
            self.checkMaxDurationMinstes(element);
        },
        _saveHomework      : function(element){
            var $element = $(element);
            if($element.hasClass("w-btn-disabled")){
                return false;
            }
            var self = this;
            var _comment = self.comment();
            var commentLen = _comment.length;
            if(commentLen > 100 || commentLen < 0){
                _comment = "";
            }

            var _homeworkContent = constantObj._homeworkContent;
            _homeworkContent.prize     = self.beanCount();
            _homeworkContent.remark    = _comment;
            _homeworkContent.subject   = constantObj.subject;
            _homeworkContent.extData   = constantObj.extData;
            _homeworkContent.duration  = 600;
            _homeworkContent.durations = ko.mapping.toJS(self.clazzNames());
            _homeworkContent.startTime = self.startDateTime();
            _homeworkContent.endTime   = self.endDateTime();
            $(window).unbind('beforeunload');
            $element.addClass("w-btn-disabled");
            App.postJSON("/teacher/new/homework/assign.vpage", _homeworkContent, function(data){
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op    : "popup_assign_confirmcontent_show",
                    s0    : constantObj.subject,
                    s1    : _homeworkContent.prize,
                    s2    : _homeworkContent.endTime,
                    s3    : data.success ? "SUCCESS" : "FAILURE",
                    s4    : data.info || ""
                });
                if(data.success){
                    if(data.homeworkType === "YiQiXue"){
                        window.opener=null;
                        window.open(constantObj.ucenterUrl + '/sso/logout.vpage','_self');
                        window.close();
                        return;
                    }
                    var forwardHwList = function(){
                        location.href = "/teacher/new/homework/report/list.vpage?subject=" + constantObj.subject;
                    };
                    $17.alert("布置成功",forwardHwList);
                    // $17.homeworkv3 && $17.homeworkv3.teacherAppPopup("推荐成功","m_H1VyyebB","popup_assign_confirmcontent_close_click",forwardHwList,constantObj.subject);
                }else{
                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/assign.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(constantObj._homeworkContent),
                        s3     : $uper.env
                    });
                    $17.alert(data.info);
                }

                $element.removeClass("w-btn-disabled");
            }, function(){
                $17.alert("请求失败，请确认网络情况再重试");
                $element.removeClass("w-btn-disabled");
            });
        },
        initialise        : function(option){
            var self = this;
            option = option || {};
            self.clazzNames(option.clazzNames || []);
            var _startDateTime = option.startDateTime;
            self.startDateTime(_startDateTime);
            self.nowEndTime = option.nowEndTime;

            self.tabDetails(option.tabDetails);
            var _minDate = option.startDateTime.substring(0,10);
            self.endDateTime(_minDate + " 23:59:59");
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
        }
    };
    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getConfirmModule: function(){
            return new ConfirmModule();
        }
    });
}($17,ko));


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