<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='中秋活动诗词导入' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        中秋活动-诗词导入
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal">
                <ul class="inline">
                    <li>
                        <label>选择EXCEL
                            <input type="file" id="poetry_file" name="poetry_file"
                                   placeholder="诗词文件"/>
                        </label>
                    </li>
                    <li>
                        <button type="button" id="btn_upload" class="btn btn-primary">导入</button>
                    </li>
                </ul>
            </form>
        </div>
    </div>
</div>
<script language="javascript" type="application/javascript">
    $(document).ready(function () {
        $("#btn_upload").click(function () {

            var control = $("#poetry_file");

            if (control.val() == "") {
                alert("请选择要上传的文件");
                return;
            }

            var file = control.prop("files")[0];
            var formData = new FormData();
            formData.append('inputFile', file);
            $.ajax({
                url: 'batch.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        alert("上传成功");
                    } else {
                        alert("上传失败");
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>