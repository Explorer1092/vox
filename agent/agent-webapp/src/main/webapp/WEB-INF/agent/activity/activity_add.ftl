<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='活动管理' page_num=19>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>添加/修改活动</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*活动名称</label>
                    <div class="controls">
                        <input id="real_name" name="name" class="js-postData input-xlarge focused js-needed" maxlength="20" type="text" data-einfo="请填写活动名称">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*开始日期</label>
                    <div class="controls">
                        <input id="startDate" name="startDate" class="startDate js-postData input-xlarge focused js-needed" type="text" data-needInfo="开始日期不能为空"
                               data-einfo="请选择开始日期">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">*结束日期</label>
                    <div class="controls">
                        <input id="endDate" name="endDate" class="endDate js-postData input-xlarge focused js-needed" type="text" data-einfo="请选择结束日期">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">原价</label>
                    <div class="controls">
                        <input name="originalPrice" class="js-postData input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">现价</label>
                    <div class="controls">
                        <input name="presentPrice" class="js-postData input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
                    <button type="button" class="btn btn-primary submitBtn" data-info="1">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
$(function () {
//时间控件
    $("#startDate,#endDate").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : new Date(),
        numberOfMonths  : 1,
        onSelect : function (selectedDate){
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if($(this).hasClass('startDate')){
                if (endDate != "" && selectedDate > endDate) {
                    layer.alert("开始时间不能小于结束时间");
                    $("#endDate").val("");
                }
            }else{
                if (startDate > selectedDate) {
                    layer.alert("开始时间不能小于结束时间");
                    $("#endDate").val("");
                }
            }
        }
    });

    $('.submitBtn').on('click',function () {
        var info = $(this).data('info');
        if(info == 0){
            window.history.back();
        }else{

            var activity_name = $('input[name="name"]').val().trim();
            var startDate = $('input[name="startDate"]').val().trim();
            var endDate = $('input[name="endDate"]').val().trim();
            var originalPrice = $('input[name="originalPrice"]').val().trim();
            var presentPrice = $('input[name="presentPrice"]').val().trim();

            if(activity_name == ''){
                layer.alert("活动名称不能为空！");
                return;
            }
            if(startDate == ''){
                layer.alert("活动开始时间不能为空！");
                return;
            }
            if(endDate == ''){
                layer.alert("活动结束时间不能为空！");
                return;
            }

            $.get('add.vpage',{
                name:activity_name,
                startDate:startDate,
                endDate:endDate,
                originalPrice:originalPrice,
                presentPrice:presentPrice
            }).done(function (res) {
                if(res.success){
                    layer.alert('保存成功',function () {
                        window.history.back();
                    });
                }else{
                    layer.alert(res.info);
                }
            })
        }

    });
});
</script>
</@layout_default.page>
