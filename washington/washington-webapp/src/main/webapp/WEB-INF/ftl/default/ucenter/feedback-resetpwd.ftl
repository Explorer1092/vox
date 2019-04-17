<html>
<head>
    <@sugar.capsule js=["jquery"] />
    <style>
        body { font-size: 14px; margin:0;padding: 0; background-color: white; }
        a { text-decoration: none; }
        div.row { margin: 20px 0 20px 0; }
        div.row span.field_desc { display: inline-block; width: 120px;}
        span.text_blue { color: #39f; }
        #feedbackSubType-list label { display: inline-block; width: 190px; margin: 5px 0; }
    </style>
</head>
<body>
<div>
    <div id='page-feedback'>
        <div class="row">
            <span class="field_desc">我是：</span>
            <label for="role-student"><input id="role-student" type="radio" name="role" value="学生">学生</option></label>
            <label for="role-teacher"><input id="role-teacher" type="radio" name="role" value="老师">老师</option></label>
            <label for="role-parent"><input id="role-parent" type="radio" name="role" value="家长">家长</option></label>
        </div>
        <div id="feedbackSubType-container" class="row">
            <span class="field_desc">遇到的问题：</span>
            <div id="feedbackSubType-list" style="margin: 5px 0; background: #eee;">
            </div>
        </div>
        <div class="row">
            请描述您遇到的具体问题，以便我们更好的为您提供服务：<br />
            <textarea style="display: block; clear: both; width: 100%; height: 54px;" name="content" maxlength="200"></textarea>
            （紧急问题请拨打客服电话 <span class='text_blue'><@ftlmacro.hotline /></span>）
        </div>
        <div class="row" style="text-align: center;">
            <button id="submit" style="width: 160px; height: 30px;">发送反馈</button>
        </div>
    </div>
</div>
<script>
    (function(){
        var postData = {};

        $(function(){
            var $feedbackSubTypeList = $('#feedbackSubType-list');

            var subTypes = [
                '忘记学号', '没有绑定手机/邮箱', '操作复杂不会用', '收不到短信验证码', '收不到验证邮件', '其他'
            ];
            var count = 0;
            for(var i = 0; i < subTypes.length; i ++) {
                var text = subTypes[i];
                var tmpId = 'feedbackSubTypeId' + count;
                var $radio = $('<input id="' + tmpId + '" type="radio" name="feedbackSubType" />').val(text);
                var $label = $('<label for="' + tmpId + '" />').append($radio).append(text);
                $feedbackSubTypeList.append($label);
                count++;
            }




            var $submit = $("#submit");
            var submit_text = $submit.text();
            $submit.click(function() {
                postData.feedbackType = '重置密码';
                postData.feedbackSubType1 = $('input[name=role]:checked').val();
                postData.feedbackSubType2 = $('input[name=feedbackSubType]:checked').val();
                postData.content = $.trim($('textarea[name=content]').val());

                if(!postData.feedbackSubType2 && !postData.content) {
                    alert('请写一下您的问题');
                    $('textarea[name=content]').focus();
                    $('textarea[name=content]').highlight();
                    return false;
                }

                $submit.prop('disabled', true);
                $submit.text('发送反馈中 ...');
                $.post('/ucenter/feedback.vpage', postData)
                        .done(function() {
                            $submit.text('反馈发送成功');
                            if(window.parent && window.parent.$ && window.parent.$.prompt)
                                window.parent.$.prompt.close();
                        }).fail(function(){
                            alert('反馈发送失败，请重试');
                            $submit.prop('disabled', false);
                            $submit.text(submit_text);
                        });

                return false;
            });
        });

    })();

    $(function() {
        $("textarea[maxlength]").bind('input propertychange', function() {
            var $this = $(this);
            var maxLength = $this.attr('maxlength');
            if ($this.val().length > maxLength) {
                $this.val($this.val().substring(0, maxLength));
            }
        });
    });

</script>
</body></html>
