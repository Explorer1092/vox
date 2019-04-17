define(['jquery', 'weui'], function($){
    var giftId = null;

    $(document).on("click", ".JS-selectGift", function(){
        var $self = $(this);

        giftId = $self.attr("data-gift_id");

        $self.children("div").addClass("active");
        $self.siblings().children("div").removeClass("active");
    });

    $(document).on("click", ".JS-submitGift", function(){
        var $self = $(this);
        var sendGiftTime = $(".JS-datetimeInput").val();

        if($self.hasClass("dis")){
            return false;
        }

        if(sendGiftTime == null || sendGiftTime == ""){
            $.alert("请填写方便收货时间！");
            return false;
        }

        if(giftId == null){
            $.alert("请先选择礼物哦!");
            return false;
        }

        $self.addClass("dis");
        $.confirm({
            text: '确认领取该礼物吗？',
            onOK: function () {
                $.post("/teacher/gz/gift/book.vpage", {
                    gift_id : giftId,
                    receive_time : sendGiftTime
                }, function(data){
                    if(data.success){
                        $.alert("领取成功！<br/>请静待送货到学校哦~", function(){
                            location.reload();
                        });
                    }else{
                        $.alert(data.info || "非系统抽取的老师暂时无法领取哦，<br/>请期待后续其他活动吧！");
                    }
                    $self.removeClass("dis");
                });
            },
            onCancel: function () {
                $self.removeClass("dis");
            }
        });
    });

    $(document).on("click", ".JS-authIn", function(){
        $.alert("<div style='text-align: left'>1、设置姓名并绑定手机<br/>2、至少8名学生完成3次作业<br/>3、至少3名学生绑定手机</div>", "认证条件");
    });
});