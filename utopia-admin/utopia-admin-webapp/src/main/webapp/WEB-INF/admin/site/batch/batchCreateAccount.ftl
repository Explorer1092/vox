<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">

    <@h.head/>

    <fieldset>
        <legend>批量创建家长和学生账号</legend>
        <div class="modal-body">
            请上传要创建账号的手机号:<input type="file"  accept=".xls, .xlsx" id="importMobileExcel" />
            <br>
            <button class="btn" id="uploadMobileBtn">提交</button>
            <br><br>
            模板下载:
            <a href="/site/batch/downloadExample.vpage">手机号填写模板</a>
        </div>

        <br />
        <ul id="preview_fails" style="color: red;"></ul>
    </fieldset>
</div>

<script type="text/javascript">
    $(function(){
        $('#uploadMobileBtn').on('click', function () {
            if ($("#uploadMobileBtn").attr("disabled")) {
                return;
            }
            var file = $('#importMobileExcel')[0].files[0];
            if (!file){
                alert('请先上传文件哦~');
                return ;
            }
            if (['.xls', '.xlsx'].indexOf(file.name.substring(file.name.lastIndexOf('.'))) === -1) {
                alert('你选择的文件格式有误哦~');
                return ;
            }
            $("#uploadMobileBtn").attr("disabled", true).text("账号生成中,账号生成完成会发送到您的邮箱，请稍等...");
            var uploadMobileFormData = new FormData();
            uploadMobileFormData.append('file', file);
            $.ajax({
                url: '/site/batch/batchCreateAccount.vpage',
                type: 'POST',
                data: uploadMobileFormData,
                processData: false,
                contentType: false,
                async: true,
                timeout: 0,
                success: function (res) {
                    if (res.success) {
                        alert('创建成功');
                        //下载报告
                        var filePath = res.filePath;
                        var requestUrl = "/site/batch/downReport.vpage?filePath=" + filePath + "&fileName=新账号.xlsx";
                        var downloadIframe = "<iframe style='display:none;' src=" + requestUrl + "/>";
                        $("body").append(downloadIframe);
                    } else {
                        alert(res.info);
                    }
                    $("#uploadMobileBtn").attr("disabled", false).text("提交");
                }
            })
        });

    });
</script>
</@layout_default.page>