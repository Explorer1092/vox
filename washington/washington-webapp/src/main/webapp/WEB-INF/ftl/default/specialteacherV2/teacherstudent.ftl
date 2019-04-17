<#import "../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起作业-教务老师"
pageJs=["common", "teacherstudentapp", "jquery"]
pageJsFile={"common": "public/script/specialteacherV2/common", "teacherstudentapp": "public/script/specialteacherV2/teacherstudentapp"}
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
            <li class="nav_school_bank JS-teacStuManageBox tab-active bg-active color-active">
                <a href="javascript:void(0);"><i class="icon"></i>老师学生管理<i class="arrow"></i></a>
            </li>
            <div data-bind="template: { name: 'manageMenuTemp', data: manageMenu}"></div>

            <#--个人中心-->
            <li class="nav_my_favorites JS-personCenterBox">
                <a href="javascript:void(0);"><i class="icon "></i>个人中心<i class="arrow"></i></a>
            </li>
        </ul>
    </div>

    <#--右侧主体-->
    <div id="page_body" style="min-height: 640px;">
        <#--固定共用按钮（下载学生名单、打印学生条形码）-->
        <div data-bind="template: { name: 'userGuideModal' }"></div>
        <#--添加老师账号-->
        <div data-bind="template: { name: 'addTeacherModal' },visible:isTeacherShow()"></div>
        <#--为老师建班授课-->
        <div data-bind="template: { name: 'createClazzModal' },visible:createClazzShow()"></div>
        <#--添加学生账号-->
        <div data-bind="template: { name: 'addStudentModal' },visible:isStudentShow()"></div>
        <#--校内打散换班-->
        <div data-bind="template: { name: 'changeClazzModal' },visible:changeClazzShow()"></div>
        <#--复制教学班学生-->
        <div data-bind="template: { name: 'copyTeachingModal' },visible:copyTeachingShow()"></div>
        <#--标记借读生-->
        <div data-bind="template: { name: 'markTransientModal' },visible:markTransientShow()"></div>
    </div>
</div>
<#include "../specialteacherV2/footer.ftl">
<#include "../specialteacherV2/teacherstudenttemp.ftl">
</@layout.page>