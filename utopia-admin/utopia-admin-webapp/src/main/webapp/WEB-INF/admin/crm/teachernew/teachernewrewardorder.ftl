<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="background: none;">
<div style="margin-left: 2em">
    <div style="margin-top: 2em">
    <legend>老师兑换历史（新版奖品中心）</legend>
    <table class="table table-bordered">
        <tr>
            <th>产品ID</th>
            <th>产品名称</th>
            <th>兑换数量</th>
            <th>单价</th>
            <th>花费园丁豆</th>
            <th>兑换时间</th>
            <th>订单状态</th>
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
</div>
</body>
</html>