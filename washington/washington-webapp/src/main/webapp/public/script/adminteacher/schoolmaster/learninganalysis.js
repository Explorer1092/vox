/**
 * @author: pengmin.chen
 * @description: "校长账号-学情分析"
 * @createdDate: 2018.06.19
 * @lastModifyDate: 2018.06.20
 */

// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗
define(["jquery", "knockout", "YQ", "echarts", "echarts-adminteacher", "knockout-switch-case", "impromptu", "voxLogs"],function($, ko, YQ, echarts){
    var learninganalysisModal = function () {
        var self = this;
        var databaseLogs = "tianshu_logs";
        var moduleName = "m_JCe0IYhO";
        var EChartsTheme = 'walden';
        var unitTestChart = null; // 单元测试情况图表
        var subjectAbilityChart = null; // 学科能力养成图表
        var knowledgeModuleChart = null; // 知识板块掌握度图表

        YQ.voxLogs({
            database: databaseLogs,
            module: moduleName,
            op: "learning_analysis_load",
            s0: "校长"
        });
        $.extend(self, {
            analysisDateList: ko.observableArray([]), // 学情分析-时间
            analysisGradeList: ko.observableArray([]), // 学情分析-年级
            analysisSubjectList: ko.observableArray([]), // 学情分析-学科
            analysisGradeClazzList: ko.observableArray([]), // 学情分析-年级班级列表（存储接口返回的数据）
            analysisClazzList: ko.observableArray([]), // 学情分析-班级

            choiceDateInfo: ko.observable({}), // 选择的时间信息
            choiceGradeInfo: ko.observable({}), // 选择的年级信息
            choiceSubjectInfo: ko.observable({}), // 选择的学科信息
            choiceClazzInfo: ko.observable({}), // 选择的班级信息

            isShowDateSelect: ko.observable(false), // 是否展示时间下拉框
            isShowGradeSelect: ko.observable(false), // 是否展示年级下拉框
            isShowSubjectSelect: ko.observable(false), // 是否展示学科下拉框
            isShowClazzSelect: ko.observable(false), // 是否展示学科下拉框

            isShowUnitTestChartEmptyTip: ko.observable(false), // 是否提示单元测试情况为空
            isShowSubjectAbilityChartEmptyTip: ko.observable(false), // 是否提示学科能力养成为空
            isShowKnowledgeModuleChartEmptyTip: ko.observable(false), // 是否提示学科能力养成为空

            knowledgePlateIndex: ko.observable(1), // 当前知识板块

            // 点击时间
            showDateSelect: function () {
                self.isShowDateSelect(!self.isShowDateSelect());
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
                self.isShowClazzSelect(false);
            },
            // 点击年级
            showGradeSelect: function () {
                self.isShowGradeSelect(!self.isShowGradeSelect());
                self.isShowDateSelect(false);
                self.isShowSubjectSelect(false);
                self.isShowClazzSelect(false);
            },
            // 点击学科
            showSubjectSelect: function () {
                self.isShowSubjectSelect(!self.isShowSubjectSelect());
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
                self.isShowClazzSelect(false);
            },
            // 点击学科
            showClazzSelect: function () {
                self.isShowClazzSelect(!self.isShowClazzSelect());
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
            },

            // 选择时间下拉
            choiceDate: function (choiceData) {
                self.choiceDateInfo(choiceData);
                self.isShowDateSelect(false);
                self.analysisClazzList(self.useTremGradeValueFindClazzList(self.choiceGradeInfo().value + '_' + self.choiceDateInfo().jie));
                self.choiceClazzInfo(self.analysisClazzList()[0]);
                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求知识板块掌握度数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "learning_analysis_term_click",
                    s0: self.choiceDateInfo().name,
                    s1: '校长'
                });
            },
            // 选择年级下拉
            choiceGrade: function (choiceData) {
                self.choiceGradeInfo(choiceData);
                self.isShowGradeSelect(false);
                // 刷新对应的班级列表
                self.analysisClazzList(self.useTremGradeValueFindClazzList(self.choiceGradeInfo().value + '_' + self.choiceDateInfo().jie));
                self.choiceClazzInfo(self.analysisClazzList()[0]);
                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求知识板块掌握度数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "learning_analysis_grade_click",
                    s0: self.choiceGradeInfo().name,
                    s1: '校长'
                });
            },
            // 选择学科下拉
            choiceSubject: function (choiceData) {
                self.choiceSubjectInfo(choiceData);
                self.isShowSubjectSelect(false);
                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求知识板块掌握度数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "learning_analysis_subject_click",
                    s0: self.choiceSubjectInfo().value
                });
            },
            // 选择班级下拉
            choiceClazz: function (choiceData) {
                self.choiceClazzInfo(choiceData);
                self.isShowClazzSelect(false);
                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id
                ); // 接口请求知识板块掌握度数据
            },
            // 通过选中的term和grade 去 gradeClazz中查找对应的clazzList
            useTremGradeValueFindClazzList: function (termGrade) {
                var gradeClazzList = self.analysisGradeClazzList();
                var classList = [];
                for (var i = 0; i < gradeClazzList.length; i++) {
                    if (gradeClazzList[i].grade === termGrade) {
                        classList = gradeClazzList[i].clazzList;
                    }
                }
                return classList;
            },

            // 切换知识板块
            changeKnowledgePlate: function (index) {
                if (self.knowledgePlateIndex() === index) return ;

                self.knowledgePlateIndex(index);
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value,
                    self.choiceClazzInfo().id,
                    self.knowledgePlateIndex()
                );
            }
        });

        // 请求条件数据
        var getAnalysisListInfo = function () {
            $.ajax({
                url: '/schoolmaster/loadUnitAvgQuestionsCondition.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.result) {
                        self.analysisDateList(res.termList); // 时间列表
                        self.analysisGradeList(res.gradeList); // 年级列表
                        self.analysisSubjectList(res.subjectList); // 学科列表
                        self.analysisGradeClazzList(res.gradeClazzList); // 时间-年级 班级列表

                        self.choiceDateInfo(self.analysisDateList()[self.analysisDateList().length - 1]); // 默认选择的时间信息
                        self.choiceGradeInfo(self.analysisGradeList()[0]); // 默认选择的年级信息
                        self.analysisClazzList(self.useTremGradeValueFindClazzList(self.choiceGradeInfo().value + '_' + self.choiceDateInfo().jie)); // 学期和年级 对应班级列表
                        self.choiceSubjectInfo(self.analysisSubjectList()[0]); // 默认选择的学科信息
                        self.choiceClazzInfo(self.analysisClazzList()[0]); // 默认选择的班级信息


                        getUnitTestInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSubjectInfo().value,
                            self.choiceClazzInfo().id
                        ); // 请求首屏单元训练情况数据
                        getSubjectAbilityInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSubjectInfo().value,
                            self.choiceClazzInfo().id
                        ); // 请求首屏学科能力养成数据
                        getKnowledgeModuleInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSubjectInfo().value,
                            self.choiceClazzInfo().id
                        ); // 接口请求知识板块掌握度数据
                    } else {
                        alertError(res.info || '出错了，请重试！');
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            });
        };

        // 请求单元训练情况数据
        var getUnitTestInfo = function (dataValue, gradeValue, subjectValue, clazzValue) {
            $.ajax({
                url: '/schoolmaster/loadUnitAvgQuestions.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    subject: subjectValue || '',
                    clazz: clazzValue || ''
                },
                success: function (res) {
                    unitTestChart.hideLoading();
                    unitTestChart.clear();

                    if (res.result) {
                        // 单元测试情况
                        if (res.seriesData.length) {
                            // 绘制ECharts
                            drawUnitTestChart(res);
                            self.isShowUnitTestChartEmptyTip(false);
                        } else {
                            self.isShowUnitTestChartEmptyTip(true);
                        }
                    } else {
                        self.isShowUnitTestChartEmptyTip(true);
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            });
        };

        // 请求学科能力养成数据
        var getSubjectAbilityInfo = function (dataValue, gradeValue, subjectValue, clazzValue) {
            $.ajax({
                url: '/schoolmaster/loadLearningSkills.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    subject: subjectValue || '',
                    clazz: clazzValue || ''
                },
                success: function (res) {
                    subjectAbilityChart.hideLoading();
                    subjectAbilityChart.clear();

                    if (res.result) {
                        // 学科能力养成
                        if (res.barSeriesData.length) {
                            // 绘制ECharts
                            drawSubjectAbilityChart(res);
                            self.isShowSubjectAbilityChartEmptyTip(false);
                        } else {
                            self.isShowSubjectAbilityChartEmptyTip(true);
                        }
                    } else {
                        self.isShowSubjectAbilityChartEmptyTip(true);
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            });
        };

        // 请求知识板块掌握度数据
        var getKnowledgeModuleInfo = function (dataValue, gradeValue, subjectValue, clazzValue, plateIndex) {
            $.ajax({
                url: '/schoolmaster/loadKnowledgeModule.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    subject: subjectValue || '',
                    clazz: clazzValue || '',
                    knowledgeModuleLevel: plateIndex || 1
                },
                success: function (res) {
                    knowledgeModuleChart.hideLoading();
                    knowledgeModuleChart.clear();

                    if (res.result) {
                        // 知识板块掌握度
                        if (res.barSeriesData.length) {
                            // 绘制ECharts
                            drawKnowledgeModuleChart(res);
                            self.isShowKnowledgeModuleChartEmptyTip(false);
                        } else {
                            self.isShowKnowledgeModuleChartEmptyTip(true);
                        }
                    } else {
                        self.isShowKnowledgeModuleChartEmptyTip(true);
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
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

        // 初始化EChart图表，然后异步请求图表数据，再填充
        var drawInitChart = function () {
            // 初始化init
            unitTestChart = echarts.init(document.getElementById('unitTestChart'), EChartsTheme); // 单元训练情况
            subjectAbilityChart = echarts.init(document.getElementById('subjectAbilityChart'), EChartsTheme); // 学科能力养成情况
            knowledgeModuleChart = echarts.init(document.getElementById('knowledgeModuleChart'), EChartsTheme); // 知识板块掌握度情况

            // loading加载
            unitTestChart.showLoading();
            subjectAbilityChart.showLoading();
            knowledgeModuleChart.showLoading();
        };

        // 绘制单元训练情况
        var drawUnitTestChart = function (res) {
            var seriesData = res.seriesData;
            for (var i = 0, len = seriesData.length; i < len; i++) {
                seriesData[i].barMaxWidth = 150;
                if (seriesData[i].type === 'line') {
                    seriesData[i].yAxisIndex = 1; // 折线图（正确率使用1号y轴，默认使用0号y轴）
                }
            }
            unitTestChart.setOption({
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross',
                        crossStyle: {
                            color: '#999'
                        }
                    }
                },
                // toolbox: {
                //     right: 5,
                //     feature: {
                //         dataView: {show: true, readOnly: false},
                //         magicType: {show: true, type: ['line', 'bar']},
                //         restore: {show: true},
                //         saveAsImage: {show: true}
                //     }
                // },
                legend: {
                    right: 0,
                    top: 1,
                    data: res.legendData
                },
                // 底下dragbar
                dataZoom: [{
                    endValue: res.unitNames[4] // 默认展示0~4
                }, {
                    type: 'slider'
                }],
                xAxis: [{
                    type: 'category',
                    data: res.unitNames,
                    splitLine: {
                        show: false
                    },
                    axisPointer: {
                        type: 'shadow'
                    }
                }],
                yAxis: [{
                    type: 'value',
                    name: '人均题数',
                    min: 0,
                    interval: 50,
                    axisLabel: {
                        formatter: '{value}'
                    }
                }, {
                    type: 'value',
                    name: '正确率 %',
                    min: 0,
                    max: 100,
                    interval: 25,
                    axisLabel: {
                        formatter: '{value}'
                    }
                }],
                grid: {
                    top: 70,
                    left: 100,
                    right: 100,
                    bottom: 70
                },
                series: seriesData
            });
        };

        // 绘制学科能力养成
        var drawSubjectAbilityChart = function (res) {
            var barSeriesData = res.barSeriesData;

            for (var i = 0, len = barSeriesData.length; i < len; i++) {
                barSeriesData[i].type = 'bar';
                barSeriesData[i].barMaxWidth = 100;
                barSeriesData[i].label = {
                    normal: {
                        show: true,
                        position: 'top'
                    }
                };
            }
            subjectAbilityChart.setOption({
                legend: {
                    right: 0,
                    top: 1,
                    data: res.barLegendData
                },
                tooltip : {
                    trigger: 'axis',
                    axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                        type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                    }
                },
                xAxis: {
                    type: 'category',
                    // name: '学科', // 坐标轴名称
                    axisTick: {
                        show: false // 坐标轴刻度
                    },
                    splitLine: {
                        show: true // 纵向分割线
                    },
                    axisLine: {
                        lineStyle: {
                            color: '#e0e0e0'
                        }
                    },
                    axisLabel: {
                        interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                    },
                    data: res.barSkillData
                },
                yAxis: {
                    show: false,
                    type: 'value',
                    // name: '分值',
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    }
                },
                grid: {
                    top: 70,
                    left: 100,
                    right: 100,
                    bottom: 48
                },
                series: barSeriesData
            });
        };

        // 绘制知识板块掌握度
        var drawKnowledgeModuleChart = function (res) {
            var barSeriesData = res.barSeriesData;

            for (var i = 0, len = barSeriesData.length; i < len; i++) {
                barSeriesData[i].type = 'bar';
                barSeriesData[i].barMaxWidth = 100;
                barSeriesData[i].label = {
                    normal: {
                        show: res.barSkillData.length <= 5 ? true : false,
                        position: 'top'
                    }
                };
            }
            knowledgeModuleChart.setOption({
                legend: {
                    right: 0,
                    top: 50,
                    data: res.barLegendData
                },
                tooltip : {
                    trigger: 'axis',
                    axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                        type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                    }
                },
                // 底下dragbar
                dataZoom: [{
                    endValue: res.barSkillData[4] // 默认展示0~4
                }, {
                    type: 'slider',
                }],
                xAxis: {
                    type: 'category',
                    // name: '学科', // 坐标轴名称
                    axisTick: {
                        show: false // 坐标轴刻度
                    },
                    splitLine: {
                        show: res.barSkillData.length <= 5 ? true : false // 纵向分割线
                    },
                    axisLine: {
                        lineStyle: {
                            color: '#e0e0e0'
                        }
                    },
                    axisLabel: {
                        interval: res.barSkillData.length <= 5 ? 0 : 'auto' // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                    },
                    data: res.barSkillData
                },
                yAxis: {
                    show: res.barSkillData.length <= 5 ? false : true,
                    type: 'value',
                    // name: '分值',
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    }
                },
                grid: {
                    top: 100,
                    left: 100,
                    right: 100,
                    // bottom: 48
                    bottom: 64
                },
                series: barSeriesData
            });
        };

        // 绑定全局事件
        var bindGlobalEvent = function () {
            // 点击页面收起所有的下拉
            $(document).on('click', function () {
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
                self.isShowClazzSelect(false);
            });

            // 听力卷TTS打点
            $(document).on('click', '.trackTTS', function () {
                YQ.voxLogs({
                    database: databaseLogs,
                    module: 'm_V254hUwf',
                    op: "listening_test_click"
                });
            });
        };

        // 初始化触发
        getAnalysisListInfo();
        drawInitChart();
        bindGlobalEvent();
    };

    var tModal = new learninganalysisModal();
    ko.applyBindings(tModal, document.getElementById("learninganalysis"));
});