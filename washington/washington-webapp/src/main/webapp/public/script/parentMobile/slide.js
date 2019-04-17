/* global define : true, $:true */
/**
 *  @date 2015/9/8
 *  @auto liluwei
 *  @description 该模块主要负责banner的轮播效果 主要提供的功能:  关闭banner  根据制定时间自动轮播  支持移动端左右滑动事件  目前直支持横向
 */

// TODO 该模块只考虑了 requirejs

/*

 使用方法:  该plugin使用全局代理模式，只需要html注明特有的class, 这个神力就会出现
 html 结构(emmet写法):
 div.doScroll[data-SlideOption='{"xx" : "yy"}']>(ul>(li>img{内容容器})*n)

 javascript: 也可以用javascript来进行更丰富的diy
 Slide(
 dom,
 {
 // 配置详细看DEFAULT_OPTIONS
 }
 );
 */

define([], function(){

    'use strict';

    var doc = document,
        win = window;

    var SlideDataName = 'PlugSlide';

    // 检测 是否支持触屏事件
    var DocumentTouch = win.DocumentTouch,
        support = {
            touch : ('ontouchstart' in win) || DocumentTouch && doc instanceof DocumentTouch
        };

    // 因为所依赖的jQuery封装了原生的event， 所以touch属性需要从原来的event获取
    var setOriginTouchEvent = function(event){
        if(support.touch && !event.touches){
            event.touches = event.originalEvent.touches;
        }
    };

    // 自动轮播
    var fnAutoSlide = function(slide){
        if(slide.opts.autoSwipe){
            fnStopSlide(slide);
            slide.swipeInterval = setInterval(function(){
                doSlide(slide, 'next', '.3');
            }, slide.opts.speed);
        }
    };

    // 停止轮播
    var fnStopSlide = function(slide){
        slide.opts.autoSwipe && slide.swipeInterval && clearInterval(slide.swipeInterval);
    };

    var touchMoveEvent = function(event, slide){
        // 如果自动切换，move的时候清除autoSlide自动轮播方法
        fnStopSlide(slide);

        slide.allowSlideClick = false;

        // 触摸时的坐标
        slide._curX = support.touch ? event.touches[0].pageX : (event.pageX || event.clientX);
        slide._curY = support.touch ? event.touches[0].pageY : (event.pageY || event.clientY);

        // 触摸时的距离
        slide._moveX = slide._curX - slide._startX;
        slide._moveY = slide._curY - slide._startY;

        // 优化触摸禁止事件
        if(slide.iSlidecrolling === undefined){
            if(slide.opts.axisX){
                slide.iSlidecrolling = Math.abs(slide._moveX) >= Math.abs(slide._moveY);
            }else{
                slide.iSlidecrolling = Math.abs(slide._moveY) >= Math.abs(slide._moveX);
            }
        }

        // 距离
        if(slide.iSlidecrolling){
            event.preventDefault();

            // 触摸时跟手
            setTransition(slide, slide.opts.ul, 0);
            slide._moveDistance = slide._moveDistanceIE = slide.opts.axisX ? slide._moveX : slide._moveY;

        }

        // 触摸时跟手滚动
        doTranslate(slide, slide.opts.ul, -(slide._slideDistance * slide.currentIndex - slide._moveDistance));
    };

    var resizeEvent = 'onorientationchange' in win ? 'orientationchange' : 'resize',
        transitionEndEvent = 'webkitTransitionEnd MSTransitionEnd transitionend',
        slideEvents = {
            touchstart : function(event, slide){

                setOriginTouchEvent(event);

                slide.iSlidecrolling = undefined;
                slide._moveDistance = slide._moveDistanceIE = 0;
                // 按下时的坐标
                slide._startX = support.touch ? event.touches[0].pageX : (event.pageX || event.clientX);
                slide._startY = support.touch ? event.touches[0].pageY : (event.pageY || event.clientY);
            },
            touchmove  : function(event, slide){
                setOriginTouchEvent(event);
                touchMoveEvent(event, slide);
            },
            touchend   : function(event, slide){
                // 优化触摸禁止事件
                if(!slide.iSlidecrolling){
                    fnAutoSlide(slide);
                }

                // 距离小
                if(Math.abs(slide._moveDistance) <= slide._distance){
                    doSlide(slide, '', '.3');
                    // 距离大
                }else{
                    // 手指触摸上一屏滚动
                    if(slide._moveDistance > slide._distance){
                        doSlide(slide, 'prev', '.3');
                        // 手指触摸下一屏滚动
                    }else if(Math.abs(slide._moveDistance) > slide._distance){
                        doSlide(slide, 'next', '.3');
                    }
                }
                slide._moveDistance = slide._moveDistanceIE = 0;
            }
        };

    slideEvents[resizeEvent] = function(event, slide){
        clearTimeout(slide.timer);

        slide.timer = setTimeout(function(){
            initSlideDistance(slide);
        },150);
    };

    // css过渡
    var setTransition = function(slide, dom, num){
        dom.css({
            'transition':'all '+num+'s '+slide.opts.transitionType
        });
    };

    // css位移
    var doTranslate = function(slide, dom, distance){
        var result = slide.opts.axisX ? distance+'px,0,0' : '0,'+distance+'px,0';

        dom.css({
            'transform':'translate3d('+result+')'
        });
    };

    // 初始化幻灯片位置信息
    var initSlideDistance = function (slide){

        var $li = slide.opts.ul.children();

        slide._slideDistance = slide.opts.axisX ? slide.opts.ul.width() : slide.opts.ul.height();

        // 定位
        setTransition(slide, slide.opts.ul, 0);

        doTranslate(slide, slide.opts.ul, -slide._slideDistance*slide.currentIndex);

        setTransition(slide, $li, 0);

        var num =  -1;
        $li.each(function(index){
            doTranslate(slide, $(this), slide._slideDistance * (index + num));
        });

    };

    // 绑定全局事件
    var getSlideBySlideDom = function(slideDom){
        var $slideDom = $(slideDom);
        return $slideDom.data(SlideDataName) || new Slide($slideDom, $slideDom.data('SlideOption'));
    };

    (function(){

        var isOnlyOne = function(liItem){
            return $(liItem).closest("ul").find("li").length < 2;
        };

        // 绑定触摸
        var event_name,
            bind_slide_event = function(){
                $(doc).on(
                    event_name,
                    '.doSlide',
                    function(event){

                        if( isOnlyOne(this) ){
                            return ;
                        }

                        slideEvents[event_name].call(
                            this,
                            event,
                            getSlideBySlideDom(
                                $(this)
                            )
                        );
                    }
                );

            };

        for( event_name in slideEvents){
            if(slideEvents.hasOwnProperty(event_name)){
                bind_slide_event();
            }
        }

        // 绑定css3运动结束
        $(doc).on(
            transitionEndEvent,
            '.doSlide>ul',
            function(){
                fnAutoSlide(
                    getSlideBySlideDom(
                        $(this).closest('.doSlide')
                    )
                );
            }
        );

        $.iosOnClick(
            '.doSlide .doSlideDots .doDot',
            function(){
                var $self = $(this),
                    slideDom = $self.closest('.doSlide'),
                    slide = getSlideBySlideDom(slideDom);

                doSlide(
                    slide,
                    $self.index(),
                    '.3'
                );

                slideDom.find('.doSlideDots .doDot').removeClass('active');
                $self.addClass('active');

            }
        );

    })();

    // 轮播动作
    var doScroll = function(slide, num){
        setTransition(slide, slide.opts.ul, num);
        doTranslate(slide, slide.opts.ul, -slide.currentIndex * slide._slideDistance);
    };

    // 轮播方法
    var doSlide = function(slide, command, num){

        // 判断方向
        var nextIndex = slide.currentIndex;

        if(typeof command === 'number'){
            nextIndex = command;
        }else if(command === 'next'){
            nextIndex += 1;
        }else if(command === 'prev'){
            nextIndex -= 1;
        }

        slide.currentIndex = nextIndex;

        doScroll(slide, num);

        // 当超过的限制的时候，[平滑]归位
        var homing = function(index){
            slide.currentIndex = index;
            setTimeout(
                function(){
                    doScroll(slide, 0);
                },
                300
            );
        };

        if(nextIndex >= slide._itemCount){
            homing(0);
        }else if(nextIndex < 0){
            homing(slide._itemCount-1);
        }

        // 如果第二个参数为空，就不回调
        var changeCallback = slide.opts.changeCb;
        arguments[1] !== '' && $.isFunction(changeCallback) && changeCallback.call(slide, slide.currentIndex, slide._itemCount, slide.slide);
    };

    var Slide = function(element, options){

        var slide = this;

        slide.slide = $(element);

        if(slide.slide.data(SlideDataName) !== undefined){
            return ;
        }

        slide._distance = 50;

        slide.allowSlideClick = true;

        slide.init(options);

        return slide;

    };

    // 初始化
    var DEFAULT_OPTION = {
        index               : 0,            // 轮播初始值
        autoSwipe           : true,        // 自动切换
        speed               : 3000,         // 切换速度
        axisX               : true,        // X轴
        transitionType      : 'ease',       // 过渡类型
        builtCb             : $.noop,       // 初始化完毕后，回调函数
        changeCb            : $.noop        // 每次滚动回调
    };

    $.extend(
        Slide.prototype,
        {
            comandTo: function (index) {
                doSlide(this, index, '.3');
            },
            destroy : function(){
                var slide = this,
                    prop;

                fnStopSlide(slide);

                slide.slide.removeData(SlideDataName).remove();

                for(prop in slide){
                    if(slide.hasOwnProperty(prop)){
                        slide[prop] = null;
                    }
                }

                slide = null;
            },
            init : function(options){
                var slide = this,
                    slideDom = slide.slide,
                    $ul = slideDom.children('ul');

                // default option
                slide.opts = $.extend(
                    {
                        ul                  : $ul,                 // 父dom
                        li                  : $ul.children('li')   // 子dom
                    },
                    DEFAULT_OPTION,
                    options
                );

                slide.currentIndex = slide.opts.index;

                // 轮播数量
                slide._itemCount = slide.opts.li.length;
                slide.iSlidecrolling;

                // 如果轮播小于等于1个，跳出
                if(slide._itemCount < 2){
                    return ;
                }

                // 为自动滚动做准备
                slide.opts.ul
                    .prepend(
                    slide.opts.li.last().clone()
                )
                    .append(
                    slide.opts.li.first().clone()
                );

                // 设置轮播的总宽度
                initSlideDistance(slide);

                // 默认开始自动调用轮播
                fnAutoSlide(slide);

                // 给当前Dom设置已经初始化完毕的标识
                slideDom.data(SlideDataName, slide);

                // 调用初始化完毕回调函数
                var builtCallback = slide.opts.builtCb;
                $.isFunction(builtCallback) && builtCallback.call(slide, slide.currentIndex, slide._itemCount, slideDom);
            }
        }
    );

    $(function(){
        // 初始化启动一次  TODO 这里以后可以使用MutationObserver 监听新Dom添加进来
       $('.doSlide').each(function(index, dom){
           getSlideBySlideDom(dom);
       });
    });

    return Slide;

});
