<#import "../module.ftl" as module>
<@module.page
title="部门管理-编辑"
pageJsFile={"siteJs" : "public/script/config/sysgroup"}
pageJs=["siteJs"]
leftMenu="部门管理"
>

<div class="bread-nav">
    <a class="parent-dir" href="/config/user/index.vpage">部门管理</a>
    &gt;
    <a class="current-dir" href="javascript:void(0)" style="cursor: default">${isNew?string("新增","编辑")}部门</a>
</div>
<form id="detail-form" action="/config/group/updategroup.vpage" method="post">
    <input value="${groupId!}" name="id" style="display:none;">
    <input value="" name="roles" style="display:none;">
    <div class="input-control">
        <label><span class="red-mark">*</span> 名称：</label>
        <input name="name" data-title="账号" class="require item" value="${(groupInfo.departmentName)!}"
                placeholder="账号只能输入中文、字母、数字以及下划线"/>
    </div>
    <div class="input-control">
        <label>介绍：</label>
        <textarea name="description" data-title="介绍" style="resize: none" rows="3">${(groupInfo.description)!}</textarea>
    </div>
    <div class="input-control">
        <label><span class="red-mark">*</span>包含角色：</label>
        <#--<select name="role" class="require item">
            <#list allRoleMap?keys as item>
                <option value="${item!}" <#if ((groupInfo.role)!)?string == item>selected="selected"</#if> >${allRoleMap[item?string].roleName!}</option>
            </#list>
        </select>-->
        <div class="checkboxes clearfix" style="overflow: hidden;">
            <#if isNew && currentUser.isBD()>
                <#-- BD角色只能新建机构业主 -->
                <label class="checkbox">
                    <input class="role-checkbox" data-value="21" type="checkbox" checked disabled/>机构业主
                </label>
            <#else>
                <#list allRoleMap?keys as item>
                    <label class="checkbox"><input class="role-checkbox" data-value="${item!}" type="checkbox"
                        <#if groupInfo?? && groupInfo.ownRoles??>
                            <#list groupInfo.ownRoles as roleItem>
                                <#if item == roleItem?string> checked </#if>
                                <#-- BD角色只能编辑机构业主 -->
                                <#if currentUser.isBD() && item != "21"> disabled </#if>
                            </#list>
                        </#if>
                    />${allRoleMap[item?string].roleName!}</label> <br/>
                </#list>
            </#if>
        </div>
    </div>
    <#if (!isNew)!false>
        <div class="input-control">
            <label>机构列表：</label>
            <a id="AddingAgenciesBtn" class="blue-btn submit-search" href="javascript:void(0)" style="float: none; display: inline-block;">添加机构</a>
            <#if shopList?? && shopList?size gt 0>
                <div class="op-wrapper clearfix">
                    <table class="data-table" style="width:550px; margin-left: 84px;">
                        <#list shopList as shop>
                            <tr>
                                <td style="width:140px;">${shop.id!}</td>
                                <td>${shop.fullName!}</td>
                                <td style="width:70px;">
                                    <a id="delete_usershop_${shop.id!}" class="op-btn change-remark" href="javascript:void(0)" style="float: none; display: inline-block;">删除</a>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>
            </#if>
        </div>
    <#--BD不能编辑公众号-->
        <#if !currentUser.isBD()>
            <div id="officialAccountDiv" class="input-control" <#if showOA?? &&showOA><#else>style="display: none"</#if> >
                <label>公众号列表：</label>
                <a id="addOfficialAccountBtn" class="blue-btn submit-search" href="javascript:void(0)" style="float: none; display: inline-block; width: auto;">添加公众号</a>

            </div>
            <div id="officialAccountBox" class="input-control"></div>
        </#if>
    </#if>
    <#if ((!isNew)!false) && users?? && users?size gt 0>
    <div class="input-control" id="users-list">
        <label><span class="red-mark">*</span>用户列表：</label>
        <#--<a id="add-user-btn" class="blue-btn submit-search" href="javascript:void(0)" style="float: none; display: inline-block;">添加用户</a>-->
        <div id="users-table" class="input-control" style="display:inline-block">

        </div>
    </div>
    </#if>


    <div class="clearfix submit-box" style="margin:20px 85px;">
        <a id="save-btn" class="submit-btn save-btn" href="javascript:void(0)">保存</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="/config/group/index.vpage">取消</a>
    </div>
</form>
</@module.page>

<script type="text/html" id="officialAccount_item">
    <%if(list.length > 0){%>
    <table class="data-table one-page displayed"  style="margin-left: 84px;width:90%">
        <thead>
        <tr>
            <th>公众号标识</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <%for(var i = 0; i < list.length;i++){%>
        <tr>
            <td><%=list[i].officialAccountKey%></td>
            <td>
                <a class="OfficialAccountDeleteBtn" href="javascript:void (0);" style="color: #00a0e9;" data-index="<%=i%>" data-accountkey="<%=list[i].officialAccountKey%>">删除</a>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <%}%>
</script>

<script type="text/html" id="users_item">
    <% if(users.length > 0){%>
    <table class="data-table one-page displayed" style="width:680px;">
        <thead>
        <tr>
            <th>账号</th>
            <th>姓名</th>
            <th>角色</th>
            <#--<th>备注</th>-->
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <%for(var i = 0;i < users.length;i++){%>
        <tr>
            <td><%=users[i].accountName%></td>
            <td><%=users[i].realName%></td>
            <td></td>
            <#--<td><%=users[i].userComment%></td>-->
            <td>
                <a class="del-user-btn" href="javascript:void (0);" style="color: #00a0e9;" data-index="<%=i%>" data-userid="<%=users[i].id%>">删除</a>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <%}%>
</script>

<script type="text/javascript">
    <#if officialAccountsList?? && officialAccountsList?size gt 0>
    var pageOfficialAccountsList = [];
        <#list officialAccountsList as officialAccounts>
        pageOfficialAccountsList.push({
             officialAccountKey : '${officialAccounts.accountsKey!}'
        });
        </#list>
    </#if>

    <#if users?? && users?size gt 0>
    var userList = [];
    <#list users as user>
        userList.push({
            id:"${user.id}",
            accountName:"${user.accountName}",
            realName:"${user.realName}",
            userComment:"${user.userComment}"
        });
    </#list>
    </#if>
</script>
