<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='退款状态查询' page_num=2>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 用户退款失败查询</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <button type="submit" id="search_btn" class="btn btn-success">查询</button>
            </div>
        </div>
        <div class="box-content">
            <form id="query_form"  action="failorderlist/index.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">用户ID</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="userId" name="userId" value="${(userId)!''}">
                        </div>
                    </div>
                </fieldset>
            </form>
            <div class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable" id="dt_task_donelist">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 120px;">订单号</th>
                        <th class="sorting" style="width: 50px;">用户ID</th>
                        <th class="sorting" style="width: 80px;">支付方式</th>
                        <th class="sorting" style="width: 80px;">退款金额</th>
                        <th class="sorting" style="width: 80px;">状态</th>
                        <th class="sorting" style="width: 60px;">操作</th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if failList??>
                            <#list failList as data>
                                <tr class="odd">
                                    <td class="center  sorting_1">${data.orderId!}</td>
                                    <td class="center  sorting_1">${data.userId!}</td>
                                    <td class="center  sorting_1">${data.payMethod!}</td>
                                    <td class="center  sorting_1">${data.payAmount!}</td>
                                    <td class="center  sorting_1">${data.paymentStatus!}</td>
                                    <td class="center  sorting_1">
                                        <a class="btn btn-info"
                                           href="javascript:updatePaymentHis('${data.userId!""}', '${data.orderId!""}')">
                                            <i class="icon-edit icon-white"></i>
                                            手动退款完成
                                        </a>
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
<script type="text/javascript">
    $(function(){
        $('#search_btn').on('click',function(){
            var userId = $('#userId').val();

            if (userId == '' || userId == 0) {
                alert("请填写要查询的用户ID!");
                return false;
            }
            window.location.href = "index.vpage?userId=" + userId;
        });
    });


    function updatePaymentHis(userId, orderId) {
        if (confirm("是否确认已手动退款成功？")) {
            $.post("updatepaymenthistory.vpage", {userId: userId, orderId: orderId}, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            })
        }
    }
</script>
</@layout_default.page>
