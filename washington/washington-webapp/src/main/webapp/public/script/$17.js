/**
 * $17 定义及基本功能
 */
;
(function(){
    "use strict";

    var version = "3.1.0";

    //元素选择器，只用于屏蔽
    function $17(element){
        if(arguments.length > 1){
            for(var i = 0, elements = [], length = arguments.length; i < length; i++){
                elements.push($17(arguments[i]));
            }
            return elements;
        }

        if(Object.prototype.toString.call(element) == "[object String]"){
            element = document.getElementById(element);
        }
        return element;
    }

    //用于扩展当前对象
    //第一个参数：要扩展的对象，第二个参数：参考的属性
    function extend(child, parent){
        var key;
        for(key in parent){
            if(parent.hasOwnProperty(key)){
                child[key] = parent[key];
            }
        }
    }

    //扩展原型对象
    //第一个参数：要扩展的原型对象，第二个参数：参考的属性
    function include(child, parent){
        var key;
        for(key in parent){
            if(parent.hasOwnProperty(key)){
                child.prototype[key] = parent[key];
            }
        }
    }

    var Life = function(){
        var life;
        life = function(dady) {
            extend(this, dady);
            this.init.apply(this, arguments);
            return false;
        };
        life.prototype.init = function() {};
        life.prototype.parent = life;
        life.prototype.extend = function(dady) {
            var key;
            for (key in dady) {
                if (dady.hasOwnProperty(key)) {
                    this[key] = dady[key];
                }
            }
            return this;
        };
        life.include = function(dady) {
            var key;
            for (key in dady) {
                if (dady.hasOwnProperty(key)) {
                    this.prototype[key] = dady[key];
                }
            }
            return this;
        };
        life.extend = function(dady) {
            var key;
            for (key in dady) {
                if (dady.hasOwnProperty(key)) {
                    this[key] = dady[key];
                }
            }
            return this;
        };
        return life;
    };

    extend(window, {
        $17: $17
    });

    extend(window.$17, {
        version: version,
        extend : extend,
        include: include,
        Life   : Life,
        Model  : new Life()
    });
}());


(function(){
    var f = jQuery.ajax;

    function shtml2dat(s){
        return s.replace(/^([^?]+)\.vpage(.*)$/, '$1.api$2');
    }

    jQuery.ajax = function(url, opts){
        if(typeof url === "object"){
            opts = url;
            url = undefined;
        }
        opts = opts || {};
        if(url) opts.url = url;
        if(opts.url){
            //只对 17zuoye 网站启用这个机制
            if(opts.url.indexOf('://') != -1){
                if(/^http:\/\/[^/]*17zuoye[^/]*/.test(opts.url)){
                    opts.url = shtml2dat(opts.url);
                }
            }else{
                opts.url = shtml2dat(opts.url);
            }
        }
        return f.call(this, opts);
    }
})();

/**
 * 小工具
 */
