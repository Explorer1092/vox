<div class="tb-box">
    <div class="t-center-list">
    <#if emailVerified?? && emailVerified>
        <div class="tf-left w-fl-left">
            <span class="w-detail w-right"></span>
        </div>
        <div class="tf-center w-fl-left">
            <p class="w-green">邮箱绑定：已绑定</p>
            <p>绑定后，可以通过邮箱登录一起作业，以及找回密码 </p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-gray-new accountBut" data-box_type="email" href="javascript:void(0);">修改邮箱</a>
        </div>

    <#else>
        <div class="tf-left w-fl-left">
            <span class="w-detail w-wrong"></span>
        </div>
        <div class="tf-center w-fl-left">
            <p class="w-red">邮箱绑定：未设置</p>
            <p>绑定后，可以通过邮箱登录一起作业，以及找回密码 </p>
        </div>
        <div class="tf-right w-fl-left">
            <a class="w-btn-dic w-btn-green-new accountBut" data-box_type="email" href="javascript:void(0);">绑定邮箱</a>
        </div>
    </#if>
        <div class="w-clear"></div>
    </div>
    <div class="w-form-table accountBox" data-box_type="email" style="display: none;">
        <dl id="email_show_box" style="width: auto;">
            <#if email?has_content>
                <dt>原邮箱地址：</dt>
                <dd>${email}</dd>
            </#if>
            <dt><#if email?has_content>新</#if>邮箱地址：</dt>
            <dd>
                <input id="email_box" type="text"  data-label="邮箱地址" value="" class="w-int require">
                <span class="w-form-misInfo w-form-info-error"></span>
            </dd>
            <dd>
                <a id="email_submit" href="javascript:void(0);" class="w-btn-dic w-btn-green-new">保存</a>
            </dd>
        </dl>

        <div id="email_success_box" style="line-height: 28px; text-align: center; display: none;">
            <i class="v_icon v_icon_tg"></i> <span class="text">邮箱修改成功</span>
            <div>
                请在您的邮箱里点击链接激活邮箱
            </div>
            <a class="w-btn-dic w-btn-green-new" href="/student/center/account.vpage"><strong>好</strong></a>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $("#email_box").focus();
        //发送邮件
        $("#email_submit").on('click', function(){
            var email = $("#email_box");
            var success = validate("div[data-box_type='email']");
            if(success){
                $.post('/student/center/sendvalidateEmail.vpage',{email : email.val()}, function(data){
                    if(data.success){
                        $('#email_show_box').hide();
                        $('#email_success_box').show();
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        });
    })
</script>