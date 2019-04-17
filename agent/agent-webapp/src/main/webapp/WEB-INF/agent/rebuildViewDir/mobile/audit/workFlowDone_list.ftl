<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="审批完成" pageJs="todo_done" footerIndex=4>
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <#--<div class="res-top fixed-head">-->
        <#--<div class="return js-return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">审批完成</span>-->
    <#--</div>-->
    <div class="c-main">
        <div class="c-opts gap-line c-flex c-flex-2 js-head" style="margin-bottom:.5rem">
            <span class="js-pending">待我审批</span>
            <span class="js-tabItem the">审批完成</span>
        </div>
        <div>
            <#if dataList?? && dataList?size gt 0>
                <div style="clear:both">
                    <div class="c-list">
                        <#list dataList as item>
                            <div class="adjustmentExamine-box" style="margin-top: .5rem">
                                <div class="adjust-head js-itemBtn2" data-sid="${item.workFlowRecord.id!0}"
                                     data-type="${item.workFlowRecord.workFlowType!""}" style="border:none">
                                    <div class="right">${item.processHistory.createDatetime?string("MM-dd")}</div>
                                    <div>${item.workFlowRecord.taskName!""}</div>
                                    <div>申请人：${item.workFlowRecord.creatorName!""}</div>
                                    <div>${item.workFlowRecord.taskContent!""}</div>
                                    <div style="margin-top:.5rem;font-size: .6rem;"><#if item.processHistory.result??><#if item.processHistory.result = "agree"><span style="color:#99cc66">我已通过</span><#elseif item.processHistory.result = "reject"><span style="color: #ff7d5a">被我驳回<br>驳回原因：${item.processHistory.processNotes!""}</span></#if></#if></div>
                                </div>
                            </div>
                        </#list>
                    </div>
                </div>
            </#if>
        </div>
    </div>
</div>
</@layout.page>
