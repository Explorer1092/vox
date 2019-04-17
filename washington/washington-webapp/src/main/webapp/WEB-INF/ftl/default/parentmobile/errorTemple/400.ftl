<#assign result = result!{} showTitle=showTitle!true>

<#if showTitle>
    <#assign topType = "topTitle">
    <#assign topTitle = "报错了!!">
    <#include "../top.ftl" >
</#if>

<div class="parentApp-error500 code_400">
    <div>${errorInfo}</div>
    <a  href="javascript:;">请稍后再试...</a>
</div>
