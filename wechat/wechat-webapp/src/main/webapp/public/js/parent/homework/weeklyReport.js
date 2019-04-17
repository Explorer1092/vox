define(["jquery", "knockout", 'userpopup', '$17',"menu"], function ($, knockout, userpopup, $17) {

    //依赖初始化
    userpopup.selectStudent("weeklyReport");

    var HomeworkModel = function (homeworks) {
        var self = this;
        self.englishHomeworks = knockout.observableArray(homeworks);
        self.mathHomeworks = knockout.observableArray(homeworks);
        self.hasEnglishTeacher = knockout.observable(true);
        self.hasMathTeacher = knockout.observable(true);
        self.isGraduate = knockout.observable(false);

        //数据
        self.homeworkTotalCount = knockout.observable(0);
        self.completeHomeworkCount = knockout.observable(0);
        self.curWeekHomeworkCount = knockout.observable(0);
        self.curWeekCompleteCount = knockout.observable(0);
        self.shareKey = knockout.observable('');
        self.studentId = knockout.observable(0);
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
            self.focusTab(subject);
        };
    };

    var viewModel = new HomeworkModel([]);
    knockout.applyBindings(viewModel);

    return {
        loadMessageById: function (sid) {
            //初始化数据
            viewModel.englishHomeworks({});
            viewModel.mathHomeworks({});

            //获取每周报告信息
            $17.ajax({
                url: '/parent/homework/load_weekly_report.vpage',
                data: {
                    studentId: sid
                },
                showLoading: true,
                success: function (data) {
                    if(data.report){
                        viewModel.englishHomeworks(data.report);
                        viewModel.homeworkTotalCount(data.report.homeworkTotalCount);
                        viewModel.completeHomeworkCount(data.report.completeHomeworkCount);
                        viewModel.curWeekHomeworkCount(data.report.curWeekHomeworkCount);
                        viewModel.curWeekCompleteCount(data.report.curWeekCompleteCount);
                        viewModel.shareKey(data.share_key);
                        viewModel.studentId(sid);
                    }
                    viewModel.isGraduate(data.isGraduate);
                },
                error: function () {


                }
            });

        }
    };
});
