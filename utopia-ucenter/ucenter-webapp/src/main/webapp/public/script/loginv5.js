(function () {
    //登录首页
    if(window.navigator.userAgent.indexOf('MSIE') > -1){
        var version = navigator.appVersion.split(";");
        var trim_Version = version[1].replace(/[ ]/g,"");
        if(trim_Version == "MSIE6.0" || trim_Version == "MSIE7.0" || trim_Version == "MSIE8.0" || trim_Version == "MSIE9.0"){
            $('#animateBox1').removeClass('animate_box').addClass('static_box');
            $('#animateBox2').removeClass('animate_box').addClass('static_box');
        }
    }

    /* 登录注册按钮淡入淡出 */
     function feadIn() {
        setTimeout(function () {
            $('.animate_box .btn_box').fadeIn('slow');
        },800);
    }
    feadIn();


    /*$(".JS-indexSwitch-main").flexslider({
        animation: "slide",
        slideshow: true,
        slideshowSpeed: 5000,
        //startAt: 1,
        directionNav: false,
        animationLoop: true,
        manualControls: ".JS-indexSwitch-mode li",
        touch: true //是否支持触屏滑动
    });*/

    //pic switch 执行模块
    indexSwitch();

    //login执行模块
    LoginModeInit();

    //register执行模块
    RegisterModeInit();

    $(window).resize(function () {
        indexSwitch();
    });

    //Login Mode
    function LoginModeInit() {
        /*
         * ID Render: RenderMain
         * Template: T:LoginTemplateMain
         * */
        var _RenderMain = $("#RenderMain");

        $(document).on("click", ".JS-login-main", function () {
            _RenderMain.html(template("T:LoginTemplateMain", {}));
            $("body").addClass('overflow-h');

            var _userName = $("#index_login_username");
            var _password = $("#index_login_password");

            //url has userId set userName value
            if(!$17.isBlank($17.getQuery("userId"))){
                _userName.val($17.getQuery("userId"));
                _password.focus();
            }else{
                if(_userName.val() == ""){
                    _userName.focus();
                }
            }

            // 低于IE9不展示眼睛功能
            if (typeof IEVersion() === 'number' && IEVersion() > 5 && IEVersion() < 9) {
                $('.JS-pwd').hide();
            }
        });

        //初始化登录
        if($17.getQuery("ref") == 'login'){
            $(".JS-login-main").trigger('click');
        }

        $(document).on('submit', '.JS-formSubmit', function () {
            var _userName = $("#index_login_username");
            var _password = $("#index_login_password");
            if (_userName.val() == "") {
                ErrorInfoAlert(_userName, "请输入学号/手机号");
                return false;
            }

            if (_password.val() == "") {
                ErrorInfoAlert(_password, "请输入密码");
                return false;
            }
        });

        $(document).on("click", ".JS-rememberMe-btn", function(){
            var $self = $(this);
            var $checked = $self.find("input");

            if($checked.prop("checked")){
                $checked.prop("checked", false).val("off");
                $self.removeClass("active");
            }else{
                $checked.prop("checked", true).val("on");
                $self.addClass("active");
            }
        });
    }
    //Register Mode
    function RegisterModeInit() {
        /*
         * ID Render: RenderMain
         * Template: T:RegisterModeInit
         * */
        var _RenderMain = $("#RenderMain");

        $(document).on("click", ".JS-register-main", function () {
            _RenderMain.html(template("T:RegisterSelectMain", {}));
            $("body").addClass('overflow-h');
        });

        //初始化登录
        if($17.getQuery("ref") == 'register'){
            $(".JS-register-main").trigger('click');
        }

        //切换到学生
        $(document).on({
            click : function(){
                if($(this).hasClass('active')){
                    return false;
                }
                _RenderMain.html(template("T:RegisterStudentMain", {}));
            }
        }, '.JS-selectStudent-main');

        //切换到老师
        $(document).on({
            click : function(){
                if($(this).hasClass('active')){
                    return false;
                }

                _RenderMain.html(template("T:RegisterTeacherMain", {}));

                setTimeout(function(){
                    refreshCaptcha();
                }, 100);
            }
        }, '.JS-selectTeacher-main');

        //获取老师班级列表
        $(document).on('click', '.JS-getClassLIst', function(){
            var $self = $(this);
            var $classInput = $('.JS-classInput');
            var $inputVal = $.trim($classInput.val());

            if(!$(".JS-agreement").hasClass('active')){
                $self.addClass("disabled");
                return false;
            }
            if($self.hasClass('dis')){
                return false;
            }

            if( !$17.isNumber($inputVal) || $inputVal.length < 5){
                ErrorInfoAlert($classInput, "老师账号错误");
                return false;
            }

            $self.addClass('dis');
            $.post("/signup/checkclazzinfo.vpage", {
                id: $inputVal,
                webSource: studentForm.webSource
            }, function (data) {
                if(data.success && data.clazzList && data.clazzList.length > 0){
                    studentForm.teacherId = $inputVal;
                    hasBeenUser();

                    //success
                    $("#allSearchClazzItem").html( template("T:选择班级列表", {clazzList: data.clazzList}) );

                    $(".js-registerTemplateType[data-type='teacher']").hide();
                    $(".js-registerTemplateType[data-type='student']").show();
                    $("#register_template").show();

                    $(".JS-indexPageBox").hide();
                    $("body").removeClass('overflow-h');
                }else{
                    ErrorInfoAlert($classInput, data.info);
                    $self.removeClass('dis');
                }
            });
        }).on("click",".JS-agreement",function () {
            var _this = $(this);
            // var $inputVal = $.trim($('.JS-classInput').val());
            // var flag = $17.isNumber($inputVal) && $inputVal.length >= 5 && !$('.JS-classInput').parent().hasClass("error");
            if (_this.hasClass("active")){
                // if(flag){
                    $(".JS-getClassLIst").addClass("disabled");
                // }
                _this.removeClass("active");
            }else{
                // if(flag){
                    $(".JS-getClassLIst").removeClass("disabled");
                // }
                _this.addClass("active");
            }
        });


        //注册老师验证手机号
        $(document).on('click', '.JS-teacherVerMobile', function(){
            var $self = $(this);
            var _teacherMobile = $(".JS-teacherMobile");
            var _teacherSmsCode = $(".JS-teacherSmsCode");
            if(!$(".JST-agreement").hasClass('active')){
                $self.addClass("disabled");
                return false;
            }
            if( $self.hasClass('dis') ){
                return false;
            }

            if( !$17.isMobile(_teacherMobile.val()) ){
                ErrorInfoAlert(_teacherMobile, "手机号错误");
                return false;
            }

            if( !$17.isNumber(_teacherSmsCode.val()) ){
                ErrorInfoAlert(_teacherSmsCode, "短信验证码错误");
                return false;
            }

            $self.addClass('dis');
            $.post("/signup/validatemobileonly.vpage", {
                code : _teacherSmsCode.val(),
                mobile : _teacherMobile.val()
            }, function(data){
                if(data.success){
                    $(".js-registerTemplateType[data-type='teacher']").show();
                    $(".js-registerTemplateType[data-type='student']").hide();
                    $("#register_template").show();

                    $(".JS-indexPageBox").hide();
                    $("body").removeClass('overflow-h');

                    teacherForm.mobile = _teacherMobile.val();
                    teacherForm.code = _teacherSmsCode.val();
                }else{
                    if( data.info.indexOf("手机号") > -1 ){
                        ErrorInfoAlert(_teacherMobile, data.info);
                    }else{
                        ErrorInfoAlert(_teacherSmsCode, data.info);
                    }
                    $self.removeClass('dis');
                }
            });
        }).on("click",".JST-agreement",function () {
            var _this = $(this);
            // var $inputVal = $.trim($('.JS-classInput').val());
            // var flag = $17.isNumber($inputVal) && $inputVal.length >= 5 && !$('.JS-classInput').parent().hasClass("error");
            if (_this.hasClass("active")){
                // if(flag){
                $(".JS-teacherVerMobile").addClass("disabled");
                // }
                _this.removeClass("active");
            }else{
                // if(flag){
                $(".JS-teacherVerMobile").removeClass("disabled");
                // }
                _this.addClass("active");
            }
        });
    }


    /* 新官网首页 */
    var productList = $('.JS-productServer');
    productList.mouseover(function () {
        $('.JS-productWrap').show();
    });
    productList.mouseout(function () {
        $('.JS-productWrap').hide();
    });




    $(document).on("click", ".JS-clear-btn", function () {
        clearMain($("#RenderMain"));
    });

    $(document).on("keyup", ".JS-inputEvent", function () {
        var $self = $(this);

        if ($self.val() != '') {
            $self.parent().removeClass("error");
        }
    });

    $(document).on("click", ".JS-pwd", function () {
        var $self = $(this);

        if (!$self.hasClass('show')) {
            $('#index_login_password').prop('type', 'text');
            $self.addClass('show');
        } else {
            $('#index_login_password').prop('type', 'password');
            $self.removeClass('show');
        }
    });

    //收不到验证码按钮
    $(document).on("click", ".JS-receiveNotCode", function () {
        var $self = $(this);

        var _teacherMobile = $(".JS-teacherMobile");
        var _teacherCaptcha = $(".JS-teacherCaptcha");

        if( !$17.isMobile(_teacherMobile.val())){
            ErrorInfoAlert(_teacherMobile, "请填写正确的手机号码");
            return false;
        }

        if( !$17.isNumber(_teacherCaptcha.val()) || _teacherCaptcha.val().length < 4){
            ErrorInfoAlert(_teacherCaptcha, "验证码不可为空");
            return false;
        }

        if( $self.attr('data-type') == 'black' ){
            getCodeSwitchMode(0);
        }else{
            getCodeSwitchMode(1);
        }
    });

    function getCodeSwitchMode(step){
        if(step && step == 1){
            $(".JS-defaultMode").hide();
            $(".JS-voiceMode").show();
        }else{
            $(".JS-defaultMode").show();
            $(".JS-voiceMode").hide();
        }
    }

    //全局变量，记录弹窗提示确认教师身份的次数。
    var areYouTeacherCount = 0;
    $(document).on("click", ".JS-getCheckCode", function(){
        var $self = $(this);
        var _teacherMobile = $(".JS-teacherMobile");
        var _teacherCaptcha = $(".JS-teacherCaptcha");
        var AllGetCheckCodeBtn = $('.JS-getCheckCode');

        if($self.hasClass("dis")){
            return false;
        }

        if( !$17.isMobile(_teacherMobile.val())){
            ErrorInfoAlert(_teacherMobile, "请填写正确的手机号码");
            return false;
        }

        if( !$17.isNumber(_teacherCaptcha.val()) || _teacherCaptcha.val().length < 4){
            ErrorInfoAlert(_teacherCaptcha, "验证码不可为空");
            return false;
        }

        var postData = {
            mobile: _teacherMobile.val(),
            count : 1,
            cid: contextId,
            captchaToken : captchaToken,
            captchaCode : _teacherCaptcha.val()
        };

        //语音获取验证码
        if( $self.attr('data-type') == 'voice' ){
            postData.voice = true;
        }

        var sendReq = function(){
            AllGetCheckCodeBtn.addClass("dis");

            $.post("/signup/tmsignsvc.vpage", postData, function(data){
                var timerCount, timer, second = 60;

                if(data.success){
                    timerCount = second;
                }else{
                    timerCount = data.timer || null;
                }

                //语音获取验证码 - 跳回初始注册
                if( $self.attr('data-type') == 'voice' ){
                    getCodeSwitchMode(0);
                }

                if(timerCount == null) {
                    var info = data.info;

                    if(info.indexOf("手机") > -1){
                        ErrorInfoAlert(_teacherMobile, info);
                        $17.voxLog({
                            module : "register-getCodeError",
                            op : "mobile",
                            s0: _teacherMobile.val()
                        });
                    }else if(info.indexOf("验证码") > -1){
                        ErrorInfoAlert(_teacherCaptcha, "验证码错误");
                        $17.voxLog({
                            module : "register-getCodeError",
                            op : "captcha",
                            s0: _teacherCaptcha.val()
                        });
                    }else{
                        ErrorInfoAlert($self, info);
                    }
                    refreshCaptcha();

                    AllGetCheckCodeBtn.removeClass("dis");


                    return false;
                }

                AllGetCheckCodeBtn.addClass("dis").html(timerCount + "s后重发");

                timer = $.timer(function() {
                    /*短信验证码的计时*/
                    if(timerCount <= 0){
                        /*短信验证码的计时*/
                        AllGetCheckCodeBtn.removeClass("dis");
                        AllGetCheckCodeBtn.text("获取验证码");
                        timer.stop();
                    } else {
                        /*短信验证码的计时*/
                        timerCount--;
                        AllGetCheckCodeBtn.text(timerCount + "s后重发");
                    }
                });
                timer.set({ time : 1000});
                timer.play();
            });
        };

        //先提示是否是教师角色
        areYouTeacher(sendReq);
    });

    //input event
    $(document).on("keyup blur", "input:text, input:password", function(){
        var $self = $(this);
        var $label = $self.siblings("label");
        if($self.parent().hasClass("ln-int")){
            return false;
        }
        if($self.is(":focus")){
            $self.val() == '' ? $label.show() : $label.hide();
        }else{
            $self.val() == '' ? $label.show() : $label.hide();
        }
    });

    //input event
    $(document).on("click", "label", function(){
        var $this = $(this);

        $this.siblings("input").focus();
    });

    function ErrorInfoAlert(id, val){
        id.focus();
        id.parent().addClass('error');
        id.siblings(".errorTips").text(val);
    }

    function areYouTeacher(callback){
        if(areYouTeacherCount === 0){
            $.prompt('<div style="padding: 0 38px;line-height: 22px;text-align: center;">注册后，我们将主动联系您核实老师身份。</div>', {
                focus : 1,
                title: '注册老师账号',
                buttons: {'取消': false, '我是老师': true},
                submit : function(e, v){
                    if(v) {
                        areYouTeacherCount++;
                        callback();
                    }
                }
            });
        }
        else{
            callback();
        }
    }

    //clear document html
    function clearMain(id) {
        id.empty();
        $("body").removeClass('overflow-h');
    }

    function indexSwitch() {
        var _winHeight = $(window).height();

        if(_winHeight <= 600){
            $(".JS-indexSwitch-main li").height(600);
        }else{
            $(".JS-indexSwitch-main li").height( _winHeight );
        }
    }
}());

