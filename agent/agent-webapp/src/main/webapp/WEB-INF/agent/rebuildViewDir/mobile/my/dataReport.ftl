<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="大数据报告申请" pageJs="workflow" footerIndex=4>
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <div class="c-main">
        <div class="c-opts gap-line c-flex c-flex-3 js-head" style="margin-bottom:.5rem">
            <span class="js-tabItem the" data-index="PENDING">审批中<#if dataMap?? && dataMap["PENDING"]??>(${dataMap["PENDING"]?size})</#if></span>
            <span class="js-tabItem" data-index="COMPLETED">审批完成</span>
            <span class="js-tabItem" data-index="REVOKED">已撤销</span>
        </div>
        <div>
            <#if dataMap??>
                <#list dataMap?keys as key>
                    <#assign data = dataMap[key]>
                    <#if data?? && data?size gt 0>
                        <div class="tab-${key!""} tabItem" style="clear:both" <#if key != "PENDING">hidden</#if>>
                            <div class="c-list">
                                <#list data as item>
                                    <div class="adjustmentExamine-box" style="margin-top: .5rem">
                                        <div class="adjust-head js-itemBtn" data-sid="${item.apply.id!0}" data-type="AGENT_DATA_REPORT_APPLY" style="border:none">
                                            <div class="right">${item.apply.createDatetime?string("MM-dd")}</div>
                                            <div>大数据报告申请</div>
                                            <div class=""><#if item.apply.reportLevel == 1>${item.apply.cityName!}/${item.apply.countyName!}<#elseif item.apply.reportLevel == 2>${item.apply.countyName!}<#elseif item.apply.reportLevel == 3>${item.apply.schoolName!}</#if>（<#if item.apply.subject?? && item.apply.subject == 1>小学英语报告</#if><#if item.apply.subject?? && item.apply.subject == 2>小学数学报告</#if>）
                                            </div>
                                            <#if item.apply.status == "APPROVED">
                                            <p style="color: rgb(58,181,74)">
                                            审批通过</p>
                                                <#elseif item.apply.status == "REJECTED" || item.apply.status == "PENDING">
                                                    <p style="color:#ff7d5a">
                                                <#if item.processResultList?? && item.processResultList?size gt 0>
                                                <#list item.processResultList?reverse as process>
                                                    <#if key == "PENDING">
                                                        <#if process.result == "待审核">
                                                            ${process.accountName!''} 审核中
                                                        </#if>
                                                    <#elseif key == "COMPLETED">
                                                        <#if process.result == "驳回">
                                                        被驳回<br>
                                                        驳回原因：${process.processNotes!''}【驳回人：${process.accountName!''}】
                                                        </#if>
                                                    <#elseif key == "PENDING">
                                                    </#if>
                                                </#list>
                                                </#if>
                                            </p>
                                            <#elseif item.apply.status == "REVOKED">
                                            <p style="color: #ff7d5a">已撤销</p>
                                            </#if>
                                        </div>
                                    </div>
                                </#list>
                            </div>
                        </div>
                    </#if>
                </#list>
            </#if>
        </div>
    </div>
</div>
</@layout.page>