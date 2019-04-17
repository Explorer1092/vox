<table id="customer_service_record" class="table table-hover table-striped table-bordered">
    <tr id="comment_title">
        <th>用户ID</th>
        <th>添加人</th>
        <th>创建时间</th>
        <th>问题描述</th>
        <th>所做操作</th>
        <th>类型</th>
    </tr>
    <#if customerServiceRecordList?has_content >
    <#list customerServiceRecordList as record >
        <tr>
            <td>${record.userId!""}</td>
            <td>${record.operatorId!""}</td>
            <td>${record.createTime!""}</td>
            <td width="150">${record.operationContent!""}</td>
            <td width="150">${record.comments!""}</td>
            <td>${record.operationType!""}</td>
        </tr>
    </#list>
    </#if>
</table>