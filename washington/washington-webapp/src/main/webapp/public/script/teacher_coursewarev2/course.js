/**
 * @author: pengmin.chen
 * @description: "课件大赛-全部作品"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'jqPaginator', 'voxLogs'], function ($, ko, YQ) {
    var courseModal = function () {
        var self = this;
        var orderList = [
            {
                tabName: '评分最高',
                tabStatus: 1
            },
            {
                tabName: '最新上传',
                tabStatus: 2
            }
        ];
        var subjectList = [
            {
                englishName: "",
                id: 0,
                name: "全部"
            },
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
        var gradeList = [
            {
                id: 0,
                name: "全部"
            },
            {
                id: 1,
                name: "一年级"
            },
            {
                id: 2,
                name: "二年级"
            },
            {
                id: 3,
                name: "三年级"
            },
            {
                id: 4,
                name: "四年级"
            },
            {
                id: 5,
                name: "五年级"
            },
            {
                id: 6,
                name: "六年级"
            }
        ];
        var awardList = [
            {
                id: 0,
                name: "全部"
            },
            {
                id: 1,
                name: "国家级"
            },
            {
                id: 2,
                name: "省级"
            },
            {
                id: 3,
                name: "市级"
            },
            {
                id: 4,
                name: "校级"
            },
            {
                id: 5,
                name: "其他"
            }
        ];
        doTrack('o_wyyn64FCaE');
        $.extend(self, {
            orderList: ko.observableArray(orderList), // 过滤条件
            subjectList: ko.observableArray(subjectList), // 学科列表
            gradeList: ko.observableArray(gradeList), // 年级列表
            awardList: ko.observableArray(awardList), // 获奖级别列表

            courseList: ko.observableArray([]), // 课件列表
            currentPageIndex: ko.observable(1), // 课件分页（当前页索引，初始或切换tab时变成1）
            totalNum: ko.observable(0), // 数目
            needInitJQPagationFlag: ko.observable(true), // 需要初始化分页（初始）

            choiceOrderInfo: ko.observable(orderList[0]), // 选择的过滤条件
            choiceSubjectInfo: ko.observable(subjectList[0]), // 选择的学科
            choiceGradeInfo: ko.observable(gradeList[0]), // 选择的年级
            choiceAwardInfo: ko.observable(awardList[0]), // 选择的年级
            inputKeyWord: ko.observable(''), // 搜索的关键词
            serarchKeyWordFlag: ko.observable(false), // 是否带关键词搜索

            // 选择过滤条件
            choiceOrder: function (data) {
                doTrack('o_4k0w8o8Sgc', userInfo.subject, data.tabName);
                self.choiceOrderInfo(data);
                self.needInitJQPagationFlag(true); // 置为true，需要重新初始分页插件
                requestAllCourse(0);
            },
            // 选择学科
            choiceSubject: function (data) {
                doTrack('o_EgEses31PM', userInfo.subject, data.name);
                self.choiceSubjectInfo(data);
                self.needInitJQPagationFlag(true);
                requestAllCourse(0);
            },
            // 选择年级
            choiceGrade: function (data) {
                doTrack('o_4k0w8o8Sgc', userInfo.subject, data.name);
                self.choiceGradeInfo(data);
                self.needInitJQPagationFlag(true);
                requestAllCourse(0);
            },
            // 选择年级
            choiceAward: function (data) {
                self.choiceAwardInfo(data);
                self.needInitJQPagationFlag(true);
                requestAllCourse(0);
            },

            // 搜索框回车
            inputKeyWordKeyUp: function () {
                if (event.keyCode == '13') { // 回车
                    self.needInitJQPagationFlag(true);
                    resetFilter();
                    requestAllCourse(0);
                }
            },
            // 点击搜索icon，搜索课件
            searchKeyWord: function () {
                self.needInitJQPagationFlag(true);
                resetFilter();
                requestAllCourse(0);
            },

            // 跳转详情页
            toDetailPage: function (data) {
                doTrack('o_pEi7oBsgfj', userInfo.subject, data.courseId);
                window.open('/courseware/contest/detail.vpage?courseId=' + data.courseId);
            }
        });

        // 重置搜索条件（当触发搜索时）
        function resetFilter() {
            self.choiceOrderInfo(orderList[0]); // 选择的过滤条件
            self.choiceSubjectInfo(subjectList[0]); // 选择的学科
            self.choiceGradeInfo(gradeList[0]); // 选择的年级
            self.choiceAwardInfo(awardList[0]); // 选择的年级
        }

        // 请求首屏数据
        function requestAllCourse(pageIndex) {
            $.ajax({
                url: '/courseware/contest/allCourses.vpage',
                type: 'GET',
                data: {
                    clazzLevel: self.choiceGradeInfo().id,
                    subject: self.choiceSubjectInfo().id,
                    awardLevelId: self.choiceAwardInfo().id,
                    orderMode: self.choiceOrderInfo().tabStatus, // 排序方式，1为按评分排序，2为按时间排序
                    pageNum: pageIndex,
                    pageSize: 8,
                    keyword: $.trim(self.inputKeyWord())
                },
                success: function (res) {
                    if (res.success) {
                        self.currentPageIndex(pageIndex);
                        self.courseList(res.data);
                        self.totalNum(res.total);

                        if ($.trim(self.inputKeyWord())) {
                            self.serarchKeyWordFlag(true);
                        } else {
                            self.serarchKeyWordFlag(false);
                        }

                        if (self.needInitJQPagationFlag()) {
                            initJQPagination(res.total, pageIndex);
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            });
        }

        // 初始化分页(totalCount: 总数, pageIndex)
        function initJQPagination (totalCount, pageIndex) {
            if (totalCount <= 8) {
                $('#JS-pagination').hide();
            } else {
                $('#JS-pagination').show().jqPaginator({
                    totalPages: Math.ceil(totalCount / 8) , // 分页的总页数，8个为1页
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
                            requestAllCourse(num - 1);
                        }
                    }
                });
            }
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

        requestAllCourse(0); // 请求首屏数据
    };

    ko.applyBindings(new courseModal(), document.getElementById('courseContent'));
});