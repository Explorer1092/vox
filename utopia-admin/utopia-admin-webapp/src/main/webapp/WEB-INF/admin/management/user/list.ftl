<#include "../index.ftl" />


<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>
                    ${pageMessage!''}列表：
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a href="/adminuser/edit.vpage" class="button">添加新用户</a>
                </legend>
                <form class="form-horizontal" method="get">
                    <select id="depart_select" name="departmentName">
                        <option value="">All</option>
                        <#if departmentList?has_content>
                            <#list departmentList as departmentItem>
                                <option value="${departmentItem.name!}" <#if departmentItem.name == departmentName > selected="selected" </#if>>${departmentItem.description!}</option>
                            </#list>
                        </#if>
                    </select>

                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>用户名</td>
                        <td>真实姓名</td>
                        <td>部门</td>
                        <td></td>
                    </tr>
                    <#if userList??>
                    <#list userList as userInfo>
                    <tr>
                        <td>${userInfo_index+1!}</td>
                        <td>${userInfo.adminUserName!}</td>
                        <td>${userInfo.realName!}</td>
                        <td>${departmentNames[userInfo.departmentName]!}</td>
                        <td>
                            <a href="user_group.vpage?name=${userInfo.adminUserName!}" class="btn btn-mini">权限组</a>
                            <#if departmentForWrite?contains(userInfo.departmentName) >
                            <a href="edit.vpage?userName=${userInfo.adminUserName!}" class="btn btn-mini">修改</a>
                            <#--<a href="resetpw/?cn=${userInfo.id!}" class="btn btn-mini" onclick="javascript:return de_confirm('确认重置密码？');">重置密码</a>-->
                            </#if>
                            <#if departmentForDelete?contains(userInfo.departmentName) >
                            <#--<a href="del/?cn={{ one.dn }}" class="btn btn-mini btn-danger" onclick="javascript:return de_confirm('确认删除用户？');">删除</a>-->
                            <#--<a href="group/clear/?cn={{ one.dn }}" class="btn btn-mini btn-danger">清空权限</a>-->
                            </#if>
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
    $("select").change(function () {
        var str = $("select option:selected").val();
        $('form').submit();
    });

    function de_confirm(msg){
        if ( ! confirm(msg)){
            return false;
        }
    }
</script>