(function($17){
    "use strict";

    //生成命名空间
    //第一个参数：检查对象是否存在，不存在就初始化成 Object 对象
    //第二个参数[可选]：检查对象是否存在，不存在就用第二个参数初始化第一个对象值
    function namespace(){
        var space = arguments[0];
        var str = "window.";
        space = space.split(".");
        for(var i = 0, len = space.length; i < len; i++){
            str += space[i];

            if(i == len - 1 && arguments.length == 2){
                eval("if(!" + str + "){ " + str + " = '" + arguments[1] + "';}");
            }else{
                eval("if(!" + str + "){ " + str + " = {};}");
            }

            str += ".";
        }
        return true;
    }

    //代理函数
    function proxy(fun){
        var self = this;
        return (function(){
            return fun.apply(self, arguments);
        });
    }

    //辅助生成 GUID 编号
    function guid(format){
        return format.toLowerCase().replace(/[xy]/g, function(c){
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        }).toUpperCase();
    }

    //获得地址栏参数
    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    //获得地址栏参数
    function getQueryParams(){
        var p = {};
        var query = window.location.search.substring(1);
        var vars = query.split('&');
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split('=', 2);
            if(pair.length != 2 || !pair[0]) continue;
            p[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
        }
        return p;
    }

    //获得地址栏参数
    function getBaseLocation(){
        var p = window.location.href.indexOf('?');
        return (p == -1) ? window.location.href : window.location.href.substr(0, p);
    }

    //获得 Hash 参数
    function getHashQuery(item){
        var svalue = location.hash.match(new RegExp('[\#\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    //替换所有符合要求的字符串
    //第一个参数：需要检查的字符串
    //第二个参数：需要替换的字符串
    //第三个参数：用于替换的字符串
    function replaceAll(target, str1, str2){
        return target.replace(new RegExp(str1, "gm"), str2);
    }

    function listSort(arr, field, order){
        var refer = [];
        var result = [];
        var order = order == 'asc' ? 'asc' : 'desc';
        var index = null;
        for(var i = 0; i < arr.length; i++){
            refer[i] = arr[i][field] + ':' + i;
        }

        refer.sort();
        if(order == "desc"){
            refer.reverse();
        }

        for(i = 0; i < refer.length; i++){
            index = refer[i].split(':');
            index = index[index.length - 1];
            result[i] = arr[index];
        }
        return result;
    }

    //补位函数
    //第一个参数：需要补位的字符串
    //第二个参数：用于补位的字符串
    //第三个参数：补位后字符串长度
    //第四个参数[可选]：补位位置
    function strPad(str, padStr, padLength, position){
        var i = 0;
        var s = "";

        while(i != padLength){
            s += padStr.toString();
            i++;
        }

        position = position || "l";

        str = position == "l" ? s.concat(str) : str.concat(s);
        return position == "l" ? str.substring(str.length - padLength, str.length) : str.substring(0, padLength);
    }

    function setCookieOneDay(name, value, day){
        var date = new Date();
        date.setTime(date.getTime() + ((day ? day : 1) * 24 * 60 * 60 * 1000));
        $.cookie(name, value ? value : '', {path: '/', expires: date});
    }

    function getCookieWithDefault(name){
        var value = $.cookie(name);
        return value ? value : '';
    }

    //MVC － Model & Controller 没有 view 部分
    function Model(param){
        var key;
        for(key in param){
            if(param.hasOwnProperty(key)){
                this[key] = param[key];
            }
        }
        return this;
    }

    function getSMSVerifyCode($this, data, second){
        var timerCount;
        var timer;
        second = second ? second : 60;

        $this.addClass("btn_disable");
        if(data.success){
            timerCount = second;
        }else{
            timerCount = data.timer || null;
            if(timerCount == null){
                $this.removeClass("btn_disable");
                return false;
            }
        }

        timer = $.timer(function(){
            if(timerCount <= 0){
                $this.removeClass("btn_disable");
                $this.find("span, strong").html("免费获取短信验证码");
                $this.siblings(".init, .hint, .msgInfo").html("");
                timerCount = second;
                timer.stop();
            }else{
                $this.find("span, strong").html(--timerCount + "秒之后可重新发送");
            }
        });
        timer.set({ time: 1000});
        timer.play();
    }

    function detectZoom(){
        var ra = 0;
        var screen = window.screen;
        var ua = navigator.userAgent.toLowerCase();
        if(~ua.indexOf('firefox')){
            if(window.devicePixelRatio !== undefined){
                ra = window.devicePixelRatio;
            }
        }else if(~ua.indexOf('msie') || ~ua.indexOf('rident/')){  //IE11: Trident/7.0
            if(screen.deviceXDPI && screen.logicalXDPI){
                ra = screen.deviceXDPI / screen.logicalXDPI;
            }
        }else if(window.outerWidth !== undefined && window.innerWidth !== undefined){
            ra = window.outerWidth / window.innerWidth;
        }

        if(ra){
            ra = Math.round(ra * 100);
        }

        //360浏览器
        if(ra !== 100 && ra >= 95 && ra <= 105){
            ra = 100;
        }

        return ra;
    }

    //获取当月总天数
    function getMonthTotalDay(year, month, day){
        /*yeah：设置年，month: 设置月，day : 设置某天*/
        var date = new Date();
        var def = new Date(year ? year : date.getFullYear(), month ? month : date.getMonth() + 1, day ? day : 0);
        return def.getDate();
    }

    $17.extend($17, {
        proxy               : proxy,
        namespace           : namespace,
        guid                : guid,
        getBaseLocation     : getBaseLocation,
        getQuery            : getQuery,
        getQueryParams      : getQueryParams,
        getHashQuery        : getHashQuery,
        replaceAll          : replaceAll,
        listSort            : listSort,
        strPad              : strPad,
        setCookieOneDay     : setCookieOneDay,
        getCookieWithDefault: getCookieWithDefault,
        getSMSVerifyCode    : getSMSVerifyCode,
        getMonthTotalDay    : getMonthTotalDay,
        detectZoom          : detectZoom
    });
}($17));

/**
 * 验证函数
 */
(function($17){
    "use strict";

    //验证是否数字
    function isNumber(value){
        var reg = /^[0-9]+$/;
        if($17.isBlank(value) || !reg.test(value)){
            return false;
        }
        return true;
    }

    //新的验证数字，待验证
    function isNumberNew(value){
        return value != "" && isFinite(value);
    }

    //验证是否未定义或null或空字符串
    function isBlank(str){
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    //验证是否邮政编码
    function isZipCode(value){
        var req = /^[0-9]{6}$/;
        if($17.isBlank(value) || !req.test(value)){
            return false;
        }
        return true;
    }

    //验证是否中文字符
    function isCnString(value){
        if(!value) return false;
        //var req = /^[\u4e00-\u9fa5]+$/;
        //var req = /^[\u4e00-\u9fff]+$/;
        var req = /^[\u2E80-\uFE4F]+$/;

        value = value.replace(/\s+/g, "");
        return req.test(value);
    }
    
    //验证是否中文字符（带间隔符）
    function isChinaString(value) {
        if(!value) return false;
        var req = /^[\u2E80-\uFE4F]+([·•][\u2E80-\uFE4F]+)*$/;
        value = value.replace(/\s+/g, "");
        return req.test(value);
    }

    //验证是否中文字符（带间隔符）
    function isChinaString(value) {
        if(!value) return false;
        var req = /^[\u2E80-\uFE4F]+([·•][\u2E80-\uFE4F]+)*$/;
        value = value.replace(/\s+/g, "");
        return req.test(value);
    }

    //验证中文字符，只验证过滤前后空格的字符，字符串中间含空格或含标点符号的返回false
    function isValidCnName(value) {
        if(!value) return false;
        value = $.trim(value);
        // 标点符号
        var req = /[\u3002|\uFF1F|\uFF01|\uFF0C|\u3001|\uFF1B|\uFF1A|\u300C|\u300D|\u300E|\u300F|\u2018|\u2019|\u201C|\u201D|\uFF08|\uFF09|\u3014|\u3015|\u3010|\u3011|\u2014|\u2026|\u2013|\uFF0E|\u300A|\u300B|\u3008|\u3009]+/;
        if (req.test(value)) {
            return false;
        }
        req = /^[\u2E80-\uFE4F]+$/;
        return req.test(value);
    }

    //验证是否手机号
    function isMobile(value){
        value = value + "";
        //严格判定
        var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
        //简单判定
        var reg = /^1[0-9]{10}$/;
        if(!value || value.length != 11 || !reg.test(value)){
            return false;
        }
        return true;
    }

    //验证是否邮箱
    function isEmail(value){
        var req = /^[-_.A-Za-z0-9]+@[-_.A-Za-z0-9]+(\.[-_.A-Za-z0-9]+)+$/;
        return value && req.test(value);
    }

    //验证是否是函数(jQuery的isFunction在IE6下有Bug，建议用这个函数)
    function isFunction(func){
        return !!func && !func.nodeName && func.constructor != String && func.constructor != RegExp && func.constructor != Array && /function/i.test(func + "");
    }

    $17.extend($17, {
        isNumber                : isNumber,
        isBlank                 : isBlank,
        isZipCode               : isZipCode,
        isCnString              : isCnString,
        isChinaString           : isChinaString,
        isMobile                : isMobile,
        isEmail                 : isEmail,
        isFunction              : isFunction,
        isValidCnName           : isValidCnName
    });
}($17));

/**
 * 日期相关方法
 */
(function($17){
    "use strict";

    var formats = {
        s: function(date){
            return $17.strPad(date.getSeconds(), "0", 2);
        },

        m: function(date){
            return $17.strPad(date.getMinutes(), "0", 2);
        },

        h: function(date){
            return $17.strPad(date.getHours(), "0", 2);
        },

        d: function(date){
            return $17.strPad(date.getDate(), "0", 2);
        },

        M: function(date){
            return $17.strPad(date.getMonth() + 1, "0", 2);
        },

        y: function(date){
            return $17.strPad(date.getYear() % 100, "0", 2);
        },

        Y: function(date){
            return date.getFullYear();
        },

        w: function(date){
            return date.getDay();
        },

        W: function(date){
            var _week = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"];
            return _week[date.getDay()];
        }
    };

    function _strftime(_format, diff, type, _date_){
        var _date = _date_ == null ? new Date() : _date_;
        switch(type){
            case "Y":
            case "y":
                _date.setFullYear(_date.getFullYear() + diff);
                break;
            case "M":
                _date.setMonth(_date.getMonth() + diff);
                break;
            case "D":
            case "d":
                _date.setDate(_date.getDate() + diff);
                break;
            case "H":
            case "h":
                _date.setHours(_date.getHours() + diff);
                break;
            case "m":
                _date.setMinutes(_date.getMinutes() + diff);
                break;
            case "S":
            case "s":
                _date.setSeconds(_date.getSeconds() + diff);
                break;
            case "W":
            case "w":
                _date.setDate(_date.getDate() + diff * 7);
                break;
        }

        return (_format + "").replace(/%([a-zA-Z])/g, function(m, f){
            var formatter = formats && formats[f];

            switch(typeof formatter){
                case "function":
                    return formatter.call(formats, _date);
                case "string":
                    return _strftime(formatter, date);
            }

            return f;
        });
    }

    //无参数：返回 "%Y-%M-%d" 格式的当前日期时间
    //一个参数：指定格式的当前日期时间
    //二个参数：
    //   第一个参数：返回日期时间格式
    //   第二个参数：与当天的所差天数
    //三个参数：
    //   第一个参数：返回日期时间格式
    //   第二个参数：第三个参数指定的单位所差值
    //   第三个参数：制定第二个参数的单位 w d h m s
    //四个参数：
    //   第一个参数：返回日期时间格式
    //   第二个参数：第三个参数指定的单位所差值
    //   第三个参数：指定第二个参数的单位 w d h m s
    //   第四个参数：指定要返回的日期
    function dateUtils(){
        switch(arguments.length){
            case 0:
                return _strftime("%Y-%M-%d", 0, "d", null);
            case 1:
                return _strftime(arguments[0], 0, "d", null);
            case 2:
                return _strftime(arguments[0], arguments[1], "d", null);
            case 3:
                return _strftime(arguments[0], arguments[1], arguments[2], null);
            case 4:
                return _strftime(arguments[0], arguments[1], arguments[2], arguments[3]);
            default:
                return _strftime("%Y-%M-%d");
        }
    }

    //时间对比函数
    //第一个参数：开始时间
    //第二个参数：结束时间
    //第三个参数：要得到差异的单位
    //第四个参数[可选。第三个为timer时]：返回的计时格式
    //第五个参数[可选。第四个参数包括日期属性时]：日期格式化长度
    function dateDiff(start, end, type, format, dayLength){
        var startDate = $17.strPad(start, "0", 20, "r");
        var endDate = $17.strPad(end, "0", 20, "r");
        var diff = null;
        startDate = new Date(startDate.substring(0, 4), ~~startDate.substring(5, 7) - 1, startDate.substring(8, 10), startDate.substring(11, 13), startDate.substring(14, 16), startDate.substring(17, 19));
        endDate = new Date(endDate.substring(0, 4), ~~endDate.substring(5, 7) - 1, endDate.substring(8, 10), endDate.substring(11, 13), endDate.substring(14, 16), endDate.substring(17, 19));
        diff = Date.parse(endDate) - Date.parse(startDate);
        format = format || "%d %h:%m:%s";
        dayLength = dayLength || 0;
        switch(type){
            case "W":
            case "w":
                return Math.floor(diff / (7 * 24 * 60 * 60 * 1000));
            case "D":
            case "d":
                return Math.floor(diff / (24 * 60 * 60 * 1000));
            case "H":
            case "h":
                return Math.floor(diff / (60 * 60 * 1000));
            case "m":
                return Math.floor(diff / (60 * 1000));
            case "S":
            case "s":
                return Math.floor(diff / 1000);
            case "timer":
                format = format.replace(/%d/g, dayLength == 0 ? Math.floor(diff / (24 * 60 * 60 * 1000)) : $17.strPad(Math.floor(diff / (24 * 60 * 60 * 1000)), "0", dayLength));
                format = format.replace(/%h/g, $17.strPad(Math.floor(diff / (60 * 60 * 1000)) % 24, "0", 2));
                format = format.replace(/%m/g, $17.strPad(Math.floor(diff / (60 * 1000)) % 60, "0", 2));
                format = format.replace(/%s/g, $17.strPad(Math.floor(diff / 1000) % 60, "0", 2));
                return format;
            default:
                return null;
        }
    }

    //yyyy-MM-dd hh:mm:ss格式
    function myDate(info){
        var info = info.split(/:|-|\s/g);
        return new Date(info[0], ~~info[1] - 1, info[2], info[3], info[4], info[5]);
    }

    /*
     将 Date 转化为指定格式的String
     月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
     年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
     $17.dateToString(new Date(),"yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
     $17.dateToString(new Date(),"yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
     */
    function dateToString(datetime,pattern){
        var date,fmt = pattern;
        if(typeof datetime === "number"){
            date = new Date(datetime);
        }else if(datetime && datetime instanceof Date){
            date = datetime;
        }
        if(!date){
            throw "datetime参数类型只能为数字或Date类型";
        }

        var o = {
            "M+": date.getMonth() + 1,                 //月份
            "d+": date.getDate(),                    //日
            "h+": date.getHours(),                   //小时
            "m+": date.getMinutes(),                 //分
            "s+": date.getSeconds(),                 //秒
            "q+": Math.floor((date.getMonth() + 3) / 3), //季度
            "S": date.getMilliseconds()             //毫秒
        };
        if (/(y+)/.test(fmt))
            fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }

    $17.extend($17, {
        DateUtils: dateUtils,
        DateDiff : dateDiff,
        Date     : myDate,
        dateToString : dateToString
    });


}($17));


////////////////// 扩展框架 //////////////////

/**
 * Hash回调
 */
(function($17){
    "use strict";

    function hash(name, value){
        if($17.isBlank(name)){
            return;
        }

        var clearReg = new RegExp("(&" + name + "=[^&]*)|(\\b" + name + "=[^&]*&)|(\\b" + name + "=[^&]*)", "ig");
        if(value === null){
            location.hash = location.hash.replace(clearReg, "");
        }else{
            value = value + "";

            var temp = location.hash.replace(clearReg, "");
            temp += ((temp.indexOf("=") != -1) ? "&" : "") + name + "=" + encodeURIComponent(value);
            location.hash = temp;
        }
    }

    function urlCallback(prams, callback){
        var time = new Date().getTime();
        var _v = null;

        for(var i = 0, len = prams.length; i < len; i++){
            _v = $17.getQuery(prams[i]);
            if(_v == ""){
                return;
            }else{
                $17.namespace("__hashCallback__" + time + "." + prams[i], _v);
            }
        }

        var callFun = $17.proxy(callback);
        callFun(eval("__hashCallback__" + time));
    }

    function hashCallback(prams, callback){
        var time = new Date().getTime();
        var _v = null;

        for(var i = 0, len = prams.length; i < len; i++){
            _v = $17.getHashQuery(prams[i]);
            if(_v == ""){
                return;
            }else{
                $17.namespace("__hashCallback__" + time + "." + prams[i], _v);
            }
            hash(prams[i], null);
        }

        hash("=^_^", "");

        var callFun = $17.proxy(callback);
        callFun(eval("__hashCallback__" + time));
    }

    $17.extend($17, {
        hashCallback: hashCallback,
        urlCallback : urlCallback
    });
}($17));

/**
 * jQuery 扩展小工具
 * 注: 依赖 'jQuery'
 */
(function($17){
    "use strict";

    jQuery.fn.getClassId = function(){
        var classId = $(this).val().toUpperCase();
        return classId.substring(0, 1) == "C" ? $.trim(classId.substring(1, classId.length)) : $.trim(classId);
    };

    function getClassId(classId){
        classId = classId.toUpperCase();
        return classId.substring(0, 1) == "C" ? $.trim(classId.substring(1, classId.length)) : $.trim(classId);
    }

    function setSelect(elem, keys, values, def){
        $(elem).html("");

        for(var i = 0, len = keys.length; i < len; i++){
            if(keys[i] == def){
                $(elem).append('<option value ="' + keys[i] + '" selected="selected">' + values[i] + '</option>');
            }else{
                $(elem).append('<option value ="' + keys[i] + '">' + values[i] + '</option>');
            }
        }
    }

    function backToTop(time){
        top.$('html, body').animate({scrollTop: '0px'}, time || 0);
    }

    jQuery.fn.backToCenter = function(time){
        top.$('html, body').animate({scrollTop: $(this).offset().top}, time || 1000);
        return this;
    };

    function promptAlert(){
        switch(arguments.length){
            case 1:
                $.prompt("<div class='w-ag-center'>" + arguments[0] + "</div>", { title: "系统提示", buttons: { "知道了": true }, position: {width: 500}});
                break;
            case 2:
                $.prompt("<div class='w-ag-center'>" + arguments[0] + "</div>", { title: "系统提示", buttons: { "知道了": true }, position: {width: 500}, submit: arguments[1], close: arguments[1]});
                break;
            case 3:
                $.prompt("<div class='w-ag-center'>" + arguments[0] + "</div>", { title: "系统提示", buttons: { "知道了": true }, position: {width: 500}, submit: arguments[1], close: arguments[2]});
                break;
        }
    }

    function wordLengthLimit(wordLen, defaultLen){
        if($17.isBlank(wordLen)){
            return false
        }
        defaultLen = $17.isBlank(defaultLen) ? 140 : defaultLen;
        var i = "<span>还可以输入" + (defaultLen - wordLen ) + "个字</span>";
        var s = "<span style='color: #ff1100'>已超出" + -(defaultLen - wordLen ) + "个字</span>";
        var t = defaultLen - wordLen < 0 ? s : i;
        return t;
    }

    /*blockUi*/
    function blockUI(message){
        message = $17.isBlank(message) ? "数据加载中..." : message;
        $.blockUI({
            css    : {
                border                 : 'none',
                padding                : '15px',
                backgroundColor        : '#fff',
                '-webkit-border-radius': '10px',
                '-moz-border-radius'   : '10px',
                'border-radius'        : '10px',
                opacity                : 0.7,
                color                  : '#000',
                baseZ                  : 2001,
                timeout                : 2000
            },
            message: '<h2><img width="40" height="40" src="/public/skin/leak/html/images/loading.gif"/>' + message + '</h2>'
        });
        setTimeout(function(){
            $.unblockUI()
        }, 5000);
    }

    function unBlockUI(){
        $.unblockUI()
    }

    //获取操作系统
    function getOperatingSystem() {
        var userAgent = navigator.userAgent || navigator.vendor || window.opera;
        if (userAgent.match(/iPad/i) || userAgent.match(/iPhone/i) || userAgent.match(/iPod/i)) {
            return 'iOS';
        }
        else if (userAgent.match(/Android/i)) {
            return 'Android';
        }
        else {
            return 'unknown';
        }
    }

    $17.extend($17, {
        setSelect      : setSelect,
        backToTop      : backToTop,
        getClassId     : getClassId,
        alert          : promptAlert,
        wordLengthLimit: wordLengthLimit,
        blockUI        : blockUI,
        unBlockUI      : unBlockUI,
        getOperatingSystem : getOperatingSystem
    });
}($17));


/**
 * 扩展剪贴板功能
 * 注: 依赖 'ZeroClipboard'
 */
(function($17){
    "use strict";

    function copyToClipboard($target, $button, $info, $info1, $callBack){
        if(window.clipboardData){
            $button.on("click", function(){

                window.clipboardData.setData("Text", $target.val());
                if($info == "copyInfo"){
                    $(".copyInfo").show().attr("is-show", 1);
                }else{
                    alert("复制成功，请使用 ctrl + v 贴到您需要的地方！");
                    if($callBack){$callBack();}
                }
                return true;
            });
        }else{
            var clip = new ZeroClipboard.Client();
            clip.setHandCursor(true);
            if($info1){
                clip.glue($info, $info1);
            }else{
                clip.glue("clip_button", "clip_container");
            }

            clip.addEventListener("mouseover", function(){
                clip.setText($target.val());
            });

            clip.addEventListener("complete", function(){
                if($info == "copyInfo"){
                    $(".copyInfo").show().attr("is-show", 1);
                }else{
                    alert("复制成功，请使用 ctrl + v 贴到您需要的地方！");
                    if($callBack){$callBack();}
                }
                return true;
            });
        }

        return false;
    }

    $17.extend($17, {
        copyToClipboard: copyToClipboard
    });
}($17));


/**
 * 错误统计
 * 注： 依赖 jQuery 框架
 */
window.onerror = function(errMsg, file, line){
    var userId = ($.cookie ? $.cookie('uid') : '');
    var useragent = (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information";
    var target = window.location.pathname;
        encodeURI(useragent);

    var url = 'http://log.17zuoye.cn/log?_c=vox_logs:js_errors_logs&_l=3&_log={"userId":"' + userId + '","errMsg":"' + errMsg + '","file":"' + file + '","line":"' + line + '","useragent":"' + useragent + '","target":"'+target+'"}';
    $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
};

/**
 * 事件代理
 * 注: 依赖 jQeury 框架
 */
(function($, $17){
    "use strict";

    function delegate(config){
        for(var item in config){
            var selecter = item.split("->")[0];
            var event = item.split("->")[1];

            $(selecter).on(event, config[item]);
        }
    }

    $17.extend($17, {
        delegate: delegate
    });
}(jQuery, $17));


/**
 * 实现下拉组建
 * 注： 依赖 jQuery 框架
 */
    $.vox || ($.vox = {});
$.vox.select = function(target, mt){
    var $self = $(target);
    var $b = $self.find("b.title");
    var $ul = $self.find("ul");
    var $focus = $ul.find(".active");

    $ul.width($self.width() + 12);
    if(mt) $ul.css("top", mt);

    $b.html($focus.text()).attr("data-value", $focus.attr("data-value"));

    $self.delegate("a", "click", function(){
        $ul.slideDown(100);
    });
    $self.delegate("li", "click", function(){
        $ul.slideUp(100);
        $focus = $(this);
        $focus.radioClass("active");
        $b.html($focus.text()).attr("data-value", $focus.attr("data-value"));
    });
    $ul.mouseleave(function(){
        $ul.slideUp(100);
    });
    return true;
};
$.vox.selectFocus = function(target, value){
    var $target = $(target);
    var $focus = $target.find("li[data-value='" + value + "']").radioClass("active");
    $target.find("b.title").attr("data-value", $focus.attr("data-value")).html($focus.text());
    return true;
};


/**
 * 实现翻页组件
 * 注： 依赖 jQuery 框架
 */
$.fn.page = function(option){
    function draw($target, def){
        if(def.model == "normal"){
            var current = parseInt(def.current);
            var maxNum = parseInt(def.maxNumber);
            var _total = parseInt(def.total);
            var _start = current - Math.floor(def.maxNumber / 2);
            var _end = current + Math.floor(def.maxNumber / 2);

            _start = _start < maxNum ? 1 : _start;
            _end = _end > _total ? _total : _end;

            $target.html('<a v="prev" href="' + def.href + '" class="' + (def.current > 1 ? def.enableMark : def.disabledMark) + ' ' + def.prev.className + '" style="' + def.prev.style + '">' + def.prev.text + '</a>');
            if(_start > 1){
                $target.append('<a href="' + def.href + '"><span>1</span></a>');
                $target.append('<span class="points"> ... </span>');
            }
            for(var i = _start; i <= _end; i++){
                $target.append('<a href="' + def.href + '" ' + (i == def.current ? ('class="' + def.currentMark + '"') : '') + '><span>' + i + '</span></a>');
            }
            if(_end < _total){
                $target.append('<span class="points"> ... </span>');
                if (def.showTotalPage)
                    $target.append('<a href="' + def.href + '"><span>' + _total + '</span></a>');
            }
            $target.append('<a v="next" href="' + def.href + '" class="' + ( _total <= 1 || def.current >= _total ? def.disabledMark : def.enableMark ) + ' ' + def.next.className + '" style="' + def.next.style + '">' + def.next.text + '</a>');
            $target.show();

            $target.find('a[class != ' + def.currentMark + '][class != ' + def.disabledMark + ']').one("click", function(){
                switch($(this).attr("v")){
                    case "prev":
                        jump($target, def, current - 1);
                        break;
                    case "next":
                        jump($target, def, current + 1);
                        break;
                    default:
                        jump($target, def, $(this).find('span').html());
                        break;
                }
            });
        }else{
            //为非常规页码，如“A，B..”，预留
        }
    }

    function jump($target, def, index){
        if(index < 1 || index > def.total){
            return false;
        }

        def.current = index;

        draw($target, def);

        if($.isFunction(def.jumpCallBack)){
            def.jumpCallBack(def.current);
        }else if(def.jumpCallBack){
            eval(def.jumpCallBack + "(options.current)");
        }
        if(def.autoBackToTop){
            $17.backToTop();
        }
    }

    return this.each(function(){
        var $target = $(this);
        var def = {
            total        : 0,
            current      : 1,
            maxNumber    : 5,
            currentMark  : "this",
            disabledMark : "disable",
            enableMark   : "enable",
            model        : "normal",
            showTotalPage: true,
            autoBackToTop: true,
            next         : {
                text     : "<span>下一页</span>",
                className: "",
                style    : ""
            },
            prev         : {
                text     : "<span>上一页</span>",
                className: "",
                style    : ""
            },
            href         : "javascript:void(0);",
            jumpCallBack : null
        };

        $.extend(def, option);

        if($target.length < 1){
            return false;
        }

        if(def.total < 1){
            $target.empty().hide();
            return false;
        }

        def.maxNumber = def.maxNumber > 5 ? def.maxNumber : 5;

        draw($target, def);
    });
};

/**
 * 实现radioClass
 * 注： 依赖 jQuery 框架
 */
(function($){
    $.fn.radioClass = function(className){
        return this.addClass(className).siblings().removeClass(className).end();
    };

    $.fn.radioOption = function(){
        return this.siblings().attr("selected", false).end().attr("selected", true);
    };
}(jQuery));


/**
 * 容错方案
 */
(function($17){
    "use strict";

    //IE8 hasOwnProperty bug 容错
    if(!window.hasOwnProperty){
        window.hasOwnProperty = Object.prototype.hasOwnProperty;
    }
    if(!document.hasOwnProperty){
        document.hasOwnProperty = Object.prototype.hasOwnProperty;
    }

    $17.extend($17, {
        config: {
            debug: false
        }
    });

    $17.namespace("console.info", "isNotFunction");
    if(console.info === "isNotFunction"){
        console.info = function(){
        };
    }

    $17.namespace("console.log", "isNotFunction");
    if(console.log === "isNotFunction"){
        console.log = function(){
        };
    }

    $17.namespace("console.dir", "isNotFunction");
    if(console.dir === "isNotFunction"){
        console.dir = function(){
        };
    }

    $17.namespace("console.error", "isNotFunction");
    if(console.error === "isNotFunction"){
        console.error = function(){
        };
    }

    $17.namespace("console.time", "isNotFunction");
    if(console.time === "isNotFunction"){
        console.time = function(){
        };
    }

    $17.namespace("console.timeEnd", "isNotFunction");
    if(console.timeEnd === "isNotFunction"){
        console.timeEnd = function(){
        };
    }

    function _info(msg){
        if($17.config.debug){
            console.info(msg);
        }
    }

    function _dir(msg){
        if($17.config.debug){
            console.dir(msg);
        }
    }

    function _log(msg){
        if($17.config.debug){
            console.log(msg);
        }
    }

    function _error(msg){
        if($17.config.debug){
            console.error(msg);
        }
    }

    //  第一个参数:正式环境参数, 第二个参数:测试环境参数
    function setDebugValue(value, debugValue){
        return ($17.config.debug && $17.getQuery("debug") === "true") ? debugValue : value;
    }

    $17.extend($17, {
        info         : _info,
        dir          : _dir,
        log          : _log,
        error        : _error,
        sdv          : setDebugValue,
        setDebugValue: setDebugValue
    });
}($17));


/**
 * 百度统计
 */
(function(){
    function tongji(){
        //ga 统计 https://developers.google.com/analytics/devguides/collection/gajs/eventTrackerGuide
        /*值类型是否必需说明
         Category String 是一般是用户与之互动的对象（例如按钮）
         Action String 是互动的类型（例如点击）
         Label String 否可用于给事件分类（例如导航按钮）
         Value Number 否值不得为负。可用于传递计数（例如 4 次）*/
        function ga(a, b, c, d, e){}
        $17.info(arguments);
        switch (arguments.length) {
            case 1:
                ga("send", 'event', arguments[0].toString(), arguments[0].toString(), arguments[0].toString());
                break;
            case 2:
                ga("send", 'event', arguments[0].toString(), arguments[1].toString(), arguments[0] + "_" + arguments[1]);
                break;
            case 3:
                ga("send", 'event', arguments[0].toString(), arguments[1].toString(), arguments[2].toString());
                break;
        }
        return false;
    }

    // a标签上添加百度统计（只面向在本页面跳转的统计）其他统计正常使用。
    // eg：<a onclick="$17.atongji('要统计的内容','要跳转的url')" href="javascript:void (0);"></a>
    function aTongJi(tjContent, url){
        if(!$17.isBlank(tjContent)){
            $17.tongji(tjContent);
        }
        if(!$17.isBlank(url)){
            setTimeout(function(){
                location.href = url;
            }, 200);
        }
        return false;
    }

    $17.extend($17, {
        tongji : tongji,
        atongji: aTongJi
    });
}());

//获取微信二维码
(function () {
    function getQRCodeImgUrl(operation, callback) {
        //operation.campaignId: campaignId没有统计需求传0
        var url = operation.role == 'teacher' ? '/teacher/qrcode.vpage' : '/student/qrcode.vpage';
        var defaultImgUrl = operation.role == 'teacher' ? '//cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg' : '//cdn.17zuoye.com/static/project/app/publiccode_student.jpg';
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: url,
            data: "campaignId=" + operation.campaignId,
            dataType: "json",
            success: function (json) {
                var QRCodeImgUrl = '';
                if (json.success) {
                    if ($.browser.msie && parseInt($.browser.version, 10) == 6) {
                        //return QRCodeUrl = $17.replaceAll(data.qrcode_url, 'https://', 'http://');
                        QRCodeImgUrl = $17.replaceAll(json.qrcode_url, 'https://', 'http://')
                    } else {
                        QRCodeImgUrl = json.qrcode_url;
                    }
                } else {
                    QRCodeImgUrl = defaultImgUrl
                }

                if (typeof callback === 'function') callback.apply(this, [QRCodeImgUrl]);
            }
        });
    }

    //获取短链接方法
    var $_shortUrl = "", $_originalUrl = "";
    function getShortUrl(u, callback){
        if($_shortUrl != '' && $_originalUrl == u  && callback){
            callback($_shortUrl);
            return false;
        }

        $_originalUrl = u;
        $.post("/project/crt.vpage", {url : u}, function(data){
            if(data.success){
                $_shortUrl = u = data.url;
            }

            if (callback){ callback(u); }
        });
    }

    $17.extend($17, {
        getQRCodeImgUrl : getQRCodeImgUrl,
        getShortUrl : getShortUrl
    });
}());

/**
 * 云跟踪日志
 */
(function(){
    //flash游戏log （基础/应试/pk/收费产品）
    window.voxLogger = {
        log: function(v){
            if($.type(v) == 'string' && v[0] == '{'){
                v = $.evalJSON(v);
            }

            if(v.collection && v.collection != ""){
                voxLog(v, v.collection);
            }else{
                $(function(){
                    try{
                        var _ip = "";
                        if(window._17zuoye && window._17zuoye.realRemoteAddr){
                            _ip = window._17zuoye.realRemoteAddr;
                        }

                        var _baseData = {
                            _c: "vox_flash:flash",
                            _l: "info",
                            _t: new Date().getTime(),
                            _ip: _ip,
                            app: "",
                            module: "",
                            op: "",
                            userAgent: navigator.userAgent,
                            uid: $.cookie('uid')
                        };

                        $.extend(_baseData, v);

                        //以IE最少的GET 长度2083
                        if($.param(_baseData).length > 2000){
                            $.post("//log.17zuoye.cn/log", _baseData, function(data){});
                        }else{
                            var url = '//log.17zuoye.cn/log?'+ $.param(_baseData);
                            if(window._17zuoye && window._17zuoye.realRemoteAddr)
                                url += '&_ip=' + window._17zuoye.realRemoteAddr;
                            $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
                        }
                    }catch (e){
                        //error
                    }
                });
            }
        }
    };

    function traceLog(){
        var pathName = window.location.pathname;
        var appName = pathName.split("/");

        //跟踪日志初始化
        var l = {
            sys   : 'web',
            type  : 'notify',
            app   : appName[1] || pathName,
            module: appName[2] || pathName,
            op    : "Load",
            target: pathName
        };

        $17.extend(l, arguments[0]);

        window.voxLogger.log(l);

        $17.info(l);

        return false;
    }

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
            "userAgent" : window.navigator.appVersion,
            "target": pathName
        };

        $17.extend(tempObj, arguments[0]);

        $17.info(tempObj);

        var url = '//log.17zuoye.cn/log?_c=vox_logs:web_' + roleType + '_logs&_l=3&_log=' + encodeURIComponent($.toJSON(tempObj)) + '&_t=' + new Date().getTime();

        $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));

        return false;
    }


    function voxPageTimeLogs(){
        var doLog = $17.isBlank(arguments[0].isOpen) ? true : arguments[0].isOpen;
        var _param = arguments[0];
        var time = arguments[0].rt || 1000 * 30;
        if(false){
            window.onload = function(){
                var startDate = new Date();
                var uniqueId = startDate.getTime() + $17.guid('xxxxxx');
                var userId = ($.cookie ? $.cookie('uid') : '');
                var param = {};
                $17.info('页面停留时长日志分析》》》已开启');

                param = {
                    _c    : 'vox_logs:page_time_logs',
                    _l    : 2,
                    key   : uniqueId,
                    userId: userId,
                    target: location.href,
                    t     : 0

                };
                $17.extend(param, _param);
                createPageTimeLogs(param);

                setInterval(function(){
                    param.t = (new Date().getTime() - startDate.getTime()) / 1000;
                    createPageTimeLogs(param);
                    $17.info('你已在当前页面停留了' + param.t + 's');
                }, time);
            };
        }else{
            $17.info('页面停留时长日志分析》》》已关闭');
        }
        return false;
    }

    function createPageTimeLogs(param){
        var url = 'http://101.251.192.236/st_log?' + $.param(param) + '&_t=' + new Date().getTime();
        $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
    }

    $17.extend($17, {
        traceLog       : traceLog,
        voxLog         : voxLog,
        voxPageTimeLogs: voxPageTimeLogs
    });
}());

