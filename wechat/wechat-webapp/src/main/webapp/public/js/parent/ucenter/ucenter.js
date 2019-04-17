/*
 * 会员中心
 */
define(["jquery", "menu"], function ($, menu) {
    $(document).on("click", ".js-changeMobile", function () {
        location.href = "/parent/ucenter/changebindmobile.vpage?returnUrl=" + "/parent/ucenter/index.vpage";
    });
});