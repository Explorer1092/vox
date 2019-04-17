<script id="T:修改异常用户姓名" type="text/html">
    <div class="modifyname-box">
        <p class="tips">系统检测您的姓名存在异常，仅支持2-10位中文名字</p>
        <div class="input-box">
            <span>请修改姓名：</span>
            <input type="text" placeholder="请输入中文姓名" maxlength="10" value="${(currentUser.profile.realname)!}" id="modifyedName">
        </div>
        <p class="tips error" id="errorName"></p>
    </div>
</script>

<script>
    var needSupplementName = '${(isSupplementName!false)?string}'; // 需要修改姓名
    if (needSupplementName === 'true') {
        $.prompt(template("T:修改异常用户姓名", {}), {
            focus: 0,
            title: "系统提示",
            buttons: {"确定": true},
            loaded: function () {
                $('.jqiclose').hide(); // 隐藏关闭按钮
            },
            submit: function (e, v) {
                if (v) {
                    e.preventDefault();
                    var inputName = $.trim($('#modifyedName').val());
                    if (!/^[\u2E80-\uFE4F]+([·•][\u2E80-\uFE4F]+)*$/.test(inputName)) {
                        $('#errorName').text('您输入的姓名不符合规范，请输入2-10位中文名字');
                        return false;
                    }
                    App.postJSON('/ucenter/changName.vpage', {
                        userName: inputName
                    }, function(res){
                        if(res.success){
                            $.prompt.close();
                            setTimeout(function () {
                                $17.alert(res.info, function(){
                                    window.location.href = "/index.vpage";
                                });
                            }, 10);
                        }else{
                            $('#errorName').text(res.info);
                        }
                    });
                }
            }
        });
    }
</script>