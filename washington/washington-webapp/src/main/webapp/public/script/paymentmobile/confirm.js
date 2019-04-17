// 获取query参数
var currentPayData;//当前支付方式
var lockSubmit = false;
var dialogAlertCallback = null;// 弹出alert提示
var coupons=[],
    selectedCouponFlag = false,
    selectedCouponIndex = 0;

var loadSendCoupon ; //是否显示送弹出框 {name: "任一活动产品开通送3天", desc: "有效期15天"}
var nowFinance;
var flagTrigger = true;
var flagPrice = [];
var paySuccess = false;
var ua = window.navigator.userAgent.toLowerCase();
var frontType = getQuery("frontType");
var app_type = ua.indexOf("17student") > -1 ? 'primary' : 'junior'; // paySuccess  Ios跳转到浏览器后，需要判断是跳转到小学学生还是中学学生  if (_payMethod.indexOf('studentapp') > -1) {if (appType === 'junior') {scheme调起中学学生app} else { 打开小学app}
var h5payVersionOpen = false;


var defaultIndex = 1;

var origin = 'https://parent.test.17zuoye.net';
if (location.href.indexOf('staging.17zuoye.net') > -1) {
    origin = 'https://parent.staging.17zuoye.net';
} else if (location.href.indexOf('17zuoye.com') > -1){
    origin = 'https://parent.17zuoye.com';
}

