define(['jquery','logger',"$17"],function($,logger,$17) {

    //试用版报名
    $(document).on("click",".js-tryBtn",function(){
        setTimeout(function(){
            location.href = "/parent/product/globalmath-trial.vpage";
        },100);
        logger.log({
            module: 'globalmath_activity',
            op: 'globalmath_try_btn_click'
        });
    });

    //标准版报名
    $(document).on("click",".js-standardBtn",function(){
        setTimeout(function(){
            location.href = "/parent/product/globalmath-standard.vpage";
        },100);
        logger.log({
            module: 'globalmath_activity',
            op: 'globalmath_standard_btn_click'
        });
    });

    logger.log({
        module: 'globalmath_activity',
        op: 'globalmath_activity_pv'
    })
});