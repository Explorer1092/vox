<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='短信管理平台' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">短信任务管理&nbsp;&nbsp;&nbsp;&nbsp;
        <#if level??><#if level lt 4>
        <a id="new-sms-btn" class="btn btn-info" href="smsdetail.vpage?mode=new">
        <i class="icon-envelope icon-white"></i>
        新建短信
        </a>
        </#if></#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="activity-query" class="form-horizontal" method="post" action="${requestContext.webAppContextPath}/opmanager/smstask/index.vpage" >
                    <input id="source" name="source" value="search" type="hidden"/>
                    <ul class="inline">
                        <li>
                            <label>创建人&nbsp;
                                <select id="creator" name="creator">
                                    <option value="">所有创建人</option>
                                    <#if creatorList??>
                                    <#list creatorList as c>
                                        <option <#if creator??><#if creator == c> selected="selected"</#if></#if> value = "${c!}">${c!}</option>
                                    </#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>请选择状态&nbsp;
                                <select id="status" name="status">
                                    <option value=99>所有状态</option>
                                    <option <#if status??><#if status==0>selected="selected" </#if></#if> value=0>新建</option>
                                    <option <#if status??><#if status==10>selected="selected" </#if></#if> value=10>待审核</option>
                                    <option <#if status??><#if status==20>selected="selected" </#if></#if> value=20>审核完成</option>
                                    <option <#if status??><#if status==30>selected="selected" </#if></#if> value=30>发送完成</option>
                                </select>
                            </label>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">查  询</button>
                        </li>
                    </ul>
                </form>
                <div id="data_table_journal">
                    <table class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <th width="30px;" style="text-align: center;vertical-align: middle;">优先级</th>
                            <th width="13%" style="text-align: center;vertical-align: middle;">活动</th>
                            <th style="text-align: center;vertical-align: middle;">短信内容</th>
                            <th style="display: none; text-align: center;">ut</th>
                            <th width="90px" style="text-align: center;vertical-align: middle;">对象用户</th>
                            <th width="150px" style="text-align: center;vertical-align: middle;">发送时间</th>
                            <th width="130px" style="text-align: center;vertical-align: middle;">创建人</th>
                            <th width="80px" style="text-align: center;vertical-align: middle;">审核状态</th>
                            <th style="display: none; text-align: center;">status</th>
                            <#if level gt 0>
                            <th width="80px" style="text-align: center;vertical-align: middle;">操作</th>
                            <#--// 只有管理员级别有这个按钮组的状态-->
                            <#--// 转上级: (status,level)=(11,1),(12,2)-->
                            <#--// 批准: (status,level)=(11,1),(12,2),(13,4)-->
                            <#--// 驳回: (status,level)=(11,1),(12,2),(13,4)-->
                            </#if>
                        </tr>
                        </thead>
                        <#if smsList??>
                            <tbody>
                                <#list smsList as sms>
                                <tr
                                <#switch sms.status>
                                    <#case 11><#case 12><#case 13>class="warning"<#break>
                                    <#case 21><#case 31>class="success"<#break>
                                    <#case 22><#case 32>class="error"<#break>
                                    <#default>
                                </#switch>
                                >
                                    <td style="text-align: center;vertical-align: middle;">${sms.priority!}</td>
                                    <td id="purpose_${sms.id!}" class="center" style="vertical-align: middle;"><a href="smsdetail.vpage?smsId=${sms.id!}&mode=view">${sms.purpose!''}</a></td>
                                    <td id="smsText_${sms.id!}" class="center" style="vertical-align: middle;">${sms.smsText!''}</td>
                                    <td id="userType_${sms.id!}" class="center" style="display: none;">${sms.ut!}</td>
                                    <td class="center" style="vertical-align: middle;">${sms.userType!''}</td>
                                    <td id="sendTime_${sms.id!}" class="center" style="text-align: center;vertical-align: middle;">${sms.sendTime!''}</td>
                                    <td id="creator_${sms.id!}" class="center" style="text-align: center;vertical-align: middle;">${sms.creator!''}</td>
                                    <td class="center" style="text-align: center;vertical-align: middle;">
                                        <#--<#if sms.status==31>-->
                                            <#--<a id="send_record_${sms.id!}" href="javascript:void(0);">${sms.statusDesc!''}</a>-->
                                        <#--<#else>-->
                                            <a id="trace_sms_${sms.id!}" href="javascript:void(0);">${sms.statusDesc!''}</a>
                                        <#--</#if>-->
                                    </td>
                                    <td id="status_${sms.id!}" style="display: none;">${sms.status!}</td>
                                    <#if level gt 0>
                                    <td class="center" style="text-align: center;vertical-align: middle;">
                                    <#if level == 1 && sms.status == 11>
                                        <a id="raise_up_${sms.id!}" href="javascript:void(0);" title="转上级"><i class="icon-arrow-up"></i></a>
                                        <#if requestContext.getCurrentAdminUser().realName != sms.creator>
                                        <a id="approve_${sms.id!}" href="javascript:void(0);" title="审批通过" data-prio="${sms.priorityVal!0}" data-sms="${sms.smsType!}"><i class="icon-ok"></i></a>
                                        <a id="reject_${sms.id!}" href="javascript:void(0);" title="驳回"><i class="icon-remove"></i></a>
                                        </#if>
                                    <#elseif level == 2 && sms.status == 12>
                                        <a id="raise_up_${sms.id!}" href="javascript:void(0);" title="转上级"><i class="icon-arrow-up"></i></a>
                                        <#if requestContext.getCurrentAdminUser().realName != sms.creator>
                                        <a id="approve_${sms.id!}" href="javascript:void(0);" title="审批通过" data-prio="${sms.priorityVal!0}" data-sms="${sms.smsType!}"><i class="icon-ok"></i></a>
                                        <a id="reject_${sms.id!}" href="javascript:void(0);" title="驳回"><i class="icon-remove"></i></a>
                                        </#if>
                                    <#elseif level == 4 && sms.status == 13>
                                        <a id="approve_${sms.id!}" href="javascript:void(0);" title="审批通过" data-prio="${sms.priorityVal!0}" data-sms="${sms.smsType!}"><i class="icon-ok"></i></a>
                                        <a id="reject_${sms.id!}" href="javascript:void(0);" title="驳回"><i class="icon-remove"></i></a>
                                    </#if>
                                    </td>
                                    </#if>
                                </tr>
                                </#list>
                            </tbody>
                        </#if>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="approve-sms-dialog" class="modal fade hide">
    <input id="approve-sms-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">请确认短信发送事项</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">发送优先级:</label>
                        <div class="controls">
                            <select id="sms_priority" style="width: 120px;border: none;">
                                <option value=0>☆☆☆☆☆</option>
                                <option value=1>★☆☆☆☆</option>
                                <option value=3>★★☆☆☆</option>
                                <option value=5>★★★☆☆</option>
                                <option value=7>★★★★☆</option>
                                <option value=9>★★★★★</option>
                            </select>
                            <span>（星级越高，优先级越高）</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">发送通道:</label>
                        <div class="controls">
                            <select id="sms_type" style="width: 220px;border: none;">
                                <#list validSmsTypes as smsType>
                                    <option value=${smsType.name()!''}>${smsType.getDescription()!''}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="approve_btn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="reject-sms-dialog" class="modal fade hide">
    <input id="reject-sms-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">请填写驳回原因</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">驳回原因:</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="reject_comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="reject_btn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="raise-up-dialog" class="modal fade hide">
    <input id="raise-up-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">请填写转上级描述</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">描述:</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="raise_comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="raise_btn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="trace-dialog" class="modal fade hide" style="width: 40%; left: 40%;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">短信流转历史</h4>
            </div>
            <div class="box-content">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th>操作人</th>
                        <th>处理备注</th>
                        <th>时间</th>
                    </tr>
                    </thead>
                    <tbody id="traceFlowBody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<div id="send-dialog" class="modal fade hide" style="width: 30%; left: 50%;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">短信发送状态记录</h4>
            </div>
            <div class="box-content">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th>状态</th>
                        <th>数量</th>
                        <th>发送失败用户</th>
                        <th>失败原因</th>
                    </tr>
                    </thead>
                    <tbody id="recordBody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $('#sendtime').datetimepicker({
            startDate: new Date(),
            format: "yyyy-mm-dd hh:ii:ss"
        });

        // 转上级功能
        $("a[id^='raise_up_']").on('click', function () {
            var id = $(this).attr("id").substring("raise_up_".length);
            $('#raise-up-id').val(id);
            $('#raise-up-dialog').modal('show');

        });

        $('#raise_btn').on('click', function() {
            var id = $('#raise-up-id').val();
            var comment = $('#raise_comment').val().trim();
            $.post('raiseup.vpage', {smsId: id, comment: comment}, function(data){
                if(data.success) {
                    alert("转上级成功，请等待审核");
                    window.location.href = 'index.vpage';
                } else {
                    alert(data.info);
                }
            });
        });

        // 批准功能
        $("a[id^='approve_']").on('click', function () {
            var id = $(this).attr("id").substring("approve_".length);
            var priorityVal = $(this).data("prio");
            var smsType = $(this).data("sms");
            // 批准之前，去定义一个优先级
            $('#approve-sms-id').val(id);
            $('#sms_type').val(smsType);
            $('#sms_priority').val(priorityVal);
            $('#approve-sms-dialog').modal('show');
        });

        $('#approve_btn').on('click', function() {
            var id = $('#approve-sms-id').val();
            var priority = $('#sms_priority').val();
            var smsType = $('#sms_type').val();
            $.post('approvesms.vpage', {smsId: id, priority: priority, smsType: smsType}, function(data){
                if(data.success) {
                    alert("批准成功！短信将在5分钟后陆续发送");
                    window.location.href = 'index.vpage';
                } else {
                    alert(data.info);
                }
            });
        });

        // 驳回功能
        $("a[id^='reject_']").on('click', function () {
            var id = $(this).attr("id").substring("reject_".length);
            $('#reject-sms-id').val(id);
            $('#reject-sms-dialog').modal('show');
         });

        $('#reject_btn').on('click', function () {
            var id = $('#reject-sms-id').val();
            var comment = $('#reject_comment').val();
            $.post('rejectsms.vpage',{
                smsId: id, comment: comment
                }, function(data){
                  if (data.success){
                      alert("驳回成功！");
                      window.location.href = 'index.vpage';
                  } else {
                      alert(data.info);
                  }
            });
        });

        // 查看流转记录
        $("a[id^='trace_sms_']").on('click', function() {
            var id = $(this).attr("id").substring("trace_sms_".length);
            $.post('tracesms.vpage',{
                smsId:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $('#traceFlowBody').html('');
                    for(var i=0; i<data.trace.length; i++){
                        var str = "<tr><td>"+data.trace[i].operation+"</td>";
                        str += "<td class=\"center\">"+data.trace[i].operator+"</td>";
                        str += "<td class=\"center\">"+data.trace[i].comment+"</td>";
                        str += "<td class=\"center\">"+data.trace[i].createtime+"</td>";
                        $('#traceFlowBody').append(str);
                    }
                    $('#trace-dialog').modal('show');
                }
            });
        });

        // 查看发送记录
        $("a[id^='send_record_']").on('click', function () {
            var id = $(this).attr("id").substring("send_record_".length);
            $.post('sendrecord.vpage?', {
                smsId: id
            }, function (data) {
                if (!data.success) {
                    alert(data.info);
                } else {
                    $('#recordBody').html('');
                    var str = "<tr><td class=\"center\">发送成功</td>";
                    str += "<td class=\"center\">" + data.successList.length + "</td><td></td><td></td></tr>";
                    str += "<tr><td class=\"center\">发送失败</td>";
                    str += "<td class=\"center\">" + data.failedList.length + "</td><td></td><td></td></tr>";
                    for (var i = 0; i < data.failedList.length; ++i) {
                        str += "<tr><td></td><td></td><td class=\"center\">"+ data.failedList[i].smsReceiver+"</td>";
                        str += "<td class=\"center\">"+ data.failedList[i].notes+"</td></tr>";
                    }
                    $('#recordBody').append(str);
                    $('#send-dialog').modal('show');
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