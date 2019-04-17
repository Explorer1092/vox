<#import "registermodule.ftl" as com>
<@com.page title="注册">
<div id="send_pw_sms" class="loginbox">
<h4>发送手机短信</h4>
    <ul class="ulpad">
        <li class="gray pad">
            <h5>您将收到如下短信：</h5>
            <em style="width:auto; padding:12px;">
                ${message!}
            </em>
        </li>
        <li class="inp "><b class="tit"><strong class="clrblue">请输入手机号码：</strong></b><input id="mobile" name="mobile" type="text"  value="${mobile!}"></li>

        <li class="inp pad "><a id="send" href="javascript:void(0);" class="greenBtn widthB"><i>发送</i></a></li>
    </ul>
</div>

<div id="send_success" class="loginbox" style="display: none;">
    <h4>密码发送成功</h4>
    <ul class="ulpad">
        <li class="inp aliCenter regSuccess"><i></i><b>短信已成功发送至</b><span id="mobilenum"></span></li>
        <li class="inp aliCenter"><i class="clrgray">系统将在 <span id="time">3</span>秒后自动登录，如页面未跳转，请手动点击</i><a href="/"><span class="clrblue">立即登录</span></a></li>
    </ul>
</div>
</@com.page>
<script type="text/javascript">
    var timer;
    var seconds  = 3;
    function timecountdown(){
         if(seconds > 0){
             seconds--;
             $("#time").html(seconds);
         }else{
             clearInterval(timer);
             setTimeout(function(){ location.href = "/"; }, 200);
         }
    }

    $(function(){
    <#--发送短信弹窗-->
        $("#send").on("click", function(){
            var mobile = $("#mobile").val();
            if(mobile!=""){
                if( !$17.isMobile(mobile)){
                    alert("请填写正确的手机号码");
                    return false;
                }
            }else{
                alert("请输入手机号");
                return false;
            }

            $("#mobilenum").html(mobile);
            App.postJSON('/ucenter/sendpwsms.vpage',{"mobile":''+mobile+''}, function(_data){
                if(_data.success){
                    $("#send_pw_sms").hide();
                    $("#send_success").show();
                    timer = setInterval("timecountdown()",1000);
                }else{
                    alert("网络连接失败，请稍后尝试");
                }
            });

            return false;
        });
    })
</script>