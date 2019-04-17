<#if pagination?? && pagination.getTotalElements() gt 0 >
    <#list pagination.getContent() as message >
    <div class="t-notice-box <#if message.status == "UNREAD">t-notColorBlack</#if>" data-message_id="${message.id!}">
        <div class="w-fl-left tn-send">
            <span class="tn-letter tn-icon tn-icon-1"></span>
        </div>
        <div class="tn-info w-fl-left tn-info-w">
            <p>系统消息</p>
            ${message.payload!}
            <p class="w-gray w-ag-right w-magT-10">${(message.createTime)?number_to_datetime}</p>
        </div>
        <div class="tn-btn w-fl-right">
            <a href="javascript:void (0)" data-letter_id="${message.id!}" data-delete_type="deleteSystemMsg" class="delete_content_but w-blue">删除</a>
        </div>
    </div>
    </#list>
    <div class="system_message_page_list message_page_list" style="float:right; padding:0 0 10px;"></div>
    <script type="text/javascript">
        $(function () {
            $17.tongji("老师-系统消息","老师-系统消息");
            $(".system_message_page_list").page({
                total: ${pagination.getTotalPages()!0},
                current: ${currentPage!},
                jumpCallBack: loadMessage.loadSystemMessage
            });

            //标记已读
            $(".t-notColorBlack").on('click', function(){
                var $this = $(this);
                var megId = $this.data('message_id');
                if(!$this.hasClass('t-notColorBlack')){return ;}
                $.get('/teacher/message/mark.vpage?messageId='+megId, function(data){
                    if(data.success){
                        $this.removeClass('t-notColorBlack');
                        // 加载时已标记已读
//                        $.get("/teacher/bubbles.vpage",function(data){
//                            if((data.pendingApplicationCount + data.unreadNoticeCount + data.unreadLetterAndReplyCount) == 0){
//                                $(".v-msg-count").hide();
//                            }else{
//                                $(".v-msg-count").text(data.pendingApplicationCount + data.unreadNoticeCount + data.unreadLetterAndReplyCount).show();
//                            }
//                        });
                    }else{
                        $17.alert('系统消息标记已读失败');
                    }
                });
            });
        });
    </script>
<#else>
    <div class="w-noData-box">没有系统消息</div>
</#if>
