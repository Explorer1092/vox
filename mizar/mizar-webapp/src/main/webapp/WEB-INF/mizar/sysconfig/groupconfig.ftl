<#import "../module.ftl" as module>
<@module.page
title="部门管理"
pageJsFile={"siteJs" : "public/script/config/sysgroup"}
pageJs=["siteJs"]
leftMenu = "部门管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="index-form" action="/config/group/index.vpage" method="post">
        <div class="item">
            <p>组名</p>
            <input value="${searchGroupName!}" name="searchGroupName" class="v-select" />
        </div>
        <div class="item">
            <p>组状态</p>
            <select name="searchStatus" class="v-select sel">
                <option value="1" <#if disabledStatus == 1>selected</#if>>有 效</option>
                <option value="0" <#if disabledStatus == 0>selected</#if>>无 效</option>
                <option value="-1" <#if disabledStatus == -1>selected</#if>>全 部</option>
            </select>
        </div>
        <div class="item" style="width:auto;margin-right:10px;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="index-filter" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="index-add" style="float:left;" href="javascript:void(0)">新增</a>
        </div>
    </form>
</div>
<#if groupList?? && groupList?size gt 0>
    <#list groupList as page>
        <table class="data-table one-page <#if page_index == 0>displayed</#if>">
            <thead>
            <tr>
                <th style="width:120px;">组名</th>
                <th style="width:100px;">角色</th>
                <th style="width:100px;">介绍</th>
                <th style="width:70px;">状态</th>
                <th style="width:110px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <#if page?? && page?size gt 0>
                <#list page as group>
                <tr>
                    <td>${group.departmentName!}</td>
                    <td>
                        <#if group.ownRoles?? && group.ownRoles?has_content>
                            <#list group.ownRoles as role>
                                ${(roleMap[role?string])!''}<#if role_has_next> | </#if>
                            </#list>
                        <#else>
                            --
                        </#if>
                    </td>
                    <td>${group.description!}</td>
                    <td><#if group.disabled!false>无效<#else>有效</#if></td>
                    <td>
                        <a class="op-btn" href="update.vpage?id=${group.id!}">编辑</a> &nbsp;
                        <a data-id="${group.id!}" class="close-group-btn op-btn" href="javascript:void(0);">关闭</a>
                    </td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
    </#list>
<#else>
<table class="data-table one-page displayed">
    <thead>
    <tr>
        <th style="width:138px;">组名</th>
        <th style="width:100px;">介绍</th>
        <th style="width:70px;">状态</th>
        <th style="width:110px;">操作</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td colspan="7" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
    </tr>
    </tbody>
</table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>