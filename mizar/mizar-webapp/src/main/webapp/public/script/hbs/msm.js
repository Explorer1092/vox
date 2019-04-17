/**
 * Created by fengwei on 2017/2/24.
 */
define(["jquery","knockout","prompt"],function($,ko){
    var timer = '';
    function HbsLoginMode() {
        var _this = this;
        var submitFlag = true;
        _this.captchaBaseUrl = '/hbs/score/captcha.vpage?token='+token;
        _this.mobile = ko.observable('');
        _this.psw = ko.observable('');
        _this.psw2 = ko.observable('');
        _this.viaCode = ko.observable('');
        _this.captcha = ko.observable(_this.captchaBaseUrl);
        _this.phoneViaCode = ko.observable();
        _this.btnText = ko.observable('获取短信');
        _this.msmSending = ko.observable(0);
        _this.countDown = ko.observable(120);
        _this.sendMsm = function () {
            if(_this.validateMobile(_this.mobile()) || hasPhone){
                if(!_this.msmSending()){
                    var url = '/hbs/score/sendsms.vpage?phoneNumber='+_this.mobile();
                    if(hasPhone){
                        url = '/hbs/score/sendsms.vpage?phoneNumber='+$("#phoneNumber").text();
                    }
                    $.get(url,function (res,status) {
                        if(status == "success"){
                            if(res.success){
                                _this.alertDialog('提示',res.info);
                                _this.msmSending(1);
                                timer = setInterval(function () {
                                    if(_this.countDown() > 0){
                                        _this.countDown(_this.countDown()-1);
                                        _this.btnText(_this.countDown()+'秒后重新获取');
                                        $('#getCodeBtn').addClass('gray');
                                    }else{
                                        clearInterval(timer);
                                        _this.countDown(120);
                                        $('#getCodeBtn').removeClass('gray');
                                        _this.btnText('获取短信');
                                        _this.msmSending(0);
                                    }
                                },1000);
                            }else{
                                _this.msmSending(0);
                                _this.alertDialog('提示',res.info);
                            }
                        }
                        if(status == "error"){
                            _this.msmSending(0);
                            _this.alertDialog('提示','请求出错');
                        }
                    });
                }
            }else{
                _this.alertDialog('提示','请填写正确格式的手机号');
            }
        };
        _this.refreshCaptcha = function () { //刷新验证码
            _this.captcha(_this.captchaBaseUrl+'&t='+(new Date().getTime()));
        };
        _this.validateData = function () { //验证提交数据
            var result = {};
            if(!hasPhone && _this.isNull(_this.mobile())){
                result.success = false;
                result.info = '手机号码不能为空';
                return result;
            }
            if(!hasPhone && !_this.validateMobile(_this.mobile())){
                result.success = false;
                result.info = '请填写正确格式的手机号';
                return result;
            }
            if(_this.isNull(_this.psw())){
                result.success = false;
                result.info = '密码不能为空';
                return result;
            }
            if(_this.psw().length < 6){
                result.success = false;
                result.info = '请设置6位数以上密码';
                return result;
            }
            if(_this.isNull(_this.psw2())){
                result.success = false;
                result.info = '确认密码不能为空';
                return result;
            }
            if(_this.psw2().length < 6){
                result.success = false;
                result.info = '请设置6位数以上确认密码';
                return result;
            }
            if(_this.psw2() != _this.psw()){
                result.success = false;
                result.info = '两次输入密码不一致';
                return result;
            }
            if(_this.isNull(_this.viaCode())){
                result.success = false;
                result.info = '验证码不能为空';
                return result;
            }
            if(_this.isNull(_this.phoneViaCode())){
                result.success = false;
                result.info = '短信验证码不能为空';
                return result;
            }

            return {success:true};
        };

        _this.submitBtn = function () { //提交
            var valResult = _this.validateData();
            if(valResult.success && submitFlag){
                submitFlag = false;
                var phone = _this.mobile();
                if(hasPhone){
                    phone = $("#phoneNumber").text();
                }
                $.ajax({
                    url:'verify.vpage',
                    type:'POST',
                    data:{
                        smsCode:_this.phoneViaCode(),
                        phoneNumber:phone,
                        verifyCode:_this.viaCode(),
                        captchaToken:token,
                        newPwd:_this.psw(),
                        confirmPwd:_this.psw2()
                    },
                    success:function (res) {
                        submitFlag = true;
                        if(res.success){
                            location.href = '/hbs/score/result.vpage';
                        }else{
                            _this.refreshCaptcha();
                            if(res.returnHome){
                                _this.alertDialog('提示',res.info?res.info:'提交数据出错',function () {
                                    location.href = "/hbs/score/login.vpage";
                                });
                            }else{
                                _this.alertDialog('提示',res.info?res.info:'提交数据出错');
                            }
                        }
                    },
                    error:function (e) {
                        submitFlag = true;
                        _this.alertDialog('提示','好像出问题了');
                    }
                });
            }else{
                _this.alertDialog('提示',valResult.info,{});
            }
        };

        _this.alertDialog = function (title,info,successCallback) { //提示弹窗
            $.prompt("<div style='text-align:center;'>"+info+"</div>", {
                title: title?title:'提示',
                buttons: { "确定": true },
                focus : 1,
                submit: function( e,v ){
                    if(v){
                        if(successCallback && typeof successCallback == "function" ){
                            successCallback();
                        }
                    }
                }
            });
        };

        _this.isNull = function (str) {
            return str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        };

        _this.validateMobile = function (value) {//验证手机号
            value = value + "";
            var reg = /^1[0-9]{10}$/;
            if(!value || value.length != 11 || !reg.test(value)){
                return false;
            }
            return true;
        };

    }

    ko.applyBindings(HbsLoginMode);

});