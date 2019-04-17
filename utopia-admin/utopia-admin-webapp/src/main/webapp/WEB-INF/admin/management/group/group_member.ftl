<#include "../index.ftl" />
<#import "../../mizar/pager.ftl" as pager />

<!-- start content -->
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>${pageMessage!}：</legend>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>权限组标识</td>
                        <td>权限组名称</td>
                    </tr>
                    <#if groupInfo??>
                    <tr>
                        <td>${groupInfo.name!}</td>
                        <td>${groupInfo.description!}</td>
                    </tr>
                    </#if>
                </table>

                <form class="form-horizontal" method="get" id="query_frm">
                    <select name='departmentName'>
                        <option value="">All</option>
                        <#list departmentList as departmentItem>
                        <option value="${departmentItem.name!}" <#if departmentItem.name==departmentName> selected="selected" </#if>>${departmentItem.description!}</option>
                        </#list>
                    </select>
                    <input type="text" class="input-large" placeholder="请输入用户名" name="adminUserName" value="${adminUserName!}">
                    <input type="hidden" name="name" value="${groupName!}">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <button type="submit" class="btn">查找</button>
                </form>

                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>部门</td>
                        <td>用户名</td>
                        <td>真实姓名</td>
                    </tr>
                    <#if userPage?? && userPage.content?? >
                        <#list userPage.content as userItem>
                        <tr>
                            <td>${userItem_index+1}</td>
                            <td>${userItem.departmentName!}</td>
                            <td>${userItem.adminUserName!}</td>
                            <td>${userItem.realName!}</td>
                            <td class="form-inline">
                                <label class="checkbox">
                                    <input type="checkbox" value="${userItem.adminUserName!}" class="user_uid" <#if groupUserInfo?contains(userItem.adminUserName)>checked</#if>>
                                </label>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </table>
                <@pager.pager/>


                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>所属系统</td>
                        <td>路径</td>
                        <td>名称</td>
                        <td class="form-inline">已有角色</td>
                    </tr>
                    <#list appPathRoleByGroup as appPathRoleByGroupItem>
                    <tr>
                        <td>${appPathRoleByGroupItem_index+1}</td>
                        <td>${appNames[appPathRoleByGroupItem.APP_NAME]!}</td>
                        <td>${appPathRoleByGroupItem.PATH_NAME!}</td>
                        <td>${appPathRoleByGroupItem.PATH_DESCRIPTION!}</td>
                        <td>${roleNames[appPathRoleByGroupItem.ROLE_NAME]!}</td>
                    </tr>
                    </#list>
                </table>
            </div>
        </div>
    </div>
</div>
<!-- end content -->
<script type="text/javascript">
    $("select").change(function () {
        $('form').submit();
    });

    $('.user_uid').click( function () {
        var status = $(this).is(':checked');
        if (status) {
            $.post('group_member_edit.vpage'
                    , {'do': 'add', 'groupName': '${groupInfo.name!}', 'userName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('group_member_edit.vpage'
                    , {'do': 'del', 'groupName': '${groupInfo.name!}', 'userName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
</script>