function refreshCaptcha() {
    $("#captchaInputLogin").val("");
    $('#captchaImageLogin').attr('src', "/captcha?" + $.param({
        'module': 'regCaptcha',
        'token': captchaToken,
        't': new Date().getTime()
    }));
}

function IEVersion() {
    var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    var isIE = userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1; //判断是否IE<11浏览器
    var isEdge = userAgent.indexOf("Edge") > -1 && !isIE; //判断是否IE的Edge浏览器
    var isIE11 = userAgent.indexOf('Trident') > -1 && userAgent.indexOf("rv:11.0") > -1;
    if(isIE) {
        var reIE = new RegExp("MSIE (\\d+\\.\\d+);");
        reIE.test(userAgent);
        var fIEVersion = parseFloat(RegExp["$1"]);
        if (fIEVersion == 7) {
            return 7;
        } else if (fIEVersion == 8) {
            return 8;
        } else if (fIEVersion == 9) {
            return 9;
        } else if (fIEVersion == 10) {
            return 10;
        } else {
            return 6;//IE版本<=7
        }
    } else if (isEdge) {
        return 'edge';//edge
    } else if (isIE11) {
        return 11; //IE11
    } else {
        return -1;//不是ie浏览器
    }
}

function hasBeenUser(){
    //如果已有注册成功学生号，必须填写手机绑定
    var studentSecondary = $(".js-registerTemplateType[data-type='student']");
    //（第二次注册学生号必须绑定手机号）
    if($17.getCookieWithDefault("stregscs")){
        studentSecondary.find("input[name='mobile']").addClass("require").parent().show();
        studentSecondary.find("input[name='ver_code']").addClass("require").parent().show();

        $17.voxLog({
            app : "student",
            module : "studentRegisterFirstPopup",
            op : "showMobileReg2C"
        });

        studentForm.mobileGray = "showMobileReg2C";
    }else{
        $17.voxLog({
            app : "student",
            module : "studentRegisterFirstPopup",
            op : "hideMobileReg"
        });
        studentForm.mobileGray = "hideMobileReg";
    }

    $.prompt(template("T:已用过一起作业提示", {}),{
        buttons : { },
        position: {width: 540},
        classes : {
            fade: 'jqifade',
            close: 'w-hide'
        },
        loaded : function(){
            $(document).on("click", ".v-clickHasBeenUser", function(){
                $(".t-propClazz-box[data-type='welcome']").hide().siblings("[data-type='login']").show();
                $17.voxLog({
                    app : "student",
                    module : "studentRegisterFirstPopup",
                    op : "clickYes"
                });
            });

            $(document).on("click", ".v-clickNewRegisterUser", function(){
                $.prompt.close();
                $17.voxLog({
                    app : "student",
                    module : "studentRegisterFirstPopup",
                    op : "clickNo"
                });
            });
        }
    });
}
