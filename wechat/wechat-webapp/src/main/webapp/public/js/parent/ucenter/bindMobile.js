define(["jquery","$17","smsBtn"],function($,$17,smsBtn){
    var bindingSubmitBtn = $('#bindingSubmitBtn'),
        mobile = $('#mobile'),
        code = $('#code'),
        getVerifyCodeBtn = $("#getVerifyCodeBtn");

    bindingSubmitBtn.on('click', function(){
        if(!$17.isMobile(mobile.val())){
            $17.msgTip("请输入正确的手机号码");
            return false;
        }
        if($17.isBlank(code.val())){
            $17.msgTip("请输入正确的验证码");
            return false;
        }
        $.post('/parent/ucenter/bindmobile.vpage',{
            mobile : mobile.val(),
            code : code.val()
        },function(data){
            if(data.success){
                location.href = '/parent/homework/index.vpage';
            }else{
                $17.msgTip(data.info);
            }
        });
    });
});