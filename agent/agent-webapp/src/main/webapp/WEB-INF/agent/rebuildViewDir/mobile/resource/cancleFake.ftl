<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="取消判假" pageJs=""  footerIndex=4>
    <@sugar.capsule css=['new_home']/>
<div class="homeText-box">
    <span class="ht-right js-submit" style="display: none;">提交</span>
    <div class="ht-text">
        <textarea id="regMsg" placeholder="点此填写为老师取消判假的原因(10-100字之间)" maxlength="99" min="10"></textarea>
    </div>
</div>
<script>
    var AT = new agentTool();
    var teacherId = ${teacherId!''}
    $(".js-submit").on("click",function(){
        var msg = $.trim($("#regMsg").val());
        if(msg.length != 0 && msg.length >= 10){
            $.post("relieve_faketeacher.vpage",{desc:msg,teacherId:teacherId},function(res){
                if(res.success){
                    AT.alert("取消判假成功，老师可正常使用");
                    disMissViewCallBack();
                }else{
                    AT.alert('提交失败');
                }
            });
        }else{
            AT.alert("取消判假原因不能少于10字符！");
        }
    });
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:"提交",
            rightTextColor:"ff7d5a",
            needCallBack:true
        };
        var topBarCallBack = function () {
            $(".js-submit").click();
        };
        setTopBarFn(setTopBar,topBarCallBack);
    })
</script>
</@layout.page>