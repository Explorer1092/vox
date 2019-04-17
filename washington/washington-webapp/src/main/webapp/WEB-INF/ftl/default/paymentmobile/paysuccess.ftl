<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
    title=''
    pageJs=["jquery"]
    pageJsFile={}
    pageCssFile={"paymentmobile" : ["public/skin/paymentmobile/css/paymentskin"]}
>
<div class="header-title paysuccess">
    <div class="p-inner">
        <a href="javascript:void(0);" class="back JS-returnPreviousBtn"></a>
        <div>订单支付</div>
    </div>
</div>

<div class="payment-mobile-box">
    <div class="pm-img JS-payResultImg"></div>
    <p class="pm-para JS-payResultInfo"></p>

    <div class="pm-btnbox">
        <a href="javascript:void(0);" class="pm-btn JS-returnPreviousBtn">确定</a>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function ($) {
        var _returnUrl = "${returnUrl!}";
        var _pageAppKey = "${(appKey)!}";
        var _payMethod = "${payment_gateway!}";
        var _orderId = "${order_id!}";
        var frontType =  "${frontType!''}"; // 跳转到支付页面的页面类型  frontType == 'h5'
        var isSendAppInfo =  "${isSendAppInfo!''}";
        var appType = "${appType!''}";
        var ua = navigator.userAgent.toLowerCase();
        var iOSVersion = (ua.indexOf("iphone") > -1 || ua.indexOf("ipad") > -1) ? parseInt(((navigator.appVersion).match(/OS (\d+)_(\d+)_?(\d+)?/))[1], 10) : 0;
        var payFlag = '';
        var payResult = ''; // 支付结果
        // 检测到iOS非壳内跳回APP（兼容iOS 微信支付完成会调起第三方浏览器）
        if (ua.indexOf("student") === -1
            && ua.indexOf("17parent") === -1
            && (ua.indexOf('iphone') > -1 || ua.indexOf('ipad') > -1)){
            $(".JS-returnPreviousBtn").hide();
            if (_payMethod.indexOf('studentapp') > -1) { // 学生端支付，如果跳转链接拼上url=encodeUr
                if (appType == 'junior') {
                    window.location.href = 'a17MiddleStudent://platform.open.api:/student_main?yq_from=h5&yq_type=webview'; // scheme调起中学学生app
                } else {
                    if (iOSVersion >= 11) {
                        window.location.href = 'https://wechat.17zuoye.com/s/student?yq_from=h5&type=h5'; // 通用协议调起APP
                    } else {
                        window.location.href = 'a17zuoye://platform.open.api:/student_main?yq_from=h5&yq_type=webview'; // scheme调起app
                    }
                }
            } else if (_payMethod.indexOf('parentapp') > -1) { // 家长端支付，如果跳转链接拼上yq_val=encodeUrl
                if (iOSVersion >= 11) {
                    window.location.href = 'https://wechat.17zuoye.com/j/jzt?yq_from=h5&yq_type=webview';
                } else {
                    window.location.href = 'a17parent://platform.open.api:/parent_main?yq_from=h5&yq_type=webview';
                }
            }

            setTimeout(function () {
                window.location.replace('/project/mobilegoin/index.vpage');
            }, 10000);

            return ;
        }

        if (_pageAppKey.indexOf("LevelReading") > -1){
            if (isWinExternal()['setTopBarInfo']){
                isWinExternal().setTopBarInfo(JSON.stringify({
                    show : false
                }));
            }

            if (compare_version(getAppVersion(), '3.0.3') > -1 && ua.indexOf("ios") > -1){
                if (isWinExternal()['setRightCloseBtn']){
                    isWinExternal().setRightCloseBtn(JSON.stringify({
                        show : false
                    }));
                }
            }
        }

        $(document).on("click", ".JS-returnPreviousBtn", function(){
            // 发送更新消息
            if (isWinExternal()["sendNotification"] && payFlag === 'success' && (_pageAppKey.indexOf("LevelReading") > -1 || isSendAppInfo === "true")) {
                isWinExternal().sendNotification(30004);
            }


            /* 成长世界，错题宝 等 特殊的h5跳转改为 openFairylandPage后，从confirm.vpage 开始，一直到支付结果页，都是只保留右上角关闭按钮，自己所在一个新的webview, 所以，返回逻辑分为以下2种情况：

                **注意：当前逻辑，只需要H5（成长世界，错题宝等）相关页面需要改变跳转方式，游戏页面也不用改**
                    ----目前通过H5新添加的链接参数 frontType == 'h5'来判断 业务页面是游戏页面还是H5页面----   目前带这个参数的页面有：新成长世界，小鹰学堂   速算脑里王不用带，因为此应用特殊，存在return_url

                    一、H5（成长世界、错题宝等）页面：
                    1. success:
                        (1) location.href = _returnUrl (有returnUrl,  这种情况目前没发现，但还是需要写上，以防万一 *这种情况会造成存在两个webview*)
                       （2） disMissView
                    2. 未支付:
                        未支付：window.history.go(- (window.history.length - 1))
                    3. 支付失败等其他情况：
                        disMissView 回到业务页面重新下单支付

                   二、 游戏页面：
                    1. success：
                       （1）location.href = _returnUrl (有returnUrl)
                        (2) window.history.go(- (window.history.length - 1))   ( --- 无returnUrl，目前没发现这种情况 --- 如果是游戏页面，并且没有returnUrl, 则直接go(length-1))
                    2. 未支付:
                        未支付：跳转到confirm.vpage 此时，需要后端已经记录confirm.vpage 具体地址，前端需要添加未支付时的标识 &paysuccess=true, 在confirm页面区分，显不显示左上角h5的返回按钮。
                    3. 支付失败等其他情况：
                        window.history.go(- (window.history.length - 1))

            */


            if (frontType === 'h5') {
                if (payFlag === 'success') {
                    if (_returnUrl !== '') {
                        location.href = _returnUrl;
                    } else {
                        if (isWinExternal()['disMissView']) {
                            isWinExternal().disMissView();
                        }
                    }
                } else if (payFlag === 'noPay') {// 表示未支付
                    window.history.go(-(window.history.length - 1));
                } else {
                    if (isWinExternal()['disMissView']) {
                        isWinExternal().disMissView();
                    }
                }
            } else {
                if (_pageAppKey ==='Arithmetic') {
                    if (payFlag === 'success' && _returnUrl !== ''){
                        if(isWinExternal()['openSecondWebview']){
                            isWinExternal().openSecondWebview( JSON.stringify({
                                url : _returnUrl,
                                page_close: true
                            }) );
                        }
                    } else {
                        if(isWinExternal()['disMissView']){
                            isWinExternal().disMissView();
                        }
                    }
                } else if (_pageAppKey.indexOf("LevelReading") > -1 || isSendAppInfo === 'true') {
                    if (payFlag === 'noPay') {// 表示未支付
                        window.history.go(- (window.history.length - 1));
                    } else {
                        if(isWinExternal()['disMissView']){
                            isWinExternal().disMissView();
                        }
                    }
                }else {
                    if (payFlag === 'success'){
                        if (_returnUrl !==''){
                            location.href = _returnUrl;
                        }else{
                            window.history.go(- (window.history.length - 1));
                        }
                    } else if (payFlag === 'noPay'){

                        if(_payMethod.indexOf('alipay_wap') > -1 && window.history.length > 3){ // 支付宝会在调起前先跳转一个自有的页面
                            window.history.go(-3);
                        } else if (_payMethod.indexOf('wechatpay_h5') > -1 && window.history.length > 2) {
                            window.history.go(-2);
                        } else {
                            window.history.go(- (window.history.length - 1));
                        }
                    } else {
                        window.history.go(- (window.history.length - 1));
                    }
                }
            }

        });
        // 初始调用一次
        // 安卓微信支付会在未支付的时候就打开return_url页面，初始化不请求接口
        // if (ua.indexOf("17student") === -1 || (ua.indexOf('android') === -1 || _payMethod.indexOf('wechatpay_h5') === -1)) {
        // if (!(ua.indexOf('android') > -1 && _payMethod.indexOf('wechatpay_h5') > -1)) {
            reqeustPayState(); // 初始调用一次
        // }
        var loadRequestFlag = false; // flag兼容iOS 触发两次load bug
        // 监测到页面可见的时候再次调用接口查询支付状态
        document.addEventListener('_17m.load', function () {
            if (loadRequestFlag) return ;
            loadRequestFlag = true;
            reqeustPayState();
            setTimeout(function () {
                loadRequestFlag = false;
            }, 100);
        }, false);

        //请求支付状态接口
        function reqeustPayState(){
            // 应后端要求延迟500ms请求
            setTimeout(function () {
                $.ajax({
                    url: "/apps/order/mobile/orderquery.vpage",
                    type: 'POST',
                    data: {
                        order_id: _orderId,
                        payment_gateway: _payMethod
                    },
                    success: function (res) {
                        payResult = res;
                        payReturnInfoWap(res);
                    },
                    error: function () {
                        payReturnInfoWap({tradeCode: -1});
                    }
                });
            }, 300);
        }
        // 处理支付结果
        function payReturnInfoWap(res){
            if (res.tradeCode === '1') { // 支付成功
                $('.JS-payResultImg').addClass("success");
                $('.JS-payResultInfo').html('您已成功开通' + res.productName);
                payFlag = 'success';
            } else if (res.tradeCode === '0') { // 未支付
                $('.JS-payResultImg').addClass('error');
                var nopayErrText = Math.abs(window.orientation) === 90 ? '支付遇到问题，请返回重新支付，如有疑问，请联系客服' : '支付遇到问题，请返回重新支付<br>如有疑问，请联系客服';
                $('.JS-payResultInfo').html(nopayErrText);
                payFlag = 'noPay';
            } else if (res.tradeCode === '2') { // 支付失败
                $('.JS-payResultImg').addClass('error');
                var errText = Math.abs(window.orientation) === 90 ? '支付失败，请返回重新支付，如有疑问，请联系客服' : '支付失败，请返回重新支付<br>如有疑问，请联系客服';
                $('.JS-payResultInfo').html(errText);
                payFlag = '';
            } else if (res.tradeCode === '-1'){
                $('.JS-payResultImg').addClass('error');
                var defaultErrText = Math.abs(window.orientation) === 90 ? '支付遇到问题，请返回重新支付，如有疑问，请联系客服' : '支付遇到问题，请返回重新支付<br>如有疑问，请联系客服';
                $('.JS-payResultInfo').html(defaultErrText);
                payFlag = '';
            }
        }

        // 获取链接参数
        function getQuery(item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
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

        // 检测横屏
        function initOrientationChange() {
            window.addEventListener('orientationchange', updateOrientation, false);
            updateOrientation();
        }
        // 更新横竖屏
        function updateOrientation () {
            payReturnInfoWap(payResult);
        }
        initOrientationChange();

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
    }
</script>

</@layout.page>