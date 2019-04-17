/*
 * 改版所用msg框
 */
define(["jquery"], function ($) {
    var closeTimer;
    var msg = {
        /**
         * 弹出框节点
         * @property _el
         * @type Element|Null
         * @default null
         * @private
         */
        _el: null,
        /**
         * 遮盖层节点
         * @property _el_bg
         * @type Element|Null
         * @default null
         * @private
         */
        _el_bg: null,
        /**
         * 弹出框操作后的回调方法集
         * @property _fun
         * @type Objcet
         * @default {}
         * @private
         */
        _fun: {},
        /**
         * 是否点击后自动关闭
         * @property _auto_close
         * @type Boolean
         * @default true
         * @private
         */
        _auto_close: true,
        /**
         * 创建弹出框方法
         * @namespace 17.msg
         * @method _create
         * @private
         */
        _create: function (conf) {
            //处理点击操作
            var _this = this;

            //插入遮盖层
            this._el_bg = $('<div class="msg-bg"></div>');
            if (!conf.disableShade) {
                $('body').append(this._el_bg);
            }
            //插入弹出层
            this._el = $('<div id="msg" class="msg-doc"></div>');
            $('body').append(this._el);
            this._el_bg.click(function () {
                if (conf.closeOut) {
                    _this.close();
                }
            });

            this._el.on('click', '.mj-close,.msg-btn,.msg-btn-cancel', function (e) {
                var event = $(this).attr('data-event');
                if (typeof(_this._fun[event]) == 'function') {
                    _this._fun[event]($(this));
                    e.preventDefault();
                }
                if (_this._auto_close || event == "cancel") {
                    _this.close();
                }
            });
        },
        /**
         * _内部方法，显示对话框并且定位
         * @namespace 17.msg
         * @method _show
         * @param {Object} conf 弹框配置
         * {
         *     content : '', //弹框内容
         *     title : '提示', //弹出框标题
         *     okText : '确定', //Ok按钮的文字
         *     okGE : 'a/b/c', //OK的gaevent值
         *     cancelText : '取消', //Cancel按钮的文字
         *     cancelGE : 'a/b/c', //Cancel的gaevent值
         *     closeRT : true, //是否显示右上角关闭按钮
         *     type : 'alert',//弹框类型
         *     autoClose : false //是否点击按钮后自动关闭
         * }
         * @static
         */
        _show: function (conf) {
            conf = conf || {};
            this.conf = conf;
            //如果之前没有创建，就即时创建
            clearTimeout(closeTimer);
            $('.msg-doc').remove();
            $('.msg-bg').remove();
            this._create(conf);

            //自动关闭处理
            this._auto_close = conf.autoClose == undefined ? true : conf.autoClose;

            //组织代码
            if (conf.type == 'alert' || conf.type == 'confirm') {
                //处理ga值
                conf.okGE = conf.okGE || 'msg/ok';
                conf.cancelGE = conf.cancelGE || 'msg/cancel';

                var html = '';
                if (conf.title != undefined) {
                    html += '<div class="msg-hd">'
                        + conf.title
                        + (conf.closeRT === true ? '<a class="msg-close" gaevent="' + conf.okGE + '" data-event="close" href="#">×</a>' : '')
                        + '</div>';
                }
                html += '<div class="msg-bd">' + conf.content + '</div>'
                    + '<div class="msg-ft cf">'
                    + '<span class="msg-btn msg-btn-ok" gaevent="' + conf.okGE + '" data-event="ok">' + (!conf.okText ? '确定' : conf.okText) + '</span>'
                    + (conf.type == 'confirm' ? '<span class="msg-btn msg-btn-cancel" gaevent="' + conf.cancelGE + '" data-event="cancel">' + (!conf.cancelText ? '取消' : conf.cancelText) + '</span>' : '')
                    + '</div>';
            } else if (conf.type == 'dialog') {
                var html = '';
                html += '<div class="msg-hd">'
                    + conf.title
                    + '</div>';
                html += '<div class="msg-bd">' + conf.content + '</div>'
                    + '<div class="msg-ft cf">'
                    + '<span class="msg-btn msg-btn-ok" gaevent="' + conf.okGE + '" data-event="ok">' + (!conf.okText ? '确定' : conf.okText) + '</span>'
                    + '</div>';
            } else if (conf.content) {
                var html = conf.content;
            }

            //遮盖部分的计算
            this._el_bg.css('left', '0');
            this._el_bg.css('height', Math.max($(window).height(), $(document).height()) + 'px');

            //输出内容
            if (html) {
                this._el.html(html);
            } else {
                this._el.append(conf.dom);
            }
            this._el[0].className = 'msg-doc msg-' + conf.type;

            window.scrollBy(0, 1);
        },
        /**
         * 关闭对话框
         * @namespace 17.msg
         * @method close
         * @static
         */
        close: function () {
            if (this.conf.closeFun) {
                this.conf.closeFun();
            } else {
                var _this = this;
                _this._el.animate({
                    scale: ".8",
                    opacity: "0"
                }, 200, "ease-in");
                _this._el_bg.fadeOut(200);
                closeTimer = setTimeout(function () {
                    _this._el.remove();
                    _this._el_bg.remove();
                }, 300);
            }
        },

        dialog: function (conf) {
            conf = conf || {};
            conf.type = 'dialog';
            conf.closeOut = (conf.closeOut === false) ? false : true;
            this.conf = conf;
            this._fun.ok = conf.okFun;
            this._show(conf);
        },
        /**
         * 自定义Alert
         * @namespace 17.msg
         * @method alert
         * @param {String} text 弹出框显示的文本内容
         * @param {Function} [ok_fun] 点击Ok后的处理方法，默认点击后会直接关闭对话框
         * @param {Object} [conf] 弹出框的配置
         *     {
         *         title : '提示', //弹出框标题
         *         okText : '确定', //OK按钮的文字
         *         okGE : 'a/b/c', //OK的gaevent值
         *         closeRT : true, //是否显示右上角关闭按钮
         *         autoClose : false //是否点击按钮后自动关闭
         *     }
         * @static
         */
        alert: function (text, ok_fun, conf) {
            conf = conf || {};
            conf.type = 'alert';
            conf.content = text;
            this._fun = ok_fun ? {ok: ok_fun} : {};
            this._show(conf);
        },
        toast: function (text, conf) {
            $('.msg-toast').remove();
            var dom = $('<div class="msg-doc msg-toast">' + text + '</div>').appendTo('body');
        },
        /**
         * 自定义Confirm
         * @namespace 17.msg
         * @method confirm
         * @param {String} text 弹出框显示的文本内容
         * @param {Object} [fun] 点击后的处理方法，默认点击后会直接关闭对话框
         *     {
         *         ok : function(){},
         *         cancel : function(){}
         *     }
         * @param {Object} [conf] 弹出框的配置
         *     {cancelT
         *         title : '提示', //弹出框标题
         *         okText : '确定', //Ok按钮的文字
         *         okGE : 'a/b/c', //OK的gaevent值
         *         cancelText : '取消', //Cancel按钮的文字
         *         cancelGE : 'a/b/c', //Cancel的gaevent值
         *         closeRT : true, //是否显示右上角关闭按钮
         *         autoClose : false //是否点击按钮后自动关闭
         *     }
         * @static
         */
        confirm: function (text, fun, conf) {
            conf = conf || {};
            conf.type = 'confirm';
            conf.content = text;
            if (typeof fun == "function") {
                fun = {ok: fun}
            }
            this._fun = fun || {};
            this._show(conf);
        },
        /**
         * 多按钮弹出框
         * @namespace 17.msg
         * @method option
         * @param {String} text 弹出框显示内容
         * @param {Array} [btns] 按钮组的配置
         *     [
         *         {
         *             text : '按钮名称', //按钮的名称
         *             url : '#', //按钮需要跳转的URL
         *             fun : function(){}, //点击按钮需要运行的方法
         *             cls : 'class', //按钮需要使用的特殊样式
         *             ge : 'a/b/c' //按钮的gaevent值
         *         },
         *         ...
         *     ]
         * @param {Function} [cancel_fun] 弹出框的配置
         * @param {Object} [option] 弹出框的配置
         *     {
         *         cancelText : '取消', //Cancel按钮的文字
         *         cancelGE : 'a/b/c', //Cancel的gaevent值
         *         autoClose : false //是否点击按钮后自动关闭
         *     }
         * @static
         */
        option: function (text, btns, cancel_fun, conf) {
            conf = conf || {};
            conf.type = 'option';
            this._fun = cancel_fun ? {cancel: cancel_fun} : {};
            var html = '';
            html += text ? '<div class="msg-bd">' + text + '</div>' : '';
            html += '<div class="msg-option-btns">';
            for (var i in btns) {
                if (btns[i].text) {
                    if (btns[i].fun) {
                        this._fun['btn_' + i] = btns[i].fun;
                    }
                    var ge = btns[i].ge || 'msg/btn_' + i;
                    if (btns[i].url) {
                        html += '<a class="btn msg-btn' + (btns[i].cls ? ' ' + btns[i].cls : '') + '" gaevent="' + ge + '" href="' + btns[i].url + '">' + btns[i].text + '</a>';
                    } else {
                        html += '<button class="btn msg-btn' + (btns[i].cls ? ' ' + btns[i].cls : '') + '" gaevent="' + ge + '" data-event="btn_' + i + '" type="button">' + btns[i].text + '</button>';
                    }
                }
            }
            html += '</div>';
            html += '<button class="btn msg-btn-cancel" gaevent="' + conf.cancelGE + '" data-event="cancel" type="button">' + (!conf.cancelText ? '取消' : conf.cancelText) + '</button>';
            conf.content = html;
            var _this = this;
            conf.closeFun = function () {
                _this._el.animate({
                    translateY: "100%"
                }, 200, "ease-in");
                _this._el_bg.fadeOut(200);
                closeTimer = setTimeout(function () {
                    _this._el.remove();
                    _this._el_bg.remove();
                }, 300);
            }
            this._show(conf);
        },
        slide: function (dom, conf) {
            conf.type = 'slide';
            conf.dom = dom;
            var _this = this;
            conf.closeFun = function () {
                _this._el.animate({
                    translateY: "100%"
                }, 200, "ease-in");
                _this._el_bg.fadeOut(200);
                closeTimer = setTimeout(function () {
                    _this._el.remove();
                    _this._el_bg.remove();
                }, 300);
            }
            this._show(conf);
        },
        /**
         * 自定义弹框
         * @namespace 17.msg
         * @method diy
         * @param {String|Element} html 弹出框显示内容
         * @param {Object} [fun] 弹出框的配置
         *     {
         *         fun : function(){},
         *         ...
         *     }
         * @param {Objcet} opt 配置，类似其他方法的 conf
         *      {
         *          type: 'diy',
         *          closeOut: true
         *      }
         */
        diy: function (html, fun, opt) {
            var conf = {
                content: html,
                type: opt.type || 'diy',
                closeOut: (opt.closeOut === false ? false : true)
            };
            this._fun = fun || {};
            this._show(conf);
        }
    };
    return msg;

});
