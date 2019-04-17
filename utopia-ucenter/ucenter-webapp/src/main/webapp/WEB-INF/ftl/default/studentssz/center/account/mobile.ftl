<#macro newText><#if mobile??>新</#if></#macro>
<div class="tc-person-single">
    <div class="baseinfo-box">
        <p class="label">手机绑定</p>
        <div class="edit-box accountBut" data-box_type="mobile">
            <span>更换手机</span>
            <i></i>
        </div>
    </div>
    <div class="modify-box accountBox" data-box_type="mobile" style="display:none;">
        <#if mobile??>
            <div class="modify-onebox">
                <label for="">原号码：</label>
                <p class="info">${mobile!''}更换后，原号码不能再作为登录使用</p>
            </div>
        </#if>
        <div class="modify-onebox validateMobile">
            <label for=""><@newText />手机号：</label>
            <input id="mobile" type="text"  maxlength="11" data-label="手机号" class="single-input small-input w-int require" placeholder="请输入新的手机号">
            <div class="operate-btn" id="getVerifyCode"><span>发送验证码</span></div>
            <span class="w-form-misInfo w-form-info-error"></span>
        </div>
        <div class="modify-onebox">
            <label for="">验证码：</label>
            <input id="settVerifyCode" type="text" data-label="验证码" maxlength="6" class="single-input w-int require" placeholder="请输入验证码">
            <span class="w-form-misInfo w-form-info-error"></span>
        </div>
        <div id="bindingPhoneSubmit" class="sure-box">确定</div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        var mobileId = $("#mobile");
        var getVerifyCode = $("#getVerifyCode");
        var settVerifyCode = $("#settVerifyCode");
        var bindingPhoneSubmit = $("#bindingPhoneSubmit");

        getVerifyCode.on("click", function(){
            var $this = $(this);
            var success = validate("div[data-box_type='mobile'] .validateMobile");

            if(success){
                if($this.hasClass("btn_disable")){return false;}
                $.post("/student/center/sendmobilecode.vpage", {
                    mobile: $.trim(mobileId.val())
                }, function(data){
                    getSMSVerifyCode($this, data);
                });
            }
        });

        bindingPhoneSubmit.on("click", function(){
            var success = validate("div[data-box_type='mobile']");
            if(success){
                $.post("/student/center/validatemobile.vpage", { latestCode : $.trim(settVerifyCode.val())}, function(data){
                    if(data.success){
                        $17.alert("绑定手机成功", function(){
                            setTimeout(function(){
                                window.location.href = "/student/center/account.vpage";
                            }, 100);
                        });
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        });

    });
</script>