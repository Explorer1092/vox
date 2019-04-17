<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='客户端版本管理测试' page_num=4>
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
                                   <#if ver??>value="${ver.productId!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">产品名称(productName)：</label>

                        <div class="controls">
                            <input type="text" id="productName" name="productName"
                                   <#if ver??>value="${ver.productName!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK名称(apkName)：</label>

                        <div class="controls">
                            <input type="text" id="apkName" name="apkName" <#if ver??>value="${ver.apkName!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">客户端版本号(apkVer)：</label>

                        <div class="controls">
                            <input type="text" id="apkVer" name="apkVer" <#if ver??>value="${ver.apkVer!''}"</#if>/>
                            (4段，精确到Bulid,例如1.9.9.1004)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">版本标示(androidVerCode)：</label>

                        <div class="controls">
                            <input type="text" id="androidVerCode" name="androidVerCode"
                                   <#if ver??>value="${ver.androidVerCode!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">渠道号(channel)：</label>

                        <div class="controls">
                            <input type="text" id="channel" name="channel" <#if ver??>value="${ver.channel!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">SDK版本(sdkVer)：</label>

                        <div class="controls">
                            <input type="text" id="sdkVer" name="apkVer" <#if ver??>value="${ver.sdkVer!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">系统版本(sysVer)：</label>

                        <div class="controls">
                            <input type="text" id="sysVer" name="sysVer" <#if ver??>value="${ver.sysVer!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机厂商(brand)：</label>

                        <div class="controls">
                            <input type="text" id="brand" name="brand" <#if ver??>value="${ver.brand!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机型号(model)：</label>

                        <div class="controls">
                            <input type="text" id="model" name="model" <#if ver??>value="${ver.model!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">区域编码(region)：</label>

                        <div class="controls">
                            <input type="text" id="region" name="region" <#if ver??>value="${ver.region!''}"</#if>/>
                            (6位数字)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">学校ID(school)：</label>

                        <div class="controls">
                            <input type="text" id="school" name="school" <#if ver??>value="${ver.school!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">班级ID(clazz)：</label>

                        <div class="controls">
                            <input type="text" id="clazz" name="clazz" <#if ver??>value="${ver.clazz!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">年级(clazzLevel)：</label>

                        <div class="controls">
                            <input type="text" id="clazzLevel" name="clazzLevel"
                                   <#if ver??>value="${ver.clazzLevel!''}"</#if>/>
                            (范围1~6)
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label">用户ID(user)：</label>

                        <div class="controls">
                            <input type="text" id="user" name="user" <#if ver??>value="${ver.user!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">用户身份(userType)：</label>

                        <div class="controls">
                            <select id="userType" name="userType">
                                <option value="">请选择</option>
                                <option  <#if ver??><#if ver.userType=="1">selected="selected" </#if></#if>  value="1">
                                    老师
                                </option>
                                <option  <#if ver??><#if ver.userType=="2">selected="selected" </#if></#if>  value="2">
                                    家长
                                </option>
                                <option  <#if ver??><#if ver.userType=="3">selected="selected" </#if></#if>  value="3">
                                    学生
                                </option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机号码(mobile)：</label>
                        <div class="controls">
                            <input type="text" id="mobile" name="mobile" <#if ver??>value="${ver.mobile!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机IMEI号码(imei)：</label>
                        <div class="controls">
                            <input type="text" id="imei" name="imei" <#if ver??>value="${ver.imei!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="addFaqBtn" value="提交" class="btn btn-large btn-primary">
                            <input type="button" id="return" value="返回" onclick="window.location.href='list.vpage';" class="btn btn-large btn-primary">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">response：</label>

                        <div class="controls">
                            <textarea id="content" name="content" style="width: 70%;height: 300px;"><#if data??>${data.content!''}</#if></textarea>
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
                productId: $("#productId").val(),
                apkVer: $("#apkVer").val(),
                androidVerCode: $("#androidVerCode").val(),
                channel: $("#channel").val(),
                sdkVer: $("#sdkVer").val(),
                sysVer: $("#sysVer").val(),
                region: $("#region").val(),
                brand: $("#brand").val(),
                model: $("#model").val(),
                school: $("#school").val(),
                clazz: $("#clazz").val(),
                clazzLevel: $("#clazzLevel").val(),
                user: $("#user").val(),
                userType: $("#userType").val(),
                mobile: $("#mobile").val(),
                imei: $("#imei").val(),
                productName: $("#productName").val(),
                apkName: $("#apkName").val()
                <#if ver??>, id: '${(ver.id)!''}'</#if>
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


            if (!(verMapper.androidVerCode == undefined || verMapper.androidVerCode.trim() == '')) {
                re2 = checknum(verMapper.androidVerCode.trim())
                if (!re2.tf) {
                    alert("版本标示" + re2.ms);
                    return false;
                }
            }


            if (!(verMapper.channel == undefined || verMapper.channel.trim() == '')) {
                re2 = checknum(verMapper.channel.trim());
                if (!re2.tf) {
                    alert("渠道号" + re2.ms);
                    return false;
                }
            }

