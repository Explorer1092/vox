<#import "../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起作业-教务老师"
pageJs=["common", "teachercenterapp", "jquery"]
pageJsFile={"common": "public/script/specialteacherV2/common", "teachercenterapp": "public/script/specialteacherV2/teachercenterapp"}
pageCssFile={"index" : ["/public/skin/specialteacherV2/css/skin"]}>

<#include "../specialteacherV2/header.ftl">
<div id="page_bd" style="min-height: 680px; display: block;">
    <#--左侧导航-->
    <div id="main_nav">
        <ul>
            <li class="nav_user">
                <a href="/specialteacher/center/index.vpage">
                    <img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt="" class="nav_user_favicon">
                </a>
                <div class="nav_user_information">
                    <p class="nav_user_username">${(currentUser.profile.realname)!}</p>
                    <#--<a href="//ucenter.17zuoye.com/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage">-->
                        <#--<i class="nav_user_no_certified"></i>-->
                    <#--</a>-->
                </div>
            </li>
            <li class="line"></li>

            <#--班级管理-->
            <li class="nav_plan JS-clazzManageBox">
                <a href="javascript:void(0);"><i class="icon"></i>班级管理<i class="arrow" style="display: none;"></i></a>
            </li>

            <#--老师学生管理-->
            <li class="nav_school_bank JS-teacStuManageBox">
                <a href="javascript:void(0);"><i class="icon"></i>老师学生管理<i class="arrow"></i></a>
            </li>

            <#--个人中心-->
            <li class="nav_my_favorites JS-personCenterBox tab-active bg-active color-active">
                <a href="javascript:void(0);"><i class="icon "></i>个人中心<i class="arrow"></i></a>
            </li>
            <div data-bind="template: { name: 'centerSecondTabTemp', data: centerSecTabMenu}"></div>
        </ul>
    </div>

    <#--右侧主体-->
    <div id="page_body" style="min-height: 640px;">
        <#--默认展示信息-->
        <div data-bind="template: { name: 'teacherInfoTemp' }, visible: isTeacherInfoTemp()"></div>
        <#--修改个人信息-->
        <div data-bind="template: { name: 'modifyInfoTemp' }, visible: isModifyInfoTemp()"></div>
        <#--修改账户安全-->
        <div data-bind="template: { name: 'modifyAccountTemp' }, visible: isModifyAccountTemp()"></div>
    </div>
</div>
<script>
    var globalTeacherName = "${(currentUser.profile.realname)!}";
</script>
<#include "../specialteacherV2/footer.ftl">
<#include "../specialteacherV2/teachercentertemp.ftl">
</@layout.page>