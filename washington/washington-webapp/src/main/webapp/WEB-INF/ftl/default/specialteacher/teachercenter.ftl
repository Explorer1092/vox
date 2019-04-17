<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业-教务老师"
pageJs=["jquery", "app"]
pageJsFile={"app" : "public/script/specialteacher/teachercenterapp"}
pageCssFile={"index" : ["/public/skin/specialteacher/css/skin"]}>

<div id="teacherCenterApp">
<#include "header.ftl">
<div class="class-section">
    <div class="class-sidebar">
        <a class="head-face clearfix">
            <div class="face"><img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt=""></div>
            <div class="name">${(currentUser.profile.realname)!}</div>
            <#--<div class="tag"><img src="<@app.link href='public/skin/specialteacher/images/img-01.png'/>" alt=""></div>-->
        </a>
        <a href="/specialteacher/center.vpage" class="nav-home"><span>返回首页</span></a>
        <div class="nav-mode JS-navMode">
            <a href="/specialteacher/center/teachercenter.vpage" class="sort personal"><span>个人中心</span></a>
            <a href="#modifyInfoTemp" class="link"><span>我的资料</span></a>
            <a href="#modifyAccountTemp" class="link"><span>账号安全</span></a>
            <#--<a href="#messageCenter" class="link"><span>消息中心</span></a>-->
            <#--<a href="javascript:;" class="link"><span>我的认证</span></a>-->
            <#--<a href="#" class="service">联系客服</a>-->
        </div>
    </div>

    <div class="class-content mt-20" id="moduleBox">
        <#--默认展示信息-->
        <div data-bind="template: { name: 'teacherInfoTemp' }" style="display: none;"></div>
        <#--修改个人信息-->
        <div data-bind="template: { name: 'modifyInfoTemp' }" style="display: none;"></div>
        <#--修改账户安全-->
        <div data-bind="template: { name: 'modifyAccountTemp' }" style="display: none;"></div>
        <#--消息中心-->
        <#--<div data-bind="template: { name: 'messageCenter' }" style="display: none;"></div>-->
    </div>
</div>
</div>
<script>
    var globalTeacherName = "${(currentUser.profile.realname)!}";
</script>

    <#include "footer.ftl">
    <#include "teachercentertemp.ftl">
</@layout.page>