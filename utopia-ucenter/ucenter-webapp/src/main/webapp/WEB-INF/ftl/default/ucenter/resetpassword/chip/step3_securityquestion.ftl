<#import "../module.ftl" as com>
<@com.page>
<h4>
    找回密码
</h4>
<ul class="stepInfoBox">
    <li class="sel"><i>1</i><b>输入学号</b></li>
    <li class="sel"><s></s><i>2</i><b>验证信息</b></li>
    <li><s></s><i>3</i><b>重置密码</b></li>
    <li><s></s><i>4</i><b>成功</b></li>
</ul>
<ul class="formList">
    <li class="inp">
        <b class="tit">请回答密保问题:</b>
        ${(question)!''}
    </li>

    <li class="inp">
        <b class="tit">问题答案:</b>
        <input id="answer" name="" type="text"  value="" maxlength="20">
    </li>

    <li class="btn">
        <a href="javascript:goBack()" class="w-btn w-btn-small w-btn-green">上一步</a>
        <a href="javascript:next();" class="w-btn w-btn-small">下一步</a>
        <@com.feedbackButton />
    </li>
</ul>
<script type="text/javascript">
    function goBack(){
        setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step2', 'token': '${context.token}' }); }, 200);
    }
    $("#answer").focus();

    function next(){
        var answer = $("#answer");
        if($17.isBlank(answer.val())){
            answer.focus();
            alert("请输入问题答案！");
        }else{
            $.post('/ucenter/verifysecurityquestionanswer.vpage', { 'token': '${context.token}' , answer: answer.val() }, function(data){
                if(data.success){
                    setTimeout(function(){ location.href = "resetpwdstep.vpage?" + $.param({'step': 'step4', 'token': '${context.token}' }); }, 200);
                }else{
                    answer.val('').focus();
                    alert("你输入的答案与设密保答案不符，请重新输！");
                }
            });
        }
    }
</script>
</@com.page>