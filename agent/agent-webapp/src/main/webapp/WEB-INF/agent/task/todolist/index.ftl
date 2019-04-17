<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=2>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 待处理任务</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a class="btn btn-danger JS-BulkRefundBtn" href="javascript:void(0);">批量退款</a>
                <a href="exportOrderInfo.vpage" class="btn btn-primary">导出订单表</a>
            </div>
        </div>
        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <div style="position: relative;">
                    <div style="position: absolute; top: 40px; left: 9px;"><input class="JS-selectAllBox" type="checkbox" data-ids=""/></div>
                </div>
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" id="DataTables_Table_0">
                    <thead>
                    <tr role="row">
                        <th class="sorting_disabled" style="width: 50px;"></th>
                        <th class="sorting" style="width: 50px;">订单号</th>
                        <th class="sorting" style="width: 50px;">下单人</th>
                        <th class="sorting" style="width: 80px;">订单类型</th>
                        <th class="sorting" style="width: 220px;">购买商品</th>
                        <th class="sorting" style="width: 70px;">订单金额</th>
                        <th class="sorting" style="width: 170px;">备注</th>
                        <th class="sorting" style="width: 110px;">订单状态</th>
                        <th class="sorting" style="width: 110px;">收货信息</th>
                        <th class="sorting" style="width: 70px;">可用余额</th>
                        <th class="sorting" style="width: 70px;">申请时间</th>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if assignments??>
                            <#list assignments as assignment>
                                <#if assignment.orderTypeStr == '材料购买' || assignment.orderTypeStr == '保证金收款'><#else>
                                    <tr class="odd">
                                        <td class="sorting" style="width: 50px;"><input type="checkbox" class="JS-selectSignBox" data-id="${assignment.id!}"/> </td>

                                        <td class="center  sorting_1">
                                            <#if (assignment.orderType != 5 && assignment.orderType != 6)>
                                                <a id="load_order_his_${assignment.orderId!}"  href="javascript:void(0);">${assignment.orderId!}</a>
                                            <#else>
                                                ${assignment.orderId!}
                                            </#if>
                                        </td>
                                        <td class="center  sorting_1">${assignment.creator!}</td>
                                        <td class="center  sorting_1">${assignment.orderTypeStr!}</td>
                                        <td class="center  sorting_1">${assignment.orderProducts!}</td>
                                        <td class="center  sorting_1"><#if assignment.orderAmount??>${assignment.orderAmount?string(",##0.##")}</#if></td>
                                        <td class="center  sorting_1" id="order_note_${assignment.orderId!}">${assignment.orderNotes!}</td>
                                        <td class="center  sorting_1">${assignment.orderStatusStr!}</td>
                                        <td class="center  sorting_1">
                                            收货人:${assignment.consignee!}<br/>
                                            收货人电话:${assignment.mobile!}<br/>
                                            收货地址:${assignment.province!""}${assignment.city!""}${assignment.county!""}${assignment.address!}
                                        </td>
                                        <td class="center  sorting_1"><#if assignment.usableCashAmount??>${assignment.usableCashAmount?string(",##0.##")}</#if></td>
                                        <td class="center  sorting_1">${assignment.createDate!''}</td>
                                        <td class="center ">
                                            <#--<#if ((assignment.orderType == 0 || assignment.orderType == 1) && (assignment.orderStatus = 1))-->
                                            <#--||((assignment.orderType == 2 || assignment.orderType == 3) && (assignment.orderStatus = 2))>-->
                                            <#if (assignment.orderType == 5 || assignment.orderType == 6)>
                                                <a id="finance_flow_${assignment.orderId!}" class="btn btn-info" href="javascript:void(0);">
                                                    <i class="icon-eye-open icon-white"></i>
                                                    查看财务流水
                                                </a>
                                                <br/>
                                            </#if>
                                                <a id="approve_order_${assignment.id!}" data-type="${assignment.orderType!}" class="btn btn-success" href="javascript:void(0);">
                                                        <i class="icon-ok icon-white"></i>
                                                        通过
                                                </a>
                                                <a id="reject_order_${assignment.id!}" class="btn btn-danger" href="javascript:void(0);">
                                                    <i class="icon-trash icon-white"></i>
                                                    拒绝
                                                </a>

                                        </td>
                                        <!--
                                        <td class="center ">
                                            <a id="load_order_his_${assignment.orderId!}" class="btn btn-success" href="javascript:void(0);">
                                                <i class="icon-ok icon-white"></i>
                                                查看
                                            </a>
                                        </td>
                                        -->
                                    </tr>
                                </#if>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div><!--/span-->