/**
 * 锁标识位工具
 */
(function($){

    var ft = {
        name    : "ice_cream",
        freezing: "freezing",
        thaw    : "thaw"
    };

    $.fn.extend({
        //冻结函数
        //无参数：给对象添加冰激淋属性，并将其冻结
        //一个参数：给对象添加指定属性，并将其冻结
        freezing  : function(){
            this.attr(arguments[0] || ft.name, ft.freezing);
            return this;
        },
        //判断是否冻结
        //无参数：判断对象的冰激淋属性是否被冻结
        //一个参数：判断指定对象是否被冻结
        isFreezing: function(){
            return this.attr(arguments[0] || ft.name) == ft.freezing;
        },
        //解冻函数
        //无参数：将对象的冰激淋属性解冻
        //一个参数：将指定属性解冻
        thaw      : function(){
            this.attr(arguments[0] || ft.name, ft.thaw);
            return this;
        },
        //判断是否解冻
        //无参数：判断冰激淋属性是否被解冻
        //一个参数：判断指定属性是否被解冻
        isThaw    : function(){
            return this.attr(arguments[0] || ft.name) == ft.thaw;
        }
    });
}(jQuery));


//购物车效果
(function($){
    "use strict";

    var option = {
        target  : null,
        position: 'absolute',
        border  : '5px #f00 solid',
        width   : 5,
        height  : 5,
        opacity : 1,
        time    : 800
    };

    $.fn.fly = function(){
        if($.type(arguments[0]) === "string"){
            option.target = $(arguments[0]);
        }else{
            $.extend(option, arguments[0]);
        }

        return this.each(function(){
            var $self = $(this);
            var left = $self.offset().left;
            var top = $self.offset().top;

            if($self.is(':animated')){
                return false;
            }

            var $newLife = $self.clone();

            $("body").append($newLife);
            $newLife.css({
                position: option.position,
                border  : option.border,
                left    : left,
                top     : top
            });
            $newLife.animate({
                width   : option.width,
                height  : option.height,
                left    : $(option.target).offset().left,
                top     : $(option.target).offset().top,
                opcacity: option.opacity
            }, option.time, function(){
                $newLife.remove();
            });
        });
    };
}(jQuery));

