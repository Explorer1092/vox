<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='应用月度统计' page_num=7>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 应用月总收入统计</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="monthstatistics.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-medium" id="startDate" name="startDate" value="${startDate}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-medium" id="endDate" name="endDate" value="${endDate}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button id="btnQuery" type="submit" class="btn btn-success">查询</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 50px;">月份</th>
                        <th class="sorting" style="width: 100px;">应用名称</th>
                        <th class="sorting" style="width: 100px;">总收入</th>
                        <th class="sorting" style="width: 100px;">付费率</th>
                        <th class="sorting" style="width: 100px;">手续费</th>
                        <th class="sorting" style="width: 100px;">退款金额</th>
                        <th class="sorting" style="width: 100px;">可分成收入</th>
                        <th class="sorting" style="width: 100px;">应得收入</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if monthList??>
                            <#list monthList as month>
                                <tr class="odd">
                                    <td class="center  sorting_1">${month.month}</td>
                                    <td class="center  sorting_1">${month.appKey!}</td>
                                    <td class="center  sorting_1">${month.totalRevenue!}</td>
                                    <td class="center  sorting_1">${month.paidRate!}%</td>
                                    <td class="center  sorting_1">${month.procedureFees!}</td>
                                    <td class="center  sorting_1">${month.refundAmount!}</td>
                                    <td class="center  sorting_1">${month.sharedRevenue!}</td>
                                    <td class="center  sorting_1">${month.deservedRevenue!}</td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<style>
    .ui-datepicker-calendar {
        display: none;
    }
</style>
<script type="text/javascript">
    $(function(){

        $("#startDate").datepicker({
            dateFormat      : 'yymm',  //日期格式，自己设置
            closeText       : "确定",
            currentText     : "本月",
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            onSelect : function (selectedDate){},
            onClose: function(dateText, inst) {
                var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                $(this).datepicker('setDate', new Date(year, month, 1));
            }
        });

        $("#endDate").datepicker({
            dateFormat      : 'yymm',  //日期格式，自己设置
            closeText       : "确定",
            currentText     : "本月",
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            onSelect : function (selectedDate){},
            onClose: function(dateText, inst) {
                var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                $(this).datepicker('setDate', new Date(year, month, 1));
            }
        });
    });

</script>
</@layout_default.page>
