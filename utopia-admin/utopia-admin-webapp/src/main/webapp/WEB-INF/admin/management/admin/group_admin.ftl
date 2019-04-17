<#include "../index.ftl" />

<div class="container">
    <div class="breadcrumb">${pageMessage!}</div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>权限组标识</td>
                        <td>权限组名称</td>
                    </tr>
                    <tr>
                        <td>${groupInfo.name!}</td>
                        <td>${groupInfo.description!}</td>
                    </tr>
                </table>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>管理员ID</td>
                        <td>管理</td>
                    </tr>
                    <#list masterList as masterItem>
                    <tr>
                        <td>${masterItem_index+1}</td>
                        <td>${masterItem.userName!}</td>
                        <td class="form-inline">
                            <#if rightDelete>
                            <label class="checkbox">
                                <input type="checkbox" data-action="rightRead" value="${masterItem.id!''}" class="role" <#if masterItem.rightRead! >checked</#if>> 读
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" data-action="rightWrite" value="${masterItem.id!''}" class="role" <#if masterItem.rightWrite! >checked</#if>> 写
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" data-action="rightDelete" value="${masterItem.id!''}" class="role" <#if masterItem.rightDelete!>checked</#if> > 删除
                            </label>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </table>

                <form class="form-horizontal" id="form1" name="form1" method="post" action="" >
                    <input type="text" class="input-small" placeholder="用户名" name="userName" value="${userName!}">
                    <input type="hidden"  name="groupName" value="${groupInfo.name!}">
                    <button type="submit" class="btn">添加新管理员</button>
                </form>

            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $('.role').click( function () {
        var status = $(this).is(':checked');
        if (status) {
            $.post('group_admin_edit.vpage'
                    , {'do': 'add', 'action': $(this).data('action'), adminMasterId: $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('group_admin_edit.vpage'
                    , {'do': 'del', 'action': $(this).data('action'), adminMasterId: $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
</script>