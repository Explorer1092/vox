/*
 * Created by free on 2016/01/05
 */
define(["jquery","$17","logger"],function($,$17,logger){
    var oid = $17.getQuery("oid");
    $(document).on("click",".js-payBtn",function(){
        //var oid = $(this).attr("data-oid");
        setTimeout(function(){

            if(window.isFromParent){
                window.external.payOrder(String(oid), "trusteecls");
                return ;
            }

            location.href = "/parent/wxpay/pay-trusteecls.vpage?oid="+oid;

        },200);
        $17.tongjiTrustee("支付订单页","确认支付",oid);
        logger.log({
            module: 'mytrustee_payConfirm',
            op: 'confirm_pay_btn_click',
            orderId: oid
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_payConfirm',
        op: 'payConfirm_pv',
        orderId: oid
    });
});