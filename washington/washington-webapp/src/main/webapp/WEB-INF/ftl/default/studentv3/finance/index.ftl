<div class="w-form-table">
    <h2>我的作业币</h2>
    <dl>
        <dt>学号：</dt>
        <dd>${(currentUser.id)!}</dd>
        <dt>余额：</dt>
        <dd style="margin-top: -6px;">
            ${balance!} <span class="text_gray_9 text_small">个作业币</span>
            <#--<a href="recharging.vpage?types=recharging-go" class="w-btn-dic w-btn-yellow-well v-studentVoxLogRecord" data-op="recharge">立即充值</a>-->
        </dd>
        <dd>
        <#if haspaypwd>
            <div class="alert_vox alert_vox_success">
                支付密码已设置，
                <a onclick="$17.atongji('个人中心-我的充值-去修改支付密码','${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=password');" href="javascript:void (0);" class="w-blue">修改支付密码>></a>
            </div>
        <#else>
            <div class="alert_vox alert_vox_error">
                支付密码未设置，设置支付密码才能购买网站产品，并且保证账户安全。
                <a href="${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=paymentpassword" class="w-blue">去设置>></a>
            </div>
        </#if>
        </dd>
    </dl>

    <#--<h2>交易记录</h2>
    <div class="w-table w-table-border">
        <table>
            <thead>
            <tr>
                <td>日期</td>
                <td>交易金额</td>
                <td>交易方式</td>
            </tr>
            </thead>
            <tbody>
            <#if flows?size gt 0 >
                <#list flows as flow>
                <tr>
                    <td>${flow.updateDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${flow.amount}元</td>
                    <td>
                        <#if flow.source??>
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
                                    Q卡
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
                                    ${flow.source}
                                    </#if>
                            </#switch>
                        </#if>
                    </td>
                </tr>
                </#list>
            <#else>
            <tr>
                <td colspan="3">
                    <div style="padding: 30px; color: #666; text-align: center;">
                        暂无任何记录！
                    </div>
                </td>
            </tr>
            </#if>
            </tbody>
        </table>
    </div>-->
</div>