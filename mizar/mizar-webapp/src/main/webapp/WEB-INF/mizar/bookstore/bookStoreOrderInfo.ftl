<#import "../module.ftl" as module>
<@module.page
title="订单明细"
pageJsFile={"siteJs" : "public/script/bookstore/bookstoreinfo"}
pageJs=["siteJs"]
leftMenu="订单明细"
>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<style>
    table thead th{ white-space: nowrap;}
</style>
<div class="bread-nav">
    <span id="storeName"></span>
    >
   <span>订单明细</span>
</div>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th>序号</th>
            <th>下单时间</th>
            <th>交易单号</th>
            <th>学生姓名</th>
        </tr>
        </thead>
        <tbody>
    <#if orderInfoBeanPage?? && orderInfoBeanPage.content?size gt 0>
        <#list orderInfoBeanPage.content as item>
            <tr>
                <td>${(page-1) * 20 +(item_index +1)}</td>
                <td>${item.payDatetime!''}</td>
                <td>${item.tradeId!''}</td>
                <td>${item.studentName!''}</td>
            </tr>
        </#list>
    <#else>
    <tr>
        <td colspan="4" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"没有数据"}</td>
    </tr>
    </#if>
        </tbody>
    </table>
    <#if orderInfoBeanPage?? && orderInfoBeanPage.content?size gt 0>
    <div id="paginator" pageIndex="${(page!0)}" class="paginator clearfix"
         totalPages="<#if totalPages?? >${totalPages}<#else>1</#if>"></div>
    </#if>

</@module.page>