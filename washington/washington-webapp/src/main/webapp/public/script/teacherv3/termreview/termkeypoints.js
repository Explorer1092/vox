(function(window,$17,constantObj,undefined){

    var ADD_OPERATOR = "ADD",REMOVE_OPERATOR = "REMOVE";

    function TermKeyPoints(obj,termCarts){
        var self = this;
        self.termType               = obj.type;
        self.homeworkType           = obj.homeworkType;
        self.termCarts              = termCarts || {};
        self.loading                = ko.observable(true);
        self.info                   = ko.observable("");
        self.packageList            = ko.observableArray([]);
        self.currentPackage         = ko.observable({});
        self.currentPackageQuestions= ko.observableArray([]);
        self.currentPackagePageNo   = ko.observable(1); //当前包所处的页码
        self.currentPackageLoding   = ko.observable(true);
        self.pageSize               = 5;
        self.silderObj              = {};
        self.focusExamList          = ko.pureComputed(function(){
            var pageNo = self.currentPackagePageNo()
                ,startIndex = (pageNo - 1) * self.pageSize
                ,endIndex = pageNo * self.pageSize;
            return self.currentPackageQuestions.slice(startIndex, endIndex);
        },self);
        self.currentPackage.subscribe(self.setCurrentPackageQuestions,self);

        self.packageQuestionsMap = {};
        self.questionHandles = {};//查看答案与解析

        self.termPage = null; //分页对象

    }
    TermKeyPoints.prototype = {
        constructor : TermKeyPoints,
        run : function(option){
            var self = this
                ,paramData = {
                    subject      : constantObj.subject,
                    type         : option.type,
                    bookId       : option.bookId,
                    unitIds      : option.unitIds.join(","),
                    clazzGroupId : option.clazzGroupId
                };
            self.loading(true);
            $.get("/teacher/termreview/content.vpage", paramData, function(data){
                if(data.success && data.content && data.content.length > 0){
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
                                    videoDuration   : packageObj.seconds || 0,
                                    bookId          : book.bookId || null,
                                    unitId          : packageObj.unitId || null
                                });
                            });
                            if(packages.length > 0){
                                self.packageList(ko.mapping.fromJS(packages)());
                                self.currentPackage(ko.mapping.fromJS(packages[0]));
                            }else{
                                self.packageList([]);
                                self.currentPackage(null);
                            }
                            break;
                        }
                    }

                    self.setTermPage();
                    self.loading(false);
                    self.initSlider();
                }else{
                    self.packageList([]);
                    self.currentPackage(null);
                    self.setTermPage();
                    self.loading(false);
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

        initSlider : function () {
            if (this.packageList().length <= 3){
                return false;
            }
            var type = this.termType;
            new SimpleSlider({
                slideName: "J_" + type,
                clickLeftId: "#left-" + type,
                clickRightId: "#right-" + type,
                slideContainer: "#container-" + type,
                slideItem: ".slideItem",
                slideCount: 3,
                itemWidth: "241",
                totalCount: this.packageList().length
            });
        },

        getVideoQuestionsMap : function(){
            var self = this
                ,videoQuestionsMap = {} //{videoId : [questionId,questionId,....]}
                ,getQuestionsByHomeworkType = self.termCarts.getQuestionsByHomeworkType;
            if(typeof getQuestionsByHomeworkType === 'function'){
                var videoContents = getQuestionsByHomeworkType(self.homeworkType) || [];
                for(var m = 0,mLen = videoContents.length; m < mLen; m++){
                    var questions = videoContents[m].questions || []
                        ,questionIds = [];
                    for(var t = 0,tLen = questions.length; t < tLen; t++){
                        questionIds.push(questions[t].questionId);
                    }
                    videoQuestionsMap[videoContents[m].videoId] = questionIds;
                }
            }
            return videoQuestionsMap;
        },
        setCurrentPackageQuestions : function(){
            //当题包改变，重新设置
            var self = this,currentPage = self.currentPackage();//TermKeyPoints
            self.currentPackageLoding(true);
            if(!currentPage){
                self.currentPackagePageNo(1); //当前包所处的页码
                self.currentPackageQuestions([]);
                return false;
            }
            var questions = self.packageQuestionsMap[currentPage.videoId()] || [];
            self.currentPackagePageNo(1); //当前包所处的页码
            self.currentPackageQuestions(ko.mapping.fromJS(questions)());
            self.currentPackageLoding(false);
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
        playVideoLog   : function(element){
            $(element).attr("data-playst") === "stop" && $17.voxLog({
                module : "m_8NOEdAtE",
                op     : "key_points_btn_play",
                s0     : constantObj.subject
            });
            $(element).attr("data-playst","play");
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
        viewExamAnswer : function(self){
            var question = this
                ,_questionId = question.id()
                ,renderQuestion = self.questionHandles[_questionId] || {};
            typeof renderQuestion.showAnalysis === 'function'
            && renderQuestion.showAnalysis(_questionId);
        },
        loadExamImg : function(examId,index){
            var self = this;
            if(!$17.isBlank(examId) && constantObj.examInitComplete){
                var $mathExamImg = $("#mathExamImg" + index);
                $mathExamImg.empty();
                $("<div style='overflow-x: auto;overflow-y: hidden;'></div>").attr("id","examImg-" + index).appendTo($mathExamImg);
                var node = document.getElementById("examImg-" + index)
                    ,renderQuestion = vox.exam.render(node, 'normal', {
                        ids       : [examId],
                        imgDomain : constantObj.imgDomain,
                        env       : constantObj.env,
                        domain    : constantObj.domain
                    });
                !self.questionHandles.hasOwnProperty(examId) && (self.questionHandles[examId] = renderQuestion);
            }else{
                $("#mathExamImg" + index).html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
            }
            return "";
        },
        internalAddOrRemoveExam : function(operator,selectQuestions,callback){
            var self = this
                ,currentPackage = self.currentPackage()
                ,videoId = currentPackage.videoId()
                ,content = {
                    type        : self.homeworkType,
                    videoId     : currentPackage.videoId(),
                    bookId      : currentPackage.bookId(),
                    unitId      : currentPackage.unitId(),
                    questions   : []
                }
                ,addQuestionFn = self.termCarts.addQuestion
                ,removeQuestionFn = self.termCarts.removeQuestion;

            if(!$.isArray(selectQuestions) || selectQuestions.length == 0){
                return false;
            }
            //获取选过的题目ID
            var videoQuestionsMap = ((operator === "ADD") ? self.getVideoQuestionsMap() : {})
                ,newAddQuestions = []
                ,repeatCount = 0;
            for(var t = 0,tLen = selectQuestions.length; t < tLen; t++){
                var questionId = selectQuestions[t].id
                    ,existsInCarts = false;
                if(newAddQuestions.indexOf(questionId) != -1){
                    continue;
                }

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

                if((!existsInCarts && operator === ADD_OPERATOR) || operator === REMOVE_OPERATOR){
                    newAddQuestions.push(questionId);
                    content.questions.push({
                        questionId     : questionId,
                        seconds        : selectQuestions[t].seconds,
                        assignTimes    : selectQuestions[t].assignTimes,
                        difficulty     : selectQuestions[t].difficulty,
                        difficultyName : selectQuestions[t].difficultyName,
                        questionType   : selectQuestions[t].questionType,
                        answerType     : selectQuestions[t].answerType,
                        submitWay      : selectQuestions[t].submitWay
                    });
                }
            }
            operator === ADD_OPERATOR
            && content.questions.length > 0
            && typeof addQuestionFn === 'function'
            && addQuestionFn(content);

            operator === REMOVE_OPERATOR
            && content.questions.length > 0
            && typeof removeQuestionFn === "function"
            && removeQuestionFn(content);

            typeof callback === 'function' && callback({
                repeatCount     : repeatCount,
                newAddQuestions : newAddQuestions
            });
        },
        checkedAllExam : function(){
            var self = this
                ,videoId = self.currentPackage().videoId()
                ,operator = (self.currentPackage().selCount() >= self.currentPackage().totalCount()) ? REMOVE_OPERATOR : ADD_OPERATOR
                ,questions = self.packageQuestionsMap[videoId] || []
                ,questionChecked = (operator === REMOVE_OPERATOR);
            self.internalAddOrRemoveExam(operator,questions,function(data){
                (operator === ADD_OPERATOR && data.repeatCount > 0) && $17.alert("与已选题目重复");

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
                    if(data.newAddQuestions.indexOf(questions[m].id) != -1){
                        questions[m]["checked"] = (operator === ADD_OPERATOR);
                    }
                }
                self.packageQuestionsMap[videoId] = questions;
            });

            $17.voxLog({
                module : "m_8NOEdAtE",
                op     : "final_review_select_all",
                s0     : constantObj.subject,
                s1     : self.termType,
                s2     : operator === REMOVE_OPERATOR ? "cancel" : "select"
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
                (operator === ADD_OPERATOR && data.repeatCount > 0) && $17.alert("与已选题目重复");

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
            });
        }
    };


    $17.termreview = $17.termreview || {};
    $17.extend($17.termreview, {
        getKey_points : function(obj,termCarts){
            return new TermKeyPoints(obj,termCarts);
        }
    });
}(window,$17,constantObj));