/**
 * Created by huihui.li on 2017/7/27.
 */

function CjlschoolMode(){
    var _this = this;
    setTimeout(function(){
        _this.refreshCaptcha();
    }, 100);
    _this.mobileVerify = ko.observable(true);
    _this.makeSureBind = ko.observable(false);
    _this.bindMobile = ko.observable(false);
    _this.type = ko.observable();
    _this.mobileNum = ko.observable("请输入您的手机号");
    _this.captchaToken = ko.observable();
    _this.teacherId = ko.observable();
    _this.errorMobile = ko.observable();
    _this.errorCaptcha = ko.observable();
    _this.errorCode = ko.observable();
    _this.errorPassword = ko.observable();
    _this.errorPasswordAgain = ko.observable();
    _this.schoolName = ko.observable();
    _this.teacherMobile = ko.observable();
    _this.teacherName = ko.observable();
    _this.teacherSubject = ko.observable();
    $.get("/cjlschool/init.vpage",
        {
            sourceUid: sourceUid
        },
        function(data) {
        if (data.success){
            _this.type(data.type);
            _this.captchaToken(data.captchaToken);
            _this.teacherId(data.teacherId || "");
            if(_this.type() == "bind"){
                _this.mobileNum(data.mobile);
                $(".JS-teacherMobile").attr("value",data.mobile).addClass("txt-readonly").attr("readonly","readonly");
            }else if (_this.type() == "success"){
                location.reload();
            }
        }
    });

    _this.isNotReceiveBtn = function () {
        $.prompt(template("T:一起作业绑定手机帮助",{}),{
            title:"一起作业绑定手机帮助",
            buttons:{}
        });
    };
    _this.refreshCaptcha = function () {
        $("#captchaInputLogin").val("");
        $('#captchaImageLogin').attr('src', "/captcha?" + $.param({
            'module': 'regCaptcha',
            'token': _this.captchaToken(),
            't': new Date().getTime()
        }));
    };

    _this.ErrorInfoAlert = function(id, val){
        id.focus();
        id.parent().addClass('error');
        id.siblings(".errorTips").text(val);
    };

    _this.getCheckCode = function (data,event) {
        var $self = $(event.currentTarget);

        var _teacherMobile = $(".JS-teacherMobile");
        var _teacherCaptcha = $(".JS-teacherCaptcha");
        var AllGetCheckCodeBtn = $('.JS-getCheckCode');

        if($self.hasClass("dis")){
            return ;
        }
        if( !$17.isMobile(_teacherMobile.val())){
            _this.ErrorInfoAlert(_teacherMobile, "请填写正确的手机号码");
            return ;
        }
        if( !$17.isNumber(_teacherCaptcha.val()) || _teacherCaptcha.val().length < 4){
            _this.ErrorInfoAlert(_teacherCaptcha, "验证码不可为空");
            return ;
        }
        var sendReq = function(){
            AllGetCheckCodeBtn.addClass("dis");
            var data = {
                mobile: _teacherMobile.val(),
                captchaToken: _this.captchaToken(),
                captchaCode: _teacherCaptcha.val()
            };
            $.post("validation/code.vpage", data, function(data){
                var timerCount, timer, second = 60;

                if(data.success){
                    timerCount = second;

                    AllGetCheckCodeBtn.addClass("dis").html(timerCount + "s后重发");

                    timer = $.timer(function() {
                        if(timerCount <= 0){
                            AllGetCheckCodeBtn.removeClass("dis");
                            AllGetCheckCodeBtn.text("获取验证码");
                            timer.stop();
                        } else {
                            timerCount--;
                            AllGetCheckCodeBtn.text(timerCount + "s后重发");
                        }
                    });
                    timer.set({ time : 1000});
                    timer.play();
                }else{
                    var info = data.info;
                    if(info.indexOf("正确的手机号") > -1){
                        _this.ErrorInfoAlert(_teacherMobile, info);
                    }else if(info.indexOf("验证码已发送") > -1){
                        _this.ErrorInfoAlert(_teacherCaptcha, info);
                    }else if(info.indexOf("其他老师注册") > -1){
                        _this.ErrorInfoAlert(_teacherMobile, info);
                    }else if(info.indexOf("已经注册") > -1){
                        _this.ErrorInfoAlert(_teacherMobile, info);
                    }else{
                        _this.ErrorInfoAlert(_teacherCaptcha, "验证码输入错误");
                    }
                    _this.refreshCaptcha();
                }
                AllGetCheckCodeBtn.removeClass("dis");
            });
        };
        sendReq();
    };

    _this.nextTip = function (data,event) {
        var $self = $(event.currentTarget);
        var _teacherMobile = $(".JS-teacherMobile");
        var _teacherSmsCode = $(".JS-teacherSmsCode");

        if( $self.hasClass("dis") ){
            return ;
        }

        if( !$17.isMobile(_teacherMobile.val()) ){
            _this.ErrorInfoAlert(_teacherMobile, "手机号错误");
            return ;
        }

        if( !$17.isNumber(_teacherSmsCode.val()) ){
            _this.ErrorInfoAlert(_teacherSmsCode, "短信验证码错误");
            return ;
        }

        $self.addClass("dis");
        var mobileData = {
            type: _this.type(),
            code : _teacherSmsCode.val(),
            mobile : _teacherMobile.val(),
            dataKey : dataKey
        };
        if (_this.teacherId() !=""){
            mobileData.teacherId = _this.teacherId();
        }
        $.post("validation/mobile.vpage",mobileData , function(data){
            if(data.success){
                if (data.teacherId){
                    _this.teacherId(data.teacherId);
                }
                var isNewTip = false;
                if (data.isNew){
                    isNewTip = true;
                }
                _this.mobileVerify(false);
                _this.makeSureBindBtn(isNewTip);
                _this.type("bind");
            }else{
                if( data.info.indexOf("手机号") > -1 ){
                    _this.ErrorInfoAlert(_teacherMobile, data.info);
                }else{
                    _this.ErrorInfoAlert(_teacherSmsCode, data.info);
                }
                $self.removeClass("dis");
            }
        });
    };

    _this.makeSureBindBtn = function (isNewTip) {
        if (isNewTip){
            _this.bindMobile(true);
        }else{
            _this.makeSureBind(true);
        }
        $.get("validation/info.vpage", {
            teacherId:_this.teacherId()
        }, function(data){
            if(data.success){
                _this.schoolName(data.schoolName || "");
                _this.teacherMobile(data.teacherMobile || "");
                _this.teacherName(data.teacherName || "");
                _this.teacherSubject(data.teacherSubject || "");
            }else{
                $17.alert(data.info);
            }
        });
    };

    _this.bindAndLogin = function (data,event) {
        if (_this.type() == "bind"){
            _this.gotoLogin();
        }
        var $self = $(event.currentTarget);
        var _password = $(".JS-password");
        var _passwordAgain = $(".JS-passwordAgain");

        if( $self.hasClass("dis") ){
            return ;
        }

        if( $17.isBlank(_password.val()) ){
            _this.ErrorInfoAlert(_password, "请输入密码");
            return ;
        }

        if( $17.isBlank(_passwordAgain.val()) ){
            _this.ErrorInfoAlert(_passwordAgain, "请再次输入密码");
            return ;
        }
        if (_password.val() != _passwordAgain.val()) {
            _this.ErrorInfoAlert(_passwordAgain, "密码填写不一致，请重新填写");
            return ;
        }
        $self.addClass("dis");
        $.get("validation/setpwd.vpage", {
            teacherId:_this.teacherId(),
            password: _password.val(),
            confirm: _passwordAgain.val()
        }, function(data){
            if(data.success){
                _this.gotoLogin();
            }else{
                $17.alert(data.info);
            }
        });
    };

    _this.gotoLogin = function () {
        $.post("validation/confirm.vpage", {
            dataKey:dataKey,
            teacherId: _this.teacherId()
        }, function(data){
            if(data.success){
                location.href = "/";
            }else{
                $17.alert(data.info);
            }
        });
    };

    $(document).on("keyup", ".JS-inputEvent", function () {
        var $self = $(this);
        if ($self.val() != "") {
            $self.parent().removeClass("error");
        }
    });

}

ko.applyBindings(new CjlschoolMode());