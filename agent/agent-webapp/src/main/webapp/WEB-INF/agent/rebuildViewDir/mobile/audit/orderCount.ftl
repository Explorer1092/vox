<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="商品购买" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['audit'] />
<div class="crmList-box resources-box">
    <div class="res-top fixed-head">
        <div class="return js-return"><a href="javascript:void(0)"><i class="return-icon"></i>返回</a></div>
        <span class="return-line"></span>
        <span class="res-title">商品购买</span>
    </div>
    <div class="c-main">
        <div>
            <div class="c-opts gap-line c-flex c-flex-3" style="margin-bottom: .5rem">
                <span class="the js-todo">待处理</span>
                <span class="js-done" data-index="1">已通过</span>
                <span class="js-done" data-index="2">已驳回</span>
            </div>
            <div class="tab-main" style="clear:both">
            <#--待审核-->
                <div>
                    <#if dataList??>
                        <#list dataList as item>
                            <div class="adjustmentExamine-box">
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
                                    <#--<p class="info">处理意见：${item.processHistory.processNotes!''}</p>-->

                                </div>
                                <div class="adjust-side">
                                    <textarea class="textarea_${item.apply.workflowId}" placeholder="请填写处理意见"></textarea>
                                    <div class="btn">
                                        <a href="javascript:void(0);" class="white_btn js-submit" data-info="驳回成功" data-result="2" data-aid="${item.apply.workflowId!0}">驳回</a>
                                        <a href="javascript:void(0);" class="white_btn orange js-submit" data-info="确认成功" data-result="1" data-aid="${item.apply.workflowId!0}">同意</a>
                                    </div>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>
<script>

    $(document).on('click','.js-done',function(){
        location.href = "/mobile/audit/done_list.vpage?processResult="+$(this).data('index')+"&workflowType=2";
    });
    var AT = new agentTool();
    //拒绝按钮 + 同意按钮
    $(document).on('click','.js-submit',function(){
        var data=$(this).data();
        if($('.textarea_'+data.aid).val() == ''){
            AT.alert('请填写处理意见');
            return false;
        }
        var reqData={
            processResult: data.result ,
            workflowId    : data.aid ,
            processNote :$('.textarea_'+data.aid).val()
        };
        console.log(reqData);
        $.post("process.vpage",reqData,function(res){
            if (res.success) {
                AT.alert(data.info);
                setTimeout('window.location.reload()',1500);
            } else {
                AT.alert(res.info);
            }
        });
    });
    $(document).on('click','.js-return',function(){
        location.href = "/mobile/audit/index.vpage";
    });
</script>
</@layout.page>
