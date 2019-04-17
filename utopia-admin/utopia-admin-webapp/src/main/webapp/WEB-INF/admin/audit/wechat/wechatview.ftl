<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=21>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>
                <strong>发送微信模板消息</strong>
                <#if audit?? && audit>
                    <div style="float:right;">
                        <#if workflowRecord??>
                            <#if workflowRecord.status == 'lv1' ||  workflowRecord.status == 'lv2'>
                                <a href="javascript:void(0);" data-id="${workflowRecord.id!''}" data-type="send" data-text="发送" class="btn btn-success operation">
                                    <i class="icon-ok icon-white"></i> 发 送
                                </a>
                                <a href="javascript:void(0);" data-id="${workflowRecord.id!''}" data-type="reject" data-text="驳回" class="btn btn-danger operation">
                                    <i class="icon-remove icon-white"></i> 驳 回
                                </a>
                            </#if>
                            <#if workflowRecord.status == 'lv1'>
                                <a href="javascript:void(0);" data-id="${workflowRecord.id!''}" data-type="raiseup" data-text="转上级" class="btn btn-info operation">
                                    <i class="icon-signal icon-white"></i> 转上级
                                </a>
                            </#if>
                        </#if>
                    </div>
                </#if>
            </legend>
        </fieldset>
        <div id="error_div" class="alert alert-error" style="display: none;">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="error_msg"></strong>
        </div>
        <div class="row-fluid form-horizontal">
            <div>
                <ul class="inline">
                    <li>
                        <label><strong> 选择消息发送端：</strong>&nbsp;&nbsp;&nbsp;&nbsp;
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>消息主题：</strong></label>
                                <div class="controls" style="line-height: 30px;">
                                    <#if wechatMsg?? && wechatMsg.wechatType??>
                                        <#switch (wechatMsg.wechatType)>
                                            <#case 'PARENT'> 微信家长通 <#break/>
                                            <#case 'TEACHER'> 微信老师端 <#break/>
                                            <#default> 未选择 <#break/>
                                        </#switch>
                                    </#if>
                                </div>
                            </div>
                        </label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <label><strong>请填写消息模板：</strong></label>
                        <div class="well" style="float: left; width: 100%;">
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>消息主题：</strong></label>
                                <div class="controls" style="line-height: 30px;">
                                    <#if wechatMsg??>${(wechatMsg.firstInfo)!'--'}</#if>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong id="lb_keyword1">Keyword1：</strong></label>
                                <div class="controls" style="line-height: 30px;">
                                    <#if wechatMsg??>${(wechatMsg.keyword1)!'--'}</#if>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong id="lb_keyword2">Keyword2：</strong></label>
                                <div class="controls" style="line-height: 30px;">
                                    <#if wechatMsg??>${(wechatMsg.keyword2)!'--'}</#if>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>备注：</strong></label>
                                <div class="controls" style="line-height: 30px;">
                                    <#if wechatMsg??>${(wechatMsg.remark)!'--'}</#if>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>跳转链接：</strong></label>
                                <div class="controls" style="line-height: 30px;">
                                    <#if wechatMsg??>${(wechatMsg.url)!'--'}</#if>
                                </div>
                            </div>

                        </div>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <label><strong>直播广告专用:</strong> <#if wechatMsg?? && wechatMsg.isWkt! =="isZbgg"><i class="icon-ok"></i><#else><i class="icon-remove"></i></#if> </label>
                    </li>
                    <li>
                        <label><strong>微课堂专用：</strong> <#if wechatMsg?? && wechatMsg.isWkt! =="isWkt"><i class="icon-ok"></i><#else><i class="icon-remove"></i></#if> </label>
                    </li>
                </ul>
                <ul class="inline">
                    <#if wechatMsg?? && wechatMsg.sendType == 1>
                        <li>
                            <label><strong>发送时间：</strong> <#if wechatMsg.sendTime?has_content>${(wechatMsg.sendTime)?string('yyyy-MM-dd HH:mm:ss')}<#else> 未指定 </#if> </label>
                        </li>
                        <br/>
                        <li>
                            <label><strong>用户ID：</strong>  ${(wechatMsg.userIds)!} </label>
                        </li>
                    <#elseif wechatMsg?? && wechatMsg.sendType == 2>
                        <li>
                            <label><strong>附件：</strong>  <a href="${(wechatMsg.fileUrl)!}">点击下载附件</a> </label>
                        </li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
    <#if histories?? && histories?has_content>
        <div class="control-group well" style="margin-top: 50px;">
            <label class="col-sm-2 control-label"><strong>处理意见区</strong></label>
            <div class="controls">
                <table class="table table-striped table-bordered" style="font-size: 14px;">
                    <thead>
                    <tr>
                        <th>处理日期</th>
                        <th>处理人</th>
                        <th>处理结果</th>
                        <th>处理意见</th>
                    </tr>
                    </thead>
                    <#list histories as history>
                        <tr>
                            <td>${history.updateDatetime?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${history.processorName!}(${history.processorAccount!})</td>
                            <td>${history.result!}</td>
                            <td>${history.processNotes!}</td>
                        </tr>
                    </#list>
                </table>
            </div>
        </div>
    </#if>
</div>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 150%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在处理，请稍候……</p>
</div>
<script>

    $(function () {
        $(".operation").on('click', function() {
            var workFlowRecordId = $(this).data("id");
            var operationType = $(this).data("type");

            $.post('checkwechatmsg.vpage',{"workFlowRecordId":workFlowRecordId,"operationType":operationType},function (data){
                if(data.success){
                    alert("操作成功");
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            })

        });
    });

</script>
</@layout_default.page>
