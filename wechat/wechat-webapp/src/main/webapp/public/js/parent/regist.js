/*
 * 账号注册
 */
define(["jquery","$17"], function ($,$17) {
    var childrenId = $('#children_id'), childrenPassword = $('#children_password'),
        nextBtn = $('#next_but'),
        woid = $('#woid');
    nextBtn.on('click', function () {
        if($17.isBlank(childrenId.val())){
            $17.msgTip("账号不能为空");
            childrenId.focus();
            return false;
        }

        if($17.isBlank(childrenPassword.val())){
            $17.msgTip("密码不能为空");
            return false;
        }
        $.post('/signup/parent/regist.vpage', {
            woid : woid.val(),
            sid : childrenId.val(),
            pwd : childrenPassword.val()
        }, function (data) {
            if (data.success) {
                location.href = '/parent/homework/index.vpage';
            } else {
                $17.msgTip(data.info);
            }
        });
    });

});