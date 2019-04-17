<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="新增老师">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText">老师列表</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-rankInfo">
    <div class="infoBox infoTab">
        <div class="activtaor active" active-value="REG">
            <div class="boxNum">${addRegTeachers?size}</div>
            <div class="boxFoot">新增注册老师</div>
        </div>
        <div class="activtaor" active-value="AUTH">
            <div class="boxNum">${addAuthTeachers?size}</div>
            <div class="boxFoot">新增认证老师</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info activity" active-value="REG">
    <#if addRegTeachers?has_content>
        <ul class="mobileCRM-V2-list">
            <#list addRegTeachers as teacher>
                <li>
                    <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${teacher.id!}" class="link link-ico">
                        <div class="side-fl side-mode">${teacher.fetchRealname()}</div>
                        <div class="side-fr side-gray">${(teacher.subject.value)!}</div>
                    </a>
                </li>
            </#list>
        </ul>
    </#if>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info activity" active-value="AUTH" style="display: none">
    <#if addAuthTeachers?has_content>
        <ul class="mobileCRM-V2-list">
            <#list addAuthTeachers as teacher>
                <li>
                    <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${teacher.id!}" class="link link-ico">
                        <div class="side-fl side-mode">${teacher.fetchRealname()}</div>
                        <div class="side-fr side-gray">${(teacher.subject.value)!}</div>
                    </a>
                </li>
            </#list>
        </ul>
    </#if>
</div>
<script type="text/javascript">
    $(function () {
        activtaor.bind();
    });
</script>
</@layout.page>