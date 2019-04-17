<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="待我审批" pageJs="todo_done" footerIndex=4>
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <#--<div class="res-top fixed-head">-->
        <#--<div class="return js-return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">待我审批</span>-->
    <#--</div>-->
    <div class="c-main">
        <div class="c-opts gap-line c-flex c-flex-2 js-head" style="margin-bottom:.5rem">
            <span class="js-tabItem the">待我审批<#if dataList??>(${dataList?size})</#if></span>
            <span class="js-completed">审批完成</span>
        </div>
        <div>
            <#if dataList?? && dataList?size gt 0>
                <div style="clear:both">
                    <div class="c-list">
                        <#list dataList as item>
                            <div class="adjustmentExamine-box" style="margin-top: .5rem">
                                <div class="adjust-head js-itemBtn1" data-sid="${item.workFlowRecord.id!0}"
                                     data-type="${item.workFlowRecord.workFlowType!""}" style="border:none">
                                    <div class="right">${item.workFlowRecord.createDatetime?string("MM-dd")}</div>
                                    <div>${item.workFlowRecord.taskName!""}</div>
                                    <div>申请人：${item.workFlowRecord.creatorName!""}</div>
                                    <div>${item.workFlowRecord.taskContent!""}</div>
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