/*
 * Created by free on 2016/02/18
 */
define(["jquery","$17","jbox","logger"],function($,$17,jbox,logger){
    var oid = $17.getQuery("oid");
    var postData = {};

    $(document).on("click",".js-reasonList>li",function(){
        postData.reasonId = $(this).data("id");
        $("#refundReason").find('input').attr("placeholder",$(this).html());
    });

    //退款原因
    $(document).on("click","#refundReason",function(){
        $(this).toggleClass("slideDown");
    });

    //提交申请
    $(document).on("click",".js-applyRefundBtn",function(){
        if(postData.reasonId){
            postData.refundDesc = $(".js-refundReasonDesc").val();
            postData.orderId = oid;
            $.post('/parent/trustee/refund.vpage',postData,function(result){
                if(result.success){
                    setTimeout(function(){
                        location.href = "/parent/trustee/order/detail.vpage?oid="+oid;
                    },200);
                    $17.tongjiTrustee("退款说明页","提交申请按钮",oid);
                    logger.log({
                        module: 'mytrustee_refund_Explain',
                        op: 'apply_btn_click',
                        orderId: oid
                    });

                }else{
                    $17.jqmHintBox(result.info);
                }
            });
        }else{
            $17.jqmHintBox("请选择您的退款原因");
        }
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_refund_Explain',
        op: 'refund_explain_pv',
        orderId: oid
    });

});