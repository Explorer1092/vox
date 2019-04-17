<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='系统用户设置' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 市场人员账户设置</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent()>
            <div class="pull-right">
                <a id="addRolePath" class="btn btn-success" href="addsysuser.vpage">
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
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">用户名</th>
                        <th class="sorting" style="width: 100px;">真实姓名</th>
                        <th class="sorting" style="width: 120px;">用户描述</th>
                        <th class="sorting" style="width: 100px;">合同开始日期</th>
                        <th class="sorting" style="width: 100px;">合同结束日期</th>
                        <th class="sorting" style="width: 120px;">用户所在群组</th>
                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isRegionManager()>
                        <th class="sorting" style="width: 110px;">市场人员保证金</th>
                        </#if>
                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent()>
                        <th class="sorting" style="width: 145px;" >操作 </th>
                        </#if>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if users??>
                            <#list users as user>
                            <tr class="odd">
                                <td class="center  sorting_1">
                                    <#if user.memberList?has_content>
                                        <a href="index.vpage?userId=${user.id}">
                                            ${user.accountName!}
                                        </a>
                                    <#else>
                                        ${user.accountName!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">${user.realName!}</td>
                                <td class="center  sorting_1">${user.userComment!}</td>
                                <td class="center  sorting_1"><#if user.contractStartDate??>${user.contractStartDate?string('yyyy-MM-dd')}</#if></td>
                                <td class="center  sorting_1"><#if user.contractEndDate??>${user.contractEndDate?string('yyyy-MM-dd')}</#if></td>
                                <td class="center  sorting_1">${user.groupName!}</td>
                                <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isRegionManager()>
                                <td class="center  sorting_1">
                                    <#if user.cashDeposit?has_content>
                                        ${user.cashDeposit!}
                                        <#if user.cashDepositReceived?? && user.cashDepositReceived?string == 'true'>
                                            <span class="label label-info">已到帐</span>
                                        <#else>
                                            <span class="label label-important">未到帐</span>
                                        </#if>
                                    </#if>
                                </td>
                                </#if>
                                <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent()>
                                <td class="center ">
                                    <a class="btn btn-info" href="addsysuser.vpage?id=${user.id!}">
                                        <i class="icon-edit icon-white"></i>
                                        编辑
                                    </a>
                                    &nbsp;
                                    <a id="delete_sys_user_${user.id!}" class="btn btn-danger" href="javascript:void(0);">
                                        <i class="icon-trash icon-white"></i>
                                        关闭
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

</div>


<script type="text/javascript">
    $(function(){
        $("a[id^='delete_sys_user_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_sys_user_".length);
            if(!confirm("确定要删除此条记录?")){
                return false;
            }
            $.post('delsysuser.vpage',{
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
