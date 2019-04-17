<script id="t:错误提示_20140821151943" type="text/html">
    <i class="w-icon-public w-icon-error"></i><strong class="info"><%=errorMessage%></strong>
</script>
<script type="text/javascript">
    var invalidatePW = {
        "123456789":true,
        "a123456":true,
        "123456":true,
        "a123456789":true,
        "1234567890":true,
        "qq123456":true,
        "abc123456":true,
        "123456a":true,
        "12345678910":true,
        "abc123":true
    };

    $("input").on("blur",function(e){
        var _this = $(this);
        var row = _this.parent();
        var _type = _this.attr("id");
        var span = _this.siblings("span.errorMsg");
        var condition = true;
        var errorMessage = "";
        switch (_type)
        {
            case "realname":
                var value = _this.val();
                if(!$17.isValidCnName(value)){
//                if(!$17.isCnString(value)){
                    condition = false;
                    errorMessage = "请输入您的真实姓名,须为中文";
                }else if(value.length < 2 || value.length > 10 ){
                    errorMessage = "请输入2-10位中文名字";
                    condition = false;
                }
                break;
            case "detailAddress":
                var _value = _this.val().replace(/\s+/g, "");
                if($17.isBlank(_value)){
                    condition = false;
                    errorMessage = "请在此补充乡镇、街道具体地址";
                }else if(_value.length > 200){
                    condition = false;
                    errorMessage = "请输入200以内的具体地址";
                }
                break;
            case "phone":
                condition = $17.isMobile(_this.val());
                errorMessage = "请填写正确的手机号码";
                break;
            case "newmobile":
                condition = $17.isMobile(_this.val());
                errorMessage = "请填写正确的手机号码";
                break;
            case "validatecode":
                if($17.isBlank(_this.val())){
                    condition = false;
                    errorMessage = "请填写验证码"
                }
                break;
            case "email":
                condition = $17.isEmail(_this.val());
                errorMessage = "请填写正确格式的邮箱";
                break;
            case "old_password":
                if($17.isBlank(_this.val())){
                    condition = false;
                    errorMessage = "原密码不能为空";
                }
                break;
            case "password":
                if($17.isBlank(_this.val())){
                    condition = false;
                    errorMessage = "密码不能为空";
                } else if(_this.val().length > 16){
                    errorMessage = "密码过长，请输入1-16位任意字符";
                    condition = false;
                } else if (_this.val().length < 5) {
                    errorMessage = "密码过短";
                    condition = false;
                } else if (invalidatePW[_this.val()]) {
                    errorMessage = "密码过于简单";
                    condition = false;
                }
                // notice = "请输入1—16位任意字符（字母区分大小写）";
                break;
            case "verify_password":
                if($17.isBlank(_this.val())){
                    condition = false;
                    errorMessage = "新密码不能为空";
                } else {
                    var password = $("#password");
                    condition = (password.val() == _this.val());
                    errorMessage = "密码填写不一致，请重新填写";
                }
                break;
            case "receivename":
                var value = _this.val();
                if(!$17.isValidCnName(value)){
                    condition = false;
                    errorMessage = "请输入正确的收件人姓名";
                }else if(value.length < 2 || value.length > 10 ){
                    errorMessage = "请输入2-10位中文名字";
                    condition = false;
                }
                break;
            case "receivetel":
                var value = _this.val();
                if(!$17.isMobile(value)){
                    condition = false;
                    errorMessage = "请您输入有效号码";
                }
                break;
            default:
                break;
        }
        if(e.type == "blur"){
            if(!condition){
                span.html(template("t:错误提示_20140821151943",{errorMessage : errorMessage}));
                _this.addClass("w-int-error");
            }else{
                _this.removeClass("w-int-error");
                span.html("");
                //pc端更改老师姓名时过滤敏感词汇
                if(_type == "realname"){
                    $.post("/signup/filtersensitiveusername.vpage",{userName:value},function (data) {
                        if(data.success){
                        }else{
                            errorMessage="<strong class='info'>输入的姓名信息不合适哦，请重新输入<br/>如有疑问，可<a href='http://www.17zuoye.com/redirector/onlinecs_new.vpage?type=teacher&question_type=question_account_pt&origin=PC-更改老师姓名' target='_blank' style='color:#fa7252;text-decoration:underline'>点击联系客服</a></strong>";
                            span.html(errorMessage);
                            _this.addClass("w-int-error");
                        }
                    });
                }
            }
        }
    });
    //target下的input验证
    function validate(target){
        var $inputs;
        var $errorInputs;

        if($17.isBlank(target)){
            $inputs = $("input.w-int");
        }else{
            $inputs = $(target).find("input.w-int");
        }

        $inputs.each(function(){
            if(!$17.isBlank($(this).attr("id"))){
                $(this).trigger("blur");
            }
        });

        if($17.isBlank(target)){
            $errorInputs = $("input.w-int-error");
        }else{
            $errorInputs = $(target).find("input.w-int-error");
        }

        if($errorInputs.size() == 0){
            return true;
        }else{
            return false;
        }
    }
</script>