<#import "../module.ftl" as module>
<@module.page
title="经营数据"
pageJsFile={"siteJs" : "public/script/data/statistic"}
pageJs=["siteJs"]
leftMenu="经营数据"
>
<div class="op-wrapper clearfix">
    <form id="filter-form" action="/data/statistic/index.vpage" method="get">
        <div class="item shop-name">
            <p>机构信息</p>
            <input value="${shopToken!}" name="shopToken" class="v-select" placeholder="机构名称/ID"/>
        </div>
        <div class="item time-region">
            <p>查询时间</p>
            <div>
                <div class="time-select">
                    <input id="startTime" value="${startDate!}" name="startDate" autocomplete="off" class="v-select" placeholder="2016-09-10"  />
                    <div style="margin:0 5px;line-height:30px;">至</div>
                    <input id="endTime" value="${endDate!}" name="endDate" autocomplete="off" class="v-select" placeholder="2016-09-20" />
                </div>
            </div>
        </div>
        <a class="blue-btn submit-search" style="margin-top:28px;" href="javascript:void(0)">搜索</a>
    </form>
</div>
<#if result?? && result?size gt 0>
    <#list result as page>
    <table class="data-table one-page <#if page_index == 0></#if>displayed">
        <thead>
        <tr>
            <th colspan="2">合计</th>
            <th>-</th>
            <th>${pvTotal!"-"}</th>
            <th>${reserveTotal!"-"}</th>
        </tr>
        </thead>
        <thead>
        <tr>
            <th style="width: 200px;">机构ID</th>
            <th style="width: 200px;">机构名称</th>
            <th style="width: 200px;">独立浏览量(UV)</th>
            <th style="width: 200px;">浏览量(PV)</th>
            <th style="width: 240px;">产生预约量</th>
        </tr>
        </thead>
        <tbody>
            <#list page as item>
            <tr>
                <td>${item.shopId!"--"}</td>
                <td>${item.shopName!"--"}</td>
                <td>${item.showUv!0}</td>
                <td>${item.showPv!0}</td>
                <td>${item.reserveCnt!0}</td>
            </tr>
            </#list>
        </tbody>
    </table>
    </#list>
<#else>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th colspan="2">合计</th>
            <th>--</th>
            <th>--</th>
            <th>--</th>
        </tr>
        </thead>
        <thead>
        <tr>
            <th style="width: 200px;">机构ID</th>
            <th style="width: 200px;">机构名称</th>
            <th style="width: 200px;">独立浏览量(UV)</th>
            <th style="width: 200px;">浏览量(PV)</th>
            <th style="width: 240px;">产生预约量</th>
        </tr>
        </thead>
        <tbody>
            <tr>
                <td colspan="5" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下暂时没有数据信息哦~"}</td>
            </tr>
        </tbody>
    </table>
</#if>
<div id="paginator" class="paginator clearfix"></div>
</@module.page>