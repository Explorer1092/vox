<#import "../module.ftl" as com>
<@com.page>

<h4>
    找回密码
</h4>
    <@ftlmacro.smsdelay />
<ul class="stepInfoBox">
    <li class="sel"><i>1</i><b>输入学号</b></li>
    <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
    <li><s></s><i>3</i><b>重置密码</b></li>
    <li><s></s><i>4</i><b>成功</b></li>
</ul>
<div class="findWayBox">
    <ul>
        <li class="inp"><b class="tit">请输入短信验证码</b><input id="checkCode" name="" type="text"  value="">
            <a id="sendMsg" href="javascript:void(0);" class="w-btn w-btn-mini" style="width: 150px;"><i id="sendMsg_i">获取免费短信验证码</i></a>
            <span class="hint" style="width: auto;"></span>
        </li>
        <li class="btn">
            <a href="javascript:goBack()" class="w-btn w-btn-small w-btn-green">返回</a>
            <a href="javascript:next();" class="w-btn w-btn-small v-forgetStaticLog" data-op="verification-code">下一步</a>
            <@com.feedbackButton />
        </li>
    </ul>
</div>
<script type="text/javascript">
    $(function(){
        $17.tongji("进入验证信息步骤总数");

        var $sendMsg = $("#sendMsg");
        var $sendMsgI = $("#sendMsg_i");
        var $hint = $(".hint");
        var timer = null;
        var timerCount = null;

        $sendMsg.on("click", function(){
            if($sendMsg.hasClass("w-btn-disabled")) {
                return false;
            }

            $17.tongji("发送短信验证邮件次数");

            $hint.html("正在努力发送，请稍候片刻...");

            $sendMsg.addClass("w-btn-disabled");

            $.post("/ucenter/sendverificationcode.vpage", { token: '${context.token}' }, function(data){
                if(data && data.info)
                    $(".hint").html(data.info);

                if(data && data.timer){
                    if(timer != null)
                        timer.stop();

                    timerCount = data.timer;
                    $sendMsg.addClass("w-btn-disabled");
                    timer = $.timer(function() {
                        if(timerCount <= 0){
                            $("#sendMsg").removeClass("w-btn-disabled");
                            $("#sendMsg_i").html("获取免费短信验证码");
                            $(".hint").html("");
                            timer.stop();
                        } else {
                            $sendMsgI.html(--timerCount + "秒之后可重新发送");
                        }
                    });
                    timer.set({ time : 1000});
                    timer.play();

                    $(".hint").html("验证码已发送，如未收到请稍候片刻");
                }
                else {
                    $("#sendMsg").removeClass("w-btn-disabled");
                }
                return false;
            });
        });

        $sendMsg.trigger('click');
    });

    function goBack(){
        setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step2', 'token': '${context.token}' }); }, 200);
    }

    function next(){
        if($("#checkCode").val() == ""){
            $.prompt('请输入短信验证码！', {
                title: "系统提示",
                buttons: {知道了: false}
            });
            return false;
        }else{
            $.post('/ucenter/verifycode.vpage', { 'token': '${context.token}' , code: $("#checkCode").val() }, function(data){
                if(data.success){
                    setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step4', 'token': '${context.token}' }); }, 200);
                }else{
                    $("#checkCode").val("");
                    $17.alert(data.info ? data.info : "验证码错误，请重新输入！");
                }
            });
        }
    }
</script>
</@com.page>