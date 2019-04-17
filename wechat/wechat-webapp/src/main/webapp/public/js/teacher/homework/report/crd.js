/**
 * Created by xinqiang.wang on 2016/2/18.
 * 班级报告详情
 */

define(["$17", "knockout", "komapping", "examCore_new","jbox", "jp", 'logger'], function ($17, ko, komapping, examCore,jbox, jp, logger) {
    "use strict";
    var ReportModel = function () {
        var self = this;
        self.currentTabName = $17.getQuery("tabType");
        self.homeworkId = $17.getQuery("homeworkId");
        self.pictureQuality = '@70q';//图片压缩量%
        self.defaultPicture = '/public/images/teacher/homework/math/upImages-none.png';

        self.subject = ko.observable(LoggerProxy.subject);
        self.sendLog = function (obj) {
            var def = {
                app: "teacher",
                module: 'm_pGqNIEG2',
                s0: self.subject()
            };
            $.extend(def, obj);
            logger.log(def);

        };

        self.currentStudentQuestion = ko.observable();//选择的同学批改
        self.currentCorrection = "";
        self.divScrollTop = ko.observable(0);

        self.readingScoreDetailBox = ko.observable(false);
        self.readingScoreDetail = ko.observableArray([]);


        self.currentPage = ko.observable(1);
        self.pageSize = 5;
        self.loading = ko.observable(true);

        /*可切换的作业类型*/
        self.categoryList = ko.mapping.fromJS([
            {name: "同步习题", value: 'EXAM', checked: false, show: false},
            {name: "口算", value: 'MENTAL', checked: false, show: false},
            {name: "历年考题", value: 'UNIT_QUIZ', checked: false, show: false},
            {name: "期中测验", value: 'MID_QUIZ', checked: false, show: false},
            {name: "期末测验", value: 'END_QUIZ', checked: false, show: false},
            {name: "动手做一做", value: 'PHOTO_OBJECTIVE', checked: false, show: false},
            {name: "概念说一说", value: 'VOICE_OBJECTIVE', checked: false, show: false},
            {name: "生字词练习", value: 'WORD_PRACTICE', checked: false, show: false},
            {name: "课文读背题", value: 'READ_RECITE', checked: false, show: false},
            {name: "基础练习", value: 'BASIC_APP', checked: false, show: false},
            {name: "绘本阅读", value: 'READING', checked: false, show: false},
            {name: "听力练习", value: 'LISTEN_PRACTICE', checked: false, show: false},
            {name: "口语习题", value: 'ORAL_PRACTICE', checked: false, show: false}
        ]);

        self.getCategoryNameByValue = function (value) {
            var name = '';
            ko.utils.arrayForEach(self.categoryList(), function (cate) {
                if (cate.value() == value) {
                    name = cate.name();
                }
            });
            return name;
        };

        /*作业批改项*/
        self.homeworkLevel = ko.mapping.fromJS([
            {name: "阅", value: 'REVIEW', checked: false, showName: "阅", show: true},
            {name: "优", value: 'EXCELLENT', checked: false, showName: "优", show: false},
            {name: "良", value: 'GOOD', checked: false, showName: "良", show: false},
            {name: "中", value: 'FAIR', checked: false, showName: "中", show: false},
            {name: "差", value: 'PASS', checked: false, showName: "差", show: false},
            {name: "对", value: 'RIGHT', checked: false, showName: "√", show: false},
            {name: "错", value: 'WRONG', checked: false, showName: "×", show: false}
        ]);

        /*作业难度级别*/
        self.difficulty = function (value) {
            return value == 3 ? "中等" : (value == 4 || value == 5 ? "困难" : "容易")
        };

        self.secondsToMinute = function (seconds) {
            var s = seconds % 60;
            var m = Math.floor(seconds / 60);
            return m > 0 ? m + '分' + s + '秒' : s + '秒';
        };

        self.allReportDetail = ko.observable({});//本次作业详情

        self.currentReportDetail = ko.observableArray([]);//展示已选择的作业类型内容
        self.showReportDetail = ko.observableArray([]);//展示已选择的作业类型内容
        self.showViewHomeworkSelectBox = ko.observable(false);//预览作业页-选择框
        self.selectedTab = ko.observable({});//已选择的作业类型
        self.questionId = ko.observable(0);
        self.showCheckBox = ko.observable(false);//检查选择框
        self.checkHomeworkDetail = ko.observable({});//检查选择框详情
        self.showType = ko.observable(0);//0纯文本 1图片 2音频【后端字段】
        self.showQuickCheckBtn = ko.observable(false);//一键批改是否展示

        /*获取已选的批改类型*/
        self.getSelectedLevel = function () {
            var name = '';
            for (var i = 0, il = self.homeworkLevel(); i < il.length; i++) {
                if (il[i].checked()) {
                    name = il[i].value();
                }
            }
            return name;
        };

        /*批改作业详情*/
        self.getReportDetail = function () {
            $17.weuiLoadingShow();
            $.post("/teacher/homework/report/answer/detail/clazz.vpage", {homeworkId: self.homeworkId}, function (data) {
                if (data.success) {
                    self.subject(data.subject);
                    var obj = data.questionInfoMapper;
                    if (!obj || obj === 'null' || obj === 'undefined') {
                        $17.msgTip("暂无数据");
                    }
                    self.allReportDetail(ko.mapping.fromJS(data.questionInfoMapper));
                    //类型名称重置
                    ko.utils.arrayForEach(self.categoryList(), function (list) {
                        for (var i in data.objectiveConfigTypes) {
                            if (i == list.value()) {
                                list.name(data.objectiveConfigTypes[i]);
                            }
                        }
                    });
                    self.showHomeworkTypeList();
                    self.showQuickCheckBtn(data.includeSubjective || false);

                } else {
                    $17.msgTip(data.info);
                }
                $17.weuiLoadingHide();
                self.sendLog({
                    op: "hometype_all_details_load",
                    s2: $17.getQuery("homeworkId"),
                    s3: $17.getQuery("tabType")
                });
            });
        };

        /*根据作业选择展示作业类型列表*/
        self.showHomeworkTypeList = function () {
            var report = ko.mapping.toJS(self.allReportDetail());
            for (var i = 0, cl = self.categoryList(); i < cl.length; i++) {
                for (var j in report) {
                    if (cl[i].value() == j) {
                        if (cl[i].value() == self.currentTabName) {
                            cl[i].checked(true);
                        }
                        if ($17.isBlank(self.currentTabName)) {
                            cl[0].checked(true);
                        }
                        cl[i].show(true);

                    }
                }
                if (cl[i].checked()) {
                    self.showViewHomeworkClick(cl[i]);
                }
            }
        };

        /*切换作业类型-弹框*/
        self.showViewHomeworkTitleClick = function () {
            self.showCheckBox(false);
            self.showViewHomeworkSelectBox(!self.showViewHomeworkSelectBox());
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_passport_' + self.subject(),
                op: 'change_homework_style_click'
            });
        };

        /*切换作业类型-选择*/
        self.showViewHomeworkClick = function (that) {
            $17.weuiLoadingShow();
            for (var i = 0, dl = self.categoryList(); i < dl.length; i++) {
                dl[i].checked(false);
            }
            that.checked(true);
            self.showViewHomeworkSelectBox(false);
            self.currentReportDetail(self.allReportDetail()[that.value()]);
            self.selectedTab(ko.mapping.toJS(that));

            if ($.inArray(that.value(), ['BASIC_APP', 'READING']) == -1) {
                $('#homeworkListBox').animate({scrollTop: '0px'}, 0);
                self.showReportDetail([]);
                self.currentPage(1);
                self.getQuestions(1);
            }
            setTimeout(function () {
                $17.weuiLoadingHide();
            }, 500);

            self.sendLog({
                op: "hometype_details_switchtype_click",
                s2: self.homeworkId,
                s3: that.value()
            });

        };

        /*查看基础练习详情*/
        self.basicAppDetailBtn = function (lessonId) {
            var that = this;
            var param = {
                homeworkId: that.homeworkId(),
                categoryId: that.categoryId(),
                lessonId: lessonId,
                subject: self.subject()
            };
            setTimeout(function () {
                location.href = '/teacher/homework/report/basicappreportdetail.vpage?' + $.param(param);
            }, 200);

            self.sendLog({
                op: "hometype_all_details_seedetails_click",
                s2: "BASIC_APP",
                s3: param.categoryId,
                s4: that.averageScore()
            });

        };

        $('#homeworkListBox').scroll(function () {
            var $this = $(this);
            if ($this.scrollTop() == 0) {
                $17.backToTop();
            }
            self.divScrollTop($this.scrollTop());
        });

        /*应试题展示*/
        self.loadExamImg = function (examId, index) {
            var $mathExamImg = $("#examImg" + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id", "examImg-" + index).appendTo($mathExamImg);
            var node = document.getElementById("examImg-" + index);
            vox.exam.render(node, 'normal', {
                ids: [examId],
                getQuestionByIdsUrl: 'teacher/homework/query/questions.vpage',
                imgDomain: homeworkConstant.imgDomain,
                env: homeworkConstant.env,
                domain: homeworkConstant.domain
            });
        };

        /*分页获取应试*/
        self.getQuestions = function (page) {
            var groupExam = self.currentReportDetail.slice((page - 1) * self.pageSize, page * self.pageSize);
            ko.utils.arrayForEach(groupExam, function (exam) {
                self.showReportDetail.push(ko.mapping.fromJS(exam));
            });
        };

        //滚动加载应试题目
        self.homeworkScrolled = function (data, event) {
            var elem = event.target;
            if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 200) && self.loading()) {
                self.currentPage(self.currentPage() + 1);
                self.getQuestions(self.currentPage());
            }
        };

        /*批改*/
        self.checkBtn = function (qid, showType) {
            var that = this;
            that.review(that.review());
            self.currentStudentQuestion(that);
            self.showCheckBox(true);
            self.checkHomeworkDetail(ko.mapping.toJS(that));
            self.questionId(qid);
            self.showType(showType);

            //根据类型展示对应标签
            for (var i = 0, il = self.homeworkLevel(); i < il.length; i++) {
                il[i].checked(false);
                if (il[i].value() != "REVIEW") {
                    il[i].show(false);
                }
                if (self.selectedTab().value == 'EXAM') {
                    if (il[i].value() == "RIGHT" || il[i].value() == "WRONG") {
                        il[i].show(true);
                    }
                } else {
                    if (il[i].value() != "RIGHT" && il[i].value() != "WRONG") {
                        il[i].show(true);
                    }
                }
                if (that.correction() != null) {
                    if (that.correction() == il[i].value()) {
                        il[i].checked(true);
                    }
                } else {
                    if (il[i].value() == "REVIEW" && that.review() != null) {
                        il[i].checked(true);
                    }
                }
            }
            if (self.showViewHomeworkSelectBox()) {
                self.showViewHomeworkSelectBox(false);
            }

            if (self.selectedTab().value == 'PHOTO_OBJECTIVE' || self.selectedTab().value == 'VOICE_OBJECTIVE') {
                if (showType == 2) {
                    logger.log({
                        app: "teacher",
                        module: 'WECHAT_Newhomework_passport_' + self.subject(),
                        op: 'SUBJECT_video_correct_pageload '
                    });
                } else {
                    logger.log({
                        app: "teacher",
                        module: 'WECHAT_Newhomework_passport_' + self.subject(),
                        op: 'SUBJECT_photo_correct_pageload '
                    });
                }
            } else {
                logger.log({
                    app: "teacher",
                    module: 'WECHAT_Newhomework_passport_' + self.subject(),
                    op: 'EXAM_photo_correct_pageload'
                });
            }
        };

        /*点击批改选项*/
        self.levelClick = function () {
            var that = this;
            for (var i = 0, il = self.homeworkLevel(); i < il.length; i++) {
                il[i].checked(false);
            }
            that.checked(true);
            self.currentCorrection = (that.value() == 'REVIEW' || that.value() == '') ? null : that.value();
        };

        /*批改-提交*/
        self.checkSubmitBtn = function () {
            var correction = (self.getSelectedLevel() == 'REVIEW' || self.getSelectedLevel() == '') ? null : self.getSelectedLevel();
            var data = {
                homeworkId: self.homeworkId,
                type: self.selectedTab().value,
                questionId: self.questionId(),
                isBatch: false,
                corrections: [{
                    userId: self.checkHomeworkDetail().userId,
                    review: true,
                    correctType: null,
                    correction: correction,
                    teacherMark: ''
                }]
            };
            $17.weuiLoadingShow();
            $.post("/teacher/homework/report/correct.vpage", {data: JSON.stringify(data)}, function (data) {
                if (data.success) {
                    self.showCheckBox(false);
                    /*更改批改选项*/
                    if (self.getSelectedLevel() == 'REVIEW') {
                        self.currentStudentQuestion().review(true)
                    }
                    self.currentStudentQuestion().correction(self.currentCorrection);
                    $17.msgTip("批改成功");
                }
                $17.weuiLoadingHide();
            });
        };

        /*音频题-阅*/
        self.reviewClick = function (qid, showType) {
            var that = this;
            self.checkHomeworkDetail(ko.mapping.toJS(that));
            self.questionId(qid);
            self.showType(showType);
            for (var i = 0, il = self.homeworkLevel(); i < il.length; i++) {
                il[i].checked(false);
                if (il[i].value() == "REVIEW") {
                    il[i].checked(true);
                }
            }
            self.checkSubmitBtn();
        };

        /*批改-关闭*/
        self.closeCheckBoxBtn = function () {
            self.showCheckBox(false);
        };

        /*一键批改*/
        self.quickCheckClick = function () {
            $17.weuiLoadingShow();
            $.post("/teacher/homework/report/batchcorrect.vpage", {homeworkId: self.homeworkId}, function (data) {
                if (data.success) {
                    $17.msgTip("一键批改成功");
                    setTimeout(function () {
                        location.reload();
                    }, 2000);
                } else {
                    $17.msgTip(data.info);
                }
                $17.weuiLoadingHide();
            });
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_passport_' + self.subject(),
                op: 'comment_a_key_correct_btn'
            });
        };

        /*阅读绘本详情*/
        self.readingScoreDetailClick = function () {
            var that = this;
            self.readingScoreDetailBox(true);
            self.readingScoreDetail(ko.mapping.fromJS(that.studentInfo));
        };

        self.gotoTop = function () {
            $('#homeworkListBox').scrollTop(0);
        };


        /*音频播放*/
        var my_jPlayer = $("#jplayerId");
        var isPlaying = false;
        var playErrorCount = 0; //播放失败的个数
        var urlIndex = 0;
        self.voicePlay = function (urls, urlIndex) {
            my_jPlayer.jPlayer("destroy");
            urlIndex = 0;
            playErrorCount = 0;
            my_jPlayer.jPlayer({
                ready: function () {},
                playing : function(event){
                    isPlaying = true;
                },
                timeupdate : function(event){
                    //fix 音频文件头中没有content-Length属性，造成不调用ended结束方法
                    if(isPlaying && event.jPlayer.status.duration == 0 && event.jPlayer.status.currentTime == 0){
                        isPlaying = false;
                        urlIndex = self.playNextAudio(urlIndex,urls);
                    }
                },
                ended: function () {
                    if(isPlaying){
                        isPlaying = false;
                        urlIndex = self.playNextAudio(urlIndex,urls);
                    }
                },
                error: function (event) {
                    isPlaying = false;
                    playErrorCount++;
                    urlIndex = self.playNextAudio(urlIndex,urls);
                    logger.log({
                        app: "teacher",
                        module: 'WECHAT_Newhomework_passport_' + self.subject(),
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
        self.playNextAudio = function(playIndex,audioArr){
            if(playIndex >= audioArr.length - 1){
                $(".playAudioBtn").removeClass('stop');
                my_jPlayer.jPlayer("destroy");
                (playErrorCount == audioArr.length) && $17.jqmHintBox("播放失败，出现兼容性问题！");
            }else{
                playIndex++;
                self.playSpecialAudio(audioArr[playIndex]);
            }
            return playIndex;
        };
        self.playSpecialAudio = function(url){
            if(url){
                my_jPlayer.jPlayer("setMedia", {
                    mp3: url
                }).jPlayer("play");
            }
        };
        self.voicePlayOrStopBtn = function (element,urls) {
            var that = this,$element = $(element),$playAudioBtn = $(".playAudioBtn");
            $playAudioBtn.removeClass('stop');

            if($element.hasClass("stop")){
                my_jPlayer.jPlayer("destroy");
            }else{
                var voiceList;
                if(typeof urls === "string"){
                    voiceList = voiceList.split("|");
                }else{
                    voiceList = urls;
                }
                if (!$.isArray(voiceList) || voiceList.length <= 0) {
                    $17.jqmHintBox("音频地址缺失");
                    return false;
                }
                self.voicePlay(voiceList, 0);
                $element.addClass("stop");
                logger.log({
                    app: "teacher",
                    module: 'WECHAT_Newhomework_passport_' + self.subject(),
                    op: 'SUBJECT_video_play_click'
                });
            }
        };


        /*初始化*/
        self.getReportDetail();

        /*exam初始化*/
        window.ko = ko;
        try {
            vox.exam.create(function (data) {
                if (!data.success) {
                    logger.log({
                        app: "teacher",
                        module: 'homework',
                        op: 'vox_exam_create_fail'
                    });
                } else {

                }
            });
        } catch (exception) {
            logger.log({
                app: "teacher",
                module: 'homework',
                op: 'vox_exam_create_exception'
            });
        }
    };

    ko.applyBindings(new ReportModel());
});