/**
 * @author xinqiang.wang
 * @description "自学礼物"
 * @createDate 2016/12/6
 */

define(['jquery', 'knockout', 'weui', 'voxLogs'], function ($, ko) {
    var LotteryModule = function () {
        var self = this;

        self.freeChance = ko.observable(0); //今日领取次数
        self.totalFreeChance = ko.observable(0); //总计次数

        self.circleNum = ko.observable(5);

        self.lotteryIndex = ko.observable(0); //奖品初始化位置

        self.award = ko.observableArray([]);
        self.awardId = ko.observable(0); // 已抽奖品Id
        self.awardName = ko.observable(''); // 已抽奖品Id

        self.awardsContent = ko.observableArray([]);
        self.drawing = ko.observable(false);

        self.bingoContent = ko.observableArray([]); //我的学习礼物

        self.tipContent = ko.observable('');

        self.tipAlert = function (content) {
            self.tipContent(content);
            self.koTemplateName('tip_tem');
        };

        self.latestContent = ko.observableArray([]);//同学领取动态列表

        self.getQuery = function (item) {
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };

        self.from = self.getQuery("from");

        self.sendLog = function () {
            var logMap = {
                app: "student",
                module: 'm_1buEBmzn'
            };
            $.extend(logMap, arguments[0]);
            YQ.voxLogs(logMap);
        };

        self.activityId = self.getQuery('activityId');

        /*自定义下拉模板*/
        self.koTemplateName = ko.observable(''); // ko template
        self.koTemplateClose = function () {
            self.koTemplateName('');
            self.getFetchActivityData();
        };

        self.getAppVersion = function () {
            var native_version = "2.5.0";
            if (self.getExternal()["getInitParams"]) {
                var $params = self.getExternal().getInitParams();

                if ($params) {
                    $params = $.parseJSON($params);
                    native_version = $params.native_version;
                }
            }
            return native_version;
        };


        self.getExternal = function () {
            var _WIN = window;
            if (_WIN['yqexternal']) {
                return _WIN.yqexternal;
            } else if (_WIN['external']) {
                return _WIN.external;
            } else {
                return _WIN.external = function () {
                };
            }
        };

        self.OpenApp = function (_this) {
            //console.info(JSON.stringify(_this, '', 4));
            //fix 兼容ios和Android 实现pageQueueNew和openFairylandPage的差异。#38664
            var en = '';
            if (self.from && self.from == 'fairyland') {
                en = 'pageQueueNew';
            } else {
                en = 'openFairylandPage';
            }
            if (self.getExternal()[en]) {
                setTimeout(function () {
                    self.getExternal()[en](JSON.stringify({
                        url: window.location.origin + _this.launchUrl,
                        name: "fairyland_app:" + (_this.appKey || "link"),
                        useNewCore: _this.browser || "system",
                        orientation: _this.orientation || "sensor",
                        initParams: JSON.stringify({hwPrimaryVersion: _this.hwPrimaryVersion || "V2_4_0"}),
                        page_viewable: true
                    }));
                }, 200);
                //关闭提示框
                self.koTemplateClose();
            } else {
                YQ.voxLogs({
                    module: 'fairyland_app',
                    op: 'error',
                    s0: 'wonderland-activity-lottery-null'
                });
            }


        };

        //获取奖品列表
        self.getFetchActivityData = function () {
            $.get('/wonderland/activity/fetchactivitydata.vpage', {
                activityId: self.activityId,
                version: self.getAppVersion()
            }, function (data) {
                if (data.success) {
                    self.freeChance(data.freeChance);
                    self.totalFreeChance(data.totalFreeChance);
                    self.awardsContent(data.awards || []);

                    self.bingoContent(data.bingo || []);

                    self.latestContent(data.latest || []);
                }
            });
        };
        self.getFetchActivityData();

        //领取
        self.lotteryBtn = function () {
            if (self.drawing()) {
                return false;
            }
            self.drawing(true);
            $.showLoading();
            $.get("/wonderland/activity/drawlottery.vpage", {
                activityId: self.activityId,
                version: self.getAppVersion()
            }, function (data) {
                $.hideLoading();
                if (data.success) {
                    self.awardId(data.award.id);
                    self.awardName(data.award.name);
                    self.award(data);

                    self.drawAnimation();
                    self.freeChance(self.freeChance() - 1);
                } else {
                    self.tipAlert(data.info);
                    self.drawing(false);
                }


            }).fail(function () {
                self.tipAlert("数据请求失败");
                self.drawing(false);
                $.hideLoading();
            });

            self.sendLog({
                op: 'o_3Powyp7v'
            });
        };

        //动画
        self.drawAnimation = function () {
            var list = $('.lotteryBox .lotteryList'), len = list.length, interval = null, _count = 0, _easing_time = 0;
            var circle = 0;

            function _scroll() {
                //缓动效果
                ++_count;
                _easing_time = Math.floor((_count - 0.5 * len) * (_count - 0.5 * len) / 5) + 60;

                interval = setTimeout(_scroll, _easing_time);
                if (self.lotteryIndex() == len) {
                    self.lotteryIndex(0);
                    circle += 1;
                }
                var id = list.eq(self.lotteryIndex()).data('id');
                if (circle >= self.circleNum() && id == self.awardId()) {

                    list.removeClass('active');
                    list.eq(self.lotteryIndex()).addClass('active');
                    clearInterval(interval);
                    //alert('你抽中了' + self.awardName());
                    self.drawing(false);
                    //谢谢参与不弹窗
                    var category = self.award().award.category;
                    if (category != 'Default') {
                        setTimeout(function () {
                            self.koTemplateName('lotterySuccess_tem');
                            self.getFetchActivityData();
                        }, 800);
                        self.sendLog({
                            op: 'o_pPdaEcue',
                            s0: self.awardName(),
                            s1: self.awardId()
                        });
                    }

                } else {
                    list.removeClass('active');
                    list.eq(self.lotteryIndex()).addClass('active');
                    self.lotteryIndex(self.lotteryIndex() + 1);
                }
            }

            interval = setTimeout(_scroll, 0.5 * len * 0.5 * len + 60);
        };

        //列表中立即使用
        self.useBtn = function () {
            var that = this;
            self.OpenApp(that);
            self.sendLog({
                op: 'o_ornYVRv9',
                s0: that.name || '',
                s1: that.id || ''
            });
        };

        //弹窗中立即使用
        self.usePopupBtn = function () {
            var that = this;
            self.OpenApp(that);

            self.sendLog({
                op: 'o_J2Iqm96M',
                s0: self.awardName(),
                s1: self.awardId()
            });
        };

        //礼盒点击
        self.giftBtn = function (element) {
            var timer = 1200;

            if (self.drawing()) {
                return false;
            }
            self.drawing(true);
            $.showLoading();
            $.get("/wonderland/activity/drawlottery.vpage", {
                activityId: self.activityId,
                version: self.getAppVersion()
            }, function (data) {
                $(element).addClass('shake');
                $.hideLoading();
                if (data.success) {
                    self.awardId(data.award.id);
                    self.awardName(data.award.name);
                    self.award(data);

                    self.freeChance(self.freeChance() - 1);
                    setTimeout(function () {
                        //"谢谢参与" 弹默认弹窗
                        var category = self.award().award.category;
                        if (category != 'Default') {
                            self.koTemplateName('lotterySuccess_tem');
                        } else {
                            self.tipAlert(data.info);
                        }
                        $(element).removeClass('shake');
                        self.drawing(false);
                    }, timer);
                } else {
                    setTimeout(function () {
                        self.tipAlert(data.info);
                        $(element).removeClass('shake');
                        self.drawing(false);
                    }, timer);
                }

            }).fail(function () {
                self.tipAlert("数据请求失败");
                $(element).removeClass('shake');
                self.drawing(false);
                $.hideLoading();
            });

            self.sendLog({
                op: 'o_3Powyp7v'
            });

        };

        //设置title
        if (self.getExternal()["updateTitle"]) {
            self.getExternal().updateTitle("自学礼物", "494a4a", "ffffff");
        }

        self.sendLog({
            op: "o_9rvNxYSQ"
        });
    };
    ko.applyBindings(LotteryModule);
});