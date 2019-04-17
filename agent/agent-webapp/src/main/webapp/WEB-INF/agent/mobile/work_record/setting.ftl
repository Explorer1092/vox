<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page footerIndex=4 title="设置">
<@sugar.capsule css=['team']/>
<style>
    body {background-color: #f9f9fa;}
</style>
<div class="crmList-box">
    <div class="myTitle">
        <a onclick="openSecond('/mobile/my/my_info.vpage')" class="item gap">个人信息</a>
        <a onclick="openSecond('/resetPassword.vpage?client=h5&return_show=true')" class="item gap">修改密码</a>
        <a href="javascript:;" class="item gap js-layout">退出登录</a>
    </div>
</div>
<script>
    $(document).on("click",".js-layout",function () {
        window.external.innerJump(JSON.stringify({name:'go_login'}));
    })
</script>
</@layout.page>
