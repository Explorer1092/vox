<#import "module.ftl" as com>
<@com.page step=2 title="暂未收到付款通知" stepOnOff = "${orderStatus!''}">
    <div class="main">
    <div class="payMainBox">
        <div class="curaddress">暂未收到付款通知</div>
        <div class="tabbox">
            <!--Purchase success-->
            <div class="tabLevel">

                <div class="successBox loseBox" style="width: 580px;">
                    <s class="iblock iexclamation"></s>
                    <div class="content">
                        <p class="txt">暂未收到付款通知</p>
                        <p class="ctn">
                            如果您已经支付成功，这属于正常的网络延迟，只需稍候10分钟即可。<br/>
                            如果您支付过程遇到问题，可返回“我的订单”页面再次进行支付。  <br/>
                            如果使用手机充值卡、骏卡、盛大一卡通、Q卡支付时，请及时查看我的充值中心。 <br/>
                            <#--<#if "${orderStatus!''}" != "Confirmed">如果您自己无法完成支付，您还可以找别人帮你付款。</#if>-->
                        </p>
                        <#-- 这个url可能会跨payment和order，所以用相对路径 -->
                        <#if "${orderStatus!''}" != "Confirmed">
                        <div class="btn">
                            <a href="/student/center/order.vpage" class="public_send_btn public_blue_btn public_nomal_btn" style="margin-right: 50px; width: 180px; text-align: center; padding: 0;">返回我的订单</a>
                            <#--<a href="/apps/afenti/order/agentmobile.vpage?orderId=${orderId!}" class="public_send_btn public_green_btn public_well_btn" style="width: 180px;text-align: center; padding: 0;">找人代付</a>-->
                        </div>
                        </#if>
                    </div>
                </div>

            </div>
            <!--//-->
        </div>
    </div>
</div>

</@com.page>