define(["jquery", "voxLogs", "template", "external"], function ($) {

    /*if (location.href.indexOf('https') > -1) {
        location.href = 'http' + location.href.substring(5);
    }*/

    /*家长端灰度地区只限苹果支付*/
    if (IOSFinanceRecharge && ua.indexOf('ios') > -1 && ua.indexOf('17parent') > -1 && compare_version(getAppVersion(), '2.6.2') > -1) {
        IOSFinanceRecharge = true;
        $(".recharge_box").show();
    } else {
        IOSFinanceRecharge = false;
        $(".other_box").show();
    }

    if (ua.indexOf("junior") == -1){
        $(".JS-header-title").show();
    }
    if ((pageAppKey == 'ListenWorld' || compare_version(getAppVersion(), '3.0.3') > -1) && ua.indexOf('17student') > -1 && openH5payment) {
        h5payVersionOpen = true;
    }

    if (hideWechatpay) {
        defaultIndex = 2;
    }
    
    bind_trigger('refreshData', function () {
        location.reload();
    });

    setTimeout(function () {
        //是否是审核账号
        $.ajax({
            url: '/userMobile/pinfo.vpage',
            type: 'GET',
            dataType: 'json',
            success: function(data){
                if (data.success && data.userId !== 20001){
                    $(".JS-isAuditCccount").show();
                } else {
                    $(".JS-isAuditCccount").hide();
                }
            }
        });
    }, 300);

    // 灰度开启wap支付
    if (h5payVersionOpen) {
        $(".js-selectPayment[data-paytype='5']").show();
        $(".js-selectPayment[data-paytype='6']").show();
        if (frontType === 'h5'){
            if (isWinExternal()['homeworkHTMLLoaded']){
                isWinExternal().homeworkHTMLLoaded();
            }
        }
    }

    if (pageAppKey.indexOf("LevelReading") > -1 || getQuery("hideAppTitle") === "true" || getQuery("isSendAppInfo") === "true"){
        if (isWinExternal()['setTopBarInfo']){
            isWinExternal().setTopBarInfo(JSON.stringify({
                show : false
            }));
        }
    }

    if (compare_version(getAppVersion(), '3.0.3') > -1 && ua.indexOf('student') > -1 && (pageAppKey.indexOf("LevelReading") > -1 || getQuery("isSendAppInfo") === "true") && ua.indexOf("ios") > -1){
        if (isWinExternal()['setRightCloseBtn']){
            isWinExternal().setRightCloseBtn(JSON.stringify({
                show : true
            }));
        }
    }

    $(document).on('click', '.js-closenoSet',function () {
        $(".js-noSet").hide();
    });

    $(document).on('click', '.js-closepassWord',function () {
        $(".js-passWord").hide();
    });

    $(document).on('click', '.js-jumpSet', function () {
        if (isWinExternal()["openSecondWebview"]) {
            isWinExternal().openSecondWebview( JSON.stringify({
                url : origin + '/view/mobile/parent/payment/index.vpage?useNewCore=wk&rel=3',
                page_close: true
            }) );
        } else {
            location.href = origin + '/view/mobile/parent/payment/index.vpage?useNewCore=wk&rel=3';
        }
        YQ.voxLogs({
            module: 'm_Og4azoheoS',
            op: 'o_YOs9b4KhZ8',
            s0: getQuery('rel'),
            s1: getQuery('oid'),
            s2: $(".js-finalPrice").text()
        });
    });

    $(document).on('click', '.js-enterAgain', function () {
        $(".js-notCorrect").hide();
        $(".js-passWord").show();
        $(".js-pwInput").val('');
        $(".js-pwBox").find('span').text('');
    });

    //确认支付
    $(document).on({
        click: function () {
            var $this = $(this);
            var $thisData = $this.data();
            var $paymentGateway = $this.attr("data-payment");

            if (app_type ==='junior' && $paymentGateway === 'wechatpay_studentapp') {
                $paymentGateway = 'wechatpay_studentapp_junior';
            }
            var $orderId = $thisData.order_id;
            var $orderSeq = $thisData.order_seq;
            var $appKey = $thisData.appkey;
            var $sessionKey = $thisData.sessionkey;
            var $appParams = isWinExternal()['getInitParams'] ? $.parseJSON(isWinExternal().getInitParams()) : ''; // native初始化参数
            // 兼容安卓getInitParams方法取不到system_type的bug
            if (!$appParams.system_type || ua.indexOf('android') > -1) {
                $appParams.system_type = 'android';
            }

            if (lockSubmit) {
                return false;
            }

            if (IOSFinanceRecharge && $(".js-selectPayment").hasClass("disabled")) {
                onDialog({
                    info: "立即去充值",
                    btnText: "确 定",
                    btnCancel: "取 消",
                    callback: function(){
                        do_external('openSecondWebview', {
                            url : location.origin + '/view/mobile/parent/17my_shell/coin.vpage?rel=confirm'
                        });
                    }
                });
                return;
            }
            if($paymentGateway == "" && ($(".js-couponMoney").text() < currentPrice)){
                onDialog({info: "请选择支付方式。"});
                return;
            }
            var checkFlag = true;
            if (ua.indexOf('parent') > -1 && compare_version(getAppVersion(), '2.5.0') > -1) {
                $(".js-pwInput").val('');
                $(".js-pwBox").find('span').text('');
                $.ajax({
                    url: origin + '/mobile/payment/password/exist.vpage',
                    xhrFields: {
                        withCredentials: true
                    }
                }).done(function (res) {
                    if (!res.success) {
                        onDialog({info: res.info});
                        return;
                    }
                    if (res.exist === 0) {
                        $('.js-noSet').show();
                    } else if (res.exist === 1) {
                        $('.js-passWord').show();
                        YQ.voxLogs({
                            module: 'm_Og4azoheoS',
                            op: 'o_TQfyUfByQ8',
                            s0: getQuery('rel'),
                            s1: getQuery('oid'),
                            s2: $(".js-finalPrice").text()
                        });
                        $(document).on('focus', 'input[readonly]', function () {
                            $(this).blur();
                        }).on('keyup', '.js-pwInput', function () {
                            $(".js-pwBox").find('span').text('');
                            var val = $(this).val();
                            for (var i=0;i<val.length;i++) {
                               $(".js-pwBox").find('span').eq(i).text('*');
                            }

                            if ($(this).val().length >= 6 && checkFlag) {
                                $(this).val($(this).val().slice(0, 6));
                                $.ajax({
                                    url: origin + '/mobile/payment/password/check.vpage',
                                    data: {
                                        code: $(this).val()
                                    },
                                    xhrFields: {
                                        withCredentials: true
                                    }
                                }).done(function (res) {
                                    if (!res.success) {
                                        checkFlag = true;
                                        $(".js-pwInput").blur();
                                        $(".js-notCorrect").show();
                                        $(".js-passWord").hide();
                                        return;
                                    }
                                    checkFlag = false;
                                    $(".js-pwInput").blur();
                                    $(".js-passWord").hide();
                                    payNow();
                                });
                            }
                            YQ.voxLogs({
                                module: 'm_Og4azoheoS',
                                op: 'o_CMCZVoDwKy',
                                s0: getQuery('rel'),
                                s1: getQuery('oid'),
                                s2: $(".js-finalPrice").text()
                            });
                        });
                    } else if (res.exist === 2) {
                        payNow();
                    }
                });
            } else {
                payNow();
            }

            YQ.voxLogs({
                module: 'm_Og4azoheoS',
                op: 'o_HgwfnLP45u',
                s0: getQuery('rel'),
                s1: getQuery('oid'),
                s2: $(".js-finalPrice").text()
            });

            function payNow() {
                loadSendCouponInit($orderId);

                var doPayFun = function () {
                    if (isFromWeChat()) {
                        window.location.href = window.location.protocol + weChatLinkHost + '/parent/wxpay/pay-order.vpage?oid=' + $orderSeq;
                    } else {
                        lockSubmit = true;
                        var return_url = '';
                        if (pageAppKey === 'Arithmetic' && returnUrl != '') {
                            var par = {
                                h5runpage: getQueryHaveUrl("h5runpage", returnUrl),
                                appName: getQueryHaveUrl("appName", returnUrl),
                                appVersion: getQueryHaveUrl("appVersion", returnUrl)
                            };
                            return_url = returnUrl.substring(0, returnUrl.indexOf('?'))+ '?' + $.param(par);
                        }
                        if($thisData.type == "afenti"){
                            afentiPay({
                                order_id: $orderId,
                                payment_gateway : $paymentGateway,
                                mobile_sys: $appParams.system_type + $appParams.system_version,
                                mobile_model: $appParams.model,
                                frontType: frontType,
                                isSendAppInfo: getQuery("isSendAppInfo"),
                                appType: app_type,
                                arithmeticUrl: return_url
                            });
                        }else{
                            submitConfirmCallback({
                                order_id: $orderId,
                                payment_gateway: $paymentGateway,
                                order_token: orderToken,
                                app_key: $appKey,
                                session_key: $sessionKey,
                                return_url:returnUrl,
                                mobile_sys: $appParams.system_type + $appParams.system_version,
                                mobile_model: $appParams.model,
                                frontType: frontType,
                                isSendAppInfo: getQuery("isSendAppInfo"),
                                appType: app_type,
                                arithmeticUrl: return_url
                            });
                        }
                    }
                };

                if(selectedCouponFlag){
                    $.post('/coupon/relatedcouponorder.vpage', {
                        orderId: currentOrderId,
                        refId: coupons[selectedCouponIndex].couponUserRefId,
                        couponId: coupons[selectedCouponIndex].couponId
                    }, function (res) {
                        if (res.success) {
                            $('.js-couponStyle').addClass('changeColor');

                            //学贝支付
                            if($paymentGateway == "recharge"){
                                rechargePay({
                                    orderId: $orderId,
                                    userId: $this.attr("data-parent_userid")
                                });
                            }else{
                                //微信或支付
                                doPayFun();
                            }
                        }else{
                            onDialog({info: '关联优惠券出错'});
                            selectedCouponFlag = false; //未选择任何优惠券
                            $('.js-DisplayCoupon').removeClass('txtRed').html('未使用');
                            $('.js-finalPrice').html(currentPrice);
                            $('.js-couponMoney').html('0.00');
                            $('.js-couponItem.active').removeClass('active');
                        }
                        $('.js-hasCoupon').hide();
                        $('.js-payDiv').show();
                    });
                }else{
                    $('.js-couponStyle').addClass('changeColor');
                    //学贝支付
                    if($paymentGateway === "recharge" || $paymentGateway === ""){
                        rechargePay({
                            orderId: $orderId,
                            userId: $this.attr("data-parent_userid")
                        });
                    }else{
                        doPayFun();
                    }
                }

                YQ.voxLogs({
                    module: 'paymentmobile',
                    op: 'confirm_pay_btn_click',
                    s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
                    s1: currentOrderId
                });
            }
        }
    }, ".js-submitConfirm");

    /*是否显示送优惠券 弹出框 start*/
    function loadSendCouponInit(orderId){
        if(parseInt(currentType) == 3){
            $.post("/coupon/loadsendcouponinfo.vpage", {
                orderId: orderId
            }, function(data){
                if(data.success && data['coupon']){
                    loadSendCoupon = data.coupon; //{name: "任一活动产品开通送3天", desc: "有效期15天"}
                }
            });
        }
    }

    /*是否显示送优惠券 弹出框 end*/

    /*
    *学贝支付start
    * */
    //支付submit
    function rechargePay(opt){
        /*
        * orderId : number
        * userId : number
        * */
        lockSubmit = true;
        onDialog({
            info: "确定学贝支付?",
            btnText: "确 定",
            btnCancel: "取 消",
            callback: function(){
                $.post("/finance/recharge/financepay.vpage", {
                    orderId: opt.orderId,
                    userId: opt.userId
                }, function(data){
                    if(data.success){
                        payReturnInfo(0, {success: true});
                    }else{
                        payReturnInfo('error', {
                            success: false,
                            error: data.info
                        });
                    }
                });
            }
        });
    }

    //选择家长支付账号
    var financeListRecord = [];//缓存数据
    function payParentSelect(){
        if(financeListRecord.length > 0){
            //缓存数据
            onDialog({
                info: template("T:家长列表", {financeList : financeListRecord, userId: $('.js-submitConfirm').attr("data-parent_userid")}),
                btnText: 'hide'
            });
            return false;
        }

        $.post("/finance/recharge/loadparentfinance.vpage", {
            studentId : getStudentId()
        }, function(data){
            var financeList = data.financeList || [];//data.financeList
            flagPrice = financeList;
            //家长列表有绑定显示学贝支付入口
            if(data.success && financeList.length > 0 && data['financeShow']){
                onDialog({
                    info: template("T:家长列表", {financeList : financeList, userId: $('.js-submitConfirm').attr("data-parent_userid")}),
                    btnText: 'hide'
                });
                tryErrorFunction(function(){
                    var selectParentId;
                    var priceValue = currentPrice;
                    if (selectedCouponFlag){
                        priceValue = coupons[selectedCouponIndex].discountPrice;
                    }
                    /*优惠卷价格*/
                    if(typeof(selectedCouponFlag) != 'undefined' && selectedCouponFlag){
                        priceValue = coupons[selectedCouponIndex].discountPrice;
                    }
                    /*优惠卷价格*/
                    var $financeParentName = { finance: 0 };
                    for(var i = 0; i < financeList.length; i++){
                        if(financeList[i].finance >= priceValue){
                            nowFinance = financeList[i].finance;
                            selectParentId = financeList[i].userId;
                            break;
                        }
                        else{
                            //看看是不是家长号余额足够
                            if(financeList[i].finance > 0 && financeList[i].finance >= priceValue){
                                $financeParentName = financeList[i];
                                nowFinance = $financeParentName.finance;
                                break;
                            }else{
                                nowFinance = 0;
                                $financeParentName = financeList[0];
                            }
                        }
                    }
                    //余额是否足够
                    if(selectParentId){
                        $('.js-rechargeParent[data-user_id="'+ selectParentId +'"]').trigger('click');
                        financeListRecord = data.financeList;
                    }else{
                        $(".js-rechargeInfoText").text("余额不足");
                        if(currentType == 2){
                            /*家长端显示提示文字*/
                            $(".js-rechargeInfoName").html("<p class='describe' style='color: #ff8971;'>点击立即去充值</p>")
                                .parents("a").addClass("disabled");
                        }else{

                            $(".js-selectPayment").removeClass("active");
                            $('.js-submitConfirm').attr("data-parent_userid", $financeParentName.userId);
                            $(".js-rechargeInfoName").html("<p class='describe'>"+ $financeParentName.callName + "账户余额："+ $financeParentName.finance +"学贝</p>")
                                .parents("a").addClass("disabled");
                        }
                        if (isFinancePayment) {
                            $('.js-submitConfirm').attr({
                                'data-payment': '',
                                'data-paytype' : ''
                            });
                        } else {
                            if (h5payVersionOpen) {
                                $(".js-selectPayment[data-paytype='6']").click();//默认选择微信支付wap
                            } else {
                                $(".js-selectPayment[data-paytype="+defaultIndex+"]").click();//默认选择微信支付
                            }
                        }
                    }

                    $("#paymentDialogAlert").hide();
                });
            }else{
                if (!isFinancePayment) {
                    if (h5payVersionOpen) {
                        $(".js-selectPayment[data-paytype='6']").click();//默认选择微信支付wap
                    } else {
                        $(".js-selectPayment[data-paytype="+defaultIndex+"]").click();//默认选择微信支付
                    }
                }
            }
        });
    }

    $(document).on("click", ".js-rechargeParent", function(){
        var $this = $(this);

        $this.addClass('active');
        $this.siblings().removeClass('active');
        /*$(".js-rechargeInfoText").text("余额 "+ $this.attr('data-finance'));*/
        $(".js-rechargeInfoName").html("<p class='describe'>" + $this.attr('data-callname') + "账户余额："+ $this.attr('data-finance') +"学贝</p>");

        $('.js-submitConfirm').attr({
            'data-parent_userid': $this.attr('data-user_id')
        });
        var newPrice = currentPrice;
        if (selectedCouponFlag){
            newPrice = coupons[selectedCouponIndex].discountPrice;
        }
        nowFinance = $this.attr('data-finance');
        if (!flagTrigger){
            if (Number(nowFinance) < Number(newPrice)){
                $(".js-rechargeInfoText").text("余额不足").parents(".js-selectPayment").addClass("disabled");
                if (isFinancePayment) {
                    $('.js-submitConfirm').attr({
                        'data-payment': '',
                        'data-paytype' : ''
                    });
                    $(".js-selectPayment").removeClass("active");
                } else {
                    if (h5payVersionOpen) {
                        $(".js-selectPayment[data-paytype='6']").click();//默认选择微信支付wap
                    } else {
                        $(".js-selectPayment[data-paytype="+defaultIndex+"]").click();//默认选择微信支付
                    }
                }
            }else{
                $(".js-rechargeInfoText").text("").parents(".js-selectPayment").removeClass("disabled");
            }
        }
        flagTrigger = false;
        //关闭家长列表
        var $dialogAlert = $("#paymentDialogAlert");
        $dialogAlert.hide();
    });
    /*
    *学贝支付end
    * */

    //black returnPreviousBtn
    $(document).on("click", ".JS-returnPreviousBtn", function () {
        if (paySuccess && isWinExternal()["sendNotification"] && (pageAppKey.indexOf("LevelReading") > -1 || getQuery("isSendAppInfo") === "true")) {
            isWinExternal().sendNotification(30004);
        }

        if(isWinExternal()['disMissView'] && (pageAppKey.indexOf("LevelReading") > -1 || getQuery("appPaySeccess") == "close")){
            isWinExternal().disMissView();
            return false;
        }

        if (returnUrl != "" && paySuccess) {
            window.location.href = returnUrl;
        } else {
            if (frontType === 'h5') {
                if(isWinExternal()['disMissView']){
                    isWinExternal().disMissView();
                }
            } else {
                if (window.history.length === 1) {
                    if(isWinExternal()['disMissView']){
                        isWinExternal().disMissView();
                    }
                } else {
                    window.history.back();
                }
            }
        }

    });

    //dialog close
    $(document).on("click", "#paymentDialogAlert .js-submit", function () {
        var $dialogAlert = $("#paymentDialogAlert");
        $dialogAlert.hide();

        if($(this).attr("data-type") == 'cancel'){
            return false;
        }

        if (dialogAlertCallback != null) {
            dialogAlertCallback();
        }
    });


    //studentAppList.indexOf(getStudentId()) > -1)
    var isShowApplePay = false;

    //select payment type
    $(document).on('click', '.js-selectPayment', function(){
        var $this = $(this);
        var flag = false;

        for (var i=0;i<flagPrice.length;i++){
            if (flagPrice[i].finance >= $(".js-finalPrice:first").text()){
                flag = true;
                break;
            }
        }

        //余额不足跳充值
        if(($this.hasClass("disabled") && !flag) ){

            if($this.attr('data-payment') == "recharge"){
                if(currentType == 2){
                    var $userAgent = navigator.userAgent;
                    //IOS
                    if (isWinExternal()["openSecondWebview"] && $userAgent.indexOf("Mac OS") > -1) {
                        isWinExternal().openSecondWebview( JSON.stringify({
                            url : location.origin + '/view/mobile/parent/17my_shell/coin.vpage?rel=gamePay'
                        }) );
                    } else {
                        onDialog({
                            info: "请前往“个人中心”->“我的学贝”<br/>页面充值。"
                        });
                    }
                }else{
                    if(isShowApplePay){
                        location.href = '/view/mobile/common/workcoinstudent/recharge?refer=gamePay';
                    }else{
                        onDialog({
                            info: "告诉家长去家长通充值学贝吧。"
                        });
                    }
                }
            }
            if (!isFinancePayment) {
                if (h5payVersionOpen) {
                    $(".js-selectPayment[data-paytype='6']").click();//默认选择微信支付wap
                } else {
                    $(".js-selectPayment[data-paytype="+defaultIndex+"]").click();//默认选择微信支付
                }
            }
            return;
        }

        $this.addClass('active');
        $this.siblings().removeClass('active');

        $('.js-submitConfirm').attr({
            'data-payment': $this.attr('data-payment'),
            'data-paytype' : $this.attr('data-paytype')
        });

        currentPayData = $this.attr('data-paytype');

        if($this.attr('data-payment') === "recharge"){
            payParentSelect();
        }
    });


    tryErrorFunction(function(){
        if (openRecharge){
            $(".js-selectPayment[data-paytype='0']").trigger('click');
        }else{
            if (!isFinancePayment) {
                if (h5payVersionOpen) {
                    $(".js-selectPayment[data-paytype='6']").trigger('click');
                } else {
                    $(".js-selectPayment[data-paytype="+defaultIndex+"]").trigger('click');
                }
            }
        }
    });

    //afenti payment
    function afentiPay(pushData){
        // 家长端支付payOrder参数
        if(parseInt(currentType) == 2 && getQuery("appPayments") == "parent"){
            if (isWinExternal()["payOrder"]) {
                isWinExternal().payOrder(JSON.stringify({
                    orderId: currentOrderId,
                    orderType: "order",
                    payType: currentPayData
                }));
            } else {
                onDialog({info: "请重新选择支付方式"});
                YQ.voxLogs({
                    module: 'paymentmobile',
                    op: 'error',
                    s0 : "payOrder() method does not exist"
                });
            }
            lockSubmit = false;//解锁
            return;
        }

        $.ajax({
            url: "/api/1.0/afenti/order/confirm.vpage",
            type: 'POST',
            data: pushData,
            success: function (data) {
                if (data.success) {
                    if (data.order_status === 'Paid') {
                        payReturnInfo(0, {success: true});
                    } else {
                        if(pushData.payment_gateway == "alipay_wap_studentapp" || pushData.payment_gateway == "wechatpay_h5_studentapp"
                            || pushData.payment_gateway == "alipay_wap_parentapp" || pushData.payment_gateway == "wechatpay_h5_parentapp"){
                            lockSubmit = false;///解锁
                            if(pushData.payment_gateway == "alipay_wap_studentapp" || pushData.payment_gateway == "alipay_wap_parentapp"){
                                // alert('wapForm')
                                var $wapForm = $("#wapForm");
                                $wapForm.html(data.wapForm);
                            }else{
                                // alert('location')
                                window.location.href = "/apps/order/mobile/wechat_h5.vpage?mwebUrl="+encodeURIComponent(data.mwebUrl);
                            }
                        }else{
                            lockSubmit = false;//解锁

                            if (isWinExternal()["payOrder"]) {
                                // alert('payOrder')
                                isWinExternal().payOrder(JSON.stringify({
                                    orderType: currentPayData, // 1,2
                                    handler: "returnUrlHandler",//回调方法名
                                    data: data.payParams
                                }));
                            } else {
                                onDialog({info: "请重新选择支付方式"});
                                YQ.voxLogs({
                                    module: 'paymentmobile',
                                    op: 'error',
                                    s0 : "payOrder() method does not exist"
                                });
                            }
                        }
                    }

                } else {
                    lockSubmit = false;///解锁
                    onDialog({info: data.info});
                }
            },
            error: function () {
                lockSubmit = false;///解锁
                onDialog({info: '网络错误'});
            }
        });
    }

    YQ.voxLogs({
        module: 'paymentmobile',
        op: 'pay_order_pageload',
        s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
        s1: currentOrderId
    });

    tryErrorFunction(function(){
        var isGetVersion = true; //解決无法获取到壳版本记录，在次获取

        //是否显示alipay
        $(document).ready(function(){
            showNewPay();
            var autoGetVersion =  setInterval(function(){
                if(isGetVersion){
                    showNewPay();
                }else{
                    clearInterval(autoGetVersion);
                }
            }, 500);
        });

        function showNewPay(){
            var setAppItem = {
                //家长APP版本
                '2': {
                    version_ali: [1, 6, 3]
                },
                //学生App版本
                '3': {
                    version_ali: [2, 7, 0]
                }
            };

            var currentApp = setAppItem[currentType];

            //微信只支持支付
            if(isFromWeChat()){
                if (h5payVersionOpen) {
                    $(".js-selectPayment[data-paytydpe='6']").show().siblings('a').hide();// 只开启微信Wap支付
                } else {
                    $(".js-selectPayment[data-paytydpe='1']").show().siblings('a').hide();// 只开启微信支付
                }
            }else{
                if (openRecharge){
                    $(".js-selectPayment[data-paytype='0']").show().siblings('a').hide();
                } else {
                    $(".js-selectPayment").hide();
                }
                /*if (h5payVersionOpen) {
                    $(".js-selectPayment[data-paytype='5']").show();
                    $(".js-selectPayment[data-paytype='6']").show();
                } else {
                    if (hideWechatpay) {
                        $(".js-selectPayment[data-paytype='2']").show();
                    } else {
                        $(".js-selectPayment[data-paytype='1']").show();
                        $(".js-selectPayment[data-paytype='2']").show();
                    }
                }*/
            }

            //currentType
            if( isWinExternal()["getInitParams"] ){
                var $params = isWinExternal().getInitParams();
                if($params){
                    $params = $.parseJSON($params);
                    var native_version = $params.native_version;
                    if(native_version.length > 4){
                        var version = native_version.split('.'),
                            part0 = parseInt(version[0]),
                            part1 = parseInt(version[1]),
                            part2 = parseInt(version[2]);
                        function showNewPayVersion(arr,obj) {
                            if(part0 > arr[0]){
                                obj.show();
                                $(".js-selectPayment[data-paytype='3']").show();
                            }else if (part0 >= arr[0] && part1 > arr[1]){
                                obj.show();
                                $(".js-selectPayment[data-paytype='3']").show();
                            }else if (part0 >= arr[0] && part1 >= arr[1] && part2 >= arr[2]){
                                obj.show();
                                $(".js-selectPayment[data-paytype='3']").show();
                            }
                        }

                        /* isShowApplePay = (navigator.userAgent.toLowerCase().indexOf('mac os') > -1 && (getAppVersion() >= '2.9.3'));
                         if(isShowApplePay){
                             return;
                         }*/
                        if (h5payVersionOpen) {
                            $(".js-selectPayment[data-paytype='6']").show();
                        } else {
                            $(".js-selectPayment[data-paytype="+defaultIndex+"]").show();
                        }

                        if (h5payVersionOpen) {
                            showNewPayVersion(currentApp.version_ali,$(".js-selectPayment[data-paytype='5']"));
                        } else {
                            showNewPayVersion(currentApp.version_ali,$(".js-selectPayment[data-paytype='2']"));
                        }
                        isGetVersion = false;
                    }
                }
            }
        }
    });

    /*
    * 优惠券 功能 start
    * */
    //获取优惠券
    function getCouponData() {
        $.get('/coupon/loadcoupons.vpage?orderId='+currentOrderId,function (res) {
            if(res.success){
                coupons = res.coupons;
                if(coupons && coupons.length != 0){
                    var temp = '',
                        disCountTemp='',
                        disCountlist = [
                            {key:'Discount',value:'折'},
                            {key:'Amount',value:'元'},
                            {key:'Period',value:'天'},
                            {key:'Voucher', value: '元'}
                        ];
                    for(var i = 0;i<coupons.length;i++){
                        for(var j=0;j<disCountlist.length;j++){
                            if(disCountlist[j].key == coupons[i].couponType){
                                disCountTemp = '<span class="num">'+coupons[i].typeValue+'</span>'+disCountlist[j].value+'</div>';
                            }
                        }

                        if(coupons[i].hasLinked){//如果已经关联订单，则不可选其他优惠券
                            $('.js-couponStyle').addClass('changeColor');
                            $('.js-DisplayCoupon').addClass('txtRed').html(coupons[i].couponName);
                            $('.js-finalPrice').html(coupons[i].discountPrice);
                            return false;
                        }

                        var headerTemp = '<div class="couponList js-couponItem" data-index="'+i+'">';
                        if(i==0){
                            headerTemp = '<div class="couponList js-couponItem active" data-index="'+i+'">';
                        }

                        temp += headerTemp+
                            '<div class="left">'+disCountTemp+
                            '<div class="right">'+
                            '<div class="info">'+
                            '<p class="describe">'+coupons[i].couponName+'</p>'+
                            '<p class="time">'+coupons[i].effectiveDateStr+'</p>'+
                            '</div>'+
                            '</div>'+
                            '</div>';
                    }
                    $('.js-couponBox').append(temp);
                    $('.js-couponLength').html(coupons.length).parent().removeClass("labelGray");
                    selectCoupon();
                }else{
                    $('.js-noCouponItem').remove();
                    $('.js-couponBox').append('<div class="couponEmpty">暂无可用的优惠券</div>');
                }
            }else{
                //onDialog({info: '获取优惠券信息出错'});
            }
        })
    }

    //优惠券说明

    if(loadCouponFlag){
        getCouponData();
    }

    function selectCoupon() {
        var selectedCoupon = $('.js-couponItem.active');
        if(selectedCoupon.length >0){//支持选择一张优惠券
            var couponNode = selectedCoupon[0];
            selectedCouponIndex = $(couponNode).data('index');
            var indexTypeValue = coupons[selectedCouponIndex].typeValue,
                discountDisplay='',couponMoney = '0.00';

            switch(coupons[selectedCouponIndex].couponType){
                case 'Discount':
                    discountDisplay = indexTypeValue+'折';
                    break;
                case 'Amount':
                    discountDisplay = '￥-'+indexTypeValue+'元';
                    couponMoney = indexTypeValue;
                    break;
                case 'Period':
                    discountDisplay = '赠送'+indexTypeValue+'天';
                    break;
                case 'Voucher':
                    discountDisplay = '￥-'+indexTypeValue+'元';
                    couponMoney = indexTypeValue;
                    break;
                default:

            }
            selectedCouponFlag = true; //有选择优惠券
            $('.js-DisplayCoupon').addClass('txtRed').html(discountDisplay);
            $('.js-couponMoney').html(couponMoney);
            $('.js-finalPrice').html(coupons[selectedCouponIndex].discountPrice);

            var parentName = "",financePrice;
            for (var i=0;i<flagPrice.length;i++){
                parentName = flagPrice[0].callName;
                financePrice = flagPrice[0].finance;
                if (flagPrice[i].finance >= $(".js-finalPrice:first").text()){
                    parentName = flagPrice[i].callName;
                    financePrice = flagPrice[i].finance;
                    break;
                }
            }

            nowFinance = financePrice;
            var newPrice = coupons[selectedCouponIndex].discountPrice;

            if ( nowFinance < newPrice){
                $(".js-rechargeInfoText").text("余额不足").parents(".js-selectPayment").addClass("disabled");
                if (isFinancePayment) {
                    $('.js-submitConfirm').attr({
                        'data-payment': '',
                        'data-paytype' : ''
                    });
                    $(".js-selectPayment").removeClass("active");
                } else {
                    if (h5payVersionOpen) {
                        $(".js-selectPayment[data-paytype='6']").click();//默认选择微信支付wap
                    } else {
                        $(".js-selectPayment[data-paytype="+defaultIndex+"]").click();//默认选择微信支付
                    }
                }
            }else{
                $(".js-rechargeInfoText").text("").parents(".js-selectPayment").removeClass("disabled");
                $(".js-rechargeInfoName").html("<p class='describe'>"+ parentName + "账户余额："+ financePrice +"学贝</p>")
            }
        }else{
            selectedCouponFlag = false; //未选择任何优惠券
            if ( Number(nowFinance) < Number(currentPrice)){
                $(".js-rechargeInfoText").text("余额不足").parents(".js-selectPayment").addClass("disabled");
                if (isFinancePayment) {
                    $('.js-submitConfirm').attr({
                        'data-payment': '',
                        'data-paytype' : ''
                    });
                    $(".js-selectPayment").removeClass("active");
                } else {
                    if (h5payVersionOpen) {
                        $(".js-selectPayment[data-paytype='6']").click();//默认选择微信支付wap
                    } else {
                        $(".js-selectPayment[data-paytype="+defaultIndex+"]").click();//默认选择微信支付
                    }
                }
            }else{
                $(".js-rechargeInfoText").text("").parents(".js-selectPayment").removeClass("disabled");
            }
            $('.js-DisplayCoupon').removeClass('txtRed').html('未使用');
            $('.js-couponMoney').html('0.00');
            $('.js-finalPrice').html(currentPrice);
        }
        $('.js-hasCoupon').hide();
        $('.js-payDiv').show();
    }

    $(document).on("click",'.js-couponStyle',function () {
        //关联优惠券后不能更换
        if( $(this).hasClass("changeColor") ){
            onDialog({info:'若要修改优惠券，请重新下单'});
            YQ.voxLogs({
                module: 'm_HdKWCdiV',
                op: 'o_iTh7lFvi',
                s0: false
            });
            return false;
        }
        //验证优惠券
        // if(hasCoupon) {
        $('.js-hasCoupon').show();
        $('.js-couponTitle').html('选择优惠券');
        $('.js-payDiv').hide();
        // }
        YQ.voxLogs({
            module: 'm_HdKWCdiV',
            op: 'o_iTh7lFvi',
            s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
            s1: currentOrderId
        });
    }).on('click','.js-couponItem',function () {
        $('.js-noIcon').removeClass('active');
        var $this = $(this);
        $this.addClass('active').siblings().removeClass('active');
    }).on('click','.js-noCoupon',function () {
        var $this = $('.js-noIcon');
        if($this.hasClass('active')){
            $this.removeClass('active')
        }else{
            $this.addClass('active');
            $('.js-couponItem').removeClass('active');
        }

    }).on('click','.js-sureCouponBtn',function () {//设置优惠券信息
        selectCoupon();
    }).on('click','#couponReturnBtn',function () {
        if($(".js-couponExplain").is(":visible")){//优惠券说明
            $('.js-couponExplain').hide();
            $('.js-chooseCouponDiv').show();
            $('.js-couponTitle').html('选择优惠券');
        }else{
            selectCoupon();
        }
    }).on('click','.js-explainedBtn',function () {
        $('.js-chooseCouponDiv').hide();
        $('.js-couponExplain').show();
        $('.js-couponTitle').html('优惠券说明');
    });
    /*
     * 优惠券 功能 start
     * */
});

