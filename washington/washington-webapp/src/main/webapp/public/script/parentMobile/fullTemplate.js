/* global define : true, $ : true, log : true */
/**
 *  @date 2015/9/10
 *  @auto liluwei
 *  @description 该模块功能：  根据ajax获取的Date以及Ant模版字符串， 生成html字符串。 填充到制定Dom
 */

define(['ajax', 'template'], function( ioPromise, template){

    'use strict';

    /**
     * @param url  String ajax url;
     * @param ajaxData Object ajax 所需的参数
     * @param targetDomId String  生成的模版 填充到那？那个Dom的id
     * @param templateId String  模版Dom id   默认是   targeDomId + 'Temp'
     */

    var fullTemplateByAjax = function(url, ajaxData, targetDomId, templateId){

        var dfd = $.Deferred(),
            errorFn = function(error){
                log(
                    {
                        errMsg : [error],
                        op : "fullTemplate"
                    },
                    "error"
                );
            };

        dfd
        .fail(errorFn);

        if(!url){
            return dfd.reject('填充模版功能 必须制定ajax url');
        }

        $.isPlainObject(ajaxData) || (ajaxData = {});

        var $targetDom = $('#' + targetDomId);

        if( $targetDom.length === 0 ){
            dfd.reject('填充模版功能 targetDomId 找不到制定Dom targetDomId : ' + targetDomId);
            return dfd;
        }

        templateId || (templateId = targetDomId + 'Temp');

        var $templateStrDom = $('#' + templateId);

        if( $templateStrDom.length === 0 ){
            dfd.reject('填充模版功能 参数templateId 找不到制定Dom templateId : ' + templateId);
            return dfd;
        }

        return ioPromise(
                    url,
                    ajaxData
                ).
                done(function(getTemplateDataResult){
                    console.log(getTemplateDataResult);
                    if(getTemplateDataResult.success === false){
                        errorFn('填充模版功能 获取模版data 出错 出错信息: \n' + getTemplateDataResult.info);
                        return ;
                    }

                    var templateData= $.extend(
                        {},
                        getTemplateDataResult,
                        {
                           window : window
                        }
                    );
                    $targetDom.html(
                        template(
                            null,
                            $templateStrDom.html()
                        )(
                            templateData
                        )
                    );
                });

    };

    /**
     * @description 给定一个模版数据数组，一个一个 填充模版.
     * @param templateDate Array 一个数组存放着填充模版的数据
     * @param callback Function 全部执行完毕 执行的操作
     */
    var seqFullTemplate = function(templateDataArray, callback){

        $.isFunction(callback) || (callback = $.noop);

        if(!$.isArray(templateDataArray)){
            callback([
                {
                    msg : "填充模板方法只接受数组类型的数据"
                }
            ]);

            return ;
        }

        $.isArray(templateDataArray[0]) || (templateDataArray = [templateDataArray]);

        var index = -1,
            count = templateDataArray.length,
            errors = [];

        var doFullTemplateByAjax = function(){

            if(++index >= count){
                return callback(errors);
            }

            var templateData = templateDataArray[index];

            fullTemplateByAjax
                .apply(window, templateData)
                .fail(function(error){
                    errors.push(
                        {
                            index : index,
                            templateData : templateData,
                            msg : error
                        }
                    );
                })
                .always(doFullTemplateByAjax);
        };

        doFullTemplateByAjax();

    };

    seqFullTemplate.template = template;

    return seqFullTemplate;

});
