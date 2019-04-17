<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="老师工作记录" pageJs="" footerIndex=4>
<@sugar.capsule css=['new_base','school']/>
<div class="head fixed-head">
    <a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>
    <span class="return-line"></span>
    <span class="h-title">老师工作记录</span>
</div>
<div class="flow w-record">
    <#if teacherWorkRecord?has_content && teacherWorkRecord?size gt 0>
        <#list teacherWorkRecord as record>
            <#if record.workType == "进校">
                <div class="item">
                    <p class="name">
                    ${record.partnerName!}（${record.workType!}）
                        <span class="inner-right-text">${record.workTime?string("yyyy-MM-dd")!}</span>
                    </p>
                    <div class="content">
                            备注：<#if record.visitInfo?has_content>${record.visitInfo!'无'}<#else>无</#if>
                    </div>
                </div>
            <#else>
                <div class="item">
                    <p class="name">
                        ${record.workerName!}（${record.workType!}）
                        <span class="inner-right-text">${record.workTime?string("yyyy-MM-dd")!}</span>
                    </p>
                    <div class="content">
                            备注：<#if record.workContent?has_content>${record.workContent!'无'}<#else>无</#if>
                    </div>
                </div>
            </#if>
        </#list>
    <#else>
        <p style="text-align: center;margin-top: 30px;">暂无相关工作记录...</p>
    </#if>
</div>
</@layout.page>