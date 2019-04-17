<script type="text/javascript">
    var contextId = '${contextId!0}';
    var captchaToken = '${captchaToken!0}';
    var fromShensz = '${(isShensz!false)?string}';
    var studentForm = {
        webSource : "classCode",
        classCode : "",              //设置班级编号
        classCodeName : "",          //设置班级名称
        mobileGray : "",          //设置手机号
        teacherId : "",
        gender : ""
    };

    var teacherForm = {
        webSource: "",
        mobile : 0,
        code : 0
    };
    // 来源为神算子，修改webSource（register_next.ftl注册接口时传给后端）
    if (fromShensz === 'true') {
        studentForm.webSource = 'Shensz';
        teacherForm.webSource = 'Shensz';
    }
</script>
<script type="text/html" id="T:RegisterSelectMain">
    <#--选择身份-->
    <div class="loginPop-box loginBg">
        <div class="loginPop-close JS-clear-btn"></div>
        <div class="lop-inner">
            <div class="loginPop-logo"></div>
            <h1>请选择你的身份</h1>
            <div class="lop-content">
                <div class="clearfix"></div>
                <div class="lop-tab">
                    <ul>
                        <li class="JS-selectStudent-main yes">
                            <div class="image"></div>
                            <div class="side">我是学生</div>
                        </li>
                        <li class="JS-selectTeacher-main yes">
                            <div class="image image-tea"></div>
                            <div class="side">我是老师</div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:RegisterStudentMain">
    <#--学生注册-->
    <div class="loginPop-box loginBg">
        <div class="loginPop-close JS-clear-btn"></div>
        <div class="lop-inner">
            <div class="loginPop-logo"></div>
            <h1>注册学生账号</h1>
            <div class="lop-content contentMar">
                <div class="lop-right">
                    <div class="c-text">
                        <span class="login-icon icon-1"></span>
                        <input type="text" placeholder="请输入老师手机号或ID号" class="txt1 JS-inputEvent JS-classInput" maxlength="11"/>
                        <div class="errorTips">老师账号错误</div>
                    </div>
                    <div class="" style="padding-left: 100px;">
                        <a href="javascript:void(0);" class="login-btn login-orange JS-getClassLIst">注册学生账号</a>
                        <a href="javascript:void(0);" class="info-btn JS-registerQuestion-stu" style="font-size: 14px;">注册遇到问题？</a>
                    </div>
                    <div class="agreement_main">
                        <i class="active JS-agreement"></i>
                        <span>注册即代表同意<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=0" target="_blank">《一起教育科技学生端用户服务协议》</a></span>
                    </div>
                </div>
                <div class="lop-tab tabLeft">
                    <ul>
                        <li class="JS-selectStudent-main stu active">
                            <div class="image"></div>
                            <div class="side">我是学生</div>
                        </li>
                        <li class="JS-selectTeacher-main">
                            <div class="image image-tea"></div>
                            <div class="side">我是老师</div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:RegisterTeacherMain">
    <#--老师注册-->
    <div class="loginPop-box loginBg">
        <div class="loginPop-close JS-clear-btn"></div>
        <div class="lop-inner">
            <div class="loginPop-logo"></div>
            <h1>注册老师账号</h1>
            <div class="lop-content">
                <div class="clearfix"></div>
                <div class="JS-defaultMode">
                    <div class="c-text">
                        <span class="login-icon icon-1"></span>
                        <input type="text" placeholder="请输入您的手机号" class="txt1 JS-teacherMobile JS-inputEvent" maxlength="11" />
                        <div class="errorTips"></div>
                    </div>
                    <div class="c-text">
                        <span class="login-icon icon-2"></span>
                        <input type="text" placeholder="请输入右侧数字" class="txt2 JS-teacherCaptcha JS-inputEvent" maxlength="4" id="captchaInputLogin" />
                        <img onclick="refreshCaptcha();" id='captchaImageLogin' class="code" style="padding: 0; height: 38px; cursor: pointer;"/>
                        <div class="errorTips"></div>
                    </div>
                    <div class="c-text">
                        <span class="login-icon icon-3"></span>
                        <input type="text" placeholder="请输入短信验证码" class="txt2 JS-teacherSmsCode JS-inputEvent" maxlength="6" />
                        <span class="code codeBg login-blue JS-getCheckCode" style="cursor: pointer;">获取验证码</span>
                        <div class="errorTips"></div>
                    </div>
                    <div class="c-text-left"><a href="javascript:void(0);" class="info-btn JS-receiveNotCode" style="font-size: 14px;">获取语音验证码</a> | <a href="javascript:void(0);" class="info-btn JS-registerQuestion-tea" style="font-size: 14px;">短信验证码遇到问题？</a></div>
                    <a href="javascript:void(0);" class="login-btn login-blue JS-teacherVerMobile">注册老师账号</a>
                    <div class="agreement_main">
                        <i class="active teacher_icon JST-agreement"></i>
                        <span>注册即代表同意<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/serviceagreement.vpage?agreement=1" target="_blank">《一起教育科技老师端用户服务协议》</a></span>
                    </div>
                </div>
                <div class="JS-voiceMode" style="display: none;">
                    <div class="lop-right">
                        <div class="sub">免费获取语音验证码</div>
                        <div class="con">一起作业将给你拨打电话，通过语音播报验证码，请注意接听来电。</div>
                    </div>
                    <a href="javascript:void(0);" class="login-btn return-btn login-small-btn JS-receiveNotCode" data-type="black">返回</a>
                    <a href="javascript:void(0);" class="login-btn login-blue login-small-btn JS-getCheckCode" data-type="voice">获取验证码</a>
                </div>
                <div class="lop-tab tabLeft">
                    <ul>
                        <li class="JS-selectStudent-main">
                            <div class="image"></div>
                            <div class="side">我是学生</div>
                        </li>
                        <li class="JS-selectTeacher-main tea active">
                            <div class="image image-tea"></div>
                            <div class="side">我是老师</div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="T:学生注册帮助">
    <div class="main-box student-banding-box-2" style="padding: 0 10px 10px 10px; text-align: left;line-height: 24px;">
        <p style="margin-bottom: 10px">（1）学生注册：学生注册账号需要使用老师告知的号码，登录一起作业网站或者手机APP点击注册新账号，按照提示进行注册哦！如果老师未注册一起作业学生是无法使用的。</p>
        <p style="margin-bottom: 10px">（2）老师号码：老师号码是您自己的老师在注册一起作业账号后，系统生成的老师账号或者绑定账号的手机号码。</p>
        <p style="margin-bottom: 10px">（3）怎样获取老师号码：需要咨询您的老师是否注册，然后使用老师告知的号码进行注册账号哦。</p>
    </div>
</script>
<script type="text/html" id="T:老师注册帮助">
    <div class="main-box student-banding-box-2" style="padding: 0 10px 10px 10px; text-align: left;line-height: 24px;">
        <p style="margin-bottom: 10px">（1）验证码收不到：验证码是以短信的形式发送到您的手机上的，如果没有收到验证码建议查看是否开启了短信拦截，或者建议您隔一段时间后重新点击获取，如果还是无法收到，可以拨打400-160-1717进行咨询。</p>
        <p style="margin-bottom: 10px">（2）验证码获取上限：由于您今天获取验证码次数已达到上限，建议您换个手机号重新获取验证码、或者明天再进行获取验证码操作。</p>
    </div>
</script>
<script>
    $(document).on("click", ".JS-registerQuestion-stu", function(){
        $.prompt(template("T:学生注册帮助",{}),{
            title:"学生注册帮助",
            buttons:{},
        });
    });

    $(document).on("click", ".JS-registerQuestion-tea", function(){
        $.prompt(template("T:老师注册帮助",{}),{
            title:"老师注册帮助",
            buttons:{},
        });
    });
</script>