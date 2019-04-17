/* global define : true, PM : true, $:true */
/**
 *  @date 2015/12/29
 *  @auto liluwei
 *  @description 该模块主要寒假作业相关
 */
require(["ajax", "getKids", "progress", "jqPopup"], function(promise, getKids){
    'use strict';

    var sid = PM.sid;

    var init = function(){

        var drawProgress = function(){
            $(".doProgress").progress().each(function(index, canvas){
                // TODO 因为这里实在无法抽出来做成一个抽象模块， 暂且在这里统一服务我们目前遇到的问题
                var $canvas = $(canvas),
                    ctx = canvas.getContext('2d'),
                    percent = "" + $canvas.data('percent'),
                    fillZero = ("00" + percent).substr(percent.length),
                    text = fillZero + "/" + ($canvas.data("all") || 30);

                ctx.save();
                ctx.font="38px Arial";
                ctx.fillStyle="#2e2f30";
                ctx.fillText(text, 34, 78);
                ctx.font="18px Arial";
                ctx.fillText("进度", 60, 104);
                ctx.restore();

            });
        };

        $("#mockBody").waitSomething(
            "bodyIsShow",
            function(){
                return $("#mockBody").is(":visible");
            },
            drawProgress
        );

        // 领取学豆奖励
        $.iosOnClick(
            ".doAcceptReward",
            function(){
                var $self = $(this);

                promise(
                    "/parentMobile/homework/completereward.vpage",
                    {
                        sid : sid,
                        homeworkType : $.trim($self.data("homework_type"))
                    },
                    "POST"
                )
                    .done(function(res){
                        if(res.success){

                            $.alert("领取成功");

                            $self
                                .text(function(index, text){
                                    return "已" + text;
                                })
                                .addClass("btn-gary");

                            return ;
                        }

                        $.alert(res.info);

                    });
            }
        );

    };

    if(+PM.no_select_kids){
        init();
    }else{

        PM.doExternal("goHome", "0");

        getKids(
            '.parentApp-mock-background',
            function(select_sid, event, default_selected_index){
                if(default_selected_index === undefined || default_selected_index === -1){
                    location.href = "/parentMobile/homework/vhindex.vpage?" + $.param(
						$.extend({
							sid : select_sid
						}, PM.client_params)
					);
                }else{
                    init();
                }
            },
            function($ul){
                var $select_li = sid && $ul.find('[data-sid="' + sid + '"]'),
                    index = $select_li ? $select_li.index() : -1,
                    click_param = [index];

                if(index === -1){
                    $select_li = $ul.find('li').eq(0);
                }

                $select_li.trigger('click', click_param);

            }
        )
    }


});
