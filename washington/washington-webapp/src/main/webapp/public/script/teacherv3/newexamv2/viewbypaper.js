/**
 * Created by dell on 2017/10/26.
 * 按试卷查看
 */
;(function () {
    function ViewByPaper(opts) {
        this.webLoading = ko.observable(true);
        this.newExamPaperInfos = opts.newExamPaperInfos || [];
        this.currentPaperIndex = ko.observable("0");
        this.examId = opts.examId;
        this.clazzId = opts.clazzId;
        this.subject = opts.subject;
        this.questionMap = [];
        this.themeForSubs = ko.observableArray([]);
        this.jointNum = ko.observable(0);
        this.submitNum = ko.observable(0);
        this.endCorrect = true;
        this.init();
    }

    ViewByPaper.prototype = {
        constructor: ViewByPaper,
        name: "ViewByPaper",
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
            $.get("/teacher/newexam/report/newpaperclazzanswer.vpage", {
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
                ;
            });
        },
        getthemeForSubs: function () {
            var self = this;
            $.get("/container/report/newpaperquestioninfo.vpage", {
                paperId: self.newExamPaperInfos[self.currentPaperIndex()].paperId,
                examId: self.examId
            }, function (res) {
                if (res.success) {
                    res.themeForSubs.forEach(function (item) {
                        item.questionList = [];
                        item.subQuestions.forEach(function (squestion) {
                            if (item.questionList.indexOf(squestion.qid) == -1) {
                                item.questionList.push(squestion.qid);
                            }
                        })
                    });
                    self.themeForSubs(res.themeForSubs);
                    var maxHeight = $(window).height() - 250;
                    $(".cardInner").css("maxHeight", maxHeight + "px");
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
                ;
            });
        },
        questionNoClick: function (qid, index, subIndex) {
            // var actualTop = element.offsetTop;
            // var current = element.offsetParent;
            // while (current !== null){
            //     actualTop += current. offsetTop;
            //     current = current.offsetParent;
            // }
            // if (document.compatMode == "BackCompat"){
            //     var elementScrollTop=document.body.scrollTop;
            // } else {
            //     var elementScrollTop=document.documentElement.scrollTop;
            // }

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
        loadQuestionContent: function (qid, index) {
            var self = this;
            var $mathExamImg = $("#question_" + qid + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "examImg_" + qid + index).appendTo($mathExamImg);
            var qIndex = []
            self.questionMap[qid] && self.questionMap[qid].subQuestions.forEach(function (item) {
                qIndex.push(item.index)
            });
            SystemJS.import('main').then(function () {
                // forTest
                vox.examination.create({
                    domain: constantObj.domain,
                    img_domain: constantObj.imgDomain,
                    env: constantObj.env,
                    user_id: '',
                    renderType: 'student_preview',
                }, function () {
                    vox.examination.render({
                        ids: [qid],
                        qIndex: [qIndex],
                        dom: "examImg_" + qid + index,
                    }, function (divs) {
                        if (divs.length > 0) {
                            divs[0].SDiv.forEach(function (item, index) {
                                var tmpl = "tmpl" + self.questionMap[qid].subQuestions[index].type;
                                var data = self.questionMap[qid].subQuestions[index];
                                data.endCorrect = self.endCorrect;
                                $(item).html(template(tmpl, data))
                            });
                        }
                    });
                });
            });
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

                    if (type == 1) {
                        $(this).text("显示全部");
                        $(this).parents(".answeBox").find(".answerContent").removeClass("showAll");
                    }
                    else {
                        $(this).text("展开详情");
                        $(this).parents(".answeBox").find(".answerContent").hide();
                    }
                }
                else {
                    $(this).text("收起");
                    $(this).parent().addClass("put-away");
                    if (type == 1) {
                        $(this).parents(".answeBox").find(".answerContent").addClass("showAll");
                    }
                    else {
                        $(this).parents(".answeBox").find(".answerContent").show();
                    }
                }
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
    $17.newexamv2 = $17.newexamv2 || {};
    $17.extend($17.newexamv2, {
        getViewByPaper: function (opt) {
            return new ViewByPaper(opt);
        }
    });
    window.onload = function () {

    };
}());