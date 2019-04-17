/**
 * Created by pengmin.chen on 2017/6/26.
 */
define(["jquery","knockout","YQ","knockout-switch-case","impromptu"],function($,ko,YQ){
    var teacherCenterModal = function () {
        var self = this;
        // 使用$.extend语法将对象合并到self中，简化写法
        $.extend(self, {
            schoolName: ko.observable(''),
            schoolRegion: ko.observable(''),
            mobile: ko.observable(''),
            // 请求基础信息
            requestBasicInfo: function () {
                $.ajax({
                    url: '/specialteacher/center/basicinfo.vpage',
                    type: 'GET',
                    success: function (data) {
                        if (data.success) {
                            self.schoolName(data.schoolName);
                            self.schoolRegion(data.schoolRegion);
                            self.mobile(data.mobile);
                        } else {
                            self.showPrompt(data.info, '系统提示');
                        }
                    }
                });
            },
            // 提交我的资料修改
            sureModifyInfo: function () {
                var modifyTeacherName = $('.JS-teacherName');
                if (!$.trim(modifyTeacherName.val())) {
                    self.showPrompt('姓名不能为空', '系统提示');
                    return false;
                }
                if ($.trim(modifyTeacherName.val()) === globalTeacherName) {
                    self.showPrompt('当前姓名未修改', '系统提示');
                    return false;
                }
                var data = {name: $.trim(modifyTeacherName.val())};
                $.post('/specialteacher/center/modifyprofile.vpage', data, function (data) {
                    if (data.success) {
                        self.showPrompt('姓名修改成功', '系统提示', function () {
                            window.location.href = '/specialteacher/center/teachercenter.vpage';
                        });
                    } else {
                        self.showPrompt(data.info, '系统提示');
                    }
                });
            },
            // 去更换手机
            toChangeMobilePage: function () {
                window.location.href = '#modifyAccountTemp';
                self.changeMobile();
            },
            // 点击修改密码
            modifySecret: function () {
                $('.JS-showModifySecret').toggle('fast');
            },
            // 点击更换手机
            changeMobile: function () {
                $('.JS-ShowModifyMobile').toggle('fast');
            },
            // 获取短信验证码
            getVerifiCode: function (index, data, event) {
                var getMobileBtn = $('.JS-getMobileCodeBtn' + index);
                if (getMobileBtn.hasClass('btn_disable')) return false;
                var data = index === 1 ? {} : {mobile: $.trim($('.JS-newMobile').val())};
                var URL= index === 1 ? '/specialteacher/center/smsvalidatecode.vpage' : '/specialteacher/center/sendmobilecode.vpage';
                $.post(URL, data, function (data) {
                    if (data.success) {
                        var countTime = 60;
                        getMobileBtn.text(countTime + '秒之后可重新发送').addClass('btn_disable');
                        var countdownTimer = setInterval(function () {
                            if (countTime > 0) {
                                countTime--;
                                getMobileBtn.text(countTime + '秒之后可重新发送');
                            } else {
                                clearInterval(countdownTimer);
                                getMobileBtn.text('获取短信验证码').removeClass('btn_disable');
                            }
                        }, 1000);
                    } else {
                        self.showPrompt(data.info, '系统提示');
                    }
                });
            },
            // 修改登录密码
            modifyLoginSecret: function () {
                var modifySecretCode = $('.JS-modifySecretCode');
                var newLoginSecret1 = $('.JS-newLoginSecret1');
                var newLoginSecret2= $('.JS-newLoginSecret2');
                if (!$.trim(modifySecretCode.val())) {
                    self.showPrompt('请输入短信验证码', '系统提示');
                    return false;
                }
                if ($.trim(modifySecretCode.val()).length !== 4) {
                    self.showPrompt('请输入正确的短信验证码', '系统提示');
                    return false;
                }
                if (!$.trim(newLoginSecret1.val())) {
                    self.showPrompt('请输入新的登录密码', '系统提示');
                    return false;
                }
                if (!$.trim(newLoginSecret2.val())) {
                    self.showPrompt('请再次输入新的登录密码', '系统提示');
                    return false;
                }
                if (newLoginSecret1.val() !== newLoginSecret2.val()) {
                    self.showPrompt('请输入相同的登录密码', '系统提示');
                    return false;
                }
                var data = {
                    verifyCode: $.trim(modifySecretCode.val()),
                    newPassword: $.trim(newLoginSecret1.val())
                }
                $.post('/specialteacher/center/resetpassword.vpage', data, function (data) {
                    if (data.success) {
                        self.showPrompt('密码修改成功', '系统提示', function () {
                            window.location.href = '/ucenter/logout.vpage'; // 退出当前账号
                        });
                    } else {
                        self.showPrompt(data.info, '系统提示');
                    }
                })
            },
            // 修改手机号码
            modifyMobile: function () {
                var newMobile = $('.JS-newMobile');
                var modifyMobileCode = $('.JS-modifyMobileCode');

                if (!$.trim(newMobile.val())) {
                    self.showPrompt('请输入新的手机号码', '系统提示');
                    return false;
                }
                if (!$.trim(modifyMobileCode.val())) {
                    self.showPrompt('请输入短信验证码', '系统提示');
                    return false;
                }
                if ($.trim(modifyMobileCode.val()).length !== 4) {
                    self.showPrompt('请输入正确的短信验证码', '系统提示');
                    return false;
                }

                var data = {
                    code: $.trim(modifyMobileCode.val())
                };
                $.post('/specialteacher/center/validatemobile.vpage', data, function (data) {
                    if (data.success) {
                        self.showPrompt('手机号修改成功', '系统提示', function () {
                            window.location.href = '/specialteacher/center/teachercenter.vpage';
                        });
                    } else {
                        self.showPrompt(data.info, '系统提示');
                    }
                });
            },
            // 删除消息
            delMessage: function () {

            },
            // 弹窗显示普通信息
            showPrompt: function (para, title, closeCallback) {
                $.prompt(para, {
                    title: title,
                    buttons: {"确定": true },
                    close: function () {
                        if (closeCallback && typeof(closeCallback) === 'function') {
                            closeCallback()
                        }
                    }
                });
            }
        });
        self.requestBasicInfo();

        // 点击导航时更换hash，且不刷新页面更换module
        var hashMsg = window.location.hash;
        hashPage(hashMsg);
        $(window).on('hashchange', function (e) {
            hashMsg = window.location.hash;
            hashPage(hashMsg);
        });
        function hashPage(hashMsg) {
            // 收集导航条链接属性，这样做的目的是为了防止后期其他同学开发的时候添加导航条却忘了在此处增加hash，估采用自动收集方法
            var hashArr = [];
            var navList = $('.JS-navMode').children('a');
            for (var i = 0, len = navList.length; i < len; i++) {
                hashArr.push(navList.eq(i).attr('href'));
            }
            hashMsg = hashArr.indexOf(hashMsg) > -1 ? hashMsg : $('.JS-navMode').children('a').eq(0).attr('href');
            // $('#moduleBox').html($(hashMsg).html()); // 显示id号对应的模块
            $('#moduleBox').children('div').eq(hashArr.indexOf(hashMsg)).show().siblings().hide(); // 切换模块显示，此处有个注意点，模块如果不实现插入，后续插入时绑定失效
            $('.JS-navMode').children('a').eq(hashArr.indexOf(hashMsg)).addClass('active').siblings().removeClass('active'); // 切换导航条激活样式
        }
    };

    var tcModal = new teacherCenterModal();
    ko.applyBindings(tcModal,document.getElementById("teacherCenterApp"));
});