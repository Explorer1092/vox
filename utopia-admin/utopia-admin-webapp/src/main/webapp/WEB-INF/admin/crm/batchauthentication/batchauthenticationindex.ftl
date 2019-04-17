<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    span {font: "arial";}
    .index {color: #0000ff;}
    .index, .item {font-size: 18px; font: "arial";}
    .warn {color: red;}
</style>
<div class="span9">
    <fieldset><legend>老师批量认证</legend></fieldset>
    <form action="?" method="post" class="form-horizontal">
        <ul class="inline">
            <li><label>功能说明：绑定用户手机并认证</label></li>
        </ul>
        <ul class="inline">
            <li>
                <label>输入用户ID：<textarea name="teacherId" cols="35" rows="3" placeholder="请以','或空白符隔开">${teacherId!}</textarea></label>
            </li>
            <li>
                <label>输入内容：<textarea name="description" cols="35" rows="3" placeholder="请在这里输入备注，内容不能为空">${description!}</textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button class="btn btn-primary" type="submit">提交</button>
            </li>
        </ul>
        <br/>
        <fieldset>
            <legend>用户ID列表</legend>
            <div class="clear"></div>
            <div id="message_tip"></div>
            <div class="clear"></div>
            <div id="message_list"></div>
        </fieldset>
    </form>
    <script>

        $(function(){
            $('[name="teacherId"]').on('keyup', function(){

                var content = $(this).val();
                var teacherIdList = content.split(/[,，\s]+/);

                var $messageList = $('#message_list');
                $messageList.empty();
                $messageList.append('<br/><ul class="inline"></ul>');

                var $messageTip = $('#message_tip');
                $messageTip.text('');

                var $messageListULNode = $messageList.find('ul');
                var wrongIds = '';

                for(var i = 0, length = teacherIdList.length; i < length; i++) {

                    if(teacherIdList[i] == '') {
                        continue;
                    }

                    if(!teacherIdList[i].match(/^\d+$/)) {
                        if(wrongIds != '') {
                            wrongIds += ','
                        } else {
                            wrongIds += '<span class="warn">提示：</span>';
                        }

                        wrongIds += '<span class="warn">[' + i + ']</span><span>' + teacherIdList[i] + '</span>';
                        $messageListULNode.append('<li><span class="index warn">[' + i + '] </span><span class="item">' + teacherIdList[i] + '</span></li><br/>');
                    } else {
                        $messageListULNode.append('<li><span class="index">[' + i + '] </span><span class="item">' + teacherIdList[i] + '</span></li><br/>');
                    }

                }

                if (wrongIds != '') {
                    $messageTip.append( wrongIds + '<span class="warn"> 不是规范的用户ID</span>');
                }

            });
        });
    </script>
    
</div>
</@layout_default.page>