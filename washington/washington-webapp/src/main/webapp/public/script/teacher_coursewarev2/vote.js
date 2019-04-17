/**
 * @author: pengmin.chen
 * @description: "课件大赛-"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'jqPaginator', 'voxLogs'], function ($, ko, YQ) {
    var voteModal = function () {
        var self = this;
        var orderList = [
            {
                id: 'default',
                name: '默认'
            },
            {
                id: 'canvass',
                name: '得票高低'
            }
        ];
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

        doTrack('o_hDKfMvWJ63');
        $.extend(self, {
            voteOver: ko.observable(false), // 投票倒计时结束
            leftVoteTime: ko.observable(0), // 今日剩余投票数
            isAuth: ko.observable(false), // 是否是认证用户
            canVoteFlag: ko.observable(true), // 是否可投票（倒计时是否结束）
            leftTimeObj: ko.observable({}), // 倒计时剩余的时
            isShowAllRule: ko.observable(false), // 是否展开规则
            orderList: ko.observableArray(orderList), // 排序方式列表
            subjectList: ko.observableArray(subjectList), // 学科列表
            choiceOrderInfo: ko.observable(orderList[0]), // 选择的排序信息
            choiceSubjectInfo: ko.observable(subjectList[0]), // 选择的学科信息
            allCourseList: ko.observableArray([]), // 全部课件列表
            courseList: ko.observableArray([]), // 课件列表
            inputKeyWord: ko.observable(''), // 搜索的关键词
            serarchKeyWordFlag: ko.observable(false), // 是否带关键词搜索

            isShowCommonAlert: ko.observable(false), // 通用弹窗
            commonAlertOpt: ko.observable({}), // 通用弹窗配置

            // 选择排序方式
            choiceOrder: function (data) {
                doTrack('o_R3OWcPJ5n5', data.name);
                self.choiceOrderInfo(data);
                getVoteCourseList();
            },
            // 选择学科
            choiceSubject: function (data) {
                doTrack('o_G4w36kB3Bm', data.name);
                self.choiceSubjectInfo(data);
                getVoteCourseList();
            },
            // 收起或展开规则
            showAllRule: function () {
                doTrack('o_THvc7DvENX');
                self.isShowAllRule(!self.isShowAllRule());
            },
            // 搜索框回车
            inputKeyWordKeyUp: function () {
                if (event.keyCode == '13') { // 回车
                    getVoteCourseList();
                }
            },
            // 点击搜索icon，搜索课件
            searchKeyWord: function () {
                getVoteCourseList();
            },
            // 投票
            voteCourse: function (data) {
                doTrack('o_b9dgg0Loz5', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
                if (!data.surplus || self.voteOver()) { // 当前作品没有投票次数 或 活动已结束
                    return;
                }
                if (!self.leftVoteTime()) { // 总次数为0
                    if (self.isAuth()) {
                        commonAlertVoteFailAuth(data);
                    } else {
                        commonAlertVoteFailNotAuth(data);
                    }
                    return;
                }
                requestVoteCourse(data);
            },
            // 跳转详情页
            toDetailPage: function (data) {
                doTrack('o_BlXzHB93sb', self.choiceSubjectInfo().name, data.coursewareId);
                window.open('/courseware/contest/detail.vpage?courseId=' + data.coursewareId);
            }
        });

        // 投票成功
        function commonAlertVoteSuccess(data) {
            doTrack('o_E9xeSh0Wwu', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
            showCommonAlert({
                state: 'vote-success', // 状态
                title1: '投票成功', // 大标题
                title2: '',  // 小标题
                content: '您已成功投票！点击“为TA拉票”，让更多老师为您喜爱的作品投票！', // html形式
                left_btn_text: '为TA拉票',
                right_btn_text: '为其他作品投票',
                left_btn_cb: function () {
                    doTrack('o_SNLcLSyDIp', self.choiceSubjectInfo().name, data.coursewareId, '投票成功弹窗');
                    requestCanvass(data);
                    var mobileCourseUrl = window.location.protocol + '//' + window.location.host + '/view/mobile/teacher/activity2018/coursewarematch/detail?courseId=' + data.coursewareId + '&referrer=pc_share';
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                },
                right_btn_cb: function () {
                    doTrack('o_Gxk2guy1EL', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
                    self.isShowCommonAlert(false);
                }
            });
        }

        // 投票失败-已认证
        function commonAlertVoteFailAuth(data) {
            doTrack('o_c0eKJ0mPOS', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
            showCommonAlert({
                state: 'vote-share', // 状态
                title1: '投票失败', // 大标题
                title2: '',  // 小标题
                content: '您今天的10次投票机会已用尽了哦～<br>点击“为Ta拉票”，让更多老师为您喜爱的作品投票吧！', // html形式
                left_btn_text: '为TA拉票',
                right_btn_text: '查看投票结果',
                left_btn_cb: function () {
                    doTrack('o_SNLcLSyDIp', self.choiceSubjectInfo().name, data.coursewareId, '投票失败弹窗');
                    requestCanvass(data);
                    var mobileCourseUrl = window.location.protocol + '//' + window.location.host + '/view/mobile/teacher/activity2018/coursewarematch/detail?courseId=' + data.coursewareId + '&referrer=pc_share';
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                },
                right_btn_cb: function () {
                    doTrack('o_mSulHYlkov', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
                    self.isShowCommonAlert(false);
                }
            });
        }

        // 投票失败-未认证
        function commonAlertVoteFailNotAuth(data) {
            doTrack('o_c0eKJ0mPOS', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
            showCommonAlert({
                state: 'vote-fail', // 状态
                title1: '投票失败', // 大标题
                title2: '',  // 小标题
                content: '您今天的5次投票机会已用尽了哦～<br>点击“去认证”，解锁每天10次投票机会！', // html形式
                left_btn_text: '为TA拉票',
                right_btn_text: '去认证',
                left_btn_cb: function () {
                    doTrack('o_SNLcLSyDIp', self.choiceSubjectInfo().name, data.coursewareId, '投票失败弹窗');
                    requestCanvass(data);
                    var mobileCourseUrl = window.location.protocol + '//' + window.location.host + '/view/mobile/teacher/activity2018/coursewarematch/detail?courseId=' + data.coursewareId + '&referrer=pc_share';
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                },
                right_btn_cb: function () {
                    doTrack('o_EyNJ7kmyZA', self.choiceSubjectInfo().name, data.coursewareId, '投票汇聚页');
                    window.open('/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', '_blank'); // 认证地址
                }
            });
        }

        // 显用通用弹窗（不满足条件、次数超限、评价成功、下载成功等）
        function showCommonAlert(option) {
            // 弹窗配置
            var default_opt = {
                state: 'success', // 状态：success、error
                title1: '', // 大标题
                title2: '',  // 小标题
                content: '内容', // html形式
                left_btn_text: '取消',
                right_btn_text: '确定',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    self.isShowCommonAlert(false);
                }
            };
            var opt = $.extend(default_opt, option);

            self.isShowCommonAlert(true);
            self.commonAlertOpt(opt);
        }

        // 获取投票列表
        function getVoteCourseList(){
            $.ajax({
                url: '/courseware/canvass/list.vpage',
                type: 'GET',
                data: {
                    subject: self.choiceSubjectInfo().englishName,
                    sort: self.choiceOrderInfo().id,
                    keyword: $.trim(self.inputKeyWord())
                },
                success: function (res) {
                    if (res.success) {
                        // 增加rankIndex排序索引
                        var responseDate = res.canvassData;
                        for (var i = 0; i < responseDate.length; i++) {
                            responseDate[i].rankIndex = i + 1;
                        }
                        self.allCourseList(responseDate); // 投票课件列表
                        self.leftVoteTime(res.totalSurplus); // 今日剩余投票数
                        self.isAuth(res.auth); // 是否是认证用户
                        setCountdown(new Date().getTime() + res.leftTime); // 剩余倒计时

                        initJQPagination(res.canvassData.length || 0);

                        if ($.trim(self.inputKeyWord())) {
                            self.serarchKeyWordFlag(true);
                        } else {
                            self.serarchKeyWordFlag(false);
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            });
        }

        // 初始化分页(totalCount: 总数)
        function initJQPagination (totalCount) {
            if (totalCount) {
                $('#JS-pagination').jqPaginator({
                    totalPages: Math.ceil(totalCount / 8) , // 分页的总页数，8个为1页
                    visiblePages: 5, // 同时展示的页码数
                    currentPage: 1, // 当前的页面
                    first: '<li class="first"><<</li>',
                    prev: '<li class="prev"><</li>',
                    page: '<li class="page">{{page}}</li>',
                    next: '<li class="next">></li>',
                    last: '<li class="last">>></li>',
                    onPageChange: function (num) {
                        self.courseList(self.allCourseList().slice((num - 1) * 8, num * 8));
                    }
                });
            } else {
                self.courseList([]);
            }

            if (totalCount <= 8) { // 个数少于8，不显示分页器
                $('#JS-pagination').hide();
            } else {
                $('#JS-pagination').show();
            }
        }

        // 投票
        function requestVoteCourse(data) {
            $.ajax({
                url: '/courseware/canvass/vote.vpage',
                type: 'GET',
                data: {
                    courseId: data.coursewareId
                },
                success: function (res) {
                    if (res.success) {
                        self.leftVoteTime(res.totalSurplus); // 剩余总次数
                        var tmp = clone(data); // 此处如果直接修改data里面的值，ko是无法同步视图的
                        tmp.totalCanvassNum = res.canvassNum; // 当前作品得票数
                        tmp.surplus = res.surplus; // 当前作品剩余投票次数
                        self.courseList.replace(data, tmp);

                        commonAlertVoteSuccess(data);
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            });
        }

        // 拉票
        function requestCanvass(data) {
            $.ajax({
                url: '/courseware/canvass/canvass.vpage',
                data: {
                    courseId: data.coursewareId
                },
                success: function (res) {
                    if (!res.success) {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
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

        // 秒转换成天时分秒
        function timeFormat (ms) {
            var timeObj = {}, day, hour, min, sec, s = parseInt(ms / 1000, 10);
            if(s > -1){
                day = parseInt(s / 86400);
                hour = Math.floor(s / 3600) % 24;
                min = Math.floor(s / 60) % 60;
                sec = s % 60;

                timeObj.day = ('0' + day).slice(-2);
                timeObj.hour = ('0' + hour).slice(-2);
                timeObj.min = ('0' + min).slice(-2);
                timeObj.sec = ('0' + sec).slice(-2);
            }
            return timeObj;
        }

        // 递归倒计时（参数为倒计时结束时的ms）
        function setCountdown (endMillSec) {
            var leftMillSec = endMillSec -new Date().getTime(); // 剩余时间 = 结束时间 - 当前时间，WHY: 防止退到后台倒计时暂停
            if (leftMillSec < 0) {
                self.voteOver(true); // 倒计时结束了，不可再投票
                return;
            }
            self.leftTimeObj(timeFormat(leftMillSec));
            setTimeout(setCountdown, 1000, endMillSec); // 递归调用
        }

        // 克隆对象
        function clone (Obj) {
            var buf;
            if (Obj instanceof Array) {
                var i = Obj.length;
                while (i--) {
                    buf[i] = clone(Obj[i]);
                }
                return buf;
            } else if (Obj instanceof Object) {
                buf = {};
                for (var k in Obj) {
                    buf[k] = clone(Obj[k]);
                }
                return buf;
            } else {
                return Obj;
            }
        }


        getVoteCourseList();
    };

    ko.applyBindings(new voteModal(), document.getElementById('voteContent'));
});