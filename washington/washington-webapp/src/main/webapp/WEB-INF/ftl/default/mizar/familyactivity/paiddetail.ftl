<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='${title!"我的课程"}'
pageJs=["jquery","voxLogs"]
pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/familyActivity"]}
bodyClass="bg-white">

<div class="orderDetails">
    <div class="orderState success"><span>已支付</span></div><!--支付成功添加类success-->
    <div class="orderInfo">
        <div class="pic"><img src="${cover!""}"></div>
        <div class="info">${title!''}</div>
    </div>
    <div class="orderDescribe">
        <div class="title">订单信息</div>
        <#if actTime?has_content>
            <div class="text">活动时间：${actTime!''}</div>
        </#if>
        <#if productType?has_content>
            <div class="text">产品类型：${productType!''}</div>
        </#if>
        <div class="text">下单时间：${createTime!''}</div>
        <div class="text">订单编号：${orderNo!''}</div>
    </div>
    <div class="orderDescribe">
        <div class="title">活动地点</div>
        <div class="text address" id="mapBtn">
            ${address!''}
            <span class="number">
                <#if distance?has_content>
                    ${distance?string('#.##')}km
                </#if>
            </span>
        </div>
    </div>
    <div class="orderDescribe">
        <div class="title">联系人</div>
        <div class="text">${userName!''}：${userPhone!''}</div>
    </div>
</div>
<div class="w-footer">
    <div class="inner fixed"><!--置低添加fixed-->
        <div class="orderOperate">
            <#--<a href="javascript:void(0);" class="btn">退款</a>-->
            <#if tels?? && tels?size gt 0>
                <a href="tel:${tels[0]}" class="btn">咨询</a>
            </#if>

            <#if successUrl?has_content>
                <a href="${successUrl!'javascript:void(0);'}" class="btn btn-red">去上课</a>
            </#if>

            <#--<a href="javascript:void(0);" class="btn btn-red">评价订单</a>-->
        </div>
    </div>
</div>
<script>
    signRunScript = function(){
        $('#mapBtn').on('click',function () {
            location.href = '/mizar/activitymap.vpage?actId=${actId!0}';
        });
    };
</script>

</@layout.page>