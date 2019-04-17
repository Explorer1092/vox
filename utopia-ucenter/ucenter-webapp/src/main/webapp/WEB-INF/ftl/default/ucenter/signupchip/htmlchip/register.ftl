<div class="m-register PNG_24">
    <div class="m-register-core">
        <div class="r-hd">新用户注册</div>
        <div class="r-menu clearfix">
            <div class="tep-1 v-reg-type" data-type="teacher" data-title="老师">
                <a href="javascript:void(0);" class="">
                    <i></i>
                </a>
                <p class="text">我是老师</p>
            </div>
            <div class="tep-3 v-reg-type" data-type="student" data-title="学生">
                <a href="javascript:void(0);" class="">
                    <i></i>
                </a>
                <p class="text">我是学生</p>
            </div>
        </div>
    </div>
    <div class="m-register-tea  v-reg-box" data-type="teacher" data-title="老师注册" style="display: none;">
        <div class="r-item-hd">老师注册<a href="javascript:void(0)" class="v-reg-type" data-type="student" data-title="学生">切换到学生注册》</a></div>
        <div class="js-teacherSignBox">
            <div class="r-item-box">
                <ul class="r-form">
                    <li class="fist r-mobile mobileinfo PNG_24">
                        <label>请输入您的手机号</label>
                        <input autocomplete="off" type="text" value="" maxlength="11" name="mobile" class="require" data-label="手机号"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="r-code codeinfo PNG_24">
                        <label>请输入右侧数字</label>
                        <input autocomplete="off"  id="captchaCode" type="text" value="" maxlength="4" class="require" name="verificationCode" data-label="验证码"/>
                <span class="info-text">
                    <img id='captchaImage_1' style="width: 70px; height: 28px; cursor: pointer;" onclick="refreshCaptcha();"/>
                </span>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="r-code smsCodeInfo PNG_24">
                        <label>请输入短信验证码</label>
                        <input autocomplete="off" type="text" value="" maxlength="6" class="require" name="ver_code" data-label="短信验证码"/>
                        <span class="info-text"><a href="javascript:void(0);" class="code-btn js" id="getCheckCodeBtn">免费获取验证码</a></span>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                </ul>
            </div>
            <div class="tvoicecode">
                <a href="javascript:void(0);">收不到短信验证码？</a>
            </div>
        </div>

        <div class="voicecodearea">
            <h3 class="vctitle">免费获取语音验证码！</h3>
            <div class="vctext">一起教育科技将给您拨打电话，通过语音播报验证码，请注意接听来电。</div>
            <div class="voicecodebtn">
                <a href="javascript:void(0);" class="backvoicecode">返回</a>
                <a href="javascript:void(0);" class="getvoicecode">获取语音验证码</a>
            </div>
        </div>
    </div>
    <div class="m-register-stu v-reg-box" data-type="student" data-title="学生注册" style="display: none;">
        <div class="r-item-hd">学生注册<a href="javascript:void(0)" class="v-reg-type"data-type="teacher" data-title="老师">切换到老师注册》</a></div>
        <div class="r-item-box">
            <ul class="r-form">
                <li class="r-mobile">
                    <div id="regKtwelveInfo"></div>
                    <label>请输入老师手机号或者ID号</label>
                    <input type="text" value="" maxlength="11" name="clazzId" class="require" data-label="老师号码"/>
                    <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                </li>
            </ul>
        </div>
    </div>


    <div class="r-submit" style="display: none;">
        <a href="javascript:void(0);" class="v-submit-one-register" data-type="student">注册学生账号</a>
    </div>
</div>

<script type="text/html" id="T:选择班级列表">
    <div class="main-title">请选择你所在的班级 : </div>
    <div style="font-size: 20px; color: #69ce50; text-align: center; padding-top: 10px; line-height: 120%; white-space: nowrap; width: 100%; overflow: hidden; text-overflow: ellipsis;" id="showClazzName"></div>
    <div class="main-class clearfix">
        <%if(clazzList.length > 0){%>
            <%for(var i = 0; i < clazzList.length; i++){%>
                <span class="click-select-code" data-type="clazzList" data-clazzid="<%=clazzList[i].clazzId%>" data-clazzname="<%=clazzList[i].clazzName%>" title="<%=clazzList[i].clazzName%>"><%=clazzList[i].clazzName%></span>
            <%}%>
        <%}else{%>
            此老师还没有创建班级！
        <%}%>

        <div class="main-title" style="clear:both; padding:15px 0 10px;">请选择性别 : </div>
        <div>
            <span class="click-select-code" data-gender="M">男生</span>
            <span class="click-select-code" data-gender="F">女生</span>
        </div>
    </div>
