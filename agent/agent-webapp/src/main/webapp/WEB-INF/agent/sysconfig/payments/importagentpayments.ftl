<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='学校字典表导入' page_num=6>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在上传，请等待……</p>
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
            <h2><i class="icon-th"></i> 业务结算数上传</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="javascript:iSave();" class="btn btn-success"><i class="icon-plus icon-white"></i>提交</a>&nbsp;&nbsp;
            </div>
        </div>

        <div class="box-content ">
            <form id="importPerformanceConfig" method="post" enctype="multipart/form-data"
                  action="/sysconfig/payments/import_agent_payments.vpage" data-ajax="false"
                  class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">模版类别</label>
                    <div class="controls">
                        <input id="payments-type-1" <#if type==1> checked</#if> name="type"
                               type="radio"
                               value="1">
                        <label for="payments-type-1" style="display:inline">大区经理结算指标</label>
                        <input id="payments-type-2" <#if type==2> checked</#if> name="type"
                               type="radio"
                               value="2">
                        <label for="payments-type-2" style="display:inline">代理结算指标</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">上传excel</label>
                    <div class="controls">
                        <input id="sourceFile" name="sourceFile" type="file">
                        <a href="javascript:downloadTemplent()" class="btn btn-primary">下载导入模版</a>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
    function iSave() {
        $("div.alert-info").hide();
        $("div.alert-error").hide();
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

        var formElement = document.getElementById("importPerformanceConfig");
        var postData = new FormData(formElement);

        $("#loadingDiv").show();

        $.ajax({
            url: "import_agent_payments.vpage",
            type: "POST",
            data: postData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function (res) {
                $("#loadingDiv").hide();
                if (res.success) {
                    var right = res.right;
                    setInfo(right, "alert-info", "info-panel");
                    alert(getInfoNoBr(right));
                } else {
                    var right = res.right;
                    setInfo(right, "alert-info", "info-panel");
                    var error = res.error;
                    setInfo(error, "alert-error", "error-panel");
                }
            },
            error: function (e) {
                console.log(e);
                $("#loadingDiv").hide();
            }
        });
    }
    function setInfo(info, classEle, idEle) {
        resInfo = getInfo(info);
        if (resInfo) {
            $("div." + classEle).show();
            $("#" + idEle).html(resInfo);
        }
    }

    function getInfoNoBr(info) {
        if (info) {
            var res = "";
            info.forEach(function (e) {
                res += (e + ",");
            });
            return res;
        }
        return false;
    }

    function getInfo(info) {
        if (info) {
            var res = "";
            info.forEach(function (e) {
                res += (e + "<br/>");
            });
            return res;
        }
        return false;
    }

    function downloadTemplent() {
        var type = $("input[name=type]:checked").val();
        window.location.href = "import_agent_payments_template.vpage?type=" + type;
    }
</script>
</@layout_default.page>