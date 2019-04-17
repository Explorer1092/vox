/**
 * @author xinqiang.wang
 * @description "亲子活动详情页"
 * @createDate 2016/10/28
 */
define(['jquery', 'knockout', "weui", 'voxLogs'], function ($, ko) {
    var FamilyActivityModule = function () {
        var self = this;
        self.getQuery = function (item) {
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };

        self.from = self.getQuery('_from');

        //reportDesc
        self.reportDescBtn = function (url) {
            setTimeout(function () {
                $.showLoading();
                location.href = url;
            },200);
        };

        self.sendLog = function () {
            var logMap = {
                app: "parent",
                module: 'm_BqyGPVoT',
                s0: self.getQuery('actId')
            };
            $.extend(logMap, arguments[0]);
            YQ.voxLogs(logMap);
        };

        //subTabBtn
        self.subTabBtn = function (element) {
            var that = $(element);
            that.addClass('active').siblings().removeClass('active');
            var name = that.data('name');
            var offset = '';
            if(name == 'activity'){
                offset = $('#activityShowBox').offset().top;
            }else if(name == 'expense'){
                offset = $('#kostenindicatieBox').offset().top;
            }
            $('body,html').animate({
                scrollTop: offset
            });

            self.sendLog({
                op: "o_qvG5D62X",
                s1: that.text()
            });
        };

        //立即报名
        self.applyBtn = function () {
            $.showLoading();
            if (directlyPay) {
                setTimeout(function () {
                    location.href = '/mizar/familyactivity/pay.vpage?actId=' + self.getQuery('actId')+"&dp=true";
                },200);
            } else {
                setTimeout(function () {
                    location.href = '/mizar/familyactivity/apply.vpage?actId=' + self.getQuery('actId');
                },200);
            }
            self.sendLog({
                op: "o_9KQj0TG5"
            });
        };

        //咨询
        self.telBtn = function () {
            self.sendLog({
                op: "o_NRTh23B7"
            });
            return true;
        };

        self.mapBtn = function () {
            $.showLoading();
            location.href = '/mizar/activitymap.vpage?actId='+ self.getQuery('actId');
        };

        self.sendLog({
            op: "o_EjwtjMDh",
            s1: self.from == 'list' ? '列表页' : '列表页banner'
        });
    };

    ko.applyBindings(new FamilyActivityModule());
});