<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='维护老师添加' page_num=16>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>维护老师添加</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content school_manage">
            <form id="form" class="form-horizontal" method="post" enctype="multipart/form-data"
                  action="/taskcenter/maintainteacher/task_add.vpage" data-ajax="false">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">标题</label>
                    <div class="controls">
                        <input name="title" id="task_title" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写标题">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">截止时间</label>
                    <div class="controls">
                        <input id="endTime" name="endTime" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="截止时间不能为空">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">任务说明：</label>
                    <div class="controls">
                        <textarea name="comment" id="comment" style="width: 725px;height: 50px;padding: 3px;resize: none;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">上传附件</label>
                    <div class="controls">
                        <input id="sourceFile" name="sourceExcelFile" type="file" />
                        <#--<a href="javascript:;" class="btn btn-primary savaBtn">上传</a>-->
                        <a href="/taskmanage/maintainteacher/task_import_template.vpage" class="btn btn-primary">下载模板</a>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary cancleBtn">取消</button>
                    <button type="button" class="btn btn-primary saveBtn">保存并发布</button>
                </div>
            </form>
        </div>
    </div>
</div>
<link href="https://cdn.bootcss.com/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.min.css" rel="stylesheet">
<script src="https://cdn.bootcss.com/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript">

$(function () {
    //时间控件
    $("#endTime").datetimepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        timeFormat      : 'HH:mm',
        closeText       : "关闭",
        currentText     : "本月",
        timeText        : '时间',
        hourText        : '小时',
        minuteText      : '分钟',
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : new Date(),
        minDate         : new Date(),
        numberOfMonths  : 1,
        changeMonth: true,
        changeYear: true,
        showButtonPanel: true,
        onSelect : function (selectedDate){}
    });

    $("#sourceFile").on('change',function () {
        var sourceFile = $("#sourceFile").val();
        if (blankString(sourceFile)) {
            layer.alert("请上传excel！");
            return;
        }
        var fileParts = sourceFile.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt !== "xls" && fileExt !== "xlsx" && fileExt !== "csv") {
            $("#sourceFile").val('');
            $('.filename').text('No file selected');
            layer.alert("请上传正确格式(xls、xlsx、csv)的excel！");
            return;
        }
    });

    function checkData() {
        var flag = true;
        $.each($(".js-postData"),function(i,item){
            if($(item).hasClass('js-needed')){
                if(!($(item).val())){
                    layer.alert($(item).data("einfo"));
                    flag = false;
                }
            }
        });
        if($('#sourceFile').val() == ''){
            layer.alert('请选择附件');
            flag = false;
        }
        if($('#task_title').val().trim().length > 15){
            layer.msg('标题限定15个字');
            flag = false;
        }
        if($('#comment').val().trim().length > 100){
            layer.msg('任务说明限定100个字');
            flag = false;
        }
        return flag;
    }

    // 上传操作
    var saveBtn = $('.saveBtn');//上传按钮
    var _loading;
    saveBtn.on('click',function () {
        var formElement = $('#form')[0];
        var postData = new FormData(formElement);
        if(checkData()){
            _loading = layer.load(1, {
                shade: [0.5,'#fff'] //0.1透明度的白色背景
            });
            $.ajax({
                url: "/taskmanage/maintainteacher/add_task.vpage",
                type: "POST",
                data: postData,
                dataType: "json",
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType : false,
                success: function (res) {
                    layer.close(_loading);//关闭loading
                    if(res.success){
                        layer.alert('共计上传'+ res.teacherNum +'条老师信息',function () {
                            window.history.back();
                        });
                    }else{
                        if(res.info){
                            layer.alert(res.info);
                            return;
                        }
                        if(res.errorList){
                            layer.alert(res.errorList.toString());
                            return;
                        }
                    }
                }
            });
        }

    });

    // 取消操作
    var cancleBtn = $('.cancleBtn');//上传按钮
    cancleBtn.on('click',function () {
        window.history.back();
    });

});
</script>
</@layout_default.page>