//获取WeChat
function isFromWeChat() {
    return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
}

//选择支付callback
function submitConfirmCallback(pushData) {
    // 家长端支付payOrder参数 - 家长通是让壳自动生成第三方订单号
    if(parseInt(currentType) == 2 && ("FeeCourse".indexOf(pageAppKey) > -1 || getQuery("appPayments") == "parent")){
        if (isWinExternal()["payOrder"]) {
            isWinExternal().payOrder(JSON.stringify({
                orderId: currentOrderId,
                orderType: "order",
                payType: currentPayData
            }));
        } else {
            onDialog({info: "支付失败:10001"});
            YQ.voxLogs({
                module: 'paymentmobile',
                op: 'error_payOrder_null',
                s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
                s1: currentOrderId
            });
        }
        lockSubmit = false;///解锁
        return;
    }

    // 学生端支付payOrder参数  调用外壳接口
    $.ajax({
        url: "/apps/order/mobile/confirm.vpage",
        type: 'POST',
        data: pushData,
        success: function (data) {
            if (data.result) {
                if (data.order_status === 'Paid') {
                    payReturnInfo(0, {success: true});
                } else {
                    // 测试 h5 微信支付 本地调试，去掉
                    if(pushData.payment_gateway == "alipay_wap_studentapp" || pushData.payment_gateway == "wechatpay_h5_studentapp"
                        || pushData.payment_gateway == "alipay_wap_parentapp" || pushData.payment_gateway == "wechatpay_h5_parentapp"){
                        lockSubmit = false;///解锁
                        if(pushData.payment_gateway == "alipay_wap_studentapp" || pushData.payment_gateway == "alipay_wap_parentapp"){
                            // alert('wapForm')
                            var $wapForm = $("#wapForm");
                            $wapForm.html(data.wapForm);
                        }else{
                            // alert('location')
                            window.location.href = "/apps/order/mobile/wechat_h5.vpage?mwebUrl="+encodeURIComponent(data.mwebUrl);
                        }
                    } else {
                        // 调用外壳接口
                        var $orderType = currentPayData;
                        lockSubmit = false;///解锁

                        if (isWinExternal()["payOrder"]) {
                            // alert('payOrder')
                            isWinExternal().payOrder(JSON.stringify({
                                orderType: currentPayData, // 1,2
                                handler: "returnUrlHandler",//回调方法名
                                data: data.payParams
                            }));
                        } else {
                            onDialog({info: "支付失败:10001"});
                            YQ.voxLogs({
                                module: 'paymentmobile',
                                op: 'error_payOrder_null',
                                s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
                                s1: currentOrderId
                            });
                        }
                    }
                }
            } else {
                lockSubmit = false;///解锁
                onDialog({info: data.message});
            }
        },
        error: function () {
            lockSubmit = false;///解锁
            onDialog({info: '网络错误'});
        }
    });
}

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
        case 4 :
            payReturnInfo(dataJson.code, {
                'success' : (parseInt(dataJson.code) == 0),
                'other' :  '支付失败'
            });
            break;
        default :
            onDialog({info: "请选择支付方式"});
    }
}

