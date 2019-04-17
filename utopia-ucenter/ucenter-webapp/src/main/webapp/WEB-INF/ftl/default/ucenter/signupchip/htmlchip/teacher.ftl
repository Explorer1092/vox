<#import "../module.ftl" as com>
<@com.page title="老师" t=1>
<h1 class="reg_title">
    <#if dataKey?? && dataKey?has_content>
        <span class="rt">已有一起教育账号？<a href="/ssologinbind.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="clrblue">绑定账号</a></span>
    </#if>
    完善个人信息
</h1>
<div class="reg_step">
    <p class="s_2"></p>
</div>
<#if dataKey?? && dataKey?has_content>
<input name="dataKey" id="dataKey" type="hidden" value="${dataKey}">
<#else>
<input name="dataKey" id="dataKey" type="hidden" value="">
</#if>

<div class="reg_from">
    <ul class="loginbox">
        <li id="fromPort">
            <ul>
                <li class="inp">
                    <b class="tit"><i>*</i> 手机号码(必填)：</b>
                    <input id="mobile" class="require" data-label="手机号" name="mobile" type="text" value="${defUserMobile!''}"  data-role="teacher" autocomplete="off" maxlength="11"/>
                    <span class="hint"></span>
                </li>

                <li class="inp codeinfo">
                    <b class="tit"><i>*</i> 验证码(必填)：</b>
                    <input id="captchaCode" value="请输入右侧数字" class="require" name="verificationCode" type="text" data-label="验证码" data-role="teacher" style="width: 80px;" maxlength="6"/>
                    <img id='captchaImage_1' style="width: 70px; height: 28px; cursor: pointer;"/>
                    <span class="hint"></span>
                </li>

                <#--<li class="pad phoneType" style="padding-bottom: 10px;">
                    没有手机? <a class="register_type_but" data-register_type="email" href="javascript:void (0);" style="color: #39f;"><b>用邮箱注册</b></a>
                </li>-->

                <li class="pad phoneType" style="padding-bottom: 10px;">
                    <a id="getCheckCodeBtn" href="javascript:void(0);" class="reg_btn reg_btn_orange reg_btn_small" >
                        <span>免费获取短信验证码</span>
                    </a>
                    <span class="hint" style="color: #f00;"></span>
                </li>
                <li class="inp phoneType">
                    <b class="tit"><i>*</i> 手机验证码(必填)：</b>
                    <input id="checkCodeBox" class="require" name="checkCodeBox" type="text" data-label="验证码" value="" data-role="teacher" data-content-id="smsCodeBox"/>
                    <span class="hint"></span>
                </li>
                <li class="inp">
                    <b class="tit"><i>*</i> 真实姓名(必填)：</b>
                    <input name="realname" type="text" value="${defUserName!''}" id="realname" data-label="真实姓名" data-role="teacher" class="require" autocomplete="off" required/>
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
                </li>
                <li class="inp">
                    <b class="tit">邀请人(可不填)：</b>
                    <input name="invite" type="text" value="请填写邀请人手机号或者ID" id="invite_info" data-role="teacher" autocomplete="off">
                    <span class="hint"></span>
                </li>-->
            </ul>
        </li>
        <li class="inp txt pad">
            <span class="rememberme">
                <s id="accept_protocol" class="checku"><i></i></s>我已经阅读并接受
                <a class="clrblue" title="用户协议" href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/agreement.vpage" target="_blank">《一起教育用户协议》</a>
                <span class="hint"></span>
            </span>
        </li>
        <li class="inp pad"><a id="register_teacher_btn" href="javascript:void(0);" class="reg_btn submitBtn" style=" width: 134px;">提交</a>
        </li>
    </ul>
    <div class="type_three">
        <span class="tt-icon teacher">
            <strong>我是老师</strong>
            <a id="forback" href="/signup/index.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="reg_btn reg_btn_well">重新选择用户类型</a>
        </span>
    </div>
    <div class="clear"></div>
