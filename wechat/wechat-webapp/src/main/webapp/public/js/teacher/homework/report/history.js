/**
 * @author xinqiang.wang
 * @description   "作业历史"
 * @createDate 2016/2/17
 *
 */

define(["$17", "knockout", "komapping", "jbox", 'logger'], function ($17, ko, komapping, jbox, logger) {
    "use strict";
    var ReportModel = function () {
        var self = this;
        self.getSubjectValue = function (subjectName) {
            var subjectValue = '';
            switch (subjectName) {
                case "ENGLISH":
                    subjectValue = '英语';
                    break;
                case "MATH":
                    subjectValue = '数学';
                    break;
                case "CHINESE":
                    subjectValue = '语文';
                    break;
            }
            return subjectValue;
        };
        self.subject = ko.observable(LoggerProxy.subject);
        self.subjectList = LoggerProxy.subjectList;
        self.showSubjectSelectBox = ko.observable(false);
        for (var i = 0; i < self.subjectList.length; i++) {
            self.subjectList[i]['checked'] = !i;
        }
        self.subjectList = ko.mapping.fromJS(self.subjectList);

        self.allClazzDetail = ko.observableArray([]);
        self.allClazzIds = [];
        self.selectedClazzIds = [];
        self.pageSize = 10;
        self.currentPage = 1;
        self.reportLoading = ko.observable(false);
        self.isLastPage = ko.observable(false);
        self.getSelectedClazzIds = function () {
            return self.selectedClazzIds.join(",")
        };

        self.reportDetail = ko.observableArray([]); //报告详情

        self.processRate = function (molecular, denominator) {
            molecular = +molecular || 0;
            denominator = +denominator || 0;
            if (denominator == 0) return '0%';
            return Math.ceil(molecular / denominator * 100) + "%";
        };

        self.sendLog = function (obj) {
            var def = {
                app: "teacher",
                module: 'm_pGqNIEG2',
                s0: self.subject()
            };
            $.extend(def, obj);
            logger.log(def);
        };

        /*获取班级*/
        self.getAllClazz = function () {
            $17.weuiLoadingShow();
            $.post("/teacher/homework/report/getClazzs.vpage", {subject: self.subject()}, function (data) {
                if (data.success) {
                    for (var i = 0; i < data.clazzs.length; i++) {
                        data.clazzs[i].checked = false;
                        self.allClazzIds.push(data.clazzs[i].id);
                        self.selectedClazzIds.push(data.clazzs[i].id);
                    }
                    self.allClazzDetail(ko.mapping.fromJS(data.clazzs)());
                    self.getReportList()
                } else {

                }
                $17.weuiLoadingHide();
            });
        };

        /*初始化*/
        self.getAllClazz();

        self.getReportList = function () {
            self.reportLoading(true);
            $17.weuiLoadingShow();
            $.post('/teacher/homework/history.vpage', {
                clazzIds: self.getSelectedClazzIds(),
                page: self.currentPage,
                size: self.pageSize,
                subject: self.subject()
            }, function (data) {
                if (data.success) {
                    if (data.page.content.length > 0) {
                        for (var i = 0; i < data.page.content.length; i++) {
                            var map = {
                                checked: data.page.content[i].checked,
                                clazzId: data.page.content[i].clazzId,
                                clazzName: data.page.content[i].clazzName,
                                content: data.page.content[i].content,
                                correctedCount: data.page.content[i].correctedCount,
                                endTime: data.page.content[i].endTime,
                                finishedCount: data.page.content[i].finishedCount,
                                homeworkId: data.page.content[i].homeworkId,
                                homeworkName: data.page.content[i].homeworkName,
                                includeSubjective: data.page.content[i].includeSubjective,
                                showCheck: data.page.content[i].showCheck,
                                terminated: data.page.content[i].terminated,
                                userCount: data.page.content[i].userCount,
                                homeworkType: data.page.content[i].homeworkType,
                                offlineHomeworkId : data.page.content[i].offlineHomeworkId,
                                showAssignOffline : data.page.content[i].showAssignOffline
                            };
                            self.reportDetail.push(map);

                        }
                    }
                    self.isLastPage(data.page.last);
                    self.reportLoading(false);
                } else {
                    $17.jqmHintBox(data.info);
                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
            });
        };

        /*选择要查看的班级*/
        self.selectClazzBtn = function () {
            var that = this;
            self.selectedClazzIds = [];
            self.currentPage = 1;
            that.checked(!that.checked());
            var detail = ko.mapping.toJS(self.allClazzDetail);
            for (var i = 0; i < detail.length; i++) {
                if (detail[i].checked) {
                    self.selectedClazzIds.push(detail[i].id);
                }
            }
            if (self.selectedClazzIds.length == 0) {
                self.selectedClazzIds = self.allClazzIds;
            }
            self.reportDetail([]);
            self.getReportList();
            self.sendLog({
                op: 'page_reportlist_class_select_click'
            });
        };

        self.reportScrolled = function (data, event) {
            var elem = event.target;
            if (elem.scrollTop > (elem.scrollHeight - elem.offsetHeight - 100)) {
                if (!self.isLastPage() && !self.reportLoading()) {
                    self.currentPage += 1;
                    self.getReportList();
                }
            }
        };

        /*查看作业详情*/
        self.viewHomeworkDetailBtn = function (homeworkId) {
            var that = this;
            var homeworkType = that.homeworkType, name = '';
            if (homeworkType == 'Normal') {
                name = "普通作业";
            } else if (homeworkType == 'Similar') {
                name = "改错类题作业";
            }
            self.sendLog({
                op: 'page_reportlist_see_details_click',
                s1: name,
                s2: homeworkId
            });
            setTimeout(function () {
                location.href = '/teacher/homework/report/detail.vpage?homeworkId=' + homeworkId;
            }, 200);
        };

        /*检查作业*/
        self.checkHomework = function () {
            var kh = this;
            if (!kh.showCheck) {
                return false;
            }
            var __check = function () {
                if ($17.isBlank(kh.homeworkId)) {
                    self.sendLog({
                        op: 'homeworkId_null'
                    });
                    return false;
                }
                $.post("/teacher/homework/report/check.vpage", {
                    homeworkId: kh.homeworkId,
                    subject: self.subject()
                }, function (data) {
                    if (data.success) {
                        $17.msgTip("已成功检查作业！");
                        setTimeout(function () {
                            location.href = "/teacher/homework/report/detail.vpage?homeworkId=" + kh.homeworkId;
                        }, 200);

                        self.sendLog({
                            op: 'popup_checkup_success_show'
                        });
                    } else {
                        var _info = $17.isBlank(data.info) ? '检查作业失败！' : data.info;
                        $17.msgTip(_info);
                    }

                });
            };
            var unFinishedCount = kh.userCount - kh.finishedCount;
            if (!kh.terminated && unFinishedCount != 0) {
                var myModal = new jBox('Confirm', {
                    title: '系统提示',
                    content: "本次作业尚未到期，可能有学生还未完成作业，您确认要提前检查作业吗？",
                    cancelButton: '取消',
                    confirmButton: '确定',
                    confirm: function () {
                        __check();
                    }
                });
                myModal.open();
                self.sendLog({
                    op: 'popup_whether_check_advance_show'
                });
            } else {
                __check();
            }

            self.sendLog({
                op: 'page_reportlist_checkup_click'
            });
        };

        /*切换学科*/
        self.selectSubjectBtn = function () {
            var that = this;
            self.selectedClazzIds = [];
            self.allClazzIds = [];
            self.reportDetail([]);

            ko.utils.arrayForEach(self.subjectList(), function (subject) {
                subject.checked(false);
            });
            that.checked(true);
            self.subject(that.name());

            self.showSubjectSelectBox(false);
            self.getAllClazz();

            self.sendLog({
                op: "page_reportlist_changeSubject_subject_click"
            });
        };

        self.sendLog({
            op: 'page_reportlist_load'
        });
        self.addOfflineHomework = function(){
            var hk = this;
            self.sendLog({
                module : "m_pGqNIEG2",
                op     : "o_S9jQ0gQu"
            });
            setTimeout(function(){
                var param = {
                    from : "HOMEWORK_LIST",
                    subject : self.subject(),
                    homeworkIds : hk.homeworkId
                };
                location.href = "/teacher/homework/offlinehomework/index.vpage?" + $.param(param);
            },200);
        };
        self.viewOfflineHomework = function(){
            var hk = this;
            self.sendLog({
                module : "m_pGqNIEG2",
                op     : "o_8M2wClph"
            });
            setTimeout(function(){
                var param = {
                    ohids : hk.offlineHomeworkId
                };
                location.href = "/teacher/homework/offlinehomework/detail.vpage?" + $.param(param);
            },200);
        };
    };

    ko.applyBindings(new ReportModel());
});