<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='绩效考核管理' page_num=6>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 绩效考核指标(KPI)一览 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isAdmin()>
            <div class="pull-right">
                <a id="addKpi" class="btn btn-success" href="addkpi.vpage">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>&nbsp;
            </div>
            </#if>
        </div>

        <div class="box-content">
        <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">

        <table class="table table-striped table-bordered bootstrap-datatable dataTable">
            <thead>
                <tr>
                    <th class="sorting" style="width: 250px;">KPI名称</th>
                    <th class="sorting" style="width: 350px;"> KPI说明 </th>
                    <th class="sorting" style="width: 150px;"> 适用对象角色 </th>
                    <th class="sorting" style="width: 145px;" >操作 </th>
                </tr>
            </thead>

            <tbody>
                <#if kpiDefList??>
                    <#list kpiDefList as kpiDef>
                    <tr class="odd">
                        <td class="center  sorting_1">${kpiDef.kpiName!}</td>
                        <td class="center  sorting_1">${kpiDef.kpiDesc!}</td>
                        <td class="center  sorting_1">${allAgentRoleMap[kpiDef.kpiRole?string].roleName!}</td>
                        <td class="center ">
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                            <a class="btn btn-info" href="addkpi.vpage?kpiId=${kpiDef.id!}">
                                <i class="icon-edit icon-white"></i>
                                编辑
                            </a>
                            &nbsp;
                            <a id="delete_kpi_def_${kpiDef.id!}" class="btn btn-danger" href="javascript:void(0);">
                                <i class="icon-trash icon-white"></i>
                                删除
                            </a>
                            <#else>
                            <a class="btn btn-info" href="addkpi.vpage?kpiId=${kpiDef.id!}">
                                <i class="icon-search icon-white"></i>
                                查看
                            </a>
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

<script type="text/javascript">
    $(function(){
        $("a[id^='delete_kpi_def_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_kpi_def_".length);
            if(!confirm("确定要删除此绩效考核指标吗?")){
                return false;
            }

            $.post('deletedef.vpage',{
                kpiId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
</@layout_default.page>
