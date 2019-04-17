<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='新建短信任务' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
    <legend style="font-weight: 700;">
        新建短信任务&nbsp;&nbsp;&nbsp;&nbsp;
        <#if mode=="new"><input type="button" id="save-new-sms" class="btn btn-primary" value="保  存"/></#if>
        <#if smsTask??><#if requestContext.getCurrentAdminUser().realName == smsTask.creator>
        <#if smsTask.status == 0 || smsTask.status == 22>
            <input type="hidden" id="editable" value="true">
            <input type="button" id="save-sms-btn" class="btn btn-primary" value="保  存"/>
        </#if>
        <#if smsTask.status == 0>
        <input type="button" id="submit-sms-btn" class="btn btn-primary" value="提交审核"/>
        <input type="button" id="delete-sms-btn" class="btn btn-danger" value="删  除"/>
        </#if>
        </#if></#if>
        <input type="button" id="return" onclick="window.location.href='index.vpage'" class="btn" value="返  回" />
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="upload_form" name="upload_form" class="form-horizontal" method="post" action="smsdetail.vpage" enctype="multipart/form-data">
                <fieldset>
                    <#if error??>
                        <div class="alert alert-error">
                            <button type="button" class="close" data-dismiss="alert">×</button>
                            <strong>出错啦！ ${error!}</strong>
                        </div>
                    </#if>
                    <input id="smsId" name="smsId" <#if smsTask??>value="${smsTask.id!''}"</#if> type="hidden"/>
                    <div class="control-group">
                        <label class="control-label">短信发送目的</label>
                        <div class="controls">
                            <textarea id="purpose" name="purpose" style="width: 300px; height: 80px;" placeholder="请认真填写发送该短信的目的，字数控制在100字以内"
    <#if smsTask??><#if requestContext.getCurrentAdminUser().realName != smsTask.creator || smsTask.status gt 0>disabled</#if></#if>><#if smsTask??>${smsTask.purpose!''}</#if></textarea>
                            <span style="color: red">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">短信正文</label>
                        <div class="controls">
                            <textarea id="smsText" name="smsText" style="width: 300px; height: 80px;" placeholder="每条短信字数上限64个汉字，超出将分为多条发送，字数请尽量控制在一条短信以内"
                                <#if smsTask??><#if requestContext.getCurrentAdminUser().realName != smsTask.creator || smsTask.status gt 0>disabled</#if></#if>><#if smsTask??>${smsTask.smsText!''}</#if></textarea>
                            <span style="color: red">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group" >
                        <label class="control-label">发送时间</label>
                        <div class="controls">
                            <input type="text" id="sendtime" name="sendtime" <#if smsTask??> value="${smsTask.smsSendtime!''}"</#if>
                                <#if smsTask??><#if requestContext.getCurrentAdminUser().realName != smsTask.creator || smsTask.status gt 0>disabled</#if></#if>/>
                            <span style="color: red">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">目标用户类型</label>
                        <div class="controls">
                            <select name="userType" id="userType" class="multiple" <#if smsTask??><#if requestContext.getCurrentAdminUser().realName != smsTask.creator || smsTask.status gt 0>disabled</#if></#if>>
                            <option <#if smsTask??><#if smsTask.ut==1> selected="selected"</#if></#if> value=1>教师</option>
                            <option <#if smsTask??><#if smsTask.ut==2> selected="selected"</#if></#if> value=2>家长</option>
                            <option <#if smsTask??><#if smsTask.ut==3> selected="selected"</#if></#if> value=3>学生</option>
                            </select>
                            <span style="color: red">(必选)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">短信类型</label>
                        <div class="controls">
                            <select name="smsType" id="smsType" class="multiple" <#if smsTask??><#if requestContext.getCurrentAdminUser().realName != smsTask.creator || smsTask.status gt 0>disabled</#if></#if>>
                                <option value="">--请选择--</option>
                                <#list validSmsTypes as type>
                                    <option <#if smsTask??><#if smsTask.smsType==type.name()> selected="selected"</#if></#if> value="${type.name()}">${type.getDescription()!''}</option>
                                </#list>
                            </select>
                            <span style="color: red">(必选)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">短信发送方式</label>
                        <div class="controls">
                            <select name="sendType" id="sendType" class="multiple" <#if mode=="view">disabled="disabled"</#if>>
                                <option value=1>根据用户ID或手机号</option>
                            </select>
                            <span style="color: red">(必选)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">接收用户</label>
                        <div class="controls">
                            <input id="targetFile" name="targetFile" type="file" <#if mode=="view">disabled="disabled"</#if>>
                            <a href="download_template.vpage" target="_blank" class="btn btn-primary">下载模版</a>
                        </div>
                        <#--<div class="controls">
                             <textarea id="target" name="target" style="width: 300px; height: 500px;"
                                       placeholder="请输入用户ID或手机号，每条记录一行。建议直接从Excel直接拷贝进来" <#if mode=="view">disabled="disabled"</#if>><#if smsTask??>${smsTask.target!''}</#if></textarea>
                            <span style="color: red">(必填)</span>
                        </div>-->
                    </div>
                    <#if smsTask??><#if smsTask.status ==31 || smsTask.status ==32>
                        <div>
                            <label>统计：</label>
                            <table class="table table-bordered">
                                <tr>
                                    <td>发送成功：</td><td><#if successList??>${successList?size}<#else>0</#if>人</td>
                                    <td>发送失败：</td><td><#if failedList??>${failedList?size}<#else>0</#if>人</td>
                                </tr>
                            </table>
                            <#if failedList??><#if failedList?size gt 0>
                            <label>失败记录：</label>
                            <table class="table table-bordered table-condensed">
                                <thead>
                                <th width="25%">用户ID或手机号</th>
                                <th>失败原因</th>
                                </thead>
                                <tbody>
                                    <#list failedList as l>
                                        <tr>
                                            <td>${l.smsReceiver!''}</td>
                                            <td>${l.notes!''}</td>
                                        </tr>
                                    </#list>
                                </tbody>
                                </#if></#if>
                            </table>
                        </div>
                    </#if></#if>
                </fieldset>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('#sendtime').datetimepicker({
            startDate: new Date(),
            format: "yyyy-mm-dd hh:ii:ss"
        });

        // 保存
        $("#save-sms-btn").on("click", function () {
            if (confirm("是否确认保存？")) {
                var id = $('#smsId').val();
                var sendtime = $('#sendtime').val().trim();
                var smsText = $('#smsText').val().trim();
                var userType = $('#userType').val();
                var purpose = $('#purpose').val().trim();
                var smsType = $('#smsType').val().trim();
                // 校验
                var valid = "";
                var enable = true;
                var textLen = countStrLength(smsText);
                if (sendtime == '') {
                    valid += "发送时间不能为空！\r\n";
                }
                if (smsText == '') {
                    valid += "短信内容不能为空！\r\n"
                } else if (textLen > 240) {
                    valid += "请将内容控制在120字(2条短信)以内 ！\r\n";
                }
                if (purpose == '') {
                    valid += "请填写该短信发送的目的 ！\r\n";
                }
                if (smsType == '') {
                    valid += "请选择发送通道 ！\r\n";
                }
                if (valid.length > 0) {
                    alert(valid);
                    return false;
                }
                if (textLen > 128) {
                    enable = confirm("短信字数超过64个字，将分为两条短信发送，是否继续？");
                }
                if (enable) {
                    $.post('savesms.vpage', {
                        source: "edit",
                        smsId:id,
                        sendtime: sendtime,
                        smsText: smsText,
                        userType: userType,
                        purpose: purpose,
                        smsType:smsType
                    }, function (data) {
                        if (data.success) {
                            alert("保存成功！确认无误后请提交审核");
                            window.location.href = 'index.vpage';
                        } else {
                            alert(data.info);
                        }
                    });
                }
            }
        });

        // 保存
        $("#save-new-sms").on("click", function () {
            if (confirm("是否确认保存？")) {
                var sendtime = $('#sendtime').val().trim();
                var smsText = $('#smsText').val().trim();
                var userType = $('#userType').val().trim();
                //var target = $('#target').val().trim();
                var purpose = $('#purpose').val().trim();
                var smsType = $('#smsType').val().trim();
                // 校验
                var valid = "";
                var enable = true;
                var textLen = countStrLength(smsText);
                if (sendtime == '') {
                    valid += "发送时间不能为空！\r\n";
                }
                if (smsText == '') {
                    valid += "短信内容不能为空！\r\n"
                } else if (textLen > 240) {
                    valid += "请将内容控制在120字(2条短信)以内 ！\r\n";
                }
                /*if (target == '') {
                    valid += "短信接受用户不得为空！\r\n";
                }*/
                if (purpose == '') {
                    valid += "请填写该短信发送的目的 ！\r\n";
                }
                if (smsType == '') {
                    valid += "请选择发送通道 ！\r\n";
                }
                if (valid.length > 0) {
                    alert(valid);
                    return false;
                }
                if (textLen > 128) {
                    enable = confirm("短信字数超过64个字，将分为两条短信发送，是否继续？");
                }
                if (enable) {
                    /*$.post('savesms.vpage', {
                        source: "new",
                        sendtime: sendtime,
                        smsText: smsText,
                        userType: userType,
                        target: target,
                        purpose: purpose,
                        smsType:smsType
                    }, function (data) {
                        if (data.success) {
                            alert("保存成功！确认无误后请提交审核");
                            window.location.href = 'index.vpage';
                        } else {
                            alert(data.info);
                        }
                    });*/
                    $("#upload_form").ajaxSubmit({
                        type: "post",
                        dataType: "json",  // 'xml', 'script', or 'json' (expected server response type)
                        url: "savesms.vpage?source=new",
                        success: function (data) {
                            if (data.success) {
                                alert("保存成功！确认无误后请提交审核");
                                window.location.href = 'index.vpage';
                            } else {
                                alert(data.info);
                            }
                        },
                        error: function (msg) {
                            alert("操作失败");
                        }
                    });
                }
            }
        });

        // 删除功能
        $('#delete-sms-btn').on('click', function() {
            if (confirm("是否确认删除")) {
                var id = $('#smsId').val();
                $.post('deletesms.vpage', {
                    smsId: id
                }, function (data) {
                    if (data.success) {
                        alert("删除成功！");
                    } else {
                        alert(data.info);
                    }
                    window.location.href = 'index.vpage';
                });
            }
        });

        // 提交功能
        $("#submit-sms-btn").on('click', function () {
            var id = $('#smsId').val();
            $.post('sumbitsms.vpage', {smsId: id}, function(data){
                if(data.success) {
                    alert("提交成功，请等待审核");
                    window.location.href = 'index.vpage';
                } else {
                    alert(data.info);
                }
            });
        });
    });

    function countStrLength(str) {
        var len = 0;
        for (var i = 0; i < str.length; ++i) {
            if (str.charCodeAt(i) > 0 && str.charCodeAt(i) < 128)
                len++;
            else
                len += 2;
        }
        return len;
    }

</script>
</@layout_default.page>
