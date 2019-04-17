<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='退款说明' pageJs="refundExplain">
<@sugar.capsule css=['mytrustee','jbox'] />
<div class="mc-refundExplain mc-wrap mc-margin15">
    <div class="mre-list">
        <p class="title">退款原因*</p>
        <div class="info" id="refundReason">
            <input placeholder="请选择您的退款原因" readonly="readonly">
            <span class="arrow"></span>
            <div class="slideInfo">
                <ul class="js-reasonList">
                    <#if reasons?? && reasons?size gt 0>
                        <#list reasons as reason>
                            <li data-id="${reason.id!""}">${reason.content!""}</li>
                        </#list>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
    <div class="mre-list">
        <p class="title">退款说明</p>
        <div class="info">
            <textarea class="js-refundReasonDesc" placeholder="方便客服人员后续处理"></textarea>
        </div>
    </div>
    <div class="mre-btn">
        <a href="javascript:void(0);" class="mc-btn-orange js-applyRefundBtn">提交申请</a>
    </div>
</div>
</@trusteeMain.page>