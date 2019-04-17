<#include "../index.ftl" />

<script type="text/javascript">
    function de_confirm(){
        if ( ! confirm("确认删除吗?")){
            return false;
        }
    }
</script>

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
                    </tr>
                    <tr>
                        <td>${pathInfo.appName!}</td>
                        <td>${pathInfo.pathName!}</td>
                        <td>${pathInfo.pathDescription!}</td>
                    </tr>
                </table>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>角色标识</td>
                        <td>角色名称</td>
                        <td></td>
                    </tr>
                    <#if roleList??>
                    <#list roleList as roleInfo>
                    <tr>
                        <td>${roleInfo_index+1}</td>
                        <td>${roleInfo.name!}</td>
                        <td>${roleInfo.description!}</td>
                        <td class="form-inline">
                            <label class="checkbox">
                                <input type="checkbox" value="${roleInfo.name!}" class="role" <#if pathRoles?contains(roleInfo.name)>checked</#if>>
                            </label>
                        </td>
                    </tr>
                    </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $('.role').click( function () {
        var status = $(this).is(':checked');
        if (status) {
            $.post('path_role_edit.vpage'
                    , {'do': 'add', 'pathId': '${pathInfo.id!}', 'roleName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('path_role_edit.vpage'
                    , {'do': 'del', 'pathId': '${pathInfo.id!}', 'roleName': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
</script>