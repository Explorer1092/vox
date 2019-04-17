    <div class="tc-person-single">
        <div class="baseinfo-box">
            <p class="label">登录密码</p>
            <div class="edit-box accountBut" data-box_type="password">
                <span>修改密码</span>
                <i></i>
            </div>
        </div>
        <div class="modify-box accountBox" data-box_type="password" style="display:none;">
            <#--手机号已绑定-->
            <#if mobileVerified!false>
                <div class="modify-onebox">
                    <label for="">验证手机：</label>
                    <p class="info">${mobile!''}</p>
                    <div class="send-btn" id="sendVerifyCode"><span>发送验证码</span></div>
                </div>
                <div class="modify-onebox">
                    <label for="">验证码：</label>
                    <input id="verify_code" class="single-input w-int require" maxlength="6" type="verifycode" data-label="验证码" placeholder="请输入验证码">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </div>
                <div class="modify-onebox">
                    <label for="">设置新密码：</label>
                    <input id="user_new_password" class="single-input w-int require" maxlength="16" type="password" data-label="新密码" placeholder="请输入1-16位任意字符、字母区分大小写">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </div>
                <div class="modify-onebox">
                    <label for="">确认新密码：</label>
                    <input id="user_new_again_password" class="single-input w-int require" maxlength="16" type="password" data-label="确认密码" placeholder="请输入新密码">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </div>
                <div class="sure-box" id="reset_user_password_button">确定</div>
            <#else>
            <#--手机号未绑定-->
                <div class="no-bindphone-box">
                    <p>请先绑定手机，以免修改密码后遗忘不能找回</p>
                    <a class="accountBut v-studentVoxLogRecord" data-op="bindMobile" data-box_type="mobile" href="javascript:void(0);">立即去绑定手机</a>
                </div>
            </#if>
        </div>
    </div>

<script type="text/javascript">
    $(function(){
        $("#sendVerifyCode").on('click', function () {
            var $this = $(this);
            if($this.hasClass("btn_disable")){return false;}

            $.post("/student/center/sendTCPWcode.vpage", {
            }, function(data){
                getSMSVerifyCode($this, data);
            });
        });
        $("#reset_user_password_button").on('click',function(){
            var success = validate("div[data-box_type='password']");
            if(success){
                var verifyCode = $.trim($("#verify_code").val());
                var newPwd = $.trim($("#user_new_password").val());
                $.post(' /ucenter/resetpwbycode.vpage', {
                    verify_code: verifyCode,
                    new_password: newPwd,
                    user_type: 3
                }, function (data) {
                    if (data.success) {
                        $.prompt("密码修改成功。", {
                            title: "系统提示",
                            buttons: { "知道了": true },
                            close: function(){
                                $("#logout").click();
                            }
                        });
                    } else {
                        $17.alert(data.info);
                    }
                });
            }
        });
    });
</script>