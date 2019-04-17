<#import "module.ftl" as center>
<@center.studentCenter currentPage='account'>
    <div class="t-center-box w-fl-right">
        <span class="center-rope"></span>
        <span class="center-rope center-rope-1"></span>
        <div class="t-center-safe">
            <#-- 密码 -->
            <#include 'account/password.ftl' />
            <#--手机绑定-->
            <#include 'account/mobile.ftl' />

            <#--绑定邮箱-->
            <#--<#include 'account/email.ftl' />-->

            <#--密保 encrypted-->
            <#--<#include 'account/security.ftl' />-->

            <#--关联家长账号-->
            <#include 'account/parent.ftl' />

            <#--支付密码-->
            <#include 'account/paymentpassword.ftl' />

            <#--解除QQ绑定-->
            <#if qq?size gt 0>
                <#include 'account/qq.ftl' />
            </#if>
        </div>
    </div>

    <script type="text/javascript">
        $(function(){

            //根据选择类型  展开对应的输入框
            $(".accountBut").on('click', function(){
                var boxType = $(this).data('box_type');
                //设置支付密码
                if(boxType == 'paymentpassword' && $(this).hasClass('w-btn-green-new')){
                    <#if ((studentParentStatus.getStatus())?? && studentParentStatus.getStatus() != 2) && (mobileVerified?? && !mobileVerified)>
                        $17.alert("您需要下载家长通或者关联家长账号才能设置支付密码！");
                        return false;
                    </#if>
                }

                $('.accountBox').slideUp().filter('.accountBox[data-box_type='+boxType+']').toggle();
            });

            //根据updateType  展开对应的修改/设置选择框
            <#if updateType?has_content >
                var button = $(".accountBut[data-box_type=${updateType}]");
                var buttonTop = button.offset().top;
                $('html, body').animate({scrollTop: buttonTop - 50}, 1000);
                setTimeout(function(){button.trigger('click');},1100);
            </#if>

            $("input").on("focus blur change", function (e) {
                var $this = $(this);
                var notice = "";
                var idType = $this.attr("id");
                var span = $this.siblings('span');
                var errorIcon = '<i class="w-spot w-icon-error"></i>';
                var successIcon = '<i class="w-spot w-icon-success"></i>';
                var condition = true;
                var errorMessage = "";
                var user_new_password = $("#user_new_password");
                var user_new_again_password = $("#user_new_again_password");
                var errorClass = 'w-int-error';

                if(e.type != 'blur'){
                    switch (idType) {
                        case "parentname" :
                            var value = $this.val().replace(/\s+/g, "");
                            condition = !(value.match(/[^\u4e00-\u9fa5]/g));
                            errorMessage = "请输入家长真实姓名,须为中文";
                            break;

                        case "user_current_password":

                            notice = '填写初始密码';
                            break;
                        case "user_new_password":
                            if ($this.val().length > 16) {
                                errorMessage = "密码不可超过16位";
                                condition = false;
                            }
                            notice = '填写新密码';
                            break;
                        case "user_new_again_password":
                            condition = (user_new_password.val() == $this.val());
                            if (!$17.isBlank(user_new_password.val())) {
                                if (condition && $this.val() < 16) {
                                    user_new_again_password.html(successIcon);
                                }
                            }

                            errorMessage = "密码填写不一致，请重新填写";
                            notice = '再次填写新密码';
                            break;
                        case "confirmNewPaymentPassword_reset":
                            var newPaymentPassword_reset = $("#newPaymentPassword_reset");
                            condition = (newPaymentPassword_reset.val() == $this.val());
                            if (!$17.isBlank(newPaymentPassword_reset.val())) {
                                if (condition && $this.val() < 16) {
                                    $("#confirmNewPaymentPassword_reset").html(successIcon);
                                }
                            }
                            errorMessage = "密码填写不一致，请重新填写";
                            notice = '再次填写新密码';

                            break;

                        case "confirmNewPaymentPassword":
                            var newPaymentPassword = $("#newPaymentPassword");
                            condition = (newPaymentPassword.val() == $this.val());
                            if (!$17.isBlank(newPaymentPassword.val())) {
                                if (condition && $this.val() < 16) {
                                    $("#confirmNewPaymentPassword").html(successIcon);
                                }
                            }
                            errorMessage = "密码填写不一致，请重新填写";
                            notice = '再次填写新密码';

                            break;
                        case "mobile":
                        case "mobile_text":
                        case "edit_mobile_text":

                            condition = $17.isMobile($this.val());
                            errorMessage = "请填写正确的手机号码";
                            notice = "请输入手机号，验证通过后可用于登录、找回密码";
                            break;
                        case "email_box":
                            condition = $17.isEmail($this.val());
                            errorMessage = "请填写正确格式的邮箱";
                            notice = "请输入常用邮箱，验证通过后可用于登录和找回密码";
                            break;
                        default:

                            break;
                    }
                }

                if (e.type == "focus") {

                } else if (e.type == "blur") {

                } else if (e.type == "change") {
                    if (!$17.isBlank($this.val())) {
                        if (!condition) {
                            span.html(errorIcon + '<div class="info">'+errorMessage+'</div>');
                            $this.addClass(errorClass);
                        } else {
                            span.html(successIcon);
                            errorMessage = "";
                            $this.removeClass(errorClass);
                        }
                    } else {
                        if($this.hasClass("require")){
                            errorMessage = $this.data("label") + '不可为空';
                            span.html(errorIcon + '<div class="info">'+errorMessage+'</div>');
                            $this.addClass(errorClass);
                        }
                    }
                }
            });
        });

        //target下的input验证
        function validate(target){
            var $inputs;
            var $errorInputs;
            $inputs = $(target).find("input.w-int");
            $inputs.each(function(){
                if(!$17.isBlank($(this).attr("id"))){
                    $(this).trigger("change");
                }
            });
            $errorInputs = $(target).find("input.w-int-error:visible");
            if($errorInputs.size() == 0){
                return true;
            }else{
                return false;
            }
        }
    </script>
</@center.studentCenter>