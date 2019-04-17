<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="我的" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['team']/>
<script src="https://qiyukf.com/script/f10a2349a4bead156114e00f9084177c.js" charset="utf-8"></script>
<script>
    var AT = new agentTool();
    (function(){
        $.get('${ProductConfig.getMainSiteBaseUrl()}/help/getconfig.vpage?userId=${userId!0}&questionType=TIANJI_ALL')
                .done(function(result){
                    if(result.success) {
                        ysf.config(result.config);
                       setTimeout("location.href = ysf.url()",1500)
                    }else{
                        AT.alert('请求失败，请返回重试');
                    }
                })
                .fail(function(xhr, status, err_msg){
                    AT.alert(err_msg);
                });
    })();

</script>
</@layout.page>