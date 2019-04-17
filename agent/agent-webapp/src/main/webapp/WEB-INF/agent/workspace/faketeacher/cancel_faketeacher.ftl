<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='取消判假老师明细' page_num=10>
<style>
    #DataTables_Table_0_filter{
        position:absolute;
        top:16%
    }
</style>
<div class="row-fluid sortable ui-sortable" style="position: relative;">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 取消判假老师明细</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <button type="submit" id="search_btn" class="btn btn-success">查询</button>
                <button type="button" id="download_btn" class="btn btn-success" onclick="exportApplyExcel();">导出Excel</button>
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="cancel_faketeacher.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="startDate" name="startDate" value="${(startDate?string('yyyy-MM-dd'))!''}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="endDate" name="endDate" value="${(endDate?string('yyyy-MM-dd'))!''}">
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 50px;">日期</th>
                        <th class="sorting" style="width: 50px;">大区</th>
                        <th class="sorting" style="width: 50px;">部门</th>
                        <th class="sorting" style="width: 50px;">操作人</th>
                        <th class="sorting" style="width: 50px;">老师id</th>
                        <th class="sorting" style="width: 50px;">老师姓名</th>
                        <th class="sorting" style="width: 50px;">取消判假原因</th>
                    </tr>
                    </thead>
                    <tbody>
                       <#if cancelFaketeacherList??>
                                <#list cancelFaketeacherList?sort_by("cancleDate")?reverse as cancel>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1">${cancel.cancleDate?string('yyyyMMdd')!}</td>
                                    <td class="center  sorting_1">${cancel.region!}</td>
                                    <td class="center  sorting_1">${cancel.department!}</td>
                                    <td class="center  sorting_1">${cancel.operationName!}</td>
                                    <td class="center  sorting_1">${cancel.teacherId!}</td>
                                    <td class="center  sorting_1">${cancel.teacherName!}</td>
                                    <td class="center  sorting_1">${cancel.reason!}</td>
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

        $('#search_btn').on('click',function(){
            var startDate = $('#startDate').val();
            var endDate = $('#endDate').val();

            if (startDate == '') {
                alert("请选择开始日期!");
                return false;
            }

            if (endDate == '') {
                alert("请选择结束日期!");
                return false;
            }

            if(startDate == endDate){
                endDate = new Date();
            }
            window.location.href = "cancel_faketeacher.vpage?startDate=" + startDate + "&endDate=" + endDate;
        });
    });

    function exportApplyExcel(){
        var startDate = $('#startDate').val();
        var endDate = $('#endDate').val();

        if (startDate > endDate) {
            alert("开始时间不能大于结束时间!");
            return;
        }
        if(startDate == endDate){
            endDate = new Date();
        }
        window.location.href = "dowmload_cancel_faketeacher.vpage?startDate=" + startDate + "&endDate=" + endDate;
    }
</script>
</@layout_default.page>
