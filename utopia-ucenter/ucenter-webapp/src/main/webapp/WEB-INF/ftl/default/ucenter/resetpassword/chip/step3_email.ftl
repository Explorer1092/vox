<#import "../module.ftl" as com>
<@com.page>
<div id="step3_1">
    <h4>
        找回密码
    </h4>
    <ul class="stepInfoBox">
        <li class="sel"><i>1</i><b>输入学号</b></li>
        <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
        <li><s></s><i>3</i><b>重置密码</b></li>
        <li><s></s><i>4</i><b>成功</b></li>
    </ul>
    <div class="aliCenter" style="padding:40px 0;"><a href="javascript:sendEmail();" class="reg_btn" id="blueSmallBtn"><i>发送免费验证邮件到已绑定邮箱</i></a></div>
    <div style="padding: 40px 0; text-align: center;">
        <a href="javascript:goBack();" class="reg_btn reg_btn_green">返回</a>
    </div>
</div>


<div id="step3_2" style="display: none;">
    <h4>
        找回密码
    </h4>
    <ul class="stepInfoBox">
        <li class="sel"><i>1</i><b>输入学号</b></li>
        <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
        <li><s></s><i>3</i><b>重置密码</b></li>
        <li><s></s><i>4</i><b>成功</b></li>
    </ul>
    <div id="ddd" class="aliCenter clrgray" style="padding:40px 0;">验证邮件已发送至绑定邮箱，请登录邮箱查看并进行下一步操作</div>
    <div style="padding: 40px 0; text-align: center;">
        <a href="/" class="w-btn w-btn-small w-btn-green">返回首页</a>
        <a href="javascript:checkEmail();" class="w-btn w-btn-small">登录邮箱</a>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.tongji("进入验证信息步骤总数");
        sendEmail();
    });

    var obscuredEmail = '${userInfo.obscuredEmail}';
    function sendEmail(){
        $17.tongji("发送手机验证码次数");

        if(!$("#blueSmallBtn").hasClass("graySmallBtn")){
            $("#blueSmallBtn").addClass("graySmallBtn");
            $("#blueSmallBtn i").html("正在努力发送邮件，请稍后片刻...");

            $.post('/ucenter/sendverificationlink.vpage', {'token': '${context.token}'}, function(data){
                if(data.success){
                    $("#step3_1").hide();
                    $("#step3_2").show();
                    $("#blueSmallBtn i").html("发送免费验证邮件到已绑定邮箱");
                    $("#blueSmallBtn").removeClass("graySmallBtn");
                } else {
                    alert(data.info);
                }
            });
        }
    }

    function checkEmail(){
        var mail_url={
            'qq.com': 'http://mail.qq.com',
            'gmail.com': 'http://mail.google.com',
            'sina.com': 'http://mail.sina.com.cn',
            '163.com': 'http://mail.163.com',
            '126.com': 'http://mail.126.com',
            'yeah.net': 'http://www.yeah.net/',
            'sohu.com': 'http://mail.sohu.com/',
            'tom.com': 'http://mail.tom.com/',
            'sogou.com': 'http://mail.sogou.com/',
            '139.com': 'http://mail.10086.cn/',
            'outlook.com': 'http://www.outlook.com',
            'live.com': 'http://login.live.com/',
            'live.cn': 'http://login.live.cn/',
            'live.com.cn': 'http://login.live.com.cn',
            '189.com': 'http://webmail16.189.cn/webmail/',
            'yahoo.com.cn': 'http://mail.cn.yahoo.com/',
            'yahoo.cn': 'http://mail.cn.yahoo.com/',
            'eyou.com': 'http://www.eyou.com/',
            '21cn.com': 'http://mail.21cn.com/',
            '188.com': 'http://www.188.com/',
            'foxmail.com': 'http://www.foxmail.com'
        };

        if(obscuredEmail.indexOf("@") != -1){
            url = obscuredEmail.substring(obscuredEmail.indexOf("@") + 1).toLowerCase();
            if(mail_url[url]!= undefined)
                setTimeout(function(){ location.href =  mail_url[url]; }, 200);
            else
               $17.alert("请登录邮箱查看");
        }
    }

    function goBack(){
        setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step2', 'token': '${context.token}'}); }, 200);
    }
</script>
</@com.page>