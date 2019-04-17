/*global define:true, window:true, document:true, $:true */
(function(){

    "use strict";

    // 常量
    var win = window,
        doc = document;

    var jqPopup = function(){

        var $win  = $(win);

        var default_opts = {
            mask     : true,          // 黑色遮盖层
            refer    : $win,          // 弹出层相对谁进行定位  默认window
            position : 'center',      // 弹出层相对refer的定位  center 还是 right  可以使用_来进行多个定位 eg : 左下 left_bottom
            type     : 'normal',      // 弹出层是一个alert 还是 confirm  定义弹出层的类型  可以多个绑定  eg   : 一个错误信息的alert类型   alert_err   会在展示上有差别的
            title    : '提示',        // 弹出层的标题
            ok_text  : '确定',        // 确定按钮 显示的文案
            no_text  : '取消',        // 取消按钮 显示的文案
            modal    : false,          // 在此次弹出层没有结束(done or fail)之前, 不允许重复弹出来
            content  : '',            // 弹出层内容

            on_ok : function(){
                return true;
            }
            //verifier_dfd : $.Deferred()      //  帮你生成的辅助on_ok 异步函数的【延迟对象】  不是promise。  当然 你完全可以自定义延迟对象

        };

        /**
         * @description 例子
         *
         *	$.popup({
        *		on_ok : function(){
        *			var self = this,
        *				popup = self.popup;    // 获得当前的弹出窗

        *			if(Synchronous_action){   // 同步验证 如果出错 只需要返回一个Boolean值即可
        *				return Boolean;  //   true : 告诉popup 你可以关掉窗口了,我已经成功了，  false: 告诉popup 窗口不要关，我出错了
        *			}

        *			var verifier_dfd = slef.verifier_dfd;

        *			setTimeout(function(){
        *				if(异步成功){
        *					verifier_dfd.resolve();   // 告诉popup 我成功了，你可以关掉窗口了
        *				}else{
        *					verifier_dfd.reject();   // 告诉popup 我失败了，不要关掉窗口，我还要做点事
        *				}
        *			}, 6000);

        *			return verifier_dfd;
        *		}
        *	})
         *	.progress(function(){
        *		this.popup // 获得当前的弹出窗
        *		// popuo第一次装载完毕后，要展示的时候，调用的函数
        *	})
         *	.done(function(){   // 点击确定之后 成功关闭弹出层后的回调函数
        *		this.popup // 获得当前的弹出窗
        *		console.log(this);  // this 就是popup的opts
        *		console.log('this is done! click the do_sure !');
        *	})
         *	.fail(function(){  // 点击取消之后的回调函数
        *		this.popup // 获得当前的弹出窗
        *		console.log('this is fail! click the no !');
        *	});
         *
         */

        var get_guid = function(){

            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c){
                var r = Math.random()*16|0,
                    v = c === 'x' ? r : (r&0x3|0x8);

                return v.toString(16);
            }).toUpperCase();

        };


        // 方法
        var get_popup_dom = function(build_popup){

                if($('#popup_cover').length === 0){

                    $('body').append(
                        $('<div>', {
                            id      : 'popup_modal',
                            'class' : 'popup_modal_block'
                        })
                        // 灰色遮盖层 TODO 这里有待和赵阳沟通 现状是他在弹出层那个Dom上写死成 都带遮盖层的效果了 故 暂且将其隐藏 只添加用于防止点击后面的modal层
                        //$('<div>', {
                        //    id      : 'popup_cover',
                        //    'class' : 'popup_cover_block'
                        //})
                        //.add(
                        //  防止在弹窗还显示的前提下，点击别的Dom
                        //    $('<div>', {
                        //        id      : 'popup_modal',
                        //        'class' : 'popup_modal_block'
                        //    })
                        //)
                    );

                }

                var popupDomHtml = '<div class="layerInner">' +
                                        '<div class="layerClose close"></div>' +
                                        '<div class="layerHead header"><p class="popup_title"></p></div>' +
                                        '<div class="layerMain content">' +
                                            // content
                                        '</div>' +
                                        '<div class="layerFoot footer">' +
                                            '<div class="pbtn no">'+ build_popup.no_text  +'</div>' +
                                            '<div class="pbtn do_sure ok">'+ build_popup.ok_text  +'</div>' +
                                        '</div>' +
                                    '</div>';

                return $('<div>', {
                    id : 'popup' + get_guid(),
                    'class' : 'parentApp-layerBox popup_block',
                    tabIndex : 0,
                    html : popupDomHtml
                }).appendTo('body');

            },

            /**
             *@Description 获取可见元素的zIndex
             * (可见元素: 不是display !== 'none' && visibility !== 'hidden' && 该dom的left top 不能有负数)
             * @param dom DomObject 一个html dom元素
             *
             * @return zIndex Number  如果无或不满足条件  返回 0;
             */
            get_visible_dom_zIndex = function(dom){

                var $self = $(dom),
                    offset = $self.offset();

                if(
                    $self.is(':visible') &&
                    (offset.left + offset.top >= offset.top)
                ){

                    // 有可能会遇到一个dom的zIndex 会超级超级大 js会自动把他弄成科学计算法
                    // 如果是auto  则取0
                    return parseInt($(dom).css('zIndex'), 10) || 0;
                }

                return 0;
            },

            /**
             * @Description 同上 获取 document 中 最大的zIndex
             */
            get_visible_zIndex = function(target, range){

                var highest = target ? +$(target).css('zIndex') : 30,
                    $range = $('*:visible'),
                    $exists_visible_popup = $range.filter('.popup_block');

                range || (range = $exists_visible_popup.length > 0 ? $exists_visible_popup : '*');

                $range.filter(range).each(function(index, value) {
                    var current_index = get_visible_dom_zIndex(this);

                    (highest < current_index) && (highest = current_index);
                });

                return highest;
            },

            set_content = function(target, selector, content){
                target.find(selector).html(content);
            },

            set_popup_type = function(target, types){

                target.removeClass('err warning');

                var type_action = {
                    alert : function(){
                        target.find('.footer .no').hide();
                    },
                    confirm : function(){
                        target.find('.close').hide();
                    },
                    onlySureBtn : function(){
                        target.find('.footer .no').hide();
                    },
                    tip : function(){
                        target.find('.header,.footer,.close').hide();
                        target.find(".content").attr("style","text-align:center;padding:15px");
                    },
                    nobtn : function(){ // 不显示“确定”“取消”按钮
                        target.find('.footer').hide();
                    },
                    err : function(){
                        target.addClass('err');
                    },
                    warning : function(){
                        target.addClass('warning');
                    }
                };

                $.each(types.split('_'), function(index, type){
                    var type_fn = type_action[type];

                    $.isFunction(type_fn) && type_fn();
                });
            },

            set_position = function(popup){

                var self = popup ? $(popup).data('opts') : this,
                    $popup = self.popup,
                    position = self.position,
                    refer = self.refer;

                if(!position){
                    console.warn("必须有position");
                    return ;
                }

                refer = $(refer);

                var target_origin_props = {};

                target_origin_props.left = $popup.offset().left,
                    target_origin_props.display = $popup.css('display');

                // 为了获取target真正的高宽 因为 隐藏的元素，无法获取其宽高
                $popup.css({
                    left    : -1000,
                    display : 'block'
                });

                var target_half_height = $popup.height() /2,
                    target_half_width = $popup.width() /2,

                    refer_offset = refer.offset() || {  // window offset is null
                            top  : 0,
                            left : 0
                        };

                refer_offset.right  = $win.width() - (refer_offset.left + refer.outerWidth());
                refer_offset.bottom = $win.width() - (refer_offset.top  + refer.outerHeight());

                var position_obj = {
                    center : {
                        top  : Math.max(0, refer.height() / 2 -target_half_height),
                        left : Math.max(0, refer.width() / 2 -target_half_width)
                    },
                    top : {
                        top  : refer_offset.top
                    },
                    left : {
                        left :  refer_offset.left
                    },

                    down : {
                        top  : refer_offset.bottom - target_half_height * 2
                    },
                    right : {
                        left : refer_offset.right - target_half_width * 2
                    }

                };

                $.each(position.split('_'), function(index, key){
                    var value = position_obj[key];

                    value && $.extend(target_origin_props, value);
                });

                //var content = $popup.find('.content'),
                    //header = $popup.find('.header'),
                    //footer = $popup.find('.footer'),
                    //max_height_content = $popup.height() - footer.outerHeight() - header.outerHeight();

                //var content_style = content.height() > max_height_content - 20 ?
                //{
                //    height    : max_height_content,
                //    overflowY : 'scroll'
                //} :
                //{
                //    height    : 'auto',
                //    overflowY : 'visible'
                //};

                //content.css(content_style);

                $popup.css(target_origin_props);

            };

        function PopUp(options){

            var self = $.extend(this, default_opts, options),
                content = self.content;

            (typeof content !== 'string')  && ( content = $(content).clone(true) );

            if( (content || '' ).length === 0){
                return;
            }

            // content里有id的dom元素集合
            var content_have_id_doms = $('<div>').html(content).find('[id][id!=""]');

            content_have_id_doms.each(function(){
                if(this.id.trim() !== ''){
                    var err_msg = '不要传一个带有id的content';

                    win.alert(err_msg);
                    $.error(err_msg + '\nid为:' + this.id);
                }
            });

            self.content = content;

            var dfd = self.dfd =  $.Deferred(),
                popup = get_popup_dom(self);

            popup.data('opts', self);

            self.popup = popup;
            self.pid = popup.attr('id');

            set_content(popup, '.content', content);
            set_content(popup, '.popup_title', self.title);

            set_popup_type(popup, self.type);

            dfd.notifyWith(self);

            self.show(self);

            self.popup.focus();

            var promise_hide = self.destroy.bind(self);

            return dfd.promise()
                .done(promise_hide)
                .fail(promise_hide);

        }

        $.extend(PopUp.prototype, {

            show : function(){

                var self = this,
                    max_index = get_visible_zIndex(),

                    popup_cover = self.mask ? $('#popup_cover') : $(''),
                    modal = self.modal ? $('#popup_modal') : $(''),

                    target = self.popup;

                popup_cover.css('zIndex', max_index -1);
                modal.css('zIndex', max_index - 2);

                set_position.call(self);

                target.css('zIndex', max_index).add(popup_cover).add(modal).show();

            },
            destroy : function(){

                if($('.popup_block').length === 1){
                    $('#popup_cover,#popup_modal').hide();
                }

                var self = this;

                self.popup.remove();

                self = null;
            }

        });

        // resize 事件
        $win.resize(
            //TODO 考虑使用throttle
            function(){
                $('.popup_block:visible').each(function(){
                    set_position(this);
                });
            }
        );

        $(doc).on('click', '.popup_block .no, .popup_block .close', function(event){

            var opts = $(this).closest('.popup_block').data('opts');

            opts.dfd.rejectWith(opts);

        });

        $(doc).on('click', '.popup_block .do_sure', function(event){

            var cur = $(this);

            cur.removeClass('do_sure');

            var opts = $(this).closest('.popup_block').data('opts');

            var on_ok = opts.on_ok,
                dfd = opts.dfd;

            if($.isFunction(on_ok) ){

                var return_on_sure_verifier = on_ok.call(
                    $.extend(
                        {},
                        opts,
                        {
                            verifier_dfd : $.Deferred()
                        }
                    )
                );

                if( return_on_sure_verifier === false ){
                    cur.addClass('do_sure');
                    return ;
                }

                if(
                    $.isPlainObject(return_on_sure_verifier) && ('done' in return_on_sure_verifier)
                ){

                    return_on_sure_verifier
                        .done(function(){
                            dfd.resolveWith(opts);
                        })
                        .always(function(){
                            cur.addClass('do_sure');
                        });

                    return;
                }

                dfd.resolveWith(opts);

            }

        });


        /**
         * 这是弹出层的基类。 如果你想实现更牛逼的diy功能 可以使用这个进行定制
         * @param options Object  详细见 default_opts
         *
         * @return Promise Object 一个承诺 您可以为其直接链式添加done 等方法
         */
        $.popup = function(options){
            return new PopUp(options);
        };

        /**
         * 这是一个完全模仿window.alert 行为的alert弹出框 主要用于提示信息
         * @param  content String || Dom   要展示的内容
         * @param  opts   Object   详细设置见 default_opts
         *
         * @return Promise Object 承诺对象
         */
        $.alert = function(content, opts){
            opts = $.extend(
                (opts || {}),
                {
                    mask : false,
                    modal : true,
                    content : content,
                    type : 'alert'
                }
            );

            return $.popup(opts);
        };

        /**
         * 这是一个类似window.confirm 行为的confirm弹出框 主要用于一个判断
         * @param  content       String || JQueryDom || Dom   要展示的内容
         * @param  sure_verifier Function 一个验证确定的方法  详见default_opts.verifier_dfd
         * @param  opts          Object   详细设置见default_opts
         *
         * @return Promise Object 承诺对象
         */
        $.confirm = function(content, sure_verifier, opts){

            opts = $.extend(
                (opts || {}),
                {
                    mask : false,
                    modal : true,
                    content : content,
                    type : 'confirm'
                }
            );

            $.isFunction(sure_verifier) && (opts.on_ok = sure_verifier);

            return $.popup(opts);
        };

        /**
         * 这是一个简单地tip提示框 主要用于提示一个信息  比如: 祝贺您 登录成功
         * @param  content String || Dom   要展示的内容
         * @param  type   String   设置tip的样式  error  warning  // TODO  这里样式还不太全
         * @param  timeout   Number  多长时间关闭  default : 1500ms
         *
         * @return Promise Object 承诺对象
         *
         */
        $.tip = function(content, type, timeout){

            var popup_obj;

            $.popup({
                mask : false,
                content : content,
                type : 'tip_' + type
            })
                .progress(function(){
                    popup_obj = this;
                });

            timeout = timeout || 1500;

            win.setTimeout(function(){
                popup_obj.destroy();
            }, timeout);
        };

        // 统一的提示说明弹层 eg: 星星奖励是什么? 点击弹出一个对应层 将来可以考虑集成到$.tip中
		(function(){
			var bindTouchMove = (function(){

				var bodyStopScroll = function(event){
					event.preventDefault();
				};

				return function(method){
					$(doc)[method](
						"touchmove",
						bodyStopScroll
					);
				};

			})();

			$(doc)
			.on("touchmove", ".doTipContent", function(event){
				event.stopPropagation();
			})
			.on("click", ".doShowTip", function(){
				var $self = $(this),
					target = $(
						$self.data("tip_content")
					);

				target.add("#jBox-overlay").show();

				bindTouchMove('on');
			})
			.on("click", ".doClickTip", function(){
				$(this).closest(".doTipContent").add("#jBox-overlay").hide();
				bindTouchMove('off');
			});
		})();

        var loadingPopup = function(content){
            var template =
                '<div class="g-layer" style="display:block;"></div>' +
                ' <div class="w-error-popup">' +
                    ' <div class="w-error-inner">' +
                        ' <div class="er-loading">' +
                            '<span class="ico"></span>' +
                        '</div>' +
                        ' <div class="er-info">'
                            + '__content__' +
                        '</div>' +
                    ' </div>' +
                ' </div>';

            var _loadingPopup = function(content){
                var $template = $(template.replace('__content__', content));
                $("body").append($template);
                return {
                    destory : $.proxy($template.remove, $template)
                };
            };

            loadingPopup = _loadingPopup;

            return _loadingPopup(content);
        };

        $.loading = loadingPopup;

    };

    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery'], jqPopup);
    } else if (typeof exports === 'object') {
        // CMD, CommonJS之类的
        module.exports = jqPopup(require('jquery'));
    }else{
        jqPopup(win.jQuery);
    }

}());
