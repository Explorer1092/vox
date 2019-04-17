<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='平台更新日志' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>平台更新日志</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>

            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right">
                    <a id="add_content" class="btn btn-success" href="update_log_detail.vpage">
                        <i class="icon-plus icon-white"></i>
                        添加
                    </a>
                </div>
            </#if>
        </div>
        <#if errorMessage??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>出错啦！ ${errorMessage!}</strong>
            </div>
        </#if>
        <div class="box-content ">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid">
            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                   id="DataTables_Table_0"
                   aria-describedby="DataTables_Table_0_info">
                <thead>

                <tr class="data-delete-packet">
                    <th class="sorting" style="width: 80px;">日期</th>
                    <th class="sorting" style="width: 90px;">涉及产品</th>
                    <th class="sorting" style="width: 60px;">日志标题</th>
                    <th class="sorting" style="width: 100px;">操作</th>
                </tr>
                </thead>

                <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <#list  updateLog as data>
                    <tr class="odd">
                        <td class="center data-date">${data.createDate?string("yyyy-MM-dd")!""}</td>
                        <td class="center date-type">${data.productName!""}</td>
                        <td class="center date-fileName">${data.updateContent!""}</td>
                        <td class="center data-operation">
                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                            <a class="btn btn-info"
                               href="update_log_detail.vpage?id=${data.id!''}">
                                <i class="icon-edit icon-white"></i>
                                编辑
                            </a>
                            <#if !data.disabled && data.state?? && data.state.stateCode ==1>
                                <a class="btn btn-primary"
                                   href="javascript:publishContent('${data.id!""}')">
                                    <i class="icon-file icon-white"></i>
                                    发布
                                </a>
                            </#if>
                            <#if !data.disabled >
                                <a class="btn btn-danger"
                                   href="javascript:deleteAppContent('${data.id!""}')">
                                    <i class="icon-trash icon-white"></i>
                                    删除
                                </a>
                            </#if>
                        </#if>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
            <input id="update-log-packet" <#if showDelete?? && showDelete ==1>checked</#if> onclick="showDelete()"
                   type="checkbox"/><label for="delete-data-packet"
                                           style="display: inline"><strong>显示已删除的资料包</strong></label>
        </div>
        </div>
    </div>
</div>
    <script type="text/javascript">
        function showDelete() {
            if ($("#update-log-packet").attr("checked")) {
                window.location.href = "/workspace/appupdate/update_log_manage.vpage?&showDelete=1";
            } else {
                window.location.href = "/workspace/appupdate/update_log_manage.vpage";
            }
        }

        function publishContent(id) {
            if (confirm("是否确认发布该日志？")) {
                $.post("publish_content.vpage", {id: id}, function (res) {
                    if (res.success) {
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                })
            }
        }

        function deleteAppContent(id) {
            if (confirm("是否确认删除该条数据？")) {
                $.post("remove_app_content_packet.vpage", {id: id}, function (res) {
                    if (res.success) {
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                })
            }
        }
    </script>
</@layout_default.page>