<#import "../../layout_default.ftl" as layout_default>

<@layout_default.page page_title="付款" page_num=3>
<div class="span9">
    <#if errorMessage??>
        <div class="alert alert-error">
            ${errorMessage}
        </div>
    </#if>

    <form class="form-horizontal" method="post" action='manuallypay.vpage?orderId=${orderId?html}'>
        <legend>付款</legend>
        <div class="control-group">
            <label class="control-label" for="orderId">订单号：</label>
            <div class="controls">
                <input type="text" id="orderId" value="${orderId}" disabled="disabled" readonly="readonly">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="payAmount">付款金额：</label>
            <div class="controls">
                <input name="payAmount" type="text" id="payAmount" value="${payAmount!''}" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="extTradeNo">外部付款号：</label>
            <div class="controls">
                <input name="extTradeNo" type="text" id="extTradeNo" value="${extTradeNo!''}" />
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <input type="submit" class="btn" value="提交" />
            </div>
        </div>
    </form>
</div>
</@layout_default.page>