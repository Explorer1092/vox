<#import "module.ftl" as temp />
<@temp.page title='myorder'>
<!--//start--><#--待发货-->
<div class="w-content">
    <div class="t-prizesCenter-box">
        <ul class="pc-tab">
            <li class="active"><a href="/reward/order/myorder.vpage"><span class="h-arrow"></span>待发货</a></li>
            <li><a href="/reward/order/history.vpage"><span class="h-arrow"></span>已发货</a></li>
        </ul>
        <div class="my_order_box">
    <#if orderMapList?has_content>
        <#list orderMapList as c>
            <div class="my_order_inner_box">
                <#if c.discount?has_content && c.discount lt 1>
                    <div class="coupon-number JS-couponNumber">${c.discount * 10}折</div>
                </#if>
                <div class="my_order_product_box clearfix">
                    <#if c.image??>
                        <#if c.image?index_of("oss-image.17zuoye.com")!=-1>
                            <img src="${c.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" class="float_left" />
                        <#else>
                            <img src="<@app.avatar href="${c.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />" class="float_left" />
                        </#if>
                    <#else>
                        <img src="<@app.avatar href="${c.image!''}" />" class="float_left" />
                    </#if>
                    <dl class="float_left">
                        <dt>
                        <p>${c.productName!''}<#if currentUser.userType == 1 && c.price lte 0><i class="tag"></i></#if></p>
                        <#if c.skuName?has_content><p class="pc-time">兑换款式：${c.skuName!''}</p></#if>
                        <p class="pc-time">
                            <#if (c.source!'') == 'gift'>抽奖<#elseif (c.source!'') != 'gift' && c.price != 0>兑换<#elseif (c.source!'') != 'gift' && c.price == 0>中奖</#if>时间：${c.createTime!''}
                        </p>
                        </dt>
                        <dd class="clearfix">
                            <span class="float_left">
                                <#if c.statusCode == 'SUBMIT'>
                                    <#--<i class="point_icon_1 float_left"></i>-->
                                <#elseif c.statusCode == 'IN_AUDIT'>
                                    <i class="point_icon_2 float_left"></i>
                                <#elseif c.statusCode == 'PREPARE'>
                                    <i class="point_icon_3 float_left"></i>
                                <#elseif c.statusCode == 'SORTING'>
                                    <i class="point_icon_3 float_left"></i>
                                </#if>

                                <#if (c.source!'') == 'gift' && c.status == '兑换成功'>
                                    抽奖成功
                                <#else>
                                    ${c.status!""}
                                </#if>

                                <#if c.statusCode == 'EXCEPTION'>
                                    <i class="point_icon_1 float_left"></i>
                                    您的账号使用异常，为保护账号安全，已将您兑换的奖品取消发货。请联系客服400-160-1717解决。
                                <#elseif c.statusCode == 'PREPARE'>
                                    配货过程中会对您的信息进行审核，审核通过后您的奖品会在10日内寄出，请耐心等待！
                                </#if>
                            </span>
                        </dd>
                    </dl>
                    <#if c.source?has_content && c.source == 'power_pillar'>
                        <p class="float_left my_order_price_box">能量柱奖励</p>
                    <#else>
                        <p class="float_left my_order_price_box"><span>价格：</span><strong class="J_red orderPrice">${c.totalPrice}</strong><@ftlmacro.garyBeansText/></p>
                    </#if>

                    <p class="float_left pc-number"><span class="float_left">
                        数量：</span><span class="my_order_number float_left">${c.quantity!''}</span>
                        <#--<#if c.statusCode == 'SUBMIT'>-->
                            <#--<#if c.price lte 0>-->
                            <#--<#else>-->
                                <#--<a data-order_id="${c.orderId!''}" href="javascript:void(0);" class="float_left my_order_revise_btn editProductNum">修改</a>-->
                            <#--</#if>-->
                        <#--</#if>-->
                    </p>
                    <#--此处之前使用statusCode来控制展示取消按钮，后由于抽奖需求增加returnable字段-->
                    <#--<#if c.statusCode == 'SUBMIT'>-->
                    <#if c.returnable>
                        <a data-order_id="${c.orderId!''}" href="javascript:void(0);" class="float_right deleteOrderBut"><i class="my_order_delete_btn"></i>取消</a>
                    </#if>
                </div>
            </div>
        </#list>
    <#else>
        <div class="no_order_box" style=" border: 1px solid #f5e6d6; margin-top: -1px;">
            <div class="no_order_bg"></div>
            <p class="btn_box font_twenty">您还没有待发货奖品呢</p>
            <p class="btn_box J_light_gray" style="padding-top:6px;">继续加油吧！</p>
        </div>
    </#if>
        </div>
    </div>
</div>
<!--end//-->

<script id="t:editProductNumBox" type="text/html">
    <p class="J_deep_red btn_box font_twenty" style="border-bottom:1px solid #f0f0f0; padding:0px 0 20px 0; margin-bottom: 20px;">请选择你要兑换的数量吧！</p>
    <div class="home_sales_box clearfix" style="height:50px;">
        <div class="home_sales_right">
            <dl class="clearfix" style="width: 200px; margin:0 auto;">
                <dt style="width:50px;">数量：</dt>
                <dd style="width:120px;">
                    <strong class="minusBtn disabled">-</strong>
                    <input type="text" maxlength="3" value="<%=ordernumber%>" name="productNumber" class="J_gray tempNum" readonly="readonly"/>
                    <strong class="plusBtn">+</strong>
                </dd>
            </dl>
        </div>
    </div>
</script>

<script type="text/javascript">
    function updateIntegralNumber(count){
        var box = $("#integral_total");
        var total = box.text() * 1;
        box.text(total - count*1);
    }

    function checkNum (){
        var maxNum = 100; //单次兑换数量最大量
        var changeNum = 1; //加减奖品数差值

        if(arguments[0] > maxNum || !$17.isNumber(arguments[0])){
            return 10;
        }

        if(arguments[1] == "minus"){
            if(arguments[0] - changeNum < 0 || arguments[0] - changeNum > maxNum){
                return 1;
            }else if (arguments[0] - changeNum == 0){
                return 1;
            }else{
                return arguments[0] - changeNum;
            }
        }else{
            if(arguments[0] + changeNum < 0 || arguments[0] + changeNum > maxNum){
                return arguments[0];
            }else{
                return arguments[0] + changeNum;
            }
        }
    }

    $(function(){

        //删除
        $(".deleteOrderBut").on('click', function(){
            var $this = $(this);
            $.prompt({
                state0:{
                    html : "确定要取消兑换该奖品吗？<br />取消后将返还对应奖品的<@ftlmacro.garyBeansText/>",
                    buttons : {'否': false, '是': true},
                    focus : 1,
                    submit : function(e,v){
                        if(v){
                            var orderId = $this.data('order_id');
                            var ordernumber = $this.siblings().find('.my_order_number').text() * 1;
                            var price = $this.siblings().find('.orderPrice').text() * 1;
                            if($this.hasClass('loading')){return false}
                            $this.addClass('loading');
                            $.post('/reward/order/removeorder.vpage', {orderId : orderId}, function(data){
                                if(data.success){
                                    $this.closest('.my_order_product_box').remove();
                                    updateIntegralNumber(ordernumber*price);
                                    updateMyRewardCount('minus');
                                    //删除最后一个时重新加载页面
                                    location.reload();
                                }else{
                                    $.prompt(data.info, {
                                        title : "",
                                        buttons : {"知道了" : true},
                                        submit : function(){
                                            $.prompt.close();
                                        }
                                    });
                                }
                                $this.removeClass('loading');
                            });
                        }else{
                            $.prompt.close();
                        }
                    }
                }
            });
            YQ.voxLogs({ module : "m_2ekTvaNe", op : "o_Y6zHdyY1", s1: "${(currentUser.userType)!0}"});
        });

        //修改产品数量
        $(".editProductNum").on('click', function(){
            var $this = $(this);
            var orderId = $this.data('order_id');
            var ordernumber = $this.siblings('.my_order_number').text() * 1;
            var price = $this.siblings().find('.orderPrice').text() * 1;
            $.prompt(template("t:editProductNumBox",{ordernumber : ordernumber}),{
                title : '',
                buttons : { '确定' : true},
                focus : 0,
                submit : function(e,v){
                    e.preventDefault();
                    if(v){
                        var tempNumVal = $(".tempNum").val() * 1;
                        $.post('/reward/order/updateorder.vpage', {orderId : orderId, quantity: tempNumVal}, function(data){
                            if(data.success){
                                $.prompt.close();
                                //数据更新
                                //$this.siblings('.my_order_number').text(tempNumVal);
                                //updateIntegralNumber();
                                location.reload();
                            }else{
                                $.prompt(data.info, {
                                    title : "",
                                    buttons : {"知道了" : true},
                                    submit : function(){
                                        $.prompt.close();
                                    }
                                });
                            }
                        });

                    }else{
                        $.prompt.close();
                    }
                },
                loaded : function(){
                    $(".minusBtn").on('click', function(){
                        var tempNumVal = $(".tempNum").val() * 1;
                        $(".tempNum").val(checkNum(tempNumVal,'minus'));
                        if($(".tempNum").val() == 1){
                            $(this).addClass("disabled");
                        }
                    });
                    $(".plusBtn").on('click', function(){
                        var tempNumVal = $(".tempNum").val() * 1;
                        $(".tempNum").val(checkNum(tempNumVal));
                        if($(".tempNum").val() > 1){
                            $(".minusBtn").removeClass("disabled");
                        }
                    });
                }
            });

            YQ.voxLogs({ module : "m_2ekTvaNe", op : "o_y3SAdC4V", s1: "${(currentUser.userType)!0}"});
        });

        YQ.voxLogs({module: "m_2ekTvaNe", op: "o_DgbiZbIM", s1: "${(currentUser.userType)!0}"});
    });
</script>
</@temp.page>