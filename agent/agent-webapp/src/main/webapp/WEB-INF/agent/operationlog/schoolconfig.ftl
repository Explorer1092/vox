<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='市经理调整学校' page_num=10>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">数据查询中，请稍后……</p>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 学校负责人调整 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="javascript:void(0)" class="btn btn-primary" id="searchBtn">查询</a>
                <a href="javascript:void(0)" class="btn btn-primary" id="exportBtn">导出</a>
            </div>
        </div>
        <div class="box-content">
            <style>
                select {
                    width: 120px;
                    position: relative;
                    z-index: 3
                }

                /*label会遮住select的点击区域*/
            </style>
            <form id="date_form" <#--action="exportDataReport.vpage"--> method="GET" class="form-horizontal">
                <div class="row-fluid">
                    <div class="span3">
                        <div class="control-group">
                            <label class="control-label">开始日期</label>
                            <div class="controls">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="startDate" name="startDate" value="${startDate!""}">
                            </div>
                        </div>
                    </div>
                    <div class="span3">
                        <div class="control-group">
                            <label class="control-label">结束日期</label>
                            <div class="controls">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="endDate" name="endDate" value="${endDate!""}">
                            </div>
                        </div>
                    </div>
                </div>

            </form>
            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                   id="DataTables_Table_0"
                   aria-describedby="DataTables_Table_0_info">
                <thead>
                <tr>
                    <th class="sorting">日期</th>
                    <th class="sorting">大区</th>
                    <th class="sorting">部门</th>
                    <th class="sorting">操作人</th>
                    <th class="sorting">类型</th>
                    <th class="sorting">学校ID</th>
                    <th class="sorting">学校名称</th>
                    <th class="sorting">阶段</th>
                    <th class="sorting">原始负责人</th>
                    <th class="sorting">新负责人</th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
<script>
    $(function(){
        searchTableDetail();
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
                var endDate = $("#endDate").val();
                if (endDate != "" && selectedDate > endDate) {
                    alert("开始时间不能小于结束时间");
                    $("#endDate").val("");
                }
            }
        });

        $("#endDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
                var startDate = $("#startDate").val();
                if (startDate > selectedDate) {
                    alert("开始时间不能小于结束时间");
                    $("#endDate").val("");
                }
            }
        });
    });
    // 查询
    var searchFlag = true;

    var searchTableDetail =  function () {
        if (searchFlag) {
            $("#loadingDiv").show();
            searchFlag = false;
            var formElement = document.getElementById("date_form");
            var postData = new FormData(formElement);
            $.ajax({
                url: "search.vpage",
                type: "POST",
                data: postData,
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType: false,
                success: function (res){
                    $("#loadingDiv").hide();
                    searchFlag = true;
                    if (res.success) {
                        var table = $('#DataTables_Table_0').dataTable();
                        table.fnClearTable(); //清除表格的数据
                        $('#DataTables_Table_0').dataTable().fnAddData(res.data); //添加添加新数据
                    } else {
                        alert(res.info);
                    }
                },
                error: function (e) {
                    console.log(e);
                    $("#loadingDiv").hide();
                    searchFlag = true;
                }
            });
        }
    }

    $(document).on("click", "#searchBtn",searchTableDetail);
    // 下载
    $(document).on("click", "#exportBtn", function () {
        $("#date_form").attr({
            "action": "exportlog.vpage",
            "method": "GET"
        });
        var formElement = document.getElementById("date_form");
        formElement.submit();
    });
</script>
</@layout_default.page>