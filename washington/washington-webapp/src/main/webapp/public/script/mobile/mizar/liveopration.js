/**
 * @author huihui.li
 * @description "教研直播运营页"
 * @createDate 2017.4.24
 */
define(['jquery', 'knockout', 'YQ', "weui", 'voxLogs'], function ($, ko, yq) {

    function LiveOpration() {
        var _this = this;
        var trackStr = "liveopration";
        _this.statusSuccess = ko.observable();
        _this.timeFlag1 = ko.observable();
        _this.timeFlag2 = ko.observable();
        _this.timeFlag3 = ko.observable();
        _this.payAll1 = ko.observable();
        _this.payAll2 = ko.observable();
        _this.payAll3 = ko.observable();
        _this.status1 = ko.observable();
        _this.status2 = ko.observable();
        _this.status3 = ko.observable();
        _this.waiting1 = ko.observable();
        _this.waiting2 = ko.observable();
        _this.waiting3 = ko.observable();
        _this.offline1 = ko.observable();
        _this.offline2 = ko.observable();
        _this.offline3 = ko.observable();
        var data = {
            period1 : "58feb1bf12a0cb646e85f8a7",
            period2 : "58feb20512a0cb646e860179",
            period3 : "58feb24f12a0cb646e860ab1"
        };
        $.get('/mizar/course/courseperiods.vpage',data, function (res) {
            if (res.success){
                _this.statusSuccess(true);
                if (res.offline1){
                    _this.timeFlag1("BEFORE");
                    _this.status1(0);
                }else{
                    _this.timeFlag1(res.timeFlag1);
                    _this.payAll1(res.payAll1);
                    _this.status1(res.status1);
                    _this.waiting1(res.waiting1);
                    _this.offline1(res.offline1);
                }
                if (res.offline2){
                    _this.timeFlag2("BEFORE");
                    _this.status2(0);
                }else{
                    _this.timeFlag2(res.timeFlag2);
                    _this.payAll2(res.payAll2);
                    _this.status2(res.status2);
                    _this.waiting2(res.waiting2);
                    _this.offline2(res.offline2);
                }
                if (res.offline3){
                    _this.timeFlag3("BEFORE");
                    _this.status3(0);
                }else{
                    _this.timeFlag3(res.timeFlag3);
                    _this.payAll3(res.payAll3);
                    _this.status3(res.status3);
                    _this.waiting3(res.waiting3);
                    _this.offline3(res.offline3);
                }
            }else{
                _this.statusSuccess(false);
                _this.timeFlag1("BEFORE");
                _this.timeFlag2("BEFORE");
                _this.timeFlag3("BEFORE");
                _this.status1(0);
                _this.status2(0);
                _this.status3(0);
            }
        });
        // 点击立即预约 定位到课程安排
        _this.gotoLiveLink = function () {
            var height = $(".liveScroll").offset().top;
            $(window).scrollTop(height);
        };
        // 不同状态的按钮点击选项
        _this.liveCourse = function (periodId,timeFlag,payAll,status,waiting,offline) {
            if (offline){
                $.alert("访问人数过多，预约功能稍后开放");
                return false;
            }
            if (!_this.statusSuccess()){
                $.alert("请登录一起作业老师端再预约哦~");
                return false;
            }
            if (timeFlag == "BEFORE" && status == 0 ){
                var data = {
                    id: periodId,
                    payAll: payAll,
                    track: trackStr
                };
                $.post('/mizar/microcourse/reserve.vpage', data, function (res) {
                    if (res.success) {
                        $.alert('预约成功', function () {
                            location.reload();
                        });
                    } else {
                        $.alert(res.info);
                    }
                })
            }
            if (timeFlag == "BEFORE" && status == 2 ){
                return false;
            }
            if (timeFlag == "ING" || (timeFlag == "AFTER" && !waiting) ){
                var h5Link = "http://" + location.host + "/mizar/microcourse/newgate.vpage?track=" + trackStr + "&period=" + periodId + "&_protocol=1";
                location.href = h5Link;
            }
            if (timeFlag == "AFTER" && waiting){
                return false;
            }
        };
        // 浏览器或移动设备
        function browserRedirect() {
            var sUserAgent = navigator.userAgent.toLowerCase();
            var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
            var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
            var bIsMidp = sUserAgent.match(/midp/i) == "midp";
            var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
            var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
            var bIsAndroid = sUserAgent.match(/android/i) == "android";
            var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
            var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";

            if (bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid || bIsCE || bIsWM) {
                $(".liveWrap").removeClass("livePc").show();
            } else {
                $(".liveWrap").addClass("livePc").show();
            }
        }
        browserRedirect();
        $(window).resize(function() {
            browserRedirect();
        });
    }

    ko.applyBindings(new LiveOpration());

    window.vox = window.vox || {};
    vox.task = {
        refreshData: function () {
            location.reload();
        }
    };
});
