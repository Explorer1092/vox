<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="字典表调整" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['audit'] />
<div class="crmList-box resources-box">
    <div class="res-top fixed-head">
        <div class="return js-return"><a href="javascript:void(0)"><i class="return-icon"></i>返回</a></div>
        <span class="return-line"></span>
        <span class="res-title">字典表调整</span>
    </div>
    <div class="c-main">
        <div>
            <div class="c-opts gap-line c-flex c-flex-3" style="margin-bottom:.5rem">
                <span class="js-todo">待处理</span>
                <span class="js-done <#if processResultId?? && processResultId == 1>the</#if>" data-index="1">已通过</span>
                <span class="js-done <#if processResultId?? && processResultId == 2>the</#if>" data-index="2">已驳回</span>
            </div>
            <div class="tab-main" style="clear:both">
                <div>
                    <#if dataList??>
                        <#list dataList as item>
                            <div class="adjustmentExamine-box">
                                <div class="adjust-head">
                                    <div class="right">${item.apply.createDatetime?string("MM-dd")}</div>
                                    <div class="name">${item.apply.accountName!''}【<#if item.apply.modifyType?? && item.apply.modifyType == 1>添加学校<#elseif item.apply.modifyType?? && item.apply.modifyType == 2>删除学校<#elseif item.apply.modifyType?? && item.apply.modifyType == 3>业务变更</#if>】</div>
                                </div>
                                <div class="adjust-content">
                                    <p class="title">名称：${item.apply.schoolName!''}(${item.apply.schoolId!0})</p>
                                    <p class="stage">阶段：<#if item.apply.schoolLevel??><#if item.apply.schoolLevel == 1>小学<#elseif item.apply.schoolLevel == 2>中学<#elseif item.apply.schoolLevel == 3>高中<#elseif item.apply.schoolLevel == 5>学前</#if></#if></p>
                                    <p class="area">区域：${item.apply.regionName!''}</p>
                                    <p class="pattern"><span style="display: inline-block;float: left;">模式：</span><span class="modifyDesc" style="display: inline-block">${item.apply.modifyDesc!''}</span></p>
                                    <p class="grade">等级：<#if item.apply.schoolPopularity?has_content><#if item.apply.schoolPopularity == 'A'>名校</#if><#if item.apply.schoolPopularity == 'B'>重点校</#if><#if item.apply.schoolPopularity == 'C'>普通校</#if><#else>重点校</#if></p>
                                    <p class="reason">调整原因：${item.apply.comment!''}</p>
                                    <p class="info">处理意见：${item.processHistory.processNotes!''}</p>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).on('click','.js-done',function(){
        location.href = "/mobile/audit/done_list.vpage?processResult="+$(this).data('index')+"&workflowTypeId=1";
    });
    $(document).on('click','.js-todo',function(){
        location.href = "/mobile/audit/todo_list.vpage?workflowType=1";
    });
    $(document).on('click','.js-return',function(){
        location.href = "/mobile/audit/index.vpage";
    });
    $('.modifyDesc').each(function(){
        var pattern = $(this).html().trim();
        $(this).html(pattern.replace(/\n/g, '<br />'));
    });
</script>
</@layout.page>