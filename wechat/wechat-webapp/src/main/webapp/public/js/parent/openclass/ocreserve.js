/*
 * Created by free on 2015/12/14.
 */
define(["jquery", "$17", "knockout", "getVerifyCodeModal", "jbox", "logger"], function ($, $17, knockout, getVerifyCodeModal, jbox, logger) {
    /****************变量声明***********/
    var bookRegistModalAndView = {
        phoneNo: knockout.observable(""),
        validCode: knockout.observable(""),
        parentId: pid,
        subBook: subBook
    };

    bookRegistModalAndView.phoneNo(pNo);

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
        var isOdd = bookRegistModalAndView.parentId % 2 != 0;

        if (!$17.isMobile(bookRegistModalAndView.phoneNo())) {
            $17.jqmHintBox("请输入正确格式的手机号");
            return false;
        }

        if (isOdd && ($17.isBlank(bookRegistModalAndView.validCode()) || bookRegistModalAndView.validCode().length != 6)) {
            $17.jqmHintBox("请输入6位数验证码");
            return false;
        }

        var data = {
            mobile: bookRegistModalAndView.phoneNo(),
            shopId: $17.getQuery("shopId")
        };
        if (isOdd) {
            data.code = bookRegistModalAndView.validCode();
        }
        var url = isOdd ? 'verifycode.vpage' : 'reservewithoutcode.vpage';
        $.post(url, data, function (result) {
            if (result.success) {
                location.href = "reservepay.vpage?shopId=" + $17.getQuery("shopId");
            } else {
                $17.jqmHintBox(result.info);
            }
        });
        var name = isOdd ? "奇数" : "偶数";
        $17.tongjiTrustee("公开课报名_" + name + "_" + $17.getQuery("shopId"), "支付30元，立即报名");
        logger.log({
            module: "open_class_book_" + $17.getQuery("shopId"),
            op: "pay_one_yuan_book_btn_click"
        })
    }

    function subBook() {
        verifyCodePost();
    }

    function gaCallBack() {
        $17.tongjiTrustee("公开课报名_" + $17.getQuery("shopId"), "获取验证码");
        logger.log({
            module: "open_class_book_" + $17.getQuery("shopId"),
            op: "get_ver_code_btn_click"
        });
    }

    /****************事件交互***********/
    knockout.applyBindings(bookRegistModalAndView);

    logger.log({
        module: "open_class_book_" + $17.getQuery("shopId"),
        op: "open_class_book_page_load"
    });

});