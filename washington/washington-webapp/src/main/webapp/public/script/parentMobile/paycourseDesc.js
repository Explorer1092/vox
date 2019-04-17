/**
 * Created by free on 2016/05/05.
 */
define(['jqPopup'],function(){
        $(document).on("click",".js-tagBtn>li",function(){
            if(!$(this).hasClass("active")){
                $(this).addClass("active").siblings("li").removeClass("active");
                if($(this).data("type") == "course"){
                    $(".courseFeature-box").show();
                    $(".famousTeacher-box").hide();
                }else{
                    $(".courseFeature-box").hide();
                    $(".famousTeacher-box").show();
                }
            }
        });


        // 点击支付
        $.iosOnClick(".js-submitBtn", function(){
            var sid = $(this).data("sid");
            var orderType = $(this).data("type");

            $.post('/parentMobile/order/createtrusteeorder.vpage', {
                sid         : sid,
                trusteeType : orderType
            }, function(res){
                if(res.success){
                    return PM.doExternal("payOrder", res.orderId, "trustee");
                }else{
                    $.alert(res.info);
                }
            });
        });

        var h=$("#fc-main01").offset().top, fixedNav=$("#fixed-nav");


        $(document).scroll(function(){
            if($(this).scrollTop()>h){fixedNav.fadeIn("fast");}
            else{fixedNav.fadeOut("fast");}
        });

        $(".navFixed").find("a").click(function(){
            $(this).siblings().removeClass("active");
            $(this).addClass("active");
        });

        $(".fc-main01").on("click",function(){$(document).scrollTop(parseInt($("#fc-main01").offset().top)-152);});
        $(".fc-main02").on("click",function(){$(document).scrollTop(parseInt($("#fc-main02").offset().top)-152);});
        $(".fc-main03").on("click",function(){$(document).scrollTop(parseInt($("#fc-main03").offset().top)-152);});
        $(".fc-main04").on("click",function(){$(document).scrollTop(parseInt($("#fc-main04").offset().top)-152);});

});