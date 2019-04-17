<div>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>奖品ID</th>
            <th>奖品名称</th>
            <th>单品名称</th>
            <th>总兑换数量</th>
            <th>配货中数量</th>
            <th>用户信息异常数量</th>
            <th>已发货数量</th>
        </tr>
    <#if results?has_content>
        <#list results as result>
            <tr>
                <th>${result.pid!}</th>
                <th>${result.pname!}</th>
                <td>${result.sname!}</td>
                <td>${(result.deliverCount!0)?int + (result.exceptionCount!0)?int + (result.prepareCount!0)?int}</td>
                <td>${result.prepareCount!0}</td>
                <td>${result.exceptionCount!0}</td>
                <td>${result.deliverCount!0}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>