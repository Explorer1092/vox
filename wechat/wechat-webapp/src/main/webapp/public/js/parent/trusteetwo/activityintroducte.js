/*
 * Created by free on 2015/12/15.
 */
define(["jquery", "$17", "logger"], function ($, $17, logger) {

    $(document).on("click", ".js-bookregistBtn", function () {
        setTimeout(function () {
            location.href = "reserve.vpage?shopId=" + $17.getQuery("shopId");
        }, 200);

        $17.tongjiTrustee("活动介绍页_" + $17.getQuery("shopId"), "去预约体验");
        logger.log({
            module: "trusteeActivity" + $17.getQuery("shopId"),
            op: "to_book_experience_btn_click"
        });
    });

    $(document).on("click", ".js-knowclazzinfoBtn", function () {
        setTimeout(function () {
            location.href = "detail.vpage?shopId=" + $17.getQuery("shopId");
        }, 200);
        $17.tongjiTrustee("活动介绍页_" + $17.getQuery("shopId"), "了解托管班");
        logger.log({
            module: "trusteeActivity" + $17.getQuery("shopId"),
            op: "know_trustee_class_btn_click"
        });
    });

    $(document).on("click", ".js-ruledescBtn", function () {
        setTimeout(function () {
            location.href = "ruledesc.vpage?shopId=" + $17.getQuery("shopId");
        }, 200);
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: "trusteeActivity" + $17.getQuery("shopId"),
        op: "activity_page_load"
    });

});