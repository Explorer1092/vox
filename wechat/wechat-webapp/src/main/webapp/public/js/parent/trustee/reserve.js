/**
 * Created by Administrator on 2015/11/10.
 */
define(["jquery","$17", "knockout","getVerifyCodeModal","jbox", "flexslider"],function($,$17,knockout,getVerifyCodeModal){
    /****************变量声明***********/
    var cid = $(".js-cid").html();
    var needPayV = $(".js-needPay").html();
    var needPayVal = true;
    if(needPayV == 'false'){
        needPayVal = false;
    }
    var bindMobileModalAndView = {
        phoneNo: knockout.observable(""),
        validCode: knockout.observable(""),
        showPrivilege: showPrivilege,
        subBook: subBook,
        showLawPage: showLawPage
    };

    bindMobileModalAndView.phoneNo(mobile);
    
    new getVerifyCodeModal({
        phoneNoInputId: "tt_phoneNo",
        btnId: "tt_verifyCodeBtn",
        url: "sendtrusteesmscode.vpage",
        cid: cid,
        countSeconds: 120,
        btnCountingText:"秒后重新获取",
        warnText: "验证码已发送，如未收到请2分钟后再试",
        gaCallBack: gaCallBack
    });

    /****************方法声明***********/
    function verifyMobileVal () {
        if(bindMobileModalAndView.phoneNo() == ""){
            $17.jqmHintBox("请输入手机号");
            return false;
        }
        if(!$17.isMobile(bindMobileModalAndView.phoneNo())){
            $17.jqmHintBox("请输入正确格式的手机号");
            return false;
        }
        return true;
    }

    function verifyCodeVal () {
        if(bindMobileModalAndView.validCode() == ""){
            $17.jqmHintBox("请输入验证码");
            return false;
        }
        if(bindMobileModalAndView.validCode().length != 6){
            $17.jqmHintBox("请输入6位数验证码");
            return false;
        }
        return true;
    }

    function verifyCodePost () {

        var data = {
            mobile: bindMobileModalAndView.phoneNo(),
            code: bindMobileModalAndView.validCode(),
            needPay: needPayVal
        };
        $.post("verifycode.vpage",data,function(result){
            if(result.success){

                setTimeout(function(){
                    if(needPayVal){
                        location.href= "reservepay.vpage";
                    }else{
                        location.href= "signpic.vpage";
                    }
                },1000);
            }else{
                $17.jqmHintBox(result.info);
            }
        });
        //location.href= "signpic.vpage";
    }

    function subBook () {
        if(verifyMobileVal()){
            if(verifyCodeVal()){
                verifyCodePost();
            }
        }
        if(needPayVal){
            $17.tongjiTrustee("B在线预约页面","B立即预约");
        }else{
            $17.tongjiTrustee("A在线预约页面","A立即预约");
        }
    }

    function showPrivilege () {
        if(needPayVal){
            $17.tongjiTrustee("B首页点击","B查看特权使用说明");
        }else{
            $17.tongjiTrustee("A首页点击","A查看特权使用说明");
        }
        location.href= "privilege.vpage?needPay=" +needPayVal;
    }

    function gaCallBack () {
        if(needPayVal){
            $17.tongjiTrustee("B首页点击","B获取验证码");
        }else{
            $17.tongjiTrustee("A首页点击","A获取验证码");
        }
    }

    function showLawPage () {
        if(needPayVal){
            $17.tongjiTrustee("B在线预约页面","B法律声明及隐私政策");
        }else{
            $17.tongjiTrustee("A在线预约页面","A法律声明及隐私政策");
        }
        location.href= "legalnotice.vpage";
    }

    /****************事件交互***********/
    knockout.applyBindings(bindMobileModalAndView);

    $(document).on("click",".js-jumpTab",function(){
        var _this = this;
        var tabId = "#"+_this.dataset.totab;
        $("html,body").animate({scrollTop: $(tabId).offset().top}, 1000);
    });

    $(document).on("click",".js-bookNow",function(){
        if(needPayVal){
            $17.tongjiTrustee("B首页点击","B立即预约");
        }else{
            $17.tongjiTrustee("A首页点击","A立即预约");
        }
    });

    //$('.active-wrap').on('scroll',function(){
    //
    //    console.log($("#reservep2").scrollTop());
    //    console.log($("#reservep3").scrollTop());
    //
    //    if($("#reservep2").scrollTop() == 0){
    //        console.log("第二页");
    //    }
    //    if($("#reservep3").scrollTop() == 0){
    //        console.log("第三页");
    //    }
    //});


    //计算高度
    function layout_resize(){
        var winHeight="";//浏览器窗口大小
        if(window.innerHeight){
            winHeight = window.innerHeight;
        }else if ((document.body) && (document.body.clientHeight)){
            winHeight = document.body.clientHeight;
        }else if(document.documentElement && document.documentElement.clientHeight && document.documentElement.clientWidth){
            winHeight = document.documentElement.clientHeight;
        }

        if(winHeight < 960){
            winHeight = 960;
        }

        $(".active03-box").height(winHeight);

        $(".flexslider").flexslider({
            animation: "slide",
            directionNav:false,
            slideshowSpeed: 5000,
            animationSpeed: 600,
            direction:'vertical',
            controlNav:false,
            slideshow:false,
            animationLoop:false,
            touch: true, //是否支持触屏滑动
            start:function(slider){
                slider.find(".js-jumpTab").on("click",function(){
                    slider.flexAnimate(slider.getTarget("next"), true);
                });

                //预约
                slider.find(".js-bookNow").on("click",function(){
                    slider.flexAnimate(2, true);
                });
            }

        });
    }

    layout_resize();

    ga('trusteeTracker.send', 'pageview');

    require(['logger'], function(logger) {
        logger.log({
            s0: !!LoggerProxy && LoggerProxy.openId,
            module: 'trustee',
            op: 'trustee_reserve_pv',
            userId: logger.getCookie('uid')
        });
    });

});