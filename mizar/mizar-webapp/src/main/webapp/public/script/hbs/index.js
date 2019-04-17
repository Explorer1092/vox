/**
 * Created by fengwei on 2017/2/23.
 */
define(["jquery","knockout","prompt"],function($,ko){
    
    function HbsLoginMode() {
        var _this = this;
        _this.captchaBaseUrl = '/hbs/score/captcha.vpage?token='+token;
        _this.userName = ko.observable('');
        _this.psw = ko.observable('');
        _this.viaCode = ko.observable('');
        _this.captcha = ko.observable(_this.captchaBaseUrl);//验证码
        _this.refreshCaptcha = function () { //刷新验证码
            _this.captcha(_this.captchaBaseUrl+'&t='+(new Date().getTime()));
        };
        _this.validateData = function () { //验证提交数据
            var result = {};
            if(_this.isNull(_this.userName())){
                result.success = false;
                result.info = '用户名不能为空';
                return result;
            }
            if(_this.isNull(_this.psw())){
                result.success = false;
                result.info = '密码不能为空';
                return result;
            }
            if(_this.isNull(_this.viaCode())){
                result.success = false;
                result.info = '验证码不能为空';
                return result;
            }

            return {success:true};
        };

        _this.submitBtn = function () { //提交
            var valResult = _this.validateData();
            if(valResult.success){
                // $('#loginForm').submit();
                $.post('/hbs/score/login.vpage',{
                    username:_this.userName(),
                    password:_this.psw(),
                    verifyCode:_this.viaCode(),
                    captchaToken:_this.captcha().split('token=')[1].split('&')[0]
                },function (res) {
                    if(res.success){
                        _this.jumpLink(res.position);
                    }else{
                        _this.alertDialog('提示',res.info,function () {
                            _this.jumpLink(res.position);
                        })
                    }
                });

            }else{
                _this.alertDialog('提示',valResult.info,{})
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
                        _this.refreshCaptcha();
                    }
                }
            });
        };

        _this.isNull = function (str) {
            return str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        };

        if(errorMsg){
            _this.alertDialog('提示',errorMsg);
        }

        _this.jumpLink = function (type) {
            if(type && type == "msm"){
                location.href = 'msm.vpage';
            }
            if(type && type == "result"){
                location.href = 'result.vpage';
            }
        }

    }

    ko.applyBindings(HbsLoginMode);

});