(function ($) {
    "use strict";

    $.fn.transitionEnd = function (callback) {
        var events = ['webkitTransitionEnd', 'transitionend', 'oTransitionEnd', 'MSTransitionEnd', 'msTransitionEnd'],
            i, dom = this;

        function fireCallBack(e) {
            /*jshint validthis:true */
            if (e.target !== this) return;
            callback.call(this, e);
            for (i = 0; i < events.length; i++) {
                dom.off(events[i], fireCallBack);
            }
        }

        if (callback) {
            for (i = 0; i < events.length; i++) {
                dom.on(events[i], fireCallBack);
            }
        }
        return this;
    };

    $.support = (function () {
        return {
            touch: !!(('ontouchstart' in window) || window.DocumentTouch && document instanceof window.DocumentTouch)
        };
    })();

    $.touchEvents = {
        start: $.support.touch ? 'touchstart' : 'mousedown',
        move: $.support.touch ? 'touchmove' : 'mousemove',
        end: $.support.touch ? 'touchend' : 'mouseup'
    };

    $.getTouchPosition = function (e) {
        e = e.originalEvent || e; //jquery wrap the originevent
        if (e.type === 'touchstart' || e.type === 'touchmove' || e.type === 'touchend') {
            return {
                x: e.targetTouches[0].pageX,
                y: e.targetTouches[0].pageY
            };
        } else {
            return {
                x: e.pageX,
                y: e.pageY
            };
        }
    };

    $.fn.scrollHeight = function () {
        return this[0].scrollHeight;
    };

    $.fn.transform = function (transform) {
        for (var i = 0; i < this.length; i++) {
            var elStyle = this[i].style;
            elStyle.webkitTransform = elStyle.MsTransform = elStyle.msTransform = elStyle.MozTransform = elStyle.OTransform = elStyle.transform = transform;
        }
        return this;
    };
    $.fn.transition = function (duration) {
        if (typeof duration !== 'string') {
            duration = duration + 'ms';
        }
        for (var i = 0; i < this.length; i++) {
            var elStyle = this[i].style;
            elStyle.webkitTransitionDuration = elStyle.MsTransitionDuration = elStyle.msTransitionDuration = elStyle.MozTransitionDuration = elStyle.OTransitionDuration = elStyle.transitionDuration = duration;
        }
        return this;
    };

    $.getTranslate = function (el, axis) {
        var matrix, curTransform, curStyle, transformMatrix;

        // automatic axis detection
        if (typeof axis === 'undefined') {
            axis = 'x';
        }

        curStyle = window.getComputedStyle(el, null);
        if (window.WebKitCSSMatrix) {
            // Some old versions of Webkit choke when 'none' is passed; pass
            // empty string instead in this case
            transformMatrix = new WebKitCSSMatrix(curStyle.webkitTransform === 'none' ? '' : curStyle.webkitTransform);
        }
        else {
            transformMatrix = curStyle.MozTransform || curStyle.OTransform || curStyle.MsTransform || curStyle.msTransform || curStyle.transform || curStyle.getPropertyValue('transform').replace('translate(', 'matrix(1, 0, 0, 1,');
            matrix = transformMatrix.toString().split(',');
        }

        if (axis === 'x') {
            //Latest Chrome and webkits Fix
            if (window.WebKitCSSMatrix)
                curTransform = transformMatrix.m41;
            //Crazy IE10 Matrix
            else if (matrix.length === 16)
                curTransform = parseFloat(matrix[12]);
            //Normal Browsers
            else
                curTransform = parseFloat(matrix[4]);
        }
        if (axis === 'y') {
            //Latest Chrome and webkits Fix
            if (window.WebKitCSSMatrix)
                curTransform = transformMatrix.m42;
            //Crazy IE10 Matrix
            else if (matrix.length === 16)
                curTransform = parseFloat(matrix[13]);
            //Normal Browsers
            else
                curTransform = parseFloat(matrix[5]);
        }

        return curTransform || 0;
    };
    $.requestAnimationFrame = function (callback) {
        if (window.requestAnimationFrame) return window.requestAnimationFrame(callback);
        else if (window.webkitRequestAnimationFrame) return window.webkitRequestAnimationFrame(callback);
        else if (window.mozRequestAnimationFrame) return window.mozRequestAnimationFrame(callback);
        else {
            return window.setTimeout(callback, 1000 / 60);
        }
    };

    $.cancelAnimationFrame = function (id) {
        if (window.cancelAnimationFrame) return window.cancelAnimationFrame(id);
        else if (window.webkitCancelAnimationFrame) return window.webkitCancelAnimationFrame(id);
        else if (window.mozCancelAnimationFrame) return window.mozCancelAnimationFrame(id);
        else {
            return window.clearTimeout(id);
        }
    };

    $.fn.join = function (arg) {
        return this.toArray().join(arg);
    }
})($);

