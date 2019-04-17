<#import "../../layout_default.ftl" as layout_default />
<#import "../../mizar/pager.ftl" as pager />
<@layout_default.page page_title="老师任务" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <a href="index.vpage">资源列表</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <strong>老师查询</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="query_task.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <input type="hidden" id="action" name="action"/>
                    <ul class="inline">
                        <li>
                            老师ID：<input type="text" id="teacher-id" name="teacherId" value="${queryTeacherId!''}" style="width:100px;" required>
                        </li>
                        <li>
                            <button type="button" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查询
                            </button>
                        </li>
                        <li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="display: none">ID</th>
                        <th style="text-align: center; width: 55px;">任务开始时间</th>
                        <th style="text-align: center; width: 100px;">名称</th>
                        <th style="text-align: center;width: 150px;">任务类型</th>
                        <th style="text-align: center; width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if taskList?? && taskList?has_content>
                            <#list taskList as task>
                            <tr>
                                <td style="display:none">${task.id!'0'}</td>
                                <td style="text-align: center">${task.createAt!'0'}</td>
                                <td>${task.name!''}</td>
                                <td>${task.task!''}</td>
                                <td style="text-align: center;width:120px;">
                                    <#if (task.status!'') == 'ONGOING'>
                                        <button id="finish-btn" class="btn btn-danger">
                                            <i class=""></i> 设为完成
                                        </button>
                                        <#else>
                                        ${task.status!''}
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center;"><strong>No Data Found</strong></td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>


</div>
<script>
    $(function () {
        $("button#filter").click(function(){
            var tId = $("input[name=teacherId]").val();
            if(tId == ""){
                alert("老师ID不能为空!");
                return;
            }

            $("form#query_frm").submit();
        });

        $("button#finish-btn").click(function () {
            var $that = this;
            if (confirm("确认操作?完成后将不能撤回")) {
                var taskId = $($that).parent().siblings("td").eq(0).html();
                $.ajax({
                    url: "finish_task.vpage",
                    data:{"taskId":taskId},
                    success:function (data) {
                        if(data.success) {
                            alert("操作成功!");
                            window.location.reload();
                        }
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>