function payReturnInfo(code, data){
    if(data.success){
        paySuccess = true;

        onDialog({
            info: "您已成功开通" + productName,
            btnText: "确 定",
            callback: function(){
                if(loadSendCoupon){
                    $("body").html( template("T:支付完成奖励礼券Page", (loadSendCoupon || {})) );
                    return false;
                }
                if (isWinExternal()["sendNotification"] && (pageAppKey.indexOf("LevelReading") > -1 || getQuery("isSendAppInfo") === "true")) {
                    isWinExternal().sendNotification(30004);
                }

                if (returnUrl != "") {
                    window.location.href = returnUrl;
                } else {
                    if (frontType === 'h5' || window.history.length === 1 || getQuery("appPaySeccess") == "close") {
                        if(isWinExternal()['disMissView']){
                            isWinExternal().disMissView();
                        }
                    } else {
                        window.history.back();
                    }
                }
            }
        });

        YQ.voxLogs({
            module: 'paymentmobile',
            op: 'pay_success_popup',
            s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
            s1: currentOrderId
        });
    }else if(data[code]){
        onDialog({info: data[code]});
        YQ.voxLogs({
            module: 'paymentmobile',
            op: 'pay_fail_popup',
            s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
            s1: currentOrderId
        });
    }else{
        onDialog({info: data.other});
        YQ.voxLogs({
            module: 'paymentmobile:payType_',
            op: 'pay_fail_popup',
            s0: 'userType_' + currentType + ':payType_' + currentPayData + ':payAppKey_' + pageAppKey,
            s1: currentOrderId
        });
    }
}