</div>
<div id="approveDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">订单流程备注</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">备注</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="approve_comment" rows="5"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="approve_btn" type="button" class="btn btn-primary">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="BulkRefundDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">退款</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">备注</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="BulkRefund_comment" rows="5"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="BulkRefund_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="BulkRefundDialog-Result" class="modal fade hide" style="width: 700px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close JS-closeList" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">退款处理结果</h4>
            </div>
            <div class="box-content">
                <div id="BulkResultList"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary JS-closeList">知道了</button>
            </div>
        </div>
    </div>
    <script type="text/html" id="T:Module-BulkResultList">
        <table class="table table-condensed">
            <thead>
            <tr>
                <th>订单号</th>
                <th>流水号</th>
                <th>退款金额</th>
                <th>状态</th>
                <th>错误码</th>
            </tr>
            </thead>
            <tbody>
                <%for(var i = 0; i < resultInfos.length; i++){%>
                <tr>
                    <td><%=resultInfos[i].orderId%></td>
                    <td><%=resultInfos[i].transactionId%></td>
                    <td><%=resultInfos[i].refundFee%></td>
                    <td><%=resultInfos[i].status%></td>
                    <td><%=resultInfos[i].errorCode%></td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </script>
</div>


<div id="rejectDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">拒绝订单流程描述</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">描述</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="reject_comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="reject_btn" type="button" class="btn btn-primary">保存</button>
                </div>
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

<div id="financeFlow" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">CRM财务流水明细</h4>
            </div>
            <div class="box-content">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th>订单号</th>
                        <th>类型</th>
                        <th>金额</th>
                        <th>支付渠道</th>
                        <th>场景</th>
                    </tr>
                    </thead>
                    <tbody id="financeFlowBody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="task_id" value="" data-type=""/>

<div id="FormAlipayPage"></div>

<script type="text/javascript">
    $(function(){
        $("a[id^='approve_order_']").live('click',function(){
            var id = $(this).attr("id").substring("approve_order_".length);
            $('#task_id').val(id);
            $('#task_id').attr("data-type",$(this).attr("data-type"));
            $('#approveDialog').modal('show');
        });

        $("a[id^='reject_order_']").live('click',function(){
            var id = $(this).attr("id").substring("reject_order_".length);
            $('#task_id').val(id);
            $('#task_id').attr("data-type",$(this).attr("data-type"));
            $('#rejectDialog').modal('show');
        });

        $("#approve_btn").live('click',function(){

            var id = $('#task_id').val();
            var orderType = $('#task_id').attr("data-type");
            var comment = $('#approve_comment').val();
            if(comment == '') {
                alert("请填写备注！");
                return false;
            }
            if(!confirm("确定要通过此订单?")){
                return false;
            }
            $.post('approveorder.vpage',{
                id:id,
                orderType:orderType,
                comment:comment
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });

        $("#reject_btn").live('click',function(){

            var id = $('#task_id').val();
            var orderType = $('#task_id').attr("data-type");
            var comment = $('#reject_comment').val();
            if(comment == '') {
                alert("请填写备注！");
                return false;
            }
            if(!confirm("确定要拒绝此订单?")){
                return false;
            }
            $.post('rejectorder.vpage',{
                id:id,
                comment:comment
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });

        $("a[id^='confirm_order_']").live('click',function(){
            var id = $(this).attr("id").substring("confirm_order_".length);
            $.post('confirmorder.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });

        $("a[id^='load_order_his_']").live('click',function(){
            $('#orderHistoryBody').html('');
            var id = $(this).attr("id").substring("load_order_his_".length);
            $.post('loadorderhistories.vpage',{
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

        $("a[id^='finance_flow_']").live('click',function(){
            $('#financeFlowBody').html('');
            var id = $(this).attr("id").substring("finance_flow_".length);
            $.post('financeflow.vpage',{
                agentOrderId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    for(var i=0; i<data.flows.length; i++){
                        var str = "<tr><td>"+data.flows[i].orderId+"</td>";
                        str += "<td class=\"center\">"+data.flows[i].paymentStatus+"</td>";
                        str += "<td class=\"center\">"+data.flows[i].payAmount+"</td>";
                        str += "<td class=\"center\">"+data.flows[i].payMethod+"</td>";
                        str += "<td class=\"center\">"+"UserOrder</td>";
                        $('#financeFlowBody').append(str);
                    }
                    $('#financeFlow').modal('show');
                }
            });
        });

        /* 批量退款 Start */
        var checkedIds = [];
        //全选
        $(document).on("click", ".JS-selectAllBox", function(){
            var $this = $(this);
            var $selectSignBox = $(".JS-selectSignBox");

            checkedIds = [];

            if( $this.is(':checked') ){
                $selectSignBox.prop('checked', true).parent().addClass('checked');

                $selectSignBox.each(function(index){
                    checkedIds.push($(this).attr("data-id"));
                });
            }else{
                $selectSignBox.prop('checked', false).parent().removeClass('checked');
            }

            $this.attr('data-ids', checkedIds.join());
        });

        //单选
        $(document).on("click", ".JS-selectSignBox", function(){
            var $this = $(this);
            var $selectAllBox = $(".JS-selectAllBox");

            if( $this.is(':checked') ){
                $this.prop('checked', true).parent().addClass('checked');
                checkedIds.push($this.attr("data-id"));
            }else{
                $this.prop('checked', false).parent().removeClass('checked');
                checkedIds.splice($.inArray($this.attr("data-id"), checkedIds), 1);
            }

            if(checkedIds.length == $(".JS-selectSignBox").length){
                $selectAllBox.prop('checked', true).parent().addClass('checked');
            }else{
                $selectAllBox.prop('checked', false).parent().removeClass('checked');
            }

            $selectAllBox.attr('data-ids', checkedIds.join());
        });

        //确认退款弹出框
        $(document).on("click", ".JS-BulkRefundBtn", function(){
            $("#BulkRefundDialog").modal('show');
        });

        //提交退款
        $(document).on("click", "#BulkRefund_submit", function(){
            if(checkedIds.length < 1){
                alert("请选择处理任务！");
                return false;
            }

            if($("#BulkRefund_comment").val() == ""){
                alert("请填写备注！");
                return false;
            }

            if(!confirm("确定要提交退款?")){
                return false;
            }

            $.ajax({
                url: "/task/todolist/batchrefund.vpage",
                type: "POST",
                data :{
                    comment : $("#BulkRefund_comment").val(),
                    orderIds : checkedIds.join(',')
                },
                success: function(data){
                    if(data.success){
                        $("#BulkRefundDialog").modal('hide');
                        if(data.resultInfos && data.resultInfos.length){
                            $("#BulkRefundDialog-Result").modal('show');
                            $("#BulkResultList").html( template("T:Module-BulkResultList", {
                                resultInfos : data.resultInfos
                            }) );
                        }

                        if(data.alipayForm){
                            $("#FormAlipayPage").html(data.alipayForm);

                            setTimeout(function(){
                                $("#refundForm").submit();
                            }, 200);
                        }

                        var $selectAllBox = $(".JS-selectAllBox");
                        checkedIds = [];
                        $selectAllBox.prop('checked', false).parent().removeClass('checked');
                        $selectAllBox.attr('data-ids', checkedIds.join());
                        $(".JS-selectSignBox").prop('checked', false).parent().removeClass('checked');
                    }else{
                        alert(data.info);
                    }
                },
                error: function(data){
                    alert(data.status)
                }
            });

            $(document).on("click", ".JS-closeList", function(){
                $("#BulkRefundDialog-Result").modal('hide');
                location.reload();
            });
        });
        /*批量退款 End*/
    });
</script>
</@layout_default.page>
