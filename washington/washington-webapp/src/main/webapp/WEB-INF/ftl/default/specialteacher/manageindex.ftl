<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="老师学生管理"
pageJs=["app","jquery"]
pageJsFile={"app" : "public/script/specialteacher/adminapp"}
pageCssFile={"index" : ["/public/skin/specialteacher/css/skin"]}>
<div id="stuApp">
<#include "header.ftl">
<div class="teacherManagement-top">
    <a href="/specialteacher/center.vpage">首页</a>
    <span>＞</span>
    <a href="/specialteacher/admin/index.vpage">老师学生管理</a>
</div>
<div class="class-section">
    <div data-bind="template: { name: 'manageMenuTemp', data: manageMenu}"></div>
    <div class="class-content mt-20">
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
</div>
    <#include "footer.ftl">
    <#include "adminpagetemp.ftl">
</@layout.page>