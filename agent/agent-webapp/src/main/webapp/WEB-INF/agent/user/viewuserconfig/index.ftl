<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='协作账户设置' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 协作人员账户设置</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addRolePath" class="btn btn-success" href="addviewuser.vpage">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">用户名</th>
                        <th class="sorting" style="width: 100px;">真实姓名</th>
                        <th class="sorting" style="width: 120px;">用户描述</th>
                        <th class="sorting" style="width: 120px;">协作区域</th>
                        <th class="sorting" style="width: 145px;" >操作 </th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if users??>
                            <#list users as user>
                            <tr class="odd">
                                <td class="center  sorting_1">${user.accountName!}</td>
                                <td class="center  sorting_1">${user.realName!}</td>
                                <td class="center  sorting_1">${user.userComment!}</td>
                                <td class="center  sorting_1">
                                    <#if user.viewRegions??>
                                        <#list user.viewRegions as region>
                                            ${region!} <br/>
                                        </#list>
                                    </#if>
                                </td>
                                <td class="center ">
                                    <a class="btn btn-info" href="addviewuser.vpage?id=${user.id!}">
                                        <i class="icon-edit icon-white"></i>
                                        编辑
                                    </a>
                                    &nbsp;
                                    <a id="delete_view_user_${user.id!}" class="btn btn-danger" href="javascript:void(0);">
                                        <i class="icon-trash icon-white"></i>
                                        关闭
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
        $("a[id^='delete_view_user_']").live('click',function(){
            var id = $(this).attr("id").substring("delete_view_user_".length);
            if(!confirm("确定要关闭此协作账户吗?")){
                return false;
            }

            $.post('delviewuser.vpage',{
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
