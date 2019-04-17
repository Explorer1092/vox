<#-- @ftlvariable name="detail" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#-- @ftlvariable name="detail" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#if (currentUser.userType) == 3>
    <#assign userType="STUDENT"/>
<#elseif (currentUser.userType) == 1>
    <#assign userType="TEACHER"/>
<#elseif (currentUser.userType) == 8>
    <#assign userType="RSTAFF"/>
</#if>
<#import "../layout/layout.ftl" as temp />
<@temp.page index='' columnType="empty">
<#--订单确认-->
<div class="orderConfirm">
    <h3 class="oc-title">收货地址：</h3>
    <div class="addressInfo">
        <p>
            <span>${address.provinceName!''}${address.cityName!''}${address.countyName!''}${address.detailAddress!''}</span>
            <span>${address.receiver!''}(收)</span>
            <span>${receiverPhone!''}</span>
        </p>
        <a href="javascript:;" class="editBtn">修改地址<i class="editIcon"></i></a>
    </div>
    <h3 class="oc-title">订单信息：<span class="coupon_tip">（优惠券一旦使用，取消兑换时亦不予返还）</span></h3>
    <div class="orderInfo">
        <div class="left">
            <div class="orderPic">
                <#list productDetail.images as img>
                    <div class="coupon-number JS-couponNumber" style="display: none;"></div>
                    <#if img_index == 0>
                        <#if img.location?? && img.location?index_of("oss-image.17zuoye.com")!=-1>
                            <img src="${img.location!''}"/>
                        <#else>
                            <img src="<@app.avatar href="${img.location!''}"/>"/>
                        </#if>
                    </#if>
                </#list>
            </div>
            <div class="orderDesc">
                <p>${productDetail.productName!''}</p>
                <p><i class="w-gold-icon w-gold-icon-8"></i><span class="num vip_price_number JS-discountPrice"></span></p>
            </div>
        </div>
        <div class="right">
            兑换数量
            <div class="numOp">
                <span class="opLabel js-minusBtn">-</span>
                <input type="text" class="opNum" value="" maxlength="3">
                <span class="opLabel js-plusBtn">+</span>
            </div>
        </div>
    </div>
    <div class="exchangeInfo">
        合计：<i class="w-gold-icon w-gold-icon-8"></i><span class="num js-lastPrice"></span>
        <a href="javascript:void(0)" class="exchangeBtn" id="p_exchange_but_confirm">兑换</a>
    </div>
