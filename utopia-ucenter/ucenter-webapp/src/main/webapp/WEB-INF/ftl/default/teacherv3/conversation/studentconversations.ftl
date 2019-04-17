<#-- @ftlvariable name="currentPage" type="java.lang.Integer" -->
<#-- @ftlvariable name="pagination" type="com.voxlearning.utopia.service.conversation.api.mapper.ConversationPagination" -->
<#if pagination?? && pagination.totalPages gt 0>
    <#list pagination.content as conversation>
    <div id="infobox_${conversation.letterId!}" <#if (conversation_index%2 == 0)>class="t-notice-box-back"</#if>>
        <div class="t-notice-box" >
            <div class="tn-del w-fl-left">
                <div class="avatar">
                    <img src="<@app.avatar href="${conversation.sender.senderImageUrl!}"/>" width="80" height="80" onerror="this.onerror='';this.src='<@app.avatar/>'"/>
                    <i class="tn-icon tn-icon-green"></i>
                </div>
            </div>
            <div class="tn-info w-fl-left">
                <p><#if conversation.moderator>您<#else>${conversation.sender.senderName!}</#if>对
                    <#if conversation.moderator>
                        <#if conversation.receivers??>
                            <#list conversation.receivers as receiver>
                                <#if receiver_has_next>
                                    <span>${receiver.receiverName+" , "!}</span>
                                <#else>
                                    <span>${receiver.receiverName+" "!}</span>
                                </#if>
                            </#list>
                        </#if>
                    <#else>您
                    </#if>
                    说：</p>
                ${conversation.payload!}
                <p class="w-gray w-ag-right w-magT-10">${conversation.formatCreateTime()!}</p>
            </div>
            <div class="tn-btn w-fl-right">
                <#if conversation.replies?size gt 0>
                    <a class="show_reply_more_box unfold w-blue" href="javascript:void (0);">
                        <span data-conversation_id="${conversation.uniqueId!''}">展开对话</span>
                        <span data-conversation_id="${conversation.uniqueId!''}" style="display:none;">收起对话</span>
                    </a>
                </#if>
                <#if !conversation.moderator>
                    <a data-letter_id="${conversation.letterId}" class="reply_but w-blue" href="javascript:void(0);">
                        回复
                    </a>
                </#if>
                <a class="w-blue delete_content_but" data-delete_type="deleteLetterAndReply" data-letter_id="${conversation.uniqueId!}" href="javascript:void (0)">删除</a>
            </div>
            <div class="w-clear"></div>
        </div>
    <#--展开回复-->
        <div id="replys_box_${conversation.uniqueId!}" style="display: none;">
            <#list conversation.replies as message >
                <div class="t-notice-box" style="padding-left: 40px;">
                    <div class="tn-del w-fl-left">
                        <div class="avatar">
                            <img src="<@app.avatar href="${message.sender.senderImageUrl!}"/>" onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/>
                        </div>
                    </div>
                    <div class="tn-info w-fl-left">
                        <p>${message.sender.senderName!}于 ${message.formatCreateTime()!} 回复给 ${message.getFirstReceiver().receiverName!}</p>
                    ${message.payload!}
                        <p class="w-gray w-ag-right w-magT-10">--</p>
                    </div>
                    <div class="tn-btn w-fl-right">
                        <#if (id != message.sender.senderId)>
                            <a data-letter_id="${message.id}" class="reply_but w-blue">回复</a>
                        </#if>
                    </div>
                </div>
                <@textareabox  message.id conversation.uniqueId/>
                <@itemlist message.children conversation.uniqueId />
            </#list>
        </div>
        <@textareabox  conversation.letterId conversation.uniqueId />
    </div>
    </#list>


<div class="student_message_page_list message_page_list" style="float:right; padding:0 0 10px;"></div>
<#--递归查询回复-->
    <#macro itemlist items conversationId>
        <#list items as c>
        <div class="t-notice-box" style="padding-left: 100px;">
            <div class="tn-del w-fl-left">
                <div class="avatar">
                    <img src="<@app.avatar href="${c.sender.senderImageUrl!}"/>" onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/>
                </div>
            </div>
            <div class="tn-info w-fl-left">
                <p>${c.sender.senderName!}于 ${c.formatCreateTime()!}
                    回复给 ${c.getFirstReceiver().receiverName!}：</p>
            ${c.payload!}
                <p class="w-gray w-ag-right w-magT-10">--</p>
            </div>
            <div class="tn-btn w-fl-right">
                <#if id !=c.sender.senderId>
                    <a class="reply_but w-blue" data-letter_id="${c.id!}" href="javascript:void (0);">回复</a>
                </#if>
            </div>
        </div>
            <@textareabox c.id conversationId />
        <#--判断是否有children-->
            <#if c?size gt 0>
                <@itemlist  c.children conversationId/>
            </#if>
        </#list>
    </#macro>

    <#macro textareabox id conversationId>
    <div id="reply_box_${id!}" class="entry" style="display:none; margin: 10px 50px; clear: both;">
        <div>
            <textarea id="reply_content_box_${id!}" style="width: 97%; height: 60px;" class="w-int"></textarea>
        </div>
        <div style="text-align: right; padding: 5px 0;">
            <span class="w-icon-md">还可以输入<span id="surplus_count_${id!}" style="font-size:16px; font-weight:bold;"> 140 </span>字</span>
            <a href="javascript:void (0);" data-letter_id="${id!}" data-conversation_id="${conversationId}" class="w-btn w-btn-mini send_reply_but">
                发送
            </a>
        </div>
    </div>
    </#macro>
<script type="text/javascript">
    $(function () {
        $(".student_message_page_list").page({
            total: ${pagination.totalPages!0},
            current: ${currentPage!},
            jumpCallBack: loadMessage.loadStudentMessage
        });

        /*展开收起*/
        $(".show_reply_more_box").on('click', function () {
            var _this = $(this);
            var uniqueId = _this.find('span').data('conversation_id');
            _this.find("span:visible").hide().siblings().show();
            $("#replys_box_" + uniqueId).toggle();
        });

        //发送回复
        $(".send_reply_but").click(function () {
            var $this = $(this);
            var letterId = $this.data('letter_id');
            var conversationId = $this.data('conversation_id');
            var payload = $("#reply_content_box_" + letterId).val();
            if($this.hasClass('loading')){return false}
            $this.removeClass('btn_mark_primary').addClass('loading').text('提交中');
            App.postJSON("/teacher/conversation/replyletter.vpage", {letterId: letterId, content: payload}, function (data) {
                if (data.success) {
                    //更新学生留言板回复
                    $.get('/teacher/conversation/studentconversations.vpage?currentPage=${currentPage!}', function (data) {
                        $(".studentMessage").html(data);
                        //展开回复
                        $(".show_reply_more_box span[data-conversation_id="+conversationId+"]").last().show().click().siblings().hide();
                        $this.addClass('btn_mark_primary').removeClass('loading').text('提交');
                    });
                } else {
                    $17.alert("你的回复发送失败，稍后再发送吧！");
                    $this.addClass('btn_mark_primary').removeClass('loading').text('提交');
                }

            });
        });
    });
</script>
<#else>
<div class="w-noData-box">您的学生留言箱没有任何内容！</span></div>
</#if>