+function ($) {
    "use strict";

    var defaults;

    $.modal = function (params, onOpen) {
        params = $.extend({}, defaults, params);


        var buttons = params.buttons;

        var buttonsHtml = buttons.map(function (d, i) {
            return '<a href="javascript:;" class="weui_btn_dialog ' + (d.className || "") + '">' + d.text + '</a>';
        }).join("");

        var tpl = '<div class="weui_dialog" id='+params.id+'>' +
            '<div class="weui_dialog_hd"><strong class="weui_dialog_title">' + params.title + '</strong></div>' +
            ( params.text ? '<div class="weui_dialog_bd">' + params.text + '</div>' : '') +
            '<div class="weui_dialog_ft">' + buttonsHtml + '</div>' +
            '</div>';

        var dialog = $.openModal(tpl, onOpen);

        dialog.find(".weui_btn_dialog").each(function (i, e) {
            var el = $(e);
            el.click(function () {
                //先关闭对话框，再调用回调函数
                if (params.autoClose) $.closeModal();

                if (buttons[i].onClick) {
                    buttons[i].onClick.call(dialog);
                }
            });
        });

        return dialog;
    };

    $.openModal = function (tpl, onOpen) {
        var mask = $("<div class='weui_mask'></div>").appendTo(document.body);
        mask.show();

        var dialog = $(tpl).appendTo(document.body);

        if (onOpen) {
            dialog.transitionEnd(function () {
                onOpen.call(dialog);
            });
        }

        dialog.show();
        mask.addClass("weui_mask_visible");
        dialog.addClass("weui_dialog_visible");


        return dialog;
    };

    $.closeModal = function () {
        $(".weui_mask_visible").removeClass("weui_mask_visible").transitionEnd(function () {
            $(this).remove();
        });
        $(".weui_dialog_visible").removeClass("weui_dialog_visible").transitionEnd(function () {
            $(this).remove();
        });
    };

    $.alert = function (text, title, onOK) {
        var config;
        if (typeof text === 'object') {
            config = text;
        } else {
            if (typeof title === 'function') {
                onOK = arguments[1];
                title = undefined;
            }

            config = {
                text: text,
                title: title,
                onOK: onOK
            }
        }
        return $.modal({
            text: config.text,
            title: config.title,
            buttons: [{
                text: defaults.buttonOK,
                className: "primary",
                onClick: config.onOK
            }]
        });
    };

    $.confirm = function (text, title, onOK, onCancel) {
        var config;
        if (typeof text === 'object') {
            config = text
        } else {
            if (typeof title === 'function') {
                onCancel = arguments[2];
                onOK = arguments[1];
                title = undefined;
            }

            config = {
                text: text,
                title: title,
                onOK: onOK,
                onCancel: onCancel
            }
        }
        return $.modal({
            text: config.text,
            title: config.title,
            buttons: [
                {
                    text: defaults.buttonCancel,
                    className: "default",
                    onClick: config.onCancel
                },
                {
                    text: defaults.buttonOK,
                    className: "primary",
                    onClick: config.onOK
                }]
        });
    };

    //如果参数过多，建议通过 config 对象进行配置，而不是传入多个参数。
    $.prompt = function (text, title, onOK, onCancel, input) {
        var config;
        if (typeof text === 'object') {
            config = text;
        } else {
            if (typeof title === 'function') {
                input = arguments[3];
                onCancel = arguments[2];
                onOK = arguments[1];
                title = undefined;
            }
            config = {
                text: text,
                title: title,
                input: input,
                onOK: onOK,
                onCancel: onCancel,
                empty: false  //allow empty
            }
        }

        var modal = $.modal({
            text: '<p class="weui-prompt-text">' + (config.text || '') + '</p><input type="text" class="weui_input weui-prompt-input" id="weui-prompt-input" value="' + (config.input || '') + '" />',
            title: config.title,
            autoClose: false,
            buttons: [
                {
                    text: defaults.buttonCancel,
                    className: "default",
                    onClick: function () {
                        $.closeModal();
                        config.onCancel && config.onCancel.call(modal);
                    }
                },
                {
                    text: defaults.buttonOK,
                    className: "primary",
                    onClick: function () {
                        var input = $("#weui-prompt-input").val();
                        if (!config.empty && (input === "" || input === null)) {
                            modal.find('.weui-prompt-input').focus()[0].select();
                            return false;
                        }
                        $.closeModal();
                        config.onOK && config.onOK.call(modal, input);
                    }
                }]
        }, function () {
            this.find('.weui-prompt-input').focus()[0].select();
        });

        return modal;
    };

    //如果参数过多，建议通过 config 对象进行配置，而不是传入多个参数。
    $.login = function (text, title, onOK, onCancel, username, password) {
        var config;
        if (typeof text === 'object') {
            config = text;
        } else {
            if (typeof title === 'function') {
                password = arguments[4];
                username = arguments[3];
                onCancel = arguments[2];
                onOK = arguments[1];
                title = undefined;
            }
            config = {
                text: text,
                title: title,
                username: username,
                password: password,
                onOK: onOK,
                onCancel: onCancel
            }
        }

        var modal = $.modal({
            text: '<p class="weui-prompt-text">' + (config.text || '') + '</p>' +
            '<input type="text" class="weui_input weui-prompt-input" id="weui-prompt-username" value="' + (config.username || '') + '" placeholder="输入用户名" />' +
            '<input type="password" class="weui_input weui-prompt-input" id="weui-prompt-password" value="' + (config.password || '') + '" placeholder="输入密码" />',
            title: config.title,
            autoClose: false,
            buttons: [
                {
                    text: defaults.buttonCancel,
                    className: "default",
                    onClick: function () {
                        $.closeModal();
                        config.onCancel && config.onCancel.call(modal);
                    }
                }, {
                    text: defaults.buttonOK,
                    className: "primary",
                    onClick: function () {
                        var username = $("#weui-prompt-username").val();
                        var password = $("#weui-prompt-password").val();
                        if (!config.empty && (username === "" || username === null)) {
                            modal.find('#weui-prompt-username').focus()[0].select();
                            return false;
                        }
                        if (!config.empty && (password === "" || password === null)) {
                            modal.find('#weui-prompt-password').focus()[0].select();
                            return false;
                        }
                        $.closeModal();
                        config.onOK && config.onOK.call(modal, username, password);
                    }
                }]
        }, function () {
            this.find('#weui-prompt-username').focus()[0].select();
        });

        return modal;
    };

    defaults = $.modal.prototype.defaults = {
        title: "提示",
        text: undefined,
        buttonOK: "确定",
        buttonCancel: "取消",
        buttons: [{
            text: "确定",
            className: "primary"
        }],
        autoClose: true //点击按钮自动关闭对话框，如果你不希望点击按钮就关闭对话框，可以把这个设置为false
    };

}($);

