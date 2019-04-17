<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='最新活动' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>最新活动管理</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>

            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right">
                    <a id="add_content" class="btn btn-success" href="activity_presentation_detail.vpage">
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
        <div class="box-content">
            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                   id="DataTables_Table_0"
                   aria-describedby="DataTables_Table_0_info">
                <thead>

                <tr>
                    <th class="sorting" style="width: 80px;">活动名称</th>
                    <th class="sorting" style="width: 90px;">开始日期</th>
                    <th class="sorting" style="width: 90px;">结束日期</th>
                    <th class="sorting" style="width: 60px;">活动范围</th>
                    <th class="sorting" style="width: 100px;">活动入口</th>
                    <th class="sorting" style="width: 100px;">活动城市</th>
                    <th class="sorting" style="width: 100px;">操作</th>
                </tr>
                </thead>

                <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <#list activity as data>
                    <tr class="odd">
                        <td class="center data-activity-name">${data.activityName!""}</td>
                        <td class="center date-start-date">${data.startDate?string("yyyy-MM-dd")!""}</td>
                        <td class="center date-end-date">${data.endDate?string("yyyy-MM-dd")!""}</td>
                        <td class="center date-activity-scope">${data.activityScope!""}</td>
                        <td class="center date-activity-entrance">${data.activityEntrance!""}</td>
                        <td class="center date-activity-city">${data.activityCity!""}</td>
                        <td class="center data-operation">
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                <a class="btn btn-info" href="activity_presentation_detail.vpage?id=${data.id!""}">
                                    <i class="icon-edit icon-white"></i>
                                    编辑
                                </a>
                                <#if data.endDate?date gte .now?date && !data.disabled && data.state?? && data.state.stateCode ==1>
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
            <input id="delete-data-packet" <#if showDelete?? && showDelete ==1>checked</#if> onclick="showDelete()"
                   type="checkbox"/><label for="delete-data-packet"
                                           style="display: inline"><strong>显示已过期或已删除的活动</strong></label>
        </div>
    </div>
</div>

<script type="text/javascript">
    function showDelete() {
        if ($("#delete-data-packet").attr("checked")) {
            window.location.href = "/workspace/appupdate/marketing_activity_manage.vpage?showDelete=1";
        } else {
            window.location.href = "/workspace/appupdate/marketing_activity_manage.vpage";
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