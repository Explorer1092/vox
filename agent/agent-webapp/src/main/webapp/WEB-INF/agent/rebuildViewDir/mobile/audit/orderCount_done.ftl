<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="商品购买" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['audit'] />
<div class="crmList-box resources-box">
    <div class="c-main">
        <div>
            <div class="c-opts gap-line c-flex c-flex-3" style="margin-bottom: .5rem">
                <span class="js-todo">待处理</span>
                <span class="js-done <#if processResultId?? && processResultId == 1>the</#if>" data-index="1">已通过</span>
                <span class="js-done <#if processResultId?? && processResultId == 2>the</#if>" data-index="2">已驳回</span>
            </div>
            <div class="tab-main" style="clear:both">
            <#--待审核-->
                <div>
                    <#if dataList??>
                        <#list dataList as item>
                            <div class="adjustmentExamine-box">
                                <#--<input <#if item.apply.readFlag?? && !item.apply.readFlag>name="js-ipt"</#if> type="hidden" value="${an.id!0}" />-->
                                <div class="adjust-head">
                                    <div class="right">${item.apply.createDatetime?string("MM-dd")}</div>
                                    <div class="name">${item.apply.accountName!''}【订单号：${item.apply.id!''}】</div>
                                </div>
                                <div class="adjust-content">
                                    <p class="title">金额：${item.apply.orderAmount!''}元</p>
                                    <p class="stage">支付方式：<#if item.apply.cityCostMonth?has_content>${item.apply.cityCostMonth!''}月</#if><#if item.apply.paymentMode?? && item.apply.paymentMode == 1>物料费用<#elseif item.apply.paymentMode?? && item.apply.paymentMode == 2>城市支持费用<#elseif item.apply.paymentMode?? && item.apply.paymentMode == 3>自付</#if></p>
                                    <div class="commodity-list">
                                        <p>商品明细：</p>
                                        <#if item.apply.orderProductList?? && item.apply.orderProductList?size gt 0>
                                            <ul>
                                                <#list item.apply.orderProductList as order>
                                                    <#if order.price?has_content && order.productName?has_content && order.productQuantity?has_content>
                                                        <li>
                                                            <div class="right">￥${(order.price!0)*(order.productQuantity!0)}</div>
                                                            <div class="left">${order.productName!''}×${order.productQuantity!''}</div>
                                                        </li>
                                                    </#if>
                                                </#list>
                                            </ul>
                                        </#if>
                                    </div>
                                    <p class="reason">收货信息：${item.apply.accountName!''}，${item.apply.province!""}${item.apply.city!""}${item.apply.county!""}${item.apply.address!''} （${item.apply.mobile!''}）</p>
                                <p class="info">处理意见：${item.processHistory.processNotes!''}</p>

                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
            <#--已通过-->
            </div>
        </div>
    </div>
</div>
<script>

    var AT = new agentTool();
    //拒绝按钮
    $(".js-AuditOpinionChange").on("click",function(){
        var data=$(this).data();
        var reqData={
            applyId    : data.aid
        };
        $.post("/mobile/resource/teacher/reject_clazz_apply.vpage",reqData,function(res){
            if (res.success) {
                AT.alert("驳回成功");
                window.location.reload();
            } else {
                AT.alert(res.info);
            }
        });
    });
    $(document).on('click','.js-done',function(){
        location.href = "/mobile/audit/done_list.vpage?processResult="+$(this).data('index')+"&workflowType=2";
    });
    $(document).on('click','.js-todo',function(){
        location.href = "/mobile/audit/todo_list.vpage?workflowType=2";
    });
    $(document).on('click','.js-return',function(){
        location.href = "/mobile/audit/index.vpage";
    });
    //同意按钮
    $(".js-agree").on("click",function(){
        var data=$(this).data();
        var reqData={
            applyId    : data.aid
        };
        $.post("/mobile/resource/teacher/approve_clazz_apply.vpage",reqData,function(res){
            if (res.success) {
                AT.alert("确认成功");
                window.location.reload();
            } else {
                AT.alert(res.info);
            }
        });
    });
//    $(document).ready(function(){
//        var notifyIdsStr = "";
////        $("input[name='js-ipt']").each(function(){
////            notifyIdsStr += $(this).val() + ",";
////        });
//        $.post("readNoticeList.vpage",{notifyIds:1},function(){
//
//        });
//    });
</script>
</@layout.page>
