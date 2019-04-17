/* global  require : true, WCT : true, $ : true, requirejs : true  */
/*todo  测试中。。。 */
(function () {
    "use strict";
    if (!('requireOpts' in WCT)) {
        return;
    }

    var requireOpts = WCT.requireOpts,
        suffixObj = requireOpts.suffixObj,
        baseUrl = requireOpts.baseUrl,
        pageJs = requireOpts.pageJs || "";


    var buildUrl = function (fileName, suffix) {
        return baseUrl + suffix;
    };

    var paths = {};

    $.each(suffixObj, function (fileName, suffix) {
        /*处理题库提供的js examCore*/
        if(fileName == 'examCore'){
            paths[fileName] = suffix;
        }else{
            paths[fileName] = buildUrl(fileName, suffix);
        }
    });

    // 添加jquery模块
    if (window.jQuery) {
        define('jquery', function () {
            return jQuery;
        });
    }
    //todo
    console.info(paths);

    requirejs.config({
        paths: paths,
        shim: {
            "jquery": {
                exports: "jquery"
            },
            "sammy" : {
                deps: ["jquery"]
            },
            'jbox': {
                deps: ['jquery']
            },
            "examCore" : {
                deps : ['jquery','knockout']
            },
            "flexslider" : {
                deps : ['jquery']
            },
            "datetimepicker": {
                deps : ['jquery']
            }
        }
    });

    require([pageJs], $.noop);
})();
