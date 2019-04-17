(function(window,$17,constantObj,undefined){
    "use strict";
    //当前页面需要应试信息，只初始化一次
    try{
        vox.exam.create(function(data){
            if(!data.success){
                $17.voxLog({
                    module  : 'vox_exam_create',
                    op      : 'create_error'
                });
            }else{
                constantObj.examInitComplete = true;
            }
        },false,{
            imgDomain : constantObj.imgDomain,
            env       : constantObj.env,
            domain    : constantObj.domain
        });
    }catch(exception){
        constantObj.examInitComplete = false;
        $17.voxLog({
            module      : 'vox_exam_create',
            op          : 'examCoreJs_error',
            errMsg      : exception.message,
            userAgent   : (window.navigator && window.navigator.userAgent) ? window.navigator.userAgent : "No browser information"
        });
    }
    //作业TAB对应的模板
    var objectiveConfigType  = {
        BASIC_WORD			: "T:BASIC_WORD",
        BASIC_SENTENCE		: "T:BASIC_WORD",
        BASIC_CALCULATION 	: "T:BASIC_WORD",
        BASIC_READ_RECITE_WITH_SCORE : "T:BASIC_WORD",
        WORD                : "T:TERM_EXAM",
        GRAMMAR				: "T:TERM_EXAM",
        LISTENING 			: "T:TERM_EXAM",
        READING 			: "T:TERM_EXAM",
        EN_NATION_ERROR 	: "T:TERM_EXAM",
        EN_CLAZZ_ERROR		: "T:TERM_EXAM",
        UNIT_PAPER 			: "T:TERM_EXAM",
        UNIT_KEY_POINTS 	: "T:TERM_EXAM",
        MATH_NATION_ERROR 	: "T:TERM_EXAM",
        NUMBER				: "T:TERM_EXAM",
        GEOMETRY			: "T:TERM_EXAM",
        STATISTICS			: "T:TERM_EXAM",
        PRACTICE			: "T:TERM_EXAM",
        MATH_CLAZZ_ERROR	: "T:TERM_EXAM",
        FINAL_PAPER 		: "T:TERM_EXAM",
        CH_WORD 			: "T:TERM_EXAM",
        APPLICATION 		: "T:TERM_EXAM",
        CH_READING 			: "T:TERM_EXAM",
        CH_UNIT_PAPER 		: "T:TERM_EXAM",
        CH_FINAL_PAPER 		: "T:TERM_EXAM",
        UNIT_DIAGNOSIS      : "t:INTELLIGENT_TEACHING"
    };

    function TermReview(){
        var self = this;
        self.levelsAndBook  = null;
        self.termTabs       = null;
        self.termTabContent = null;
        self.tabContentMap  = {};  //tab对应的具体实现对象
        self.termCarts      = null;
        self.termConfirm    = null;
    }
    TermReview.prototype = {

        constructor : TermReview,

        run : function(){
            var self = this;

            !self.termCarts && (self.termCarts = $17.termreview.getTermCarts({
                nodes           : document.getElementsByClassName("assignAndPreview"),
                termCartsAssign : self.termCartsAssign.bind(self),
                previewBtnClick : self.previewBtnClick.bind(self),
                backAdjustClick : self.backAdjustClick.bind(self),
                deleteTypeDataCb: self.deleteTypeData.bind(self)
            }));

            if(!self.levelsAndBook){
                self.levelsAndBook = $17.termreview.getLevelsAndBook({
                    subject : constantObj.subject
                },self.levelsAndBookResolve.bind(self),self.levelsAndBookReject.bind(self),self.termCarts);
                ko.applyBindings(self.levelsAndBook, document.getElementById("levelsAndBook"));
            }
            self.levelsAndBook.run();
        },
        levelsAndBookResolve : function(data){
            var self = this;
            $("#J_hkTabcontent").show();
            if(!self.termTabs){
                self.termTabs = $17.termreview.getTermTabs(undefined, self.termTabsResolve.bind(self),self.termTabsReject.bind(self));
                ko.applyBindings(self.termTabs,document.getElementById("termTabs"));
            }

            self.termTabs.run($.extend(data,{
                success : true,
                bookId  : self.levelsAndBook.getBookId(),
                clazzGroupIds : self.levelsAndBook.getClazzGroupId()
            }));
        },
        levelsAndBookReject  : function(data){
            var self = this;
            if(data.errorCode === "NO_CLAZZ"){
                $("#J_hkTabcontent").hide();
            }else{
                $("#termTabContent").empty();
                if(!self.termTabs){
                    self.termTabs = $17.termreview.getTermTabs(undefined, self.termTabsResolve.bind(self),self.termTabsReject.bind(self));
                    ko.applyBindings(self.termTabs,document.getElementById("termTabs"));
                }
                self.termTabs.run($.extend(data,{
                    success : true,
                    bookId  : self.levelsAndBook.getBookId(),
                    clazzGroupIds : self.levelsAndBook.getClazzGroupId()
                }));
            }
        },
        termTabsResolve : function(data){
            var self = this,
                termTabContent  = self.termTabContent,
                type            = data.type,
                homeworkType    = data.homeworkType,
                $termTabContent = $("#termTabContent"),
                getHomeworkType = "getExam",
                elementId;

            switch(objectiveConfigType[type]){
                case "T:BASIC_WORD":
                    getHomeworkType = "getBasic_word";
                    break;
                case "t:INTELLIGENT_TEACHING":
                    getHomeworkType = "getIntelligent_teaching";
                    break;
                default:
                    break;
            }
            var fn = $17.termreview[getHomeworkType];

            $17.backToTop(500);
            $termTabContent.empty();
            if(typeof fn === 'function') {
                $("<div></div>").attr("id",type).attr("data-bind","template:{'name':'" + objectiveConfigType[type]  + "'}").appendTo($termTabContent);
                if(self.tabContentMap[type]){
                    termTabContent = self.tabContentMap[type];
                }else{
                    termTabContent = fn.apply(null, [{type : type, homeworkType : homeworkType},self.termCarts,self.levelsAndBook]);
                    self.tabContentMap[type] = termTabContent;
                }
                elementId = type;
            }else{
                termTabContent = $17.homeworkv3.getDefault();
                $("<div></div>").attr("id","default").attr("data-bind","template:{'name':'t:default'}").appendTo($termTabContent);
                elementId = "default";
            }
            termTabContent.run($.extend(true,{
                bookId       : self.levelsAndBook.getBookId(),
                bookName     : self.levelsAndBook.getBookName(),
                clazzGroupId : self.levelsAndBook.getClazzGroupId(),
                homeworkType : homeworkType
            },data));

            $17.voxLog({
                module : "m_8NOEdAtE",
                op     : "content_load",
                s0     : constantObj.subject,
                s1     : data.type
            });
            var node = document.getElementById(elementId);
            ko.cleanNode(node);
            ko.applyBindings(termTabContent, node);
        },
        termTabsReject : function () {

            $("#termTabContent").empty();
        },
        termCartsAssign : function(data){
            var self = this;
            self.termConfirm = $17.termreview.getTermConfirm({
                clazzGroupIds    : [self.levelsAndBook.getClazzGroupId()],
                clazzNames      : [self.levelsAndBook.checkedClazzName()],
                startDateTime   : constantObj.currentDateTime,
                endDate         : constantObj.endDate,
                nowEndTime      : constantObj.endTime,
                tabDetails      : data.tabDetails,
                totalTime       : data.totalTime
            },self.termConfirmAssign.bind(self));
            self.termConfirm.run();
        },
        previewBtnClick   : function(){
            $("#J_assignLevelBookPanel").hide();
            $("#J_hkTabcontent").hide();
            $("#J_previewPanel").show();
        },
        backAdjustClick   : function(){
            var self = this,termTabs = self.termTabs;
            $("#J_previewPanel").hide();
            $("#J_assignLevelBookPanel").show();
            $("#J_hkTabcontent").show();
            termTabs != null
            && typeof termTabs.refresh === 'function'
            && self.termTabs.refresh();
        },
        deleteTypeData    : function(homeworkType){
            var self = this;
            self.termTabs != null
            && self.termTabs.focusHomeworkType() == homeworkType
            && $.isFunction(self.termTabs.refresh)
            && self.termTabs.refresh();
        },
        termConfirmAssign : function($element,subContent){
            var self = this;
            $element.addClass("w-btn-disabled");
            //确认窗口布置回调
            var practicesAndBooks = self.termCarts.getPracticesAndBooks();
            var _homeworkContent = $.extend(true,{},subContent,{
                homeworkType : "TermReview",
                homeworkTag  : "Next_TermReview",
                clazzIds     : self.levelsAndBook.getClazzGroupId(),
                subject      : constantObj.subject,
                practices    : practicesAndBooks[0],
                books        : practicesAndBooks[1]
            });

            App.postJSON("/teacher/new/homework/assign.vpage", _homeworkContent, function(data){
                if(data.success){
                    setTimeout(function(){
                        location.href = "/teacher/new/homework/report/list.vpage?subject=" + constantObj.subject;
                    },200);
                }else{
                    $17.alert(data.info);
                    $17.voxLog( {
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/assign.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(_homeworkContent)
                    });
                }
                $element.removeClass("w-btn-disabled");
            }, function(){
                $17.alert("请求失败，请确认网络情况再重试");
                $element.removeClass("w-btn-disabled");
                $17.voxLog( {
                    module : "API_REQUEST_ERROR",
                    op     : "API_STATE_ERROR",
                    s0     : "/teacher/new/homework/assign.vpage",
                    s1     : "请求失败，请确认网络情况再重试",
                    s2     : $.toJSON(_homeworkContent)
                });
            });
        }
    };

    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getTermReview: function(){
            return new TermReview();
        }
    });
}(window,$17,constantObj));