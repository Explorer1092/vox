<#import "../module.ftl" as module>
<@module.page
title="用户管理"
pageJsFile={"siteJs" : "public/script/config/sysuser"}
pageJs=["siteJs"]
leftMenu = "用户管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="index-form" action="/config/user/index.vpage" method="post">
        <div class="item">
            <p>用户名</p>
            <input value="${searchUserName!}" name="searchUserName" class="v-select" />
        </div>
        <div class="item">
            <p>用户状态</p>
            <select name="searchUserStatus" class="v-select" class="sel">
                <option value="1" <#if searchUserStatus == 1>selected</#if> >使用中</option>
                <option value="0" <#if searchUserStatus == 0>selected</#if> >未使用</option>
                <option value="9" <#if searchUserStatus == 9>selected</#if> >已关闭</option>
                <option value="-1" <#if searchUserStatus == -1>selected</#if> >全 部</option>
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
<#if userList?? && userList?size gt 0>
    <#list userList as page>
        <table class="data-table one-page <#if page_index == 0>displayed</#if>">
            <thead>
            <tr>
                <th style="width:138px;">账号</th>
                <th style="width:100px;">姓名</th>
                <th style="width:110px;">电话号码</th>
                <th style="width:150px;">角色列表</th>
                <th>备注</th>
                <th style="width:70px;">状态</th>
                <th style="width:110px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <#if page?? && page?size gt 0>
                <#list page as user>
                <tr>
                    <td>${user.accountName!}</td>
                    <td>${user.realName!}</td>
                    <td>${user.mobile!}</td>
                    <td>
                        <#if user.groupRoleIds?? && user.groupRoleIds?size gt 0>
                            <#list user.groupRoleIds as roleId>
                            ${(allGroupMap[roleId])!''}<br/>
                            </#list>
                        </#if>
                    </td>
                    <td>${user.userComment!}</td>
                    <td><#if user.status == 0>未登录<#elseif user.status == 1>使用中<#else>已关闭</#if></td>
                    <td>
                        <a class="op-btn" href="addindex.vpage?id=${user.id!}">编辑</a> &nbsp;
                        <a id="delete_user_${user.id!}" class="op-btn" href="javascript:void(0);">关闭</a>
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
        <th style="width:138px;">账号</th>
        <th style="width:100px;">姓名</th>
        <th style="width:110px;">电话号码</th>
        <th style="width:100px;">角色列表</th>
        <th>备注</th>
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