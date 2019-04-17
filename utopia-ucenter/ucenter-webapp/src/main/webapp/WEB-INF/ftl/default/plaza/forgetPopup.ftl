<div id="forgetInfoPopup"></div>
<script type="text/javascript">
    $(function(){
        var forgetInfoPopup = $("#forgetInfoPopup");
        var _tempToken = "";
        var _tempMobile = "";
        var _tempStep = 1;
        var $currentUserId = $17.getCookieWithDefault('voxlastname');

        if( $17.getHashQuery("error") &&  $17.getHashQuery("type")){
            _tempMobile = $17.getHashQuery("mobile");
            _tempToken = $17.getHashQuery("captchaToken");

            $17.hashCallback(["error", "type"], function(obj){
                $(".JS-login-main").trigger('click');

                switch (obj.type){
                    case "mobile" :
                        forgetInfoPopup.html( template("T:忘记密码提示", {mobile : _tempMobile}) );

                        setTimeout(function(){
                            forgetInfoPopup.find("[name='mobile']").focus();
                            refreshCaptcha(_tempToken);
                        }, 200);

                        $17.voxLog({
                            userId : $currentUserId,
                            module : "loginInfoError",
                            op : "load-popup-mobile",
                            step : _tempStep
                        });
                        break;
                    case "wechat" :
                        forgetInfoPopup.html( template("T:有绑定二维码", {}) );
                        $17.voxLog({
                            userId : $currentUserId,
                            module : "login-NewForgotPassword",
                            op : "load-popup-WeChat",
                            step : _tempStep
                        });
                        break;
                    case "junior" :
                        forgetInfoPopup.html( template("T:初中学号提示", {}) );
                        $17.voxLog({
                            userId : $currentUserId,
                            module : "loginInfoError",
                            op : "load-popup-junior",
                            step : _tempStep
                        });
                        break;
                    case "studentForbidden" :
                        $17.voxLog({
                            userId : $currentUserId,
                            module : "loginInfoError",
                            op : "studentForbidden"
                        }, 'student');
                        baseErrorForgot('<p style="margin-top: -10px;">账号异常暂时无法登录，若有疑问请联系客服 400-160-1717</p>');
                        break;
                    case "forbidden" :
                        baseErrorForgot('<p style="margin-top: -10px;">账号异常暂时无法登录，若有疑问请联系客服 400-160-1717</p>');
                        break;
                    case "infant" :
                        $17.voxLog({
                            userId : $currentUserId,
                            module : "loginInfoError",
                            op : "infant"
                        }, 'student');
                        baseErrorForgot('<p style="margin-top: -10px;">学前阶段用户请使用APP登录哦！</p>');
                        break;
                }
            });
        }else{
            $17.hashCallback(["error"], function(obj){
                $(".JS-login-main").trigger('click');
                baseErrorForgot('<p style="margin-top: -10px;">登录失败！<br/><a href="/ucenter/resetnavigation.vpage?ref=popup" target="_blank" class="info">忘记学号/密码了？</a></p>');

                $17.voxLog({
                    userId : $currentUserId,
                    module : "login-OldForgotPassword",
                    op : "load-popup-old",
                    step : _tempStep
                });

                if( $17.getHashQuery("record") == "true" ){
                    //七天内登录过的记录日志
                    $17.voxLog({
                        userId : $currentUserId,
                        module : "login-NewForgotPassword",
                        op : "sevenDays-login"
                    });
                }
            });
        }

        //第一步找回密码 submit
        var _returnToken = "";
        $(document).on("click", ".v-forgotPassword-1", function(){
            var $this = $(this);
            var $forgotModule = $("[data-forgot-type='1']");
            var $mobile = $forgotModule.find("input[name='mobile']").val();
            var $code = $forgotModule.find("input[name='code']").val();
            var success = validateForget("[data-forgot-type='1']");

            if(!success){
                return false;
            }

            $.post("/ucenter/mrpwds1.vpage", {
                mobile : $mobile,
                captchaToken : _tempToken,
                captchaCode : $code
            }, function(data){
                if(data.success){
                    _tempMobile = $mobile;
                    _returnToken = data.token;
                    if(data.needUserType){
                        stepHasShow(1, 2);
                        _tempStep = 2;
                    }else{
                        stepHasShow(1, 3);
                        _tempStep = 3;
                        getSMSVerifyCode($(".v-forgotResendCode"), data, data.timer);
                    }

                }else{
                    refreshCaptcha(_tempToken);
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-step1-submit",
                step : _tempStep
            });
        });

        //第二步找回密码 submit
        $(document).on("click", ".v-forgotPassword-2", function(){
            var $this = $(this);
            var $userType = $this.attr("data-type");

            if( $17.isBlank($userType) ){
                return false;
            }

            $.post("/ucenter/mrpwds2.vpage", {
                token : _returnToken,
                userType : $userType
            }, function(data){
                if(data.success){
                    stepHasShow(2, 3);
                    _tempStep = 3;
                    getSMSVerifyCode($(".v-forgotResendCode"), data, data.timer);
                }else{
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-step2-selectCharacter",
                step : _tempStep
            });
        });

        //第三步找回密码 submit
        $(document).on("click", ".v-forgotPassword-3", function(){
            var $this = $(this);
            var $forgotModule = $("[data-forgot-type='3']");
            var $verCode = $forgotModule.find("input[name='ver_code']").val();
            var success = validateForget("[data-forgot-type='3']");

            if(!success){
                return false;
            }

            $.post("/ucenter/mrpwds3.vpage", {
                token : _returnToken,
                code : $verCode
            }, function(data){
                if(data.success){
                    stepHasShow(3, 4);
                    _tempStep = 4;
                }else{
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-step3-nextSubmit",
                step : _tempStep
            });
        });

        //第四步找回密码 submit
        $(document).on("click", ".v-forgotPassword-4", function(){
            var $this = $(this);
            var $forgotModule = $("[data-forgot-type='4']");
            var $password = $forgotModule.find("input[name='password']").val();
            var $verifyPassword = $forgotModule.find("input[name='verify_password']").val();
            var success = validateForget("[data-forgot-type='4']");

            if(!success){
                return false;
            }

            if($password != $verifyPassword){
                $forgotModule.find("input[name='verify_password']").parent().addClass('error');
                $forgotModule.find("input[name='verify_password']").siblings(".errorTips").text( '密码不一致' );
                return false;
            }

            $.post("/ucenter/mrpwds4.vpage", {
                token : _returnToken,
                password : $verifyPassword
            }, function(data){
                if(data.success){
                    stepHasShow(4, 5);
                    _tempStep = 5;

                    $17.voxLog({
                        userId : $currentUserId,
                        module : "login-NewForgotPassword",
                        op : "click-step4-setSuccess",
                        step : _tempStep
                    });
                }else{
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-step4-setNewPass",
                step : _tempStep
            });
        });

        //重新发送验证码
        $(document).on("click", ".v-forgotResendCode", function(){
            var $this = $(this);
            if($this.hasClass("btn_disable")){
                return false;
            }

            $.post("/ucenter/svc.vpage", {
                token : _returnToken
            }, function(data){
                if(data.success){
                    getSMSVerifyCode($(".v-forgotResendCode"), data, data.timer);
                }else{
                    $17.alert(data.info);
                }
            });

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-sendCode",
                step : _tempStep
            });
        });

        //下一步显示
        function stepHasShow(current, next){
            $("[data-forgot-type='"+current+"']").hide();
            $("[data-forgot-type='"+next+"']").show();
            $(".JS-verMobileShow").html(_tempMobile);
        }

        //基础错误提示
        function baseErrorForgot(val){
            var errorInfoIdx =  $("#index_login_password");

            errorInfoIdx.parent().addClass('error');
            errorInfoIdx.siblings(".errorTips").html(val);
            setTimeout(function(){
                errorInfoIdx.parent().removeClass('error');
                errorInfoIdx.siblings(".errorTips").html("");
            }, 4000);

            $("#index_login_username, #index_login_password").on("keydown", function(){
                errorInfoIdx.parent().removeClass('error');
                errorInfoIdx.siblings(".errorTips").html("");
            });
        }

        //关闭忘记密码提示
        $(document).on("click", ".v-closeForgetInfo", function(){
            $("#forgetInfoPopup").empty();

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-closePopup",
                step : _tempStep
            });
        });

        $(document).on("click", "#captchaImage", function(){
            refreshCaptcha(_tempToken);

            $17.voxLog({
                userId : $currentUserId,
                module : "login-NewForgotPassword",
                op : "click-getCode-4digit"
            });
        });

        //获取验证码
        function refreshCaptcha(token) {
            $('#captchaImage').attr('src', "/captcha?" + $.param({
                'module': 'resetpwd',
                'token': token,
                't': new Date().getTime()
            }));
        }

        //获取短信验证码
        function getSMSVerifyCode($this, data, second){
            var timerCount;
            var timer;
            second = second ? second : 60;

            if(data.success){
                timerCount = second;
            }else{
                timerCount = data.timer || null;
                if(timerCount == null){
                    return false;
                }
            }

            $this.addClass("dis").html(timerCount + "s后重发");

            timer = $.timer(function(){
                if(timerCount <= 0){
                    $this.removeClass("dis").html("重新发送");
                    timerCount = second;
                    timer.stop();
                }else{
                    $this.addClass("dis").html(--timerCount + "s后重发");
                }
            });
            timer.set({ time: 1000});
            timer.play();
        }

        function validateForget(typeBox){
            $(typeBox).find(".require").each(function(){
                var $self = $(this);
                if($17.isBlank($self.val())){
                    $self.parent().addClass('error');
                    $self.siblings(".errorTips").text( $self.data("label") + '不可为空' );
                }else{
                    $self.parent().removeClass('error');
                    $self.siblings(".errorTips").text('');
                }
            });

            return ($(typeBox).find(".error").size() < 1) ? true : false;
        }
    });
