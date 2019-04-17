<#import "module.ftl" as temp>
<@temp.messagePage>
<div class="t-center-box w-fl-right">
    <div class="t-messages-data">
        <div class="t-messages-title">
            <div class="title-inner-back">
                <#--<a class="w-change-btn w-fl-right" href="javascript:void (0)" title="写消息">写消息</a>-->
                通知
            </div>
        </div>
        <div id="message_list_box" class="t-messages-list"><#--内容显示区--></div>
        <div class="message_page_list"><#--分页--></div>
    </div>
</div>

<script type="text/javascript">
    function createPageList(index) {
        var message_list_box = $("#message_list_box");
        message_list_box.html('<div style="color: #666666; padding-top: 55px; text-align: center;">数据加载中...</div>');
        $.get('/student/message/list.vpage?currentPage='+index, function (data) {
            message_list_box.html(data);
        }).fail(function(){
            message_list_box.html("数据加载失败，请重试");
        });
    }

    $(function () {
        /*消息中心初始化*/
        createPageList(1);

        //log
        $17.voxLog({
            module: 'message',
            op:  'load'
        },'student');
    });
</script>
</@temp.messagePage>

