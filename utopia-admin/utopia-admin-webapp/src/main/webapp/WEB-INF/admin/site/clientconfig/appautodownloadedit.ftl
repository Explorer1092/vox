<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Add Or Edit' page_num=4>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html">
    <legend>Add Or Edit</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">控制字段</label>
                        <label class="control-label">(多选字段用&分隔)</label>
                    </div>

                    <div class="control-group">
                        <label class="control-label">产品ID(productId)：</label>

                        <div class="controls">
                            <input type="text" id="productId" name="productId"
                                   <#if patch??>value="${patch.productId!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">客户端版本号(apkVer)：</label>

                        <div class="controls">
                            <input type="text" id="apkVer" name="apkVer"
                                   <#if patch??>value="${patch.apkVer!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:(4段，精确到Bulid,例如1.9.9.1004)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">区域编码(region)：</label>

                        <div class="controls">
                            <input type="text" id="region" name="region" <#if patch??>value="${patch.region!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝(6位数字)
                        </div>
                    </div>

                    <hr>

                    <div class="control-group">
                        <label class="control-label">应答内容(response)</label>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK版本号(apkVer)：</label>
                        <div class="controls">
                            <input type="text" id="apkVerNew" name="apkVerNew" <#if patch??>value="${patch.response.apkVer!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK大小(apkSize)：</label>
                        <div class="controls">
                            <input type="text" id="apkSize" name="apkSize" <#if patch??>value="${patch.response.apkSize!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK下载url(apkUrl)：</label>
                        <div class="controls">
                            <input type="text" id="apkUrl" name="apkUrl" <#if patch??>value="${patch.response.patchUrl!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK文件MD5(apkMD5)：</label>
                        <div class="controls">
                            <input type="text" id="apkMD5" name="apkMD5" <#if patch??>value="${patch.response.patchMD5!''}"</#if>/>
                        </div>
                    </div>

                    <input type="hidden" id="status" name="status"  <#if patch??>value="${patch.status!'draft'}" </#if>/>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="addFaqBtn" value="提交" class="btn btn-large btn-primary">
                            <input type="button" id="return" value="返回" onclick="window.location.href='list.vpage?type=${type!''}'"
                                   class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
<div id="add_dialog" class="modal fade hide" style="width: 60%; left: 40%;">
    <div class="modal-dialog">
        <div class="modal-content" style="padding: 10px 10px;">


        </div>
    </div>
</div>


<script type="text/javascript">
    $(function () {

        $("#addFaqBtn").on("click", function () {
            var verMapper = {
                type:'${type!''}',
                productId: $("#productId").val(),
                apkVer: $("#apkVer").val(),
                region: $("#region").val(),
                apkVerNew: $("#apkVerNew").val(),
                apkSize: $("#apkSize").val(),
                patchUrl: $("#apkUrl").val(),
                patchMD5: $("#apkMD5").val(),
                status: $("#status").val()
                <#if patch??>, id: '${(patch.id)!''}'</#if>
            };

            var signall = ["<=", ">=", "!=", "<", ">", "="];
            var signpart = ["=", "!="];
            var signtime = [">=", "<="];

            var reg = /^[0-9]+$/;
            if (verMapper.productId == undefined || verMapper.productId.trim() == '') {
                alert("请输入产品ID");
                return false;
            }
            else {
                re = checknum(verMapper.productId.trim());
                if (!re.tf) {
                    alert("产品ID" + re.ms);
                    return false;
                }
            }

            appPostJson("addconfig.vpage", verMapper, function (data) {
                if (data.success) {
                    location.href = "list.vpage?type=" + "APP_DOWNLOAD";
                } else {
                    alert(data.info);
                }
            });

        });

    });
    function appPostJson(url, data, callback, error, dataType) {
        dataType = dataType || "json";
        return $.ajax({
            type: 'post',
            url: url,
            data: JSON.stringify(data),
            success: callback,
            error: error,
            dataType: dataType,
            contentType: 'application/json;charset=UTF-8'
        });

    }

    function checksign(str, sign) {
        re = false;
        message = "起始符号有误";
        content = "";
        for (var i = 0; i < sign.length; i++) {
            if (str.indexOf(sign[i]) == 0) {
                re = true;
                message = "";
                if (sign.length > 2) {
                    if (i > 2) content = str.substring(1);
                    else content = str.substring(2);
                }
                else {
                    if (i == 0) content = str.substring(1);
                    else content = str.substring(2);
                }
                break;
            }

        }
        return {"tf": re, "ms": message, "content": content};
    }

    function checksigntime(str, sign) {
        re = false;
        message = "起始符号有误";
        content = "";
        for (var i = 0; i < sign.length; i++) {
            if (str.indexOf(sign[i]) == 0) {
                re = true;
                message = "";
                content = str.substring(2);
                break;
            }

        }
        return {"tf": re, "ms": message, "content": content};
    }

    function checkver(str) {
        re = false;
        var regx = /\d+\.\d+\.\d+\.\d+/;
        message = "版本格式有误";
        if (regx.test(str)) {
            re = true;
            message = "";
        }
        return {"tf": re, "ms": message};
    }

    function checkSysver(str) {
        for (var i = 0; i < str.length; i++) {
            if ((str[i] < "0" || str[i] > "9") && str[i] != ".") {
                return {"tf": false, "ms": "版本格式有误"};
            }
        }
        return {"tf": true, "ms": ""};
    }

    function checknum(str) {
        re = true;
        message = "";
        for (i = 0; i < str.length; i++) {
            if (str[i] < "0" || str[i] > "9") {
                re = false;
                message = "含非数字内容";
                break;
            }
        }
        return {"tf": re, "ms": message};
    }


</script>
</@layout_default.page>