<#import "../layout.ftl" as layout>
<@layout.page group="workbench" title="有效工作量">
<div id="visit-nav" data-role="navbar">
    <ul>
        <li><a href="#" data-ajax="false" class="ui-btn-active">统计</a></li>
        <li><a href="workload_rank.vpage" data-ajax="false">排行榜</a></li>
        <li><a href="visitor_list.vpage" data-ajax="false">联系列表</a></li>
    </ul>
</div>

<form id="workload_search_form" action="workload.vpage" method="get" data-ajax="false">
    <fieldset data-role="controlgroup" data-type="horizontal" data-mini="true">
        <#assign member = memberId?string>
        <select name="memberId" id="memberId">
            <#list members?keys as m>
                <#if member == m>
                    <option value="${m}" selected="selected">${members[m]}</option>
                <#else>
                    <option value="${m}">${members[m]}</option>
                </#if>
            </#list>
        </select>
    </fieldset>
    <fieldset data-role="controlgroup" data-type="horizontal" data-mini="true">
        <div class="ui-grid-b">
            <div class="ui-block-a">
                <input name="startDate" id="startDate" value="${startDate!}" data-role="date" type="text"
                       placeholder="开始日期" readonly="readonly">
            </div>
            <div class="ui-block-b">
                <input name="endDate" id="endDate" value="${endDate!}" data-role="date" type="text"
                       placeholder="截止日期" readonly="readonly">
            </div>
        </div>
        <a id="search_btn" href="#" class="ui-btn ui-btn-inline">查询</a>
    </fieldset>
</form>

<table data-role="table" data-mode="reflow" class="ui-responsive table-stroke">
    <thead>
    <tr>
    </tr>
    </thead>
    <tbody>
        <#if result??>
        <tr>
            <td><strong>类别</strong></td>
            <td style="text-align: center"><strong>人数</strong></td>
            <td style="text-align: center"><strong>工作量</strong></td>
        </tr>
        <tr style="color: red">
            <td>工作总量</td>
            <td style="text-align: center">${result.totalVisitorCount!0}</td>
            <td style="text-align: center">${((result.totalWorkload!0)/100)?string("#")}</td>
        </tr>
            <#if result.visitWorkloads??>
                <#list result.visitWorkloads as e>
                <tr>
                    <td>${(e.visitorType.value)!}</td>
                    <td style="text-align: center">${(e.visitorCount)!0}</td>
                    <td style="text-align: center">${(((e.workload)!0)/100)?string("#")}</td>
                </tr>
                </#list>
            </#if>
        </#if>
    </tbody>
</table>

<script>
    $("input[data-role='date']").date({dateFormat: "yy-mm-dd", showOtherMonths: true});
    $(function () {
        $("#search_btn").click(function () {
            return doSubmit();
        });
    });

    function doSubmit() {
        var startDate = $("#startDate").val();
        if (startDate == null || $.trim(startDate) == "") {
            alert("请选择开始日期!");
            return false;
        }
        var endDate = $("#endDate").val();
        if (endDate == null || $.trim(endDate) == "") {
            alert("请选择截止日期!");
            return false;
        }
        $("#workload_search_form").submit();
        return true;
    }
</script>
</@layout.page>