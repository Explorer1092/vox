<#if pagination?? && pagination.getTotalElements() gt 0 >
    <#list pagination.getContent() as message >
        <div class="acs-list t-notColorBlack">
            <div class="listRight fr">
                <a href="javascript:;" data-letter_id="${message.id!}" data-delete_type="deleteSystemMsg" class="delete_content_but green_fontBtn blue_fontBtn fr">删除</a>
                <p class="t-3">${(message.createTime)?number_to_datetime}</p>
            </div>
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image07.png"/>"></div>
            <div class="acs-title <#if message.status == "UNREAD">bold</#if>" data-message_id="${message.id!}">
                <p class="t-1">系统消息</p>
                <p class="t-2">${message.payload!}</p>
            </div>
        </div>
    </#list>
    <#--todo 翻页样式集成-->
    <div class="system_message_page_list message_page_list" style="float:right;"></div>
    <script type="text/javascript">
        $(function () {
            $17.tongji("老师-系统消息","老师-系统消息");
            $(".system_message_page_list").page({
                total: ${pagination.getTotalPages()!0},
                current: ${currentPage!},
                jumpCallBack: loadMessage.loadSystemMessage
            });

            /*删除信息*/
            $(".delete_content_but").on("click", function () {
                var $this = $(this);
                var letterId = $this.data("letter_id");
                var deleteType = $this.data("delete_type");
                var postUrl = null;
                var postData = null;
                if (deleteType == "deleteSystemMsg") {
                    postUrl = '/teacher/message/deleteSysMessage.vpage?messageId='+letterId;
                    postData = {};
                } else {
                    postUrl = '/teacher/conversation/deleteconversation.vpage';
                    postData = {uniqueId: letterId};
                }
                $.prompt("<div style='text-align: center; padding: 30px 0 10px;'>确定删除该条信息吗?</div>",{
                    title : "系统提示",
                    buttons : {"取消" : false ,"确定" : true},
                    position : {width : 400},
                    focus : 1,
                    submit : function(e,v){
                        e.preventDefault();
                        if(v){
                            App.postJSON(postUrl, postData, function (data) {
                                if (data.success) {
                                    $.prompt.close();
                                    $this.closest(".acs-list").remove();
                                } else {
                                    $17.alert("参数错误,请刷新页面重试");
                                }
                            });
                        }else{
                            $.prompt.close();
                        }
                    }
                });
            });

            //标记已读
            $(".bold").on('click', function(){
                var $this = $(this);
                var megId = $this.data('message_id');
                if(!$this.hasClass('bold')){return false;}
                $.get('/teacher/message/mark.vpage?messageId='+megId, function(data){
                    if(data.success){
                        $this.removeClass('bold');
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
