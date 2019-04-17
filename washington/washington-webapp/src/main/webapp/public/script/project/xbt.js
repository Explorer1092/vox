define(['jquery', 'knockout', 'weui', 'openApp', 'voxLogs'], function($, ko){
    var defaultInitMain;

    //打点用
    var isReadingBook   = (getQueryString('activity') === 'readingZdy'),
        _activityName   = isReadingBook ? 'readingZdy' : 'picxbt',
        _logModule      = isReadingBook ? 'm_E7Ljxo1M' : 'm_oxE6BtxH',
        _shareTitle     = isReadingBook ? '亲子读书会' : '期末英雄团',
        _shareContent   = isReadingBook ? '寒假到啦，同学们一起阅读英文绘本，过个充实的寒假吧！' : '期末到啦，同学们组团复习效果棒！还能得到学豆奖励哦！',
        _shareSource    = getQueryString('userType'),
        _info           = '',
        _userType       = '',
        _s2             = '',
        _dataType       = /(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent) ? 'app_17Parent_ios' : 'app_17parent_android';

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'IndexMain':
                defaultInitMain = new IndexMain();//index
                break;
            case 'ShareMain':
                defaultInitMain = new ShareMain();//share
                break;
            default:
                //default
        }
    }

    function IndexMain(){
        var $this = this;

        $this.templateContent = ko.observable('T:StepModuleIndex');

        var $_shortUrl = "", $_originalUrl = "";
        function getShortUrl(u, callback){
            if($_shortUrl != '' && $_originalUrl == u  && callback){
                callback($_shortUrl);
                return false;
            }

            $_originalUrl = u;
            $.post("/project/crt.vpage", {url : u, activity: _activityName}, function(data){
                if(data.success){
                    $_shortUrl = u = data.url;
                }

                if (callback){ callback(u); }
            });
        }

        //click share
        var shareLink = encodeURI(location.protocol + '//'+ location.host + '/usermobile/xbt/share.vpage?userId=' + userData.userId + '&activity=' + _activityName + '&avatarUrl=' + userData.currentUserAvatarUrl + '&userName=' + userData.currentUserName + '&userType=' + userData.userType);
        getShortUrl(shareLink, function(u){
            shareLink = u;

            if (window['external'] && window.external['shareMethod']) {
                window.external.shareMethod(JSON.stringify({
                    title: _shareTitle,
                    content: _shareContent,
                    url     : shareLink,
                    type    : "SHOW_NATIVE_BUTTON",//显示分享
                    channel : 4,
                    moudle: _shareTitle
                }));
            }
        });


        $this.clickInvite = function(){
            //点击分享即关注
            $.post('/usermobile/xbt/follow_official_account.vpage', {sid: getQueryString('sid'), activity: _activityName});
            if (window.external && window.external['shareMethod']) {
                window.external.shareMethod(JSON.stringify({
                    title: _shareTitle,
                    content: _shareContent,
                    url: shareLink,
                    type: "SHARE",
                    channel: 4,
                    moudle: _shareTitle
                }));
            }else if (window['external'] && window.external['shareInfo']) {
                window.external.shareInfo(JSON.stringify({
                    title: _shareTitle,
                    content: _shareContent,
                    url: shareLink
                }));
            }else{
                $.alert('分享失败');
            }
        };

        //rule
        $this.isRuleShow = ko.observable(false);
        $this.clickRule = function(){
            if($this.isRuleShow()){
                $this.isRuleShow(false);
            }else{
                $this.isRuleShow(true);
            }
        }
    }

    var captchaToken;

    function ShareMain(){
        var $this = this;

        $this.templateContent = ko.observable('T:StepModule-Mobile');
        $this.database = ko.observable({});
        $this.mobile = ko.observable();
        $this.inviterId = ko.observable(getQueryString('userId') || 0);
        $this.imgCode = ko.observable();
        $this.imgSrc = ko.observable();
        $this.smsCode = ko.observable();

        $this.userData = {
            avatarUrl: getQueryString('avatarUrl'),
            userName: getQueryString('userName')
        };

        $this.joinNow = function(){
            if(!isMobile($this.mobile())){
                $.alert('请输入正确的手机号');
                return false;
            }

            $.post('/usermobile/xbt/join_step1.vpage', {
                mobile: $this.mobile(),
                inviter_id: $this.inviterId(),
                activity: _activityName
            }, function(data){
                if(data.success){
                    if(data.send_code){
                        $this.templateContent('T:StepModule-Ver');
                        captchaToken = data.captchaToken;
                        refreshCaptcha();

                        //打点：参团页_老用户验证码页_被加载
                        YQ.voxLogs({
                            dataType : _dataType,
                            database : 'normal',
                            module  : _logModule,
                            op      : "o_ZLuaOjx5",
                            s0      : _shareSource
                        });
                    }else{
                        $this.templateContent('T:StepModule-Over');
                        $this.database({info: data.info || ''});

                        //打点：参团页_新用户页面_被加载
                        _info = data.info || '';
                        YQ.voxLogs({
                            dataType : _dataType,
                            database : 'normal',
                            module  : _logModule,
                            op      : "o_ONOs6Gf5",
                            s0      : _shareSource,
                            s1      : _info
                        });
                    }
                }else{
                    $.alert(data.info);
                }
            });
        };

        $this.justNumber = function(e, num, maxlength, max){
            if(e){
                num(e.target.value);
                num(e.target.value.replace(/\D/g, ''));

                //设置字符串最大长度
                maxlength && (num().length > parseInt(maxlength)) && num(num().substr(0, parseInt(maxlength)));
                //设置最大值
                max && (parseInt(num()) > parseInt(max)) && num(max);
            }
        };

        $this.recordTime = ko.observable(0);
        $this.getMobileCode = function(data, event){
            if($(event.target).hasClass('dis')){
                return false;
            }

            if(!isNumber($this.imgCode()) || $this.imgCode().length < 4){
                $.alert('请输入正确的图片验证码');
                return false;
            }

            $(event.target).addClass('dis');
            $.post('/usermobile/xbt/send_code.vpage', {
                captchaToken: captchaToken,
                captchaCode: $this.imgCode(),
                mobile: $this.mobile(),
                activity: _activityName
            }, function(data){
                if(data.success){
                    setIntervalTime(60, function(){
                        $(event.target).removeClass('dis');
                    });
                }else{
                    if(data.timer){
                        setIntervalTime(data.timer, function(){
                            $(event.target).removeClass('dis');
                        });
                    }else{
                        $.alert('图形验证码错误', function(){
                            refreshCaptcha();
                        });
                        $(event.target).removeClass('dis');
                    }
                }
            });
        };

        function setIntervalTime(rTime, callback){
            var autoTime = setInterval(function(){
                if(rTime <= 0){
                    if(callback){callback();}
                    clearInterval(autoTime);
                }
                $this.recordTime(rTime);
                rTime--;
            }, 1000);
        }

        $this.submitForm = function(data, event){
            if($(event.target).hasClass('dis')){
                return false;
            }

            if(!isNumber($this.smsCode()) || $this.smsCode().length < 4){
                $.alert('请输入正确的短信验证码');
                return false;
            }

            $(event.target).addClass('dis');
            $.post('/usermobile/xbt/join_step2.vpage', {
                mobile: $this.mobile(),
                inviter_id: $this.inviterId(),
                code: $this.smsCode(),
                activity: _activityName
            }, function(data){
                if(data.success){
                    $this.database({info: data.info || '', no_child: data.no_child || false});
                    $this.templateContent('T:StepModule-Success');

                    //打点：参团结果页_被加载
                    _userType = data.ut || '';
                    _s2 = data.info || '';
                    YQ.voxLogs({
                        dataType : _dataType,
                        database : 'normal',
                        module  : _logModule,
                        op      : "o_LNafTlvq",
                        s0      : _shareSource,
                        s1      : _userType,
                        s2      : _s2
                    });
                }else{
                    $.alert(data.info, function(){
                        if(data.info.indexOf('自己不能') !== -1){
                            $this.templateContent('T:StepModule-Mobile');
                            $this.mobile('');
                        }
                    });
                }
                $(event.target).removeClass('dis');
            });
        };

        $this.clickImgCode = function(){
            refreshCaptcha();
        };

        function refreshCaptcha() {
            $this.imgCode('');

            $this.imgSrc(
                "/captcha?" + $.param({
                    'module': 'regCaptcha',
                    'token': captchaToken,
                    't': new Date().getTime()
                })
            );
        }
    }

    function getQueryString(item) {
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    //验证是否手机号
    function isMobile(value){
        value = value + "";
        //严格判定
        var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
        //简单判定
        var reg = /^1[0-9]{10}$/;
        if(!value || value.length != 11 || !reg.test(value)){
            return false;
        }
        return true;
    }

    function getExternal(){
        var _WIN = window;
        if(_WIN['yqexternal']){
            return _WIN.yqexternal;
        }else if(_WIN['external']){
            return _WIN.external;
        }else{
            return _WIN.external = function(){};
        }
    }

    function isNumber(value){
        var reg = /^[0-9]+$/;
        if(value == '' || !reg.test(value)){
            return false;
        }
        return true;
    }

    ko.applyBindings(defaultInitMain);

    /*-----------打点-------------*/
    //打点：活动页_被加载
    if(location.href.indexOf('xbt/index') !== -1){
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_PaC3l49A",
            s0      : getQueryString('from'),
            s1      : hasGroup
        });
    }
    $('.js-invite').on('click', function(){
        if($(this).hasClass('for-log')){
            //打点：活动页_+按钮_被点击
            YQ.voxLogs({
                module  : _logModule,
                op      : "o_ck1rqriY"
            });
        }else{
            //打点：活动页_呼朋唤友按钮_被点击
            YQ.voxLogs({
                module  : _logModule,
                op      : "o_khfnaGiw",
                s0      : hasGroup
            });
        }
    });
    $('.js-rule').on('click', function(){
        //打点：活动页_详细规则按钮_被点击
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_H69goQFT"
        });
    });

    //打点：参团页_被加载
    if(location.href.indexOf('xbt/share') !== -1){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_ovWYx603",
            s0      : _shareSource
        });
    }
    //打点：参团页_【立即参加】按钮_被点击
    $('.js-join').on('click', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_W8GWNLfl",
            s0      : _shareSource
        });
    });
    //打点：参团页_新用户页面_【下载】按钮_被点击
    $(document).on('click', '.js-download', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_L1M9rD5f",
            s0      : _shareSource,
            s1      : _info
        });
    });

    //打点：参团页_老用户验证码页_【获取验证码】按钮_被点击
    $(document).on('click', '.js-getMobileCode', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_Cme7JLKa",
            s0      : _shareSource
        });
    });
    //打点：参团页_老用户验证码页_【立即参加】按钮_被点击
    $(document).on('click', '.js-join-old', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_1EMNqCsm",
            s0      : _shareSource
        });
    });

    //打点：参团结果页_【建团】按钮_被点击
    $(document).on('click', '.js-build', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_QwvdEa0Z",
            s0      : _shareSource,
            s1      : _userType,
            s2      : _s2
        });
    });
    //打点：参团结果页_【进入点读机】按钮_被点击
    $(document).on('click', '.js-redirect', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_QRuRn89W",
            s0      : _shareSource,
            s1      : _userType,
            s2      : _s2
        });
    });
    //打点：参团结果页_【去绑定】按钮_被点击
    $(document).on('click', '.js-goBind', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module  : _logModule,
            op      : "o_r7Gh5035",
            s0      : _shareSource
        });
    });
    /*------------打点结束------------*/
});