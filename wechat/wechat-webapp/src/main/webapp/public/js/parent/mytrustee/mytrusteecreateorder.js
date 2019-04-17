/*
 * Created by free on 2016/01/04
 */
define(["jquery","$17","logger","jbox"],function($,$17,logger){
    var gid = $17.getQuery("gid");
    var sid = $17.getQuery("sid");
    var oid = $17.getQuery("oid");
    var goodName = $(".js-goodName").html();
    var perMobile = $("#perMobile").val();
    var maxPeriodNum = $("#maxPeriodNum").val();

    //购买月份选定后进行价格渲染
    var displayDesrc = function() {
        var singlePrice;
        if($('li[data-type="experience"]').length){
            singlePrice = $(".js-expPrice").html();
            $(".js-totalPrice").html(singlePrice);
        }
        if($('li[data-type="common"]').length){
            singlePrice = $(".js-singlePrice").html();
            $(".js-totalPrice").html((singlePrice*($('#buyMonth').val())).toFixed(2));
        }
    };

    //检测数值
    var isNumber = function(value){
        var reg = /^[0-9]+$/;
        if($17.isBlank(value) || !reg.test(value)){
            return false;
        }
        return true;
    };

    //确认并支付
    var payItem = function(bid,flag){
        if($("#buyMonth").val() != 0){
            var count;
            if($('li[data-type="experience"]').length){
                //体验数量约定
                //count = parseInt($(".js-expMaxPeriodNum").html());
                count = 1;
            }
            if($('li[data-type="common"]').length){
                count = parseInt($("#buyMonth").val());
            }

            var data = {
                studentId: sid,
                goodsId: gid,
                branchId: bid,
                count: count
            };

            if(flag){
                //续费支付
                data.oid = oid;
                if($("#orderMobile").val() != perMobile){
                    data.mobile = $("#orderMobile").val();
                }
            }else{
                data.mobile = $("#orderMobile").val();
            }
            $.post("createorder.vpage",data,function(result){
                if(result.success){

                    var orider_id = result.oid;

                    if(window.isFromParent){
                        window.external.payOrder(String(orider_id), "trusteecls");
                        return ;
                    }

                    location.href = "/parent/wxpay/trusteecls_confirm.vpage?oid="+ orider_id;
                }else{
                    $17.jqmHintBox(result.info);
                }
                console.log(result);
            });
            console.log(data);
        }else{
            $17.jqmHintBox("请选择购买时长");
        }
        $17.tongjiTrustee("提交订单",goodName+"+提交订单",gid);
        logger.log({
            module: 'mytrustee_create_order',
            op: 'create_submit_order',
            goodId: gid
        });
    };

    /****************事件交互***********/

    //时长输入
    $(document).on('blur', '#buyMonth', function(){
        var self = this;
        var currentBeanVal = parseInt($(self).val());

        $(self).siblings(".js-reduceMonth").removeClass("disabled");
        $(self).siblings(".js-addMonth").removeClass("disabled");

        if(currentBeanVal%1 != 0){
            currentBeanVal = 1 - currentBeanVal%1 + currentBeanVal ;
            $(self).val(currentBeanVal)
        }

        if(currentBeanVal < 2 || !isNumber(currentBeanVal)){
            $(self).val(1);
            $(self).siblings(".js-reduceMonth").addClass("disabled");
        }else if(currentBeanVal >= maxPeriodNum){
            $(self).val(maxPeriodNum);
            $(self).siblings(".js-addMonth").addClass("disabled");
        }

        displayDesrc();
    });

    //减时长
    $(document).on('click', '.js-reduceMonth', function(){
        var self = this;
        var currentNumNode = $(self).siblings("input");
        var currentBeanVal = parseInt(currentNumNode.val());
        if($(self).hasClass("disabled")){
            return false;
        }

        var clazzNum = currentBeanVal - 1;

        if(clazzNum < 2 || !isNumber(clazzNum) ){
            clazzNum = 1;
            $(self).addClass("disabled");
        }

        currentNumNode.val(clazzNum);
        $(self).siblings(".js-addMonth").removeClass("disabled");

        displayDesrc();
    });

    //加时长
    $(document).on('click', '.js-addMonth', function(){
        var self = this;
        var currentNumNode = $(self).siblings("input");
        var currentBeanVal = parseInt(currentNumNode.val());
        if($(self).hasClass("disabled")){
            return false;
        }

        var clazzNum = currentBeanVal + 1;

        if(clazzNum >= maxPeriodNum || !isNumber(clazzNum) ){
            clazzNum = maxPeriodNum;
            $(self).addClass("disabled");
        }

        currentNumNode.val(clazzNum);
        $(self).siblings(".js-reduceMonth").removeClass("disabled");

        displayDesrc();
    });

    $(document).on("click",".js-payAndBuy",function(){
        //检测手机号
        var phoneNo = $("#orderMobile").val();
        var bid = $(this).attr("data-bid");
        if($17.isBlank(phoneNo)){
            $17.jqmHintBox("手机号不能为空");
            return;
        }
        //带出了手机号
        if(!$17.isBlank(perMobile)){
            //
            payItem(bid,true);
        }else{
            if($17.isMobile(phoneNo)){
                payItem(bid);
            }else{
                $17.jqmHintBox("请输入正确格式的手机号码");
            };
        }
    });

    //初始化count状态
    if($("#buyMonth").val() == 1){
        $(".js-reduceMonth").addClass("disabled");
    }
    if($("#buyMonth").val() >= maxPeriodNum){
        $("#buyMonth").val(maxPeriodNum);
        $(".js-addMonth").addClass("disabled");
    }
    //渲染价格
    displayDesrc();

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_create_order',
        op: 'create_order_pv',
        goodId: gid
    });
});
