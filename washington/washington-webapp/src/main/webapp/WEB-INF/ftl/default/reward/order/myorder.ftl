<#import "module.ftl" as temp />
<@temp.page title='myorder'>
    <#assign currentMonth = .now?string('M') />
    <#assign currentDay = .now?string('d') />
    <div class="my_order_box">
        <#if ftlmacro.isInSummerRange >
            <p class="my_order_title">暑假我想要的奖品 <span style="float: right; font-size: 12px;">注：奖品质量问题，请收到后当月联系客服，过期不再受理！</span></p>
        <#elseif ftlmacro.isInWinterRange>
            <p class="my_order_title">寒假我想要的奖品 <span style="float: right; font-size: 12px;">注：奖品质量问题，请收到后当月联系客服，过期不再受理！</span></p>
        <#else>
            <p class="my_order_title">${currentMonth}月我想要的奖品 <span style="float: right; font-size: 12px;">注：奖品质量问题，请收到后当月联系客服，过期不再受理！</span></p>
        </#if>
        <div class="my_order_inner_box">
            <#if currentMonthOrders?has_content>
                <#list currentMonthOrders as c>
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
                            <dt>${c.productName!''}</dt>
                            <dd class="clearfix">
                                <i class="J_sprites float_left"></i>
                                <#if ftlmacro.isInSummerRange >
                                    <span class="float_left">奖品将于9月1日开始寄送，寄送后就不能修改或者取消了！</span>
                                <#elseif ftlmacro.isInWinterRange>
                                    <span class="float_left">奖品将于3月1日开始寄送，寄送后就不能修改或者取消了！</span>
                                <#else>
                                    <span class="float_left">奖品将于下个月1日开始寄送，寄送后就不能修改或者取消了！</span>
                                </#if>
                            </dd>
                        </dl>
                        <p class="float_left my_order_price_box"><span>价格：</span><strong class="J_red orderPrice">${c.price!''}</strong><@ftlmacro.garyBeansText/></p>
                        <p class="float_left"><span class="float_left">
                            数量：</span><span class="my_order_number float_left">${c.quantity!''}</span>
                            <a data-order_id="${c.orderId!''}" href="javascript:void(0);" class="float_left my_order_revise_btn editProductNum">修改</a>
                        </p>
                        <a data-order_id="${c.orderId!''}" href="javascript:void(0);" class="float_right J_sprites my_order_delete_btn deleteOrderBut"></a>
                    </div>
                </#list>
                <div class="my_order_sum_price clearfix">
                    <p class="float_right">总共花费：&nbsp;<strong id="integral_total" class="J_red">${currentTotalPrice!''}</strong>&nbsp;<@ftlmacro.garyBeansText/></p>
                </div>
            <#else>
                <div class="no_order_box">
                    <div class="no_order_bg"></div>
                    <p class="btn_box" style="font-size: 14px;">你还没有兑换奖品</p>
                    <p class="btn_box"><a href="#a1" class="J_blue_btn">查看上个月兑换？</a></p>
                </div>
            </#if>
        </div>
        <#if  currentMonthBoxs??>
            <#if ftlmacro.isInSummerRange >
                <p class="my_order_title">暑假我抽中的奖品 <span style="float: right; font-size: 12px;">注：奖品质量问题，请收到后当月联系客服，过期不再受理！</span></p>
            <#elseif ftlmacro.isInWinterRange>
                <p class="my_order_title">寒假我抽中的奖品 <span style="float: right; font-size: 12px;">注：奖品质量问题，请收到后当月联系客服，过期不再受理！</span></p>
            <#else>
                <p class="my_order_title">${currentMonth}月我抽中的奖品 <span style="float: right; font-size: 12px;">注：奖品质量问题，请收到后当月联系客服，过期不再受理！</span></p>
            </#if>
            <div class="my_order_inner_box">
                <#if currentMonthBoxs?has_content>
                    <#list currentMonthBoxs as c>
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
                                <dt>${c.productName!''}</dt>
                                <dd class="clearfix">
                                    <i class="J_sprites float_left"></i>
                                    <#if ftlmacro.isInSummerRange >
                                        <span class="float_left">奖品将于9月1日开始寄送！</span>
                                    <#elseif ftlmacro.isInWinterRange>
                                        <span class="float_left">奖品将于3月1日开始寄送！</span>
                                    <#else>
                                        <span class="float_left">奖品将于下个月1日开始寄送！</span>
                                    </#if>
                                </dd>
                            </dl>
                            <p class="float_left my_order_price_box"><span>价格：</span><strong class="J_red orderPrice">0</strong><@ftlmacro.garyBeansText/></p>
                            <p class="float_left"><span class="float_left">
                            数量：</span><span class="my_order_number float_left">1</span>
                            </p>
                        </div>
                    </#list>
                <#else>
                    <div class="no_order_box">
                        <div class="no_order_bg"></div>
                        <p class="btn_box" style="font-size: 14px;">你还没有兑换奖品</p>
                        <p class="btn_box"><a href="#a1" class="J_blue_btn">查看上个月兑换？</a></p>
                    </div>
                </#if>
            </div>
        </#if>
    </div>
    <#if lastMonthOrders?has_content>
        <div class="my_order_box">
            <p class="my_order_title">正在审核、配送中的奖品<a id="a1"></a></p>
            <div class="my_order_inner_box">
                <div class="my_order_process">
                    <div class="my_order_status_bg">
                        <#if ftlmacro.isInSummerRange >
                            <#if currentMonth?number == 6>
                                <div class="my_order_status" style="width: <#if currentDay?number == 1>13%<#elseif currentDay?number gte 5 && currentDay?number lt 10>38%<#elseif currentDay?number gte 10>63%</#if>;"></div>
                            <#else>
                                <div class="my_order_status" style="width: 100%;"></div>
                            </#if>
                        <#elseif ftlmacro.isInWinterRange>
                            <#if currentMonth?number == 12>
                                <div class="my_order_status" style="width: <#if currentDay?number == 1>13%<#elseif currentDay?number gte 5 && currentDay?number lt 10>38%<#elseif currentDay?number gte 10>63%</#if>;"></div>
                            <#else>
                                <div class="my_order_status" style="width: 100%;"></div>
                            </#if>
                        <#else>
                            <div class="my_order_status" style="width: <#if currentDay?number == 1>13%<#elseif currentDay?number gte 5 && currentDay?number lt 10>38%<#elseif currentDay?number gte 10>63%</#if>;"></div>
                        </#if>
                    </div>
                    <ul class="clearfix">
                        <#if (ftlmacro.isInSummerRange)!false><#assign currentMonth = 6/></#if>
                        <#if (ftlmacro.isInWinterRange)!false><#assign currentMonth = 12/></#if>
                        <#if ((ftlmacro.isInSummerRange || ftlmacro.isInWinterRange) && currentMonth?number != 6 && currentMonth != 12)!false>
                            <li class="active">
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">审核中</p>
                                <p>（${currentMonth}月1日）</p>
                            </li>
                            <li class="active">
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">准备奖品中</p>
                                <p>（${currentMonth}月5日）</p>
                            </li>
                            <li class="active">
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">已发货</p>
                                <p>（${currentMonth}月10日）</p>
                            </li>
                            <li class="my_order_recieved active">
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">收到奖品</p>
                                <p>（预计${currentMonth}月15日-20日到达）</p>
                            </li>
                        <#else>
                            <li class="active">
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">审核中</p>
                                <p>（${currentMonth}月1日）</p>
                            </li>
                            <li <#if currentDay?number gte 5> class="active" </#if>>
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">准备奖品中</p>
                                <p>（${currentMonth}月5日）</p>
                            </li>
                            <li <#if currentDay?number gte 10> class="active" </#if>>
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">已发货</p>
                                <p>（${currentMonth}月10日）</p>
                            </li>
                            <li class="my_order_recieved <#if currentDay?number gte 10> active</#if>">
                                <p><i class="J_sprites"></i></p>
                                <p class="my_order_status_text">收到奖品</p>
                                <p>（预计${currentMonth}月15日-20日到达）</p>
                            </li>
                        </#if>
                    </ul>
                </div>
                <div class="my_ordered_box clearfix">
                    <div class="float_left">
                        <#list lastMonthOrders as l>
                            <div class="my_order_product_box clearfix">
                                <#if l.image??>
                                    <#if l.image?index_of("oss-image.17zuoye.com")!=-1>
                                        <img src="${l.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" class="float_left" />
                                    <#else>
                                        <img src="<@app.avatar href="${l.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />" class="float_left" />
                                    </#if>
                                <#else>
                                    <img src="<@app.avatar href="${l.image!''}" />" class="float_left" />
                                </#if>
                                <dl class="float_left">
                                    <dt>${l.productName!''}</dt>
                                </dl>
                                <p class="float_left">价格：${l.price!''}<@ftlmacro.garyBeansText/></p>
                                <p class="float_left my_order_number_box" style="margin-left:20px;">数量：${l.quantity!''}</span></p>
                            </div>
                        </#list>

                    </div>
                    <div class="float_right second_code_box">
                        <p class="clearfix point_box"><i class="J_sprites float_left"></i><span class="float_left">收到有问题？</span></p>
                    </div>
                </div>
                <div class="my_order_sum_price clearfix my_ordered_sum_price">
                    <p class="float_left point_box"><i class="J_sprites float_left"></i><span class="float_left">奖品已经准备寄送，不能修改或取消了哦！</span></p>
                    <p class="float_right">总共花费：&nbsp;<strong class="J_red">${lastMonthTotalPrice!''}</strong>&nbsp;<@ftlmacro.garyBeansText/></p>
                </div>
            </div>
        </div>
    </#if>
    <#if lastMonthBoxs?has_content>
        <div class="my_order_box">
        <p class="my_order_title">正在审核、配送中的奖品<a id="a1"></a></p>
        <div class="my_order_inner_box">
            <div class="my_order_process">
                <div class="my_order_status_bg">
                    <#if ftlmacro.isInSummerRange >
                        <#if currentMonth?number == 6>
                            <div class="my_order_status" style="width: <#if currentDay?number == 1>13%<#elseif currentDay?number gte 5 && currentDay?number lt 10>38%<#elseif currentDay?number gte 10>63%</#if>;"></div>
                        <#else>
                            <div class="my_order_status" style="width: 100%;"></div>
                        </#if>
                    <#elseif ftlmacro.isInWinterRange>
                        <#if currentMonth?number == 12>
                            <div class="my_order_status" style="width: <#if currentDay?number == 1>13%<#elseif currentDay?number gte 5 && currentDay?number lt 10>38%<#elseif currentDay?number gte 10>63%</#if>;"></div>
                        <#else>
                            <div class="my_order_status" style="width: 100%;"></div>
                        </#if>
                    <#else>
                        <div class="my_order_status" style="width: <#if currentDay?number == 1>13%<#elseif currentDay?number gte 5 && currentDay?number lt 10>38%<#elseif currentDay?number gte 10>63%</#if>;"></div>
                    </#if>
                </div>
                <ul class="clearfix">
                    <#if (ftlmacro.isInSummerRange)!false><#assign currentMonth = 6/></#if>
                    <#if (ftlmacro.isInWinterRange)!false><#assign currentMonth = 12/></#if>
                        <#if ((ftlmacro.isInSummerRange || ftlmacro.isInWinterRange) && currentMonth?number != 6 && currentMonth != 12)!false>
                        <li class="active">
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">审核中</p>
                            <p>（${currentMonth}月1日）</p>
                        </li>
                        <li class="active">
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">准备奖品中</p>
                            <p>（${currentMonth}月5日）</p>
                        </li>
                        <li class="active">
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">已发货</p>
                            <p>（${currentMonth}月10日）</p>
                        </li>
                        <li class="my_order_recieved active">
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">收到奖品</p>
                            <p>（预计${currentMonth}月15日-20日到达）</p>
                        </li>
                    <#else>
                        <li class="active">
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">审核中</p>
                            <p>（${currentMonth}月1日）</p>
                        </li>
                        <li <#if currentDay?number gte 5> class="active" </#if>>
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">准备奖品中</p>
                            <p>（${currentMonth}月5日）</p>
                        </li>
                        <li <#if currentDay?number gte 10> class="active" </#if>>
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">已发货</p>
                            <p>（${currentMonth}月10日）</p>
                        </li>
                        <li class="my_order_recieved <#if currentDay?number gte 10> active</#if>">
                            <p><i class="J_sprites"></i></p>
                            <p class="my_order_status_text">收到奖品</p>
                            <p>（预计${currentMonth}月15日-20日到达）</p>
                        </li>
                    </#if>
                </ul>
            </div>
            <div class="my_ordered_box clearfix">
                <div class="float_left">
                    <#list lastMonthBoxs as l>
                        <div class="my_order_product_box clearfix">
                            <#if l.image??>
                                <#if l.image?index_of("oss-image.17zuoye.com")!=-1>
                                    <img src="${l.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" class="float_left" />
                                <#else>
                                    <img src="<@app.avatar href="${l.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />" class="float_left" />
                                </#if>
                            <#else>
                                <img src="<@app.avatar href="${l.image!''}" />" class="float_left" />
                            </#if>
                            <dl class="float_left">
                                <dt>${l.productName!''}</dt>
                            </dl>
                            <p class="float_left">价格：0<@ftlmacro.garyBeansText/></p>
                            <p class="float_left my_order_number_box" style="margin-left:20px;">数量：${l.quantity!''}</span></p>
                        </div>
                    </#list>

                </div>
                <div class="float_right second_code_box">
                    <p class="clearfix point_box"><i class="J_sprites float_left"></i><span class="float_left">收到有问题？</span></p>
                </div>
            </div>
            <div class="my_order_sum_price clearfix my_ordered_sum_price">
                <p class="float_left point_box"><i class="J_sprites float_left"></i><span class="float_left">奖品已经准备寄送，不能修改或取消了哦！</span></p>
                <p class="float_right">总共花费：&nbsp;<strong class="J_red">${lastMonthTotalPrice!''}</strong>&nbsp;<@ftlmacro.garyBeansText/></p>
            </div>
        </div>
    </div>
    </#if>

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
            <#if (.now lt "2016-03-01 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
            if(!$17.getCookieWithDefault("WDHIF")){
                $17.setCookieOneDay("WDHIF", "1", 1);
                $.prompt("<p style='font-size: 14px; padding: 20px 70px; text-align: left;'>2.22—2.29号奖品中心后台升级<br/>期间将无法查看、修改订单<br/>同时12、1、2月份的订单也暂不显示<br/>但不会影响正常给您寄出奖品，请见谅！<br/>如有问题请直接联系客服解决~</p>", {
                    title : "系统提示",
                    focus : 0,
                    buttons : {"原谅你" : true},
                    position :{},
                    submit : function(e, v){}
                });
            }
            </#if>

            //删除
            $(".deleteOrderBut").on('click', function(){
                var $this = $(this);
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
            });
        });
    </script>
</@temp.page>