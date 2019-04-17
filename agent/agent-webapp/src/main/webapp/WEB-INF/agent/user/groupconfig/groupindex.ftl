<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='系统群组设置' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 代理区域设置</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent() >
            <div class="pull-right">
                <a id="addGroup" class="btn btn-success" href="addsysgroup.vpage">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
                &nbsp;
                <a class="btn btn-round" href="javascript:window.history.back();">
                    <i class="icon-chevron-left"></i>
                </a>&nbsp;
            </div>
            </#if>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 150px;">代理区域名称 </th>
                        <th class="sorting_asc" style="width: 200px;"> 代理区域说明 </th>
                        <th class="sorting" style="width: 100px;"> 角色 </th>
                        <th class="sorting" style="width: 100px;">成员列表 </th>
                        <th class="sorting" style="width: 200px;">负责地区列表 </th>
                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent()>
                        <th class="sorting" style="width: 145px;">操作 </th>
                        </#if>
                    </tr>
                    </thead>

                    <tbody>
                        <#if agentGroups??>
                            <#list agentGroups as group>
                            <tr class="odd">
                                <td class="center  sorting_1">
                                    <#if group.subGroupList?has_content>
                                    <a href="index.vpage?groupId=${group.id}">
                                        ${group.groupName!}
                                    </a>
                                    <#else>
                                        ${group.groupName!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">${group.description!}</td>
                                <td class="center  sorting_1"><#if allAgentRoleMap[group.roleId?string]??>${allAgentRoleMap[group.roleId?string].roleName!}</#if></td>
                                <td class="center  sorting_1">
                                    <#if group.agentUserList??>
                                        <#list group.agentUserList as agentUser>
                                    ${agentUser.realName!}
                                    </#list>
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if group.agentGroupRegionList??>
                                        <#list group.agentGroupRegionList as groupRegion>
                                    ${groupRegion.regionName!} <br/>
                                    </#list>
                                    </#if>
                                    <#if group.agentGroupSchoolList??>
                                        <#list group.agentGroupSchoolList as groupSchool>
                                        ${groupSchool.schoolName!} <br/>
                                        </#list>
                                    </#if>
                                </td>
                                <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent()>
                                <td class="center ">
                                    <a class="btn btn-info" href="addsysgroup.vpage?id=${group.id!}">
                                        <i class="icon-edit icon-white"></i>
                                        编辑
                                    </a>
                                    &nbsp;
                                    <a id="delete_group_${group.id!}" class="btn btn-danger" href="javascript:void(0);">
                                        <i class="icon-trash icon-white"></i>
                                        删除
                                    </a>
                                </td>
                                </#if>
                            </tr>
                            </#list>
                        </#if>
                    </tbody></table>
            </div>
        </div>
    </div>
    <!--/span-->

</div>


<script type="text/javascript">
    $(function(){
        $("a[id^='delete_group_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_group_".length);
            if(!confirm("将要删除此代理区域已经其下所有的代理区域，确定要删除吗?")){
                return false;
            }
            $.post('delsysgroup.vpage',{
                id:id
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
