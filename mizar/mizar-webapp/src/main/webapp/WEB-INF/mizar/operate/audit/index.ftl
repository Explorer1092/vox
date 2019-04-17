<#import "../../module.ftl" as module>
<@module.page
title="变更审核"
pageJsFile={"siteJs" : "public/script/operate/audit"}
pageJs=["siteJs"]
leftMenu="变更审核"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" action="/operate/audit/index.vpage" method="get">
        <div class="item">
            <p>账号</p>
            <input value="${token!}" name="token" class="v-select" placeholder="申请人账号/手机"/>
        </div>
        <div class="item">
            <p>申请人</p>
            <input value="${applicant!}" name="applicant" class="v-select" placeholder="申请人"/>
        </div>
        <div class="item">
            <p>类型</p>
            <select name="type" class="v-select">
                <option value="">全部</option>
                <option <#if type?? && type=='brand'>selected</#if> value="brand">品牌</option>
                <option <#if type?? && type=='shop'>selected</#if> value="shop">机构</option>
                <option <#if type?? && type=='goods'>selected</#if> value="goods">课程</option>
                <option <#if type?? && type=='family_activity'>selected</#if> value="family_activity">亲子活动</option>
            </select>
        </div>
        <div class="item">
            <p>申请状态</p>
            <select name="status" class="v-select">
                <option <#if status?? && status=='PENDING'>selected</#if> value="PENDING">待审核</option>
                <option <#if status?? && status=='APPROVE'>selected</#if> value="APPROVE">通过</option>
                <option <#if status?? && status=='REJECT'>selected</#if> value="REJECT">驳回</option>
            </select>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>
</div>
<#if auditList?? && auditList?size gt 0>
    <#list auditList as page>
    <table class="data-table one-page <#if page_index == 0></#if>displayed">
        <thead>
        <tr>
            <th>类型</th>
            <th>新增/变更</th>
            <th>申请人</th>
            <th>申请时间</th>
            <th>描述</th>
            <th>处理状态</th>
            <th style="width:100px;">操作</th>
        </tr>
        </thead>
        <tbody>
            <#list page as item>
                <#switch (item.entityType)!>
                    <#case "brand">
                         <#assign entityName = "品牌" detailUrl="/operate/audit/auditbrand.vpage?id=${item.id!}&targetId=${item.targetId!}" />
                         <#break />
                    <#case "shop">
                        <#assign entityName = "机构" detailUrl="/operate/audit/auditshop.vpage?id=${item.id!}&targetId=${item.targetId!}" />
                        <#break />
                    <#case "goods">
                        <#assign entityName = "课程" detailUrl="/operate/audit/goodsinfo.vpage?rid=${item.id!}&gid=${item.targetId!}" />
                        <#break />
                    <#case "family_activity">
                        <#assign entityName = "亲子活动" detailUrl="/operate/audit/activityinfo.vpage?rid=${item.id!}&gid=${item.targetId!}" />
                        <#break />
                </#switch>
            <tr>
                <#switch (item.auditStatus)!>
                    <#case "PENDING">
                        <#assign displayHtml = "待审核" opType="op-pending" />
                        <#break />
                    <#case "APPROVE">
                        <#assign displayHtml = "已通过" opType="op-approve"/>
                        <#break />
                    <#case "REJECT">
                        <#assign displayHtml = "已驳回" opType="op-reject"/>
                        <#break />
                </#switch>
                <td>${entityName!''}</td>
                <td><#if item.targetId?? && item.targetId?has_content>变更<#else>新增</#if></td>
                <td>${item.applicant!''}</td>
                <td>${item.createAt?string('yyyy-MM-dd')}</td>
                <td>${item.desc!''}</td>
                <td>${displayHtml!''}</td>
                <td>
                    <a class="op-btn ${opType}" data-link="${detailUrl!}" data-gid="${item.targetId!}" href="javascript:void(0);">查看详情</a>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
    </#list>
<#else>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th>类型</th>
            <th>新增/变更</th>
            <th>申请人</th>
            <th>申请时间</th>
            <th>描述</th>
            <th>处理状态</th>
            <th style="width:100px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td colspan="7" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下暂时没有申请~"}</td>
        </tr>
        </tbody>
    </table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>