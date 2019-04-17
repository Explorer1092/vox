<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='管理员后台任务' page_num=1>
<div class="row-fluid sortable ui-sortable" xmlns="http://www.w3.org/1999/html">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 2016秋季KPI</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="calSalaryExecuteBtn" class="btn btn-primary" href="#">
                    <i class="icon-ok icon-white"></i>
                    运行
                </a>&nbsp;
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">结算日期</label>

                    <div class="controls">
                        <input type="text" class="input-xlarge date" id="evalDate">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">用户ID( , 分隔)</label>

                    <div class="controls">
                        <textarea class="input-xlarge" id="userIds" rows="10"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">上传已离职人员数据</label>
                    <div class="controls">
                        <input type="file" id="sourceExcelFile" name="sourceExcelFile" />
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="focusedInput">是否留存字典表学校业绩</label>
                    <div class="controls">
                        <input type="checkbox" id="includeDictSchool" name="includeDictSchool" class="apply-role"/>（注：如果已经留存过了，则不需再次留存，再次留存会重刷原来数据）
                    </div>
                </div>

            </form>
        </div>

        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 2016秋季KPI微调(慎用)</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="adjustBtn" class="btn btn-danger" href="#">
                    <i class="icon-ok icon-white"></i>
                    确认调整
                </a>&nbsp;
                <a id="downloadInsert" class="btn btn-success" href="downloadtemplate.vpage?type=insert">
                    <i class="icon-download-alt icon-white"></i>
                    下载插入数据模板
                </a>&nbsp;
                <a id="downloadUpdate" class="btn btn-success" href="downloadtemplate.vpage?type=update">
                    <i class="icon-download-alt icon-white"></i>
                    下载更新数据模板
                </a>&nbsp;
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">调整类型</label>
                    <div class="controls">
                       <select name="type" id="type">
                           <option value="">-请选择-</option>
                           <option value="insert">插入数据</option>
                           <option value="update">更新数据</option>
                       </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">上传Excel</label>
                    <div class="controls">
                        <input type="file" id="adjustExcel" name="adjustExcel" />
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(".date").datepicker({
        dateFormat: 'yy-mm-dd',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        onSelect: function (selectedDate) {
        }
    });

    $(function () {
        $('#calSalaryExecuteBtn').live('click', function () {
            var evalDate = $('#evalDate').val();
            if (evalDate === "") {
                alert("请指定结算日期！");
                return;
            }
            var userIds = $('#userIds').val();
            $('#calSalaryExecuteBtn').attr("disabled", true);

            var formData = new FormData();
            formData.append("runDate", evalDate);
            formData.append("userIds", userIds);
            var file = $('#sourceExcelFile')[0].files[0];
            formData.append('sourceExcelFile', file);
            var includeDictSchool = false;
            if($("#includeDictSchool").is(':checked')){
                includeDictSchool = true;
            }
            formData.append("includeDictSchool", includeDictSchool);

            $.ajax({
                url: "calsalary.vpage",
                type: "POST",
                data: formData,
                processData: false,
                contentType: false,
                async: true,
                timeout: 5 * 60 * 1000,
                error: function(){
                    alert("请求超时，确认后台数据还在计算的前提下，无妨");
                },
                success: function(data) {
                    if (!data.success) {
                        alert("Error:\n" + data.info);
                    } else {
                        alert("Success:\n" + data.info);
                    }
                    $('#calSalaryExecuteBtn').attr("disabled", false);
                }
            });

        });

        $('#adjustBtn').live('click', function () {
            var type = $('#type').find('option:selected').val();
            if (type === "") {
                alert("请选择导入模板类型");
                return;
            }
            $('#adjustBtn').attr("disabled", true);

            var formData = new FormData();
            formData.append("type", type);
            var file = $('#adjustExcel')[0].files[0];
            formData.append('adjustExcel', file);

            $.ajax({
                url: "adjustsalary.vpage",
                type: "POST",
                data: formData,
                processData: false,
                contentType: false,
                async: true,
                timeout: 5 * 60 * 1000,
                success: function(data) {
                    if (!data.success) {
                        alert("Error:\n" + data.info);
                    } else {
                        alert("Success:\n" + data.info);
                    }
                    $('#adjustBtn').attr("disabled", false);
                }
            });
        });

    });
</script>

</@layout_default.page>