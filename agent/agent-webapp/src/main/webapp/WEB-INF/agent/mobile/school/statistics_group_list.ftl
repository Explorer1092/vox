<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="学校详情">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText">${title!}</div>
        </div>
    </div>
</div>


<div class="mobileCRM-V2-box mobileCRM-V2-info">
    <ul class="mobileCRM-V2-list">
        <#if list??>
            <#list list as item>
                <li>
                    <#if item.teacherData??>
                        <a style="color: #39c;" href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${item.teacherData.teacherId!}">
                            <div class="side-fl ">
                                ${item.teacherData.realName!''}
                                <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-green">
                                    <#if item.teacherData.subject?? && item.teacherData.subject.value?? && item.teacherData.subject.value?length gt 0>${item.teacherData.subject.value[0..0]}</#if>
                                </span>
                                <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-orange">
                                        <#if item.teacherData.authStatus?? && item.teacherData.authStatus.state?? && item.teacherData.authStatus.state == 1>证</#if>
                                </span>
                            </div>
                        </a>
                    </#if>
                    <a class="link link-ico"
                        <#if item.statisticsData??>
                       <#if type == 1>href="/mobile/school/group/student_register_info.vpage?groupId=${item.statisticsData.groupId!}"
                       <#elseif type == 2>href="/mobile/school/group/state_clazz_auth_new.vpage?groupId=${item.statisticsData.groupId!}"
                       <#elseif type == 3>href="/mobile/school/group/state_clazz_hca_new.vpage?groupId=${item.statisticsData.groupId!}"
                       <#elseif type == 4>href="/mobile/school/group/state_clazz_double_auth_new.vpage?&groupId=${item.statisticsData.groupId!}"
                       </#if>
                        </#if>
                    >
                        <#if item.statisticsData??>
                            <#if type == 1>
                                <div class="side-fr side-width side-orange" style="width: 10%;">${item.statisticsData.totalCount!0}</div>
                            <#elseif type == 2>
                                <div class="side-fr side-width side-orange" style="width: 5%;">${(item.statisticsData.totalCount!0) - (item.statisticsData.authedCount!0)}</div>
                                <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                <div class="side-fr side-width" style="width: 5%;">${item.statisticsData.authedCount!0}</div>
                                <div class="side-fr side-width side-small" style="width: 15%;">认证</div>
                            <#elseif type == 3>
                                <div class="side-fr side-width side-orange" style="width: 5%;">${(item.statisticsData.authedCount!0) - (item.statisticsData.hcaActiveCount!0)}</div>
                                <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                <div class="side-fr side-width" style="width: 5%;">${item.statisticsData.hcaActiveCount!0}</div>
                                <div class="side-fr side-width side-small" style="width: 15%;">本月</div>
                            <#elseif type == 4>
                                <div class="side-fr side-width side-orange" style="width: 5%;">${item.statisticsData.noDoubleSubject!0}</div>
                                <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                <div class="side-fr side-width" style="width: 5%;">${item.statisticsData.doubleSubjectCount!0}</div>
                                <div class="side-fr side-width side-small" style="width: 15%;">双科</div>
                            </#if>
                        </#if>
                    </a>
                </li>
            </#list>
        </#if>
    </ul>
</div>

</@layout.page>