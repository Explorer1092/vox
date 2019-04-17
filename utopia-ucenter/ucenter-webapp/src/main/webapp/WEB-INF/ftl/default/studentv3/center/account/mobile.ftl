<#macro newText><#if mobile??>新</#if></#macro>
<div class="tb-box">
    <div class="t-center-list">
    <#if mobileVerified?? && mobileVerified>
        <div class="tf-left w-fl-left">
            <span class="w-detail w-right"></span>
        </div>
        <div class="tf-center w-fl-left">
            <p class="w-green">手机绑定：已绑定</p>
            <p>手机绑定后，您即可享受手机登录，手机找回密码等服务。</p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-gray-new accountBut" data-box_type="mobile" href="javascript:void(0);">修改手机</a>
        </div>

    <#else>
        <div class="tf-left w-fl-left">
            <span class="w-detail w-wrong"></span>
        </div>
        <div class="tf-center w-fl-left">
            <p class="w-red">手机绑定：未设置</p>
            <p>手机绑定后，您即可享受手机登录，手机找回密码等服务。</p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-green-new accountBut v-studentVoxLogRecord" data-op="bindMobile" data-box_type="mobile" href="javascript:void(0);">绑定手机</a>
        </div>
    </#if>
        <div class="w-clear"></div>
    </div>
    <div class="w-form-table accountBox" data-box_type="mobile" style="display: none;">
        <dl style="width: auto;">
            <#if mobile??>
                <dt>原手机号码：</dt>
                <dd>${mobile!''}</dd>
            </#if>
            <dt><@newText />手机号码：</dt>
            <dd class="validateMobile" style="margin-bottom: 10px;">
                <input id="mobile" data-label="手机号码" type="text" value="" class="w-int require">
                <span class="w-form-misInfo w-form-info-error"></span>
            </dd>
            <dd>
                <a class="w-btn-dic w-btn-gray-normal" href="javascript:void(0);" id="getVerifyCode"><span>免费获取短信验证码</span></a>
                <span class="getVerifyCodeInfo" style="font-size: 12px; color: #828282;"></span>
            </dd>
            <dt>短信验证码：</dt>
            <dd>
                <input id="settVerifyCode" data-label="验证码" type="text" value="" class="w-int require">
                <span class="w-form-misInfo w-form-info-error"></span>
            </dd>
            <dd>
                <a id="bindingPhoneSubmit" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">保存</a>
            </dd>
        </dl>
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

            var success = validate("div[data-box_type='mobile'] dd.validateMobile");

            if(success){
                if($this.hasClass("btn_disable")){return false;}

                $.post("/student/center/sendmobilecode.vpage", {
                    mobile: mobileId.val()
                }, function(data){
                    $this.siblings("span.getVerifyCodeInfo").html(data.info) ;
                    $17.getSMSVerifyCode($this, data);
                });
            }
        });

        bindingPhoneSubmit.on("click", function(){
            var success = validate("div[data-box_type='mobile']");
            if(success){
                $.post("/student/center/validatemobile.vpage", { latestCode : settVerifyCode.val()}, function(data){
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