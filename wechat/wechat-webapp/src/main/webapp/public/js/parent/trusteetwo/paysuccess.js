/**
 * Created by free on 2015/12/15.
 */
define(["jquery","$17"],function($,$17){

    $(document).on("click",".js-continuePurchaseBtn",function(){
        setTimeout(function(){
            location.href = "registtrustee.vpage";
        },1000);

        //$17.tongjiTrustee("A首页点击","A获取验证码");
    });

});