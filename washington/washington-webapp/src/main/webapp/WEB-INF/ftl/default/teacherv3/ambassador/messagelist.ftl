<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <div class="w-base" style="margin-top: 15px;">
        <div class="w-base-title">
            <h3>通知中心</h3>
        </div>
        <div class="w-base-container">
            <!--//start-->
            <div class="systemMessage" style="display: block;">
                <#if messageList?has_content>
                    <#list messageList as mesg>
                        <div class="t-notice-box ${(mesg.status == "UNREAD")?string("t-notColorBlack", "")}" data-message_id="${mesg.id}">
                            <div class="w-fl-left tn-send">
                                <span class="tn-letter tn-icon tn-icon-1"></span>
                            </div>
                            <div>
                                <#--<p>系统消息</p>-->
                                ${mesg.payload}
                                <p class="w-gray w-ag-right w-magT-10">${mesg.createTime?number_to_datetime}</p>
                            </div>
                        </div>
                    </#list>
                <#else>
                    <div class="t-notice-box" style="border: none; text-align: center; padding: 150px 0;">
                        暂无通知
                    </div>
                </#if>
            </div>
            <!--end//-->
            <div class="w-clear"></div>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            //标记已读
            $(document).on('click', '.t-notColorBlack', function(){
                var $this = $(this);
                var megId = $this.data('message_id');
                if(!$this.hasClass('t-notColorBlack')){return false;}
                $.get('/teacher/message/mark.vpage?messageId='+megId, function(data){
                    if(data.success){
                        $this.removeClass('t-notColorBlack');
                    }else{
                        $17.alert('系统消息标记已读失败');
                    }
                });
            });
        });
    </script>
</@temp.page>

