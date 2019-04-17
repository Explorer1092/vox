<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='维护老师编辑页面' page_num=16>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>维护老师编辑</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content task_manage">
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
                        <textarea name="comment" style="width: 725px;height: 50px;padding: 3px;resize: none;"></textarea>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary cancelBtn">取消</button>
                    <button type="button" class="btn btn-primary saveBtn">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>

<#--合同信息模板-->
<script type="text/html" id="task_info">
    <form id="form" class="form-horizontal">
        <input type="hidden" name="id" value="<%=res.id%>">
        <div class="control-group">
            <label class="control-label" for="focusedInput">标题</label>
            <div class="controls">
                <input name="title" id="task_title" value="<%=res.title%>" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写标题">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">截止时间</label>
            <div class="controls">
                <input id="endTime" name="endTime" value="<%=res.endTime%>" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="截止时间不能为空">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">任务说明：</label>
            <div class="controls">
                <textarea name="comment" id="comment" style="width: 725px;height: 50px;padding: 3px;resize: none;"><%=res.comment%></textarea>
            </div>
        </div>
        <div class="form-actions">
            <button type="button" class="btn btn-primary cancelBtn">取消</button>
            <button type="button" class="btn btn-primary saveBtn">保存</button>
        </div>
    </form>
</script>
<link href="https://cdn.bootcss.com/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.min.css" rel="stylesheet">
<script src="https://cdn.bootcss.com/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript">
$(function () {
    var id = getQuery("id");

    $.get('/taskmanage/maintainteacher/main_task_detail.vpage?id='+id,function (res) {
        $('.task_manage').html(template('task_info',{res:res.dataMap || ''}));
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
    $(document).on('click','.saveBtn',function () {
        var formElement = $('#form')[0];
        var postData = new FormData(formElement);
        if(checkData()){
            $.ajax({
                url: "/taskmanage/maintainteacher/edit_main_task.vpage",
                type: "POST",
                data: postData,
                dataType: "json",
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType : false,
                success: function (res) {
                    if(res.success){
                        layer.alert('编辑成功',function () {
                            window.history.back();
                        });
                    }else{
                        if(res.info){
                            layer.alert(res.info);
                        }
                    }
                }
            });
        }

    });

    // 取消操作
    $(document).on('click','.cancelBtn',function () {
        window.history.back();
    });

});
</script>
</@layout_default.page>