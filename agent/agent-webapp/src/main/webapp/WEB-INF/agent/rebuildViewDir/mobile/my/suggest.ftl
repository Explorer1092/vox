<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="" pageJs="regionmsg"  footerIndex=4>
    <@sugar.capsule css=['new_home']/>
<div class="homeText-box">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();" style="color: #76797e;"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">天玑建议</span>
        <span class="ht-right js-submit">提交</span>
    </div>
    <div class="ht-text">
        <textarea id="regMsg" placeholder="天玑（CRM）团队正在倾听您的建议..." maxlength="500"></textarea>
    </div>
</div>
<script>
    var AT = new agentTool();
    $(".js-submit").on("click",function(){
        var msg = $("#regMsg").val();
        if(msg.length != 0 ){
            $.post("save_suggest.vpage",{content:msg},function(res){
                if(res.success){
                    AT.alert("保存成功");
                    location.href = "/mobile/my/index.vpage";
                }else{
                    AT.alert(res.info);
                }
            });
        }else{
            AT.alert("建议内容不能为空！");
        }
    })
</script>
</@layout.page>