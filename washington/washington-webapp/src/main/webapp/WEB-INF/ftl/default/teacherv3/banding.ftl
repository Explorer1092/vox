<!--getGold-->
<div id="getGold">
    <div class="alpha_back"></div>
    <div class="alpha_content_layer" style="margin: -235px 0 0 -270px;">
        <div class=" <#if userIntegral?has_content>getGold<#else>releaseAlert</#if>">
            <#if userIntegral?has_content>
                <p class="title_number">
                    <#list 1..userIntegral?length as number>
                        <span class="number number_${userIntegral?substring(number-1,number)}"></span>
                    </#list>
                </p>
            <#else>
                <p class="nav">${(currentUser.profile.realname)!}</p>
                <div class="title">
                    绑定后可用手机登录，同时还可以保障账号安全，防止您的学生信息泄露导致账号被盗！
                </div>
            </#if>

            <div class="setgoldForm">
                <dl class="horizontal_vox">
                    <dt>手机号码：</dt>
                    <dd>
                        <input id="mobile_box" placeholder="绑定手机后可以用于登录和找回密码" class="int_vox" />
                        <a id="send_validate_code" class="number freebtn" href="javascript:void (0);"><span>免费获取短信验证码</span></a>
                        <br/><span></span>
                    </dd>
                    <dt>验证码：</dt>
                    <dd>
                        <input id="captcha_box" placeholder="请把你收到短信中的数字输入此处" class="int_vox" />
                        <br/><span></span>
                    </dd>
                </dl>
                <div class="text_center"><a id="submit_but" class="number keep" href="javascript:void (0);"></a></div>
            </div>
            <a id="close_box_but" href="javascript:void (0);" class="number close"></a>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        //来自检查作业和测验的 不记录cookie
        <#if !(userIntegral?has_content)>$.cookie("tb", 1, { expires : 1 });</#if>


        /*获取验证码*/
        $("#send_validate_code").on('click', function(){
            var $this = $(this);
            var mobileBox = $("#mobile_box");
            if(!$17.isMobile(mobileBox.val())){
                mobileBox.siblings('span').text('输入正确的手机号');
                return false;
            }else{
                mobileBox.siblings('span').text("");
            }

            if($this.hasClass("btn_disable"))return false;

            $.post("/teacher/center/sendmobilecode.vpage?mobile="+mobileBox.val(), function(data){
                if(data.success){
                    $this.siblings("span").removeClass('text_red').text('验证码已发送');
                }else{
                    $this.siblings("span").text(data.info);
                }
                $17.getSMSVerifyCode($this, data);
            });


        });

        //数据提交
        $("#submit_but").on('click', function(){
            var mobileBox = $("#mobile_box");
            var captchaBox = $("#captcha_box");
            if(!$17.isBlank(mobileBox.val())){
                if(!$17.isMobile(mobileBox.val())){
                    mobileBox.siblings('span').text('输入正确的手机号');
                    return false;
                }else{
                    mobileBox.siblings('span').text("");
                }
                if($17.isBlank(captchaBox.val())){
                    captchaBox.siblings('span').text('输入短信验证码');
                    captchaBox.focus();
                    return false;
                }else{
                    captchaBox.siblings('span').text("");
                }
            }else{
                mobileBox.siblings('span').text('输入正确的手机号');
                mobileBox.focus();
                return false;
            }

            $.post('/teacher/center/validatemobile.vpage?latestCode='+captchaBox.val())
                    .done(function(data){
                        if(data.success){
                            $17.tongji("未绑定弹窗-老师绑定");
                            $("#getGold").hide();
                            $17.alert(data.info, function(){
                                setTimeout(function(){
                                    location.href = "/teacher/index.vpage";
                                }, 100);
                            });
                        }else{
                            captchaBox.siblings('span').text(data.info);
                            captchaBox.focus();
                        }
                    })
                    .fail(function(){
                        $17.alert('数据提交失败！');
                    });

        });
        //关闭弹窗
        $("#close_box_but").on('click', function(){
            $("#getGold").hide();
        });
    });
</script>
