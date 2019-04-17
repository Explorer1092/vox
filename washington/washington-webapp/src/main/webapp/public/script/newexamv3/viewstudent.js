;(function () {
    Freya.init({
        isDev: ["staging","prod","test"].indexOf(constantObj.env) === -1,
        cheat : ["staging","test"].indexOf(constantObj.env) !== -1,
        cdnHost:constantObj.imgDomain
    }).then(function(){
        $17.info("Freya init completed");
    });

    var subject = constantObj.subject;
    var StudentDetail = function () {
        this.webLoading = ko.observable(true);
        this.subject = subject;
        this.examId = $17.getQuery("examId");
        this.userId = $17.getQuery("userId");
        this.from = $17.getQuery("from");
        this.questionMap = {};
        this.themeForSubs = ko.observableArray([]);
        this.score = ko.observable("");
        this.newExamName = ko.observable("");
        this.gradeType = ko.observable("");
        this.embedRank = ko.observable("");
        this.userName = ko.observable("");
        this.playIndex = 0;
        this.audioList = [];
        this.questionEntityMap = {};  //题目完整信息字段
        this.subQuestionNumberMap = {}; //每道小题在试卷的题号映射 ,题ID + 小题在大题中的下标为key
        this.questionIdsInPaper = []; //题目ID在试卷中的顺序
        this.freyaElem = null;
        this.showExam = ko.observable(null); //tab栏目具体展示-试卷详情 考试报告？  //examDetails : 考试报告 ,examReport : 试卷详情
        this.initMethod = false;  //是否调用过init方法
        this.changeNav(this.subject === "MATH" ? "examDetails" : "examReport");

    };

    StudentDetail.prototype = {

        constructor: StudentDetail,

        init: function () {

            this.getPaperDetail();
            this.initDomEvent();
            this.initAudioPlayer();
        },
        getPaperDetail: function () {
            var self = this;
            $.get("/container/report/paperstudentanswer.vpage", {
                userId: self.userId,
                examId: self.examId
            }, function (res) {
                if (res.success) {
                    self.questionMap = res.questionMap;
                    self.newExamName(res.newExamName);
                    self.gradeType(res.gradeType);
                    self.embedRank(res.embedRank);
                    self.userName(res.userName);
                    self.score(res.score);
                    self.webLoading(false);
                    self.getthemeForSubs();
                    $17.voxLog({
                        module: "m_yJO2o3u3",
                        op: "o_417j3O0r",
                        s0: self.examId,
                        s1: self.userId,
                        s2: self.gradeType() == 0 ? self.score() : self.embedRank(),
                        s3: self.from
                    },(self.from === "student_history" ? "student" : "teacher"));
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
            });
        },
        getthemeForSubs: function () {
            var self = this;
            $.get("/container/report/newpaperquestioninfov1.vpage", {
                userId: self.userId,
                examId: self.examId
            }, function (res) {
                if (res.success) {
                    var questionIds = [];
                    res.themeForSubs.forEach(function (item,mIndex) {
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
                        })
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
                        }else{
                            $17.alert("获取题目失败");
                        }
                    });

                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
            });
        },
        getQuestionByIds : function(questionIds,callback){
            $.get("/exam/flash/load/newquestion/byids.vpage", {
                data : JSON.stringify({
                    ids : questionIds,
                    containsAnswer : true
                })
            }).done(callback).fail(callback);
        },
        questionNoClick: function (qid, index) {
            $('html,body').animate({scrollTop: $("#anchor_id_"+index).offset().top},'slow');
        },
        getQuestionNoClass: function (qid, subIndex) {
            var iconClass = "";
            if (this.questionMap[qid] && this.questionMap[qid].subQuestions[subIndex]) {
                if (this.questionMap[qid].subQuestions[subIndex].examQuestionType === "ORAL") {
                    if(this.questionMap[qid].subQuestions[subIndex].voiceUrlList.length > 0)
                    {
                        iconClass = "sk-blue";
                    }else{
                        iconClass = "sk-red";
                    }
                }else if (this.questionMap[qid].subQuestions[subIndex].grasp) {
                    iconClass = "sk-green";
                }else {
                    iconClass = "sk-red";
                }
            } else {
                iconClass = "sk-green";
            }
            return iconClass;
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
        updateContentMainHeight : function(){
            document.getElementsByClassName("h-mk-answer")[0].style.height = this.freyaElem.offsetHeight + 150 + 'px';
            document.getElementById("J_paperContent").style.height =  this.freyaElem.offsetHeight + 150 + 'px';
        },
        getAnswerAreaTarget : function(target,qid,subIndex,subQuestion){
            var self = this;
            var questionAnswer = self.questionMap[qid];
            var subQuestionAnswer = questionAnswer.subQuestions[subIndex];
            subQuestionAnswer.endCorrect = self.endCorrect;  //应用于口语
            subQuestionAnswer.gradeType = self.gradeType();
            subQuestionAnswer.userName = self.userName();
            subQuestionAnswer.from = self.from;
            var examQuestionType = subQuestionAnswer.examQuestionType;
            var tmpl = "tmpl_" + examQuestionType;
            var answerAreaHtml = template(tmpl, subQuestionAnswer);
            var answerAreaDivElement = document.createElement("div");
            answerAreaDivElement.innerHTML = answerAreaHtml;
            answerAreaDivElement.children[0].appendChild(target.answer.container);
            var fragment = document.createDocumentFragment();
            fragment.appendChild(answerAreaDivElement);
            return fragment;
        },
        loadQuestionContent: function () {
            var self = this;

            var questionAnswers = [];
            var subGrasps = [];
            var questionIdsInPaper = self.questionIdsInPaper;
            var questionEntityMap = self.questionEntityMap;
            var questionAnswerMap = self.questionMap;
            var questionObjects = [];
            for(var m = 0; m < questionIdsInPaper.length; m++){
                questionAnswers.push(questionAnswerMap[questionIdsInPaper[m]].userAnswer);
                subGrasps.push(questionAnswerMap[questionIdsInPaper[m]].subGrasp);
                questionObjects.push(questionEntityMap[questionIdsInPaper[m]]);
            }
            var moduleElem,questionListContainerElem;
            var testData = {
                container: "#J_paperContent",
                renderOptions: {
                    showAnswerAnalysis : true,
                    state: 2,
                    active : false,
                    showAudioOperation : true
                },
                userAnswers : questionAnswers, //用户作答数据
                userAnswersJudge : subGrasps,
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
        initAudioPlayer: function () {
            var self = this;
            $("#jquery_jplayer_1").jPlayer({
                ready: function (event) {

                },
                error: function (event) {
                    self.playNextAudio();
                },
                ended: function (event) {
                    self.playNextAudio();
                },
                volume: 0.8,
                solution: "html,flash",
                swfPath: "/public/plugin/jPlayer",
                supplied: "mp3"
            });
        },
        playAudio: function (target, voiceList) {
            var self = this;
            self.audioList = voiceList;

            if (self.audioList.length > 0) {
                if ($(target).hasClass("stud-audio-s3")) {
                    $(target).removeClass("stud-audio-s3");
                    $("#jquery_jplayer_1").jPlayer("clearMedia");
                } else {
                    self.playIndex = 0;
                    self.playNextAudio();
                    $(".btnPlay").removeClass("stud-audio-s3");
                    $(target).addClass("stud-audio-s3");
                }
            } else {
                $17.alert("暂无音频");
            }

        },
        playNextAudio: function () {
            if (this.playIndex > this.audioList.length - 1) {
                $(".btnPlay").removeClass("stud-audio-s3");
                $("#jquery_jplayer_1").jPlayer("clearMedia");
            } else {
                this.playSpecialAudio();
                this.playIndex++;
            }
        },
        playSpecialAudio: function () {
            if (this.playIndex > this.audioList.length - 1)
                return;
            $("#jquery_jplayer_1").jPlayer("setMedia", {
                mp3: this.audioList[this.playIndex]
            }).jPlayer("play");

        },
        back:function () {
            if(this.from=="student_history")
            {
                window.location.href = "/student/learning/examination.vpage?subject="+this.subject;
            }
            else{
                window.location.href = "/teacher/newexam/report/index.vpage?subject="+this.subject;
            }

        },
        initDomEvent: function () {
            var self = this;
            $(document).on("click",".gotop",function () {
                $('html,body').animate({scrollTop: 0});
            });
            $(window).scroll(function () {
                var scrollTop = $(this).scrollTop(); //滚动条距离顶部的高度
                var top = 100 - scrollTop < 0 ? 0 : 100 - scrollTop;
                $(".h-answerCard").css("top", top);
                var maxHeight = $(window).height() - 175 - 30 - top;
                $(".cardInner").css("maxHeight", maxHeight + "px");
            });
            $(document).on("click", ".btnPlay", function () {
                //播放音频；
                var audio_src = $(this).data("audio_src");
                var audioList = audio_src.split("|");
                self.playAudio(this, audioList);
            });
        },
        changeNav:function (param) {
            var self = this;
            if(self.showExam() === param){
                return false;
            }

            if(param === "examReport" && !self.initMethod){
                self.initMethod = true;
                self.init();
            }
            self.showExam(param);

            /*if(param == 'examDetails'){//考试报告
                $("#examDetailsAction").show();
                $("#examDetailsNav").addClass("on");
                $("#examReportAction").hide();
                $("#examReportNav").removeClass("on");
            }else if (param == 'examReport') {//试卷详情
                $("#examDetailsAction").hide();
                $("#examDetailsNav").removeClass("on");
                $("#examReportAction").show();
                $("#examReportNav").addClass("on");
            }*/
        },
        changeNavClass:function (param) {
            if(param == 'examDetails'){//试卷详情
                return "nav-item on"
            }else if (param == 'examReport') {//考试报告
                return "nav-item on"
            }
        }
    };
    window.onload = function () {
        var maxHeight = $(window).height() - 100 - 175 - 30;
        $(".cardInner").css("maxHeight", maxHeight + "px");
    };
    ko.applyBindings(new StudentDetail(), document.getElementById('paperModule'));
}());