/**
 * @author: pengmin.chen
 * @description: "教研员账号-学情分析"
 * @createdDate: 2018.06.19
 * @lastModifyDate: 2018.06.20
 */

// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗, echarts: 图表
define(["jquery", "knockout", "YQ", "echarts", "echarts-adminteacher", "knockout-switch-case", "impromptu", "voxLogs"],function($, ko, YQ, echarts){
    var learninganalysisModal = function () {
        var self = this;
        var databaseLogs = "tianshu_logs";
        var moduleName = "m_JCe0IYhO";
        var EChartsTheme = 'walden';
        var unitTestChart = null; // 单元测试图表
        var subjectAbilityChart = null; // 学科能力养成图表
        var knowledgeModuleChart = null; // 知识板块掌握度图表

        YQ.voxLogs({
            database: databaseLogs,
            module: moduleName,
            op: "learning_analysis_load",
            s0: "教研员"
        });
        $.extend(self, {
            analysisDateList: ko.observableArray([]), // 学情分析-时间
            analysisGradeList: ko.observableArray([]), // 学情分析-年级
            analysisCityList: ko.observableArray([]), // 学情分析-区（两级列表）
            analysisSchoolList: ko.observableArray([]), // 学校分享-区-学校
            analysisSearchSchoolList: ko.observableArray([]), // 学情分析-学校搜索

            choiceDateInfo: ko.observable({}), // 选择的时间信息
            choiceGradeInfo: ko.observable({}), // 选择的年级信息
            choiceCityInfo: ko.observable({}), // 选择的区信息
            hoverCityInfo: ko.observable({}), // hover状态的区信息
            choiceSchoolInfo: ko.observable({}), // 选择的学校信息
            choiceSearchSchoolInfo: ko.observable({}), // 选择的学校信息
            searchInputText: ko.observable(''), // 实时搜索输入的文字

            isShowDateSelect: ko.observable(false), // 是否展示时间下拉框
            isShowGradeSelect: ko.observable(false), // 是否展示年级下拉框
            isShowCitySelect: ko.observable(false), // 是否展示区下拉框
            isShowSchoolSelect: ko.observable(false), // 是否展示区-学校下拉框
            isShowCitySchoolNameTip: ko.observable(false), // 是否展示学校名过长提示
            isShowSearchSchoolSelect: ko.observable(false), // 是否展示学校搜索下拉框

            isShowUnitTestChartEmptyTip: ko.observable(false), // 是否提示单元测试情况为空
            isShowSubjectAbilityChartEmptyTip: ko.observable(false), // 是否提示学科能力养成为空
            isShowKnowledgeModuleChartEmptyTip: ko.observable(false), // 是否提示学科能力养成为空

            schoolSelectTopValue: ko.observable(0), // 区-学校下拉框top值
            isMouseOverCity: ko.observable(false), // 鼠标是否滑过区一级下拉
            isMouseOverSchoolList: ko.observable(false), // 鼠标是否滑动学校二级下拉

            knowledgePlateIndex: ko.observable(1), // 当前知识板块

            // 点击时间
            showDateSelect: function () {
                self.isShowDateSelect(!self.isShowDateSelect());
                self.isShowGradeSelect(false);
                self.isShowCitySelect(false);
                self.isShowSearchSchoolSelect(false);
            },
            // 点击年级
            showGradeSelect: function () {
                self.isShowGradeSelect(!self.isShowGradeSelect());
                self.isShowDateSelect(false);
                self.isShowCitySelect(false);
                self.isShowSearchSchoolSelect(false);
            },
            mouseoverCityList: function (data, event) {
                event.stopPropagation();
            },
            mouseoutCityList: function (data, event) {
                event.stopPropagation();
            },
            // 点击城市学校
            showCitySelect: function () {
                self.isShowCitySelect(!self.isShowCitySelect());
                self.isShowDateSelect(false);
                self.isShowGradeSelect(false);
                self.isShowSearchSchoolSelect(false);
            },
            // 鼠标over学校名(学校名长度大于6展示提示)
            mouseoverCitySchoolSelect: function () {
                if (self.choiceSchoolInfo().schoolName.length > 6) {
                    self.isShowCitySchoolNameTip(true);
                }
            },
            // 鼠标out学校名
            mouseoutCitySchoolSelect: function () {
                self.isShowCitySchoolNameTip(false);
            },
            //  鼠标out 学校名tip
            mouseoutCitySchoolTip: function () {
                self.isShowCitySchoolNameTip(false);
            },
            // 鼠标over区
            mouseoverCity: function (index, cityInfo) {
                self.isMouseOverCity(true);
                self.hoverCityInfo(cityInfo);
                self.analysisSchoolList(cityInfo.schoolList);
                self.isShowSchoolSelect(true);

                // 根据区的index来确定schoolList的坐标top值
                if (self.analysisCityList().length <= 7) {
                    self.schoolSelectTopValue(40 * (index + 1) + 'px');
                } else {
                    var scrollTop = document.getElementsByClassName('JS-CityBox')[0].scrollTop;
                    self.schoolSelectTopValue((40 * index - scrollTop + 40) + 'px');
                }

                // 确定schoolList的scrollTop值（定位到指定学校位置）
                if (self.hoverCityInfo().cityId === self.choiceCityInfo().cityId) { // 滑过时当前选中的，定位值指定位置
                    // 根据当前已选中或输入的学校scrollTop值
                    for (var i = 0; i < self.analysisSchoolList().length; i++) {
                        if (self.analysisSchoolList()[i].schoolId === self.choiceSchoolInfo().schoolId) {
                            $('.JS-SchoolBox').scrollTop(i * 40);
                            break;
                        }
                    }
                } else { // 统一置顶
                    $('.JS-SchoolBox').scrollTop(0);
                }
            },
            // 鼠标out区
            mouseoutCity: function () {
                self.isMouseOverCity(false);

                // 延迟原因：当区列表（一级下拉）有滚动条是，mouseout li后会先隐藏schoolList，但是可能鼠标来不及移到二级下拉上
                setTimeout(function () {
                    // 鼠标不在区li上 且 不在城市列表ul上
                    if (!self.isMouseOverCity() && !self.isMouseOverSchoolList()) {
                        self.isShowSchoolSelect(false);
                    }
                    // 鼠标在区列表li上 或 在城市列表ul上
                    if (self.isMouseOverCity() || self.isMouseOverSchoolList()) {
                        self.isShowSchoolSelect(true);
                    }
                }, 500);
            },
            // 鼠标over学校
            mouseoverSchool: function (data, event) {
                self.isMouseOverSchoolList(true);
                self.isShowSchoolSelect(true);
                event.stopPropagation();
            },
            // 鼠标out学校
            mouseoutSchool: function (data, event) {
                self.isMouseOverSchoolList(false);
                self.isShowSchoolSelect(false);
                event.stopPropagation();
            },
            // 选择时间下拉
            choiceDate: function (choiceData) {
                self.choiceDateInfo(choiceData);
                self.isShowDateSelect(false);

                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求知识板块掌握度数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "learning_analysis_term_click",
                    s0: self.choiceDateInfo().name,
                    s1: "教研员"
                });
            },
            // 选择年级下拉
            choiceGrade: function (choiceData) {
                self.choiceGradeInfo(choiceData);
                self.isShowGradeSelect(false);

                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求知识板块掌握度数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "learning_analysis_grade_click",
                    s0: self.choiceGradeInfo().name,
                    s1: "教研员"
                });
            },
            // 选择学校下拉
            choiceSchool: function (choiceData) {
                self.choiceCityInfo(self.hoverCityInfo());
                self.choiceSchoolInfo(choiceData);
                self.isShowCitySelect(false);
                self.isShowSchoolSelect(false);
                // 清空输入框
                self.searchInputText('');

                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求知识板块掌握度数据

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "learning_analysis_school_click"
                });
            },
            // 选择学校搜索下拉
            choiceSearchSchool: function (choiceData) {
                self.choiceSearchSchoolInfo(choiceData);
                self.isShowSearchSchoolSelect(false);
                self.searchInputText(choiceData.schoolName);

                // 刷新对应的学校列表
                self.refreshCitySchoolChoice(choiceData);
            },
            // 根据搜索结果刷新学校列表选中情况
            refreshCitySchoolChoice: function (choiceData) {
                var analysisCityList = self.analysisCityList();
                for (var i = 0; i < analysisCityList.length; i++) {
                    if (choiceData.cityId === analysisCityList[i].cityId) {
                        self.choiceCityInfo(analysisCityList[i]);
                        break;
                    }
                }
                self.choiceSchoolInfo(choiceData);

                getUnitTestInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求单元训练情况数据
                getSubjectAbilityInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求学科能力养成数据
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId
                ); // 接口请求知识板块掌握度数据
            },
            // 实时搜索学校
            searchSchool: function () {
                searchSchool(self.searchInputText());
            },

            // 切换知识板块
            changeKnowledgePlate: function (index) {
                if (self.knowledgePlateIndex() === index) return ;

                self.knowledgePlateIndex(index);
                getKnowledgeModuleInfo(
                    self.choiceDateInfo().value,
                    self.choiceGradeInfo().value,
                    self.choiceSchoolInfo().schoolId,
                    self.knowledgePlateIndex()
                );
            }
        });

        // 请求条件数据
        var getAnalysisListInfo = function () {
            $.ajax({
                url: '/rstaff/loadUnitAvgResearchCondition.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.result) {
                        self.analysisDateList(res.termList);
                        self.analysisGradeList(res.gradeList);
                        self.analysisCityList(res.citySchoolList);
                        self.analysisSchoolList(res.citySchoolList[0].schoolList);

                        self.choiceDateInfo(self.analysisDateList()[self.analysisDateList().length - 1]); // 默认选择的时间信息
                        self.choiceGradeInfo(self.analysisGradeList()[0]); // 默认选择的年级信息
                        self.choiceCityInfo(self.analysisCityList()[0]);
                        self.choiceSchoolInfo(self.analysisCityList()[0].schoolList[0]);

                        getUnitTestInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSchoolInfo().schoolId
                        ); // 请求首屏单元训练情况数据
                        getSubjectAbilityInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSchoolInfo().schoolId
                        ); // 请求首屏学科能力养成数据
                        getKnowledgeModuleInfo(
                            self.choiceDateInfo().value,
                            self.choiceGradeInfo().value,
                            self.choiceSchoolInfo().schoolId
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

        // 搜索学校
        var searchSchool = function (inputSchoolInfo) {
            var inputSchoolName = inputSchoolInfo.replace(/(^\s*)|(\s*$)/g, '');
            if (!inputSchoolName) {
                self.isShowSearchSchoolSelect(false);
                return ;
            }
            $.ajax({
                url: '/rstaff/completionSchool.vpage',
                data: {
                    schoolName: inputSchoolName
                },
                success: function (res) {
                    if (res.result) {
                        self.analysisSearchSchoolList(res.schoolList);
                        self.isShowSearchSchoolSelect(true);
                    } else {
                        // alertError(res.info || '出错了，请重试！');
                        self.isShowSearchSchoolSelect(false);
                    }
                },
                error: function () {
                    alertError('出错了，请重试！');
                }
            });
        };

        // 请求单元训练情况数据
        var getUnitTestInfo = function (dataValue, gradeValue, schoolValue) {
            $.ajax({
                url: '/rstaff/loadResearchUnitAvgQuestions.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    schoolId: schoolValue || ''
                },
                success: function (res) {
                    unitTestChart.hideLoading();
                    unitTestChart.clear();

                    if (res.result) {
                        // 单元测试情况
                        if (res.seriesData.length) {
                            // 绘制ECharts
                            drawUnitChart(res);
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
        var getSubjectAbilityInfo = function (dataValue, gradeValue, schoolValue) {
            $.ajax({
                url: '/rstaff/loadLearningSkills.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    schoolId: schoolValue || ''
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
        var getKnowledgeModuleInfo = function (dataValue, gradeValue, schoolValue, plateIndex) {
            $.ajax({
                url: '/rstaff/loadKnowledgeModule.vpage',
                type: 'GET',
                data: {
                    schoolYearTerm: dataValue || '',
                    grade: gradeValue || '',
                    schoolId: schoolValue || '',
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

        // 初始化绘制图表
        var drawInitChart = function () {
            // 初始化init
            unitTestChart = echarts.init(document.getElementById('unitTestChart'), EChartsTheme); // 单元训练情况
            subjectAbilityChart = echarts.init(document.getElementById('subjectAbilityChart'), EChartsTheme); // 学科能力养成
            knowledgeModuleChart = echarts.init(document.getElementById('knowledgeModuleChart'), EChartsTheme); // 知识板块掌握度

            // loading加载
            unitTestChart.showLoading();
            subjectAbilityChart.showLoading();
            knowledgeModuleChart.showLoading();
        };

        // 绘制单元训练情况
        var drawUnitChart = function (res) {
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
                    type: 'inside'
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
                        show: res.barSkillData.length <= 5 ? true : false, // 纵向分割线
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
                self.isShowCitySelect(false);
                self.isShowSchoolSelect(false);
                self.isShowSearchSchoolSelect(false);
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