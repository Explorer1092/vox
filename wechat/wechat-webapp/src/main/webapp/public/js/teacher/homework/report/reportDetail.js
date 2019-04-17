/**
 * @author xinqiang.wang
 * @description   "作业报告"
 * @createDate 2016/2/18
 *
 */

define(["$17", "knockout", "komapping", "logger", "jbox", "radialIndicator", "jp"], function ($17, ko, komapping, logger) {
    "use strict";
    var ReportModel = function () {
        var self = this;
        self.subject = ko.observable(LoggerProxy.subject);
        self.homeworkId = $17.getQuery("homeworkId");
        self.homeworkType = ko.observable('');

        self.clazzName = ko.observable();
        self.createAt = ko.observable();
        self.finishedCount = ko.observable();
        self.includeSubjectiveFlag = ko.observable(false);

        self.homeworkDetail = [];
        self.studentHomeworkDetail = ko.observableArray([]);//学生完成情况
        self.gatherHomeworkDetail = ko.observableArray([]);//作业数据汇总
        self.objectiveConfigTypesList = ko.observableArray([]);
        self.objectiveConfigTypesLength = ko.observable(0);
        self.tabList = ko.mapping.fromJS([{name: "学生完成情况", value: 'student', checked: true}, {
            name: "作业数据汇总",
            value: 'homework',
            checked: false
        }]);

        self.processRate = function (molecular, denominator) {
            molecular = +molecular || 0;
            denominator = +denominator || 0;
            if (denominator == 0) return '0%';
            return Math.ceil(molecular / denominator * 100) + "%";
        };

        /*是否显示订正（普通数学作业 且作业中含有同步练习）*/
        self.canCorrect = function () {
            var hasExam = $.inArray('同步习题', self.objectiveConfigTypesList()) != -1;
            return self.subject() == "MATH" && hasExam && self.homeworkType() != 'Similar';
        };
        /*标签选择*/
        self.selectedTabName = function () {
            var name = '';
            for (var i = 0, tb = self.tabList(); i < tb.length; i++) {
                if (tb[i].checked()) {
                    name = tb[i].value();
                }
            }
            return name;
        };

        self.sendLog = function (obj) {
            var def = {
                app: "teacher",
                module: 'm_pGqNIEG2',
                s0: self.subject()
            };
            $.extend(def, obj);
            logger.log(def);
        };

        /*录音推荐*/
        self.voiceRecommendListDetail = ko.observableArray([]);
        self.voiceRecommendShowTip = ko.observable(true);
        self.hasRecommend = ko.observable(true); //是否有录音推荐列表
        self.hasRecommended = ko.observable(true); //  已推荐or未推荐
        self.voiceExpired = ko.observable(false);

        self.requestParentCount = ko.observable(0);
        /*推荐给家长*/
        self.voiceRecommendToParentBtn = function () {
            var myModal = new jBox('Confirm', {
                title: '系统提示',
                content: "推荐成功后，会将优秀语音发给家长。被推荐的学生也会收到通知",
                cancelButton: '取消',
                confirmButton: '确定',
                minWidth: 500,
                maxWidth: 600,
                confirm: function () {
                    myModal.close();
                    var recommendVoiceList = [];

                    var recVoiceList = ko.mapping.toJS(self.voiceRecommendListDetail());
                    $.each(recVoiceList, function (index, student) {
                        recommendVoiceList.push({
                            studentId: student.studentId,
                            studentName: student.studentName,
                            categoryName: student.categoryName,
                            voiceList: student.voiceList
                        });
                    });
                    $.post("/teacher/homework/report/voicerecommend.vpage", {
                        homeworkId: self.homeworkId || null,
                        recommendComment: "这些同学读得很不错！",
                        recommendVoiceList: JSON.stringify(recommendVoiceList)
                    }, function (data) {
                        if (data.success) {
                            self.hasRecommended(true);
                            $17.alert("推荐成功");
                        }
                    });
                }
            });
            myModal.open();
        };

        /*音频播放*/
        var my_jPlayer = $("#jplayerId");
        var isPlaying = false;
        var playErrorCount = 0;
        self.setStopAll = function () {
            ko.utils.arrayForEach(self.voiceRecommendListDetail(), function (_detail) {
                _detail.isPlay(false);
            });
            my_jPlayer.jPlayer("destroy");
        };

        self.voicePlay = function (urls, urlIndex) {
            playErrorCount = 0;
            my_jPlayer.jPlayer({
                ready: function () {
                },
                playing : function(event){
                    isPlaying = true;
                },
                timeupdate : function(event){
                    //fix 音频文件头中没有content-Length属性，造成不调用ended结束方法
                    if(isPlaying && event.jPlayer.status.duration == 0 && event.jPlayer.status.currentTime == 0){
                        isPlaying = false;
                        urlIndex += 1;
                        if (urls.length == urlIndex) {
                            self.setStopAll();
                            return false;
                        }
                        my_jPlayer.jPlayer("setMedia", {
                            mp3: urls[urlIndex]
                        }).jPlayer("play");
                    }
                },
                ended: function () {
                    if(isPlaying){
                        isPlaying = false;
                        urlIndex += 1;
                        if (urls.length == urlIndex) {
                            self.setStopAll();
                            return false;
                        }
                        my_jPlayer.jPlayer("setMedia", {
                            mp3: urls[urlIndex]
                        }).jPlayer("play");
                    }
                },
                error: function (event) {
                    isPlaying = false;
                    urlIndex += 1;
                    playErrorCount += 1;
                    if (urls.length == urlIndex) {
                        var startStr = playErrorCount < urls.length ? "部分音频" : "";
                        playErrorCount > 0 && $17.jqmHintBox(startStr + "播放失败");
                        return false;
                    }
                    my_jPlayer.jPlayer("setMedia", {
                        mp3: urls[urlIndex]
                    }).jPlayer("play");

                    logger.log({
                        app: "teacher",
                        module: 'WECHAT_Newhomework_voice_recommend_' + self.subject(),
                        op: 'video_play_error',
                        s0: event.jPlayer.error.context || '',
                        s1: event.jPlayer.error.message || '',
                        s2: event.jPlayer.error.hint || ''
                    });

                }
            });

            my_jPlayer.jPlayer("setMedia", {
                mp3: urls[urlIndex]
            }).jPlayer("play");
        };

        self.voiceStop = function () {
            my_jPlayer.jPlayer("stop");
        };

        self.voicePlayOrStopBtn = function () {
            var that = this;
            if (self.voiceExpired()) {
                $17.jqmHintBox("录音已过期");
                return false;
            }
            var voiceList = that.voiceList();
            my_jPlayer.jPlayer("destroy");
            if (that.isPlay()) {
                that.isPlay(false);
                self.voiceStop();
                return false;
            }
            ko.utils.arrayForEach(self.voiceRecommendListDetail(), function (_detail) {
                _detail.isPlay(false);
            });
            that.isPlay(true);
            self.voicePlay(voiceList, 0);
        };


        self._getHomeworkDetail = function () {
            $17.weuiLoadingShow();
            $.post("/teacher/homework/report/completion.vpage", {homeworkId: self.homeworkId}, function (data) {
                if (data.success) {
                    self.homeworkDetail = data;
                    self.objectiveConfigTypesList(data.objectiveConfigTypes);
                    self.objectiveConfigTypesLength(data.objectiveConfigTypes.length);
                    self.homeworkType(data.homeworkType);

                    self.clazzName(data.clazzName);
                    self.createAt(data.createAt);
                    self.finishedCount(data.finishCount);
                    self.includeSubjectiveFlag(data.includeSubjective && !data.corrected);
                    self.subject(data.subject);

                    /*录音推荐*/
                    self.hasRecommend(!$17.isBlank(data.hasRecommend));
                    if (!$17.isBlank(data.hasRecommend)) {
                        self.hasRecommended(data.hasRecommend);
                        self.requestParentCount(data.requestParentCount);
                        var voiceList = data.voiceList || [];
                        for (var i = 0; i < voiceList.length; i++) {
                            voiceList[i].isPlay = false;
                        }
                        self.voiceRecommendListDetail(ko.mapping.fromJS(voiceList)());
                    }

                    if (!$17.isBlank(data.voiceExpired)) {
                        self.voiceExpired(data.voiceExpired);
                    }

                    /*初始化*/
                    self.getStudentHomeworkDetail();
                    self.sendLog({
                        op: "page_reportdetails_load",
                        s1: self.homeworkType(),
                        s2: self.homeworkId
                    });

                } else {
                    $17.jqmHintBox(data.info);
                }
                $17.weuiLoadingHide();
            });
        }();

        /*获取学生完成情况*/
        self.getStudentHomeworkDetail = function () {
            self.studentHomeworkDetail(self.homeworkDetail);
        };

        self.setRadialIndicator = function (qid, value, color, qtype) {
            var barBgColor = '#eef4f9', barColor = '#56c1a5';
            if (color == 'red') {
                barBgColor = '#ffe2df';
                barColor = '#f3754c';
            }
            $('#radial_' + qtype + qid).radialIndicator({
                barBgColor: barBgColor,
                barColor: barColor,
                barWidth: 10,
                initValue: value,
                displayNumber: false
            });
        };

        /*获取作业数据汇总*/
        self.getGatherHomeworkDetail = function () {
            self.gatherHomeworkDetail(self.homeworkDetail);
        };

        /*标签选择*/
        self.tabClick = function () {
            var that = this;
            for (var i = 0, tb = self.tabList(); i < tb.length; i++) {
                tb[i].checked(false);
            }
            that.checked(true);
            if (self.selectedTabName() == 'student') {
                self.getStudentHomeworkDetail();
                self.sendLog({
                    op: "page_reportdetails_stuTab_click",
                    s1: self.homeworkType(),
                    s2: self.homeworkId
                });
            } else {
                self.getGatherHomeworkDetail();
                self.sendLog({
                    op: "page_reportdetails_timuTab_click",
                    s1: self.homeworkType(),
                    s2: self.homeworkId
                });
            }
        };

        /*去批改*/
        self.gotoCheckHomeworkBtn = function () {
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_passport_' + LoggerProxy.subject,
                op: 'homework_passport_details_correct_btn'
            });
            setTimeout(function () {
                location.href = '/teacher/homework/report/clazzreportdetail.vpage?homeworkId=' + self.homeworkId;
            }, 200);

        };

        /*详情点击*/
        self.detailBtn = function (htype) {
            self.sendLog({
                op: "page_reportdetails_hometype_details_click",
                s1: self.homeworkType(),
                s2: self.homeworkId,
                s3: htype
            });
            setTimeout(function () {
                location.href = '/teacher/homework/report/clazzreportdetail.vpage?homeworkId=' + self.homeworkId + '&tabType=' + htype
            }, 200);
        };

        /*一键写评语*/
        self.quickRemarksBtn = function (_from) {
            setTimeout(function () {
                var param = {
                    homeworkId: self.homeworkId,
                    from: _from,
                    subject: self.subject()
                };
                location.href = '/teacher/homework/report/quickremarks.vpage?' + $.param(param);
            }, 200);

            var op = 'page_reportdetails_write_comments_click';
            if (_from == 'bottom') {
                op = 'page_reportdetails_onekey_write_comments_click';
            }
            self.sendLog({
                op: op,
                s1: self.homeworkType(),
                s2: self.homeworkId
            });
        };

        /*一键发奖励*/
        self.quickAwardsBtn = function (clazzId) {
            setTimeout(function () {
                var param = {
                    homeworkId: self.homeworkId,
                    clazzId: clazzId,
                    subject: self.subject()
                };
                location.href = '/teacher/homework/report/quickaward.vpage?' + $.param(param);
            }, 200);

            self.sendLog({
                op: "page_reportdetails_onekey_award_click",
                s1: self.homeworkType(),
                s2: self.homeworkId
            });

        };
    };

    ko.applyBindings(new ReportModel());


});