<#-- @falsifiable name="pagination" type="com.voxlearning.utopia.service.conversation.api.mapper.ConversationPagination" -->
<style type="text/css">
    .null_teacher_conversation_box { background: none repeat scroll 0 0 #FCFCFC; border: 1px solid #DDDDDD; clear: both; font-size: 20px; padding: 55px; color: #666666; text-align: center; }
    .morectn { overflow: hidden; word-wrap: break-word; white-space: normal; }
</style>
<#if pagination?? && pagination.content?size gt 0>
    <#list pagination.content as conversation >
        <ul class="show_val_box" data-unique_id="${conversation.uniqueId!}">
            <li>
                <div class="student_conversation" data-unique_id="${conversation.uniqueId!}"
                     data-root_letter_id="${conversation.letterId!}">
                    <div class="currentctn">
                        <dl>
                            <dt class="avatar">
                            <span>
                                <img src="<@app.avatar href="${(conversation.sender.senderImageUrl)!}"/>" width="60"
                                     height="60" onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/></span>
                                <s class="arrowl"></s>
                            </dt>
                            <dd>
                                <div class="title">
                                    <span class="tL">
                                        ${(conversation.sender.senderName)!}在${conversation.formatCreateTime()!}
                                            对${conversation.getFirstReceiverName()!}说
                                    </span>
                                    <div class="tR">
                                        <#if conversation.moderator || conversation.sender.senderType == 3>
                                            <a class="delete_conversation_by_uniqueId w-change-btn w-fl-right" style="margin: 0 2px;" href="javascript:void (0);" data-conversation_id="${conversation.uniqueId!''}">
                                                删除
                                            </a>

                                        </#if>
                                        <#if !conversation.moderator>
                                            <a class="reply_but w-change-btn w-fl-right" data-letter_id="${conversation.letterId!}" data-conversation_id="${conversation.uniqueId!''}" href="javascript:void(0);">
                                                回复
                                            </a>
                                        </#if>

                                        <#if conversation.replies?size gt 0>
                                        <div style="padding: 10px 0 0; text-align: center; clear: both;">
                                            <a class="show_reply_more_box w-green" href="javascript:void (0);">
                                                <span data-conversation_id="${conversation.uniqueId!''}">
                                                    展开<span class="conversation_sum_box"></span>对话
                                                </span>
                                                <span data-conversation_id="${conversation.uniqueId!''}" style="display:none;">
                                                    收起<span class="conversation_sum_box"></span>对话
                                                </span>
                                            </a>
                                        </div>
                                        </#if>
                                    </div>
                                </div>
                                <div class="ctn morectn">${conversation.payload!}</div>

                            <#--回复显示区-->
                                <div class="ctn" id="reply_box_${conversation.uniqueId!''}"
                                     style="display: none; font-size: 12px;">
                                    <#list conversation.replies as reply >
                                        <dl>
                                            <dt class="avatar">
                                                <span>
                                                    <img src="<@app.avatar href="${reply.sender.senderImageUrl!}"/>" width="60" height="60" onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/>
                                                </span>
                                                <s class="arrowl"></s>
                                            </dt>
                                            <dd>
                                                <div class="title">
                                                    <span class="tL">${reply.sender.senderName!}
                                                        于 ${reply.formatCreateTime()!}
                                                        回复给 ${reply.getFirstReceiver().receiverName!}</span>
                                                    <span class="tR">
                                                        <#if (conversation.moderator)>
                                                            <a href="javascript:void(0);" data-letter_id="${reply.id!}"
                                                               class="reply_but w-change-btn w-fl-right" data-conversation_id="${conversation.uniqueId!''}">
                                                                <strong><span>回复</span></strong>
                                                            </a>
                                                        </#if>
                                                    </span>
                                                </div>
                                                <div class="ctn morectn">
                                                ${reply.payload!}
                                                </div>
                                            </dd>
                                            <dd class="clear"></dd>
                                        </dl>
                                        <@textareabox reply.id!''/>
                                        <@itemlist reply.children conversation.uniqueId />
                                    </#list>
                                </div>
                            </dd>
                            <dd class="clear"></dd>
                        </dl>
                    <#--输入框-->
                        <@textareabox conversation.letterId!''/>
                    </div>
                </div>
            </li>
        </ul>
    </#list>
    <#macro itemlist items uniqueId>
        <#list items as c>
        <dl style="padding-left: 30px; width: auto;">
            <dt class="avatar">
                <img src="<@app.avatar href="${c.sender.senderImageUrl!}"/>" width="60" height="60"
                     onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/></span>
                <s class="arrowl"></s>
            </dt>
            <dd>
                <div class="title">
                    <span class="tL">${c.sender.senderName!}于 ${c.formatCreateTime()!}
                        回复给 ${c.getFirstReceiver().receiverName!}</span>
                        <span class="tR">
                            <#if currentUserId != c.sender.senderId>
                                <a href="javascript:void(0);" data-letter_id="${c.id!}" class="reply_but w-change-btn w-fl-right" data-conversation_id="${uniqueId!''}">
                                    <strong><span>回复</span></strong>
                                </a>
                            </#if>
                        </span>
                </div>
                <div class="ctn morectn"> ${c.payload!} </div>
            </dd>
            <dd class="clear"></dd>
        </dl>

            <@textareabox c.id/>
            <#if c?size gt 0>
                <@itemlist  c.children uniqueId/>
            </#if>
        </#list>
    </#macro>

    <#macro textareabox id>
    <div class="replybox reply_box" id="reply_letter_box_${id}" style="display:none;">
        <div class="txa">
            <textarea class="reply_content_box_${id!} w-int" maxlength="140" placeholder="输入你要回复的内容" name="" cols=""
                      rows=""></textarea>
            <span class="tif surplusCount_${id!}">还能输入140个字</span>
        </div>
        <div class="btn">
            <a data-letter_id="${id!}"  href="javascript:void (0);"
               class="w-btn w-btn-green send_letter_but">
                <strong><span>发 送</span></strong>
            </a>
        </div>
    </div>
    </#macro>

<script type="text/javascript">
    $(function () {
        var currentUniqueId = '';
        /*分页加载页面*/
        $(".message_page_list").page({
            total: ${(pagination.getTotalPages())!0},
            current: ${pagination.getNumber()+1},
            jumpCallBack: createPageList
        });

        /*展开或者收起*/
        $(".show_reply_more_box").on('click', function () {
            var _this = $(this);
            var uniqueId = _this.find('span').data('conversation_id');
            currentUniqueId = uniqueId;
            _this.find("span:visible").hide().siblings().show();
            $("#reply_box_" + uniqueId).toggle();
        });

        /*回复  -- 输入框显示或者隐藏*/
        $(".reply_but").on('click', function () {
            var letterId = $(this).data('letter_id');
            var uniqueId = $(this).data('conversation_id');
            currentUniqueId = uniqueId;
            $("#reply_letter_box_" + letterId).toggle();
            $(".reply_content_box_" + letterId).on('keyup', function () {
                var replyContent = $(this).val();
                $(".surplusCount_" + letterId).html($17.wordLengthLimit(replyContent.length, 140));
            });
        });

        /*发送回复*/
        $(".send_letter_but").click(function () {
            var letterId = $(this).data('letter_id');
            var payload = $(".reply_content_box_" + letterId).val();
            App.postJSON("/student/conversation/replyletter.vpage", {letterId: letterId, payload: payload}, function (data) {
                if (data.success) {
                    //回复发送成功后数据更新
                    setTimeout(function(){
                        $.get('/student/conversation/conversations.vpage?currentPage=${pagination.getNumber()+1}', function (data) {
                            $("#message_list_box").html(data);
                            $(".show_reply_more_box span[data-conversation_id="+currentUniqueId+"]").last().show().click().siblings().hide();
                        });
                    },1000);
                    //$17.alert("你的回复已成功提交！");
                } else {
                    $17.alert(data.info);
                }
            });
        });

        /*删除留言*/
        $(".delete_conversation_by_uniqueId").on("click", function () {
            var $this = $(this);
            var deleteMessage = {
                state: {
                    title: "系统提示",
                    html: "确定删除该条留言？",
                    buttons: {"取消": false, "确定": true},
                    position: {width: 400},
                    focus: 1,
                    submit: function (e, v) {
                        e.preventDefault();
                        if (v) {
                            var uniqueId = $this.closest(".student_conversation").data("unique_id");
                            if (!uniqueId) {
                                $.prompt.close();
                                $17.alert("参数错误,请刷新页面重试");
                                return;
                            }
                            App.postJSON('/student/conversation/deleteconversation.vpage', {uniqueId: uniqueId}, function (data) {
                                if (data.success) {
                                    $17.tongji('消息中心-留言板-删除');
                                    $.prompt.close();
                                    $this.closest(".show_val_box").remove();
                                } else {
                                    $17.alert("留言删除失败！");
                                }
                            });
                        } else {
                            $.prompt.close();
                        }
                    }
                }
            };
            $.prompt(deleteMessage);
        });
    });
</script>
<#else>
    <div class="w-noData-box">留言箱没有任何内容</div>
</#if>