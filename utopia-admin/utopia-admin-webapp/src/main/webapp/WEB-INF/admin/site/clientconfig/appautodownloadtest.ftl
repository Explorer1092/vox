<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Test' page_num=4>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html">
    <legend>Test</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>

                    <div class="control-group">
                        <label class="control-label">产品ID(productId)：</label>

                        <div class="controls">
                            <input type="text" id="productId" name="productId"
                                   <#if patch??>value="${patch.productId!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">区域编码(region)：</label>

                        <div class="controls">
                            <input type="text" id="region" name="region" <#if ver??>value="${ver.region!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝(6位数字)
                        </div>
                    </div>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="addFaqBtn" value="提交" class="btn btn-large btn-primary">
                            <input type="button" id="return" value="返回" onclick="window.location.href='list.vpage?type=${type!''}'"
                                   class="btn btn-large btn-primary">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">response：</label>

                        <div class="controls">
                            <textarea id="content" name="content" style="width: 400px;height: 200px"><#if data??>${data.content!''}</#if></textarea>

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
                region: $("#region").val()
                <#if patch??>, id: '${(patch.id)!''}'</#if>
            };

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

            if (!(verMapper.apkVer == undefined || verMapper.apkVer.trim() == '')) {
                re2 = checkver(verMapper.apkVer.trim())
                if (!re2.tf) {
                    alert("客户端版本号" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.sysVer == undefined || verMapper.sysVer.trim() == '')) {
                re2 = checkSysver(verMapper.sysVer.trim())
                if (!re2.tf) {
                    alert("系统版本号" + re2.ms);
                    return false;
                }
            }

            appPostJson("test.vpage", verMapper, function (data) {
                if (data.success) {
                    $("#content").val(data.content);
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