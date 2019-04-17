<#import "../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起作业-教务老师"
<#--pageJs=["jquery"]-->
pageJs=["common", "clazzmanageapp", "teacherstudentapp", "teachercenterapp", "jquery"]
pageJsFile={"common": "public/script/specialteacherV2/common", "clazzmanageapp": "public/script/specialteacherV2/clazzmanageapp", "teacherstudentapp": "public/script/specialteacherV2/teacherstudentapp", "teachercenterapp": "public/script/specialteacherV2/teachercenterapp"}
pageCssFile={"index" : ["/public/skin/specialteacherV2/css/skin"]}>

<#--此ftl页面未使用-->
<#--注：先前考虑使用一个模板，引入三个脚本，在每个脚本的开头通过判断地址栏信息来决定是否读取后面的js，后来发现多次刷新页面后会产生缓存，页面会报ko bind bug-->
<#--所以后期改用三个模板，分别对应三页，具体可看README.md-->

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
                        <a href="//ucenter.17zuoye.com/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage">
                            <i class="nav_user_no_certified"></i>
                        </a>
                    </div>
                </li>
                <li class="line"></li>

                <#--班级管理-->
                <li class="nav_plan JS-clazzManageBox">
                    <a href="/specialteacher/clazz/index.vpage"><i class="icon"></i>班级管理<i class="arrow"></i></a>
                </li>

                <#--老师学生管理-->
                <li class="nav_school_bank JS-teacStuManageBox">
                    <a href="javascript:void(0);"><i class="icon"></i>老师学生管理<i class="arrow"></i></a>
                </li>
                <div class="JS-teacStuManageBox2" data-bind="template: { name: 'manageMenuTemp', data: manageMenu}"></div>

                <#--个人中心-->
                <li class="nav_my_favorites JS-personCenterBox">
                    <a href="javascript:void(0);"><i class="icon "></i>个人中心<i class="arrow"></i></a>
                </li>
                <div class="JS-personCenterBox2" data-bind="template: { name: 'centerSecondTabTemp', data: centerSecTabMenu}"></div>
            </ul>
        </div>

        <#--右侧主体-->
        <div id="page_body" style="min-height: 640px;">
            <#-------------------------------------班级管理模板------------------------------------->
            <div class="JS-clazzTempBox">

            </div>
            <#-----------------------------------老师学生管理模板------------------------------------>
            <div class="JS-adminTempBox">
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
            <#-------------------------------------个人中心模板------------------------------------->
            <div class="JS-centerTempBox">
                <#--默认展示信息-->
                <div data-bind="template: { name: 'teacherInfoTemp' }, visible: isTeacherInfoTemp()"></div>
                <#--修改个人信息-->
                <div data-bind="template: { name: 'modifyInfoTemp' }, visible: isModifyInfoTemp()"></div>
                <#--修改账户安全-->
                <div data-bind="template: { name: 'modifyAccountTemp' }, visible: isModifyAccountTemp()"></div>
            </div>
        </div>
    </div>
    <script>
        var globalTeacherName = "${(currentUser.profile.realname)!}";
    </script>
    <#include "../specialteacherV2/footer.ftl">
    <#include "../specialteacherV2/clazzmanagetemp.ftl">
    <#include "../specialteacherV2/teacherstudenttemp.ftl">
    <#include "../specialteacherV2/teachercentertemp.ftl">
</@layout.page>