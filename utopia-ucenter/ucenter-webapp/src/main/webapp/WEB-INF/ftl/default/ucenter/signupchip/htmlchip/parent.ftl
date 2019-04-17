<#import "../module.ftl" as com>
<@com.page title="家长" t=3>
    <h1 class="reg_title">
        <#if dataKey?? && dataKey?has_content>
            <span class="rt">已有一起教育账号？<a href="/ssologinbind.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="clrblue">绑定账号</a></span>
        </#if>
        完善个人信息
    </h1>
    <div class="reg_step">
        <#--<p class="s_2"></p>-->
    </div>

<div class="reg_from">
    <div style="float: right; width: 400px; padding-right: 150px; margin-top:35px;">
        <div class="scanning-sweep-box">
            <div class="sw-box" style="border-bottom: none;">
                <p><span class=sw-bg></span></p>
                <p class="font-big">家长：随时陪伴孩子学习</p>
            </div>
        </div>
    </div>

    <div class="type_three">
        <span class="tt-icon parents">
            <strong>我是家长</strong>
            <a id="forback" href="/signup/index.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="reg_btn reg_btn_well">重新选择用户类型</a>
        </span>
    </div>
    <div class="clear"></div>
</div>

<script type="text/html" id="t:手机">
    <li class="inp">
        <b class="tit"><i>*</i> 手机号码(必填)：</b>
        <input id="mobile" name="mobile" class="require" type="text" value="" data-label="手机号" data-role="parent" autocomplete="off"/>
        <span class="hint"></span>
    </li>

    <li class="pad phoneType" style="padding-bottom: 10px;">
        没有手机? <a class="register_type_but" data-register_type="email" href="javascript:void (0);" style="color: blue;"><b>用邮箱注册</b></a>
    </li>

    <li class="pad phoneType" style="padding-bottom: 10px;">
        <a id="getCheckCodeBtn" href="javascript:void(0);" class="reg_btn reg_btn_orange reg_btn_small" style="padding: 5px 10px;">
            <span>免费获取短信验证码</span>
        </a>
        <span class="hint"></span>
    </li>
    <li class="inp phoneType">
        <b class="tit"><i>*</i> 手机验证码(必填)：</b>
        <input id="checkCodeBox" class="require" name="checkCodeBox" type="text" data-label="验证码" value="" data-role="parent" data-content-id="smsCodeBox"/>
        <span class="hint"></span>
    </li>
    <li class="inp">
        <b class="tit"><i>*</i> 真实姓名(必填)：</b>
        <input name="realname" type="text" value="" id="realname" data-label="真实姓名" data-role="parent" class="require" autocomplete="off" required/>
        <span class="hint"></span>
    </li>
   <#-- <li class="inp">
        <b class="tit"><i>*</i> 设置密码(必填)：</b>
        <input name="password" type="password" value="" id="password" data-label="密码" class="require" autocomplete="off" required/>
        <span class="hint"><i></i></span>
    </li>
    <li class="inp">
        <b class="tit"><i>*</i> 确认密码(必填)：</b>
        <input name="verify_password" type="password" value="" data-label="确认密码" class="require" id="verify_password" autocomplete="off" required/>
        <span class="hint"><i></i></span>
    </li>-->
</script>

<script type="text/html" id="t:邮箱">
    <li class="inp">
        <b class="tit"><i>*</i> 注册邮箱(必填)：</b>
        <input id="email" name="email" class="require" type="text" value="" data-label="邮箱" data-role="parent" autocomplete="off"/>
        <span class="hint"></span>
    </li>

    <li class="pad phoneType" style="padding-bottom: 10px;">
        没有邮箱? <a class="register_type_but" data-register_type="mobile" href="javascript:void (0);" style="color: blue;"><b>用手机注册</b></a>
    </li>

    <li class="inp">
        <b class="tit"><i>*</i> 真实姓名(必填)：</b>
        <input name="realname" type="text" value="" id="realname" data-label="真实姓名" data-role="parent" class="require" autocomplete="off" required/>
        <span class="hint"></span>
    </li>
    <#--<li class="inp">
        <b class="tit"><i>*</i> 设置密码(必填)：</b>
        <input name="password" type="password" value="" id="password" data-label="密码" class="require" autocomplete="off" required/>
        <span class="hint"><i></i></span>
    </li>
    <li class="inp">
        <b class="tit"><i>*</i> 确认密码(必填)：</b>
        <input name="verify_password" type="password" value="" data-label="确认密码" class="require" id="verify_password" autocomplete="off" required/>
        <span class="hint"><i></i></span>
    </li>-->
</script>

