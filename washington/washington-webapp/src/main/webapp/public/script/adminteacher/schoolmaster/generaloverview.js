/**
 * @author: pengmin.chen
 * @description: "校长账号-总体概览"
 * @createdDate: 2018.06.19
 * @lastModifyDate: 2018.06.20
 */

// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗, echarts: 图表
define(["jquery", "knockout", "YQ", "echarts", "echarts-adminteacher", "knockout-switch-case", "impromptu", "voxLogs"],function($, ko, YQ, echarts){
    var generaloverviewModal = function () {
        var self = this;
        var databaseLogs = "tianshu_logs";
        var moduleName = "m_V254hUwf";
        var EChartsTheme = 'walden';
        var teacherUseChart = null; // 老师使用情况图表chaetr
        var studentUseChart = null; // 学生使用情况图表
        var typePercentChart = null; // 作业情况-类型占比图表
        var numContrastChart = null; // 作业情况-类型及数量对比图表
        var detailChart = null; // 弹窗图例

        YQ.voxLogs({
            database: databaseLogs,
            module: moduleName,
            op: "overview_load",
            s0: "校长"
        });
        // 切换看板打点（天枢v2.0）
        $('#switchBtn').on('click',function () {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_rXBC8BoT60',
                op: "click_overview_page_switch_to_data_superversion",
            });
        });

        $.extend(self, {
            teacherCurMonthUseNum: ko.observable(0), // 老师本月使用人数
            teacherCurMonthAddNum: ko.observable(0), // 老师本月新增人数
            studentCurMonthUseNum: ko.observable(0), // 学生本月使用人数
            studentCurMonthAddNum: ko.observable(0), // 学生本月新增人数
            choiceTeacherType: ko.observable(1), // 老师本月选择的type
            choiceStudentType: ko.observable(1), // 学生本月选择的type

            homeworkDateList: ko.observableArray([]), // 作业情况-时间列表
            homeworkGradeList: ko.observableArray([]), // 作业情况-年级列表
            homeworkSubjectList: ko.observableArray([]), // 作业情况-学科列表
            choiceDateInfo: ko.observable({}), // 选择的时间信息
            choiceGradeInfo: ko.observable({}), // 选择的年级信息
            choiceSubjectInfo: ko.observable({}), // 选择的学科信息
            isShowDateSelect: ko.observable(false), // 是否展示时间下拉框
            isShowGradeSelect: ko.observable(false), // 是否展示年级下拉框
            isShowSubjectSelect: ko.observable(false), // 是否展示学科下拉框

            isShowTeacherUseChartEmptyTip: ko.observable(false), // 老师使用情况为空提示
            isShowStudentUseChartEmptyTip: ko.observable(false), // 学生使用情况为空提示
            isShowTypePercentChartEmptyTip: ko.observable(false), // 类型占比为空提示
            isShowNumContrastChartEmptyTip: ko.observable(false), // 类型及数量对比为空提示

            isShowDeatilChart: ko.observable(false), // 是否展示弹窗图表
            detailChartMaskHeight: ko.observable(''), // 弹窗图表蒙层的高度
            detailChartMarginTop: ko.observable(0), // 弹窗的偏移（由于弹窗过大故未使用绝对定位）
            detailChartTitle: ko.observable(''), // 弹窗图表的标题
            detailChartData: ko.observable(''), // 弹窗图表的数据

            clazzHomeworkList: ko.observableArray([]), // 班级作业排行榜
            teacherAssignmentList: ko.observableArray([]), // 老师布置排行榜


            // 点击老师使用情况type
            reqeustTeacherInfo: function (index) {
                getBaseInfo(index, self.choiceStudentType());
                self.choiceTeacherType(index);
                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: index === 1 ? 'overview_teacher_user_button_click' : 'overview_teacher_new_button_click'
                });
            },
            // 点击学生使用情况type
            reqeustStudentInfo: function (index) {
                getBaseInfo(self.choiceTeacherType(), index);
                self.choiceStudentType(index);
                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: index === 1 ? 'overview_student_user_button_click' : 'overview_student_new_button_click'
                });
            },

            // 点击作业情况-时间
            showDateSelect: function () {
                self.isShowDateSelect(!self.isShowDateSelect());
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
            },
            // 点击作业情况-年级
            showGradeSelect: function () {
                self.isShowGradeSelect(!self.isShowGradeSelect());
                self.isShowDateSelect(false);
                self.isShowSubjectSelect(false);
            },
            // 点击作业情况-学科
            showSubjectSelect: function () {
                self.isShowSubjectSelect(!self.isShowSubjectSelect());
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
            },
            // 选择作业情况-时间下拉
            choiceDate: function (choiceData) {
                self.choiceDateInfo(choiceData);
                self.isShowDateSelect(false);
                getHomeworkInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value
                ); // 接口请求数据
            },
            // 选择作业情况-年级下拉
            choiceGrade: function (choiceData) {
                self.choiceGradeInfo(choiceData);
                self.isShowGradeSelect(false);
                getHomeworkInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value
                ); // 接口请求数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: 'overview_grade_click',
                    s0: self.choiceGradeInfo().name,
                    s1: '校长'
                });
            },
            // 选择作业情况-学科下拉
            choiceSubject: function (choiceData) {
                self.choiceSubjectInfo(choiceData);
                self.isShowSubjectSelect(false);
                getHomeworkInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSubjectInfo().value
                ); // 接口请求数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: 'overview_subject_click',
                    s0: self.choiceSubjectInfo().value
                });
            },
            // 点击图表详情
            showChartDetail: function (type, title) {
                self.isShowDeatilChart(true);
                self.detailChartTitle(title);
                self.detailChartMaskHeight($(document).height() + 'px');
                self.detailChartMarginTop(($(document).scrollTop() + 100) + 'px');

                drawDetailChart(type, self.detailChartData());
            },
            // 隐藏图标详情弹窗
            hideDetailChart: function () {
                self.isShowDeatilChart(false);
                detailChart.dispose();
            }
        });

        // 获取老师、学生使用情况
        var getBaseInfo = function (teacherViewType, studentViewType) {
            $.ajax({
                url: '/schoolmaster/loadSchoolUsageData.vpage',
                type: 'GET',
                data: {
                    teacViewType: teacherViewType || 1,
                    studViewType: studentViewType || 1
                },
                success: function (res) {
                    teacherUseChart.hideLoading();
                    studentUseChart.hideLoading();

                    if (res.result) {
                        self.teacherCurMonthUseNum(res.usageTeachers < 100000 ? res.usageTeachers : convertUnit(res.usageTeachers, 1000, 0) + 'k'); // 小于100000直接显示，否则以K单位展示
                        self.teacherCurMonthAddNum(res.increaseTeachers < 100000 ? res.increaseTeachers : convertUnit(res.increaseTeachers, 1000, 0) + 'k');
                        self.studentCurMonthUseNum(res.usageStudents < 100000 ? res.usageStudents : convertUnit(res.usageStudents, 1000, 0) + 'k');
                        self.studentCurMonthAddNum(res.increaseStudents < 100000 ? res.increaseStudents : convertUnit(res.increaseStudents, 1000, 0) + 'k');

                        // 老师使用情况
                        if (res.teacSeries.length) {
                            // 绘制ECharts
                            drawTeacherUseChart(res);
                            self.isShowTeacherUseChartEmptyTip(false);
                        } else {
                            self.isShowTeacherUseChartEmptyTip(true);
                        }

                        // 学生使用情况
                        if (res.studSeries.length) {
                            // 绘制ECharts
                            drawStudentUseChart(res);
                            self.isShowStudentUseChartEmptyTip(false);
                        } else {
                            self.isShowStudentUseChartEmptyTip(true);
                        }
                    } else {
                        self.isShowTeacherUseChartEmptyTip(true);
                        self.isShowStudentUseChartEmptyTip(true);
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            });
        };

        // 获取作业列表
        var getHomeworkListInfo = function () {
            $.ajax({
                url: '/schoolmaster/loadHomeworkCondition.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.result) {
                        self.homeworkDateList(res.dateList);
                        self.homeworkGradeList(res.gradeList);
                        self.homeworkSubjectList(res.subjectList);

                        self.choiceDateInfo(self.homeworkDateList()[self.homeworkDateList().length - 1]); // 默认选择的时间信息
                        self.choiceGradeInfo(self.homeworkGradeList()[0]); // 默认选择的年级信息
                        self.choiceSubjectInfo(self.homeworkSubjectList()[0]); // 默认选择的学科信息

                        getHomeworkInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSubjectInfo().value
                        );
                    } else {
                        alertError(res.info || '出错了，请重试！');
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            })
        };

        // 获取作业情况
        var getHomeworkInfo = function (date, grade, subject) {
            $.ajax({
                url: '/schoolmaster/loadHomework.vpage',
                type: 'GET',
                data: {
                    dateStr: date || '',
                    grade: grade || '',
                    subject: subject || ''
                },
                success: function (res) {
                    typePercentChart.hideLoading();
                    numContrastChart.hideLoading();
                    typePercentChart.clear();
                    numContrastChart.clear();

                    if (res.result) {
                        self.clazzHomeworkList(res.clazzDoHomeworkData);
                        self.teacherAssignmentList(res.assignmentData);
                        self.detailChartData(res);

                        // 饼状图
                        if (res.pieSeriesData.length) {
                            // 绘制ECharts
                            drawTypePercentChart(res);
                            self.isShowTypePercentChartEmptyTip(false);
                        } else {
                            self.isShowTypePercentChartEmptyTip(true);
                        }

                        // 柱状图
                        if (res.barSeriesData.length) {
                            // 绘制ECharts
                            drawNumContrastChart(res);
                            self.isShowNumContrastChartEmptyTip(false);
                        } else {
                            self.isShowNumContrastChartEmptyTip(true);
                        }
                    } else {
                        // 此处重新赋空的原因：切换条件会重新请求，需要清掉上一次的结果
                        self.clazzHomeworkList([]);
                        self.teacherAssignmentList([]);
                        self.detailChartData('');

                        self.isShowTypePercentChartEmptyTip(true);
                        self.isShowNumContrastChartEmptyTip(true);
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            })
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

        // 转换成K(参数num，0表示不保留位数，10表示保留1位, 100表示保留2位，以此类推)
        var convertUnit = function (a, b, num) {
            if (num) {
                return (parseInt((a / b) * num) / num); // 不进行四舍五入
            } else {
                return (parseInt(a / b)); // 不保留位数
            }
        };

        // 初始化EChart图表，然后异步请求图表数据，再填充
        var drawInitChart = function () {
            // 初始化init
            teacherUseChart = echarts.init(document.getElementById('teacherUseChart'), EChartsTheme); // 老师使用情况
            studentUseChart = echarts.init(document.getElementById('studentUseChart'), EChartsTheme); // 学生使用情况
            typePercentChart = echarts.init(document.getElementById('typePercentChart'), EChartsTheme); // 作业类型占比
            numContrastChart = echarts.init(document.getElementById('numContrastChart'), EChartsTheme); // 作业类型及数量占比

            // loading加载
            teacherUseChart.showLoading();
            studentUseChart.showLoading();
            typePercentChart.showLoading();
            numContrastChart.showLoading();
        };

        // 绘制老师使用情况ECharts图表
        var drawTeacherUseChart = function (res) {
            var teacSeries = res.teacSeries;
            for (var i = 0, len = teacSeries.length; i < len; i++) {
                teacSeries[i].type = 'bar';
                teacSeries[i].barMaxWidth = 100;
            }
            teacherUseChart.setOption({
                legend: {
                    right: 0,
                    top: 1,
                    data: res.teacLegendData
                },
                tooltip : {
                    trigger: 'axis',
                    axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                        type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                    }
                },
                xAxis: {
                    type: 'category',
                    name: '年级', // 坐标轴名称
                    axisTick: {
                        show: false // 坐标轴刻度
                    },
                    splitLine: {
                        show: false // 纵向分割线
                    },
                    data: res.teacGradeData
                },
                yAxis: {
                    type: 'value',
                    name: '人数',
                    axisTick: {
                        show: false
                    }
                },
                grid: { // 图例位置
                    top: 60,
                    right: 46,
                    bottom: 40
                },
                series: teacSeries
            });
        };

        // 绘制学生使用情况ECharts图表
        var drawStudentUseChart = function (res) {
            studentUseChart.setOption({
                tooltip : {
                    trigger: 'axis',
                    axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                        type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                    }
                },
                xAxis: {
                    type: 'category',
                    name: '年级',
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    },
                    data: res.studGradeData
                },
                yAxis: {
                    type: 'value',
                    name: '人数',
                    axisTick: {
                        show: false
                    }
                },
                grid: {
                    top: 60,
                    right: 46,
                    bottom: 40
                },
                series : [{
                    name: '学生使用情况',
                    type: 'bar',
                    barMaxWidth: 100,
                    data: res.studSeries
                }]
            })
        };

        // 绘制类型占比ECharts图表
        var drawTypePercentChart = function (res) {
            typePercentChart.setOption({
                tooltip : {
                    trigger: 'item',
                    formatter: "{a} <br/>{b} : {c} ({d}%)"
                },
                legend: {
                    show: false
                },
                series : [{
                    name: '类型占比',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data: res.pieSeriesData,
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            });
        };

        // 绘制类型及数量对比ECharts图表
        var drawNumContrastChart = function (res) {
            var barSeriesData = res.barSeriesData;
            for (var i = 0, len = barSeriesData.length; i < len; i++) {
                barSeriesData[i].type = 'bar';
                barSeriesData[i].stack = '总量';
                barSeriesData[i].barMaxWidth = 100;
            }
            numContrastChart.setOption({
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                        type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                    }
                },
                xAxis: {
                    type: 'category',
                    name: '周',
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    },
                    data: res.barWeekData
                },
                yAxis: {
                    type: 'value',
                    name: '题数',
                    axisTick: {
                        show: false
                    }
                },
                grid: {
                    top: 110,
                    left: 50,
                    right: 30,
                    bottom: 70
                },
                legend: {
                    show: false
                },
                series: barSeriesData
            });
        };

        // 绘制弹窗详情图表
        var drawDetailChart = function (type, res) {
            detailChart = echarts.init(document.getElementById('detailChart'), EChartsTheme);
            detailChart.showLoading();
            if (res.result)  detailChart.hideLoading();
            if (type === 'typePercentChart') {
                detailChart.setOption({
                    tooltip : {
                        trigger: 'item',
                        formatter: "{a} <br/>{b} : {c} ({d}%)"
                    },
                    legend: {
                        top: 10,
                        data: res.pieLegendData
                    },
                    series : [{
                        name: '类型占比',
                        type: 'pie',
                        radius : '55%',
                        center: ['50%', '60%'],
                        data: res.pieSeriesData,
                        itemStyle: {
                            emphasis: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        }
                    }]
                });
            } else if (type === 'numContrastChart') {
                var barSeriesData = res.barSeriesData;
                for (var i = 0, len = barSeriesData.length; i < len; i++) {
                    barSeriesData[i].type = 'bar';
                    barSeriesData[i].stack = '总量';
                    barSeriesData[i].barMaxWidth = 150;
                }
                detailChart.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    xAxis: {
                        type: 'category',
                        name: '周',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        data: res.barWeekData
                    },
                    yAxis: {
                        type: 'value',
                        name: '题数',
                        axisTick: {
                            show: false
                        }
                    },
                    grid: {
                        top: 155,
                        left: 120,
                        right: 120,
                        bottom: 40
                    },
                    legend: {
                        top: 10,
                        data: res.barLegendData
                    },
                    series: barSeriesData
                });
            }
        };

        // 绑定全局事件
        var bindGlobalEvent = function () {
            // 点击页面收起所有的下拉
            $(document).on('click', function () {
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
                self.isShowSubjectSelect(false);
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
        getBaseInfo();
        getHomeworkListInfo();
        drawInitChart();
        bindGlobalEvent();
    };

    var tModal = new generaloverviewModal();
    ko.applyBindings(tModal, document.getElementById("generaloverview"));
});