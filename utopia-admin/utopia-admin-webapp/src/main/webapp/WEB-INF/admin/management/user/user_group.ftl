<#include "../index.ftl" />

<!-- start content -->
<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>${pageMessage!}：</legend>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>用户ID</td>
                        <td>姓名</td>
                        <td>部门</td>
                    </tr>
                    <tr>
                        <td>${userInfo.adminUserName!}</td>
                        <td>${userInfo.realName!}</td>
                        <td>${departmentNames[userInfo.departmentName]!}</td>
                    </tr>
                </table>

                <div class="oulist table">
                    <table class="table table-striped table-bordered">
                        <tr>
                            <td></td>
                            <td>权限组标识ID</td>
                            <td>权限组名称</td>
                            <td></td>
                        </tr>
                        <#list groupList as groupItem>
                        <tr>
                            <td>${groupItem_index+1}</td>
                            <td>${groupItem.name!}</td>
                            <td>${groupItem.description!}</td>
                            <td class="form-inline">
                                <label class="checkbox">
                                    <input type="checkbox" value="${groupItem.name!}" class="role" <#if userGroupInfo?contains(groupItem.name)>checked</#if>>
                                </label>
                            </td>
                        </tr>
                        </#list>
                    </table>
                </div>

            </div>
        </div>
    </div>
</div>
<!-- end content -->
<script type="text/javascript">
    $('.role').click( function () {
        var status = $(this).is(':checked');
        if (status) {
            $.post('/management/group/group_member_edit.vpage'
                    , {'do': 'add', 'userName': '${userInfo.adminUserName!}', 'groupName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('/management/group/group_member_edit.vpage'
                    , {'do': 'del', 'userName': '${userInfo.adminUserName!}', 'groupName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
</script>