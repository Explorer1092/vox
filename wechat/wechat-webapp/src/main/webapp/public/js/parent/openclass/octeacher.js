/**
 * Created by free on 2015/12/15.
 */
define(["jquery","$17","logger"],function($,$17,logger){

    var sid = shopId?shopId:$17.getQuery("shopId");

    $(document).on("click",".js-buyClassBtn",function(){
        setTimeout(function(){
            location.href = "skupay.vpage?shopId="+sid;
        },1000);

        $17.tongjiTrustee("老师介绍_"+sid,"购买课程");
        logger.log({
            module: "open_class_teacher_"+$17.getQuery("shopId"),
            op: "buy_class_btn_click"
        });
    });
});