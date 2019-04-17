<div id="task-new" title="新建任务" style="font-size: small; display: none" users="" action="" source="">
    <table width="100%">
        <tr>
            <td style="text-align: left">执行人：</td>
            <td style="text-align: left">
                <select id="task-new-executor" style="width:180px">
                <#if taskUsers?has_content>
                    <#list taskUsers?keys as user>
                        <option value="${user}">${taskUsers[user]!}</option>
                    </#list>
                </#if>
                </select>
            </td>
        </tr>
        <tr>
            <td style="text-align: left">任务分类：</td>
            <td style="text-align: left">
                <select id="task-new-type" style="width:180px">
                <#if taskTypes?has_content>
                    <#list taskTypes as taskType>
                        <option value="${taskType.name()}">${taskType.name()!}</option>
                    </#list>
                </#if>
                </select>
            </td>
        </tr>
        <tr>
            <td style="text-align: left">截止时间：</td>
            <td style="text-align: left"><input id="task-new-endTime" type="text" style="width: 120px" class="date"/></td>
        </tr>
        <tr>
            <td style="text-align: left">任务主题：</td>
            <td style="text-align: left"><input id="task-new-title" type="text" style="width: 300px"/></td>
        </tr>
        <tr>
            <td style="text-align: left">任务内容：</td>
            <td style="text-align: left"><textarea id="task-new-content" style="height: 120px; width: 300px" placeholder="任务详情描述（目标、方式等）"></textarea></td>
        </tr>
        <tr>
            <td colspan="2" style="text-align: right">
                <input type="button" value="提交" onclick="task.save()"/>
                <input type="button" value="取消" onclick="closeDialog('task-new')"/>
            </td>
        </tr>
    </table>
</div>