function onDialog(opt) {
    //参数空解锁
    if (opt && opt.info) {
        var $dialogAlert = $("#paymentDialogAlert");
        $dialogAlert.show().find(".js-content").html(opt.info);
        if (opt.btnText) {
            if(opt.btnText == 'hide'){
                $dialogAlert.find(".js-submit").html("").hide();
            }else{
                $dialogAlert.find(".js-submit").html(opt.btnText).show();
            }
        } else {
            $dialogAlert.find(".js-submit").html("知道了").show();
        }

        if(opt.btnCancel){
            $dialogAlert.find(".js-submit[data-type='cancel']").html(opt.btnCancel).show();
        }else{
            $dialogAlert.find(".js-submit[data-type='cancel']").hide();
        }

        if (opt.callback) {
            dialogAlertCallback = opt.callback;
        } else {
            dialogAlertCallback = null;
        }
    }

    lockSubmit = false;
}

//是否有X5的存在
function isWinExternal() {
    var _win = window;
    if (_win['yqexternal']) {
        return _win.yqexternal;
    } else if (_win['external']) {
        return _win.external;
    }else{
        _win.external = {};
        return _win.external
    }
}

function tryErrorFunction(callback){
    try{
        callback()
    }catch(e){
        YQ.voxLogs({
            module: 'paymentmobile',
            op: 'error'
        });
    }
}

