/*
 * Created by free on 2016/01/11
 */
define(["jquery","$17","logger"],function($,$17,logger){
    var oid = $17.getQuery("oid");
    $(document).on("click",".js-contectUsBtn",function(){
        $17.tongjiTrustee("退款详情页","请联系客服",oid);
        logger.log({
            module: 'mytrustee_refund',
            op: 'contact_customer_click_'+oid
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_refund',
        op: 'refund_pv'
    });
});