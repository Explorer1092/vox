<#import "../layout_new.ftl" as layout>
<@layout.page group="login">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerBack"><a href="javascript:window.history.back()">&lt;&nbsp;返回</a></div>
            <div class="headerText">手机号登录</div>
        </div>
    </div>
</div>
    <#include '../../widget_alert_messages.ftl'/>
<form action="/mobile/login_by_mobile.vpage" method="post" id="loginForm">
    <input type="hidden" id="client" name="client" value="h5">

    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <ul class="mobileCRM-V2-list">
            <li class="user-name">
                <input type="text" style="width: 50%;padding: 0" id="mobile" name="mobile" maxlength="11" placeholder="请输入手机号">
                <button id="code-btn" type="button" class="side-fr" style="text-align:center;width:90px;cursor:pointer;border:none;line-height:22px;color:#fff;border-radius:4px;background-color: #39AC6A;padding:0px 4px;font: 0.8rem/2rem 'Microsoft YaHei';">获取验证码</button>
            </li>
            <li>
                <input type="tel" value="" id="captchaCode" name="captchaCode" style="width: 120px; text-align: left" maxlength="6" placeholder="请输入验证码"/>
            </li>
        </ul>
    </div>
    <div class="mobileCRM-V2-submit">
        <input type="submit" value="登录" style="border-radius:4px;font-size:1rem;border:0px;">
    </div>
</form>
<script type="text/javascript">
    $(function () {

        $("#loginForm").submit(function () {
            return validate();
        });

        $("#code-btn").on("click", function () {
            getSmsCode();
        });

    });

    $(document).ready(function () {
        try {
            var code = location.href.split("code=")[1];
            if (code == 1) {
                alert("验证码校验失败");
            } else if (code == 2) {
                alert("手机号或验证码有误!");
            } else if (code == 3) {
                alert("暂不支持协作账号使用本应用，请申请专员账号!");
            }
        } catch (e) {
        }
    });

    function getSmsCode(){
        var checkResult = checkMobile();
        if (!checkResult) {
            alert("请填写正确的手机号");
            $("#mobile").focus();
            return;
        }
        var mobile = $.trim($("#mobile").val());
        $.post("/mobile/getSMSCode.vpage",
                {mobile:mobile},
                function(data){
                    if(!data.success){
                        alert(data.info);
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
            $("#code-btn").attr("style","text-align:center;width:90px;cursor:pointer;border:none;line-height:22px;color:#fff;border-radius:4px;background-color: #39AC6A;padding:0px 4px;font: 0.8rem/2rem 'Microsoft YaHei';");
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
            alert("请填写正确的手机号");
            $("#mobile").focus();
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
