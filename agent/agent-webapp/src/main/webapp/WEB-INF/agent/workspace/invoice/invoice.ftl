<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='物料发货管理' page_num=14>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 物料发货管理</h2>
            <div class="box-icon">
                <button id="generateInvoice" style="background:#3f9fd9;float:left;margin-right:20px" type="button" class="btn btn-round btn-success" onclick="generateInvoice();">生成发货单</button>
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
                </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <div class="well">
                <input type="file" id="sourceExcelFile" name="sourceExcelFile" />
                <button id="importLogisticsInfo" type="button" class="btn btn-success" onclick="importLogisticsInfo();" >导入发货单</button>
                <button id="downloadTemplate" type="button" class="btn btn-success" onclick="downloadTemplate();" >下载模板</button>

            </div>

            <form id="query_form"  action="list.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" style="width:60px;" for="selectError3">发货单ID:</label>
                        <div class="controls"  style="margin-left:80px;">
                            <input type="text" class="input-medium" id="invoiceId" name="invoiceId" value="${invoiceId!}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" style="width:60px;" for="selectError3">物流单号:</label>
                        <div class="controls"  style="margin-left:80px;">
                            <input type="text" class="input-medium" id="logisticsId" name="logisticsId" value="${logisticsId!}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" style="width:60px;" for="selectError3">物流状态:</label>
                        <div class="controls" style="margin-left:80px;">
                            <select id="logisticsStatus" name="logisticsStatus" style="width:150px;padding:4px;" >
                                <option value="" style="">全部</option>
                                <#list logisticsStatusList as item>
                                    <option value="${item.name()}" <#if logisticsStatus?? && logisticsStatus.name() == item.name()>selected</#if> >${item.value}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span3" >
                        <label class="control-label" style="width:60px;" for="selectError3">开始日期</label>
                        <div class="controls" style="margin-left:80px;">
                            <input type="text" class="input-medium" id="startDate" name="startDate" value="${startDate!}">
                        </div>
                    </div>
                    <div class="control-group span3" style="margin-left:0">
                        <label class="control-label" style="width:60px;" for="selectError3">结束日期</label>
                        <div class="controls" style="margin-left:80px;">
                            <input type="text" class="input-medium" id="endDate" name="endDate" value="${endDate!}">
                        </div>
                    </div>
                    <div class="control-group span3" style="width:50px">
                        <div class="controls" style="margin-left:0px;">
                            <button id="btnQuery" type="submit" class="btn btn-success" onclick="setFormAction('list.vpage');" on>查询</button>
                        </div>
                    </div>
                    <div class="control-group span3" style="width:50px">
                        <div class="controls" style="margin-left:0px;">
                            <button id="btnQuery" type="submit" class="btn btn-success" onclick="setFormAction('export_list.vpage');">导出</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 50px;">发货单ID</th>
                        <th class="sorting" style="width: 100px;">物流公司</th>
                        <th class="sorting" style="width: 100px;">物流单号</th>
                        <th class="sorting" style="width: 100px;">物流价格</th>
                        <th class="sorting" style="width: 100px;">物流状态</th>
                        <th class="sorting" style="width: 100px;">物料明细</th>
                        <th class="sorting" style="width: 100px;">收货人</th>
                        <th class="sorting" style="width: 100px;">收货人电话</th>
                        <th class="sorting" style="width: 100px;">收货地址</th>
                        <th class="sorting" style="width: 100px;">生成时间</th>
                        <th class="sorting" style="width: 100px;">更新时间</th>
                        <th class="sorting" style="width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if invoiceList??>
                            <#list invoiceList as invoice>
                                <tr class="odd">
                                    <td class="center  sorting_1">${invoice.id!}</td>
                                    <td class="center  sorting_1">${invoice.logisticsCompany!}</td>
                                    <td class="center  sorting_1">${invoice.logisticsId!}</td>
                                    <td class="center  sorting_1"><#if invoice.logisticsPrice??>${invoice.logisticsPrice?string(",##0.##")}</#if></td>
                                    <td class="center  sorting_1"><#if invoice.logisticsStatus??>${invoice.logisticsStatus.value!}</#if></td>
                                    <td class="center  sorting_1">
                                        <#if invoice.productList??>
                                            <#list invoice.productList as product>
                                                ${product.productName!}*${product.productQuantity!}<br/>
                                            </#list>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">${invoice.consignee!}</td>
                                    <td class="center  sorting_1">${invoice.mobile!}</td>
                                    <td class="center  sorting_1">${invoice.province!""}${invoice.city!""}${invoice.county!""}${invoice.address!""}</td>
                                    <td class="center  sorting_1">${invoice.createDatetime?string("yyyy-MM-dd")}</td>
                                    <td class="center  sorting_1">${invoice.updateDatetime?string("yyyy-MM-dd")}</td>
                                    <td class="center  sorting_1">
                                        <#if invoice.logisticsStatus?? && invoice.logisticsStatus.value! == "配货中">
                                            <a href="javascript:void(0)"
                                               class="revocation_invoice"
                                               data-invoice="${invoice.id!}">撤销发货单</a>
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

<script type="text/javascript">
    $(function(){

        $("#startDate").datepicker({
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

        $("#endDate").datepicker({
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
    });

    $(document).on("click", ".revocation_invoice", function () {
        var invoiceId = $(this).data("invoice");
        if (confirm("是否确定撤销发货单？")) {
            $.post("revocation_invoice.vpage", {invoiceId: invoiceId}, function (data) {
                if (data.success) {
                    alert("撤回成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            })
        }
    });

    function setFormAction(action){
        $("#query_form").attr("action", action);
        return true;
    }
    var invoiceBooloean = true ;
    function generateInvoice(){
        if(invoiceBooloean){
            invoiceBooloean = false ;
            $.post('generate_invoice.vpage', {
            }, function (data) {
                if (!data.success) {
                    alert(data.info);
                    invoiceBooloean = true ;
                } else {
                    alert("生成成功");
                    window.location.reload();
                }
            });
        }
    }

    function importLogisticsInfo(){
        var formData = new FormData();
        var file = $('#sourceExcelFile')[0].files[0];
        formData.append('sourceExcelFile', file);
        $.ajax({
            url: 'import_logistics_info.vpage',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if (data.success) {
                    if(!alert("导入成功")){
                        window.location.reload();
                    }
                } else {
                    alert(data.info);
                }
            }
        });
    }

    function downloadTemplate(){
        var form=$("<form>");//定义一个form表单
        form.attr("style","display:none");
        form.attr("target","");
        form.attr("method","post");
        form.attr("action","download_template.vpage");
        $("body").append(form);//将表单放置在web中
        form.submit();//表单提交
    }

    function exportInvoiceList(){
        var form=$("<form>");//定义一个form表单
        form.attr("style","display:none");
        form.attr("target","");
        form.attr("method","post");
        form.attr("action","download_template.vpage");
        $("body").append(form);//将表单放置在web中
        form.submit();//表单提交
    }

</script>
</@layout_default.page>