</script>
<script type="text/html" id="T:学生注册完成">
    <div class="hd hd-success">注册成功！推荐绑定家长手机号</div>
    <div class="main-box student-banding-box-2">
        <div class="main-inner pr-layer-prom" style="border-radius:8px;padding:0 39px 15px;">
            <div class="text" style="padding:20px 0 0;">
                <p style="text-align: left;line-height:32px;">你的学号是：<span><%=studentId%></span></p>
                <p style="text-align: left;line-height:32px;">建议绑定家长手机，便于找回密码、避免账号丢失哦</p>
            </div>
            <ul class="main-list">
                <li>
                    <label>请输入家长手机号</label>
                    <input type="text" value="" maxlength="11" name="mobile" class="require" data-label="手机号"/>
                    <span class="info-text"><a href="javascript:void(0);" class="code-btn js-bind-now"><span>免费获取验证码</span></a></span>
                    <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    <div class="reg-arrowInfo">
                        <span class="sar">◆<span class="ir">◆</span></span>
                        <p>1.不绑定手机，你兑换的奖品将不能寄送；</p>
                        <p>2.不绑定手机，密码丢失将不能通过手机号找回！</p>
                    </div>
                </li>
                <li>
                    <label>请输入手机收到的短信验证码</label>
                    <input type="text" maxlength="4" class="require" name="ver_code" data-label="验证码"/>
                    <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                </li>
            </ul>
            <div class="submit"><a href="javascript:void(0);" class="js-band-confirm-2" data-type="student">确定</a></div>
            <p align="center" style="line-height:32px;"><a href="javascript:void(0)" class="js-student-downloadUser-popup" style="color:#189cfb">暂不绑定</a></p>
        </div>
    </div>
</script>
<script type="text/html" id="T:学生注册完成2">
    <div class="hd hd-success">恭喜，注册成功!</div>
    <div class="main-box">
        <div class="main-inner pr-layer-prom" style="border-radius:8px;padding:0 39px 15px;">
            <div class="text" style="padding:20px 0 0;">
                <p style="text-align: left;line-height:32px;">你的学号是：<span><%=studentId%></span></p>
                <p style="text-align: left;line-height:32px;">下次使用学号+密码登录即可完成作业</p>
            </div>
            <div class="submit"><a href="javascript:void(0);" class="js-student-downloadUser" data-type="student">确定</a></div>
        </div>
    </div>
</script>
<script type="text/html" id="T:下载账号信息">
    <p style="text-align:center;font-size:16px;color:#4b4b50;">推荐把账号信息保存到电脑上，避免账号丢失哦</p>
    <div align="center" style="margin-top:58px;padding-bottom:20px;"><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/student/index.vpage" style="color:#4b4b50;text-decoration:underline;margin-right:58px;">不保存</a><a class="save-2-local" href="javascript:void(0);" style="display:inline-block;width:140px;background-color:#5aca3b;border-radius:6px;padding:13px 0;border-bottom:2px solid #64ba4a;text-align:center;color:#fff;font-size:18px
">保存至电脑</a></div>
</script>
<script type="text/html" id="T:已用过一起作业提示">
    <div class="t-propClazz-box" data-type="welcome">
        <p class="title" style="text-align: center; padding: 20px 0;">欢迎加入一起教育科技，你是？</p>
        <div class="pp-btn">
            <a class="reg-btn v-clickHasBeenUser" href="javascript:void (0);">已用过一起学生端</a>
            <a class="reg-btn reg-btn-orange v-clickNewRegisterUser" href="javascript:void (0);">新用户</a>
        </div>
    </div>
    <div class="t-propClazz-box" style="display: none;" data-type="login">
        <p class="title" style="text-align: center; font-size: 18px;">原来的账号就可以换班级、换老师，或新增一个老师</p>
        <div class="pp-help-back"><!--help back--></div>
        <div class="pp-btn">
            <a class="reg-btn" href="/login.vpage?ref=signup">去登录</a>
            <a href="/ucenter/resetnavigation.vpage?ref=signup" style="color: #39f; text-decoration: underline; display: inline-block; vertical-align: middle;">忘记原来账号了？</a>
        </div>
    </div>
