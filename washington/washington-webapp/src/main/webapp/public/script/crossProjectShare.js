/*
 * 此文件用于跨项目共享的代码
 * author: 吕小豹
 * date:2015年10月20日
 * */
(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery'], factory);
    } else if (typeof exports === 'object') {
        // CMD, CommonJS之类的
        module.exports = factory(require('jquery'));
    } else {
        // 浏览器全局变量(root 即 window)
        root.YQplayer = factory(root.$);
    }
}(this, function ($) {
    var toJSON = typeof JSON === 'object' && JSON.stringify ? JSON.stringify : $.toJSON;
    function voxLog(){
        //调用方式[$17.voxLog({}, "student") 第一参数如不增加,值全部默认. 第二参数为角色类型,如不增加默认为teacher.
        var pathName = window.location.pathname;
        var appName = pathName.split("/");
        var roleType = arguments[1] || "teacher";

        var tempObj = {
            "userId": ($.cookie ? $.cookie('uid') : ''),
            "auth"  : typeof $uper == "undefined" ? false : $uper.userAuth,   // 是否认证用户
            "app"   : appName[1] || pathName,
            "module": appName[1] || pathName,
            "op"    : "Load",
            "subject":typeof $uper == "undefined" ? false : $uper.subject.key,
            "target": pathName
        };

        $.extend(tempObj, arguments[0]);

        var url = '//log.17zuoye.cn/log?_c=vox_logs:web_' + roleType + '_logs&_l=3&_log=' + encodeURIComponent(toJSON(tempObj)) + '&_t=' + new Date().getTime();

        $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));

        return false;
    }
    //检查流量劫持
    //白名单，只登记关键字。采用宽松的策略，不误杀。凡是属性能匹配到关键字的，都认为合法。
    var iframeWhiteList = [
        'upload',
        'WeixinJSBridge',
        'jiathis',
        'proxy',
        'ueditor',
        'vox17zuoyeIframe'
    ];
    function inWhiteList(html, list){
        if(!html)return;
        var result = false;
        for(var i=0; i<list.length; i++){
            if(html.indexOf(list[i])>-1){
                result = true;
                break;
            }
        }
        return result;
    }
    function checkTH(){
        //检查非法iframe
        $('iframe').each(function(index, element){
            if(inWhiteList(element.outerHTML, iframeWhiteList))return;
            var el = $(element);
            if(el.attr('yqif')!==''){
                el.css({width:0, height:0, border:0, position:'absolute', display:'none'});
                voxLog({
                    app: 'student',
                    module: 'thcheck',
                    op: 'illegalIframe',
                    html: element.outerHTML.replace(/</g, '&lt;').replace(/>/g, '&gt;')
                }, 'student');
            }
        });
    }
    checkTH();
    setTimeout(checkTH, 1000);
    setTimeout(checkTH, 5000);
}));
