<#import "../layout_default.ftl" as layout_default />

<@layout_default.page page_title="管理员列表" page_num=2>
    <div class="row-fluid">
            <div style="padding: 0 0 10px;">
                <a href='edit.vpage' class="btn"><i class="icon-plus"></i> 新建</a>
            </div>

            <table class="table table-hover table-striped table-bordered ">
                <tr>
                    <th>adminUserName</th>
                    <th>createDatetime</th>
                    <th>realName</th>
                    <th>comment</th>
                    <th>操作</th>
                </tr>

                <#list adminUserList![] as adminUser>

                    <tr>
                        <td>${adminUser.adminUserName}</td>
                        <td>${adminUser.createDatetime}</td>
                        <td>${adminUser.realName}</td>
                        <td>${adminUser.comment?html}</td>
                        <td>
                            <a href='edit.vpage?adminUserName=${adminUser.adminUserName}' class="btn btn-success"><i class="icon-edit icon-white"></i> 编辑</a>
                        </td>
                    </tr>

                </#list>
            </table>
    </div>
</@>