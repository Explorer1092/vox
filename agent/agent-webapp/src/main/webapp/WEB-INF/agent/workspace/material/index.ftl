<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='物料费用明细' page_num=1>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 下载物料费用明细</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="export_order_list.vpage" method="post" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">请选择日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="orderStartDate" name="orderStartDate" value="">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <input type="text" class="input-small" id="orderEndDate" name="orderEndDate" value="">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="submit" id="order_search_btn" class="btn btn-success">下载订单数据</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <form id="query_form"  action="export_invoice_list.vpage" method="post" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">请选择日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="invoiceStartDate" name="invoiceStartDate" value="">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <input type="text" class="input-small" id="invoiceEndDate" name="invoiceEndDate" value="">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="submit" id="invoice_search_btn" class="btn btn-success">下载发货单数据</button>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>

        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 下载物料费用明细（以前功能，按月份导出，其中订单申请日期为商品加入订单日期）</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form id="query_form"  action="export_list.vpage" method="post" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">请选择月份</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="date" name="date" value="">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="submit" id="old_search_btn" class="btn btn-success">下载</button>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){

        $("#orderStartDate, #orderEndDate, #invoiceStartDate, #invoiceEndDate, #date").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });



        $('#order_search_btn').on('click',function(){
            var startDate = $('#orderStartDate').val();
            var endDate = $('#orderEndDate').val();
            if (startDate == '') {
                alert("请选择开始日期!");
                return false;
            }
            if (endDate == '') {
                alert("请选择结束日期!");
                return false;
            }
            return true;
        });

        $('#invoice_search_btn').on('click',function(){
            var startDate = $('#invoiceStartDate').val();
            var endDate = $('#invoiceEndDate').val();
            if (startDate == '') {
                alert("请选择开始日期!");
                return false;
            }
            if (endDate == '') {
                alert("请选择结束日期!");
                return false;
            }
            return true;
        });

        $('#old_search_btn').on('click',function(){
            var startDate = $('#date').val();
            if (startDate == '') {
                alert("请选择月份!");
                return false;
            }
            return true;
        });


    });
</script>
</@layout_default.page>
