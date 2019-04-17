<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="background: none;">
    <div >
        <table>
            <tr>
                <td>
                    <form id="cleanupBindedMobile" class="well form-horizontal" style="background-color: #fff;">
                        <fieldset>

                            <div class="control-group">
                                <label class="control-label"  for="cleanupBindedMobile_mobile">手机号</label>
                                <div class="controls">
                                    <input type="text" name="mobile" value="" id="cleanupBindedMobile_mobile" class="input"/>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label"  for="cleanupBindedMobile_mobile">角色</label>
                                <div class="controls">
                                    <input type="checkbox" checked="checked" name="cleanup_role" value="teacher" id="cleanupBindedMobile_teacher" class="checkbox"/>老师
                                    <input type="checkbox" checked="checked" name="cleanup_role" value="student" id="cleanupBindedMobile_student" class="checkbox"/>学生
                                    <input type="checkbox" checked="checked" name="cleanup_role" value="parent" id="cleanupBindedMobile_parent" class="checkbox"/>家长
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label"  for="cleanupBindedMobile_reason">原因</label>
                                <div class="controls">
                                    <textarea name="reason"></textarea>
                                </div>
                            </div>

                            <div class="control-group">
                                <div class="controls">
                                    <input type="button" id="cleanupBindedMobile_Button" value="清除绑定手机号" class="btn btn-primary"/>
                                </div>
                            </div>
                        </fieldset>
                    </form>
                </td>
            </tr>
            <tr>
                <td>
                    <form style="background-color: #fff;" class="well form-horizontal"  name="setBabelVitality" id="setBabelVitality">
                        <fieldset>

                            <div class="control-group">
                                <label for="setBabelVitality" class="control-label">用户ID</label>
                                <div class="controls">
                                    <input type="text" class="input" id="setBabelVitality_userIds" value="" name="userIds">
                                </div>
                                <label for="setBabelVitality" class="control-label">通天塔活力值</label>
                                <div class="controls">
                                    <input type="text" class="input" id="setBabelVitality_vitality" value="" name="vitality">
                                </div>
                            </div>

                            <div class="control-group">
                                <div class="controls">
                                    <input type="button" class="btn btn-primary" value="设置通天塔活力" id="setBabelVitality_Button">
                                </div>
                                <div class="controls">
                                    通天塔活力值。<br>
                                    用户ID:把需要复制的用户ID以逗号隔开<br>
                                    正数：增加活力（每次最多5点）。负数：减少活力（绝对值不可大于当前活力）。
                                </div>
                            </div>
                        </fieldset>
                    </form>
                </td>
            </tr>
            <tr>
                <td>
                    <form id="cleanupBindedEmail" name="cleanupBindedEmail"  class="well form-horizontal" style="background-color: #fff;">
                        <fieldset>

                            <div class="control-group">
                                <label class="control-label"  for="cleanupBindedEmail_email">邮件</label>
                                <div class="controls">
                                    <input type="text" name="email" value="" id="cleanupBindedEmail_email" class="input"/>
                                </div>
                            </div>

                            <div class="control-group">
                                <div class="controls">
                                    <input type="button" id="cleanupBindedEmail_Button" value="清除绑定邮件" class="btn btn-primary"/>
                                </div>
                            </div>
                        </fieldset>
                    </form>
                </td>
            </tr>
            <tr>
                <td>
                    <form id="queryMobilMessage" name="queryMobilMessage" action="/crm/toolkit/findMobileMessage.vpage" method="post" class="well form-horizontal" style="background-color: #fff;">
                        <fieldset>

                            <div class="control-group">
                                <label class="control-label"  for="queryMobilMessage_mobile">手机号</label>
                                <div class="controls">
                                    <input type="text" name="mobile" value="<#if mobile??>${mobile}</#if>" id="queryMobilMessage_mobile" class="input"/>
                                </div>
                            </div>

                            <div class="control-group">
                                <div class="controls">
                                    <input type="submit" id="queryMobilMessage_Button" value="查询短信" class="btn btn-primary"/>
                                </div>
                            </div>
                        </fieldset>
                    </form>
                <#if error??>${error}</#if>
                <#if smsMessageList??>
                    <table style="border-width: 2px;" class="table table-bordered">
                        <thead>
                        <tr>
                            <th colspan="2">手机号：<span style="color:dodgerblue;">${mobile!}</span></th>
                        </tr>
                        <tr>
                            <th style="width:100px;">创建时间</th>
                            <th>短信类型</th>
                            <th width="30%">短信内容</th>
                            <th>状态</th>
                            <th>错误原因</th>
                            <th>发送通道</th>
                        </tr>
                        </thead>
                    <tbody>
                        <#if smsMessageList?size gt 0>
                            <#list smsMessageList as SMSMessage>
                            <tr>
                                <td>${(SMSMessage.createTime)?string("yyyy-MM-dd HH:mm:ss")?replace(" ", "<br/>")}</td>
                                <td>
                                ${(SMSMessage.smsType.getDescription())!'--'}
                                    <br/>${(SMSMessage.smsType.name())!'--'}
                                </td>
                                <td>
                                ${(SMSMessage.smsContent)!?html}
                                </td>
                                <td>
                                    <#if (SMSMessage.status)??>
                                        <#if SMSMessage.status == '1'>
                                            提交成功
                                        <#elseif SMSMessage.status == '2'>
                                            发送成功
                                        <#else>
                                            发送失败
                                        </#if>
                                    <#else>
                                        --
                                    </#if>
                                    <#if (SMSMessage.verification)?? && (SMSMessage.verification)>
                                        <br/> 是否消费: ${(SMSMessage.consumed)?string("是","否")}
                                    <#else>
                                        <br/>送达时间：${(SMSMessage.receiveTime)!'--'}
                                    </#if>
                                </td>
                                <td>${(SMSMessage.errorDesc)!'--'}(${(SMSMessage.errorCode)!'--'})</td>
                                <td>${(SMSMessage.smsChannel)!'--'}</td>
                            </tr>
                            </#list>
                        </tbody>
                        <#else>
                            <tbody>
                            <tr>
                                <td colspan="6" style="text-align: center;">未查询出符合条件的数据</td>
                            </tr>
                            </tbody>
                        </#if>
                    </table>
                </#if>
                </td>


            </tr>
        </table>
    </div>
<script type="text/javascript">
    $(function(){
        $("#cleanupBindedMobile_Button").on("click",function(){
            $.ajax({
                type:"post",
                url:"/crm/toolkit/cleanupBindedMobile.vpage",
                data:$("#cleanupBindedMobile").serialize(),
                success:function(data){
                    alert("提示:" + data.info);
                    if(data.success){
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        });
        $("#setBabelVitality_Button").on("click",function(){
            $.ajax({
                type:"post",
                url:"/crm/toolkit/setVitality.vpage",
                data:$("#setBabelVitality").serialize(),
                success:function(data){
                    alert(data.info);
                    if(data.success){
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        });
        $("#cleanupBindedEmail_Button").on("click",function(){
            $.ajax({
                type:"post",
                url:"/crm/toolkit/cleanupBindedEmail.vpage",
                data:$("#cleanupBindedEmail").serialize(),
                success:function(data){
                    alert(data.info);
                    if(data.success){
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        });
    });
</script>
</body>
</html>