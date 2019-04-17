/*
 * 订单详情
 */
define(["jquery","$17","logger"], function ($,$17,logger) {
   $(document).on("click",".js-payNow",function(){
       var self = this;
       var tag = $17.getQuery('_from');

       if(tag != ""){
           logger.log({
               module: 'wxpay',
               op: 'confirm_wxpay_clickpaynow_from_'+tag
           });
       }

       logger.log({
           module: 'wxpay',
           op: 'confirm_wxpay_clickpaynow_from_total'
       });

       setTimeout(function(){
           location.href = $(self).attr("data-href");
       },1000);
   });

    //趣味数学或者世界趣味数学赛
    if(productType == "Stem101" || productType == "GlobalMath" && stemLevelName){
        $("p.js-learnCycle").hide();
        $(".js-levelName").html(stemLevelName);
        $("p.js-stemLevel").show();
    }

    //AfenTi支持手机文案
    if(productType == "AfentiExam"){
        $(".js-infoText").parent("div.info").css("text-align","left");
        $(".js-infoText").css('color',"blue").html("学生可以在手机和电脑使用本产品");
    }
});