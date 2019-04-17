<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <legend>学生兑换历史（新版教学用品中心）</legend>
    <table class="table table-bordered">
        <tr>
            <th>产品ID</th>
            <th>产品名称</th>
            <th>兑换方式</th>
            <th>兑换数量</th>
            <th>单价</th>
            <th>花费学豆</th>
            <th>兑换时间</th>
            <th>订单状态</th>
            <th>收货人</th>
            <th>物流公司</th>
            <th>物流单号</th>
            <th>取消订单</th>
            <th>是否取消</th>
        </tr>
        <#if orders?? >
            <#list orders as order >
                <tr>
                    <td>${order.productId!}</td>
                    <td>${order.productName!}</td>
                    <td><#if order.source == 'gift'>抽奖<#else>兑换</#if></td>
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
                        <#if order.disabled == false>
                            <input type="button" value="取消订单"
                                   class="btn btn-primary" onclick="cancelOrder(${order.id!''})">
                        </#if>
                    </td>
                    <td>
                        ${order.disabled?string('已取消','未取消')}
                    </td>
                </tr>
            </#list>
        </#if>
    </table>
</div>

<script type="text/javascript">

    function cancelOrder(data){
        if(window.confirm('你确定要取消订单吗？')){
            var orderId = data;
            $.post('/reward/order/cancelOrder.vpage',{orderId:orderId},function(data){
                alert(data.info);
                location.reload();
            });
        }else{
            //alert("取消");
            return false;
        }
    }
</script>

</@layout_default.page>