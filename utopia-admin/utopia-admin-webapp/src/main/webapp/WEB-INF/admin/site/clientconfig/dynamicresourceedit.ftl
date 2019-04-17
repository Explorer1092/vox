<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Add Or Edit' page_num=4>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html">
    <legend>动态资源配置编辑</legend>
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
                        <label class="control-label">系统版本(sysVer)：</label>

                        <div class="controls">
                            <input type="text" id="sysVer" name="sysVer" <#if patch??>value="${patch.sysVer!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机型号(model)：</label>

                        <div class="controls">
                            <input type="text" id="model" name="model" <#if patch??>value="${patch.model!''}"</#if>/>
                            <span style="color: red">*</span>=、!=
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">用户ID(user)：</label>

                        <div class="controls">
                            <input type="text" id="user" name="user" <#if patch??>value="${patch.user!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">是否启用(isOpen)：</label>

                        <div class="controls">
                            <input type="radio" name="isOpen" value="true" <#if patch?? && patch.isOpen?? && patch.isOpen> checked="checked" </#if>/>启用
                            <input type="radio" name="isOpen" value="false" <#if !patch?? || !patch.isOpen?? || !patch.isOpen> checked="checked" </#if>/>禁用
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">备注(comment)：</label>

                        <div class="controls">
                            <input type="text" id="comment" name="comment" <#if patch??>value="${patch.comment!''}"</#if>/>
                        </div>
                    </div>

                    <hr>

                    <div class="control-group">
                        <label class="control-label">应答内容(response)</label>
                    </div>

                    <div class="control-group">
                        <label class="control-label">资源类型(resourceType)：</label>
                        <div class="controls">
                            <select id="apkVerNew" name="apkVerNew">
                                <option <#if patch?? && (patch.response.apkVer?string)=="resource_name">selected="selected"</#if>
                                        value="resource_name">resource_name
                                </option>
                                <option <#if patch?? && (patch.response.apkVer?string)=="resource_old">selected="selected"</#if>
                                        value="resource_old">resource_old
                                </option>
                                <option <#if patch?? && (patch.response.apkVer?string)=="resource_path">selected="selected"</#if>
                                        value="resource_path">resource_path
                                </option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">资源大小(resourceSize)：</label>
                        <div class="controls">
                            <input type="text" id="apkSize" name="apkSize" <#if patch??>value="${patch.response.apkSize!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">资源下载url(resourceUrl)：</label>
                        <div class="controls">
                            <input type="text" id="apkUrl" name="apkUrl" <#if patch??>value="${patch.response.patchUrl!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">MD5码(resourceMD5)：</label>
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
<script type="text/javascript">
    $(function () {

        $("input.check_all").on("click", function () {
            $("input.check_single").prop('checked', $(this).prop('checked'));
            if ($(this).prop('checked')) {
                var schools = $(this).val();
                $("input.check_all").attr("check_values", schools.toString());
            } else {
                $("input.check_all").attr("check_values", "");
            }
        });

        $("input.check_single").on("click", function () {
            if ($("input.check_single").size() == $("input.check_single:checked").size()) {
                $("input.check_all").prop('checked', true);

            } else {
                $("input.check_all").prop('checked', false);
            }
            var check_values = [];
            $("input.check_single:checked").each(function() {
                check_values.push($(this).val());
            });
            var schools = check_values.join("&");
            $("input.check_all").attr("check_values", schools.toString());
        });

        $("#addFaqBtn").on("click", function () {
            var schools = $("input.check_all").attr("check_values");
            var verMapper = {
                type:'${type!''}',
                productId: $("#productId").val(),
                apkVer: $("#apkVer").val(),
                sysVer: $("#sysVer").val(),
                model: $("#model").val(),
                ktwelve: schools,
                user: $("#user").val(),
                region: $("#region").val(),
                patchUrl: $("#apkUrl").val(),
                patchMD5: $("#apkMD5").val(),
                apkSize: $("#apkSize").val(),
                apkVerNew:$("#apkVerNew").val(),
                isOpen: $('input:radio[name=isOpen]:checked').val(),
                comment: $('#comment').val(),
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

            if (!(verMapper.model == undefined || verMapper.model.trim() == '')) {
                re1 = checksign(verMapper.model.trim(), signpart);
                if (!re1.tf) {
                    alert("手机型号" + re1.ms);
                    return false;
                }
            }
            appPostJson("addconfig.vpage", verMapper, function (data) {
                if (data.success) {
                    location.href = "list.vpage?type=DYNAMIC_APP_RESOURCE";
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