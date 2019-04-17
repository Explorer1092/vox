<#include "../index.ftl" />

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>
                    ${pageMessage!}列表：
                        <span class="pull-right">
                            <a class="btn btn-primary" href="group_new.vpage">添加新${pageMessage!}</a>
                        </span>
                </legend>

                <form class="form-horizontal">
                    <select name="groupName">
                        <option value="">All</option>
                        <#list selectGroupList as groupItem>
                        <option value="${groupItem.name!}" <#if groupItem.name == groupName> selected="selected" </#if>>${groupItem.description!}</option>
                        </#list>
                    </select>
                </form>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>标识ID</td>
                        <td>名称</td>
                        <td></td>
                    </tr>
                    <#if groupList??>
                    <#list groupList as groupItem>
                    <tr>
                        <td>${groupItem_index+1}</td>
                        <td>${groupItem.name!}</td>
                        <td>${groupItem.description!}</td>
                        <td>
                        <#if groupForWrite?contains(groupItem.name)>
                            <a href="group_member.vpage?name=${groupItem.name!}" class="btn btn-mini btn-info">关联用户</a>
                            <a href="group_role.vpage?name=${groupItem.name!}&t=group" class="btn btn-mini">关联角色</a>
                            <a href="group_edit.vpage?name=${groupItem.name!}" class="btn btn-mini">修改</a>
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
    $('form').submit();
});
</script>