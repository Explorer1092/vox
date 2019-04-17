<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="数据下载" page_num=10>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 快乐学学校每日扫描量学校详情</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <style>
            select {
                width: 120px;
                position: relative;
                z-index: 3
            }

            /*label会遮住select的点击区域*/
        </style>
        <div class="box-content ">
            <form method="GET" id="school_every_scan_form" class="form-horizontal">
                <ul class="row-fluid">
                    <li class="span3" style="width:20%">
                        <div class="control-group">
                            <label class="control-label" style="width:90px">选择日期</label>
                            <div class="controls" style="margin-left:100px">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="schoolDate"
                                       name="startDate" value="${date!}">
                            </div>
                        </div>
                    </li>
                    <li class="span6" style="width:20%;margin-left:10%">
                        <a href="javascript:void(0);" class="btn btn-primary" id="schoolExportBtn"
                           target="_blank">下载</a>
                    </li>
                    <input type="hidden" value="${schoolType!''}" name="type">
                </ul>
            </form>
        </div>
    </div>
</div>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 快乐学学校每日扫描量老师详情</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <style>
            select {
                width: 120px;
                position: relative;
                z-index: 3
            }

            /*label会遮住select的点击区域*/
        </style>
        <div class="box-content ">
            <form method="GET" id="teacher_every_scan_form" class="form-horizontal">
                <ul class="row-fluid">
                    <li class="span3" style="width:20%">
                        <div class="control-group">
                            <label class="control-label" style="width:90px">选择日期</label>
                            <div class="controls" style="margin-left:100px">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="teacherDate"
                                       name="startDate" value="${date!}">
                            </div>
                        </div>
                    </li>
                    <li class="span6" style="width:20%;margin-left:10%">
                        <a href="javascript:void(0);" class="btn btn-primary" id="teacherExportBtn"
                           target="_blank">下载</a>
                    </li>
                    <input type="hidden" value="${teacherType!''}" name="type">
                </ul>
            </form>
        </div>
    </div>
</div>
<script>
    //时间控件
    $("#schoolDate").datepicker({
        dateFormat: 'yy-mm',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"]
    });

    $("#teacherDate").datepicker({
        dateFormat: 'yy-mm',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"]
    });

    // 下载
    $(document).on("click", "#schoolExportBtn", function () {
        if ($('#schoolDate').val() != '') {
            $("#school_every_scan_form").attr({
                "action": "export_scan_detail.vpage",
                "method": "GET"
            });
            var formElement = document.getElementById("school_every_scan_form");
            formElement.submit();
        } else {
            alert('请选择日期')
        }
    });

    $(document).on("click", "#teacherExportBtn", function () {
        if ($('#teacherDate').val() != '') {
            $("#teacher_every_scan_form").attr({
                "action": "export_scan_detail.vpage",
                "method": "GET"
            });
            var formElement = document.getElementById("teacher_every_scan_form");
            formElement.submit();
        } else {
            alert('请选择日期')
        }
    });
</script>
</@layout_default.page>