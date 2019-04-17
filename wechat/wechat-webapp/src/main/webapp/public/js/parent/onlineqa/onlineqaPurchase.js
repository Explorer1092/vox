define(['jquery', 'logger'], function ($, logger) {
    $('span.select-times').on('click', function () {
        $('.select-type').removeClass('selected-btn');
        $(this).addClass('selected-btn');
        $('.select-period').addClass('selected-normal');
        $("div.select-times").show();
        $("div.select-times span.select-product:first").trigger('click');
        $("div.select-period").hide();

    });
    $('span.select-period').on('click', function () {
        $('.select-type').removeClass('selected-btn');
        $(this).addClass('selected-btn');
        $('.select-times').addClass('selected-normal');
        $("div.select-times").hide();
        $("div.select-period").show();
        $("div.select-period span.select-product:first").trigger('click');
    });
    $(".select-product").on('click', function () {
        $(this).addClass('selected-btn');
        $(this).siblings().removeClass('selected-btn');
        var price = $(this).data('price');
        var productid = $(this).data('productid');
        $('#selected-product-price').text(price);
        $('#selected-product-id').val(productid);
    });
    $('#order').on('click', function () {
        var productId = $('#selected-product-id').val();
        console.info("atend to buy "+productId);
        if (!productId) {
            return false;
        }
        logger.log({
            //点击立即购买
             module: 'onlineqa',
             op: 'onlineqa_purchase_'+productType+'_click_buy_now',
             productId:productId
         });
        $.post('/parent/onlineqa/order.vpage', {
            productId: productId
        }, function (data) {
            if (data.success) {
                //下单成功
                logger.log({
                    module: 'onlineqa',
                    op: 'onlineqa_purchase_'+productType+'_order_succeed'
                });
                location.href = '/parent/wxpay/onlineqa_confirm.vpage?oid=' + data.orderId;
            } else {
                //下单失败
                logger.log({
                    module: 'onlineqa',
                    op: 'onlineqa_purchase_'+productType+'_order_fail'
                });
            }
        })
    });
    logger.log({
        module: 'onlineqa',
        op: 'onlineqa_purchase_'+productType+'_pv'
    });
});