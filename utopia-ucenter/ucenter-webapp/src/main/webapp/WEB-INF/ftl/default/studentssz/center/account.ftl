<#import "module.ftl" as center>
<@center.studentCenter currentPage='account'>
    <div class="t-center-box w-fl-right">
        <div class="t-center-data">
            <div class="t-center-title">账号安全</div>
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
                <#--<#include 'account/parent.ftl' />-->

                <#--支付密码-->
                <#--<#include 'account/paymentpassword.ftl' />-->

                <#--解除QQ绑定-->
                <#--<#if qq?size gt 0>-->
                    <#--<#include 'account/qq.ftl' />-->
                <#--</#if>-->
            </div>
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

                foldCard(boxType);
            });

            //根据updateType  展开对应的修改/设置选择框
            <#if updateType?has_content >
                var button = $(".accountBut[data-box_type=${updateType}]");
                var buttonTop = button.offset().top;
                $('html, body').animate({scrollTop: buttonTop - 50}, 1000);
                setTimeout(function(){button.trigger('click');},1100);
            </#if>
        });
    </script>
</@center.studentCenter>