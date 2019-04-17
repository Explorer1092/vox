<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="进校记录" pageJs="" footerIndex=4>
<@sugar.capsule css=['new_base','school']/>
<div class="s-record">
    <#if schoolRecordList?? && schoolRecordList?size gt 0>
        <#list schoolRecordList as school>

            <div class="item">
                <p class="time">
                    ${school.workTime?string("yyyy-MM-dd")!""}
                </p>
                <a href="/view/mobile/crm/workrecord/into_school_info.vpage?recordId=${school.schoolRecordId!''}">
                    <div class="info">
                        <#if school.workerId??>
                            <p style="font-size:0.75rem;">拜访人：${school.workerName!''}</p>
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
                    </div>
                </a>
            </div>

        </#list>
    <#else>
        <p style="text-align: center;margin-top: 30px;">暂无进校记录...</p>
    </#if>
</div>
</@layout.page>