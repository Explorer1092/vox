<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=2>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 已处理任务</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable" id="dt_task_donelist">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">订单号</th>
                        <th class="sorting" style="width: 50px;">下单人</th>
                        <th class="sorting" style="width: 80px;">订单类型</th>
                        <th class="sorting" style="width: 245px;">购买商品</th>
                        <th class="sorting" style="width: 80px;">订单金额</th>
                        <th class="sorting" style="width: 150px;">补充信息</th>
                        <th class="sorting" style="width: 110px;">收货信息</th>
                        <th class="sorting" style="width: 80px;">可用余额</th>
                        <th class="sorting" style="width: 120px;">处理时间</th>
                        <th class="sorting" style="width: 60px;">处理结果</th>
                        <th class="sorting" style="width: 100px;">备注</th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if doneList??>
                            <#list doneList as orderHistory>
                                <#if orderHistory.orderTypeStr == '材料购买' || orderHistory.orderTypeStr == '保证金收款'><#else>
                                    <tr class="odd">
                                        <td class="center  sorting_1"><a id="load_order_his_${orderHistory.orderId!}" href="javascript:void(0)">${orderHistory.orderId!}</a></td>
                                        <td class="center  sorting_1">${orderHistory.creator!}</td>
                                        <td class="center  sorting_1">${orderHistory.orderTypeStr!}</td>
                                        <td class="center  sorting_1">${orderHistory.orderProducts!}</td>
                                        <td class="center  sorting_1"><#if orderHistory.orderAmount??>${orderHistory.orderAmount?string(",##0.##")}</#if></td>
                                        <td class="center  sorting_1">${orderHistory.orderNotes!}</td>
                                        <td class="center  sorting_1">
                                            收货人:${orderHistory.consignee!}<br/>
                                            收货人电话:${orderHistory.mobile!}<br/>
                                            收货地址:${orderHistory.province!""}${orderHistory.city!""}${orderHistory.county!""}${orderHistory.address!}
                                        </td>
                                        <td class="center  sorting_1"><#if orderHistory.usableCashAmount??>${orderHistory.usableCashAmount?string(",##0.##")}</#if></td>
                                        <td class="center  sorting_1">${orderHistory.createDatetime?string('yyyy-MM-dd HH:mm:ss')!}</td>
                                        <td class="center  sorting_1">
                                            <#if orderHistory.result == 0>
                                                <i class="icon-ok icon-white"></i>
                                                通过
                                            <#else>
                                                <i class="icon-remove icon-white"></i>
                                                拒绝
                                            </#if>
                                        </td>
                                        <td class="center  sorting_1">${orderHistory.processNote!}</td>
                                    </tr>
                                </#if>
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
                <h4 class="modal-title">订单流程描述</h4>
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
            $.post('../todolist/loadorderhistories.vpage',{
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

        $('#dt_task_donelist').dataTable({
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
            "sPaginationType": "bootstrap",
            "aaSorting": [[5,'desc']],
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

    });
</script>
</@layout_default.page>
