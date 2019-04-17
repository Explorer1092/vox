<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="进校记录">
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <#if schoolRecordList?? && schoolRecordList?size gt 0>
            <#list schoolRecordList as school>
                <li>
                    ${school.workTime?string("yyyy-MM-dd")!""}
                    <a href="showSchoolRecord.vpage?recordId=${school.schoolRecordId!''}" class="link js-schoolRecord">
                        <#if school.workerId??>
                            <p>拜访人：${school.workerName!''}</p>
                        </#if>
                        <#if school.partnerId??>
                            <p>陪访人：<#if school.partnerId == 0>无<#else>${school.partnerName!'0'}</#if></p>
                        </#if>
                            <p>${school.workTitle!""} | 拜访${school.instructorCount!0}位关键人</p>
                        <#if school.followingPlan??>
                            <p>待办：${school.followingPlan!""} </p>
                        </#if>
                        <#if followingTime??>
                            <p>计划下次：${school.followingTime?string("yyyy-MM-dd")!""} </p>
                        </#if>
                    </a>
                </li>
            </#list>
        </#if>
    </ul>
</div>
</@layout.page>