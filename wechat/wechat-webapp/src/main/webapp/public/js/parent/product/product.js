define(["jquery","$17"], function ($,$17) {
    var selectedStudentId = $('#child_list_box').data('selected_student'),savePrice = $('#savePrice');

    var priceDay = 0; //产品的基准价格
    function priceFun(days,realityPrice){
        $('#dayPrice').text((realityPrice/days).toFixed(2));
        savePrice.text((priceDay*days-realityPrice).toFixed(2));
        if((priceDay*days-realityPrice) < 0){
            savePrice.closest('p').hide();
        }else{
            savePrice.closest('p').show();
        }

    }

    //选择周期
    $(document).on('click', '#cycle_list_box li', function () {
        var $this = $(this);
        $this.addClass('active').siblings().removeClass('active');
        var cycleNum = $this.data('cycle');
        $(".price_box").text(productMap[cycleNum] + "元");
        $("#array-product").val($this.data('product_id')); //选择的周期数

        priceFun(cycleNum,productMap[cycleNum]);
    });

    var productMap = {};
    //选择孩子
    $(document).on('click', "#child_list_box li", function () {
        var $this = $(this);
        $this.addClass('active').siblings().removeClass('active');

        //动态生成周期数
        var product = $this.data('products');

        //计算每天的价格
        priceDay = (product[0].price/product[0].period).toFixed(2);

        var key = [],dis = 0;
        var periodHtml = '';
        for (var i = 0; i < product.length; i++) {
            key.push(product[i].period);
            productMap[product[i].period] = product[i].price;
            //折扣
            dis = (product[i].price/(product[i].period * priceDay)*10).toFixed(1);
            if(i == 0 || dis >= 10){
                periodHtml += "<li class='no' data-cycle=" + product[i].period + " data-product_id=" + product[i].productId + "><p>" + product[i].period + "天</p></li>";
            }else{
                periodHtml += "<li data-cycle=" + product[i].period + " data-product_id=" + product[i].productId + "><p>" + product[i].period + "天</p><span >"+dis+" <b>折</b></span></li>";
            }


        }
        $("#cycle_list_box").html(periodHtml).find("li:first").trigger('click');
        $("#array-student").val($this.data("student_id"));  //选择的孩子ID
    });

    //默认选中学生
    if ($17.isBlank(selectedStudentId)) {
        $("#child_list_box").find("li:first").click();
    } else {
        $("#child_list_box").find("li[data-child_id=" + selectedStudentId + "]").trigger('click');
    }

    //立即购买
    $("#buy_but").on('click', function () {
        var childId = $("#child_list_box li.active").data("student_id"); //child_id
        if ($17.isBlank(childId)) {
            $17.jqmHintBox("选择孩子");
            setTimeout(function () {
                $17.backToTop(1000);
            }, 1600);
            return false;
        } else {
            //$17.blockUI();
            document.forms[0].submit();
        }


    });

    $("li[data-cycle='90']").trigger('click');

});