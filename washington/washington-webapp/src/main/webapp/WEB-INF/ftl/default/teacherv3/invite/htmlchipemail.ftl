<!--邮件邀请-->
<div class="form_wrap">
    <ul>
        <li class="but"><strong style="color:#999;">请您填写想要邀请的老师信息，便于生成邮件内容：</strong></li>
        <li class="lab"><label><i class="color_red">*</i> 姓名：</label>
            <input id="invite_teacher_username" type="text"><span class="init">用于发送邮件时的称呼</span>
        </li>
        <li class="lab"><label><i class="color_red">*</i>  邮箱：</label>
            <input id="invite_teacher_email" type="text"><span class="init">用于发送邮件给被邀请老师</span>
        </li>
        <li class="but">
            <a id="invite_teacher_button" class="public_b orange_b" href="javascript:void(0);"><i><span>发送</span></i></a>
        </li>
    </ul>
</div>
<!--//-->
<script type="text/javascript">
    $(function(){
        $("#invite_teacher_button").lock("click", function(_this){
            var _name                       = $("#invite_teacher_username");
            var _email                      = $("#invite_teacher_email");
            var invite_teacher_username     = _name.val();
            var invite_teacher_schoolname   = $("#invite_teacher_schoolname").val();
            var invite_teacher_email        = _email.val();

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
                $("#invite_teacher_submit_button span").html("发送");
            }

            /**验证邮箱*/
            if(!$17.isEmail(invite_teacher_email)){
                _email.closest("li").removeClass("over");
                _email.closest("li").addClass("err");
                $("#useremail").empty();
                _email.after('<span id="useremail" style="color:red;">请填写正确的邮箱</span>');
                return false;
            }else{
                $("#useremail").empty();
                _email.closest("li").addClass("over");
                _email.closest("li").removeClass("err");
            }

            _this.postJSON("/teacher/invite/email.vpage", {
                email    : invite_teacher_email,
                realname : invite_teacher_username
            }, function(data) {
                if(data.success){
                    $("#usermobile_error").empty();
                    $("#useremail_error").empty();
                    $("#usermobile").hide();
                    alert("邮件已发送！记得提醒你邀请的老师查收邮件哦！");
                    setTimeout(function(){ location.reload(); }, 200);
                }else{
                    _email.closest("li").removeClass("over");
                    _email.closest("li").addClass("err");
                    $("#useremail_error").empty();
                    $("#usermobile_error").empty();
                    _email.after('<span id="useremail_error" style="color:red;">'+data.info+'</span>');
                }
            });
        }, 1000);
    });
</script>
