<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='学校字典表导入' page_num=6>
<style>
    body {
        text-shadow: none;
    }

    /*不显示日期面板*/
    .ui-datepicker-calendar {
        display: none;
    }
</style>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在下载，请等待……</p>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="alert alert-error" hidden>
        <button type="button" class="close" data-dismiss="alert">×</button>
        <strong id="error-panel"></strong>
    </div>
    <div class="alert alert-info" hidden>
        <button type="button" class="close" data-dismiss="alert">×</button>
        <strong id="info-panel"></strong>
    </div>
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>导出字典表</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content ">
            <form id="exportSchoolDict" method="get" enctype="multipart/form-data"
                  action="/sysconfig/schooldic/exportSchoolDictInfo.vpage" data-ajax="false"
                  class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">选择月份</label>
                    <div class="controls">
                        <input type="text" class="reportMonth input-small checkData" id="startDate" name="startDate"
                               value="" data-info="请选择月份">
                        <input type="submit" id="export" value="导出">
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<script>
    $("#startDate").datepicker({
        dateFormat: 'yymm',  //日期格式，自己设置
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        prevText: '上月',         // 前选按钮提示
        nextText: '下月',
        showButtonPanel: true,        // 显示按钮面板
        minDate: '11-7-280',
        showMonthAfterYear: true,
        currentText: "本月",  // 当前日期按钮提示文字
        closeText: "关闭",
        numberOfMonths: 1,
        changeMonth: true,
        changeYear: true,
        onSelect: function (selectedDate) {
        },
        onClose: function (dateText, inst) {// 关闭事件
            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).datepicker('setDate', new Date(year, month, 1));
        }
    });
</script>
</@layout_default.page>
