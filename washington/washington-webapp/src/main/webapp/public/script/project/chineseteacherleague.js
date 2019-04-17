$(function () {

    function TeacherLeagueMode() {
        var _this = this;
        _this.alertDialogFlag = ko.observable(false);
        _this.captchaInput = ko.observable();
        _this.successList = ko.observableArray([]);
        _this.failureList = ko.observableArray([]);
        _this.invitedTeacherNum = ko.observable();
        _this.particTime = ko.observable();
        _this.totalAwardNum = ko.observable();
        _this.inviteFailedNum = ko.observable();
        _this.authenticate = ko.observable();
        _this.captchaToken = ko.observable("0");
        _this.alertDialogData = ko.observable({
            state: ''
        });

        YQ.voxLogs({
            database: "web_teacher_logs",
            module: 'm_lIHTCSqE',
            op : 'o_QwJQ75Bd',
            s0: '小学'
        });

        _this.howToAuthBtn = function () {
            _this.alertDialogFlag(true);
            _this.alertDialogData({
                state: 'auth'
            });
        };

        _this.inputKeyUp = function (data,event) {
            var $element = $(event.currentTarget);
            $element.parent().siblings("span").removeClass("isShow");
        };

        $.get("/teacher/invite/getCaptchaToken.vpage", function(data){
            if(data.success){
                _this.captchaToken(data.captchaToken);
            }
        });
        setTimeout(function () {
            _this.changeCode();
        },200);

        // 看不清，换一张  TODO
        _this.changeCode = function () {
            _this.captchaInput("");
            $("#captchaImage").attr('src', "/captcha?" + $.param({
                    'module': 'regCaptcha',
                    'token': _this.captchaToken(),
                    't': new Date().getTime()
                }));
        };

        _this.closeDialog = function () {
            location.reload();
            _this.alertDialogFlag(false);
            _this.alertDialogData({
                state: ''
            });
            location.reload();
        };
        //发送邀请 TODO
        _this.inviteTeacher = function (data,event) {
            var $self = $(event.currentTarget);
            var _teacherName = $(".JS-teacherName");
            var _teacherMobile = $(".JS-teacherMobile");
            var _teacherCaptcha = $(".JS-teacherCaptcha");

            if( $self.hasClass('dis') ){
                return ;
            }

            if($17.isBlank(_teacherName.val()) || !$17.isCnString(_teacherName.val())){
                $(".JS-teacherName").val("");
                ErrorInfoAlert(_teacherName,"请填写正确的姓名");
                return ;
            }

            if( !$17.isMobile($.trim(_teacherMobile.val())) || $17.isBlank(_teacherMobile.val()) ){
                ErrorInfoAlert(_teacherMobile, "请输入正确格式的手机号");
                return ;
            }

            if( !$17.isNumber($.trim(_teacherCaptcha.val())) || $17.isBlank(_teacherCaptcha.val()) ){
                ErrorInfoAlert(_teacherCaptcha, "图片验证码格式错误");
                return ;
            }
            $self.addClass('dis');
            var data = {
                invitedTeacherName:_teacherName.val(),
                invitedTeacherMobile:_teacherMobile.val(),
                captchaToken:_this.captchaToken(),
                captchaCode:_teacherCaptcha.val()
            };
            $.post("/teacher/invite/inviteteacherbysms.vpage", data, function(data){
                if(data.success){
                    YQ.voxLogs({
                        database: "web_teacher_logs",
                        module: 'm_lIHTCSqE',
                        op : 'o_WwtcgLIl',
                        s0: '小学'
                    });
                    _this.alertDialogFlag(true);
                    _this.alertDialogData({
                        state: 'message'
                    });
                }else{
                    if( data.info.indexOf("正确的手机号码") > -1 ){
                        ErrorInfoAlert(_teacherMobile, data.info);
                    }else if( data.info.indexOf("验证码") > -1 ){
                        _this.changeCode();
                        ErrorInfoAlert(_teacherCaptcha, data.info);
                    }else if( data.info.indexOf("已注册") > -1 ){
                        _this.alertDialogFlag(true);
                        _this.alertDialogData({
                            state: 'isRegister'
                        });
                    }else{
                        $17.alert(data.info);
                    }
                    $self.removeClass('dis');
                }
            });

        };

        $.get("/teacher/invite/getActivityProcess.vpage",{
            activeSign: 'primaryChinese'
        }, function(data){
            if (data.success){
                _this.invitedTeacherNum(data.invitedTeacherNum);
                _this.totalAwardNum(data.totalAwardNum);
                _this.particTime(data.inviteDate);
                _this.inviteFailedNum(data.inviteFailedNum);
                _this.authenticate(data.authenticate);
                if (data.successList.length > 0){
                    _this.successList(data.successList);
                }
                if (data.failureList.length > 0){
                    _this.failureList(data.failureList);
                }
            }else{
                $17.alert(data.info);
            }
        });

        function ErrorInfoAlert(id, val){
            id.focus();
            id.val("");
            id.parent().siblings("span").addClass("isShow").text(val);
        }

    }

    ko.applyBindings(new TeacherLeagueMode());
});