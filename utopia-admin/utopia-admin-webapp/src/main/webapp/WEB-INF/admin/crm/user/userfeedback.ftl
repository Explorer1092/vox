<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="userFeedbackInfoList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
    <div class="span9">
        <fieldset>
            <legend>用户<a href="../user/userhomepage.vpage?userId=${userId!}">${userName!}</a>反馈信息</legend>
        </fieldset>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>反馈时间</th>
                <th>更新时间</th>
                <th style="width:200px;">反馈内容</th>
                <th>反馈类型</th>
                <th>练习类型</th>
                <th>状态</th>
                <th style="width:200px;">回复</th>
                <th style="width:200px;">操作</th>
            </tr>
            <#if userFeedbackInfoList?has_content>
            <#list userFeedbackInfoList as userFeedbackInfo>
                <tr data-id="${userFeedbackInfo.id}">
                    <td>${userFeedbackInfo.createDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${userFeedbackInfo.updateDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${userFeedbackInfo.content?html}</td>
                    <td>${userFeedbackInfo.tagFeedbackType!}</td>
                    <td>${userFeedbackInfo.practiceType!}</td>
                    <td>${userFeedbackInfo.state!}</td>
                    <td><#if userFeedbackInfo.reply??>${(userFeedbackInfo.reply?html)?replace('@!=@','<br /><i class="icon-star"></i>')}</#if></td>
                    <td><a id="reply_feedback_${userFeedbackInfo.id!}" href="javascript:void(0);" data-user_id="${userFeedbackInfo.userId}">回复</a></td>
                </tr>
            </#list>
        </#if>
        </table>
    </div>

<div id="reply_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>回复用户<span id="user_id"></span></h3>
        <input type="hidden" id="feedback_id"/>
    </div>
    <div class="modal-body">
        <div class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dl>
                        <dt>请填写回复内容</dt>
                        <dd><label><textarea id="reply_content" cols="35" rows="4" value=""></textarea></label></dd>
                    </dl>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dl>
                        <dt>快捷回复内容：</dt>
                        <dd>
                            <label><select id="feedback_quick_reply_content">
                                <option value="" selected="selected">无</option>
                                <#if feedbackQuickReplyList?has_content>
                                    <#list feedbackQuickReplyList as feedbackQuickReply>
                                        <option value="${feedbackQuickReply!}">${feedbackQuickReply!}</option>
                                    </#list>
                                </#if>
                            </select></label>
                        </dd>
                    </dl>
                </li>
            </ul>
        </div>
    </div>
    <div class="modal-footer">
        <button id="reply_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script type="text/javascript">
    $('[id^="reply_feedback_"]').on('click', function() {
        var $this = $(this);
        $('#feedback_id').val($this.closest('tr').data('id'));
        $('#user_id').text($this.data('user_id'));
        $('#reply_content').val('');
        $('#feedback_quick_reply_content').val('');
        $('#reply_dialog').modal('show');
    });

    $('#reply_dialog_btn_ok').on('click', function() {
        var feedbackId = $('#feedback_id').val();
        var feedbackMap = {};
        feedbackMap[feedbackId] = $('#user_id').text();
        var data = {
            feedbackMapJson : JSON.stringify(feedbackMap),
            reply       :   $('#reply_content').val()
        };
        $.post('../feedback/replyfeedback.vpage', data, function(data) {
            if(data.success) {
                $('#reply_dialog').modal('hide');
            }
            alert(data.info);
            window.location.reload();
        });
    });

    $('#feedback_quick_reply_content').click(function() {
        $('#reply_content').val($(this).val());
    });
</script>
</@layout_default.page>