<div class="class-sidebar">
    <div class="head-face clearfix">
        <a href="/teacher/center/index.vpage?ref=newIndex" class="face"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" alt="" width="80" height="80"></a>
        <div class="name" style="padding-top: 20px">
            <#if (currentUser.profile.realname)?? && currentUser.profile.realname != "">
                ${(currentUser.profile.realname)!}
            <#else>
                <a href="/teacher/center/index.vpage#/teacher/center/myprofile.vpage?ref=newIndex" class="w-red">设置姓名</a>
            </#if>
        </div>
        <#--<a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" class="tag">-->
            <#--<#if currentUser.fetchCertificationState() == "SUCCESS" >-->
                <#--<span class="w-icon-public w-icon-new-authVip" style="padding: 0" title="已认证">已认证</span>-->
            <#--<#else>-->
                <#--<span class="w-icon-public w-icon-new-authVip-dis" style="padding: 0">未认证</span>-->
            <#--</#if>-->
        <#--</a>-->
        <#--<div class="tag"></div>-->
    </div>
    <!--主菜单-->
    <#include "leftmenu.ftl" />
</div>