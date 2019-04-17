<#import "../module.ftl" as module>
<@module.page
title="门店概况"
pageJsFile={"siteJs" : "public/script/biz/report"}
pageJs=["siteJs"]
leftMenu="门店概况"
>
<div class="op-wrapper clearfix">
    <form id="filter-form" action="/biz/report/index.vpage" method="get">
        <div class="item shop-name">
            <p>门店名称</p>
            <select class="v-select" name="shopId">
                <option value="all">全部门店</option>
                <#if shop?? && shop?size gt 0>
                    <#list shop as s>
                        <option value="${s.shopId!}" <#if shopId == s.shopId>selected</#if>>${s.shopName!}</option>
                    </#list>
                </#if>
            </select>
        </div>
        <div  class="item time-region" style="width: 440px;">
            <p>查询时间</p>
            <div>
                <div class="time-select">
                    <input id="startTime" value="${startDate!}" name="startDate" autocomplete="off" class="v-select" placeholder="2016-09-10"  />
                    <div style="margin:0 5px;line-height:30px;">至</div>
                    <input id="endTime" value="${endDate!}" name="endDate" autocomplete="off" class="v-select" placeholder="2016-09-20" />
                </div>
                <a class="blue-btn submit-search" style="float: right;" href="javascript:void(0)">搜索</a>
            </div>
        </div>
    </form>
</div>
<table class="data-table">
    <thead>
    <tr>
        <th>门店名称</th>
        <th style="width: 200px;">浏览量</th>
        <th style="width: 240px;">产生预约量</th>
    </tr>
    </thead>
    <tbody>
    <#if result?? && result?size gt 0>
        <#list result as item>
        <tr>
            <td>${item.shopName!"--"}</td>
            <td>${item.showPv!0}</td>
            <td>${item.reserveCnt!0}</td>
        </tr>
        </#list>
    <#else>
        <tr>
            <td colspan="3" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下暂时没有门店信息哦~"}</td>
        </tr>
    </#if>
    </tbody>
</table>
</@module.page>