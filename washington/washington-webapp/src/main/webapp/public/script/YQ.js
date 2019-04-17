(function(root){
    var YQ = {};

    YQ.extend = function(child, parent){
        for(var k in parent){
            if(parent.hasOwnProperty(k)){
                child[k] = parent[k];
            }
        }
    };

    YQ.setCookie =  function(name, value, day){
        var Days = day || 1;
        var exp = new Date();
        exp.setTime(exp.getTime() + Days * 24 * 60 * 60 * 1000);
        document.cookie = name + "=" + escape(value) + ";expires=" + exp.toGMTString();
    };

    YQ.getCookie = function(name){
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if(arr=document.cookie.match(reg))
            return unescape(arr[2]);
        else
            return null;
    };

    YQ.getQuery =  function(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    };

    YQ.getExternal = function (){
        var _WIN = root;
        if(_WIN['yqexternal']){
            return _WIN.yqexternal;
        }else if(_WIN['external']){
            return _WIN.external;
        }else{
            return _WIN.external = function(){};
        }
    };

    //ali image press
    YQ.pressImage = function(link, w){
        var defW = 200;
        if(w){
            defW = w;
        }

        if(link && link != "" && link.indexOf('oss-image.17zuoye.com') > -1 ){
            return link + '@' + defW + 'w_1o_75q';
        }else{
            return link;
        }
    };

    YQ.eventLogs = function(opt){
        if(YQ['voxLogs']){
            if(typeof opt === "string"){
                YQ.voxLogs({
                    module: opt,
                    op: arguments[1]
                });
            }else if(typeof opt === 'function'){
                YQ.voxLogs(opt);
            }else{
                //type error;
            }
        }else{
            //voxLogs not exist;
        }
    };

    YQ.updateNativeTitle = function (title, txtColor, bgColor) {

        /**
         * title：string，文本内容
         * txtColor：string，文字颜色代码（6位hex string，如"ffff00"）
         * bgColor：string，背景颜色代码（hex string）
         */

        title = title || '一起作业';
        txtColor = txtColor || '494a4a';
        bgColor = bgColor || 'ffffff';

        if (YQ.getExternal()["updateTitle"]) {
            YQ.getExternal().updateTitle(title, txtColor, bgColor);
        }
    };

    YQ.getAppVersion = function () {
        var native_version = "2.5.0";
        if (YQ.getExternal()["getInitParams"]) {
            var $params = YQ.getExternal().getInitParams();
            if ($params) {
                $params = eval("(" + $params + ")");
                native_version = $params.native_version;
            }
        } else if (YQ.getQuery("app_version")) {
            native_version = YQ.getQuery("app_version") || "";
        }
        return native_version;
    };

    YQ.isBlank = function (str){
        return typeof str == 'undefined' || String(str) == 'null' || str == "";
    };
    //验证是否中文字符
    YQ.isCnString = function (value) {
        if(!value) return false;
        var req = /^[\u2E80-\uFE4F]+$/;
        value = value.replace(/\s+/g, "");
        return req.test(value);
    };
    //验证是否中文字符（带间隔符）
    YQ.isChinaString = function (value) {
        if(!value) return false;
        var req = /^[\u2E80-\uFE4F]+([·•][\u2E80-\uFE4F]+)*$/;
        value = value.replace(/\s+/g, "");
        return req.test(value);
    };
    //验证是否数字
    YQ.isNumber = function (value) {
        var reg = /^[0-9]+$/;
        if(YQ.isBlank(value) || !reg.test(value)){
            return false;
        }
        return true;
    };
    /**
     * 打开游戏
     * @param app ==> {
     *      appKey: '',
     *      fromFairyland: true  //默认true
     * }
     */
    YQ.openGameApp = function (app) {
        app.fromFairyland = YQ.isBlank(app.fromFairyland.toString()) ? true : app.fromFairyland;
        var url = '';
        if (app.appKey == 'Arithmetic') {
            //速算脑力王
            if(YQ.getExternal()["innerJump"]){
                YQ.getExternal()["innerJump"](JSON.stringify({name: 'arithmetic', page_viewable: true}));
            }
        } else {
            //1.打开阿分提
            //fix 兼容ios和Android 实现pageQueueNew和openFairylandPage的差异。#38664
            var externalOpenAfentiName = (app && app.fromFairyland) ? 'pageQueueNew' : 'openFairylandPage';

            if (app.appKey.indexOf('Afenti') != -1) {
                url = window.location.origin + '/app/redirect/selfApp.vpage?appKey=' + app.appKey + '&platform=STUDENT_APP&productType=APPS';
                if(YQ.getExternal()[externalOpenAfentiName]){
                    YQ.getExternal()[externalOpenAfentiName](JSON.stringify({
                        url: url,
                        name: "fairyland_app:" + app.appKey,
                        useNewCore: "crossWalk",
                        orientation: "portrait"
                    }));
                }
            }
            //2.走遍美国 TODO

        }
    };

    if(typeof root === "undefined"){
        root = {};
    }

    if(typeof root['YQ'] === 'undefined'){
        root.YQ = YQ;
    }else{
        YQ.extend(root.YQ, YQ);
    }

    if(typeof define === 'function' && define.amd){
        define([], function () {
            'use strict';
            return root.YQ;
        });
    }else if(typeof module !== 'undefined'){
        module.exports = root.YQ;
    }
}(window));