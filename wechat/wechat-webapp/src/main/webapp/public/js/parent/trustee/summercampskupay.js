define(['jquery', '$17','jbox'], function ($, $17) {
    var studentListBox = $('#studentListBox'),
        productListBox = $('#productListBox'),
        payPriceBox = $('#payPriceBox'),
        paySubmitBtn = $('#paySubmitBtn'),
        payAllBtn = $('#payAllBtn');

    /*选择孩子*/
    studentListBox.find('li').on('click', function () {
        var $this = $(this);
        var studentId = $this.data('student_id');
        $this.addClass('active').siblings().removeClass('active');
        productListBox.find('li').removeClass("disabled");
        payAllBtn.hide();

        $.post("/parent/trustee/loadstudentorders.vpage", {studentId: studentId}, function (data) {
            if(data.success){
                productListBox.find('li').each(function(){
                    var name = $(this).data('name');
                    for(var i = 0 ; i < data.orderRecords.length; i++){
                        if(name == data.orderRecords[i].trusteeType){
                            $(this).addClass("disabled");
                        }
                    }
                });
                productListBox.find("li:not(.disabled)").eq(0).click();
                if(productListBox.find("li:not(.disabled)").length == 0){
                    payAllBtn.show();
                }
            }else{
                $17.jqmHintBox("数据加载失败");
            }
        });

    }).eq(0).click();

    //选择周期
    productListBox.find("li").on('click', function () {
        var $this = $(this);
        if($this.hasClass('disabled')){return}
        var name = $this.data('name');
        var price = $this.data('price');
        $this.addClass('active').siblings().removeClass('active');
        payPriceBox.text(price + "元");
    });

    // 家长通强制返回首页
    if(window.isFromParent){
        window.external.goHome("0");
    }

    //确认并支付
    paySubmitBtn.on('click', function () {
        var sid = studentListBox.find('li.active').data('student_id');
        var name = productListBox.find('li.active').data('name');
        if ($17.isBlank(sid)) {
            $17.jqmHintBox("请选择孩子");
            return false;
        }
        if ($17.isBlank(name)) {
            $17.jqmHintBox("请选择种类");
            return false;
        }
        $('input[name=trusteeType]').val(name);
        $('input[name=sid]').val(sid);

        if(window.isFromParent){
            return $.post('/parent/trustee/orderforapp.vpage', {
                        sid         : sid,
                        trusteeType : name
                    })
					.done(function(res){
						if(res.success){
                            return window.external.payOrder("" + res.orderId, "trustee");
						}

                        $17.jqmHintBox(res.info);
					});
        }

        $('#payForm').submit();
    });
});
