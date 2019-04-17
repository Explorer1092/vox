<#import "module.ftl" as layout>
<@layout.page>
<div class="zy-header">
    <div class="zy-nav">
        <a href="/" class="logo"></a>
    </div>
    <div class="rightIn">
        <a href="javascript:void(0)" class="inBtn referBtn active" onclick="window.open('http://www.test.17zuoye.net/redirector/onlinecs.vpage?type=teacher&amp;question_type=question_account','','width=856,height=519');" data-tongji="学生-在线咨询">在线咨询</a>
        <span class="referTime">咨询时间<br>8:00-21:00</span>
    </div>
</div>
<div class="cjl-main">
    <div class="cjl-inner" style="display:None;" data-bind="visible: mobileVerify">
        <h1>验证手机号</h1>
        <div class="tabLeft">
            <div class="image"></div>
            <div class="side ">一起作业账号绑定</div>
        </div>
        <div class="cjl-content">
            <div class="c-text">
                <span class="login-icon icon-1"></span>
                <input type="text" class="txt1 JS-teacherMobile JS-inputEvent" maxlength="11" data-bind="attr:{placeholder: mobileNum}">
                <div class="errorTips" data-bind="text: errorMobile"></div>
            </div>
            <div class="c-text">
                <span class="login-icon icon-2"></span>
                <input type="text" placeholder="请输入右侧数字" class="txt2 JS-inputEvent JS-teacherCaptcha">
                <img data-bind="click: refreshCaptcha" id="captchaImageLogin" class="code" />
                <div class="errorTips" data-bind="text: errorCaptcha"></div>
            </div>
            <div class="c-text">
                <span class="login-icon icon-3"></span>
                <input type="text" placeholder="请输入短信验证码" class="txt2 JS-inputEvent JS-teacherSmsCode">
                <span class="getCheckCode login-blue JS-getCheckCode" data-bind="click: getCheckCode.bind($data)">获取验证码</span>
                <div class="errorTips" data-bind="text: errorCode"></div>
            </div>
            <div class="c-text c-text2">
                <a href="javascript:void(0);" class="info-btn" data-bind="click:isNotReceiveBtn">收不到短信验证码？</a>
            </div>
            <div class="hei-40"></div>
            <a href="javascript:void(0);" class="login-btn login-blue" data-bind="click: nextTip.bind($data)">下一步</a>
        </div>
    </div>

    <div class="cjl-inner" style="display:none;" data-bind="visible: makeSureBind">
        <h1>确认绑定</h1>
        <div class="tabLeft">
            <div class="image"></div>
            <div class="side ">一起作业账号绑定</div>
        </div>
        <div class="cjl-content">
            <p class="cjl-tip">请确认如下账号信息无误后，点击“绑定并登录”按钮<br/>如有问题，请点击右上角咨询客服处理</p>
            <ul class="cjl-info">
                <li>姓名： <!--ko text:teacherName--><!--/ko--></li>
                <li>学科： <!--ko text:teacherSubject--><!--/ko--></li>
                <li>学校： <!--ko text:schoolName--><!--/ko--></li>
                <li>手机： <!--ko text:teacherMobile--><!--/ko--></li>
            </ul>
            <div class="hei-40"></div>
            <a href="javascript:void(0);" class="login-btn login-blue" data-bind="click: bindAndLogin.bind($data)">绑定并登录</a>
        </div>
    </div>

    <div class="cjl-inner" style="display:none;" data-bind="visible: bindMobile">
        <h1>绑定账号</h1>
        <div class="cjl-content cjl-content2">
            <ul class="cjl-info cjl-info2">
                <li>姓名： <!--ko text:teacherName--><!--/ko--></li>
                <li>学科： <!--ko text:teacherSubject--><!--/ko--></li>
                <li>学校： <!--ko text:schoolName--><!--/ko--></li>
                <li>手机： <!--ko text:teacherMobile--><!--/ko--></li>
            </ul>
            <div class="c-text">
                <input type="text" placeholder="请设置密码" maxlength="16" class="txt1 JS-inputEvent JS-password">
                <div class="errorTips" data-bind="text: errorPassword"></div>
            </div>
            <div class="password-tip">此密码用于通过17zuoye.com登录该账号</div>
            <div class="c-text">
                <input type="password" placeholder="请再次输入设置的密码" maxlength="16" class="txt1 JS-inputEvent JS-passwordAgain">
                <div class="errorTips" data-bind="text: errorPasswordAgain"></div>
            </div>
            <a href="javascript:void(0);" class="login-btn login-blue login-btn-top" data-bind="click: bindAndLogin.bind($data)">绑定并登录</a>
        </div>
    </div>
</div>
<script type="text/html" id="T:一起作业绑定手机帮助">
    <div class="main-box student-banding-box-2" style="padding: 0 10px 10px 10px; text-align: left;line-height: 24px;">
        <p style="margin-bottom: 10px">（1）验证码收不到：验证码是以短信的形式发送到您的手机上的，如果没有收到验证码建议查看是否开启了短信拦截，或者建议您隔一段时间后重新点击获取，如果还是无法收到，可以拨打400-160-1717进行咨询。</p>
        <p style="margin-bottom: 10px">（2）验证码获取上限：由于您今天获取验证码次数已达到上限，建议您换个手机号重新获取验证码、或者明天再进行获取验证码操作。</p>
    </div>
</script>
<script>
    var sourceUid = '${sourceUid!''}';
    var dataKey = '${dataKey!''}';
</script>
</@layout.page>