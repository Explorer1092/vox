<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <legend>老师兑换历史（新版教学用品中心）</legend>
    <table class="table table-bordered">
        <tr>
            <th>产品ID</th>
            <th>产品名称</th>
            <th>兑换数量</th>
            <th>单价</th>
            <th>花费园丁豆</th>
            <th>兑换时间</th>
            <th>兑换状态</th>
            <th>收货人</th>
            <th>物流公司</th>
            <th>物流单号</th>
            <th>是否取消</th>
        </tr>
        <#if orders?? >
            <#list orders as order >
                <tr>
                    <td>${order.productId!}</td>
                    <td>${order.productName!}</td>
                    <td>${order.quantity!}</td>
                    <td>${order.price!}</td>
                    <td>${order.totalPrice!}</td>
                    <td>${order.createDatetime!}</td>
                    <td>
                        <#if orderStatus?? >
                            <#list orderStatus as t >
                            <#if t.name() == order.status>${t.getDescription()!}</#if>
                        </#list>
                        </#if>
                    </td>
                    <td>${order.receiverName!''} ${order.receiverId!''}</td>
                    <td>${order.companyName!''}</td>
                    <td>${order.logisticNo!''}</td>
                    <td>
                        ${order.disabled?string('已取消','未取消')}
                    </td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
</@layout_default.page>