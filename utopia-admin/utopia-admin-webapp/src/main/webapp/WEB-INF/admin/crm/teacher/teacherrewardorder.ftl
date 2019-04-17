<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <legend>老师兑换历史</legend>
    <table class="table table-bordered">
        <tr>
            <th>订单号</th>
            <th>产品名称</th>
            <th>兑换数量</th>
            <th>单价</th>
            <th>花费园丁豆</th>
            <th>兑换时间</th>
            <th>兑换状态</th>
        </tr>
        <tbody id="tbody">

        </tbody>
    </table>
</div>
<script type="text/javascript">
    $(function(){
        var order = $.parseJSON('${orderinfo!}');
        var html ='';
        var orderinfo = order.orderlist;
        for(var i=orderinfo.length-1;i>=0;i--){
            var rowspan = orderinfo[i].orderDetail.length;
            html +='<tr>';
            html +='<td rowspan="'+rowspan+'">'+orderinfo[i].orderCode+'</td>';
            html +='<td>'+orderinfo[i].orderDetail[0].name+'</td>';
            html +='<td>'+orderinfo[i].orderDetail[0].count+'</td>';
            html +='<td>'+orderinfo[i].orderDetail[0].price+'</td>';
            html +='<td rowspan="'+rowspan+'">'+orderinfo[i].useIntegral+'</td>';
            html +='<td rowspan="'+rowspan+'">'+orderinfo[i].orderTime+'</td>';
            html +='<td rowspan="'+rowspan+'">'+orderinfo[i].status+'</td>';
            html +='</tr>';
            if(rowspan > 1){
                for(var j=1;j<orderinfo[i].orderDetail.length;j++){
                    html +='<tr>';
                    html +='<td>'+orderinfo[i].orderDetail[j].name+'</td>';
                    html +='<td>'+orderinfo[i].orderDetail[j].count+'</td>';
                    html +='<td>'+orderinfo[i].orderDetail[j].price+'</td>';
                    html +='</tr>';
                }
            }
        }
        $('#tbody').html(html);
    });
</script>
</@layout_default.page>