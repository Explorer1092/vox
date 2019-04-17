<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='一起托付' pageJs="mytrusteeCountdown">
<@sugar.capsule css=['mytrustee'] />
<#function countdownDay day>
    <#local number = 0>
    <#if day?number lt 29>
        <#local number = 29 - day?number>
    </#if>
    <#return number>
</#function>
<div class="t-entrust-box">
    <div class="e-banner">
        <div class="e-title">剩余 <span class="e-number">${countdownDay(.now?string("d"))}</span> 天</div>
    </div>
    <div class="e-content">
        <div class="tags">引言</div>
        <div class="ec-info">俗话说，一年之计在于春，对于学习和好习惯的培养，也是如此。开学是一个很好的契机，快来参与我们的活动，帮助孩子“天天向上”吧！</div>
        <div class="tags">时间</div>
        <div class="ec-info">2016年2月26日~2016年2月28日</div>
        <div class="tags">参与规则</div>
        <div class="ec-info ec-infodetail">
            凡是与2016年“2月26日~2月28日”期间，在“一起作业家长通“，购买任意托管服务的家长，均可享受到下述福利：
            <p><span class="tip">1</span>享受经过一起作业，实地考察、筛选出的托管班！</p>
            <p><span class="tip">2</span>价值10元的，10天“阿分题”免费服务！</p>
            <p><span class="tip">3</span>购买后3天内不满意，随时退款！</p>
            PS:阿分题服务，将于3月3日前，统一发放，尽请期待
        </div>
    </div>
    <div class="e-btn">
        <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ((.now>="2016-02-26 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-02-29 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))>
            <a href="javascript:void(0);" class="e-enroll js-signUpBtn">立即报名</a>
        <#else>
            <a href="javascript:void(0);" class="e-enroll disabled">活动已过期</a>
        </#if>
    </div>
</div>
</@trusteeMain.page>