<#import "module.ftl" as module>
<@module.page
title="一起作业"
pageCssFile={"mizar" : ["public/skin/css/skin"]}
pageJsFile={"siteJs" : "public/script/auth"}
pageJs=["siteJs"]
leftMenu=""
>
<div class="op-wrapper clearfix">
    <span class="title-h1">欢迎使用一起作业</span>
    <#--觉得给商家看这个有点突兀，暂时先隐藏起来-->
    <#if currentUser.isOperator() || currentUser.isAdmin()>
    <div class="jumbotron" style="background-color: #345e82;margin-top: 16px; ">
        <div style="text-align: center;padding: 100px;">
            <img class="center-align" src="/public/skin/images/message.png" alt="">
            <h3><a href="/basic/notify/index.vpage" style="color: #fff;">消息：<span class="badge" style="font-size: inherit;">${unreadCnt!0}</span></a></h3>
        </div>
    </div>
    </#if>
</div>
</@module.page>