<#macro page title="" pageJs=[] pageJsFile={} pageCssFile={} bodyClass="">
<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass=bodyClass
title=title
pageJs=pageJs
pageJsFile=pageJsFile
pageCssFile={"index" : ["/public/skin/specialteacher/css/skin"]}>
    <div id="koApp">
    <!--头部-->
        <#include "header.ftl">
        <div class="teacherManagement-top">
            <a href="/specialteacher/center.vpage">首页</a>
             <span>＞</span>
            <a href="/specialteacher/clazzmanage.vpage">班级管理</a>
        </div>
    <!--主体-->
        <div class="class-section">
            <div class="js-leftSideMenuContent" data-bind="template: { name: 'gradeMenuTemp', data: gradeMenu }"></div>
            <#nested />
        </div>
    </div>
    <#include "footer.ftl">
    <#include "pagetemp.ftl">
</@layout.page>
</#macro>