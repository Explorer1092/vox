<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="教师查询">
        <div class="mobileCRM-V2-header">
            <div class="inner">
                <div class="box">
                    <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
                    <div class="headerText">教师统计</div>
                    <a href="school_teacher_count_status.vpage?schoolId=${schoolId!}" class="headerBtn">状态查询</a>
                </div>
            </div>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <li name="english" class="type_teacher">
                <#if englishTeacher!=0 >
                    <a href="school_teacher_list.vpage?schoolId=${schoolId!}&subjectName=ENGLISH" class="link link-ico">
                        <div class="side-fl side-mode">英语老师</div>
                        <div class="side-fl side-mode">${englishTeacher!}人</div>
                    </a>
                <#else>
                    <div class="side-fl side-mode">英语老师</div>
                    <div class="side-fl side-mode">${englishTeacher!}人</div>
                </#if>
                </li>

                <li name="math" class="type_teacher">
                    <#if mathTeacher!=0 >
                    <a href="school_teacher_list.vpage?schoolId=${schoolId!}&subjectName=MATH" class="link link-ico">
                        <div class="side-fl side-mode">数学老师</div>
                        <div class="side-fl side-mode">${mathTeacher!}人</div>
                    </a>
                    <#else>
                        <div class="side-fl side-mode">数学老师</div>
                        <div class="side-fl side-mode">${mathTeacher!}人</div>
                    </#if>
                </li>
                <li name="chinese" class="type_teacher">
                    <#if chineseTeacher!=0 >
                    <a href="school_teacher_list.vpage?schoolId=${schoolId!}&subjectName=CHINESE" class="link link-ico">
                        <div class="side-fl side-mode">语文老师</div>
                        <div class="side-fl side-mode">${chineseTeacher!}人</div>
                    </a>
                    <#else>
                        <div class="side-fl side-mode">语文老师</div>
                        <div class="side-fl side-mode">${chineseTeacher!}人</div>
                    </#if>
                </li>
            </ul>
        </div>
</@layout.page>

