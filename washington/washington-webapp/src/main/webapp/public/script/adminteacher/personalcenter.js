/**
 * @author: pengmin.chen
 * @description: "校长或教研员-个人中心"
 * @createdDate: 2018.06.19
 * @lastModifyDate: 2018.06.20
 */


// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗
define(["jquery", "knockout", "YQ", "knockout-switch-case", "impromptu", "voxLogs"],function($, ko, YQ){
    var personalcenterModal = function () {
        var self = this;
        var databaseLogs = "tianshu_logs";

        $.extend(self, {
            isShowCenterIndexTemp: ko.observable(false),
            isShowInformationTemp: ko.observable(false),
            isShowAccountSafeTemp: ko.observable(false),

            schoolName: ko.observable(''),
            reginonAddress: ko.observable(''),
            phoneNumber: ko.observable('123'),

            modifyNameInputName: ko.observable(userName), // 修改姓名
            isShowMobileModifySecret: ko.observable(true), // 是否展示修改登录密码模块（无号码时不展示，该字段控制首页不展示手机号、账号安全页不展示修改密码模块、展示更换手机还是绑定手机）

            modifySecretCodeBtnText: ko.observable('获取短信验证码'), // 修改密码获取验证码按钮文案
            isDisabledModifySecretCodeBtn: ko.observable(false), // 修改密码获取验证码按钮状态
            modifySecretInputCode: ko.observable(''), // 修改密码输入的验证码
            modifySecretInputSecret1: ko.observable(''), // 修改密码输入的密码1
            modifySecretInputSecret2: ko.observable(''),// 修改密码输入的密码1

            modifyMobileInputMobile: ko.observable(''), // 修改手机输入的手机号
            modifyMobileCodeBtnText: ko.observable('获取短信验证码'), // 修改手机获取验证码按钮文案
            isDisabledModifyMobileCodeBtn: ko.observable(false), // 修改手机获取验证码按钮状态
            modifyMobileInputCode: ko.observable(''), // 修改手机输入的验证码

            // 跳转到个人中心
            jumpToInfomation: function () {
                var param = $.param({
                    module: 'information'
                });
                window.location.href = window.location.origin + window.location.pathname + '?' + param;
            },
            // 跳转到账号安全
            jumpToAccountSafe: function (type) {
                if (type) {
                    var param = $.param({
                        module: 'accountsafe',
                        type: type
                    });
                } else {
                    var param = $.param({
                        module: 'accountsafe'
                    });
                }
                window.location.href = window.location.origin + window.location.pathname + '?' + param;
            },
            // 展示修改登录密码
            showModifySecretModule: function () {
                $('#showModifySecret').toggle('fast');
            },
            // 展示更换手机
            showModifyMobileModule: function () {
                $('#showModifyMobile').toggle('fast');
            },
            getBasicInfo: function () {
                $.ajax({
                    url: '/schoolmaster/loadPersonInfo.vpage',
                    type: 'GET',
                    success: function (res) {
                        if (res.success) {
                            if (!res.mobile) {
                                self.isShowMobileModifySecret(false);
                            }
                            self.schoolName(res.schoolName);
                            self.reginonAddress(res.schoolRegion || res.regionNames.join('、'));
                            self.phoneNumber(res.mobile);
                        } else {
                            alertError(res.info || '出错了，请重试！');
                        }
                    }
                });
            },
            // 确认修改资料
            sureModifyInfo: function () {
                var modifyNameInputName = self.modifyNameInputName().replace(/(^\s*)|(\s*$)/g, '');
                if (!modifyNameInputName) {
                    alertError('请输入新手机号码');
                    return ;
                }
                if (modifyNameInputName === window.userName) {
                    alertError('当前姓名未修改，请修改后再保存');
                    return ;
                }
                $.ajax({
                    url: '/schoolmaster/modifyprofile.vpage',
                    type: 'POST',
                    data: {
                        name: modifyNameInputName
                    },
                    success: function (res) {
                        if (res.success) {
                            alertError('资料修改成功', '', function () {
                                window.location.href = '/' + window.idType + '/admincenter.vpage'; // 返回首页
                            });
                        } else {
                            alertError(res.info || '出错了，请重试！');
                        }
                    }
                });
            },
            // 修改密码获取验证码
            modifySecretGetCode: function () {
                if (self.isDisabledModifySecretCodeBtn()) return ;
                $.ajax({
                    url: '/schoolmaster/smsvalidatecode.vpage',
                    type: 'POST',
                    data: {
                        mobile: self.modifySecretInputCode()
                    },
                    success: function (res) {
                        if (res.success) {
                            // 倒计时60s
                            var countTime = 60;
                            self.isDisabledModifySecretCodeBtn(true);
                            self.modifySecretCodeBtnText(countTime + 's后重新获取');
                            var countdownTimer = setInterval(function () {
                                if (countTime > 0) {
                                    countTime--;
                                    self.modifySecretCodeBtnText(countTime + 's后重新获取');
                                } else {
                                    clearInterval(countdownTimer);
                                    self.modifySecretCodeBtnText('获取验证码');
                                    self.isDisabledModifySecretCodeBtn(false);
                                }
                            }, 1000);
                        } else {
                            alertError(res.info || '出错了，请重试！');
                        }
                    }
                });
            },
            // 确认修改密码
            sureModifySecret: function () {
                var modifySecretInputCode = self.modifySecretInputCode().replace(/(^\s*)|(\s*$)/g, '');
                var modifySecretInputSecret1 = self.modifySecretInputSecret1().replace(/(^\s*)|(\s*$)/g, '');
                var modifySecretInputSecret2 = self.modifySecretInputSecret2().replace(/(^\s*)|(\s*$)/g, '');
                if (!modifySecretInputCode) {
                    alertError('请输入短信验证码');
                    return ;
                }
                if (!modifySecretInputSecret1) {
                    alertError('请输入新的登录密码');
                    return ;
                }
                if (!(/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$/i.test(modifySecretInputSecret1))) {
                    alertError('请设置6~16位数字和字母组合的密码');
                    return ;
                }
                if (!modifySecretInputSecret2) {
                    alertError('请再次输入新的登录密码');
                    return ;
                }
                if (modifySecretInputSecret1 !== modifySecretInputSecret2) {
                    alertError('请输入相同的登录密码');
                    return ;
                }
                $.ajax({
                    url: '/schoolmaster/resetpassword.vpage',
                    type: 'POST',
                    data: {
                        verifyCode: self.modifySecretInputCode(),
                        newPassword: self.modifySecretInputSecret1()
                    },
                    success: function (res) {
                        if (res.success) {
                            alertError('密码修改成功', '', function () {
                                window.location.href = '/ucenter/logout.vpage'; // 退出当前账号
                            });
                        } else {
                            alertError(res.info || '出错了，请重试！');
                        }
                    }
                });
            },
            // 修改手机获取验证码
            modifyMobileGetCode: function () {
                if (self.isDisabledModifyMobileCodeBtn()) return ;
                var modifyMobileInputMobile = self.modifyMobileInputMobile().replace(/(^\s*)|(\s*$)/g, '');
                if (!modifyMobileInputMobile) {
                    alertError('请输入新手机号码');
                    return ;
                }
                $.ajax({
                    url: '/schoolmaster/sendmobilecode.vpage',
                    type: 'POST',
                    data: {
                        mobile: modifyMobileInputMobile
                    },
                    success: function (res) {
                        if (res.success) {
                            // 倒计时60s
                            var countTime = 60;
                            self.isDisabledModifyMobileCodeBtn(true);
                            self.modifyMobileCodeBtnText(countTime + 's后重新获取');
                            var countdownTimer = setInterval(function () {
                                if (countTime > 0) {
                                    countTime--;
                                    self.modifyMobileCodeBtnText(countTime + 's后重新获取');
                                } else {
                                    clearInterval(countdownTimer);
                                    self.modifyMobileCodeBtnText('获取验证码');
                                    self.isDisabledModifyMobileCodeBtn(false);
                                }
                            }, 1000);
                        } else {
                            alertError(res.info || '出错了，请重试！');
                        }
                    }
                });
            },
            // 确认更换手机
            sureModifyMobile: function () {
                var modifyMobileInputCode = self.modifyMobileInputCode().replace(/(^\s*)|(\s*$)/g, '');
                if (!modifyMobileInputCode) {
                    alertError('请输入新手机号码');
                    return ;
                }
                $.ajax({
                    url: '/schoolmaster/validatemobile.vpage',
                    type: 'POST',
                    data: {
                        code: modifyMobileInputCode
                    },
                    success: function (res) {
                        if (res.success) {
                            alertError('手机号绑定成功', '', function () {
                                window.location.href = '/' + window.idType + '/admincenter.vpage'; // 返回首页
                            });
                        } else {
                            alertError(res.info || '出错了，请重试！');
                        }
                    }
                });
            }
        });

        // 简单弹窗报错
        var alertError = function (content, title, callback) {
            var title = title || '系统提示';

            $.prompt(content, {
                title: title,
                buttons: {'确定': true},
                focus : 0,
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        e.preventDefault();
                        if (callback) {
                            callback();
                        } else {
                            $.prompt.close();
                        }
                    }
                }
            });
        };

        // 根据链接参数的不同展示不同的模板
        var initShowModule = function () {
            var module = YQ.getQuery('module');
            var type = YQ.getQuery('type');

            // 控制大模块展示
            switch (module) {
                case '':
                    self.isShowCenterIndexTemp(true);
                    break;
                case 'information':
                    self.isShowInformationTemp(true);
                    break;
                case 'accountsafe':
                    self.isShowAccountSafeTemp(true);
                    break;
                default:
                    self.isShowCenterIndexTemp(true);
            }

            // 控制展开账号安全里面的某个类型(1表示修改密码，2表示更换手机)
            if (module === 'accountsafe') {
                setTimeout(function () {
                    if (+type === 1) {
                        $('#showModifySecret').toggle('fast');
                    } else if (+type === 2) {
                        $('#showModifyMobile').toggle('fast');
                    }
                }, 10);
            }

            // 根据errorType决定是否弹窗报错
            if (YQ.getQuery('errtype') === '1') { // 没有绑定手机号码
                alertError('系统检测到您的账号还未绑定手机号码，请先绑定手机号码再查看数据哦~');
            } else if (YQ.getQuery('errtype') === '2') { // 密码过于简单
                alertError('系统检测到您的账号密码过于简单，请先修改密码再查看数据哦~');
            }
        };

        // 绑定全局事件
        var bindGlobalEvent = function () {
            // 听力卷TTS打点
            $(document).on('click', '.trackTTS', function () {
                YQ.voxLogs({
                    database: databaseLogs,
                    module: 'm_V254hUwf',
                    op: "listening_test_click"
                });
            });
        };

        // 初始化
        initShowModule();
        bindGlobalEvent();
        self.getBasicInfo();
    };

    var tcModal = new personalcenterModal();
    ko.applyBindings(tcModal,document.getElementById("personalcenter"));
});