//        if (!(verMapper.sdkVer == undefined || verMapper.sdkVer.trim() == '')) {
//            re2 = checknum(verMapper.sdkVer.trim());
//            if (!re2.tf) {
//                alert("SDK版本" + re2.ms);
//                return false;
//            }
//        }

            if (!(verMapper.region == undefined || verMapper.region.trim() == '')) {
                if (!checksix(verMapper.region.trim())) {
                    alert("区域编码有误");
                    return false;
                }
            }

            if (!(verMapper.school == undefined || verMapper.school.trim() == '')) {
                re2 = checknum(verMapper.school.trim());
                if (!re2.tf) {
                    alert("学校ID" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.clazz == undefined || verMapper.clazz.trim() == '')) {
                re2 = checknum(verMapper.clazz.trim());
                if (!re2.tf) {
                    alert("班级ID" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.clazzLevel == undefined || verMapper.clazzLevel.trim() == '')) {
                re2 = checknumclazzLevel(verMapper.clazzLevel.trim());
                if (!re2.tf) {
                    alert("年级" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.user == undefined || verMapper.user.trim() == '')) {
                re2 = checknum(verMapper.user.trim());
                if (!re2.tf) {
                    alert("用户ID" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.mobile == undefined || verMapper.mobile.trim() == '')) {
                re2 = checknum(verMapper.mobile.trim());
                if (!re2.tf) {
                    alert("手机号码" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.imei == undefined || verMapper.imei.trim() == '')) {
                re2 = checknum(verMapper.imei.trim());
                if (!re2.tf) {
                    alert("手机IMEI码" + re2.ms);
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

    function checkrange(str) {
        re = false;
        l = str.indexOf(":");
        content1 = "";
        content2 = "";
        if (l > 0) {
            content1 = str.substring(0, l);
            content2 = str.substring(l + 1);
            re = true;
        }
        return {"tf": re, "l": l, "content1": content1, "content2": content2};
    }
    function checkrangetime(str) {
        re = false;
        message = "时间格式有误";
        var regd = /^\d{4}\.\d{2}\.\d{2} \d{2}:\d{2}:\d{2}:\d{4}\.\d{2}\.\d{2} \d{2}:\d{2}:\d{2}$/;
        if (regd.test(str)) {
            re = true;
            message = "";
        }
        return {"tf": re, "ms": message};
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

    function checknumclazzLevel(str) {
        re = true;
        message = "";
        if (str.length > 1) {
            re = false;
            message = "年级数字过大";
        } else {
            if (str < "1" || str > "6") {
                re = false;
                message = "年级应为1~6数字";
            }
        }

        return {"tf": re, "ms": message};
    }

    function checksix(str) {
        var reg6 = /\d{6}/;
        if (reg6.test(str)) return true;
        else return false;
    }

    function checkdate(str) {
        var regd = /^\d{4}\.\d{2}\.\d{2} \d{2}:\d{2}:\d{2}$/;
        re = false;
        message = "时间格式错误";
        if (regd.test(str)) {
            re = true;
            message = "";
        }
        return {"tf": re, "ms": message};
    }


</script>
</@layout_default.page>