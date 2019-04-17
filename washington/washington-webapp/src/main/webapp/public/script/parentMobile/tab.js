/* global define : true, $:true */
/**
 *  @date 2015/9/8
 *  @auto liluwei
 *  @description 该模块主要负责家长端的 Tab 操作  支持ajax 以及 local data两种方式. 设计思想用的是 Promise
 *
 *
 *  TODO 将来如果需要支持web那样，根据url进去不同的Tab 可以采用 hashChange 的方式 http://git.oschina.net/_rambo/css3_tab
 *  TODO 该模块只考虑了 requirejs
 */

/*
 使用方法:  该plugin使用全局代理模式，只需要html注明特有的class, 这个神力就会出现
 */

define([ 'ajax', 'template'], function( ioPromise, antTemplate){

    'use strict';

    var tabCache          = {},
        TAB_CACHE_TIMEOUT  = 1000 * 60 * 20,
        stringify          = JSON.stringify;

    var tabAction = {
        ajax : function(ajaxUrl, ajaxData, needCache){
            var dfd = $.Deferred();

            $.isPlainObject(ajaxData) || (ajaxData = {});

            if(!ajaxUrl){
                return dfd.reject(
                    '请给定Tab切换功能所需的合法的ajax url'
                );
            }

            // FIXME 这里设置一个 过期时间
            if(needCache && (ajaxUrl in tabCache) ){

                var timeStamp       = new Date().getTime(),
                    cacheData       = tabCache[ajaxUrl],
                    timeIsNotExpire = timeStamp < cacheData.time + TAB_CACHE_TIMEOUT,
                    dataIsNotChange = stringify(ajaxData) !== stringify(cacheData.ajaxData);

                if(timeIsNotExpire && dataIsNotChange){
                    return dfd.resolve(
                        {
                            html : cacheData.html
                        }
                    );
                }

            }

            return ioPromise(
                ajaxUrl,
                ajaxData
            );
        },
        local : function(selector){
            var dfd = $.Deferred();

            if(!selector ){
                return dfd.reject(
                        '请给定Tab切换功能所需的合法的选择器'
                );
            }

            if(selector === "nullTab"){
                return dfd.resolve(
                    {
                        success : true
                    }
                );
            }


            var $selector = $(selector);

            if($selector.length === 0){
                return dfd.reject(
                        '给定的选择器，找不到制定的Dom \n 选择器: ' + selector
                );
            }

            return dfd.resolve(
                {
                    success : true,
                    html : $selector.html()
                }
            );
        }
    };

    $.iosOnClick(
        '.doTab',
        function(event){
            event.preventDefault();

           var $self = $(this),
               $selfData = $self.data(),
               targetEl = $selfData.tab_target_el,
               tabTemplateEl = $selfData.tab_template_el,
               ajaxUrl =  $selfData.tab_ajax_url,
               ajaxData = $selfData.tab_ajax,
               needCache = $selfData.tab_cache === undefined,
               localDomSelctor = $selfData.tab_local;

            var promise = ajaxUrl ?
                tabAction.ajax(ajaxUrl, ajaxData, needCache) :
                tabAction.local(localDomSelctor);

            // FIXME  loading 图

            promise
                .done(function(tabResult){
                    if(tabResult.success === false){
                        console.error(tabResult.msg);
                        return ;
                    }


                    var templateData= $.extend(
                        {},
                        tabResult,
                        {
                            window : window
                        }
                    );

                    antTemplate.helper('priceFormat', function (data, format) {
                        if(isNaN(data)){
                            throw new Error("priceFormat: param error type");
                        }
                        return new Number(data).toFixed(format);
                    });

                    var tabHtml = tabTemplateEl && !tabResult.html ?
                        antTemplate(
                            null,
                            $(tabTemplateEl).html()
                        )(
                            templateData
                        )
                        :
                        tabResult.html;

                    $(targetEl).html(tabHtml || '');

                    $self
                        .closest('.doTabBlock')
                        .find('.doTab')
                        .removeClass('active');

                    $self.addClass("active");

                    if(ajaxUrl){

                        tabCache[ajaxUrl] = {
                            time : new Date().getTime(),
                            ajaxData : ajaxData,
                            html : tabHtml
                        };

                    }

                    var tabChangeFn = $selfData.tab_change_fn;
                    $.isFunction(tabChangeFn) || (tabChangeFn = $.noop);
                    tabChangeFn.call($self,tabResult);

                })
                .fail(function(error_msg){
                    // TODO 错误处理
                    console.error(error_msg);
                })
                .always(function(){
                  // FIXME loading 图 隐藏
                });

        }
    );

    $(function(){
        $('.doTab:first').click();
    });

});
