<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='编辑客户端版本' page_num=4>
<style>
    .uploader {  position: relative;  display: inline-block;  background: #D0EEFF;  border: 1px solid #99D3F5;  border-radius: 4px;  padding: 4px 12px;  overflow: hidden;  color: #1E88C7;  text-decoration: none;  text-indent: 0;  line-height: 20px;  }
    .uploader input {  position: absolute;  font-size: 100px;  right: 0;  top: 0;  opacity: 0;  }
    .uploader:hover {  background: #AADFFD;  border-color: #78C3F3;  color: #004974;  text-decoration: none;  }
</style>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html">
    <legend>编辑客户端版本</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">控制字段</label>
                        <label class="control-label">(多选字段用&分隔)</label>
                    </div>
                    <input type="hidden" id="verId" name="verId"  value="<#if ver??>${(ver.id)!''}</#if>" />
                    <div class="control-group">
                        <label class="control-label">产品ID(productId)：</label>
                        <div class="controls">
                            <input type="text" id="productId" name="productId" <#if ver??>value="${ver.productId!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">优先级(rank)：</label>
                        <div class="controls">
                            <input type="text" id="rank" name="rank" value="<#if ver??>${ver.rank!''}</#if>"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">客户端版本号(apkVer)：</label>
                        <div class="controls">
                            <input type="text" id="apkVerOld" name="apkVerOld" <#if ver??>value="${ver.apkVer!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:(4段，精确到Bulid,例如1.9.9.1004)
                        </div>
                    </div>

                    <#--<div class="control-group">-->
                        <#--<label class="control-label">版本标识(androidVerCode)：</label>-->
                        <#--<div class="controls">-->
                            <#--<input type="text" id="androidVerCode" name="androidVerCode"-->
                                   <#--<#if ver??>value="${ver.androidVerCode!''}"</#if>/>-->
                            <#--<span style="color: red">*</span>=、!=、>、<、>＝、<＝、:-->
                        <#--</div>-->
                    <#--</div>-->

                    <div class="control-group">
                        <label class="control-label">渠道号(channel)：</label>
                        <div class="controls">
                            <input type="text" id="channel" name="channel" <#if ver??>value="${ver.channel!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">SDK版本(sdkVer)：</label>
                        <div class="controls">
                            <input type="text" id="sdkVer" name="sdkVer" <#if ver??>value="${ver.sdkVer!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">系统版本(sysVer)：</label>
                        <div class="controls">
                            <input type="text" id="sysVer" name="sysVer" <#if ver??>value="${ver.sysVer!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机厂商(brand)：</label>
                        <div class="controls">
                            <input type="text" id="brand" name="brand" <#if ver??>value="${ver.brand!''}"</#if>/>
                            <span style="color: red">*</span>=、!=
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手机型号(model)：</label>
                        <div class="controls">
                            <input type="text" id="model" name="model" <#if ver??>value="${ver.model!''}"</#if>/>
                            <span style="color: red">*</span>=、!=
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
                        <label class="control-label">时间段(time)：</label>
                        <div class="controls">
                            <input type="text" id="time" name="time" <#if ver??>value="${ver.time!''}"</#if>/>
                            <span style="color: red">*</span>>=、<=、:(YYYY-MM-MM HH:MM:SS,例如:2011-01-01 00:00:00,时间段用#分隔)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">账号异常类型(accountStatus)：</label>
                        <div class="controls">
                            <input type="checkbox" class="account_status_all" value="ALL" <#if ver?? && ver.accountStatus?? && (ver.accountStatus?string)=="ALL">checked="checked" account_status="${ver.accountStatus}"</#if>>全选
                            <input type="checkbox" class="account_status_single" value="FREEZING" <#if ver?? && ver.accountStatus?? && ((ver.accountStatus?string)=="FREEZING" || (ver.accountStatus?string)=="ALL")>checked="checked"</#if>>冻结
                            <input type="checkbox" class="account_status_single" value="FORBIDDEN" <#if ver?? && ver.accountStatus?? && ((ver.accountStatus?string)=="FORBIDDEN" || (ver.accountStatus?string)=="ALL")>checked="checked"</#if>>封禁
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">学段(ktwelve)：</label>
                        <div class="controls">
                            <input type="checkbox" class="check_all" value="PRIMARY_SCHOOL&JUNIOR_SCHOOL&INFANT" <#if ver?? && ver.ktwelve?? && (ver.ktwelve?string)=="PRIMARY_SCHOOL&JUNIOR_SCHOOL&INFANT">checked="checked" check_values="${ver.ktwelve}"</#if>>全选
                            <input type="checkbox" class="check_single"  value="PRIMARY_SCHOOL" <#if ver?? && ver.ktwelve?? && ((ver.ktwelve?string)?index_of("PRIMARY_SCHOOL")!=-1)>checked="checked"</#if>>小学
                            <input type="checkbox" class="check_single" value="JUNIOR_SCHOOL" <#if ver?? && ver.ktwelve?? && ((ver.ktwelve?string)?index_of("JUNIOR_SCHOOL")!=-1)>checked="checked"</#if>>初中
                            <input type="checkbox" class="check_single" value="INFANT" <#if ver?? && ver.ktwelve?? && ((ver.ktwelve?string)?index_of("INFANT")!=-1)>checked="checked"</#if>>学前
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">学校ID(school)：</label>
                        <div class="controls">
                            <input type="text" id="school" name="school" <#if ver??>value="${ver.school!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">班级ID(clazz)：</label>
                        <div class="controls">
                            <input type="text" id="clazz" name="clazz" <#if ver??>value="${ver.clazz!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">年级(clazzLevel)：</label>
                        <div class="controls">
                            <input type="text" id="clazzLevel" name="clazzLevel" <#if ver??>value="${ver.clazzLevel!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝(范围1~9,学前:51-54)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">用户ID(user)：</label>
                        <div class="controls">
                            <input type="text" id="user" name="user" <#if ver??>value="${ver.user!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">用户身份(userType)：</label>
                        <div class="controls">
                            <select id="userType" name="userType">
                                <option value="">请选择</option>
                                <option <#if ver??><#if ver.userType=="=1">selected="selected" </#if></#if> value="=1">老师</option>
                                <option <#if ver??><#if ver.userType=="=2">selected="selected" </#if></#if> value="=2">家长</option>
                                <option <#if ver??><#if ver.userType=="=3">selected="selected" </#if></#if> value="=3">学生</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">手动升级(isManual)：</label>
                        <div class="controls">
                            <select id="isManual" name="isManual">
                                <option <#if ver??><#if (ver.isManual!false)?string=="true">selected="selected" </#if></#if> value="true">是</option>
                                <option <#if ver??><#if (ver.isManual!false)?string=="false">selected="selected" </#if></#if> value="false">否</option>
                            </select>
                        </div>
                    </div>

                    <#--<div class="control-group">-->
                        <#--<label class="control-label">手机号码(mobile)：</label>-->
                        <#--<div class="controls">-->
                            <#--<input type="text" id="mobile" name="mobile" <#if ver??>value="${ver.mobile!''}"</#if>/>-->
                            <#--<span style="color: red">*</span>=、!＝-->
                        <#--</div>-->
                    <#--</div>-->

                    <div class="control-group">
                        <label class="control-label">手机IMEI号码(imei)：</label>
                        <div class="controls">
                            <input type="text" id="imei" name="imei" <#if ver??>value="${ver.imei!''}"</#if>/>
                            <span style="color: red">*</span>=、!＝
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">宿主AP产品ID：</label>
                        <div class="controls">
                            <input type="text" id="ownerAppPid" name="ownerAppPid" <#if ver??>value="${ver.ownerAppPid!''}"</#if>/>
                            <span>安卓插件升级需要配置</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">宿主AP版本：</label>
                        <div class="controls">
                            <input type="text" id="ownerAppApkVer" name="ownerAppApkVer" <#if ver??>value="${ver.ownerAppApkVer!''}"</#if>/>
                            <span style="color: red">*</span>=、!=、>、<、>＝、<＝、:(4段，精确到Bulid,例如1.9.9.1004)， 安卓插件升级需要配置
                        </div>
                    </div>

                    <#--<div class="control-group">-->
                        <#--<label class="control-label">百分比(rate)：</label>-->
                        <#--<div class="controls">-->
                            <#--<input type="text" id="rate" name="rate" <#if ver??>value="${ver.rate!''}"</#if>/>-->
                            <#--(基数为1000，例如100表示100/1000=10%)-->
                        <#--</div>-->
                    <#--</div>-->

                    <#--<div class="control-group">-->
                        <#--<label class="control-label">数量(count)：</label>-->
                        <#--<div class="controls">-->
                            <#--<input type="text" id="count" name="count" <#if ver??>value="${ver.count!''}"</#if>/>-->
                        <#--</div>-->
                    <#--</div>-->

                    <div class="control-group">
                        <label class="control-label">应答内容(response)</label>
                    </div>

                    <div class="control-group">
                        <label class="control-label">产品Id(productId)：</label>
                        <div class="controls">
                            <input type="text" id="productIdNew" name="productIdNew" <#if ver??>value="${ver.response.productId!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK版本号(apkVer)：</label>

                        <div class="controls">
                            <input type="text" id="apkVerNew" name="apkVerNew"
                                   <#if ver??>value="${ver.response.apkVer!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK大小(apkSize)：</label>
                        <div class="controls">
                            <input type="text" id="apkSize" name="apkSize" <#if ver??>value="${ver.response.apkSize!''}"</#if>/>
                            (例如： 780KB，1.5MB)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK下载URL(apkUrl)：</label>
                        <div class="controls">
                            <input type="text" id="apkUrl" name="apkUrl" style="width: 400px" <#if ver??>value="${ver.response.apkUrl!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">APK文件MD5(apkMD5)：</label>
                        <div class="controls">
                            <input type="text" id="apkMD5" name="apkMD5" style="width: 400px" <#if ver??>value="${ver.response.apkMD5!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">升级类型(upgradeType)：</label>
                        <div class="controls">
                            <select id="upgradeType" name="upgradeType">
                                <option <#if ver??><#if ver.response.upgradeType=="1">selected="selected" </#if></#if> value="1">普通升级</option>
                                <option <#if ver??><#if ver.response.upgradeType=="0">selected="selected" </#if></#if> value="0">强制升级</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">升级说明(description)：</label>
                        <div class="controls">
                            <textarea id="description" name="description" style="width: 400px;height: 200px"><#if ver??>${ver.response.description!''}</#if></textarea>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">升级配图(image)：</label>
                        <div class="controls">
                            <div style="height: 200px; width:300px; margin-bottom:2px;">
                                <img id="imgSrc" src="<#if (ver.response.image)?? && (ver.response.image)?has_content>${(ver.response.image)}</#if>" style="width:100%;height: 100%;"/>
                            </div>
                            <a href="javascript:void(0);" class="uploader">
                                <input type="file" name="file" id="img-file" accept="image/gif, image/jpeg, image/png, image/jpg" onchange="previewImg(this)" /> 请选择图片
                            </a>
                            <a title="确认上传" href="javascript:void(0);" class="uploader" id="img-upload"><i class="icon-ok"></i> 确 定 </a>
                            <#if (ver.response.image)?? && (ver.response.image)?has_content>
                                <a title="删除" href="javascript:void(0);" class="uploader" id="img-clear"><i class="icon-trash"></i> 删 除 </a>
                            </#if>
                            <input id="image" name="image" type="hidden" value="<#if ver??>${(ver.response.image)!''}</#if>"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">升级间隔(updateTime)：</label>
                        <div class="controls">
                            <input type="text" id="updateTime" name="updateTime" <#if ver??>value="${ver.response.updateTime!''}"</#if>/>
                            (单位：小时)
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">要升级的包的包名(Android用)：</label>
                        <div class="controls">
                            <input type="text" id="packageName" name="packageName" style="width: 400px" <#if ver??>value="${ver.response.packageName!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">要升级的包的scheme(IOS用)：</label>
                        <div class="controls">
                            <input type="text" id="scheme" name="scheme" style="width: 400px" <#if ver??>value="${ver.response.scheme!''}"</#if>/>
                        </div>
                    </div>

                    <input type="hidden" id="status" name="status"  <#if ver??>value="${ver.status!'draft'}" </#if>/>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="addFaqBtn" value="提交" class="btn btn-large btn-primary">
                            <input type="button" id="return" value="返回" onclick="window.history.back();" class="btn btn-large btn-primary">
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

        //账号状态选择
        $("input.account_status_all").on("click", function () {
            $("input.account_status_single").prop('checked', $(this).prop('checked'));
            if ($(this).prop('checked')) {
                var accountStatus = $(this).val();
                $("input.account_status_all").attr("account_status", accountStatus.toString());
            }else{
                $("input.account_status_all").attr("account_status", "");
            }
        });

        $("input.account_status_single").on("click", function () {
            if ($("input.account_status_single").size() == $("input.account_status_single:checked").size()) {
                $("input.account_status_all").prop('checked', true);
                $("input.account_status_all").attr("account_status", "ALL");
            } else {
                $("input.account_status_all").prop('checked', false);
                var accountStatus = $("input.account_status_single:checked").val();
                $("input.account_status_all").attr("account_status", accountStatus.toString());
            }
        });

        // 上传、清除素材
        $("#img-upload").on('click', function () {
            var file = $('#img-file')[0].files[0];
            if (file.size >= 2*1024*1024) {
                alert("图片大小不要超过2MB");
                return false;
            }
            if(file.type.indexOf('image') == -1) {
                alert("图片类型错误！");
                return false;
            }
            var formData = new FormData();
            formData.append('file', file);
            $.ajax({
                url: 'uploadimage.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        alert("图片上传成功！");
                        $('#image').val(data.fileName);
                    } else {
                        alert("图片上传失败:" + data.info);
                    }
                },
                error: function (msg) {
                    alert("图片上传失败！");
                }
            });
        });


        $("#img-clear").on('click', function () {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            $.post('clearimage.vpage', {verId: $('#verId').val()}, function (data) {
                if (!data.success) {
                    alert("清除图片失败:" + data.info);
                }
                window.location.reload();
            });
        });


        $("#addFaqBtn").on("click", function () {
            var schools = $("input.check_all").attr("check_values");
            var accountStatus = $("input.account_status_all").attr("account_status");
            var verMapper = {
                productId: $("#productId").val(),
                apkVerOld: $("#apkVerOld").val(),
                // androidVerCode: $("#androidVerCode").val(),
                channel: $("#channel").val(),
                sdkVer: $("#sdkVer").val(),
                sysVer: $("#sysVer").val(),
                apkVerNew: $("#apkVerNew").val(),
                apkSize: $("#apkSize").val(),
                apkUrl: $("#apkUrl").val(),
                apkMD5: $("#apkMD5").val(),
                upgradeType: $("#upgradeType").val(),
                description: $("#description").val(),
                region: $("#region").val(),
                brand: $("#brand").val(),
                model: $("#model").val(),
                time: $("#time").val(),
                accountStatus: accountStatus,
                ktwelve: schools,
                school: $("#school").val(),
                clazz: $("#clazz").val(),
                clazzLevel: $("#clazzLevel").val(),
                user: $("#user").val(),
                userType: $("#userType").val(),
                // subject : $("#subject").val(),
                isManual: $("#isManual").val(),
                // mobile: $("#mobile").val(),
                imei: $("#imei").val(),
                //rate: $("#rate").val(),
                //count: $("#count").val(),
                ownerAppPid: $("#ownerAppPid").val(),
                ownerAppApkVer: $("#ownerAppApkVer").val(),
                rank: $("#rank").val(),
                updateTime: $("#updateTime").val(),
                productIdNew: $("#productIdNew").val(),
                packageName: $("#packageName").val(),
                scheme: $("#scheme").val(),
                status: $("#status").val(),
                image: $("#image").val()
                <#if ver??>, id: '${(ver.id)!''}'</#if>
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

            if (verMapper.productIdNew == undefined || verMapper.productIdNew.trim() == '') {
                alert("请输入产品ID(response)");
                return false;
            }
            else {
            if (verMapper.productId.trim().substring(0, 4) !== '1007') {
                re = checknum(verMapper.productIdNew.trim());
                if (!re.tf) {
                    alert("产品ID(response)" + re.ms);
                    return false;
                }
            }
            }

//            if (!(verMapper.apkVerOld == undefined || verMapper.apkVerOld.trim() == '')) {
//                re1 = checksign(verMapper.apkVerOld.trim(), signall);
//                if (re1.tf) {
//                    re2 = checkver(re1.content)
//                    if (!re2.tf) {
//                        alert("客户端版本号" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    re2 = checkrange(verMapper.apkVerOld.trim())
//                    if (re2.tf) {
//                        if (!checkver(re2.content1).tf || !checkver(re2.content2).tf) {
//                            alert("客户端版本号" + "版本格式有误");
//                            return false;
//                        }
//                    }
//                    else {
//                        alert("客户端版本号" + re1.ms);
//                        return false;
//                    }
//                }
//            }

//            if (!(verMapper.androidVerCode == undefined || verMapper.androidVerCode.trim() == '')) {
//                re1 = checksign(verMapper.androidVerCode.trim(), signall);
//                if (re1.tf) {
//                    re2 = checknum(re1.content)
//                    if (!re2.tf) {
//                        alert("版本标示" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    re2 = checkrange(verMapper.androidVerCode.trim());
//                    if (re2.tf) {
//                        if (!checknum(re2.content1).tf || !checknum(re2.content2).tf) {
//                            alert("版本标示" + "含非数字内容");
//                            return false;
//                        }
//                    }
//                    else {
//                        alert("版本标示" + re1.ms);
//                        return false;
//                    }
//                }
//            }

//            if (!(verMapper.channel == undefined || verMapper.channel.trim() == '')) {
//                re1 = checksign(verMapper.channel.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknum(re1.content);
//                    if (!re2.tf) {
//                        alert("渠道号" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("渠道号" + re1.ms);
//                    return false;
//                }
//            }

//            if (!(verMapper.sdkVer == undefined || verMapper.sdkVer.trim() == '')) {
//                re1 = checksign(verMapper.sdkVer.trim(), signall);
//                if (re1.tf){
//                    re2=checknum(re1.content) || checkver(re1.content);
//                    if (!re2.tf){
//                        alert("SDK版本"+re2.ms);
//                        return false;
//                    }
//                }
//                else{
//                    re2=checkrange(verMapper.sdkVer.trim());
//                    if (re2.tf){
//                        if ((!checknum(re2.content1).tf && !checkver(re2.content1).tf)
//                                ||(!checknum(re2.content2).tf && !checkver(re2.content2).tf)){
//                            alert("SDK版本"+"含非数字内容");
//                            return false;
//                        }
//                    }
//                    else{
//                        alert("SDK版本"+re1.ms);
//                        return false;
//                    }
//                }
//            }

//            if (!(verMapper.sysVer == undefined || verMapper.sysVer.trim() == '')) {
//                re1 = checksign(verMapper.sysVer.trim(), signall);
//                if (re1.tf) {
//                    re2 = checkSysver(re1.content);
//                    if (!re2.tf) {
//                        alert("SYS版本" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    re2 = checkrange(verMapper.sysVer.trim());
//                    if (re2.tf) {
//                        if (!checkSysver(re2.content1).tf || !checkSysver(re2.content2).tf) {
//                            alert("SYS版本" + "含非数字内容");
//                            return false;
//                        }
//                    }
//                    else {
//                        alert("SYS版本" + re1.ms);
//                        return false;
//                    }
//                }
//            }

            if (!(verMapper.apkVerNew == undefined || verMapper.apkVerNew.trim() == '')) {
                re2 = checkver(verMapper.apkVerNew.trim());
                if (!re2.tf) {
                    alert("APK版本号" + re2.ms);
                    return false;
                }
            }

            // if(verMapper.apkSize == undefined || verMapper.apkSize.trim() == ''){
            //     alert("请输入APK大小");
            //     return false;
            //  }

            //  if(verMapper.apkUrl == undefined || verMapper.apkUrl.trim() == ''){
            //        alert("请输入APK下载URL");
            //      return false;
            //   }

            //    if(verMapper.apkMD5 == undefined || verMapper.apkMD5.trim() == ''){
            //        alert("请输入APK文件MD5");
            //        return false;
            //   }


            //   if(verMapper.description == undefined || verMapper.description.trim() == ''){
            //       alert("请输入升级说明");
            //       return false;
            //   }

            if (!(verMapper.updateTime == undefined || verMapper.updateTime.trim() == '')) {
                re2 = checknum(verMapper.updateTime.trim());
                if (!re2.tf) {
                    alert("升级间隔" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.brand == undefined || verMapper.brand.trim() == '')) {
                re1 = checksign(verMapper.brand.trim(), signpart);
                if (!re1.tf) {
                    alert("手机厂商" + re1.ms);
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

//            if (!(verMapper.region == undefined || verMapper.region.trim() == '')) {
//                re1 = checksign(verMapper.region.trim(), signpart);
//                if (re1.tf) {
//                    if (!checksix(verMapper.region.trim())) {
//                        alert("区域编码有误");
//                        return false;
//                    }
//                }
//                else {
//                    alert("区域编码" + re1.ms);
//                    return false;
//                }
//            }

//            if (!(verMapper.time == undefined || verMapper.time.trim() == '')) {
//                re1 = checksigntime(verMapper.time.trim(), signtime);
//                if (re1.tf) {
//                    re2 = checkdate(re1.content);
//                    if (!re2.tf) {
//                        alert("时间段" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    re2 = checkrangetime(verMapper.time.trim());
//                    if (!re2.tf) {
//                        alert("时间段" + re2.ms);
//                        return false;
//                    }
//                }
//            }

//            if (!(verMapper.school == undefined || verMapper.school.trim() == '')) {
//                re1 = checksign(verMapper.school.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknum(re1.content);
//                    if (!re2.tf) {
//                        alert("学校ID" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("学校ID" + re1.ms);
//                    return false;
//                }
//            }

//            if (!(verMapper.clazz == undefined || verMapper.clazz.trim() == '')) {
//                re1 = checksign(verMapper.clazz.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknum(re1.content);
//                    if (!re2.tf) {
//                        alert("班级ID" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("班级ID" + re1.ms);
//                    return false;
//                }
//            }

//            if (!(verMapper.clazzLevel == undefined || verMapper.clazzLevel.trim() == '')) {
//                re1 = checksign(verMapper.clazzLevel.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknumclazzLevel(re1.content);
//                    if (!re2.tf) {
//                        alert("年级" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("年级" + re1.ms);
//                    return false;
//                }
//            }

//            if (!(verMapper.user == undefined || verMapper.user.trim() == '')) {
//                re1 = checksign(verMapper.user.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknum(re1.content);
//                    if (!re2.tf) {
//                        alert("用户ID" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("用户ID" + re1.ms);
//                    return false;
//                }
//            }

            //      if(verMapper.userType == undefined || verMapper.userType.trim() == ''){
            //         alert("请输入用户类型");
            //         return false;
            //      }

            //     if(!(verMapper.subject == undefined || verMapper.subject.trim() == '')){
            //         re1=checksign(verMapper.subject.trim(),signpart);
            //         if (re1.tf){
            //             re2=checknum(re1.content);
            //             if (!re2.tf){
            //                  alert("科目ID"+re2.ms);
            //                 return false;
            //             }
            //        }
            //         else{
            //            alert("科目ID"+re1.ms);
            //            return false;
            //        }
            //    }

//            if (!(verMapper.mobile == undefined || verMapper.mobile.trim() == '')) {
//                re1 = checksign(verMapper.mobile.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknum(re1.content);
//                    if (!re2.tf) {
//                        alert("手机号码" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("手机号码" + re1.ms);
//                    return false;
//                }
//            }

//            if (!(verMapper.imei == undefined || verMapper.imei.trim() == '')) {
//                re1 = checksign(verMapper.imei.trim(), signpart);
//                if (re1.tf) {
//                    re2 = checknum(re1.content);
//                    if (!re2.tf) {
//                        alert("手机IMEI码" + re2.ms);
//                        return false;
//                    }
//                }
//                else {
//                    alert("手机IMEI码" + re1.ms);
//                    return false;
//                }
//            }

            if (!(verMapper.rate == undefined || verMapper.rate.trim() == '')) {
                re2 = checknum(verMapper.rate.trim());
                if (!re2.tf) {
                    alert("百分比" + re2.ms);
                    return false;
                }
            }

            if (!(verMapper.count == undefined || verMapper.count.trim() == '')) {
                re2 = checknum(verMapper.count.trim());
                if (!re2.tf) {
                    alert("数量" + re2.ms);
                    return false;
                }
            }

            appPostJson("addver.vpage", verMapper, function (data) {
                if (data.success) {
                    location.href = "list.vpage?id=" + data.id;
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
        var regd = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}:\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
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
            if (str < "1" || str > "9") {
                re = false;
                message = "年级应为1~9数字";
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
        var regd = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
        re = false;
        message = "时间格式错误";
        if (regd.test(str)) {
            re = true;
            message = "";
        }
        return {"tf": re, "ms": message};
    }

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }

</script>
</@layout_default.page>