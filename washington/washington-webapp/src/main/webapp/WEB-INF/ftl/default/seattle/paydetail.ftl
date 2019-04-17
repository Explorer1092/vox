<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="${(activity.title)!''}"
pageJs=['init']
pageJsFile={"init" : "public/script/mobile/seattle/main"}
pageCssFile={"css" : ["public/skin/paymentmobile/css/paymentskin"]}
>
<div class="paymentBox junior_paymentBox" style="overflow: hidden;">
    <div class="paySection">
        <div class="p-content">
            <p class="txtRed">¥<span class="p-txt">${(activity.productPrice)!}</span></p>

            <p class="txtGray orderNum JS-selectchild" style="display:none;">当前选择的孩子：${(studentInfo.name)!'---'}</p>
            <p class="txtGray orderNum">课程明细：${(activity.productName)!}</p>
        </div>

    </div>
    <div class="paySection">
        <div class="p-list">
            <p class="p-info">选择支付方式：</p>
            <a href="javascript:void(0);" class="typeList02 p-icon-1 active JS-selectPayType" data-pay_type="1" style="display: none;">
                <div class="inner">
                    <p class="type">微信支付</p>
                    <p class="describe">推荐微信5.0及以上版本的用户使用</p>
                </div>
            </a>
            <a href="javascript:void(0);" class="typeList02 p-icon-2 JS-selectPayType" data-pay_type="2" style="display: none;">
                <div class="inner">
                    <p class="type">支付宝支付</p>
                    <p class="describe">推荐有支付宝账号的用户使用</p>
                </div>
            </a>
        </div>

        <#if (activity.remarked)!false>
            <div class="p-list" style="margin-top: 10px;  ">
                <p class="p-info">请输入备注：</p>
                <div class="typeList01" style="padding: 5px 0 0;">
                    <textarea placeholder="${(activity.remarkContent)!'备注信息最多输入不超过100字'}" id="remark" maxlength="100" style="outline: none; resize: none; width: 100%; border: none; background: none; height: 50px; font-size: 0.625rem; display: block;"></textarea>
                </div>
            </div>
        </#if>
    </div>
</div>
<div class="p-footer junior_paymentFooter">
    <div class="footerInner">
        <a href="javascript:void(0);" class="submit-btn JS-submit" data-type="pay" data-id="${(activity.id)!}" style="width:100%;">
            立即支付
        </a>
    </div>
</div>

<#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>
    <#assign weChatLinkHost = "//wechat.test.17zuoye.net">
<#elseif ProductDevelopment.isStagingEnv()>
    <#assign weChatLinkHost = "//wechat.staging.17zuoye.net">
<#elseif ProductDevelopment.isProductionEnv()>
    <#assign weChatLinkHost = "//xue.17xueba.com/p17wechat">
</#if>
<script type="text/javascript">
    var remarked = ${((activity.remarked)!false)?string};
    var backSuccessUrl = "${(activity.returnUrl)!}";
    var weChatLinkHost = "${weChatLinkHost!}";
</script>
</@layout.page>