<#import "../layout.ftl" as layout>
<@layout.page group="workbench" title="有效工作量">
<div id="visit-nav" data-role="navbar">
    <ul>
        <li><a href="workload.vpage" data-ajax="false">统计</a></li>
        <li><a href="#" data-ajax="false" class="ui-btn-active">排行榜</a></li>
        <li><a href="visitor_list.vpage" data-ajax="false">联系列表</a></li>
    </ul>
</div>

<br>
<table data-role="table" data-mode="reflow" class="ui-responsive table-stroke">
    <thead>
    <tr>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td colspan="3"><a>本月</a></td>
    </tr>
    <tr>
        <td style="text-align: center; color: gold">冠军</td>
        <td style="text-align: center; color: yellowgreen">亚军</td>
        <td style="text-align: center; color: greenyellow">季军</td>
    </tr>
    <tr>
        <td style="text-align: center; color: red"><strong>${(thisMonthRank.top1.ranker)!"-"}</strong></td>
        <td style="text-align: center; color: red"><strong>${(thisMonthRank.top2.ranker)!"-"}</strong></td>
        <td style="text-align: center; color: red"><strong>${(thisMonthRank.top3.ranker)!"-"}</strong></td>
    </tr>
    <tr>
        <td style="text-align: center; color: blue">
            <#if (thisMonthRank.top1)??>
                <strong>${((thisMonthRank.top1.workload)/100)?string("#")}</strong>
            <#else>
                <strong>-</strong>
            </#if>
        </td>
        <td style="text-align: center; color: blue">
            <#if (thisMonthRank.top2)??>
                <strong>${((thisMonthRank.top2.workload)/100)?string("#")}</strong>
            <#else>
                <strong>-</strong>
            </#if>
        </td>
        <td style="text-align: center; color: blue">
            <#if (thisMonthRank.top3)??>
                <strong>${((thisMonthRank.top3.workload)/100)?string("#")}</strong>
            <#else>
                <strong>-</strong>
            </#if>
        </td>
    </tr>
    </tbody>
</table>

<br>
<table data-role="table" data-mode="reflow" class="ui-responsive table-stroke">
    <thead>
    <tr>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td colspan="3"><a>上月</a></td>
    </tr>
    <tr>
        <td style="text-align: center; color: gold">冠军</td>
        <td style="text-align: center; color: yellowgreen">亚军</td>
        <td style="text-align: center; color: greenyellow">季军</td>
    </tr>
    <tr>
        <td style="text-align: center; color: red"><strong>${(lastMonthRank.top1.ranker)!"-"}</strong></td>
        <td style="text-align: center; color: red"><strong>${(lastMonthRank.top2.ranker)!"-"}</strong></td>
        <td style="text-align: center; color: red"><strong>${(lastMonthRank.top3.ranker)!"-"}</strong></td>
    </tr>
    <tr>
        <td style="text-align: center; color: blue">
            <#if (lastMonthRank.top1)??>
                <strong>${((lastMonthRank.top1.workload)/100)?string("#")}</strong>
            <#else>
                <strong>-</strong>
            </#if>
        </td>
        <td style="text-align: center; color: blue">
            <#if (lastMonthRank.top2)??>
                <strong>${((lastMonthRank.top2.workload)/100)?string("#")}</strong>
            <#else>
                <strong>-</strong>
            </#if>
        </td>
        <td style="text-align: center; color: blue">
            <#if (lastMonthRank.top3)??>
                <strong>${((lastMonthRank.top3.workload)/100)?string("#")}</strong>
            <#else>
                <strong>-</strong>
            </#if>
        </td>
    </tr>
    </tbody>
</table>
</@layout.page>