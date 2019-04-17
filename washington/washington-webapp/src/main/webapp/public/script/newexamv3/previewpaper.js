/**
 *  路由地址：/newexamv2/previewpaper.vpage?paperIds=&sig=&app_key=
 */
$(function(){
    var constantObj = window.constantObj || {};
    var paperIds = $17.getQuery("paperIds");
    var sig = $17.getQuery("sig");
    var appKey = $17.getQuery("appKey") || "17Agent";

    //初始化题目渲染库
    Freya.init({
        isDev: ["staging","prod","test"].indexOf(constantObj.env) === -1,
        cheat : ["staging","test"].indexOf(constantObj.env) !== -1,
        cdnHost:constantObj.imgDomain
    }).then(function(){
        $17.info("Freya init completed");
    });

    function ExamHistory(){
        this.hsLoading = ko.observable(true);
        /*this.correctStartTime = ko.observable("");
        this.correctStopTime = ko.observable("");
        this.hasOral = ko.observable(false);
        this.newExamName = ko.observable("");*/
        this.paperCacheMap = {};
        this.newExamPaperInfos = ko.observableArray([]);
        this.currentPaperIndex = ko.observable(-1);
        this.themeForSubs = ko.observableArray([]);
        this.subQuestionNumberMap = {};
        this.questionIdsInPaper = []; //题目ID在试卷中的顺序
        this.freyaElem = null;
        this.init();
    }

    ExamHistory.prototype = {
        constructor : ExamHistory,
        init : function(){
            var self = this;
            $.get("/newexam/marketing/newpaperinfo.vpage",{
                paperIds  : paperIds,
                sig       : sig,
                appKey    : appKey
            }).done(function(res){
                if(res.success){
                    self.newExamPaperInfos(res.newExamPaperInfos);
                    self.changePaper(0);
                    self.initDomEvent();
                    self.hsLoading(false);
                }else{
                    $17.alert(res.info || '查询失败');
                }
            }).fail(function(jqXHR,textStatus,error){
                $17.alert(error);
            });
        },
        changePaper: function (index) {
            var self = this;
            var currentPaperIndexFn = self.currentPaperIndex;
            if(currentPaperIndexFn() === index){
                return false;
            }
            currentPaperIndexFn(index);
            self.getthemeForSubs();
        },
        getthemeForSubs: function () {
            var self = this;
            var paperIdArr = paperIds.split(",");
            var paperId = paperIdArr[self.currentPaperIndex()];
            if(self.paperCacheMap.hasOwnProperty(paperId)){
                self.analysisThemeForSubs(self.paperCacheMap[paperId]);
                return false;
            }
            $.get("/newexam/marketing/newpaper/questioninfo.vpage", {
                paperIds : paperIds,
                paperId  : paperId,   //self.newExamPaperInfos()[self.currentPaperIndex()].paperId
                sig      : sig,
                appKey   : appKey
            }, function (res) {
                if (res.success) {
                    self.paperCacheMap[paperId] = res;
                    self.analysisThemeForSubs(res);
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
            });
        },
        analysisThemeForSubs : function(res){
            var self = this;
            var questionIds = [];
            res.themeForSubs.forEach(function (item,mIndex) {
                item.questionList = [];
                var subQuestionsLen = item.subQuestions.length;
                item.subQuestions.forEach(function (squestion,index) {
                    var questionId = squestion.qid;
                    if (squestion.subIndex === 0) {
                        questionIds.push(questionId);
                    }
                    self.subQuestionNumberMap[questionId + "_" + squestion.subIndex] = $.extend(true,squestion,{
                        mePartTitle : $17.Arabia_To_SimplifiedChinese(mIndex + 1) + '、' + item.desc,
                        moduleNumber : (mIndex + 1),
                        moduleFirstSubQuestion : index === 0,
                        moduleLastSubQuestion : (index === subQuestionsLen - 1)
                    });
                });
            });
            self.questionIdsInPaper = [].concat(questionIds);
            self.getQuestionByIds(questionIds,function(data){
                if(data && data.success){
                    var questionCollect = data.result || [];
                    var questionEntityMap = {};
                    for(var m = 0,mLen = questionCollect.length; m < mLen; m++){
                        var questionObj = questionCollect[m];
                        questionEntityMap[questionObj.id] = questionObj;
                    }
                    self.questionEntityMap = questionEntityMap;
                    self.themeForSubs(res.themeForSubs);
                    var maxHeight = $(window).height() - 250;
                    $(".cardInner").css("maxHeight", maxHeight + "px");
                }else{
                    $17.alert("获取题目失败");
                }
            });
        },
        getQuestionByIds : function(questionIds,callback){
            $.get("/newexam/marketing/load/newquestion/byids.vpage", {
                data : JSON.stringify({
                    ids : questionIds,
                    containsAnswer : true
                }),
                paperIds  : paperIds,
                sig       : sig,
                appKey    : appKey
            }).done(callback).fail(callback);
        },
        createSubQuestionNumber : function(target,qid,subIndex){
            var self = this;
            var paperSubQuestion = self.subQuestionNumberMap[qid + "_" + subIndex];
            //创建题号html
            var iElement = document.createElement("i");
            iElement.innerText = paperSubQuestion.index;
            var className = iElement.className;
            iElement.className = className ? className + " " + "question-number" : "question-number";
            var fragment = document.createDocumentFragment();
            var subQuestionElem = target.subQuestion;
            subQuestionElem.id = "anchor_id_" + paperSubQuestion.index;
            subQuestionElem.style.cssText = "display:flex;align-items: flex-start;margin-top:10px;";
            subQuestionElem.insertBefore(iElement,subQuestionElem.children[0]);
            fragment.appendChild(subQuestionElem);
            fragment.appendChild(self.getAnswerAreaTarget(target,qid,subIndex));
            return fragment;
        },
        getAnswerAreaTarget : function(target,qid,subIndex){
            var self = this;
            var paperSubQuestion = self.subQuestionNumberMap[qid + "_" + subIndex];
            var answerAreaHtml = template("TPL_DESCRIPTION", {
                standardScore : paperSubQuestion.standardScore
            });
            var answerAreaDivElement = document.createElement("div");
            answerAreaDivElement.innerHTML = answerAreaHtml;
            answerAreaDivElement.children[0].appendChild(target.answer.container);
            var fragment = document.createDocumentFragment();
            fragment.appendChild(answerAreaDivElement);
            return fragment;
        },
        updateContentMainHeight : function(){
            document.getElementsByClassName("content-main")[0].style.height = this.freyaElem.offsetHeight + 150 + 'px';
            document.getElementById("J_paperContent").style.height =  this.freyaElem.offsetHeight + 150 + 'px';
        },
        loadQuestionContent: function () {
            var self = this;

            var questionIdsInPaper = self.questionIdsInPaper;
            var questionEntityMap = self.questionEntityMap;
            var questionObjects = [];
            for(var m = 0; m < questionIdsInPaper.length; m++){
                questionObjects.push(questionEntityMap[questionIdsInPaper[m]]);
            }

            var moduleElem,questionListContainerElem;
            var testData = {
                container: "#J_paperContent",
                renderOptions: {
                    showAnswerAnalysis : true,
                    state: 1,
                    active : false,
                    showAudioOperation : true
                },
                questions: questionObjects,
                onFreyaUpdated : function(freya){
                    freya.contentContainer.style.overflow = "visible";
                    freya.questionContainer.style.width = "100%";
                    self.freyaElem = freya.contentContainer;
                },
                onQuestionRender : function(target,question){
                    var qid = question.id;
                    target.container.style = "margin-left:20px;";
                    target.questionContainer.style="margin-top:10px;";
                    target.description.style = "margin-top:20px;";
                    var paperSubQuestion = self.subQuestionNumberMap[qid + "_" + 0];

                    //题目容器
                    var questionContainerElem = document.createElement("div");
                    questionContainerElem.style.cssText = "position:relative;";
                    questionContainerElem.appendChild(target.container);

                    if(paperSubQuestion.moduleFirstSubQuestion){
                        //模块标题
                        var moduleTitleElem = document.createElement("h5");
                        moduleTitleElem.innerHTML = paperSubQuestion.mePartTitle;
                        moduleTitleElem.className = "sub-title";
                        //模块题目列表容器
                        questionListContainerElem = document.createElement("div");
                        questionListContainerElem.appendChild(questionContainerElem);

                        moduleElem = document.createElement("div");
                        moduleElem.className = "subject-type";
                        moduleElem.appendChild(moduleTitleElem);
                        moduleElem.appendChild(questionListContainerElem);
                        return moduleElem;
                    }else{
                        questionListContainerElem.appendChild(questionContainerElem);
                    }
                },
                onSubQuestionRender : function(target,subQuestion,question){
                    //复合题回调
                    var subIndex = question.content.subContents.indexOf(subQuestion);
                    if(subIndex === -1){
                        return false;
                    }
                    var qid = question.id;
                    var targetTemp = self.createSubQuestionNumber(target,qid,subIndex);

                    if(questionIdsInPaper[questionIdsInPaper.length - 1] === qid && ((question.content.subContents.length - 1) === subIndex)){
                        $17.info("最后一道题");
                        //最后一道题
                        setTimeout(function(){
                            self.updateContentMainHeight();
                            moduleElem = null;
                            questionListContainerElem = null;
                        },1000);
                    }
                    return targetTemp;
                }
            };
            Freya.render(testData);
            return "";
        },
        questionNoClick: function (qid, index, subIndex) {
            $('html,body').animate({scrollTop: $("#anchor_id_" + index).offset().top},'slow');
            $17.voxLog({
                module: "m_yJO2o3u3",
                op: "o_ZOWfqcqv",
                s0: this.examId,
                s1: this.newExamPaperInfos()[this.currentPaperIndex()].paperId,
                s2: index
            });
        },
        initDomEvent: function () {
            var self = this;
            $(document).on("click", ".teach-gotop", function () {
                $('html,body').animate({scrollTop: 0});
            });
            $(window).scroll(function () {
                var scrollTop = $(this).scrollTop(); //滚动条距离顶部的高度
                var top = 100 - scrollTop < -100 ? -100 : 100 - scrollTop;
                $(".h-answerCard").css("top", top);
                var maxHeight = $(window).height() - 150 - top;
                $(".cardInner").css("maxHeight", maxHeight + "px");
            });
        }
    };
    ko.applyBindings(new ExamHistory(),document.getElementById("newexamv2Root"));
});