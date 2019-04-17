/* global PM:true, define : true, $ : true */

/**
 *  @date 2015/9/8
 *  @auto liluwei
 *  @description 该模块主要负责全局的ajax事件捕获
 */

(function(){
    "use strict";
    var win = window,
        log = win.log;

    var io = function($){
        // ajax global setting
        $.ajaxSetup(
            {
                cache   : false,  // 所有的ajax 不要缓存
                timeout : 25 * 1000  // 因为我们面对的是移动用户，因此将最大延迟时间设置为 25s
            }
        );

        //  ajax pre filter action   eg: csrf  xss
        $.ajaxPrefilter(function(options, origOptions, jqXHR){
			var _data = options.data || '';

			if(options.dataType === 'json' && (typeof _data === "string")){ // TODO From Data 方式的data是一个对象 暂且不处理 只处理普通json ajax

				options.data = _data + ($.trim(_data) ? '&' : '') + $.param(PM.client_params);
			}
		});

        /**
         * received from the defined data for ajax
         * the ajax type is post
         * the ajax data type is json
         * options is ajax option,eg: timeout or cache, url,data and type will overwirte options same key
         */

        // 用来辅助缓存ajax
        var cacheAjaxUrls = {
        };

        return function(url, data, type, options) {

            type = (type || 'GET').toUpperCase();

            var opts =  $.extend(
                {
                    url: url,
                    data: data,
                    type: type,
                    seq : type === "POST",
                    dataType: "json"
                },
                options
            );

            if(opts.freeze_ms){

                var start_time = new Date().getTime(),
                    freeze_key = url + 'freeze_ms';

                if( start_time - cacheAjaxUrls[freeze_key] < opts.freeze_ms){
                    console.log('两次请求间隔太短\n url : %s \n opts: %j ', url, opts);
                    return ;
                }

                cacheAjaxUrls[freeze_key] = start_time;
            }

            if(opts.seq){

                if(cacheAjaxUrls[url] === "loading" ){
                    console.log('请等待相同url的上一个请求结束\n url : %s \n opts: %j ', url, opts);
                    return  ;
                }

                cacheAjaxUrls[url] = "loading";
            }

            return $.ajax(opts)
                .done(function(cbData){
                    if(!cbData.success){

                        log(
                            {
                                errMsg : ['ajax 返回逻辑出错 调用参数 %j, 出错信息 %j', opts, arguments],
                                op : "ajax"
                            },
                            "error"
                        );

                        if(+cbData.errorCode === 900){

                            if(!PM.doExternal( "redirectLogin", "" )){
                                win.location.href = "/";
                            }

                            return ;
                        }

                    }
                })
                .fail(function (res, status, error) {
                    log(
                        {
                            errMsg : ['ajax 物理出错 调用参数 %j, 出错信息 %j', opts, arguments],
                            op : "ajax"
                        },
                        "error"
                    );
                    if(error === "timeout"){
                        win.alert("服务器正忙，请稍后重试!")
                    }
                })
                .always(function(){
                    if(opts.seq){
                        cacheAjaxUrls[url] = null;
                    }
                });
        };
    };


    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery'], io);
    } else if (typeof exports === 'object') {
        // CMD, CommonJS之类的
        module.exports = io(require('jquery'));
    }else{
        win.io = io(win.jQuery);
    }

})();

