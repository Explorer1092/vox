<#--TODO 这个页面是不是也不需要了？-->
<#import "../module.ftl" as com>
<@com.page title="老师" t=1>
<div style="background-color: #ebe9e4;">
    <div class="containerDiscipline">
        <div class="inner">
            <div class="infoVoerBox">
                <div class="back">
                    <h4><span class="correct"></span>一封验证邮件已经发送到了<span id='J_success_email'></span>邮箱，立即激活开始体验一起作业吧！</h4>
                    <p><a id="J_success_email_url" href="http://${emailLogin!''?html}" target="_blank" title="登录邮箱验证"></a></p>
                </div>
                <ul>
                    <li>激活遇到问题？</li>
                    <li>1. 没收到验证邮件？请查看是否进入了垃圾邮件</li>
                    <li>2.如果多次尝试没有收到邮件，请重新 <a href="/signup/htmlchip/teacher.vpage">注册</a></li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script>
    <#-- 这里会构造对外的url跳转，有些安全检测网站认为这里有xss隐患，所以我们先特殊处理一下 -->
    var email = unescape('${escapedEmail!}');
    var emailLoginHost = unescape('${escapedEmailLoginHost!}');
    $(function(){
        $('#J_success_email_url').attr('href', 'http://' + emailLoginHost);
        $('#J_success_email').text(email);
    });
</script>
</@com.page>