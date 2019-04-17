/**
 * Created by free on 2015/12/15.
 */
define(["jquery","$17","logger"],function($,$17,logger){
    var sid = shopId?shopId:$17.getQuery("shopId");
    $(document).on("click",".js-buyClassBtn",function(){
        setTimeout(function(){
            location.href = "skupay.vpage?shopId="+sid;
        },1000);

        $17.tongjiTrustee("报名成功_"+sid,"购买课程");
        logger.log({
            module: "open_class_book_success"+$17.getQuery("shopId"),
            op: "buy_class"
        });

    });

    $(document).on("click",".js-teacherInfoBtn",function(){
        setTimeout(function(){
            location.href = "teacherinfo.vpage?shopId="+sid;
        },1000);

        $17.tongjiTrustee("报名成功_"+sid,"公开课老师介绍");
        logger.log({
            module: "open_class_book_success"+$17.getQuery("shopId"),
            op: "open_class_teacher_desc_btn_click"
        });
    });

    logger.log({
        module: "open_class_book_success"+$17.getQuery("shopId"),
        op: "open_class_book_success_page_load"
    });

});