<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="教师查询">
        <div class="mobileCRM-V2-header">
            <div class="inner">
                <div class="box">
                    <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
                    <div class="headerText">教师统计</div>
                    <a href="school_teacher_count.vpage?schoolId=${schoolId!}" class="headerBtn">学科查询</a>
                </div>
            </div>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <li name="english" class="type_teacher">
                    <#if noUseTeacherCount!=0 >
                    <a href="school_teacher_list_status.vpage?schoolId=${schoolId!}&status=noUse" class="link link-ico">
                        <div class="side-fl side-mode">未使用</div>
                        <div class="side-fl side-mode">${noUseTeacherCount!}人</div>
                    </a>
                    <#else>
                        <div class="side-fl side-mode">未使用</div>
                        <div class="side-fl side-mode">${noUseTeacherCount!}人</div>
                    </#if>
                </li>
                <li name="math" class="type_teacher">
                    <#if noAuthTeacherCount!=0 >
                    <a href="school_teacher_list_status.vpage?schoolId=${schoolId!}&status=noAuth" class="link link-ico">
                        <div class="side-fl side-mode">使用未认证</div>
                        <div class="side-fl side-mode">${noAuthTeacherCount!}人</div>
                    </a>
                    <#else>
                        <div class="side-fl side-mode">使用未认证</div>
                        <div class="side-fl side-mode">${noAuthTeacherCount!}人</div>
                    </#if>
                </li>
                <li name="chinese" class="type_teacher">
                    <#if authedTeacherCount!=0 >
                        <a href= "school_teacher_list_status.vpage?schoolId=${schoolId!}&status=authed" class="link link-ico">
                            <div class="side-fl side-mode">已认证</div>
                            <div class="side-fl side-mode">${authedTeacherCount!}人</div>
                        </a>
                    <#else>
                        <div class="side-fl side-mode">已认证</div>
                        <div class="side-fl side-mode">${authedTeacherCount!}人</div>
                    </#if>
                </li>
            </ul>
        </div>
</@layout.page>

