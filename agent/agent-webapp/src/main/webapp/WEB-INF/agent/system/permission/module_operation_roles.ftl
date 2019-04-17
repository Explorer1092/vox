<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='角色管理' page_num=6>
<style>
    .row-fluid .span12 table tr td {
        padding:0;
    }
    .row-fluid .span12 table tr td div {
        height:20px;line-height: 20px;border-bottom: 1px solid #dddddd;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>权限管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content" style="width:auto;height:50px">
            <div class="control-group span5" >
                <div class="controls">
                    <a href="role_list.vpage" class="btn btn-success" style="height:30px;line-height:30px">角色管理</a>
                    <a href="javascript:;" class="btn btn-faild" style="height:30px;line-height:30px">权限管理</a>
                    <a href="elements_roles_list.vpage" class="btn btn-success" style="height:30px;line-height:30px">页面元素配置</a>
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
                        <th class="sorting" style="width: 60px;">序号</th>
                        <th class="sorting" style="width: 60px;">模块</th>
                        <th class="sorting" style="width: 140px;">子模块</th>
                        <th class="sorting" style="width: 140px;">功能</th>
                        <th class="sorting" style="width: 140px;">角色列表</th>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if moduleAndOperations?has_content>
                            <#list moduleAndOperations?keys as key>
                                <#assign module = moduleAndOperations[key]>
                            <tr>
                                <td style="line-height:100%">${key_index!''}</td>
                                <td>${key!''}</td>
                                <td>
                                    <#if module?has_content && module?size gt 0>
                                        <#list module?keys as key1>
                                            <div>
                                            ${key1!''}
                                            </div>
                                        </#list>
                                    </#if>
                                </td>
                                <td>
                                    <#if module?has_content && module?size gt 0>
                                        <#list module?keys as key1>
                                            <#assign item = module[key1]>
                                            <#if item?has_content && item?size gt 0>
                                                <#list item as list1>
                                                    <div>
                                                        <#if list1.operationDesc?has_content>
                                                            ${list1.operationDesc!''}
                                                        <#else>
                                                        ${list1.path!''}
                                                        </#if>
                                                    </div>
                                                </#list>
                                            </#if>
                                        </#list>
                                    </#if>
                                </td>
                                <td>
                                    <#if module?has_content && module?size gt 0>
                                        <#list module?keys as key1>
                                            <#assign item = module[key1]>
                                            <#if item?has_content && item?size gt 0>
                                                <#list item as list1>
                                                    <div>
                                                        <#if  list1.roleTypeList?has_content && list1.roleTypeList?size gt 0>
                                                        <#list list1.roleTypeList as list2>
                                                            ${list2.roleName!''}
                                                        </#list>
                                                        </#if>
                                                    </div>
                                                </#list>
                                            </#if>
                                        </#list>
                                    </#if>
                                </td>
                                <td>
                                    <#if module?has_content && module?size gt 0>
                                        <#list module?keys as key1>
                                            <#assign item = module[key1]>
                                            <#if item?has_content && item?size gt 0>
                                                <#list item as list1>
                                                    <div style="height:20px;line-height:20px;border-bottom: 1px solid #dddddd;">
                                                        <a class="change_path" href="path_roles_config.vpage?path=${list1.path!''}">编辑</a>
                                                    </div>
                                                </#list>
                                            </#if>
                                        </#list>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>

