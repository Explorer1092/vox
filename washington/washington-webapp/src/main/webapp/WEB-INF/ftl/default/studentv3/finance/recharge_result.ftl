
<div style="padding: 0 0 29px 70px;">
<#if flow??>
    <#if flow.state == 'SUCCESS'>
        <i class="w-detail w-right"></i> <span class="text">恭喜你充值成功！</span>
        <script type="text/javascript">
            $(function(){
                $17.voxLog({
                    app : "student",
                    module: "studentOperationTrack",
                    op: "debug-payment-success"
                }, "student");
            });
        </script>
    <#else>
        <i class="w-detail w-wrong"></i> <span class="text">充值失败</span>
    </#if>
</#if>
</div>

<div class="w-form-table">
    <dl>
        <dt style="width: 25%;">你充值的学号：</dt>
        <dd>${(currentUser.id)!}</dd>
        <dt style="width: 25%;">你充值的方式：</dt>
        <dd>
        <#if flow??>
            <#switch flow.source>
                <#case "heepay/10">
                    骏卡充值
                    <#break>
                <#case "heepay/13">
                    移动神州行卡充值
                    <#break>
                <#case "heepay/14">
                    联通卡充值
                    <#break >
                <#case "heepay/15">
                    电信卡充值
                    <#break >
                <#case "heepay/41">
                    盛大一卡通充值
                    <#break >
                <#case "heepay/57">
                    Q卡支付
                    <#break >
                <#case "alipay">
                    支付宝充值
                    <#break >
                <#case "voxpay">
                    余额支付
                    <#break >
                <#case "umpay">
                    手机短信支付
                    <#break >
                <#case "wechatpay_parent">
                <#case "wechatpay">
                <#case "wechatpay_pcnative">
                <#case "wechatpay_studentapp">
                    微信
                    <#break >
                <#case "debugpay">
                    测试支付
                    <#break >
                <#case "admin">
                    系统操作
                    <#break >
                <#default>
                    <#if flow.source?index_of('alipay/directPay') gte 0 >
                        支付宝充值
                    <#elseif flow.source?index_of('alipay/bankPay') gte 0>
                        银行卡充值
                    <#else>
                    <#--${flow.source}-->
                    </#if>
            </#switch>
        </#if>
        </dd>
        <dt style="width: 25%;">你充值的金额：</dt>
        <dd>
            ${(flow.amount)!''}
        </dd>
    </dl>
</div>
<div style="padding: 10px; clear: both; text-align: center;">
    <a href="/student/center/recharging.vpage?types=recharging" class="w-btn w-btn-green" id="pay"><strong>返回充值中心</strong></a>
</div>