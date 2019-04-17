/**
 * Created by Administrator on 2015/11/10.
 */
define(["jquery","$17", "knockout","getVerifyCodeModal","menu"],function($,$17,knockout,getVerifyCodeModal,menu){
    /****************变量声明***********/
    var bindMobileModalAndView = {
        phoneNo: knockout.observable(""),
        validCode: knockout.observable(""),
        //getMobileValid: getMobileValid,
        subBook: subBook
    };
    
    new getVerifyCodeModal({
        phoneNoInputId: "tt_phoneNo",
        btnId: "tt_verifyCodeBtn",
        url: "/parent/ucenter/sendcode.vpage"
    });

    /****************方法声明***********/
    function getMobileValid () {
        if(bindMobileModalAndView.phoneNo() == ""){
            $17.jqmHintBox("请输入手机号码后再获取验证码");
            return;
        }
        console.log(bindMobileModalAndView.phoneNo());
        console.log($17.isMobile(bindMobileModalAndView.phoneNo()));
        if(!$17.isMobile(bindMobileModalAndView.phoneNo())){
            $17.jqmHintBox("请输入正确的手机号码格式");
            return;
        };
        console.log("获取验证码");
    }

    function subBook () {
        console.log("立即预定");
    }


    /****************事件交互***********/
    knockout.applyBindings(bindMobileModalAndView);

});