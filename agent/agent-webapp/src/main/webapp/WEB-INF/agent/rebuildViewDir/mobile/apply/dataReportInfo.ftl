<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="大数据报告申请" pageJs="" footerIndex=4>
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <div class="tab-main" style="clear:both">
    <#--待审核-->
        <div>
            <#if applyData?? && applyData.apply??>
                <#assign item = applyData>
                <div class="adjustmentExamine-box">
                <#-- <div class="adjust-head js-itemBtn" data-sid="${item.apply.id!0}">
                     <div class="right">${item.apply.createDatetime?string("MM-dd")}</div>
                     <div class="">${item.apply.accountName!'123'}</div>
                 </div>-->
                    <div class="adjust-content">
                        <p class="pattern"><span style="color:#ff7d5a">学科：</span>
                            <#if item.apply.subject?? && item.apply.subject == 1>
                                小学英语
                            </#if>
                            <#if item.apply.subject?? && item.apply.subject == 2>
                                小学数学
                            </#if>
                        </p>
                        <p class="grade"><span style="color:#ff7d5a">区域/学校：</span>
                            <#if item.apply.reportLevel == 1>${item.apply.cityName!}/${item.apply.countyName!}
                            <#elseif item.apply.reportLevel == 2>${item.apply.countyName!}
                            <#elseif item.apply.reportLevel == 3>${item.apply.schoolName!}（${item.apply.schoolId!}）
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">时间维度：</span>
                            <#if item.apply.reportType?? && item.apply.reportType == 1>学期报告
                                <#if item.apply.reportTerm?? && item.apply.reportTerm == 1>2016年9-12月
                                <#elseif item.apply.reportTerm?? && item.apply.reportTerm == 2>2017年1-6月
                                </#if>
                            <#elseif item.apply.reportType?? && item.apply.reportType == 2>月度报告
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">月份：</span>
                            <#if item.apply.reportType?? && item.apply.reportType == 2>
                            ${item.apply.reportMonth!''}
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">样本校：</span>
                            <#if item.apply.sampleSchoolName??>
                            ${item.apply.sampleSchoolName!''}(${item.apply.sampleSchoolId!''})
                            <#else>
                                无
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">申请人历史申请记录：</span>
                            共计${historyApplies!'-'}条
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">申请原因：</span>
                            ${item.apply.comment!''}
                        </p>
                    </div>
                </div>
                <div class="adjustmentExamine-box" style="margin-top: .5rem">
                    <p style="font-size:.6rem;color:#898c91;line-height:1rem;height:1rem">审核进度</p>
                    <ul class="schoolClueContent">
                        <#if item.processResultList?? && item.processResultList?size gt 0>
                            <#list item.processResultList as list>
                                <li>
                                    <div>${list.accountName!""}</div>
                                    <div <#if list.result??>style="<#if list.result == "同意">color:#99cc66<#elseif list.result == "驳回" || list.result == "撤销">color:#ff7d5a</#if>"</#if>>${list.result!""}</div>
                                    <div><#if list.processDate??>${list.processDate?string("MM-dd HH:mm")}</#if></div>
                                </li>
                                <#if list.result?? && list.result == "驳回">
                                    <li style="color:#ff7d5a">${list.processNotes!""}</li>
                                </#if>
                            </#list>
                        </#if>
                        <li>
                            <div>${item.apply.accountName!''}</div>
                            <div>发起申请</div>
                            <div><#if item.apply.createDatetime?has_content>${item.apply.createDatetime?string("MM-dd HH:mm")}</#if></div>
                        </li>
                    </ul>
                </div>
            </#if>
        </div>
    <#--已通过-->
    </div>
</div>
</@layout.page>