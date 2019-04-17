/**
 * @author: pengmin.chen
 * @description: "校长账号/教研员账号 模块报告"
 * @createdDate: 2018/8/1
 * @lastModifyDate: 2018/8/1
 */

// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗, echarts: 图表
define(["jquery", "knockout", "YQ", "echarts", "html2canvas", "echarts-adminteacher", "knockout-switch-case", "impromptu", "voxLogs"],function($, ko, YQ, echarts, html2canvas) {
    var testreportModal = function () {
        var self = this;
        var downloadPdfFlag = false; // 下载padf flag（防止下载中再触发）
        var base64ObjectArr = []; // 收集base64 url，最终传给后端
        var databaseLogs = "tianshu_logs";
        var moduleName = "m_6q3pjPVz";
        var EChartsTheme = 'walden';
        var breakupNum = 40; // 拆分（按40个数组拆分，40为一组，多余则生成第二张图）
        var breakupDownloadNum = 15; // 下载报告拆分（15个一组传给后端）

        $.extend(self, {
            // 公用字段
            examFullName: ko.observable(YQ.getQuery('reportName') || ''), // 报告名称
            examRegionLevel: ko.observable('市'), // 考试范围
            examTime: ko.observable(''), // 考试时间
            durationMinutes: ko.observable(0), // 答卷时长
            gradeName: ko.observable(''), // 年级
            subject: ko.observable(''), // 学科

            dataLevel: ko.observable(''), // 报告级别（city / country / school）
            viewRegionLevel: ko.observable(''), // 报告级别（市 / 区 / 学校）

            // 参与概况字段
            examSurveyEmptyFlag: ko.observable(false), // 参与概况为空标志
            examSurvey: ko.observable({}), // 参与概况基础信息
            examSurveyDetail: ko.observableArray([]), // 参与概况附图表格数据

            // 得分状况字段
            examScoreEmptyFlag: ko.observable(false), // 得分状况为空标志
            examScoreLastData: ko.observable(''), // 表现最差的1个
            examScoreTopThreeData: ko.observableArray([]), // 表现最好的3个
            examScoreWholeScore: ko.observable({}), // 得分请求
            examScoreWholeScoreDetail: ko.observableArray([]), // 得分状况附图表格数据
            examScoreCoefficient: ko.observable(0), // 离散系数
            examScoreScatterPointMaxMap: ko.observable({}), // 得分差异最大的信息
            examScoreScatterPointMinMap: ko.observable({}), // 得分差异最大的新

            // 学业水平字段
            studyLevelEmptyFlag: ko.observable(false), // 学业水平为空标志
            studyLevelInfo: ko.observableArray([]), // 学业水平各等级数据
            studyLevelWholeQualifiledRatio: ko.observable(0), // 整体合格率
            studyLevelGridDataList: ko.observableArray([]), // 学业水平附图表格数据
            studyLevelExcellentTopThreeData: ko.observableArray([]), // 优秀率最好的3个
            studyLevelGoodTopThreeData: ko.observableArray([]), // 良好率最好的3个
            studyLevelQulifiledTopThreeData: ko.observableArray([]), // 合格率最好的3个
            studyLevelQulifiledLastData: ko.observableArray([]), // 合格率率最差的3个
            studyLevelQulifiledLastDataDiff: ko.observable(0), // 合格率最低的低于平均水平值
            studyLevelUnqulifiledTopThreeData: ko.observableArray([]), // 待合格率最好的3个

            // 学科能力养成字段
            subjectAbilityEmptyFlag: ko.observable(false), // 学科能力为空标志
            subjectAbilityInfo: ko.observableArray([]), // 学科能力各能力数据
            subjectAbilityMax: ko.observable({}), // 表现最好的学科能力
            subjectAbilityMin: ko.observable({}), // 表现最差的学科能力
            subjectAbilityGrid: ko.observable({}), // 学科能力表格数据
            subjectAbilityMapList: ko.observableArray([]), // 学科能力map list

            // 知识板块掌握度字段
            knowledgePlateEmptyFlag: ko.observable(false), // 知识板块为空标志
            knowledgePlateInfo: ko.observableArray([]), // 学科能力各能力数据
            knowledgePlateMax: ko.observable({}), // 表现最好的学科能力
            knowledgePlateMin: ko.observable({}), // 表现最差的学科能力
            knowledgePlateGrid: ko.observable({}), // 学科能力表格数据
            knowledgePlateMapList: ko.observableArray([]), // 学科能力map list

            // 下载报告
            isShowDownloadBtn: ko.observable(false), // 是否显示下载按钮
            downloadReport: function () {
                // 判断非IE环境
                if ((!!window.ActiveXObject || "ActiveXObject" in window)) {
                    var ChangeBrowserDialogModal = function () {};
                    var changeBrowserDialogModal = new ChangeBrowserDialogModal();
                    var changeBrowserDialogHtml = "<div id=\"changeBrowserDialogContent\" data-bind=\"template: { name: 'changeBrowserTemp', data: self}\"></div>";
                    $.prompt(changeBrowserDialogHtml, {
                        title: '提示',
                        focus: 0,
                        position: {width: 500},
                        buttons: {"确定": true},
                        loaded: function () {
                            ko.applyBindings(changeBrowserDialogModal, document.getElementById("changeBrowserDialogContent"));
                        }
                    });
                    return ;
                }

                showDownloadLoading(); // 显示loading弹窗

                // 正在下载中时不再触发
                if (downloadPdfFlag) return ;
                downloadPdfFlag = true;

                // 异步原因：防止走预处理消耗浏览器资源导致弹窗不能出现
                setTimeout(function () {
                    if (base64ObjectArr.length) { // 已经生成过，则直接掉接口下载
                        requestGetPdfUrl();
                    } else { // 未生成过，先走前端生成流程再下载
                        prepareConvertPdfWork();
                    }
                }, 1000);

                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "exam_detail_download_click",
                    s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                    s1: self.gradeName(), // 年级
                    s2: self.subject(), // 学科
                    S3:'ACADEMIC_ACHIEVEMENT'//报告类型
                });

                // 判断该报告之前是否下载过
                // queryReportExist(function (filePath) {
                //     if (filePath) { // 之前已下载过
                //         window.open(filePath, "_blank");
                //     } else { // 之前未下载过
                //
                //     }
                // });
            }
        });

        // 查询该报告是否下载过
        var queryReportExist = function (cb) {
            $.ajax({
                url: '/schoolmaster/getExamReport.vpage',
                type: 'POST',
                data: {
                    fileKey: getHrefParamsStr()
                },
                success: function (res) {
                    if (res.result) {
                        cb(res.filePath);
                    }
                }
            });
        };

        // 显示生成loading
        var showDownloadLoading = function () {
            var ConvertReportDialogModal = function () {};
            var convertReportDialogModal = new ConvertReportDialogModal();
            var convertReportDialogHtml = "<div id=\"convertReportDialogContent\" data-bind=\"template: { name: 'convertReportTemp', data: self}\"></div>";
            $.prompt(convertReportDialogHtml, {
                title: '系统提示',
                focus: 0,
                position: {width: 360},
                buttons: {},
                loaded: function () {
                    ko.applyBindings(convertReportDialogModal, document.getElementById("convertReportDialogContent"));
                    $('.jqiclose').hide();
                }
            });
        };

        // 预处理生成pdf操作
        var prepareConvertPdfWork = function () {
            // 下载pdf流程：前端使用html2canvas将html生成canvas，然后转成base64 url，再传给后端，由后端生成pdf并下载
            var downloadSections = $('#downloadContent .loadSection:visible'); // 只操作显示的loadSection
            var covertNum = 0; // html2canvas是个异步流程，需要统计异步完成所有的操作才请求后端
            for (var i = 0; i < downloadSections.length; i++) {
                (function(i) {
                    var width = downloadSections[i].offsetWidth;
                    var height = downloadSections[i].offsetHeight;
                    var canvas = document.createElement("canvas");
                    var scale = 1.5;
                    canvas.width = width * scale;
                    canvas.height = height * scale;
                    canvas.getContext("2d").scale(scale, scale);

                    html2canvas(downloadSections[i], {
                        scale: scale,
                        canvas: canvas,
                        width: width,
                        height: height,
                        logging: false, // debug模式
                        useCORS: true // 允许跨域
                    }).then(function (canvas) {
                        covertNum++; // 计数

                        // TODO:此处不知道为什么for循环中 异步函数的i的顺序会错乱，使用自执行函数也不能解决，最终办法：搭配自执行函数，收集时带上i，然后再根据i排序，最后遍历取出base64Url组成数组
                        var base64Url = canvas.toDataURL('image/jpeg', 1.0); // toBase64
                        base64ObjectArr.push({
                            base64Url: base64Url.split(',')[1],
                            index: i
                        });

                        // 所有图片生成完毕
                        if (covertNum === downloadSections.length) {
                            requestGetPdfUrl();
                        }
                    });
                })(i);
            }
        };

        // 请求后端生成pdf
        var requestGetPdfUrl = function () {
            // 排序：解决i顺序错乱bug
            base64ObjectArr = base64ObjectArr.sort(function (a, b) {
                return a.index - b.index;
            });
            // 收集base64Url组成数组
            var base64Arr = [];
            var indexArr = [];
            for (var j = 0; j < base64ObjectArr.length; j++) {
                base64Arr.push(base64ObjectArr[j].base64Url);
                indexArr.push(base64ObjectArr[j].index);
            }

            var dir = 'image' + new Date().getTime();  // 存放路径
            var filePaths = []; //收集每次返回的文件路径
            var loopTime = Math.ceil(base64ObjectArr.length / breakupDownloadNum);
            for (var m = 0; m < loopTime; m++) {
                (function(m) {
                    // 开始请求下载pdf
                    var noList = indexArr.slice(m * breakupDownloadNum, (m + 1) * breakupDownloadNum);
                    var contentList = base64Arr.slice(m * breakupDownloadNum, (m + 1) * breakupDownloadNum);

                    var formData = new FormData();
                    formData.append("nos", noList); // 编号数组，对应content
                    formData.append("contents", contentList); // 地址数组
                    formData.append("fileName", self.examFullName());
                    formData.append("dir", dir); // 服务端图片路径
                    formData.append("filePaths", filePaths); // n - 1次图片地址
                    formData.append("endflag", m === loopTime - 1 ? 'true' : '');
                    $.ajax({
                        url: '/report/createReports.vpage',
                        type: 'POST',
                        processData: false,
                        contentType: false,
                        data: formData,
                        async: false,
                        success: function (res) {
                            if (res.result) {
                                filePaths.push(res.filePathTemp);
                                if(m === loopTime - 1) { // 最后一次
                                    $.prompt.close(); // 关闭loading弹窗
                                    downloadPdf(res.reportPath, self.examFullName());
                                }
                            } else {
                                alertError(res.info || '下载出错，请稍后重试');
                            }
                        },
                        error: function () {
                            alertError('下载出错，请稍后重试');
                        },
                        complete: function () {
                            if(m === loopTime - 1){
                                downloadPdfFlag = false; // 重置下载按钮
                            }
                        }
                    });
                })(m);
            }
        };

        // 下载报告
        var downloadPdf = function (reportPath, examFullName) {
            var requestUrl = "/report/downReport.vpage?filePath=" + reportPath + "&fileName=" + examFullName + '.pdf';
            var downloadIframe = "<iframe style='display:none;' src=" + requestUrl + "/>";
            $("body").append(downloadIframe);
        };

        // 请求模块一: 参与概况数据
        var requestExamSurvey = function () {
            var dfd = new $.Deferred();
            $.ajax({
                url: '/examReport/loadExamSurvey.vpage',
                type: 'GET',
                data: {
                    cityCode: YQ.getQuery('cityCode'),
                    regionCode: YQ.getQuery('regionCode'),
                    schoolId: YQ.getQuery('schoolId'),
                    examId: YQ.getQuery('examId')
                },
                success: function (res) {
                    dfd.resolve();
                    if (res.result) {
                        self.dataLevel(self.dataLevel() || res.dataLevel); // 报告等级（英文）
                        self.viewRegionLevel(self.viewRegionLevel() || res.viewRegionLevel); // 报告直属下一级（中文）

                        self.examRegionLevel(res.examRegionLevel || ''); // 考试范围
                        self.gradeName(exchangeGradeName(res.grade || 1)); // 年级
                        self.subject(res.subject || '英语'); // 学科
                        self.examTime(res.examTime || ''); // 考试时间
                        self.durationMinutes(res.durationMinutes || 0); // 答卷时长

                        self.examSurvey(res.examSurvey || {}); // 参与概况基础数据
                        self.examSurveyDetail(res.examSurveyDetail || []); // 参与概况表格数据

                        // 打点
                        YQ.voxLogs({
                            database: databaseLogs,
                            module: moduleName,
                            op: "exam_detail_load",
                            s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                            s1: '', // 页面停留时间，不计
                            s2: self.gradeName(), // 年级
                            s3: self.subject(),// 学科
                            S4:'ACADEMIC_ACHIEVEMENT'
                        });
                    } else {
                        self.examSurveyEmptyFlag(true);
                    }
                },
                error: function () {
                    self.examSurveyEmptyFlag(true);
                    dfd.reject();
                }
            });
            return dfd;
        };

        // 请求模块二: 得分状况
        var requestExamScoreState = function () {
            var dfd = new $.Deferred();
            $.ajax({
                url: '/examReport/loadExamScoreState.vpage',
                type: 'GET',
                data: {
                    cityCode: YQ.getQuery('cityCode'),
                    regionCode: YQ.getQuery('regionCode'),
                    schoolId: YQ.getQuery('schoolId'),
                    examId: YQ.getQuery('examId')
                },
                success: function (res) {
                    dfd.resolve();
                    if (res.result) {
                        self.dataLevel(self.dataLevel() || res.dataLevel); // 报告等级（英文）
                        self.viewRegionLevel(self.viewRegionLevel() || res.viewRegionLevel); // 报告直属下一级（中文）

                        self.examScoreLastData(res.lastData || []); // 表现最差的1个
                        self.examScoreTopThreeData(res.topThreeData || []); // 表现最好的3个
                        self.examScoreWholeScore(res.wholeScore || {}); // 得分情况
                        self.examScoreWholeScoreDetail(res.wholeScoreDetail || []); // 得分状况表格
                        self.examScoreCoefficient(res.coefficient || 0); // 离散系数
                        self.examScoreScatterPointMaxMap(res.scatterPointMaxMap || {}); // 得分差异最大的信息
                        self.examScoreScatterPointMinMap(res.scatterPointMinMap || {}); // 得分差异最小的信息

                        // 绘制图表
                        drawExamScoreStateChart(res);
                    } else {
                        self.examScoreEmptyFlag(true);
                    }
                },
                error: function () {
                    self.examScoreEmptyFlag(true);
                    dfd.reject();
                }
            });
            return dfd;
        };

        // 请求模块三: 学业水平
        var requestStudyLevelInfo = function () {
            var dfd = new $.Deferred();
            $.ajax({
                url: '/examReport/loadStudyLevelInfo.vpage',
                type: 'GET',
                data: {
                    cityCode: YQ.getQuery('cityCode'),
                    regionCode: YQ.getQuery('regionCode'),
                    schoolId: YQ.getQuery('schoolId'),
                    examId: YQ.getQuery('examId')
                },
                success: function (res) {
                    dfd.resolve();
                    if (res.result) {
                        self.dataLevel(self.dataLevel() || res.dataLevel); // 报告等级（英文）
                        self.viewRegionLevel(self.viewRegionLevel() || res.viewRegionLevel); // 报告直属下一级（中文）

                        self.studyLevelInfo(res.studyLevelInfo || []); // 学业水平各等级数据
                        self.studyLevelWholeQualifiledRatio(res.wholeQualifiledRatio || 0); // 整体合格率
                        self.studyLevelGridDataList(res.gridDataList || []); // 学业水平附图表格数据
                        self.studyLevelExcellentTopThreeData(res.excellentBarMap.topThreeExcellentRatio || []); // 优秀率最好的3个
                        self.studyLevelGoodTopThreeData(res.excellgoodBarMap.topThreeExcellentgoodRatio || []); // 良好率最好的3个
                        self.studyLevelQulifiledTopThreeData(res.excellgoodqulifiledBarMap.topThreeExcellentgoodqualifiledRatio || []); // 合格率最好的3个
                        self.studyLevelQulifiledLastData(res.excellgoodqulifiledBarMap.lastExcellentgoodqualifiledRatio || []); // 合格率率最差的3个
                        self.studyLevelQulifiledLastDataDiff(res.excellgoodqulifiledBarMap.diff || 0); // 合格率最低的低于平均水平值
                        self.studyLevelUnqulifiledTopThreeData(res.unqulifiledBarMap.topThreeUnqualifiledRatio || []); // 待合格率最好的3个

                        // 绘制图表
                        drawStudyLevelChart(res);
                    } else {
                        self.studyLevelEmptyFlag(true);
                    }
                },
                error: function () {
                    self.studyLevelEmptyFlag(true);
                    dfd.reject();
                }
            });
            return dfd;
        };

        // 请求模块四: 学科能力
        var requestSubjectAbilityInfo = function () {
            var dfd = new $.Deferred();
            $.ajax({
                url: '/examReport/loadSubjectAbilityInfo.vpage',
                type: 'GET',
                data: {
                    cityCode: YQ.getQuery('cityCode'),
                    regionCode: YQ.getQuery('regionCode'),
                    schoolId: YQ.getQuery('schoolId'),
                    examId: YQ.getQuery('examId')
                },
                success: function (res) {
                    dfd.resolve();
                    if (res.result) {
                        self.dataLevel(self.dataLevel() || res.dataLevel); // 报告等级（英文）
                        self.viewRegionLevel(self.viewRegionLevel() || res.viewRegionLevel); // 报告直属下一级（中文）

                        self.subjectAbilityInfo(res.subjectAbilityInfo || []); // 学科能力各等级数据
                        self.subjectAbilityMax(res.subjectAbilityMax || {}); // 表现最好的学科能力
                        self.subjectAbilityMin(res.subjectAbilityMin || {}); // 表现最差的学科能力
                        self.subjectAbilityGrid(res.subjectAbilityGrid || {}); // 学科能力表格数据
                        self.subjectAbilityMapList(res.subjectAbilityDataMapList || []); // 学科能力map list

                        // 绘制图表
                        drawSubjectAbilityChart(res);
                    } else {
                        self.subjectAbilityEmptyFlag(true);
                    }
                },
                error: function () {
                    self.subjectAbilityEmptyFlag(true);
                    dfd.reject();
                }
            });
            return dfd;
        };

        // 请求模块五: 知识板块
        var requestKnowledgePlateInfo = function () {
            var dfd = new $.Deferred();
            $.ajax({
                url: '/examReport/loadKnowledgePlateInfo.vpage',
                type: 'GET',
                data: {
                    cityCode: YQ.getQuery('cityCode'),
                    regionCode: YQ.getQuery('regionCode'),
                    schoolId: YQ.getQuery('schoolId'),
                    examId: YQ.getQuery('examId')
                },
                success: function (res) {
                    dfd.resolve();
                    if (res.result) {
                        self.dataLevel(self.dataLevel() || res.dataLevel); // 报告等级（英文）
                        self.viewRegionLevel(self.viewRegionLevel() || res.viewRegionLevel); // 报告直属下一级（中文）

                        self.knowledgePlateInfo(res.knowledgePlateInfo || []); // 知识板块各等级数据
                        self.knowledgePlateMax(res.knowledgePlateMax || {}); // 表现最好的知识板块
                        self.knowledgePlateMin(res.knowledgePlateMin || {}); // 表现最差的知识板块
                        self.knowledgePlateGrid(res.knowledgePlateGrid || {}); // 知识板块表格数据
                        self.knowledgePlateMapList(res.knowledgePlateDataMapList || []); // 知识板块map list

                        // 绘制图表
                        drawKnowledgePlateChart(res);
                    } else {
                        self.knowledgePlateEmptyFlag(true);
                    }
                },
                error: function () {
                    self.knowledgePlateEmptyFlag(true);
                    dfd.reject();
                }
            });
            return dfd;
        };

        // 绘制得分情况图表
        var drawExamScoreStateChart = function (res) {
            var examScoreStateChart1 = echarts.init(document.getElementsByClassName('examScoreStateChart1')[0], EChartsTheme); // 圆环图
            var examScoreStateChart3 = echarts.init(document.getElementsByClassName('examScoreStateChart3')[0], EChartsTheme); // 离散图

            // 1、圆环图-start
            examScoreStateChart1.setOption({
                series: [
                    {
                        name:'满分',
                        type:'pie',
                        center: ['20%', '50%'],
                        radius: [72, 92], // 半径
                        avoidLabelOverlap: false,
                        clockwise: false, // 是否顺时针
                        hoverAnimation: false, // hover 动画
                        // silent: true, // 是否不响应鼠标
                        label: {
                            normal: { // 默认提示
                                show: true,
                                position: 'center',
                                textStyle: {
                                    fontSize: '20',
                                    fontWeight: 'bold'
                                },
                                formatter: function (params) {
                                        // '{a|这段文本采用样式a}',
                                        // '{b|这段文本采用样式b}这段用默认样式{x|这段用样式x}'
                                    return params.name === '满分' ? ('{a|' + params.name + '}\n\n{b|' + params.value + '}{c|' + '分}')  : '';
                                },
                                rich: {
                                    a: {
                                        fontSize: 18,
                                        color: '#555555'
                                    },
                                    b: {
                                        fontSize: 40,
                                        fontWeight: 'bold',
                                        color: '#1B92FF',
                                        verticalAlign: 'bottom',
                                    },
                                    c: {
                                        fontSize: 20,
                                        color: '#555555',
                                        verticalAlign: 'bottom',
                                        padding: [5, 0]
                                    }
                                }
                            }
                        },
                        labelLine: {
                            normal: {
                                show: false
                            }
                        },
                        data:[
                            {
                                value: res.wholeScore.fullMarks,
                                name:'满分',
                                itemStyle: {
                                    color: '#1B92FF'
                                }
                            }
                        ]
                    },
                    {
                        name:'总体平均分',
                        type:'pie',
                        center: ['50%', '50%'],
                        radius: [72, 92], // 半径
                        avoidLabelOverlap: false,
                        clockwise: false, // 是否顺时针
                        hoverAnimation: false, // hover 动画
                        // silent: true, // 是否不响应鼠标
                        label: {
                            normal: { // 默认提示
                                show: true,
                                    position: 'center',
                                    textStyle: {
                                    fontSize: '20',
                                    fontWeight: 'bold'
                                },
                                formatter: function (params) {
                                    // '{a|这段文本采用样式a}',
                                    // '{b|这段文本采用样式b}这段用默认样式{x|这段用样式x}'
                                    return params.name === '总体平均分' ? ('{a|' + params.name + '}\n\n{b|' + params.value + '}{c|' + '分}')  : '';
                                },
                                rich: {
                                    a: {
                                        fontSize: 18,
                                        color: '#555555'
                                    },
                                    b: {
                                        fontSize: 40,
                                        fontWeight: 'bold',
                                        color: '#4DCB73',
                                        verticalAlign: 'bottom',
                                    },
                                    c: {
                                        fontSize: 20,
                                        color: '#555555',
                                        verticalAlign: 'bottom',
                                        padding: [5, 0]
                                    }
                                }
                            }
                        },
                        labelLine: {
                            normal: {
                                show: false
                            }
                        },
                        data:[
                            {
                                value: res.wholeScore.averageScore,
                                name:'总体平均分',
                                itemStyle: {
                                    color: '#4DCB73'
                                }
                            },
                            {
                                value: res.wholeScore.fullMarks - res.wholeScore.averageScore,
                                name:'无',
                                itemStyle: {
                                    normal: {
                                        color: '#f0f2f5' // 正常颜色
                                    },
                                    emphasis: {
                                        color: '#f0f2f5' // 鼠标滑过颜色
                                    }
                                }
                            }
                        ]
                    },
                    {
                        name:'得分率',
                        type:'pie',
                        center: ['80%', '50%'],
                        radius: [72, 92], // 半径
                        avoidLabelOverlap: false,
                        clockwise: false, // 是否顺时针
                        // silent: true, // 是否不响应鼠标
                        hoverAnimation: false, // hover 动画
                        label: {
                            normal: { // 默认提示
                                show: true,
                                    position: 'center',
                                    textStyle: {
                                    fontSize: '20',
                                    fontWeight: 'bold',
                                },
                                formatter: function (params) {
                                    // '{a|这段文本采用样式a}',
                                    // '{b|这段文本采用样式b}这段用默认样式{x|这段用样式x}'
                                    return params.name === '得分率' ? ('{a|' + params.name + '}\n\n{b|' + params.value+ '}{c|' + '%}')  : '';
                                },
                                rich: {
                                    a: {
                                        fontSize: 18,
                                        color: '#555555'
                                    },
                                    b: {
                                        fontSize: 40,
                                        fontWeight: 'bold',
                                        color: '#FBCD17'
                                    },
                                    c: {
                                        fontSize: 20,
                                        color: '#555555',
                                        verticalAlign: 'bottom',
                                        padding: [5, 0]
                                    }
                                }
                            }
                        },
                        labelLine: {
                            normal: {
                                show: false
                            }
                        },
                        data:[
                            {
                                value: res.wholeScore.wholeScoreRate,
                                name:'得分率',
                                itemStyle: {
                                    color: '#FBCD17'
                                }
                            },
                            {
                                value: 100 - res.wholeScore.wholeScoreRate,
                                name:'得分率',
                                name:'无',
                                itemStyle: {
                                    normal: {
                                        color: '#f0f2f5' // 正常颜色
                                    },
                                    emphasis: {
                                        color: '#f0f2f5' // 鼠标滑过颜色
                                    }
                                }
                            }
                        ]
                    }
                ]
            });
            // 1、圆环图-end

            // 3、得分离散程度-start
            examScoreStateChart3.setOption({
                grid: {
                    top: '18%',
                    left: '3%',
                    right: '5%',
                    bottom: '6%',
                    containLabel: true
                },
                tooltip: { // 鼠标悬浮提示
                    showDelay: 0,
                    formatter: function (params) {
                        if (params.componentType === "series") {
                            return params.value[2] + ' :<br/>'
                                + params.marker + '总分平均分: ' + params.value[0] + '<br/>'
                                + params.marker + '总分标准差: ' + params.value[1];
                        }

                    },
                    axisPointer: {
                        show: true,
                        type: 'cross',
                        lineStyle: {
                            type: 'dashed',
                            width: 1
                        }
                    }
                },
                xAxis: [
                    {
                        type: 'value',
                        scale: true,
                        name: '总分平均分', // 坐标轴名称
                        nameLocation: 'middle', // 坐标轴名称显示位置
                        nameGap: 18, // 坐标轴名称与轴线之间的距离
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        scale: true,
                        name: '总分标准差', // 坐标轴名称
                        nameLocation: 'middle', // 坐标轴名称显示位置
                        nameGap: 30, // 坐标轴名称与轴线之间的距离
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    }
                ],
                series: [
                    {
                        // name:'得分',
                        type:'scatter',
                        data: res.scatterPointData,
                        markArea: { // 区域
                            silent: true,
                            itemStyle: {
                                normal: {
                                    color: 'transparent',
                                    borderWidth: 1,
                                    borderType: 'dashed'
                                }
                            },
                            data: [[{
                                // name: '总分平均分',
                                xAxis: 'min',
                                yAxis: 'min'
                            }, {
                                xAxis: 'max',
                                yAxis: 'max'
                            }]]
                        },
                        // markPoint: { // 最大值、最小值
                        //     silent: true, // 标注是否不触发鼠标事件
                        //     data : [
                        //         {type : 'max', valueIndex: 1, name: '最大值', value: '1'},
                        //         {type : 'min', valueIndex: 1, name: '最小值', value: '2'},
                        //     ]
                        // },
                        markLine: { // 收到绘制的坐标轴
                            lineStyle: {
                                normal: {
                                    type: 'solid'
                                }
                            },
                            symbol: ['none', 'triangle'], // 两端的形状
                            data: [{
                                type: 'average',
                                name: '平均值'
                            }, {
                                type: 'average',
                                valueIndex: 0,
                                name: '平均值'
                            }]
                        }
                    }
                ]
            });
            // 3、得分离散程度-end

            // 对于图表按40为一组拆分
            var echart_num = Math.ceil(res.wholeScoreDetail.length / breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
            // clone节点
            for (var i = 0; i < echart_num; i++) {
                if (i > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                    $('.examScoreStateChart2').eq($('.examScoreStateChart2').length - 1).after($('.examScoreStateChart2').eq(0).clone(true));
                }
            }
            // 绘制图表
            for (var j = 0; j < echart_num; j++) {
                // 2、得分率折线图-start
                echarts.init(document.getElementsByClassName('examScoreStateChart2')[j], EChartsTheme).setOption({
                    title: {
                        text: echart_num > 1 ? ((j * breakupNum + 1) + '-' + ((j + 1) * breakupNum > res.xAxisData.length ? res.xAxisData.length : (j + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' 得分率') : '',
                        x: 'center',
                        textStyle: {
                            fontSize: 12,
                            fontWeight: 'normal'
                        },
                        bottom: 0
                    },
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            return res.wholeScoreDetail[+params[0].name - 1].name + '<br/>' + params[0].marker + '得分率: ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        right: 120
                    },
                    legend: {
                        right: 0,
                        selectedMode: false,
                        data: [{
                            name: '各' + res.viewRegionLevel + '得分率'
                        },{
                            name: exchangLevelChinese(res.dataLevel) + '得分率',
                            icon:'image://' + window.echartImg1
                        }]
                    },
                    xAxis: {
                        type: 'category',
                        name: '序号',
                        data: res.xAxisData.slice(j * breakupNum, (j + 1) * breakupNum),
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        axisLabel: {
                            interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                        }
                    },
                    yAxis: {
                        type: 'value',
                        name: '得分率',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        name: '各' + res.viewRegionLevel + '得分率',
                        data: res.seriesData.slice(j * breakupNum, (j + 1) * breakupNum),
                        type: 'line',
                        markLine: {
                            symbol: 'none', // 两端的形状
                            lineStyle: {
                                normal: {
                                    type: 'circle', // 虚线
                                    width: 2, // 线宽
                                    color: '#32C35D'
                                }
                            },
                            data: [
                                // {type : 'average', name: '平均值'}, // 取平均值
                                {
                                    yAxis: res.wholeScore.wholeScoreRate,  // 指定线的值
                                    label: {
                                        normal: {
                                            formatter: exchangLevelChinese(res.dataLevel) + '得分率{c}%'
                                        }
                                    }
                                }
                            ]
                        }
                    }, { // 附加一个假的数据格式，用于构成图例
                        name: exchangLevelChinese(res.dataLevel) + '得分率',
                        type: 'line'
                    }]
                });
                // 2、得分率折线图-end
            }
        };

        // 绘制学业水平图表
        var drawStudyLevelChart = function (res) {
            var studyLevelChart1 = echarts.init(document.getElementsByClassName('studyLevelChart1')[0], EChartsTheme); // 饼状图

            // 1、饼状图-start
            var pieSeriesData = [];
            for (var i = 0; i < res.studyLevelInfo.length; i++) {
                pieSeriesData.push({
                    name: res.studyLevelInfo[i].levelCname + ': \n' + res.studyLevelInfo[i].studentnum + '(' + res.studyLevelInfo[i].levelrate + '%)',
                    value: res.studyLevelInfo[i].levelrate,
                    sudentNum: res.studyLevelInfo[i].studentnum
                });
            }
            studyLevelChart1.setOption({
                tooltip : {
                    trigger: 'item',
                    formatter: function (params) {
                        return params.name;
                    }
                },
                // legend: {
                //     x : 'center',
                // },
                series : [
                    {
                        type: 'pie',
                        radius : '55%',
                        center: ['50%', '50%'],
                        data: pieSeriesData,
                        itemStyle: {
                            emphasis: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        }
                    }
                ]
            });
            // 1、饼状图-end

            // 对于图表按40为一组拆分
            var echart_num = Math.ceil(res.gridDataList.length / breakupNum); // 以gridDataList字段做拆分，40个为一组
            // clone节点
            for (var j = 0; j < echart_num; j++) {
                if (j > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                    $('.studyLevelChart2').eq($('.studyLevelChart2').length - 1).after($('.studyLevelChart2').eq(0).clone(true)); // 柱状叠加图
                    $('.studyLevelChart3').eq($('.studyLevelChart3').length - 1).after($('.studyLevelChart3').eq(0).clone(true)); // 柱状图-优秀率
                    $('.studyLevelChart4').eq($('.studyLevelChart4').length - 1).after($('.studyLevelChart4').eq(0).clone(true)); // 柱状图-良好率
                    $('.studyLevelChart5').eq($('.studyLevelChart5').length - 1).after($('.studyLevelChart5').eq(0).clone(true)); // 柱状图-合格率
                    $('.studyLevelChart6').eq($('.studyLevelChart6').length - 1).after($('.studyLevelChart6').eq(0).clone(true)); // 柱状图-待合格率
                }
            }
            for (var k = 0; k < echart_num; k++) {
                // 2、柱状叠加图-start
                echarts.init(document.getElementsByClassName('studyLevelChart2')[k], EChartsTheme).setOption({
                    title: {
                        text: echart_num > 1 ? ((k * breakupNum + 1) + '-' + ((k + 1) * breakupNum > res.gridDataList.length ? res.gridDataList.length : (k + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' 学业水平各等级占比') : '',
                        x: 'center',
                        textStyle: {
                            fontSize: 12,
                            fontWeight: 'normal'
                        },
                        bottom: 5
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            var toortipContent = [res.gridDataList[+params[0].name - 1].name];
                            for (var t = 0; t < params.length; t++) {
                                toortipContent.push(params[t].marker + ' ' + params[t].seriesName + ': ' + params[t].value + '%');
                            }
                            return toortipContent.join('<br />');
                        }
                    },
                    legend: {
                        x: 'right',
                        data: res.wholeBarMap.legendData
                    },
                    xAxis: {
                        type: 'category',
                        name: '序号',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        axisLabel: {
                            interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                        },
                        data: res.wholeBarMap.xAxisData.slice(k * breakupNum, (k + 1) * breakupNum)
                    },
                    yAxis: {
                        type: 'value',
                        name: '比例',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: getSeriesDataSome(res.wholeBarMap.seriesData, k)
                });
                // 2、柱状叠加图-end

                // 3、柱状图-优秀率-start
                echarts.init(document.getElementsByClassName('studyLevelChart3')[k], EChartsTheme).setOption({
                    title: {
                        text: echart_num > 1 ? ((k * breakupNum + 1) + '-' + ((k + 1) * breakupNum > res.gridDataList.length ? res.gridDataList.length : (k + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' 优秀率') : '',
                        x: 'center',
                        textStyle: {
                            fontSize: 12,
                            fontWeight: 'normal'
                        },
                        bottom: 5
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            return res.gridDataList[+params[0].name - 1].name + '<br/>' + params[0].marker + '优秀率: ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        right: 120
                    },
                    legend: {
                        right: 0,
                        selectedMode: false,
                        data: [{
                            name: '各' + res.viewRegionLevel + '优秀率'
                        },{
                            name: exchangLevelChinese(res.dataLevel) + '优秀率',
                            icon:'image://' + window.echartImg1
                        }]
                    },
                    xAxis: {
                        type: 'category',
                        name: '序号',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        axisLabel: {
                            interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                        },
                        data: res.excellentBarMap.xAxisData.slice(k * breakupNum, (k + 1) * breakupNum)
                    },
                    yAxis: {
                        type: 'value',
                        name: '比例',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        name: '各' + res.viewRegionLevel + '优秀率',
                        data: res.excellentBarMap.seriesData.slice(k * breakupNum, (k + 1) * breakupNum),
                        type: 'bar',
                        barMaxWidth: 50,
                        label: {
                            normal: {
                                show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                position: 'top'
                            }
                        },
                        markLine: {
                            symbol: 'none', // 两端的形状
                            lineStyle: {
                                normal: {
                                    type: 'circle', // 圆虚线
                                    width: 2, // 线宽
                                    color: '#32C35D'
                                }
                            },
                            data: [
                                // {type : 'average', name: '平均值'}, // 取平均值
                                {
                                    yAxis: res.excellentBarMap.averageData,  // 指定线的值
                                    label: {
                                        normal: {
                                            formatter: exchangLevelChinese(res.dataLevel) + '优秀率{c}%'
                                        }
                                    }
                                }
                            ]
                        }
                    }, { // 附加一个假的数据格式，用于构成图例
                        name: exchangLevelChinese(res.dataLevel) + '优秀率',
                        type: 'line'
                    }]
                });
                // 3、柱状图-优秀率-end

                // 4、柱状图-良好率-start
                echarts.init(document.getElementsByClassName('studyLevelChart4')[k], EChartsTheme).setOption({
                    title: {
                        text: echart_num > 1 ? ((k * breakupNum + 1) + '-' + ((k + 1) * breakupNum > res.gridDataList.length ? res.gridDataList.length : (k + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' 良好率') : '',
                        x: 'center',
                        textStyle: {
                            fontSize: 12,
                            fontWeight: 'normal'
                        },
                        bottom: 5
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            return res.gridDataList[+params[0].name - 1].name + '<br/>' + params[0].marker + '良好率: ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        right: 120
                    },
                    legend: {
                        right: 0,
                        selectedMode: false,
                        data: [{
                            name: '各' + res.viewRegionLevel + '良好率',
                        },{
                            name: exchangLevelChinese(res.dataLevel) + '良好率',
                            icon:'image://' + window.echartImg1
                        }]
                    },
                    xAxis: {
                        type: 'category',
                        name: '序号',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        axisLabel: {
                            interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                        },
                        data: res.excellgoodBarMap.xAxisData.slice(k * breakupNum, (k + 1) * breakupNum)
                    },
                    yAxis: {
                        type: 'value',
                        name: '比例',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        name: '各' + res.viewRegionLevel + '良好率',
                        data: res.excellgoodBarMap.seriesData.slice(k * breakupNum, (k + 1) * breakupNum),
                        type: 'bar',
                        barMaxWidth: 50,
                        label: {
                            normal: {
                                show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                position: 'top'
                            }
                        },
                        markLine: {
                            symbol: 'none', // 两端的形状
                            lineStyle: {
                                normal: {
                                    type: 'circle', // 圆虚线
                                    width: 2, // 线宽
                                    color: '#32C35D'
                                }
                            },
                            data: [
                                // {type : 'average', name: '平均值'}, // 取平均值
                                {
                                    yAxis: res.excellgoodBarMap.averageData,  // 指定线的值
                                    label: {
                                        normal: {
                                            formatter: exchangLevelChinese(res.dataLevel) + '良好率{c}%'
                                        }
                                    }
                                }
                            ]
                        }
                    }, { // 附加一个假的数据格式，用于构成图例
                        name: exchangLevelChinese(res.dataLevel) + '良好率',
                        type: 'line'
                    }]
                });
                // 4、柱状图-良好率-end

                // 5、柱状图-合格率-start
                echarts.init(document.getElementsByClassName('studyLevelChart5')[k], EChartsTheme).setOption({
                    title: {
                        text: echart_num > 1 ? ((k * breakupNum + 1) + '-' + ((k + 1) * breakupNum > res.gridDataList.length ? res.gridDataList.length : (k + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' 合格率') : '',
                        x: 'center',
                        textStyle: {
                            fontSize: 12,
                            fontWeight: 'normal'
                        },
                        bottom: 5
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            return res.gridDataList[+params[0].name - 1].name + '<br/>' + params[0].marker + '合格率: ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        right: 120
                    },
                    legend: {
                        right: 0,
                        selectedMode: false,
                        data: [{
                            name: '各' + res.viewRegionLevel + '合格率'
                        },{
                            name: exchangLevelChinese(res.dataLevel) + '合格率',
                            icon:'image://' + window.echartImg1
                        }]
                    },
                    xAxis: {
                        type: 'category',
                        name: '序号',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        axisLabel: {
                            interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                        },
                        data: res.excellgoodqulifiledBarMap.xAxisData.slice(k * breakupNum, (k + 1) * breakupNum)
                    },
                    yAxis: {
                        type: 'value',
                        name: '比例',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        name: '各' + res.viewRegionLevel + '合格率',
                        data: res.excellgoodqulifiledBarMap.seriesData.slice(k * breakupNum, (k + 1) * breakupNum),
                        type: 'bar',
                        barMaxWidth: 50,
                        label: {
                            normal: {
                                show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                position: 'top'
                            }
                        },
                        markLine: {
                            symbol: 'none', // 两端的形状
                            lineStyle: {
                                normal: {
                                    type: 'circle', // 圆虚线
                                    width: 2, // 线宽
                                    color: '#32C35D'
                                }
                            },
                            data: [
                                // {type : 'average', name: '平均值'}, // 取平均值
                                {
                                    yAxis: res.excellgoodqulifiledBarMap.averageData,  // 指定线的值
                                    label: {
                                        normal: {
                                            formatter: exchangLevelChinese(res.dataLevel) + '合格率{c}%'
                                        }
                                    }
                                }
                            ]
                        }
                    }, { // 附加一个假的数据格式，用于构成图例
                        name: exchangLevelChinese(res.dataLevel) + '合格率',
                        type: 'line'
                    }]
                });
                // 5、柱状图-合格率-end

                // 6、柱状图-待合格率-start
                echarts.init(document.getElementsByClassName('studyLevelChart6')[k], EChartsTheme).setOption({
                    title: {
                        text: echart_num > 1 ? ((k * breakupNum + 1) + '-' + ((k + 1) * breakupNum > res.gridDataList.length ? res.gridDataList.length : (k + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' 待合格率') : '',
                        x: 'center',
                        textStyle: {
                            fontSize: 12,
                            fontWeight: 'normal'
                        },
                        bottom: 5
                    },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            return res.gridDataList[+params[0].name - 1].name + '<br/>' + params[0].marker + '待合格率: ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        right: 120
                    },
                    legend: {
                        right: 0,
                        selectedMode: false,
                        data: [{
                            name: '各' + res.viewRegionLevel + '待合格率'
                        },{
                            name: exchangLevelChinese(res.dataLevel) + '待合格率',
                            icon:'image://' + window.echartImg1
                        }]
                    },
                    xAxis: {
                        type: 'category',
                        name: '序号',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        axisLabel: {
                            interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                        },
                        data: res.unqulifiledBarMap.xAxisData.slice(k * breakupNum, (k + 1) * breakupNum)
                    },
                    yAxis: {
                        type: 'value',
                        name: '比例',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        name: '各' + res.viewRegionLevel + '待合格率',
                        data: res.unqulifiledBarMap.seriesData.slice(k * breakupNum, (k + 1) * breakupNum),
                        type: 'bar',
                        barMaxWidth: 50,
                        label: {
                            normal: {
                                show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                position: 'top'
                            }
                        },
                        markLine: {
                            symbol: 'none', // 两端的形状
                            lineStyle: {
                                normal: {
                                    type: 'circle', // 圆虚线
                                    width: 2, // 线宽
                                    color: '#32C35D'
                                }
                            },
                            data: [
                                // {type : 'average', name: '平均值'}, // 取平均值
                                {
                                    yAxis: res.unqulifiledBarMap.averageData,  // 指定线的值
                                    label: {
                                        normal: {
                                            formatter: exchangLevelChinese(res.dataLevel) + '待合格率{c}%',
                                            // distance: 50,
                                            // padding: [0, 0, 20, 0]
                                        }
                                    }
                                }
                            ]
                        }
                    }, { // 附加一个假的数据格式，用于构成图例
                        name: exchangLevelChinese(res.dataLevel) + '待合格率',
                        type: 'line'
                    }]
                });
                // 6、柱状图-待合格率-end
            }
        };

        // 绘制学科能力图表
        var drawSubjectAbilityChart = function (res) {
            var subjectAbilityChart1 = echarts.init(document.getElementsByClassName('subjectAbilityChart1')[0], EChartsTheme); // 雷达图(>=3) 或 柱状图（<3）
            // 1、雷达图或柱状图-start
            if (res.subjectAbilityInfo.length >= 3) { // 雷达图
                var indicatorData = [];
                for (var u = 0; u < res.radarMap.indicatorData.length; u++) {
                    indicatorData.push({
                        name: res.radarMap.indicatorData[u] + '\n' + res.radarMap.seriesData[u] + '%',
                        max: 100
                    });
                }
                subjectAbilityChart1.setOption({
                    tooltip: {
                        formatter: function (params) {
                            var tooltipContent = [params.name];
                            for (var v = 0; v < params.value.length; v++) {
                                tooltipContent.push(res.radarMap.indicatorData[v] + ': ' + params.value[v] + '%');
                            }
                            return tooltipContent.join('<br />');
                        }
                    },
                    radar: {
                        name: {
                            textStyle: {
                                color: '#0092fa',
                                // backgroundColor: '#999',
                                // borderRadius: 3,
                                // padding: [3, 5]
                            }
                        },
                        axisLine: { // 坐标轴线
                            show: false
                        },
                        splitNumber: 10, // 分割段数
                        splitLine: { // 分割线
                            lineStyle: {
                                width: 1,
                                color: '#e6e6e6'
                            }
                        },
                        splitArea: { // 分割区域
                            areaStyle: {
                                color: '#ffffff'
                            }
                        },
                        indicator: indicatorData, // 指示器
                        radius: '62%', // 半径
                    },
                    series: [{
                        name: '学科能力',
                        type: 'radar',
                        // symbol: 'none', // 数据标记的图形(拐点的图形)
                        lineStyle: { // 线条
                            color: '#0092fa',
                            width: 3
                        },
                        // areaStyle: { // 填充色
                        //     color: '#eee'
                        // },
                        data: [
                            {
                                value: res.radarMap.seriesData,
                                name: '学科能力',
                                // radarIndex: 0,
                                label: {
                                    normal: {
                                        // show: true,
                                        // color: '#0092fa',
                                        // position: 'top', // 位置
                                        // borderColor: '#0092fa',
                                        // formatter: '{c}%' // 拐点处数值
                                    }
                                }
                            }
                        ]
                    }]
                });
            } else { // 柱状图
                subjectAbilityChart1.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            return params[0].marker + params[0].name + ': ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        top: 10,
                    },
                    xAxis: {
                        type: 'category',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        data: res.barMap.xAxisData
                    },
                    yAxis: {
                        type: 'value',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        data: res.barMap.seriesData,
                        type: 'bar',
                        barMaxWidth: 50,
                        label: {
                            normal: {
                                show: true,
                                formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                position: 'top'
                            }
                        }
                    }]
                });
            }
            // 1、雷达图或柱状图-end

            // 对于图表按40为一组拆分
            var echart_num = Math.ceil(res.subjectAbilityGrid.gridData.length / breakupNum); // 以subjectAbilityGrid.gridData字段做拆分，40个为一组
            // clone节点
            for (var m = 0; m < echart_num; m++) {
                for (var n = 0; n < res.subjectAbilityDataMapList.length; n++) {
                    if (m > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                        $('.subjectAbilityChart' + (n + 2)).eq($('.subjectAbilityChart' + (n + 2)).length - 1).after($('.subjectAbilityChart' + (n + 2)).eq(0).clone(true)); // 能力
                    }
                }
            }
            for (var x = 0; x < echart_num; x++) {
                for (var y = 0; y < res.subjectAbilityDataMapList.length; y++) {
                    // 此处使用自执行函数原因：for循环内部有formatter异步函数，不适用自执行会造成无法获取正常的循环索引
                    (function(y){
                        echarts.init(document.getElementsByClassName('subjectAbilityChart' + (y + 2))[x], EChartsTheme).setOption({
                            title: {
                                text: echart_num > 1 ? ((x * breakupNum + 1) + '-' + ((x + 1) * breakupNum > res.subjectAbilityGrid.gridData.length ? res.subjectAbilityGrid.gridData.length : (x + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' ' + res.subjectAbilityDataMapList[y].legendData + ' 占比') : '',
                                x: 'center',
                                textStyle: {
                                    fontSize: 12,
                                    fontWeight: 'normal'
                                },
                                bottom: 5
                            },
                            tooltip: {
                                trigger: 'axis',
                                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                },
                                formatter: function (params) {
                                    return res.subjectAbilityGrid.gridData[+params[0].name - 1].name + '<br/>' + params[0].marker + res.subjectAbilityDataMapList[y].legendData  + ' 得分率: ' +  params[0].value + '%';
                                }
                            },
                            grid: {
                                right: 120
                            },
                            legend: {
                                right: 0,
                                selectedMode: false,
                                data: [{
                                    name: '各' + res.viewRegionLevel + '得分率'
                                },{
                                    name: exchangLevelChinese(res.dataLevel) + '得分率',
                                    icon:'image://' + window.echartImg1
                                }]
                            },
                            xAxis: {
                                type: 'category',
                                name: '序号',
                                data: res.subjectAbilityDataMapList[y].xAxisData.slice(x * breakupNum, (x + 1) * breakupNum),
                                axisTick: {
                                    show: false
                                },
                                splitLine: {
                                    show: false
                                },
                                axisLabel: {
                                    interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                                }
                            },
                            yAxis: {
                                type: 'value',
                                name: '得分率',
                                max: 100, // 坐标轴最大值
                                interval: 20, // 坐标轴间隔
                                axisTick: {
                                    show: false // 隐藏刻度线
                                },
                                axisLabel: {
                                    formatter: '{value}%'
                                }
                            },
                            series: [{
                                name: '各' + res.viewRegionLevel + '得分率',
                                data: res.subjectAbilityDataMapList[y].seriesData.slice(x * breakupNum, (x + 1) * breakupNum),
                                type: 'line',
                                markLine: {
                                    symbol: 'none', // 两端的形状
                                    lineStyle: {
                                        normal: {
                                            type: 'circle', // 圆虚线
                                            width: 2, // 线宽
                                            color: '#32C35D'
                                        }
                                    },
                                    data: [
                                        // {type : 'average', name: '平均值'}, // 取平均值
                                        {
                                            yAxis: res.subjectAbilityDataMapList[y].markLineData,  // 指定线的值
                                            label: {
                                                normal: {
                                                    formatter: exchangLevelChinese(res.dataLevel) + '得分率{c}%'
                                                }
                                            }
                                        }
                                    ]
                                }
                            }, { // 附加一个假的数据格式，用于构成图例
                                name: exchangLevelChinese(res.dataLevel) + '得分率',
                                type: 'line'
                            }]
                        });
                    })(y);
                }
            }
        };

        // 绘制知识板块图表
        var drawKnowledgePlateChart = function (res) {
            var knowledgePlateChart1 = echarts.init(document.getElementsByClassName('knowledgePlateChart1')[0], EChartsTheme); // 雷达图(>=3) 或 柱状图（<3）
            // 1、雷达图或柱状图-start
            if (res.knowledgePlateInfo.length >= 3) { // 雷达图
                var indicatorData = [];
                for (var u = 0; u < res.radarMap.indicatorData.length; u++) {
                    indicatorData.push({
                        name: res.radarMap.indicatorData[u] + '\n' + res.radarMap.seriesData[u] + '%',
                        max: 100
                    });
                }
                knowledgePlateChart1.setOption({
                    tooltip: {
                        formatter: function (params) {
                            var tooltipContent = [params.name];
                            for (var v = 0; v < params.value.length; v++) {
                                tooltipContent.push(res.radarMap.indicatorData[v] + ': ' + params.value[v] + '%');
                            }
                            return tooltipContent.join('<br />');
                        }
                    },
                    radar: {
                        name: {
                            textStyle: {
                                color: '#0092fa',
                                // backgroundColor: '#999',
                                // borderRadius: 3,
                                // padding: [3, 5]
                            }
                        },
                        axisLine: { // 坐标轴线
                            show: false
                        },
                        splitNumber: 10, // 分割段数
                        splitLine: { // 分割线
                            lineStyle: {
                                width: 1,
                                color: '#e6e6e6'
                            }
                        },
                        splitArea: { // 分割区域
                            areaStyle: {
                                color: '#ffffff'
                            }
                        },
                        indicator: indicatorData, // 指示器
                        radius: '62%', // 半径
                    },
                    series: [{
                        name: '学科能力',
                        type: 'radar',
                        // symbol: 'none', // 数据标记的图形(拐点的图形)
                        lineStyle: { // 线条
                            color: '#0092fa',
                            width: 3
                        },
                        // areaStyle: { // 填充色
                        //     color: '#eee'
                        // },
                        data: [
                            {
                                value: res.radarMap.seriesData,
                                name: '学科能力',
                                // radarIndex: 0,
                                label: {
                                    normal: {
                                        // show: true,
                                        // color: '#0092fa',
                                        // position: 'top', // 位置
                                        // borderColor: '#0092fa',
                                        // formatter: '{c}%' // 拐点处数值
                                    }
                                }
                            }
                        ]
                    }]
                });
            } else { // 柱状图
                knowledgePlateChart1.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            return params[0].marker + params[0].name + ': ' +  params[0].value + '%';
                        }
                    },
                    grid: {
                        top: 10,
                    },
                    xAxis: {
                        type: 'category',
                        axisTick: {
                            show: false
                        },
                        splitLine: {
                            show: false
                        },
                        data: res.barMap.xAxisData
                    },
                    yAxis: {
                        type: 'value',
                        max: 100, // 坐标轴最大值
                        interval: 20, // 坐标轴间隔
                        axisTick: {
                            show: false // 隐藏刻度线
                        },
                        axisLabel: {
                            formatter: '{value}%'
                        }
                    },
                    series: [{
                        data: res.barMap.seriesData,
                        type: 'bar',
                        barMaxWidth: 50,
                        label: {
                            normal: {
                                show: true,
                                formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                position: 'top'
                            }
                        }
                    }]
                });
            }
            // 1、雷达图或柱状图-end

            // 对于图表按40为一组拆分
            var echart_num = Math.ceil(res.knowledgePlateGrid.gridData.length / breakupNum); // 以knowledgePlateGrid.gridData字段做拆分，40个为一组
            // clone节点
            for (var m = 0; m < echart_num; m++) {
                for (var n = 0; n < res.knowledgePlateDataMapList.length; n++) {
                    if (m > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                        $('.knowledgePlateChart' + (n + 2)).eq($('.knowledgePlateChart' + (n + 2)).length - 1).after($('.knowledgePlateChart' + (n + 2)).eq(0).clone(true)); // 能力
                    }
                }
            }
            for (var x = 0; x < echart_num; x++) {
                for (var y = 0; y < res.knowledgePlateDataMapList.length; y++) {
                    // 此处使用自执行函数原因：for循环内部有formatter异步函数，不适用自执行会造成无法获取正常的循环索引
                    (function (y) {
                        echarts.init(document.getElementsByClassName('knowledgePlateChart' + (y + 2))[x], EChartsTheme).setOption({
                            title: {
                                text: echart_num > 1 ? ((x * breakupNum + 1) + '-' + ((x + 1) * breakupNum > res.knowledgePlateGrid.gridData.length ? res.knowledgePlateGrid.gridData.length : (x + 1) * breakupNum) + '号 ' + res.viewRegionLevel + ' ' + res.knowledgePlateDataMapList[y].legendData + ' 占比') : '',
                                x: 'center',
                                textStyle: {
                                    fontSize: 12,
                                    fontWeight: 'normal'
                                },
                                bottom: 5
                            },
                            tooltip: {
                                trigger: 'axis',
                                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                },
                                formatter: function (params) {
                                    return res.knowledgePlateGrid.gridData[+params[0].name - 1].name + '<br/>' + params[0].marker + res.knowledgePlateDataMapList[y].legendData  + ' 得分率: ' +  params[0].value + '%';
                                }
                            },
                            grid: {
                                right: 120
                            },
                            legend: {
                                right: 0,
                                selectedMode: false,
                                data: [{
                                    name: '各' + res.viewRegionLevel + '得分率'
                                },{
                                    name: exchangLevelChinese(res.dataLevel) + '得分率',
                                    icon:'image://' + window.echartImg1
                                }]
                            },
                            xAxis: {
                                type: 'category',
                                name: '序号',
                                data: res.knowledgePlateDataMapList[y].xAxisData.slice(x * breakupNum, (x + 1) * breakupNum),
                                axisTick: {
                                    show: false
                                },
                                splitLine: {
                                    show: false
                                },
                                axisLabel: {
                                    interval: 0 // 0表示完全显示，1表示隔1个，auto不重叠的策略间隔显示标签
                                }
                            },
                            yAxis: {
                                type: 'value',
                                name: '得分率',
                                max: 100, // 坐标轴最大值
                                interval: 20, // 坐标轴间隔
                                axisTick: {
                                    show: false // 隐藏刻度线
                                },
                                axisLabel: {
                                    formatter: '{value}%'
                                }
                            },
                            series: [{
                                name: '各' + res.viewRegionLevel + '得分率',
                                data: res.knowledgePlateDataMapList[y].seriesData.slice(x * breakupNum, (x + 1) * breakupNum),
                                type: 'line',
                                markLine: {
                                    symbol: 'none', // 两端的形状
                                    lineStyle: {
                                        normal: {
                                            type: 'circle', // 圆虚线
                                            width: 2, // 线宽
                                            color: '#32C35D'
                                        }
                                    },
                                    data: [
                                        // {type : 'average', name: '平均值'}, // 取平均值
                                        {
                                            yAxis: res.knowledgePlateDataMapList[y].markLineData,  // 指定线的值
                                            label: {
                                                normal: {
                                                    formatter: exchangLevelChinese(res.dataLevel) + '得分率{c}%'
                                                }
                                            }
                                        }
                                    ]
                                }
                            }, { // 附加一个假的数据格式，用于构成图例
                                name: exchangLevelChinese(res.dataLevel) + '得分率',
                                type: 'line'
                            }]
                        });
                    })(y);
                }
            }
        };

        // 从seriesData中取出第*组数据（40个为一组，主要获取seriesData中的data数据，即操作seriesData中的data字段）
        // seriesData 为源数据，index为 取出第*组(从0开始)， 返回值为目标数据
        var getSeriesDataSome = function (seriesData, index) {
            var newSeriesData = [];
            for (var s = 0; s < seriesData.length; s++) {
                newSeriesData.push({
                    type: 'bar',
                    stack: '总量',
                    barMaxWidth: 50,
                    name:  seriesData[s].ratioName,
                    data: seriesData[s].data.slice(index * breakupNum, (index + 1) * breakupNum)
                });
            }
            return newSeriesData;
        };

        // 将dataLevel匹配对应的中文
        var exchangLevelChinese = function (dataLevel) {
            var dataLevelChinese = '';
            switch (dataLevel) {
                case 'city':
                    dataLevelChinese = '市';
                    break;
                case 'country':
                    dataLevelChinese = '区';
                    break;
                case 'school':
                    dataLevelChinese = '学校';
                    break;
                default:
                    dataLevelChinese = '学校';
            }
            return dataLevelChinese;
        };

        // 将subject匹配对应的中文
        var exchangeGradeName = function (grade) {
            var chineseNumber = ['一' ,'二', '三', '四', '五', '六' ,'七', '八', '九'];
            return chineseNumber[grade - 1] + '年级';
        };

        // 获取地址栏所有参数
        var getHrefParamsStr = function () {
            var locationHref = window.location.href;
            if (locationHref.indexOf('?') > -1) {
                return locationHref.substring(locationHref.indexOf('?') + 1);
            } else {
                return '';
            }
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
            $(window).on('scroll', function() {
                if ($(window).scrollTop() >= 500) { // 滚动了500之后显示
                    $('#gotoTop').fadeIn(300);
                } else {
                    $('#gotoTop').fadeOut(300);
                }
            });

            $('#gotoTop').on('click', function(){ // 点击置顶
                $('html').animate({
                    scrollTop: '0px'
                }, 300);
            });
        };

        // 初始化触发($.when搭配Deferred 实现多个请求的回调)
        $.when(requestExamSurvey(), requestExamScoreState(), requestStudyLevelInfo(), requestSubjectAbilityInfo(), requestKnowledgePlateInfo()).done(function () {
            setTimeout(function () {
                self.isShowDownloadBtn(true);
                var loadSection = $('#downloadContent .loadSection:visible'); // 无数据时会隐藏区块，无需记页码
                for(var i = 0; i < loadSection.length; i++) {
                    loadSection.eq(i).find('.pageNum').find('.num').text(i);
                }
            }, 0); // 异步让数据先渲染
        });
        bindGlobalEvent();
    };

    var tModal = new testreportModal();
    ko.applyBindings(tModal, document.getElementById("testreport"));
});