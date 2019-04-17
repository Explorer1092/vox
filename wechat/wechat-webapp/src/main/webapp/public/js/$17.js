/**
 *基础工具包
 */
define([], function () {
    var version = "0.0.1";

    var $17 = {};

    function extend(child, parent) {
        var key;
        for (key in parent) {
            if (parent.hasOwnProperty(key)) {
                child[key] = parent[key];
            }
        }
    }

    function include(child, parent) {
        var key;
        for (key in parent) {
            if (parent.hasOwnProperty(key)) {
                child.prototype[key] = parent[key];
            }
        }
    }


    function backToTop(time) {
        $('html, body').animate({scrollTop: '0px'}, time || 0);
    }

    //显示loading图标
    function loadingStart() {
        var loadingIcon = $('.loading_ajax_module');
        if (loadingIcon.length == 0) {
            loadingIcon = $('<div class="loading_ajax_module"><img src="/public/images/loading.gif" alt="正在加载" /></div>').appendTo(document.body);
        }
        loadingIcon.show();
    }

    //隐藏loading图标
    function loadingEnd() {
        $('.loading_ajax_module').hide();
    }

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

    /*依赖于weui  目前【只能应用于微信老师端】 start 小范围使用 */
    function weuiLoadingShow(){
        var $loadingToast = $('#loadingToast_weui');
        if($loadingToast.length == 0){
            var html = '<div id="loadingToast_weui" class="weui_loading_toast"><div class="weui_mask_transparent"></div><div class="weui_toast"><div class="weui_loading"><div class="weui_loading_leaf weui_loading_leaf_0"></div><div class="weui_loading_leaf weui_loading_leaf_1"></div><div class="weui_loading_leaf weui_loading_leaf_2"></div><div class="weui_loading_leaf weui_loading_leaf_3"></div><div class="weui_loading_leaf weui_loading_leaf_4"></div><div class="weui_loading_leaf weui_loading_leaf_5"></div><div class="weui_loading_leaf weui_loading_leaf_6"></div><div class="weui_loading_leaf weui_loading_leaf_7"></div><div class="weui_loading_leaf weui_loading_leaf_8"></div><div class="weui_loading_leaf weui_loading_leaf_9"></div><div class="weui_loading_leaf weui_loading_leaf_10"></div><div class="weui_loading_leaf weui_loading_leaf_11"></div></div><p class="weui_toast_content">数据加载中</p></div></div>';
            $('html body').append(html);
        }
        $loadingToast.show();
    }

    function weuiLoadingHide(){
        $('#loadingToast_weui').hide();
    }

    /*依赖于weui  目前【只能应用于微信老师端】 end */



    /*通用消息提示弹窗*/
    function msgTip(text,callback) {
        var div = $('.loading_ajax_module_tip');
        if(div.length == 0){
            var l = $('<div class="loading_ajax_module_tip">'+text+'</div>').appendTo(document.body).show();
            setTimeout(function(){
                l.remove();
                if(typeof callback === "function"){
                    callback();
                }
            },2000);
        }
    }

    //通用ajax请求
    function ajax(conf) {
        var confObj = {
            url: conf.url || '',
            type: conf.type || 'POST',
            dataType: conf.dataType || 'json',
            data: conf.data,
            showLoading: conf.showLoading === undefined ? true : conf.showLoading,  //是否显示loading图标
            success: function (returnData) {
                loadingEnd();
                if (returnData) {
                    if (returnData.success) {
                        conf.success && conf.success(returnData);
                    } else {
                        var errormsg = returnData.info || '操作失败，请重试！';
                        msgTip(errormsg);
                    }
                }
            },
            error: function (xhr, textStatus) {
                if (textStatus != 'abort') {
                    if (conf.error) {
                        conf.error();
                    }
                    else {
                        msgTip('发送请求失败，请重试！');
                    }

                }
            },
            complete: function () {
                conf.complete && conf.complete();

                //如果页面js报错导致阻塞，隐藏loading
                if ($('.loading_ajax_module').css('display') == 'block') {
                    loadingEnd();
                }
            }
        };
        if(confObj.showLoading){
            loadingStart();
        }
        $.ajax(confObj);
    }

    //验证是否未定义或null或空字符串
    function isBlank(str) {
        return str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    //验证是否手机号
    function isMobile(value) {
        value = value + "";
        //严格判定
        var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
        //简单判定
        var reg = /^1[0-9]{10}$/;
        if (!value || value.length != 11 || !reg.test(value)) {
            return false;
        }
        return true;
    }

    //验证是否邮箱
    function isEmail(value){
        var req = /^[-_.A-Za-z0-9]+@[-_.A-Za-z0-9]+(\.[-_.A-Za-z0-9]+)+$/;
        return value && req.test(value);
    }

    //获得地址栏参数
    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    function hint(content, callBack){
        var confirm = new jBox('Confirm', {
            content: content,
            confirmButton: '知道了',
            closeButton : false,
            cancelButton : '',
            confirm :callBack ? callBack : function(){ confirm.close();},
            onOpen : function(){
                $('.jBox-Confirm-button-cancel').hide();
            }
        });
        confirm.open()
    }
    function jqmHintBox(content) {
        new jBox('Notice', {
            color    : 'black',
            content  : content,
            autoClose: 1500,
            position : {
                x: 'center',
                y: 'center'
            },
            stack    : false,
            animation: {
                open : 'tada',
                close: 'zoomIn'
            }
        });
    }

    /*ga event analytics*/
    function tongji(){
        /*值类型是否必需说明
         Category String 是一般是用户与之互动的对象（例如按钮）
         Action String 是互动的类型（例如点击）
         Label String 否可用于给事件分类（例如导航按钮）
         Value Number 否值不得为负。可用于传递计数（例如 4 次）*/
        function ga(a, b, c, d, e){}
        switch (arguments.length) {
            case 1:
                alert('GA：不建议使用该方式');
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

    /*ga event analytics for O2O trustee*/
    function tongjiTrustee(){
        /*值类型是否必需说明
         Category String 是一般是用户与之互动的对象（例如按钮）
         Action String 是互动的类型（例如点击）
         Label String 否可用于给事件分类（例如导航按钮）
         Value Number 否值不得为负。可用于传递计数（例如 4 次）*/
        switch (arguments.length) {
            case 1:
                alert('GA：不建议使用该方式');
                ga("trusteeTracker.send", 'event', arguments[0].toString(), arguments[0].toString(), arguments[0].toString());
                break;
            case 2:
                ga("trusteeTracker.send", 'event', arguments[0].toString(), arguments[1].toString(), arguments[0] + "_" + arguments[1]);
                break;
            case 3:
                ga("trusteeTracker.send", 'event', arguments[0].toString(), arguments[1].toString(), arguments[2].toString());
                break;
        }
        return false;
    }

    extend($17, {
        version: version,
        include: include,
        extend: extend,
        backToTop: backToTop,
        loadingStart: loadingStart,
        loadingEnd: loadingEnd,
        msgTip: msgTip,
        ajax: ajax,
        isBlank : isBlank,
        isMobile : isMobile,
        isEmail : isEmail,
        getQuery : getQuery,
        alert : hint,
        jqmHintBox : jqmHintBox,
        tongji : tongji,
        tongjiTrustee : tongjiTrustee,
        weuiLoadingShow : weuiLoadingShow,
        weuiLoadingHide : weuiLoadingHide,
        strPad: strPad

    });

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


        $17.extend($17, {
            DateUtils: dateUtils
        });


    }($17));


    return $17
});
