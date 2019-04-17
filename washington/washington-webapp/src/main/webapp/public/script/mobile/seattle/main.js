define(['jquery', 'knockout', 'weui', 'voxLogs', 'external'], function ($, ko) {
    //开关，控制subscribe页面点击按钮颜色
    var btnFlag = false;

    function SeattleMode() {
        var $this = this;
        //弹出框
        $this.SID = ko.observable(getStudentId());
        $this.AID = ko.observable(activityId || 0);
        $this.track = getQueryString("track") || "";

        //footer status
        $this.status = (activityType || 'pay');
        $this.database = ko.observable({
            isNotBuy: false,
            sid : $this.SID()
        });
        $this.getStatus = function (aid, sid) {
            switch ($this.status) {
                case "Reserve":
                    $.post("/seattle/loadstudentreserve.vpage", {
                        activityId: aid,
                        studentId: sid
                    }, function (data) {
                        if (data.success) {
                            $this.database({
                                isNotBuy: data.reserve || false,
                                sid : sid
                            });
                        }
                    });
                    break;
                case "Subscribe":

                    $.post("/seattle/loadparentsubscribe.vpage", {
                        activityId: aid,
                        studentId: sid
                    }, function (data) {
                        if (data.success) {
                            $this.database({
                                isNotBuy: data.subscribe || false,
                                sid : sid
                            });
                            if((data.subscribe || false) || btnFlag){
                                $('.inner a').css('background','#d1d1d1');
                                $('.inner a').attr('href','javascript:;');
                            }
                        }
                    });
                    break;
                default:
                    $.post("/seattle/loadstudentorders.vpage", {
                        activityId: aid,
                        studentId: sid
                    }, function (data) {
                        if (data.success) {
                            $this.database({
                                isNotBuy: data.pay || false,
                                sid : sid
                            });
                        }
                    });
            }
        };
        //init
        $this.getStatus($this.AID(), $this.SID());

        //获取孩子列表
        $this.getStudentList = ko.observableArray();
        $this.getStudent = function () {
            $.get('/parentMobile/activity/getStudentList.vpage', function (data) {
                if (data.success) {
                    var isExistId = true;
                    for (var i = 0, items = data.students; i < items.length; i++) {
                        if ($this.SID() == items[i].id) {
                            isExistId = false;
                            break;
                        }
                    }

                    if (isExistId) {
                        $this.SID(null);
                    }

                    $this.getStudentList(data.students);
                }
            });
        };

        //init
        $this.getStudent();

        //select student
        $this.selectStudent = function (d, element, link) {
            var $self = $(element);

            if ($self.hasClass("active")) {
                return false;
            }

            if (d && d.id) {
                _setCookie("sid", d.id);
            }

            $this.SID(d.id);
            $this.getStatus($this.AID(), $this.SID());
        };


        $(document).ready(function () {
            $(window).resize(function () {
                indexSwitch($("#footerBox").height());
            });

            setTimeout(function(){
                indexSwitch($("#footerBox").height());
            }, 200);
        });

        //预约页
        $(document).on('click', ".JS-indexSubmit[data-type='Subscribe']",function(){
            var data = {
                studentId: $this.SID(),
                activityId: $this.AID()
            };

            if(getQueryString("track") != null){
                data.track = getQueryString("track");
            }

            $.post('subscribe.vpage', data, function(res){
                if(res.success){
                    btnFlag = true;
                    $.alert('已成功预约',function(){
                        $this.getStatus($this.AID(), $this.SID());
                    })
                }else{
                    $.alert(res.info);
                }

            })
        });

        YQ.voxLogs({database : "parent", module : 'm_Gn4M35Q8', op : 'o_O9wNHLPh', s0 : $this.AID(), s1: getQueryString("track")});
    }

    if (location.pathname == "/seattle/index.vpage") {
        ko.applyBindings(new SeattleMode());
    }

    if (location.pathname == "/seattle/paydetail.vpage") {
        YQ.voxLogs({database : "parent", module : 'm_Gn4M35Q8', op : 'o_uQftw78z', s0 : getQueryString("activityId"), s1: getQueryString("track")});

        if(isWeChat()){
            $(".JS-selectPayType[data-pay_type='2']").hide();
        }

    }

    if (location.pathname == "/seattle/reserve.vpage") {
        YQ.voxLogs({database : "parent", module : 'm_Gn4M35Q8', op : 'o_O9wNHLPh', s0 : getQueryString("activityId"), s1: getQueryString("track")});
    }

    //提交报名页发送验证码
    $(document).on("click", ".JS-sendCode", function () {
        var $this = $(this);
        var mobile = $("#mobile");

        if ($this.hasClass("w-disabled")) {
            return false;
        }

        if (mobile.val() == "") {
            $.alert("手机号不能为空！");
            return false;
        }

        $.post("sendrcode.vpage", {mobile: mobile.val()}, function (res) {
            if (res.success) {
                $this.html("发送中...");
                $this.addClass("w-disabled").removeClass("JS-sendCode");

                var clock = 60;
                var timer = setInterval(function () {
                    $this.html(--clock + 's');
                    if (clock <= 0) {
                        clearInterval(timer);
                        $this.removeClass("w-disabled").addClass("JS-sendCode").html("获取验证码");
                    }
                }, 1000);
            }else {
                $.alert(res.info);
            }
        });
    });

    //提交报名页submit按钮
    $(document).on("click", ".JS-submit[data-type='reserve']", function () {
        var _dataId = $(this).attr("data-id");
        var _dataType = $(this).attr("data-type");

        var mobile = $("#mobile");
        var code = $("#code");

        //判空
        if (!mobile.val()) {
            $.alert("手机号不能为空！");
            return;
        }
        if (!code.val()) {
            $.alert("验证码不能为空");
            return;
        }

        var data = {
            mobile: mobile.val(),
            code: code.val(),
            studentId: getStudentId(),
            activityId: _dataId
        };

        if(getQueryString("track") != null){
            data.track = getQueryString("track");
        }

        $.post("verifycode.vpage", data, function (res) {
            if (res.success) {
                //报名成功打点
                $.alert("工作人员会在48小时内和您联系", "恭喜您已成功报名", function () {
                    window.history.back();
                });
            } else {
                $.alert(res.info);
            }
        });
        YQ.voxLogs({database: "parent", module: 'm_Gn4M35Q8', op: 'o_v4yYR2am', s0: _dataId, s1: getQueryString("track"), s2: _dataType});
    });
    
    //选择pay type
    var recordPayType = 1;
    $(document).on("click", ".JS-selectPayType", function(){
        var $this = $(this);

        $this.addClass("active")
            .siblings().removeClass("active");

        recordPayType = $this.attr("data-pay_type");
    });

    var ua = window.navigator.userAgent.toLowerCase();

    var native_name = '';
    check_external('getInitParams', function (exists) {
        if (exists) {
            do_external('getInitParams', function (appParams) {
                appParams = typeof appParams === 'string' ? JSON.parse(appParams) : appParams;
                native_name = appParams.client_name.toLowerCase();
            });
        }
    });

    if (native_name.indexOf("juniorstu") > -1) {
        recordPayType = 2;
        $(".JS-selectPayType[data-pay_type='2']").show().addClass("active");
    } else {
        $(".JS-selectchild").show();
        $(".JS-selectPayType").show();
    }


    //支付确认页submit按钮
    $(document).on("click", ".JS-submit[data-type='pay']", function () {
        var $this = $(this);
        var _dataId = $this.attr("data-id");
        var _dataType = $this.attr("data-type");
        var _remarkVal = $("#remark").val();

        if(typeof(remarked) === 'boolean' && remarked && (_remarkVal == "" || _remarkVal.length > 100)){
            $.alert("请输入备注<br/>备注信息最多输入100字");
            return false;
        }

        var data = {
            sid: getStudentId(),
            activityId: _dataId,
            remark: _remarkVal
        };

        if(getQueryString("track") != null){
            data.track = getQueryString("track");
        }

        $.post('order.vpage', data, function (res) {
            if (res.success) {
                if (window['external'] && window.external['payOrder']) {
                    var uid = _getCookie('uid');
                    //学端支付
                    if(uid.substr(0 , 1) == 3 || native_name.indexOf('juniorpar') > -1){
                        var paymentGateway = {
                            1: "wechatpay_studentapp",
                            2: "alipay_studentapp",
                            3: "qpay_studentapp"
                        };

                        $.post("/api/1.0/afenti/order/confirm.vpage", {
                            order_id: res.orderId,
                            payment_gateway : paymentGateway[recordPayType]
                        }, function(data){
                            window.external.payOrder(JSON.stringify({
                                orderType: recordPayType, // 1,2
                                handler: "returnUrlHandler",//回调方法名
                                data: data.payParams
                            }));
                        });
                    }else{
                        window.external.payOrder(JSON.stringify({
                            orderId: res.orderId,
                            orderType: "order",
                            payType: recordPayType
                        }));
                    }
                } else {
                    if(isWeChat()){
                        location.href = window.location.protocol + weChatLinkHost + '/parent/wxpay/pay-order.vpage?oid=' + res.orderId;
                    }else{
                        $.alert("跳转支付失败！");
                    }
                }
            } else {
                $.alert(res.info);
            }
        });
        YQ.voxLogs({database: "parent", module: 'm_Gn4M35Q8', op: 'o_v4yYR2am', s0: _dataId, s1: getQueryString("track"), s2: _dataType});
    });

    function isWeChat(){
        return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") != -1);
    }

    $(document).on("click", ".JS-indexSubmit[data-type='joinGroup']", function(){
        //判断App版本号是否满足（大于1.5.2）
        var $this = $(this);
        var _dataId = $this.attr("data-id");
        if ($this.data().qcode == '') {
            $.alert("群号为空，加群失败！");
            return false;
        }

        var data = {
            groupId: $this.attr("data-qcode"),
            groupName: $this.attr("data-qname"),
            type: 'GROUP_ADD'
        };

        if (window['external'] && window.external['chatGroupMethod']) {
            window.external.chatGroupMethod(JSON.stringify(data));
            YQ.voxLogs({
                database: "parent",
                module: 'm_Gn4M35Q8',
                op: 'o_IarvZfyI',
                s0: _dataId,
                s1: getQueryString("track")
            });
        } else {
            $.alert("请求加群失败！");
        }



        YQ.voxLogs({
            database: "parent",
            module: 'm_Gn4M35Q8',
            op: 'o_zfQOcz2m',
            s0: _dataId,
            s1: getQueryString("track")
        });
    });

    $(document).on("click", ".JS-indexSubmit[data-type='Reserve']", function(){
        var $this = $(this);
        var _dataId = $this.attr("data-id");
        YQ.voxLogs({
            database: "parent",
            module: 'm_Gn4M35Q8',
            op: 'o_st4iqku1',
            s0: _dataId,
            s1: getQueryString("track")
        });

    });

    function getStudentId() {
        var uid = _getCookie('uid');
        if (getQueryString('sid')) {
            return getQueryString('sid');
        } else if (_getCookie) {
            if(uid.substr(0 , 1) == 3){
                return _getCookie('uid');
            }else{
                return _getCookie('sid');
            }
        } else {
            return 0;
        }
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    function _getCookie(name) {
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if (arr = document.cookie.match(reg))
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

    function indexSwitch(jh) {
        var _winHeight = $(window).height();

        if (_winHeight <= 600) {
            $("#vox17zuoyeIframe").height(600 - jh);
        } else {
            $("#vox17zuoyeIframe").height(_winHeight - jh);
        }
    }
});

//支付完成回调方法或Url跳转
function returnUrlHandler(data) {
    /*1 : wechat callback info and 2 alipay callback*/
    var dataJson = $.parseJSON(data);

    switch (parseInt(dataJson.orderType)) {
        case 1 :
            payReturnInfo(dataJson.code, {
                'success' : (parseInt(dataJson.code) == 0),
                '31000' : '没有安装微信客户端',
                '31001' : '微信版本过低不支持支付',
                '-5' : '微信不支持',
                'other' :  '支付失败'
            });
            break;
        case 2 :
            payReturnInfo(dataJson.code, {
                'success' : (parseInt(dataJson.code) == 9000),
                '31000' : '没有安装支付宝客户端',
                '31001' : '支付宝版本过低不支持支付',
                '8000' : '正在处理中，支付结果未知(有可能已经支付成功)，请查询商户订单列表中订单的支付状态。',
                '4000' : '订单支付失败',
                '6001' : '中途取消支付',
                '6002' : '网络连接出错',
                '6004' : '支付结果未知(有可能已经支付成功)，请查询商户订单列表中订单的支付状态。',
                'other' :  '支付失败'
            });
            break;
        case 3 :
            payReturnInfo(dataJson.code, {
                'success' : (parseInt(dataJson.code) == 0),
                '-11001' : '中途取消支付',
                '-1' : '中途取消支付',
                '-11003' : '参数错误',
                '-101' : '参数错误',
                '4' : '网络连接出错',
                '-100' : '网络连接出错',
                '-2' : '登录状态超时',
                '-3' : '重复提交订单',
                '-4' : '快速注册用户手机号不一致',
                '-5' : '账户被冻结',
                '-6' : '支付密码输入错误次数超过上限',
                'other' :  '支付失败'
            });
            break;
        default :
            $.alert("请选择支付方式");
    }
}

function payReturnInfo(code, data){
    if(data.success){
        $.alert("您已经支付成功。",function(){
            if(backSuccessUrl){
                window.location.href = backSuccessUrl;
            }else{
                window.history.back();
            }
        });
    }else if(data[code]){
        $.alert(data[code]);
    }else{
        $.alert(data.other);
    }
}