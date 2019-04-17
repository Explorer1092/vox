/*
 * Created by free on 2016/02/24
 */
define(["jquery","$17","logger"],function($,$17,logger){
    $(document).on("click",".js-signUpBtn",function(){
        setTimeout(function(){
            location.href = "/parent/trustee/index.vpage";
        },200);
        $17.tongjiTrustee("开学活动页","立即报名按钮","活动桥页");
        logger.log({
            module: 'mytrustee_countdown',
            op: 'signUp_btn_click'
        });
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_countdown',
        op: 'countdown_page_pv'
    });
});