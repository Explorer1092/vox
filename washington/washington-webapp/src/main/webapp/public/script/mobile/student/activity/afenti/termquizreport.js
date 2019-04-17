/**
 * @author xinqiang.wang
 * @description "期末测评报告"
 * @createDate 2016/12/21
 */

define(['jquery', 'knockout', 'radialIndicator', 'weui', 'voxLogs'], function ($, ko) {
    var TermQuizReportModule = function () {
        var self = this;

        self.content = ko.observableArray([]);
        self.score = ko.observable(0);//分数
        self.integral = ko.observable(0);
        self.errorPointsList = ko.observableArray([]);

        self.getQuery = function (item) {
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };

        self.subject = ko.observable(self.getQuery('subject'));

        self.sendLog = function () {
            var logMap = {
                app: "student",
                module: 'm_1yzw709n'
            };
            $.extend(logMap, arguments[0]);
            YQ.voxLogs(logMap);
        };

        self.setScoreTip = function () {
            var score = self.score();
            var scoreMap = {};
            if (score >= 90 && score <= 100) {
                scoreMap.status = '优秀';
                scoreMap.title = '继续加油哦';
            } else if (score >= 70 && score < 90) {
                scoreMap.status = '良好';
                scoreMap.title = '继续加油哦';
            } else if (score >= 60 && score < 70) {
                scoreMap.status = '及格';
                scoreMap.title = '继续加油哦';
            } else {
                scoreMap.status = '不及格';
                scoreMap.title = '要加油哦';
            }
            return scoreMap;
        };

        self.getAppKeyBySubject = function () {
            switch (self.subject()){
                case "ENGLISH":
                    return "AfentiExam";
                    break;
                case "MATH":
                    return "AfentiMath";
                    break;
                case "CHINESE":
                    return "AfentiChinese";
                    break;
            }
        };

        self.fetchTermQuizReport = function () {
            $.get('/afenti/api/quiz/fetchtermquizreport.vpage',{subject: self.subject()}, function (data) {
                if (data.success) {
                    self.score(data.score);
                    self.integral(data.integral);
                    self.errorPointsList(data.knowledges);

                    self.setRadialIndicator(data.score,'green');
                } else {
                    /*$.alert(data.info,function () {
                     location.reload();
                     });*/
                    YQ.voxLogs({
                        module: 'afenti_quiz_report',
                        op: 'fetchtermquizreport',
                        s0: data.code
                    });
                }
            });
        };

        self.fetchTermQuizReport();

        self.setRadialIndicator = function (value, color) {
            var barBgColor = '#bfbfbf', barColor = '#29b3fb';
            if (color == 'red') {
                barBgColor = '#ffe2df';
                barColor = '#f3754c';
            }
            $('#radial').radialIndicator({
                barBgColor: barBgColor,
                barColor: barColor,
                barWidth: 15,
                fontWeight:100,
                initValue: value,
                displayNumber: true,
                percentage: true
            });
        };

        //打开阿分提
        self.openAppBtn = function () {
            var url = window.location.origin + '/app/redirect/selfApp.vpage?appKey='+self.getAppKeyBySubject()+'&platform=STUDENT_APP&productType=APPS';
            if (window['external'] && window.external['openFairylandPage']) {
                window.external.openFairylandPage(JSON.stringify({
                    url: url,
                    name: "fairyland_app:" + (self.getAppKeyBySubject() || "link"),
                    useNewCore: "crossWalk",
                    orientation: "portrait",
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }));
            } else {
                location.href = url;
            }
            self.sendLog({
                op: 'o_y6bxeRf3'
            })
        };

        self.sendLog({
            op: 'o_nAemvZyt'
        })

    };

    ko.applyBindings(TermQuizReportModule);
});