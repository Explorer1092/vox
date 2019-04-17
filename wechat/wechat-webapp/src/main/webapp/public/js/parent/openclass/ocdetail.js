/**
 * Created by free on 2015/12/15.
 */
define(["jquery","$17","logger"],function($,$17,logger){

    $(document).on("click",".js-bookOpenClassBtn",function(){
        setTimeout(function(){
            location.href = "reserve.vpage?shopId="+$17.getQuery("shopId");
        },1000);

        $17.tongjiTrustee("公开课介绍_"+$17.getQuery("shopId"),"去报名");
        logger.log({
            module: "open_class_detail_"+$17.getQuery("shopId"),
            op: "book_open_class_btn_click"
        })
    });

    logger.log({
        module: "open_class_detail_"+$17.getQuery("shopId"),
        op: "book_open_class_page_load"
    })

});