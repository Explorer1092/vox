<div id="task-detail" title="任务详情" style="font-size: small; display: none" task-id="" user-id="" admin-user="${(adminUser.adminUserName)!''}" task-type="">
    <table id="task-show" width="100%" style="display: none">
        <tr>
            <td><strong>创建人：</strong><span id="task-show-creator"></span></td>
            <td><strong>执行人：</strong><span id="task-show-executor"></span></td>
            <td><strong>任务分类：</strong><span id="task-show-type"></span></td>
            <td><strong>任务状态：</strong><span id="task-show-status"></span></td>
        </tr>
        <tr>
            <td><strong>创建时间：</strong><span id="task-show-createTime"></span></td>
            <td><strong>截止时间：</strong><span id="task-show-endTime"></span></td>
            <td colspan="2">
                <strong>用户：</strong>
                <a id="task-show-user" style="color: blue" target="_blank"></a> (<span id="task-show-userId"></span>)
            </td>
        </tr>
        <tr>
            <td colspan="4"><strong>任务主题：</strong><span id="task-show-title"></span></td>
        </tr>
        <tr>
            <td colspan="4"><strong>任务内容：</strong><span id="task-show-content"></span></td>
        </tr>
        <tr>
            <td colspan="4" style="text-align: right">
                <input id="edit-task" type="button" value="任务修改" onclick="task.edit()" style="display: none"/>
                <input type="button" value="关闭" onclick="closeDialog('task-detail')"/>
            </td>
        </tr>
    </table>
    <table id="task-edit" width="100%" style="display: none">
        <tr>
            <td><strong>执行人：</strong><span id="task-edit-executor"></span></td>
            <td><strong>任务分类：</strong><span id="task-edit-type"></span></td>
            <td><strong>截止时间：</strong><input id="task-edit-endTime" type="text" style="width: 120px" class="date"/></td>
            <td>
                <strong>任务状态：</strong>
                <select id="task-edit-status">
                    <option value="FOLLOWING">待跟进</option>
                    <option value="FINISHED">已完成</option>
                </select>
            </td>
        </tr>
        <tr>
            <td colspan="4"><strong>任务主题：</strong><span id="task-edit-title"></span></td>
        </tr>
        <tr>
            <td colspan="4">
                <strong>任务内容：</strong>
                <textarea id="task-edit-content" style="height: 100px; width: 600px" placeholder="任务详情描述（目标、方式等）"></textarea>
            </td>
        </tr>
        <tr>
            <td colspan="4" style="text-align: right">
                <input type="button" value="确认修改" onclick="task.update()"/>
                <input type="button" value="取消" onclick="closeDialog('task-detail')"/>
            </td>
        </tr>
    </table>

    <hr style="height: 1px; border: none; border-top: 2px solid black;"/>

    <div id="new-record" style="display: none">
        <a href="javascript:task.newRecord();" style="color: blue">添加新的工作记录</a>
        <br><br>
    </div>

    <div id="record-stubs">
        <ul id="task-stubs"></ul>
        <a id="stubs-show" href="javascript:moreStubs();" style="color: blue; display: none">全部>></a>
        <ul id="stubs-more" style="display: none"></ul>
    </div>
    <br>
</div>

<script type="text/javascript">
    function moreStubs() {
        $("#stubs-show").hide();
        $("#stubs-more").show("fast");
    }
</script>
