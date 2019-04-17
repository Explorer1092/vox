<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='导入活动兑换码' page_num=19>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 导入兑换码</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <script type="text/javascript">
                ${requestContext.getAlertMessageManager().clearMessages()};
            </script>
            <#--<form method="post" action="/activity_card/importCardAndredeemCode.vpage" enctype="multipart/form-data">-->
                <#--<div class="control-group">-->
                    <#--<div class="controls">-->
                        <#--<input type="file" name="sourceFile">-->
                        <#--<input  type="submit" value="上传" />-->
                        <#--<input type="button" id="download_model" value="下载模板">-->
                    <#--</div>-->
                <#--</div>-->
            <#--</form>-->

            <form id="importRedeem" method="post" enctype="multipart/form-data"
                  action="importlogisticexcel.vpage" data-ajax="false"
                  class="form-horizontal">
                <input id="sourceFile" name="sourceFile" type="file">
                <a id="backImport" href="javascript:iSave();"  role="button" class="btn btn-warning">上传</a>
                <input type="button" id="download_model" value="下载模板">
            </form>

        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        String.prototype.replaceAll = function(s1,s2) {
            return this.replace(new RegExp(s1,"gm"),s2);
        }
        $("#download_model").click(function(){
           window.location.href="/activity_card/import/import_redeem_model.vpage";
        })
    });

    function iSave(){
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

        var formElement = document.getElementById("importRedeem");
        var postData = new FormData(formElement);

        $("#loadingDiv").show();

        $.ajax({
            url: "/activity_card/importCardAndredeemCode.vpage",
            type: "POST",
            data: postData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function (res) {
                $("#loadingDiv").hide();
                if(res.success){
                    alert("导入成功！");
                }else{
                    alert(res.info);
                }
            }
        });
    }
</script>
</@layout_default.page>
