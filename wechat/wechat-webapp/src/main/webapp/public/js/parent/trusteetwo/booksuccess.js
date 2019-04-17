/*
 * Created by free on 2015/12/15.
 */
define(["jquery", "$17", "logger"], function ($, $17, logger) {
    var shopId = shopId ? shopId : $17.getQuery("shopId");

    $(document).on("click", ".js-activityInfoBtn", function () {
        $17.loadingStart();
        location.href = "detail.vpage?shopId=" + shopId;
    });

    $(document).on("click", ".js-payNowBtn", function () {
        $17.loadingStart();
        setTimeout(function () {
            location.href = "skupay.vpage?shopId=" + shopId;
        }, 200);

        $17.tongjiTrustee("预约成功_" + shopId, "立即购买");
        logger.log({
            module: 'book_success' + shopId,
            op: 'buy_now_btn_click'
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'book_success' + shopId,
        op: 'book_success_page_load'
    });
});