</div>
<script type="text/html" id="t:手机">
    <li class="inp">
        <b class="tit"><i>*</i> 手机号码(必填)：</b>
        <input id="mobile" name="mobile" class="require" type="text" value="${defUserMobile!''}" data-label="手机号" data-role="teacher" autocomplete="off"/>
        <span class="hint"></span>
    </li>
            <!-- FIXME temp close the email register -->
    <!--<li class="pad phoneType" style="padding-bottom: 10px;">-->
        <!--没有手机? <a class="register_type_but" data-register_type="email" href="javascript:void (0);" style="color: #39f;"><b>用邮箱注册</b></a>-->
    <!--</li>-->

    <li class="pad phoneType" style="padding-bottom: 10px;">
        <a id="getCheckCodeBtn" href="javascript:void(0);" class="reg_btn reg_btn_orange reg_btn_small">
            <span class="text_blue text_small text_normal">免费获取短信验证码</span>
        </a>
        <span class="hint"></span>
    </li>
    <li class="inp phoneType">
        <b class="tit"><i>*</i> 手机验证码(必填)：</b>
        <input id="checkCodeBox" class="require" name="checkCodeBox" type="text" data-label="验证码" value="" data-role="teacher" data-content-id="smsCodeBox"/>
        <span class="hint"></span>
    </li>
    <li class="inp">
        <b class="tit"><i>*</i> 真实姓名(必填)：</b>
        <input name="realname" type="text" value="${defUserName!''}" id="realname" data-label="真实姓名" data-role="teacher" class="require" autocomplete="off" required/>
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
    </li>
    <li class="inp">
        <b class="tit">邀请人(可不填)：</b>
        <input name="invite" type="text" value="请填写邀请人手机号或者ID" id="invite_info" data-role="teacher" autocomplete="off">
        <span class="hint"></span>
    </li>-->
</script>
<script type="text/html" id="t:邮箱">
    <li class="inp">
        <b class="tit"><i>*</i> 注册邮箱(必填)：</b>
        <input id="email" name="email" class="require" type="text" value="${defUserMobile!''}" data-label="邮箱" data-role="teacher" autocomplete="off"/>
        <span class="hint"></span>
    </li>

    <li class="pad phoneType" style="padding-bottom: 10px;">
        没有邮箱? <a class="register_type_but" data-register_type="mobile" href="javascript:void (0);" style="color: #39f;"><b>用手机注册</b></a>
    </li>

    <li class="inp">
        <b class="tit"><i>*</i> 真实姓名(必填)：</b>
        <input name="realname" type="text" value="${defUserName!''}" id="realname" data-label="真实姓名" data-role="teacher" class="require" autocomplete="off" required/>
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
    </li>
    <li class="inp">
        <b class="tit">邀请人(可不填)：</b>
        <input name="invite" type="text" value="请填写邀请人手机号或者ID" id="invite_info" data-role="teacher" autocomplete="off">
        <span class="hint"></span>
    </li>-->
