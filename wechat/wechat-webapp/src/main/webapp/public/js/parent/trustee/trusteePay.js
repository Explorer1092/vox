define(["jquery","$17",'jbox'],function($, $17){

    //点击提交购买
    $(document).on("click", "#buy_but", function(){
        //var $arrayStudent = $("#array-student").val();
        //var $arrayProduct = $("#array-product").val();
        //
        //if($arrayProduct != "YM_Reserve"){
        //    if($arrayProduct == "YM_Night"){
        //        $17.tongjiTrustee("B报名套餐页","确认支付");
        //    }else{
        //        $17.tongjiTrustee("A报名套餐页","确认支付");
        //    }
        //    if ($17.isBlank($arrayStudent)) {
        //        $17.jqmHintBox("请选择孩子");
        //        return false;
        //    }
        //}else{
        //    $17.tongjiTrustee("B支付预约体验费用","B体验费确认支付");
        //}
        //
        //if ($17.isBlank($arrayProduct)) {
        //    $17.jqmHintBox("请选择购买类型");
        //    return false;
        //}

        $("#orderPayForm").submit();
        return false;
    });


    //选择类型
    $(document).on("click", ".v-selectProductType", function(){
        var $this = $(this);
        var $typeArray = $this.find("input[type='hidden']").val().split("|");

        $("#array-product").val($typeArray[2]);//type
        $(".v-selectType").text($typeArray[3]);//名称
        $(".v-price").text($typeArray[0]);//原价
        $(".v-discountPrice").text($typeArray[1]);//优惠价

        $this.addClass("active").siblings().removeClass("active");

        if($typeArray[2] == "TJFX_NoonAndNight"){
            $17.tongjiTrustee("A报名套餐页","A午托和晚托");
        }
        if($typeArray[2] == "TJFX_Noon"){
            $17.tongjiTrustee("A报名套餐页","A午托");
        }
        if($typeArray[2] == "TJFX_Night"){
            $17.tongjiTrustee("A报名套餐页","A晚托");
        }
    });

    //选择孩子
    $(document).on("click", ".v-selectStudentType", function(){
        var $this = $(this);

        if($this.hasClass("succeed")){
            return false;
        }

        $("#array-student").val($this.data("id"));

        $this.addClass("active").siblings().removeClass("active");
    });

    //$(".v-selectProductType:first").click();
    //$(".v-selectStudentType:first").click();

    $(document).on("click",".js-payNowBtn",function(){
        var self = this;
        if(trusteeType.indexOf("TJFX") != -1){
            $17.tongjiTrustee("A订单详情页","立即支付");
        }
        if(trusteeType.indexOf("YM_Night") != -1){
            $17.tongjiTrustee("B订单详情页","立即支付");
        }
        location.href = "pay-trustee.vpage?oid=" + self.dataset.oid;
    });

    ga('trusteeTracker.send', 'pageview');
});