/**
 * @author xinqiang.wang
 * @description ""
 * @createDate 2016/8/26
 */

define(["$17", "knockout", "komapping", 'logger', "wx", "weuijs"], function ($17, ko, komapping, logger, wx) {
    var shareTitle = '有点意外，没想到收到这样的教师节礼物！';
    var shareDescription = '教师节年年有，但这次，不一样。有惊喜，更有感动……';
    var shareLink = location.href + "&share=false";
    var shareIcon = "http://cdn-cnc.17zuoye.cn/resources/app/17teacher/res/icon.png";

    /*处理图片加载失败*/
    ko.bindingHandlers.img = {
        update: function (element, valueAccessor) {
            var value = ko.utils.unwrapObservable(valueAccessor()),
                src = ko.utils.unwrapObservable(value.src),
                fallback = ko.utils.unwrapObservable(value.fallback),
                $element = $(element);
            if (src) {
                $element.attr("src", src);
            } else {
                $element.attr("src", fallback);
            }
        },
        init: function (element, valueAccessor) {
            var $element = $(element);
            $element.error(function () {
                var value = ko.utils.unwrapObservable(valueAccessor()),
                    fallback = ko.utils.unwrapObservable(value.fallback);

                $element.attr("src", fallback);
            });
        }
    };

    var TeachersDayShare = function () {
        var self = this;

        self.shareListDetail = ko.observableArray([]);
        self.showShareBtn = ko.observable(true);
        self.istapp = ko.observable(false);
        self.showShareTipBox = ko.observable(false);
        self.defaultImg = ko.observable('/public/images/teacher/activity/teachersDay2016/student.jpg');
        if (!$17.isBlank($17.getQuery('share'))) {
            self.showShareBtn(false);
        }
        if (!$17.isBlank($17.getQuery('istapp'))) {
            self.istapp(true);
        }
        self.from = $17.getQuery('_from');
        self.shareListDetail(ko.mapping.fromJS(teachersDayShareMap.shareList)());

        /*分享*/
        self.shareBtn = function () {
            if (isFromTeacherApp) {
                var shareInfo = {
                    title: shareTitle,
                    content: shareDescription,
                    url: shareLink + "&istapp=true"
                };
                if (window.external && ('shareInfo' in window.external)) {
                    window.external.shareInfo(JSON.stringify(shareInfo));
                }
                self.sendLogToTeacherApp({
                    module: 'm_YGUa6u9c',
                    op: 'o_9xwEzude'
                });
            } else {
                self.showShareTipBox(true);
                logger.log({
                    app: 'teacher',
                    module: 'm_B8i52sJm',
                    op: 'o_f1MQp45Y'
                });
            }
        };

        /*图片transform*/
        self.transformImg = function (index) {
            var myArray = [-3, -2, -1, 1, 2, 3];
            var random = myArray[Math.floor(Math.random() * myArray.length)];
            $('#transformImg' + index).css({
                '-webkit-transform': 'rotate(' + random + 'deg)',
                '-moz-transform': 'rotate(' + random + 'deg)'
            });
        };

        /*图片预览*/
        self.viewBigImg = function () {
            var that = this;
            $.alert({
                title: '预览',
                text: '<div style="width: 100%; max-height: 350px; overflow-y: auto; display: inline-block;"><img style="display: inline-block; width: 100%" src="' + (that.imgUrl() + "@640w_1o") + '" alt=""></div>'
            });
            $('.weui_dialog, .weui_toast').css({'top': '1%'});
        };

        self.playVideoBtn = function () {
            var videoBox = document.getElementById("videoBox");
            if (videoBox.paused) {
                videoBox.play();
                if (self.showShareBtn()) {
                    if (isFromTeacherApp) {
                        self.sendLogToTeacherApp({
                            module: 'm_YGUa6u9c',
                            op: 'o_TKxnrYdO'
                        });
                    } else {
                        logger.log({
                            app: 'teacher',
                            module: 'm_B8i52sJm',
                            op: 'o_uUx8GbhR'
                        });
                    }
                } else {
                    logger.log({
                        app: 'teacher',
                        module: 'm_B8i52sJm',
                        op: 'o_kxZeA61Z',
                        s0: self.istapp() || isFromTeacherApp ? 'teacherApp' : 'wechat'
                    });
                }
            } else {
                videoBox.pause();
            }
        };

        self.goto17Btn = function () {
            setTimeout(function () {
                location.href = '//www.17zuoye.com';
            }, 200);

            logger.log({
                app: 'teacher',
                module: 'm_B8i52sJm',
                op: 'o_22h9Wgpv',
                s1: self.istapp() ? 'teacherApp' : 'wechat'
            });
        };

        self.sendWechatLog = function (s0) {
            logger.log({
                app: 'teacher',
                module: 'm_B8i52sJm',
                op: 'o_h4OBTyYR',
                s0: s0
            });
        };

        self.sendLogToTeacherApp = function (logMap) {
            if (window.external && ('log_b' in window.external)) {
                window.external.log_b('', JSON.stringify(logMap));
            }
        };

        wx.config({
            debug: false,
            appId: teachersDayShareMap.wxJsApiMap.appId || '',
            timestamp: teachersDayShareMap.wxJsApiMap.timestamp || '',
            nonceStr: teachersDayShareMap.wxJsApiMap.noncestr || '',
            signature: teachersDayShareMap.wxJsApiMap.signature || '',
            jsApiList: ['onMenuShareTimeline', 'onMenuShareAppMessage', 'onMenuShareQQ', "onMenuShareQZone"]
        });

        wx.ready(function () {
            //分享到朋友圈
            wx.onMenuShareTimeline({
                title: shareTitle, // 分享标题
                desc: shareDescription,
                link: shareLink + "&_from=wechat_circle", // 分享链接
                imgUrl: shareIcon, // 分享图标
                success: function () {
                    // 用户确认分享后执行的回调函数
                    self.sendWechatLog('wechat_circle');
                },
                cancel: function () {
                    // 用户取消分享后执行的回调函数
                }
            });

            //分享给朋友
            wx.onMenuShareAppMessage({
                title: shareTitle, // 分享标题
                desc: shareDescription, // 分享描述
                link: shareLink + "&_from=wechat", // 分享链接
                imgUrl: shareIcon, // 分享图标
                type: '', // 分享类型,music、video或link，不填默认为link
                dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
                success: function () {
                    self.sendWechatLog('wechat');
                },
                cancel: function () {
                    // 用户取消分享后执行的回调函数
                }
            });

            //分享到QQ
            wx.onMenuShareQQ({

                title: shareTitle, // 分享标题
                desc: shareDescription, // 分享描述
                link: shareLink + "&_from=qq", // 分享链接
                imgUrl: shareIcon, // 分享图标
                success: function () {
                    // 用户确认分享后执行的回调函数
                    self.sendWechatLog('qq');
                },
                cancel: function () {
                    // 用户取消分享后执行的回调函数
                }
            });

            wx.onMenuShareQZone({
                title: shareTitle, // 分享标题
                desc: shareDescription, // 分享描述
                link: shareLink + "&_from=qq_zone", // 分享链接
                imgUrl: shareIcon, // 分享图标
                success: function () {
                    self.sendWechatLog('qq_zone');
                },
                cancel: function () {

                }
            });
        });


        if (self.showShareBtn()) {
            logger.log({
                app: 'teacher',
                module: 'm_B8i52sJm',
                op: 'o_D6IuiMPn',
                s0: self.from == 'qr_code' ? 'qr_code' : 'picwall_button',
                s1: self.istapp() || isFromTeacherApp ? 'teacherApp' : 'wechat'
            });
        } else {
            logger.log({
                app: 'teacher',
                module: 'm_B8i52sJm',
                op: 'o_2lVgqSAA',
                s0: self.from,
                s1: self.istapp() || isFromTeacherApp ? 'teacherApp' : 'wechat'
            });
        }

    };
    ko.applyBindings(new TeachersDayShare());
});