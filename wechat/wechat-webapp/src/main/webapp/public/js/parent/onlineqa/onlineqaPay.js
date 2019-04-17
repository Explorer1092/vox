define(['jquery', 'logger'], function ($, logger) {
    $(document).on("click",".js-payNowBtn",function(){
        var self = this;
        logger.log({
            module:'onlineqa',
            op: 'onlineqa_purchase_'+productType+'_click_pay_now'
        });
        setTimeout(function(){
            location.href = self.dataset.href;
        },1000);
    });
});