</script>

<script type="text/html" id="T:初中学号提示">
    <div class="loginPop-box">
        <div class="lop-inner">
            <div class="loginPop-close v-closeForgetInfo"></div>
            <h1>暂不支持中学学生电脑端登录，请下载APP用手机登录</h1>
            <div class="lop-main">
                <div class="c-text" style="text-align: center;">
                    <img src="<@app.link href="public/skin/studentv3/images/koudaixueshe_code.png?1.0.2"/>" alt="" width="180"/>
                    <p>扫描二维码</p>
                    <p>下载一起作业学生端</p>
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:有绑定二维码">
    <#--登录失败-->
    <div class="loginPop-box">
        <div class="lop-inner">
            <div class="loginPop-close v-closeForgetInfo"></div>
            <h1>登录失败，在家长通中重置密码</h1>
            <div class="lop-main">
                <div class="c-text">
                    <p>1、打开家长通APP<img src="<@app.link href="public/skin/default/v5/images/par-icon.png"/>" class="pic"></p>
                    <p>2、点击个人中心-重置孩子密码</p>
                    <img src="<@app.link href="public/skin/default/v5/images/login-image.png"/>" class="pic">
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:忘记密码提示">
    <div class="loginPop-box" style="z-index: 32">
        <div class="lop-inner">
            <div class="loginPop-close v-closeForgetInfo"></div>
            <#--//start-->
            <#--step1 mobile get code-->
            <div data-forgot-type="1">
                <h1>登录失败，找回密码？</h1>
                <div class="lop-content">
                    <div class="tips">提醒：手机号<span><%=mobile%></span>可作为账号登录哦！</div>
                    <div class="c-text">
                        <span class="login-icon icon-1"></span>
                        <input type="text" placeholder="填写手机号码" maxlength="11" name="mobile" class="require txt1 JS-inputEvent" data-label="手机号">
                        <div class="errorTips"></div>
                    </div>
                    <div class="c-text">
                        <span class="login-icon icon-2"></span>
                        <input type="text" placeholder="填写右侧数字验证码" name="code" data-label="验证码" maxlength="4" class="require txt2 JS-inputEvent">
                        <img id='captchaImage' height="34" style="padding: 0; height: 38px; cursor: pointer;" class="code"/>
                        <div class="errorTips"></div>
                    </div>
                    <div class="loginPop-footer">
                        <a class="login-btn v-forgotPassword-1" href="javascript:void (0);">下一步</a>
                    </div>
                </div>
            </div>

            <#--step2-select-actor-->
            <div style="display: none;" data-forgot-type="2">
                <h1>登录失败，找回密码？</h1>
                <div class="lop-content">
                    <div class="tips">请选择你的身份</div>
                    <div class="lop-tab">
                        <ul>
                            <li class="v-forgotPassword-2" data-type="1">
                                <div class="image image-tea"></div>
                                <div class="side">我是老师</div>
                            </li>
                            <li class="v-forgotPassword-2" data-type="3" >
                                <div class="image"></div>
                                <div class="side">我是学生</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <#--step3 mobile-->
            <div style="display: none;" data-forgot-type="3">
                <h1>登录失败，找回密码？</h1>
                <div class="lop-content">
                    <div class="tips">请输入<span class="JS-verMobileShow"><%=mobile%></span>收到的短信验证码</div>
                    <div class="c-text">
                        <span class="login-icon icon-3"></span>
                        <input type="text" placeholder="手机收到的验证码" value="" maxlength="6" class="require txt2 JS-inputEvent" name="ver_code" data-label="验证码">
                        <span class="code codeBg v-forgotResendCode" style="cursor: pointer;">重新发送</span>
                        <div class="errorTips"></div>
                    </div>
                    <div class="loginPop-footer">
                        <a href="javascript:void(0);" class="login-btn v-forgotPassword-3">下一步</a>
                    </div>
                </div>
            </div>

            <!--验证通过 step4 mobile-->
            <div style="display: none;" data-forgot-type="4">
                <h1>验证通过</h1>
                <div class="lop-content">
                    <div class="tips">请为账号<span class="JS-verMobileShow"><%=mobile%></span>重新设置密码</div>
                    <div class="c-text">
                        <span class="login-icon icon-4"></span>
                        <input type="password" value="" maxlength="16" name="password" data-label="密码" class="txt1 require JS-inputEvent" placeholder="请输入新密码">
                        <div class="errorTips"></div>
                    </div>
                    <div class="c-text">
                        <span class="login-icon icon-4"></span>
                        <input type="password" value="" maxlength="16" name="verify_password" data-label="确认密码" class="txt1 require JS-inputEvent" placeholder="请再次输入新密码">
                        <div class="errorTips"></div>
                    </div>
                    <div class="loginPop-footer">
                        <a href="javascript:void(0);" class="login-btn v-forgotPassword-4">下一步</a>
                    </div>
                </div>
            </div>

            <!--重置成功 登录 step4 mobile -->
            <div style="display: none;" data-forgot-type="5">
                <h2>重置成功，马上用新密码登录！</h2>
                <div class="loginPop-footer">
                    <a href="javascript:void(0);" class="login-btn v-closeForgetInfo">确定</a>
                </div>
            </div>
            <#--end//-->
        </div>
    </div>
</script>