function getStudentId(){
    var gq = function(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    };
    var gck = function (name){
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if(arr=document.cookie.match(reg))
            return unescape(arr[2]);
        else
            return null;
    };
    var sid;
    if(currentType == 2){
        if(gck("sid")){
            sid = gck("sid");
        }else{
            sid = gq("sid");
        }
    }else{
        sid = gck("uid");
    }
    return sid;
}

function getQuery(item){
    var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
    return svalue ? decodeURIComponent(svalue[1]) : '';
}

function getQueryHaveUrl(item, url){
    var svalue = url.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
    return svalue ? decodeURIComponent(svalue[1]) : '';
}

function compare_version(src, dest){
    var src_arr = src.split('.'),
        dest_arr = dest.split('.'),
        len = Math.max(src_arr.length, dest_arr.length);

    for(var index = 0; index < len; index++){
        var src_cache  = parseInt(src_arr[index]),
            dest_cache = parseInt(dest_arr[index]);

        if((src_cache && !dest_cache && src_cache > 0) || (src_cache > dest_cache)){
            return 1;
        }else if((dest_cache && !src_cache && dest_cache > 0) || (src_cache < dest_cache)){
            return -1;
        }
    }

    return 0;
}

function getAppVersion() {
    var native_version = "2.5.0";
    if (isWinExternal()["getInitParams"]) {
        var $params = isWinExternal().getInitParams();
        if ($params) {
            $params = eval("(" + $params + ")");
            native_version = $params.native_version;
        }
    } else if (getQuery("app_version")) {
        native_version = getQuery("app_version") || "";
    }
    return native_version;
}