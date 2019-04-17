<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='推荐书籍' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>推荐书籍管理</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>

            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right">
                    <a id="add_content" class="btn btn-success" href="recommend_book_detail.vpage">
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
            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                   id="DataTables_Table_0"
                   aria-describedby="DataTables_Table_0_info">
                <thead>

                <tr>
                    <th class="sorting" style="width: 80px;">日期</th>
                    <th class="sorting" style="width: 80px;">角色</th>
                    <th class="sorting" style="width: 120px;">书籍</th>
                    <th class="sorting" style="width: 100px;">封面</th>
                    <th class="sorting" style="width: 60px;">操作</th>
                </tr>
                </thead>

                <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <#list recommendBooks as data>
                    <tr class="odd">
                        <td class="center data-create-date">${data.createTime?string("yyyy-MM-dd")!""}</td>
                        <td class="center data-role">${data.role!""}</td>
                        <td class="center date-book">${data.bookName!""}</td>
                        <td class="center date-cover"><img src="${data.coverUrl!""}" width="80px" height="80px"></td>
                        <td class="center data-operation">
                        <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
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
                                           style="display: inline"><strong>显示历史推荐的书籍</strong></label>
        </div>
    </div>
</div>
<script type="text/javascript">
    function showDelete() {
        if ($("#update-log-packet").attr("checked")) {
            window.location.href = "/workspace/appupdate/recommend_book_manage.vpage?&showDelete=1";
        } else {
            window.location.href = "/workspace/appupdate/recommend_book_manage.vpage";
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