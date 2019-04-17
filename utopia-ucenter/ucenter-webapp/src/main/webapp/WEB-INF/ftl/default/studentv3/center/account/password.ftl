<div class="tb-box">
    <div class="t-center-list">
        <div class="tf-left w-fl-left">
            <span class="w-detail w-right"></span>
        </div>
        <div class="tf-center w-fl-left">
        <#if pwdState?? && pwdState == 1>
            <p class="w-green">登录密码：已设置</p>
        <#else>
            <p class="w-red">登录密码：未设置</p>
        </#if>
            <p>安全性高的密码，可以使账号更安全。</p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-gray-new accountBut" data-box_type="password" href="javascript:void(0);">修改密码</a>
        </div>
        <div class="w-clear"></div>
    </div>
    <div class="w-form-table accountBox" data-box_type="password" style="display: none;">
        <#if mobileVerified!false>
        <dl>
            <dt>初始密码 ：</dt>
            <dd>
                <input id="user_current_password" type="password" value="" data-label="初始密码" class="w-int require">
                <span class="w-form-misInfo w-form-info-error"></span>
            </dd>
            <dt>新密码 ：</dt>
            <dd>
                <input id="user_new_password" type="password" value="" data-label="新密码" class="w-int require">
                <span class="w-form-misInfo w-form-info-error"></span>
            </dd>
            <dt>确认新密码 ：</dt>
            <dd style="margin-bottom: 30px;">
                <input id="user_new_again_password" type="password" value="" data-label="确认新密码" class="w-int require">
                <span class="w-form-misInfo w-form-info-error"></span>
            </dd>
            <dd>
                <a id="reset_user_password_button" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">保存</a>
            </dd>
        </dl>
        <#else>
            <div style=" text-align: center; padding: 30px;">
                <p style="background-color: #fff2c9; margin-bottom: 10px; border-radius: 6px; line-height: 36px; color: #ca3438; font-size: 14px;">请先绑定手机，以免修改密码后遗忘不能找回</p>
                <p>
                    <a class="w-btn-dic w-btn-green-well accountBut v-studentVoxLogRecord" data-op="bindMobile" data-box_type="mobile" href="javascript:void(0);">立即去绑定手机</a>
                </p>
            </div>
        </#if>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $("#reset_user_password_button").on('click',function(){
            var success = validate("div[data-box_type='password']");

            if(success){
                var currentPwd = $("#user_current_password").val();
                var newPwd = $("#user_new_password").val();
                var nextPwd = $("#user_new_again_password").val();
                $.post('/ucenter/resetmypw.vpage', {current_password: currentPwd, new_password: newPwd}, function (data) {
                    if (data.success) {
                        $.prompt("密码修改成功。", {
                            title: "系统提示",
                            buttons: { "知道了": true },
                            submit: function(){
                                window.location.href = "/student/center/account.vpage";
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