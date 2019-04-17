/* global  require : true, PM : true, $ : true, requirejs : true  */

(function(){

    "use strict";

    if(!('requireOpts' in PM)){
        return ;
    }

    var requireOpts = PM.requireOpts,
		suffixObj = requireOpts.suffixObj,
        baseUrl = requireOpts.baseUrl,
        pluginJs = requireOpts.isPlugin,
        pluginSrc = requireOpts.pluginSrc,
        normalSrc = requireOpts.normalSrc,
        pageJs = requireOpts.pageJs || "",
        notIsExtraRequireJs = pageJs.search("isExtraRequireJs") === -1;

    if(!(pageJs in suffixObj) && notIsExtraRequireJs){
        log(
            {
                errMsg: ['未能找到对应的与 %s,相匹配的requirejs模板 ', pageJs],
                op    : "requireModule"
            },
            "error"
        );
        return ;
    }

    var buildUrl = function(fileName, suffix, isNotPlugin){
            var publicSrc = isNotPlugin ? normalSrc :  pluginSrc;
            //suffix = fileName in jsSuffixObj ? jsSuffixObj[fileName] : "";

            return baseUrl + publicSrc + fileName + suffix;
        },
        paths = {};

    $.each(
        suffixObj,
        function(fileName, suffix){
            paths[fileName] = buildUrl(fileName, suffix, $.inArray(fileName, pluginJs) === -1);
        }
    );

    var shim = {};
    if("studyTrack" === pageJs){
        var chartDef = ["chart/Chart.Core"];

        shim = {
            "chart/Chart.PolarArea" : chartDef,
            "chart/Chart.Doughnut" : chartDef,
            "chart/Chart.Line" : chartDef,
            "chart/Chart.Bar" : chartDef
        }
    }

    // 添加jquery模块
    if(window.jQuery){
        define('jquery', function(){
            return jQuery;
        })
    }

    requirejs.config({
        shim : shim,
        paths :  paths
    });

    notIsExtraRequireJs && require([pageJs], $.noop);

})();
