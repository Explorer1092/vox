define(["jquery","prompt","datetimepicker","paginator","jqform"],function($){
    /*------------------------登录页------------------------------*/
    $("#js-close").on("click",function(){
        $(this).parent().remove();
    });

    var requireInputsLogin = $(".require-login");
    function isEmptyLogin(){
        for(var i = 0;i < requireInputsLogin.length;++i){
            if(requireInputsLogin.eq(i).val() == ''){
                requireInputsLogin.eq(i).parent().siblings(".error").show();
                return true;
            }
        }
        return false;
    }
    requireInputsLogin.on("focus",function(){
        $("#error-tip").html('');
    });

    var loginFun = function(){
        if(!isEmptyLogin()){
            $("#login-form").ajaxSubmit(function(res){
                if(res.success){
                    location.href = "/index.vpage";
                }else{
                    if(res.code == "1001"){
                        location.href = "/index.vpage";
                    }else{
                        $("#error-tip").html(res.info);
                    }
                }
            });

        }
    };

    $("#js-login").on("click",function(){
        loginFun();
    });

    $("#login-form").on("keydown",function(e){
        if(e.keyCode == 13){
            loginFun();
        }
    });

    requireInputsLogin.on("focus",function(){
        $(this).parent().siblings(".error").hide();
    });

    /*------------------------修改密码和找回密码------------------------------*/
    var resetInputs = $(".reset-input"),
        tel=$("#tel"),
        code=$("#code"),
        newPassword=$("#new-password"),
        repeatPassword=$("#repeat-password");

    function isEmpty(){
        var res = false;
        resetInputs.each(function(){
            var $this = $(this);
            if($this.val() == ''){
                $this.siblings(".error-tip").show().html($this.attr("data-empty-tip"));
                res = true;
            }
        });
        return res;
    }
    resetInputs.on("focus",function(){
        $(this).siblings(".error-tip").hide();
        $(".general-error-tip").html('');
    });
    $(document).on("click","#js-reset",function(){
        var $this = $(this);
        if(isEmpty()){
            return false;
        }else{
            if(repeatPassword.val() != newPassword.val()){
                repeatPassword.siblings(".error-tip").show().html("两次填写密码不同");
                return false;
            }
            $("#reset-form").ajaxSubmit(function(res){
                if(res.success){
                    $(".send-success").fadeOut();
                    if($this.attr("data-op-type") == "modify"){
                        // $(".layer-module").fadeIn("fast").delay(2000).fadeOut("slow");
                        $.prompt("<div style='text-align:center;'>恭喜您！密码修改成功，请牢记。点击确定去往登录页</div>", {
                            title: "修改成功",
                            buttons: { "确定": true },
                            focus : 1,
                            submit: function( e,v ){
                                if ( v ) {
                                    location.href = "/index.vpage";
                                }
                            },
                            useiframe:true
                        });
                    }else{
                        $.prompt("<div style='text-align:center;'>恭喜，重置密码成功。点击确定去往登录页</div>", {
                            title: "修改成功",
                            buttons: { "确定": true },
                            focus : 1,
                            submit: function( e,v ){
                                if ( v ) {
                                    location.href = "/index.vpage";
                                }
                            },
                            useiframe:true
                        });
                    }
                }else{
                    switch(res.code){
                        case 2001:
                            tel.siblings(".error-tip").show().html(res.info);
                            break;
                        case 2002:
                            code.siblings(".error-tip").show().html(res.info);
                            break;
                        case 2003:
                            newPassword.siblings(".error-tip").show().html(res.info);
                            break;
                        case 2004:
                            repeatPassword.siblings(".error-tip").show().html(res.info);
                            break;
                        default:
                            $(".general-error-tip").html(res.info);
                            break;
                    }
                }
            });
        }
    });
    $("#send-code").on("click",function(){
        var $this = $(this);
        $this.siblings(".error-tip").hide();
        tel.siblings(".error-tip").hide();
        if(tel.val() == ''){
            tel.siblings(".error-tip").show().html(tel.attr("data-empty-tip"));
        }else{
            $.get("getSMSCode.vpage",{mobile:tel.val()},function(res){
                if(res.success){
                    $this.addClass("count-down");
                    var time = 60;
                    $this.siblings(".send-success").show();
                    $this.html(time+' s');
                    var timer = setInterval(function(){
                        time = time - 1;
                        if(time == 0){
                            clearInterval(timer);
                            $this.siblings(".send-success").fadeOut();
                            $this.removeClass("count-down").html("获取验证码");
                        }else{
                            $this.html(time+' s');
                        }
                    },1000);
                }else{
                    if(res.timer >0){
                        $this.siblings(".error-tip").show().html(res.info);
                    }else{
                        tel.siblings(".error-tip").show().html(res.info);
                    }
                }
            });
        }
    });
    console.info($("a").find())
});