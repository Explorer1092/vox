<#import "../layout_new_no_group.ftl" as layout>
<@layout.page  title="注册老师">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">注册老师</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">姓名</div>
            <div class="side-fr side-time side-bWidth" style="width: 31%;" >注册日期</div>
            <div class="side-fr side-time side-bWidth" style="width: 16%;" >学生数</div>
        </div>
    </li>
    <#if teacherList?has_content>
        <#list teacherList as teacher>
            <li>
                <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${teacher.teacherId!}" class="link link-ico" >
                    <div class="side-fl" style="width: 47%; ">${teacher.realName!'未知'}
                        <span class="id mobileCRM-V2-globalTag mobileCRM-V2-globalTag-green" >
                            <#if teacher?? && teacher.subjectValue?? && teacher.subjectValue?length gt 0>${teacher.subjectValue[0..0]}</#if>
                        </span>
                        <span class="id mobileCRM-V2-globalTag mobileCRM-V2-globalTag-orange" >
                            <#if teacher.authState?? && teacher.authState == 1>
                                证
                            </#if>
                        </span>
                    </div>
                    <div class="side-fr side-bWidth side-orange" style="width: 31%;">${teacher.registerTimeStr!}</div>
                    <div class="side-fr side-bWidth side-total" style="width: 10%;">${teacher.registerStudentCount!}</div>
                </a>
            </li>
        </#list>
    <#else>
        <li>
            <div class="side-fl side-small" style="width: 40%; ">
                未找到相应数据
            </div>
        </li>
    </#if>
</ul>
</@layout.page>