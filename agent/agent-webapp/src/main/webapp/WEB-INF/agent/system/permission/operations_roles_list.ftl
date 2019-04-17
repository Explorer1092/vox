<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='功能操作配置' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>功能操作设置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content" style="width:auto;height:50px">
            <div class="control-group span5" >
                <div class="controls" style="width:100%">
                    <a href="role_list.vpage" class="btn btn-faild" style="height:30px;line-height:30px">角色管理</a>
                    <a href="elements_roles_list.vpage" class="btn btn-faild" style="height:30px;line-height:30px">页面元素配置</a>
                    <a href="javascript:;" class="btn btn-success" style="height:30px;line-height:30px">操作权限配置</a>
                </div>
            </div>
            <a href="operation_roles_config.vpage" class="btn btn-success add_new_config" style="height:30px;line-height:30px;float:right">添加</a>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">序号</th>
                        <th class="sorting" style="width: 60px;">操作权限编码</th>
                        <th class="sorting" style="width: 140px;">模块</th>
                        <th class="sorting" style="width: 140px;">子模块</th>
                        <th class="sorting" style="width: 140px;">功能</th>
                        <th class="sorting" style="width: 140px;">备注</th>
                        <th class="sorting" style="width: 140px;">角色列表</th>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list dataList as list>
                        <tr>
                            <td>${list_index!0}</td>
                            <td>${list.operationCode!''}</td>
                            <td>${list.module!''}</td>
                            <td>${list.subModule!''}</td>
                            <td>${list.operationName!''}</td>
                            <td>${list.comment!''}</td>
                            <td>
                                <#if list.roleTypeList?? &&list.roleTypeList?size gt 0>
                                    <#list list.roleTypeList as item>
                                    ${item.roleName!''};
                                    </#list>
                                </#if>
                            </td>
                            <td>
                                <a href="operation_roles_config.vpage?operationId=${list.id!0}">编辑</a>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>