/*获取同步习题*/
(function($){
    /**
     * var callback = function(){
        //初始化完成
        var node = document.getElementById('examImgUrl');
        vox.exam.render(node, 'normal', {
            ids: [examId]
        });
        };
     vox.exam.create(callback)
     见 http://wiki.17zuoye.net/pages/viewpage.action?pageId=14189144
     */
    function examRender(obj) {
        var dom = obj.dom;
        var viewType = obj.viewType;
        var params = {};
        $17.extend(params,obj.custom);
        vox.exam.render(document.getElementById(dom), viewType, params);
    }
}());


//////////////////////// App库 ////////////////////

/**禁止使用缓存*/
$.ajaxSetup({ cache: false });


var App = {
    postJSON: function(url, data, callback, error, dataType){
        dataType = dataType || "json";
        if(error == null || !$.isFunction(error)){
            error = function(){
                console.info(App.config.info._404);
            };
        }
        return $.ajax({
            type       : 'post',
            url        : url,
            data       : $.toJSON(data),
            success    : callback,
            error      : error,
            dataType   : dataType,
            contentType: 'application/json;charset=UTF-8'
        });
    },
    getJSON : function(url, callback, error, dataType){
        dataType = dataType || "json";
        if(error == null || !$.isFunction(error)){
            error = function(){
                console.info(App.config.info._404);
            };
        }
        return $.ajax({
            type       : 'get',
            url        : url,
            success    : callback,
            error      : error,
            dataType   : dataType,
            contentType: 'application/json;charset=UTF-8'
        });
    },
    post    : function(url, data, callback, error){
        if($.isFunction(data)){
            callback = data;
            data = undefined;
        }
        if(error == null || !$.isFunction(error)){
            error = function(){
                console.info(App.config.info._404);
            };
        }
        return $.ajax({
            type       : 'post',
            url        : url,
            data       : data,
            success    : callback,
            error      : error,
            contentType: 'text/plain;charset=UTF-8'
        });
    },

    call: function(callback, value){
        try{
            if($.isFunction(callback)){
                callback(value);
            }else if(!$17.isBlank(callback)){
                eval(callback + "(value)");
            }
        }catch(e){
        }
    },

    parseInt: function(value, defaultValue){
        value = value || ( defaultValue || 0 );
        value = parseInt(value);
        return ( !isNaN(parseFloat(value)) && isFinite(value) ) ? value : ( defaultValue || 0 );
    },

    config: {
        sign: {
            locked     : "app_locked",
            lockedDelay: "app_unlock_delay"
        },
        info: {
            _404: "网络请求失败，请稍等重试或者联系客服人员"
        }
    },

    districtSelect: {
        installState          : 0,
        clearDistrictNextLevel: function(obj){
            if(obj.attr("next_level")){
                App.districtSelect.clearDistrictNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
            }
        },
        get                   : function(_this){
            var next_level = _this.attr("next_level");
            if(next_level){
                next_level = $("#" + next_level);
                App.districtSelect.clearDistrictNextLevel(_this);
                if($17.isBlank(_this.val())){
                    return false
                }
                $.getJSON('/getregion-' + _this.val() + '.vpage', function(data){
                    if(data.success && data.total > 0){
                        var html = '';
                        var defaultOption = next_level.attr("default_option");

                        if(!$17.isBlank(defaultOption)){
                            try{
                                defaultOption = eval("(" + defaultOption + ")");
                                html = '<option value="' + defaultOption.key + '">' + defaultOption.value + '</option>';
                            }catch(e){
                            }
                        }

                        $.each(data.rows, function(){
                            html += '<option value="' + this.key + '">' + this.value + '</option>';
                        });

                        next_level.html(html);
                        var defaultValue = next_level.attr("defaultValue");
                        if($17.isBlank(defaultValue) && !$17.isBlank(defaultOption)){
                            defaultValue = defaultOption.key;
                        }
                        if(!$17.isBlank(defaultValue)){
                            //under IE6, 'select' can not be used after change. must delay some time
                            setTimeout(function(){
                                next_level.val(defaultValue);
                                next_level.attr("defaultValue", '');
                            }, 1);
                        }
                        if(!$17.isBlank(next_level.attr("next_level"))){
                            if(!$17.isBlank(next_level.val()) && next_level.val() != "-1"){
                                //under IE6, 'select' can not be used after change. must delay some time
                                setTimeout(function(){
                                    next_level.trigger('change');
                                }, 5);
                            }
                        }
                        App.call(next_level.attr("success_callback"), next_level);
                    }else{
                        if($17.isBlank(next_level.attr("show_error")) || next_level.attr("show_error") == "true"){
                            alert(data.info);
                        }
                        App.call(next_level.attr("error_callback"), next_level);
                    }
                });
            }
        },
        install               : function(obj){
            if(App.districtSelect.installState == 1) return;
            obj = obj || $("select.district_select");
            obj.live("change", function(){
                App.districtSelect.get($(this));
            });
            App.districtSelect.installState = 1;
            return App.districtSelect;
        },
        init                  : function(obj){
            if(App.districtSelect.installState == 0){
                App.districtSelect.install();
            }
            obj = obj || $("select.district_select:first");
            if(obj.attr("isLoaded") != "1"){
                obj.trigger('change');
                obj.attr("isLoaded", 1);
            }
        }
    },

    string  : {
        transformUrl: function(url){
            return url.replace(/((https?\:\/\/|ftp\:\/\/)|(www\.))(\S+)(\w{2,4})(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/gi, function(m){
                return '<a href="' + m + '" target="_blank">' + m + '</a>';
            });
        }
    },
    lock    : {
        init       : function(_this, types, fn){
            if(!_this) return;

            _this.locked = function(){
                App.lock.locked(_this);
            };

            _this.unlock = function(delay){
                App.lock.lockedDelay(_this, delay || 0);
                App.lock.unlock(_this);
            };

            return _this.on(types, function(){
                if(App.lock.isLocked(_this)) return;
                App.call(fn, _this);
            });
        },
        locked     : function(_this){
            _this.attr(App.config.sign.locked, 1);
        },
        unlock     : function(_this){
            setTimeout(function(){
                _this.attr(App.config.sign.locked, 0);
            }, App.parseInt(_this.attr(App.config.sign.lockedDelay)));
        },
        lockedDelay: function(_this, delay){
            _this.attr(App.config.sign.lockedDelay, delay);
        },
        isLocked   : function(_this){
            return _this.attr(App.config.sign.locked) == 1;
        }
    },
    focusEnd: function(_this){
        if(!_this) return;
        var length = _this.val().length;
        if(_this.val().lengh == 0) return _this;
        var input = _this[0];
        if(input.createTextRange){
            var range = input.createTextRange();
            range.collapse(true);
            range.moveEnd('character', length);
            range.moveStart('character', length);
            range.select();
        }else if(input.setSelectionRange){
            input.focus();
            input.setSelectionRange(length, length);
        }
        return _this;
    }
};

$.fn.extend({
    postJSON: function(url, data, callback, error){
        var _this = $(this);
        if(App.lock.isLocked(_this)) return;

        var _callback = function(data){
            App.call(callback, data, _this);
            App.lock.unlock(_this);
        };
        var _error = function(){
            console.info(App.config.info._404);
            App.call(error, _this);
            App.lock.unlock(_this);
        };

        App.lock.locked(_this);
        App.postJSON(url, data, _callback, _error);
        return this;
    },
    post    : function(url, data, callback, error){
        var _this = $(this);
        if(App.lock.isLocked(_this)) return;

        var _callback = function(data){
            App.call(callback, data, _this);
            App.lock.unlock(_this);
        };
        var _error = function(){
            console.info(App.config.info._404);
            App.call(error, _this);
            App.lock.unlock(_this);
        };

        App.lock.locked(_this);
        App.post(url, data, _callback, _error);
        return this;
    },
    lock    : function(types, fn, delay){
        App.lock.lockedDelay(this, delay || 0);
        return App.lock.init(this, types, fn);
    },
    focusEnd: function(){
        return App.focusEnd(this);
    }
});


$(function(){

    if($.isFunction($.fn.jQselectable)){
        $("select.selectable").jQselectable({set: "fadeIn", setDuration: "fast", opacity: .9, callback: function(){
            $(this).trigger("change");
        }});
        $("select.simpleselectable").jQselectable({style: "simple", set: "slideDown", out: "fadeOut", setDuration: 150, outDuration: 150, setDuration: "fast", opacity: .9, callback: function(){
            $(this).trigger("change");
        }});
    }

    /**链接事件统一控制*/
    $(".app_get_click_event").live("click", function(){
        var _this = $(this);
        if(_this.attr("app_loading") == "1"){
            return;
        }
        _this.attr("app_loading", 1);
        var _callbeforeback = _this.attr("app_call_before");
        var _callback = _this.attr("app_call_back");
        var detail = _this.data('detail');
        var _redirectUrl = _this.attr('redirectUrl');
        var _app_delay = _this.attr('app_delay');
        var _nocache = _this.attr('app_nocache') || "false";
        var _app_error_prompt = _this.attr('app_error_prompt') || "true";

        if(_callbeforeback){
            eval(_callbeforeback + "( _this )");
        }

        if(detail){
            if(_callback){
                eval(_callback + "( _this, detail )");
            }
            _this.attr("app_loading", 0);
        }else{
            App.getJSON(_this.attr("dataurl"), function(data){
                if(data.success && _nocache == "false"){
                    _this.data('detail', data);
                }

                if(!$17.isBlank(_redirectUrl)){
                    setTimeout(function(){
                        location.href = _redirectUrl;
                    }, App.parseInt(_app_delay, 0));
                }

                eval(_callback + "( _this, data )");
                _this.attr("app_loading", 0);
            }, function(e){
                var data = { success: false, info: "网络请求失败" };
                eval(_callback + "( _this, data )");
                _this.attr("app_loading", 0);
                if(_app_error_prompt == "true"){
                    alert("网络请求失败，请稍等重试或者联系客服人员");
                }
            });
        }
    });


    /**链接事件统一控制*/
    $(".app_get_html_click_event").live("click", function(){
        var _this = $(this);
        var _appName = _this.attr("app_name");
        var _callbeforeback = _this.attr("app_call_before");
        var _calllaterback = _this.attr("app_call_later");
        var _callcompleteback = _this.attr("app_call_complete");
        var _writeOnce = _this.attr("app_write_once");
        var _writeTarget = _this.attr("app_write_target");
        var _htmlPlace = _this.attr("app_html_place");

        _writeTarget = _writeTarget || "body";
        _writeTarget = _writeTarget == "this" ? this : _writeTarget;

        if(_callbeforeback){
            App.call(_callbeforeback, _this);
        }

        var detail = _this.data("detail");
        if(!detail){
            $.get(_this.attr("dataurl"), function(data){
                _this.data("detail", data);
                if(_calllaterback){
                    App.call(_calllaterback, _this);
                }

                if(_htmlPlace == "foot"){
                    $(_writeTarget).append(data);
                }else if(_htmlPlace == "body"){
                    $(_writeTarget).html(data);
                }else{
                    $(_writeTarget).prepend(data);
                }

                if(_callcompleteback){
                    App.call(_callcompleteback, _this);
                }
            });
        }else{
            if(_calllaterback){
                App.call(_calllaterback, _this);
            }

            if(!_writeOnce){

                if(_htmlPlace == "foot"){
                    $(_writeTarget).append(detail);
                }else if(_htmlPlace == "body"){
                    $(_writeTarget).html(detail);
                }else{
                    $(_writeTarget).prepend(detail);
                }
            }else{
                if(!$17.isBlank(_appName)){
                    App.call("app_auto_html_" + _appName + "_init", _this);
                }
            }

            if(_callcompleteback){
                App.call(_callcompleteback, _this);
            }
        }
    });

    /**自动加载*/
    $(".app_init_auto_get_html").each(function(){
        var _this = $(this);
        var _app_delay = _this.attr("app_delay") || 0;

        if(!_this.attr("dataurl")) return;

        setTimeout(function(){
            $.get(_this.attr("dataurl"), function(data){
                _this.html(data);
            });
        }, _app_delay);
    });

});

;(function(){
    function Arabia_To_SimplifiedChinese(Num) {
        for (i = Num.length - 1; i >= 0; i--) {
            Num = Num.replace(",", "");//替换Num中的“,”
            Num = Num.replace(" ", "");//替换Num中的空格
        }
        if (isNaN(Num)) { //验证输入的字符是否为数字
            //alert("请检查小写金额是否正确");
            return;
        }
        //字符处理完毕后开始转换，采用前后两部分分别转换
        part = String(Num).split(".");
        newchar = "";
        //小数点前进行转化
        for (i = part[0].length - 1; i >= 0; i--) {
            if (part[0].length > 10) {
                //alert("位数过大，无法计算");
                return "";
            }//若数量超过拾亿单位，提示
            tmpnewchar = ""
            perchar = part[0].charAt(i);
            switch (perchar) {
                case "0":  tmpnewchar = "零" + tmpnewchar;break;
                case "1": tmpnewchar = "一" + tmpnewchar; break;
                case "2": tmpnewchar = "二" + tmpnewchar; break;
                case "3": tmpnewchar = "三" + tmpnewchar; break;
                case "4": tmpnewchar = "四" + tmpnewchar; break;
                case "5": tmpnewchar = "五" + tmpnewchar; break;
                case "6": tmpnewchar = "六" + tmpnewchar; break;
                case "7": tmpnewchar = "七" + tmpnewchar; break;
                case "8": tmpnewchar = "八" + tmpnewchar; break;
                case "9": tmpnewchar = "九" + tmpnewchar; break;
            }
            switch (part[0].length - i - 1) {
                case 0: tmpnewchar = tmpnewchar; break;
                case 1: if (perchar != 0) tmpnewchar = tmpnewchar + "十"; break;
                case 2: if (perchar != 0) tmpnewchar = tmpnewchar + "百"; break;
                case 3: if (perchar != 0) tmpnewchar = tmpnewchar + "千"; break;
                case 4: tmpnewchar = tmpnewchar + "万"; break;
                case 5: if (perchar != 0) tmpnewchar = tmpnewchar + "十"; break;
                case 6: if (perchar != 0) tmpnewchar = tmpnewchar + "百"; break;
                case 7: if (perchar != 0) tmpnewchar = tmpnewchar + "千"; break;
                case 8: tmpnewchar = tmpnewchar + "亿"; break;
                case 9: tmpnewchar = tmpnewchar + "十"; break;
            }
            newchar = tmpnewchar + newchar;
        }
        //替换所有无用汉字，直到没有此类无用的数字为止
        while (newchar.search("零零") != -1 || newchar.search("零亿") != -1 || newchar.search("亿万") != -1 || newchar.search("零万") != -1) {
            newchar = newchar.replace("零亿", "亿");
            newchar = newchar.replace("亿万", "亿");
            newchar = newchar.replace("零万", "万");
            newchar = newchar.replace("零零", "零");
        }
        //替换以“一十”开头的，为“十”
        if (newchar.indexOf("一十") == 0) {
            newchar = newchar.substr(1);
        }
        //替换以“零”结尾的，为“”
        if (newchar.lastIndexOf("零") == newchar.length - 1) {
            newchar = newchar.substr(0, newchar.length - 1);
        }
        return newchar;
    }
    $17.extend($17, {
        Arabia_To_SimplifiedChinese       : Arabia_To_SimplifiedChinese
    });
}());

;(function (root, factory) {
    $17.extend( $17, factory());
}($17, function () {
    /**
     * 一个函数节流的函数 主要用在频繁操作上(resize scroll)
     * eg : 加特林10s内打出100发子弹(100次回调), 为了发热什么的,我们用throttle降低他的频率2s所以结果就是 10s内执行了100/2 50发子弹(50次回调)
     * @param
     *    fn : 要执行的函数
     *    delay : 多少时间后执行 单位ms
     *    scope :  指定上下文
     * @return
     *    function
     */
    var throttle = function(fn, delay, scope){

        delay || (delay = 250);

        var last,
            deferTimer;

        return function(){
            var context = scope || this;

            var now = +new Date(),
                args = arguments,
                done_fn = function(immediate){
                    last = immediate ? now : +new Date();

                    deferTimer = null;
                    fn.apply(context, args);
                };

            if (last === undefined || (now - last) >= delay){
                clearTimeout(deferTimer);

                return done_fn(true);
            }

            // deferTimer !== null  就会阻止一次动作结束后，新一轮的动作在结束时，应该还会有一次动作的行为
            deferTimer === null && (deferTimer = setTimeout(done_fn, delay));

        };
    };


    /**
     * 一个控制频繁操作的函数节流。
     * eg : 电梯系统，当电梯门要关闭时，外面有人进来，电梯门就不能关闭，只有2s内没人进出，他才会关闭。
     * @param func      : 要执行的函数
     * @param wait      : 等待时间ms
     * @param immediate : 是否在规定时间头执行，还是时间过后才执行
     * @param score     : 上下文
     *
     * @return function
     *
     */
    var debounce = function(func, wait, immediate, score){

        var timeout;

        wait || (wait = 250);

        return function(){
            var context = score || this,
                args = arguments;

            var later = function(){
                timeout = null;

                !immediate && func.apply(context, args);
            };

            var callNow = immediate && !timeout;

            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            callNow && func.apply(context, args);

        };

    };

    return {throttle: throttle, debounce: debounce};

}));