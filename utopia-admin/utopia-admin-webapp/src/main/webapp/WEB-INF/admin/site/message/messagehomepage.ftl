<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {font: "arial";}
    .index {color: #0000ff;}
    .index, .item {font-size: 18px; font: "arial";}
    .warn {color: red;}
</style>
<div class="span9">
    <fieldset>
        <legend>系统消息</legend>
        <ul class="inline">
            <li>
                <label>使用链接例子：&lt;a href="http://www.17zuoye.com"&gt;一起作业&lt;/a&gt;</label>
            </li>
            <li>
                <label>效果：<a href="http://www.17zuoye.com">一起作业</a></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>输入用户ID：<textarea name="receiveUserId" cols="35" rows="3" placeholder="请以','或空白符隔开"></textarea></label>
            </li>
            <li>
                <label>输入内容：<textarea name="messageContent" cols="35" rows="3" placeholder="请在这里输入要发送的内容"></textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>全局消息：<input type="checkbox" name="globalMessage">，</label>
            </li>
            <li>
               <label>老师消息：<input type="checkbox" name="teacherMessage"/>，</label>
            </li>
            <li>
                <label>家长消息：<input type="checkbox" name="parentMessage"/>，</label>
            </li>
            <li>
                <label>学生消息：<input type="checkbox" name="studentMessage"/></label>
            </li>
            <li>
                <label>大使消息：<input type="checkbox" name="ambassadorMessage"/></label>
            </li>
            <li>
                <a class="btn" href="messagelist.vpage">查询</a>
            </li>
            <li>
                <button class="btn btn-primary" id="submit_button">提交</button>
            </li>
        </ul>
    </fieldset>
    <br/>
    <fieldset>
        <legend>用户ID列表</legend>
        <div class="clear"></div>
        <div id="message_tip"></div>
        <div class="clear"></div>
        <div id="message_list"></div>
    </fieldset>
</div>
<script>

    $(function(){
        $('[name="receiveUserId"]').on('keyup', function(){

            var content = $(this).val();
            var userIdList = content.split(/[,，\s]+/);

            var $messageList = $('#message_list');
            $messageList.empty();
            $messageList.append('<br/><ul class="inline"></ul>');

            var $messageTip = $('#message_tip');
            $messageTip.text('');

            var $messageListULNode = $messageList.find('ul');
            var wrongIds = '';

            for(var i = 0, length = userIdList.length; i < length; i++) {

                if(userIdList[i] == '') {
                    continue;
                }

                if(!userIdList[i].match(/^\d+$/)) {
                    if(wrongIds != '') {
                        wrongIds += ','
                    } else {
                        wrongIds += '<span class="warn">提示：</span>';
                    }

                    wrongIds += '<span class="warn">[' + i + ']</span><span>' + userIdList[i] + '</span>';
                    $messageListULNode.append('<li><span class="index warn">[' + i + '] </span><span class="item">' + userIdList[i] + '</span></li><br/>');
                } else {
                    $messageListULNode.append('<li><span class="index">[' + i + '] </span><span class="item">' + userIdList[i] + '</span></li><br/>');
                }

            }

            if (wrongIds != '') {
                $messageTip.append( wrongIds + '<span class="warn"> 不是规范的用户ID</span>');
            }

        });

        $('#submit_button').on('click', function() {
            var postData = {
                receiveUserId       :   $('[name="receiveUserId"]').val(),
                messageContent      :   $('[name="messageContent"]').val(),
                globalMessage       :   $('input[name="globalMessage"]').prop('checked'),
                teacherMessage      :   $('input[name="teacherMessage"]').prop('checked'),
                parentMessage       :   $('input[name="parentMessage"]').prop('checked'),
                studentMessage      :   $('input[name="studentMessage"]').prop('checked'),
                ambassadorMessage   :   $('input[name="ambassadorMessage"]').prop('checked')
            };
            $.post('?', postData, function(data) {
                alert(data.info);
                if(data.success) {
                    location.href = "?";
                }
            });
        });
    });
</script>
</@layout_default.page>