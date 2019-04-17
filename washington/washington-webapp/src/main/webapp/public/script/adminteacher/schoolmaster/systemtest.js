/**
 * @author: pengmin.chen
 * @description: "校长账号-模考统测"
 * @createdDate: 2018.06.19
 * @lastModifyDate: 2018.08.01
 */

// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗
define(["jquery", "knockout", "YQ", "knockout-switch-case", "impromptu", "voxLogs"],function($, ko, YQ){
    var systemtestModal = function () {
        var self = this;
        var databaseLogs = "tianshu_logs";
        var moduleName = "m_6q3pjPVz";

        YQ.voxLogs({
            database: databaseLogs,
            module: moduleName,
            op: "exam_load",
            s0: "校长"
        });
        $.extend(self, {
            testDateList: ko.observableArray([]), // 模考统测-时间
            testGradeList: ko.observableArray([]), // 模考统测-年级
            subjectList: ko.observableArray([]), // 模考统测-学科

            choiceDateInfo: ko.observable({}), // 选择的时间信息
            choiceGradeInfo: ko.observable({}), // 选择的年级信息
            choiceSubjectInfo: ko.observable({}), // 选择的学科信息

            isShowDateSelect: ko.observable(false), // 是否展示时间下拉
            isShowGradeSelect: ko.observable(false), // 是否展示年级下拉
            isShowSubjectSelect: ko.observable(false), // 是否展示学科下拉

            systemTestList: ko.observableArray([]), // 模考统测列表数据
            systemTestListEmptyTip: ko.observable(false), // 列表为空提示
            isShowChoicePopup: ko.observable(false), // 是否展示选择报告弹窗
            url:ko.observable(),
            num:ko.observable('0'),
            reportUrlList: ko.observableArray([]), // 报告地址

            // 点击时间
            showDateSelect: function () {
                self.isShowDateSelect(!self.isShowDateSelect());
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
            },
            // 点击年级
            showGradeSelect: function () {
                self.isShowGradeSelect(!self.isShowGradeSelect());
                self.isShowDateSelect(false);
                self.isShowSubjectSelect(false);
            },
            // 点击学科
            showSubjectSelect: function () {
                self.isShowSubjectSelect(!self.isShowSubjectSelect());
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
            },

            // 选择时间下拉
            choiceDate: function (choiceData) {
                self.choiceDateInfo(choiceData);
                self.isShowDateSelect(false);

                getSystemTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value
                ); // 接口请求模考统测数据
            },
            // 选择年级下拉
            choiceGrade: function (choiceData) {
                self.choiceGradeInfo(choiceData);
                self.isShowGradeSelect(false);

                getSystemTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value
                ); // 接口请求模考统测数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "exam_grade_click",
                    s0: self.choiceGradeInfo().value,
                    s1: "校长"
                });
            },
            // 选择学科下拉
            choiceSubject: function (choiceData) {
                self.choiceSubjectInfo(choiceData);
                self.isShowSubjectSelect(false);

                getSystemTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value
                ); // 接口请求模考统测数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "exam_subject_click",
                    s0: self.choiceSubjectInfo().value
                });
            },

            // 查看报告
            showChoiceReportPop: function (exam) {
                // 1、平台报告存在 且 测评报告不存在（exam.reportLink1 && !exam.reportLink2，为防止不被浏览器拦截，已处理成a链接跳转）
                // 2、平台报告不存在 且 测评报告存在（!exam.reportLink1 && exam.reportLink2，为防止不被浏览器拦截，已处理成a链接跳转）
                // 3、平台报告不存在 且 测评报告不存在
                if (!exam.reportLink1 && !exam.reportLink2) {
                    alertError(exam.info);
                    return
                }
                // 4、平台报告存在 且 测评报告存在（弹窗选择）
                if (exam.reportLink1 && exam.reportLink2) {
                    self.isShowChoicePopup(true);
                    self.reportUrlList([exam.reportLink1, exam.reportLink2]);
                }
            },
            hideChoicePopup: function () {
                self.isShowChoicePopup(false);
            }
        });
        // 请求过滤列表
        var getTestListInfo = function () {
            $.ajax({
                url: '/examReport/loadExamListCondition.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.result) {
                        self.testDateList(res.dateList);
                        self.testGradeList(res.gradeList);
                        self.subjectList(res.subjectList);

                        self.choiceDateInfo(self.testDateList()[self.testDateList().length - 1]); // 默认选择的时间信息
                        self.choiceGradeInfo(self.testGradeList()[0]); // 默认选择的年级信息
                        self.choiceSubjectInfo(self.subjectList()[0]); // 默认选择的学科

                        getSystemTestInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSubjectInfo().value
                        ); // 请求首屏模考统测数据
                    } else {
                        alertError(res.info || '出错了，请重试！');
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            });
        };
        // 请求模考统测数据
        var getSystemTestInfo = function (dataValue, gradeValue, subjectValue) {
            $.ajax({
                url: '/examReport/loadExamList.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    subject: subjectValue || ''
                },
                success: function (res) {
                    if (res.result) {
                        var examList = res.examList;
                        for (var i = 0; i < examList.length; i++) {
                            // 平台报告
                            if(examList[i].examRegionLevel !== regionLevel){
                                examList[i].reportLink1 = ''; // 学业成就分析报告may no
                            }else{
                                examList[i].reportLink1 = '/schoolmaster/testreport.vpage?examId=' + examList[i].id + '&cityCode=' + examList[i].cityCode + '&regionCode=' + examList[i].regionCode + '&schoolId=' + examList[i].schoolId + '&reportName=' + window.encodeURIComponent(examList[i].name);
                            }

                            // 测评报告
                            if (examList[i].result === 'false') { // 测评报告没有数据
                                examList[i].reportLink2 = '';
                            } else {
                                var regionCode = examList[i].schoolId
                                if(regionLevel == 'city'){
                                    regionCode = examList[i].cityCode;
                                }else if(regionLevel == 'county'){
                                    regionCode = examList[i].regionCode;
                                }
                                examList[i].reportLink2 = '/exam/evaluationReport/rstaff.vpage?examId=' + examList[i].id + '&regionLevel=' + regionLevel+ '&regionCode=' + regionCode;
                            }
                        }
                        self.systemTestList(examList);

                        if (res.examList.length) {
                            self.systemTestListEmptyTip(false); // 使用字段控制原因：防止切换条件时出错，使用length会出问题
                        } else {
                            self.systemTestListEmptyTip(true);
                        }
                    } else {
                        self.systemTestListEmptyTip(true);
                    }
                },
                error: function () {
                    self.systemTestListEmptyTip(true);
                }
            });
        };

        // 简单弹窗报错
        var alertError = function (content, title, callback) {
            var title = title || '系统提示';

            $.prompt(content, {
                title: title,
                buttons: {'确定': true},
                focus : 0,
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        e.preventDefault();
                        if (callback) {
                            callback();
                        } else {
                            $.prompt.close();
                        }
                    }
                }
            });
        };

        // 绑定全局事件
        var bindGlobalEvent = function () {
            // 点击页面收起所有的下拉
            $(document).on('click', function () {
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
            });
        };

        // 初始化触发
        getTestListInfo();
        bindGlobalEvent();
    };

    var tModal = new systemtestModal();
    ko.applyBindings(tModal, document.getElementById("systemtest"));
});