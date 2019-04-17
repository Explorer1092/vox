/**
 * Created by pengmin.chen on 2018/1/10.
 * decription: 对接希悦平台
 */
function SeiueSchoolMode(){
    var _this = this;
    _this.choiceAccount = ko.observable(false); // 选择账号module
    _this.mobileVerify = ko.observable(false); // 验证手机号module
    _this.makeSureBind = ko.observable(false); // 确认绑定信息module
    _this.bindMobile = ko.observable(false); // 绑定账号，设置密码module

    _this.sourceUid = ko.observable('');
    _this.dataKey = ko.observable('');
    _this.multiAccountInfo = ko.observable([]);
    _this.choiceAccountInfo = ko.observable('');

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

    // 初始登录
    $.post("/seiue/login.vpage", {
        token: getParam("access_token"),
        type: getParam("token_type"),
        expire: getParam("expires_in"),
    }, function(data) {
        if (data.success) {
            _this.sourceUid(data.sourceUid);
            if (data.multi) { // 有多个账号（此时无需绑定手机号流程）
                _this.choiceAccount(true); // 显示选择账号模块
                _this.multiAccountInfo(data.teachers);
            } else { // 单个账号
                if (data.type == 'success') { // 已经登陆过，直接校验通过
                    window.location.href = '/'; // 回到一起作业首页
                } else {
                    _this.dataKey(data.dataKey);
                    requestInit(data.sourceUid, data.dataKey);
                }
            }
        } else {
            $.prompt(template("T:系统错误信息提示",{}), {
                title:"系统提示",
                focus: 0,
                buttons:{'知道了': true},
                loaded: function () {
                    $('.JS-errorAlertInfo').html(data.info);
                }
            });
            $('.jqi .jqibuttons button, .jqi .jqiclose').on('click', function () {
                window.location.href = '/';
            });
        }
    });

    // 选择一个账号（multi为true时用）
    _this.choiceOneAccount = function (data, event) {
        var $thisNode = $(event.currentTarget);
        $thisNode.addClass('active').siblings().removeClass('active');
        _this.choiceAccountInfo(data);
    };

    // 选择账号后下一步
    _this.choiceAccountToNext = function () {
        if (_this.choiceAccountInfo()) {
            window.location.href = '/seiue/science.vpage?sourceUid=' + _this.sourceUid() + '&selectTeacher=' + _this.choiceAccountInfo().teacherId;
        }
    };

    // 获取绑定状态
    function requestInit (sourceUid, dataKey) {
        $.get("/seiue/init.vpage", {
            sourceUid: sourceUid
        }, function(data) {
            if (data.success){
                _this.choiceAccount(false);
                _this.mobileVerify(true); // 显示验证手机号模块
                _this.type(data.type);
                _this.captchaToken(data.captchaToken); // 验证码
                _this.refreshCaptcha();
                _this.teacherId(data.teacherId || "");
                if (_this.type() == "success") { // 已登录
                    window.location.href = '/';
                } else if (_this.type() == "bind") { // 之前绑定过手机号
                    _this.mobileNum(data.mobile); // 自动填充手机号
                    $(".JS-teacherMobile").attr("value",data.mobile).addClass("txt-readonly").attr("readonly","readonly");
                }
            }
        });
    }

    // 点击收不到短信（显示帮助）
    _this.isNotReceiveBtn = function () {
        $.prompt(template("T:一起作业绑定手机帮助",{}),{
            title:"一起作业绑定手机帮助",
            focus: 0,
            buttons:{'知道了': true}
        });
    };

    // 刷新验证码
    _this.refreshCaptcha = function () {
        $("#captchaInputLogin").val("");
        $('#captchaImageLogin').attr('src', "/captcha?" + $.param({
                'module': 'regCaptcha',
                'token': _this.captchaToken(),
                't': new Date().getTime()
            }));
    };

    // 绑定手机表单报错弹窗
    _this.ErrorInfoAlert = function(id, val){
        id.focus();
        id.parent().addClass('error');
        id.siblings(".errorTips").text(val);
    };

    // 点击获取验证码
    _this.getCheckCode = function (data,event) {
        var $self = $(event.currentTarget);

        var _teacherMobile = $(".JS-teacherMobile");
        var _teacherCaptcha = $(".JS-teacherCaptcha");
        var AllGetCheckCodeBtn = $('.JS-getCheckCode');
        if(AllGetCheckCodeBtn.hasClass("dis")){
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
                    AllGetCheckCodeBtn.removeClass("dis");
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
                // AllGetCheckCodeBtn.removeClass("dis");
            });
        };
        sendReq();
    };

    // 点击验证手机号下一步
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
            dataKey : _this.dataKey()
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
                _this.mobileVerify(false); // 关闭验证手机号module
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
        // 希悦平台不含绑定密码模块，默认isNewTip为false，走确认绑定流程
        if (isNewTip){
            _this.bindMobile(true); // 绑定账号密码module
        }else{
            _this.makeSureBind(true); // 确认绑定module
        }
        // 获取用户信息
        $.get("validation/info.vpage", {
            teacherId:_this.teacherId()
        }, function(data){
            if(data.success){
                _this.schoolName(data.schoolName || "");
                _this.teacherMobile(data.teacherMobile || "");
                _this.teacherName(data.teacherName || "");
                _this.teacherSubject(data.teacherSubject || "");
            }else{
                $.prompt(template("T:系统错误信息提示",{}), {
                    title:"系统提示",
                    focus: 0,
                    buttons:{'知道了': true},
                    loaded: function () {
                        $('.JS-errorAlertInfo').html(data.info);
                    }
                });
            }
        });
    };

    // 绑定并登陆
    _this.bindAndLogin = function (data,event) {
        _this.gotoLogin();
        // if (_this.type() == "bind"){ // 已绑定
        //     _this.gotoLogin();
        // }
        // var $self = $(event.currentTarget);
        // var _password = $(".JS-password");
        // var _passwordAgain = $(".JS-passwordAgain");
        //
        // if( $self.hasClass("dis") ){
        //     return ;
        // }
        //
        // if( $17.isBlank(_password.val()) ){
        //     _this.ErrorInfoAlert(_password, "请输入密码");
        //     return ;
        // }
        //
        // if( $17.isBlank(_passwordAgain.val()) ){
        //     _this.ErrorInfoAlert(_passwordAgain, "请再次输入密码");
        //     return ;
        // }
        // if (_password.val() != _passwordAgain.val()) {
        //     _this.ErrorInfoAlert(_passwordAgain, "密码填写不一致，请重新填写");
        //     return ;
        // }
        // $self.addClass("dis");
        // $.get("validation/setpwd.vpage", {
        //     teacherId:_this.teacherId(),
        //     password: _password.val(),
        //     confirm: _passwordAgain.val()
        // }, function(data){
        //     if(data.success){
        //         _this.gotoLogin();
        //     }else{
        //         $.prompt(template("T:系统错误信息提示",{}), {
        //             title:"系统提示",
        //             focus: 0,
        //             buttons:{'知道了': true},
        //             loaded: function () {
        //                 $('.JS-errorAlertInfo').html(data.info);
        //             }
        //         });
        //     }
        // });
    };

    // 去登陆
    _this.gotoLogin = function () {
        $.post("validation/confirm.vpage", {
            dataKey:_this.dataKey(),
            teacherId: _this.teacherId()
        }, function(data){
            if(data.success){
                window.location.href = "/";
            }else{
                $.prompt(template("T:系统错误信息提示",{}), {
                    title:"系统提示",
                    focus: 0,
                    buttons:{'知道了':true},
                    loaded: function () {
                        $('.JS-errorAlertInfo').html(data.info);
                    }
                });
            }
        });
    };

    $(document).on("keyup", ".JS-inputEvent", function () {
        var $self = $(this);
        if ($self.val() != "") {
            $self.parent().removeClass("error");
        }
    });

    function getParam(item){
        var svalue = location.hash.match(new RegExp('[\#\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

}

ko.applyBindings(new SeiueSchoolMode());