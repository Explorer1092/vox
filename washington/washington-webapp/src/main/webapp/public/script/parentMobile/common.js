/* global $ : true, log : true */
/**
 *  全局公用的函数
 */

(function($){

    "use strict";

    var WIN = window,
        DOC = document,
        USERAGENT = WIN.navigator.userAgent.toLowerCase(),
        PM = WIN.PM,
        isNotAndroid = USERAGENT.indexOf('android') === -1;

    var app_version =  USERAGENT.match(/17parent\/((\d\.?)+)/);

    if(app_version){
        $.extend(PM, {
            app_version : app_version[1],
            isWebview : true
        });
    }


    (!WIN.notUseAdapt) && $.isFunction(WIN.adaptUILayout) && WIN.adaptUILayout(640);



    WIN.logApp || (WIN.logApp = "17Parent");
    WIN.logWhere || (WIN.logWhere = "parent");


    // TODO 防劫持
    /*
    (function(){
        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver,
            target = document.querySelector('body');

        var checkDomUrl = function(dom){
            if(!dom){
                return ;
            }
            $(dom).find("img, embed, object, video, audio, iframe").each(function(index, value){
                if( (value.src || value.href || "").search("17zuoye.com") === -1 ){
                    $(value).remove();
                }
            });
        };

        // 创建观察者对象
        var observer = new MutationObserver(function(mutations) {

            mutations.forEach(function(mutation) {
                checkDomUrl(mutation.addedNodes[0]);
            });
        });

        var config = { addedNodes: true,childList: true };

        observer.observe(target, config);

        setTimeout(observer.disconnect.bind(observer), 6000);

    })();
    */

    if(typeof WIN.log !== "function"){
        var url = '//log.17zuoye.cn/log?_c=vox_logs:js_errors_logs&_l=3&_log={"errMsg":"未成功加载log.js模块(/plug/log.js)"}';
        $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
        WIN.log = $.noop;
    }

    //global check ajax
    (function(){

        var taskQueue = {
                androidTopHeight : function(){

                    if( isNotAndroid ){
                        return ;
                    }

                    $(".parentApp-topBar").addClass("parentApp-topBarAndroid");

                }
            },
            taskCount = Object.keys(taskQueue).length;

        PM.ajaxCount = PM.ajaxCount || 1;

        // For 红米note出现loading重叠和不不对齐的问题 Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn; HM NOTE 1TD Build/JDQ39) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30
        // 详细分析 请见 ： http://project.17zuoye.net/redmine/issues/14390
        var LoadingShowTime = USERAGENT.indexOf('hm note') === -1 ? 0 : 500;
        setTimeout(function(){//body添加style样式是为了对方有些页面body不是白色的情况
            $("body").attr("style","background-color: #fff;");
            $("#globalLoading").show();
        }, LoadingShowTime);

        var destoryGlobalLoading = function(focus){

            if( !focus && $("#globalLoading").length > 0 && (PM.ajaxCount > 0 || taskCount > 0) ){
                return;
            }
            //去除body loading时添加的白底
            $("body").removeAttr("style");
            $("#globalLoading").remove();

            focus || $("#mockBody").show().removeClass('hide').trigger("bodyIsShow");

        };

        setTimeout(destoryGlobalLoading, 4000, true);

        // 判断全局的ajax
        $(DOC)
            .ajaxSend(function(){
                PM.ajaxCount++;
            })
            .ajaxComplete(function(){
                if( --PM.ajaxCount > 0 ){
                    return ;
                }

                PM.ajaxCount = 0;
                destoryGlobalLoading();

            });

        $.each(taskQueue, function(taskName, fn){
            fn();
            taskCount--;
        });

        var dfd = $.Deferred();
        dfd.done(destoryGlobalLoading);

        WIN.setTimeout(function(){
            PM.ajaxCount--;
            dfd.resolve();
        }, 600);

    })();

    // 等待某些事情结束
    $.fn.waitSomething = function(eventName, condition, fn, notOne, timer, not_record_log){

        var $self = $(this),
            waitTimer = 0,
            method = notOne ? "bind" : "one",
            isDone;

        timer || (timer = 800);

        $self[method](eventName, function(){
            isDone = true;
            fn.apply($self, arguments);
        });

        var _setInterval = setInterval(function(){

            if(++waitTimer < 10 && !condition() && isDone === undefined ){
                return;
            }

            clearInterval(_setInterval);

            if(waitTimer > 9 && !not_record_log){

                log(
                    {
                        errMsg: ["等待 " + eventName + " 事件, 超时"],
                        op    : "callOpenSecondWebview"
                    },
                    "error"
                );

                return ;
            }

            $self.trigger(eventName);

        }, timer);

        return $self;
    };

    // 检查external是否已经加载了某个方法
    var checkExternalMethodIsExist = function(methodName){
        var external = "external" in  window && window.external;

        if(!external){
            return false;
        }

        var method = external[methodName];

        return (typeof method === "function") && function(){ method.apply(external, arguments);};

    };

    PM.doExternal = (function(){

        var externalErrorLog = function(errorMsg, logOp){
            log(
                {
                    errMsg: [errorMsg],
                    op    : logOp
                },
                "error"
            );
        };

        return function(methodName){
            var method = checkExternalMethodIsExist(methodName),
                logOp = "external_" + methodName,
                errorMsg;

            if(!method){

                errorMsg = '未找到原生提供的 '+ methodName +' 方法';

                externalErrorLog(errorMsg, logOp);
                return false;
            }

            var methodArgs = Array.prototype.slice.call(arguments, 1),
                result = true;

            try{
                method.apply(null, methodArgs);
            }catch(err){
                result = false;

                errorMsg = '调用原生 ' + methodName+ ' 方法失败';
                externalErrorLog(errorMsg, logOp);
            }

            return result;
        };
    })();

    // 修复IOS点击问题 TODO  FastClick 也修复了改事件， 因此有了FastClick可以去掉这个函数  留作以后吧
    (function(){

        // 启动fastClick
        WIN.FastClick.attach(DOC.body);

        var iosReg = /iPad|iPhone|iPod/,
            isiOS = iosReg.test(WIN.navigator.platform) && iosReg.test(WIN.navigator.userAgent) && !WIN.MSStream;

        $.iosOnClick = function(target, clickFn){

            $(document).on("click", target, clickFn );

            if(!isiOS){
                return $;
            }

            var ignoreReg = /button|a|input/,
                checkIsCanUseClick = function(target){

                    var $target = $(target);

                    return $target.css("cursor") === "pointer" || ignoreReg.test($target[0].tagName.toLocaleString());
                };

            $(document)
                .on("touchstart", target, function(){
                    var self = this,
                        $self = $(self);

                    if( $self.data("doBindClicked") || checkIsCanUseClick(target) ){
                        return ;
                    }

                    $self.data("doBindClicked", true).css("cursor", "pointer");
                });

            return $;

        };

    })();

    // 上报打点功能
    (function(){

		PM.doTrack = $.noop;

        //只针对手机打点 因此只监听来touchstart 保证打点数据的干净
        if( !(/android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/.test(USERAGENT) && ("ontouchstart" in document) ) ){
            return ;
        }

		var build_track_s_obj = function(track_s, split){
				var track_s_obj = {};

				track_s && track_s.split(split).forEach(function(key, index){
					track_s_obj['s' + index] = key;
				});

				return track_s_obj;
			},
			global_track_s_obj = build_track_s_obj(PM.track_s, '|');

		PM.doTrack = function(moduleName, trackName, customer_track_s, track_opts){

            var _track_opts = $.extend(
                {track_s_separator : '_'},
                $.isPlainObject(track_opts) ? track_opts : {}
            );

			log(
				$.extend(
					{
						app:"17Parent",
						"module" : $.trim(moduleName),
						op : $.trim(trackName)
					},
					customer_track_s ? build_track_s_obj(customer_track_s, _track_opts.track_s_separator) : global_track_s_obj
				),
				"point"
			);
		};

		var doTrackByDom = function(){
			var $self = $(this),
				track_info = $self.length === 0 ? [] : $.trim($self.data("track")).split("|");

			(track_info.length >= 2) && PM.doTrack.apply(null, track_info);

			return $self;
		};

		$(".doAutoTrack").each(function(index, dom){
			doTrackByDom.call( dom ).removeClass("doAutoTrack");
		});

		// 增加thrott 缓流措施
		$(WIN).on("scroll", function(){
			$(DOC.getElementsByClassName("doScrollTrack")).each(function(){
				doTrackByDom.call(this).remove();
			});
		});


        $.iosOnClick(".doTrack", doTrackByDom);

    })();


    $(DOC).on("click", ".doShowDetailError", function(){
        $(".detailErrorContent").toggle();
    });

    //page from weChat by ua
    WIN.isFromWeChat = function(){
        return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") != -1);
    }


	$.iosOnClick('a', function(){

		var self = this;

		if(
			(self.href.search("javascript") === 0) ||
			$(self).hasClass('do_not_add_client_params')
		){
			return ;
		}

		var client_params = $.param(PM.client_params),
			href_search = self.search;

		href_search.search('app_version=') === -1 && (self.href += (self.search ? '&' : '?') + client_params);

	});


    //ui2.0 版本大于1.6的remove掉title div
    (function(){
        var titleDOM=$("#do-head-title-adapt");
            if(titleDOM.length>0){
                var version=(PM.client_params&&PM.client_params.app_version)||PM.app_version||'0.0';
                if(version>='1.6'){
                    titleDOM.remove();
                }
            }
    })();

	$(WIN).waitSomething(
		'close_loading',
		function(){
			return 'closeLoading' in (WIN.external || {});
		},
		function(){
			PM.doExternal('closeLoading', '');
		},
		false,
		300,
		true
	);

})($);

