/*
 * Created by free on 2016/02/18
 */
define(["jquery","$17","knockout","logger"],function($,$17,ko,logger){
    var oid = $17.getQuery("oid");
    /****************变量声明***********/
    var refundDetailModalAndView = {
        goodsName : ko.observable(),
        orderId : ko.observable(),
        voucher : ko.observable(),
        studyState : ko.observable(),
        count : ko.observable(),
        refundReason : ko.observable(),
        refundDesc : ko.observable(),
        amount : ko.observable(),
        voucherState : ko.observable(),
        refundStatuses : ko.observableArray([])
    };

    var initDetail = function () {
        $.post('/parent/trustee/order/detail.vpage',{orderId: orderId},function(result){
            if(result.success){
                refundDetailModalAndView.goodsName(result.order.goodsName);
                refundDetailModalAndView.orderId(result.order.orderId);
                refundDetailModalAndView.voucher(result.order.voucher);
                refundDetailModalAndView.count(result.order.count);
                refundDetailModalAndView.refundReason(result.order.refundReason);
                refundDetailModalAndView.refundDesc(result.order.refundDesc);
                refundDetailModalAndView.amount(result.order.amount);
                refundDetailModalAndView.refundStatuses(result.order.refundStatuses);

                if(result.order.voucherActive){
                    refundDetailModalAndView.voucherState("(已激活)");
                }else{
                    refundDetailModalAndView.voucherState("(未激活)");
                }

                //首条状态
                var firstStatusNode = $('div.listInfo')[0];
                $(firstStatusNode).addClass("active");
                if($(firstStatusNode).find('p.title').attr('status') == 7){
                    $(firstStatusNode).find('p.title').addClass("txt-red");
                }else{
                    $(firstStatusNode).find('p.title').addClass("txt-yellow");
                }
            }
        });
    };

    initDetail();

    ko.applyBindings(refundDetailModalAndView);

    $(document).on("click",".js-callServiceBtn",function(){
        $17.tongjiTrustee('退款详情页', '咨询客服按钮',oid);
        logger.log({
            module: 'mytrustee_refund_detail',
            op: 'call_service_btn_click',
            orderId: oid
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_refund_detail',
        op: 'refund_detail_pv',
        orderId: oid
    });
});