define(['jquery'], function($){
    //模拟支付
    $(document).on("click", "#paymentTestBox button", function(){
        var $this = $(this);
        var $orderId = $this.data("order_id");
        var $ajaxType = $this.data("ajax_type");

        if($this.hasClass("back")){
            if (returnUrl != "") {
                window.location.href = returnUrl;
            } else {
                window.history.back()
            }
            return false;
        }

        $.ajax({
            url : testLinkFiles + "payfortest-"+ $ajaxType +".vpage",
            type : 'post',
            data : {
                oid : $orderId
            },
            success : function(data){
                if(data.success){
                    $("#testBoxInfo").text($ajaxType);
                    onDialog({info: "模拟成功" + $ajaxType});
                }else{
                    onDialog({info: "模拟失败" + $ajaxType});
                }
            },
            error : function(){
                onDialog({info: "请求发送失败"});
            }
        });
    });

    if(returnUrl){
        $(".returnUrl").text(returnUrl);
    }
});