<#import "module.ftl" as center>
<@center.studentCenter currentPage='order'>
<div class="t-center-box w-fl-right">
    <span class="center-rope"></span>
    <span class="center-rope center-rope-1"></span>
    <div class="t-center-safe" id="isTab">
        <#if orders?has_content>
            <#list orders as order>
                <div class="t-center-list">
                    <ul>
                        <li class="title">
                            <span class="order-number">订单号：<strong style="font-weight: normal" class="w-blue-special">${order.id!''}</strong></span>
                            <#--<span class="order-cycles">周期</span>-->
                            <span class="order-price">价格</span>
                            <#--<span class="order-time">有效期</span>-->
                            <span class="order-option">操作</span>
                        </li>
                        <li class="con">
                            <span class="order-number">${order.productName!''}</span>
                            <#--<span class="order-cycles"><#if order.validPeriod?has_content>${order.validPeriod!''}天<#else> - </#if></span>-->
                            <span class="order-price">${order.orderPrice}<#if (order.orderProductServiceType!'') == 'VoxAppPay'>作业币<#else>元</#if></span>

                            <#--<#if (order.payStatus == 'Paid')!false>-->
                                <#--<#if (order.activateStatus!'') == 'Activated' >-->
                                    <#--<#if order.serviceEndDatetime?? && order.serviceEndDatetime?string('yyyy-MM-dd HH:mm:ss')?datetime('yyyy-MM-dd HH:mm:ss') gt .now>-->
                                        <#--<#assign action="valid" />-->
                                    <#--<span class="order-time">${(order.serviceStartDatetime?string('yyyy-MM-dd'))!} 至<br>${(order.serviceEndDatetime?string('yyyy-MM-dd'))!}</span>-->
                                    <#--<#else>-->
                                        <#--<#assign action="renew" />-->
                                        <#--<span class="order-time w-red">${(order.serviceStartDatetime?string('yyyy-MM-dd'))!} 至<br>${(order.serviceEndDatetime?string('yyyy-MM-dd'))!}<br />(已过期)</span>-->
                                    <#--</#if>-->
                                <#--<#else>-->
                                    <#--<#if (order.productServiceType!'') == 'VoxAppPay'>-->
                                        <#--<#assign action="activate" />-->
                                        <#--<span class="order-time">-</span>-->
                                    <#--<#else>-->
                                        <#--<#assign action="activate" />-->
                                        <#--<span class="order-time">未激活</span>-->
                                    <#--</#if>-->
                                <#--</#if>-->
                            <#--<#else>-->
                                <#--<#assign action="pay" />-->
                                <#--<span class="order-time">未支付</span>-->
                            <#--</#if>-->

                            <span class="order-option">
                                 <#if order.orderStatus == 'Confirmed'>
                                     <a href="javascript:void (0)" class="on-btn">
                                         <span class="w-detail w-right-small"></span>
                                         <span class="w-green">已开通</span>
                                     </a>
                                 <#--<#elseif order.orderStatus == 'New'>-->
                                     <#--<#switch order.orderProductServiceType>-->
                                         <#--<#case 'AfentiBasic'>-->
                                             <#--<a href='/apps/afenti/order/basic-cart.vpage' class="w-btn-dic w-btn-green-new" target="_blank">续费</a>-->
                                             <#--<#break />-->
                                         <#--<#case 'AfentiTalent'>-->
                                             <#--<a href='/apps/afenti/order/talent-cart.vpage' class="w-btn-dic w-btn-green-new" target="_blank">续费</a>-->
                                             <#--<#break />-->
                                         <#--<#case 'TravelAmerica'>-->
                                             <#--<a href='/apps/afenti/order/travel-cart.vpage' class="w-btn-dic w-btn-green-new" target="_blank">续费</a>-->
                                             <#--<#break />-->
                                         <#--<#case 'AfentiExam'>-->
                                             <#--<a href='/apps/afenti/order/exam-cart.vpage' class="w-btn-dic w-btn-green-new" target="_blank">续费</a>-->
                                             <#--<#break />-->
                                         <#--<#case 'KaplanPicaro'>-->
                                             <#--&lt;#&ndash;<a href='/apps/afenti/order/picaro-cart.vpage' class="w-btn-dic w-btn-green-new" target="_blank">续费</a>&ndash;&gt;-->
                                             <#--<a href='javascript:void(0);' class="w-btn-dic w-btn-gray-new"  style="cursor: default;">暂停购买</a>-->
                                             <#--<#break />-->
                                         <#--<#case 'Walker'>-->
                                             <#--<a href='/apps/afenti/order/walker-cart.vpage' class="w-btn-dic w-btn-green-new" target="_blank">续费</a>-->
                                             <#--<#break />-->
                                     <#--</#switch>-->
                                 <#elseif order.orderStatus == 'New'>
                                    <#if order.orderProductServiceType == "KaplanPicaro">
                                        <a href='javascript:void(0);' class="w-btn-dic w-btn-gray-new" style="cursor: default;">暂停购买</a>
                                    <#else>
                                        <#--下线列表-->
                                        <#assign offlineList = {
                                            'A17ZYSPG' : 'spg',
                                            'SanguoDmz' : 'sanguodmz'
                                        }/>
                                        <#if (offlineList[order.orderProductServiceType]?has_content)!false>
                                            <a href="/apps/afenti/order/${offlineList[order.orderProductServiceType]!}-cart.vpage" target="_blank" class="w-btn-dic w-btn-gray-new">暂停购买</a>
                                        <#else>
                                            <a onclick="$17.tongji('个人中心-我的订单-支付');" href="/apps/afenti/order/confirm.vpage?orderId=${order.genUserOrderId()}" target="_blank" data-order_id="${order.genUserOrderId()}" data-product_type="${order.orderProductServiceType!}" class="payOrder  w-btn-dic w-btn-green-new">
                                                支付
                                            </a>
                                        </#if>
                                    </#if>
                                 </#if>
                            </span>
                        </li>
                    </ul>
                </div>
            </#list>
        <#else>
            <div style="font-size: 14px; text-align: center;">你还没有订单哦</div>
        </#if>
    </div>
    <div class="page message_page_list" style="margin: 0 40px 20px;"></div>
</div>
<script type="text/javascript">
    (function($){
        "use strict";

        var $this	= $("#isTab");
        var $tr		= $this.find(".t-center-list");
        var groud	= 5;
        var trLen	= $tr.length;

        $tr.each(function(index) {
            var x = index + 1;

            $(this).find("td:first").append(x);

            if( index >= groud){
                $tr.eq(index).hide();
            }

            if( index < (trLen/groud) ){
                $(".page").append("<a pv=" + x + ">" + x + "</a>");
            }
        });

        $(".page a[pv]:first").addClass("this");

        $(".page a[pv]").live('click', function(){
            var $that = $(this);
            var x = $(this).attr("pv");
            var y = x * groud;

            $that.addClass("this").siblings().removeClass("this");

            if( x > 1){
                $that.siblings("a[pic='back']").addClass("enable").removeClass("disable");
            }else{
                $that.siblings("a[pic='back']").addClass("disable").removeClass("enable");
            }

            if( x >= (trLen/groud)){
                $that.siblings("a[pic='next']").addClass("disable").removeClass("enable");
            }else{
                $that.siblings("a[pic='next']").addClass("enable").removeClass("disable");
            }

            for(var i= 0; i < trLen; i++){
                $tr.eq(i).show();
                if( i >= y || i < (y-groud)){
                    $tr.eq(i).hide();
                }
            }
        });
    })(jQuery);
</script>
</@center.studentCenter>