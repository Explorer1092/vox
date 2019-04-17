<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page navBar="hidden" footer="none">
<@sugar.capsule css=['school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
<div>
    <div class="head" style="color:#76797e;text-align: center">
        登录
    </div>
    <div class="login-wrapper">
        <form id="loginForm" method="post" action="/mobile/login.vpage">
            <input type="hidden" id="client" name="client" value="h5">
            <div class="input-control username">
                <i class="user-icon"></i>
                <input placeholder="请输入用户名" id="username" autocomplete="off" name="username" type="text" value="${userName!}"/>
            </div>
            <div class="input-control password">
                <i class="user-icon password-icon"></i>
                <input placeholder="请输入密码" id="password" name="password" type="password" autocomplete="off"/>
            </div>
            <div class="input-control auth-code">
                <input type="hidden" value="${captchaToken!0}" name="captchaToken" autocomplete="off"/>
                <span class="inner-right" id="captchaClick"> <img src="" alt="" id="captchaImage"> </span>
                <i class="user-icon code-icon"></i>
                <input placeholder="验证码" id="captchaCode" autocomplete="off" name="captchaCode" type="tel"/>
            </div>
        </form>
        <div class="login js-submit" id="loginFormBtn">登录</div>
        <div class="phone-login"><a href="/mobile/login_by_mobile.vpage">手机登录</a></div>
    </div>
    <div id="appVersion" style="margin-top: 4rem;text-align: center;color: #76797e;font-size: .75rem;"></div>
</div>
<script type="text/javascript">
    var AT = new agentTool();


    $(document).ready(function () {
            var setTopBar = {
                show:false
            };
            setTopBarFn(setTopBar);

        try {
            var code = location.href.split("code=")[1];
            if (code == 1) {
                AT.alert("验证码失效或错误，请重新输入");
            } else if (code == 2) {
                AT.alert("用户名错误，请重新输入");
            } else if (code == 3) {
                AT.alert("暂不支持协作账号使用本应用，请申请专员账号");
            } else if (code == 4) {
                AT.alert("密码错误，请重新输入");
            }
        } catch (e) {
        }

        $("#captchaClick").click(function () {
            refreshCaptcha();
        });
        $("#loginFormBtn").click(function () {
            validate();
        });

        // 验证码
        refreshCaptcha();

        var version = getQuery("app_version");
        if(version){
            $("#appVersion").html("版本号:"+version);
        }

    });

    function refreshCaptcha() {
        $('#captchaImage').attr('src', "/captcha.vpage?" + $.param({
                    'module': 'findAccount',
                    'token': "${captchaToken!""}",
                    't': new Date().getTime()
                }));
    }

    function validate() {
        var username = $("#username").val();
        if (!username) {
            AT.alert("用户名为空<br>请输入用户名");
            $("#username").focus();
            return false;
        }
        var password = $("#password").val();
        if (!password) {
            AT.alert("密码为空<br>请输入密码");
            $("#password").focus();
            return false;
        }
        var captchaCode = $("#captchaCode").val();
        if (!captchaCode) {
            AT.alert("验证码为空<br>请输入验证码");
            $("#captchaCode").focus();
            return false;
        }

        $("#loginForm").submit();
    }
</script>
</@layout.page>
