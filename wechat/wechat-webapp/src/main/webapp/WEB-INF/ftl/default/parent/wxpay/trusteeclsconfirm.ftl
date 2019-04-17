<#import "../layout.ftl" as ucenter>
<@ucenter.page title='支付订单' pageJs="trusteeclsconfirm">
<@sugar.capsule css=['mytrustee', 'jbox'] />
<div class="mc-orderPayment mc-wrap mc-margin15">
    <table class="mc-payment" cellpadding="0" cellspacing="0">
        <tr>
            <td>已购服务：</td>
            <td><p>${name!""}</p>
                <p>
                    <#if tags?? && tags?size gt 0>
                        <#list tags as tag>
                            ${tag!""}
                        </#list>
                    </#if>
                </p>
            </td>
        </tr>
        <tr>
            <td>托管机构：</td>
            <td><p>${branch!""}</p></td>
        </tr>
        <tr>
            <td>订单编号：</td>
            <td><p>${orderId!''}</p></td>
        </tr>
        <tr>
            <td>孩子姓名：</td>
            <td><p>${studentName!""}</p></td>
        </tr>
        <tr>
            <td>数量：</td>
            <td><p>${count!0}</p></td>
        </tr>
        <tr>
            <td>总价：</td>
            <td><p>￥${price!""}</p></td>
        </tr>
    </table>
    <div class="pay-box">
        <a href="javascript:void(0)" class="mc-btn-orange btn js-payBtn" data-oid="${orderId!''}">确认支付</a>
    </div>
    <div class="fix-footerText">点击确认支付，即表示您同意<a href="/parent/trustee/lawstates.vpage" class="mc-txtBlue">《法律声明及隐私政策》</a></div>
</div>
</@ucenter.page>