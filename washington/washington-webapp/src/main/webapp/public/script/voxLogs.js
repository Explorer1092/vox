/*
 * @datetime : 2016-05-31
 * @author : yifei.peng
 * @rely : null
 * */
(function(window){
    "use strict";
    var $WIN = window;

    if(typeof $WIN === "undefined"){
        $WIN = {};
    }

    function _info(msg){
        if( location.host.indexOf("test") > -1 || location.host.indexOf("staging") > -1 || location.host.indexOf("localhost") > -1 ){
            console.info(msg);
        }
    }

    function _extend(child, parent){
        var $key;
        for($key in parent){
            if(parent.hasOwnProperty($key)){
                child[$key] = parent[$key];
            }
        }
    }

    function _getCookie(name){
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if(arr=document.cookie.match(reg))
            return unescape(arr[2]);
        else
            return null;
    }

    function _setCookie(name, value, day) {
        var Days = day || 1;
        var exp = new Date();
        exp.setTime(exp.getTime() + Days * 24 * 60 * 60 * 1000);
        document.cookie = name + "=" + escape(value) + ";expires=" + exp.toGMTString();
    }

    //Get Query
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    //获取App版本
    function getAppVersion(){
        var native_version = "";

        if(window["external"] && window.external["getInitParams"] ){
            var $params = window.external.getInitParams();

            if($params){
                $params = eval("(" + $params + ")");

                native_version = $params.native_version;
            }
        }else if(getQueryString("app_version")){
            native_version = getQueryString("app_version") || "";
        }
        return native_version;
    }

    function json2str(o) {
        var arr = [];
        var str = function(s) {
            if (typeof s == 'object' && s != null) return json2str(s);
            return /^(string|number)$/.test(typeof s) ? '"' + s + '"' : s;
        };
        for (var i in o) arr.push('"' + i + '":' + str(o[i]));
        return '{' + arr.join(',') + '}';
    }

    function voxLogs(){
        var pathName = $WIN.location.pathname;
        var appName = pathName.split("/");
        var $child = {
            "dataType": 'vox_logs',
            "database" : "web_student_logs",
            "_l" : 3,
            "userId": _getCookie("uid"),
            "auth"  : typeof $uper == "undefined" ? false : $uper.userAuth,   // 是否认证用户
            "app"   : appName[1] || pathName,
            "module": appName[1] || pathName,
            "op"    : "Load",
            "subject" : typeof $uper == "undefined" ? false : $uper.subject.key,//学科
            "userAgent" : $WIN.navigator.appVersion,
            "appVersion" : getAppVersion(),
            "target": pathName,
            "referrer" : document.referrer || "",
            "aid" : _getCookie("_aid") || 0 //广告ID来源
        };

        _extend($child, arguments[0]);

        //App logs
        if(window["external"] && window.external["log_b"] ){
            window.external["log_b"]('App-h5', json2str($child));
            return false;
        }
        
        if(json2str($child).length > 2000 && $){
            $.post("//log.17zuoye.cn/log", {
                _c: $child.dataType + ':' + $child.database,
                _l: $child._l,
                _log: encodeURIComponent( json2str($child) ),
                _t: new Date().getTime()
            }, function(data){});
        }else{
            var url = '//log.17zuoye.cn/log?_c='+ $child.dataType +':' + $child.database + '&_l='+ $child._l +'&_log=' + encodeURIComponent( json2str($child) ) + '&_t=' + new Date().getTime();

            var logImage = document.createElement('img');
            logImage.style.display = "none";
            logImage.src = url;

            document.getElementsByTagName("html")[0].appendChild(logImage);
        }

        _info($child);
    }

    //设置广告来源ID
    if(getQueryString("_aid") && getQueryString("_aid") != ""){
        _setCookie("_aid", getQueryString("_aid"));
    }

    if(typeof $WIN['YQ'] === 'undefined'){
        $WIN.YQ = {
            voxLogs : voxLogs
        };
    }else{
        _extend($WIN.YQ, {
            voxLogs : voxLogs
        });
    }

    if(typeof define === 'function' && define.amd){
        define([], function () {
            'use strict';
            return voxLogs;
        });
    }else if(typeof module !== 'undefined'){
        module.exports = voxLogs;
    }
}(window));