<div class="js-scrollMainIdBox" id="register_template" style="display: none; min-height: 100%">
    <div class="zy-header">
        <div class="zy-nav">
            <a href="/" class="logo"></a>
        </div>
        <div class="rightIn">
            <a href="javascript:void(0)" class="inBtn referBtn active" onclick="window.open('${ProductConfig.getMainSiteBaseUrl()}/redirector/onlinecs_new.vpage?type=student&question_type=question_account_ps&origin=PC-注册','','width=856,height=519');" data-tongji="学生-在线咨询">在线咨询</a>
            <span class="referTime">咨询时间<br>8:00-21:00</span>
        </div>
    </div>
    <#--学生-->
    <div class="pr-main pr-main-child js-registerTemplateType" data-type="student">
        <div class="hd">请设置你的密码！</div>
        <div class="main-box">
            <div class="main-inner">
                <div id="allSearchClazzItem"></div>
                <ul class="main-list">
                    <li style="background: none;border: none;padding: 0;margin: 0;position: relative;top: -90px;">
                        <input type="text" value="" maxlength="4" name="clazzId" data-label="班级" class="require js-select-clazz-id" style="display: none;"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li style="background: none;border: none;padding: 0;margin: 0;position: relative;top: -20px;">
                        <input type="text" value="" maxlength="4" name="gender" data-label="性别" class="require js-select-gender" style="display: none;"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                </ul>
                <ul class="main-list">
                    <li class="r-username ">
                        <label>请输入真实姓名</label>
                        <input type="text" value="" maxlength="20" name="username" data-label="真实姓名" class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                        <div class="reg-arrowInfo">
                            <#--<span class="sar">◆<span class="ir">◆</span></span>-->
                            <p>1.注册后账号将发送给老师；</p>
                            <p>2.姓名填写后不能修改。</p>
                        </div>
                    </li>
                    <#--极算用户展示填涂号-->
                    <#if isShensz!false>
                    <li class="r-scannumber">
                        <label>请输入填涂号（选填）</label>
                        <input type="text" value="" maxlength="20" name="scannumber" data-label="填涂号">
                        <#--<span class="i-error-info"><b></b><span class="ar">◆</span></span>-->
                    </li>
                    </#if>
                    <li class="r-password ">
                        <label>请设置密码</label>
                        <input type="password" value="" maxlength="16" name="password" data-label="密码"  class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li>
                        <label>请再次输入设置的密码</label>
                        <input type="password" value="" maxlength="16" name="verify_password" data-label="确认密码"  class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="" style="display: none;">
                        <label>请输入家长手机号</label>
                        <input type="text" value="" maxlength="11" name="mobile" class="" data-label="手机号"/>
                        <span class="info-text"><a href="javascript:void(0);" class="code-btn" id="studentGetCheckCodeBtn"><span>免费获取验证码</span></a></span>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                        <div class="reg-arrowInfo">
                            <#--<span class="sar">◆<span class="ir">◆</span></span>-->
                            <p>1.不绑定手机，你兑换的奖品将不能寄送；</p>
                            <p>2.不绑定手机，密码丢失将不能通过手机号找回！</p>
                        </div>
                    </li>
                    <li style="display: none;">
                        <label>请输入手机收到的短信验证码</label>
                        <input type="text" value=""maxlength="6" class="" name="ver_code" data-label="验证码"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                </ul>
                <div class="submit"><a href="javascript:void(0);" class="v-submit-register" data-type="student">完成注册</a></div>
            </div>
        </div>
    </div>
    <#--老师-->
    <div class="pr-main pr-main-teacher js-registerTemplateType" style="display: none;" data-type="teacher">
        <div class="hd">请设置你的密码！</div>
        <div class="main-box">
            <div class="main-inner">
                <ul class="main-list">
                    <li class="r-username ">
                        <label>请输入真实姓名</label>
                        <input type="text" value="" maxlength="20" class="require" name="username" data-role="teacher" data-label="真实姓名"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="r-password ">
                        <label>请设置密码</label>
                        <input type="password" value="" maxlength="16" name="password" class="require" data-label="密码"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="last ">
                        <label>请再次输入设置的密码</label>
                        <input type="password" value="" maxlength="16" name="verify_password" data-label="确认密码"  class="require"/>
                        <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                    </li>
                    <li class="last r-invite null-bor ">
                        <label>请填写邀请人手机或ID（可不填）</label>
                        <input name="invite" type="text" value="" id="invite_info">
                        <#--<span class="info-text">（可不填）</span>-->
                    </li>
                </ul>
                <div class="submit"><a href="javascript:void(0);" class="v-submit-register" data-type="teacher">下一步</a></div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    //老师注册
    $(function(){
        //第二步完成-提交注册
        $(document).on('click', ".v-submit-register[data-type='teacher']", function () {
            var $this = $(this);
            var regBox = $(".js-registerTemplateType[data-type='teacher']");
            var success = validate(".js-registerTemplateType[data-type='teacher']");
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
                    mobile          : teacherForm.mobile,
                    code            : teacherForm.code,
                    realname        : $userName,
                    password        : passwordId.val(),
                    registerType    : 0,
                    inviteInfo      : inviteInfoIdVal,
                    dataKey         : "",
                    webSource       : teacherForm.webSource
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
    });

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
                        gender          : studentForm.gender,
                        scanNumber      : regBox.find("input[name='scannumber']").val()
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
                                        buttons:{
                                            "不保存" : false,
                                            "保存至电脑" : true
                                        },
                                        loaded:function(){
                                            $(document).on("click", ".save-2-local", function(){
                                                downLoadAccount();
                                            });
                                        },
                                        submit: function(e, v){
                                            if(v){
                                                downLoadAccount();
                                            }else{
                                                location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/student/index.vpage";
                                            }
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

                                function downLoadAccount(){
                                    var ifr = document.createElement("a");
                                    ifr.setAttribute("href","/ucenter/fetchaccount.vpage");
                                    ifr.setAttribute("target","_blank");
                                    ifr.style.display = 'none';
                                    document.body.appendChild(ifr);
                                    ifr.click();
                                    setTimeout(function(){
                                        location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/student/index.vpage";
                                    }, 200);
                                }
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
                            notice = "请输入邀请人的一起作业号";
                        }else{
                            notice = "请输入邀请人的一起作业号或手机号";
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
                        span.html("<i></i>"+errorMessage);
                        row.removeClass("i-info-mini");
                    }else{
                        row.removeClass("i-error");
                        errorMessage = "";
                        span.html("<i></i>"+errorMessage);
                        row.removeClass("i-info-mini");
                        //验证是否是敏感词汇
                        if(_type=="username"){
                            var userName = _this.val();
                            $.post("/signup/filtersensitiveusername.vpage",{userName:userName},function (data) {
                                if(data.success){
                                }else{
                                    row.addClass("i-error");
                                    errorMessage="输入的姓名信息不合适哦，请重新输入<br/>如有疑问，可<a href='http://www.17zuoye.com/redirector/onlinecs_new.vpage?type=teacher&question_type=question_account_ps&origin=PC-注册' target='_blank' style='color:white;text-decoration:underline'>点击联系客服</a>";
                                    span.html("<i></i>"+errorMessage);
                                    row.removeClass("i-info-mini");
                                }
                            });
                        }
                    }
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

        $(document).on('click', '.JS-gotoLoginPage', function(){
            $("#register_template").hide();
            $.prompt.close();

            $(".JS-indexPageBox").show();
            $(".JS-login-main").trigger('click');
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

<script type="text/html" id="T:选择班级列表">
    <div class="main-title">请选择你所在的班级 : </div>
    <div style="font-size: 20px; color: #ff7b0a; text-align: center; padding-top: 10px; line-height: 120%; white-space: nowrap; width: 100%; overflow: hidden; text-overflow: ellipsis; height: 38px;" id="showClazzName"></div>
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
    <div class="hd hd-success" style="font-size: 22px;">注册成功！推荐绑定家长手机号</div>
    <div class="main-box student-banding-box-2">
        <div class="main-inner pr-layer-prom">
            <div class="text" style="padding:20px 0 0; font-size: 14px;">
                <p style="text-align: left;line-height:150%;">你的学号是：<span><%=studentId%></span></p>
                <p style="text-align: left;line-height:150%;">建议绑定家长手机，便于找回密码、避免账号丢失哦</p>
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
                    <input type="text" maxlength="6" class="require" name="ver_code" data-label="验证码"/>
                    <span class="i-error-info"><b></b><span class="ar">◆</span></span>
                </li>
            </ul>
            <div class="submit"><a href="javascript:void(0);" class="js-band-confirm-2" data-type="student">确定</a></div>
            <p align="center" style="line-height:32px;"><a href="javascript:void(0)" class="js-student-downloadUser-popup" style="color:#999">暂不绑定</a></p>
        </div>
    </div>
</script>

<script type="text/html" id="T:学生注册完成2">
    <div class="hd hd-success">恭喜，注册成功!</div>
    <div class="main-box">
        <div class="main-inner pr-layer-prom" style="border-radius:8px;padding:0 0 15px;">
            <div class="text" style="padding:20px 0 0; text-align: center;">
                <p style="line-height:32px;">你的学号是：<span><%=studentId%></span></p>
                <p style="line-height:32px;">下次使用学号+密码登录即可完成作业</p>
            </div>
            <div class="submit"><a href="javascript:void(0);" class="js-student-downloadUser" data-type="student">确定</a></div>
        </div>
    </div>
</script>

<script type="text/html" id="T:下载账号信息">
    <p style="text-align:center;font-size:16px;color:#4b4b50;">推荐把账号信息保存到电脑上，避免账号丢失哦</p>
</script>

<script type="text/html" id="T:已用过一起作业提示">
    <div class="t-propClazz-box" data-type="welcome">
        <p class="title" style="text-align: center; padding: 20px 0;">欢迎加入一起作业，你是？</p>
        <div class="pp-btn">
            <a class="reg-btn reg-btn-green v-clickHasBeenUser" href="javascript:void (0);">已用过一起作业</a>
            <a class="reg-btn v-clickNewRegisterUser" href="javascript:void (0);">新用户</a>
        </div>
    </div>
    <div class="t-propClazz-box" style="display: none;" data-type="login">
        <p class="title" style="text-align: center; font-size: 18px;">原来的账号就可以换班级、换老师，或新增一个老师</p>
        <div class="pp-help-back"><!--help back--></div>
        <div class="pp-btn">
            <a class="reg-btn JS-gotoLoginPage" href="javascript:;">去登录</a>
            <a href="/ucenter/resetnavigation.vpage?ref=signup" style="color: #39f; text-decoration: underline; display: inline-block; vertical-align: middle;">忘记原来账号了？</a>
        </div>
    </div>

</script>

<script type="text/html" id="T:已注册学号">
    <div class="reg-arrowInfo" style="background: #fff; z-index: 4; right: -296px; top: -3px;">
        <span class="sar"></span>
        <p class="txtRed"><%=userName%> 已注册学号，<%=userId%>，请直接登录! <a href="/login.vpage?userid=<%=userId%>" class="link-btn01">去登录</a></p>
        <p class="txtGrey">不是你的学号？<a href="javascript:void(0)" class="link" onclick="window.open('${ProductConfig.getMainSiteBaseUrl()}/redirector/onlinecs_new.vpage?type=student&question_type=question_account_ps&origin=PC-注册','','width=856,height=519')">联系客服</a></p>
    </div>
</script>