define(["jquery", "knockout", 'userpopup', '$17', 'menu','logger'], function ($, knockout, userpopup, $17,menu,logger) {

    //依赖初始化
    userpopup.selectStudent("homework");

    var HomeworkModel = function (homeworks) {
        var self = this;
        self.englishHomeworks = knockout.observableArray(homeworks);
        self.mathHomeworks = knockout.observableArray(homeworks);
        self.hasEnglishTeacher = knockout.observable(true);
        self.hasMathTeacher = knockout.observable(true);
        self.isGraduate = knockout.observable(false);

        //学科标签切换
        self.focusTab = knockout.observable('english');
        self.getHomework = function () {
            if (self.focusTab() == 'english') {
                return self.englishHomeworks();
            } else if (self.focusTab() == 'math') {
                return self.mathHomeworks();
            }
        };
        self.changeTab = function (subject) {
            if ((self.focusTab() == 'math' && subject == 'math') || (self.focusTab() == 'english' && subject == 'english')) {
                return false;
            }
            self.focusTab(subject);
        };

        //送花
        self.sendFlowerBtn = function ($this, canNotSendFlower) {
            if (canNotSendFlower) {
                $17.alert('孩子还没完成作业，不能送花');
                return false;
            } else {
                var studentId = $this.homework.studentId;
                var homeworkType = $this.homework.homeworkLocation.homeworkType;
                var homeworkId = $this.homework.homeworkLocation.homeworkId;
                var teacherId = $this.homework.teacherId;
                $.post('/parent/homework/sendflower.vpage', {
                    studentId: studentId,
                    homeworkType: homeworkType,
                    homeworkId: homeworkId,
                    teacherId: teacherId
                }, function (data) {
                    if (data.success) {
                        new jBox('Confirm', {
                            content: '<div><p>送花成功！</p>老师感受到了您对孩子的关心<div>',
                            confirmButton: '知道了',
                            closeButton: false,
                            cancelButton: '',
                            confirm: function () {
                                location.reload();
                            },
                            onOpen: function () {
                                $('.jBox-Confirm-button-cancel').hide();
                            }
                        }).open();
                    } else {
                        $17.jqmHintBox(data.info);
                    }
                });
            }
        };

        //作业已完成但未检查
        self.homeworkUnCheck = function () {
            $17.alert('作业已完成，等待老师检查');
        };

        //显示已完成的学生数
        self.showFinishStudentCount = function (data) {
            $17.alert("已有" + (data.homework.classMateFinishCount || 0) + "位同学完成");
        }
    };

    var viewModel = new HomeworkModel([]);
    knockout.applyBindings(viewModel);

    logger.log({
        module: 'homework',
        op: 'homework_pv_index'
    });

    return {
        loadMessageById: function (sid) {
            //初始化数据
            viewModel.englishHomeworks({});
            viewModel.mathHomeworks({});

            //获取作业信息
            $17.ajax({
                url: '/parent/homework/loadhomeworks.vpage',
                showLoading: true,
                data: {
                    sid: sid
                },
                success: function (data) {
                    //是否展示送花
                    if (data.ENGLISH) {
                        for (var i = 0, show = 0; i < data.ENGLISH.homeworks.length; i++) {
                            var quiz = data.ENGLISH.homeworks[i].quiz;
                            var canSendFlower = data.ENGLISH.homeworks[i].canSendFlower;
                            var workbook = data.ENGLISH.homeworks[i].workbook;
                            var homework = data.ENGLISH.homeworks[i].homework;
                            if (!workbook && homework.certificated) {
                                if (!homework.sentFlag && !show) {
                                    show = 1;

                                    if (homework.certificated && homework.finished) {
                                        data.ENGLISH.homeworks[i]["state"] = "giveFlower";
                                    } else {
                                        data.ENGLISH.homeworks[i]["state"] = "notgiveFlower";
                                    }
                                    //过期的测验不显示 送花按钮
                                    if(quiz && !canSendFlower && !$17.isBlank(canSendFlower)){
                                        data.ENGLISH.homeworks[i]["state"] = "none";
                                    }

                                } else if (homework.sentFlag) {
                                    show = 1;
                                    data.ENGLISH.homeworks[i]["state"] = "finish";
                                } else {
                                    data.ENGLISH.homeworks[i]["state"] = "none";
                                }
                            }
                        }
                    }
                    viewModel.englishHomeworks(data.ENGLISH || {});

                    //是否展示送花
                    if (data.MATH) {
                        for (var i = 0, show = 0; i < data.MATH.homeworks.length; i++) {
                            var quiz = data.MATH.homeworks[i].quiz;
                            var canSendFlower = data.MATH.homeworks[i].canSendFlower;
                            var workbook = data.MATH.homeworks[i].workbook;
                            var homework = data.MATH.homeworks[i].homework;
                            if (!workbook && homework.certificated) {
                                if (!homework.sentFlag && !show) {
                                    show = 1;
                                    if (homework.certificated && homework.finished) {
                                        data.MATH.homeworks[i]["state"] = "giveFlower";
                                    } else {
                                        data.MATH.homeworks[i]["state"] = "notgiveFlower";
                                    }
                                    //过期的测验不显示 送花按钮
                                    if(quiz && !canSendFlower && !$17.isBlank(canSendFlower)){
                                        data.MATH.homeworks[i]["state"] = "none";
                                    }
                                } else if (homework.sentFlag) {
                                    show = 1;
                                    data.MATH.homeworks[i]["state"] = "finish";
                                } else {
                                    data.MATH.homeworks[i]["state"] = "none";
                                }
                            }
                        }
                    }

                    viewModel.mathHomeworks(data.MATH || {});

                    if (!data.MATH) {
                        viewModel.hasMathTeacher(false);
                    } else {
                        viewModel.hasMathTeacher(true);
                    }

                    if (!data.ENGLISH) {
                        viewModel.hasEnglishTeacher(false);
                    } else {
                        viewModel.hasEnglishTeacher(true);
                    }
                    viewModel.isGraduate(data.isGraduate)
                },
                error: function (data) {

                }
            });
        }
    };
});
