<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='任务管理' page_num=2>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 新建任务 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="javascript:iSave();" class="btn btn-success"><i class="icon-plus icon-white"></i>提交</a>&nbsp;&nbsp;
            </div>
        </div>
        <div class="box-content">
            <form id="task-form" method="post" enctype="multipart/form-data" action="/task/manage/create_task.vpage" data-ajax="false" class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">任务分类</label>

                    <div class="controls">
                        <select id="iCategory" name="category">
                            <#list categories as category>
                                <option value="${category.name()}">${category.value}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">截止时间</label>

                    <div class="controls">
                        <input id="endTime" name="endTime" type="text" class="date">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">任务主题</label>

                    <div class="controls">
                        <input id="iTitle" name="title" type="text" class="input-xlarge" placeholder="让大家知道干什么">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">任务说明</label>

                    <div class="controls">
                        <textarea id="iContent" name="content" style="height: 100px; width: 400px" placeholder="让大家知道该怎么去干，尽可能详细"></textarea>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="focusedInput">需要外呼</label>

                    <div class="controls">
                        <input id="iNeedOutbound" name="needOutbound" type="checkbox" value="needOutbound" >
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">上传excel</label>

                    <div class="controls">
                        <input id="sourceFile" name="sourceFile" type="file">
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <a href="javascript:iTemplate();" class="btn btn-info">下载模本</a>
                    </div>
                </div>
                <input name="createStart" value="${createStart!}" type="hidden">
                <input name="createEnd" value="${createEnd!}" type="hidden">
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        dater.render();
    });
    function iTemplate() {
        var category = $("#iCategory").val();
        if (blankString(category)) {
            alert("请选择任务分类！");
            return;
        }
        window.location.href = "/task/manage/download_template.vpage?category=" + category;
    }
    function iSave() {
        if (blankString($("#iCategory").val())) {
            alert("请选择任务分类！");
            return;
        }
        if (blankString($("#endTime").val())) {
            alert("请填写截止日期！");
            return;
        }
        if (blankString($("#iTitle").val())) {
            alert("请填写任务主题！");
            return;
        }
        if(stringLength($("#iTitle").val()) > 15){
            alert("任务主题不能超过15个字！");
            return;
        }
        if (blankString($("#iContent").val())) {
            alert("请填写任务说明！");
            return;
        }
        if(stringLength($("#iContent").val()) > 50){
            alert("任务说明不能超过50个字！");
            return;
        }
        var sourceFile = $("#sourceFile").val();
        if (blankString(sourceFile)) {
            alert("请上传excel！");
            return;
        }
        var fileParts = sourceFile.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt != "xls" && fileExt != "xlsx") {
            alert("请上传正确格式的excel！");
            return;
        }
        $("#task-form").submit();
    }
</script>
</@layout_default.page>
