/**
 * @author huihui.li
 * @description "微课堂详情页"
 * @createDate 2016/12.20
 */
define(['jquery', 'knockout', 'YQ', "weui", 'voxLogs'], function ($, ko, yq) {

    var defaultInitMode;

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'MicroDetailMode':
                defaultInitMode = new MicroDetailMode();
                break;
            case 'CourseLiveMode':
                defaultInitMode = new CourseLiveMode();
                break;
            case 'MicroOpenMode':
                defaultInitMode = new MicroOpenMode();
                break;
            default:
            //initMode null
        }
    }
    //微课堂详情页
    function MicroDetailMode() {
        var $this = this;
        var boxHeight = $(".sub-actIntro .sub-box-text").height();
        var courseTimeSub = microData.courseTime.substring(5, microData.courseTime.length - 3);
        $(".courseTimes").text(courseTimeSub);
        var trackStr = yq.getQuery("track") || "";
        var getExternalLoop = "",
            totalLoopTime = 2000;
        //打点  课程详情页 加载
        YQ.voxLogs({
            database: 'parent',
            module: 'm_kUjlHnIL',
            op: 'o_syrbUwra',
            s0: microData.id,
            s1: trackStr,
            s2: microData.timeSure
        });
        $this.isFromWeChat = function () {
            return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
        };
        $this.isFromParent = function () {
            return window.navigator.userAgent.toLowerCase().indexOf("17parent") > -1;
        };
        $this.appVersionCanUse = function () {
            if (versionCompare(getAppVersion(), '1.8.8.0') > -1) {
                return true;
            }
            return false;
        };
        $this.subscribeSucPop = function () {
            var timeFifth = Date.parse(new Date(microData.courseTime)) - 900000;
            var times = funTime(timeFifth);
            var minutes = times.split(" ")[1].split(":")[0];
            var seconds = times.split(" ")[1].split(":")[1];
            if (minutes < 10){
                minutes = "0" + minutes;
            }
            if (seconds < 10){
                seconds = "0" + seconds;
            }
            var courseTimeSub =times.split(" ")[0] + " " + minutes + ":" +  seconds;
            $.alert("课前15分钟，才能进入教室哦！<br/>请于" + courseTimeSub + "再来打开。");
        };
        $this.ingVideoRoomClick = function (id) {
            var h5Link = "http://" + location.host + "/mizar/microcourse/newgate.vpage?track=" + trackStr + "&period=" + id + "&_protocol=1";
            //对接SDK
            if($this.isFromParent()){
                if(getAppVersion() != ""){
                    if($this.appVersionCanUse()){
                        getExternalLoop = setInterval(function () {
                            totalLoopTime -= 20;
                            if(totalLoopTime != 0){
                                if (window.external && window.external.openLiveStream) {
                                    clearInterval(getExternalLoop);
                                    getExternalLoop = null;

                                    $.get('/mizar/microcourse/newentrance.vpage?period=' + id, function (res) {
                                        if (res.success) {
                                            // accessToken —— accessKey（客户端发版后需要改的参数值）
                                            if (res.accessKey && res.accessKey != "" && res.rMode) {
                                                window.external.openLiveStream(JSON.stringify({
                                                    type: 'live_talkfun',
                                                    play_mode: res.rMode,
                                                    access_key: res.accessKey,
                                                    class_name: res.spreadText || "",
                                                    class_pay_url:res.spreadUrl || "",
                                                    course_id: id
                                                }));
                                            } else {
                                                location.href = h5Link;
                                            }
                                        } else {
                                            if (res.code && res.code == 'DOWNGRADE') {
                                                location.href = h5Link;
                                            } else {
                                                $.alert(res.info ? res.info : '当前访问的用户过多，请稍后再试');
                                            }
                                        }
                                    });
                                }
                            }else{
                                $.alert("系统繁忙，请稍后再试");
                                clearInterval(getExternalLoop);
                                getExternalLoop = null;
                            }
                        },20);
                    }else{
                        location.href = h5Link;
                    }
                }
            }else{
                // 欢拓建议使用http的链接,&_protocol=1 使用http,沒有就走默认
                location.href = h5Link;
            }
        };
        // 内容详情 展开按钮操作 显示隐藏
        // $this.subTextHeight = function () {
            // $(".sub-actIntro .sub-text").css("max-height",$(".sub-box-text").height()+"px")
            // if (boxHeight > screen.height/3){
            //     $(".actShowBtn").show();
            // }else{
            //     $(".actShowBtn").hide();
            // }
        // };
        // $this.subTextHeight();
        $this.seriesClick = function (seriesListId) {
            location.href = "/mizar/course/courseperiod.vpage?period=" + seriesListId + "&track=" + trackStr;
        };
        $this.payHrefClick = function () {
            if (logged) {
                var link = "/mizar/microcourse/pay.vpage?id=" + microData.id + "&payAll=" + microData.payAll + "&track=" + trackStr;
                if (isFromWeChat()) { //添加微信来源
                    link += "&refer=wechat_parent";
                }
                location.href = link;
            } else {
                $.alert("家长用户请关注一起作业家长通后预约；老师用户请下载一起作业老师app后预约");
            }
        };
        // 内容详情 系列课程 展开
        $(document).on('click', ".actShowBtn", function () {
            // $(".sub-actIntro .sub-text").css("max-height",boxHeight+"px")
            // $(".actShowBtn").hide();
        }).on('click', ".serShowBtn", function () {
            $(".gtMore").show();
            $(".serShowBtn").hide();
        }).on('click', ".JS-indexSubmit", function () {
            if (logged) {
                // 立即预约 按钮
                var data = {
                    id: microData.id,
                    payAll: microData.payAll,
                    track: trackStr
                };
                if (isFromWeChat()) { //添加微信来源
                    data["refer"] = "wechat_parent";
                }
                $.post('/mizar/microcourse/reserve.vpage', data, function (res) {
                    if (res.success) {
                        if (microData['qqUrl'] && microData.qqUrl != "") {
                            $.confirm({
                                text: "预约成功，加群领取更多学习资料",
                                title: "提示",
                                onOK: function () {
                                    location.href = microData.qqUrl;
                                },
                                onCancel: function () {
                                    location.reload();
                                }
                            })
                        } else {
                            $.alert('预约成功', function () {
                                location.reload();
                            });
                        }
                    } else {
                        $.alert(res.info);
                    }

                })
            } else {
                $.alert("家长用户请关注一起作业家长通后预约；老师用户请下载一起作业老师app后预约");
            }
        });

        $(".sub-tab").on("click","li",function () {
            $(this).addClass('active').siblings().removeClass('active');
            var name = $(this).data('name');
            var offset = '';
            if (name == 'activity') {
                offset = $('#activityShowBox').offset().top;
            } else if (name == 'series') {
                offset = $('#seriesShowBox').offset().top;
            } else if (name == 'clazz') {
                offset = $('#clazzBox').offset().top;
            }
            $('body,html').animate({
                scrollTop: offset
            });
        });
    }
    // 微课堂跳转 欢拓
    function CourseLiveMode() {
        var $currentEnterDate = Date.now();
        YQ.voxLogs({
            database: 'parent',
            module: 'm_EXxXlXbF',
            op: 'o_eWHG1BCq',
            s0: periodId
        });
        window.onbeforeunload = window.onunload = function (event) {
            YQ.voxLogs({
                database: 'parent',
                module: 'm_EXxXlXbF',
                op: 'o_OgOskxtQ',
                s0: periodId,
                s1: Date.now() - $currentEnterDate
            });
        };

        var isFromWeChat = function () {
            return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
        };

        if (isFromWeChat()) { //微课堂支持微信打开，屏蔽分享
            function onBridgeReady() {
                WeixinJSBridge.call('hideOptionMenu');
            }

            if (typeof WeixinJSBridge == "undefined") {
                if (document.addEventListener) {
                    document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                } else if (document.attachEvent) {
                    document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                    document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                }
            } else {
                onBridgeReady();
            }
        }
    }

    // 短信跳转中转页
    function MicroOpenMode() {
        var direct_url = encodeURIComponent(location.origin+"/mizar/course/courseperiod.vpage?track=message&period=" + getQueryString("period")),
            ua = navigator.userAgent.toLowerCase(),
            open_app_url = 'a17parent://platform.open.api:/parent_main?from=h5&type=news_detail&url='+direct_url,
            ios_version = /ip(ad|hone|od)/.test(ua) && ua.match(/os (\d+)_(\d+)/);
        if(ios_version){
            open_app_url = 'a17parent://parent_main?yq_from=h5&yq_type=webview&yq_val='+direct_url;
        }
        location.href = open_app_url;

        setTimeout(function(){
            location.href = "//wx.17zuoye.com/download/17parentapp";
        }, 1000);
    }

    $(document).on("click", "[data-logs]", function () {
        // 打点
        try {
            var $self = $(this);
            var $logsString = $self.attr("data-logs");
            var $logsItems = {};
            if ($logsString != "") {
                var $logsJson = eval("(" + $logsString + ")");
                // m : $logsJson.m, op: $logsJson.op, s0:$logsJson.s0, s1 : $logsJson.s1
                if ($logsJson.database) {
                    $logsItems.database = $logsJson.database;
                }
                if ($logsJson.m) {
                    $logsItems.module = $logsJson.m;
                }
                if ($logsJson.op) {
                    $logsItems.op = $logsJson.op;
                }
                $logsItems.s0 = $logsJson.s0 || $self.attr('data-s0');
                $logsItems.s1 = $logsJson.s1 || $self.attr('data-s1');
                $logsItems.s2 = $logsJson.s2 || $self.attr('data-s2');
                YQ.voxLogs($logsItems);
            }
        } catch (e) {
            console.log(e.message);
        }
    });

    if (defaultInitMode) {
        ko.applyBindings(defaultInitMode);
    }

    $("#microVideo").attr('webkit-playsinline', '').on("webkitendfullscreen", function () {
        if (window.external && window.external['commonJs']) {
            window.external['commonJs'](JSON.stringify({type: 'VIDEO_FINISH'}));
        }
    });

    var isFromWeChat = function () {
        return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
    };
    Date.prototype.format = function(format) {
        var date = {
            "M+": this.getMonth() + 1,
            "d+": this.getDate(),
            "h+": this.getHours(),
            "m+": this.getMinutes(),
            "s+": this.getSeconds(),
            "q+": Math.floor((this.getMonth() + 3) / 3),
            "S+": this.getMilliseconds()
        };
        if (/(y+)/i.test(format)) {
            format = format.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));
        }
        for (var k in date) {
            if (new RegExp("(" + k + ")").test(format)) {
                format = format.replace(RegExp.$1, RegExp.$1.length == 1
                    ? date[k] : ("00" + date[k]).substr(("" + date[k]).length));
            }

        }
        return format;
    };
    function funTime(timestamp){
        var newDate = new Date();
        newDate.setTime(timestamp );
        return newDate.format('MM月dd日 h:m');
    }
    var versionCompare = function (versiona, versionb) {
        var diff;
        for (var i = 0; i <= 3; i++) {
            if (Number(versiona.split(".")[i]) > Number(versionb.split(".")[i])) {
                diff = 1;
                return diff;
            } else if (Number(versiona.split(".")[i]) < Number(versionb.split(".")[i])) {
                diff = -1;
                return diff;
            }
        }
        diff = 0;
        return diff;
    };

    function getAppVersion() {
        var native_version = "";
        if (window["external"] && window.external["getInitParams"]) {
            var $params = window.external.getInitParams();
            if ($params) {
                $params = eval("(" + $params + ")");
                native_version = $params.native_version;
            }
        } else if (getQueryString("app_version")) {
            native_version = getQueryString("app_version") || "";
        }
        return native_version;
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    window.vox = window.vox || {};
    vox.task = {
        refreshData: function () {
            location.reload();
        }
    };
});