/*loading*/
+function ($) {
    "use strict";

    var show = function (html, className) {
        className = className || "";

        var tpl = '<div class="weui_toast ' + className + '">' + html + '</div>';
        var dialog = $(tpl).appendTo(document.body);

        dialog.show();
        dialog.addClass("weui_toast_visible");
    };

    var hide = function (callback) {
        $(".weui_mask_transparent").remove();
        $(".weui_toast_visible").removeClass("weui_toast_visible").transitionEnd(function () {
            var $this = $(this);
            $this.remove();
            callback && callback($this);
        });
    };

    $.toast = function (text, style, callback) {
        if (typeof style === "function") {
            callback = style;
        }
        var className;
        if (style == "cancel") {
            className = "weui_toast_cancel";
        } else if (style == "forbidden") {
            className = "weui_toast_forbidden";
        } else if (style == "text") {
            className = "weui_toast_text";
        }



        show('<i class="weui_icon_toast"></i><p class="weui_toast_content">' + (text || "已经完成") + '</p>', className);

        setTimeout(function () {
            hide(callback);
        }, toastDefaults.duration);
    };

    $.showLoading = function (text) {
        var html = '<div class="weui_loading">';
        for (var i = 0; i < 12; i++) {
            html += '<div class="weui_loading_leaf weui_loading_leaf_' + i + '"></div>';
        }
        html += '</div>';
        html += '<p class="weui_toast_content">' + (text || "数据加载中") + '</p>';
        show(html, 'weui_loading_toast');
    };

    $.hideLoading = function () {
        hide();
    };

    var toastDefaults = $.toast.prototype.defaults = {
        duration: 2000
    }

}($);
