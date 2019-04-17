/**
 * @author: pengmin.chen
 * @description: "课件大赛-排行榜"
 * @createdDate: 2018/10/26
 * @lastModifyDate: 2018/10/29
 */

define(['jquery', 'knockout', 'YQ', 'jqPaginator', 'voxLogs'], function ($, ko, YQ) {
    var rankingModal = function () {
        var self = this;
        var stepOneMonthDay = new Date('11/19/2018 00:00:00').getTime(); // 月榜第一期节点
        var stepTwoMonthDay = new Date('12/17/2018 00:00:00').getTime(); // 月榜第二期节点
        var todayNowTime = new Date().getTime(); // 当前时间
        var todayYMD = new Date().getFullYear() + '-' + ('0' + (new Date().getMonth() + 1)).slice(-2) + '-' + ('0' + new Date().getDate()).slice(-2); // 今天 年月日

        var subjectList = [
            {
                englishName: "CHINESE",
                id: 101,
                name: "语文"
            },
            {
                englishName: "MATH",
                id: 102,
                name: "数学"
            },
            {
                englishName: "ENGLISH",
                id: 103,
                name: "英语"
            }
        ];
        var dateTypeList = [
            {
                id: 0,
                name: "日榜"
            },
            {
                id: 1,
                name: "周榜"
            },
            {
                id: 2,
                name: "月榜"
            },
            {
                id: 3,
                name: "总榜"
            }
        ];
        var bigRankTypeArr = ['最具人气排行榜', '高分作品排行榜', '点评达人排行榜'];
        doTrack('o_TH2ZyeS5Pi');
        $.extend(self, {
            activeBigRankType : ko.observable(0), // 当前激活的排行榜(人气榜0、高分作品榜1、点评达人榜2)
            rankTitle: ko.observable(''),
            rankUpdateTime: ko.observable(''),
            rankNotification: ko.observable(''), // 通知文案
            rankNotificationFlag: ko.observable(false), // 通知文案是否展示
            rankNotificationClosed: ko.observable(false), // 通知文案是否被关闭过
            showRuleAlert: ko.observable(false), // 规则弹窗是否展示
            needInitJQPagationFlag: ko.observable(true), // 需要初始化分页（初始）

            subjectList: ko.observableArray(subjectList), // 学科
            dateTypeList: ko.observableArray(dateTypeList), // 日期过滤列表（人气榜:日榜、周榜、总榜; 高分作品榜:周榜、月榜、总榜; 点评达人榜:日榜、周榜、总榜）
            dateList: ko.observableArray([]), // 日列表数据
            weekList: ko.observableArray([]), // 周列表数据
            monthList: ko.observableArray([]), // 月列表数据
            choiceSubjectInfo: ko.observable(subjectList[0]), // 选择的学科信息
            choiceDateTypeInfo: ko.observable(dateTypeList[0]), // 选择的日期类型
            choiceDateInfo: ko.observable(''), // 选择的日期（日榜对应）
            choiceWeekInfo: ko.observable({}), // 选择的星期数据（周榜对应）
            choiceMonthInfo: ko.observable({}), // 选择的月份数据（月榜对应）
            choiceDatePeriodText: ko.observable(''), // 期数文案

            showSubjectList: ko.observable(false), // 是否展示学科下拉
            showDateTypeList: ko.observable(false), // 是否展示日期类型下拉
            showDayPickerPanel: ko.observable(false), // 是否展示时间选择器
            showDateList: ko.observable(false), // 是否展示日期下拉
            showWeekList: ko.observable(false), // 是否展示月份下拉
            showMonthList: ko.observable(false), // 是否展示月份下拉

            popularRankList: ko.observableArray([]), // 人气榜数据
            excellentRankList: ko.observableArray([]), // 高分作品数据
            juageRankList: ko.observableArray([]), // 点评达人数据
            juageUserData: ko.observable({}), // 点评达人自己的数据
            pageIndex: ko.observable(0), // 页码

            // 选择排行榜类型(人气榜、高分作品榜、大屏评委达人榜)
            choiceBigRankType: function (index) {
                if (index === self.activeBigRankType()) return;
                self.activeBigRankType(index);
                resetFilterCondition(index);
                controlParaInfo(index, self.choiceDateTypeInfo().id);
                self.needInitJQPagationFlag(true);
                requestRankList(0);
                doTrack('o_bH7avBh8tm', bigRankTypeArr[index]);
            },
            // 显示规则
            showRule: function () {
                self.showRuleAlert(true);
                doTrack('o_ckha2xVA1K', bigRankTypeArr[self.activeBigRankType()], userInfo.subject);
            },
            // 关闭通知
            closeNotification: function () {
                self.rankNotificationFlag(false);
                self.rankNotificationClosed(true); // 记录已经被手动关闭过一次，后续不再显示
            },
            // 点击学科下拉
            clickSubject: function (data, event) {
                self.showSubjectList(!self.showSubjectList());
                self.showDateTypeList(false);
                self.showDateList(false);
                self.showWeekList(false);
                self.showMonthList(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击日期榜单下拉
            clickDateType: function (data, event) {
                self.showDateTypeList(!self.showDateTypeList());
                self.showSubjectList(false);
                self.showDateList(false);
                self.showWeekList(false);
                self.showMonthList(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击日期下拉
            clickDate: function (data, event) {
                self.showDateList(!self.showDateList());
                self.showSubjectList(false);
                self.showDateTypeList(false);
                self.showWeekList(false);
                self.showMonthList(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击周下拉
            clickWeek: function (data, event) {
                self.showWeekList(!self.showWeekList());
                self.showSubjectList(false);
                self.showDateTypeList(false);
                self.showDateList(false);
                self.showMonthList(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击月份下拉
            clickMonth: function (data, event) {
                self.showMonthList(!self.showMonthList());
                self.showSubjectList(false);
                self.showDateTypeList(false);
                self.showDateList(false);
                self.showWeekList(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 选择学科
            choiceSubject: function(data) {
                self.choiceSubjectInfo(data);
                self.needInitJQPagationFlag(true);
                requestRankList(0);
                doTrack('o_PfiHJHurrr', data.name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name);
            },
            // 选择日期类型
            choiceDateType: function(data) {
                self.choiceDateTypeInfo(data);
                controlParaInfo(self.activeBigRankType(), data.id);
                self.needInitJQPagationFlag(true);
                requestRankList(0);
                doTrack('o_MtXjTH5qhd', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name);
            },
            // 选择日期
            choiceDate: function (data) {
                self.choiceDateInfo(data);
                self.choiceDatePeriodText((getWeekDay() === 1 ? '上' : '本') + '周第' + getWeekDay(self.choiceDateInfo()) + '天');
                self.needInitJQPagationFlag(true);
                requestRankList(0);
                doTrack('o_IgYIb0kP0D', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, self.choiceDateInfo());
            },
            // 选择周期数
            choiceWeek: function (data) {
                self.choiceWeekInfo(data);
                self.choiceDatePeriodText('第' + data.period + '期');
                self.needInitJQPagationFlag(true);
                requestRankList(0);
                doTrack('o_IgYIb0kP0D', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, self.choiceWeekInfo().text);
            },
            // 选择月份期数
            choiceMonth: function (data) {
                self.choiceMonthInfo(data);
                self.choiceDatePeriodText('第' + data.id + '期');
                self.needInitJQPagationFlag(true);
                requestRankList(0);
                doTrack('o_IgYIb0kP0D', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, self.choiceMonthInfo().text);
            },
            // 跳转资源详情页
            toDetailPage: function (data) {
                if (self.choiceDateTypeInfo().id === 0) { // 日榜
                    doTrack('o_lrEn201TzQ', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, self.choiceDateInfo(), data.coursewareId);
                } else if (self.choiceDateTypeInfo().id === 1) { // 周榜
                    doTrack('o_lrEn201TzQ', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, self.choiceWeekInfo().text, data.coursewareId);
                } else if (self.choiceDateTypeInfo().id === 2) { // 月榜
                    doTrack('o_lrEn201TzQ', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, self.choiceMonthInfo().text, data.coursewareId);
                } else { // 总榜
                    doTrack('o_lrEn201TzQ', self.choiceSubjectInfo().name, bigRankTypeArr[self.activeBigRankType()], self.choiceDateTypeInfo().name, '', data.coursewareId);
                }
                window.open('/courseware/contest/detail.vpage?courseId=' + data.coursewareId);
            }
        });
        
        // 根据链接参数tab激活排行榜类型
        function initRankType() {
            setTimeout(function () {
                if (YQ.getQuery('rankType')) {
                    self.choiceBigRankType(+YQ.getQuery('rankType'));
                }
            }, 10);
        }

        // 根据激活的榜单类型和过滤类型 来展示不同的文案（参数bigRankType表示排行榜类型，参数dateRankType表示过滤日期的类型）
        function controlParaInfo(bigRankType, dateRankType) {
            if (bigRankType === 0) { // 人气榜
                self.rankTitle('人气作品榜');
                if (dateRankType === 1) { // 周榜
                    if (!self.rankNotificationClosed()) self.rankNotificationFlag(true);
                    self.rankNotification('每周周榜前三作品，上榜”每周最具人气作品“，获得官方证书、奖杯、价值300元奖品。');
                } else if (dateRankType === 3) { // 总榜
                    if (!self.rankNotificationClosed()) self.rankNotificationFlag(true);
                    self.rankNotification('截止至12月20日23:59，总榜前三作品，上榜“年度最具人气作品”，获得官方证书、奖杯、价值1000元奖品。');
                } else {
                    self.rankNotificationFlag(false);
                }
            } else if (bigRankType === 1) { // 高分作品榜
                self.rankTitle('高分作品榜');
                if (dateRankType === 2) { // 月榜
                    if (!self.rankNotificationClosed()) self.rankNotificationFlag(true);
                    self.rankNotification('月榜前三作品，评为“月度优秀教学设计作品”，获得官方证书、奖杯、价值500元奖品。');
                } else if (dateRankType === 3) { // 总榜
                    if (!self.rankNotificationClosed()) self.rankNotificationFlag(true);
                    self.rankNotification('截止至12月20日23:59，总榜前10%作品进入年度专家评审（各科最高取100份）。');
                } else {
                    self.rankNotificationFlag(false);
                }
            } else if (bigRankType === 2) { // 点评达人榜
                self.rankTitle('点评达人榜');
                if (dateRankType === 1) { // 周榜
                    if (!self.rankNotificationClosed()) self.rankNotificationFlag(true);
                    self.rankNotification('每周周榜前五的老师，评为“点评达人”，获得价值199元大礼包。');
                } else {
                    self.rankNotificationFlag(false);
                }
            }

            if (dateRankType === 0) { // 日榜
                self.rankUpdateTime('每日更新');
                self.choiceDatePeriodText((getWeekDay() === 1 ? '上' : '本') + '周第' + getWeekDay(self.choiceDateInfo()) + '天');
            } else if (dateRankType === 1) { // 周榜
                self.rankUpdateTime('每周更新');
                self.choiceDatePeriodText('第'+ self.choiceWeekInfo().period + '期');
            } else if (dateRankType === 2) { // 月榜
                self.rankUpdateTime('每月1号更新');
                self.choiceDatePeriodText('第'+ self.choiceMonthInfo().id + '期');
            } else if (dateRankType === 3) { // 总榜
                self.rankUpdateTime('每日更新');
                self.choiceDatePeriodText('');
            }
        }

        // 重置过滤条件（切换大排行榜时）
        // 注：此处在活动快要结束时增加需求（人气、达人隐藏日榜，优秀隐藏周榜）故进行了一次优化（但原有逻辑还是再实现并保留了）
        // 如有二期, 去除人气榜、达人榜隐藏的日榜，展开优秀榜注释的代码即可
        function resetFilterCondition(bigRankType) {
            self.choiceSubjectInfo(subjectList[0]); // 学科置为第一个[语文]
            if (bigRankType === 0) { // 人气榜:日榜、周榜、总榜（后期增加固定隐藏日榜）
                if (!self.weekList().length) {
                    // 注： 此处全部通过动态删减的方式得到展示的列表，而没用重复性的写数据，维护成本更低
                    self.dateTypeList(filterDateType(dateTypeList, [0, 1, 2])); // 删除周、月
                } else {
                    self.dateTypeList(filterDateType(dateTypeList, [0, 2])); // 删除月
                }
                self.choiceDateTypeInfo(self.dateTypeList()[0]);
            } else if (bigRankType === 1) { // 高分作品榜:周榜、月榜、总榜（后期增加固定隐藏周榜）
                // 前期逻辑（前期上线时判断了月份）
                // if (!self.weekList().length && todayNowTime < stepOneMonthDay) { // 不存在week 且 2018-11-19之前
                //     self.dateTypeList(filterDateType(dateTypeList, [0, 1, 2])); // 删除日、周、月
                // } else if (!self.weekList().length && todayNowTime >= stepOneMonthDay) { // 不存在week 且 在2018-11-19之后
                //     self.dateTypeList(filterDateType(dateTypeList, [0, 1])); // 删除日、周
                // } else if (self.weekList().length && todayNowTime < stepOneMonthDay) { // 存在week 且 2018-11-19之前
                //     self.dateTypeList(filterDateType(dateTypeList, [0, 2])); // 删除日、月
                // } else { // 存在week 且 在 2018-11-19之后
                //     self.dateTypeList(filterDateType(dateTypeList, [0])); // 删除日
                // }

                // 后期逻辑
                self.dateTypeList(filterDateType(dateTypeList, [0, 1])); // 删除日、周

                self.choiceDateTypeInfo(self.dateTypeList()[0]);
            } else if (bigRankType === 2) { // 大众达人榜:日榜、周榜、总榜（后期增加固定隐藏日榜）
                if (!self.weekList().length) {
                    self.dateTypeList(filterDateType(dateTypeList, [0, 1, 2])); // 删除周、月
                } else {
                    self.dateTypeList(filterDateType(dateTypeList, [0, 2])); // 删除月
                }
                self.choiceDateTypeInfo(self.dateTypeList()[0]);
            }
        }

        // 从日周月年中过滤不需要的(dateTypeList为原数组，filterIndexArr为需要过滤的索引数组, 如[0, 2]表示删除日榜和月榜)
        function filterDateType(dateTypeList, filterIndexArr) {
            var temporaryDateTypeList = dateTypeList.slice(0); // 使用数组slice方法产生一个不会影响源数据的数组（concat、filter、some等都不会修改）
            for (var i = 0; i < temporaryDateTypeList.length; i++) {
                if (filterIndexArr.indexOf(temporaryDateTypeList[i].id) > -1) { // 当前项需要删除
                    temporaryDateTypeList.splice(i, 1);
                    i--;
                }
            }
            return temporaryDateTypeList;
        }

        // 设置日期数据(每周一展示上周的数据，非周一展示本周当天之前的数据)
        function setDateList() {
            var dateList = [];
            if (getWeekDay() === 1) { // 遍历去取上周7天的日期
                for (var i = 1; i <= 7; i++) {
                    dateList.push(countDay(todayYMD, -i));
                }
            } else {
                for (var i = 1; i < getWeekDay(); i++) {
                    dateList.push(countDay(todayYMD, -i));
                }
            }
            self.dateList(dateList);
            self.choiceDateInfo(self.dateList()[0]);
            self.choiceDatePeriodText((getWeekDay() === 1 ? '上' : '本') + '周第' + getWeekDay(self.choiceDateInfo()) + '天');
        }

        // 设置月份数据(两个时间节点产生两条数据)
        function setMonthList() {
            if (todayNowTime >= stepOneMonthDay) { // 11/21/2018 开始展示榜单
                self.monthList([
                    {
                        id: 1,
                        text: '2018-09-25 ~ 2018-11-18'
                    }
                ]);
                self.choiceMonthInfo(self.monthList()[0]);
            }
            if (todayNowTime >= stepTwoMonthDay) {
                self.monthList([
                    {
                        id: 2,
                        text: '2018-11-19 ~ 2018-12-16'
                    },
                    {
                        id: 1,
                        text: '2018-09-25 ~ 2018-11-18'
                    }
                ]);
                self.choiceMonthInfo(self.monthList()[0]);
            }
        }

        // 请求周列表数据
        function requestWeekList() {
            $.ajax({
                url: '/courseware/rank/weeklyTimes.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.success) {
                        if (res.data.length) {
                            var responseData = res.data;
                            for (var i = 0; i < responseData.length; i++) {
                                responseData[i].text = responseData[i]._startTime + ' ~ ' + responseData[i]._endTime;
                            }
                            self.weekList(responseData);
                            self.choiceWeekInfo(self.weekList()[0]);
                        }
                        resetFilterCondition(0); // 首次设置
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            })
        }

        // 请求列表数据
        function requestRankList(pageIndex) {
            if (self.activeBigRankType() === 0) {
                requestPopuplarRankList(pageIndex);
            } else if (self.activeBigRankType() === 1) {
                requestExcellentRankList(pageIndex);
            } else if (self.activeBigRankType() === 2) {
                requestJudgeRankList(pageIndex);
            }
        }

        // 请求人气榜数据
        function requestPopuplarRankList(pageIndex) {
            var dateType = self.choiceDateTypeInfo().id;
            var data = {
                type: dateType,
                subject: self.choiceSubjectInfo().englishName,
                pageNum: pageIndex,
                pageSize: 10
            };
            // 参数说明:
            // type: 日期榜单类型（0、1、2、3）
            // period: 期数（周榜使用，传第几期）
            // subject: 学科
            // date: 日期（日榜使用）
            // pageNum: 第几页
            // pageSize: 几条数据
            if (dateType === 0) { // 日榜
                data = $.extend(data, {
                    period: '',
                    date: self.choiceDateInfo()
                });
            } else if (dateType === 1) { // 周榜
                data = $.extend(data, {
                    period: self.choiceWeekInfo().period,
                    date: ''
                });
            } else if (dateType === 3) { // 总榜
                data = $.extend(data, {
                    period: '',
                    date: ''
                });
            }
            $.ajax({
                url: '/courseware/rank/popularityRank.vpage',
                type: 'GET',
                data: data,
                success: function (res) {
                    if (res.success) {
                        self.pageIndex(res.pageNum);
                        var responseDate = res.data;
                        for (var i = 0; i < responseDate.length; i++) {
                            responseDate[i].rankIndex = (res.pageNum - 1) * 10 + (i + 1);
                        }
                        self.popularRankList(responseDate);
                        if (self.needInitJQPagationFlag()) {
                            initJQPagination(res.total || 0, pageIndex);
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            });
        }

        // 请求高分作品数据
        function requestExcellentRankList(pageIndex) {
            var dateType = self.choiceDateTypeInfo().id;
            var data = {
                type: dateType,
                subject: self.choiceSubjectInfo().englishName,
                pageNum: pageIndex,
                pageSize: 10
            };
            // 参数说明:
            // type: 日期榜单类型（0、1、2、3）
            // period: 期数（周榜、月榜使用，传第几期）
            // subject: 学科
            // pageNum: 第几页
            // pageSize: 几条数据
            if (dateType === 1) { // 周榜
                data = $.extend(data, {
                    period: self.choiceWeekInfo().period
                });
            } else if (dateType === 2) { // 月榜
                data = $.extend(data, {
                    period: self.choiceMonthInfo().id
                });
            } else if (dateType === 3) { // 总榜
                data = $.extend(data, {
                    period: ''
                });
            }
            $.ajax({
                url: '/courseware/rank/excellentRank.vpage',
                type: 'GET',
                data: data,
                success: function (res) {
                    if (res.success) {
                        self.pageIndex(res.pageNum);
                        var responseDate = res.data;
                        for (var i = 0; i < responseDate.length; i++) {
                            responseDate[i].rankIndex = (res.pageNum - 1) * 10 + (i + 1);
                        }
                        self.excellentRankList(responseDate);
                        if (self.needInitJQPagationFlag()) {
                            initJQPagination(res.total || 0, pageIndex);
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 请求点评榜数据
        function requestJudgeRankList(pageIndex) {
            var dateType = self.choiceDateTypeInfo().id;
            var data = {
                type: dateType,
                subject: self.choiceSubjectInfo().englishName,
                pageNum: pageIndex,
                pageSize: 10
            };
            // 参数说明:
            // type: 日期榜单类型（0、1、2、3）
            // period: 期数（周榜使用，传第几期）
            // subject: 学科
            // date: 日期（日榜使用）
            // pageNum: 第几页
            // pageSize: 几条数据
            if (dateType === 0) { // 日榜
                data = $.extend(data, {
                    period: '',
                    date: self.choiceDateInfo()
                });
            } else if (dateType === 1) { // 周榜
                data = $.extend(data, {
                    period: self.choiceWeekInfo().period,
                    date: ''
                });
            } else if (dateType === 3) { // 总榜
                data = $.extend(data, {
                    period: '',
                    date: ''
                });
            }
            $.ajax({
                url: '/courseware/rank/talentRank.vpage',
                type: 'GET',
                data: data,
                success: function (res) {
                    if (res.success) {
                        self.pageIndex(res.pageNum);
                        var responseDate = res.data;
                        for (var i = 0; i < responseDate.length; i++) {
                            responseDate[i].rankIndex = (res.pageNum - 1) * 10 + (i + 1);
                        }
                        self.juageRankList(responseDate);
                        self.juageUserData(res.userData || {});
                        if (self.needInitJQPagationFlag()) {
                            initJQPagination(res.total || 0, pageIndex);
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 初始化分页(totalCount: 总数, pageIndex)
        function initJQPagination (totalCount, pageIndex) {
            if (totalCount <= 10) {
                $('#JS-pagination').css('visibility', 'hidden');
            } else {
                $('#JS-pagination').css('visibility', 'visible').jqPaginator({
                    totalPages: Math.ceil(totalCount / 10) , // 分页的总页数，10个为1页
                    visiblePages: 5, // 同时展示的页码数
                    currentPage: pageIndex + 1, // 当前的页面
                    first: '<li class="first"><<</li>',
                    prev: '<li class="prev"><</li>',
                    page: '<li class="page">{{page}}</li>',
                    next: '<li class="next">></li>',
                    last: '<li class="last">>></li>',
                    onPageChange: function (num, type) {
                        if (type !== 'init') { // 非首次
                            self.needInitJQPagationFlag(false); // 置为false，暂时不再需要初始分页插件
                            requestRankList(num - 1);
                        }
                    }
                });
            }

            // 因为三个榜单共用了一个分页器，当用户来回切换太快时，如果上一个请求还未完成（实际有数据）就切到下一个请求（实际无数据），
            // 等前一个请求出来的时候就会造成 提示下一个请求空数据的同时 展示上一个请求的分页器

            // 再次判断当前类型的list是否有数据，无数据时隐藏分页器
            if ((self.activeBigRankType() === 0 && !self.popularRankList().length)
                || (self.activeBigRankType() === 1 && !self.excellentRankList().length)
                || (self.activeBigRankType() === 2 && !self.juageRankList().length)) {
                $('#JS-pagination').css('visibility', 'hidden');
            }
        }

        // 绑定点击
        function bindDocumentOneClick() {
            $(window).on('click', function () {
                self.showSubjectList(false);
                self.showDateTypeList(false);
                self.showSubjectList(false);
                self.showDateList(false);
                self.showWeekList(false);
                self.showMonthList(false);
            });
        }

        // 获取周几
        function getWeekDay(date) {
            var day = date ? new Date(date).getDay() : new Date().getDay();
            return (day % 7) === 0 ? 7 : day; // 周日返回0，变成取值7
        }

        // 日期加减(date: 操作的日期, days: days为正数表示加**天，负数表示减**天)
        function countDay(date, days) {
            var nowDate = new Date(date);
            nowDate.setDate(nowDate.getDate() + days);
            var month = nowDate.getMonth() + 1;
            var day = nowDate.getDate();
            if (month < 10) {
                month = '0' + month;
            }
            if (day < 10) {
                day = '0' + day;
            }
            var YMD = nowDate.getFullYear() + '-' + month + '-' + day;
            return YMD;
        }

        // 简易通用弹窗
        function alertTip(content, callback) {
            var commonPopupHtml = "<div class=\"coursePopup commonAlert\">" +
                "<div class=\"popupInner popupInner-common\">" +
                "<div class=\"closeBtn commonAlertClose\"></div>" +
                "<div class=\"textBox\">" +
                "<p class=\"shortTxt\">" + content + "</p>" +
                "</div>" +
                "<div class=\"otherContent\">" +
                "<a class=\"surebtn commonSureBtn\" href=\"javascript:void(0)\">确 定</a>" +
                "</div>" +
                "</div>" +
                "</div>";
            // 不存在则插入dom
            if (!$('.commonAlert').length) {
                $('body').append(commonPopupHtml);
            }
            // 监听按钮点击
            $(document).one('click', '.commonAlertClose',function(){ // 关闭
                $('.commonAlert').remove();
            }).one('click', '.commonSureBtn', function() { // 点击按钮
                if (callback) {
                    callback();
                } else {
                    $('.commonAlert').remove();
                }
            });
        }

        // 打点方法
        function doTrack () {
            var track_obj = {
                database: 'web_teacher_logs',
                module: 'm_f1Bw7hDbxx'
            };
            for (var i = 0; i < arguments.length; i++) {
                if (i === 0) {
                    track_obj['op'] = arguments[i];
                } else {
                    track_obj['s' + (i - 1)] = arguments[i];
                }
            }
            YQ.voxLogs(track_obj);
        }

        $(function () {
            initRankType();
            controlParaInfo(0, 0); // 控制文案
            setDateList(); // 设置周数据
            requestWeekList(); // 请求周数据
            setMonthList(); // 设置月数据
            requestRankList(0); // 请求初始榜单数据
        });
    };
    ko.applyBindings(new rankingModal(), document.getElementById('rankingContent'));
});