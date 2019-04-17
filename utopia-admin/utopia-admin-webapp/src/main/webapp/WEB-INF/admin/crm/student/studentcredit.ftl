<table class="table table-hover table-striped table-bordered">
    <tr>
        <th>学生id</th>
        <th>增加学分</th>
        <th>创建前总学分</th>
        <th>创建可用学分</th>
        <th>创建后总学分</th>
        <th>创建后用学分</th>
        <th>修改时间</th>
        <th>备注</th>
    </tr>
    <tbody>
            <#if creditHistories?has_content >
            <#list creditHistories as creditHistory >
            <tr>
                <td>${creditHistory.userId!""}</td>
                <td>${creditHistory.amount!""}</td>
                <td>${creditHistory.totalCreditBefore!""}</td>
                <td>${creditHistory.usableCreditBefore!""}</td>
                <td>${creditHistory.totalCreditAfter!""}</td>
                <td>${creditHistory.usableCreditAfter!""}</td>
                <td>${creditHistory.updateDatetime!""}</td>
                <td>${creditHistory.comment!""}</td>
            </tr>
            </#list>
            </#if>
    </tbody>
</table>