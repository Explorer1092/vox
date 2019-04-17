<#import "../layout_new.ftl" as layout>
<@layout.page group="业绩" title="新增认证老师">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">新增认证老师</div>
        </div>
    </div>
</div>

<ul class="mobileCRM-V2-list">
    <#if addAuthTeachers?has_content>
        <#list addAuthTeachers as teacher>
            <li>
                <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${teacher.id!}" class="link link-ico">
                    <div class="side-fl side-mode">${teacher.fetchRealname()}</div>
                    <div class="side-fr side-gray">${(teacher.subject.value)!}</div>
                </a>
            </li>
        </#list>
    </#if>
</ul>
</@layout.page>