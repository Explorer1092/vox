/**
 * @author: pengmin.chen
 * @description: "课件大赛-个人中心"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'jqPaginator', 'voxLogs'], function ($, ko, YQ) {
    var personalcenterModal = function () {
        var self = this;
        doTrack('o_3Z7k9L94vU');
        $.extend(self, {
            myCoursewareTabList: ko.observableArray([
                {
                    tabName: '全部',
                    tabStatus: ''
                },
                {
                    tabName: '已发布',
                    tabStatus: 'PUBLISHED'
                },
                {
                    tabName: '审核中',
                    tabStatus: 'EXAMINING'
                },
                {
                    tabName: '未提交',
                    tabStatus: 'DRAFT'
                },
                {
                    tabName: '被退回',
                    tabStatus: 'REJECTED'
                }
            ]), // 我的作品tab列表
            myCoursewareTabIndex: ko.observable(0), // 我的作品激活的tab（全部、已发布、审核中、未提交、被驳回）
            myCoursewareList: ko.observableArray([]), // 我的作品
            isShowDeleteCourseSure: ko.observable(false), // 删除课件确认弹窗
            isShowRejectDetail: ko.observable(false), // 驳回原因弹窗
            rejectCourseInfo: ko.observable(''), // 驳回原因
            deleteCourseId: ko.observable(''), // 删除的课件id
            myCourseCurrentPageIndex: ko.observable(1), // 我的作品分页（当前页索引，初始或切换tab时变成1）
            needInitJQPagationFlag: ko.observable(true), // 需要初始化分页（初始和删除元素后）
            // 切换个人中心我的作品tab
            switchMycourseState: function (index, data) {
                doTrack('o_CV0fMKIoHv', userInfo.subject, data.tabName);
                self.myCoursewareTabIndex(index);
                self.needInitJQPagationFlag(true); // 切换tab后，需要再次初始分页插件
                requestUserCourseInfo(data.tabStatus, 0); // 请求当前tab下的首屏数据
            },
            // 查看退回原因
            seeRejectDetail: function (data) {
                self.isShowRejectDetail(true);
                self.rejectCourseInfo(data.desc); // 驳回原因
            },
            // 编辑我的作品（未提交 + 被退回）
            editMyCourse: function (data) {
                doTrack('o_OKMxyEt4NN', userInfo.subject, data.id);
                window.location.href = '/courseware/contest/upload.vpage?courseId=' + data.id + '#nav';
            },
            // 询问是否删除（未提交 + 被退回）
            deleteMyCourse: function (data) {
                doTrack('o_fEldY3ovRQ', userInfo.subject, data.id);
                self.isShowDeleteCourseSure(true);
                self.deleteCourseId(data.id);
            },
            // 确认删除作品
            sureDeleteCourse: function () {
                doTrack('o_dBoq4zzSoe', userInfo.subject, self.deleteCourseId());
                self.isShowDeleteCourseSure(false);
                requestDeleteCourse();
            },
            // 已发布 课件可跳转到详情页
            toDetailPage: function (data) {
                window.open('/courseware/contest/detail.vpage?courseId=' + data.id);
            }
        });

        // 请求个人中心-我的作品，参数tabIndex表示对应相应的tab,pageIndex表示当前的页数
        function requestUserCourseInfo(tabStatus, pageIndex) {
            $.ajax({
                url: '/courseware/contest/myworks/list.vpage',
                type: 'GET',
                data: {
                    pageNum: pageIndex, // 第几页
                    pageSize: 8, // 几条数据
                    status: tabStatus
                },
                success: function (res) {
                    if (res.success) {
                        self.myCourseCurrentPageIndex(pageIndex); // 将当前页存储为全局变量
                        self.myCoursewareList(res.coursewares); // 接收当页数据并渲染

                        if (self.needInitJQPagationFlag()) {
                            initJQPagination(tabStatus, res.totalCount, pageIndex);
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            });
        }

        // 初始化分页(tabStatus: tab名称, totalCount: 总数, pageIndex: 页码)
        function initJQPagination (tabStatus, totalCount, pageIndex) {
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
                            requestUserCourseInfo(tabStatus, num - 1);
                        }
                    }
                });
            }
        }

        // 删除课件
        function requestDeleteCourse() {
            $.ajax({
                url: '/courseware/contest/myworks/delete.vpage',
                type: 'POST',
                data: {
                    id: self.deleteCourseId()
                },
                success: function (res) {
                    if (res.success) {
                        self.needInitJQPagationFlag(true); // 删除课件后，需要再次初始分页插件
                        var nowCoursewareTabStatus = !self.myCoursewareTabIndex() ? '' : self.myCoursewareTabList()[self.myCoursewareTabIndex()].tabStatus;
                        requestUserCourseInfo(nowCoursewareTabStatus, self.myCourseCurrentPageIndex());
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
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

        requestUserCourseInfo('', 0);
    };
    ko.applyBindings(new personalcenterModal(), document.getElementById('personalcenterContent'));
});