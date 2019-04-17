<#include "../index.ftl" />

<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>${pageMessage!}列表：</legend>

                <form class="form-horizontal">
                    <input type="text" class="input-small" placeholder="Group" name="groupName" value="${groupName!}">
                    <button type="submit" class="btn">查找</button>
                </form>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>标识ID</td>
                        <td>名称</td>
                        <td>管理员</td>
                    </tr>
                    <#if groupList?has_content>
                        <#list groupList as groupInfo>
                        <tr>
                            <td>${groupInfo_index+1}</td>
                            <td>${groupInfo.name!}</td>
                            <td>${groupInfo.description!}</td>
                            <td>
                                ${masterList[groupInfo.name]!}
                                <#if groupListForWrite?contains(groupInfo.name) ><a href="group_admin.vpage?name=${groupInfo.name!}" class="btn btn-small pull-right">管理</a></#if>
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
    function de_confirm(){
        if ( ! confirm("确认删除吗?")){
            return false;
        }
    }
</script>