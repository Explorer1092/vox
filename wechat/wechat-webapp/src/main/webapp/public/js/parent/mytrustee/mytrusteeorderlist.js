/*
 * Created by free on 2016/01/06
 */
define(["jquery","$17","logger"],function($,$17,logger){

    var loadPage = function(url,oid){
        setTimeout(function(){
            location.href = url+oid;
        },200);
        $17.tongjiTrustee("订单列表页","订单点击查看",oid);
        logger.log({
            module: 'mytrustee_order_list',
            op: 'list_item_click',
            orderId: oid
        });
    };

    $(document).on("click","li.li-list",function(){
        //未支付的去订单支付页
        //其他状态去订单详情
        var oid = $(this).attr("data-oid");
        var statusNode = $(this).find('span.js-statu');

        if(statusNode.attr('data-type') == "NOT_PAY"){
            loadPage("/parent/wxpay/trusteecls_confirm.vpage?oid=",oid);
        }else{
            loadPage("/parent/trustee/orderdetail.vpage?oid=",oid);
        }
    });

    $(document).on("click",".goToPayBtn",function(e){
       //阻止事件冒泡
       e.stopPropagation();
       var oid = $(this).attr("data-oid");

        setTimeout(function(){
           location.href = "/parent/wxpay/trusteecls_confirm.vpage?oid="+oid;
        },200);

        $17.tongjiTrustee("订单列表页","付款按钮",oid);

        logger.log({
            module: 'mytrustee_order_list',
            op: 'pay_order_click',
            orderId: oid
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_order_list',
        op: 'order_list_pv'
    });
});