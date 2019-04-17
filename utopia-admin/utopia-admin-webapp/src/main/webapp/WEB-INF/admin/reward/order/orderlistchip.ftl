<#if orderPage??>
<ul class="pager">
<#if orderPage?? && (orderPage.hasPrevious())>
    <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
<#else>
    <li class="disabled"><a href="#">上一页</a></li>
</#if>
<#if orderPage?? && (orderPage.hasNext())>
    <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
<#else>
    <li class="disabled"><a href="#">下一页</a></li>
</#if>
    <li>当前第 ${pageNumber!} 页 |</li>
    <li>共 ${orderPage.totalPages!} 页</li>
</ul>
<div id="data_table_journal">
    <table class="table table-striped table-bordered">
        <tr>
            <td>用户ID</td>
            <td>用户姓名</td>
            <td>奖品ID</td>
            <td>奖品名称</td>
            <td>款式</td>
            <td>数量</td>
            <td>价格</td>
            <td>总价</td>
            <td>折扣</td>
            <td>下单时间</td>
            <td>状态</td>
            <td>状态变更原因</td>
            <td>快递单ID</td>
        </tr>
    <#if orderPage.content?? >
        <#list orderPage.content as order >
            <tr>
                <td>${order.buyerId!}</td>
                <td>${order.buyerName!}</td>
                <td>${order.productId!}</td>
                <td>${order.productName!}</td>
                <td>${order.skuName!}</td>
                <td>${order.quantity!}</td>
                <td>${order.price!}</td>
                <td>${order.totalPrice!}</td>
                <td>${order.discount!}</td>
                <td>${order.createDatetime!}</td>
                <td>
                    <#if order.status == 'PREPARE'>
                        配货中
                    <#elseif order.status == 'SUBMIT'>
                        待审核
                    <#elseif order.status == 'EXCEPTION'>
                        用户信息异常
                    <#elseif order.status == 'DELIVER'>
                        已发货
                    </#if>
                </td>
                <td>${order.reason!''}</td>
                <td>${order.logisticsId!''}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>
<ul class="pager">
<#if (orderPage.hasPrevious())>
    <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
<#else>
    <li class="disabled"><a href="#">上一页</a></li>
</#if>
<#if (orderPage.hasNext())>
    <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
<#else>
    <li class="disabled"><a href="#">下一页</a></li>
</#if>
    <li>当前第 ${pageNumber!} 页 |</li>
    <li>共 ${orderPage.totalPages!} 页</li>
</ul>

<script type="text/javascript">
    function pagePost(pageNumber){
        $('#order_list_chip').load('getorderlist.vpage',
                {   userId : $('#userId').val(),
                    status: $("#status").val(),
                    startDate: $("#startDate").val(),
                    endDate: $("#endDate").val(),
                    pageNumber : pageNumber
                }
        );
    }

</script>
<#else >
    错误：${error!}
</#if>