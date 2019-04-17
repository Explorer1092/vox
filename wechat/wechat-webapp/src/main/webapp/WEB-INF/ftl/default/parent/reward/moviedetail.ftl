<#import "../layout.ftl" as ucenter>
<@ucenter.page title='设定目标和奖励' pageJs="">
<@sugar.capsule css=['ucentersetmission'] />
<div class="t-setRewards-content">
    <#list 0..13 as index>
        <div class="inner-${index}"></div>
    </#list>
</div>
</@ucenter.page>