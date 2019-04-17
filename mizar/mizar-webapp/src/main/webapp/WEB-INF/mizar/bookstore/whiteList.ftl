<#import "../module.ftl" as module>
<@module.page
title="转介绍白名单"
pageJsFile={"siteJs" : "public/script/bookstore/white"}
pageJs=["siteJs"]
leftMenu="转介绍白名单"
>
<style>
    table thead th{ white-space: nowrap;}
</style>
<div class="op-wrapper orders-wrapper clearfix">
    <div class="item" style="width:auto; ">
        <p style="color:transparent;">.</p>
        <a class="blue-btn" style="width: 120px" href="/bookstore/manager/whiteList/upsert.vpage">新增白名单</a>
    </div>
</div>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th style="text-align: center;width: 5%;">序号</th>
            <th style="text-align: center;width: 100px;">配置的门店ID</th>
            <th style="text-align: center;width: 5%;">个数</th>
            <th style="text-align: center;width: 15%;">操作人</th>
            <th style="text-align: center;width: 100px;">说明</th>
            <th >操作时间</th>
            <th style="text-align: center">操作</th>
        </tr>
        </thead>
        <tbody>
    <#if whiteListExtendBeanPage.content?? && whiteListExtendBeanPage.content?size gt 0>

            <#list whiteListExtendBeanPage.content as goods>
            <tr>
                <td style="text-align: center;width: 5%;">${goods_index+1}</td>
                <td style="text-align: center;width: 20%;word-break: break-all; word-wrap: break-word; ">${goods.content!''}</td>
                <td style="text-align: center;width: 5%;">${goods.bookStoreNum!''}</td>
                <td style="text-align: center;width: 15%;">${goods.operationUserName!''}</td>
                <td style="text-align: center;width: 30%;">${goods.remark!''}</td>
                <td >${goods.operationTime!''}</td>
                <td style="text-align: center;width: 5%;">
                    <a class="op-btn op-status" data-status="OFFLINE" data-gid="${goods.id!}" href="/bookstore/manager/whiteList/upsert.vpage?id=${goods.id!}" style="margin-right:0;float:right;">编辑</a>
                </td>

            </tr>
            </#list>
    <#else>
    <tr>
        <td colspan="7" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
    </tr>
    </#if>
        </tbody>
    </table>


    <#if whiteListExtendBeanPage.content?? &&  whiteListExtendBeanPage.content?size gt 0>

    <div id="paginator" pageIndex="${(page!0)}" class="paginator clearfix"
         totalPages="<#if totalPages??>${totalPages}<#else>1</#if>"></div>
    </#if>

    </@module.page>
