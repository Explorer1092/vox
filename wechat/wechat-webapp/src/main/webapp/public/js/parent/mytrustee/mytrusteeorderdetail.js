/*
 * Created by free on 2016/01/06
 */
define(["jquery","$17","logger","jbox"],function($,$17,logger){
    var sid = $('.js-continuePayBtn').attr("data-sid");
    var oid = $(".js-refundBtn").attr("data-oid");
    var statu = $("#ostatus").val();
    var gtype = $("#gtype").val();

    //机构详情
    $(document).on("click",".js-trusteeDetailItem",function(){
        var bid = $(this).attr("data-bid");
        var branchName = $(this).find("h3.textOverflow").text();
        setTimeout(function(){
            location.href = "/parent/trustee/branchdetail.vpage?sid="+sid+"&bid="+bid;
        },200);
        $17.tongjiTrustee("订单详情页",branchName,oid);
        logger.log({
            module: 'mytrustee_order_detail',
            op: 'branch_name_click',
            branchId: bid
        });
    });

    //申请退款
    $(document).on("click",".js-refundBtn",function(){
        if(!$(this).hasClass("mc-btn-grey-s")){
            var contentHtml = '<div class="jBox-content" style="width: 380px; height: 80px; min-width: 320px; text-align: center;padding-top: 30px;">您确定要申请退款吗？</div><div class="jBox-Confirm-footer"><div class="jBox-Confirm-button jBox-Confirm-button-submit js-confirmBtn">确定</div></div>';
            var callBack = function(){
                $(".js-confirmBtn").on("click",function(){
                    setTimeout(function(){
                        location.href = "/parent/trustee/refund.vpage?oid="+oid;
                    },200);
                    $17.tongjiTrustee("订单详情页","申请退款确定按钮",oid);
                    logger.log({
                        module: 'mytrustee_order_detail',
                        op: 'refund_sure_btn_click',
                        orderId: oid
                    });
                });
            };

            var confirmModal = new jBox('Modal', {
                color: 'black',
                content: contentHtml,
                title: '<p style="font-size: 24px;">申请退款</p>',
                position: {
                    x: 'center',
                    y: 'center'
                },
                width: 400,
                height: 170,
                closeOnEsc: true,
                closeOnClick: 'box',
                closeButton: 'title',
                overlay: true,
                onOpen: callBack
            });
            confirmModal.open();

            $17.tongjiTrustee("订单详情页","申请退款按钮",oid);
            logger.log({
                module: 'mytrustee_order_detail',
                op: 'refund_btn_click',
                orderId: oid
            });
        }else{
            return false;
        }
    });

    //续费
    $(document).on("click",".js-continuePayBtn",function(){
        if(!$(this).hasClass("mc-btn-grey-s")){
           var gid = $(this).attr("data-gid");
            setTimeout(function(){
               location.href = "/parent/trustee/createorder.vpage?sid="+sid+"&gid="+gid+"&oid="+oid;
            },200);
            $17.tongjiTrustee("订单详情页","续费按钮",oid);
            logger.log({
                module: 'mytrustee_order_detail',
                op: 'continuePay_btn_click',
                orderId: oid
            });
        }else{
            return false;
        }
    });

    //查看退款详情
    $(document).on("click",".js-refundDetailBtn",function(){
        setTimeout(function(){
            location.href = "/parent/trustee/order/detail.vpage?oid="+oid;
        },200);

        logger.log({
            module: 'mytrustee_order_detail',
            op: 'see_refundDetail_btn_click',
            orderId: oid
        });
    });

    //控制遮罩
    $(document).on("click",".js-tipsBtn",function(){
        $(".js-tipsDiv").remove();
    });

    if((statu == "REFUNDING")|| (statu == "APPLY_REFUND") || (statu == "REFUNDED")){
        $(".mc-btn-greenWhite").removeClass("js-refundBtn").addClass('js-refundDetailBtn').text("查看退款详情");
    }
    if(!gtype || gtype == "experience"){
        $(".js-continuePayBtn").addClass("mc-btn-grey-s");
        $(".js-oPrice").hide();
    }

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_order_detail',
        op: 'order_detail_pv',
        orderId: oid
    });
});