<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='任务管理' page_num=2>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 任务管理 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="/task/manage/edit_task.vpage?createStart=${createStart!}&createEnd=${createEnd!}" class="btn btn-success">
                    <i class="icon-plus icon-white"></i>新建任务
                </a>&nbsp;&nbsp;
            </div>
        </div>
        <div class="box-content">
            <form id="task-form" action="/task/manage/creater_tasks.vpage" method="get" data-ajax="false">
                创建时间&nbsp;&nbsp;
                <input id="createStart" name="createStart" type="text" class="date" style="width: 100px" value="${createStart!}">
                &nbsp;--&nbsp;
                <input id="createEnd" name="createEnd" type="text" class="date" style="width: 100px" value="${createEnd!}">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <a href="javascript:iSearch();" class="ui-btn ui-btn-inline">查询</a>
            </form>
            <#setting datetime_format="yyyy-MM-dd"/>
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">任务分类</th>
                        <th class="sorting" style="width: 100px;">任务主题</th>
                        <th class="sorting" style="width: 260px;">任务说明</th>
                        <th class="sorting" style="width: 60px;">创建时间</th>
                        <th class="sorting" style="width: 60px;">截止时间</th>
                        <th class="sorting" style="width: 60px;">任务总数</th>
                        <th class="sorting" style="width: 60px;">任务跟进数</th>
                        <th class="sorting" style="width: 60px;">需要外呼数</th>
                        <th class="sorting" style="width: 60px;">任务跟进率</th>
                        <th class="sorting" style="width: 78px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if createrTasks?has_content>
                            <#list createrTasks as task>
                                <#assign base = task.totalCount!1>
                                <#if base < 1>
                                    <#assign base = 1>
                                </#if>
                            <tr class="odd">
                                <td class="center sorting_1">${(task.category.value)!}</td>
                                <td class="center sorting_1">${task.title!}</td>
                                <td class="center sorting_1">${task.content!}</td>
                                <td class="center sorting_1">${task.createTime!}</td>
                                <td class="center sorting_1">${task.endTime!}</td>
                                <td class="center sorting_1">${task.totalCount!}</td>
                                <td class="center sorting_1">${task.finishCount!}</td>
                                <td class="center sorting_1">${task.needOutboundCount!}</td>
                                <td class="center sorting_1">${((task.finishCount!0)*100/base)?string("0.##")}%</td>
                                <td class="center sorting_1">
                                    <a href="/task/manage/export_task.vpage?id=${task.id!}&title=${task.title!}" class="btn btn-info">下载excel</a>
                                    <a id="delete_${task.id!}" class="btn btn-danger" href="javascript:void(0);">
                                        <i class="icon-trash icon-white"></i>
                                        删 除
                                    </a>
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
    $(function () {
        dater.render();
        <#if errorMessage??>
            alert("${errorMessage}");
        </#if>
    });
    function iSearch() {
        var createStart = $("#createStart").val();
        var createEnd = $("#createEnd").val();
        if (blankString(createStart) || blankString(createEnd) || createEnd < createStart) {
            alert("请选择查询起止日期,且结束日期大于起始时间！");
            return
        }
        $("#task-form").submit();
    }

    $(function() {
        $("a[id^='delete_").live('click', function() {
            if (confirm("是否确认删除该任务？")) {
                $.post('delete_task.vpage', {
                    taskId : $(this).attr("id").substring("delete_".length)
                }, function(data) {
                    if (data.success) {
                        alert("删除成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>
