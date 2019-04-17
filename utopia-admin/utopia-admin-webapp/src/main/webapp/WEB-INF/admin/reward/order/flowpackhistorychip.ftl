<div>
    <#if results?has_content>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>订单ID</th>
            <th>用户ID</th>
            <th>运营商</th>
            <th>流量包(MB)</th>
            <th>状态</th>
            <th>时间线</th>
        </tr>
        <#list results as result>
            <tr>
                <td>${result.orderNo!}</td>
                <td>${result.userId!}</td>
                <td>
                    <#if (result.vendor)?? && result.vendor == 'AXJ'> 安信捷
                    <#elseif (result.vendor)?? && result.vendor == 'JJLL'> 加加流量
                    <#else> --
                    </#if>
                </td>
                <td>${result.flowSize!}</td>
                <td>
                    <#if (result.status)?? && result.status == 1> 提交成功
                    <#elseif (result.status)?? && result.status == 2> 充值成功
                    <#elseif (result.status)?? && result.status == 9> 充值失败<br/>错误码:${(result.responseCode)!'--'}<br/>错误信息:${(result.responseMsg)!'--'}
                    <#else> 未知状态
                    </#if>
                </td>
                <td>
                    创建时间：${(result.createDatetime!)?string("yyyy-MM-dd HH:mm:ss")}<br/>
                    更新时间：${(result.updateDatetime!)?string("yyyy-MM-dd HH:mm:ss")}
                </td>
            </tr>
        </#list>
    </table>
    <#else>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>该用户没有充值记录</strong>
        </div>
    </#if>

</div>