/*
 * 领取登录奖励
 */
define(["jquery","logger"], function ($,logger) {
    var do_track = function(op){
        logger.log({
            module: "receivereward",
            op: op
        });
    };

    $(document).on("click", ".doReceive", function(){
        do_track("loginjzt_reward");
    });

    do_track("receivereward_list");

});

