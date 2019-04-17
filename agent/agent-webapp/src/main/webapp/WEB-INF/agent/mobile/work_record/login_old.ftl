<#import "../layout_new.ftl" as layout>
<@layout.page group="login">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerText">登录</div>
        </div>
    </div>
</div>
    <#include '../../widget_alert_messages.ftl'/>
<form action="/auth/login.vpage" method="post" id="loginForm">
    <input type="hidden" id="client" name="client" value="h5">

    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <ul class="mobileCRM-V2-list">
            <li class="user-name">
                <input type="text" style="width: 100%;padding: 0;" id="username" maxlength="30" name="username" placeholder="请输入用户名">
            </li>
            <li class="user-pass">
                <input type="password" id="password" name="password" style="padding: 0;" placeholder="请输入密码" maxlength="30">
            </li>
            <li  class="user-pass">
                <input type="hidden" value="${captchaToken!0}" name="captchaToken"/>
                <input type="tel" value="" id="captchaCode" name="captchaCode" style="width: 120px; text-align: left" maxlength="4" placeholder="请输入验证码"/>
                <span style="display: inline-block; vertical-align: middle;" id="captchaClick"><img id="captchaImage"/></span>
            </li>
        </ul>
    </div>
    <div class="mobileCRM-V2-submit">
        <input type="submit" value="登录"  style="border-radius:4px;font-size:1rem;border:0px;">
    </div>
    <div class="mobileCRM-V2-noteBar">
        <a href="/mobile/login_by_mobile.vpage"  style="font-size: 0.8rem; float: right; color:#39AC6A;" >手机号登录</a>
    </div>
</form>
<script type="text/javascript">
    $(function () {
        $("#captchaClick").click(function () {
            refreshCaptcha();
        });
        $("#loginForm").submit(function () {
            return validate();
        });
        // 验证码
        refreshCaptcha();
    });

    $(document).ready(function () {
        try {
            var code = location.href.split("code=")[1];
            if (code == 1) {
                alert("验证码校验失败");
            } else if (code == 2) {
                alert("用户名或密码有误!");
            } else if (code == 3) {
                alert("暂不支持协作账号使用本应用，请申请专员账号!");
            }
        } catch (e) {
        }
    });

    function refreshCaptcha() {
        $('#captchaImage').attr('src', "/captcha.vpage?" + $.param({
            'module': 'findAccount',
            'token': '${captchaToken!0}',
            't': new Date().getTime()
        }));
    }

    function validate() {
        var username = $("#username").val();
        if (!username) {
            alert("用户名为空，请输入用户名");
            $("#username").focus();
            return false;
        }
        var password = $("#password").val();
        if (!password) {
            alert("密码为空，请输入密码");
            $("#password").focus();
            return false;
        }
        var captchaCode = $("#captchaCode").val();
        if (!captchaCode) {
            alert("验证码为空，请输入验证码");
            $("#captchaCode").focus();
            return false;
        }
        return true;
    }
</script>
</@layout.page>
