<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='角色管理' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>添加页面元素设置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content" style="width:auto;height:50px">
            <div class="control-group span5" >
                <div class="controls">
                    <a href="javascript:;" class="btn btn-faild" style="height:30px;line-height:30px">角色管理</a>
                    <a href="module_operation_roles.vpage" class="btn btn-success" style="height:30px;line-height:30px">权限管理</a>
                </div>
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">角色名称</th>
                        <th class="sorting" style="width: 60px;">说明</th>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list roleList as list>
                        <tr>
                            <td>${list.roleName}</td>
                            <td>${list.desc}</td>
                            <td>
                                <a href="role_paths_config.vpage?roleId=${list.id!0}">编辑权限</a>
                                <a href="add_page_element_page.vpage?roleId=${list.id!0}">页面元素权限配置</a>
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