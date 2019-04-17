(function(window,$17,constantObj,undefined){
    "use strict";
    ko.bindingHandlers.singleExamHover = {
        init: function(element, valueAccessor){
            $(element).hover(
                function(){
                    $(element).addClass("current");
                    $(element).find("a.feedback").show();
                    $(element).find("a.viewExamAnswer").show();
                },
                function(){
                    var _value = ko.unwrap(valueAccessor());
                    if(!_value){
                        $(element).removeClass("current");
                    }
                    $(element).find("a.feedback").hide();
                    $(element).find("a.viewExamAnswer").hide();
                }
            );
        },
        update:function(element, valueAccessor){
            var _value = ko.unwrap(valueAccessor());
            if(_value){
                $(element).addClass("current");
            }else{
                $(element).removeClass("current");
            }
        }
    };

    var ADD_OPERATOR = "ADD",REMOVE_OPERATOR = "REMOVE";

    function KeyPoints(){
        var self = this;
        self.tabType                = "";
        self.examLoading            = ko.observable(true); //正在加载应试
        self.carts                  = null;
        self.loading                = ko.observable(true);
        self.info                   = ko.observable("");
        self.packageList            = ko.observableArray([]);
        self.currentPackage         = ko.observable({});
        self.currentPackageQuestions= ko.observableArray([]);
        self.currentPackagePageNo   = ko.observable(1); //当前包所处的页码
        self.currentPackageLoding   = ko.observable(true);
        self.pageSize               = 5;
        self.silderObj              = {};
        self.focusExamMap = {};
        self.focusExamList          = ko.pureComputed(function(){
            var pageNo = self.currentPackagePageNo()
                ,startIndex = (pageNo - 1) * self.pageSize
                ,endIndex = pageNo * self.pageSize;
            return self.currentPackageQuestions.slice(startIndex, endIndex);
        },self);
        self.currentPackage.subscribe(self.setCurrentPackageQuestions,self);

        self.packageQuestionsMap = {};

        self.termPage = null; //分页对象
        self.param = {};  //外部参数
    }
    KeyPoints.prototype = {
        constructor : KeyPoints,
        getQuestion : function(examId){
            var self = this;
            var questionObj = self.focusExamMap[examId];
            if(!questionObj){
                return 	[];
            }
            var questions = questionObj.questions;
            if(!$.isArray(questions) || questions.length === 0){
                return [];
            }
            return questions.slice(0,1);
        },
        run : function(option){
            var self = this
                ,paramData = {
                subject      : constantObj.subject,
                type         : self.tabType,
                bookId       : self.param.bookId,
                unitId       : self.param.unitId,
                clazzGroupId : option.clazzGroupIdsStr,
                objectiveConfigId : self.param.objectiveConfigId
            };
            $.get("/teacher/new/homework/objective/content.vpage", paramData, function(data){
                if(data.success){
                    if(data.content && data.content.length > 0){
                        for(var k = 0,kLen = data.content.length; k < kLen; k++){
                            var contentObj = data.content[k];
                            if(contentObj.type == "package"){
                                var packages = [],
                                    videoQuestionsMap = self.getVideoQuestionsMap();
                                $.each(contentObj.packages,function(i,packageObj){
                                    var videoId = packageObj.videoId
                                        ,selectQuestionIds = videoQuestionsMap[videoId] || []
                                        ,questions = $.isArray(packageObj.questions) ? packageObj.questions : []
                                        ,book = packageObj.book || {}
                                        ,totalSeconds = 0;
                                    $.each(questions,function(j,question){
                                        question["checked"] = (selectQuestionIds.indexOf(question.id) != -1);
                                        totalSeconds += (+question.seconds || 0);
                                    });
                                    self.packageQuestionsMap[videoId] = questions;
                                    packages.push({
                                        videoId         : videoId,
                                        videoName       : packageObj.videoName,
                                        videoUrl        : packageObj.videoUrl,
                                        coverUrl        : packageObj.coverUrl,
                                        videoSummary    : packageObj.videoSummary,
                                        solutionTracks  : packageObj.solutionTracks,
                                        selCount        : selectQuestionIds.length || 0,
                                        totalCount      : questions.length,
                                        totalSeconds    : totalSeconds,
                                        videoDuration   : packageObj.videoSeconds || 0,
                                        book            : book
                                    });
                                });
                                if(packages.length > 0){
                                    self.packageList(ko.mapping.fromJS(packages)());
                                    self.currentPackage(ko.mapping.fromJS(packages[0]));
                                    $17.voxLog({
                                        module : "m_H1VyyebB",
                                        op     : "page_key_points_detail",
                                        s0     : constantObj.subject
                                    });
                                }else{
                                    self.packageList([]);
                                    self.currentPackage(null);
                                }
                                break;
                            }
                        }
                        self.setTermPage();
                    }else{
                        self.packageList([]);
                        self.currentPackage(null);
                    }
                    self.loading(false);
                }else{
                    (data.errorCode !== "200") && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/termreview/content.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : constantObj.env
                    });
                }
            });
        },
        getVideoQuestionsMap : function(){
            var self = this
                ,videoQuestionsMap = {}; //{videoId : [questionId,questionId,....]}

            var videoContents = constantObj._homeworkContent.practices[self.tabType].apps || [];
            for(var m = 0,mLen = videoContents.length; m < mLen; m++){
                var questions = videoContents[m].questions || []
                    ,questionIds = [];
                for(var t = 0,tLen = questions.length; t < tLen; t++){
                    questionIds.push(questions[t].questionId);
                }
                videoQuestionsMap[videoContents[m].videoId] = questionIds;
            }
            return videoQuestionsMap;
        },
        setCurrentPackageQuestions : function(){
            //当题包改变，重新设置
            var self = this,currentPage = self.currentPackage();//KeyPoints
            self.currentPackageLoding(true);
            if(!currentPage){
                self.currentPackagePageNo(1); //当前包所处的页码
                self.currentPackageQuestions([]);
                return false;
            }
            var questions = self.packageQuestionsMap[currentPage.videoId()] || [];

            var questionIds = [];
            if(questions.length > 0){
                for(var m = 0,mLen = questions.length; m < mLen; m++){
                    questionIds.push(questions[m].id);
                }
            }
            $17.QuestionDB.getQuestionByIds(questionIds,function(result){
                self.focusExamMap = result.success ? result.questionMap : {};
                self.currentPackagePageNo(1); //当前包所处的页码
                self.currentPackageQuestions(ko.mapping.fromJS(questions)());
                self.currentPackageLoding(false);
            });
        },
        setTermPage   : function(){
            var self = this,totalPage = (self.currentPackage() != null ? Math.ceil(self.currentPackage().totalCount()/self.pageSize) : 0);
            if(!self.termPage){
                self.termPage = new $17.pagination.initPages({
                    totalPage   : totalPage,
                    pageClickCb : self.page_click.bind(self)
                });
            }else{
                self.termPage.setPage(undefined,totalPage);
            }
        },
        changePackage : function(element,self){
            if(!$(element).hasClass("active")){
                $(element).addClass("active").siblings().removeClass("active");
                self.currentPackage(this);
                self.setTermPage();
            }
        },
        getVideoFlashVars : function(embedContainerElem){
            var self = this
                ,videoUrl = self.currentPackage() ? self.currentPackage().videoUrl() : null
                ,videoConverUrl = self.currentPackage() ? self.currentPackage().coverUrl() : ""
                ,$embedContainerElem = $(embedContainerElem)
                ,htmlStr = '<embed width="438" height="300" flashvars="file=' + videoUrl + '&amp;image=' + videoConverUrl + '&amp;width=438&amp;height=300" allowfullscreen="true" quality="high" wmode="opaque" windowlessVideo=1 value="transparent" src="' + constantObj.flashPlayerUrl + '" type="application/x-shockwave-flash">';
            $embedContainerElem.html(htmlStr);
            return "1";
        },
        page_click     : function(currentPage){
            //翻页回调
            var self = this;
            self.currentPackagePageNo(currentPage);
        },
        viewExamAnswer : function(self,index){
            var question = this
                ,_questionId = question.id();

            var gameUrl = "/teacher/new/homework/viewquestion.vpage?" + $.param({qids:_questionId});
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
        },
        internalAddOrRemoveExam : function(operator,selectQuestions,callback){
            var self = this,param = self.param || {}
                ,currentPackage = self.currentPackage()
                ,videoId = currentPackage.videoId()
                ,videoEntity = {
                    type        : self.tabType,
                    videoId     : currentPackage.videoId(),
                    book        : ko.mapping.toJS(currentPackage.book),
                    questions   : [],
                    objectiveId : param.objectiveTabType
                };

            if(!$.isArray(selectQuestions) || selectQuestions.length === 0){
                return false;
            }
            //获取选过的题目ID
            var newAddQuestions = []
                ,repeatCount = 0;
            var totalSeconds = 0;
            var videoIndex = -1,videoArr = constantObj._homeworkContent.practices[self.tabType].apps || [];
            for(var k = 0,kLen = videoArr.length; k < kLen; k++){
                if(videoArr[k].videoId == videoId){
                    videoIndex = k;
                    break;
                }
            }
            for(var t = 0,tLen = selectQuestions.length; t < tLen; t++){
                var questionId = selectQuestions[t].id;
                if(newAddQuestions.indexOf(questionId) != -1){
                    continue;
                }

                if(operator === ADD_OPERATOR){
                    var existsInCarts = false;
                    var videoQuestionsMap = self.getVideoQuestionsMap();
                    for(var p in videoQuestionsMap){
                        if(videoQuestionsMap.hasOwnProperty(p)){
                            var questions = videoQuestionsMap[p];
                            existsInCarts = existsInCarts || questions.indexOf(questionId) != -1;
                            if(existsInCarts && p != videoId){
                                repeatCount++;
                                break;
                            }
                        }
                    }
                    if(!existsInCarts){
                        newAddQuestions.push(questionId);
                        var newQuestion = {
                            questionId     : questionId,
                            seconds        : selectQuestions[t].seconds || 0,
                            answerType     : selectQuestions[t].answerType,
                            submitWay      : selectQuestions[t].submitWay
                        };
                        if(videoIndex == -1){
                            videoEntity.questions.push(newQuestion);
                            constantObj._homeworkContent.practices[self.tabType].apps.push(videoEntity);
                            videoIndex = constantObj._homeworkContent.practices[self.tabType].apps.length - 1;
                        }else{
                            constantObj._homeworkContent.practices[self.tabType].apps[videoIndex]["questions"].push(newQuestion);
                        }
                        totalSeconds += newQuestion.seconds;
                        constantObj._reviewQuestions[self.tabType].push(selectQuestions[t]);
                    }
                }else{
                    newAddQuestions.push(questionId);
                    if(videoIndex != -1){
                        var questionArr = constantObj._homeworkContent.practices[self.tabType].apps[videoIndex]["questions"];
                        var questionIndex = -1;
                        for(var m = 0,mLen = questionArr.length; m < mLen; m++){
                            if(questionArr[m].questionId == questionId){
                                questionIndex = m;
                                break;
                            }
                        }
                        questionArr.splice(questionIndex,1);
                        if(questionArr.length == 0){
                            videoIndex = -1;
                            constantObj._homeworkContent.practices[self.tabType].apps.splice(videoIndex,1)
                        }
                        for(var s = 0,_reviewQuestions = constantObj._reviewQuestions[self.tabType],sLen = _reviewQuestions.length; s < sLen; s++){
                            if(questionId == _reviewQuestions[s]["id"]){
                                _reviewQuestions.splice(s,1);
                                break;
                            }
                        }
                        totalSeconds -= (selectQuestions[t].seconds || 0);
                    }
                }
            }
            //更新购物车状态
            constantObj._moduleSeconds[self.tabType] += totalSeconds;
            self.carts
            && typeof self.carts["recalculate"] === 'function'
            && self.carts.recalculate(self.tabType,constantObj._reviewQuestions[self.tabType].length);


            typeof callback === 'function' && callback({
                repeatCount     : repeatCount,
                newAddQuestions : newAddQuestions
            });
        },
        checkedAllExam : function(){
            var self = this
                ,videoId = self.currentPackage().videoId()
                ,operator = (self.currentPackage().selCount() >= self.currentPackage().totalCount()) ? REMOVE_OPERATOR : ADD_OPERATOR
                ,questions = self.packageQuestionsMap[videoId] || [];
            self.internalAddOrRemoveExam(operator,questions,function(data){
                (operator === ADD_OPERATOR && data.repeatCount > 0) && $17.alert("有" + data.repeatCount + "道题与已选题目重复");

                var selCount = self.currentPackage().selCount()
                    ,newSelCount = selCount + (operator === REMOVE_OPERATOR ? 0 - data.newAddQuestions.length : data.newAddQuestions.length);
                self.currentPackage().selCount(newSelCount);
                ko.utils.arrayForEach(self.packageList(),function(packageObj){
                    (packageObj.videoId() === videoId) && packageObj.selCount(newSelCount);
                });
                ko.utils.arrayForEach(self.currentPackageQuestions(),function(questionObj){
                    data.newAddQuestions.indexOf(questionObj.id()) != -1 && questionObj.checked(operator === ADD_OPERATOR);
                });
                for(var m = 0,mLen = questions.length; m < mLen; m++){
                    questions[m]["checked"] = (operator === ADD_OPERATOR && (data.newAddQuestions.indexOf(questions[m].id) != -1));
                }
                self.packageQuestionsMap[videoId] = questions;
            });

            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "key_points_btn_selectAll",
                s0     : constantObj.subject,
                s1     : operator === REMOVE_OPERATOR ? "cancel" : "select"
            });
        },
        addOrRemoveExam : function(self,element,questionIndex){
            var question = this
                ,videoId = self.currentPackage().videoId()
                ,questionChecked = question.checked()
                ,operator = (question.checked() ? REMOVE_OPERATOR : ADD_OPERATOR);
            self.internalAddOrRemoveExam(operator,[ko.mapping.toJS(question)],function(data){
                (operator === ADD_OPERATOR && data.repeatCount == 0)
                && $(element).closest(".examTopicBox").fly({
                    target  : ".J_UFOInfo",
                    border  : "5px #39f solid",
                    time    : 600
                });
                if(operator === ADD_OPERATOR && data.repeatCount > 0){
                    $17.alert("与已选题目重复");
                }else{
                    question.checked(!questionChecked);
                    var positionIndex = (self.currentPackagePageNo() - 1) * self.pageSize + questionIndex
                        ,selCount = self.currentPackage().selCount()
                        ,newSelCount = (!questionChecked ? (selCount + 1) : (selCount - 1));
                    self.currentPackage().selCount(newSelCount);
                    ko.utils.arrayForEach(self.packageList(),function(packageObj){
                        (packageObj.videoId() === videoId) && packageObj.selCount(newSelCount);
                    });
                    self.currentPackageQuestions()[positionIndex]["checked"](!questionChecked);
                    self.packageQuestionsMap[videoId][positionIndex]["checked"] = !questionChecked;
                }
            });

            $17.voxLog({
                module : "m_H1VyyebB",
                op     : "key_points_btn_select",
                s0     : constantObj.subject,
                s1     : operator === REMOVE_OPERATOR ? "cancel" : "select",
                s2     : question.id()
            });
        },
        initialise        : function(option){
            var self = this;
            option = option || {};
            self.param = option;
            self.tabType = option.tabType; //必传字段
            self.carts = option.carts || null;

            //初始化
            var $ufoexam = $("p[type='" + self.tabType +"']",".J_UFOInfo");
            if($ufoexam.has("span").length == 0){
                $ufoexam.empty().html(template("t:UFO_EXAM",{tabTypeName : option.tabTypeName,count : 0}));
            }
        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getKey_points : function(){
            return new KeyPoints();
        }
    });
}(window,$17,constantObj));