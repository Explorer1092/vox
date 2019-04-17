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
                    <tr>
                        <td>${groupInfo.name!}</td>
                        <td>${groupInfo.description!}</td>
                    </tr>
                </table>

                <div class="oulist table">
                    <form class="form-horizontal" method="get" id="query_frm">
                        <input type="text" class="input-large" placeholder="请输入路径" name="pathName" value="${pathName!}">
                        <input type="hidden" name="name" value="${groupName!}">
                        <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                        <button type="submit" class="btn">查找</button>
                    </form>

                    <@pager.pager/>
                    <table class="table table-striped table-bordered table-hover">
                        <tr>
                            <th></th>
                            <th>所属系统</th>
                            <th>路径</th>
                            <th>名称</th>
                            <th>角色</th>
                        </tr>
                        <#if pathPage?? && pathPage.content?? >
                        <#list pathPage.content as pathInfo>
                        <tr>
                            <td>${pathInfo_index+1}</td>
                            <td>${appNames[pathInfo.appName]!}</td>
                            <td>${pathInfo.pathName!}</td>
                            <td>${pathInfo.pathDescription!}</td>
                            <td class="form-inline">
                                <#assign pathInfoId = "${pathInfo.id}" >
                                <#if pathRoleList[pathInfoId]??>
                                <#list pathRoleList[pathInfoId] as pathRoleInfo>
                                <label class="checkbox">
                                    <input type="checkbox" value="${pathRoleInfo.pathRoleId!}" class="role" <#if pathRoleIds?contains('${pathRoleInfo.pathRoleId!}')>checked</#if>>
                                ${roleNames[pathRoleInfo.roleName]!} - ${pathRoleInfo.roleName!}
                                </label>
                                <br>
                                </#list>
                                </#if>
                            </td>
                        </tr>
                        </#list>
                        </#if>
                    </table>
                    <@pager.pager/>
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
            $.post('/management/path/path_role_group_edit.vpage'
                    , {'do': 'add', 'groupName': '${groupInfo.name!}', 'pathRoleId': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }else{
            $.post('/management/path/path_role_group_edit.vpage'
                    , {'do': 'del', 'groupName': '${groupInfo.name!}', 'pathRoleId': $(this).val() }
                    , function(data) {
                        // ..
                    });
        }
    });
</script>