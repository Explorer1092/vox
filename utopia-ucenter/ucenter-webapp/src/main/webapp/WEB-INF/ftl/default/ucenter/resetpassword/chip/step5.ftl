<#import "../module.ftl" as com>
<@com.page>

<h4>
    找回密码
</h4>

<ul class="stepInfoBox">
    <li class="sel"><i>1</i><b>输入学号</b></li>
    <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
    <li class="sel"><s></s><i>3</i><b>重置密码</b></li>
    <li class="sel"><s></s><i>4</i><b>成功</b></li>
</ul>
<div class="aliCenter clrgray fontbig"><strong>密码重置成功，请重新登录！</strong></div>
<div class="aliCenter clrgray" style="padding:40px 0;">
    <i id="tips">3秒后将自动转入登录页，如没有跳转请点击</i> <a href="/"><span class="w-btn w-btn-small" style="width: 120px;">登录</span></a>
</div>
<script type="text/javascript">
    $(function(){
        //成功修改密码统计
        $17.tongji("成功修改密码", "");

        var count = 4;
        var timer = $.timer(function(){
            $('#tips').html(--count + "秒后将自动转入登录页，如没有跳转请点击");
            if(count == 0){
                timer.stop();
                setTimeout(function(){ location.href = "/"; }, 200);
            }
        });
        timer.set({ time : 1000, autostart : true });

        $17.voxLog({
            app : "teacher",
            module: "forgetStaticRefLog",
            op: "edit-success"
        });
    });
</script>
</@com.page>