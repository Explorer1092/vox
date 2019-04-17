/**
 * @author xinqiang.wang
 * @description "期末测评活动"
 * @createDate 2016/12/21
 */

define(['jquery', 'knockout', 'weui', 'voxLogs'], function ($, ko) {
    var TermQuizModule = function () {
        var self = this;

        self.showPopup = ko.observable(false);

        self.content = ko.observable();

        self.selectedSubjectMap = ko.observable({});

        self.questionIds = ko.observableArray([]);

        self.subject = ko.observable();
        self.unitId = ko.observable();
        self.bookId = ko.observable();

        self.sendLog = function () {
            var logMap = {
                app: "student",
                module: 'm_1yzw709n'
            };
            $.extend(logMap, arguments[0]);
            YQ.voxLogs(logMap);
        };

        self.getContent = function () {
            $.get('/afenti/api/quiz/fetchtermquizinfo.vpage', function (data) {
                if (data.success) {
                    data.english.name = "英语";
                    data.math.name = "数学";
                    data.chinese.name = "语文";
                    self.content(data);
                } else {
                    $.alert(data.info);
                }
            });
        };

        /*初始化*/
        self.getContent();

        //打开弹窗
        self.contentBtn = function (type) {
            $.showLoading();
            self.questionIds([]);
            self.subject(type.toUpperCase());
            $.get('/afenti/api/quiz/fetchtermquizquestion.vpage', {subject: type.toUpperCase()}, function (data) {
                $.hideLoading();
                if (data.success) {
                    for (var i = 0; i < data.questions.length; i++) {
                        if (data.questions[i].rightNum <= 0 && data.questions[i].errorNum <= 0) {
                            self.questionIds.push(data.questions[i].examId);
                            self.unitId(data.questions[i].unitId);
                            self.bookId(data.questions[i].bookId);
                        }
                    }
                    self.showPopup(true);
                    self.selectedSubjectMap(self.content()[type]);

                    self.sendLog({
                        op: 'o_6AjbkJOE',
                        s0: type
                    });

                } else {
                    $.alert(data.info);
                }

            }).fail(function () {
                $.hideLoading();
            });
        };

        //打开阿分提
        self.openAppBtn = function (type) {
            var url = window.location.origin + '/app/redirect/selfApp.vpage?appKey='+type+'&platform=STUDENT_APP&productType=APPS';
            if (window['external'] && window.external['openFairylandPage']) {
                window.external.openFairylandPage(JSON.stringify({
                    url: url,
                    name: "fairyland_app:" + (type || "link"),
                    useNewCore: "crossWalk",
                    orientation: "portrait",
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }));
            } else {
                location.href = url;
            }
            self.sendLog({
                op: 'o_y6bxeRf3',
                s0: type
            })
        };

        //关闭弹窗
        self.closePopupBtn = function () {
            self.showPopup(false);
            self.getContent();
        };

        //开始测评
        self.gotoWorkBtn = function () {
            self.closePopupBtn();

            if(self.questionIds().length == 0){
                $.alert('没有可测评的内容~',function () {
                    location.href = '/afenti/api/activity/termquizreport.vpage?subject='+self.subject();
                });
                self.sendLog({
                    op: 'o_NWkoBxJR',
                    s0: '开始测评',
                    s1: '没有可测评的内容'
                });
                return false;
            }
            var param = {
                render_type: "afenti_final_evaluation",
                qids: self.questionIds().join(','),
                exam_result_url: "/afenti/api/quiz/processtermquizresult.vpage",
                bookId: self.bookId(),
                subject: self.subject(),
                unitId: self.unitId()
            };
            var url = window.location.origin + '/resources/apps/hwh5/homework/V2_5_0/exam-v2/index.html?' + $.param(param);
            if (window['external'] && window.external['openSecondWebview']) {
                window.external.openSecondWebview(JSON.stringify({
                    navBarIsShow: true,
                    url: url
                }));

            } else if (window['external'] && window.external['pageQueueNew']) {
                window.external.pageQueueNew(JSON.stringify({
                    url: url
                }));
            } else {
                location.href = url;
            }

            self.sendLog({
                op: 'o_NWkoBxJR',
                s0: '开始测评'
            });

        };

        //去报告
        self.gotoReportBtn = function () {
            setTimeout(function () {
                location.href = '/afenti/api/activity/termquizreport.vpage?subject='+self.subject();
            }, 200);
            self.sendLog({
                op: 'o_NWkoBxJR',
                s0: '查看测评报告'
            });
        };

        self.sendLog({
            op: 'o_FWzEyS6Q'
        });
    };

    ko.applyBindings(new TermQuizModule());
});