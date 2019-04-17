<#-- 短信邀请 -->
<div class="form_wrap">
    <ul>
        <li class="but"><strong style="color:#999;">请您填写想要邀请的老师信息，便于生成短信内容：</strong></li>
        <li class="lab"><label><i class="color_red">*</i> 姓名：</label>
            <input id="invite_sms_teacher_username" target_type="name" type="text" data-label="姓名" class="require"><i style="color: red;"></i>
            <span class="init">用于发送短信时的称呼</span>
        </li>
        <li class="lab"><label><i class="color_red">*</i> 手机：</label>
            <input id="invite_sms_teacher_mobile" target_type="mobile" type="text" data-label="手机号" class="require"><i style="color: red;"></i>
            <span class="init">用于发送短信，我们会对此信息严格保密</span>
        </li>
        <li class="lab"><label><i class="color_red">*</i>验证码：</label>
            <input id="checkCodeInput" type="text" target_type="code" style="width: 80px;" data-label="验证码" class="require"><i style="color: red;"></i>
            <img id='captchaImage' />&nbsp;
            看不清？<a href="javascript:createCode();" style="color: #3399FF">换一个</a>
            <div class="error_tip" style="color: red; margin-left: 15px;margin-left: 143px;margin-top: 1px;"></div>
        </li>
        <li class="but">
            <div class="color_blue padding_five">对方将收到以下信息：</div>
            <div class="key_information">
                我是<span class="color_blue">${(realName)!}</span>，我用一起作业网站布置检查作业很方便，学生特别喜欢，成绩也提高了，还是免费的，网址17zuoye.com你也试试吧！
            </div>
        </li>
        <li class="but">
            <a id="submit_sms_but" class="public_b orange_b" href="javascript:void(0);"><i><span>发送</span></i></a>
        </li>
    </ul>
</div>
<script type="text/javascript">
    /** 生成验证码 */
    function createCode(){
        $('#captchaImage').attr('src', "/captcha?" + $.param({
            'module' : 'teacherInviteTeacher',
            'token'  : '${captchaToken}',
            't'      : new Date().getTime()
        }));
        return false;
    }

    $(function(){
        createCode();

        $("#invite_sms_teacher_username").focus();

        $("#submit_sms_but").on('click',function(){
            var _this                   = $(this);
            var _name                   = $("#invite_sms_teacher_username");
            var _mobile                 = $("#invite_sms_teacher_mobile");
            var invite_teacher_username = $.trim(_name.val());
            var invite_teacher_mobile   = _mobile.val();
            var _inputCode              = $("#checkCodeInput").val();
            var _code                   = $("#checkCode").text();
            var _tip                    = $(".error_tip");
            $("#useremail_error").empty();

            /**验证用户名*/
            if($17.isBlank(invite_teacher_username) || invite_teacher_username.length > 5 || !$17.isCnString(invite_teacher_username)){
                _name.closest("li").removeClass("over");
                _name.closest("li").addClass("err");
                $("#username").empty();
                _name.after('<span id="username" style="color:red;">请填写正确的姓名</span>');
                return false;
            }else{
                $("#username").empty();
                _name.closest("li").addClass("over");
                _name.closest("li").removeClass("err");
            }

            /**验证手机*/
            if(!$17.isMobile(invite_teacher_mobile)){
                _mobile.closest("li").removeClass("over");
                _mobile.closest("li").addClass("err");
                $("#usermobile_error").empty();
                $("#usermobile").empty();
                _mobile.after('<span id="usermobile" style="color:red;">请填写正确的手机号</span>');
                return false;
            }else{
                $("#usermobile").empty();
                $("#usermobile_error").empty();
                _mobile.closest("li").addClass("over");
                _mobile.closest("li").removeClass("err");
            }

            if(_this.hasClass("save")){
                return false;
            }
            _this.addClass("save");

            $.post("/teacher/invite/sms.vpage", {
                mobile          : invite_teacher_mobile,
                realname        : invite_teacher_username,
                captchaToken    : '${captchaToken}',
                captchaCode     : _inputCode
            }, function(data){
                if(data.success){
                    $(".error_tip").text("");
                    _tip.text("");
                    _tip.parent().removeClass("err");
                    _tip.parent().addClass("over");
                    $("#usermobile_error").empty();
                    $("#useremail_error").empty();

                    alert("短信发送成功！");

                    _this.removeClass("orange_b").addClass("gray_b");
                    _this.find("span").css({ cursor: "default"});
                    setTimeout(function(){
                        _name.val('');
                        _mobile.val('');
                        $("#checkCodeInput").val('');
                        _this.removeClass("gray_b").addClass("orange_b");
                        _this.find("span").css({ cursor: "pointer"});
                        _this.removeClass("save");
                    }, 1000 * 10);
                }else if(data.value == "codeFalse"){
                    _tip.text(data.info);
                    _tip.parent().addClass("err");
                    _this.removeClass("save");
                    $("#checkCodeInput").focus();
                    return false;
                }else {
                    _this.removeClass("save");
                    _mobile.closest("li").removeClass("over");
                    _mobile.closest("li").addClass("err");
                    $("#usermobile_error").empty();
                    $("#useremail_error").empty();
                    _mobile.after('<span id="usermobile_error" style="color:red;">'+data.info+'</span>');
                    return false;
                }
            });
        });
    });
</script>
