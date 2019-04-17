<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page navBar="hidden" footer="none">
    <@sugar.capsule css=['school']/>
<div>
    <div class="head" style="color:#76797e;text-align: center">
        手机号登录
    </div>

    <div class="login-wrapper">
        <form id="loginForm" method="post" action="/mobile/login_by_mobile.vpage">
            <input type="hidden" id="client" name="client" value="h5">
            <div class="input-control auth-code phone">
                <span class="inner-right send-code" id="code-btn">获取验证码</span>
                <i class="user-icon phone-icon"></i>
                <input id="mobile" placeholder="请输入手机号" maxlength="11" type="tel" name="mobile" value="${mobile!}"/>
            </div>
            <div class="input-control password">
                <i class="user-icon code-icon"></i>
                <input id="captchaCode" placeholder="请输入验证码" maxlength="6" type="tel" name="captchaCode" autocomplete="off"/>
            </div>
        </form>
        <div class="login js-submit">登录</div>
        <div class="phone-login"><a href="/mobile/login.vpage">账号登录</a></div>
    </div>

</div>
<script>
    var AT = new agentTool();
    $(function () {

        $("#code-btn").on("click", function () {
            getSmsCode();
        });

        $(".js-submit").click(function(){
            if(validate()){
                $("#loginForm").submit();
            }
        });

    });

    $(document).ready(function () {
        try {
            var code = location.href.split("code=")[1];
            if (code == 1) {
                AT.alert("验证码失效或错误，请重新输入");
            } else if (code == 2) {
                AT.alert("该手机号码未绑定账号，请重新输入或联系管理员换绑手机号码");
            } else if (code == 3) {
                AT.alert("暂不支持协作账号使用本应用，请申请专员账号");
            }
        } catch (e) {
        }
    });

    function getSmsCode(){
        var checkResult = checkMobile();
        if (!checkResult) {
            AT.alert("请填写正确的手机号");
            $("#mobile").focus();
            return;
        }
        var mobile = $.trim($("#mobile").val());
        $.post("/mobile/getSMSCode.vpage",
                {mobile:mobile},
                function(data){
                    if(!data.success){
                        AT.alert(data.info);
                    }else{
                        startTimer();
                    }
                }
        );
    }

    var myVar;
    var time = 60;//
    function startTimer(){
        $("#code-btn").attr("disabled",true);
        myVar=self.setInterval("myTimer()",1000);
    }
    function myTimer(){
        $("#code-btn").text(time + "s后(重发)");
        $("#code-btn").attr("style","text-align:center;width:90px;cursor:pointer;border:none;line-height:22px;color:#ccc;border-radius:4px;background-color: #eee;padding:0px 4px;font: 0.8rem/2rem 'Microsoft YaHei';");
        time = time - 1;
        if(time == 0){
            $("#code-btn").text("获取验证码");
            $("#code-btn").attr("disabled",false);
            $("#code-btn").attr("class","inner-right send-code").attr("style","");
            clearInterval(myVar);
            time = 60;
        }
    }

    function checkMobile(){
        var result = false;
        var mobile = $.trim($("#mobile").val());
        if(mobile.length != 0){
            var reg=/^(1)\d{10}$/;
            if(reg.test(mobile)){
                result = true;
            }
        }
        return result;
    }

    function validate() {
        var checkResult = checkMobile();
        if (!checkResult) {
            AT.alert("请填写正确的手机号");
            $("#mobile").focus();
            return false;
        }

        var captchaCode = $("#captchaCode").val();
        if (!captchaCode) {
            AT.alert("验证码为空，请输入验证码");
            $("#captchaCode").focus();
            return false;
        }
        return true;
    }
</script>
</@layout.page>
