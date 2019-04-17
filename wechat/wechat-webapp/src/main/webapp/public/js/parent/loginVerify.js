/**
 * 登录 手机验证
 * 用户登录和用户在个人中心绑定孩子时，都需要手机验证。且页面展示一致，所以共用。
 */
define(['jquery','getVerifyCodeModal','$17','jbox'],function($,getVerifyCodeModal,$17){
    var submitBtn = $('#submitBtn'),mobile = $('#mobile'),code = $('#code');
    var cid = submitBtn.data('cid');
    var source = submitBtn.data('source'); //signUp or ucenter
    new getVerifyCodeModal({
        phoneNoInputId: "mobile",
        btnId: "getVerifyCodeBtn",
        url: "/signup/parent/sendsmscode.vpage",
        cid: cid,
        countSeconds: 60,
        btnClass : 'btn_disable',
        btnCountingText:"秒后重新获取",
        warnText: "验证码已发送，如未收到请1分钟后再试",
        gaCallBack: function(){

        }
    });

    //初始化 有默认手机号时，获取验证码按钮可点击
    mobile.keyup();

    //数据提交
    submitBtn.on('click',function(){
        if($17.isBlank(mobile.val())){
            $17.msgTip('请输入手机号');
            return false;
        }

        if($17.isBlank(code.val())){
            $17.msgTip("请输入验证码");
            return false;
        }

        var postUrl = (source == 'signUp') ? '/signup/parent/verify.vpage' : '/parent/ucenter/verify.vpage';

        $.post(postUrl,{
            mobile : mobile.val(),
            code : code.val(),
            callNameCode : $17.getQuery('callNameCode')
        },function(data){
            if(data.success){
                location.href = '/parent/homework/index.vpage';
            }else{
                $17.msgTip(data.info);
            }
        });
    });
});