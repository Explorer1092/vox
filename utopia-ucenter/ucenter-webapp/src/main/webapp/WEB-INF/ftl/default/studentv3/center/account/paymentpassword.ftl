<#macro newtext><#if hasPaymentPassword?? && hasPaymentPassword>新的</#if></#macro>
<div class="tb-box">
    <div class="t-center-list">
        <#if hasPaymentPassword?? && hasPaymentPassword>
            <div class="tf-left w-fl-left">
                <span class="w-detail w-right"></span>
            </div>
            <div class="tf-center w-fl-left">
                <p class="w-green">支付密码：已设置</p>
                <p>设置支付密码，可以保障你的学豆安全。  </p>
            </div>
            <div class="tf-right w-fl-left">
                <a class="w-btn-dic w-btn-gray-new accountBut" data-box_type="paymentpassword" href="javascript:void(0);">修改支付密码</a>
            </div>
        <#else>
            <div class="tf-left w-fl-left">
                <span class="w-detail w-wrong"></span>
            </div>
            <div class="tf-center w-fl-left">
                <p class="w-red">支付密码：未设置</p>
                <p>设置支付密码，可以保障你的学豆安全。   </p>
            </div>
            <div class="tf-right w-fl-left">
                <a class="w-btn-dic w-btn-green-new accountBut" data-box_type="paymentpassword" href="javascript:void(0);">设置支付密码</a>
            </div>
        </#if>
        <div class="w-clear"></div>
    </div>
    <div class="w-form-table accountBox" data-box_type="paymentpassword" style="display: none;">
        <#if hasPaymentPassword?? && !hasPaymentPassword>
            <div class="" style="border: 1px solid #ccc; background-color: #fdffdf; color: #666; line-height: 24px; padding: 10px 10px;">
                支付密码是指你在消费学豆时候输入的密码，与登录密码不同，设置支付密码对登录没有任何影响。设置以后，你每次消费学豆就输入此密码，可以保障你的学豆安全
            </div>
        </#if>
        <div id="step1">
            <dl style="width: auto;">
                <#if hasPaymentPassword?? && hasPaymentPassword>
                    <dt>当前支付密码：</dt>
                    <dd style="margin-bottom: 10px;">
                        <input id="currentPaymentPassword" data-label="当前支付密码" type="password" value="" class="w-int require">
                        <a id="forgetPP" class="w-btn-dic w-btn-gray-normal" href="javascript:void(0);">忘记支付密码？</a>
                        <span class="w-form-misInfo w-form-info-error"></span>
                    </dd>
                </#if>
                <dt><@newtext />支付密码：</dt>
                <dd>
                    <input id="newPaymentPassword" data-label="支付密码" type="password" value="" class="w-int require">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </dd>
                <dt>确认<@newtext />支付密码：</dt>
                <dd>
                    <input id="confirmNewPaymentPassword" data-label="确认支付密码" type="password" value="" class="w-int require">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </dd>
                <dd>
                    <a id="paymentPasswordSubmitBut" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">保存</a>
                </dd>
            </dl>
        </div>
        <div id="step2" style="padding: 40px; line-height: 60px; text-align: center; display: none;">
            <div style="border-bottom: 1px solid #ddd;">
                <strong class="text_well inline_block">找回支付密码</strong>
                <span style="width: 70%;">
                    <strong>1.手机验证</strong>
                    <span style="width: 180px;" class="inline_block">2.找回支付密码</span>
                </span>
            </div>
            <div class="spacing_vox text_center">
                您需要下载家长通或者关联家长账号才能设置支付密码！
            </div>
            <div class="spacing_vox text_center">
                <a class="w-btn-dic w-btn-green-new" href="/student/center/account.vpage?updateType=mobile">
                    <strong><span>去绑定手机</span></strong>
                </a>
                <a class="w-btn-dic w-btn-green-new" href="/student/center/account.vpage?updateType=parent">
                    <strong><span>去关联家长手机</span></strong>
                </a>
            </div>
        </div>
        <div id="step3" style="padding: 40px; line-height: 20px; display: none;">
            <div class="" style="border-bottom: 1px solid #ddd; text-align: center; line-height: 60px;">
                <strong>找回支付密码</strong>
                <span style="width: 70%;">
                    <strong>1.手机验证</strong>
                    <span style="width: 180px;">2.找回支付密码</span>
                </span>
            </div>
            <dl>
                <dd>
                    <a class="w-btn-dic w-btn-gray-normal" href="javascript:void(0);" id="getVerifyCode_payment"><span>免费获取短信验证码</span></a>
                    <span class="infoBox" style="color: #828282; font-size: 12px;"></span>
                </dd>
                <dt>输入短信验证码：</dt>
                <dd>
                    <input id="step3_settVerifyCode" data-label="验证码" type="text" value="" class="w-int require">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </dd>
                <dd>
                    <a id="step3_submit_but_pre" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">上一步</a>
                    <a id="step3_submit_but" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">下一步</a>
                </dd>
            </dl>
        </div>

        <div id="step4" style="padding: 40px; line-height: 20px; display: none;">
            <div style="border: 1px solid #ccc; background-color: #fdffdf; color: #666; line-height: 24px; padding: 10px 10px;">
                支付密码是指你在消费学豆时候输入的密码，与登录密码不同，设置支付密码对登录没有任何影响。设置以后，你每次消费学豆就输入此密码，可以保障你的学豆安全
            </div>
            <dl>
                <dt>设置新的支付密码：</dt>
                <dd>
                    <input id="newPaymentPassword_reset" data-label="支付密码" type="password" value="" class="w-int require">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </dd>
                <dt>确认支付密码：</dt>
                <dd>
                    <input id="confirmNewPaymentPassword_reset" data-label="确认支付密码" type="password" value="" class="w-int require">
                    <span class="w-form-misInfo w-form-info-error"></span>
                </dd>
                <dd>
                    <a id="paymentPasswordSubmitBut_reset" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">保存</a>
                </dd>
            </dl>
        </div>

    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#paymentPasswordSubmitBut").on('click', function(){
            var currentPaymentPassword = $("#currentPaymentPassword").val();
            var newPaymentPassword = $("#newPaymentPassword").val();
            var confirmNewPaymentPassword = $("#confirmNewPaymentPassword").val();
            var postUrl = '/student/center/setpp.vpage';

            var success = validate("#step1");
            if(success){
                <#if hasPaymentPassword?? && hasPaymentPassword>
                    postUrl = '/student/center/changepp.vpage';
                </#if>

                var dataArray = {
                <#if hasPaymentPassword?? && hasPaymentPassword>
                    oldPwd  : currentPaymentPassword,
                </#if>
                    pwd1    : newPaymentPassword,
                    pwd2    : confirmNewPaymentPassword
                };

                $.post(postUrl, dataArray, function (data) {
                    if (data.success) {
                        $.prompt("设置<@newtext />支付密码成功。", {
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

        //忘记支付密码
        $("#forgetPP").on('click', function(){
            $.get('/student/center/forgetpp.vpage', function(data){
                //data.success  通过手机找回密码  false 去绑定手机
                $("#step1").hide();
                if(data.success){
                    $("#step3").show();
                }else{
                    $("#step2").show();
                }
            });
        });

        //找回支付密码 发送验证码
        $(document).on('click','#getVerifyCode_payment', function(){
            var $this = $(this);
            if($this.hasClass("btn_disable")){
                return false;
            }

            $.post("/student/center/sendmppvc.vpage", {}, function(data){
                $this.siblings('span.infoBox').html(data.info);
                $17.getSMSVerifyCode($this, data);
            });
        });

        //数据提交
        $(document).on('click', '#step3_submit_but', function(){
            var code = $('#step3_settVerifyCode').val();
            var success = validate("#step3");
            if(success){
                $.post("/student/center/verifymppvc.vpage", { code : code}, function(data){
                    if(data.success){
                        $("#step3").hide();
                        $("#step4").show();
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        });

        //step3_submit_but_pre
        $(document).on('click', '#step3_submit_but_pre', function(){
            $("#step1").show();
            $("#step3").hide();
        });

        //重置支付密码
        $("#paymentPasswordSubmitBut_reset").on('click', function(){
            var NewPaymentPassword = $("#newPaymentPassword_reset").val();
            var ConfirmNewPaymentPassword = $("#confirmNewPaymentPassword_reset").val();

            var success = validate("#step4");
            if(success){
                $.post("/student/center/resetpp.vpage", {pwd1 : NewPaymentPassword, pwd2 : ConfirmNewPaymentPassword}, function (data) {
                    if (data.success) {
                        $.prompt("设置支付密码成功。", {
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