</script>
<script type="text/javascript">
    var areYouTeacherCount = 0; //全局变量，记录弹窗提示确认教师身份的次数。
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

    function refreshCaptcha() {
        $("#captchaCode").val("");
        $('#captchaImage_1').attr('src', "/captcha?" + $.param({
            'module': 'regCaptcha',
            'token': '${captchaToken!0}',
            't': new Date().getTime()
        }));
    }

    //老师注册
    $(function(){
        var regBoxOne = $(".v-reg-box[data-type='teacher']");

        $(document).on("click", "#getCheckCodeBtn", function(){
            var $this = $(this);
            var mobileId = regBoxOne.find("input[name='mobile']");
            var verification = $('#captchaCode').val();

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "reg-getCode",
                app : "teacher"
            });

            if($this.hasClass("btn_disable")){
                return false;
            }

            if( !$17.isMobile(mobileId.val())){
                $this.parents("li").siblings(".r-mobile").addClass("i-error").find(".i-error-info b").html("请填写正确的手机号码");
                return false;
            }

            if( $17.isBlank(verification)){
                $this.closest("li").siblings('.codeinfo').addClass("i-info-mini").find(".i-error-info b").html("验证码不可为空");
                return false;
            }

            var sendReq = function(){
                $.post("/signup/tmsignsvc.vpage", {
                    mobile: mobileId.val(),
                    count : 1,
                    cid: "${contextId!0}",
                    captchaToken : "${captchaToken!''}",
                    captchaCode : verification
                }, function(data){
                    var timerCount;
                    var timer;
                    var second = 60;

                    if(data.success){
                        timerCount = second;
                    }else{
                        timerCount = data.timer || null;
                    }

                    if(timerCount == null) {
                        var info = data.info;

                        if(info.indexOf("手机") > -1){
                            $this.closest("li").siblings('.mobileinfo').addClass("i-info-mini").find(".i-error-info b").html(info);
                        }else if(info.indexOf("验证码") > -1){
                            $this.closest("li").siblings('.codeinfo').addClass("i-info-mini").find(".i-error-info b").html(info);
                        }else{
                            $this.closest("li").siblings('.codeinfo').addClass("i-info-mini").find(".i-error-info b").html(info);
                        }
                        refreshCaptcha();
                        return false;
                    }

                    timer = $.timer(function() {
                        var msgItem = $('.getvoicecode');
                        /*短信验证码的计时*/
                        if(timerCount <= 0){
                            /*短信验证码的计时*/
                            $this.removeClass("btn_disable");
                            $this.html("重新获取短信验证码");
                            $this.closest("li").prev('li').removeClass("i-info-mini");

                            /*语音验证码的计时*/
                            msgItem.removeClass("btn_disable");
                            msgItem.html("获取语音验证码");
                            timerCount = second;
                            timer.stop();
                        } else {
                            /*短信验证码的计时*/
                            timerCount--;
                            $this.addClass("btn_disable");
                            $this.html(timerCount + "秒之后可重新发送");
                            //小于150S后出现【免费发验证码】 ，一天只能发送两次
                            if(timerCount <= 15 && $17.getCookieWithDefault("STEL") < 2){
                                $this.closest("li.smsCodeInfo").addClass("i-info-mini").find(".i-error-info b").html("<span>收不到短信验证码？</span><a class='reg_btn reg_btn_small' href='javascript:void(0)' id='serviceCallMe' style='padding:  5px 10px; color: #fff;'>点击获取语音验证码</a>") ;
                            }

                            /*语音验证码的计时*/
                            msgItem.addClass("btn_disable");
                            msgItem.html(timerCount + "秒后可重新发送");
                        }
                    });
                    timer.set({ time : 1000});
                    timer.play();
                });
            };

            //先提示是否是教师角色
            areYouTeacher(sendReq);
        });

        $(document).on("click", "#serviceCallMe", function(){
            $('.tvoicecode a').trigger('click');
        });

        //1.长时间未收到，点击【致电给我】, 2.Cookie记录每天只能发送2次【致电给我】
        if( $17.isBlank($17.getCookieWithDefault("STEL")) || $17.getCookieWithDefault("STEL") < 2){
            var serviceCallMeCount = 0;
            $("#serviceCallMe").live("click", function(){
                return false; //  原有获取人工验证码的功能不要了。  time:2015.09.23，author:lvxiaobao，pm:yujiaping
                if(serviceCallMeCount < 1){
                    serviceCallMeCount = 1;
                    $.post("/signup/feedback.vpage", {mobile : regBoxOne.find("input[name='mobile']").val()}, function(data){
                        if(data.success){
                            waitPopup();
                        }else{
                            $17.alert(data.info);
                            serviceCallMeCount = 0;
                        }
                    });
                    //Cookie设置【致电给我】发送次数，最多记录到2
                    $17.setCookieOneDay("STEL", ($17.getCookieWithDefault("STEL")*1) + serviceCallMeCount, 1);
                }else{
                    waitPopup();
                }
                $17.tongji('老师注册-致电给我 点击次数');

                function waitPopup(){
                    $.prompt("<div class='w-ag-center'>请稍候！客服很快将验证码发送到您的手机，验证码1小时内有效。</div>", {
                        title: "系统提示",
                        focus : 1,
                        buttons: { "关闭": false, "体验绘本阅读" : true },
                        position:{width : 500},
                        submit : function(e, v){
                            if(v){
                                var dataFrame = '<object width="900" height="600" data="http://cdn-cc.17zuoye.com/resources/apps/flash/Reading.swf?_=20141024172856" type="application/x-shockwave-flash"><param name="movie" value="http://cdn-cc.17zuoye.com/resources/apps/flash/Reading.swf?_=20141024172856"><param name="allowScriptAccess" value="always"><param name="allowFullScreen" value="true"><param name="flashvars" value="isPreview=0&gameDataURL=http%3A%2F%2Fwww.17zuoye.com%2Fappdata%2Fflash%2FReading%2Fobtain-ENGLISH-4445.vpage&nextHomeWork=closeReviewWindow&tts_url=http%3A%2F%2Fwww.17zuoye.com%2Ftts.vpage&isTeacher=1&imgDomain=http%3A%2F%2Fcdn-cc.17zuoye.com%2F&domain=http%3A%2F%2Fwww.17zuoye.com"><param name="wmode" value="opaque"></object>'
                                setTimeout(function(){
                                    $.prompt('<div>'+ dataFrame +'</div>', {
                                        title    : "体验绘本阅读",
                                        buttons  : {},
                                        position : { width: 940}
                                    });
                                }, 100);
                                $17.tongji("老师注册-体验绘本阅读");
                            }
                        }
                    });
                }
            });
        }
        //第二步完成-提交注册
        $(document).on('click', ".v-submit-register[data-type='teacher']", function () {
            var $this = $(this);
            var regBox = $(".js-registerTemplateType[data-type='teacher']");
            var success = validate(".js-registerTemplateType[data-type='teacher']");
            var mobileId = regBoxOne.find("input[name='mobile']");
            var checkCodeBoxId = regBoxOne.find("input[name='ver_code']");
            var realNameId = regBox.find("input[name='username']");
            var passwordId = regBox.find("input[name='password']");
            var inviteInfoId = regBox.find("input[name='invite']");
            var inviteInfoIdVal = inviteInfoId.val();

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "reg-clickOverSubmit",
                app : "teacher"
            });

            if(inviteInfoIdVal == "请填写邀请人手机号或者ID"){
                inviteInfoIdVal = "";
            }

            if( $this.hasClass("dis") ){
                return false;
            }
            if (success) {
                if(realNameId.val().length > 20) {
                    realNameId.parent().addClass("i-error");
                    realNameId.siblings(".i-error-info").find("b").text("请不要使用过长的名称。");
                    return false;
                }

                if(!$17.isBlank(inviteInfoIdVal) && !$17.isNumber(inviteInfoIdVal)){
                    inviteInfoId.parent().addClass("i-error");
                    inviteInfoId.siblings(".i-error-info").find("b").text("邀请人请填写邀请人学号，学号为数字。");
                    return false;
                }

                var $userName = realNameId.val();

                $userName = $userName.replace(/(^\s*)|(\s*$)/g,'');

                var data = {
                    role            : 'ROLE_TEACHER',
                    userType        : 1,
                    mobile          : mobileId.val(),
                    code            : checkCodeBoxId.val(),
                    realname        : $userName,
                    password        : passwordId.val(),
                    registerType    : 0,
                    inviteInfo      : inviteInfoIdVal,
                    dataKey         : ""
                };

                $17.tongji("注册2-老师-手机方式提交");
                $this.addClass("dis");
                App.postJSON('/signup/msignup.vpage', data, function (data) {
                    if (data && data.success) {
                        setTimeout(function(){
                            location.href = "/teacher/selectschool.vpage";
                        }, 500);

                        //log
                        if($17.getOperatingSystem() == 'iOS' || $17.getOperatingSystem() == 'Android'){
                            $17.voxLog({
                                module : "newTeacherRegStep",
                                op : "reg-OverPage",
                                app : "teacher",
                                source : "mobile"
                            });
                        }else{
                            $17.voxLog({
                                module : "newTeacherRegStep",
                                op : "reg-OverPage",
                                app : "teacher",
                                source : "web"
                            });
                        }
                    } else {
                        var attrs = data.attributes;
                        if(attrs){
                            $.each(attrs, function (key, value) {
                                var el = $('#' + key);
                                if (el.length > 0) {
                                    el.parent().addClass('err');
                                    el.siblings("span").html("<i></i>" + value);
                                } else {
                                    if (attrs.none) {
                                        $17.alert(attrs.none);
                                    }
                                }
                            });
                        }else{
                            $17.alert(data.info);
                        }
                        $this.removeClass("dis");
                    }
                }, function (data) {
                    $17.alert("网络请求失败，请稍等重试或者联系客服");
                    $this.removeClass("dis");
                });
            }
        });
        //点击收不到短信验证码
        $(document).on('click', '.tvoicecode a', function(){
            var mobileId = regBoxOne.find("input[name='mobile']");
            var verCode = regBoxOne.find("input[name='verificationCode']");
            if( $17.isBlank(mobileId.val()) || $17.isBlank(verCode.val())){
                if( $17.isBlank(mobileId.val()) ){
                    regBoxOne.find('.mobileinfo').addClass("i-error").find(".i-error-info b").html("手机号不可为空");
                }
                if( $17.isBlank(verCode.val()) ){
                    regBoxOne.find('.codeinfo').addClass("i-error").find(".i-error-info b").html("验证码不可为空");
                }
                return false;
            }
            var regbox = $('.m-register');
            regbox.find('.js-teacherSignBox, .r-submit').hide();
            regbox.find('.voicecodearea').show();
        });
        //语音验证码返回
        $(document).on('click', '.backvoicecode', function(){
            $('.v-reg-type[data-type="teacher"]').trigger('click');
        });
        //获取语音验证码
        $(document).on('click', '.getvoicecode', function(){
            var $this = $(this);
            var mobileId = regBoxOne.find("input[name='mobile']");
            if($this.hasClass("btn_disable")){
                return false;
            }

            var verification = $('#captchaCode').val();

            if( $17.isBlank(verification)){
                regBoxOne.find('.codeinfo').addClass("i-info-mini").find(".i-error-info b").html("验证码不可为空");
                $('.v-reg-type[data-type="teacher"]').trigger('click');
                return false;
            }

            var sendReq = function(){
                $.post("/signup/tmsignsvc.vpage", {
                    mobile: mobileId.val(),
                    count : 2,
                    cid: "${contextId!0}",
                    captchaToken : "${captchaToken!''}",
                    captchaCode : verification,
                    voice: true
                }, function(data){
                    var timerCount;
                    var timer;
                    var second = 60;

                    if(data.success){
                        timerCount = second;
                    }else{
                        timerCount = data.timer || null;
                    }
                    $('.v-reg-type[data-type="teacher"]').trigger('click');

                    if(timerCount == null) {
                        var info = data.info;

                        if(info.indexOf("手机") > -1){
                            regBoxOne.find('.mobileinfo').addClass("i-info-mini").find(".i-error-info b").html(info);
                        }else if(info.indexOf("验证码") > -1){
                            regBoxOne.find('.codeinfo').addClass("i-info-mini").find(".i-error-info b").html(info);
                        }else{
                            regBoxOne.find('.codeinfo').addClass("i-info-mini").find(".i-error-info b").html(info);
                        }
                        refreshCaptcha();
                        return false;
                    }

                    timer = $.timer(function() {
                        /*短信验证码的计时*/
                        var msgItem = $('#getCheckCodeBtn');

                        if(timerCount <= 0){
                            /*短信验证码的计时*/
                            msgItem.removeClass("btn_disable");
                            msgItem.html("重新获取短信验证码");
                            msgItem.closest("li").prev('li').removeClass("i-info-mini");

                            /*语音验证码的计时*/
                            $this.removeClass("btn_disable");
                            $this.html("获取语音验证码");
                            regBoxOne.find('li:first').removeClass("i-info-mini");
                            timerCount = second;
                            timer.stop();
                        } else {
                            /*短信验证码的计时*/
                            timerCount--;
                            msgItem.addClass("btn_disable");
                            msgItem.html(timerCount + "秒之后可重新发送");

                            /*语音验证码的计时*/
                            $this.addClass("btn_disable");
                            $this.html(timerCount + "秒后可重新发送");
                        }
                    });
                    timer.set({ time : 1000});
                    timer.play();

                });
            };

            areYouTeacher(sendReq);
        });
    });

    $("#js-gender>span").on("click",function(){

    });

    var studentForm = {
        webSource : "",     //记录是否填写班级
        classCode : "",              //设置班级编号
        classCodeName : "",          //设置班级名称
        mobileGray : "",          //设置手机号
        teacherId : "",
        gender : ""
    };
    //学生注册
    $(function(){
        /*免费获取短信验证码*/
        $(document).on("click", "#studentGetCheckCodeBtn",function(){
            var $this = $(this);
            var mobileId = $(".js-registerTemplateType[data-type='student'] input[name='mobile']");

            if($this.hasClass("btn_disable")){
                return false;
            }

            if( $17.isBlank(mobileId.val()) ){
                $this.closest("li").addClass("i-error").find(".i-error-info b").html("手机号不可为空");
                return false;
            }

            //统计学生获取验证码
            if(studentForm.mobileGray == "showMobileReg2C"){
                $17.voxLog({
                    app : "student",
                    module : "studentRegisterFirstPopup",
                    op : "regGetCodeStudent2C",
                    userId : mobileId.val()
                });
            }

            $.post("/signup/smsignsvc.vpage", {mobile : mobileId.val(), cid: "${contextId!0}"}, function(data){
                $17.getSMSVerifyCode($this, data);
                if(!data.success){
                    $this.closest("li").addClass("i-info-mini").find(".i-error-info b").html(data.info);
                }
            });
        });

        //第二步完成-提交注册
        $(document).on('click', ".v-submit-register[data-type='student']", function () {
            var $this = $(this);
            var success = validate(".js-registerTemplateType[data-type='student']");
            var regBox = $(".js-registerTemplateType[data-type='student']");

            if($this.hasClass("dis") || !success){
                return false;
            }

            $this.removeClass("dis");
            studentRegPost(studentForm.classCode);
            function studentRegPost(classCode){
                var $userName = regBox.find("input[name='username']").val();

                $userName = $.trim($userName);

                //开始注册
                if (success) {
                    var dataJson = {
                        role            : 'ROLE_STUDENT',
                        userType        : 3,
                        realname        : $userName,
                        password        : regBox.find("input[name='verify_password']").val(),
                        childRole       : 'ROLE_STUDENT',
                        clazzId         : classCode,
                        registerType    : 0,
                        mobile          : regBox.find("input[name='mobile']").val(),
                        code            : regBox.find("input[name='ver_code']").val(),
                        inviteInfo      : "",
                        dataKey         : "",
                        webSource       : studentForm.webSource,
                        invitation      : "",
                        teacherId       : studentForm.teacherId,
                        gender          : studentForm.gender
                    };

                    $17.tongji("注册2-学生-提交");

                    $this.addClass("dis");
                    App.postJSON('/signup/signup.vpage', dataJson, function (data) {
                        if (data && data.success) {
                            $17.tongji("首页-学生-注册成功");

                            if($17.getCookieWithDefault("stregscs")){
                                $(".js-registerTemplateType[data-type='student']").html( template("T:学生注册完成2", {studentId : data.row }));
                            }
                            else{
                                $(".js-registerTemplateType[data-type='student']").html( template("T:学生注册完成", {studentId : data.row }));
                            }

                            setTimeout(function(){
                                $(document).on("click", ".js-student-downloadUser", function(){
                                    downLoadAccount();
                                });
                                $(document).on("click", ".js-student-downloadUser-popup", function(){
                                    $.prompt(template("T:下载账号信息",{}),{
                                        title:"温馨提示",
                                        buttons:{},
                                        loaded:function(){
                                            $(document).on("click", ".save-2-local", function(){
                                                downLoadAccount();
                                            });
                                        }
                                    });
                                });
                                var $bandingBox = $('.student-banding-box-2');
                                var $popuBox = $('.null-popupmessage');
                                $bandingBox.hide();

                                //若data.row 尾数为17 && 不为第二次注册
                                if(studentForm.mobileGray != "showMobileReg2C"){
                                    $popuBox.find('div.submit:contains("下载账号，开始一起作业")').hide();
                                    $bandingBox.show();
                                    $bandingBox.find('.sb-step2').hide();
                                    $bandingBox.find('.js-info').hide();

                                    $bandingBox.on("click", ".js-bind-now", function(){
                                        var bindPhoneNo = $bandingBox.find('input[name="mobile"]').val();
                                        getPhoneCode(bindPhoneNo);
                                    });

                                    $bandingBox.on("click", ".js-band-confirm-2", function(){
                                        if(validate($bandingBox)){
                                            var ver_code = $bandingBox.find('input[name="ver_code"]').val();

                                            $.post("/student/nonameverifymobile.vpage", {code : ver_code}, function(data){
                                                if(data.success){
                                                    downLoadAccount();
                                                    $17.voxLog({
                                                        app : "student",
                                                        module : "studentRegisterFirstPopup",
                                                        op : "LastTwoIs17Reg-indexPage"
                                                    });
                                                }else{
                                                    $17.alert(data.info);
                                                }
                                            });
                                        }
                                    });
                                    $bandingBox.on("click", ".js-mobile-timer", function(){
                                        var phoneNo  = $bandingBox.find('.mobile-text').html();
                                        getPhoneCode(phoneNo);
                                    });
                                    $17.voxLog({
                                        app : "student",
                                        module : "studentRegisterFirstPopup",
                                        op : "LastTwoIs17Reg-success"
                                    });
                                }

                                var getPhoneCode = function(phoneNo){
                                    $.post("/student/sendmobilecode.vpage", {mobile : phoneNo}, function(data){
                                        if(data.success){
                                            $17.getSMSVerifyCode($bandingBox.find('.js-bind-now'), data);
                                            $bandingBox.find('.sb-step2').show();
                                            $bandingBox.find('.sb-step1').hide();
                                            $bandingBox.find('.js-info').show();
                                            $bandingBox.find('.mobile-text').html(phoneNo);
                                        }else{
                                            $17.alert(data.info);
                                        }
                                    });
                                    $17.voxLog({
                                        app : "student",
                                        module : "studentRegisterFirstPopup",
                                        op : "LastTwoIs17Reg-getVerCode",
                                        target: phoneNo
                                    });
                                };

                                var downLoadAccount = function(){
                                    var ifr = document.createElement("a");
                                    ifr.setAttribute("href","/ucenter/fetchaccount.vpage");
                                    ifr.setAttribute("target","_blank");
                                    ifr.style.display = 'none';
                                    document.body.appendChild(ifr);
                                    ifr.click();
                                    setTimeout(function(){
                                        location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/student/index.vpage";
                                    }, 200);
                                };
                            }, 100);

                            if(!$17.getCookieWithDefault("stregscs")){
                                $17.setCookieOneDay("stregscs", "60", 60);//设置第一次注册成功学生号
                            }

                            //统计学生注册成功
                            if(studentForm.mobileGray == "showMobileReg2C"){
                                $17.voxLog({
                                    app : "student",
                                    module : "studentRegisterFirstPopup",
                                    op : "showMobileReg2C-success"
                                });
                            }else{
                                $17.voxLog({
                                    app : "student",
                                    module : "studentRegisterFirstPopup",
                                    op : "hideMobileReg-success"
                                });
                            }
                        } else {
                            var attrs = data.attributes;
                            if(attrs){
                                $.each(attrs, function (key, value) {
                                    var el = $('#' + key);

                                    if(key == "dirty"){
                                        $17.alert("班级人数已到达上限.");
                                        $17.voxLog({
                                            app : "student",
                                            module : "studentRegisterFirstPopup",
                                            op : "fullOfPeople"
                                        }, "student");
                                    }else{
                                        if (el.length > 0) {
                                            el.parent().addClass('err');
                                            el.siblings("span").html("<i></i>" + value);
                                        } else {
                                            if (attrs.none) {
                                                $17.alert(attrs.none);
                                            }else{
                                                $17.alert(value);
                                            }
                                        }
                                    }
                                });
                            }else{
                                if(data.clazzId){
                                    $17.alert(data.clazzId);
                                }else if(data.userId){
                                    alertInfo(regBox.find("input[name='username']"), template("T:已注册学号" , {userName : dataJson.realname, userId:data.userId}) );
                                    $17.voxLog({
                                        userId : data.userId,
                                        app : "student",
                                        module : "studentRegisterFirstPopup",
                                        op : "duplicateClass"
                                    }, "student");
                                }
                                else{
                                    $17.alert(data.info);
                                }
                            }
                        }
                        $this.removeClass("dis");
                    }, function (data) {
                        $17.alert("网络请求失败，请稍等重试或者联系客服");
                        $this.removeClass("dis");
                    });
                }
            }
            return false;
        });
    });

    //共用
    $(function(){
        //注册类型切换
        var $regType = $(".v-reg-type");
        var $regSubmit = $(".v-submit-one-register");
        $regType.on("click", function(){
            var $this = $(this);
            var $dataType = $this.attr("data-type");
            var $dataTitle = $this.attr("data-title");

            $('.voicecodearea').hide();
            var regbox = $('.m-register');
            regbox.find('.js-teacherSignBox, .r-submit').show();

            if( $17.isBlank($dataType) ){
                $dataType = "teacher";
                $dataTitle = "老师";
            }

            if( $dataType == "parent"){
                $regSubmit.hide();
            }else{
                $regSubmit.show();
            }

            //语音验证码显隐控制
            if($dataType == "teacher"){
                refreshCaptcha();
            }

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "reg-clickMenu",
                app : $dataType
            });

            $this.addClass("active").siblings().removeClass("active");
            $(".v-reg-box[data-type='"+$dataType+"']").show().siblings(".v-reg-box").hide();
            $(".m-register-core").hide();
            $regSubmit.text("注册"+ $dataTitle +"账号");
            $regSubmit.attr("data-type", $dataType);
        });

        //注册类型补充跳转
        var index_template = $("#index_template");
        var register_template = $("#register_template");
        $(document).on("click", ".v-submit-one-register", function(){
            var $this = $(this);
            var $dataType = $this.attr("data-type");
            var success = validate(".v-reg-box[data-type='"+$dataType+"']");

            if($this.hasClass("dis") || !success){
                return false;
            }

            //老师注册第一步手机号验证
            if($dataType == "teacher"){
                $17.tongji('新首页注册-老师流程-注册老师账号');
                var regBoxOne = $(".v-reg-box[data-type='teacher'] ");
                var mobileId = regBoxOne.find("input[name='mobile']");
                var checkCodeBoxId = regBoxOne.find("input[name='ver_code']");

                $17.voxLog({
                    module : "newTeacherRegStep",
                    op : "reg-clickSubmitReg",
                    app : "teacher"
                });

                $this.addClass("dis");
                $.post("/signup/validatemobileonly.vpage", {
                    code : checkCodeBoxId.val(),
                    mobile : mobileId.val()
                }, function(data){
                    if(data.success){
                        $(".js-registerTemplateType[data-type='teacher']").show();
                        $(".js-registerTemplateType[data-type='student']").hide();
                        index_template.hide();
                        register_template.show();
                        $17.voxLog({
                            module : "newTeacherRegStep",
                            op : "reg-setPassword",
                            app : "teacher"
                        });
                    }else{
                        if( data.info.indexOf("手机号") > -1 ){
                            alertInfo(".v-reg-box input[name='mobile']", data.info);
                        }else{
                            alertInfo(".v-reg-box input[name='ver_code']", data.info);
                            refreshCaptcha();

                        }
                    }
                    $this.removeClass("dis");
                });
            }

            //学生注册第一步老师号验证
            if($dataType == "student"){
                $17.tongji('新首页注册-学生流程-注册学生账号');
                var regBox = $(".v-reg-box[data-type='student']");

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

                studentForm.classCode = regBox.find("input[name='clazzId']").val();
                if (studentForm.classCode != "") {
                    studentForm.classCode = $17.getClassId(studentForm.classCode);
                    studentForm.webSource = "classCode";
                }

                if (studentForm.classCode.search(/c/i) > -1) {
                    studentForm.classCode = studentForm.classCode.replace(/c/i, "");
                }

                $this.addClass("dis");
                $.post("/signup/checkclazzinfo.vpage", {id: studentForm.classCode}, function (data) {
                    studentForm.classCode = "";
                    if (data.success) {
                        hasBeenUser();

                        // by changyuan.liu 天津新体系
                        if (data.clazzList.length > 0 && data.clazzList[0].creatorType == "SYSTEM") {
                            studentForm.teacherId = regBox.find("input[name='clazzId']").val();
                        }

                        $("#allSearchClazzItem").html( template("T:选择班级列表", {clazzList: data.clazzList}) );

                        $(".js-registerTemplateType[data-type='teacher']").hide();
                        $(".js-registerTemplateType[data-type='student']").show();
                        index_template.hide();
                        register_template.show();

                        //选择编号
                        $(document).on("click", ".click-select-code", function(){
                            var $self = $(this);

                            $self.addClass("active").siblings().removeClass("active");
                            if($self.attr("data-clazzid")){
                                studentForm.classCode = $self.attr("data-clazzid");
                                studentForm.classCodeName = $self.attr("data-clazzname");
                                $(".js-select-clazz-id").val(studentForm.classCode).parent().removeClass("i-error");
                                $("#showClazzName").html(studentForm.classCodeName);
                            }
                            else if($self.attr("data-gender")){
                                studentForm.gender = $self.attr("data-gender");
                                $(".js-select-gender").val(studentForm.gender).parent().removeClass("i-error");
                            }
                        });
                        $17.tongji('新首页注册-学生流程-学生注册页面');
                    }else{
                        if(data.ktwelve == "JUNIOR_SCHOOL"){
                            $("#regKtwelveInfo").html( template("T:初中学号提示", {}) );
                        }else{
                            alertInfo(".v-reg-box input[name='clazzId']", data.info);
                        }
                    }
                    $this.removeClass("dis");
                });
            }
        });

        //验证input
        $(document).on("focus blur change", "input", function(e){
            var _this = $(this);
            var notice = "";
            var row = _this.parent();
            var _type = _this.attr("name");
            var span = _this.siblings(".i-error-info").find("b");
            var condition = true;
            var errorMessage = "";
            var password = $("input[name='password']:visible");
            var verify_password = $("input[name='verify_password']:visible");
            if(e.type!="blur"){
                switch (_type)
                {
                    case "username":
                        condition = $17.isValidCnName( _this.val());
//                        condition = $17.isCnString( _this.val());
                        errorMessage = "请输入您的真实姓名,须为中文";

                        if(_this.val().length > 6){
                            errorMessage = "请输入1-6位以内的中文名字";
                            condition = false;
                        }

                        if(_this.data("role") == "teacher"){
                            notice = "请输入真实姓名，以便学生找到您";
                        }else{
                            notice = "请输入真实姓名";
                        }
                        break;
                    case "password":
                        if(_this.val().length > 16){
                            errorMessage = "密码不可超过16位";
                            condition = false;
                        }else{
                            if( verify_password.val() != ""){
                                if(_this.val() == verify_password.val()){
                                    verify_password.parent().removeClass("i-error");
                                }else{
                                    verify_password.parent().addClass("i-error");
                                    verify_password.siblings('.i-error-info').find("b").html("密码填写不一致，请重新填写");
                                }
                            }
                        }
                        notice = "请输入1—16位任意字符（字母区分大小写）";
                        break;
                    case "verify_password":
                        condition = (password.val() == _this.val());
                        if( password.val() != "" && _this.val().length < 17){
                            if(condition == true){
                                verify_password.parent().removeClass("i-error");
                            }
                        }

                        errorMessage = "密码填写不一致，请重新填写";
                        notice = "请再次输入密码";
                        break;
                    case "mobile":
                        condition = $17.isMobile(_this.val());
                        errorMessage = "请填写正确的手机号码";
                        notice = "请输入手机号，验证通过后可用于登录、找回密码";
                        break;
                    case "email":
                        condition = $17.isEmail(_this.val());
                        errorMessage = "请填写正确格式的邮箱";
                        notice = "请输入常用邮箱，验证通过后可用于登录和找回密码";
                        break;
                    case "clazzId":
                        var clazzId = _this.getClassId();
                        condition = ((clazzId.length >= 5 && _this.val().toUpperCase().indexOf("C") == 0) || (clazzId.length >= 5 && $17.isNumber(clazzId)));
                        errorMessage = "老师号码无效";
                        notice = "请向您的任课老师询问班级编号";
                        break;
                    case "invite_info":
                        if(_this.data("role") == "student"){
                            notice = "请输入邀请人的一起账号";
                        }else{
                            notice = "请输入邀请人的一起账号或手机号";
                        }
                        break;
                    case "ver_code":
                        condition = $17.isNumber(_this.val());
                        errorMessage = "请输入正确验证码";
                        notice = "";
                        break;
                    default:
                        break;
                }
            }
            if(e.type == "focus"){
                if(!row.hasClass("i-error") && _this.val()==""){span.html(notice)}
            }else if(e.type == "blur"){
                if(!row.hasClass("i-error") && _this.val()==""){
                    span.html("<i></i>");
                }
            }else if(e.type == "change"){
                if(!$17.isBlank(_this.val())){
                    if(!condition){
                        row.addClass("i-error");
                    }else{
                        row.removeClass("i-error");
                        errorMessage = "";
                    }
                    span.html("<i></i>"+errorMessage);
                    row.removeClass("i-info-mini");
                }else{
                    if(_this.hasClass("require")){
                        errorMessage = _this.data("label") + '不可为空';
                        row.addClass("i-error");
                        span.html(errorMessage);
                    }else{
                        row.removeClass("i-error");
                        span.html("");
                    }
                    row.removeClass("i-info-mini");
                }
            }
        });
    });

    function validate(typeBox){
        $(typeBox).find(".require").each(function(){
            if($17.isBlank($(this).val())){
                $(this).parent().addClass('i-error');
                var errorMessage = "";
                if($(this).data().label == "性别"){
                    errorMessage = "未选择性别哦";
                }else{
                    errorMessage = $(this).data("label") + '不可为空';
                }
                $(this).siblings('.i-error-info').find("b").html(errorMessage);
            }else{
                if($(this).parent().hasClass("i-info-mini")){
                    $(this).parent().removeClass('i-info-mini');
                }
            }
        });

        return ($(typeBox).find(".i-error").size() < 1) ? true : false;
    }

    function alertInfo(id, val){
        $(id).parent().addClass('i-error');
        $(id).siblings('.i-error-info').find("b").html(val);
    }
</script>
<script type="text/html" id="T:已注册学号">
    <div class="reg-arrowInfo" style="right: -264px; top:-5px;">
        <span class="sar">◆<span class="ir">◆</span></span>
        <p><%=userName%> 已注册学号：<%=userId%>，请直接登录！</p>
        <p style="text-align: center; margin: 10px; 0"><a href="/login.vpage?userid=<%=userId%>" class="reg-btn">去登录</a></p>
        <p style="color: #9a9a9a; border-top: 1px solid #e5e5e5; padding: 6px 0 0;">不是你的学号？ <a href="javascript:void(0);" onclick="window.open('${ProductConfig.getMainSiteBaseUrl()}/redirector/onlinecs_new.vpage?type=student&question_type=question_account_ps&origin=PC-注册','','width=856,height=519')">联系客服</a></p>
    </div>
</script>