/**
 * Created by dell on 2017/10/26.
 * 按试卷查看
 */
;(function () {
    //初始化题目渲染库
    Freya.init({
        isDev: ["staging","prod","test"].indexOf(constantObj.env) === -1,
        cheat : ["staging","test"].indexOf(constantObj.env) !== -1,
        cdnHost:constantObj.imgDomain
    }).then(function(){
        $17.info("Freya init completed");
    });


    function ViewByPaper(opts) {
        this.webLoading = ko.observable(true);
        this.newExamPaperInfos = opts.newExamPaperInfos || [];
        this.currentPaperIndex = ko.observable("0");
        this.examId = opts.examId;
        this.clazzId = opts.clazzId;
        this.subject = opts.subject;
        this.questionMap = [];  //题目ID与班级对该题目答题结果的映射
        this.themeForSubs = ko.observableArray([]);
        this.jointNum = ko.observable(0);
        this.submitNum = ko.observable(0);
        this.endCorrect = true;
        this.questionEntityMap = {};  //题目完整信息字段
        this.subQuestionNumberMap = {}; //每道小题在试卷的题号映射 ,题ID + 小题在大题中的下标为key
        this.questionIdsInPaper = []; //题目ID在试卷中的顺序
        this.init();
        this.freyaElem = null;
    }

    ViewByPaper.prototype = {
        constructor: ViewByPaper,
        name: "ViewByPaper",
        answerStatTypeMap : {
            "CHOICE" : "CHOICE",
            "TOP_3"  : "TOP_3",
            "SCORES" : "TOP_3",
            "RIGHT_WRONG" : "TOP_3",
            "ORAL" : "ORAL"
        },
        init: function () {

            $17.voxLog({
                module: "m_yJO2o3u3",
                op: "o_TRNxgV1a",
                s0: this.subject,
                s1: this.examId,
                s2: this.newExamPaperInfos[this.currentPaperIndex()].paperId
            });
            this.getPaperDetail();
            this.initDomEvent();
            this.initAudioPlayer();
        },
        changePaper: function (index) {
            var self = this;
            self.questionMap = [];
            self.submitNum(0);
            self.jointNum(0);
            self.themeForSubs([]);
            self.currentPaperIndex(index);
            self.getPaperDetail();
        },
        getPaperDetail: function () {
            var self = this;
            $.get("/teacher/newexam/report/paperclazzanswerdetail.vpage", {
                clazzId: self.clazzId,
                examId: self.examId,
                paperId: self.newExamPaperInfos[self.currentPaperIndex()].paperId
            }, function (res) {
                if (res.success) {
                    self.questionMap = res.questionMap;
                    self.submitNum(res.submitNum);
                    self.jointNum(res.jointNum);
                    self.endCorrect = res.endCorrect;
                    self.webLoading(false);
                    self.getthemeForSubs();
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
            });
        },
        getthemeForSubs: function () {
            var self = this;
            $.get("/container/report/newpaperquestioninfo.vpage", {
                paperId: self.newExamPaperInfos[self.currentPaperIndex()].paperId,
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
        questionNoClick: function (qid, index, subIndex) {
            $('html,body').animate({scrollTop: $("#anchor_id_"+index).offset().top},'slow');
            $17.voxLog({
                module: "m_yJO2o3u3",
                op: "o_ZOWfqcqv",
                s0: this.examId,
                s1: this.newExamPaperInfos[this.currentPaperIndex()].paperId,
                s2: index,
                s3: this.questionMap[qid].subQuestions[subIndex].rate
            });
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
            var questionAnswer = self.questionMap[qid];
            var subQuestionAnswer = questionAnswer.subQuestions[subIndex];
            subQuestionAnswer.endCorrect = self.endCorrect;  //应用于口语
            subQuestionAnswer.qid = qid;
            subQuestionAnswer.subIndex = subIndex;
            subQuestionAnswer.index = self.subQuestionNumberMap[qid + "_" + subIndex].index;

            //FIXME ：为了脱式需求，增加题干和题型字段
            var questionEntity = self.questionEntityMap[qid];
            try{
                subQuestionAnswer.content = questionEntity["content"]["subContents"][0]["content"];
                subQuestionAnswer.subContentTypeId = questionEntity["content"]["subContents"][0]["subContentTypeId"];
            }catch (e) {
                //报错时不影响主要逻辑，只是纯展示
                subQuestionAnswer.content = "";
                subQuestionAnswer.subContentTypeId = -1;
            }

            var answerStatType = self.answerStatTypeMap[subQuestionAnswer.answerStatType];
            var tmpl = "tmpl_" + (answerStatType ? answerStatType : "TOP_3");
            var answerAreaHtml = template(tmpl, subQuestionAnswer);
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
                if ($(target).hasClass("teach-audio-s3")) {
                    $(target).removeClass("teach-audio-s3");
                    $("#jquery_jplayer_1").jPlayer("clearMedia");
                } else {
                    self.playIndex = 0;
                    self.playNextAudio();
                    $(".btnPlay").removeClass("teach-audio-s3");
                    $(target).addClass("teach-audio-s3");
                }
            } else {
                $17.alert("暂无音频");
            }

        },
        playNextAudio: function () {
            if (this.playIndex > this.audioList.length - 1) {
                $(".btnPlay").removeClass("teach-audio-s3");
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
            $(document).on("click", ".btnPlay", function () {
                //播放音频；
                var audio_src = $(this).data("audio_src");
                var audioList = audio_src.split("|");
                var qid = $(this).parents(".stud-answer-itm").find(".questionId").val();
                var userId = $(this).parents(".stud-answer-itm").find(".userId").val();
                var index = $(this).parents(".stud-answer-itm").find(".index").val();

                $17.voxLog({
                    module: "m_yJO2o3u3",
                    op: "o_zCwUGWdq",
                    s0: self.examId,
                    s1: qid,
                    s2: index,
                    s3: userId
                });

                self.playAudio(this, audioList);
            });
            $(document).on("click", ".btnEditScore", function () {
                $(this).parent().hide();
                var value = $(this).parent().next().find(".scoreValue").val();
                $(this).parent().next().find(".input-box").val(value);
                $(this).parent().next().show();
            });
            $(document).on("click", ".btnShow", function () {
                var type = $(this).data("type");
                if ($(this).parent().hasClass("put-away")) {
                    $(this).parent().removeClass("put-away");
                    if (type == 1) { //英语口语题
                        $(this).text("显示全部");
                        $(this).parents(".answeBox").find(".answerContent").removeClass("showAll");
                    }else {
                        $(this).text("展开详情");
                        $(this).parents(".answeBox").find(".answerContent").hide();
                    }
                }
                else {
                    $(this).text("收起");
                    $(this).parent().addClass("put-away");
                    if (type == 1) {
                        $(this).parents(".answeBox").find(".answerContent").addClass("showAll");
                    }else {
                        var $answerContent = $(this).parents(".answeBox").find(".answerContent");
                        $answerContent.show();
                        if(!$answerContent.hasClass("mj-complete")){
                            window.MathJax && MathJax.Hub.Queue(["Typeset", MathJax.Hub, $answerContent[0]], function(){
                                $answerContent.addClass("mj-complete");
                            });
                        }else{
                            $17.info("已经渲染过公式了...");
                        }
                    }
                }
                self.updateContentMainHeight();
            });
            $(document).on("click", ".btnEditScoreOk", function () {
                if (self.endCorrect) {
                    return;
                }
                var $target = $(this);
                var url = '/teacher/newexam/correct.vpage';
                var qid = $target.parent().find(".questionId").val();
                var score = $target.parent().find(".input-box").val();
                var subIndex = $target.parent().find(".subIndex").val();
                var userId = $target.parent().find(".userId").val();
                var standardScore = $target.parent().find(".standardScore").val();
                var index = $target.parent().find(".index").val();
                var userScoreMap = {};
                userScoreMap[userId] = score;
                if (!/^(0|[1-9]\d*)(\.\d{1,2})?$/.test(score)) {
                    $17.alert("请输入正确数字格式，最多两位小数");
                    $target.parents(".stud-info").find(".err-txt").show();
                    return;
                }
                if (parseFloat(score) > parseFloat(standardScore)) {
                    $17.alert("本题满分：" + standardScore + "分");
                    $target.parents(".stud-info").find(".err-txt").show();
                    return;
                }
                var params = {
                    questionId: qid,
                    newExamId: self.examId,
                    subId: subIndex,
                    userScoreMap: userScoreMap
                };
                $.ajax({
                    type: 'POST',
                    contentType: 'application/json;charset=UTF-8',
                    url: url,
                    data: JSON.stringify(params),
                    success: function success(res) {
                        if (res.success) {
                            $target.parents(".stud-info").find(".err-txt").hide();
                            $target.parent().prev().show();
                            $target.parent().prev().find("i").html(score);
                            $target.parent().find(".scoreValue").val(score);
                            $target.parent().hide();
                            $17.voxLog({
                                module: "m_yJO2o3u3",
                                op: "o_v2iaCM82",
                                s0: self.examId,
                                s1: qid,
                                s2: index,
                                s3: userId,
                                s4: score
                            });
                        } else {
                            $target.parents(".stud-info").find(".err-txt").text(res.info || '修改失败').show();
                        }
                    },
                    dataType: 'json'
                });
            });
        }
    };
    $17.newexamv3 = $17.newexamv3 || {};
    $17.extend($17.newexamv3, {
        getViewByPaper: function (opt) {
            return new ViewByPaper(opt);
        }
    });
}());