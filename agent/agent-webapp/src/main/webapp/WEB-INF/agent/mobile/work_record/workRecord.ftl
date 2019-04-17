<#import "../layout_new.ftl" as layout>
<#assign groupName="搜索">
<@layout.page group=groupName title="老师工作记录">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerBack"><a href="javascript:window.history.back();">&lt;&nbsp;返回</a></div>
            <div class="headerText" id="pageTitle">老师工作记录</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info">
    <#setting datetime_format="yyyy-MM-dd"/>
    <ul class="mobileCRM-V2-list">
        <#if teacherWorkRecord?has_content>
            <#list teacherWorkRecord as record>
                <li>
                    <#if record.workType == "进校">
                        <#if record.recordType == 1>
                            <a href="/mobile/work_record/showSchoolRecord.vpage?recordId=${record.schoolRecordId!}" class="link link-ico">
                        <#elseif record.recordType == 2>
                            <a href="/mobile/work_record/work_info.vpage?workType=SCHOOL&workRecord=${record.schoolRecordId!}" class="link link-ico">
                        <#else>
                            <a class="link">
                        </#if>
                    <#else>
                        <a class="link">
                    </#if>
                            <div class="side-fl side-box"><span>${record.partnerName!}</span>（${record.workType!}）</div>
                            <div class="side-fr side-time">${record.workTime?string("yyyy-MM-dd")!}</div>
                        </a>
                    <div class="qa">
                        <#if record.workType == "进校">
                            <span style="color: #000000">备注：<#if record.visitInfo?has_content>${record.visitInfo!'无'}<#else>无</#if></span>
                        <#else>
                            <span style="color: #fa7252">${record.workTitle!}</span>
                            <br>
                            <span style="color: #000000">${record.workContent!}</span>
                        </#if>
                    </div>
                </li>
            </#list>
        <#else>
            <li>暂无相关工作记录...</li>
        </#if>
    </ul>
</div>
</@layout.page>