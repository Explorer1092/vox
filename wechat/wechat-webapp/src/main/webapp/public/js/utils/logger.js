/*fix jquery Global */
var __do = 'jquery',__do$ = '$';
if (!!LoggerProxy && LoggerProxy.currentUserType == 1) {
    __do = __do$ = '';
}
define([__do], function (__do$) {

    /*日志白名单 */
    var Logger, errWhiteList, isInWhiteList;
    errWhiteList = ['WeixinJSBridge is not defined'];

    /*判断当前错误是否在白名单中 */
    isInWhiteList = function (errMsg) {
        var err, i, len, result;
        result = false;
        for (i = 0, len = errWhiteList.length; i < len; i++) {
            err = errWhiteList[i];
            if (errMsg.indexOf(err) !== -1) {
                result = true;
            }
        }
        return result;
    };
    Logger = (function () {
        function Logger() {
            this.s0 = !!LoggerProxy && LoggerProxy.openId;
            this.userId = this.getCookie('uid');
            ((function (_this) {
                return function () {
                    window.onerror = function (errMsg, file, line) {
                        var url, userId, useragent, logName;
                        if (!isInWhiteList(errMsg)) {
                            userId = _this.getCookie('ssid');
                            useragent = navigator && navigator.userAgent ? navigator.userAgent : "No browser information";
                            encodeURI(useragent);
                            if (!!LoggerProxy && LoggerProxy.currentUserType == 1) {
                                logName = 'wechat_teacher_js_errors_logs';
                                userId = _this.getCookie('uid')
                            } else {
                                logName = 'wechat_parent_js_errors_logs';
                            }
                            url = window.location.protocol + '//log.17zuoye.cn/log?_c=vox_logs:' + logName + '&_l=3&_log={"userId":"' + userId + '","errMsg":"' + errMsg + '","file":"' + file + '","line":"' + line + '","useragent":"' + useragent + '"}';
                            $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
                        }
                    };
                };
            })(this))();
        }

        Logger.prototype.log = function (msg) {
            var def;
            var app = (msg.app && msg.app == 'teacher') ? 'teacher' : 'parent';
            def = {
                sys: 'wechat',
                type: 'log',
                app: app,
                code: 1,
                s0: this.s0,
                userId: this.userId,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            };
            $.extend(def, msg);
            return this.voxLogger(def);
        };

        Logger.prototype.voxLogger = function (msg) {
            if ($.type(msg) === 'string' && msg[0] === '{') {
                msg = $.parseJSON(msg);
            }
            (function () {
                var url;
                url = window.location.protocol + '//log.17zuoye.cn/log?_c=vox_logs:wechat_logs&_l=info&' + $.param(msg);
                return $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
            })();
        };

        Logger.prototype.getCookie = function (name) {
            var cookie, matched, pattern;
            pattern = RegExp(name + "=.[^;]*");
            matched = document.cookie.match(pattern);
            if (matched) {
                cookie = matched[0].split('=');
                return cookie[1];
            }
            return '';
        };

        return Logger;

    })();
    return new Logger;
});
