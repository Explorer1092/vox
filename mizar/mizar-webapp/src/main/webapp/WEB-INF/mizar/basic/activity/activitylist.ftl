<#import "../../module.ftl" as module>
<@module.page
title="亲子活动管理"
pageJsFile={"siteJs" : "public/script/basic/activity"}
pageJs=["siteJs"]
leftMenu="亲子活动"
>
<div class="op-wrapper clearfix">
    <a class="blue-btn" href="/basic/activity/detail.vpage">新增活动</a>
</div>

<form id="filter-form">
    <input id="pageIndex" type="hidden" name="pageIndex" value="${pageIndex!1}">
</form>
<table class="data-table" <#--style="margin-bottom:50px;"-->>
    <thead>
    <tr>
        <th style="text-align: center;width: 300px;">标题</th>
        <th style="text-align: center;width: 100px;">类型</th>
        <th style="text-align: center;">简介</th>
        <th style="text-align: center; width: 50px;">状态</th>
        <th style="text-align: center; width: 90px;">操作</th>
    </tr>
    </thead>
    <tbody>
    <#if activityList?size gt 0>
        <#list activityList as activity>
        <tr>
            <td>${(activity.title)!''}</td>
            <td style="text-align: center;">
                <#switch (activity.goodsType)!>
                    <#case 'family_activity'> 亲子活动 <#break>
                    <#case 'ustalk'> USTalk <#break>
                    <#default> 未知类型 <#break>
                </#switch>
            </td>
            <td>${(activity.desc)!''}</td>
            <td>${(activity.status)!''}</td>
            <td>
                <#if (activity.status) == "在线">
                    <a class="op-btn" href="/basic/activity/detail.vpage?gid=${activity.id!''}" style="margin-right:0;">查看</a>
                    <a class="op-btn op-status" data-status="OFFLINE" data-gid="${activity.id!}" href="javascript:void(0);" style="margin-right:0;float:right;">下线</a>
                <#else>
                    <a class="op-btn" href="/basic/activity/detail.vpage?gid=${activity.id!''}" style="margin-right:0;">编辑</a>
                    <a class="op-btn op-status" data-status="ONLINE" data-gid="${activity.id!}" href="javascript:void(0);" style="margin-right:0;float:right;">上线</a>
                </#if>
            </td>
        </tr>
        </#list>
    <#else>
        <tr>
            <td colspan="4" style="text-align: center">暂时还没有亲子活动哦~</td>
        </tr>
    </#if>
    </tbody>
</table>
<div id="paginator" pageIndex="${(pageIndex!1)}" title="后台从0开始,分页插件从第1页开始" class="paginator clearfix" totalPage="${totalPages!1}"></div>
</@module.page>