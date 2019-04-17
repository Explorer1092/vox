;(function () {

    var subject = constantObj.subject;

    var StudentDetail = function () {
        this.webLoading = ko.observable(true);
        this.subject = subject;
        this.examId = $17.getQuery("examId");
        this.userId = $17.getQuery("userId");
        this.from = $17.getQuery("from");
        this.questionMap = ko.observable({});
        this.themeForSubs = ko.observableArray([]);
        this.score = ko.observable("");
        this.newExamName = ko.observable("");
        this.gradeType = ko.observable("");
        this.embedRank = ko.observable("");
        this.userName = ko.observable("");
        this.playIndex = 0;
        this.audioList = [];

        this.init();

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
            $.get("/container/report/newpaperpersonalanswer.vpage", {
                userId: self.userId,
                examId: self.examId
            }, function (res) {
                if (res.success) {
                    self.questionMap(res.questionMap);
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
                        s2: self.gradeType()==0?self.score():self.embedRank(),
                        s3: self.from
                    },(self.from=="student_history"?"student":"teacher"));
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
                ;
            });
        },
        getthemeForSubs: function () {
            var self = this;
            $.get("/container/report/newpaperquestioninfov1.vpage", {
                userId: self.userId,
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
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
                ;
            });
        },
        questionNoClick: function (qid, index) {
            $('html,body').animate({scrollTop: $("#anchor_id_"+index).offset().top},'slow');
        },
        getQuestionNoClass: function (qid, subIndex) {
            var iconClass = "";
            if (this.questionMap()[qid] && this.questionMap()[qid].subQuestions[subIndex]) {
                if (this.questionMap()[qid].subQuestions[subIndex].type == 1) {
                    if(this.questionMap()[qid].subQuestions[subIndex].hasAnswer)
                    {
                        iconClass = "sk-blue";
                    }
                    else{
                        iconClass = "sk-red";
                    }

                }
                else if (this.questionMap()[qid].subQuestions[subIndex].personalGrasp) {
                    iconClass = "sk-green";
                }
                else {
                    iconClass = "sk-red";
                }

            } else {
                iconClass = "sk-green";
            }
            return iconClass;
        },
        loadQuestionContent: function (qid, index) {
            var self = this;
            var $mathExamImg = $("#question_" + qid + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "examImg_" + qid + index).appendTo($mathExamImg);
            var qIndex = [];
            self.questionMap()[qid] && self.questionMap()[qid].subQuestions.forEach(function (item) {
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
                                var tmpl = "tmpl" + self.questionMap()[qid].subQuestions[index].type;
                                var data = self.questionMap()[qid].subQuestions[index];
                                data.userName = self.userName();
                                data.gradeType = self.gradeType();
                                data.from = self.from;
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
            $(".gotop").click(function () {
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
        }
    };
    window.onload = function () {
        var maxHeight = $(window).height() - 100 - 175 - 30;
        $(".cardInner").css("maxHeight", maxHeight + "px");
    };
    ko.applyBindings(new StudentDetail(), document.getElementById('paperModule'));
}());