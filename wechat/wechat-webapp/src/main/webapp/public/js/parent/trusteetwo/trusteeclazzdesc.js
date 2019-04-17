/*
 * Created by free on 2015/12/15.
 */
define(["jquery", "$17", 'logger'], function ($, $17, logger) {
    var nStr;
    $(document).on("click", ".js-goToBookBtn", function () {
        setTimeout(function () {
            location.href = "reserve.vpage?shopId=" + $17.getQuery("shopId");
        }, 200);

        $17.tongjiTrustee("托管班介绍--" + nStr + "照片__学校" + $17.getQuery("shopId"), "去预约体验");
        logger.log({
            module: ' trusteeclass_desc' + $17.getQuery("shopId"),
            op: 'to_book_exp_btn_click'
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: ' trusteeclass_desc' + $17.getQuery("shopId"),
        op: 'trusteeclass_desc_page_load'
    });

});