</script>
<script type="text/javascript">
    $(function () {
        $17.tongji("注册1-老师");

        $("#forback").on("click", function(){
            $17.tongji("注册2-老师-重选账号");
        });

        //进入提示框
       <#--$.prompt("<h3>老师您好！</h3><p style='width: 360px; margin: 0 auto; line-height: 24px; padding: 20px 0 0;'>一起作业网<strong style='color: #39f;'>暂不支持初高中全科目及小学语文</strong>！<br/>注册后我们将核实老师的真实身份，并予以认证。</p>", {-->
            <#--title: "提示",-->
            <#--focus : 1,-->
            <#--buttons: { "我不是老师": false, "继续注册": true },-->
            <#--position:{width : 500},-->
            <#--close : function(){-->
                <#--location.href = "/signup/index.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>";-->
            <#--},-->
            <#--submit: function(e, v){-->
                <#--if(!v){-->
                    <#--location.href = "/signup/index.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>";-->
                <#--}else{-->
                    <#--$.prompt.close();-->
                <#--}-->
            <#--}-->
        <#--});-->

        //选择注册方式
        var registerType = 'mobile';
        $(document).on('click','a.register_type_but', function(){
            registerType = $(this).data('register_type');
            if(registerType == 'email'){
                $17.tongji('注册-老师-邮箱');
                $("#fromPort").html(template("t:邮箱", {}));
            }else if(registerType == 'mobile'){
                $17.tongji('注册-老师-手机');
                $("#fromPort").html(template("t:手机", {}));
            }
        });

        var getClickCount = 1;

        $("#getCheckCodeBtn").live("click", function(){
            var $this = $(this);
            $17.tongji('注册2-老师-验证码');

            if($this.hasClass("btn_disable")){
                return false;
            }

            //注册老师-获取验证码
            $17.voxLog({
                module: "reg",
                op : "reg-click-getCode",
                step : 1
            });

            $.post("/signup/tmsignsvc.vpage", {
                mobile: $("#mobile").val(),
                count : getClickCount,
                cid: "${contextId}",
                captchaToken : "${captchaToken!''}",
                captchaCode : $('#captchaCode').val()
            }, function(data){
                var timerCount;
                var timer;
                var second = 60;

                if(data.success){
                    timerCount = second;
                    $this.siblings(".hint").html("验证码已发送") ;
                }else{
                    timerCount = data.timer || null;
                    $this.siblings(".hint").html(data.info);

                    if(timerCount == null) {
                        refreshCaptcha();
                    }
                }

                var smsCodeBox = $("input[data-content-id=smsCodeBox]");
                smsCodeBox.next("span").html("<b class='vox_custom_icon vox_custom_icon_1'></b><span class='text_azure'>请将您手机收到的验证码数字填写到此处</span>");

                if(timerCount == null) {
                    return false;
                }

                getClickCount++;

                timer = $.timer(function() {
                    if(timerCount <= 0){
                        $this.removeClass("btn_disable");
                        $this.find("span, strong").html("重新获取短信验证码");
                        $this.siblings(".init, .hint, .msgInfo").html("");
                        timerCount = second;
                        timer.stop();
                    } else {
                        $this.addClass("btn_disable");
                        $this.find("span, strong").html(--timerCount + "秒之后可重新发送");
                        //小于150S后出现【免费发验证码】 ，一天只能发送两次
                        if(timerCount <= 25 && $17.getCookieWithDefault("STEL") < 2){
                            $this.siblings(".hint").html("<span style='color: #f00;'>收不到验证码？</span><a class='reg_btn reg_btn_small' href='javascript:void(0)' id='serviceCallMe' style='padding:  5px 10px;'>点击人工获取</a>") ;
                        }
                    }
                });
                timer.set({ time : 1000});
                timer.play();
            });
        });

        //1.长时间未收到，点击【致电给我】, 2.Cookie记录每天只能发送2次【致电给我】
        if( $17.isBlank($17.getCookieWithDefault("STEL")) || $17.getCookieWithDefault("STEL") < 2){
            var serviceCallMeCount = 0;
            $("#serviceCallMe").live("click", function(){
                if(serviceCallMeCount < 1){
                    serviceCallMeCount = 1;
                    $.post("/signup/feedback.vpage", {mobile : $("#mobile").val()}, function(data){
                        if(data.success){
                            waitPopup();
                        }else{
                            $17.alert(data.info);
                            serviceCallMeCount = 0;
                        }
                    });
                    //Cookie设置【致电给我】发送次数，最多记录到2
                    $17.setCookieOneDay("STEL", ($17.getCookieWithDefault("STEL")*1) + serviceCallMeCount, 1);
                }else{
                    waitPopup();
                }
                $17.tongji('老师注册-致电给我 点击次数');

                function waitPopup(){
                    $.prompt("<div class='w-ag-center'>请稍候！客服很快将验证码发送到您的手机，验证码1小时内有效。</div>", {
                        title: "系统提示",
                        focus : 1,
                        buttons: { "关闭": false, "体验绘本阅读" : true },
                        position:{width : 500},
                        submit : function(e, v){
                            if(v){
                                var dataFrame = '<object width="900" height="600" data="http://cdn-cc.17zuoye.com/resources/apps/flash/Reading.swf?_=20141024172856" type="application/x-shockwave-flash"><param name="movie" value="http://cdn-cc.17zuoye.com/resources/apps/flash/Reading.swf?_=20141024172856"><param name="allowScriptAccess" value="always"><param name="allowFullScreen" value="true"><param name="flashvars" value="isPreview=0&gameDataURL=http%3A%2F%2Fwww.17zuoye.com%2Fappdata%2Fflash%2FReading%2Fobtain-ENGLISH-4445.vpage&nextHomeWork=closeReviewWindow&tts_url=http%3A%2F%2Fwww.17zuoye.com%2Ftts.vpage&isTeacher=1&imgDomain=http%3A%2F%2Fcdn-cc.17zuoye.com%2F&domain=http%3A%2F%2Fwww.17zuoye.com"><param name="wmode" value="opaque"></object>'
                                setTimeout(function(){
                                    $.prompt('<div style="margin: -20px 0 0;">'+ dataFrame +'</div>', {
                                        title    : "体验绘本阅读",
                                        buttons  : {},
                                        position : { width: 940}
                                    });
                                }, 100);
                                $17.tongji("老师注册-体验绘本阅读");
                            }
                        }
                    });
                }
            });
        }

        $(".help_but").live('click',function(){
            $17.tongji('注册2-老师-在线客服');
        });

        $(document).on("focus", "#invite_info", function(){
            if($(this).val() == "请填写邀请人手机号或者ID"){
                $(this).val("");
            }
        });

        $(document).on("blur", "#invite_info", function(){
            if($17.isBlank($(this).val())){
                $(this).val("请填写邀请人手机号或者ID");
            }
        });

        /** 注册 */
        $('#register_teacher_btn').on('click', function () {
            var _this = $(this);
            var success = validate();
            var mobileId = $("#mobile");
            var emailId = $("#email");
            var realNameId = $("#realname");
            var passwordId = $("#password");
            var inviteInfoId = $('#invite_info');
            var inviteInfoIdVal = inviteInfoId.val();
            var checkCodeBoxId = $("#checkCodeBox");
            var dataKey = $("#dataKey");

            if(inviteInfoIdVal == "请填写邀请人手机号或者ID"){
                inviteInfoIdVal = "";
            }

            if (success) {
                if(realNameId.val().length > 20) {
                    realNameId.parent().addClass("err").removeClass("cor");
                    realNameId.siblings(".hint").text("请不要使用过长的名称。");
                    return false;
                }

                if(!$17.isBlank(inviteInfoIdVal) && !$17.isNumber(inviteInfoIdVal)){
                    inviteInfoId.parent().addClass("err").removeClass("cor");
                    inviteInfoId.siblings(".hint").text("邀请人请填写邀请人学号，学号为数字。");
                    return false;
                }

                var data = null;

                //注册老师-提交注册
                $17.voxLog({
                    module: "reg",
                    op : "reg-click-submitBtn",
                    step : 2
                });

                // 密码随机生成
                var randomNum = "";
                for(var i=0;i<6;i++) {
                    randomNum += Math.floor(Math.random()*10);
                }

                if(registerType == "mobile"){
                    data = {
                        role            : 'ROLE_TEACHER',
                        userType        : 1,
                        mobile          : mobileId.val(),
                        code            : checkCodeBoxId.val(),
                        realname        : realNameId.val(),
                        password        : randomNum,
                        registerType    : 0,
                        inviteInfo      : inviteInfoIdVal,
                        dataKey         : dataKey.val()
                    };

                    $17.tongji("注册2-老师-手机方式提交");

                    App.postJSON('/signup/msignup.vpage', data, function (data) {
                        if (data && data.success) {
                            setTimeout(function(){
                                location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage";
                            }, 500);
                        } else {
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
                    }, function (data) {
                        $17.alert("网络请求失败，请稍等重试或者联系客服");
                    });
                }else{
                    data = {
                        role            : 'ROLE_TEACHER',
                        userType        : 1,
                        email           : emailId.val(),
                        realname        : realNameId.val(),
                        password        : randomNum,
                        registerType    : 0,
                        inviteInfo      : inviteInfoIdVal,
                        dataKey         : dataKey.val()
                    };

                    $17.tongji("注册2-老师-邮箱方式提交");

                    App.postJSON('/signup/esignsvl.vpage', data, function (data) {
                        if (data && data.success) {
                            setTimeout(function(){
                                location.href = "/signup/sendemailsuccess.vpage?email=" + data.email;
                            }, 500);
                        } else {
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
                    }, function (data) {
                        $17.alert("网络请求失败，请稍等重试或者联系客服");
                    });
                }
            }
        });

        function refreshCaptcha() {
            $("#captchaCode").val("");
            $('#captchaImage_1').attr('src', "/captcha?" + $.param({
                'module': 'regCaptcha',
                'token': '${captchaToken!0}',
                't': new Date().getTime()
            }));
        }

        refreshCaptcha();

        $(document).on("click", "#captchaImage_1", function(){
            refreshCaptcha();
        });
    })
</script>
</@com.page>