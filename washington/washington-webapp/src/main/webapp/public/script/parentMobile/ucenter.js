/* global define : true , $:true, PM:true */
/**
 *  @date 2015/9/19
 *  @auto liluwei
 *  @description 该模块主要负责个人中心的逻辑处理
 */

define([], function(){
    "use strict";

    // 常见问题
    var questionTab = ".doQuestionTab";

    $.iosOnClick(
        questionTab,
        function(){
            var $self = $(this);

            if($self.hasClass("active")){
                $(this).removeClass("active").find(".ps-title").removeClass("up");
                return ;
            }

            $self.closest(".doQuestionTabs").find('.doQuestionTab').removeClass("active").find(".ps-title").removeClass("up");
            $self.addClass("active").find(".ps-title").addClass("up");
        }
    );

    /*
        家长App－FAQ列表默认不展开
        http://project.17zuoye.net/redmine/issues/36339

        $(function(){
            $(questionTab).filter(":first").click();
        });
    */

    //没有找到对应的问题？联系在线客服 点击
    $(".do_parentApp_entrance").on("click",function(e){
        var $self = $(e.target);
        var href = $self.attr("href").trim();
        e.preventDefault();

        PM.doExternal('openSecondWebview',JSON.stringify({
             shareType    : 'NO_SHARE_VIEW',
             shareContent : '',
             shareUrl     : '',
             type         : '',
             url          : href
         }))
    });
});
