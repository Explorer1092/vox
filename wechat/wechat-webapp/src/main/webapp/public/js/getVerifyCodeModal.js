(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery', '$17'], factory);
    } else if (typeof exports === 'object') {
        // CommonJS
        module.exports = factory(require('jquery'), require('$17'));
    } else {
        // root or window
        root.getVerifyCodeModal = factory(root.jQuery, root.$17);
    }
}(this, function ($, $17) {
    function getVerifyCodeModal(options) {
        var timer, _this = this, sended = false;
        var opTime = options.countSeconds;
        var defaultOptions = {
            phoneNoInputId: "",             //the id of phone number input element
            btnId: "",                      //the id of get verify code button element
            url: "",                        //the url when u want to send
            cid: "",
            btnClass: "disabled",            //the className add to btn when counting
            btnCountingText: "秒后重新获取",   //the text display with the seconds when counting
            btnFinishText: "发送验证码",       //the display text when counting finish
            countSeconds: 120,                //the seconds how long count start
            warnText: "验证码已发送，如未收到请2分钟后再试",
            gaCallBack: function () {
            }
        };

        options = $.extend(defaultOptions, options);

        if (!options) {
            console.error("the options is needed when using getVerifyCodeModal");
            return;
        }
        if (!options.url) {
            console.error("the url option is needed when using getVerifyCodeModal");
            return;
        }
        if (!options.phoneNoInputId) {
            console.error("the phoneNoInputId option is needed when using getVerifyCodeModal");
            return;
        }
        if (!options.btnId) {
            console.error("the btnId option is needed when using getVerifyCodeModal");
            return;
        }
        if (!options.cid) {
            console.error("the cid option is needed when using getVerifyCodeModal");
            return;
        }

        _this.options = options;

        _this.inputSelector = $("#" + _this.options.phoneNoInputId);
        _this.btnSelector = $("#" + _this.options.btnId);

        //start count function
        _this.startCount = function () {
            clearInterval(timer);
            this.btnSelector.addClass(this.options.btnClass);
            timer = setInterval(function () {
                if (_this.options.countSeconds) {
                    _this.btnSelector.text((_this.options.countSeconds > 10 ? _this.options.countSeconds : (' ' + _this.options.countSeconds)) + _this.options.btnCountingText);
                    _this.options.countSeconds--;
                } else {
                    clearInterval(timer);
                    _this.btnSelector.removeClass(_this.options.btnClass).text(_this.options.btnFinishText);
                    sended = false;
                }
            }, 1000);
            return timer;
        };

        //stop count function
        _this.stopCount = function () {
            clearInterval(timer);
            _this.btnSelector.removeClass(_this.options.btnClass).text(_this.options.btnFinishText);
        };


        _this.inputSelector.on('keyup input', function () {
            if(!sended){
                if ($17.isMobile(_this.inputSelector.val())) {
                    _this.btnSelector.removeClass(_this.options.btnClass);
                } else {
                    _this.btnSelector.addClass(_this.options.btnClass);
                }
            }
        });

        _this.btnSelector.on('click', function () {
            var $this = $(this);
            if (!$this.hasClass(_this.options.btnClass)) {
                if (_this.inputSelector.val() == "") {
                    $17.jqmHintBox("请输入手机号码后再获取验证码");
                    return;
                }
                if (!$17.isMobile(_this.inputSelector.val())) {
                    $17.jqmHintBox("请输入正确格式的手机号码");
                    return;
                }
                _this.sendVerifyCode();
            }
            _this.options.gaCallBack();
        });

        //send mobile verify code function
        _this.sendVerifyCode = function () {
            _this.stopCount();
            $.post(_this.options.url, {mobile: _this.inputSelector.val(), cid: _this.options.cid}, function (data) {
                console.log(data);
                if (data.success) {
                    $17.jqmHintBox('发送成功');
                    sended = true;
                    _this.startCount();
                    _this.options.countSeconds = opTime;
                } else {
                    if (data.info.indexOf("验证码已发送") != -1) {
                        _this.stopCount();
                        _this.startCount();
                    } else {
                        $17.jqmHintBox(data.info);
                        _this.stopCount();
                    }
                }
            });
        };
    }
    return getVerifyCodeModal;
}));
