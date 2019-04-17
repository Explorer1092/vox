$(function(){
    /*--tab切换--*/
    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        $this.addClass("the").siblings().removeClass("the");
        $this.parent().siblings(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
    });

    /*--dropdown--*/
    var dropLayers=$(".dropdown-layer"),
        windowHeight=$(window).height(),
        mask=$(".mask");
    $(".dropdown").on("click",function(e){
        e.stopPropagation();
        var $this=$(this),
            theLayer=$("."+$this.data().bind);
        if(theLayer.hasClass("displayed")){
            theLayer.removeClass("displayed").slideUp("fast",function(){
                mask.hide();
            });
        } else{
            dropLayers.removeClass("displayed").slideUp("fast",function(){
                mask.hide();
            });
            theLayer.addClass("displayed").slideDown("fast",function(){
                mask.show();
            });
        }
    });
    $(document).on("click",function(){
        dropLayers.removeClass("displayed").slideUp("fast",function(){
            mask.hide();
        });
    });
    dropLayers.on("click",function(e){
        e.stopPropagation();
        if($(e.target).hasClass("js-sort")){
            dropLayers.removeClass("displayed").slideUp("fast",function(){
                mask.hide();
            });
        }
    });

    /*--关注按钮--*/
    $(document).on("click",".mark",function(e){
        e.stopPropagation();
        var $this=$(this);
        var followed = !$this.hasClass("marked");
        var schoolId = $this.data().sid;
        $.post('update_follow.vpage', {schoolId:schoolId, followed:followed}, function(res){
            if (res.success) {
                $this.toggleClass("marked");
            } else {
                alert(res.info);
            }
        });
    });
    /*--顶部固定--*/
    var fixed=$(".fixed");
    if(fixed.length){
        fixed.height($(window).height()-fixed.offset().top).css("overflow","scroll");
    }

    /*--模拟a标签--*/
    $("[data-href]").on("click",function(){
        location.href=$(this).data().href;
    });
});
