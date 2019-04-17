<#if dayPerformanceList?has_content>
<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <th class="sorting" style="width: 180px;">绩效考核指标(KPI)</th>
        <th class="sorting" style="width: 100px;">区域</th>
        <th class="sorting" style="width: 160px;">时间</th>
        <th class="sorting" style="width: 80px;">完成数量</th>
    </tr>
    </thead>

    <tbody role="alert" aria-live="polite" aria-relevant="all">
        <#list dayPerformanceList as dayPerformance>
        <tr class="odd">
            <td class="center  sorting_1">${(dayPerformance["kpiName"])!}</td>
            <td class="center  sorting_1">${(dayPerformance["regionName"])!}</td>
            <td class="center  sorting_1">${(dayPerformance["date"])!""}</td>
            <td class="center  sorting_1">${(dayPerformance["completeCnt"])!}</td>
        </tr>
        </#list>
    </tbody>
</table>
<#else>
暂无数据
</#if>