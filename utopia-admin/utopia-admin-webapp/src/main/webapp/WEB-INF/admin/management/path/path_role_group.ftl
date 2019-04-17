<#include "../index.ftl" />

<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>${pageMessage!}：</legend>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>所属业务系统</td>
                        <td>路径</td>
                        <td>名称</td>
                        <td>角色</td>
                    </tr>
                    <tr>
                        <td>${appNames[pathRoleInfo.APP_NAME]!}</td>
                        <td>${pathRoleInfo.PATH_NAME!}</td>
                        <td>${pathRoleInfo.PATH_DESCRIPTION!}</td>
                        <td>${pathRoleInfo.ROLE_NAME!}</td>
                    </tr>
                </table>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>权限组标识</td>
                        <td>权限组名称</td>
                        <td></td>
                    </tr>
                    <#list groupList as groupInfo>
                        <tr>
                            <td>${groupInfo_index+1}</td>
                            <td>${groupInfo.name!}</td>
                            <td>${groupInfo.description!}</td>
                            <td class="form-inline">
                                <label class="checkbox">
                                    <input type="checkbox" value="${groupInfo.name!}" class="role" <#if pathRolegroups?contains(groupInfo.name)>checked</#if>>
                                </label>
                            </td>
                        </tr>
                    </#list>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $('.role').click( function () {
        var status = $(this).is(':checked');
        if (status) {
            $.post('path_role_group_edit.vpage'
                    , {'do': 'add', 'pathRoleId': '${pathRoleInfo.ID!}', 'groupName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('path_role_group_edit.vpage'
                    , {'do': 'del', 'pathRoleId': '${pathRoleInfo.ID!}', 'groupName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
    function de_confirm(){
        if ( ! confirm("确认删除吗?")){
            return false;
        }
    }
</script>