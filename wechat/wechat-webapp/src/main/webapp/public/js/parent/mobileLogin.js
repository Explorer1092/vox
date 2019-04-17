define(["jquery","$17","getVerifyCodeModal","jbox"],function($,$17,getVerifyCodeModal){
    var source = $('#source').val();
    var cid = $("#getVerifyCodeBtn").attr("data-cid");
    var source  = $("#source").val();

    var bindingSubmitBtn = $('#bindingSubmitBtn'),
        mobile = $('#mobile'),
        code = $('#code'),
        getVerifyCodeBtn = $("#getVerifyCodeBtn");

    var verifyUrl = "/parent/ucenter/sendchangecode.vpage";

    if(source == "signUp"){
        bindingSubmitBtn.text("登录");
        $(".js-notice").hide();
        verifyUrl = "/signup/parent/verifiedlogin/sendsmscode.vpage";
    }

    new getVerifyCodeModal({
        phoneNoInputId: "mobile",
        btnId: "getVerifyCodeBtn",
        url: verifyUrl,
        cid: cid,
        countSeconds: 60,
        btnClass : 'btn_disable',
        btnCountingText:"秒后重新获取",
        warnText: "验证码已发送，如未收到请1分钟后再试",
        gaCallBack: function(){

        }
    });

    bindingSubmitBtn.on('click', function(){
        if(source == "signUp"){
            $.post('/signup/parent/verifiedlogin.vpage',{
                mobile : mobile.val(),
                code : code.val()
            },function(data){
                if(data.success){
                    location.href = '/parent/homework/index.vpage';
                }else{
                    $17.msgTip(data.info);
                }
            });
        }else{
            $.post('/parent/ucenter/changebindmobile.vpage',{
                mobile : mobile.val(),
                code : code.val()
            },function(data){
                if(data.success){
                    location.href = '/parent/ucenter/index.vpage';
                }else{
                    $17.msgTip(data.info);
                }
            });
        }
    });
});