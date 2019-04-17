/**
 * Created by free on 2015/12/15.
 */
define(["jquery","$17","jbox","logger"],function($,$17,jbox,logger){

    $(document).on("click",".js-openClassDetailBtn",function(){
        setTimeout(function(){
            location.href = "detail.vpage?shopId="+$17.getQuery("shopId");
        },1000);

        $17.tongjiTrustee("活动详情页_"+$17.getQuery("shopId"),"公开课介绍");
        logger.log({
            module: "open_class_info_"+$17.getQuery("shopId"),
            op: "open_class_detail_btn_click"
        });
    });

    $(document).on("click",".js-bookOpenClassBtn",function(){
        if(this.dataset.flag == "false"){
            setTimeout(function(){
                location.href = "reserve.vpage?shopId="+$17.getQuery("shopId");
            },1000);

            $17.tongjiTrustee("活动详情页_"+$17.getQuery("shopId"),"去报名-下");
            logger.log({
                module: "open_class_info_"+$17.getQuery("shopId"),
                op: "to_book_up_btn_click"
            });
        }else{
            $17.jqmHintBox("活动已过期");
        }
    });

    $(document).on("click",".js-bookOpenClassSocketBtn",function(){
        if(this.dataset.flag == "false") {
            setTimeout(function () {
                location.href = "reserve.vpage?shopId=" + $17.getQuery("shopId");
            }, 1000);

            $17.tongjiTrustee("活动详情页_"+$17.getQuery("shopId"),"去报名-上");
            logger.log({
                module: "open_class_info_"+$17.getQuery("shopId"),
                op: "to_book_down_btn_click"
            });
        }else{
            $17.jqmHintBox("活动已过期");
        }
    });

});