<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="字典表调整" pageJs="" footerIndex=4>
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <div class="tab-main" style="clear:both">
    <#--待审核-->
        <div>
            <#if applyData?? && applyData.apply??>
                <#assign item = applyData>
                <div class="adjustmentExamine-box" style="margin-top: .5rem">
                    <div class="adjust-content">
                        <p class="title"><span style="color:#ff7d5a">调整类别：</span><#if item.apply.modifyType?? && item.apply.modifyType == 1>添加学校<#elseif item.apply.modifyType?? && item.apply.modifyType == 2>删除学校<#elseif item.apply.modifyType?? && item.apply.modifyType == 3>业务变更</#if></p>
                        <p class="title"><span style="color:#ff7d5a">学校Id：</span>${item.apply.schoolId!0}</p>
                        <p class="title"><span style="color:#ff7d5a">名称：</span>${item.apply.schoolName!''}</p>
                        <p class="area"><span style="color:#ff7d5a">区域：</span>${item.apply.regionName!''}</p>
                        <p class="stage"><span style="color:#ff7d5a">阶段：</span><#if item.apply.schoolLevel??><#if item.apply.schoolLevel == 1>小学<#elseif item.apply.schoolLevel == 2>中学<#elseif item.apply.schoolLevel == 3>高中<#elseif item.apply.schoolLevel == 5>学前</#if></#if>
                        </p>
                        <p class="pattern"><span style="display: inline-block;color:#ff7d5a;float: left;">模式：</span><span class="modifyDesc" style="display: inline-block;font-size:.5rem">${item.apply.modifyDesc!''}</span></p>
                        <p class="grade"><span style="color:#ff7d5a">等级：</span><#if item.apply.schoolPopularity?has_content><#if item.apply.schoolPopularity == 'A'>名校</#if><#if item.apply.schoolPopularity == 'B'>重点校</#if><#if item.apply.schoolPopularity == 'C'>普通校</#if><#else>重点校</#if></p>
                        <p class="reason"><span style="color:#ff7d5a">调整原因：</span>${item.apply.comment!''}</p>
                    <#--<p class="info">处理意见：情况属实，同意</p>-->
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