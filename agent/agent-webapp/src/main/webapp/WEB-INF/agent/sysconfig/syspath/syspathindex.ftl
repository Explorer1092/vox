<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='系统权限设置' page_num=6>
<div class="row-fluid sortable ui-sortable">
<div class="box span12">
<div class="box-header well" data-original-title="">
    <h2><i class="icon-user"></i> 系统功能菜单与权限一览</h2>

    <div class="box-icon">
        <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
        <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
    </div>
    <div class="pull-right">
        <a id="addRolePath" class="btn btn-success" href="addsyspath.vpage">
            <i class="icon-plus icon-white"></i>
            添加
        </a>
        &nbsp;
    </div>
</div>

<div class="box-content">
<div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">

<table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" id="DataTables_Table_0"
       aria-describedby="DataTables_Table_0_info">
<thead>

<tr>
    <th class="sorting" style="width: 60px;">一级菜单名称</th>
    <th class="sorting" style="width: 100px;"> 二级菜单名称</th>
    <th class="sorting" style="width: 140px;">功能描述</th>
    <th class="sorting" style="width: 140px;">角色列表 </th>
    <th class="sorting" style="width: 60px;" >操作 </th>
</tr>
</thead>

<tbody role="alert" aria-live="polite" aria-relevant="all">
    <#if sysPathList??>
        <#list sysPathList as sysPath>
        <tr class="odd">
            <td class="center  sorting_1">${sysPath.appName!}</td>
            <td class="center  sorting_1">${sysPath.pathName!}</td>
            <td class="center  sorting_1">${sysPath.description!}</td>
            <td class="center  sorting_1">
                <#if sysPath.authRoleList??>
                    <#list sysPath.authRoleList as role>
                        <#if (role.roleId)?? >
                            ${(allAgentRoleMap[role.roleId?string].roleName)!''}
                        </#if>
                    </#list>
                </#if>
            </td>
            <td class="center ">
                <a class="btn btn-info" href="addsyspath.vpage?id=${sysPath.id!}">
                    <i class="icon-edit icon-white"></i>
                    编辑
                </a>
                <a id="delete_path_role_${sysPath.id!}" class="btn btn-danger" href="javascript:void(0);">
                    <i class="icon-trash icon-white"></i>
                    删除
                </a>
            </td>
        </tr>
        </#list>
    </#if>
</tbody></table>
</div>
</div>
</div>

</div>


<script type="text/javascript">
    $(function(){
        $("a[id^='delete_path_role_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_path_role_".length);
            if(!confirm("确定要删除此条记录?")){
                return false;
            }
            $.post('delsyspath.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });
    });
</script>
</@layout_default.page>
