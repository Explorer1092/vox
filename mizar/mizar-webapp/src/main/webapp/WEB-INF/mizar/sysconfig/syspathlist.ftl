<#import "../module.ftl" as module>
<@module.page
title="功能权限"
pageJsFile={"siteJs" : "public/script/config/syspath"}
pageJs=["siteJs"]
leftMenu = "功能权限"
>

<div class="op-wrapper orders-wrapper clearfix">
    <form id="index-form" action="/config/syspath/index.vpage" method="post">
        <div class="item">
            <p>一级功能名称</p>
            <input value="${searchFunctionName!}" name="searchFunctionName" class="v-select" />
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
<#if sysPathList?? && sysPathList?size gt 0>
    <#list sysPathList as page>
    <table class="data-table one-page <#if page_index == 0>displayed</#if>">
        <thead>
        <tr>
            <th style="width:80px;">一级功能</th>
            <th style="width:80px;">二级功能</th>
            <th style="width:120px;">说明</th>
            <th>角色列表</th>
            <th style="width:110px;">操作</th>
        </tr>
        </thead>
        <tbody>
            <#if page?? && page?size gt 0>
                <#list page as sysPath>
                <tr>
                    <td>${sysPath.appName!}</td>
                    <td>${sysPath.pathName!}</td>
                    <td>${sysPath.description!}</td>
                    <td>
                        <#if sysPath.authRoleList??>
                            <#list sysPath.authRoleList as roleItem>
                                <#if (roleItem.roleGroupId)?? >${(allGroupMap[roleItem.roleGroupId?string])!''}<#if roleItem_index < (sysPath.authRoleList?size - 1) >，</#if> </#if>
                            </#list>
                        </#if>
                    </td>
                    <td>
                        <a class="op-btn" href="addindex.vpage?id=${sysPath.id!}">编辑</a> &nbsp;&nbsp;
                        <a id="delete_path_${sysPath.id!}" class="op-btn" href="javascript:void(0);">删除</a>
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
            <th style="width:80px;">一级功能</th>
            <th style="width:80px;">二级功能</th>
            <th style="width:120px;">说明</th>
            <th>角色列表</th>
            <th style="width:110px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td colspan="5" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
        </tr>
        </tbody>
    </table>
</#if>
<div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
</@module.page>
