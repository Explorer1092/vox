<#import "guideLayout.ftl" as temp />
<@temp.page>
<div class="build_head_box build_head_box_isInvite">
    <div class="aph-back"></div>
    <div class="build_head_main">
        <a href="/login.vpage" class="logo"></a>
        <div style="width: 570px">
            <h2>恭喜您注册成功！</h2>
        </div>
        <div class="w-form-table" id="setPasswordContainer">
            <h3>设置新的密码，以便您下次登录</h3>
            <dl>
                <dt>账号(手机号) </dt>
                <dd>
                    <input type="text" style="border: none;" class="w-int" value="${currentUserProfileMobile!''}" readonly="readonly">
                </dd>
                <dt>新的登录密码：</dt>
                <dd>
                    <input type="password" id="newPassword" class="w-int" value="" maxlength="18">
                    <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info">请输入新密码</strong></span>
                </dd>
                <dt>确认登录密码：</dt>
                <dd>
                    <input type="password" id="newPasswordConfirm" class="w-int" value="" maxlength="18">
                    <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info">请确认新密码</strong></span>
                </dd>
                <dd class="form-btn">
                    <a class="w-btn" style="width: 160px; margin: 0;" id="confirm_validate_code" href="javascript:void(0);">确定</a>
                </dd>
            </dl>
        </div>
        <script type="text/javascript">
            $(function(){
                $("#confirm_validate_code").on("click", function(){
                    var newPassword = $("#newPassword");
                    var newPasswordVal = newPassword.val();
                    var newPasswordConfirm = $("#newPasswordConfirm");
                    var newPasswordConfirmVal = newPasswordConfirm.val();

                    if( $17.isBlank(newPasswordVal)){
                        newPassword.addClass("w-int-error");
                        newPassword.siblings(".errorMsg").show();
                        return false;
                    }

                    if( $17.isBlank(newPasswordConfirmVal) ){
                        newPasswordConfirm.addClass("w-int-error");
                        newPasswordConfirm.siblings(".errorMsg").show().find(".info").html("请输入确认新密码");
                        return false;
                    }

                    if( newPasswordVal != newPasswordConfirmVal){
                        newPasswordConfirm.addClass("w-int-error");
                        newPasswordConfirm.siblings(".errorMsg").show().find(".info").html("密码不一致，请填写输入!")
                        return false;
                    }

                    $.post("/ucenter/setmypw.vpage", {
                        new_password : newPasswordVal
                    }, function(data){
                        if(data.success){
                            $17.alert("新密码设置成功", function(){
                                location.href = "/teacher/index.vpage";
                            });

                            $17.voxLog({
                                module: "crmRegLogin",
                                op : "guide-click-setNewPassword"
                            });
                        }else{
                            $17.alert(data.info);
                        }
                    });
                });

                $("#setPasswordContainer input").on("keydown", function(){
                    $(this).removeClass("w-int-error");
                    $(this).siblings(".errorMsg").hide();
                });
            });
        </script>
    </div>
    <div class="build_head_back"></div>
</div>
</@temp.page>