<script type="text/javascript">
    function errorTipShow(data){
        var attrs = data.attributes;
        if(attrs){
            $.each(attrs, function (key, value) {
                var el = $('#' + key);
                if (el.length > 0) {
                    el.parent().addClass('err');
                    el.siblings("span").html("<i></i>" + value);
                } else {
                    if (attrs.none) {
                        $17.alert(attrs.none);
                    }
                }
            });
        }else{
            $17.alert(data.info);
        }
    }

    $(function () {
        //初始化(默认手机注册)
        $("#fromPort").html(template("t:手机", {}));
        $("#mobile").focus();

        //选择注册方式
        var registerType = 'mobile';
        $(document).on('click','a.register_type_but', function(){
            registerType = $(this).data('register_type');
            if(registerType == 'email'){
                $17.tongji('注册-家长-邮箱');
                $("#fromPort").html(template("t:邮箱", {}));
                $("#email").focus();
            }else if(registerType == 'mobile'){
                $17.tongji('注册-家长-手机');
                $("#fromPort").html(template("t:手机", {}));
                $("#mobile").focus();
            }
        });


        $("#getCheckCodeBtn").live("click", function(){
            var $this = $(this);
            var mobile = $("#mobile").val();
            $17.tongji('注册2-家长-验证码');

            if($this.hasClass("btn_disable")){
                return false;
            }

            if(!$17.isMobile(mobile)){
                $this.siblings(".hint").html('请输入正确的手机号码');
                return false;
            }

            $.post("/signup/pmsignsvc.vpage", {mobile : mobile, cid: "${contextId}"}, function(data){
                var timerCount;
                var timer;
                var second = 60;

                if(data.success){
                    timerCount = second;
                    $this.siblings(".hint").html("验证码已发送") ;
                }else{
                    timerCount = data.timer || null;
                    $this.siblings(".hint").html(data.info);
                }

                var smsCodeBox = $("input[data-content-id=smsCodeBox]");
                smsCodeBox.next("span").html("<b class='vox_custom_icon vox_custom_icon_1'></b><span class='text_azure'>请将您手机收到的验证码数字填写到此处</span>");

                if(timerCount == null) {
                    return false;
                }

                timer = $.timer(function() {
                    if(timerCount <= 0){
                        $this.removeClass("btn_disable");
                        $this.find("span, strong").html("免费获取短信验证码");
                        $this.siblings(".init, .hint, .msgInfo").html("");
                        timerCount = second;
                        timer.stop();
                    } else {
                        $this.addClass("btn_disable");
                        $this.find("span, strong").html(--timerCount + "秒之后可重新发送");
                        if(timerCount <= 30){
                            $this.siblings(".hint").html("长时间收不到验证码请致电<@ftlmacro.hotline/>") ;
                        }
                    }
                });
                timer.set({ time : 1000});
                timer.play();
            });
        });

        /** 注册 */
        $('#register_parent_btn').on('click', function () {
            var success = validate();

            var mobileId = $("#mobile");
            var emailId = $("#email");
            var realNameId = $("#realname");
            var passwordId = $("#password");
            var checkCodeBoxId = $("#checkCodeBox");

            if (success) {
                if(realNameId.val().length > 20) {
                    realNameId.parent().addClass("err").removeClass("cor");
                    realNameId.siblings(".hint").text("请不要使用过长的名称。");
                    return false;
                }

                var data = null;
                // 密码随机生成
                var randomNum = "";
                for(var i=0;i<6;i++) {
                    randomNum += Math.floor(Math.random()*10);
                }

                if(registerType == "mobile"){

                    data = {
                        role            : 'ROLE_PARENT',
                        userType        : 2,
                        mobile          : mobileId.val(),
                        code            : checkCodeBoxId.val(),
                        realname        : realNameId.val(),
                        password        : randomNum,
                        registerType    : 0
                    };

                    $17.tongji("注册2-家长-手机方式提交");

                    App.postJSON('/signup/msignup.vpage', data, function (data) {
                        if (data && data.success) {
                            setTimeout(function(){
                                location.href = "/parent/regsucc.vpage";
                            }, 500);
                        } else {
                            errorTipShow(data);
                        }
                    });
                }else{
                    data = {
                        role            : 'ROLE_PARENT',
                        userType        : 2,
                        email           : emailId.val(),
                        realname        : realNameId.val(),
                        password        : randomNum,
                        registerType    : 0
                    };

                    $17.tongji("注册2-家长-邮箱方式提交");

                    App.postJSON('/signup/esignsvl.vpage', data, function (data) {
                        if (data && data.success) {
                            setTimeout(function(){
                                location.href = "/signup/sendemailsuccess.vpage?email=" + data.email;
                            }, 500);
                        } else {
                            errorTipShow(data);
                        }
                    });
                }
            }
        });

        /*统计*/
        $17.tongji("注册1-家长");

        $("#forback").on("click", function(){
            $17.tongji("注册2-家长-重选账号");
        });

        $(".help_but").live('click',function(){
            $17.tongji('注册2-家长-在线客服');
        });
    })
</script>
</@com.page>