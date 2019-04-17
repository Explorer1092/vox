<#import "../module.ftl" as com>
<@com.page>
<h4>
    找回密码
</h4>
<ul class="stepInfoBox">
    <li class="sel"><i>1</i><b>输入学号</b></li>
    <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
    <li class="sel"><s></s><i>3</i><b>重置密码</b></li>
    <li><s></s><i>4</i><b>成功</b></li>
</ul>
<ul class="formList">
    <li class="inp "><b class="tit" style="width: 260px;">请设置新密码</b><input id="password" name="" type="password"  value=""></li>
    <li class="inp "><b class="tit" style="width: 260px;">确认新密码</b><input id="confirmpw" name="" type="password"  value=""></li>
    <li class="btn">
        <a href="javascript:goBack();" class="w-btn w-btn-small w-btn-green">返回</a>
        <a href="javascript:next();" class="w-btn w-btn-small v-forgetStaticLog" data-op="set-new-password">下一步</a>
        <@com.feedbackButton />
    </li>
</ul>
<script type="text/javascript">
    $(function(){
        $17.tongji("进入重置密码页面总数");
    });

    function goBack(){
        setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step2', 'token': '${context.token}' }); }, 200);
    }

    function next(){
        var p1 = $("#password").val();
        var p2 = $("#confirmpw").val();
        if($17.isBlank(p1)){
            $.prompt('请输入新密码', {
                title: "系统提示",
                buttons: {知道了: false}
            });
            $("#password").focus();
            return;
        }



        if($17.isBlank(p2)){
            $.prompt('请重新输入新密码', {
                title: "系统提示",
                buttons: {知道了: false}
            });
            $("#confirmpw").focus();
            return;
        }
        
        if(p1 != p2) {
            $.prompt('两次输入的密码不一致，请重新设定密码，并确保输入正确', {
                title: "系统提示",
                buttons: {知道了: false}
            });
            return;
        }
        $.post('/ucenter/resetpwd.vpage', {'token': '${context.token}', 'password': p1}, function(data) {
            if(data && data.success){
                $17.tongji("重置成功总数");
                setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step5' }); }, 200);
            }else{
                alert(data.info);
            }
        });
    }

</script>
</@com.page>