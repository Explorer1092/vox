/*
 * Created by free on 2015/12/14.
 */
define(["jquery", "$17", "knockout", "getVerifyCodeModal", "jbox", "logger"], function ($, $17, knockout, getVerifyCodeModal, jbox, logger) {
    /****************变量声明***********/
    var bookRegistModelAndView = {
        phoneNo: knockout.observable(""),
        validCode: knockout.observable(""),
        subBook: subBook,
        showLawDetail: showLawDetail
    };

    bookRegistModelAndView.phoneNo(pNo);

    new getVerifyCodeModal({
        phoneNoInputId: "phoneNo",
        btnId: "phoneVerCodeBtn",
        url: "sendtrusteesmscode.vpage",
        cid: cid,
        countSeconds: 120,
        btnCountingText: "秒后重新获取",
        warnText: "验证码已发送，如未收到请2分钟后再试",
        gaCallBack: gaCallBack
    });

    /****************方法声明***********/
    function verifyCodePost() {
        var data = {
            mobile: bookRegistModelAndView.phoneNo(),
            code: bookRegistModelAndView.validCode(),
            shopId: $17.getQuery("shopId")
        };
        $17.loadingStart();
        $.post("verifycode.vpage", data, function (result) {
            if (result.success) {
                setTimeout(function () {
                    location.href = "reservepay.vpage?shopId=" + $17.getQuery("shopId");
                }, 200);
            } else {
                $17.jqmHintBox(result.info);
            }
            $17.loadingEnd();
        }).fail(function(){
            $17.loadingEnd();
        });
        $17.tongjiTrustee("预约报名_" + $17.getQuery("shopId"), "支付10元立即预约");
        logger.log({
            module: 'trustee_book_regist' + $17.getQuery("shopId"),
            op: 'pay_ten_yuan_to_book_btn_click'
        });
    }

    function subBook() {
        if (!$17.isMobile(bookRegistModelAndView.phoneNo())) {
            $17.jqmHintBox("请输入正确格式的手机号");
            return false;
        }

        if ($17.isBlank(bookRegistModelAndView.validCode()) || bookRegistModelAndView.validCode().length != 6) {
            $17.jqmHintBox("请输入6位数验证码");
            return false;
        }
        verifyCodePost();
    }

    function gaCallBack() {
        $17.tongjiTrustee("预约报名_" + $17.getQuery("shopId"), "获取验证码");
        logger.log({
            module: 'trustee_book_regist' + $17.getQuery("shopId"),
            op: 'get_ver_code_btn_click'
        });
    }

    function showLawDetail() {
        location.href = "legalnotice.vpage";
    }

    /****************事件交互***********/
    knockout.applyBindings(bookRegistModelAndView);

    if ($17.getQuery("shopId") == 6) {
        $("#imgeFormdiv").removeClass("ab03-hd").addClass("ab03-hd ab03-hd-one");
    }

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'trustee_book_regist' + $17.getQuery("shopId"),
        op: 'book_regist_page_load'
    });

});