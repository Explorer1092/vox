<#assign result = result!{} showTitle=showTitle!true>

<#if showTitle>
    <#assign topType = "topTitle">
    <#assign topTitle = "服务器错误">
    <#include "../top.ftl" >
</#if>

<div class="parentApp-error500">
    <div>为了保障学生更好的作业体验</div>
    <a class="doShowDetailError" href="javascript:;">请稍后再试...</a>
    <div class="detailErrorContent hide">
        错误代码: ${result.errorCode!""}
        <br/>
        类型: ${result.info!""}
    </div>
</div>

