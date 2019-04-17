<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=4>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 我的订单</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable" id="dt_my_orders">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">订单日期</th>
                        <th class="sorting" style="width: 60px;">订单号</th>
                        <th class="sorting" style="width: 80px;">订单类型</th>
                        <th class="sorting" style="width: 345px;">购买商品</th>
                        <th class="sorting" style="width: 80px;">订单金额</th>
                        <th class="sorting" style="width: 200px;">补充信息</th>
                        <th class="sorting" style="width: 60px;">订单状态</th>
                        <th class="sorting" style="width: 120px;">物流信息</th>
                        <th class="sorting" style="width: 80px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if orders??>
                            <#list orders as order>
                            <tr class="odd">
                                <td class="center  sorting_1">${order.createDate!}</td>
                                <td class="center  sorting_1"><a id="load_order_his_${order.orderId!}" href="javascript:void(0)">${order.orderId!}</a></td>
                                <td class="center  sorting_1">${order.orderTypeStr!}</td>
                                <td class="center  sorting_1">${order.orderProducts!}</td>
                                <td class="center  sorting_1"><#if order.orderAmount??>${order.orderAmount?string(",##0.##")}</#if></td>
                                <td class="center  sorting_1">${order.orderNotes!}</td>
                                <td class="center  sorting_1">${order.orderStatusStr!}</td>
                                <td class="center  sorting_1">${order.logisticsInfo!}</td>
                                <td class="center  sorting_1">
                                    <#if order.orderTypeStr == '材料购买'>
                                    <#else>
                                        <#if order.orderStatus?? && (order.orderStatus == 1 || order.orderStatus == 2 || order.orderStatus == 4 || order.orderStatus == 10)><a id="cancel_order_${order.orderId!}" href="javascript:void(0)">取消订单</a></#if>&nbsp;&nbsp;
                                        <#if order.orderStatus?? && (order.orderStatus == 1 || order.orderStatus == 2 || order.orderStatus == 4 || order.orderStatus == 10)><a id="edit_order_${order.orderId!}" href="javascript:void(0)">修改订单</a></#if>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="orderHistory" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">订单处理流程</h4>
            </div>
            <div class="box-content">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th>处理人</th>
                        <th>结果</th>
                        <th>处理备注</th>
                        <th>时间</th>
                    </tr>
                    </thead>
                    <tbody id="orderHistoryBody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="task_id" value="" data-type=""/>
<script type="text/javascript">
    $(function(){

        $("a[id^='load_order_his_']").live('click',function(){
            $('#orderHistoryBody').html('');
            var id = $(this).attr("id").substring("load_order_his_".length);
            $.post('/task/todolist/loadorderhistories.vpage',{
                orderId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    for(var i=0; i<data.value.length; i++){
                        var str = "<tr><td>"+data.value[i].processor+"</td>";
                        str += "<td class=\"center\">"+data.value[i].result+"</td>";
                        str += "<td class=\"center\">"+data.value[i].notes+"</td>";
                        str += "<td class=\"center\">"+data.value[i].time+"</td>";
                        $('#orderHistoryBody').append(str);
                    }
                    $('#orderHistory').modal('show');
                }
            });
        });

        $('#dt_my_orders').dataTable({
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
            "sPaginationType": "bootstrap",
            "aaSorting": [[0,'desc']],
            "oLanguage": {
                "sProcessing": "正在加载中......",
                "sLengthMenu": "每页显示 _MENU_ 条记录",
                "sZeroRecords": "对不起，查询不到相关数据！",
                "sEmptyTable": "表中无数据存在！",
                "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
                "sInfoEmpty": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
                "sInfoFiltered": "数据表中共为 _MAX_ 条记录",
                "sSearch": "搜索",
                "oPaginate": {
                    "sFirst": "首页",
                    "sPrevious": "上一页",
                    "sNext": "下一页",
                    "sLast": "末页"
                }
            }
        });


        $("a[id^='cancel_order_']").live('click',function(){
            if (!confirm("确定要取消此订单吗?")) {
                return ;
            }
            var id = $(this).attr("id").substring("cancel_order_".length);
            $.post('cancel.vpage',{
                orderId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    alert("订单取消成功！");
                    window.location.reload();
                }
            });
        });

        $("a[id^='edit_order_']").live('click',function(){
            if (!confirm("确定要修改此订单吗?")) {
                return ;
            }
            var id = $(this).attr("id").substring("edit_order_".length);
            $.post('edit.vpage',{
                orderId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.href="/workspace/purchase/shopping_cart.vpage";
                }
            });
        });

    });
</script>
</@layout_default.page>