</div>
<script type="text/javascript">
    $(function (){
        var productDetail = new $17.Model({
            productNumber: $(".opNum"),
            maxExchangeNum: 100, //单次兑换数量最大值
            minExchangeNum: ${(productDetail.minBuyNums)!'1'} //单次兑换数量最小值
        });
        var p_num = $17.getQuery("num");
        $(".opNum").val(p_num);
        if(p_num <= productDetail.minExchangeNum){
            $(".js-minusBtn").addClass("disabled");
        }
        if(p_num >= productDetail.maxExchangeNum){
            $(".js-plusBtn").addClass("disabled");
        }
        productDetail.extend({
            checkNum: function () {
                var maxNum = productDetail.maxExchangeNum; //单次兑换数量最大量
                var changeNum = 1; //加减奖品数差值

                if (arguments[0] > maxNum || !$17.isNumber(arguments[0])) {
                    return 10;
                }


                if (arguments[1] == "minus") {
                    if (arguments[0] - changeNum < 0 || arguments[0] - changeNum > maxNum) {
                        return 1;
                    } else if (arguments[0] - changeNum == 0) {
                        return 1;
                    } else {
                        return arguments[0] - changeNum;
                    }
                } else {
                    if (arguments[0] + changeNum < 0 || arguments[0] + changeNum > maxNum) {
                        return arguments[0];
                    } else {
                        return arguments[0] + changeNum;
                    }
                }
            },
            init:function () {
                var $this = this;
                var roleTypes = "web_teacher_logs";
                <#if userType == 'STUDENT'>
                    roleTypes = "web_student_logs";
                </#if>

                var disCount = $17.getQuery("discountPrice");

                $(".JS-discountPrice").text(disCount);

                if ($17.getQuery("couponNumber") > 0) {
                    $(".JS-couponNumber").show().text($17.getQuery("couponNumber") + '折');
                } else {
                    <#if productDetail.tags?has_content && productDetail.tags != '' && productDetail.tags != '公益'>
                        $(".JS-couponNumber").show().text('${(productDetail.tags)!''}');
                    </#if>
                }

                $(".editBtn").on("click",function () {
                    var params = {
                        productId: $17.getQuery("productId"),
                        skuId: $17.getQuery("skuId"),
                        num: $this.productNumber.val(),
                        discountPrice: disCount,
                        couponNumber: $17.getQuery("couponNumber"),
                        RefId: $17.getQuery("RefId")
                    };

                   location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage?"+$.param(params);
                });

                var lastPrice = $this.productNumber.val() * disCount;
                $(".js-lastPrice").text(lastPrice);
                //兑换数量
                $(".js-minusBtn").on('click', function () {
                    var tempNumVal = $this.productNumber.val() * 1;
                    if($(this).hasClass("disabled")){
                        return false;
                    }else{
                        $this.productNumber.val($this.checkNum(tempNumVal, 'minus'));
                        //按钮样式
                        /*$(this).siblings().closest('span.js-plusBtn').removeClass("disabled");
                        if (tempNumVal == productDetail.minExchangeNum + 1) {
                            $(this).addClass("disabled");
                        }*/
                        if(tempNumVal <= productDetail.maxExchangeNum){
                            $(this).siblings(".js-plusBtn").removeClass("disabled");
                        }
                        if (tempNumVal <= productDetail.minExchangeNum+1) {
                            $(this).addClass("disabled");
                        }
                        lastPrice = $this.productNumber.val() * disCount;
                        $(".js-lastPrice").text(lastPrice);
                    }
                });

                $(".js-plusBtn").on('click', function () {
                    var tempNumVal = $this.productNumber.val() * 1;
                    if($(this).hasClass("disabled")){
                        return false;
                    }else{
                        $this.productNumber.val($this.checkNum(tempNumVal));
                        //按钮样式
                        /* $(this).siblings().closest('span.js-minusBtn').removeClass("disabled");
                         if (tempNumVal == productDetail.maxExchangeNum - 1) {
                             $(this).addClass("disabled");
                         }*/
                        if(tempNumVal >= productDetail.minExchangeNum){
                            $(this).siblings(".js-minusBtn").removeClass("disabled");
                        }
                        if (tempNumVal >= productDetail.maxExchangeNum-1) {
                            $(this).addClass("disabled");
                        }
                        lastPrice = $this.productNumber.val() * disCount;
                        $(".js-lastPrice").text(lastPrice);
                    }
                });

                var maxNumberCount = productDetail.minExchangeNum;
                $this.productNumber.on("keyup", function(){
                    var $that = $(this);
                    if( !$17.isNumber($that.val()) ){
                        $that.val(maxNumberCount);
                        return false;
                    }

                    if($that.val() > productDetail.maxExchangeNum){
                        $that.val(maxNumberCount);
                        return false;
                    }

                    maxNumberCount = $that.val();

                    lastPrice = $this.productNumber.val() * disCount;
                    $(".js-lastPrice").text(lastPrice);

                });

                //我要兑换
                $("#p_exchange_but_confirm").on('click', function () {
                    var $this = $(this);
                    var p_type_id = $17.getQuery("skuId");
                    var p_num = productDetail.productNumber.val() * 1;
                    var integralCount = $(".vip_price_number").text() * 1 * p_num;
                    var date = new Date();
                    $.post('/reward/order/createorder.vpage', {productId: '${productDetail.id!''}', skuId: p_type_id, quantity: p_num, couponUserRefId: $17.getQuery("RefId") || ''}, function (data) {
                        if (data.success) {
                            updateMyRewardCount('plus');
                            YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "button_confirm_exchange_click", s0: "${(productDetail.id)!0}",s1:"${(productDetail.productType)!0}"} );
                            var successHtml = '<div>成功兑换${productDetail.productName!}'+ p_num + '件</div>';
                            var states = {
                                state0: {
                                    title: '',
                                    html: successHtml,
                                    buttons: {'查看兑换记录': false, '确定': true },
                                    focus: 0,
                                    submit: function (e, v, m, f) {
                                        e.preventDefault();
                                        if (v) {
                                            location.href = '/reward/product/detail.vpage?productId='+${productDetail.id!''};

                                        } else {
                                            location.href = '/reward/order/myorder.vpage';
                                        }
                                    }
                                }
                            };
                            $.prompt(states);
                            if (p_num == 0) {
                                $.prompt('请选择您要兑换奖品的数量', {
                                    title: "",
                                    buttons: {"知道了": true},
                                    submit: function (e, v) {
                                        location.reload();
                                    }
                                });
                                return false;
                            }
                            if (p_num > productDetail.maxExchangeNum) {
                                $.prompt('您输入的奖品兑换数量不正确，请重新输入', {
                                    title: "",
                                    buttons: {"知道了": true},
                                    submit: function (e, v) {
                                        location.reload();
                                    }
                                });
                                return false;
                            }
                        } else {
                            var infoBtn = {"知道了": true};
                            var infoUrl = function () {
                                $.prompt.close();
                            };
                            if (!$17.isBlank(data.bindMobile)) {
                                infoBtn = {"去绑定": true};
                                infoUrl = function () {
                                    <#if userType == 'STUDENT'>
                                        window.open('${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=mobile', "_blank");
                                    <#elseif userType == 'TEACHER'>
                                        window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/securitycenter.vpage', "_blank");
                                    <#elseif userType == 'RSTAFF'>
                                        window.open('/rstaff/center/edit.vpage', "_blank");
                                    </#if>
                                };
                            }

                            if (!$17.isBlank(data.authentication)) {
                                infoBtn = {"去认证": true};
                                infoUrl = function () {
                                    window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', "_blank");
                                };
                            }

                            if (!$17.isBlank(data.address)) {
                                infoBtn = {"去填写": true};
                                infoUrl = function () {
                                    <#if userType == 'TEACHER'>
                                        var params = {
                                            productId: $17.getQuery("productId"),
                                            skuId: $17.getQuery("skuId"),
                                            num: productDetail.productNumber.val(),
                                            discountPrice: disCount,
                                            couponNumber: $17.getQuery("couponNumber"),
                                            RefId: $17.getQuery("RefId")
                                        };
                                    location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage?"+$.param(params);
                                    <#elseif userType == 'RSTAFF'>
                                        window.open('/rstaff/center/edit.vpage', "_blank");
                                    </#if>
                                };
                            }
                            $.prompt(data.info, {
                                title: "",
                                buttons: infoBtn,
                                submit: infoUrl
                            });
                        }
                    });


                    YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "o_vJCuwRSv", s0: "${(productDetail.id)!0}", s1: "${(currentUser.userType)!0}"});
                });
            }
        }).init();
    })
</script>
</@temp.page>