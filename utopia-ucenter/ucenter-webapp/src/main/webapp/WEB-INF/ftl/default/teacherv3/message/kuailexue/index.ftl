<div class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">消息中心</div>
    </div>
    <div id="v-sysMessage" class="accountSecurity-box messageList">
    </div>

</div>

<script type="text/javascript">
    function changeUnreadCount($this, obj, text){
        $.get('/teacher/bubbles.vpage', function(data){
            if(data.success){
                $this.text(data[obj] + text);
                $(".unreadConversationCount").text(data.pendingApplicationCount + data.unreadNoticeCount + data.unreadLetterAndReplyCount);
                if(data.pendingApplicationCount==0 && data.unreadNoticeCount==0 && data.unreadLetterAndReplyCount==0){
                    $("#popinfo").hide();
                }
            }
        });
    }

    <#--用于获得分页中的回调函数-->
    var loadMessage = null;

    $(function () {
        loadMessage = new $17.Model({
            tabTarget: $("#v-sysMessage")
        });
        loadMessage.extend({
            showHideContent: function (type, scope) {
                $(".systemMessage, .parentMessage, .studentMessage", scope).hide("fast").filter(function () {
                    return $(this).hasClass(type);
                }).show("fast");
            },
            loadSystemMessage: function (pageIndex) {
                $(".systemMessage").html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                $.get("/teacher/message/list.vpage?currentPage="+pageIndex, function (data) {
                    $("#v-sysMessage").html(data);
                });
            },
            init: function () {
                var $this = this;

                <#--$this.tabTarget.on("click", function () {-->
                    <#--var $that = $(this);-->

                    <#--if (!$that.data("is_load")) {-->
                        <#--$this.loadSystemMessage(1);-->
                        <#--$that.data("is_load", true);-->
                    <#--}-->
                <#--});-->

                <#--$this.tabTarget.eq(${userType!0}).trigger("click");-->
                $this.loadSystemMessage(1);
            }
        }).init();
    });

    /*回复提交后 回调*/
    function reply_list_complete($this) {
        $this.parent().find(".search_reply_box").slideToggle();
        $this.find("span").toggle();
    }

    $(function () {
        LeftMenu.changeMenu();
        LeftMenu.focus("message");

    });

    /**
     * ------------------------------------------------------------------------------------------
     */
    $(function () {
        //log
        $17.voxLog({
            module: 'message',
            op:  'load'
        },'teacher');
    });
</script>