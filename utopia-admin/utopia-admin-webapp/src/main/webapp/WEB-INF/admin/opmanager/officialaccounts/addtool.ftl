<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加工具栏" page_num=16>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加工具栏
        <a type="button" id="btn_cancel" href="toollist.vpage?accountId=${accountId!0}" name="btn_cancel"
           class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_btn" class="btn btn-primary" value="保存"/>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" name="detail_form" action="addtool.vpage" method="post">
                    <input id="accountId" name="accountId" value="${accountId!0}" type="hidden">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">工具栏名称</label>
                            <div class="controls">
                                <input type="text" id="toolName" maxlength="5" name="toolName" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">工具栏URL</label>
                            <div class="controls">
                                <input type="text" id="toolUrl" name="toolUrl" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否拼接SID</label>
                            <div class="controls">
                                <input type="checkbox" id="bindSid" name="bindSid" />
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $('#frm').on('submit',function () {
            $('#frm').ajaxSubmit({
                type: 'post',
                url: 'addtool.vpage',
                success : function(data) {
                    if (data.success) {
                        alert("保存成功");
                        window.location.href = 'toollist.vpage?accountId=${accountId!0}';
                    } else {
                        alert(data.info);
                    }
                },
                error: function() {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_btn').on('click', function () {
            var detail = {
                accountId: $('#accountId').val(),
                toolName: $('#toolName').val().trim(),
                toolUrl: $('#toolUrl').val().trim(),
                bindSid: $('#bindSid').is(':checked')
            };
            if (validateInput(detail)) {
                if (confirm("是否确认保存？")) {
                    $('#frm').submit();
                }
            }
        });
        function validateInput(detail) {
            var msg = "";
            if (detail.toolName == '') {
                msg += "请输工具栏名称！\n";
            }
            if (detail.toolUrl == '') {
                msg += "请输工具栏URL！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        }
    });
</script>
</@layout_default.page>