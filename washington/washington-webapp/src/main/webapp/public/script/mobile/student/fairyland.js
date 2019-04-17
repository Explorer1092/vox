define(['jquery', 'knockout', 'voxSpread', 'voxLogs'], function ($, ko) {
    var fairylandInit = {
        appsInfo: ko.observableArray([]),
        appUserMessage: ko.observableArray([]),
        success: ko.observable(true),
        isMessageShow : ko.observable(true),
        headerBanner: ko.observable({}),
        isShowBanner : ko.observable(false),
        ajaxLoadFinished: ko.observable(false),
        examList : ko.observableArray([]), //同步教辅列表
        selfStudyList : ko.observableArray([]), //课外自学列表

        clickOpenGame: function () {
            var _this = this;
            if (_this.appKey == "BookListen" || _this.appKey == "Arithmetic") {
                // 随身听打开方式 || 速算脑力王
                if (getExternal()["innerJump"]) {
                    switch (_this.appKey){
                        case 'BookListen':
                            getExternal()["innerJump"](JSON.stringify({ name: 'book_listen' }));
                            break;
                        case 'Arithmetic':
                            getExternal()["innerJump"](JSON.stringify({ name: 'arithmetic', page_viewable: true }));
                            break;
                    }
                } else {
                    alert("版本错误！");
                    errorInfo("Listen openURL = " + _this.appKey);
                }

                YQ.voxLogs({
                    module: 'fairyland_app',
                    op: 'clickApp',
                    s0: _this.appKey,
                    s1: _fairylandMap.page || ''
                });
            } else {
                // 应用打开
                OpenApp(_this);
                if(_this.id && _this.readType == 'SNAPCHAT'){
                    //读取消息
                    ReadMessages(_this);
                    
                    YQ.voxLogs({
                        module: 'fairyland_app',
                        op: 'fairyland_message_click',
                        s0: _this.id,
                        s1: _fairylandMap.page || ''
                    });
                }else{
                    YQ.voxLogs({
                        module: 'fairyland_app',
                        op: 'clickApp',
                        s0: _this.appKey,
                        s1: _fairylandMap.page || ''
                    });
                }
            }
        },
        setAppStatus : function(appStatus) {
            var map = {'0':{name: "未开通",color: 'tag-red'},'1':{name: "已过期",color: 'tag-yellow'},'2':{name: "已开通",color: 'tag-green'}};
            return map[appStatus];
        },

        bannerBtn :function () {
            // 应用直接打开方式
            var externalName = getQueryString("from") == 'other' ? 'openFairylandPage' : 'pageQueueNew';
            var externalMap = {};
            // fix ios 要绝对路径
            var url = hasUrlHttp('/resources/apps/hwh5/treeplantingday/v100/index.html?from=other&entranceId=310006&refer=310006');
            externalMap.name = "fairyland_app:ns";
            externalMap.url = url;
            setTimeout(function () {
                if (getExternal()[externalName]) {
                    getExternal()[externalName](JSON.stringify(externalMap));
                } else {
                    location.href = url;
                }
            },200);

            YQ.voxLogs({
                module: 'm_1buEBmzn',
                op: 'o_FVylU0aA',
                s0: url
            });
        },

        clickOpenBanner : function (){
            var _this = this;
            var $root = fairylandInit.headerBanner() || {};

            if (getExternal()["pageQueueNew"]) {
                getExternal().pageQueueNew(JSON.stringify({
                    url: hasUrlHttp("/be/london.vpage?aid="+ _this.id + "&index=0&v=" + $root.appVersion + "&s=" + $root.systemType + "&sv=" + $root.systemVersion),
                    name: "fairyland_app:link",
                    useNewCore: "system",
                    orientation: "sensor",
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }));
            } else {
                errorInfo("Banner openURL = " + this.url);
            }

            YQ.voxLogs({
                module: 'fairyland_app',
                op: 'clickBanner',
                s0: _this.id,
                s1: _fairylandMap.page || ''
            });
        },

        /*自定义下拉模板*/
        koTemplateName: ko.observable(''), // ko template
        koTemplateClose: function () {
            fairylandInit.koTemplateName('');
        }
    };

    ko.applyBindings(fairylandInit);

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

    initData();//初始化

    function initData(){
        $.ajax({
            url : "/student/fairyland/applist.vpage",
            type : "GET",
            data :{
                version: getAppVersion()
            },
            success : function(data){
                if (data.success) {
                    if (data.appsInfo) {
                        fairylandInit.appsInfo(data.appsInfo);

                        //区分“同步教辅”和“课外自学”
                        var exam = [], selfStudy = [];
                        for(var i = 0; i < data.appsInfo.length; i++){
                            if(data.appsInfo[i].catalogDesc == '同步教辅'){
                                exam.push(data.appsInfo[i]);
                            }else{
                                selfStudy.push(data.appsInfo[i]);
                            }
                        }
                        fairylandInit.examList(exam);
                        fairylandInit.selfStudyList(selfStudy);

                    }
                    fairylandInit.ajaxLoadFinished(true);
                    if (data.appUserMessages) {
                        $(".messageBox ul").html('');
                        fairylandInit.isMessageShow(true);
                        fairylandInit.appUserMessage(data.appUserMessages);

                        YQ.textScroll({
                            ele: '.messageBox',
                            line : 1,
                            speed: 1000,
                            timer: 5000
                        });

                        YQ.voxLogs({
                            module: 'fairyland_app',
                            op: 'fairyland_message_show',
                            s1: _fairylandMap.page || ''
                        });
                    }
                } else {
                    fairylandInit.success(false);
                }
            },
            error : function(){
                fairylandInit.success(false);
            }
        });
    }

    //打开应用
    function OpenApp(_this){

        var launchUrl = _this.launchUrl + "&refer=" + getRequestOrderRefer("300001");
        if(getQueryString('pagegoto') == 'yes' && getExternal()["openFairylandPage"]){
            getExternal().openFairylandPage(JSON.stringify({
                url: hasUrlHttp(launchUrl),
                name: "fairyland_app:" + (_this.appKey || "link"),
                useNewCore: _this.browser || "system",
                orientation: _this.orientation || "sensor",
                page_viewable: true,
                initParams: JSON.stringify({hwPrimaryVersion: _this.hwPrimaryVersion || "V2_4_0"})
            }));
        }else{
            if (getExternal()["pageQueueNew"]) {
                getExternal().pageQueueNew(JSON.stringify({
                    url: hasUrlHttp(launchUrl),
                    name: "fairyland_app:" + (_this.appKey || "link"),
                    useNewCore: _this.browser || "system",
                    orientation: _this.orientation || "sensor",
                    initParams: JSON.stringify({hwPrimaryVersion: _this.hwPrimaryVersion || "V2_4_0"})
                }));
            } else {
                errorInfo("App openURL = " + _this.launchUrl);
            }
        }
    }
    //是否带http
    function hasUrlHttp(url){
        if(url.substr(0,7) == "http://" || url.substr(0,8) == "https://"){
            return url;
        }

        return window.location.origin + url;
    }

    //读取消息
    function ReadMessages(_this){
        $.get("/student/fairyland/viewMessage.vpage", {id: _this.id, msgType : _this.msgType}, function (data) {
            if (data.success) {
                fairylandInit.isMessageShow(false);
                initData();
            } else {
                errorInfo("读取失败！");
            }
        });
    }

    //获取渠道入口id
    function getRequestOrderRefer(defaultId) {
        var entranceId = getQueryString("refer");
        return entranceId == null || entranceId == '' ? defaultId : entranceId;

    }
    //获取App版本
    function getAppVersion() {
        var native_version = "2.5.0";

        if (getExternal()["getInitParams"]) {
            var $params = getExternal().getInitParams();

            if ($params) {
                $params = $.parseJSON($params);
                native_version = $params.native_version;
            }
        }

        return native_version;
    }

    function getExternal(){
        var _WIN = window;
        if(_WIN['yqexternal']){
            return _WIN.yqexternal;
        }else if(_WIN['external']){
            return _WIN.external;
        }else{
            return _WIN.external = function(){};
        }
    }

    YQ.voxSpread({
        keyId: 320401
    }, function(result){
        if(result.success){
            fairylandInit.headerBanner(result);
            if(fairylandInit.headerBanner().data && fairylandInit.headerBanner().data.length > 0){
                fairylandInit.isShowBanner(true);
            }
        }
    });

    YQ.voxLogs({
        module: 'fairyland_app',
        op: 'loading',
        s1: _fairylandMap.page || ''
    });

    function errorInfo(content){
        YQ.voxLogs({
            module: 'fairyland_app',
            op: 'error',
            s0 : content || 'null',
            s1: _fairylandMap.page || ''
        });
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    //设置title
    if (getExternal()["updateTitle"]) {
        getExternal().updateTitle("自学乐园", "494a4a", "ffffff");
    }

    if(getQueryString('pagegoto') == 'yes' && getQueryString('screen') == 'all' && getQueryString('url') != ''){
        if (getExternal()["openFairylandPage"]) {
            getExternal().openFairylandPage(JSON.stringify({
                url: hasUrlHttp(getQueryString('url')),
                name: "fairyland_app:link",
                useNewCore: getQueryString('system') || "system",
                orientation: getQueryString('sensor') || "sensor",
                page_viewable: true,
                initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
            }));
        }
    }else{
        if(getQueryString('pagegoto') == 'yes' && getQueryString('url') != ''){
            if (getExternal()["pageQueueNew"]) {
                getExternal()["pageQueueNew"](JSON.stringify({
                    url: hasUrlHttp(getQueryString('url'))
                }));
            }
        }
    }
});