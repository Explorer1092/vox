<div id="task-batch-forward" title="任务转发" style="font-size: small; display: none" action="" source="" create_flag="">
    <table width="100%">
        <tr>
            <td style="text-align: left">执行人：</td>
            <td style="text-align: left">
                <select id="task-batch-forward-executor" style="width:180px">
                <#if taskUsers?has_content>
                    <#list taskUsers?keys as user>
                        <option value="${user}">${taskUsers[user]!}</option>
                    </#list>
                </#if>
                </select>
            </td>
        </tr>

        <tr>
            <td style="text-align: left">任务数量：</td>
            <td style="text-align: left"><span id="task-batch-forward-count" /></td>
        </tr>

        <tr>
            <td colspan="2" style="text-align: right">
                <input type="button" value="提交" onclick="batchForwardSubmit()"/>
                <input type="button" value="取消" onclick="closeDialog('task-batch-forward')"/>
            </td>
        </tr>
    </table>
</div>

<script type="text/javascript">
    function batchForwardSubmit(){
        var executor = $("#task-batch-forward-executor").val();
        if (blankString(executor)) {
            alert("请选择执行人！");
            return false;
        }

        var action = $("#task-batch-forward").attr("action");
        var taskIds = $("#task-batch-forward").attr("source");
        var create_flag = $("#task-batch-forward").attr("create_flag");

        $.ajax({
            url: "/crm/task/task_batch_forward.vpage",
            type: "POST",
            async: false,
            data: {
                "executor": executor,
                "taskIds": taskIds,
                "ACTION": action,
                "create_flag":create_flag
            },
            success: function (data) {
                if (!data) {
                    alert("任务转发失败！");
                } else {
                    alert("任务转发成功！");
                    $("#iform").submit();
                    closeDialog('task-batch-forward');
                }
            }
        });
    }
</script>
